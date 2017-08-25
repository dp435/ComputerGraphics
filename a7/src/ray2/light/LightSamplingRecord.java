package ray2.light;

import egl.math.Vector3d;

/**
 * This class is used to record the results of light source sampling, or choosing a point
 * on a light source for illumination calculation.  Since some light sources (e.g. directional
 * or environment lights) are not associated with points in the scene, the result of querying
 * a light source position is a direction and a distance, and the distance may be infinite.
 * 
 * @author srm
 */
public class LightSamplingRecord {
	
	/** The direction from the shading point to the light source point. */
	public final Vector3d direction = new Vector3d();
	
	/** The attenuation (e.g. 1/r^2) appropriate for this light source point. */
	public double attenuation;
	
	/** The distance from the shading point to the light source point. */
	public double distance;
	
	/** The probability, or probability density, with which the sample point was chosen. */
	public double probability;

}
