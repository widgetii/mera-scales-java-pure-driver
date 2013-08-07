package ru.aplix.mera.scales;


/**
 * Scales error message caused by error or exception.
 */
public class ThrowableErrorMessage implements ScalesErrorMessage {

	private final Throwable cause;

	/**
	 * Constructs error message caused by throwable.
	 *
	 * @param cause the throwable cause this error.
	 */
	public ThrowableErrorMessage(Throwable cause) {
		this.cause =
				cause != null
				? cause : new NullPointerException("Unknown exception");
	}

	@Override
	public String getErrorMessage() {
		return this.cause.getMessage();
	}

	@Override
	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public String toString() {
		if (this.cause == null) {
			return null;
		}
		return this.cause.toString();
	}

}
