package ru.aplix.mera.test.scales;

import ru.aplix.mera.scales.ScalesErrorHandle;
import ru.aplix.mera.scales.ScalesErrorMessage;


public class ErrorConsumer
		extends ScalesConsumer<ScalesErrorHandle, ScalesErrorMessage> {

	@Override
	public void messageReceived(ScalesErrorMessage message) {

		final Throwable cause = message.getCause();

		if (cause != null) {
			cause.printStackTrace();
		} else {
			System.err.println("ERROR: " + message.getErrorMessage());
		}

		super.messageReceived(message);
	}

}
