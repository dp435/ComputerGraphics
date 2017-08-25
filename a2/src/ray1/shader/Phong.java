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
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Phong extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colorf diffuseColor = new Colorf(Color.White);

	public void setDiffuseColor(Colorf diffuseColor) {
		this.diffuseColor.set(diffuseColor);
	}

	public Colorf getDiffuseColor() {
		return new Colorf(diffuseColor);
	}

	/** The color of the specular reflection. */
	protected final Colorf specularColor = new Colorf(Color.White);

	public void setSpecularColor(Colorf specularColor) {
		this.specularColor.set(specularColor);
	}

	public Colorf getSpecularColor() {
		return new Colorf(specularColor);
	}

	/** The exponent controlling the sharpness of the specular reflection. */
	protected float exponent = 1.0f;

	public void setExponent(float exponent) {
		this.exponent = exponent;
	}

	public float getExponent() {
		return exponent;
	}

	public Phong() {
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Phong shading
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
		// 4) Compute the color of the point using the Phong shading model. Add
		// this value
		// to the output.
		outIntensity.setZero();
		for (Light light : scene.getLights()) {
			if (!isShadowed(scene, light, record, new Ray(ray))) {
				Vector3d l = new Vector3d();
				l.set(new Vector3d(light.position.clone()).sub(record.location.clone())).normalize();

				Vector3d v = new Vector3d();
				v.set(new Vector3d(ray.origin.clone()).sub(record.location.clone())).normalize();

				Vector3d h = new Vector3d();
				h.set(v.clone().add(l)).normalize();

				double diffuseCoefficient = Math.max(0.0, record.normal.clone().dot(l));
				if (diffuseCoefficient <= 0)
					continue;

				double specularCoefficient = Math.pow(Math.max(0.0, record.normal.clone().dot(h)), exponent);
				double r_sqr = new Vector3d(light.position.clone()).distSq(record.location);

				Colorf dcolor;
				if (texture == null)
					dcolor = diffuseColor;
				else {
					Vector2d texCoord = record.texCoords.clone();
					dcolor = getTexture().getTexColor(new Vector2(texCoord));
				}

				outIntensity.add(
						(float) ((light.intensity.r() / r_sqr)
								* (dcolor.r() * diffuseCoefficient + specularColor.r() * specularCoefficient)),
						(float) ((light.intensity.g() / r_sqr)
								* (dcolor.g() * diffuseCoefficient + specularColor.g() * specularCoefficient)),
						(float) ((light.intensity.b() / r_sqr)
								* (dcolor.b() * diffuseCoefficient + specularColor.b() * specularCoefficient)));
			}
		}
	}

}
