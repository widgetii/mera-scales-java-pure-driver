package ru.aplix.mera;

import static purejavacomm.CommPortIdentifier.PORT_SERIAL;
import static purejavacomm.CommPortIdentifier.addPortName;
import static purejavacomm.CommPortIdentifier.getPortIdentifier;
import static purejavacomm.SerialPort.PARITY_MARK;
import static purejavacomm.SerialPort.PARITY_SPACE;
import static purejavacomm.SerialPortMode.DEFAULT_SERIAL_PORT_MODE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import purejavacomm.*;


public class CommTest {

	private static final SerialPortMode INIT_PORT_MODE =
			DEFAULT_SERIAL_PORT_MODE.setBaudRate(14400)
			.setParity(PARITY_MARK);
	private static final SerialPortParams ADDRESS_PARAMS =
			INIT_PORT_MODE.getParams().setParity(PARITY_MARK);
	private static final SerialPortParams COMMAND_PARAMS =
			INIT_PORT_MODE.getParams().setParity(PARITY_SPACE);

	private static final byte[] ZERO_ADDRESS = new byte[] {0, 0, 0};
	private static final byte END_OF_BLOCK = (byte) 0x0d;
	private static final byte[] READ_DEVICE_ID_CMD =
			new byte[] {1, 0, 0, 0, 1, END_OF_BLOCK};

	public static void main(String[] args) {
		try {
			new CommTest().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {

		final SerialPort port = openPort();

		try {

			final SerialReader reader = new SerialReader(port);

			port.addEventListener(reader);
	        port.notifyOnDataAvailable(true);

	        final Thread writer = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						requestDeviceId(port);
						if (!reader.waitForResponse(1000)) {
							requestDeviceId(port);
							if (!reader.waitForResponse(1000)) {
								System.err.println("(!) NO RESPONSE");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});

	        writer.start();
	        writer.join();
		} finally {
			port.close();
		}
	}

	private static void requestDeviceId(SerialPort port) throws Exception {
		sendZeroAddress(port);

		@SuppressWarnings("resource")
		final OutputStream out = port.getOutputStream();

		System.err.println("(!) Request device ID");
		out.write(READ_DEVICE_ID_CMD);
		out.flush();
	}

	private static void printResponse(final byte[] response) {
		System.out.print("Response (" + response.length + " bytes): ");

		for (byte b : response) {

			final String str = Integer.toHexString(b);

			if (str.length() == 1) {
				System.out.print("#0");
			} else {
				System.out.print("#");
			}

			System.out.print(str);
		}

		System.out.println();
	}

	private static SerialPort openPort() throws Exception {
		System.err.println("(!) Opening COM6");

		addPortName("COM6", PORT_SERIAL, null);// Prevent occasional opening.

		final CommPortIdentifier portId = getPortIdentifier("COM6");
		final SerialPort port =
				portId.openSerial("Mera scales test", 2000, INIT_PORT_MODE);

		System.err.println("(!) Port opened");

		return port;
	}

	@SuppressWarnings("resource")
	private static void sendZeroAddress(SerialPort port) throws Exception {

		final OutputStream out = port.getOutputStream();

		System.err.println("(!) Set params: " + ADDRESS_PARAMS);
		port.setSerialPortParams(ADDRESS_PARAMS);
		System.err.println("(!) Send zero address");
		out.write(ZERO_ADDRESS);
		out.flush();
		Thread.sleep(200);
		System.err.println("(!) Set params: " + COMMAND_PARAMS);
		port.setSerialPortParams(COMMAND_PARAMS);
	}

	private static final class SerialReader implements SerialPortEventListener {

		private final SerialPort port;
		private volatile boolean hasResponse;

		SerialReader(SerialPort port) {
			this.port = port;
		}

		@Override
		public void serialEvent(SerialPortEvent ev) {
			try {
				printResponse(readResponse(this.port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("resource")
		private byte[] readResponse(SerialPort port) throws IOException {
			System.err.println("(!) Read response");

			final InputStream in = port.getInputStream();
			final byte[] response = new byte[9];
			int responseLen = 0;
			boolean hasResponse = false;

			for (;;) {

				final int read = in.read();

				if (!hasResponse) {
					responseReceived();
					hasResponse = true;
				}
				if (read < 0) {
					break;
				}
				response[responseLen++] = (byte) read;
				if (read == END_OF_BLOCK) {
					System.err.println("(!) End of block");
					break;
				}
			}

			if (responseLen == response.length) {
				return response;
			}

			return Arrays.copyOf(response, responseLen);
		}

		private synchronized void responseReceived() {
			this.hasResponse = true;
			notifyAll();
		}

		private boolean waitForResponse(long timeout) {

			final long time = System.currentTimeMillis() + timeout;

			synchronized (this) {
				for (;;) {
					if (this.hasResponse) {
						return true;
					}
					if (System.currentTimeMillis() >= time) {
						return false;
					}
					try {
						wait(timeout);
					} catch (InterruptedException e) {
						return false;
					}
				}
			}
		}

	}

}
