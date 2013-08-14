package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.aplix.mera.scales.*;


public class WeighingTest {

	private TestScalesService service;
	private ScalesPort port;
	private TestScalesDriver driver;

	private ScalesStatusConsumer statusConsumer;
	private ScalesPortHandle statusHandle;
	private LoadConsumer loadConsumer;
	private WeightConsumer weightConsumer;
	private ErrorConsumer errorConsumer;

	@Before
	public void start() {
		this.service = new TestScalesService();

		this.service.addTestDevice("test");
		this.driver = this.service.getDriver("test");

		final ScalesPortId portId =
				this.service.getScalesPortIds().get(0);

		this.port = portId.getPort();
		this.statusConsumer = new ScalesStatusConsumer();
		this.statusHandle = this.port.subscribe(this.statusConsumer);

		this.errorConsumer = new ErrorConsumer();
		this.statusHandle.listenForErrors(this.errorConsumer);

		// Status required to start weighing.
		this.driver.sendStatus(this.driver.connectedStatus());

		this.loadConsumer = new LoadConsumer();
		this.statusHandle.requestLoad(this.loadConsumer);

		this.weightConsumer = new WeightConsumer();
		this.statusHandle.requestWeight(this.weightConsumer);
	}

	@Test
	public void load() {
		this.driver.addWeight(new TestWeightUpdate(10));

		final LoadMessage loadMessage = this.loadConsumer.nextMessage();

		assertThat(loadMessage.isLoaded(), is(true));
		assertThat(loadMessage.getWeight(), is(10));
		this.loadConsumer.noMoreMessages();
		this.weightConsumer.noMoreMessages();
	}

	@Test
	public void weightFluctuations() {
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(11));
		this.driver.addWeight(new TestWeightUpdate(9));

		final LoadMessage loadMessage = this.loadConsumer.nextMessage();

		assertThat(loadMessage.isLoaded(), is(true));
		assertThat(loadMessage.getWeight(), is(10));
		this.loadConsumer.noMoreMessages();
		this.weightConsumer.noMoreMessages();
	}

	@Test
	public void steadyWeight() {
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(10));

		final LoadMessage loadMessage = this.loadConsumer.nextMessage();

		assertThat(loadMessage.isLoaded(), is(true));
		assertThat(loadMessage.getWeight(), is(10));
		this.loadConsumer.noMoreMessages();

		final WeightMessage weightMessage = this.weightConsumer.nextMessage();

		assertThat(weightMessage.getWeight(), is(10));
		this.weightConsumer.noMoreMessages();
	}

	@Test
	public void unload() {
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.addWeight(new TestWeightUpdate(0));

		final LoadMessage loadMessage = this.loadConsumer.nextMessage();

		assertThat(loadMessage.isLoaded(), is(true));
		assertThat(loadMessage.getWeight(), is(10));

		final LoadMessage unloadMessage = this.loadConsumer.nextMessage();

		assertThat(unloadMessage.isLoaded(), is(false));
		assertThat(unloadMessage.getWeight(), is(0));
		this.loadConsumer.noMoreMessages();
	}

	@Test
	public void suspendOnDisconnection() {
		this.driver.sendStatus(this.driver.errorStatus("Disconnected"));
		this.driver.addWeight(new TestWeightUpdate(10));

		this.loadConsumer.noMoreMessages();
		this.weightConsumer.noMoreMessages();
	}

	@Test
	public void resumeOnConnection() {
		this.driver.sendStatus(this.driver.errorStatus("Disconnected"));
		this.driver.addWeight(new TestWeightUpdate(10));
		this.driver.sendStatus(this.driver.connectedStatus());

		final LoadMessage loadMessage = this.loadConsumer.nextMessage();

		assertThat(loadMessage.isLoaded(), is(true));
		assertThat(loadMessage.getWeight(), is(10));
	}

	@After
	public void stop() {
		this.errorConsumer.noMoreMessages();
		if (this.driver != null) {
			this.driver.stop();
		}
		if (this.statusHandle != null) {
			this.statusHandle.unsubscribe();
		}
	}

}
