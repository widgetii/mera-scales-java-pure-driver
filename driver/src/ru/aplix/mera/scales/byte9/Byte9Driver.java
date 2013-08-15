package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.byte9.Byte9Packet.byte9DeviceIdRequest;
import static ru.aplix.mera.scales.byte9.Byte9Packet.byte9WeightRequest;
import static ru.aplix.mera.scales.byte9.Byte9StatusUpdate.byte9Status;
import ru.aplix.mera.scales.backend.*;


public class Byte9Driver implements ScalesDriver {

	private final String portName;

	public Byte9Driver(String portName) {
		this.portName = portName;
	}

	@Override
	public void initScalesDriver(ScalesDriverContext context) {
	}

	@Override
	public ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {
		try (Byte9Session session = new Byte9Session(request, this.portName)) {

			final Byte9Packet response = session.send(byte9DeviceIdRequest());

			if (response == null) {
				return null;
			}

			return byte9Status(this.portName, response);
		}
	}

	@Override
	public WeightUpdate requestWeight(
			ScalesRequest request)
	throws Exception {

		final long weighingStart = System.currentTimeMillis();

		try (Byte9Session session = new Byte9Session(request, this.portName)) {

			final Byte9Packet response = session.send(byte9WeightRequest());

			if (response == null) {
				return null;

			}
			return new Byte9WeightUpdate(
					weighingStart,
					session.getResponseTime(),
					response);
		}
	}

}
