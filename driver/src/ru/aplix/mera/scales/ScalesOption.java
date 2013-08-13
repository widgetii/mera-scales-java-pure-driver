package ru.aplix.mera.scales;

import java.util.Objects;


/**
 * Scales configuration option.
 *
 * <p>{@link ScalesConfig Scales configuration} consists of such options.</p>
 *
 * <p>Options could be either global, or specific to the given
 * {@link ScalesProtocol protocol}.</p>
 *
 * <p>There should be exactly one instance of each option. Create it as static
 * field.</p>
 *
 * @param <T> option value type.
 */
public abstract class ScalesOption<T> {

	private final String optionId;
	private final Class<? extends T> valueType;
	private final ScalesProtocol protocol;

	/**
	 * Constructs global option.
	 *
	 * @param optionId option identifier, can not be <code>null</code>.
	 * @param valueType option value type's class.
	 */
	protected ScalesOption(String optionId, Class<? extends T> valueType) {
		Objects.requireNonNull(optionId, "Option identifier not specified");
		Objects.requireNonNull(valueType, "Option value type not specified");
		this.valueType = valueType;
		this.optionId = optionId;
		this.protocol = null;
	}

	/**
	 * Constructs an option specific to the given protocol.
	 *
	 * @param optionId option identifier, can not be <code>null</code>. The
	 * actual identifier will be prefixed by
	 * {@link ScalesProtocol#getProtocolId() protocol identifier}.
	 * @param valueType option value type's class.
	 * @param protocol scales protocol this option is specific to, or
	 * <code>null</code> if this option is global.
	 */
	protected ScalesOption(
			String optionId,
			Class<? extends T> valueType,
			ScalesProtocol protocol) {
		Objects.requireNonNull(optionId, "Option identifier not specified");
		Objects.requireNonNull(valueType, "Option value type not specified");
		this.valueType = valueType;
		if (protocol != null) {
			this.optionId = protocol.getProtocolId() + "." + optionId;
			this.protocol = protocol;
		} else {
			this.optionId = optionId;
			this.protocol = null;
		}
	}

	/**
	 * Option identifier.
	 *
	 * @return option identifier passed to the constructor.
	 */
	public final String getOptionId() {
		return this.optionId;
	}

	/**
	 * Option value type.
	 *
	 * @return option value type's class passed to constructor.
	 */
	public final Class<? extends T> getValueType() {
		return this.valueType;
	}

	/**
	 * Whether this option is global.
	 *
	 * @return <code>true</code> if this option is global, or <code>false</code>
	 * otherwise.
	 */
	public final boolean isGlobal() {
		return getProtocol() == null;
	}

	/**
	 * Scales protocol this option is specific to.
	 *
	 * @return scales protocol passed, or <code>null</code> if this option is
	 * global.
	 */
	public final ScalesProtocol getProtocol() {
		return this.protocol;
	}

	/**
	 * Returns the default value for this option.
	 *
	 * <p>This can be overridden by scales driver.</p>
	 *
	 * @return default option value.
	 */
	public abstract T getDefaultValue();

	/**
	 * Corrects the option value.
	 *
	 * <p>This method is called before the new option value is set. It can be
	 * used to correct the value provided by client.</p>
	 *
	 * @param value new option value provided by client.
	 *
	 * @return corrected value to apply to configuration, or <code>null</code>
	 * to apply the default one.
	 */
	public abstract T correctValue(T value);

}
