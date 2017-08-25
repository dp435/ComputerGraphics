package ray1.camera;

import egl.math.Vector3;
import ray1.Ray;

public class OrthographicCamera extends Camera {

	protected Vector3 U = new Vector3();
	protected Vector3 V = new Vector3();
	protected Vector3 W = new Vector3();
	private boolean isInitialized = false;

	/**
	 * Initialize the derived view variables to prepare for using the camera.
	 */
	public void init() {
		W.set(viewDir.clone().negate()).normalize();
		U.set(viewDir.clone().cross(viewUp)).normalize();
		V.set(W.clone().cross(U));
		isInitialized = true;
	}

	/**
	 * Set outRay to be a ray from the camera through a point in the image.
	 *
	 * @param outRay
	 *            The output ray (not normalized)
	 * @param inU
	 *            The u coord of the image point (range [0,1])
	 * @param inV
	 *            The v coord of the image point (range [0,1])
	 */
	public void getRay(Ray outRay, float inU, float inV) {
		// Initialize variables, if not already initialized.
		if (!isInitialized)
			init();
		// Calculate and set origin.
		float remappedU = remapU(inU);
		float remappedV = remapV(inV);
		Vector3 calculatedOrigin = new Vector3();
		calculatedOrigin.set(viewPoint.clone()).add(U.clone().mul(remappedU)).add(V.clone().mul(remappedV));
		outRay.origin.set(calculatedOrigin);
		// Set direction.
		outRay.direction.set(viewDir);
		// Set tMax to infinity.
		outRay.makeOffsetRay();
	}

	/**
	 * Helper function to remap inU from [0,1] to [-viewWidth/2, +viewWidth/2]
	 * 
	 * @param inU
	 *            u coord of the image point to be remapped.
	 */
	private float remapU(float inU) {
		final float lowerBound = 0f;
		final float upperBound = 1f;
		final float scaledLowerBound = -viewWidth / 2f;
		final float scaledUpperBound = viewWidth / 2f;
		return (inU - lowerBound) * (scaledUpperBound - scaledLowerBound) / (upperBound - lowerBound)
				+ scaledLowerBound;
	}

	/**
	 * Helper function to remap inV from [0,1] to [-viewHeight/2, +viewHeight/2]
	 * 
	 * @param inV
	 *            v coord of the image point to be remapped.
	 */
	private float remapV(float inV) {
		final float lowerBound = 0f;
		final float upperBound = 1f;
		final float scaledLowerBound = -viewHeight / 2f;
		final float scaledUpperBound = viewHeight / 2f;
		return (inV - lowerBound) * (scaledUpperBound - scaledLowerBound) / (upperBound - lowerBound)
				+ scaledLowerBound;
	}

}
