package ru.aplix.mera.tester.config;

import static ru.aplix.mera.scales.config.ScalesConfig.MAX_RECONNECT_DELAY;
import static ru.aplix.mera.scales.config.ScalesConfig.MIN_RECONNECT_DELAY;
import static ru.aplix.mera.scales.config.ScalesConfig.WEIGHING_PERIOD;

import javax.swing.JComboBox;

import ru.aplix.mera.scales.config.ScalesConfig;
import ru.aplix.mera.tester.TesterApp;


public class GlobalConfig extends AbstractConfig {

	private static final String[] MIN_RECONNECT_DELAYS =
			new String[] {"", "500", "1000"};
	private static final String[] MAX_RECONNECT_DELAYS =
			new String[] {"", "5000", "10000"};
	private static final String[] WEIGHING_PERIODS =
			new String[] {"", "1000", "2000"};

	private static final long serialVersionUID = -2064931821520992548L;

	private final JComboBox<String> minReconnectDelay;
	private final JComboBox<String> maxReconnectDelay;
	private final JComboBox<String> weighingPeriod;


	public GlobalConfig(TesterApp app) {
		super(app);
		this.minReconnectDelay = new JComboBox<>(MIN_RECONNECT_DELAYS);
		this.maxReconnectDelay = new JComboBox<>(MAX_RECONNECT_DELAYS);
		this.weighingPeriod = new JComboBox<>(WEIGHING_PERIODS);

		this.minReconnectDelay.setEditable(true);
		this.minReconnectDelay.setSelectedItem(
				optionValue(MIN_RECONNECT_DELAY));
		addOption(
				"Наименьшая задержка переподключения, мс",
				this.minReconnectDelay);

		this.maxReconnectDelay.setEditable(true);
		this.maxReconnectDelay.setSelectedItem(
				optionValue(MAX_RECONNECT_DELAY));
		addOption(
				"Наибольшая задержка переподключения, мс",
				this.maxReconnectDelay);

		this.weighingPeriod.setEditable(true);
		this.weighingPeriod.setSelectedItem(optionValue(WEIGHING_PERIOD));
		addOption("Период взвешивания, мс", this.weighingPeriod);
	}

	@Override
	protected ScalesConfig updateConfig(ScalesConfig config) {

		ScalesConfig cfg = config;

		cfg = cfg.set(MIN_RECONNECT_DELAY, intOption(this.minReconnectDelay));
		cfg = cfg.set(MAX_RECONNECT_DELAY, intOption(this.maxReconnectDelay));
		cfg = cfg.set(WEIGHING_PERIOD, intOption(this.weighingPeriod));

		return cfg;
	}

}
