package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesPort;
import ru.aplix.mera.scales.WeightHandle;
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
	 * Whether the measured weight is steady.
	 *
	 * <p>If the measured weight is steady, then the {@link WeightHandle weight}
	 * will be reported to the consumers immediately. Otherwise, the
	 * {@link ScalesPort} will try to detect the weight steadiness based on
	 * a few last weight updates.</p>
	 *
	 * @return <code>true</code> if weight is steady, or <code>false</code>
	 * otherwise.
	 */
	boolean isSteadyWeight();

	/**
	 * Measured weight.
	 *
	 * @return weight in metric grams.
	 */
	int getWeight();

	/**
	 * The time of weighing operation start.
	 *
	 * <p>This can be a time when weighing request sent.</p>
	 *
	 * @return UNIX time of weight measurement start, in milliseconds.
	 */
	long getWeighingStart();

	/**
	 * The time of weighing.
	 *
	 * <p>This can be a time when of weighing response reception.</p>
	 *
	 * @return UNIX time of weight measurement, in milliseconds.
	 */
	long getWeighingTime();

	/**
	 * The time of weighing operation end.
	 *
	 * <p>This can be a time when weighing response is fully read.</p>
	 *
	 * @return UNIX time of weight measurement start, in milliseconds.
	 */
	long getWeighingEnd();

}
