package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Measured weight updates handle.
 */
public final class WeightUpdateHandle
		extends MeraHandle<WeightUpdateHandle, WeightUpdate> {

	WeightUpdateHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super WeightUpdateHandle,
					? super WeightUpdate> consumer) {
		super(backend.weightSubscriptions(), consumer);
	}

}
