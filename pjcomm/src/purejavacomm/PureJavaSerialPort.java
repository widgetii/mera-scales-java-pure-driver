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
package purejavacomm;

import static jtermios.JTermios.*;
import static jtermios.JTermios.JTermiosLogging.lineno;
import static jtermios.JTermios.JTermiosLogging.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import jtermios.FDSet;
import jtermios.Termios;
import jtermios.TimeVal;

import com.sun.jna.Platform;


public class PureJavaSerialPort extends SerialPort {

	final boolean USE_POLL;
	final boolean RAW_READ_MODE;
	private Thread m_Thread;
	private volatile SerialPortEventListener m_EventListener;
	private volatile OutputStream m_OutputStream;
	private volatile InputStream m_InputStream;
	volatile int m_FD = -1;
	volatile boolean m_HaveNudgePipe = false;
	private volatile int m_PipeWrFD = 0;
	volatile int m_PipeRdFD = 0;
	private byte[] m_NudgeData = { 0 };
	private volatile int m_BaudRate;
	private volatile int m_DataBits;
	private volatile int m_FlowControlMode;
	private volatile int m_Parity;
	private volatile int m_StopBits;
	volatile Object m_ThresholdTimeoutLock = new Object();
	volatile boolean m_TimeoutThresholdChanged = true;
	volatile boolean m_ReceiveTimeoutEnabled;
	volatile int m_ReceiveTimeoutValue;
	volatile int m_ReceiveTimeoutVTIME;
	volatile boolean m_ReceiveThresholdEnabled;
	volatile int m_ReceiveThresholdValue;
	volatile boolean m_PollingReadMode;
	private volatile boolean m_NotifyOnDataAvailable;
	volatile boolean m_DataAvailableNotified;
	private volatile boolean m_NotifyOnOutputEmpty;
	volatile boolean m_OutputEmptyNotified;
	private volatile boolean m_NotifyOnRI;
	private volatile boolean m_NotifyOnCTS;
	private volatile boolean m_NotifyOnDSR;
	private volatile boolean m_NotifyOnCD;
	private volatile boolean m_NotifyOnOverrunError;
	private volatile boolean m_NotifyOnParityError;
	private volatile boolean m_NotifyOnFramingError;
	private volatile boolean m_NotifyOnBreakInterrupt;
	private volatile boolean m_ThreadRunning;
	private volatile boolean m_ThreadStarted;
	private int[] m_ioctl = { 0 };
	private int m_ControlLineStates;
	// we cache termios in m_Termios because we don't rely on reading it back with tcgetattr()
	// which for Mac OS X / CRTSCTS does not work, it is also more efficient
	final Termios m_Termios = new Termios();
	private int m_MinVTIME;

	private void sendDataEvents(boolean read, boolean write) {
		if (read && this.m_NotifyOnDataAvailable && !this.m_DataAvailableNotified) {
			this.m_DataAvailableNotified = true;
			this.m_EventListener.serialEvent(new SerialPortEvent(this, SerialPortEvent.DATA_AVAILABLE, false, true));
		}
		if (write && this.m_NotifyOnOutputEmpty && !this.m_OutputEmptyNotified) {
			this.m_OutputEmptyNotified = true;
			this.m_EventListener.serialEvent(new SerialPortEvent(this, SerialPortEvent.OUTPUT_BUFFER_EMPTY, false, true));
		}
	}

