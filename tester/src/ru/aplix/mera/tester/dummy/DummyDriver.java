package ru.aplix.mera.tester.dummy;

import static ru.aplix.mera.tester.dummy.DummyStatusUpdate.DUMMY_CONNECTED;
import ru.aplix.mera.scales.backend.*;


final class DummyDriver implements ScalesDriver {

	private static final long WEIGHING_PERIOD = 10000;
	private static final long FLUCTUATION_DURATION = 2000;

	private long start;
	private int weight;
	private boolean loading;

	@Override
	public void initScalesDriver(ScalesDriverContext context) {
		context.setConfig(context.getConfig().setWeighingPeriod(500L));
	}

	@Override
	public ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {
		return DUMMY_CONNECTED;
	}

	@Override
	public WeightUpdate requestWeight(ScalesRequest request) throws Exception {

		final long now = System.currentTimeMillis();

		if (now - this.start >= WEIGHING_PERIOD) {
			newWeight(now);
		}

		return reportWeight(now);
	}

	private void newWeight(long now) {
		this.start = now;
		this.loading = !this.loading;
		if (this.loading) {
			this.weight = 10 + (int) (Math.random() * 3210);
		} else {
			this.weight = 0;
		}
	}

	private DummyWeightUpdate reportWeight(long now) {
		if (!this.loading) {
			return new DummyWeightUpdate(now, this.weight);
		}

		final double leftToFluctuate =
				this.start + FLUCTUATION_DURATION - now;

		if (leftToFluctuate <= 0) {
			return new DummyWeightUpdate(now, this.weight);
		}

		return fluctuatedWeight(now, leftToFluctuate);
	}

	private DummyWeightUpdate fluctuatedWeight(
			long now,
			final double leftToFluctuate) {

		final double wave =
				Math.cos(leftToFluctuate / FLUCTUATION_DURATION * Math.PI * 3);
		final double fluctuation =
				wave * leftToFluctuate / FLUCTUATION_DURATION;
		final double weightDelta =
				(double) this.weight / 10 * fluctuation;
		final int currentWeight = this.weight + (int) weightDelta;

		return new DummyWeightUpdate(now, currentWeight);
	}

}
