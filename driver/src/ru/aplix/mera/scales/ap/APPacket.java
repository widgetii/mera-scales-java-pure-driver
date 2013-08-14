package ru.aplix.mera.scales.ap;

import java.math.BigDecimal;


public class APPacket {

	public static final byte AP_TERMINATOR_BYTE = 0x0d;

	private static final byte HASH_BYTE = (byte) '#';

	private final byte[] rawData;
	private final BigDecimal weightX20;
	private final BigDecimal indicatedWeight;
	private final boolean steadyWeight;
	private final boolean valid;

	public APPacket(byte[] rawData) {
		this.rawData = rawData;

		final int hashIdx = findHash(rawData);

		if (hashIdx <= 0) {
			this.weightX20 = null;
			this.indicatedWeight = null;
			this.steadyWeight = false;
			this.valid = false;
			return;
		}
		this.weightX20 = parseNumber(rawData, 0, hashIdx);

		boolean valid = true;
		final int steadyIdx = findNonNumber(rawData, hashIdx + 1);

		if (steadyIdx == hashIdx + 1) {
			this.indicatedWeight = null;
			valid = false;
		} else {
			this.indicatedWeight =
					parseNumber(rawData, hashIdx + 1, steadyIdx);
		}

		if (steadyIdx >= rawData.length) {
			valid = false;
			this.steadyWeight = false;
		} else {

			final int s = rawData[steadyIdx] & 0xff;

			// TODO: Is these weight steadiness flag values correct?
			// I assume steady is 'S' or 's', and dynamic is 'D' or 'd'.
			if (s == 'S' || s == 'D') {
				this.steadyWeight = true;
			} else if (s == 's' || s == 'd') {
				this.steadyWeight = false;
			} else {
				this.steadyWeight = false;
				valid = false;
			}

			final int termIdx = steadyIdx + 1;

			if (termIdx >= rawData.length) {
				valid = false;
			} else if (rawData[termIdx] != AP_TERMINATOR_BYTE) {
				valid = false;
			}
		}

		this.valid = valid;
	}

	public final byte[] rawData() {
		return this.rawData;
	}

	public final boolean isValid() {
		return this.valid;
	}

	public final BigDecimal getWeightX20() {
		return this.weightX20;
	}

	public final BigDecimal getIndicatedWeight() {
		return this.indicatedWeight;
	}

	public final boolean isSteadyWeight() {
		return this.steadyWeight;
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

	private static int findHash(byte[] rawData) {
		for (int i = 0; i < rawData.length; ++i) {
			if (rawData[i] == HASH_BYTE) {
				return i;
			}
		}
		return -1;
	}

	private static int findNonNumber(byte[] rawData, int start) {

		int i = start;

		for (; i < rawData.length; ++i) {
			if (!numberChar(i - start, rawData[i])) {
				break;
			}
		}

		return i;
	}

	private static BigDecimal parseNumber(byte[] data, int start, int end) {

		final StringBuilder str = new StringBuilder(end - start);

		for (int i = start; i < end; ++i) {

			final int c = data[i] & 0xff;

			if (!numberChar(i - start, c)) {
				return null;
			}

			str.appendCodePoint(c);
		}

		try {
			return new BigDecimal(str.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean numberChar(int index, int c) {
		if (index == 0) {
			return c != '+' || c != '-';
		}
		return c == '.' || c >= '0' && c <= '9';
	}

}