	private synchronized void sendNonDataEvents() {
		if (ioctl(this.m_FD, TIOCMGET, this.m_ioctl) < 0) {
			return; // FIXME decide what to with errors in the background thread
		}
		int oldstates = this.m_ControlLineStates;
		this.m_ControlLineStates = this.m_ioctl[0];
		int newstates = this.m_ControlLineStates;
		int changes = oldstates ^ newstates;
		if (changes == 0) {
			return;
		}

		int line;

		if (this.m_NotifyOnCTS && (((line = TIOCM_CTS) & changes) != 0)) {
			this.m_EventListener.serialEvent(
					new SerialPortEvent(
							this,
							SerialPortEvent.CTS,
							(oldstates & line) != 0,
							(newstates & line) != 0));
		}
		if (this.m_NotifyOnDSR && (((line = TIOCM_DSR) & changes) != 0)) {
			this.m_EventListener.serialEvent(
					new SerialPortEvent(
							this,
							SerialPortEvent.DSR,
							(oldstates & line) != 0,
							(newstates & line) != 0));
		}
		if (this.m_NotifyOnRI && (((line = TIOCM_RI) & changes) != 0)) {
			this.m_EventListener.serialEvent(
					new SerialPortEvent(
							this,
							SerialPortEvent.RI,
							(oldstates & line) != 0,
							(newstates & line) != 0));
		}
		if (this.m_NotifyOnCD && (((line = TIOCM_CD) & changes) != 0)) {
			this.m_EventListener.serialEvent(
					new SerialPortEvent(
							this,
							SerialPortEvent.CD,
							(oldstates & line) != 0,
							(newstates & line) != 0));
		}
	}

	@Override
	public synchronized void addEventListener(
			SerialPortEventListener eventListener)
	throws TooManyListenersException {
		checkState();
		if (eventListener == null) {
			throw new IllegalArgumentException("eventListener cannot be null");
		}
		if (this.m_EventListener != null) {
			throw new TooManyListenersException();
		}
		this.m_EventListener = eventListener;
		if (!this.m_ThreadStarted) {
			this.m_ThreadStarted = true;
			this.m_Thread.start();
		}
	}

	@Override
	public synchronized int getBaudRate() {
		checkState();
		return this.m_BaudRate;
	}

	@Override
	public synchronized int getDataBits() {
		checkState();
		return this.m_DataBits;
	}

	@Override
	public synchronized int getFlowControlMode() {
		checkState();
		return this.m_FlowControlMode;
	}

	@Override
	public synchronized int getParity() {
		checkState();
		return this.m_Parity;
	}

	@Override
	public synchronized int getStopBits() {
		checkState();
		return this.m_StopBits;
	}

	@Override
	public synchronized boolean isCD() {
		checkState();
		return getControlLineState(TIOCM_CD);
	}

	@Override
	public synchronized boolean isCTS() {
		checkState();
		return getControlLineState(TIOCM_CTS);
	}

	@Override
	public synchronized boolean isDSR() {
		checkState();
		return getControlLineState(TIOCM_DSR);
	}

	@Override
	public synchronized boolean isDTR() {
		checkState();
		return getControlLineState(TIOCM_DTR);
	}

	@Override
	public synchronized boolean isRI() {
		checkState();
		return getControlLineState(TIOCM_RI);
	}

	@Override
	public synchronized boolean isRTS() {
		checkState();
		return getControlLineState(TIOCM_RTS);
	}

	@Override
	public synchronized void notifyOnBreakInterrupt(boolean x) {
		checkState();
		this.m_NotifyOnBreakInterrupt = x;
	}

	@Override
	public synchronized void notifyOnCTS(boolean x) {
		checkState();
		if (x) {
			updateControlLineState(TIOCM_CTS);
		}
		this.m_NotifyOnCTS = x;
		nudgePipe();
	}

	@Override
	public synchronized void notifyOnCarrierDetect(boolean x) {
		checkState();
		if (x) {
			updateControlLineState(TIOCM_CD);
		}
		this.m_NotifyOnCD = x;
		nudgePipe();
	}

	@Override
	public synchronized void notifyOnDSR(boolean x) {
		checkState();
		if (x) {
			updateControlLineState(TIOCM_DSR);
		}
		this.m_NotifyOnDSR = x;
		nudgePipe();
	}

	@Override
	public synchronized void notifyOnDataAvailable(boolean x) {
		checkState();
		this.m_NotifyOnDataAvailable = x;
		nudgePipe();
	}

