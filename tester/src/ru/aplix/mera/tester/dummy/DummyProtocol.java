package ru.aplix.mera.tester.dummy;

import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackend;


public class DummyProtocol extends ScalesProtocol {

	public static final DummyProtocol DUMMY_PROTOCOL = new DummyProtocol();

	private DummyProtocol() {
		super("Dummy");
	}

	@Override
	protected ScalesBackend createBackend(String portId) {
		return new ScalesBackend(new DummyDriver());
	}

}
