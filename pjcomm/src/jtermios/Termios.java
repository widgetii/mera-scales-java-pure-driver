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
package jtermios;

import static jtermios.JTermios.*;
import static purejavacomm.SerialPort.*;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortParams;
import purejavacomm.UnsupportedCommOperationException;


public final class Termios {

	public int c_iflag;
	public int c_oflag;
	public int c_cflag;
	public int c_lflag;
	public byte[] c_cc = new byte[20];
	public int c_ispeed;
	public int c_ospeed;

	public void set(Termios s) {
		this.c_iflag=s.c_iflag;
		this.c_oflag=s.c_oflag;
		this.c_cflag=s.c_cflag;
		this.c_lflag=s.c_lflag;
		System.arraycopy(s.c_cc,0,this.c_cc,0,this.c_cc.length);
		this.c_ispeed=s.c_ispeed;
		this.c_ospeed=s.c_ospeed;
	}

	public final void setDataFormat(
			SerialPortParams params)
	throws UnsupportedCommOperationException {
		setDataFormat(
				params.getDataBits(),
				params.getStopBits(),
				params.getParity());
	}

	public final void setFlowControlMode(int mode) {
		this.c_iflag &= ~IXANY;

		if ((mode & (FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT)) != 0) {
			this.c_cflag |= CRTSCTS;
		} else {
			this.c_cflag &= ~CRTSCTS;
		}

		if ((mode & FLOWCONTROL_XONXOFF_IN) != 0) {
			this.c_iflag |= IXOFF;
		} else {
			this.c_iflag &= ~IXOFF;
		}

		if ((mode & FLOWCONTROL_XONXOFF_OUT) != 0) {
			this.c_iflag |= IXON;
		} else {
			this.c_iflag &= ~IXON;
		}
	}

	public final void setDataFormat(
			int dataBits,
			int stopBits,
			int parity)
	throws UnsupportedCommOperationException {

		final int db = dataBits(dataBits);
		final int sb = stopBits(stopBits);

		int fc = this.c_cflag & ~(PARENB | CMSPAR | PARODD);
		int fi = this.c_iflag & ~(INPCK | ISTRIP);

		switch (parity) {
		case SerialPort.PARITY_NONE:
			break;
		case SerialPort.PARITY_EVEN:
			fc |= PARENB;
			break;
		case SerialPort.PARITY_ODD:
			fc |= PARENB;
			fc |= PARODD;
			break;
		case SerialPort.PARITY_MARK:
			fc |= PARENB;
			fc |= CMSPAR;
			fc |= PARODD;
			break;
		case SerialPort.PARITY_SPACE:
			fc |= PARENB;
			fc |= CMSPAR;
			break;
		default:
			throw new UnsupportedCommOperationException(
					"parity = " + parity);
		}

		// update the hardware

		fc &= ~CSIZE; /* Mask the character size bits */
		fc |= db; /* Set data bits */

		if (sb == 2) {
			fc |= CSTOPB;
		} else {
			fc &= ~CSTOPB;
		}

		this.c_cflag = fc;
		this.c_iflag = fi;
	}

	private static int dataBits(
			int dataBits)
	throws UnsupportedCommOperationException {
		switch (dataBits) {
		case SerialPort.DATABITS_5:
			return CS5;
		case SerialPort.DATABITS_6:
			return CS6;
		case SerialPort.DATABITS_7:
			return CS7;
		case SerialPort.DATABITS_8:
			return CS8;
		default:
		}
		throw new UnsupportedCommOperationException(
				"dataBits = " + dataBits);
	}

	private static int stopBits(
			int stopBits)
	throws UnsupportedCommOperationException {
		switch (stopBits) {
		case SerialPort.STOPBITS_1:
			return 1;
		case SerialPort.STOPBITS_2:
			return 2;
		}
		throw new UnsupportedCommOperationException(
				"stopBits = " + stopBits);
	}

}
