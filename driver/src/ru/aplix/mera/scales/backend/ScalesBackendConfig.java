package ru.aplix.mera.scales.backend;


/**
 * Scales backend configuration.
 *
 * <p>This class is immutable. When a setter called a new instance created
 * with the target property changed.</p>
 */
public final class ScalesBackendConfig implements Cloneable {

	public static final ScalesBackendConfig DEFAULT_SCALES_BACKEND_CONFIG =
			new ScalesBackendConfig();

	private long minReconnectDelay = 1000L;
	private long maxReconnectDelay = 5000L;
	private long weighingPeriod = 2000L;

	private ScalesBackendConfig() {
	}

	/**
	 * The minimum delay between attempts to reconnect to the device.
	 *
	 * <p>During reconnection the backend repeatedly
	 * {@link ScalesDriver#requestStatus(ScalesRequest) request the status}
	 * with delays between requests, starting from the
	 * {@link #getMinReconnectDelay() minimum} and increasing up to the
	 * {@link #getMaxReconnectDelay() maximum}. When the maximum delay reached,
	 * the backend continues reconnection attempts with this delay between
	 * them.</p>
	 *
	 * @return delay in milliseconds.
	 */
	public final long getMinReconnectDelay() {
		return this.minReconnectDelay;
	}

	/**
	 * Sets the minimum delay between attempts to reconnect to the device.
	 *
	 * @param minReconnectDelay new delay in milliseconds.
	 *
	 * @return updated scales backend configuration.
	 *
	 * @see #getMinReconnectDelay()
	 */
	public final ScalesBackendConfig setMinReconnectDelay(
			long minReconnectDelay) {
		if (this.minReconnectDelay == minReconnectDelay) {
			return this;
		}

		final ScalesBackendConfig clone = clone();

		clone.minReconnectDelay = minReconnectDelay;

		return clone;
	}

	/**
	 * The maximum delay between attempts to reconnect to the device.
	 *
	 * @return delay in milliseconds.
	 *
	 * @see #getMinReconnectDelay()
	 */
	public final long getMaxReconnectDelay() {
		return this.maxReconnectDelay;
	}

	/**
	 * Sets the maximum delay between attempts to reconnect to the device.
	 *
	 * @param maxReconnectDelay new delay in milliseconds.
	 *
	 * @return updated scales backend configuration.
	 *
	 * @see #getMinReconnectDelay()
	 */
	public final ScalesBackendConfig setMaxReconnectDelay(
			long maxReconnectDelay) {
		if (this.maxReconnectDelay == maxReconnectDelay) {
			return this;
		}

		final ScalesBackendConfig clone = clone();

		clone.maxReconnectDelay = maxReconnectDelay;

		return clone;
	}

	/**
	 * Returns the period of weight measurements.
	 *
	 * <p>The backend continuously {@link ScalesDriver#requestWeight(
	 * ScalesRequest) requests the weight} from the driver. This is the delay
	 * between such requests.</p>
	 *
	 * <p>Note that this setting won't be used if a weight updater
	 * {@link ScalesDriverContext#updateWeightWith(WeightUpdater) provided}.</p>
	 *
	 * @return period in milliseconds.
	 */
	public final long getWeighingPeriod() {
		return this.weighingPeriod;
	}

	/**
	 * Sets the period of weight measurements.
	 *
	 * @param weighingPeriod new period in milliseconds.
	 *
	 * @return updated scales backend configuration.
	 *
	 * @see #getWeighingPeriod()
	 */
	public final ScalesBackendConfig setWeighingPeriod(long weighingPeriod) {
		if (this.weighingPeriod == weighingPeriod) {
			return this;
		}

		final ScalesBackendConfig clone = clone();

		clone.weighingPeriod = weighingPeriod;

		return clone;
	}

	@Override
	protected ScalesBackendConfig clone() {
		try {
			return (ScalesBackendConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

}
