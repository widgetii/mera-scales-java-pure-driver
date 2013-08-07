package ru.aplix.mera.scales.backend;

import java.util.concurrent.TimeUnit;


final class StatusRequestTask implements Runnable {

	private final ScalesBackend backend;
	private long period;

	StatusRequestTask(ScalesBackend backend) {
		this.backend = backend;
	}

	@Override
	public void run() {

		final long now = System.currentTimeMillis();
		final ScalesStatusUpdate status =
				this.backend.driver().requestStatus(
						new ScalesRequest(this.backend));

		if (status != null && this.backend.updateStatus(status)) {
			return;
		}

		final long newPeriod = Math.min(
				this.period * 2,
				this.backend.config().getMaxReconnectDelay());

		new StatusRequestTask(this.backend)
		.schedule(now + this.period, newPeriod);
	}

	public void schedule(long delay, long period) {
		this.period = period;
		this.backend.executor().schedule(this, delay, TimeUnit.MILLISECONDS);
	}

}
