package ru.aplix.mera.scales.backend;

import static ru.aplix.mera.scales.config.ScalesConfig.DEFAULT_SCALES_CONFIG;
import ru.aplix.mera.scales.config.ScalesConfig;


/**
 * Scales backend context.
 *
 * <p>Scales driver can use it to configure backend during
 * {@link ScalesDriver#initScalesDriver(ScalesDriverContext) initialization}.
 * It is illegal to update scales backend configuration after that.</p>
 */
public class ScalesDriverContext {

	private final ScalesBackend backend;
	private ScalesConfig config = DEFAULT_SCALES_CONFIG;
	private Weighing weighing;
	private volatile boolean initialized;

	ScalesDriverContext(ScalesBackend backend) {
		this.backend = backend;
	}

	/**
	 * Scales configuration.
	 *
	 * @return the configuration set with
	 * {@link #setConfig(ScalesConfig)} method, or the
	 * {@link ScalesConfig#DEFAULT_SCALES_CONFIG default one} if it didn't set
	 * yet.
	 */
	public final ScalesConfig getConfig() {
		return this.config;
	}

	/**
	 * Updates the scales configuration.
	 *
	 * @param config new configuration, or <code>null</code> to set it
	 * to the {@link ScalesConfig#DEFAULT_SCALES_CONFIG default}
	 * one.
	 */
	public final void setConfig(ScalesConfig config) {
		if (!this.initialized) {
			this.config = config != null ? config : DEFAULT_SCALES_CONFIG;
		} else {
			this.backend.setConfig(config);
		}
	}

	/**
	 * Informs the backend that the weight updates shall be reported
	 * automatically.
	 *
	 * @param weighing the weighting process responsible for weight updates
	 * reporting, or <code>null</code> to cause the backend to request the
	 * weight periodically.
	 *
	 * @see Weighing
	 */
	public final void updateWeightWith(Weighing weighing) {
		ensureNotInitialized();
		this.weighing = weighing;
	}

	final void initDriver(ScalesDriver driver) {
		driver.initScalesDriver(this);
		this.initialized = true;
		initWeightUpdater();
	}

	final Weighing getWeighting() {
		return this.weighing;
	}

	private void initWeightUpdater() {
		if (this.weighing == null) {
			this.weighing = new PeriodicalWeighing(this.backend);
		}
		this.weighing.initWeighting(new WeightReceiver(this.backend));
	}

	private void ensureNotInitialized() {
		if (this.initialized) {
			throw new IllegalStateException(
					"Scales backend already initialized. "
					+ "Can not update its config");
		}
	}

}
