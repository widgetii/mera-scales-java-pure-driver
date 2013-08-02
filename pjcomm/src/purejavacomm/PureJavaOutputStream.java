package purejavacomm;

import static jtermios.JTermios.tcdrain;

import java.io.IOException;
import java.io.OutputStream;


final class PureJavaOutputStream extends OutputStream {

	private final PureJavaSerialPort port;
	private final byte[] buffer = new byte[2048];

	PureJavaOutputStream(PureJavaSerialPort port) {
		this.port = port;
	}

	@Override
	public final void write(int b) throws IOException {
		this.port.checkState();
		byte[] buf = { (byte) b };
		write(buf, 0, 1);
	}

	@Override
	public final void write(
			byte[] buffer,
			int offset,
			int length)
	throws IOException {
		if (buffer == null) {
			throw new IllegalArgumentException();
		}
		if (offset < 0 || length < 0 || offset + length > buffer.length) {
			throw new IndexOutOfBoundsException(
					"buffer.length " + buffer.length
					+ " offset " + offset
					+ " length " + length);
		}
		this.port.checkState();

		int off = offset;
		int len = length;

		while (len > 0) {

			int n = buffer.length - off;

			if (n > this.buffer.length) {
				n = this.buffer.length;
			}
			if (n > len) {
				n = len;
			}
			if (off > 0) {
				System.arraycopy(buffer, off, this.buffer, 0, n);
				n = jtermios.JTermios.write(this.port.m_FD, this.buffer, n);
			} else {
				n = jtermios.JTermios.write(this.port.m_FD, buffer, n);
			}

			if (n < 0) {
				this.port.close();
				throw new IOException();
			}

			len -= n;
			off += n;
		}

		this.port.m_OutputEmptyNotified = false;
	}

	@Override
	public final void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public final void flush() throws IOException {
		this.port.checkState();
		if (tcdrain(this.port.m_FD) < 0) {
			close();
			throw new IOException();
		}
	}

}
