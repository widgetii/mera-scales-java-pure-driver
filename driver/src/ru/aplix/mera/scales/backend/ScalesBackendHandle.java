package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


/**
 * Scales status updates handle.
 *
 * <p>It can be used to subscribe to other scale message types, such as weight
 * updates. When this subscription revoked, all subscriptions registered
 * through it are also revoked.</p>
 */
public final class ScalesBackendHandle extends MeraServiceHandle<
		ScalesBackendHandle,
		ScalesConnectionMessage> {

	private final ScalesBackend backend;

	ScalesBackendHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super ScalesBackendHandle,
					? super ScalesConnectionMessage> consumer) {
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
	public final MeasureHandle requestWeigth(
			MeraConsumer<
					? super MeasureHandle,
					? super MeasureMessage> consumer) {
		return addSubscription(
				this.backend.weightSubscriptions().subscribe(consumer));
	}

}
