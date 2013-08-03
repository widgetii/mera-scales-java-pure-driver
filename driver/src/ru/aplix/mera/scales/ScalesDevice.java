package ru.aplix.mera.scales;


/**
 * Scales device info.
 */
public interface ScalesDevice {

	/**
	 * Scales device identifier.
	 *
	 * @return string identifier of the scales device, e.g. port name.
	 */
	String getDeviceId();

	/**
	 * Scales device type identifier.
	 *
	 * @return string identifier of the type of the device, e.g.
	 * {@code "scales"}, or {@code "detector"}.
	 */
	String getDeviceType();

	/**
	 * Major software revision.
	 *
	 * @return major software revision number.
	 */
	int getMajorRevision();

	/**
	 * Minor software revision.
	 *
	 * @return minor software revision number.
	 */
	int getMinorRevision();

}
