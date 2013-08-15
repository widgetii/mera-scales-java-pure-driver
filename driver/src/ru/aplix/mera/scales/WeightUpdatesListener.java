package ru.aplix.mera.scales;

import static ru.aplix.mera.scales.config.WeightSteadinessDetector.NON_STEADY_WEIGHT;
import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.WeightUpdate;
import ru.aplix.mera.scales.backend.WeightUpdateHandle;
import ru.aplix.mera.scales.config.WeightSteadinessDetector;
import ru.aplix.mera.scales.config.WeightSteadinessPolicy;


final class WeightUpdatesListener
		implements MeraConsumer<WeightUpdateHandle, WeightUpdate> {

	private final ScalesPort port;
	private int started = 0;
	private WeightUpdateHandle handle;
	private WeightSteadinessPolicy steadinessPolicy;
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
	public void consumerUnsubscribed(WeightUpdateHandle handle) {
	}

	final synchronized void configUpdated() {

		final WeightSteadinessPolicy steadinessPolicy =
				this.port.getConfig().getWeightSteadinessPolicy();

		if (steadinessPolicy == this.steadinessPolicy) {
			return;
		}

		this.steadinessPolicy = steadinessPolicy;
		this.steadinessDetector =
				steadinessPolicy.createSteadinessDetector(this.port);
		this.steadyWeight = null;
	}

	private void checkLoad(WeightUpdate update) {

		final WeightMessage steadyWeight;

		synchronized (this) {
			if (!weightChanged(update)) {
				return;
			}
			steadyWeight = getSteadyWeight();
			this.steadyWeight = null;
			if (checkSteadiness(update)) {
				return;
			}
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

	private boolean weightChanged(WeightUpdate update) {
		if (this.steadinessDetector != null) {

			final WeightSteadinessDetector newDetector =
					this.steadinessDetector.weightChanged(
							this.steadyWeight,
							update);

			if (newDetector == null) {
				return false;
			}

			this.steadinessDetector = newDetector;

			return true;
		}

		final WeightSteadinessPolicy steadinessPolicy =
				this.port.getConfig().getWeightSteadinessPolicy();

		this.steadinessPolicy = steadinessPolicy;
		this.steadinessDetector =
				steadinessPolicy.createSteadinessDetector(this.port);

		return true;
	}

	private boolean checkSteadiness(WeightUpdate update) {
		synchronized (this) {

			final int steadyWeight = this.steadinessDetector.steadyWeight(update);

			if (steadyWeight == NON_STEADY_WEIGHT) {
				return false;
			}

			this.steadyWeight = new WeightMessage(steadyWeight);
		}

		this.port.weightSubscriptions().sendMessage(this.steadyWeight);

		return true;
	}

}
