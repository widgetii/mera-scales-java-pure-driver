package ru.aplix.mera.test.scales;

import ru.aplix.mera.scales.backend.ScalesRequest;
import ru.aplix.mera.scales.backend.WeightUpdate;


public class TestWeightUpdate implements WeightUpdate {

	private final TestScalesStatusUpdate status;
	private final long weighingTime;
	private final int weight;

	public TestWeightUpdate(TestScalesStatusUpdate status) {
		this.status = status;
		this.weighingTime = 0;
		this.weight = 0;
	}

	public TestWeightUpdate(int weight) {
		this.status = null;
		this.weighingTime = System.currentTimeMillis();
		this.weight = weight;
	}

	@Override
	public boolean isSteadyWeight() {
		return false;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public long getWeighingStart() {
		return this.weighingTime;
	}

	@Override
	public long getWeighingTime() {
		return this.weighingTime;
	}

	@Override
	public long getWeighingEnd() {
		return this.weighingTime;
	}

	public TestWeightUpdate apply(ScalesRequest request) {
		if (this.status != null) {
			this.status.apply(request);
			return null;
		}
		return this;
	}

	@Override
	public String toString() {
		return "TestWeightUpdate[weighingTime="
				+ this.weighingTime
				+ ", weight=" + this.weight + "]";
	}

}
