package ru.aplix.mera.tester;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.SwingUtilities.isEventDispatchThread;
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
	private final JFrame frame = new JFrame("Тестирование весов \"Мера\"");
	private final TesterContent content = new TesterContent(this);

	public final ScalesService getScalesService() {
		return this.scalesService;
	}

	public final JFrame getFrame() {
		return this.frame;
	}

	public final TesterContent getContent() {
		return this.content;
	}

	@Override
	public void run() {
		this.frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				release();
				System.exit(0);
			}
		});

		this.frame.setContentPane(this.content);
		this.content.init();
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public void perform(Runnable action) {
		if (isEventDispatchThread()) {
			action.run();
		} else {
			invokeLater(action);
		}
	}

	public void log(final String message) {
		perform(new Runnable() {
			@Override
			public void run() {
				getContent().getLog().log(message);
			}
		});
	}

	private void init() {
		this.scalesService.getScalesPortIds();// Prefetch them.
	}

	private void release() {
	}

}