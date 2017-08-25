package ray2.shader;

import ray2.IntersectionRecord;
import ray2.light.LightSamplingRecord;
import ray2.Ray;
import ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * This interface specifies what is necessary for an object to be a shader.
 * @author ags, pramook
 */
public abstract class Shader {
	
	/**
	 * The material given to all surfaces unless another is specified.
	 */
	public static final Shader DEFAULT_SHADER = new Lambertian();
	
	
	protected Texture texture = null;
	public void setTexture(Texture t) { texture = t; }
	public Texture getTexture() { return texture; }
	
	/**	
	 * Calculate the intensity (color) for this material at the intersection described in
	 * the record contained in workspace.
	 * 	 
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	public abstract void shade(Colord outIntensity, Scene scene, Ray ray, 
			IntersectionRecord record, int depth);
	
	/**
	* Initialization method
	*/
	public void init() {
		// do nothing
	}

	/**
	 * A utility method to check if there is any surface between the given intersection
	 * point and the given light. shadowRay is set to point from the intersection point
	 * towards the light.
	 * 
	 * @param scene The scene in which the surface exists.
	 * @param light A light in the scene.
	 * @param iRec The intersection point on a surface.
	 * @param shadowRay A ray that is set to point from the intersection point towards
	 * the given light.
	 * @return true if there is any surface between the intersection point and the light;
	 * false otherwise.
	 */
	protected boolean isShadowed(Scene scene, LightSamplingRecord lRec, IntersectionRecord iRec, Ray shadowRay) {		
		// Setup the shadow ray to start at surface and end at light
		shadowRay.origin.set(iRec.location);
		shadowRay.direction.set(lRec.direction);
		
		// Set the ray to end at the light
		shadowRay.direction.normalize();
		shadowRay.makeOffsetSegment(lRec.distance);
		
		return scene.getAnyIntersection(shadowRay);
	}
	
	protected double fresnel(Vector3d normal, Vector3d outgoing, double refractiveIndex) {
		// compute the fresnel term using the equation in the lecture
		double cos_1 = outgoing.dot(normal);
		if (cos_1 < 0)
			return 0;
		
		double cos_2_sq = 1-(1-cos_1*cos_1) / (refractiveIndex*refractiveIndex);
		// Total reflection
		if (cos_2_sq < 0)
			return 1;
		
		double cos_2 = Math.sqrt(cos_2_sq);
		double Fp = (refractiveIndex*cos_1 - cos_2) / (refractiveIndex*cos_1 + cos_2);
		double Fs = (cos_1 - refractiveIndex * cos_2) / (cos_1 + refractiveIndex * cos_2);
		double R = 0.5 * (Fp*Fp + Fs*Fs);
		return R;
	}
}