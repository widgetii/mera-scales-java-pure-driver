package ru.aplix.mera.test.message;


public class TestHandle extends MeraHandle <TestHandle, String> {

	TestHandle(
			TestSubscriptions subscriptions,
			MeraConsumer<? super TestHandle, ? super String> consumer) {
		super(subscriptions, consumer);
	}

}
