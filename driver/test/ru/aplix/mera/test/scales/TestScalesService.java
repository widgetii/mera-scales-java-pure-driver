package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ru.aplix.mera.scales.ScalesDiscovery;
import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.ScalesService;
import ru.aplix.mera.scales.backend.ScalesBackend;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


final class TestScalesService extends ScalesService {

	private final TestScalesProtocol protocol = new TestScalesProtocol();
	private final HashSet<ScalesProtocol> discoveredProtocols = new HashSet<>();
	private final HashMap<String, TestScalesDriver> driversByPortId =
			new HashMap<>();

	public void addTestDevice(String portId) {
		this.driversByPortId.put(portId, new TestScalesDriver(portId));
	}

	public TestScalesDriver getDriver(String portId) {

		final TestScalesDriver driver = this.driversByPortId.get(portId);

		if (driver == null) {
			throw new IllegalArgumentException("No such device: " + portId);
		}

		return driver;
	}

	public void checkProtocolDiscovered(ScalesProtocol protocol) {
		assertThat(this.discoveredProtocols.contains(protocol), is(true));
	}

	@Override
	protected ScalesDiscovery createScalesDiscovery() {
		return new TestScalesDiscovery(this);
	}

	private final class TestScalesProtocol extends ScalesProtocol {

		private TestScalesProtocol() {
			super("Test");
		}

		@Override
		protected ScalesBackend createBackend(String portId) {
			return new ScalesBackend(getDriver(portId));
		}

	}

	private static final class TestScalesDiscovery extends ScalesDiscovery {

		TestScalesDiscovery(TestScalesService testService) {
			super(testService);
		}

		public final TestScalesService getTestService() {
			return (TestScalesService) getService();
		}

		@Override
		protected void discoverScales() {
			super.discoverScales();
			super.addBackendFactory(
					new TestScalesBackendFactory(getTestService()));
		}

		@Override
		protected void addBackendFactory(ScalesBackendFactory factory) {

			final ScalesProtocol protocol = factory.getScalesProtocol();

			getTestService().discoveredProtocols.add(protocol);
		}

	}

	private static final class TestScalesBackendFactory
			implements ScalesBackendFactory {

		private final TestScalesService service;

		TestScalesBackendFactory(TestScalesService service) {
			this.service = service;
		}

		@Override
		public ScalesProtocol getScalesProtocol() {
			return this.service.protocol;
		}

		@Override
		public String[] scalesPortIds() {

			final Set<String> ids = this.service.driversByPortId.keySet();

			return ids.toArray(new String[ids.size()]);
		}

	}

}
