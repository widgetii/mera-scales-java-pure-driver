package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesDevice;
import ru.aplix.mera.scales.ScalesStatus;
import ru.aplix.mera.scales.ScalesStatusMessage;


/**
 * Scales status update message.
 *
 * <p>In contrast to {@link ScalesStatusMessage}, this message is generated
 * on each status request.</p>
 */
public interface ScalesStatusUpdate {

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
