package ru.aplix.mera.test.message;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraHandle;


public class TestHandle extends MeraHandle<TestHandle, String> {

	TestHandle(
			TestSubscriptions subscriptions,
			MeraConsumer<? super TestHandle, ? super String> consumer) {
		super(subscriptions, consumer);
	}

}