	@Override
	public synchronized void notifyOnFramingError(boolean x) {
		checkState();
		this.m_NotifyOnFramingError = x;
	}

	@Override
	public synchronized void notifyOnOutputEmpty(boolean x) {
		checkState();
		this.m_NotifyOnOutputEmpty = x;
		nudgePipe();
	}

	@Override
	public synchronized void notifyOnOverrunError(boolean x) {
		checkState();
		this.m_NotifyOnOverrunError = x;
	}

	@Override
	public synchronized void notifyOnParityError(boolean x) {
		checkState();
		this.m_NotifyOnParityError = x;
	}

	@Override
	public synchronized void notifyOnRingIndicator(boolean x) {
		checkState();
		if (x) {
			updateControlLineState(TIOCM_RI);
		}
		this.m_NotifyOnRI = x;
		nudgePipe();
	}

	@Override
	public synchronized void removeEventListener() {
		checkState();
		this.m_EventListener = null;
	}

	@Override
	public synchronized void sendBreak(int duration) {
		checkState();
		// FIXME POSIX does not specify how duration is interpreted
		// Opengroup POSIX says:
		// If the terminal is using asynchronous serial data transmission, tcsendbreak()
		// shall cause transmission of a continuous stream of zero-valued bits for a specific duration.
		// If duration is 0, it shall cause transmission of zero-valued bits for at least 0.25 seconds,
		// and not more than 0.5 seconds. If duration is not 0, it shall send zero-valued bits for an implementation-defined period of time.
		// From the man page for Linux tcsendbreak:
		// The effect of a non-zero duration with tcsendbreak() varies.
		// SunOS specifies a break of duration*N seconds,
		// where N is at least 0.25, and not more than 0.5. Linux, AIX, DU, Tru64 send a break of duration milliseconds.
		// FreeBSD and NetBSD and HP-UX and MacOS ignore the value of duration.
		// Under Solaris and Unixware, tcsendbreak() with non-zero duration behaves like tcdrain().

		tcsendbreak(this.m_FD, duration);
	}

	@Override
	public synchronized void setDTR(boolean x) {
		checkState();
		setControlLineState(TIOCM_DTR, x);
	}

	@Override
	public synchronized void setRTS(boolean x) {
		checkState();
		setControlLineState(TIOCM_RTS, x);
	}

	@Override
	public synchronized void disableReceiveFraming() {
		checkState();
		// Not supported
	}

	@Override
	public synchronized void disableReceiveThreshold() {
		checkState();
		synchronized (this.m_ThresholdTimeoutLock) {
			this.m_ReceiveThresholdEnabled = false;
			thresholdOrTimeoutChanged();
		}
	}

	@Override
	public synchronized void disableReceiveTimeout() {
		checkState();
		synchronized (this.m_ThresholdTimeoutLock) {
			this.m_ReceiveTimeoutEnabled = false;
			thresholdOrTimeoutChanged();
		}
	}

	@Override
	public synchronized void enableReceiveThreshold(int value) throws UnsupportedCommOperationException {
		checkState();
		if (value < 0) {
			throw new IllegalArgumentException("threshold" + value + " < 0 ");
		}
		if (this.RAW_READ_MODE && value > 255) {
			throw new IllegalArgumentException("threshold" + value + " > 255 in raw read mode");
		}
		synchronized (this.m_ThresholdTimeoutLock) {
			this.m_ReceiveThresholdEnabled = true;
			this.m_ReceiveThresholdValue = value;
			thresholdOrTimeoutChanged();
		}
	}

	@Override
	public synchronized void enableReceiveTimeout(int value) throws UnsupportedCommOperationException {
		if (value < 0) {
			throw new IllegalArgumentException("threshold" + value + " < 0 ");
		}
		if ((value + 99) / 100 > 255) {
			throw new UnsupportedCommOperationException("threshold" + value + " too large ");
		}

		checkState();
		synchronized (this.m_ThresholdTimeoutLock) {
			this.m_ReceiveTimeoutEnabled = true;
			this.m_ReceiveTimeoutValue = value;
			thresholdOrTimeoutChanged();
		}
	}

