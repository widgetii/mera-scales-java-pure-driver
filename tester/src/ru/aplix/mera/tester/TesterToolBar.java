package ru.aplix.mera.tester;

import javax.swing.JToolBar;


public class TesterToolBar extends JToolBar {

	private static final long serialVersionUID = -8932322029296595575L;

	public TesterToolBar(TesterApp app) {
		add(new ScalesPortSelector(app));
	}

}
