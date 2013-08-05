package ru.aplix.mera.scales.backend;


/**
 * Measured weight updated message.
 */
public interface WeightUpdate {

	/**
	 * Measured weight.
	 *
	 * @return weight in metric grams.
	 */
	int getWeight();

}
