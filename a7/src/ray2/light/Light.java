package ray2.light;

import egl.math.Colord;
import egl.math.Vector3d;

/**
 * This class represents a basic point light which is infinitely small and emits
 * a constant power in all directions. This is a useful idealization of a small
 * light emitter.
 *
 * @author ags, zechenz
 */
public abstract class Light {
	
	/** How bright the light is. */
	public final Colord intensity = new Colord(1.0, 1.0, 1.0);
	public void setIntensity(Colord intensity) { this.intensity.set(intensity); }

	// initialization method
	public abstract void init();

	/**
	 * Sample the illumination due to this light source at a given shading point.
	 * @param record the record where the output is written
	 * @param shadingPoint the surface point where illumination is being computed
	 */
	public abstract void sample(LightSamplingRecord record, Vector3d shadingPoint);
		
	/**
	 * Default constructor.  Produces a unit intensity light at the origin.
	 */
	public Light() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "light: " + intensity;
	}
}