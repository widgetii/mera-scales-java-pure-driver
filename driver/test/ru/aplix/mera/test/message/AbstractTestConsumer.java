package ru.aplix.mera.test.message;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


public abstract class AbstractTestConsumer<H extends MeraHandle<H, String>>
	implements MeraConsumer<H, String> {

	private String[] expectedMessages;
	private int messagesReceived;
	private H handle;
	private boolean unsubscribed;

	public AbstractTestConsumer(String... expectedMessages) {
		this.expectedMessages = expectedMessages;
	}

	public final boolean isSubscribed() {
		return this.handle != null;
	}

	public final boolean isUnsubscribed() {
		return this.unsubscribed;
	}

	public final H getHandle() {
		return this.handle;
	}

	@Override
	public void consumerSubscribed(H handle) {
		assertThat(isSubscribed(), is(false));
		this.handle = handle;
	}

	@Override
	public void messageReceived(String message) {
		if (this.messagesReceived >= this.expectedMessages.length) {
			fail("No more messages expected");
			return;
		}

		final String expected = this.expectedMessages[this.messagesReceived++];

		assertThat(message, is(expected));
	}

	@Override
	public void consumerUnubscribed(H handle) {
		assertThat(this.unsubscribed, is(false));
		assertThat(handle, is(this.handle));
		this.unsubscribed = true;
	}

	public void ensureAllMessagesReceived() {
		if (this.expectedMessages.length > this.messagesReceived) {
			fail(
					"Only " + this.messagesReceived
					+ " messages received, but "
					+ this.expectedMessages + " expected");
		}
	}

}
