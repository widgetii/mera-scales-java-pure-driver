package ru.aplix.mera.tester.dummy;

import static ru.aplix.mera.scales.ScalesStatus.SCALES_CONNECTED;
import static ru.aplix.mera.tester.dummy.DummyDevice.DUMMY_DEVICE;
import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


final class DummyStatusUpdate implements ScalesStatusUpdate {

	static final DummyStatusUpdate DUMMY_CONNECTED = new DummyStatusUpdate();

	@Override
	public ScalesStatus getScalesStatus() {
		return SCALES_CONNECTED;
	}

	@Override
	public ScalesDevice getScalesDevice() {
		return DUMMY_DEVICE;
	}

	@Override
	public String getScalesError() {
		return null;
	}

}
