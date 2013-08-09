package ru.aplix.mera.tester;

import javax.swing.JTextArea;


public class TesterLog extends JTextArea {

	private static final long serialVersionUID = 5024361308511495098L;

	public TesterLog() {
		setEditable(false);
	}

	public void log(String message) {
		append(message + "\n");
	}

}
