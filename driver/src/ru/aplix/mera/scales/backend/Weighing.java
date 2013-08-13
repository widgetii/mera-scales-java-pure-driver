package ru.aplix.mera.scales.backend;

import ru.aplix.mera.scales.ScalesConfig;


/**
 * The weighing process interface.
 *
 * <p>A {@link ScalesDriver scales driver} normally returns the weight
 * when {@link ScalesDriver#requestWeight(ScalesRequest) requested}. The backend
 * {@link ScalesConfig#getWeighingPeriod() periodically} requests the
 * driver.</p>
 *
 * <p>Alternatively, the driver can automatically report the measured weight.
 * To do so, the weighing process instance should be
 * {@link ScalesDriverContext#updateWeightWith(Weighing) provided} during
 * the scales driver {@link ScalesDriver#initScalesDriver(ScalesDriverContext)
 * initialization}.<p>
 */
public interface Weighing {

	/**
	 * Initializes the weighting.
	 *
	 * @param weightReceiver weight receiver to report the weight updates
	 * through.
	 */
	void initWeighting(WeightReceiver weightReceiver);

	/**
	 * Starts the weighing.
	 *
	 * <p>This method is called when the first consumer subscribed to the weight
	 * updates.</p>
	 */
	void startWeighing();

	/**
	 * Suspends weighing.
	 *
	 * <p>This method is called when the
	 * {@link ScalesStatusUpdate#getScalesStatus() scales status} is error, and
	 * the scales can no longer operate normally.</p>
	 */
	void suspendWeighing();

	/**
	 * Resumes weighing.
	 *
	 * <p>This method is called when the
	 * {@link ScalesStatusUpdate#getScalesStatus() scales status} becomes
	 * normal again, and the scales can resume the operations.</p>
	 */
	void resumeWeighing();

	/**
	 * Stops weighing.
	 *
	 * <p>This method is called when the last subscription to the weight
	 * updates is revoked.</p>
	 */
	void stopWeighing();

}
