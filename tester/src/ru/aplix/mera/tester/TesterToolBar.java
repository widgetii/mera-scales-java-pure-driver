package ru.aplix.mera.tester;

import javax.swing.JToolBar;


public class TesterToolBar extends JToolBar {

	private static final long serialVersionUID = -8932322029296595575L;

	private final TesterApp app;
	private final ScalesPortSelector portSelector;

	public TesterToolBar(TesterContent content) {
		this.app = content.app();
		this.portSelector = new ScalesPortSelector(content.app());
		add(this.portSelector);
		add(new WeighingButton(this));
	}

	public final TesterApp app() {
		return this.app;
	}

	public final ScalesPortSelector getPortSelector() {
		return this.portSelector;
	}

	final void init() {
		this.portSelector.init();
	}

}
