package ray2.shader;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook, srm
 */
public class Phong extends BRDFShader {

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The exponent controlling the sharpness of the specular reflection. */
	protected double exponent = 1.0;
	public void setExponent(double exponent) { this.exponent = exponent; }

	public Phong() { }

	public String toString() {    
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}

	@Override
	protected void evalBRDF(Vector3d L, Vector3d V, 
			Vector3d N, Colord kD, Colord outColor) {
		double NdotL = N.dot(L);
		Vector3d halfVec = new Vector3d();
		halfVec.set(L).add(V).normalize();
		
		double halfDotNormal = Math.max(0.0, halfVec.dot(N));
		double factor = Math.pow(halfDotNormal, exponent) / NdotL;
		
		outColor.set(kD).addMultiple(factor, specularColor);
	}

}