	@Override
	public synchronized void enableReceiveFraming(int arg0) throws UnsupportedCommOperationException {
		checkState();
		throw new UnsupportedCommOperationException();
	}

	private void thresholdOrTimeoutChanged() { // only call if you hold the lock
		this.m_PollingReadMode = (this.m_ReceiveTimeoutEnabled && this.m_ReceiveTimeoutValue == 0) || (this.m_ReceiveThresholdEnabled && this.m_ReceiveThresholdValue == 0);
		this.m_ReceiveTimeoutVTIME = (this.m_ReceiveTimeoutValue + 99) / 100; // precalculate this so we don't need the division in read
		this.m_TimeoutThresholdChanged = true;
	}

	@Override
	public synchronized int getInputBufferSize() {
		checkState();
		// Not supported
		return 0;
	}

	@Override
	public synchronized int getOutputBufferSize() {
		checkState();
		// Not supported
		return 0;
	}

	@Override
	public synchronized void setFlowControlMode(
			int mode)
	throws UnsupportedCommOperationException {
		checkState();
		synchronized (this.m_Termios) {
			updateFlowControlMode(mode);
		}
	}

	private void updateFlowControlMode(int mode) {
		this.m_Termios.setFlowControlMode(mode);
		checkReturnCode(tcsetattr(this.m_FD, TCSANOW, this.m_Termios));
		this.m_FlowControlMode = mode;
	}

	@Override
	public synchronized void setSerialPortParams(
			int baudRate,
			int dataBits,
			int stopBits,
			int parity)
	throws UnsupportedCommOperationException {
		checkState();
		synchronized (this.m_Termios) {
			Termios prev = new Termios();// (termios);

			// save a copy in case we need to restore it
			prev.set(this.m_Termios);

			try {
				updateSerialPortParams(baudRate, dataBits, stopBits, parity);

				// finally everything went ok, so we can update our settings
				this.m_BaudRate = baudRate;
				this.m_Parity = parity;
				this.m_DataBits = dataBits;
				this.m_StopBits = stopBits;
			} catch (UnsupportedCommOperationException e) {
				this.m_Termios.set(prev);
				checkReturnCode(tcsetattr(this.m_FD, TCSANOW, this.m_Termios));
				throw e;
			} catch (IllegalStateException e) {
				this.m_Termios.set(prev);
				checkReturnCode(tcsetattr(this.m_FD, TCSANOW, this.m_Termios));
				if (e instanceof PureJavaIllegalStateException) {
					throw e;
				}
				throw new PureJavaIllegalStateException(e);
			}
		}
	}

	private void updateSerialPortParams(
			int baudRate,
			int dataBits,
			int stopBits,
			int parity)
	throws UnsupportedCommOperationException {
		checkReturnCode(setspeed(this.m_FD, this.m_Termios, baudRate));
		this.m_Termios.setDataFormat(dataBits, stopBits, parity);
		if (tcsetattr(this.m_FD, TCSANOW, this.m_Termios) != 0) {
			throw new UnsupportedCommOperationException();
		}
	}

