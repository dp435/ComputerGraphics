package ray2.shader;

import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Lambertian material scatters light equally in all directions. BRDF value is
 * a constant
 *
 * @author ags
 */
public class Lambertian extends BRDFShader {

	public Lambertian() { }
	
	public String toString() {
		return "lambertian: " + diffuseColor;
	}

	@Override
	protected void evalBRDF(Vector3d L, Vector3d V, Vector3d N, Colord kD,
			Colord outColor) {
		outColor.set(diffuseColor);
	}

}