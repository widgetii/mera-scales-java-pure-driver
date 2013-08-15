package ru.aplix.mera.scales.byte9;

import java.util.Date;

import ru.aplix.mera.scales.backend.WeightUpdate;


public class Byte9WeightUpdate implements WeightUpdate {

	private final long weighingStart;
	private final long weighingTime;
	private final Byte9Packet packet;
	private final long weighingEnd;

	public Byte9WeightUpdate(
			long weighingStart,
			long weighingTime,
			Byte9Packet packet) {
		this.weighingStart = weighingStart;
		this.weighingTime = weighingTime;
		this.packet = packet;
		this.weighingEnd = System.currentTimeMillis();
	}

	@Override
	public boolean isSteadyWeight() {
		return false;
	}

	@Override
	public int getWeight() {
		return this.packet.getSignedValue();
	}

	@Override
	public long getWeighingStart() {
		return this.weighingStart;
	}

	@Override
	public long getWeighingTime() {
		return this.weighingTime;
	}

	@Override
	public long getWeighingEnd() {
		return this.weighingEnd;
	}

	@Override
	public String toString() {
		if (this.packet == null) {
			return super.toString();
		}
		return "Byte9WeightUpdate["
				+ getWeight() + "g @ "
				+ new Date(getWeighingTime()) + ']';
	}

}
