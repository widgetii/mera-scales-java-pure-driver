package ru.aplix.mera.scales.byte9;

import static purejavacomm.CommPortIdentifier.PORT_SERIAL;
import static purejavacomm.CommPortIdentifier.addPortName;
import static purejavacomm.CommPortIdentifier.getPortIdentifier;
import static purejavacomm.SerialPort.PARITY_MARK;
import static purejavacomm.SerialPort.PARITY_SPACE;
import static purejavacomm.SerialPortMode.DEFAULT_SERIAL_PORT_MODE;
import static ru.aplix.mera.scales.byte9.Byte9Protocol.*;
import static ru.aplix.mera.scales.byte9.Byte9StatusUpdate.byte9ErrorStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import purejavacomm.*;
import ru.aplix.mera.scales.backend.InterruptAction;
import ru.aplix.mera.scales.backend.ScalesRequest;
import ru.aplix.mera.scales.config.ScalesConfig;


final class Byte9Session implements AutoCloseable {

	private static final SerialPortMode INIT_PORT_MODE =
			DEFAULT_SERIAL_PORT_MODE.setBaudRate(14400)
			.setParity(PARITY_MARK);
	private static final SerialPortParams ADDRESS_PARAMS =
			INIT_PORT_MODE.getParams().setParity(PARITY_MARK);
	private static final SerialPortParams COMMAND_PARAMS =
			INIT_PORT_MODE.getParams().setParity(PARITY_SPACE);

	private final ScalesRequest scalesRequest;
	private final SerialPort port;
	private long responseTime;

	Byte9Session(
			ScalesRequest scalesRequest,
			String portName)
	throws NoSuchPortException, PortInUseException {
		this.scalesRequest = scalesRequest;
		addPortName(portName, PORT_SERIAL, null);// Prevent occasional opening.

		final CommPortIdentifier portId = getPortIdentifier(portName);
		final ScalesConfig config = scalesRequest.getConfig();

		final String connectionName = config.get(BYTE9_CONNECTION_NAME);
		final int timeout = config.get(BYTE9_CONNECTION_TIMEOUT).intValue();

		this.port = portId.openSerial(connectionName, timeout, INIT_PORT_MODE);
	}

	public final long getResponseTime() {
		return this.responseTime;
	}

	public Byte9Packet send(Byte9Packet request) throws Exception {

		final ResponseListener listener = new ResponseListener();

		this.scalesRequest.onInterrupt(listener);
		this.port.addEventListener(listener);
		this.port.notifyOnDataAvailable(true);
		if (!request(request, listener)) {
			this.scalesRequest.updateStatus(
					byte9ErrorStatus("No response"));
			return null;
		}
		if (listener.isInterrupted()) {
			return null;
		}

		this.responseTime = listener.responseTime;

		return readResponse(request, listener);
	}

	@Override
	public void close() throws Exception {
		if (this.port != null) {
			this.port.close();
		}
	}

	private boolean request(
			Byte9Packet request,
			ResponseListener listener)
	throws Exception {

		final ScalesConfig config = this.scalesRequest.getConfig();
		final long responseTimeout =
				config.get(BYTE9_RESPONSE_TIMEOUT).longValue();
		long retriesLeft = config.get(BYTE9_COMMAND_RETRIES).longValue();
		final long dataDelay = config.get(BYTE9_DATA_DELAY).longValue();

		do {
			sendPacket(request, listener, dataDelay);
			if (listener.waitForResponse(responseTimeout)) {
				return true;
			}
			--retriesLeft;
		} while (retriesLeft >= 0);

		return false;
	}

	private void sendPacket(
			Byte9Packet packet,
			ResponseListener listener,
			long dataDelay)
	throws Exception {

		final byte[] rawData = packet.rawData();

		this.port.setSerialPortParams(ADDRESS_PARAMS);
		this.port.getOutputStream().write(rawData, 0, 3);
		if (listener.waitForResponse(dataDelay)) {
			return;
		}
		this.port.setSerialPortParams(COMMAND_PARAMS);
		this.port.getOutputStream().write(rawData, 3, rawData.length - 3);
	}

	private Byte9Packet readResponse(
			Byte9Packet request,
			ResponseListener listener)
	throws IOException {

		final byte[] responseData = readResponseData(listener);

		if (responseData == null) {
			return null;
		}

		final Byte9Packet response = new Byte9Packet(responseData);

		if (!validateResponse(request, response)) {
			return null;
		}

		return response;
	}

	@SuppressWarnings("resource")
	private byte[] readResponseData(
			ResponseListener listener)
	throws IOException {

		final InputStream in = this.port.getInputStream();
		final byte[] response = new byte[9];
		int responseLen = 0;

		for (;;) {
			if (listener.isInterrupted()) {
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
		if (listener.isInterrupted()) {
			return null;
		}
		if (responseLen == response.length) {
			return response;
		}

		return Arrays.copyOf(response, responseLen);
	}

	private boolean validateResponse(
			Byte9Packet request,
			Byte9Packet response) {

		final Byte9Validity validity = response.validate();

		if (!validity.isValid()) {
			this.scalesRequest.error(new Byte9ValidationError(validity));
			return false;
		}

		final Byte9Command command = response.getCommand();

		if (command == Byte9Command.BYTE9_ERROR_RETURN) {
			this.scalesRequest.error(new Byte9ErrorMessage(response));
			return false;
		}
		if (command != request.getCommand()) {
			throw new IllegalStateException(
					"Wrong command received in response: "
					+ command + ", while " + request.getCommand()
					+ " expected");
		}

		return true;
	}

	private static final class ResponseListener
			implements SerialPortEventListener, InterruptAction {

		private long responseTime;
		private volatile boolean interrupted;
		private boolean hasResponse;

		@Override
		public void serialEvent(SerialPortEvent ev) {
			responseReceived();
		}

		public final boolean isInterrupted() {
			return this.interrupted;
		}

		@Override
		public synchronized void interrupt() {
			this.interrupted = true;
			this.hasResponse = true;
			notifyAll();
		}

		private synchronized void responseReceived() {
			if (!this.hasResponse) {
				this.hasResponse = true;
				this.responseTime = System.currentTimeMillis();
			}
			notifyAll();
		}

		private boolean waitForResponse(long timeout) {

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

	}

}
