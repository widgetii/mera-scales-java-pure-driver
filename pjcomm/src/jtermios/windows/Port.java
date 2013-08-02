package jtermios.windows;

import static jtermios.JTermios.*;
import static jtermios.JTermios.JTermiosLogging.lineno;
import static jtermios.JTermios.JTermiosLogging.log;
import static jtermios.windows.WinAPI.*;
import static jtermios.windows.WinAPI.DCB.*;
import jtermios.Termios;
import jtermios.windows.WinAPI.COMMTIMEOUTS;
import jtermios.windows.WinAPI.COMSTAT;
import jtermios.windows.WinAPI.DCB;
import jtermios.windows.WinAPI.HANDLE;
import jtermios.windows.WinAPI.OVERLAPPED;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;


final class Port {

	private final JTermiosImpl jtios;
	volatile int m_FD = -1;
	volatile boolean m_Locked;
	volatile HANDLE m_Comm;
	volatile int m_OpenFlags;
	volatile DCB m_DCB = new DCB();
	volatile COMMTIMEOUTS m_Timeouts = new COMMTIMEOUTS();
	volatile COMSTAT m_COMSTAT = new COMSTAT();
	volatile int[] m_ClearErr = { 0 };
	volatile Memory m_RdBuffer = new Memory(2048);
	volatile int[] m_RdErr = { 0 };
	volatile int m_RdN[] = { 0 };
	volatile OVERLAPPED m_RdOVL = new OVERLAPPED();
	volatile Memory m_WrBuffer = new Memory(2048);
	volatile COMSTAT m_WrStat = new COMSTAT();
	volatile int[] m_WrErr = { 0 };
	volatile int m_WrN[] = { 0 };
	volatile int m_WritePending;
	volatile OVERLAPPED m_WrOVL = new OVERLAPPED();
	volatile int m_SelN[] = { 0 };
	volatile HANDLE m_CancelWaitSema4;
	volatile OVERLAPPED m_SelOVL = new OVERLAPPED();
	volatile IntByReference m_EventFlags = new IntByReference();
	final Termios m_Termios = new Termios();
	volatile int MSR; // initial value
	// these cached values are used to detect changes
	// in termios structure and speed up things by avoiding unnecessary updates
	volatile int m_VTIME = -1;
	volatile int m_VMIN = -1;
	volatile int m_c_speed = -1;
	volatile int m_c_cflag = -1;
	volatile int m_c_iflag = -1;
	volatile int m_c_oflag = -1;

	Port(JTermiosImpl jtios) {
		this.jtios = jtios;
		synchronized (this.jtios) {
			this.m_FD = -1;
			for (int i = 0; i < this.jtios.m_PortFDs.length; ++i) {
				if (this.jtios.m_PortFDs[i]) {
					continue;
				}
				this.m_FD = i;
				this.jtios.m_PortFDs[i] = true;
				this.jtios.m_OpenPorts.put(this.m_FD, this);
				this.m_CancelWaitSema4 =
						CreateEventA(null, false, false, null);
				if (this.m_CancelWaitSema4 == null) {
					throw new RuntimeException(
							"Unexpected failure of CreateEvent() call");
				}
				return;
			}
			throw new RuntimeException("Too many ports open");
		}
	}

	public synchronized void fail() throws Fail {

		int err = GetLastError();
		Memory buffer = new Memory(2048);

		FormatMessageW(
				FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
				null,
				err,
				MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
				buffer,
				(int) buffer.size(),
				null);

		log = log && log(
				1,
				"fail() %s, Windows GetLastError()= %d, %s\n",
				lineno(1),
				err,
				buffer.getString(0, true));

		// FIXME here convert from Windows error code to 'posix' error code

		Fail f = new Fail();
		throw f;
	}

	public synchronized void lock() throws InterruptedException {
		while (this.m_Locked) {
			wait();
		}
		this.m_Locked = true;
	}

	public synchronized void unlock() {
		if (!this.m_Locked) {
			throw new IllegalArgumentException("Port was not locked");
		}
		this.m_Locked = false;
		notifyAll();
	}

