package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.ScalesStatus.SCALES_CONNECTED;
import static ru.aplix.mera.scales.ScalesStatus.SCALES_ERROR;
import static ru.aplix.mera.scales.byte9.Byte9Command.BYTE9_DEVICE_ID_REQUEST;
import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


public class Byte9StatusUpdate implements ScalesStatusUpdate {

	public static Byte9StatusUpdate byte9Status(
			String deviceId,
			Byte9Packet packet) {
		return new Byte9StatusUpdate(deviceId, packet);
	}

	public static Byte9StatusUpdate byte9ErrorStatus(String error) {
		return new Byte9StatusUpdate(error);
	}

	private final Byte9Packet packet;
	private final Byte9Device device;
	private final String error;

	private Byte9StatusUpdate(String deviceId, Byte9Packet packet) {
		if (packet.getCommand() != BYTE9_DEVICE_ID_REQUEST) {
			throw new IllegalArgumentException(
					"Unexpected command: " + packet.getCommand()
					+ ", while " + BYTE9_DEVICE_ID_REQUEST + " expected");
		}
		this.packet = packet;
		this.device = new Byte9Device(deviceId, packet);
		this.error = null;
	}

	private Byte9StatusUpdate(String error) {
		this.packet = null;
		this.device = null;
		this.error = error;
	}

	@Override
	public ScalesStatus getScalesStatus() {
		if (this.packet == null) {
			return SCALES_ERROR;
		}
		return SCALES_CONNECTED;
	}

	@Override
	public ScalesDevice getScalesDevice() {
		return this.device;
	}

	@Override
	public String getScalesError() {
		return this.error;
	}

}
