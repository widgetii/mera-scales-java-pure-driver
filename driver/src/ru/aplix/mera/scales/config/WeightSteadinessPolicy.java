package ru.aplix.mera.scales.config;

import ru.aplix.mera.scales.ScalesPort;


/**
 * Weight steadiness detection policy.
 */
public interface WeightSteadinessPolicy {

	/**
	 * Creates a weight steadiness detector for the given scales port.
	 *
	 * <p>This method is called once per scales port.</p>
	 *
	 * @param port scales port to create detector for.
	 *
	 * @return new weight steadiness detector.
	 */
	WeightSteadinessDetector createSteadinessDetector(ScalesPort port);

}
