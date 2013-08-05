package ru.aplix.mera.util;

import java.util.Objects;


/**
 * A thread periodically performing some action, while some condition met.
 *
 * <p>Just construct this object and call {@link #performEvery(long)} method
 * to periodically invoke the {@link #action() action}. This thread can be
 * reused many times.</p>
 */
public abstract class PeriodicalAction implements Runnable {

	private final Object lock;
	private volatile long timeout;
	private Thread thread;
	private long actionTime;

	/**
	 * Constructs a new periodical action.
	 *
	 * @param lock an object to perform locks on.
	 */
	public PeriodicalAction(Object lock) {
		Objects.requireNonNull(lock, "Lock object not specified");
		this.lock = lock;
	}

	/**
	 * Lock object.
	 *
	 * @return lock object passed to the constructor.
	 */
	public final Object lock() {
		return this.lock;
	}

	/**
	 * Returns current actions timeout.
	 *
	 * @return action timeout.
	 */
	public final long getTimeout() {
		return this.timeout;
	}

	/**
	 * Initiates the action every {@code timeout} milliseconds.
	 *
	 * <p>Starts the thread if not started already. I the thread is already
	 * started, then updates the timeout.</p>
	 *
	 * @param timeout timeout between actions, in milliseconds.
	 */
	public final void performEvery(long timeout) {
		synchronized (lock()) {

			long oldTimeout = this.timeout;

			this.timeout = timeout;
			if (this.thread == null) {
				this.actionTime = System.currentTimeMillis() + this.timeout;
				this.thread = new Thread(this);
				this.thread.start();
			} else if (oldTimeout != timeout) {
				updateActionTime(this.actionTime - oldTimeout);
				lock().notify();
			}
		}
	}

	/**
	 * Stops periodical updates.
	 */
	public void stop() {
		synchronized (lock()) {
			if (this.thread != null) {
				this.thread = null;
				lock().notify();
			}
		}
	}

	@Override
	public void run() {
		while (!waitForCondition()) {
			action();
		}
	}

	/**
	 * The periodical action is performed while this condition is true.
	 *
	 * <p>Do not forget to call {@code lock().notify()} when this condition
	 * value changes.<p>
	 *
	 * @return <code>true</code> to perform periodical action, or
	 * <code>false</code> to stop it.
	 */
	protected abstract boolean condition();

	/**
	 * An action performed periodically.
	 */
	protected abstract void action();

	private boolean waitForCondition() {
		synchronized (lock()) {

			long left = this.timeout;

			for (;;) {

				boolean threadTerminated = true;

				try {
					if (this.thread != null || !condition()) {
						return true;
					}
					if (left <= 0) {
						threadTerminated = false;
						updateActionTime(this.actionTime);
						return false;
					}
					try {
						lock().wait(left);
					} catch (InterruptedException e) {
						return true;
					}
				} finally {
					if (threadTerminated) {
						this.thread = null;
					}
				}
				left = this.actionTime - System.currentTimeMillis();
			}
		}
	}

	private void updateActionTime(long startTime) {

		final long now = System.currentTimeMillis();
		final long timeSinceStart = now - startTime;
		final long timeoutsSinceStart = timeSinceStart / this.timeout;

		this.actionTime = startTime + (timeoutsSinceStart + 1) * this.timeout;
	}

}
