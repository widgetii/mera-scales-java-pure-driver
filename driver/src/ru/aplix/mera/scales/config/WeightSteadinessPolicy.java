package ru.aplix.mera.scales.config;

import ru.aplix.mera.scales.ScalesPort;


/**
 * Weight steadiness detection policy.
 */
public interface WeightSteadinessPolicy {

	/**
	 * Starts a weight steadiness detection for the given scales port.
	 *
	 * <p>This method is called on very first weighing, or when significant
	 * weight change happened.</p>
	 *
	 * @param port scales port to create detector for.
	 *
	 * @return new weight steadiness detector.
	 */
	WeightSteadinessDetector startSteadinessDetection(ScalesPort port);

}
