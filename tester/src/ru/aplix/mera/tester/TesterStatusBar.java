package ru.aplix.mera.tester;

import javax.swing.JLabel;
import javax.swing.JToolBar;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.*;


public class TesterStatusBar
		extends JToolBar
		implements PortListener,
		MeraConsumer<ScalesPortHandle, ScalesStatusMessage> {

	private static final long serialVersionUID = -4014348334455178545L;

	private static final String NOT_CONNECTED = "Не подключено";

	private final TesterApp app;
	private final JLabel device;
	private final JLabel status;
	private ScalesPortHandle handle;

	public TesterStatusBar(TesterContent content) {
		this.app = content.app();
		setFloatable(false);
		this.status = new JLabel(NOT_CONNECTED);
		this.device = new JLabel("");
		add(this.status);
		addSeparator();
		add(this.device);

		final ScalesPortSelector portSelector =
				content.getToolBar().getPortSelector();

		portSelector.addPortListener(this);
	}

	public final TesterApp app() {
		return this.app;
	}

	@Override
	public void portDeselected(PortOption port) {
		this.status.setText(NOT_CONNECTED);
		this.device.setText("");
		if (this.handle != null && this.handle.isSubscribed()) {
			this.handle.unsubscribe();
		}
	}

	@Override
	public void portSelected(PortOption port) {

		final ScalesPortId portId = port.getPortId();

		if (portId == null) {
			return;
		}
		this.status.setText(portId.toString());
		this.device.setText("");
		this.handle = portId.getPort().subscribe(this);
	}

	@Override
	public void consumerSubscribed(ScalesPortHandle handle) {
	}

	@Override
	public void messageReceived(final ScalesStatusMessage message) {
		app().perform(new Runnable() {
			@Override
			public void run() {
				updateStatus(message);
			}
		});
	}

	@Override
	public void consumerUnubscribed(ScalesPortHandle handle) {
	}

	private void updateStatus(ScalesStatusMessage message) {
		switch (message.getScalesStatus()) {
		case SCALES_ERROR:

			final String error = message.getScalesError();

			if (error == null) {
				this.app.log("Ошибка подключения");
			} else {
				this.app.log("Ошибка подключения: " + error);
			}

			this.device.setText("Ошибка подключения");

			break;
		case SCALES_CONNECTED:

			final ScalesDevice device = message.getScalesDevice();
			final String deviceDesc =
					device.getDeviceId()
					+ " (" + device.getDeviceType()
					+ ", v" + device.getMajorRevision()
					+ '.' + device.getMinorRevision() + ')';

			this.app.log("Подключено " + deviceDesc);
			this.device.setText(deviceDesc);

			break;
		default:

			final String msg = message.toString();

			this.app.log(msg);
			this.device.setText(msg);
		}
	}

}
