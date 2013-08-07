package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.backend.ScalesBackendConfig.DEFAULT_SCALES_BACKEND_CONFIG;
import static ru.aplix.mera.scales.byte9.Byte9Packet.byte9DeviceIdRequest;
import static ru.aplix.mera.scales.byte9.Byte9StatusUpdate.byte9Status;
import ru.aplix.mera.scales.backend.*;


public class Byte9Driver implements ScalesDriver {

	private String deviceId = "COM6";

	public Byte9Driver(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public ScalesBackendConfig backendConfig() {
		return DEFAULT_SCALES_BACKEND_CONFIG;
	}

	@Override
	public ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {

		final Byte9Packet response =
				new Byte9Session(request, this.deviceId)
				.send(byte9DeviceIdRequest());

		if (response == null) {
			return null;
		}

		return byte9Status(this.deviceId, response);
	}

	@Override
	public WeightUpdate requestWeight(
			ScalesRequest request)
	throws Exception {

		final Byte9Session session = new Byte9Session(request, this.deviceId);
		final Byte9Packet response =
				session.send(Byte9Packet.byte9WeightRequest());

		if (response == null) {
			return null;
		}

		return new Byte9WeightUpdate(session.getResponseTime(), response);
	}

}
