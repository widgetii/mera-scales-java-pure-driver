package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.byte9.Byte9Command.BYTE9_DEVICE_ID_REQUEST;
import static ru.aplix.mera.scales.byte9.Byte9Command.BYTE9_WEIGHT_REQUEST;
import static ru.aplix.mera.scales.byte9.Byte9Command.byte9Command;
import static ru.aplix.mera.scales.byte9.Byte9Validity.byte9CommandValidity;
import static ru.aplix.mera.scales.byte9.Byte9Validity.byte9DataValidity;
import static ru.aplix.mera.scales.byte9.Byte9Validity.byte9LengthValidity;

import java.util.Objects;

import ru.aplix.mera.util.CRC8;


/**
 * Byte9 protocol packed.
 *
 * @see "http://www.mera-device.ru/scales.pdf"
 */
public class Byte9Packet {

	/**
	 * Byte9 packet length.
	 */
	public static final int BYTE9_PACKET_LENGTH = 9;

	/**
	 * Byte9 packet terminator byte value.
	 */
	public static final byte BYTE9_TERMINATOR_BYTE = 0x0d;

	private static Byte9Packet simpleCommand(Byte9Command command) {

		final byte c = command.code();
		final byte[] data =
				new byte[] {0, 0, 0, c, 0, 0, 0, c, BYTE9_TERMINATOR_BYTE};

		return new Byte9Packet(data);
	}

	private static final Byte9Packet DEVICE_ID_REQUEST =
			simpleCommand(BYTE9_DEVICE_ID_REQUEST);
	private static final Byte9Packet WEIGHT_REQUEST =
			simpleCommand(BYTE9_WEIGHT_REQUEST);

	/**
	 * Byte9 device identifier request command.
	 *
	 * @return packet to send to receive a device identifier.
	 */
	public static Byte9Packet byte9DeviceIdRequest() {
		return DEVICE_ID_REQUEST;
	}

	/**
	 * Byte9 weight request command.
	 *
	 * @return packet to send to receive a measured weight.
	 */
	public static Byte9Packet byte9WeightRequest() {
		return WEIGHT_REQUEST;
	}

	private final byte[] rawData;

	/**
	 * Constructs the Byte9 packet.
	 *
	 * @param rawData raw packet data. Should never be <code>null</code>.
	 */
	public Byte9Packet(byte[] rawData) {
		Objects.requireNonNull(
				rawData,
				"Byte9 packet raw data is not specified");
		this.rawData = rawData;
	}

	/**
	 * Address.
	 *
	 * @return address stored in address bytes <code>#0 - #2</code>.
	 */
	public final int getAddress() {

		final int a0 = this.rawData[0] & 0xff;
		final int a1 = this.rawData[1] & 0xff;
		final int a2 = this.rawData[2] & 0xff;

		return (a0 << 16) | (a1 << 8) | a2;
	}

	/**
	 * Command.
	 *
	 * @return command, which code is stored in byte <code>#3</code>.
	 */
	public final Byte9Command getCommand() {
		return byte9Command(commandByte());
	}

	/**
	 * Signed value.
	 *
	 * @return signed value stored in data bytes <code>#4 - #6</code>.
	 */
	public final int getSignedValue() {

		final int s = this.rawData[4] & 1;
		final int b1 = (this.rawData[4] & 0xff) >>> 1;
		final int b2 = this.rawData[5] & 0xff;
		final int b3 = this.rawData[6] & 0xff;
		final int base = ((b1 << 16) | (b2 << 8) | b3) - s;

		if (s == 0) {
			return base ^ 0;
		}

		return (-base) ^ 0x7fffff;
	}

	/**
	 * Unsigned value.
	 *
	 * @return unsigned value stored in data bytes <code>#4 - #6</code>.
	 */
	public final int getUnsignedValue() {

		final int s = this.rawData[4] & 1;
		final int b1 = (this.rawData[4] & 0xff) >>> 1;
		final int b2 = this.rawData[5] & 0xff;
		final int b3 = this.rawData[6] & 0xff;

		return (s << 23) | (b1 << 16) | (b2 << 8) | b3;
	}

	/**
	 * Calculates packet CRC.
	 *
	 * @return actual packet data CRC.
	 */
	public final byte calculateCRC() {

		final int len = Math.min(7, this.rawData.length);

		return CRC8.calcCRC8(this.rawData, 0, len);
	}

	/**
	 * Raw packet data.
	 *
	 * @return packet data passed to constructor.
	 */
	public final byte[] rawData() {
		return this.rawData;
	}

	/**
	 * Command byte.
	 *
	 * @return command byte in byte <code>#3</code>, or {@code 0 } if this
	 * packet is too short.
	 */
	public final byte commandByte() {
		return this.rawData[3];
	}

	/**
	 * Control sum byte.
	 *
	 * @return CRC stored in byte <code>#7</code>.
	 */
	public final int crcByte() {
		return this.rawData[7] & 0xff;
	}

	/**
	 * Terminator byte.
	 *
	 * @return terminator value stored in byte <code>#8</code>, or {@code 0} if
	 * this packet is too short.
	 */
	public final byte terminatorByte() {
		if (this.rawData.length < 9) {
			return 0;
		}
		return this.rawData[8];
	}

	/**
	 * Validates the packet.
	 *
	 * @return packed validation result.
	 */
	public Byte9Validity validate() {
		if (this.rawData.length != BYTE9_PACKET_LENGTH) {
			return byte9LengthValidity(this.rawData.length);
		}

		final boolean crcError = calculateCRC() != crcByte();
		final boolean wrongTerminator =
				this.rawData[8] != BYTE9_TERMINATOR_BYTE;

		if (!crcError && getCommand() == null) {
			return byte9CommandValidity(this.rawData[3], wrongTerminator);
		}

		return byte9DataValidity(crcError, wrongTerminator);
	}

	@Override
	public String toString() {
		if (this.rawData == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder(27);

		for (byte b : this.rawData) {

			final String str = Integer.toHexString(b & 0xff);

			if (str.length() == 1) {
				out.append("#0");
			} else {
				out.append("#");
			}

			out.append(str);
		}

		return out.toString();
	}

}
