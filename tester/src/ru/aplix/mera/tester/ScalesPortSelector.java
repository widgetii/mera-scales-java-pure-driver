package ru.aplix.mera.tester;

import static ru.aplix.mera.scales.config.ScalesConfig.DEFAULT_SCALES_CONFIG;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.event.EventListenerList;

import ru.aplix.mera.scales.ScalesPortId;
import ru.aplix.mera.scales.config.ScalesConfig;


public class ScalesPortSelector extends JComboBox<PortOption> {

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
	}

	private ScalesConfig config = DEFAULT_SCALES_CONFIG;
	private PortOption selected;
	private EventListenerList portListeners = new EventListenerList();

	public final PortOption getSelected() {
		return this.selected;
	}

	public ScalesConfig getConfig() {
		return this.config;
	}

	public void setConfig(ScalesConfig config) {
		this.config = config;
		getSelected().updateConfig(config);
	}

	public final void addPortListener(PortListener portListener) {
		this.portListeners.add(PortListener.class, portListener);
	}

	public final void removePortListener(PortListener portListener) {
		this.portListeners.remove(PortListener.class, portListener);
	}

	final void init() {
		portSelected();
	}

	private void portSelected() {

		final PortOption selected = (PortOption) getSelectedItem();

		if (this.selected != null) {
			if (this.selected == selected) {
				return;
			}
			for (PortListener listener
					: this.portListeners.getListeners(PortListener.class)) {
				listener.portDeselected(this.selected);
			}
			this.selected.deselect();
		}
		this.selected = selected;
		selected.select();
		for (PortListener listener
				: this.portListeners.getListeners(PortListener.class)) {
			listener.portSelected(selected);
		}
	}

}
