package ru.aplix.mera.scales.byte9;


public enum Byte9DeviceType {

	UNKNOWN_DEVICE_TYPE("unknown", (byte) 0),
	DETECTOR_DEVICE_TYPE("detector", (byte) 0xc9),
	SCALES_DEVICE_TYPE("scales", (byte) 0xc8),
	TEST_STAND_DEVICE_TYPE("test stand", (byte) 0xc7);

	public static Byte9DeviceType byte9DeviceType(byte code) {
		for (Byte9DeviceType type : values()) {
			if (code == type.code()) {
				return type;
			}
		}
		return UNKNOWN_DEVICE_TYPE;
	}

	private final String id;
	private final byte code;

	Byte9DeviceType(String id, byte code) {
		this.id = id;
		this.code = code;
	}

	public final boolean isUnknown() {
		return this == UNKNOWN_DEVICE_TYPE;
	}

	public final String id() {
		return this.id;
	}

	public final byte code() {
		return this.code;
	}

}
