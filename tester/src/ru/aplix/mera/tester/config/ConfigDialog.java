package ru.aplix.mera.tester.config;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;

import ru.aplix.mera.scales.config.ScalesConfig;
import ru.aplix.mera.tester.ScalesPortSelector;
import ru.aplix.mera.tester.TesterApp;


public class ConfigDialog extends JDialog {

	private static final long serialVersionUID = 6177272804555219481L;

	private final TesterApp app;
	private final GlobalConfig globalConfig;
	private final Byte9Config byte9Config;

	public ConfigDialog(TesterApp app) {
		super(app.getFrame(), "Настройки", true);
		this.app = app;
		this.globalConfig = new GlobalConfig(app);
		this.byte9Config = new Byte9Config(app);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(app.getFrame());

		final JPanel content = new JPanel(new BorderLayout());

		final JTabbedPane tabs = new JTabbedPane();

		tabs.addTab("Общие", this.globalConfig);
		tabs.addTab("Byte9", this.byte9Config);

		content.add(tabs, BorderLayout.CENTER);

		final JToolBar buttons = new JToolBar();

		buttons.setFloatable(false);

		buttons.add(new JButton(new AbstractAction("Применить") {
			private static final long serialVersionUID = 2491787427013921945L;
			@Override
			public void actionPerformed(ActionEvent e) {
				updateConfig();
				ConfigDialog.this.dispose();
			}
		}));
		buttons.add(new JButton(new AbstractAction("Отмена") {
			private static final long serialVersionUID = -4045016633663476368L;
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigDialog.this.dispose();
			}
		}));

		content.add(buttons, BorderLayout.SOUTH);

		setContentPane(content);
		pack();
	}

	public final TesterApp app() {
		return this.app;
	}

	private void updateConfig() {

		final ScalesPortSelector portSelector =
				app().getContent().getToolBar().getPortSelector();
		ScalesConfig config = portSelector.getConfig();

		config = this.globalConfig.updateConfig(config);
		config = this.byte9Config.updateConfig(config);

		portSelector.setConfig(config);
	}

}
