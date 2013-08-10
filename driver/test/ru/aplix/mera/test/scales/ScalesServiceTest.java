package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static ru.aplix.mera.scales.byte9.Byte9ScalesProtocol.BYTE9_SCALES_PROTOCOL;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import ru.aplix.mera.scales.*;


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

	@Test
	public void connect() throws InterruptedException {
		this.service.addTestDevice("test");

		final TestScalesDriver driver = this.service.getDriver("test");
		final ScalesPortId portId =
				this.service.getScalesPortIds().get(0);
		final ScalesPort port = portId.getPort();
		final ScalesStatusConsumer statusConsumer = new ScalesStatusConsumer();
		final ScalesPortHandle statusHandle = port.subscribe(statusConsumer);

		assertThat(statusConsumer.getMessages().isEmpty(), is(true));

		final TestScalesStatusUpdate connectedStatus = driver.connectedStatus();
		driver.setStatus(connectedStatus);

		final ScalesStatusMessage status =
				statusConsumer.getMessages().poll(1, TimeUnit.SECONDS);

		assertThat(status, notNullValue());
		assertThat(status.getScalesStatus(), is(ScalesStatus.CONNECTED));
		assertThat(
				status.getScalesDevice(),
				is(connectedStatus.getScalesDevice()));

		statusHandle.unsubscribe();
	}

}
