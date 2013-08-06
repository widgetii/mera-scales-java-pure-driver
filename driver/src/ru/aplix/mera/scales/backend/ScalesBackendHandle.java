package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;
import ru.aplix.mera.scales.ScalesErrorHandle;
import ru.aplix.mera.scales.ScalesErrorMessage;


/**
 * Scales status updates handle.
 *
 * <p>It can be used to subscribe to other scale message types, such as weight
 * updates. When this subscription revoked, all subscriptions registered
 * through it are also revoked.</p>
 */
public final class ScalesBackendHandle extends MeraServiceHandle<
		ScalesBackendHandle,
		ScalesStatusUpdate> {

	private final ScalesBackend backend;

	ScalesBackendHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super ScalesBackendHandle,
					? super ScalesStatusUpdate> consumer) {
		super(backend, consumer);
		this.backend = backend;
	}

	/**
	 * Requests the updated status from the device.
	 */
	public final void refreshStatus() {
		this.backend.refreshStatus();
	}

	/**
	 * Subscribes the given consumer on error messages.
	 *
	 * @param consumer errors consumer.
	 *
	 * @return errors subscription handle.
	 */
	public final ScalesErrorHandle listenForErrors(
			MeraConsumer<
					? super ScalesErrorHandle,
					? super ScalesErrorMessage> consumer) {
		return addSubscription(
				this.backend.errorSubscriptions().subscribe(consumer));
	}

	/**
	 * Subscribes the given consumer on weight updates.
	 *
	 * @param consumer weight updates consumer.
	 *
	 * @return weight updates subscription handle.
	 */
	public final WeightUpdateHandle requestWeight(
			MeraConsumer<
					? super WeightUpdateHandle,
					? super WeightUpdate> consumer) {
		return addSubscription(
				this.backend.weightSubscriptions().subscribe(consumer));
	}

}
