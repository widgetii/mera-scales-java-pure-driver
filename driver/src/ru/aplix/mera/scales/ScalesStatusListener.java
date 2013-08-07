package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.ScalesBackendHandle;
import ru.aplix.mera.scales.backend.ScalesStatusUpdate;


final class ScalesStatusListener
		implements MeraConsumer<ScalesBackendHandle, ScalesStatusUpdate> {

	private final ScalesPort port;
	private ScalesBackendHandle handle;
	private ScalesStatusMessage lastStatus;

	ScalesStatusListener(ScalesPort port) {
		this.port = port;
	}

	@Override
	public void consumerSubscribed(ScalesBackendHandle handle) {
		this.handle = handle;
	}

	@Override
	public void messageReceived(ScalesStatusUpdate update) {
		if (updateStatus(update)) {
			this.port.portSubscriptions()
			.sendMessage(new ScalesStatusMessage(update));
		}
	}

	@Override
	public synchronized void consumerUnubscribed(ScalesBackendHandle handle) {
	}

	final ScalesBackendHandle handle() {
		return this.handle;
	}

	private synchronized boolean updateStatus(ScalesStatusUpdate message) {
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