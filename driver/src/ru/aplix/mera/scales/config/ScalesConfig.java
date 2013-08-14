package ru.aplix.mera.scales.config;

import static ru.aplix.mera.scales.config.DefaultWeightSteadinessPolicy.DEFAULT_WEIGHT_STEADINESS_POLICY;

import java.util.*;

import ru.aplix.mera.scales.backend.*;


/**
 * Scales configuration.
 *
 * <p>This class is immutable. When a setter called a new instance created
 * with the target option changed.</p>
 */
public final class ScalesConfig
		implements Iterable<ScalesOption<?>>, Cloneable {

	/**
	 * Default scales configuration.
	 */
	public static final ScalesConfig DEFAULT_SCALES_CONFIG = new ScalesConfig();

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
	public static final NaturalScalesOption MIN_RECONNECT_DELAY =
			new NaturalScalesOption("minReconnectDelay", 1000);

	/**
	 * The maximum delay in milliseconds between attempts to reconnect to the
	 * device.
	 *
	 * @see #MIN_RECONNECT_DELAY
	 */
	public static final NaturalScalesOption MAX_RECONNECT_DELAY =
			new NaturalScalesOption("maxReconnectDelay", 5000);

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
	public static final NaturalScalesOption WEIGHING_PERIOD =
			new NaturalScalesOption("weighingPeriod", 2000);

	/**
	 * Weight steadiness detection policy.
	 *
	 * <p>The default policy considers the weight is steady when the three
	 * subsequent weight updates contain the same weight value, or when the
	 * time since the last update is out. I also trust the weight updates
	 * {@link WeightUpdate#isSteadyWeight() steadiness}.</p>
	 */
	public static final ScalesOption<WeightSteadinessPolicy>
	WEIGHT_STEADINESS_POLICY =
			new ScalesOption<WeightSteadinessPolicy>(
					"weightSteadinessPolicy",
					WeightSteadinessPolicy.class) {
				@Override
				public WeightSteadinessPolicy getDefaultValue() {
					return DEFAULT_WEIGHT_STEADINESS_POLICY;
				}
				@Override
				public WeightSteadinessPolicy correctValue(
						WeightSteadinessPolicy value) {
					return value;
				}
			};

	private HashMap<ScalesOption<?>, Object> optionValues = new HashMap<>();

	private ScalesConfig() {
	}

	/**
	 * Whether this configuration is the default one.
	 *
	 * @return <code>true</code> if no option values changed in this
	 * configuration, or <code>false</code> otherwise.
	 */
	public final boolean isDefault() {
		return this.optionValues.isEmpty();
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
	 * Weight steadiness detection policy.
	 *
	 * @return policy.
	 *
	 * @see #WEIGHT_STEADINESS_POLICY
	 */
	public final WeightSteadinessPolicy getWeightSteadinessPolicy() {
		return get(WEIGHT_STEADINESS_POLICY);
	}

	/**
	 * Changes the weight steadiness detection policy.
	 *
	 * @param policy new policy or <code>null</code> to use a default one.
	 *
	 * @return updated configuration.
	 *
	 * @see #WEIGHT_STEADINESS_POLICY
	 */
	public final ScalesConfig setWeightSteadinessPolicy(
			WeightSteadinessPolicy policy) {
		return set(WEIGHT_STEADINESS_POLICY, policy);
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

		final T val = option.normalizeValue(value);
		final Object old = this.optionValues.get(option);

		if (Objects.equals(old, val)) {
			return this;
		}

		final ScalesConfig clone = clone();

		if (val != null) {
			clone.optionValues.put(option, val);
		} else {
			clone.optionValues.remove(option);
		}

		return clone;
	}

	/**
	 * Updates the configuration with options set in another one, leaving other
	 * options untouched.
	 *
	 * @param update configuration update.
	 *
	 * @return update configuration.
	 */
	public final ScalesConfig update(ScalesConfig update) {
		if (update.isDefault()) {
			return this;
		}

		ScalesConfig result = null;

		for (Map.Entry<ScalesOption<?>, Object> e
				: update.optionValues.entrySet()) {

			final ScalesOption<?> option = e.getKey();
			final Object newValue = e.getValue();
			final Object oldValue = this.optionValues.get(option);

			if (Objects.equals(oldValue, newValue)) {
				continue;
			}
			if (result == null) {
				result = clone();
			}
			result.optionValues.put(option, newValue);
		}

		if (result != null) {
			return result;
		}

		return this;
	}

	/**
	 * Iterates over option values.
	 *
	 * @return iterator over all options changed in this configuration.
	 */
	@Override
	public Iterator<ScalesOption<?>> iterator() {
		return this.optionValues.keySet().iterator();
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
