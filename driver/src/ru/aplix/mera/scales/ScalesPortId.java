package ru.aplix.mera.scales;

import static java.util.Objects.requireNonNull;


/**
 * Scales port identifier.
 */
public final class ScalesPortId {

	private final ScalesService scalesService;
	private final String portId;
	private final ScalesProtocol protocol;
	private ScalesPort port;

	ScalesPortId(
			ScalesService scalesService,
			String portId,
			ScalesProtocol protocol) {
		requireNonNull(portId, "Scales device identifier not specified");
		requireNonNull(protocol, "Scales protocol not specified");
		this.scalesService = scalesService;
		this.portId = portId;
		this.protocol = protocol;
	}

	/**
	 * Scales port identifier.
	 *
	 * @return string identifier of the port the scales connected to.
	 */
	public final String getPortId() {
		return this.portId;
	}

	/**
	 * Scales protocol.
	 *
	 * @return scales protocol instance passed to constructor.
	 */
	public final ScalesProtocol getProtocol() {
		return this.protocol;
	}

	/**
	 * Returns the scales port identified by this identifier. Opens the port if
	 * not opened yet.
	 *
	 * <p>This method is thread-safe.</p>
	 *
	 * @return a scales port instance.
	 */
	public synchronized final ScalesPort getPort() {
		if (this.port != null) {
			return this.port;
		}
		return this.port = this.scalesService.openPort(this);
	}

	@Override
	public String toString() {
		if (this.protocol == null) {
			return super.toString();
		}
		return this.portId + " (" + this.protocol.getProtocolId() + ')';
	}

}
