package ru.aplix.mera.scales;


/**
 * Unpredictable scales operations error.
 */
public interface ScalesErrorMessage {

	/**
	 * Error message.
	 *
	 * @return human readable error message.
	 */
	String getErrorMessage();

	/**
	 * Returns the throwable caused this error.
	 *
	 * @return throwable or <code>null</code> if error is not caused by
	 * error or exception.
	 */
	Throwable getCause();

}
