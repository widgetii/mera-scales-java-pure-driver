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

		final long start = System.currentTimeMillis();
		final StatusRequest request = new StatusRequest(this.backend);
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

		reschedule(start);
	}

	private void reschedule(long start) {

		final long newPeriod = Math.min(
				this.period * 2,
				this.backend.config().getMaxReconnectDelay());
		final long nextStart = start + this.period;
		final long delay = nextStart - System.currentTimeMillis();

		new StatusRequestTask(this.backend)
		.schedule(delay < 0 ? 0 : delay, newPeriod);
	}

	private static final class StatusRequest extends ScalesRequest {

		private ScalesStatusUpdate statusUpdate;

		StatusRequest(ScalesBackend backend) {
			super(backend);
		}

		@Override
		public void updateStatus(ScalesStatusUpdate statusUpdate) {
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
