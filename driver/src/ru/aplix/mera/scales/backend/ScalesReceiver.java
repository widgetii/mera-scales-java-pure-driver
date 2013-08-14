package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesErrorMessage;
import ru.aplix.mera.scales.config.ScalesConfig;


/**
 * Scales messages receiver.
 *
 * <p>It can be used to report errors or unexpected scales status changes.</p>
 */
public abstract class ScalesReceiver {

	private final ScalesBackend backend;

	ScalesReceiver(ScalesBackend backend) {
		this.backend = backend;
	}

	/**
	 * Scales configuration.
	 *
	 * @return current scales configuration.
	 */
	public final ScalesConfig getConfig() {
		return backend().getConfig();
	}

	/**
	 * Reports unpredictable scales status update, such as disconnection.
	 *
	 * @param statusUpdate scales status update.
	 */
	public void updateStatus(ScalesStatusUpdate statusUpdate) {
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

	final ScalesBackend backend() {
		return this.backend;
	}

}
