package ru.aplix.mera.test.scales;

import java.util.concurrent.LinkedBlockingQueue;

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
	public void consumerUnubscribed(H handle) {
	}

}