	/**
	 * Gets the native file descriptor used by this port.
	 * <p>
	 * The file descriptor can be used in calls to JTermios functions. This
	 * maybe useful in extreme cases where performance is more important than
	 * convenience, for example using <code>JTermios.read(...)</code> instead of
	 * <code>SerialPort.getInputStream().read(...)</code>.
	 * <p>
	 * Note that mixing direct JTermios read/write calls with SerialPort stream
	 * read/write calls is at best fragile and likely to fail, which also
	 * implies that when using JTermios directly then configuring the port,
	 * especially termios.cc[VMIN] and termios.cc[VTIME] is the users
	 * responsibility.
	 * <p>
	 * Below is a sketch of minimum necessary to perform a read using raw
	 * JTermios functionality.
	 *
	 * <pre>
	 * <code>
	 * 		// import the JTermios functionality like this
	 * 		import jtermios.*;
	 * 		import static jtermios.JTermios.*;
	 *
	 * 		SerialPort port = ...;
	 *
	 * 		// cast the port to PureJavaSerialPort to get access to getNativeFileDescriptor
	 * 		int FD = ((PureJavaSerialPort) port).getNativeFileDescriptor();
	 *
	 * 		// timeout and threshold values
	 * 		int messageLength = 25; // bytes
	 * 		int timeout = 200; // msec
	 *
	 * 		// to initialize timeout and threshold first read current termios
	 * 		Termios termios = new Termios();
	 *
	 * 		if (0 != tcgetattr(FD, termios))
	 * 			errorHandling();
	 *
	 * 		// then set VTIME and VMIN, note VTIME in 1/10th of sec and both max 255
	 * 		termios.c_cc[VTIME] = (byte) ((timeout+99) / 100);
	 * 		termios.c_cc[VMIN] = (byte) messageLength;
	 *
	 * 		// update termios
	 * 		if (0 != tcsetattr(FD, TCSANOW, termios))
	 * 			errorHandling();
	 *
	 *      ...
	 * 		// allocate read buffer
	 * 		byte[] readBuffer = new byte[messageLength];
	 *      ...
	 *
	 * 		// then perform raw read, not this may block indefinitely
	 * 		int n = read(FD, readBuffer, messageLength);
	 * 		if (n < 0)
	 * 			errorHandling();
	 * <code>
	 * </pre>
	 *
	 * @return the native OS file descriptor as int
	 */
	public int getNativeFileDescriptor() {
		return this.m_FD;
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException {
		checkState();
		if (this.m_OutputStream == null) {
			this.m_OutputStream = new PureJavaOutputStream(this);
		}
		return this.m_OutputStream;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		checkState();
		if (this.m_InputStream == null) {
			// NOTE: Windows and unixes are so different that it actually might
			// make sense to have the backend (ie JTermiosImpl) to provide
			// an InputStream that is optimal for the platform, instead of
			// trying to share of the InputStream logic here and force
			// Windows backend to conform to the the POSIX select()/
			// read()/vtim/vtime model. See the amount of code here
			// and in windows.JTermiosImpl for  select() and read().
			//
			this.m_InputStream = new PureJavaInputStream(this);
		}
		return this.m_InputStream;
	}

	@Override
	public synchronized int getReceiveFramingByte() {
		checkState();
		// Not supported
		return 0;
	}

	@Override
	public synchronized int getReceiveThreshold() {
		checkState();
		return this.m_ReceiveThresholdValue;
	}

	@Override
	public synchronized int getReceiveTimeout() {
		checkState();
		return this.m_ReceiveTimeoutValue;
	}

	@Override
	public synchronized boolean isReceiveFramingEnabled() {
		checkState();
		// Not supported
		return false;
	}

	@Override
	public synchronized boolean isReceiveThresholdEnabled() {
		checkState();
		return this.m_ReceiveThresholdEnabled;
	}

	@Override
	public synchronized boolean isReceiveTimeoutEnabled() {
		checkState();
		return this.m_ReceiveTimeoutEnabled;
	}

	@Override
	public synchronized void setInputBufferSize(int arg0) {
		checkState();
		// Not supported
	}

	@Override
	public synchronized void setOutputBufferSize(int arg0) {
		checkState();
		// Not supported
	}

	private void nudgePipe() {
		if (this.m_HaveNudgePipe) {
			write(this.m_PipeWrFD, this.m_NudgeData, 1);
		}
	}

	@Override
	public synchronized void close() {
		int fd = this.m_FD;
		if (fd != -1) {
			this.m_FD = -1;
			try {
				if (this.m_InputStream != null) {
					this.m_InputStream.close();
				}
			} catch (IOException e) {
				log = log && log(1, "m_InputStream.close threw an IOException %s\n", e.getMessage());
			} finally {
				this.m_InputStream = null;
			}
			try {
				if (this.m_OutputStream != null) {
					this.m_OutputStream.close();
				}
			} catch (IOException e) {
				log = log && log(1, "m_OutputStream.close threw an IOException %s\n", e.getMessage());
			} finally {
				this.m_OutputStream = null;
			}
			nudgePipe();
			int flags = fcntl(fd, F_GETFL, 0);
			flags |= O_NONBLOCK;
			int fcres = fcntl(fd, F_SETFL, flags);
			if (fcres != 0) {
				log = log && log(1, "fcntl(%d,%d,%d) returned %d\n", fd, F_SETFL, flags, fcres);
			}

			if (this.m_Thread != null) {
				this.m_Thread.interrupt();
			}
			int err = jtermios.JTermios.close(fd);
			if (err < 0) {
				log = log && log(1, "JTermios.close returned %d, errno %d\n", err, errno());
			}

			if (this.m_HaveNudgePipe) {
				err = jtermios.JTermios.close(this.m_PipeRdFD);
				if (err < 0) {
					log = log && log(1, "JTermios.close returned %d, errno %d\n", err, errno());
				}
				err = jtermios.JTermios.close(this.m_PipeWrFD);
				if (err < 0) {
					log = log && log(1, "JTermios.close returned %d, errno %d\n", err, errno());
				}
			}
			long t0 = System.currentTimeMillis();
			while (this.m_ThreadRunning) {
				try {
					Thread.sleep(5);
					if (System.currentTimeMillis() - t0 > 2000) {
						break;
					}
				} catch (InterruptedException e) {
					break;
				}
			}
			super.close();
		}
	}

	PureJavaSerialPort(
			String name,
			int timeout,
			SerialPortMode mode)
	throws PortInUseException {

		boolean usepoll = false;

		if (Platform.isLinux()) {
			String key1 = "purejavacomm.use_poll";
			String key2 = "purejavacomm.usepoll";
			if (System.getProperty(key1) != null) {
				usepoll = Boolean.getBoolean(key1);
				log = log && log(1, "use of '%s' is deprecated, use '%s' instead\n", key1, key2);
			} else if (System.getProperty(key2) != null) {
				usepoll = Boolean.getBoolean(key2);
			} else {
				usepoll = true;
			}
		}
		this.USE_POLL = usepoll;

		this.RAW_READ_MODE = Boolean.getBoolean("purejavacomm.rawreadmode");

		this.name = name;

		// unbelievable, sometimes quickly closing and re-opening fails on
		// Windows
		// so try a few times
		int tries = 100;
		long T0 = System.currentTimeMillis();

		while ((this.m_FD =
				open(name, mode, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			if (tries-- < 0 || System.currentTimeMillis() - T0 >= timeout) {
				throw new PortInUseException();
			}
		}

		this.m_MinVTIME = Integer.getInteger("purejavacomm.minvtime", 100);
		int flags = fcntl(this.m_FD, F_GETFL, 0);
		flags &= ~O_NONBLOCK;
		checkReturnCode(fcntl(this.m_FD, F_SETFL, flags));

		this.m_BaudRate = mode.getBaudRate();
		this.m_DataBits = mode.getDataBits();
		this.m_FlowControlMode = mode.getFlowControlMode();
		this.m_Parity = mode.getParity();
		this.m_StopBits = mode.getStopBits();

		checkReturnCode(tcgetattr(this.m_FD, this.m_Termios));

		if (!opensWithMode()) {
			cfmakeraw(this.m_FD, this.m_Termios);

			this.m_Termios.c_cflag |= CLOCAL | CREAD;
			this.m_Termios.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
			this.m_Termios.c_oflag &= ~OPOST;

			this.m_Termios.c_cc[VSTART] = (byte) DC1;
			this.m_Termios.c_cc[VSTOP] = (byte) DC3;
			this.m_Termios.c_cc[VMIN] = 0;
			this.m_Termios.c_cc[VTIME] = 0;
			try {
				updateFlowControlMode(getFlowControlMode());
				updateSerialPortParams(
						getBaudRate(),
						getDataBits(),
						getStopBits(),
						getParity());
			} catch (UnsupportedCommOperationException e) {
				throw new PureJavaIllegalStateException(e);
			}
		}

		checkReturnCode(ioctl(this.m_FD, TIOCMGET, this.m_ioctl));
		this.m_ControlLineStates = this.m_ioctl[0];

		final String nudgekey = "purejavacomm.usenudgepipe";

		if (System.getProperty(nudgekey) == null
				|| Boolean.getBoolean(nudgekey)) {
			int[] pipes = new int[2];
			if (pipe(pipes) == 0) {
				this.m_HaveNudgePipe = true;
				this.m_PipeRdFD = pipes[0];
				this.m_PipeWrFD = pipes[1];
				checkReturnCode(fcntl(
						this.m_PipeRdFD,
						F_SETFL,
						fcntl(this.m_PipeRdFD, F_GETFL, 0) | O_NONBLOCK));
			}
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					PureJavaSerialPort.this.m_ThreadRunning = true;
					// see: http://daniel.haxx.se/docs/poll-vs-select.html
					final int TIMEOUT =
							Integer.getInteger("purejavacomm.pollperiod", 10);

					TimeVal timeout = null;
					FDSet rset = null;
					FDSet wset = null;
					int[] pollfd = null;
					byte[] nudge = null;

					if (PureJavaSerialPort.this.USE_POLL) {
						pollfd = new int[4];
						nudge = new byte[1];
						pollfd[0] = PureJavaSerialPort.this.m_FD;
						pollfd[2] = PureJavaSerialPort.this.m_PipeRdFD;
					} else {
						rset = newFDSet();
						wset = newFDSet();
						timeout = new TimeVal();
						int t = TIMEOUT * 1000;
						timeout.tv_sec = t / 1000000;
						timeout.tv_usec = t - timeout.tv_sec * 1000000;
					}

					while (PureJavaSerialPort.this.m_FD >= 0) {
						boolean read = (PureJavaSerialPort.this.m_NotifyOnDataAvailable && !PureJavaSerialPort.this.m_DataAvailableNotified);
						boolean write = (PureJavaSerialPort.this.m_NotifyOnOutputEmpty && !PureJavaSerialPort.this.m_OutputEmptyNotified);
						int n = 0;

						boolean pollCtrlLines = PureJavaSerialPort.this.m_NotifyOnCTS || PureJavaSerialPort.this.m_NotifyOnDSR || PureJavaSerialPort.this.m_NotifyOnRI || PureJavaSerialPort.this.m_NotifyOnCD;

						if (read || write || (!pollCtrlLines && PureJavaSerialPort.this.m_HaveNudgePipe)) {
							if (PureJavaSerialPort.this.USE_POLL && pollfd != null) {
								int e = 0;
								if (read) {
									e |= POLLIN_IN;
								}
								if (write) {
									e |= POLLOUT_IN;
								}
								pollfd[1] = e;
								pollfd[3] = POLLIN_IN;
								if (PureJavaSerialPort.this.m_HaveNudgePipe) {
									n = poll(pollfd, 2, -1);
								} else {
									n = poll(pollfd, 1, TIMEOUT);
								}

								int re = pollfd[3];

								if ((re & POLLNVAL_OUT) != 0) {
									log = log && log(1, "poll() returned POLLNVAL, errno %d\n", errno());
									break;
								}

								if ((re & POLLIN_OUT) != 0) {
									read(PureJavaSerialPort.this.m_PipeRdFD, nudge, 1);
								}

								re = pollfd[1];
								if ((re & POLLNVAL_OUT) != 0) {
									log = log && log(1, "poll() returned POLLNVAL, errno %d\n", errno());
									break;
								}
								read = read && (re & POLLIN_OUT) != 0;
								write = write && (re & POLLOUT_OUT) != 0;
							} else {
								FD_ZERO(rset);
								FD_ZERO(wset);
								if (read) {
									FD_SET(PureJavaSerialPort.this.m_FD, rset);
								}
								if (write) {
									FD_SET(PureJavaSerialPort.this.m_FD, wset);
								}
								if (PureJavaSerialPort.this.m_HaveNudgePipe) {
									FD_SET(PureJavaSerialPort.this.m_PipeRdFD, rset);
								}
								n = select(PureJavaSerialPort.this.m_FD + 1, rset, wset, null, PureJavaSerialPort.this.m_HaveNudgePipe ? null : timeout);
								read = read && FD_ISSET(PureJavaSerialPort.this.m_FD, rset);
								write = write && FD_ISSET(PureJavaSerialPort.this.m_FD, wset);
							}

							if (PureJavaSerialPort.this.m_FD < 0) {
								break;
							}
							if (n < 0) {
								log = log && log(1, "select() or poll() returned %d, errno %d\n", n, errno());
								close();
								break;
							}
						} else {
							Thread.sleep(TIMEOUT);
						}

						if (PureJavaSerialPort.this.m_EventListener != null) {
							if (read || write) {
								sendDataEvents(read, write);
							}
							if (pollCtrlLines) {
								sendNonDataEvents();
							}
						}
					}
				} catch (InterruptedException ie) {
				} finally {
					PureJavaSerialPort.this.m_ThreadRunning = false;
				}
			}
		};
		this.m_Thread = new Thread(runnable, getName());
		this.m_Thread.setDaemon(true);
	}

	private synchronized void updateControlLineState(int line) {
		checkState();

		if (ioctl(this.m_FD, TIOCMGET, this.m_ioctl) == -1) {
			throw new PureJavaIllegalStateException("ioctl(m_FD, TIOCMGET, m_ioctl) == -1");
		}

		this.m_ControlLineStates = (this.m_ioctl[0] & line) + (this.m_ControlLineStates & ~line);
	}

	private synchronized boolean getControlLineState(int line) {
		checkState();
		if (ioctl(this.m_FD, TIOCMGET, this.m_ioctl) == -1) {
			throw new PureJavaIllegalStateException("ioctl(m_FD, TIOCMGET, m_ioctl) == -1");
		}
		return (this.m_ioctl[0] & line) != 0;
	}

	private synchronized void setControlLineState(int line, boolean state) {
		checkState();
		if (ioctl(this.m_FD, TIOCMGET, this.m_ioctl) == -1) {
			throw new PureJavaIllegalStateException("ioctl(m_FD, TIOCMGET, m_ioctl) == -1");
		}

		if (state) {
			this.m_ioctl[0] |= line;
		} else {
			this.m_ioctl[0] &= ~line;
		}
		if (ioctl(this.m_FD, TIOCMSET, this.m_ioctl) == -1) {
			throw new PureJavaIllegalStateException("ioctl(m_FD, TIOCMSET, m_ioctl) == -1");
		}
	}

	void failWithIllegalStateException() {
		throw new PureJavaIllegalStateException("File descriptor is " + this.m_FD + " < 0, maybe closed by previous error condition");
	}

	void checkState() {
		if (this.m_FD < 0) {
			failWithIllegalStateException();
		}
	}

	void checkReturnCode(int code) {
		if (code != 0) {
			String msg = String.format("JTermios call returned %d at %s", code, lineno(1));
			log = log && log(1, "%s\n", msg);
			try {
				close();
			} catch (Exception e) {
				StackTraceElement st = e.getStackTrace()[0];
				String msg2 = String.format("close threw %s at class %s line% d", e.getClass().getName(), st.getClassName(), st.getLineNumber());
				log = log && log(1, "%s\n", msg2);
			}
			throw new PureJavaIllegalStateException(msg);
		}
	}

}
