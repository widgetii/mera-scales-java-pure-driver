package ru.aplix.mera.scales;

import java.util.List;

import ru.aplix.mera.scales.backend.ScalesBackend;


/**
 * Scales support service.
 *
 * <p>This is the main interface for accessing scales.</p>
 */
public class ScalesService {

	/**
	 * Creates a new scales service instance.
	 *
	 * @return new scales service.
	 */
	public static ScalesService newScalesService() {
		return new ScalesService();
	}

	private List<? extends ScalesPortId> scalesPortIds;

	/**
	 * Constructs scales service instance.
	 *
	 * <p>The scales service can be inherited to create a custom implementation,
	 * e.g. for testing purposes.</p>
	 */
	protected ScalesService() {
	}

	/**
	 * Returns a list of all available scales ports.
	 *
	 * <p>This method is thread-safe.</p>
	 *
	 * @return a list of scales port identifiers.
	 */
	public synchronized List<? extends ScalesPortId> getScalesPortIds() {
		if (this.scalesPortIds != null) {
			return this.scalesPortIds;
		}
		return this.scalesPortIds = createScalesDiscovery().getScalesPortIds();
	}

	/**
	 * Creates a scales discovery instance responsible to use to find all
	 * available scales.
	 *
	 * @return scales discovery instance.
	 */
	protected ScalesDiscovery createScalesDiscovery() {
		return new ScalesDiscovery(this);
	}

	/**
	 * Creates a new scales port instance with the given identifier.
	 *
	 * <p>This is call for the first time a {@link ScalesPortId#getPort()}
	 * method is called.</p>
	 *
	 * @param portId an identifier of scales port to open.
	 *
	 * @return new scales port instance.
	 */
	protected ScalesPort openPort(ScalesPortId portId) {

		final ScalesBackend backend =
				portId.getProtocol().createBackend(portId.getDeviceId());

		return new ScalesPort(backend);
	}

}
