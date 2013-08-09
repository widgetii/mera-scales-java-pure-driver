package ru.aplix.mera.test.message;

import static org.junit.Assert.fail;
import static ru.aplix.mera.scales.byte9.Byte9ScalesProtocol.BYTE9_SCALES_PROTOCOL;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ru.aplix.mera.scales.ScalesDiscovery;
import ru.aplix.mera.scales.ScalesService;
import ru.aplix.mera.scales.backend.ScalesBackendFactory;


public class ScalesServiceTest {

	private TestScalesService service;

	@Before
	public void createService() {
		this.service = new TestScalesService();
	}

	@Test
	public void byte9Discovered() {
		this.service.getScalesPortIds();
		checkByte9Discovered();
	}

	private void checkByte9Discovered() {
		for (ScalesBackendFactory factory : this.service.factories) {
			if (factory.getScalesProtocol() == BYTE9_SCALES_PROTOCOL) {
				return;
			}
		}
		fail("Byte9 protocol factory not discovered");
	}

	private final class TestScalesService extends ScalesService {

		private ArrayList<ScalesBackendFactory> factories =
				new ArrayList<>();

		@Override
		protected ScalesDiscovery createScalesDiscovery() {
			return new TestScalesDiscovery(this);
		}

	}

	private final class TestScalesDiscovery extends ScalesDiscovery {

		TestScalesDiscovery(TestScalesService testService) {
			super(testService);
		}

		public final TestScalesService getTestService() {
			return (TestScalesService) getService();
		}

		@Override
		protected void addBackendFactory(ScalesBackendFactory factory) {
			getTestService().factories.add(factory);
		}

	}

}
