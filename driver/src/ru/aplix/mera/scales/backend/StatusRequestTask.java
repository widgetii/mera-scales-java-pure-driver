package ru.aplix.mera.scales.backend;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import ru.aplix.mera.scales.ThrowableErrorMessage;


final class StatusRequestTask implements Runnable {

	private final ScalesBackend backend;
	private long period;

	StatusRequestTask(ScalesBackend backend) {
		this.backend = backend;
	}

	public void schedule(long delay, long period) {
		this.period = period;
		this.backend.executor().schedule(this, delay, MILLISECONDS);
	}

	@Override
	public void run() {

		final ScalesRequest request = new ScalesRequest(this.backend);
		final long now = System.currentTimeMillis();
		final ScalesStatusUpdate status;

		try {
			status = this.backend.driver().requestStatus(request);
		} catch (Throwable e) {
			this.backend.errorSubscriptions()
			.sendMessage(new ThrowableErrorMessage(e));
			return;
		}

		if (status != null && this.backend.updateStatus(status)) {
			return;
		}

		reschedule(now);
	}

	private void reschedule(long now) {

		final long newPeriod = Math.min(
				this.period * 2,
				this.backend.config().getMaxReconnectDelay());

		new StatusRequestTask(this.backend)
		.schedule(now + this.period, newPeriod);
	}

}
