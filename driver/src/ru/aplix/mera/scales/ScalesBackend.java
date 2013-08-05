package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;


/**
 * Abstract weigher device back-end.
 */
public abstract class ScalesBackend
		extends MeraService<ScalesStatusHandle, ScalesStatusMessage> {

	private final WeightSubscription weightSubscription =
			new WeightSubscription(this);

	/**
	 * Weight updates subscriptions.
	 *
	 * @return all weight updates subscriptions.
	 */
	protected final MeraSubscriptions<
			WeightHandle,
			WeightMessage> weightSubscriptions() {
		return this.weightSubscription;
	}

	/**
	 * Called to start the weighing.
	 */
	protected abstract void startWeighing();

	/**
	 * Called to stop the weighing.
	 */
	protected abstract void stopWeighing();

	private static final class WeightSubscription
			extends MeraSubscriptions<WeightHandle, WeightMessage> {

		private final ScalesBackend backend;

		WeightSubscription(ScalesBackend backend) {
			this.backend = backend;
		}

		@Override
		protected WeightHandle createHandle(
				MeraConsumer<
						? super WeightHandle,
						? super WeightMessage> consumer) {
			return new WeightHandle(this.backend, consumer);
		}

		@Override
		protected void firstSubscribed(WeightHandle handle) {
			super.firstSubscribed(handle);
			this.backend.startWeighing();
		}

		@Override
		protected void lastUnsubscribed(WeightHandle handle) {
			super.lastUnsubscribed(handle);
			this.backend.stopWeighing();
		}

	}

}
