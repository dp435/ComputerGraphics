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
public class Glass extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's
	 * Law.
	 */
	protected double refractiveIndex;

	public void setRefractiveIndex(double refractiveIndex) {
		this.refractiveIndex = refractiveIndex;
	}

	public Glass() {
		refractiveIndex = 1.0;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading
	 * model.
	 *
	 * @param outIntensity
	 *            The color returned towards the source of the incoming ray.
	 * @param scene
	 *            The scene in which the surface exists.
	 * @param ray
	 *            The ray which intersected the surface.
	 * @param record
	 *            The intersection record of where the ray intersected the
	 *            surface.
	 * @param depth
	 *            The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7: fill in this function.
		// 1) Determine whether the ray is coming from the inside of the surface
		// or the outside.
		// 2) Determine whether total internal reflection occurs.
		// 3) Compute the reflected ray and refracted ray (if total internal
		// reflection does not occur)
		// using Snell's law and call RayTracer.shadeRay on them to shade them

		// n1 is from; n2 is to.
		double n1 = 0, n2 = 0;
		double fresnel = 0;
		Vector3d d = new Vector3d(ray.origin.clone().sub(record.location)).normalize();
		Vector3d n = new Vector3d(record.normal).normalize();
		double theta = n.dot(d);

		// CASE 1a: ray coming from outside.
		if (theta > 0) {
			n1 = 1.0;
			n2 = refractiveIndex;
			fresnel = fresnel(n, d, refractiveIndex);
		}

		// CASE 1b: ray coming from inside.
		else if (theta < 0) {
			n1 = refractiveIndex;
			n2 = 1.0;
			n.mul(-1.0);
			fresnel = fresnel(n, d, 1/refractiveIndex);
			theta = n.dot(d);
		}

		Vector3d reflectionDir = new Vector3d(n).mul(2 * theta).sub(d.clone()).normalize();
		Ray reflectionRay = new Ray(record.location.clone(), reflectionDir);
		reflectionRay.makeOffsetRay();
		Colord reflectionColor = new Colord();
		RayTracer.shadeRay(reflectionColor, scene, reflectionRay, depth+1);

		double det = 1 - (Math.pow(n1, 2.0) * (1 - Math.pow(theta, 2.0))) / (Math.pow(n2, 2.0));

		if (det < 0.0) {
			outIntensity.add(reflectionColor);
		} else {

			d = new Vector3d(record.location.clone().sub(ray.origin)).normalize();

			Vector3d refractionDir = new Vector3d((d.clone().sub(n.clone().mul(d.dot(n))).mul(n1/n2)));
			refractionDir.sub(n.clone().mul(Math.sqrt(det)));
			Ray refractionRay = new Ray(record.location.clone(), refractionDir);
			refractionRay.makeOffsetRay();

			Colord refractionColor = new Colord();
			RayTracer.shadeRay(refractionColor, scene, refractionRay, depth+1);

			refractionColor.mul(1-fresnel);
			reflectionColor.mul(fresnel);
			outIntensity.add(reflectionColor).add(refractionColor);

		}
	}

}