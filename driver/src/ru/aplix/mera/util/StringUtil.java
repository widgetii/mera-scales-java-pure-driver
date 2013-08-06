package ru.aplix.mera.util;


public final class StringUtil {

	public static String byteToHexString(byte value) {

		final String str = Integer.toHexString(value & 0xff);

		if (str.length() == 2) {
			return str;
		}

		return '0' + str;
	}

	private StringUtil() {
	}

}
