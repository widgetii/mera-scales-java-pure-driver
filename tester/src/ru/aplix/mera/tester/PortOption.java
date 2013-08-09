package ru.aplix.mera.tester;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.ScalesPortHandle;
import ru.aplix.mera.scales.ScalesPortId;
import ru.aplix.mera.scales.ScalesStatusMessage;


public final class PortOption
		implements Comparable<PortOption>,
		MeraConsumer<ScalesPortHandle, ScalesStatusMessage> {

	private final TesterApp app;
	private final ScalesPortId portId;
	private ScalesPortHandle handle;

	PortOption(TesterApp app) {
		this.app = app;
		this.portId = null;
	}

	PortOption(TesterApp app, ScalesPortId portId) {
		this.app = app;
		this.portId = portId;
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
		this.app.log("Устройство: " + this);
		this.handle = getPortId().getPort().subscribe(this);
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

		final String proto1 = pid1.getProtocol().getProtocolName();
		final String proto2 = pid2.getProtocol().getProtocolName();
		final int protoCmp = proto1.compareTo(proto2);

		if (protoCmp != 0) {
			return protoCmp;
		}

		return pid1.getDeviceId().compareTo(pid2.getDeviceId());
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
			return "Устройство";
		}
		return this.portId.toString();
	}

}