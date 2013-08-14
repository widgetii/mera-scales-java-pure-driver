package ru.aplix.mera.scales.ap;

import ru.aplix.mera.scales.ScalesErrorMessage;


final class APErrorMesssage implements ScalesErrorMessage {

	static final APErrorMesssage AP_ERROR_MESSAGE = new APErrorMesssage();

	private APErrorMesssage() {
	}

	@Override
	public String getErrorMessage() {
		return "Invalid data packet received";
	}

	@Override
	public Throwable getCause() {
		return null;
	}

}
