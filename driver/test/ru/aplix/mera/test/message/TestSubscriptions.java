package ru.aplix.mera.test.message;


public class TestSubscriptions extends MeraSubscriptions<TestHandle, String> {

	@Override
	protected TestHandle createHandle(
			MeraConsumer<? super TestHandle, ? super String> consumer) {
		return new TestHandle(this, consumer);
	}

}
