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
	private final HashMap<String, TestScalesDriver> driversById =
			new HashMap<>();

	public void addTestDevice(String deviceId) {
		this.driversById.put(deviceId, new TestScalesDriver(deviceId));
	}

	public TestScalesDriver getDriver(String deviceId) {

		final TestScalesDriver driver = this.driversById.get(deviceId);

		if (driver == null) {
			throw new IllegalArgumentException("No such device: " + deviceId);
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
		protected ScalesBackend createBackend(String deviceId) {
			return new ScalesBackend(getDriver(deviceId));
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
		public String[] scalesDeviceIds() {

			final Set<String> ids = this.service.driversById.keySet();

			return ids.toArray(new String[ids.size()]);
		}

	}

}
