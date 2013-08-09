package ru.aplix.mera.tester;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class TesterContent extends JPanel {

	private static final long serialVersionUID = 2958498598574285722L;

	private final TesterLog log = new TesterLog();

	public TesterContent(TesterApp app) {
		super(new BorderLayout());
		add(new TesterToolBar(app), BorderLayout.NORTH);

		final JScrollPane scrollPane = new JScrollPane(this.log);

		scrollPane.setPreferredSize(new Dimension(800, 600));
		add(scrollPane, BorderLayout.CENTER);
	}

	public final TesterLog getLog() {
		return this.log;
	}

}
