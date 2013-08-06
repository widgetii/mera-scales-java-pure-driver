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

	private final WeightUpdate update;

	WeightMessage(WeightUpdate update) {
		this.update = update;
	}

	public final int getWeight() {
		return this.update.getWeight();
	}

	@Override
	public String toString() {
		if (this.update == null) {
			return super.toString();
		}
		return "WeightMessage[" + getWeight() + "g]";
	}

}
