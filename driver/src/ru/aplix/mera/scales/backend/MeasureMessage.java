package ru.aplix.mera.scales.backend;


/**
 * Measured weight message.
 */
public interface MeasureMessage {

	/**
	 * Returns measured weight.
	 *
	 * @return weight in metric grams.
	 */
	int getWeight();

}
