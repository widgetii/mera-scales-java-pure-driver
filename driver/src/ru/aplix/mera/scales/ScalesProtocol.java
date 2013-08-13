package ru.aplix.mera.scales;

import java.util.Objects;

import ru.aplix.mera.scales.backend.ScalesBackend;


/**
 * Scales protocol implementation.
 */
public abstract class ScalesProtocol {

	private final String protocolId;

	/**
	 * Constructs new scales protocol implementation.
	 *
	 * @param protocolId protocol identifier, can not be <code>null</code>.
	 */
	public ScalesProtocol(String protocolId) {
		Objects.requireNonNull(protocolId, "Protocol identifier not specified");
		this.protocolId = protocolId;
	}

	/**
	 * Scales protocol identifier.
	 *
	 * @return protocol identifier passed to the constructor.
	 */
	public final String getProtocolId() {
		return this.protocolId;
	}

	/**
	 * Constructs a scales backend for the given port.
	 *
	 * @param portId scales port {@link ScalesPortId#getPortId()
	 * identifier}.
	 *
	 * @return new scales backend.
	 */
	protected abstract ScalesBackend createBackend(String portId);

}
