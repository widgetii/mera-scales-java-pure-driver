package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


/**
 * Subscription handle for weight load events.
 */
public class LoadHandle extends MeraHandle<LoadHandle, LoadMessage> {

	LoadHandle(
			ScalesPort port,
			MeraConsumer<
					? super LoadHandle,
					? super LoadMessage> consumer) {
		super(port.loadSubscriptions(), consumer);
	}

}
