package ru.aplix.mera.scales.config;

import ru.aplix.mera.scales.ScalesProtocol;


/**
 * Scales option with non-negative value.
 *
 * <p>If negative value set, then the default value used instead.</p>
 */
public class NonNegativeScalesOption extends IntegerScalesOption {

	/**
	 * Constructs global natural scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 */
	public NonNegativeScalesOption(String optionId, long defaultValue) {
		super(optionId, defaultValue);
	}

	/**
	 * Constructs protocol-specific non-negative scales option.
	 *
	 * @param optionId option identifier.
	 * @param defaultValue default value.
	 * @param protocol scales protocol this option is specific to, or
	 * <code>null</code> to make it global.
	 */
	public NonNegativeScalesOption(
			String optionId,
			long defaultValue,
			ScalesProtocol protocol) {
		super(optionId, defaultValue, protocol);
	}

	@Override
	public Long correctValue(Long value) {
		return value.longValue() < 0 ? null : value;
	}

}
