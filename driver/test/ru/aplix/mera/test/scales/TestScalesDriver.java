package ru.aplix.mera.test.scales;

import static ru.aplix.mera.test.scales.TestScalesStatusUpdate.STOP_TEST;

import java.util.concurrent.LinkedBlockingQueue;

import ru.aplix.mera.scales.backend.*;


public class TestScalesDriver implements ScalesDriver {

	private final String deviceId;
	private final LinkedBlockingQueue<TestScalesStatusUpdate> statuses =
			new LinkedBlockingQueue<>();
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

	public void sendStatus(TestScalesStatusUpdate status) {
		this.statuses.add(status);
	}

	public void stop() {
		sendStatus(STOP_TEST);
		addWeight(new TestWeightUpdate(STOP_TEST));
	}

	public void addWeight(TestWeightUpdate weight) {
		this.weights.add(weight);
	}

	@Override
	public void initScalesDriver(ScalesDriverContext context) {
		context.setConfig(
				context.getConfig()
				.setMinReconnectDelay(10)
				.setMaxReconnectDelay(50)
				.setWeighingPeriod(10));
	}

	@Override
	public synchronized ScalesStatusUpdate requestStatus(
			ScalesRequest request)
	throws Exception {
		try {

			final TestScalesStatusUpdate status = this.statuses.take();

			if (status.isStopTest()) {
				return null;
			}

			return status;
		} catch (InterruptedException e) {
			return null;
		}
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
