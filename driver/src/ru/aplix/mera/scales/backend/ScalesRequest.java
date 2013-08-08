package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesErrorMessage;


/**
 * Scales request passed to {@link ScalesDriver scales driver} request methods.
 *
 * <p>It can be used to report errors. It is expected, that when the driver
 * method can not produce any result, an error should be reported through this
 * interface.</p>
 */
public class ScalesRequest {

	private final ScalesBackend backend;

	ScalesRequest(ScalesBackend backend) {
		this.backend = backend;
	}

	/**
	 * Reports unpredictable scales status update, such as disconnection.
	 *
	 * @param statusUpdate scales status update.
	 */
	public void statusUpdate(ScalesStatusUpdate statusUpdate) {
		if (!this.backend.updateStatus(statusUpdate)) {
			backend().refreshStatus();
		}
	}

	/**
	 * Reports an error.
	 *
	 * @param error error to report.
	 */
	public void error(ScalesErrorMessage error) {
		backend().errorSubscriptions().sendMessage(error);
	}

	protected final ScalesBackend backend() {
		return this.backend;
	}

}
