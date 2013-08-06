package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;
import ru.aplix.mera.message.MeraSubscriptions;


/**
 * Scales errors subscription handle.
 */
public class ScalesErrorHandle
		extends MeraHandle<ScalesErrorHandle, ScalesErrorMessage> {

	public ScalesErrorHandle(
			MeraSubscriptions<
					ScalesErrorHandle,
					ScalesErrorMessage> subscriptions,
			MeraConsumer<
					? super ScalesErrorHandle,
					? super ScalesErrorMessage> consumer) {
		super(subscriptions, consumer);
	}

}
