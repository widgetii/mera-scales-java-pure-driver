package ru.aplix.mera.scales;

import java.util.HashMap;
import java.util.Objects;

import ru.aplix.mera.scales.backend.*;


/**
 * Scales configuration.
 *
 * <p>This class is immutable. When a setter called a new instance created
 * with the target option changed.</p>
 */
public final class ScalesConfig implements Cloneable {

	/**
	 * Default scales configuration.
	 */
	public static final ScalesConfig DEFAULT_SCALES_CONFIG =
			new ScalesConfig();

	/**
	 * The minimum delay in milliseconds between attempts to reconnect to the
	 * device.
	 *
	 * <p>During reconnection the backend repeatedly
	 * {@link ScalesDriver#requestStatus(ScalesRequest) request the status}
	 * with delays between requests, starting from the
	 * {@link #MIN_RECONNECT_DELAY minimum} and increasing up to the
	 * {@link #MAX_RECONNECT_DELAY maximum}. When the maximum delay reached,
	 * the backend continues reconnection attempts with this delay between
	 * them.</p>
	 */
	public static final ScalesOption<Long> MIN_RECONNECT_DELAY =
			new IntegerScalesOption("minReconnectDelay", 1000);

	/**
	 * The maximum delay in milliseconds between attempts to reconnect to the
	 * device.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public static final ScalesOption<Long> MAX_RECONNECT_DELAY =
			new IntegerScalesOption("maxReconnectDelay", 5000);

	/**
	 * The period in milliseconds of weight measurements.
	 *
	 * <p>The backend continuously {@link ScalesDriver#requestWeight(
	 * ScalesRequest) requests the weight} from the driver. This is the delay
	 * between such requests.</p>
	 *
	 * <p>Note that this setting won't be used when the weight updates are
	 * {@link ScalesDriverContext#updateWeightWith(Weighing) reported
	 * automatically}.</p>
	 */
	public static final ScalesOption<Long> WEIGHING_PERIOD =
			new IntegerScalesOption("minReconnectDelay", 2000);

	private HashMap<ScalesOption<?>, Object> optionValues = new HashMap<>();

	private ScalesConfig() {
	}

	/**
	 * The minimum delay between attempts to reconnect to the device.
	 *
	 * @return the value of {@link #MIN_RECONNECT_DELAY} option.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public final long getMinReconnectDelay() {
		return get(MIN_RECONNECT_DELAY).longValue();
	}

	/**
	 * Sets the minimum delay between attempts to reconnect to the device.
	 *
	 * @param minReconnectDelay new value of {@link #MIN_RECONNECT_DELAY}
	 * option.
	 *
	 * @return updated configuration.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public final ScalesConfig setMinReconnectDelay(
			long minReconnectDelay) {
		return set(MIN_RECONNECT_DELAY, minReconnectDelay);
	}

	/**
	 * The maximum delay between attempts to reconnect to the device.
	 *
	 * @return the value of {@link #MAX_RECONNECT_DELAY} option.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public final long getMaxReconnectDelay() {
		return get(MAX_RECONNECT_DELAY).longValue();
	}

	/**
	 * Sets the maximum delay between attempts to reconnect to the device.
	 *
	 * @param maxReconnectDelay new value of {@link #MAX_RECONNECT_DELAY}
	 * option.
	 *
	 * @return updated configuration.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public final ScalesConfig setMaxReconnectDelay(
			long maxReconnectDelay) {
		return set(MAX_RECONNECT_DELAY, maxReconnectDelay);
	}

	/**
	 * The period of weight measurements.
	 *
	 * @return the value of {@link #WEIGHING_PERIOD} option.
	 */
	public final long getWeighingPeriod() {
		return get(WEIGHING_PERIOD).longValue();
	}

	/**
	 * Sets the period of weight measurements.
	 *
	 * @param weighingPeriod new value of {@link #WEIGHING_PERIOD} option.
	 *
	 * @return updated configuration.
	 */
	public final ScalesConfig setWeighingPeriod(long weighingPeriod) {
		return set(WEIGHING_PERIOD, weighingPeriod);
	}

	/**
	 * Obtains the option's value.
	 *
	 * @param option option, which value to obtain.
	 *
	 * @return the {@link #set(ScalesOption, Object) previously set} value,
	 * or the {@link ScalesOption#getDefaultValue() default one} if not set.
	 */
	public final <T> T get(ScalesOption<T> option) {
		Objects.requireNonNull(option, "Option not specified");

		final Object value = this.optionValues.get(option);

		if (value == null) {
			return option.getDefaultValue();
		}

		return option.getValueType().cast(value);
	}

	/**
	 * Sets the option's value.
	 *
	 * @param option option, which value to set.
	 * @param value new option's value, or <code>null</code> to set it to
	 * default one.
	 *
	 * @return updates configuration.
	 */
	public final <T> ScalesConfig set(ScalesOption<T> option, T value) {
		Objects.requireNonNull(option, "Option not specified");

		final Object old = this.optionValues.get(option);

		if (Objects.equals(old, value)) {
			return this;
		}

		final ScalesConfig clone = clone();

		if (value != null) {
			clone.optionValues.put(option, value);
		} else {
			clone.optionValues.remove(option);
		}

		return clone;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ScalesConfig clone() {
		try {

			final ScalesConfig clone = (ScalesConfig) super.clone();
			final HashMap<ScalesOption<?>, Object> optionValues =
					(HashMap<ScalesOption<?>, Object>)
					this.optionValues.clone();

			clone.optionValues = optionValues;

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

}
