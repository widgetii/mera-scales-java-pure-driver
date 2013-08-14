package ru.aplix.mera.scales;


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

	private final int weight;

	WeightMessage(int weight) {
		this.weight = weight;
	}

	public final int getWeight() {
		return this.weight;
	}

	@Override
	public String toString() {
		return "WeightMessage[" + getWeight() + "g]";
	}

}
