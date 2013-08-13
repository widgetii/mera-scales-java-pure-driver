package ru.aplix.mera.scales;


/**
 * Scales option with natural value.
 *
 * <p>If non-positive value set, then the default value used instead.</p>
 */
public class NaturalScalesOption extends IntegerScalesOption {

	/**
	 * Constructs global natural scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 */
	public NaturalScalesOption(String optionId, long defaultValue) {
		super(optionId, defaultValue);
	}

	/**
	 * Constructs protocol-specific natural scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 * @param protocol scales protocol this option is specific to, or
	 * <code>null</code> to make it global.
	 */
	public NaturalScalesOption(
			String optionId,
			long defaultValue,
			ScalesProtocol protocol) {
		super(optionId, defaultValue, protocol);
	}

	@Override
	public Long correctValue(Long value) {
		return value.longValue() <= 0 ? null : value;
	}

}
