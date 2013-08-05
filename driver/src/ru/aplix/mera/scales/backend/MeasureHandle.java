package ru.aplix.mera.scales.backend;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Measured weight message handle.
 */
public final class MeasureHandle
		extends MeraHandle<MeasureHandle, MeasureMessage> {

	MeasureHandle(
			ScalesBackend backend,
			MeraConsumer<
					? super MeasureHandle,
					? super MeasureMessage> consumer) {
		super(backend.weightSubscriptions(), consumer);
	}

}
