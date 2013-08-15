package ru.aplix.mera.tester;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.*;


public class WeighingButton extends JToggleButton implements PortListener {

	private static final long serialVersionUID = 4445305057228682761L;

	private final TesterToolBar toolBar;
	private LoadHandle loadHandle;
	private WeightHandle weightHandle;

	public WeighingButton(TesterToolBar toolBar) {
		this.toolBar = toolBar;
		setEnabled(false);
		setButtonText();
		setAction(new AbstractAction() {
			private static final long serialVersionUID = -3212765577665980548L;
			@Override
			public void actionPerformed(ActionEvent e) {

				final JToggleButton button = (JToggleButton) e.getSource();

				toggleWeighing(button.isSelected());
			}
		});
		toolBar.getPortSelector().addPortListener(this);
	}

	public final TesterApp app() {
		return getToolBar().app();
	}

	public final TesterToolBar getToolBar() {
		return this.toolBar;
	}

	@Override
	public void portDeselected(PortOption port) {
	}

	@Override
	public void portSelected(PortOption port) {
		setEnabled(port.getPortId() != null);
		setSelected(false);
		setButtonText();
		getToolBar().getWeight().setText(null);
	}

	private void setButtonText() {
		setText(isSelected() ? "Остановить" : "Взвешивать");
	}

	private void toggleWeighing(boolean start) {
		setButtonText();
		if (!start) {
			if (this.weightHandle != null) {
				this.loadHandle.unsubscribe();
				this.weightHandle.unsubscribe();
				this.loadHandle = null;
				this.weightHandle = null;
			}
			return;
		}

		final ScalesPortHandle statusHandle =
				getToolBar().getPortSelector().getSelected().getHandle();

		this.loadHandle = statusHandle.requestLoad(
				new MeraConsumer<LoadHandle, LoadMessage>() {
					@Override
					public void consumerSubscribed(LoadHandle handle) {
					}
					@Override
					public void messageReceived(LoadMessage message) {
						setLoad(message);
					}
					@Override
					public void consumerUnsubscribed(LoadHandle handle) {
					}
				});
		this.weightHandle = statusHandle.requestWeight(
				new MeraConsumer<WeightHandle, WeightMessage>() {
					@Override
					public void consumerSubscribed(WeightHandle handle) {
					}
					@Override
					public void messageReceived(WeightMessage message) {
						setWeight(message);
					}
					@Override
					public void consumerUnsubscribed(WeightHandle handle) {
						stopWeighing();
					}
				});
	}

	private void setLoad(LoadMessage message) {

		final String indicator;
		final String logMessage;

		if (message.isLoaded()) {
			indicator = "Загружено";
			logMessage = indicator + " " + message.getWeight() + " г.";
		} else {
			indicator = "Разгружено";
			logMessage = indicator + " до " + message.getWeight() + " г.";
		}

		app().perform(new Runnable() {
			@Override
			public void run() {
				app().log(logMessage);
				getToolBar().getWeight().setText(indicator);
			}
		});
	}

	private void setWeight(final WeightMessage weight) {

		final String w = weight.getWeight() + " г.";

		app().perform(new Runnable() {
			@Override
			public void run() {
				app().log(
						"Вес: " + w + " Измерен за "
						+ weight.getWeighingDuration() + " мс");
				getToolBar().getWeight().setText(w);
			}
		});
	}

	private void stopWeighing() {
		app().perform(new Runnable() {
			@Override
			public void run() {
				setSelected(false);
				setButtonText();
				getToolBar().getWeight().setText(null);
			}
		});
	}

}
