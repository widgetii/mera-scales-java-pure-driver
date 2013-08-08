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

		final StatusRequest request = new StatusRequest(this.backend);
		final long now = System.currentTimeMillis();
		ScalesStatusUpdate statusUpdate = null;

		try {
			statusUpdate = this.backend.driver().requestStatus(request);
		} catch (Throwable e) {
			this.backend.errorSubscriptions()
			.sendMessage(new ThrowableErrorMessage(e));
		}

		final ScalesStatusUpdate lastUpdate = request.lastUpdate(statusUpdate);

		if (lastUpdate != null && this.backend.updateStatus(lastUpdate)) {
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

	private static final class StatusRequest extends ScalesRequest {

		private ScalesStatusUpdate statusUpdate;

		StatusRequest(ScalesBackend backend) {
			super(backend);
		}

		@Override
		public void statusUpdate(ScalesStatusUpdate statusUpdate) {
			this.statusUpdate = statusUpdate;
		}

		final ScalesStatusUpdate lastUpdate(ScalesStatusUpdate statusUpdate) {
			if (statusUpdate != null) {
				return statusUpdate;
			}
			return this.statusUpdate;
		}

	}

}
