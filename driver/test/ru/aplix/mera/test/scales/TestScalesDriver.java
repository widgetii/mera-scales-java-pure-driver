package ru.aplix.mera.test.scales;

import static ru.aplix.mera.test.scales.TestScalesStatusUpdate.STOP_TEST;

import java.util.concurrent.LinkedBlockingQueue;

import ru.aplix.mera.scales.backend.*;


public class TestScalesDriver implements ScalesDriver {

	private final String deviceId;
	private TestScalesStatusUpdate status;
	private final LinkedBlockingQueue<TestWeightUpdate> weights =
			new LinkedBlockingQueue<>();

	public TestScalesDriver(String deviceId) {
		this.deviceId = deviceId;
	}

	public final String getDeviceId() {
		return this.deviceId;
	}

	public TestScalesStatusUpdate errorStatus(String error) {
		return new TestScalesStatusUpdate(error);
	}

	public TestScalesStatusUpdate connectedStatus() {
		return new TestScalesStatusUpdate(new TestScalesDevice(getDeviceId()));
	}

	public synchronized void setStatus(TestScalesStatusUpdate status) {
		this.status = status;
		notifyAll();
	}

	public void stop() {
		setStatus(STOP_TEST);
		addWeight(new TestWeightUpdate(STOP_TEST));
	}

	public void addWeight(TestWeightUpdate weight) {
		try {
			this.weights.put(weight);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void initScalesDriver(ScalesDriverContext context) {
		context.setConfig(context.getConfig().setWeighingPeriod(10));
	}

	@Override
	public synchronized ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {
		while (this.status == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				return null;
			}
		}
		if (this.status.isStopTest()) {
			return null;
		}
		return this.status;
	}

	@Override
	public WeightUpdate requestWeight(ScalesRequest request) throws Exception {
		try {

			final TestWeightUpdate weight = this.weights.take();

			return weight.apply(request);
		} catch (InterruptedException e) {
			return null;
		}
	}

}
