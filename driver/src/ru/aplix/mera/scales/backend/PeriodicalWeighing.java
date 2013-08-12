package ru.aplix.mera.scales.backend;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ru.aplix.mera.scales.ThrowableErrorMessage;


final class PeriodicalWeighing implements Weighing, Runnable {

	private final ScalesBackend backend;
	private ScheduledFuture<?> future;
	private boolean enabled;
	private WeightReceiver weightReceiver;

	PeriodicalWeighing(ScalesBackend backend) {
		this.backend = backend;
	}

	@Override
	public void initWeighting(WeightReceiver weightRequest) {
		this.weightReceiver = weightRequest;
	}

	@Override
	public void run() {

		final ScalesRequest request = new ScalesRequest(this.backend);
		final WeightUpdate weightUpdate;

		try {
			weightUpdate = this.backend.driver().requestWeight(request);
			if (weightUpdate != null) {
				this.weightReceiver.updateWeight(weightUpdate);
			}
		} catch (Throwable e) {
			this.backend.errorSubscriptions()
			.sendMessage(new ThrowableErrorMessage(e));
		} finally {
			request.done();
		}
	}

	@Override
	public synchronized void startWeighing() {
		this.enabled = true;
		schedule();
	}

	@Override
	public synchronized void suspendWeighing() {
		if (this.enabled) {
			cancel();
		}
	}

	@Override
	public synchronized void resumeWeighing() {
		if (this.enabled) {
			schedule();
		}
	}

	@Override
	public synchronized void stopWeighing() {
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
		this.backend.interrupt();
	}

}
