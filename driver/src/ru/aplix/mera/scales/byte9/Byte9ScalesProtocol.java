package ru.aplix.mera.scales.byte9;

import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackend;


public final class Byte9ScalesProtocol extends ScalesProtocol {

	public static final Byte9ScalesProtocol BYTE9_SCALES_PROTOCOL =
			new Byte9ScalesProtocol();

	private Byte9ScalesProtocol() {
		super("Byte9");
	}

	@Override
	protected ScalesBackend createBackend(String deviceId) {
		return new ScalesBackend(new Byte9Driver(deviceId));
	}

}
