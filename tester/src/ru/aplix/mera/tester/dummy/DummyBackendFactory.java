package ru.aplix.mera.tester.dummy;

import static ru.aplix.mera.tester.dummy.DummyProtocol.DUMMY_PROTOCOL;
import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


public class DummyBackendFactory implements ScalesBackendFactory {

	private static final String[] PORT_IDS = new String[] {"TEST"};

	@Override
	public ScalesProtocol getScalesProtocol() {
		return DUMMY_PROTOCOL;
	}

	@Override
	public String[] scalesPortIds() {
		return PORT_IDS;
	}

}
