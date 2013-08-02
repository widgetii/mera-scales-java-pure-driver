package purejavacomm;

import static jtermios.JTermios.*;

import java.io.IOException;
import java.io.InputStream;

import jtermios.FDSet;
import jtermios.TimeVal;


final class PureJavaInputStream extends InputStream {

	private final PureJavaSerialPort port;
	// im_ for inner class members
	private int[] im_Available = { 0 };
	private byte[] im_Buffer = new byte[2048];
	// this stuff is just cached/precomputed stuff to make read() faster
	private int im_VTIME = -1;
	private int im_VMIN = -1;
	private int[] im_ReadPollFD;
	private byte[] im_Nudge;
	private FDSet im_ReadFDSet;
	private TimeVal im_ReadTimeVal;
	private int im_PollFDn;
	private boolean im_ReceiveTimeoutEnabled;
	private int im_ReceiveTimeoutValue;
	private boolean im_ReceiveThresholdEnabled;
	private int im_ReceiveThresholdValue;
	private boolean im_PollingReadMode;
	private int im_ReceiveTimeoutVTIME;

	PureJavaInputStream(PureJavaSerialPort port) {
		this.port = port;
		this.im_ReadFDSet = newFDSet();
		this.im_ReadTimeVal = new TimeVal();
		this.im_ReadPollFD = new int[4];
		this.im_ReadPollFD[0] = this.port.m_FD;
		this.im_ReadPollFD[1] = POLLIN_IN;
		this.im_ReadPollFD[2] = this.port.m_PipeRdFD;
		this.im_ReadPollFD[3] = POLLIN_IN;
		this.im_PollFDn = this.port.m_HaveNudgePipe ? 2 : 1;
		this.im_Nudge = new byte[1];
	}

	@Override
	public final int available() throws IOException {
		this.port.checkState();
		if (ioctl(this.port.m_FD, FIONREAD, this.im_Available) < 0) {
			this.port.close();
			throw new IOException();
		}
		return this.im_Available[0];
	}

