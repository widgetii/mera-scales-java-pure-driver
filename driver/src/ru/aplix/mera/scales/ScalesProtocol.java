package ru.aplix.mera.scales;

import ru.aplix.mera.scales.backend.ScalesBackend;


/**
 * Scales protocol implementation.
 */
public abstract class ScalesProtocol {

	private final String protocolName;

	/**
	 * Constructs new scales protocol implementation.
	 *
	 * @param protocolName human-readable protocol name.
	 */
	public ScalesProtocol(String protocolName) {
		this.protocolName = protocolName;
	}

	/**
	 * Scales protocol name.
	 *
	 * @return human-readable protocol name passed to constructor.
	 */
	public final String getProtocolName() {
		return this.protocolName;
	}

	/**
	 * Constructs a scales backend for the given device.
	 *
	 * @param deviceId scales device {@link ScalesPortId#getDeviceId()
	 * identifier}.
	 *
	 * @return new scales backend.
	 */
	protected abstract ScalesBackend createBackend(String deviceId);

}
