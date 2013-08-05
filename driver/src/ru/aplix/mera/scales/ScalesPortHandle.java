package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


/**
 * Scales port handle.
 *
 * <p>Other scales port event can be registered though it. When this
 * subscription revoked, all subscriptions registered through it are also
 * revoked.</p>
 */
public final class ScalesPortHandle
		extends MeraServiceHandle<ScalesPortHandle, ScalesStatusMessage> {

	ScalesPortHandle(
			ScalesPort port,
			MeraConsumer<
					? super ScalesPortHandle,
					? super ScalesStatusMessage> consumer) {
		super(port, consumer);
	}

}
