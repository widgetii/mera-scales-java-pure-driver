package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;
import ru.aplix.mera.util.PeriodicalAction;


final class ScalesStatusListener
		implements MeraConsumer<ScalesBackendHandle, ScalesStatusUpdate> {

	private static final long MIN_RECONNECTION_TIMEOUT = 1000L;
	private static final long MAX_RECONNECTION_TIMEOUT = 5000L;

	private final ScalesPort port;
	private final PortConnector connector = new PortConnector(this);
	private ScalesBackendHandle handle;
	ScalesStatusMessage lastStatus;

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
			this.port.portSubscriptions()
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