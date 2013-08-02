package purejavacomm;


/**
 * Serial port mode.
 */
public final class SerialPortMode implements Cloneable {

	/**
	 * Default serial port mode:
	 * {@link SerialPortParams#DEFAULT_SERIAL_PORT_PARAMS default} parameters,
	 * no flow control.
	 */
	public static final SerialPortMode DEFAULT_SERIAL_PORT_MODE =
			new SerialPortMode();

	private SerialPortParams params =
			SerialPortParams.DEFAULT_SERIAL_PORT_PARAMS;
	private int flowControlMode = SerialPort.FLOWCONTROL_NONE;

	private SerialPortMode() {
	}

	public final SerialPortParams getParams() {
		return this.params;
	}

	public final SerialPortMode setParams(SerialPortParams params) {
		if (this.params.equals(params)) {
			return this;
		}

		final SerialPortMode clone = clone();

		clone.params = params;

		return clone;
	}

	public final int getBaudRate() {
		return getParams().getBaudRate();
	}

	public final SerialPortMode setBaudRate(int baudRate) {
		return setParams(getParams().setBaudRate(baudRate));
	}

	public final int getDataBits() {
		return getParams().getDataBits();
	}

	public final SerialPortMode setDataBits(int dataBits) {
		return setParams(getParams().setDataBits(dataBits));
	}

	public final int getStopBits() {
		return getParams().getStopBits();
	}

	public final SerialPortMode setStopBits(int stopBits) {
		return setParams(getParams().setStopBits(stopBits));
	}

	public final int getParity() {
		return getParams().getParity();
	}

	public final SerialPortMode setParity(int parity) {
		return setParams(getParams().setParity(parity));
	}

	public final int getFlowControlMode() {
		return this.flowControlMode;
	}

	public final SerialPortMode setFlowControlMode(int flowControlMode) {
		if (this.flowControlMode == flowControlMode) {
			return this;
		}

		final SerialPortMode clone = clone();

		clone.flowControlMode = flowControlMode;

		return clone;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.flowControlMode;
		result = prime * result + this.params.hashCode();

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

		final SerialPortMode other = (SerialPortMode) obj;

		if (this.flowControlMode != other.flowControlMode) {
			return false;
		}
		if (!this.params.equals(other.params)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "SerialPortMode[params="
				+ this.params
				+ ", flowControlMode=0x"
				+ Integer.toHexString(this.flowControlMode) + "]";
	}

	@Override
	protected SerialPortMode clone() {
		try {
			return (SerialPortMode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
	}

}
