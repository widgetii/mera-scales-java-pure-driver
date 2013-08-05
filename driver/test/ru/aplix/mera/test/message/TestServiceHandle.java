package ru.aplix.mera.test.message;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraServiceHandle;


public class TestServiceHandle
		extends MeraServiceHandle<TestServiceHandle, String> {

	private final TestService testService;

	TestServiceHandle(
			TestService service,
			MeraConsumer<? super TestServiceHandle, ? super String> consumer) {
		super(service, consumer);
		this.testService = service;
	}

	public final TestHandle subscribeOn1(TestConsumer consumer) {
		return addSubscription(
				this.testService.subscriptions1().subscribe(consumer));
	}

	public final TestHandle subscribeOn2(TestConsumer consumer) {
		return addSubscription(
				this.testService.subscriptions2().subscribe(consumer));
	}

}
