package ru.aplix.mera.scales;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


/**
 * Scales support service.
 *
 * <p>This is the main interface for accessing scales.</p>
 */
public class ScalesService {

	private static final String SERVICE_FILE_NAME =
			"META-INF/services/" + ScalesService.class.getName();

	/**
	 * Creates a new scales service instance.
	 *
	 * @return new scales service.
	 */
	public static ScalesService newScalesService() {
		return new ScalesService();
	}

	private List<? extends ScalesPortId> sclalesPortIds;

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
		if (this.sclalesPortIds == null) {
			return this.sclalesPortIds;
		}
		return this.sclalesPortIds = discoverPortIds();
	}

	/**
	 * Creates a new scales port instance with the given identifier.
	 *
	 * <p>This is an implementation of {@link ScalesPortId#openPort()} method.
	 * </p>
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

	/**
	 * Discovers all available scales ports.
	 *
	 * @return a list of port identifiers.
	 */
	protected List<? extends ScalesPortId> discoverPortIds() {

		final ClassLoader classLoader = getClassLoader();
		final ArrayList<ScalesPortId> portIds = new ArrayList<>();
		final Enumeration<URL> urls;

		try {
			urls = classLoader.getResources(SERVICE_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}

		while (urls.hasMoreElements()) {
			discoverPortIdsIn(classLoader, portIds, urls.nextElement());
		}

		return unmodifiableList(portIds);
	}

	private ClassLoader getClassLoader() {

		final ClassLoader contextClassLoader =
				currentThread().getContextClassLoader();

		if (contextClassLoader != null) {
			return contextClassLoader;
		}

		return getClass().getClassLoader();
	}

	private void discoverPortIdsIn(
			ClassLoader classLoader,
			ArrayList<ScalesPortId> portIds,
			URL url) {
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

					addPortIds(portIds, factory);
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

	private void addPortIds(
			ArrayList<ScalesPortId> portIds,
			ScalesBackendFactory factory) {
		try {

			final ScalesProtocol protocol = factory.getScalesProtocol();

			for (String deviceId : factory.sclalesDeviceIds()) {
				portIds.add(new ScalesPortId(this, deviceId, protocol));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
