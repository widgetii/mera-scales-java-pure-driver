package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Measured weight message handle.
 */
public final class WeightHandle
		extends MeraHandle<WeightHandle, WeightMessage> {

	WeightHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super WeightHandle,
					? super WeightMessage> consumer) {
		super(backend.weightSubscriptions(), consumer);
	}

}
