package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraSubscriptions;


/**
 * Abstract weigher device back-end.
 */
public abstract class ScalesBackend {

	private final StatusSubscriptions statusSubscriptions =
			new StatusSubscriptions();
	private final WeightSubscription weightSubscription =
			new WeightSubscription();

	/**
	 * Subscribe on scales status updates.
	 *
	 * @param consumer status updates consumer.
	 *
	 * @return status updates handle.
	 */
	public final ScalesStatusHandle requestStatus(
			MeraConsumer<
					? super ScalesStatusHandle,
					? super ScalesStatusMessage> consumer) {
		return statusSubscriptions().subscribe(consumer);
	}

	/**
	 * Scales status updates subscriptions.
	 *
	 * @return all status updates subscriptions.
	 */
	protected final MeraSubscriptions<
			ScalesStatusHandle,
			ScalesStatusMessage> statusSubscriptions() {
		return this.statusSubscriptions;
	}

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
	 * Called to start updating the scales status.
	 */
	protected abstract void startStatusUpdates();

	/**
	 * Called to start the weighing.
	 */
	protected abstract void startWeighing();

	/**
	 * Called to stop the weighing.
	 */
	protected abstract void stopWeighing();

	/**
	 * Called to stop updating the scales status.
	 */
	protected abstract void stopStatusUpdates();

	private final class StatusSubscriptions
			extends MeraSubscriptions<ScalesStatusHandle, ScalesStatusMessage> {

		@Override
		protected ScalesStatusHandle createHandle(
				MeraConsumer<
						? super ScalesStatusHandle,
						? super ScalesStatusMessage> consumer) {
			return new ScalesStatusHandle(ScalesBackend.this, consumer);
		}

		@Override
		protected void firstSubscribed(ScalesStatusHandle handle) {
			super.firstSubscribed(handle);
			startStatusUpdates();
		}

		@Override
		protected void unsubscribed(ScalesStatusHandle handle) {
			super.unsubscribed(handle);
			handle.unsubscribeAll();
		}

		@Override
		protected void lastUnsubscribed(ScalesStatusHandle handle) {
			super.lastUnsubscribed(handle);
			stopStatusUpdates();
		}

	}

	private final class WeightSubscription
			extends MeraSubscriptions<WeightHandle, WeightMessage> {

		@Override
		protected WeightHandle createHandle(
				MeraConsumer<
						? super WeightHandle,
						? super WeightMessage> consumer) {
			return new WeightHandle(ScalesBackend.this, consumer);
		}

		@Override
		protected void firstSubscribed(WeightHandle handle) {
			super.firstSubscribed(handle);
			startWeighing();
		}

		@Override
		protected void lastUnsubscribed(WeightHandle handle) {
			super.lastUnsubscribed(handle);
			stopWeighing();
		}

	}

}
