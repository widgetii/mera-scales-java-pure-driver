package ru.aplix.mera.scales;


/**
 * Scales option with string value.
 */
public class StringScalesOption extends ScalesOption<String> {

	private String defaultValue;

	/**
	 * Constructs a global string scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 */
	public StringScalesOption(String optionId, String defaultValue) {
		super(optionId, String.class);
		this.defaultValue = defaultValue;
	}

	/**
	 * Constructs protocol-specific string scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 * @param protocol scales protocol this option is specific to, or
	 * <code>null</code> to make it global.
	 */
	public StringScalesOption(
			String optionId,
			String defaultValue,
			ScalesProtocol protocol) {
		super(optionId, String.class, protocol);
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public String correctValue(String value) {
		return null;
	}

}
