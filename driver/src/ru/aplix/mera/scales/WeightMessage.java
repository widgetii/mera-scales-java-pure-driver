package ru.aplix.mera.scales;

import ru.aplix.mera.scales.backend.WeightUpdate;


/**
 * Weight message.
 *
 * <p>This message indicates the steady weight.</p>
 *
 * <p>After dropping onto the scales, the weight may start fluctuate. This
 * message is send when fluctuations stop. To find when they start, subscribe
 * for the {@link LoadMessage load event messages}.</p>
 */
public class WeightMessage {

	private final WeightUpdate firstUpdate;
	private final WeightUpdate lastUpdate;
	private final int weight;

	WeightMessage(
			int weight,
			WeightUpdate firstUpdate,
			WeightUpdate lastUpdate) {
		this.weight = weight;
		this.firstUpdate = firstUpdate;
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Measured weight.
	 *
	 * @return weight in grams.
	 */
	public final int getWeight() {
		return this.weight;
	}

	/**
	 * First weight update.
	 *
	 * @return weight update.
	 */
	public final WeightUpdate getFirstUpdate() {
		return this.firstUpdate;
	}

	/**
	 * Last weight update.
	 *
	 * @return weight update.
	 */
	public final WeightUpdate getLastUpdate() {
		return this.lastUpdate;
	}

	/**
	 * Weighing start time.
	 *
	 * @return UNIX time of first weight measurement, in milliseconds.
	 */
	public final long getWeighingTime() {
		return this.firstUpdate.getWeighingTime();
	}

	/**
	 * Weighing duration.
	 *
	 * @return duration in milliseconds between the first and the last
	 * weight measurements.
	 */
	public final long getWeighingDuration() {
		return this.lastUpdate.getWeighingTime()
				- this.firstUpdate.getWeighingTime();
	}

	@Override
	public String toString() {
		return "WeightMessage[" + getWeight() + " g]";
	}

}
