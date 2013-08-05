package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;


/**
 * Abstract weigher device back-end.
 */
public abstract class ScalesBackend
		extends MeraService<ScalesBackendHandle, ScalesStatusUpdate> {

	private final WeightSubscription weightSubscription =
			new WeightSubscription(this);

	/**
	 * Weight updates subscriptions.
	 *
	 * @return all weight updates subscriptions.
	 */
	protected final MeraSubscriptions<
			WeightUpdateHandle,
			WeightUpdate> weightSubscriptions() {
		return this.weightSubscription;
	}

	/**
	 * Called to refresh the scales status.
	 */
	protected abstract void refreshStatus();

	/**
	 * Called to start the weighing.
	 */
	protected abstract void startWeighing();

	/**
	 * Called to stop the weighing.
	 */
	protected abstract void stopWeighing();

	private static final class WeightSubscription
			extends MeraSubscriptions<WeightUpdateHandle, WeightUpdate> {

		private final ScalesBackend backend;

		WeightSubscription(ScalesBackend backend) {
			this.backend = backend;
		}

		@Override
		protected WeightUpdateHandle createHandle(
				MeraConsumer<
						? super WeightUpdateHandle,
						? super WeightUpdate> consumer) {
			return new WeightUpdateHandle(this.backend, consumer);
		}

		@Override
		protected void firstSubscribed(WeightUpdateHandle handle) {
			super.firstSubscribed(handle);
			this.backend.startWeighing();
		}

		@Override
		protected void lastUnsubscribed(WeightUpdateHandle handle) {
			super.lastUnsubscribed(handle);
			this.backend.stopWeighing();
		}

	}

}
