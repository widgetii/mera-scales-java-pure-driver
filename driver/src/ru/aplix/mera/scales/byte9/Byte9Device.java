package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.byte9.Byte9Command.BYTE9_DEVICE_ID_REQUEST;
import static ru.aplix.mera.scales.byte9.Byte9DeviceType.UNKNOWN_DEVICE_TYPE;
import static ru.aplix.mera.scales.byte9.Byte9DeviceType.byte9DeviceType;
import static ru.aplix.mera.util.StringUtil.byteToHexString;
import ru.aplix.mera.scales.ScalesDevice;


public class Byte9Device implements ScalesDevice {

	private final String deviceId;
	private final Byte9Packet packet;
	private final Byte9DeviceType deviceType;

	public Byte9Device(String deviceId, Byte9Packet packet) {
		this.deviceId = deviceId;
		this.packet = packet;
		if (!isDevicePacket()) {
			this.deviceType = UNKNOWN_DEVICE_TYPE;
		} else {
			this.deviceType = byte9DeviceType(this.packet.rawData()[4]);
		}
	}

	public final boolean isDevicePacket() {
		return this.packet.getCommand() == BYTE9_DEVICE_ID_REQUEST;
	}

	@Override
	public String getDeviceId() {
		return this.deviceId;
	}

	@Override
	public String getDeviceType() {
		if (!this.deviceType.isUnknown()) {
			return this.deviceType.id();
		}
		if (!isDevicePacket()) {
			return this.deviceType.id();
		}
		return this.deviceType.id() +
				" #" + byteToHexString(this.packet.rawData()[4]);
	}

	@Override
	public int getMajorRevision() {
		if (!isDevicePacket()) {
			return 0;
		}
		return this.packet.rawData()[5] & 0xff;
	}

	@Override
	public int getMinorRevision() {
		if (!isDevicePacket()) {
			return 0;
		}
		return this.packet.rawData()[6] & 0xff;
	}

	@Override
	public String toString() {
		if (this.deviceType == null) {
			return super.toString();
		}
		return "Byte9Device[" + getDeviceId()
				+ ": " + getDeviceType()
				+ ", v" + getMajorRevision()
				+ '.' + getMinorRevision() + ']';
	}

}
