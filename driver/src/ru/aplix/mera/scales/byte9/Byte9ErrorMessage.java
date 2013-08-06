package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.util.StringUtil.byteToHexString;
import ru.aplix.mera.scales.ScalesErrorMessage;


public class Byte9ErrorMessage implements ScalesErrorMessage {

	private final Byte9Packet packet;

	public Byte9ErrorMessage(Byte9Packet packet) {
		if (packet.getCommand() != Byte9Command.BYTE9_ERROR_RETURN) {
			throw new IllegalArgumentException(
					"Not an error packet: " + packet);
		}
		this.packet = packet;
	}

	@Override
	public String getErrorMessage() {

		final StringBuilder out = new StringBuilder();
		final byte[] rawData = this.packet.rawData();

		out.append("Error ").append(byteToHexString(rawData[6]));
		out.append(" (byte #/command: 0x").append(byteToHexString(rawData[4]));
		out.append("value: 0x").append(byteToHexString(rawData[5]));
		out.append(')');

		return out.toString();
	}

	@Override
	public String toString() {
		if (this.packet == null) {
			return super.toString();
		}
		return "Byte9ErrorMessage[" + getErrorMessage() + ']';
	}

}
