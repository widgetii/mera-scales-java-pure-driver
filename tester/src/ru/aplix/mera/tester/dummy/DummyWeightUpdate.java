package ru.aplix.mera.tester.dummy;

import ru.aplix.mera.scales.backend.WeightUpdate;


class DummyWeightUpdate implements WeightUpdate {

	private final long weighingTime;
	private int weight;

	DummyWeightUpdate(long weighingTime, int weight) {
		this.weighingTime = weighingTime;
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
	public long getWeighingTime() {
		return this.weighingTime;
	}

}