	public synchronized void waitUnlock() {
		while (this.m_Locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				// interruption cannot cancel wait
			}
		}
	}

	public int updateFromTermios() throws Fail {

		final Termios tios = this.m_Termios;
		int c_speed = tios.c_ospeed;
		int c_cflag = tios.c_cflag;
		int c_iflag = tios.c_iflag;
		int c_oflag = tios.c_oflag;

		if (c_speed != this.m_c_speed
				|| c_cflag != this.m_c_cflag
				|| c_iflag != this.m_c_iflag
				|| c_oflag != this.m_c_oflag) {

			final DCB dcb = this.m_DCB;

			if (!GetCommState(this.m_Comm, dcb)) {
				fail();
			}

			dcb.DCBlength = dcb.size();
			dcb.BaudRate = c_speed;
			if (tios.c_ospeed != tios.c_ispeed) {
				log(
						0,
						"c_ospeed (%d) != c_ispeed (%d)\n",
						tios.c_ospeed,
						tios.c_ispeed);
			}

			int flags = 0;

			// rxtx does: if ( s_termios->c_iflag & ISTRIP ) dcb.fBinary = FALSE;
			// but Winapi doc says fBinary always true
			flags |= fBinary;
			if ((c_cflag & PARENB) != 0) {
				flags |= fParity;
			}

			if ((c_iflag & IXON) != 0) {
				flags |= fOutX;
			}
			if ((c_iflag & IXOFF) != 0) {
				flags |= fInX;
			}
			if ((c_iflag & IXANY) != 0) {
				flags |= fTXContinueOnXoff;
			}

			if ((c_iflag & CRTSCTS) != 0) {
				flags |= fRtsControl;
				flags |= fOutxCtsFlow;
			}

			// Following have no corresponding functionality in unix termios
			// fOutxDsrFlow = 0x00000008;
			// fDtrControl = 0x00000030;
			// fDsrSensitivity = 0x00000040;
			// fErrorChar = 0x00000400;
			// fNull = 0x00000800;
			// fAbortOnError = 0x00004000;
			// fDummy2 = 0xFFFF8000;
			dcb.fFlags = flags;
			// Don't touch these, windows seems to use: XonLim 2048 XoffLim 512 and who am I to argue with those
			//dcb.XonLim = 0;
			//dcb.XoffLim = 128;
			byte cs = 8;
			int csize = c_cflag & CSIZE;
			if (csize == CS5) {
				cs = 5;
			}
			if (csize == CS6) {
				cs = 6;
			}
			if (csize == CS7) {
				cs = 7;
			}
			if (csize == CS8) {
				cs = 8;
			}
			dcb.ByteSize = cs;

			if ((c_cflag & PARENB) != 0) {
				if ((c_cflag & PARODD) != 0 && (c_cflag & CMSPAR) != 0) {
					dcb.Parity = MARKPARITY;
				} else if ((c_cflag & PARODD) != 0) {
					dcb.Parity = ODDPARITY;
				} else if ((c_cflag & CMSPAR) != 0) {
					dcb.Parity = SPACEPARITY;
				} else {
					dcb.Parity = EVENPARITY;
				}
			} else {
				dcb.Parity = NOPARITY;
			}

			dcb.StopBits = (c_cflag & CSTOPB) != 0 ? TWOSTOPBITS : ONESTOPBIT;
			// In theory these could change but they only get updated if the
			// baudrate/char size changes so this could be a time bomb
			dcb.XonChar = tios.c_cc[VSTART];
			// In practice in PJC these are never changed so updating
			// on the first pass is enough
			dcb.XoffChar = tios.c_cc[VSTOP];
			dcb.ErrorChar = 0;

			// rxtx has some thing like
			// if ( EV_BREAK|EV_CTS|EV_DSR|EV_ERR|EV_RING | ( EV_RLSD & EV_RXFLAG )
			// )
			// dcb.EvtChar = '\n';
			// else
			// dcb.EvtChar = '\0';
			// But those are all defines so there is something fishy there?

			dcb.EvtChar = '\n';
			dcb.EofChar = tios.c_cc[VEOF];

			if (!SetCommState(this.m_Comm, dcb)) {
				fail();
			}

			this.m_c_speed = c_speed;
			this.m_c_cflag = c_cflag;
			this.m_c_iflag = c_iflag;
			this.m_c_oflag = c_oflag;
		}

		int vmin = this.m_Termios.c_cc[VMIN] & 0xFF;
		int vtime = (this.m_Termios.c_cc[VTIME] & 0xFF) * 100;
		if (vmin != this.m_VMIN || vtime != this.m_VTIME) {
			COMMTIMEOUTS touts = this.m_Timeouts;
			// There are really no write timeouts in classic unix termios
			// FIXME test that we can still interrupt the tread
			touts.WriteTotalTimeoutConstant = 0;
			touts.WriteTotalTimeoutMultiplier = 0;
			if (vmin == 0 && vtime == 0) {
				// VMIN = 0 and VTIME = 0 => totally non blocking,if data is
				// available, return it, ie this is poll operation
				touts.ReadIntervalTimeout = MAXDWORD;
				touts.ReadTotalTimeoutConstant = 0;
				touts.ReadTotalTimeoutMultiplier = 0;
			}
			if (vmin == 0 && vtime > 0) {
				// VMIN = 0 and VTIME > 0 => timed read, return as soon as data
				// is available, VTIME = total time
				touts.ReadIntervalTimeout = 0;
				touts.ReadTotalTimeoutConstant = vtime;
				touts.ReadTotalTimeoutMultiplier = 0;
			}
			if (vmin > 0 && vtime > 0) {
				// VMIN > 0 and VTIME > 0 => blocks until VMIN chars has arrived
				// or  between chars expired,
				// note that this will block if nothing arrives
				touts.ReadIntervalTimeout = vtime;
				touts.ReadTotalTimeoutConstant = 0;
				touts.ReadTotalTimeoutMultiplier = 0;
			}
			if (vmin > 0 && vtime == 0) {
				// VMIN > 0 and VTIME = 0 => blocks until VMIN characters have
				// been received
				touts.ReadIntervalTimeout = 0;
				touts.ReadTotalTimeoutConstant = 0;
				touts.ReadTotalTimeoutMultiplier = 0;
			}
			if (!SetCommTimeouts(this.m_Comm, this.m_Timeouts)) {
				fail();
			}
			this.m_VMIN = vmin;
			this.m_VTIME = vtime;
			log = log && log(
					2,
					"vmin %d vtime %d ReadIntervalTimeout %d "
					+ "ReadTotalTimeoutConstant %d "
					+ "ReadTotalTimeoutMultiplier %d\n",
					vmin,
					vtime,
					touts.ReadIntervalTimeout,
					touts.ReadTotalTimeoutConstant,
					touts.ReadTotalTimeoutMultiplier);
		}

		return 0;
	}

	public void close() {
		synchronized (this.jtios) {
			if (this.m_FD >= 0) {
				this.jtios.m_OpenPorts.remove(this.m_FD);
				this.jtios.m_PortFDs[this.m_FD] = false;
				this.m_FD = -1;
			}

			if (this.m_CancelWaitSema4 != null) {
				SetEvent(this.m_CancelWaitSema4);
			}
			if (this.m_Comm != null) {
				ResetEvent(this.m_SelOVL.hEvent);

				if (!CancelIo(this.m_Comm)) {
					log = log && log(
							1,
							"CancelIo() failed, GetLastError()= %d, %s\n",
							GetLastError(), lineno(1));
				}
				if (!PurgeComm(
						this.m_Comm,
						PURGE_TXABORT | PURGE_TXCLEAR
						| PURGE_RXABORT | PURGE_RXCLEAR)) {
					log = log && log(
							1,
							"PurgeComm() failed, GetLastError()= %d, %s\n",
							GetLastError(),
							lineno(1));
				}

				GetOverlappedResult(
						this.m_Comm,
						this.m_RdOVL,
						this.m_RdN,
						true);
				GetOverlappedResult(
						this.m_Comm,
						this.m_WrOVL,
						this.m_WrN,
						true);
				GetOverlappedResult(
						this.m_Comm,
						this.m_SelOVL,
						this.m_SelN,
						true);
			}

			HANDLE h; // / 'hEvent' might never have been 'read' so read it
			// to this var first

			synchronized (this.m_RdBuffer) {
				h = (HANDLE) this.m_RdOVL.readField("hEvent");
				this.m_RdOVL = null;
				if (h != null && !h.equals(NULL)
						&& !h.equals(INVALID_HANDLE_VALUE)) {
					CloseHandle(h);
				}
			}

			synchronized (this.m_WrBuffer) {
				h = (HANDLE) this.m_WrOVL.readField("hEvent");
				this.m_WrOVL = null;

				if (h != null && !h.equals(NULL)
						&& !h.equals(INVALID_HANDLE_VALUE)) {
					CloseHandle(h);
				}
			}

			// Ensure that select() is through before releasing the m_SelOVL
			waitUnlock();

			h = (HANDLE) this.m_SelOVL.readField("hEvent");
			this.m_SelOVL = null;
			if (h != null
					&& !h.equals(NULL)
					&& !h.equals(INVALID_HANDLE_VALUE)) {
				CloseHandle(h);
			}

			if (this.m_Comm != null
					&& this.m_Comm != NULL
					&& this.m_Comm != INVALID_HANDLE_VALUE) {
				CloseHandle(this.m_Comm);
			}
			this.m_Comm = null;

		}
	}

}