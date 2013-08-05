package ru.aplix.mera.test.message;


public class TestServiceConsumer
		extends AbstractTestConsumer<TestServiceHandle> {

	public TestServiceConsumer(String... expectedMessages) {
		super(expectedMessages);
	}

}
