package ru.aplix.mera.message;


/**
 * Mera service.
 *
 * <p>Subscribe to service messages to gain a {@link MeraServiceHandle service
 * subscription handle}. This handle can be used to subscribe to other message
 * types.</p>
 *
 * @param <H>
 * @param <M>
 */
public abstract class MeraService<H extends MeraServiceHandle<H, M>, M> {

	private final ServiceSubscriptions serviceSubscriptions =
			new ServiceSubscriptions();

	/**
	 * Subscribe to this service.
	 *
	 * @param consumer service consumer.
	 *
	 * @return subscription handle.
	 */
	public final H subscribe(MeraConsumer<? super H, ? super M> consumer) {
		return serviceSubscriptions().subscribe(consumer);
	}

	/**
	 * Service subscriptions.
	 *
	 * @return all subscriptions to this service.
	 */
	protected MeraSubscriptions<H, M> serviceSubscriptions() {
		return this.serviceSubscriptions;
	}

	/**
	 * Invoked to create this service subscription handle.
	 *
	 * @param consumer service subscriber.
	 *
	 * @return new service subscription handle.
	 */
	protected abstract H createServiceHandle(
			MeraConsumer<? super H, ? super M> consumer);

	/**
	 * Starts the service.
	 *
	 * <p>This is invoked after the first subscription.</p>
	 */
	protected abstract void startService();

	/**
	 * Invoked on service subscription.
	 *
	 * <p>Does nothing by default.</p>
	 *
	 * @param handle subscription handle.
	 */
	protected void subscribed(H handle) {
	}

	/**
	 * Receives the message before any subscriber.
	 *
	 * <p>Does nothing by default.</p>
	 *
	 * @param message message received.
	 *
	 * @see MeraSubscriptions#messageReceived(Object)
	 */
	protected void messageReceived(M message) {
	}

	/**
	 * Invoked after service subscription is revoked.
	 *
	 * <p>Does nothing by default.</p>
	 *
	 * @param handle revoked subscription handle.
	 */
	protected void unsubscribed(H handle) {
	}

	/**
	 * Stops the service.
	 *
	 * <p>This is invoked when the last subscription revoked.</p>
	 */
	protected abstract void stopService();

	private final class ServiceSubscriptions extends MeraSubscriptions<H, M> {

		@Override
		protected H createHandle(MeraConsumer<? super H, ? super M> consumer) {
			return createServiceHandle(consumer);
		}

		@Override
		protected void firstSubscribed(H handle) {
			startService();
			super.firstSubscribed(handle);
		}

		@Override
		protected void subscribed(H handle) {
			super.subscribed(handle);
			MeraService.this.subscribed(handle);
		}

		@Override
		protected void messageReceived(M message) {
			super.messageReceived(message);
			MeraService.this.messageReceived(message);
		}

		@Override
		protected void unsubscribed(H handle) {
			handle.unsubscribeAll();
			super.unsubscribed(handle);
			MeraService.this.unsubscribed(handle);
		}

		@Override
		protected void lastUnsubscribed(H handle) {
			super.lastUnsubscribed(handle);
			stopService();
		}

	}

}
