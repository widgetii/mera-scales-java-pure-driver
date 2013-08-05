package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.WeightMessage;


/**
 * Measured weight updated message.
 *
 * <p>In contrast to the {@link WeightMessage weight message}, an update is sent
 * each time it is available. This is just a weight measured by scales at the
 * moment.</p>
 */
public interface WeightUpdate {

	/**
	 * Measured weight.
	 *
	 * @return weight in metric grams.
	 */
	int getWeight();

}
