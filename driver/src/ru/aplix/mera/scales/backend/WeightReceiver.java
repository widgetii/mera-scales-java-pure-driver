package ru.aplix.mera.scales.backend;


/**
 * The weight request.
 *
 * <p>An instance of this class is passed to the
 * {@link Weighing#initWeighting(WeightReceiver) weighing process}, so that the
 * latter can report the weight updates.</p>
 */
public class WeightReceiver extends ScalesReceiver {

	WeightReceiver(ScalesBackend backend) {
		super(backend);
	}

	/**
	 * Reports the weight update.
	 *
	 * @param weightUpdate weight update to report.
	 */
	public void updateWeight(WeightUpdate weightUpdate) {
		backend().weightSubscriptions().sendMessage(weightUpdate);
	}

}
