package ru.aplix.mera.scales.backend;


/**
 * Scales driver responsible for performing operations.
 */
public interface ScalesDriver {

	/**
	 * Scales backend config suitable to this driver.
	 *
	 * @return backend config.
	 */
	ScalesBackendConfig backendConfig();

	/**
	 * Obtains the scales status.
	 *
	 * @param request scales request.
	 *
	 * @return scales status update, or <code>null</code> if it can't be
	 * obtained.
	 */
	ScalesStatusUpdate requestStatus(ScalesRequest request);

	/**
	 * Measures the weight.
	 *
	 * @param request scales request.
	 *
	 * @return weight update, or <code>null</code> if the weight can't be
	 * measured.
	 */
	WeightUpdate requestWeight(ScalesRequest request);

}
