package ru.aplix.mera.scales.backend;


/**
 * The weight updater interface.
 *
 * <p>A {@link ScalesDriver scales driver} normally returns the weight
 * when {@link ScalesDriver#requestWeight(ScalesRequest) requested}. The backend
 * {@link ScalesBackendConfig#getWeighingPeriod() periodically} requests the
 * deriver.</p>
 *
 * <p>Alternatively, the driver can automatically report the measured weight.
 * To do so, the weight updater instance should be
 * {@link ScalesDriverContext#updateWeightWith(WeightUpdater) provided} during
 * the scales driver {@link ScalesDriver#initScalesDriver(ScalesDriverContext)
 * initialization}.<p>
 */
public interface WeightUpdater {

	/**
	 * Initializes the weight updater.
	 *
	 * @param request weight request to report the weight updates through.
	 */
	void initWeightUpdater(WeightRequest request);

}
