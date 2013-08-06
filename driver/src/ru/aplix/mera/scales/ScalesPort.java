package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;


/**
 * Scales connection port.
 *
 * <p>Represents one scales device connected to the given port.</p>
 *
 * <p>The scales port produces an established status messages, like stable
 * weight measured after scales fluctuations stopped.</p>
 *
 * <p>The scales port uses an underlying {@link ScalesBackend scales backend}
 * to receive updates.</p>
 */
public class ScalesPort
		extends MeraService<ScalesPortHandle, ScalesStatusMessage> {

	private final ScalesBackend backend;
	private final ScalesStatusListener statusListener =
			new ScalesStatusListener(this);
	private final LoadSubscriptions loadSubscriptions =
			new LoadSubscriptions(this);
	private final WeightSubscriptions weightSubscriptions =
			new WeightSubscriptions(this);
	private final WeightUpdatesListener weightListener =
			new WeightUpdatesListener(this);

	ScalesPort(ScalesBackend backend) {
		this.backend = backend;
	}

	@Override
	protected ScalesPortHandle createServiceHandle(
			MeraConsumer<
					? super ScalesPortHandle,
					? super ScalesStatusMessage> consumer) {
		return new ScalesPortHandle(this, consumer);
	}

	@Override
	protected void startService() {
		backend().subscribe(this.statusListener);
	}

	@Override
	protected void stopService() {
		backendHandle().unsubscribe();
	}

	final ScalesBackend backend() {
		return this.backend;
	}

	final ScalesBackendHandle backendHandle() {
		return this.statusListener.handle();
	}

	final MeraSubscriptions<ScalesPortHandle, ScalesStatusMessage>
	portSubscriptions() {
		return serviceSubscriptions();
	}

	final MeraSubscriptions<LoadHandle, LoadMessage> loadSubscriptions() {
		return this.loadSubscriptions;
	}

	final MeraSubscriptions<WeightHandle, WeightMessage> weightSubscriptions() {
		return this.weightSubscriptions;
	}

	private static final class LoadSubscriptions
			extends MeraSubscriptions<LoadHandle, LoadMessage> {

		private final ScalesPort port;

		LoadSubscriptions(ScalesPort port) {
			this.port = port;
		}

		@Override
		protected LoadHandle createHandle(
				MeraConsumer<
						? super LoadHandle,
						? super LoadMessage> consumer) {
			return new LoadHandle(this.port, consumer);
		}

		@Override
		protected void firstSubscribed(LoadHandle handle) {
			this.port.weightListener.start();
			super.firstSubscribed(handle);
		}

		@Override
		protected void lastUnsubscribed(LoadHandle handle) {
			super.lastUnsubscribed(handle);
			this.port.weightListener.stop();
		}

	}

	private static final class WeightSubscriptions
			extends MeraSubscriptions<WeightHandle, WeightMessage> {

		private final ScalesPort port;

		WeightSubscriptions(ScalesPort port) {
			this.port = port;
		}

		@Override
		protected WeightHandle createHandle(
				MeraConsumer<
						? super WeightHandle,
						? super WeightMessage> consumer) {
			return new WeightHandle(this.port, consumer);
		}

		@Override
		protected void firstSubscribed(WeightHandle handle) {
			this.port.weightListener.start();
			super.firstSubscribed(handle);
		}

		@Override
		protected void lastUnsubscribed(WeightHandle handle) {
			super.lastUnsubscribed(handle);
			this.port.weightListener.stop();
		}

	}

}
