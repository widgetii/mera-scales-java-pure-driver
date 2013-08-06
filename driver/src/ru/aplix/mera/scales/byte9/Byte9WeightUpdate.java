package ru.aplix.mera.scales.byte9;

import java.util.Date;

import ru.aplix.mera.scales.backend.WeightUpdate;


public class Byte9WeightUpdate implements WeightUpdate {

	private final long weighingTime;
	private final Byte9Packet packet;

	public Byte9WeightUpdate(long weighingTime, Byte9Packet packet) {
		this.weighingTime = weighingTime;
		this.packet = packet;
	}

	@Override
	public int getWeight() {
		return this.packet.getSignedValue();
	}

	@Override
	public long getWeighingTime() {
		return this.weighingTime;
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
