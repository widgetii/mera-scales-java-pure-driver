package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


/**
 * Scales port handle.
 *
 * <p>Other scales port event can be registered though it. When this
 * subscription revoked, all subscriptions registered through it are also
 * revoked.</p>
 */
public final class ScalesPortHandle
		extends MeraServiceHandle<ScalesPortHandle, ScalesStatusMessage> {

	private final ScalesPort port;

	ScalesPortHandle(
			ScalesPort port,
			MeraConsumer<
					? super ScalesPortHandle,
					? super ScalesStatusMessage> consumer) {
		super(port, consumer);
		this.port = port;
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
				this.port.backendHandle().listenForErrors(consumer));
	}

	/**
	 * Subscribes the given consumer on weight load/unload events.
	 *
	 * @param consumer load/unload consumer to subscribe.
	 *
	 * @return load/unload subscription handle.
	 */
	public final LoadHandle requestLoad(
			MeraConsumer<? super LoadHandle, ? super LoadMessage> consumer) {
		return addSubscription(
				this.port.loadSubscriptions().subscribe(consumer));
	}

	/**
	 * Subscribes the given consumer on weight messages.
	 *
	 * @param consumer weight consumer to subscribe.
	 *
	 * @return weight subscription handle.
	 */
	public final WeightHandle requestWeight(
			MeraConsumer<
					? super WeightHandle,
					? super WeightMessage> consumer) {
		return addSubscription(
				this.port.weightSubscriptions().subscribe(consumer));
	}

}
