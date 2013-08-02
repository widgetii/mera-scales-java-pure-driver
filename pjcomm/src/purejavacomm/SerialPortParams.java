package purejavacomm;


/**
 * Serial port parameters.
 */
public final class SerialPortParams implements Cloneable {

	/**
	 * Default serial port parameters:
	 * 9600 baud,
	 * 8 data bits,
	 * 1 stop bit,
	 * no parity.
	 */
	public static final SerialPortParams DEFAULT_SERIAL_PORT_PARAMS =
			new SerialPortParams();

	public static String parityToString(int parity) {
		switch (parity) {
		case SerialPort.PARITY_EVEN: return "even";
		case SerialPort.PARITY_ODD: return "odd";
		case SerialPort.PARITY_MARK: return "mark";
		case SerialPort.PARITY_SPACE: return "space";
		case SerialPort.PARITY_NONE: return "no";
		default: return "unknown";
		}
	}

	private int baudRate = 9600;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;

	private SerialPortParams() {
	}

	public final int getBaudRate() {
		return this.baudRate;
	}

	public SerialPortParams setBaudRate(int baudRate) {
		if (this.baudRate == baudRate) {
			return this;
		}

		final SerialPortParams clone = clone();

		clone.baudRate = baudRate;

		return clone;
	}

	public final int getDataBits() {
		return this.dataBits;
	}

	public final SerialPortParams setDataBits(int dataBits) {
		if (this.dataBits == dataBits) {
			return this;
		}

		final SerialPortParams clone = clone();

		clone.dataBits = dataBits;

		return clone;
	}

	public final int getStopBits() {
		return this.stopBits;
	}

	public SerialPortParams setStopBits(int stopBits) {
		if (this.stopBits == stopBits) {
			return this;
		}

		final SerialPortParams clone = clone();

		clone.stopBits = stopBits;

		return clone;
	}

	public final int getParity() {
		return this.parity;
	}

	public final SerialPortParams setParity(int parity) {
		if (this.parity == parity) {
			return this;
		}

		final SerialPortParams clone = clone();

		clone.parity = parity;

		return clone;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.baudRate;
		result = prime * result + this.dataBits;
		result = prime * result + this.parity;
		result = prime * result + this.stopBits;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final SerialPortParams other = (SerialPortParams) obj;

		if (this.baudRate != other.baudRate) {
			return false;
		}
		if (this.dataBits != other.dataBits) {
			return false;
		}
		if (this.parity != other.parity) {
			return false;
		}
		if (this.stopBits != other.stopBits) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "SerialPortParams["
				+ this.baudRate + " baud, "
				+ this.dataBits + " data bits, "
				+ this.stopBits + " stop bits,"
				+ parityToString(this.parity) + "]";
	}

	@Override
	protected SerialPortParams clone() {
		try {
			return (SerialPortParams) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
	}

}
