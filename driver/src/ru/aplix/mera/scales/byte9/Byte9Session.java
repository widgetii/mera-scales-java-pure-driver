package ru.aplix.mera.scales.byte9;

import static purejavacomm.CommPortIdentifier.PORT_SERIAL;
import static purejavacomm.CommPortIdentifier.addPortName;
import static purejavacomm.CommPortIdentifier.getPortIdentifier;
import static purejavacomm.SerialPort.PARITY_MARK;
import static purejavacomm.SerialPort.PARITY_SPACE;
import static purejavacomm.SerialPortMode.DEFAULT_SERIAL_PORT_MODE;
import static ru.aplix.mera.scales.byte9.Byte9Packet.BYTE9_TERMINATOR_BYTE;
import static ru.aplix.mera.scales.byte9.Byte9StatusUpdate.byte9ErrorStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import purejavacomm.*;
import ru.aplix.mera.scales.backend.ScalesRequest;


final class Byte9Session {

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

		this.port = portId.openSerial("Mera scales test", 2000, INIT_PORT_MODE);
	}

	public final long getResponseTime() {
		return this.responseTime;
	}

	public Byte9Packet send(Byte9Packet request) throws Exception {

		final ResponseListener listener = new ResponseListener();

		this.port.addEventListener(listener);
		this.port.notifyOnDataAvailable(true);
		sendPacket(request);
		if (!listener.waitForResponse(1000L)) {
			sendPacket(request);
			if (!listener.waitForResponse(1000L)) {
				this.scalesRequest.updateStatus(
						byte9ErrorStatus("No response"));
				return null;
			}
		}

		this.responseTime = listener.responseTime;

		return readResponse(request);
	}

	private void sendPacket(
			Byte9Packet packet)
	throws Exception {

		final byte[] rawData = packet.rawData();

		this.port.setSerialPortParams(ADDRESS_PARAMS);
		this.port.getOutputStream().write(rawData, 0, 3);
		Thread.sleep(200);
		this.port.setSerialPortParams(COMMAND_PARAMS);
		this.port.getOutputStream().write(rawData, 3, rawData.length);
	}

	private Byte9Packet readResponse(Byte9Packet request) throws IOException {

		final byte[] responseData = readResponseData();
		final Byte9Packet response = new Byte9Packet(responseData);

		if (!validateResponse(request, response)) {
			return null;
		}

		return response;
	}

	@SuppressWarnings("resource")
	private byte[] readResponseData() throws IOException {

		final InputStream in = this.port.getInputStream();
		final byte[] response = new byte[9];
		int responseLen = 0;

		for (;;) {

			final int read = in.read();

			if (read < 0) {
				break;
			}
			response[responseLen++] = (byte) read;
			if (read == BYTE9_TERMINATOR_BYTE) {
				break;
			}
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
			implements SerialPortEventListener {

		private boolean hasResponse;
		private long responseTime;

		@Override
		public void serialEvent(SerialPortEvent ev) {
			responseReceived();
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
