package ru.aplix.mera.scales.ap;

import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.config.NaturalScalesOption;
import ru.aplix.mera.scales.config.StringScalesOption;


/**
 * Auto scales protocol.
 *
 * <p>The scales report the weight automatically, without any request.</p>
 */
public class AutoProtocol extends ScalesProtocol {

	public static final AutoProtocol AUTO_PROTOCOL = new AutoProtocol();

	/**
	 * The name of a client connecting to serial port.
	 */
	public static final StringScalesOption AP_CONNECTION_NAME =
			new StringScalesOption("connectionName", "Mera", AUTO_PROTOCOL);

	/**
	 * Serial port connection timeout.
	 */
	public static final NaturalScalesOption AP_CONNECTION_TIMEOUT =
			new NaturalScalesOption("connectionTimeout", 2000, AUTO_PROTOCOL);

	private AutoProtocol() {
		super("Auto");
	}

	@Override
	protected ScalesBackend createBackend(String portId) {
		return new ScalesBackend(new APDriver(portId));
	}

}
