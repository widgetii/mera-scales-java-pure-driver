package ru.aplix.mera.test.message;


/**
 * Mera message consumer.
 *
 * <p>Subscribe to receive messages.</p>
 *
 * @param <H> subscription handle type.
 * @param <M> message type.
 */
public interface MeraConsumer<H extends MeraHandle<H, M>, M> {

	/**
	 * Informs that this consumer that it is subscribed for message reception.
	 *
	 * <p>This message is called from
	 * {@link MeraSubscriptions#subscribe(MeraConsumer)} method. The
	 * subscription {@code handle} passed to this method is the same one, which
	 * is passed to the </p>
	 *
	 * @param handle subscription handle.
	 */
	void consumerSubscribed(H handle);

	/**
	 * Informs the consumer about new message.
	 *
	 * @param message message to process.
	 */
	void messageReceived(M message);

	/**
	 * Informs that this consumer is unsubscribed from message reception.
	 *
	 * @param handle revoked subscription handle.
	 */
	void consumerUnubscribed(H handle);

}
