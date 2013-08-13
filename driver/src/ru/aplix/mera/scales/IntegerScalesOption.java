package ru.aplix.mera.scales;


/**
 * Scales option with integer value.
 */
public class IntegerScalesOption extends ScalesOption<Long> {

	private Long defaultValue;

	/**
	 * Constructs a global integer scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 */
	public IntegerScalesOption(String optionId, long defaultValue) {
		super(optionId, Long.class);
		this.defaultValue = Long.valueOf(defaultValue);
	}

	/**
	 * Constructs protocol-specific integer scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 * @param protocol scales protocol this option is specific to, or
	 * <code>null</code> to make it global.
	 */
	public IntegerScalesOption(
			String optionId,
			long defaultValue,
			ScalesProtocol protocol) {
		super(optionId, Long.class, protocol);
		this.defaultValue = Long.valueOf(defaultValue);
	}

	@Override
	public Long getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public Long correctValue(Long value) {
		return value;
	}

}
