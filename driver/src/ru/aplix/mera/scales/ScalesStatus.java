package ru.aplix.mera.scales;


/**
 * Scales operating status.
 */
public enum ScalesStatus {

	/**
	 * Connection to scales established.
	 */
	SCALES_CONNECTED,

	/**
	 * Error connecting to scales.
	 */
	SCALES_ERROR;

	public final boolean isError() {
		return this == SCALES_ERROR;
	}

}
