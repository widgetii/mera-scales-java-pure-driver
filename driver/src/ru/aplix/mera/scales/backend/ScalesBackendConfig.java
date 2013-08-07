package ru.aplix.mera.scales.backend;


public final class ScalesBackendConfig implements Cloneable {

	public static final ScalesBackendConfig DEFAULT_SCALES_BACKEND_CONFIG =
			new ScalesBackendConfig();

	private long minReconnectDelay = 1000L;
	private long maxReconnectDelay = 5000L;
	private long weighingPeriod = 2000L;

	private ScalesBackendConfig() {
	}

	public final long getMinReconnectDelay() {
		return this.minReconnectDelay;
	}

	public final ScalesBackendConfig setMinReconnectDelay(
			long minReconnectDelay) {
		if (this.minReconnectDelay == minReconnectDelay) {
			return this;
		}

		final ScalesBackendConfig clone = clone();

		clone.minReconnectDelay = minReconnectDelay;

		return clone;
	}

	public final long getMaxReconnectDelay() {
		return this.maxReconnectDelay;
	}

	public final ScalesBackendConfig setMaxReconnectDelay(
			long maxReconnectDelay) {
		if (this.maxReconnectDelay == maxReconnectDelay) {
			return this;
		}

		final ScalesBackendConfig clone = clone();

		clone.maxReconnectDelay = maxReconnectDelay;

		return clone;
	}

	public final long getWeighingPeriod() {
		return this.weighingPeriod;
	}

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
