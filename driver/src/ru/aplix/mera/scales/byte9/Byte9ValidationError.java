package ru.aplix.mera.scales.byte9;

import ru.aplix.mera.scales.ScalesErrorMessage;


public class Byte9ValidationError implements ScalesErrorMessage {

	private final Byte9Validity validity;

	public Byte9ValidationError(Byte9Validity validity) {
		this.validity = validity;
	}

	@Override
	public String getErrorMessage() {
		return this.validity.toString();
	}

	@Override
	public Throwable getCause() {
		return null;
	}

	@Override
	public String toString() {
		if (this.validity == null) {
			return super.toString();
		}
		return this.validity.toString();
	}

}
