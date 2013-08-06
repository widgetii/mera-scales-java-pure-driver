package ru.aplix.mera.scales;

import ru.aplix.mera.scales.backend.WeightUpdate;


/**
 * Weight load/unload event message.
 *
 * <p>This message is sent when the scales are loaded or unloaded with new
 * weight. This happens when the weight changes after significant amount
 * of time it was steady.</p>
 */
public class LoadMessage {

	private final WeightUpdate update;
	private final boolean loaded;

	LoadMessage(WeightUpdate update, boolean loaded) {
		this.update = update;
		this.loaded = loaded;
	}

	/**
	 * The indicated weight.
	 *
	 * <p>Note that this value can not be relied upon. It may change in the
	 * next moment, because of the scales fluctuations, when the weight is
	 * dropped onto the scales.</p>
	 *
	 * @return weight in metric grams.
	 */
	public final int getWeight() {
		return this.update.getWeight();
	}

	/**
	 * Whether the weight is loaded or unloaded.
	 *
	 * @return <code>true</code> if the weight on the scales have been
	 * increased, or <code>false</code> otherwise.
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	@Override
	public String toString() {
		if (this.update == null) {
			return super.toString();
		}
		return "LoadMessage[" + getWeight()
				+ (this.loaded ? "g loaded]" : "g unloaded]");
	}

}
