package ru.aplix.mera.scales.byte9;

import static ru.aplix.mera.scales.byte9.Byte9Packet.BYTE9_PACKET_LENGTH;
import static ru.aplix.mera.util.StringUtil.byteToHexString;


/**
 * Byte9 packet validation result.
 */
public final class Byte9Validity {

	static final Byte9Validity BYTE9_PACKET_VALID =
			new Byte9Validity(false, false);
	private static final Byte9Validity BYTE9_PACKET_WRONG_CRC_AND_TERMINATOR =
			new Byte9Validity(true, true);
	private static final Byte9Validity BYTE9_PACKET_WRONG_CRC =
			new Byte9Validity(true, false);
	private static final Byte9Validity BYTE9_PACKET_WRONG_TERMINATOR =
			new Byte9Validity(false, true);

	static Byte9Validity byte9LengthValidity(int length) {
		if (length != BYTE9_PACKET_LENGTH) {
			return new Byte9Validity(length);
		}
		return BYTE9_PACKET_VALID;
	}

	static Byte9Validity byte9DataValidity(
			boolean crcError,
			boolean wrongTerminator) {
		if (crcError) {
			if (wrongTerminator) {
				return BYTE9_PACKET_WRONG_CRC_AND_TERMINATOR;
			}
			return BYTE9_PACKET_WRONG_CRC;
		}
		if (wrongTerminator) {
			return BYTE9_PACKET_WRONG_TERMINATOR;
		}
		return BYTE9_PACKET_VALID;
	}

	static Byte9Validity byte9CommandValidity(
			byte command,
			boolean wrongTerminator) {
		if (command != 0) {
			return new Byte9Validity(command, wrongTerminator);
		}
		if (wrongTerminator) {
			return BYTE9_PACKET_WRONG_TERMINATOR;
		}
		return BYTE9_PACKET_VALID;
	}

	private final int length;
	private final boolean crcError;
	private final byte unsupportedCommand;
	private final boolean wrongTerminator;

	private Byte9Validity(int length) {
		this.length = length;
		this.crcError = false;
		this.wrongTerminator = false;
		this.unsupportedCommand = 0;
	}

	private Byte9Validity(boolean crcError, boolean wrongTerminator) {
		this.length = BYTE9_PACKET_LENGTH;
		this.crcError = crcError;
		this.wrongTerminator = wrongTerminator;
		this.unsupportedCommand = 0;
	}

	public Byte9Validity(byte unsupportedCommand, boolean wrongTerminator) {
		this.length = BYTE9_PACKET_LENGTH;
		this.crcError = false;
		this.unsupportedCommand = unsupportedCommand;
		this.wrongTerminator = wrongTerminator;
	}

	/**
	 * Whether Byte9 packet is valid.
	 *
	 * @return <code>true</code> if packet has no data errors, or
	 * <code>false</code> otherwise.
	 */
	public final boolean isValid() {
		return !hasWrongLength()
				&& !hasCRCError()
				&& !hasUnsupportedCommand()
				&& !hasWrongTerminator();
	}

	/**
	 * Whether Byte9 packed has wrong length.
	 *
	 * @return <code>true</code> if packet is not 9 bytes long.
	 */
	public final boolean hasWrongLength() {
		return this.length != BYTE9_PACKET_LENGTH;
	}

	/**
	 * Byte9 packet length.
	 *
	 * @return packet length.
	 */
	public final int getLength() {
		return this.length;
	}

	/**
	 * Whether Byte9 packed has wrong control sum.
	 *
	 * @return <code>true</code> if packed has wrong
	 * {@link Byte9Packet#getCRC() CRC}.
	 */
	public final boolean hasCRCError() {
		return this.crcError;
	}

	/**
	 * Whether Byte9 packet contains unsupported command.
	 *
	 * @return <code>true</code> if the command byte contains wrong value.
	 */
	public final boolean hasUnsupportedCommand() {
		return this.unsupportedCommand != 0;
	}

	/**
	 * Unsupported command.
	 *
	 * @return unsupported command code or <code>0</code> if command is
	 * supported.
	 */
	public final int getUnsupportedCommand() {
		return this.unsupportedCommand;
	}

	/**
	 * Whether Byte9 packet has wrong terminator.
	 *
	 * @return <code>true</code> if the last byte of the packed is not equal to
	 * {@link Byte9Packet#BYTE9_TERMINATOR_BYTE}.
	 */
	public final boolean hasWrongTerminator() {
		return this.wrongTerminator;
	}

	@Override
	public String toString() {
		if (isValid()) {
			return "Valid Byte9 packet";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("Invalid Byte9 packet (");
		if (hasWrongLength()) {
			out.append("length = ").append(this.length);
			comma = true;
		}
		if (hasCRCError()) {
			if (comma) {
				out.append(", ");
			}
			out.append("CRC error");
			comma = true;
		}
		if (hasUnsupportedCommand()) {
			if (comma) {
				out.append(", ");
			}
			out.append("unsupported command (");
			out.append('#').append(byteToHexString(this.unsupportedCommand));
			out.append(')');
		}
		if (hasWrongTerminator()) {
			if (comma) {
				out.append(", ");
			}
			out.append("wrong terminator");
		}
		out.append(')');

		return out.toString();
	}

}
