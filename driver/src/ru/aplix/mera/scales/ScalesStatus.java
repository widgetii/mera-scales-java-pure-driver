package ru.aplix.mera.scales;


/**
 * Scales operating status.
 */
public enum ScalesStatus {

	/**
	 * Connection to scales established.
	 */
	CONNECTED,

	/**
	 * Error connecting to scales.
	 */
	ERROR;

	public final boolean isError() {
		return this == ERROR;
	}

}
