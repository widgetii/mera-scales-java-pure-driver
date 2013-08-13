package ru.aplix.mera.tester.config;

import static ru.aplix.mera.scales.byte9.Byte9Protocol.*;

import javax.swing.JComboBox;

import ru.aplix.mera.scales.ScalesConfig;
import ru.aplix.mera.tester.TesterApp;


public class Byte9Config extends AbstractConfig {

	private static final long serialVersionUID = -2111766803811004796L;

	private static final String[] CONNECTION_TIMEOUTS = {"", "2000", "5000"};
	private static final String[] COMMAND_RETRIES = {"", "0", "1", "2"};
	private static final String[] RESPONSE_TIMEOUTS =
		{"", "250", "500", "1000"};
	private static final String[] DATA_DELAYS =
		{"", "0", "50", "100", "200"};

	private final JComboBox<String> connectionTimeout;
	private final JComboBox<String> commandRetries;
	private final JComboBox<String> responseTimeout;
	private final JComboBox<String> dataDelay;

	public Byte9Config(TesterApp app) {
		super(app);
		this.connectionTimeout = new JComboBox<>(CONNECTION_TIMEOUTS);
		this.commandRetries = new JComboBox<>(COMMAND_RETRIES);
		this.responseTimeout = new JComboBox<>(RESPONSE_TIMEOUTS);
		this.dataDelay = new JComboBox<>(DATA_DELAYS);

		this.connectionTimeout.setEditable(true);
		this.connectionTimeout.setSelectedItem(
				optionValue(BYTE9_CONNECTION_TIMEOUT));
		addOption(
				"Время ожидания подключения, мс",
				this.connectionTimeout);

		this.commandRetries.setEditable(true);
		this.commandRetries.setSelectedItem(
				optionValue(BYTE9_COMMAND_RETRIES));
		addOption(
				"Повторных посылок команды",
				this.commandRetries);

		this.responseTimeout.setEditable(true);
		this.responseTimeout.setSelectedItem(
				optionValue(BYTE9_RESPONSE_TIMEOUT));
		addOption(
				"Время ожидания ответа, мс",
				this.responseTimeout);

		this.dataDelay.setEditable(true);
		this.dataDelay.setSelectedItem(
				optionValue(BYTE9_DATA_DELAY));
		addOption(
				"Задержка отправки данных, мс",
				this.dataDelay);
	}

	@Override
	protected ScalesConfig updateConfig(ScalesConfig config) {

		ScalesConfig cfg = config;

		cfg = cfg.set(
				BYTE9_CONNECTION_TIMEOUT,
				intOption(this.connectionTimeout));
		cfg = cfg.set(
				BYTE9_COMMAND_RETRIES,
				intOption(this.commandRetries));
		cfg = cfg.set(
				BYTE9_RESPONSE_TIMEOUT,
				intOption(this.responseTimeout));
		cfg = cfg.set(
				BYTE9_DATA_DELAY,
				intOption(this.dataDelay));

		return cfg;
	}

}
