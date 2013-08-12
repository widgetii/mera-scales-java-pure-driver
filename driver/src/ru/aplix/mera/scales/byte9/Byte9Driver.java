package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.byte9.Byte9Packet.byte9DeviceIdRequest;
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

		final Byte9Packet response =
				new Byte9Session(request, this.portName)
				.send(byte9DeviceIdRequest());

		if (response == null) {
			return null;
		}

		return byte9Status(this.portName, response);
	}

	@Override
	public WeightUpdate requestWeight(
			ScalesRequest request)
	throws Exception {

		final Byte9Session session = new Byte9Session(request, this.portName);
		final Byte9Packet response =
				session.send(Byte9Packet.byte9WeightRequest());

		if (response == null) {
			return null;
		}

		return new Byte9WeightUpdate(session.getResponseTime(), response);
	}

}
