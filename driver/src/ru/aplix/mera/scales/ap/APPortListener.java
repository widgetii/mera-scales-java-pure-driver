package ru.aplix.mera.scales.ap;

import static purejavacomm.CommPortIdentifier.PORT_SERIAL;
import static purejavacomm.CommPortIdentifier.addPortName;
import static purejavacomm.CommPortIdentifier.getPortIdentifier;
import static purejavacomm.SerialPortMode.DEFAULT_SERIAL_PORT_MODE;
import static ru.aplix.mera.scales.ap.APErrorMesssage.AP_ERROR_MESSAGE;
import static ru.aplix.mera.scales.ap.AutoProtocol.AP_CONNECTION_NAME;
import static ru.aplix.mera.scales.ap.AutoProtocol.AP_CONNECTION_TIMEOUT;

import java.io.InputStream;
import java.util.Arrays;

import purejavacomm.*;
import ru.aplix.mera.scales.ThrowableErrorMessage;
import ru.aplix.mera.scales.backend.InterruptAction;
import ru.aplix.mera.scales.config.ScalesConfig;


class APPortListener implements SerialPortEventListener, InterruptAction {

	private static final SerialPortMode PORT_MODE =
			DEFAULT_SERIAL_PORT_MODE.setBaudRate(115200);

	private final APDriver driver;
	private final boolean reportWeight;
	private SerialPort port;
	private boolean hasResponse;
	private volatile boolean interrupted;
	private volatile boolean error;

	APPortListener(APDriver driver, boolean reportWeight) {
		this.driver = driver;
		this.reportWeight = reportWeight;
	}

	public final boolean isInterrupted() {
		return this.interrupted;
	}

	public final boolean isError() {
		return this.error;
	}

	public final SerialPort getPort() throws Exception {
		if (this.port != null) {
			return this.port;
		}
		return this.port = openPort();
	}

	@Override
	public final synchronized void interrupt() {
		this.interrupted = true;
		notifyAll();
		done();
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		final long weighingTime = System.currentTimeMillis();

		try {
			responseReceived(respond(weighingTime));
		} catch (Throwable e) {
			this.driver.getWeightReceiver().error(new ThrowableErrorMessage(e));
		}
	}

	public boolean waitForResponse(long timeout) {

		final long time = System.currentTimeMillis() + timeout;
		long left = timeout;

		for (;;) {
			synchronized (this) {
				if (this.hasResponse) {
					return true;
				}
				if (left <= 0) {
					return false;
				}
				try {
					wait(left);
				} catch (InterruptedException e) {
					return false;
				}
			}
			left = time - System.currentTimeMillis();
		}
	}

	public void done() {
		if (this.port != null) {
			this.port.close();
			this.port = null;
		}
	}

	private SerialPort openPort() throws Exception {

		final String portName = this.driver.getDevice().getDeviceId();

		addPortName(portName, PORT_SERIAL, null);// Prevent occasional opening.

		final CommPortIdentifier portId = getPortIdentifier(portName);
		final ScalesConfig config = this.driver.getWeightReceiver().getConfig();

		final String connectionName = config.get(AP_CONNECTION_NAME);
		final int timeout = config.get(AP_CONNECTION_TIMEOUT).intValue();

		return portId.openSerial(connectionName, timeout, PORT_MODE);
	}

	private boolean respond(long weighingTime) throws Exception {

		final APPacket packet = readPacket();

		if (packet == null) {
			return true;
		}
		if (!packet.isValid()) {
			this.driver.getWeightReceiver().error(AP_ERROR_MESSAGE);
			return false;
		}
		if (this.reportWeight) {
			this.driver.getWeightReceiver().updateWeight(
					new APWeightUpdate(packet, weighingTime));
		}
		return true;
	}

	private APPacket readPacket() throws Exception {

		final byte[] rawData = readData();

		if (rawData == null) {
			return null;
		}

		return new APPacket(rawData);
	}

	@SuppressWarnings("resource")
	private byte[] readData() throws Exception {
		if (doneIfInterrupted()) {
			return null;
		}

		final InputStream in = getPort().getInputStream();
		final byte[] response = new byte[17];
		int responseLen = 0;

		for (;;) {
			if (doneIfInterrupted()) {
				return null;
			}

			final int read = in.read();

			if (read < 0) {
				break;
			}
			response[responseLen++] = (byte) read;
			if (responseLen >= response.length) {
				break;
			}
		}
		if (doneIfInterrupted()) {
			return null;
		}
		if (responseLen == response.length) {
			return response;
		}

		return Arrays.copyOf(response, responseLen);
	}

	private synchronized boolean doneIfInterrupted() {
		if (!isInterrupted()) {
			return false;
		}
		done();
		return true;
	}

	private synchronized void responseReceived(boolean error) {
		this.hasResponse = true;
		this.error = error;
		notifyAll();
	}

}
