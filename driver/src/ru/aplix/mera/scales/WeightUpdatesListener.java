package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.backend.WeightUpdate;
import ru.aplix.mera.scales.backend.WeightUpdateHandle;
import ru.aplix.mera.util.CyclicBuffer;


final class WeightUpdatesListener
		implements MeraConsumer<WeightUpdateHandle, WeightUpdate> {

	/**
	 * The number of weight measures with the same weight preceding the current
	 * one necessary to consider the weight steady.
	 */
	private static final int STEADY_MEASURES = 2;

	/**
	 * Control delay, after the which the weight is considered steady.
	 */
	private static final long STEADINESS_DELAY = 5432L;

	private final ScalesPort port;
	private int started = 0;
	private WeightUpdateHandle handle;
	private CyclicBuffer<WeightUpdate> lastUpdates =
			new CyclicBuffer<>(new WeightUpdate[STEADY_MEASURES]);
	private boolean steady = true;
	private long weighingStart;

	WeightUpdatesListener(ScalesPort port) {
		this.port = port;
	}

	public final synchronized void start() {
		if (this.started++ == 0) {
			this.port.statusHandle().requestWeight(this);
		}
	}

	public final synchronized void stop() {
		if (--this.started == 0) {
			this.handle.unsubscribe();
			this.handle = null;
		}
	}

	@Override
	public void consumerSubscribed(WeightUpdateHandle handle) {
		this.handle = handle;
	}

	@Override
	public void messageReceived(WeightUpdate update) {
		if (this.steady) {
			checkLoad(update);
		} else {
			checkSteadyness(update);
		}
		this.lastUpdates.add(update);
	}

	@Override
	public void consumerUnubscribed(WeightUpdateHandle handle) {
	}

	private void checkLoad(WeightUpdate update) {

		final int weight = update.getWeight();
		final int lastWeight;
		final WeightUpdate lastUpdate = this.lastUpdates.last();

		if (lastUpdate == null) {
			lastWeight = 0;
		} else {
			lastWeight = lastUpdate.getWeight();
		}

		final int weightDiff = weight - lastWeight;

		if (weightDiff == 0) {
			return;// Weight didn't changed.
		}

		final boolean loaded = weightDiff > 0;

		this.steady = false;// Fluctuations started.
		this.weighingStart = update.getWeighingTime();
		this.port.loadSubscriptions()
		.sendMessage(new LoadMessage(update, loaded));
	}

	private void checkSteadyness(WeightUpdate update) {

		final int len = this.lastUpdates.length();
		final int firstIdx = len - STEADY_MEASURES;

		if (firstIdx < 0) {
			return;// Too few measures.
		}

		final int weight = update.getWeight();
		final WeightUpdate[] updates = this.lastUpdates.items();
		boolean steady = true;

		for (int i = firstIdx; i < STEADY_MEASURES; ++i) {

			final WeightUpdate up = updates[i];

			if (up.getWeighingTime() < this.weighingStart) {
				// Too old data.
				steady = false;
				continue;
			}
			if (weight != up.getWeight()) {
				steady = false;
			}
		}

		if (!steady) {

			final WeightUpdate prev = this.lastUpdates.last();

			if (prev.getWeighingTime() - update.getWeighingTime()
					< STEADINESS_DELAY) {
				// Measureas are fresh enough.
				// Continue measuring.
				return;
			}
			// Too many time passed since the last update.
			// Forcibly consider the weight steady.
		}

		this.steady = true;
		this.port.weightSubscriptions()
		.sendMessage(new WeightMessage(update));
	}

}
