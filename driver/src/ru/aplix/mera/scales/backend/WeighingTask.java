package ru.aplix.mera.scales.backend;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


class WeighingTask implements Runnable {

	private final ScalesBackend backend;
	private ScheduledFuture<?> future;
	private boolean enabled;

	WeighingTask(ScalesBackend backend) {
		this.backend = backend;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public synchronized void start() {
		this.enabled = true;
		schedule();
	}

	public synchronized void suspend() {
		if (this.enabled) {
			cancel();
		}
	}

	public synchronized void resume() {
		if (this.enabled) {
			schedule();
		}
	}

	public synchronized void stop() {
		this.enabled = false;
		cancel();
	}

	private void schedule() {
		this.future = this.backend.executor().scheduleAtFixedRate(
				this,
				0,
				this.backend.config().getWeighingPeriod(),
				TimeUnit.MILLISECONDS);
	}

	private void cancel() {
		this.future.cancel(false);
	}

}
