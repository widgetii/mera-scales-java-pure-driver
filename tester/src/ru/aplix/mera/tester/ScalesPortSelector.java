package ru.aplix.mera.tester;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JComboBox;

import ru.aplix.mera.scales.ScalesPortId;


public class ScalesPortSelector
		extends JComboBox<ScalesPortSelector.PortOption> {

	private static final long serialVersionUID = -3527716597948842233L;

	private static PortOption[] options(TesterApp app) {

		final List<? extends ScalesPortId> portIds =
				app.getScalesService().getScalesPortIds();
		final TreeSet<PortOption> options = new TreeSet<>();

		options.add(new PortOption(app));
		for (ScalesPortId portId : portIds) {
			options.add(new PortOption(app, portId));
		}

		return options.toArray(new PortOption[options.size()]);
	}

	public ScalesPortSelector(TesterApp app) {
		super(options(app));
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				portSelected();
			}
		});
		portSelected();
	}

	private PortOption selected;

	private void portSelected() {

		final PortOption option = (PortOption) getSelectedItem();

		if (this.selected != null) {
			if (this.selected == option) {
				return;
			}
			this.selected.deselect();
		}
		this.selected = option;
		option.select();
	}

	public static final class PortOption implements Comparable<PortOption> {

		private final TesterApp app;
		private final ScalesPortId portId;

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

		public void select() {
			if (getPortId() == null) {
				return;
			}
			this.app.log("Устройство: " + this);
			// TODO Auto-generated method stub
		}

		public void deselect() {
			if (getPortId() == null) {
				return;
			}
			// TODO Auto-generated method stub
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

}
