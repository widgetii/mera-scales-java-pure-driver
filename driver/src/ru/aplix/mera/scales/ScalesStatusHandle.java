package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


/**
 * Scales status updates handle.
 *
 * <p>It can be used to subscribe to other scale message types, such as weight
 * updates. When this subscription revoked, all subscriptions registered
 * through it are also revoked.</p>
 */
public final class ScalesStatusHandle
		extends MeraServiceHandle<ScalesStatusHandle, ScalesStatusMessage> {

	private final ScalesBackend backend;

	ScalesStatusHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super ScalesStatusHandle,
					? super ScalesStatusMessage> consumer) {
		super(backend, consumer);
		this.backend = backend;
	}

	/**
	 * Subscribe on weight updates.
	 *
	 * @param consumer weight updates consumer.
	 *
	 * @return weight updates handle.
	 */
	public final WeightHandle requestWeigth(
			MeraConsumer<
					? super WeightHandle,
					? super WeightMessage> consumer) {
		return addSubscription(
				this.backend.weightSubscriptions().subscribe(consumer));
	}

}
