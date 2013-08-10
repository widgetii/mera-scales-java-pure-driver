package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;

import ru.aplix.mera.scales.ScalesDiscovery;
import ru.aplix.mera.scales.ScalesProtocol;
import ru.aplix.mera.scales.ScalesService;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


final class TestScalesService extends ScalesService {

	private final HashSet<ScalesProtocol> discoveredProtocols = new HashSet<>();
	private final ArrayList<ScalesBackendFactory> testFactories =
			new ArrayList<>();

	public void addTestFactory(ScalesBackendFactory factory) {
		this.testFactories.add(factory);
	}

	public void checkProtocolDiscovered(ScalesProtocol protocol) {
		assertThat(this.discoveredProtocols.contains(protocol), is(true));
	}

	@Override
	protected ScalesDiscovery createScalesDiscovery() {
		return new TestScalesDiscovery(this);
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
			for (ScalesBackendFactory testFactory
					: getTestService().testFactories) {
				super.addBackendFactory(testFactory);
			}
		}

		@Override
		protected void addBackendFactory(ScalesBackendFactory factory) {

			final ScalesProtocol protocol = factory.getScalesProtocol();

			getTestService().discoveredProtocols.add(protocol);
		}

	}

}