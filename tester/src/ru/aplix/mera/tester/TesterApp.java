package ru.aplix.mera.tester;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static ru.aplix.mera.scales.ScalesService.newScalesService;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import ru.aplix.mera.scales.ScalesService;


public class TesterApp implements Runnable {

	public static void main(String[] args) {

		final TesterApp app = new TesterApp();

		app.init();
		invokeLater(app);
	}

	private final ScalesService scalesService = newScalesService();
	private TesterLog log;

	public final ScalesService getScalesService() {
		return this.scalesService;
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

		final TesterContent content = new TesterContent(this);

		this.log = content.getLog();
		frame.setContentPane(content);
		frame.pack();
		frame.setVisible(true);
	}

	public void log(String message) {
		this.log.log(message);
	}

	private void init() {
		this.scalesService.getScalesPortIds();// Prefetch them.
	}

	private void release() {
	}

}