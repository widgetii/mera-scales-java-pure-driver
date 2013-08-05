package ru.aplix.mera.test.message;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;


public class ServiceTest {

	private static final String MESSAGE1 = "message1";
	private static final String MESSAGE2 = "message2";
	private static final String MESSAGE3 = "message3";

	private TestService service;

	@Before
	public void setup() {
		this.service = new TestService();
	}

	@Test
	public void startService() {
		this.service.subscribe(new TestServiceConsumer());

		assertThat(this.service.isStarted(), is(true));
		assertThat(this.service.isStopped(), is(false));
	}

	@Test
	public void stopService() {

		final TestServiceHandle handle =
				this.service.subscribe(new TestServiceConsumer());

		handle.unsubscribe();

		assertThat(this.service.isStarted(), is(false));
		assertThat(this.service.isStopped(), is(true));
	}

	@Test
	public void subscribe() {

		final TestServiceHandle serviceHandle =
				this.service.subscribe(new TestServiceConsumer());
		final TestConsumer consumer1 = new TestConsumer(MESSAGE1, MESSAGE2);
		final TestConsumer consumer2 = new TestConsumer(MESSAGE3);

		serviceHandle.subscribeOn1(consumer1);
		serviceHandle.subscribeOn2(consumer2);

		assertThat(consumer1.isSubscribed(), is(true));
		assertThat(consumer2.isSubscribed(), is(true));

		this.service.subscriptions1().sendMessage(MESSAGE1);
		this.service.subscriptions1().sendMessage(MESSAGE2);
		this.service.subscriptions2().sendMessage(MESSAGE3);

		consumer1.ensureAllMessagesReceived();
		consumer2.ensureAllMessagesReceived();
	}

	@Test
	public void unsubscribe() {

		final TestServiceHandle serviceHandle =
				this.service.subscribe(new TestServiceConsumer());
		final TestConsumer consumer1 = new TestConsumer(MESSAGE1);
		final TestConsumer consumer2 = new TestConsumer();

		serviceHandle.subscribeOn1(consumer1);
		serviceHandle.subscribeOn2(consumer2);

		this.service.subscriptions1().sendMessage(MESSAGE1);

		serviceHandle.unsubscribe();

		assertThat(consumer1.isUnsubscribed(), is(true));
		assertThat(consumer2.isUnsubscribed(), is(true));

		this.service.subscriptions1().sendMessage(MESSAGE2);
		this.service.subscriptions2().sendMessage(MESSAGE3);

		consumer1.ensureAllMessagesReceived();
		consumer2.ensureAllMessagesReceived();
	}

}
