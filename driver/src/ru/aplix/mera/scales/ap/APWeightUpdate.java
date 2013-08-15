package ru.aplix.mera.scales.ap;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ru.aplix.mera.scales.backend.WeightUpdate;


public class APWeightUpdate implements WeightUpdate {

	private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
	private static final BigDecimal TWENTY = BigDecimal.valueOf(20);

	private final long weighingTime;
	private final long weighingEnd;
	private final int weight;
	private final boolean steadyWeight;

	APWeightUpdate(APPacket packet, long weighingTime) {
		this.weighingTime = weighingTime;
		this.weight = packet.getWeightX20()
				.multiply(THOUSAND)
				.divide(TWENTY)
				.setScale(0, RoundingMode.HALF_UP)
				.intValueExact();
		this.steadyWeight = packet.isSteadyWeight();
		this.weighingEnd = System.currentTimeMillis();
	}

	@Override
	public boolean isSteadyWeight() {
		return this.steadyWeight;
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
		return this.weighingEnd;
	}

}
