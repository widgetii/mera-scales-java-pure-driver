package ru.aplix.mera.test.scales;

import ru.aplix.mera.scales.ScalesDevice;


public class TestScalesDevice implements ScalesDevice {

	private final String deviceId;

	public TestScalesDevice(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String getDeviceId() {
		return this.deviceId;
	}

	@Override
	public String getDeviceType() {
		return "test";
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
