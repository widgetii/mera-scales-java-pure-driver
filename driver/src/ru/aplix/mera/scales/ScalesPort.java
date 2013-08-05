package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraService;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;
import ru.aplix.mera.util.PeriodicalAction;


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

	private static final long MIN_RECONNECTION_TIMEOUT = 1000L;
	private static final long MAX_RECONNECTION_TIMEOUT = 5000L;

	private final ScalesBackend backend;
	private final ScalesStatusListener statusListener =
			new ScalesStatusListener(this);

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
		this.statusListener.handle().unsubscribe();
	}

	final ScalesBackend backend() {
		return this.backend;
	}

	private static final class ScalesStatusListener
			implements MeraConsumer<ScalesBackendHandle, ScalesStatusUpdate> {

		private final ScalesPort port;
		private final PortConnector connector = new PortConnector(this);
		private ScalesBackendHandle handle;
		private ScalesStatusMessage lastStatus;

		ScalesStatusListener(ScalesPort port) {
			this.port = port;
		}

		final ScalesBackendHandle handle() {
			return this.handle;
		}

		@Override
		public void consumerSubscribed(ScalesBackendHandle handle) {
			this.handle = handle;
		}

		@Override
		public void messageReceived(ScalesStatusUpdate update) {

			final boolean updated;

			synchronized (this) {
				updated = updateStatus(update);
				if (update.getScalesStatus().isError()) {
					this.connector.performEvery(MIN_RECONNECTION_TIMEOUT);
				}
			}

			if (updated) {
				this.port.serviceSubscriptions()
				.sendMessage(new ScalesStatusMessage(update));
			}
		}

		@Override
		public void consumerUnubscribed(ScalesBackendHandle handle) {
			this.connector.stop();
		}

		private boolean updateStatus(ScalesStatusUpdate message) {
			if (this.lastStatus == null) {
				this.lastStatus = new ScalesStatusMessage(message);
				return true;
			}

			final ScalesStatusMessage newStatus =
					this.lastStatus.update(message);

			if (newStatus == this.lastStatus) {
				return false;
			}

			this.lastStatus = newStatus;

			return true;
		}

	}

	private static final class PortConnector extends PeriodicalAction {

		private final ScalesStatusListener statusListener;

		PortConnector(ScalesStatusListener statusListener) {
			super(statusListener);
			this.statusListener = statusListener;
		}

		@Override
		protected boolean condition() {
			return this.statusListener.lastStatus.getScalesStatus().isError();
		}

		@Override
		protected void action() {
			this.statusListener.handle().refreshStatus();
			performEvery(Math.min(MAX_RECONNECTION_TIMEOUT, getTimeout() * 2));
		}

	}

}
