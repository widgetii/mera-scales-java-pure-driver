package ru.aplix.mera.test.scales;

import static ru.aplix.mera.scales.byte9.Byte9ScalesProtocol.BYTE9_SCALES_PROTOCOL;

import org.junit.Before;
import org.junit.Test;


public class ScalesServiceTest {

	private TestScalesService service;

	@Before
	public void createService() {
		this.service = new TestScalesService();
	}

	@Test
	public void byte9Discovered() {
		this.service.getScalesPortIds();
		this.service.checkProtocolDiscovered(BYTE9_SCALES_PROTOCOL);
	}

}
