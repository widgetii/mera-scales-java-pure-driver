package ru.aplix.mera.tester;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TesterApp implements Runnable {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new TesterApp());
	}

	@Override
	public void run() {

		final JFrame frame = new JFrame("Тестирование весов \"Мера\"");

		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				release();
				System.exit(0);
			}
		});

		frame.setVisible(true);
	}

	private void release() {
	}

}