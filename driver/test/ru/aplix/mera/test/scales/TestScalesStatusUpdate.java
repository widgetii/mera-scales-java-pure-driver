package ru.aplix.mera.test.scales;

import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;
import ru.aplix.mera.scales.backend.ScalesRequest;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


public class TestScalesStatusUpdate implements ScalesStatusUpdate {

	public static final TestScalesStatusUpdate STOP_TEST =
			new TestScalesStatusUpdate();

	private final TestScalesDevice device;
	private final String error;
	private final boolean stopTest;

	public TestScalesStatusUpdate(TestScalesDevice device) {
		this.device = device;
		this.error = null;
		this.stopTest = false;
	}

	public TestScalesStatusUpdate(String error) {
		this.device = null;
		this.error = error;
		this.stopTest = false;
	}

	private TestScalesStatusUpdate() {
		this.device = null;
		this.error = null;
		this.stopTest = true;
	}

	public final boolean isStopTest() {
		return this.stopTest;
	}

	@Override
	public ScalesStatus getScalesStatus() {
		return this.device != null
				? ScalesStatus.CONNECTED : ScalesStatus.ERROR;
	}

	@Override
	public ScalesDevice getScalesDevice() {
		return this.device;
	}

	@Override
	public String getScalesError() {
		return this.error;
	}

	public void apply(ScalesRequest request) {
		if (!isStopTest()) {
			request.updateStatus(this);
		}
	}

}
