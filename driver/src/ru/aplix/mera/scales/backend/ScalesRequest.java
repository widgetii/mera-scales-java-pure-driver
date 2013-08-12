package ru.aplix.mera.scales.backend;


/**
 * Scales request passed to {@link ScalesDriver scales driver} request methods.
 *
 * <p>As any scales message receiver it can used to report errors or unexpected
 * scales status changes. It is expected that when the driver method can not
 * produce any result, an error should be reported through this interface.</p>
 */
public class ScalesRequest extends ScalesReceiver {

	ScalesRequest(ScalesBackend backend) {
		super(backend);
	}

}
