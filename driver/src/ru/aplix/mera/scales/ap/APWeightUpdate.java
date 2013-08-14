package ru.aplix.mera.scales.ap;

import java.math.BigDecimal;
import java.math.MathContext;

import ru.aplix.mera.scales.backend.WeightUpdate;


public class APWeightUpdate implements WeightUpdate {

	private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
	private static final BigDecimal TWENTY = BigDecimal.valueOf(20);

	private final long weighingTime;
	private final int weight;
	private final boolean steadyWeight;

	APWeightUpdate(APPacket packet, long weighingTime) {
		this.weighingTime = weighingTime;
		this.weight = packet.getWeightX20()
				.multiply(THOUSAND)
				.divide(TWENTY)
				.round(MathContext.UNLIMITED)
				.intValueExact();
		this.steadyWeight = packet.isSteadyWeight();
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
	public long getWeighingTime() {
		return this.weighingTime;
	}

}
