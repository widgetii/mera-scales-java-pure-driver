package ru.aplix.mera.scales;

import java.util.Objects;

import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


/**
 * Scales status message.
 *
 * <p>It informs about connection establishing, errors, device type, and
 * software revision.</p>
 *
 * <p>This message is generated as a result of one or more
 * {@link ScalesStatusUpdate} events.</p>
 *
 * <p>In case of connection errors, the {@link ScalesPort} periodically requests
 * the status updates until the status change (e.g. when the scales turned on
 * again).</p>
 */
public class ScalesStatusMessage {

	private final ScalesStatusUpdate lastUpdate;

	ScalesStatusMessage(ScalesStatusUpdate lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Scales status.
	 *
	 * @return connection status.
	 */
	public ScalesStatus getScalesStatus() {
		return this.lastUpdate.getScalesStatus();
	}

	/**
	 * Scales device info.
	 *
	 * @return scales device info or <code>null</code> if it can not be obtained
	 * e.g. due to {@link ScalesStatus#SCALES_ERROR error}.
	 */
	public ScalesDevice getScalesDevice() {
		return this.lastUpdate.getScalesDevice();
	}

	/**
	 * Returns a scales error.
	 *
	 * @return error message or <code>null</code> if scales status is not
	 * {@link ScalesStatus#SCALES_ERROR} or there is no message to display.
	 */
	public String getScalesError() {
		return this.lastUpdate.getScalesError();
	}

	ScalesStatusMessage update(ScalesStatusUpdate update) {
		if (!statusChanged(update)) {
			return this;
		}
		return new ScalesStatusMessage(update);
	}

	private boolean statusChanged(ScalesStatusUpdate update) {
		if (getScalesStatus() != update.getScalesStatus()) {
			return true;
		}
		if (!update.getScalesStatus().isError()) {
			return true;
		}
		return !Objects.equals(getScalesError(), update.getScalesError());
	}

}
