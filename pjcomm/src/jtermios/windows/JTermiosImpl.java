/*
 * Copyright (c) 2011, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its
 * contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package jtermios.windows;

import static jtermios.JTermios.*;
import static jtermios.JTermios.JTermiosLogging.log;
import static jtermios.windows.WinAPI.*;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import jtermios.*;
import jtermios.windows.WinAPI.HANDLE;
import purejavacomm.SerialPortMode;

import com.sun.jna.WString;


public class JTermiosImpl implements jtermios.JTermios.JTermiosInterface {

	private volatile int m_ErrNo = 0;
	volatile boolean m_PortFDs[] = new boolean[FDSetImpl.FD_SET_SIZE];
	volatile Hashtable<Integer, Port> m_OpenPorts = new Hashtable<>();

	static private class FDSetImpl extends FDSet {
		static final int FD_SET_SIZE = 256; // Windows supports max 255 serial ports so this is enough
		static final int NFBBITS = 32;
		int[] bits = new int[(FD_SET_SIZE + NFBBITS - 1) / NFBBITS];
	}

	public JTermiosImpl() {
		log = log && log(1, "instantiating %s\n", getClass().getCanonicalName());
	}

	@Override
	public int errno() {
		return this.m_ErrNo;
	}

	@Override
	public void cfmakeraw(Termios termios) {
		termios.c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL | IXON);
		termios.c_oflag &= ~OPOST;
		termios.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
		termios.c_cflag &= ~(CSIZE | PARENB);
		termios.c_cflag |= CS8;
	}

	@Override
	public int fcntl(int fd, int cmd, int arg) {

		Port port = getPort(fd);
		if (port == null) {
			return -1;
		}
		if (F_SETFL == cmd) {
			port.m_OpenFlags = arg;
		} else if (F_GETFL == cmd) {
			return port.m_OpenFlags;
		} else {
			this.m_ErrNo = ENOTSUP;
			return -1;
		}
		return 0;
	}

	@Override
	public int tcdrain(int fd) {

		final Port port = getPort(fd);

		if (port == null) {
			return -1;
		}
		try {
			synchronized (port.m_WrBuffer) {
				writePending(port);
				if (!FlushFileBuffers(port.m_Comm)) {
					port.fail();
				}
				return 0;
			}
		} catch (Fail f) {
			return -1;
		}
	}

	@Override
	public int cfgetispeed(Termios termios) {
		return termios.c_ispeed;
	}

	@Override
	public int cfgetospeed(Termios termios) {
		return termios.c_ospeed;
	}

	@Override
	public int cfsetispeed(Termios termios, int speed) {
		termios.c_ispeed = speed;
		return 0;
	}// Error code for Interrupted = EINTR

	@Override
	public int cfsetospeed(Termios termios, int speed) {
		termios.c_ospeed = speed;
		return 0;
	}

	@Override
	public boolean opensWithMode() {
		return true;
	}

	@Override
	public int open(
			String filename,
			SerialPortMode serialPortMode,
			int flags) {

		Port port = null;

		try {
			port = new Port(this);
			port.m_OpenFlags = flags;
			if (!filename.startsWith("\\\\")) {
				filename = "\\\\.\\" + filename;
			}

			port.m_Comm = CreateFileW(
					new WString(filename),
					GENERIC_READ | GENERIC_WRITE,
					0,
					null,
					OPEN_EXISTING,
					FILE_FLAG_OVERLAPPED,
					null);

			if (INVALID_HANDLE_VALUE == port.m_Comm) {
				if (GetLastError() == ERROR_FILE_NOT_FOUND) {
					this.m_ErrNo = ENOENT;
				} else {
					this.m_ErrNo = EBUSY;
				}
				port.fail();
			}

			final int baudRate =
					supportedBaudRate(serialPortMode.getBaudRate());

			cfmakeraw(port.m_Termios);
			cfsetispeed(port.m_Termios, baudRate);
			cfsetospeed(port.m_Termios, baudRate);
			port.m_Termios.c_cc[VTIME] = 0;
			port.m_Termios.c_cc[VMIN] = 0;
			port.m_Termios.c_cc[VSTART] = (byte) DC1;
			port.m_Termios.c_cc[VSTOP] = (byte) DC3;
			port.m_Termios.setFlowControlMode(
					serialPortMode.getFlowControlMode());
			port.m_Termios.setDataFormat(serialPortMode.getParams());
			port.updateFromTermios();

			if (!SetupComm(
					port.m_Comm,
					(int) port.m_RdBuffer.size(),
					(int) port.m_WrBuffer.size())) {
				port.fail(); // FIXME what would be appropriate error code here
			}

			port.m_RdOVL.writeField(
					"hEvent",
					CreateEventA(null, true, false, null));
			if (port.m_RdOVL.hEvent == INVALID_HANDLE_VALUE) {
				port.fail();
			}

			port.m_WrOVL.writeField(
					"hEvent",
					CreateEventA(null, true, false, null));
			if (port.m_WrOVL.hEvent == INVALID_HANDLE_VALUE) {
				port.fail();
			}

			port.m_SelOVL.writeField(
					"hEvent",
					CreateEventA(null, true, false, null));
			if (port.m_SelOVL.hEvent == INVALID_HANDLE_VALUE) {
				port.fail();
			}

			return port.m_FD;
		} catch (Exception f) {
			if (port != null) {
				port.close();
			}
			return -1;
		}

	}

	private static void nanoSleep(long nsec) throws Fail {
		try {
			Thread.sleep((int) (nsec / 1000000), (int) (nsec % 1000000));
		} catch (InterruptedException ie) {
			throw new Fail();
		}
	}

	private int getCharBits(Termios tios) {
		int cs = 8; // default to 8
		if ((tios.c_cflag & CSIZE) == CS5) {
			cs = 5;
		}
		if ((tios.c_cflag & CSIZE) == CS6) {
			cs = 6;
		}
		if ((tios.c_cflag & CSIZE) == CS7) {
			cs = 7;
		}
		if ((tios.c_cflag & CSIZE) == CS8) {
			cs = 8;
		}
		if ((tios.c_cflag & CSTOPB) != 0)
		 {
			cs++; // extra stop bit
		}
		if ((tios.c_cflag & PARENB) != 0)
		 {
			cs++; // parity adds an other bit
		}
		cs += 1 + 1; // start bit + stop bit
		return cs;
	}

	private static int min(int a, int b) {
		return a < b ? a : b;
	}

	private static int max(int a, int b) {
		return a > b ? a : b;
	}

	@Override
	public int read(int fd, byte[] buffer, int length) {

		Port port = getPort(fd);
		if (port == null) {
			return -1;
		}
		synchronized (port.m_RdBuffer) {
			try {
				// limit reads to internal buffer size
				if (length > port.m_RdBuffer.size()) {
					length = (int) port.m_RdBuffer.size();
				}

				if (length == 0) {
					return 0;
				}

				int error;

				if ((port.m_OpenFlags & O_NONBLOCK) != 0) {
					clearCommErrors(port);
					int available = port.m_COMSTAT.cbInQue;
					if (available == 0) {
						this.m_ErrNo = EAGAIN;
						return -1;
					}
					length = min(length, available);
				} else {
					clearCommErrors(port);
					int available = port.m_COMSTAT.cbInQue;
					int vtime = 0xff & port.m_Termios.c_cc[VTIME];
					int vmin = 0xff & port.m_Termios.c_cc[VMIN];

					if (vmin == 0 && vtime == 0) {
						// VMIN = 0 and VTIME = 0 => totally non blocking,if data is
						// available, return it, ie this is poll operation
						// For reference below commented out is how timeouts are set for this vtime/vmin combo
						//touts.ReadIntervalTimeout = MAXDWORD;
						//touts.ReadTotalTimeoutConstant = 0;
						//touts.ReadTotalTimeoutMultiplier = 0;
						if (available == 0) {
							return 0;
						}
						length = min(length, available);
					}
					if (vmin == 0 && vtime > 0) {
						// VMIN = 0 and VTIME > 0 => timed read, return as soon as data is
						// available, VTIME = total time
						// For reference below commented out is how timeouts are set for this vtime/vmin combo
						//touts.ReadIntervalTimeout = 0;
						//touts.ReadTotalTimeoutConstant = vtime;
						//touts.ReadTotalTimeoutMultiplier = 0;

						// NOTE to behave like unix we should probably wait until there is something available
						// and then try to do a read as many bytes as are available bytes at that point in time.
						// As this is coded now, this will attempt to read as many bytes as requested and this may end up
						// spending vtime in the read when a unix would return less bytes but as soon as they become
						// available.
					}
					if (vmin > 0 && vtime > 0) {
						// VMIN > 0 and VTIME > 0 => blocks until VMIN chars has arrived or  between chars expired,
						// note that this will block if nothing arrives
						// For reference below commented out is how timeouts are set for this vtime/vmin combo
						//touts.ReadIntervalTimeout = vtime;
						//touts.ReadTotalTimeoutConstant = 0;
						//touts.ReadTotalTimeoutMultiplier = 0;
						length = min(max(vmin, available), length);
					}
					if (vmin > 0 && vtime == 0) {
						// VMIN > 0 and VTIME = 0 => blocks until VMIN characters have been
						// received
						// For reference below commented out is how timeouts are set for this vtime/vmin combo
						//touts.ReadIntervalTimeout = 0;
						//touts.ReadTotalTimeoutConstant = 0;
						//touts.ReadTotalTimeoutMultiplier = 0;
						length = min(max(vmin, available), length);
					}

				}

				if (!ResetEvent(port.m_RdOVL.hEvent)) {
					port.fail();
				}

				if (!ReadFile(port.m_Comm, port.m_RdBuffer, length, port.m_RdN, port.m_RdOVL)) {
					if (GetLastError() != ERROR_IO_PENDING) {
						port.fail();
					}
					if (WaitForSingleObject(port.m_RdOVL.hEvent, INFINITE) != WAIT_OBJECT_0) {
						port.fail();
					}
					if (!GetOverlappedResult(port.m_Comm, port.m_RdOVL, port.m_RdN, true)) {
						port.fail();
					}
				}

				port.m_RdBuffer.read(0, buffer, 0, port.m_RdN[0]);
				return port.m_RdN[0];
			} catch (Fail ie) {
				return -1;
			}
		}
	}

	@Override
	public int write(int fd, byte[] buffer, int length) {

		int len = length;
		final Port port = getPort(fd);

		if (port == null) {
			return -1;
		}

		synchronized (port.m_WrBuffer) {
			try {
				writePending(port);
				if ((port.m_OpenFlags & O_NONBLOCK) != 0) {
					if (!ClearCommError(
							port.m_Comm,
							port.m_WrErr,
							port.m_WrStat)) {
						port.fail();
					}

					final int room =
							(int) port.m_WrBuffer.size()
							- port.m_WrStat.cbOutQue;

					if (len > room) {
						len = room;
					}
				}
				if (!ResetEvent(port.m_WrOVL.hEvent)) {
					port.fail();
				}
				if (len > port.m_WrBuffer.size()) {
					len = (int) port.m_WrBuffer.size();
				}
				// copy from buffer to Memory
				port.m_WrBuffer.write(0, buffer, 0, len);

				final boolean ok = WriteFile(
						port.m_Comm,
						port.m_WrBuffer,
						len,
						port.m_WrN,
						port.m_WrOVL);

				if (!ok) {
					if (GetLastError() != ERROR_IO_PENDING) {
						port.fail();
					}
					port.m_WritePending = len;
				}
				//
				return len; // port.m_WrN[0];
			} catch (Fail f) {
				return -1;
			}
		}
	}

	private void writePending(Port port) throws Fail {
		if (port.m_WritePending <= 0) {
			return;
		}
		while (true) {

			final int res = WaitForSingleObject(
					port.m_WrOVL.hEvent,
					INFINITE);

			if (res == WAIT_TIMEOUT) {
				clearCommErrors(port);
				log = log && log(
						1,
						"write pending, cbInQue %d cbOutQue %d\n",
						port.m_COMSTAT.cbInQue,
						port.m_COMSTAT.cbOutQue);
				continue;
			}
			if (!GetOverlappedResult(
					port.m_Comm,
					port.m_WrOVL,
					port.m_WrN,
					false)) {
				port.fail();
			}
			if (port.m_WrN[0] != port.m_WritePending) {
				new RuntimeException(
						"Windows OVERLAPPED WriteFile failed "
						+ "to write all, tried to write "
						+ port.m_WritePending + " but got "
						+ port.m_WrN[0]);
			}
			break;
		}
		port.m_WritePending = 0;
	}

	@Override
	public int close(int fd) {

		final Port port = getPort(fd);

		if (port == null) {
			return -1;
		}

		port.close();

		return 0;
	}

	@Override
	public int tcflush(int fd, int queue) {

		final Port port = getPort(fd);

		if (port == null) {
			return -1;
		}
		try {
			if (queue == TCIFLUSH) {
				if (!PurgeComm(port.m_Comm, PURGE_RXABORT)) {
					port.fail();
				}
			} else if (queue == TCOFLUSH) {
				if (!PurgeComm(port.m_Comm, PURGE_TXABORT)) {
					port.fail();
				}
			} else if (queue == TCIOFLUSH) {
				if (!PurgeComm(port.m_Comm, PURGE_TXABORT)) {
					port.fail();
				}
				if (!PurgeComm(port.m_Comm, PURGE_RXABORT)) {
					port.fail();
				}
			} else {
				this.m_ErrNo = ENOTSUP;
				return -1;
			}

			return 0;
		} catch (Fail f) {
			return -1;
		}
	}

	/*
	 * (non-Javadoc) Basically this is wrong, as tcsetattr is supposed to set
	 * only those things it can support and tcgetattr is the used to see that
	 * what actually happened. In this instance tcsetattr never fails and
	 * tcgetattr always returns the last settings even though it possible (even
	 * likely) that tcsetattr was not able to carry out all settings, as there
	 * is no 1:1 mapping between Windows Comm API and posix/termios API.
	 *
	 * @see jtermios.JTermios.JTermiosInterface#tcgetattr(int, jtermios.Termios)
	 */

	@Override
	public int tcgetattr(int fd, Termios termios) {
		Port port = getPort(fd);
		if (port == null) {
			return -1;
		}
		termios.set(port.m_Termios);
		return 0;
	}

	@Override
	public int tcsendbreak(int fd, int duration) {
		Port port = getPort(fd);
		if (port == null) {
			return -1;
		}
		try {
			if (!SetCommBreak(port.m_Comm)) {
				port.fail();
			}
			nanoSleep(duration * 250000000L);
			if (!ClearCommBreak(port.m_Comm)) {
				port.fail();
			}
			return 0;
		} catch (Fail f) {
			return -1;
		}
	}

	@Override
	public int tcsetattr(int fd, int cmd, Termios termios) {
		if (cmd != TCSANOW) {
			log(0, "tcsetattr only supports TCSANOW\n");
		}

		final Port port = getPort(fd);

		if (port == null) {
			return -1;
		}

		synchronized (port.m_Termios) {
			try {
				port.m_Termios.set(termios);
				port.updateFromTermios();
				return 0;
			} catch (Fail f) {
				return -1;
			}
		}
	}

	private int maskToFDSets(
			Port port,
			FDSet readfds,
			FDSet writefds,
			FDSet exceptfds,
			int ready)
	throws Fail {
		clearCommErrors(port);
		int emask = port.m_EventFlags.getValue();
		int fd = port.m_FD;
		if ((emask & EV_RXCHAR) != 0 && port.m_COMSTAT.cbInQue > 0) {
			FD_SET(fd, readfds);
			ready++;
		}
		if ((emask & EV_TXEMPTY) != 0 && port.m_COMSTAT.cbOutQue == 0) {
			FD_SET(fd, writefds);
			ready++;
		}
		return ready;
	}

	private void clearCommErrors(Port port) throws Fail {
		synchronized (port.m_COMSTAT) {
			if (!ClearCommError(
					port.m_Comm,
					port.m_ClearErr,
					port.m_COMSTAT)) {
				port.fail();
			}
		}
	}

	@Override
	public int select(
			int n,
			FDSet readfds,
			FDSet writefds,
			FDSet exceptfds,
			TimeVal timeout) {

		// long T0 = System.currentTimeMillis();
		int ready = 0;
		final LinkedList<Port> locked = new LinkedList<>();

		try {
			try {

				final LinkedList<Port> waiting = new LinkedList<>();

				for (int fd = 0; fd < n; fd++) {
					boolean rd = FD_ISSET(fd, readfds);
					boolean wr = FD_ISSET(fd, writefds);
					FD_CLR(fd, readfds);
					FD_CLR(fd, writefds);
					if (rd || wr) {
						Port port = getPort(fd);
						if (port == null) {
							return -1;
						}
						try {
							port.lock();
							locked.add(port);
							clearCommErrors(port);

							// check if there is data to be read, as WaitCommEvent
							// does check for only *new* data that and thus
							// might wait indefinitely if select() is called twice
							// without first reading away all data

							if (rd && port.m_COMSTAT.cbInQue > 0) {
								FD_SET(fd, readfds);
								ready++;
							}

							if (wr && port.m_COMSTAT.cbOutQue == 0) {
								FD_SET(fd, writefds);
								ready++;
							}

							int flags = 0;
							if (rd) {
								flags |= EV_RXCHAR;
							}
							if (wr) {
								flags |= EV_TXEMPTY;
							}

							int[] currentMask = { 0 };
							if (!GetCommMask(port.m_Comm, currentMask)) {
								port.fail();
							}

							// check if there is no pending WaitCommEvent operation
							// pointing to port.m_SelOVL OVERLAPPED structure
							// or if event flags have changed
							// before starting a new WaitCommEvent operation
							// pointing to the same OVERLAPPED structure

							boolean startWaitCommEvent = true;
							if (currentMask[0] == flags) {
								GetOverlappedResult(port.m_Comm, port.m_SelOVL, port.m_SelN, false);
								int err = GetLastError();
								startWaitCommEvent = (err != ERROR_IO_INCOMPLETE && err != ERROR_IO_PENDING);
							} else {
								if (!SetCommMask(port.m_Comm, flags)) {
									port.fail();
								}
							}
							if (startWaitCommEvent) {
								if (!ResetEvent(port.m_SelOVL.hEvent)) {
									port.fail();
								}
								if (WaitCommEvent(port.m_Comm, port.m_EventFlags, port.m_SelOVL)) {
									if (!GetOverlappedResult(port.m_Comm, port.m_SelOVL, port.m_SelN, false)) {
										port.fail();
									}
									// actually it seems that overlapped
									// WaitCommEvent never returns true so we never get here
									ready = maskToFDSets(port, readfds, writefds, exceptfds, ready);
								} else {
									// FIXME if the port dies on us what happens
									if (GetLastError() != ERROR_IO_PENDING) {
										port.fail();
									}
									waiting.add(port);
								}
							}
						} catch (InterruptedException ie) {
							this.m_ErrNo = EINTR;
							return -1;
						}

					}
				}
				if (ready == 0) {
					int waitn = waiting.size();
					if (waitn > 0) {
						HANDLE[] wobj = new HANDLE[waiting.size() * 2];
						int i = 0;
						for (Port port : waiting) {
							wobj[i++] = port.m_SelOVL.hEvent;
							wobj[i++] = port.m_CancelWaitSema4;
						}
						int tout = timeout != null ? (int) (timeout.tv_sec * 1000 + timeout.tv_usec / 1000) : INFINITE;
						// int res = WaitForSingleObject(wobj[0], tout);
						int res = WaitForMultipleObjects(waitn * 2, wobj, false, tout);

						if (res == WAIT_TIMEOUT) {
							// work around the fact that sometimes we miss
							// events
							for (Port port : waiting) {
								clearCommErrors(port);
								int[] mask = { 0 };

								if (!GetCommMask(port.m_Comm, mask)) {
									port.fail();
								}
								if (port.m_COMSTAT.cbInQue > 0 && ((mask[0] & EV_RXCHAR) != 0)) {
									FD_SET(port.m_FD, readfds);
									log = log && log(1, "missed EV_RXCHAR event\n");
									return 1;
								}
								if (port.m_COMSTAT.cbOutQue == 0 && ((mask[0] & EV_TXEMPTY) != 0)) {
									FD_SET(port.m_FD, writefds);
									log = log && log(1, "missed EV_TXEMPTY event\n");
									return 1;
								}
							}

						}
						if (res != WAIT_TIMEOUT) {
							i = (res - WAIT_OBJECT_0) / 2;
							if (i < 0 || i >= waitn) {
								throw new Fail();
							}

							Port port = waiting.get(i);
							if (!GetOverlappedResult(port.m_Comm, port.m_SelOVL, port.m_SelN, false)) {
								port.fail();
							}

							ready = maskToFDSets(port, readfds, writefds, exceptfds, ready);
						}
					} else {
						if (timeout != null) {
							nanoSleep(timeout.tv_sec * 1000000000L + timeout.tv_usec * 1000);
						} else {
							this.m_ErrNo = EINVAL;
							return -1;
						}
						return 0;
					}
				}
			} catch (Fail f) {
				return -1;
			}
		} finally {
			for (Port port : locked) {
				port.unlock();
			}

		}
		// long T1 = System.currentTimeMillis();
		// System.err.println("select() " + (T1 - T0));

		return ready;
	}

	@Override
	public int poll(Pollfd fds[], int nfds, int timeout) {
		this.m_ErrNo = EINVAL;
		return -1;
	}

	@Override
	public int poll(int fds[], int nfds, int timeout) {
		this.m_ErrNo = EINVAL;
		return -1;
	}

	@Override
	public void perror(String msg) {
		if (msg != null && msg.length() > 0) {
			System.out.print(msg + ": ");
		}
		System.out.printf("%d\n", this.m_ErrNo);
	}

	// This is a bit pointless function as Windows baudrate constants are
	// just the baudrates so basically this is a no-op, it returns what it gets
	// Note this assumes that the Bxxxx constants in JTermios have the default
	// values ie the values are the baudrates.
	private static int baudToDCB(int baud) {
		switch (baud) {
			case 110:
				return CBR_110;
			case 300:
				return CBR_300;
			case 600:
				return CBR_600;
			case 1200:
				return CBR_1200;
			case 2400:
				return CBR_2400;
			case 4800:
				return CBR_4800;
			case 9600:
				return CBR_9600;
			case 14400:
				return CBR_14400;
			case 19200:
				return CBR_19200;
			case 38400:
				return CBR_38400;
			case 57600:
				return CBR_57600;
			case 115200:
				return CBR_115200;
			case 128000:
				return CBR_128000;
			case 256000:
				return CBR_256000;

			default:
				return baud;
		}
	}

	@Override
	public FDSet newFDSet() {
		return new FDSetImpl();
	}

	@Override
	public void FD_CLR(int fd, FDSet set) {
		if (set == null) {
			return;
		}
		FDSetImpl p = (FDSetImpl) set;
		p.bits[fd / FDSetImpl.NFBBITS] &= ~(1 << (fd % FDSetImpl.NFBBITS));
	}

	@Override
	public boolean FD_ISSET(int fd, FDSet set) {
		if (set == null) {
			return false;
		}
		FDSetImpl p = (FDSetImpl) set;
		return (p.bits[fd / FDSetImpl.NFBBITS] & (1 << (fd % FDSetImpl.NFBBITS))) != 0;
	}

	@Override
	public void FD_SET(int fd, FDSet set) {
		if (set == null) {
			return;
		}
		FDSetImpl p = (FDSetImpl) set;
		p.bits[fd / FDSetImpl.NFBBITS] |= 1 << (fd % FDSetImpl.NFBBITS);
	}

	@Override
	public void FD_ZERO(FDSet set) {
		if (set == null) {
			return;
		}
		FDSetImpl p = (FDSetImpl) set;
		java.util.Arrays.fill(p.bits, 0);
	}

	@Override
	public int ioctl(int fd, int cmd, int[] arg) {
		Port port = getPort(fd);
		if (port == null) {
			return -1;
		}
		try {
			if (cmd == FIONREAD) {
				clearCommErrors(port);
				arg[0] = port.m_COMSTAT.cbInQue;
				return 0;
			} else if (cmd == TIOCMSET) {
				int a = arg[0];
				if ((a & TIOCM_DTR) != 0) {
					port.MSR |= TIOCM_DTR;
				} else {
					port.MSR &= ~TIOCM_DTR;
				}

				if (!EscapeCommFunction(port.m_Comm, ((a & TIOCM_DTR) != 0) ? SETDTR : CLRDTR)) {
					port.fail();
				}

				if ((a & TIOCM_RTS) != 0) {
					port.MSR |= TIOCM_RTS;
				} else {
					port.MSR &= ~TIOCM_RTS;
				}
				if (!EscapeCommFunction(port.m_Comm, ((a & TIOCM_RTS) != 0) ? SETRTS : CLRRTS)) {
					port.fail();
				}
				return 0;
			} else if (cmd == TIOCMGET) {
				int[] stat = { 0 };
				if (!GetCommModemStatus(port.m_Comm, stat)) {
					port.fail();
				}
				int s = stat[0];
				int a = arg[0];
				if ((s & MS_RLSD_ON) != 0) {
					a |= TIOCM_CAR;
				} else {
					a &= ~TIOCM_CAR;
				}
				if ((s & MS_RING_ON) != 0) {
					a |= TIOCM_RNG;
				} else {
					a &= ~TIOCM_RNG;
				}
				if ((s & MS_DSR_ON) != 0) {
					a |= TIOCM_DSR;
				} else {
					a &= ~TIOCM_DSR;
				}
				if ((s & MS_CTS_ON) != 0) {
					a |= TIOCM_CTS;
				} else {
					a &= ~TIOCM_CTS;
				}

				if ((port.MSR & TIOCM_DTR) != 0) {
					a |= TIOCM_DTR;
				} else {
					a &= ~TIOCM_DTR;
				}
				if ((port.MSR & TIOCM_RTS) != 0) {
					a |= TIOCM_RTS;
				} else {
					a &= ~TIOCM_RTS;
				}
				arg[0] = a;

				return 0;
			} else {
				this.m_ErrNo = ENOTSUP;
				return -1;
			}
		} catch (Fail f) {
			return -1;
		}
	}

	private void set_errno(int x) {
		this.m_ErrNo = x;
	}

	private void report(String msg) {
		System.err.print(msg);
	}

	private Port getPort(int fd) {
		synchronized (this) {
			Port port = this.m_OpenPorts.get(fd);
			if (port == null) {
				this.m_ErrNo = EBADF;
			}
			return port;
		}
	}

	private static String getString(char[] buffer, int offset) {
		StringBuffer s = new StringBuffer();
		char c;
		while ((c = buffer[offset++]) != 0) {
			s.append(c);
		}
		return s.toString();
	}

	@Override
	public String getPortNamePattern() {
		return "^COM.*";
	}

	@Override
	public List<String> getPortList() {
		Pattern p = JTermios.getPortNamePattern(this);
		char[] buffer;
		int size = 0;
		for (size = 16 * 1024; size < 256 * 1024; size *= 2) {
			buffer = new char[size];
			int res = QueryDosDeviceW(null, buffer, buffer.length);
			if (res > 0) { //
				LinkedList<String> list = new LinkedList<>();
				int offset = 0;
				String port;
				while ((port = getString(buffer, offset)).length() > 0) {
					if (p.matcher(port).matches()) {
						list.add(port);
					}

					offset += port.length() + 1;
				}
				return list;
			}

			final int err = GetLastError();

			if (err != ERROR_INSUFFICIENT_BUFFER) {
				log = log && log(
						1,
						"QueryDosDeviceW() failed with GetLastError()"
						+ " = %d\n",
						err);
				return null;
			}
		}
		log = log && log(
				1,
				"Repeated QueryDosDeviceW() calls failed "
				+ "up to buffer size %d\n",
				size);
		return null;
	}

	@Override
	public void shutDown() {
		for (Port port : this.m_OpenPorts.values()) {
			try {
				log = log && log(1, "shutDown() closing port %d\n", port.m_FD);
				port.close();
			} catch (Exception e) {
				// should never happen
				e.printStackTrace();
			}
		}
	}

	@Override
	public int setspeed(int fd, Termios termios, int speed) {

		final int br = supportedBaudRate(speed);
		int r;

		if ((r = cfsetispeed(termios, br)) != 0) {
			return r;
		}
		if ((r = cfsetospeed(termios, br)) != 0) {
			return r;
		}
		if ((r = tcsetattr(fd, TCSANOW, termios)) != 0) {
			return r;
		}

		return 0;
	}

	private static int supportedBaudRate(int speed) {
		switch (speed) {
		case 50:
			return B50;
		case 75:
			return B75;
		case 110:
			return B110;
		case 134:
			return B134;
		case 150:
			return B150;
		case 200:
			return B200;
		case 300:
			return B300;
		case 600:
			return B600;
		case 1200:
			return B1200;
		case 1800:
			return B1800;
		case 2400:
			return B2400;
		case 4800:
			return B4800;
		case 9600:
			return B9600;
		case 19200:
			return B19200;
		case 38400:
			return B38400;
		case 7200:
			return B7200;
		case 14400:
			return B14400;
		case 28800:
			return B28800;
		case 57600:
			return B57600;
		case 76800:
			return B76800;
		case 115200:
			return B115200;
		case 230400:
			return B230400;
		}
		return speed;
	}

	@Override
	public int pipe(int[] fds) {
		this.m_ErrNo = EMFILE; // pipe() not implemented on Windows backend
		return -1;
	}

}
