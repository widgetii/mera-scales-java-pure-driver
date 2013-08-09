package ru.aplix.mera.scales;

import static java.util.Collections.unmodifiableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import ru.aplix.mera.scales.backend.ScalesBackendFactory;


/**
 * Scales discovery.
 *
 * <p>An instance of this class is {@link ScalesService#createScalesDiscovery()
 * used} by scales service to find all available scales.</p>
 *
 * <p>By default, uses standard Java service discovery mechanism to find all
 * available {@link ScalesBackendFactory scales backend factories}, and
 * registers scales backends supported by them. You may wish to change this
 * behavior by overriding the appropriate methods.</p>
 */
public class ScalesDiscovery {

	private static final String SERVICE_FILE_NAME =
			"META-INF/services/" + ScalesBackendFactory.class.getName();

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

		final ClassLoader classLoader = getClassLoader();
		final Enumeration<URL> urls;

		try {
			urls = classLoader.getResources(SERVICE_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while (urls.hasMoreElements()) {
			discoverPortIdsIn(classLoader, urls.nextElement());
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

			for (String deviceId : factory.scalesDeviceIds()) {
				addPortId(protocol, deviceId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addPortId(ScalesProtocol protocol, String deviceId) {
		this.portIds.add(new ScalesPortId(this.service, deviceId, protocol));
	}

	/**
	 * Returns a class loader used to discover and load backend factories.
	 *
	 * @return class loader.
	 */
	protected ClassLoader getClassLoader() {

		/*final ClassLoader contextClassLoader =
				currentThread().getContextClassLoader();

		if (contextClassLoader != null) {
			return contextClassLoader;
		}*/

		return getClass().getClassLoader();
	}

	private void discoverPortIdsIn(ClassLoader classLoader, URL url) {
		try {

			final BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream(), "UTF-8"));

			try {

				String line;

				while ((line = in.readLine()) != null) {

					final String className = backendFactoryClassName(line);

					if (className.isEmpty()) {
						continue;
					}

					final ScalesBackendFactory factory =
							createBackendFactory(classLoader, className);

					if (factory == null) {
						continue;
					}

					addBackendFactory(factory);
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String backendFactoryClassName(String line) {

		final int idx = line.indexOf('#');

		if (idx < 0) {
			return line;
		}

		return line.substring(0, idx).trim();
	}

	private ScalesBackendFactory createBackendFactory(
			ClassLoader classLoader,
			String className) {

		final Class<?> klass;

		try {
			klass = Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		if (!ScalesBackendFactory.class.isAssignableFrom(klass)) {
			System.err.println(
					klass.getName() + " is not an instance of "
					+ ScalesBackendFactory.class);
			return null;
		}

		try {
			return klass.asSubclass(ScalesBackendFactory.class).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
