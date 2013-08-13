package ru.aplix.mera.tester.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;

import ru.aplix.mera.scales.ScalesConfig;
import ru.aplix.mera.scales.ScalesOption;
import ru.aplix.mera.tester.TesterApp;


public abstract class AbstractConfig extends JPanel {

	private static final long serialVersionUID = 4664921595025884114L;

	private final TesterApp app;
	private final ScalesConfig config;
	private int line;

	public AbstractConfig(TesterApp app) {
		super(new GridBagLayout());
		this.app = app;
		this.config =
				app.getContent().getToolBar().getPortSelector().getConfig();
	}

	public final TesterApp app() {
		return this.app;
	}

	public final ScalesConfig getConfig() {
		return this.config;
	}

	public void addOption(String label, JComponent component) {

		final GridBagConstraints labelConstraints = new GridBagConstraints();

		labelConstraints.anchor = GridBagConstraints.EAST;
		labelConstraints.fill = GridBagConstraints.NONE;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = this.line;
		labelConstraints.insets = new Insets(2, 10, 2, 2);

		add(new JLabel(label), labelConstraints);

		final GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.gridy = this.line;
		constraints.weightx = 1;
		constraints.insets = new Insets(2, 2, 2, 10);

		add(component, constraints);

		++this.line;
	}

	protected abstract ScalesConfig updateConfig(ScalesConfig config);

	protected String optionValue(ScalesOption<?> option) {

		final Object value = getConfig().get(option);

		if (value == null) {
			return "";
		}
		if (value.equals(option.getDefaultValue())) {
			return "";
		}

		return value.toString();
	}

	protected static Long intOption(JComboBox<?> comboBox) {

		final String selected = (String) comboBox.getSelectedItem();

		if (selected == null || selected.isEmpty()) {
			return null;
		}

		try {
			return Long.parseLong(selected);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
