package ru.aplix.mera.scales.backend;


/**
 * The weight request.
 *
 * <p>This class is passed to the {@link Weighing#initWeighting(WeightRequest)
 * weighing process}, so that it can report the weight updates.</p>
 */
public class WeightRequest extends ScalesRequest {

	WeightRequest(ScalesBackend backend) {
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
