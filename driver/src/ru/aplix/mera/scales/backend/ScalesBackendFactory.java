package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesPortId;
import ru.aplix.mera.scales.ScalesProtocol;


/**
 * Scales backend factory.
 *
 * <p>The implementation class name should be written to file named
 * <code>META-INF/services/ru.aplix.mera.scales.backend.ScalesBackendFactory</code>
 * in application class path in order to be discovered.
 */
public interface ScalesBackendFactory {

	/**
	 * Returns the scales protocol supported by all devices discovered by this
	 * factory.
	 *
	 * @return scales protocol instance.
	 */
	ScalesProtocol getScalesProtocol();

	/**
	 * Discovers the scales devices.
	 *
	 * @return array of scales device {@link ScalesPortId#getPortId()
	 * identifiers}.
	 */
	String[] scalesDeviceIds();

}
