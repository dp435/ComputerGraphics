package ray2.shader;

import ray2.RayTracer;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glazed extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }

	/**
	 * The underlying material beneath the glaze.
	 */
	protected Shader substrate;
	public void setSubstrate(Shader substrate) {
		this.substrate = substrate; 
	}
	
	public Glazed() { 
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7: fill in this function.
		// You may find it helpful to create helper methods if the code here gets too long.
		//  1) Compute the Fresnel term R
        //  2) Shade the substrate and multiply the result color by 1 - R
        //  3) Compute the reflected ray and call RayTracer.shadeRay on it, multiply result color by R
		double n1 = 0, n2 = 0;

		Vector3d d = new Vector3d(ray.origin.clone().sub(record.location)).normalize();
		Vector3d n = new Vector3d(record.normal).normalize();
		double theta = n.dot(d);
		double fresnel = fresnel(n, d, refractiveIndex);

		Vector3d reflectionDir = new Vector3d(n).mul(2 * theta).sub(d.clone()).normalize();
		Ray reflectionRay = new Ray(record.location.clone(), reflectionDir);
		reflectionRay.makeOffsetRay();
		Colord reflectionColor = new Colord();
		RayTracer.shadeRay(reflectionColor, scene, reflectionRay, depth+1);
		reflectionColor.mul(fresnel);
		
		Colord substrateColor = new Colord();
		substrate.shade(substrateColor, scene, ray, record, depth+1);
		substrateColor.mul(1-fresnel);
		
		outIntensity.add(reflectionColor).add(substrateColor);
	}
}