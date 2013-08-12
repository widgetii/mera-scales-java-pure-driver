package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static ru.aplix.mera.scales.ScalesStatus.SCALES_CONNECTED;
import static ru.aplix.mera.scales.ScalesStatus.SCALES_ERROR;
import static ru.aplix.mera.scales.byte9.Byte9ScalesProtocol.BYTE9_SCALES_PROTOCOL;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.aplix.mera.scales.*;


public class ScalesServiceTest {

	private TestScalesService service;
	private ScalesPort port;
	private ScalesStatusConsumer statusConsumer;
	private TestScalesDriver driver;
	private ScalesPortHandle statusHandle;

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
		openPort();

		assertThat(this.statusConsumer.getMessages().isEmpty(), is(true));

		final TestScalesStatusUpdate connectedStatus =
				this.driver.connectedStatus();

		this.driver.sendStatus(connectedStatus);

		final ScalesStatusMessage status =
				this.statusConsumer.getMessages().poll(1, TimeUnit.SECONDS);

		assertThat(status, notNullValue());
		assertThat(status.getScalesStatus(), is(SCALES_CONNECTED));
		assertThat(
				status.getScalesDevice(),
				is(connectedStatus.getScalesDevice()));
	}

	@Test
	public void noConnection() throws InterruptedException {
		openPort();

		final TestScalesStatusUpdate errorStatus =
				this.driver.errorStatus("Connection failed");

		this.driver.sendStatus(errorStatus);

		final ScalesStatusMessage status =
				this.statusConsumer.getMessages().poll(1, TimeUnit.SECONDS);

		assertThat(status, notNullValue());
		assertThat(status.getScalesStatus(), is(SCALES_ERROR));
		assertThat(status.getScalesError(), is(errorStatus.getScalesError()));

		this.statusHandle.unsubscribe();
		this.driver.stop();
	}

	@Test
	public void reconnection() throws InterruptedException {
		openPort();

		final TestScalesStatusUpdate errorStatus =
				this.driver.errorStatus("Connection failed");

		this.driver.sendStatus(errorStatus);
		this.driver.sendStatus(errorStatus);

		final TestScalesStatusUpdate connectedStatus =
				this.driver.connectedStatus();

		this.driver.sendStatus(connectedStatus);

		final ScalesStatusMessage status1 =
				this.statusConsumer.getMessages().take();

		assertThat(status1, notNullValue());
		assertThat(status1.getScalesStatus(), is(SCALES_ERROR));
		assertThat(status1.getScalesError(), is(errorStatus.getScalesError()));

		final ScalesStatusMessage status2 =
				this.statusConsumer.getMessages().take();

		assertThat(status2, notNullValue());
		assertThat(status2.getScalesStatus(), is(SCALES_CONNECTED));
		assertThat(
				status2.getScalesDevice(),
				is(connectedStatus.getScalesDevice()));
	}

	@After
	public void stop() {
		if (this.driver != null) {
			this.driver.stop();
		}
		if (this.statusHandle != null) {
			this.statusHandle.unsubscribe();
		}
	}

	private void openPort() {
		this.service.addTestDevice("test");
		this.driver = this.service.getDriver("test");

		final ScalesPortId portId =
				this.service.getScalesPortIds().get(0);

		this.port = portId.getPort();
		this.statusConsumer = new ScalesStatusConsumer();
		this.statusHandle = this.port.subscribe(this.statusConsumer);
	}

}
