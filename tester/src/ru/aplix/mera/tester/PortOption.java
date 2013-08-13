package ru.aplix.mera.tester;

import java.io.PrintWriter;
import java.io.StringWriter;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.*;


public final class PortOption
		implements Comparable<PortOption>,
		MeraConsumer<ScalesPortHandle, ScalesStatusMessage> {

	private final TesterApp app;
	private final ScalesPortId portId;
	private final ScalesErrorListener errorListener;
	private ScalesPortHandle handle;

	PortOption(TesterApp app) {
		this.app = app;
		this.portId = null;
		this.errorListener = null;
	}

	PortOption(TesterApp app, ScalesPortId portId) {
		this.app = app;
		this.portId = portId;
		this.errorListener = new ScalesErrorListener(app);
	}

	public final ScalesPortId getPortId() {
		return this.portId;
	}

	public final ScalesPortHandle getHandle() {
		return this.handle;
	}

	public final void select() {
		if (getPortId() == null) {
			return;
		}
		this.app.log("Порт: " + this);

		final ScalesPort port = getPortId().getPort();

		updateConfig(
				this.app.getContent()
				.getToolBar()
				.getPortSelector()
				.getConfig());
		this.handle = port.subscribe(this);
	}

	public final void deselect() {
		if (getPortId() == null) {
			return;
		}
		this.handle.unsubscribe();
		this.app.log("Отключено\n");
	}

	@Override
	public void consumerSubscribed(ScalesPortHandle handle) {
		this.handle = handle;
		handle.listenForErrors(this.errorListener);
	}

	@Override
	public void messageReceived(ScalesStatusMessage message) {
	}

	@Override
	public void consumerUnubscribed(ScalesPortHandle handle) {
	}

	@Override
	public int compareTo(PortOption o) {

		final ScalesPortId pid1 = getPortId();
		final ScalesPortId pid2 = o.getPortId();

		if (pid1 == null) {
			return pid2 == null ? 0 : -1;
		}
		if (pid2 == null) {
			return 1;
		}

		final String proto1 = pid1.getProtocol().getProtocolId();
		final String proto2 = pid2.getProtocol().getProtocolId();
		final int protoCmp = proto1.compareTo(proto2);

		if (protoCmp != 0) {
			return protoCmp;
		}

		return pid1.getPortId().compareTo(pid2.getPortId());
	}

	@Override
	public int hashCode() {
		return this.portId == null ? 0 : this.portId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final PortOption other = (PortOption) obj;

		if (this.portId == null) {
			if (other.portId != null) {
				return false;
			}
		} else if (!this.portId.equals(other.portId)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		if (this.portId == null) {
			return "Порт";
		}
		return this.portId.toString();
	}

	void updateConfig(ScalesConfig config) {

		final ScalesPortId portId = getPortId();

		if (portId == null) {
			return;
		}

		final ScalesPort port = portId.getPort();
		final ScalesConfig oldConfig = port.getConfig();
		final ScalesConfig newConfig = oldConfig.update(config);

		if (newConfig != oldConfig) {
			port.setConfig(newConfig);
		}
	}

	private static final class ScalesErrorListener
			implements MeraConsumer<ScalesErrorHandle, ScalesErrorMessage> {

		private final TesterApp app;

		ScalesErrorListener(TesterApp app) {
			this.app = app;
		}

		@Override
		public void consumerSubscribed(ScalesErrorHandle handle) {
		}

		@Override
		public void messageReceived(ScalesErrorMessage message) {

			final Throwable cause = message.getCause();

			if (cause == null) {
				this.app.log("ERROR: " + message.getErrorMessage());
				return;
			}
			logStackTrace(cause);
		}

		@Override
		public void consumerUnubscribed(ScalesErrorHandle handle) {
		}

		private void logStackTrace(Throwable cause) {

			final StringWriter out = new StringWriter();
			final PrintWriter writer = new PrintWriter(out);

			try {
				writer.print("ERROR: ");
				cause.printStackTrace(writer);
			} finally {
				writer.close();
			}

			this.app.log(out.toString());
		}

	}

}