	@Override
	public final int read() throws IOException {
		this.port.checkState();
		byte[] buf = { 0 };
		int n = read(buf, 0, 1);

		return n > 0 ? buf[0] & 0xFF : -1;
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public final int read(
			byte[] buffer,
			int offset,
			int length)
	throws IOException {
		// reads++;
		if (buffer == null) {
			throw new IllegalArgumentException("buffer null");
		}
		if (length == 0) {
			return 0;
		}
		if (offset < 0 || length < 0 || offset + length > buffer.length) {
			throw new IndexOutOfBoundsException("buffer.length " + buffer.length + " offset " + offset + " length " + length);
		}

		if (this.port.RAW_READ_MODE) {
			if (this.port.m_TimeoutThresholdChanged) { // does not need the lock if we just check the value
				synchronized (this.port.m_ThresholdTimeoutLock) {
					int vtime = this.port.m_ReceiveTimeoutEnabled ? this.port.m_ReceiveTimeoutVTIME : 0;
					int vmin = this.port.m_ReceiveThresholdEnabled ? this.port.m_ReceiveThresholdValue : 1;
					synchronized (this.port.m_Termios) {
						this.port.m_Termios.c_cc[VTIME] = (byte) vtime;
						this.port.m_Termios.c_cc[VMIN] = (byte) vmin;
						this.port.checkReturnCode(tcsetattr(this.port.m_FD, TCSANOW, this.port.m_Termios));
					}
					this.port.m_TimeoutThresholdChanged = false;
				}
			}
			int bytesRead;
			if (offset > 0) {
				if (length < this.im_Buffer.length) {
					bytesRead = jtermios.JTermios.read(this.port.m_FD, this.im_Buffer, length);
				} else {
					bytesRead = jtermios.JTermios.read(this.port.m_FD, this.im_Buffer, this.im_Buffer.length);
				}
				if (bytesRead > 0) {
					System.arraycopy(this.im_Buffer, 0, buffer, offset, bytesRead);
				}
			} else {
				bytesRead = jtermios.JTermios.read(this.port.m_FD, buffer, length);
			}
			this.port.m_DataAvailableNotified = false;
			return bytesRead;

		} // End of raw read mode code

		if (this.port.m_FD < 0) {
			this.port.failWithIllegalStateException();
		}

		if (this.port.m_TimeoutThresholdChanged) { // does not need the lock if we just check the alue
			synchronized (this.port.m_ThresholdTimeoutLock) {
				// capture these here under guard so that we get a coherent picture of the settings
				this.im_ReceiveTimeoutEnabled = this.port.m_ReceiveTimeoutEnabled;
				this.im_ReceiveTimeoutValue = this.port.m_ReceiveTimeoutValue;
				this.im_ReceiveThresholdEnabled = this.port.m_ReceiveThresholdEnabled;
				this.im_ReceiveThresholdValue = this.port.m_ReceiveThresholdValue;
				this.im_PollingReadMode = this.port.m_PollingReadMode;
				this.im_ReceiveTimeoutVTIME = this.port.m_ReceiveTimeoutVTIME;
				this.port.m_TimeoutThresholdChanged = false;
			}
		}

		int bytesLeft = length;
		int bytesReceived = 0;
		int minBytesRequired;

		// Note for optimal performance: message length == receive threshold == read length <= 255
		// the best case execution path is marked with BEST below

		while (true) {
			// loops++;
			int vmin;
			int vtime;
			if (this.im_PollingReadMode) {
				minBytesRequired = 0;
				vmin = 0;
				vtime = 0;
			} else {
				if (this.im_ReceiveThresholdEnabled) {
					minBytesRequired = this.im_ReceiveThresholdValue; // BEST
				} else {
					minBytesRequired = 1;
				}
				if (minBytesRequired > bytesLeft) {
					minBytesRequired = bytesLeft;
				}
				if (minBytesRequired <= 255) {
					vmin = minBytesRequired; // BEST case
				} else {
					vmin = 255;
				}

				// FIXME someone might change m_ReceiveTimeoutEnabled
				if (this.im_ReceiveTimeoutEnabled) {
					vtime = this.im_ReceiveTimeoutVTIME; // BEST case
				} else {
					vtime = 0;
				}
			}
			if (vmin != this.im_VMIN || vtime != this.im_VTIME) { // in BEST case 'if' not taken more than once for given InputStream instance
				// ioctls++;
				this.im_VMIN = vmin;
				this.im_VTIME = vtime;
				// This needs to be guarded with m_Termios so that these thing don't change on us
				synchronized (this.port.m_Termios) {
					this.port.m_Termios.c_cc[VTIME] = (byte) this.im_VTIME;
					this.port.m_Termios.c_cc[VMIN] = (byte) this.im_VMIN;
					this.port.checkReturnCode(tcsetattr(this.port.m_FD, TCSANOW, this.port.m_Termios));
				}
			}

			// Now wait for data to be available, except in raw read mode
			// and polling read modes. Following looks a bit longish
			// but  there is actually not that much code to be executed
			boolean dataAvailable = false;
			boolean timedout = false;
			if (!this.im_PollingReadMode) {
				int n;
				// long T0 = System.nanoTime();
				// do a select()/poll(), just in case this read was
				// called when no data is available
				// so that we will not hang for ever in a read
				int timeoutValue = this.im_ReceiveTimeoutEnabled ? this.im_ReceiveTimeoutValue : Integer.MAX_VALUE;
				if (this.port.USE_POLL) { // BEST case in Linux but not on
								// Windows or Mac OS X
					n = poll(this.im_ReadPollFD, this.im_PollFDn, timeoutValue);
					if (n < 0 || this.port.m_FD < 0) {
						throw new IOException();
					}

					if ((this.im_ReadPollFD[3] & POLLIN_OUT) != 0) {
						jtermios.JTermios.read(this.port.m_PipeRdFD, this.im_Nudge, 1);
					}
					int re = this.im_ReadPollFD[1];
					if ((re & POLLNVAL_OUT) != 0) {
						throw new IOException();
					}
					dataAvailable = (re & POLLIN_OUT) != 0;

				} else { // this is a bit slower but then again it is unlikely
					// this gets executed in a low horsepower system
					FD_ZERO(this.im_ReadFDSet);
					FD_SET(this.port.m_FD, this.im_ReadFDSet);
					int maxFD = this.port.m_FD;
					if (this.port.m_HaveNudgePipe) {
						FD_SET(this.port.m_PipeRdFD, this.im_ReadFDSet);
						if (this.port.m_PipeRdFD > maxFD) {
							maxFD = this.port.m_PipeRdFD;
						}
					}
					if (timeoutValue >= 1000) {
						int t = timeoutValue / 1000;
						this.im_ReadTimeVal.tv_sec = t;
						this.im_ReadTimeVal.tv_usec = (timeoutValue - t * 1000) * 1000;
					} else {
						this.im_ReadTimeVal.tv_sec = 0;
						this.im_ReadTimeVal.tv_usec = timeoutValue * 1000;
					}
					n = select(maxFD + 1, this.im_ReadFDSet, null, null, this.im_ReadTimeVal);
					if (n < 0) {
						throw new IOException();
					}
					if (this.port.m_FD < 0) {
						// blocking in select
						throw new IOException();
					}
					dataAvailable = FD_ISSET(this.port.m_FD, this.im_ReadFDSet);
				}
				if (n == 0 && this.port.m_ReceiveTimeoutEnabled) {
					timedout = true;
				}
			}

			if (timedout) {
				break;
			}

			// At this point data is either available or we take our
			// chances in raw mode or this polling read which can't block
			int bytesRead = 0;
			if (dataAvailable || this.im_PollingReadMode) {
				if (offset > 0) {
					if (bytesLeft < this.im_Buffer.length) {
						bytesRead = jtermios.JTermios.read(this.port.m_FD, this.im_Buffer, bytesLeft);
					} else {
						bytesRead = jtermios.JTermios.read(this.port.m_FD, this.im_Buffer, this.im_Buffer.length);
					}
					if (bytesRead > 0) {
						System.arraycopy(this.im_Buffer, 0, buffer, offset, bytesRead);
					}
				} else {
					// this the BEST case execution path
					bytesRead = jtermios.JTermios.read(this.port.m_FD, buffer, bytesLeft);
				}
				// readtime += System.nanoTime() - T0;
				if (bytesRead == 0) {
					timedout = true;
				}
			}

			// Now we have read data and try to return as quickly as
			// possibly or we have timed out.

			if (bytesRead < 0) {
				throw new IOException();
			}

			bytesReceived += bytesRead;

			if (bytesReceived >= minBytesRequired) {
				break; // we have read the minimum required and will return that
			}

			if (timedout) {
				break;
			}

			// Ok, looks like we are in for an other loop, so update
			// the offset
			// and loop for some more
			offset += bytesRead;
			bytesLeft -= bytesRead;
		}

		this.port.m_DataAvailableNotified = false;

		return bytesReceived;
	}

}
