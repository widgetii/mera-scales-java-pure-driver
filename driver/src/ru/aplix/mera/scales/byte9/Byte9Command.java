package ru.aplix.mera.scales.byte9;


/**
 * Byte9 command codes.
 */
public enum Byte9Command {

	/** Device identifier request. */
	BYTE9_DEVICE_ID_REQUEST((byte) 0x01),

	/** Error return. */
	BYTE9_ERROR_RETURN((byte) 0x0f),

	/** Weight request. */
	BYTE9_WEIGHT_REQUEST((byte) 0x10);

	/**
	 * Returns the Byte9 command by its code.
	 *
	 * @param code command code extracted from Byte9 packet.
	 *
	 * @return corresponding command or <code>null</code> if the command code
	 * is not supported.
	 */
	public static Byte9Command byte9Command(byte code) {
		if (code > Registry.commands.length) {
			return null;
		}
		return Registry.commands[code];
	}

	private final byte code;

	Byte9Command(byte code) {
		this.code = code;
		Registry.commands[code] = this;
	}

	/**
	 * Command code.
	 *
	 * @return Byte9 packet command byte.
	 */
	public final byte code() {
		return this.code;
	}

	private static final class Registry {

		private static final Byte9Command[] commands =
				new Byte9Command[0x15 + 1];

	}

}
