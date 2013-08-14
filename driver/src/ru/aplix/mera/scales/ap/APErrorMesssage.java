package ru.aplix.mera.scales.ap;

import ru.aplix.mera.scales.ScalesErrorMessage;


final class APErrorMesssage implements ScalesErrorMessage {

	private final APPacket packet;

	APErrorMesssage(APPacket packet) {
		this.packet = packet;
	}

	@Override
	public String getErrorMessage() {
		return "Invalid data packet received: " + this.packet;
	}

	@Override
	public Throwable getCause() {
		return null;
	}

}
