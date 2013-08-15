package ru.aplix.mera.test.scales;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


public class ScalesConsumer<H extends MeraHandle<H, M>, M>
		implements MeraConsumer<H, M> {

	private final LinkedBlockingQueue<M> messages =
			new LinkedBlockingQueue<>();

	public final LinkedBlockingQueue<M> getMessages() {
		return this.messages;
	}

	@Override
	public void consumerSubscribed(H handle) {
	}

	@Override
	public void messageReceived(M message) {
		this.messages.add(message);
	}

	@Override
	public void consumerUnsubscribed(H handle) {
	}

	public M nextMessage() {
		try {

			final M message = this.messages.poll(1, TimeUnit.SECONDS);

			assertThat(message, notNullValue());

			return message;
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	public void noMoreMessages() {
		assertThat(this.messages.peek(), nullValue());
	}

}
