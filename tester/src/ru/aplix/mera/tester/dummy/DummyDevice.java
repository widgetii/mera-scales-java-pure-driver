package ru.aplix.mera.tester.dummy;

import ru.aplix.mera.scales.ScalesDevice;


final class DummyDevice implements ScalesDevice {

	static final DummyDevice DUMMY_DEVICE = new DummyDevice();

	private DummyDevice() {
	}

	@Override
	public String getDeviceId() {
		return "dummy";
	}

	@Override
	public String getDeviceType() {
		return "stub";
	}

	@Override
	public int getMajorRevision() {
		return 1;
	}

	@Override
	public int getMinorRevision() {
		return 0;
	}

}
