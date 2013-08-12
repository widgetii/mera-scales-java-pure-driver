package ru.aplix.mera.scales.backend;

import static ru.aplix.mera.scales.backend.ScalesBackendConfig.DEFAULT_SCALES_BACKEND_CONFIG;


/**
 * Scales backend context.
 *
 * <p>Scales driver can use it to configure backend during
 * {@link ScalesDriver#initScalesDriver(ScalesDriverContext) initialization}.
 * It is illegal to update scales backend configuration after that.</p>
 */
public class ScalesDriverContext {

	private final ScalesBackend backend;
	private ScalesBackendConfig config = DEFAULT_SCALES_BACKEND_CONFIG;
	private Weighing weighing;
	private volatile boolean initialized;

	ScalesDriverContext(ScalesBackend backend) {
		this.backend = backend;
	}

	/**
	 * Scales backend configuration.
	 *
	 * @return the configuration set with
	 * {@link #setConfig(ScalesBackendConfig)} method, or the
	 * {@link ScalesBackendConfig#DEFAULT_SCALES_BACKEND_CONFIG default one}
	 * if it wasn't set yet.
	 */
	public final ScalesBackendConfig getConfig() {
		return this.config;
	}

	/**
	 * Updates the scales backend configuration.
	 *
	 * @param config new backend configuration, or <code>null</code> to set it
	 * to the {@link ScalesBackendConfig#DEFAULT_SCALES_BACKEND_CONFIG default}
	 * one.
	 */
	public final void setConfig(ScalesBackendConfig config) {
		ensureNotInitialized();
		this.config = config != null ? config : DEFAULT_SCALES_BACKEND_CONFIG;
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
