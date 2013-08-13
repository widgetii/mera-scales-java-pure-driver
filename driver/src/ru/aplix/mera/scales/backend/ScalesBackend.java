package ru.aplix.mera.scales.backend;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;
import ru.aplix.mera.scales.ScalesConfig;
import ru.aplix.mera.scales.ScalesErrorHandle;
import ru.aplix.mera.scales.ScalesErrorMessage;


/**
 * Abstract weigher device back-end.
 */
public class ScalesBackend
		extends MeraService<ScalesBackendHandle, ScalesStatusUpdate> {

	private final ScalesDriver driver;
	private final ScalesConfig defaultConfig;
	private ScalesConfig config;
	private final ErrorSubscriptions errorSubscriptions =
			new ErrorSubscriptions();
	private final WeightSubscription weightSubscription =
			new WeightSubscription(this);
	private final Weighing weighing;
	private ScheduledExecutorService executor;
	private volatile InterruptAction interruptAction;

	public ScalesBackend(ScalesDriver driver) {
		this.driver = driver;

		final ScalesDriverContext context = new ScalesDriverContext(this);

		context.initDriver(driver);

		this.config = this.defaultConfig = context.getConfig();
		this.weighing = context.getWeighting();
	}

	/**
	 * Default scales configuration.
	 *
	 * @return configuration {@link ScalesDriverContext#setConfig(ScalesConfig)
	 * set} by the driver.
	 */
	public final ScalesConfig getDefaultConfig() {
		return this.defaultConfig;
	}

	/**
	 * Scales configuration.
	 *
	 * @return scales configuration set by driver, or overridden with
	 * {@link #setConfig(ScalesConfig)} method.
	 */
	public final ScalesConfig getConfig() {
		return this.config;
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
	public void setConfig(ScalesConfig config) {

		final WriteLock lock = serviceSubscriptions().lock().writeLock();

		lock.lock();
		try {
			this.config = config != null ? config : getDefaultConfig();
			if (this.executor != null) {
				stopService();
				startService();
			}
		} finally {
			lock.unlock();
		}
	}

	protected final ScalesDriver driver() {
		return this.driver;
	}

	/**
	 * Error subscriptions.
	 *
	 * @return all error subscriptions.
	 */
	protected final MeraSubscriptions<
			ScalesErrorHandle,
			ScalesErrorMessage> errorSubscriptions() {
		return this.errorSubscriptions;
	}

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

	@Override
	protected ScalesBackendHandle createServiceHandle(
			MeraConsumer<
					? super ScalesBackendHandle,
					? super ScalesStatusUpdate> consumer) {
		return new ScalesBackendHandle(this, consumer);
	}

	@Override
	protected void startService() {
		this.executor = newSingleThreadScheduledExecutor();
		refreshStatus();
	}

	@Override
	protected void stopService() {
		interrupt();
		this.executor.shutdownNow();
		this.executor = null;
	}

	final ScheduledExecutorService executor() {
		return this.executor;
	}

	final void interrupt() {

		final InterruptAction interruptAction = this.interruptAction;

		if (interruptAction != null) {
			this.interruptAction.interrupt();
		}
	}

	final void refreshStatus() {
		this.weighing.suspendWeighing();
		new StatusRequestTask(this).schedule(
				0,
				getConfig().getMinReconnectDelay());
	}

	void onInterrupt(InterruptAction interruptAction) {
		this.interruptAction = interruptAction;
	}

	final boolean updateStatus(ScalesStatusUpdate status) {

		final boolean error = status.getScalesStatus().isError();

		if (!error) {
			this.weighing.resumeWeighing();
		}

		serviceSubscriptions().sendMessage(status);

		return !error;
	}

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
			this.backend.weighing.startWeighing();
		}

		@Override
		protected void lastUnsubscribed(WeightUpdateHandle handle) {
			super.lastUnsubscribed(handle);
			this.backend.weighing.stopWeighing();
		}

	}

	private static final class ErrorSubscriptions
			extends MeraSubscriptions<ScalesErrorHandle, ScalesErrorMessage> {

		@Override
		protected ScalesErrorHandle createHandle(
				MeraConsumer<
						? super ScalesErrorHandle,
						? super ScalesErrorMessage> consumer) {
			return new ScalesErrorHandle(this, consumer);
		}

	}

}
