package ru.aplix.mera.scales.backend;


/**
 * Scales driver responsible for performing operations.
 */
public interface ScalesDriver {

	/**
	 * Initializes this driver.
	 *
	 * <p>This method is called prior to any attempt to use it.</p>
	 *
	 * @param context driver context the driver can use to configure backend.
	 */
	void initScalesDriver(ScalesDriverContext context);

	/**
	 * Obtains the scales status.
	 *
	 * @param request scales request.
	 *
	 * @return scales status update, or <code>null</code> if it can't be
	 * obtained.
	 *
	 * @throws Exception if operation failed.
	 */
	ScalesStatusUpdate requestStatus(ScalesRequest request) throws Exception;

	/**
	 * Measures the weight.
	 *
	 * <p>Note that this method won't be called if a weight updater
	 * {@link ScalesDriverContext#updateWeightWith(WeightUpdater) provided}.
	 * </p>
	 *
	 * @param request scales request.
	 *
	 * @return weight update, or <code>null</code> if the weight can't be
	 * measured.
	 *
	 * @throws Exception if operation failed.
	 */
	WeightUpdate requestWeight(ScalesRequest request) throws Exception;

}
