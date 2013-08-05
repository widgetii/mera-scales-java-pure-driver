package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;


/**
 * Abstract weigher device back-end.
 */
public abstract class ScalesBackend
		extends MeraService<ScalesBackendHandle, ScalesConnectionMessage> {

	private final WeightSubscription weightSubscription =
			new WeightSubscription(this);

	/**
	 * Weight updates subscriptions.
	 *
	 * @return all weight updates subscriptions.
	 */
	protected final MeraSubscriptions<
			MeasureHandle,
			MeasureMessage> weightSubscriptions() {
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
			extends MeraSubscriptions<MeasureHandle, MeasureMessage> {

		private final ScalesBackend backend;

		WeightSubscription(ScalesBackend backend) {
			this.backend = backend;
		}

		@Override
		protected MeasureHandle createHandle(
				MeraConsumer<
						? super MeasureHandle,
						? super MeasureMessage> consumer) {
			return new MeasureHandle(this.backend, consumer);
		}

		@Override
		protected void firstSubscribed(MeasureHandle handle) {
			super.firstSubscribed(handle);
			this.backend.startWeighing();
		}

		@Override
		protected void lastUnsubscribed(MeasureHandle handle) {
			super.lastUnsubscribed(handle);
			this.backend.stopWeighing();
		}

	}

}
