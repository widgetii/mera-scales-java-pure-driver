package ru.aplix.mera.scales;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


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
