package ru.aplix.mera.scales;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


final class ScalesStatusListener
		implements MeraConsumer<ScalesBackendHandle, ScalesStatusUpdate> {

	private static final long MIN_RECONNECTION_TIMEOUT = 1000L;
	private static final long MAX_RECONNECTION_TIMEOUT = 5000L;

	private final ScalesPort port;
	private ScalesBackendHandle handle;
	private ScalesStatusMessage lastStatus;
	private final Timer timer = new Timer();
	private PortReconnector reconnector;

	ScalesStatusListener(ScalesPort port) {
		this.port = port;
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
			if (updated && update.getScalesStatus().isError()) {
				scheduleReconnection(
						new Date(
								System.currentTimeMillis()
								+ MIN_RECONNECTION_TIMEOUT),
						MIN_RECONNECTION_TIMEOUT);
			}
		}

		if (updated) {
			this.port.portSubscriptions()
			.sendMessage(new ScalesStatusMessage(update));
		}
	}

	@Override
	public synchronized void consumerUnubscribed(ScalesBackendHandle handle) {
		if (this.reconnector != null) {
			this.reconnector.cancel();
			this.reconnector = null;
		}
	}

	final ScalesBackendHandle handle() {
		return this.handle;
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

	private synchronized void scheduleReconnection(
			Date firstTime,
			long period) {
		if (this.reconnector != null) {
			this.reconnector.cancel();
		}
		this.reconnector = new PortReconnector(this, period);
		this.timer.scheduleAtFixedRate(
				this.reconnector,
				firstTime,
				period);
	}

	private static final class PortReconnector extends TimerTask {

		private final ScalesStatusListener statusListener;
		private final long period;

		PortReconnector(ScalesStatusListener statusListener, long period) {
			this.statusListener = statusListener;
			this.period = period;
		}

		@Override
		public void run() {
			this.statusListener.handle().refreshStatus();

			final long newPeriod = Math.min(
					this.period * 2,
					MAX_RECONNECTION_TIMEOUT);

			if (newPeriod == this.period) {
				return;
			}

			final long thisExecutionTime =
					scheduledExecutionTime() - this.period;

			this.statusListener.scheduleReconnection(
					new Date(thisExecutionTime + newPeriod),
					newPeriod);
		}

	}

}