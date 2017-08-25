package ray2.light;


import egl.math.Vector3d;

/**
 * This class represents a basic point light which is infinitely small and emits
 * a constant power in all directions. This is a useful idealization of a small
 * light emitter.
 *
 * @author ags
 */
public class DirectionalLight extends Light {
	
	/** Direction of the light. */
	public final Vector3d direction = new Vector3d();
	public void setDirection(Vector3d direction) { this.direction.set(direction); }
		
	// initialization method
	public void init() {
		// do nothing
	}

	/**
	 * Sample the illumination due to this light source at a given shading point.
	 * A directional light provides illumination from a single direction; the outputs are:
	 *
	 *    lRec.direction is the (fixed) direction to the source
	 *    lRec.distance is infinity
	 *    lRec.attenuation is 1.0 because there is no falloff
	 *    lRec.probability is 1.0 because the same direction is always chosen
	 *
	 * @param record the record where the output is written:
	 * @param shadingPoint the surface point where illumination is being computed
	 */
	@Override
	public void sample(LightSamplingRecord lRec, Vector3d shadingPoint) {
		lRec.direction.set(direction);
		lRec.attenuation = 1.0;
		lRec.distance = Double.POSITIVE_INFINITY;
		lRec.probability = 1.0;
	}

	/**
	 * Default constructor.  Produces a unit intensity light at the origin.
	 */
	public DirectionalLight() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "Directionlight: " + direction + " " + intensity;
	}


}