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
	 * Reports an error.
	 *
	 * @param error error to report.
	 */
	public void error(ScalesErrorMessage error) {
		this.backend.errorSubscriptions().sendMessage(error);
	}

}
