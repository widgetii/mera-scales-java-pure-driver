package ru.aplix.mera.scales.config;

import ru.aplix.mera.scales.WeightMessage;
import ru.aplix.mera.scales.backend.WeightUpdate;


/**
 * Weight steadiness detector.
 *
 * <p>It is responsible for collecting the weight updates if necessary, and for
 * reporting whether the weight is steady.</p>
 *
 * <p>The weight updates are handled sequentially, one at a time. The
 * implementation is not required to be thread-safe.</p>
 */
public interface WeightSteadinessDetector {

	int NON_STEADY_WEIGHT = Integer.MIN_VALUE;

	/**
	 * Detects the steady weight.
	 *
	 * <p>This method will be called on each weight update, until it detects
	 * the steady weight. After that it won't be called on this detector
	 * instance any more.</p>
	 *
	 * <p>Note that some scales are able to detect the weight steadiness
	 * automatically. In this case the weight updated will be reported as
	 * {@link WeightUpdate#isSteadyWeight() steady}. The detector should decide
	 * whether to trust this information.</p>
	 *
	 * <p>The returned value will be used to create a {@link WeightMessage
	 * weight message}, which will be reported to subscribed weight consumers.
	 * </p>
	 *
	 * @param weightUpdate the last weight update.
	 *
	 * @return steady weight in grams, or {@link #NON_STEADY_WEIGHT} if the
	 * weight is not steady yet.
	 */
	int steadyWeight(WeightUpdate weightUpdate);

	/**
	 * Checks whether a steady weight have changed.
	 *
	 * <p>This method will be called on each weight update after a steady
	 * weight detected by {@link #steadyWeight(WeightUpdate)} method.
	 * It is responsible for the detection of significant weight changes,
	 * indicating that the scales have been loaded or unloaded, e.g. when
	 * the load have been removed from the scales.</p>
	 *
	 * <p>After this method returns a detector instance, it will replace a
	 * new one, ant the weight steadiness detection will start over over.</p>
	 *
	 * @param steadyWeight the last reported steady weight message, containing
	 * the steady weight returned by {@link #steadyWeight(WeightUpdate)} method.
	 * @param weightUpdate the last weight update.
	 *
	 * @return new detector if weight have changed significantly, or
	 * <code>null</code> otherwise.
	 */
	WeightSteadinessDetector weightChanged(
			WeightMessage steadyWeight,
			WeightUpdate weightUpdate);

}
