package ray1.shader;

import ray1.IntersectionRecord;
import ray1.Light;
import ray1.Ray;
import ray1.Scene;
import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector2;
import egl.math.Vector2d;
import egl.math.Vector3;

/**
 * A Lambertian material scatters light equally in all directions. BRDF value is
 * a constant
 *
 * @author ags, zz
 */
public class Lambertian extends Shader {

	/** The color of the surface. */
	protected final Colorf diffuseColor = new Colorf(Color.White);

	public void setDiffuseColor(Colorf inDiffuseColor) {
		diffuseColor.set(inDiffuseColor);
	}

	public Colorf getDiffuseColor() {
		return new Colorf(diffuseColor);
	}

	public Lambertian() {
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "lambertian: " + diffuseColor;
	}

	/**
	 * Evaluate the intensity for a given intersection using the Lambert shading
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
	public void shade(Colorf outIntensity, Scene scene, Ray ray, IntersectionRecord record) {
		// TODO#A2: Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for
		// the light.
		// See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		// the intersection point from the light's position.
		// 4) Compute the color of the point using the Lambert shading model.
		// Add this value
		// to the output.
		outIntensity.setZero();
		for (Light light : scene.getLights()) {
			if (!isShadowed(scene, light, record, new Ray(ray))) {
				Vector3 l = new Vector3();
				l.set(light.position.clone().sub(new Vector3(record.location.clone()))).normalize();
				double contribution = Math.max(0.0, record.normal.clone().dot(l));
				float r_sqr = light.position.clone().distSq(new Vector3(record.location));

				Colorf dcolor;
				if (texture == null)
					dcolor = diffuseColor;
				else {
					Vector2d texCoord = record.texCoords.clone();
					dcolor = getTexture().getTexColor(new Vector2(texCoord));
				}

				outIntensity.add((float) (dcolor.r() * light.intensity.r() * contribution / r_sqr),
						(float) (dcolor.g() * light.intensity.g() * contribution / r_sqr),
						(float) (dcolor.b() * light.intensity.b() * contribution / r_sqr));
			}
		}
	}

}
