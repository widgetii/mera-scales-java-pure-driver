package ru.aplix.mera.scales.ap;

import static ru.aplix.mera.scales.ap.APStatusUpdate.AP_DISCONNECTED;
import ru.aplix.mera.scales.backend.*;


public class APDriver implements ScalesDriver, Weighing {

	private final APDevice device;
	private WeightReceiver weightReceiver;
	private APPortListener weightListener;

	public APDriver(String portId) {
		this.device = new APDevice(portId);
	}

	public final APDevice getDevice() {
		return this.device;
	}

	public final WeightReceiver getWeightReceiver() {
		return this.weightReceiver;
	}

	@Override
	public void initScalesDriver(ScalesDriverContext context) {
		context.updateWeightWith(this);
	}

	@Override
	public void initWeighting(WeightReceiver weightReceiver) {
		this.weightReceiver = weightReceiver;
	}

	@Override
	public ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {
		synchronized (this) {
			if (this.weightListener != null) {
				return new APStatusUpdate(getDevice());
			}
		}

		final APPortListener listener = new APPortListener(this, false);

		try {
			request.onInterrupt(listener);
			if (!listener.waitForResponse(1000L)) {
				return AP_DISCONNECTED;
			}
		} finally {
			listener.done();
		}
		if (listener.isError() || listener.isInterrupted()) {
			return null;
		}

		return new APStatusUpdate(getDevice());
	}

	@Override
	public WeightUpdate requestWeight(ScalesRequest request) throws Exception {
		// Weight is reported automatically.
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void startWeighing() {
		this.weightListener = new APPortListener(this, true);
	}

	@Override
	public void suspendWeighing() {
		stopWeighing();
	}

	@Override
	public void resumeWeighing() {
		startWeighing();
	}

	@Override
	public synchronized void stopWeighing() {
		this.weightListener.interrupt();
		this.weightListener = null;
	}

}
