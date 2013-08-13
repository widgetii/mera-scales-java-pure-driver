package ru.aplix.mera.tester;

import static java.awt.ComponentOrientation.RIGHT_TO_LEFT;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;

import ru.aplix.mera.tester.config.ConfigDialog;


public class TesterToolBar extends JToolBar {

	private static final long serialVersionUID = -8932322029296595575L;

	private final TesterApp app;
	private final ScalesPortSelector portSelector;
	private final JLabel weight;

	public TesterToolBar(TesterContent content) {
		this.app = content.app();
		this.portSelector = new ScalesPortSelector(content.app());
		this.weight = new JLabel();
		add(new JButton(new AbstractAction("Настройка") {
			private static final long serialVersionUID = -2109894963363186357L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new ConfigDialog(app()).setVisible(true);
			}
		}));
		add(this.portSelector);
		add(new WeighingButton(this));
		this.weight.setComponentOrientation(RIGHT_TO_LEFT);
		this.weight.setPreferredSize(new Dimension(100, 20));
		add(this.weight);
	}

	public final TesterApp app() {
		return this.app;
	}

	public final ScalesPortSelector getPortSelector() {
		return this.portSelector;
	}

	public final JLabel getWeight() {
		return this.weight;
	}

	final void init() {
		this.portSelector.init();
	}

}
