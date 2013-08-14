package ru.aplix.mera.scales.byte9;

import ru.aplix.mera.scales.*;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.config.NaturalScalesOption;
import ru.aplix.mera.scales.config.NonNegativeScalesOption;
import ru.aplix.mera.scales.config.StringScalesOption;


/**
 * Byte9 scales protocol.
 *
 * @see "http://www.mera-device.ru/scales.pdf"
 */
public final class Byte9Protocol extends ScalesProtocol {

	/**
	 * Singleton Byte9 scales protocol instance.
	 */
	public static final Byte9Protocol BYTE9_PROTOCOL = new Byte9Protocol();

	/**
	 * The name of a client connecting to serial port.
	 */
	public static final StringScalesOption BYTE9_CONNECTION_NAME =
			new StringScalesOption("connectionName", "Mera", BYTE9_PROTOCOL);

	/**
	 * Serial port connection timeout.
	 */
	public static final NaturalScalesOption BYTE9_CONNECTION_TIMEOUT =
			new NaturalScalesOption("connectionTimeout", 2000, BYTE9_PROTOCOL);

	/**
	 * Maximum number of times to retry to send the command.
	 *
	 * <p>By default, there is only one retry, i.e. the command will be sent
	 * at most twice.</p>
	 */
	public static final NonNegativeScalesOption BYTE9_COMMAND_RETRIES =
			new NonNegativeScalesOption("commandRetries", 1, BYTE9_PROTOCOL);

	/**
	 * Timeout in milliseconds to wait for response after sending request.
	 */
	public static final NaturalScalesOption BYTE9_RESPONSE_TIMEOUT =
			new NaturalScalesOption(
					"responseTimeout",
					1000,
					BYTE9_PROTOCOL);

	/**
	 * The delay in milliseconds between the sending of address and data.
	 */
	public static final NonNegativeScalesOption BYTE9_DATA_DELAY =
			new NonNegativeScalesOption("dataDelay", 200, BYTE9_PROTOCOL);

	private Byte9Protocol() {
		super("Byte9");
	}

	@Override
	protected ScalesBackend createBackend(String portId) {
		return new ScalesBackend(new Byte9Driver(portId));
	}

}
