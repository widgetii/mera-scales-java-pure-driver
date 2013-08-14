package ru.aplix.mera.scales.ap;

import ru.aplix.mera.scales.ScalesDevice;


public class APDevice implements ScalesDevice {

	private final String deviceId;

	APDevice(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String getDeviceId() {
		return this.deviceId;
	}

	@Override
	public String getDeviceType() {
		return "scales";
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
