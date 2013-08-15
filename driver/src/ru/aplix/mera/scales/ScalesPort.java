package ru.aplix.mera.scales;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;
import ru.aplix.mera.scales.backend.ScalesDriverContext;
import ru.aplix.mera.scales.config.ScalesConfig;


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
	private ScalesStatusMessage status;

	ScalesPort(ScalesBackend backend) {
		requireNonNull(backend, "Scales backend not specified");
		this.backend = backend;
	}

	/**
	 * Default scales configuration.
	 *
	 * @return configuration {@link ScalesDriverContext#setConfig(ScalesConfig)
	 * set} by the driver.
	 */
	public final ScalesConfig getDefaultConfig() {
		return backend().getConfig();
	}

	/**
	 * Scales configuration.
	 *
	 * @return scales configuration set by driver, or overridden with
	 * {@link #setConfig(ScalesConfig)} method.
	 */
	public final ScalesConfig getConfig() {
		return backend().getConfig();
	}

	/**
	 * Overrides scales configuration.
	 *
	 * <p>This method call causes the backend to restart if it is already
	 * running.</p>
	 *
	 * @param config new scales configuration, or <code>null</code> to reset it
	 * to driver's default one.
	 */
	public final void setConfig(ScalesConfig config) {

		final WriteLock lock = serviceSubscriptions().lock().writeLock();

		lock.lock();
		try {
			this.weightListener.configUpdated();
			this.status = null;
			backend().setConfig(config);
		} finally {
			lock.unlock();
		}
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
		this.status = null;
		backend().subscribe(this.statusListener);
	}

	@Override
	protected void subscribed(ScalesPortHandle handle) {
		if (this.status != null) {
			handle.getConsumer().messageReceived(this.status);
		}
	}

	@Override
	protected void messageReceived(ScalesStatusMessage message) {
		this.status = message;
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
		private LoadMessage load;

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
			this.load = null;
			this.port.weightListener.start();
			super.firstSubscribed(handle);
		}

		@Override
		protected void subscribed(LoadHandle handle) {
			super.subscribed(handle);
			if (this.load != null && !this.port.weightListener.weightIsSteady()) {
				handle.getConsumer().messageReceived(this.load);
			}
		}

		@Override
		protected void messageReceived(LoadMessage message) {
			this.load = message;
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
		private WeightMessage weight;

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
			this.weight = null;
			this.port.weightListener.start();
			super.firstSubscribed(handle);
		}

		@Override
		protected void subscribed(WeightHandle handle) {
			super.subscribed(handle);
			if (this.weight != null && this.port.weightListener.weightIsSteady()) {
				handle.getConsumer().messageReceived(this.weight);
			}
		}

		@Override
		protected void messageReceived(WeightMessage message) {
			this.weight = message;
		}

		@Override
		protected void lastUnsubscribed(WeightHandle handle) {
			super.lastUnsubscribed(handle);
			this.port.weightListener.stop();
		}

	}

}
