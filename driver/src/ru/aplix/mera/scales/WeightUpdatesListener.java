package ru.aplix.mera.scales;

import static ru.aplix.mera.scales.config.WeightSteadinessDetector.NON_STEADY_WEIGHT;
import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.WeightUpdate;
import ru.aplix.mera.scales.backend.WeightUpdateHandle;
import ru.aplix.mera.scales.config.WeightSteadinessDetector;


final class WeightUpdatesListener
		implements MeraConsumer<WeightUpdateHandle, WeightUpdate> {

	private final ScalesPort port;
	private int started = 0;
	private WeightUpdateHandle handle;
	private WeightSteadinessDetector steadinessDetector;
	private WeightMessage steadyWeight;

	WeightUpdatesListener(ScalesPort port) {
		this.port = port;
	}

	public final WeightMessage getSteadyWeight() {
		return this.steadyWeight;
	}

	public final boolean weightIsSteady() {
		return this.steadyWeight != null || this.steadinessDetector == null;
	}

	public final synchronized void start() {
		if (this.started++ == 0) {
			this.port.backendHandle().requestWeight(this);
		}
	}

	public final synchronized void stop() {
		if (--this.started == 0) {
			this.handle.unsubscribe();
			this.handle = null;
		}
	}

	@Override
	public void consumerSubscribed(WeightUpdateHandle handle) {
		this.handle = handle;
	}

	@Override
	public void messageReceived(WeightUpdate update) {
		if (weightIsSteady()) {
			checkLoad(update);
		} else {
			checkSteadiness(update);
		}
	}

	@Override
	public void consumerUnubscribed(WeightUpdateHandle handle) {
	}

	private void checkLoad(WeightUpdate update) {
		if (this.steadinessDetector != null
				&& !this.steadinessDetector.weightChanged(
						this.steadyWeight,
						update)) {
			return;
		}

		final WeightMessage steadyWeight = getSteadyWeight();

		this.steadyWeight = null;
		this.steadinessDetector =
				this.port.getConfig()
				.getWeightSteadinessPolicy()
				.startSteadinessDetection(this.port);

		if (checkSteadiness(update)) {
			return;
		}

		final int weight = update.getWeight();
		final int lastWeight;

		if (steadyWeight == null) {
			lastWeight = 0;
		} else {
			lastWeight = steadyWeight.getWeight();
		}

		final int weightDiff = weight - lastWeight;
		final boolean loaded = weightDiff >= 0;

		this.port.loadSubscriptions()
		.sendMessage(new LoadMessage(update, loaded));
	}

	private boolean checkSteadiness(WeightUpdate update) {

		final int steadyWeight = this.steadinessDetector.steadyWeight(update);

		if (steadyWeight == NON_STEADY_WEIGHT) {
			return false;
		}

		this.steadyWeight = new WeightMessage(steadyWeight);
		this.port.weightSubscriptions().sendMessage(this.steadyWeight);

		return true;
	}

}
