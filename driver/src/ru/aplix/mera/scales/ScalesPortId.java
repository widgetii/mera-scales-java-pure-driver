package ru.aplix.mera.scales;

import static java.util.Objects.requireNonNull;


/**
 * Scales port identifier.
 */
public final class ScalesPortId {

	private final ScalesService scalesService;
	private final String deviceId;
	private final ScalesProtocol protocol;

	ScalesPortId(
			ScalesService scalesService,
			String deviceId,
			ScalesProtocol protocol) {
		requireNonNull(deviceId, "Scales device identifier not specified");
		requireNonNull(protocol, "Scales protocol not specified");
		this.scalesService = scalesService;
		this.deviceId = deviceId;
		this.protocol = protocol;
	}

	/**
	 * Scales device identifier.
	 *
	 * @return string identifier passed to constructor.
	 */
	public final String getDeviceId() {
		return this.deviceId;
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
	 * Opens the scales port.
	 *
	 * @return new scales port instance identified by this device.
	 */
	public final ScalesPort openPort() {
		return this.scalesService.openPort(this);
	}

	@Override
	public String toString() {
		if (this.protocol == null) {
			return super.toString();
		}
		return this.deviceId + " (" + this.protocol.getProtocolName() + ')';
	}

}
