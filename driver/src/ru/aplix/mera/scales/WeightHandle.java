package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Weight subscription handle.
 */
public class WeightHandle extends MeraHandle<WeightHandle, WeightMessage> {

	WeightHandle(
			ScalesPort port,
			MeraConsumer<
					? super WeightHandle,
					? super WeightMessage> consumer) {
		super(port.weightSubscriptions(), consumer);
	}

}
