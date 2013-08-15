package ru.aplix.mera.scales.config;

import ru.aplix.mera.scales.ScalesPort;
import ru.aplix.mera.scales.WeightMessage;
import ru.aplix.mera.scales.backend.WeightUpdate;
import ru.aplix.mera.util.CyclicBuffer;


final class DefaultWeightSteadinessPolicy implements WeightSteadinessPolicy {

	static final WeightSteadinessPolicy DEFAULT_WEIGHT_STEADINESS_POLICY =
			new DefaultWeightSteadinessPolicy();

	/**
	 * The number of weight measures with the same weight preceding the current
	 * one necessary to consider the weight steady.
	 */
	private static final int STEADY_MEASURES = 2;

	/**
	 * Control delay, after the which the weight is considered steady.
	 */
	private static final long STEADINESS_DELAY = 5432L;

	@Override
	public WeightSteadinessDetector createSteadinessDetector(ScalesPort port) {
		return new DefaultWeightSteadinessDetector();
	}

	private static final class DefaultWeightSteadinessDetector
			implements WeightSteadinessDetector {

		private CyclicBuffer<WeightUpdate> lastUpdates =
				new CyclicBuffer<>(new WeightUpdate[STEADY_MEASURES]);

		@Override
		public int steadyWeight(WeightUpdate weightUpdate) {
			if (weightUpdate.isSteadyWeight()) {
				return weightUpdate.getWeight();
			}

			final boolean weightIsSteady = checkSteadiness(weightUpdate);

			this.lastUpdates.add(weightUpdate);

			return weightIsSteady
					? weightUpdate.getWeight() : NON_STEADY_WEIGHT;
		}

		@Override
		public WeightSteadinessDetector weightChanged(
				WeightMessage steadyWeight,
				WeightUpdate weightUpdate) {
			if (steadyWeight.getWeight() == weightUpdate.getWeight()) {
				return null;
			}
			this.lastUpdates.clear();
			return this;
		}

		private boolean checkSteadiness(WeightUpdate weightUpdate) {

			final int len = this.lastUpdates.length();
			final int firstIdx = len - STEADY_MEASURES;

			if (firstIdx < 0) {
				return false;// Too few measures.
			}

			final int weight = weightUpdate.getWeight();
			final WeightUpdate[] updates = this.lastUpdates.items();
			boolean steady = true;

			for (int i = firstIdx; i < STEADY_MEASURES; ++i) {

				final WeightUpdate up = updates[i];

				if (weight != up.getWeight()) {
					steady = false;
				}
			}

			if (!steady) {

				final WeightUpdate prev = this.lastUpdates.last();

				if (prev.getWeighingTime() - weightUpdate.getWeighingTime()
						< STEADINESS_DELAY) {
					// Measures are fresh enough.
					// Continue measuring.
					return false;
				}
				// Too many time passed since the last update.
				// Forcibly consider the weight as steady.
			}

			return true;
		}

	}

}
