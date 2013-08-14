package ru.aplix.mera.scales.ap;

import static ru.aplix.mera.scales.ScalesStatus.SCALES_CONNECTED;
import static ru.aplix.mera.scales.ScalesStatus.SCALES_ERROR;
import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


public class APStatusUpdate implements ScalesStatusUpdate {

	static final APStatusUpdate AP_DISCONNECTED = new APStatusUpdate(null);

	private final APDevice device;

	APStatusUpdate(APDevice device) {
		this.device = device;
	}

	@Override
	public ScalesStatus getScalesStatus() {
		return this.device != null ? SCALES_CONNECTED : SCALES_ERROR;
	}

	@Override
	public ScalesDevice getScalesDevice() {
		return this.device;
	}

	@Override
	public String getScalesError() {
		if (this.device != null) {
			return null;
		}
		return "Not connected";
	}

}
