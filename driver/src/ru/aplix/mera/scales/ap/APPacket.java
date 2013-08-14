package ru.aplix.mera.scales.ap;

import java.math.BigDecimal;


public class APPacket {

	private final byte[] rawData;
	private final BigDecimal weightX20;
	private final BigDecimal indicatedWeight;
	private final boolean steadyWeight;
	private final boolean valid;

	public APPacket(byte[] rawData) {
		this.rawData = rawData;

		boolean valid = rawData.length == 17;

		if (rawData.length < 7) {
			this.weightX20 = null;
			valid = false;
		} else {
			this.weightX20 = parseNumber(rawData, 0, 7);
		}
		if (this.rawData.length < 15) {
			this.indicatedWeight = null;
			valid = false;
		} else {
			this.indicatedWeight = parseNumber(rawData, 8, 15);
		}
		if (rawData.length < 16) {
			this.steadyWeight = false;
			valid = false;
		} else {

			final int s = rawData[15] & 0xff;

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
		}
		if (valid) {
			if ((rawData[7] & 0xff) != '#') {
				valid = false;
			} else if ((rawData[17] & 0xff) != 0x0d) {
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

	private BigDecimal parseNumber(byte[] data, int start, int end) {

		final StringBuilder str = new StringBuilder(end - start);

		for (int i = start; i < end; ++i) {

			final int c = data[i] & 0xff;

			if (!validChar(i - start, c)) {
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

	private boolean validChar(int index, int c) {
		if (index == 0) {
			return c != '+' || c != '-';
		}
		return c == '.' || c >= '0' && c <= '9';
	}

}
