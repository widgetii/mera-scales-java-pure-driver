package ru.aplix.mera.message;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


/**
 * Mera message subscriptions list.
 *
 * <p>Can be used internally by mera message producers to maintain the
 * subscriptions list and to send the messages to all subscribed consumers.</p>
 *
 * <p>All manipulations and message sending are thread-safe.</p>
 *
 * @param <H> subscription handle type.
 * @param <M> message type.
 */
public abstract class MeraSubscriptions<H extends MeraHandle<H, M>, M> {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private volatile H first;
	private H last;

	public final boolean isEmpty() {
		return this.first == null;
	}

	/**
	 * Subscribes the given consumer to receive messages.
	 *
	 * <p>To unsubscribe, use the returned handle.</p>
	 *
	 * @param consumer consumer to subscribe.
	 *
	 * @return subscription handle.
	 */
	public final H subscribe(MeraConsumer<? super H, ? super M> consumer) {

		final H handle = createHandle(consumer);
		final WriteLock lock = this.lock.writeLock();

		lock.lock();
		try {
			if (this.first == null) {
				this.first = this.last = handle;
				firstSubscribed(handle);
			} else {
				this.last.addNext(handle);
				this.last = handle;
				subscribed(handle);
			}
		} finally {
			lock.unlock();
		}

		return handle;
	}

	/**
	 * Sends message to all subscribed consumers.
	 *
	 * <p>This method obtains a read lock. So, multiple messages could be
	 * sent simultaneously.</p>
	 *
	 * @param message the message to send.
	 */
	public void sendMessage(M message) {

		final ReadLock lock = this.lock.readLock();

		lock.lock();
		try {
			messageReceived(message);

			H handle = this.first;

			while (handle != null) {
				handle.getConsumer().messageReceived(message);
				handle = handle.next();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Creates a subscription handle for the given consumer.
	 *
	 * @param consumer consumer to subscribe.
	 *
	 * @return subscription handle.
	 */
	protected abstract H createHandle(
			MeraConsumer<? super H, ? super M> consumer);

	/**
	 * Invoked on first subscription.
	 *
	 * <p>By default invokes the {@link #subscribed(MeraHandle)} method.</p>
	 *
	 * <p>This method should be as small as possible, as it is invoked when
	 * this subscriptions are write locked.</p>
	 *
	 * @param handle first subscription handle.
	 */
	protected void firstSubscribed(H handle) {
		subscribed(handle);
	}

	/**
	 * Invoked on subscription.
	 *
	 * <p>For the very first subscription the
	 * {@link #firstSubscribed(MeraHandle)} method is invoked instead.</p>
	 *
	 * <p>This method should be as small as possible, as it is invoked when
	 * this subscriptions are write locked.</p>
	 *
	 * @param handle subscription handle.
	 */
	protected void subscribed(H handle) {
		handle.subscribed();
	}

	/**
	 * Receives the message before any subscriber.
	 *
	 * <p>This method is called from the {@link #sendMessage(Object)} one
	 * right before sending the message to subscribers. This method is called
	 * inside a read lock.</p>
	 *
	 * <p>Does nothing by default.</p>
	 *
	 * @param message message received.
	 */
	protected void messageReceived(M message) {
	}

	/**
	 * Invoked on unsubscription.
	 *
	 * <p>For the last subscription the
	 * {@link #lastUnsubscribed(MeraHandle)} method is invoked instead.</p>
	 *
	 * <p>This method should be as small as possible, as it is invoked when
	 * this subscriptions are write locked.</p>
	 *
	 * @param handle revoked subscription handle.
	 */
	protected void unsubscribed(H handle) {
		handle.unsubscribed();
	}

	/**
	 * Invoked on last unsubscription.
	 *
	 * <p>By default invokes the {@link #unsubscribed(MeraHandle)} method.</p>
	 *
	 * <p>This method should be as small as possible, as it is invoked when
	 * this subscriptions are write locked.</p>
	 *
	 * @param handle last revoked subscription handle.
	 */
	protected void lastUnsubscribed(H handle) {
		unsubscribed(handle);
	}

	final void unsubscribe(H handle) {

		final WriteLock lock = this.lock.writeLock();

		lock.lock();
		try {
			if (this.first == handle) {
				this.first = handle.next();
			}
			if (this.last == handle) {
				this.last = handle.prev();
			}
			handle.remove();

			if (this.first == null) {
				lastUnsubscribed(handle);
			} else {
				unsubscribed(handle);
			}
		} finally {
			lock.unlock();
		}
	}

}
