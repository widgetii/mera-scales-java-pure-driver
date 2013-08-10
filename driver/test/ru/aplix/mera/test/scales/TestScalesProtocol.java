package ru.aplix.mera.test.scales;

import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackend;


public class TestScalesProtocol extends ScalesProtocol {

	private TestScalesProtocol() {
		super("Test");
	}

	@Override
	protected ScalesBackend createBackend(String deviceId) {
		return new ScalesBackend(new TestScalesDriver(deviceId));
	}

}
