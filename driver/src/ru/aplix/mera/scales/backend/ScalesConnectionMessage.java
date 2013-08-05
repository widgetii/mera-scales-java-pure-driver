package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;


/**
 * Scales connection event message.
 *
 * <p>It informs about connection establishing, errors, and device
 * type, and software revision.
 */
public interface ScalesConnectionMessage {

	/**
	 * Scales status.
	 *
	 * @return connection status.
	 */
	ScalesStatus getScalesStatus();

	/**
	 * Scales device info.
	 *
	 * @return scales device info or <code>null</code> if it can not be obtained
	 * e.g. due to {@link ScalesStatus#ERROR error}.
	 */
	ScalesDevice getScalesDevice();

	/**
	 * Returns a scales error.
	 *
	 * @return error message or <code>null</code> if scales status is not
	 * {@link ScalesStatus#ERROR} or there is no message to display.
	 */
	String getScalesError();

}
