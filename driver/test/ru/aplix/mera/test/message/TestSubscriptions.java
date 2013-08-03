package ru.aplix.mera.test.message;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.message.MeraSubscriptions;


public class TestSubscriptions extends MeraSubscriptions<TestHandle, String> {

	@Override
	protected TestHandle createHandle(
			MeraConsumer<? super TestHandle, ? super String> consumer) {
		return new TestHandle(this, consumer);
	}

}
