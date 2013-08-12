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

	/**
	 * Registers an action to perform on interrupt.
	 *
	 * <p>An interrupt may happen when scales backend is stopped, or when
	 * weighing interrupted due to disconnection. In this case the
	 * {@code action} will be called. This may be used e.g. to interrupt the
	 * waiting for response from device.</p>
	 *
	 * @param action action to perform on interrupt, or <code>null</code> to do
	 * nothing.
	 */
	public void onInterrupt(InterruptAction action) {
		backend().onInterrupt(action);
	}

	final void done() {
		backend().onInterrupt(null);
	}

}
