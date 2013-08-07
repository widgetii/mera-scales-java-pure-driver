package ru.aplix.mera.scales.backend;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.util.concurrent.ScheduledExecutorService;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.message.MeraSubscriptions;
import ru.aplix.mera.scales.ScalesErrorHandle;
import ru.aplix.mera.scales.ScalesErrorMessage;


/**
 * Abstract weigher device back-end.
 */
public class ScalesBackend
		extends MeraService<ScalesBackendHandle, ScalesStatusUpdate> {

	private final ScalesDriver driver;
	private final ScalesBackendConfig config;
	private final ErrorSubscriptions errorSubscriptions =
			new ErrorSubscriptions();
	private final WeightSubscription weightSubscription =
			new WeightSubscription(this);
	private ScheduledExecutorService executor;
	private final WeighingTask weighing = new WeighingTask(this);

	public ScalesBackend(ScalesDriver driver) {
		this.driver = driver;
		this.config = driver.backendConfig();
	}

	public final ScalesBackendConfig config() {
		return this.config;
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
	}

	@Override
	protected void stopService() {
		this.executor.shutdownNow();
		this.executor = null;
	}

	final ScheduledExecutorService executor() {
		return this.executor;
	}

	final void refreshStatus() {
		this.weighing.suspend();
		new StatusRequestTask(this).schedule(
				0,
				config().getMinReconnectDelay());
	}

	final boolean updateStatus(ScalesStatusUpdate status) {

		final boolean error = status.getScalesStatus().isError();

		if (!error) {
			this.weighing.resume();
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
			this.backend.weighing.start();
		}

		@Override
		protected void lastUnsubscribed(WeightUpdateHandle handle) {
			super.lastUnsubscribed(handle);
			this.backend.weighing.stop();
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
