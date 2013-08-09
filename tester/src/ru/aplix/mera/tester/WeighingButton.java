package ru.aplix.mera.tester;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import ru.aplix.mera.message.MeraConsumer;
import ru.aplix.mera.scales.WeightHandle;
import ru.aplix.mera.scales.WeightMessage;


public class WeighingButton extends JToggleButton implements PortListener {

	private static final long serialVersionUID = 4445305057228682761L;

	private final TesterApp app;
	private final ScalesPortSelector portSelector;
	private WeightHandle weightHandle;

	public WeighingButton(TesterToolBar toolBar) {
		this.app = toolBar.app();
		this.portSelector = toolBar.getPortSelector();
		setEnabled(false);
		setButtonText();
		setAction(new AbstractAction() {
			private static final long serialVersionUID = -3212765577665980548L;
			@Override
			public void actionPerformed(ActionEvent e) {

				final JToggleButton button = (JToggleButton) e.getSource();
				final boolean selected = button.isSelected();

				setButtonText();
				toggleWeighing(selected);
			}
		});
		this.portSelector.addPortListener(this);
	}

	@Override
	public void portDeselected(PortOption port) {
		setEnabled(false);
		setSelected(false);
		setButtonText();
	}

	@Override
	public void portSelected(PortOption port) {
		setEnabled(port.getPortId() != null);
		setSelected(false);
		setButtonText();
	}

	private void setButtonText() {
		setText(isSelected() ? "Остановить" : "Взвешивать");
	}

	private void toggleWeighing(boolean start) {
		if (!start) {
			if (this.weightHandle != null) {
				this.weightHandle.unsubscribe();
				this.weightHandle = null;
			}
			return;
		}

		this.weightHandle = this.portSelector.getSelected()
		.getHandle()
		.requestWeight(new MeraConsumer<WeightHandle, WeightMessage>() {
			@Override
			public void consumerSubscribed(WeightHandle handle) {
			}
			@Override
			public void messageReceived(WeightMessage message) {
				setWeight(message);
			}
			@Override
			public void consumerUnubscribed(WeightHandle handle) {
			}

		});
	}

	private void setWeight(WeightMessage weight) {
		this.app.log("Вес: " + weight.getWeight() + " г.");
	}

}
