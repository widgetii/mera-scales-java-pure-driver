package ru.aplix.mera.tester;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class TesterContent extends JPanel {

	private static final long serialVersionUID = 2958498598574285722L;

	private final TesterApp app;
	private final TesterToolBar toolBar;
	private final TesterLog log;
	private final TesterStatusBar statusBar;

	public TesterContent(TesterApp app) {
		super(new BorderLayout());
		this.app = app;
		this.toolBar = new TesterToolBar(this);
		this.log = new TesterLog();
		this.statusBar = new TesterStatusBar(this);

		add(this.toolBar, BorderLayout.NORTH);

		final JScrollPane scrollPane = new JScrollPane(this.log);

		scrollPane.setPreferredSize(new Dimension(800, 600));
		add(scrollPane, BorderLayout.CENTER);
		add(this.statusBar, BorderLayout.SOUTH);

		init();
	}

	public final TesterApp app() {
		return this.app;
	}

	public final TesterLog getLog() {
		return this.log;
	}

	public final TesterStatusBar getStatusBar() {
		return this.statusBar;
	}

	public final TesterToolBar getToolBar() {
		return this.toolBar;
	}

	private void init() {
		this.toolBar.init();
	}

}
