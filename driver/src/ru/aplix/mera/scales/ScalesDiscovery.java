package ru.aplix.mera.scales;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import ru.aplix.mera.scales.backend.ScalesBackendFactory;


/**
 * Scales discovery.
 *
 * <p>An instance of this class is {@link ScalesService#createScalesDiscovery()
 * used} by scales service to find all available scales.</p>
 *
 * <p>By default, uses a standard Java service discovery mechanism to find all
 * available {@link ScalesBackendFactory scales backend factories}, and
 * registers scales backends supported by them. You may wish to change this
 * behavior by overriding the appropriate methods.</p>
 */
public class ScalesDiscovery {

	private final ScalesService service;
	private final ArrayList<ScalesPortId> portIds = new ArrayList<>();

	/**
	 * Constructs new scales backend.
	 *
	 * @param service scales service to discovers backends for.
	 */
	public ScalesDiscovery(ScalesService service) {
		this.service = service;
	}

	/**
	 * Scales service to discover backends for.
	 *
	 * @return scales service instance passed to constructor.
	 */
	public final ScalesService getService() {
		return this.service;
	}

	/**
	 * Returns all discovered scales ports.
	 *
	 * <p>A {@link #discoverScales()} method is called to find them.</>
	 *
	 * @return all available scales port identifiers.
	 */
	public final List<? extends ScalesPortId> getScalesPortIds() {
		discoverScales();
		return unmodifiableList(this.portIds);
	}

	/**
	 * Discovers scales.
	 *
	 * <p>Use {@link #addPortId(ScalesProtocol, String)} and/or
	 * {@link #addBackendFactory(ScalesBackendFactory)} to register found scales
	 * ports and/or backend factories.</p>
	 */
	protected void discoverScales() {
		for (ScalesBackendFactory factory
				: ServiceLoader.load(ScalesBackendFactory.class)) {
			addBackendFactory(factory);
		}
	}

	/**
	 * Registers the scales backends supported by the given factory.
	 *
	 * @param factory scales factory.
	 */
	protected void addBackendFactory(ScalesBackendFactory factory) {
		try {

			final ScalesProtocol protocol = factory.getScalesProtocol();

			for (String portId : factory.scalesPortIds()) {
				addPortId(protocol, portId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addPortId(ScalesProtocol protocol, String deviceId) {
		this.portIds.add(new ScalesPortId(this.service, deviceId, protocol));
	}

}
