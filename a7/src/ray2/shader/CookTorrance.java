package ray2.shader;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

public class CookTorrance extends BRDFShader {

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The roughness controlling the roughness of the surface. */
	protected double roughness = 1.0;
	public void setRoughness(double roughness) { this.roughness = roughness; }

	/** The index of refraction of this material. Used when calculating Fresnel factor. */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	public CookTorrance() { }

	public String toString() {    
		return "CookTorrance " + diffuseColor + " " + specularColor + " " + roughness + " end";
	}

	@Override
	protected void evalBRDF(Vector3d L, Vector3d V, Vector3d N, Colord kD,
			Colord outColor) {
		Vector3d H = new Vector3d();
		H.set(L).add(V).normalize();

		// calculate intermediary values
		double NdotL = N.dot(L);
		double NdotH = N.dot(H); 
		double NdotV = N.dot(V);
		double VdotH = V.dot(H);
		double mSquared = roughness * roughness;
	 
		// fresnel
		double fresnelTerm = fresnel(N, V, refractiveIndex);
		
		// roughness (or: microfacet distribution function)
		// beckmann distribution function
		double r1 = 1.0 / (mSquared * Math.pow(NdotH, 4.0));
		double r2 = (NdotH * NdotH - 1.0) / (mSquared * NdotH * NdotH);
		double roughnessTerm = r1 * Math.exp(r2);
		
		// geometric attenuation
		double NH2 = 2.0 * NdotH;
		double g1 = (NH2 * NdotV) / VdotH;
		double g2 = (NH2 * NdotL) / VdotH;
		double geoAttTerm = Math.min(1.0, Math.min(g1, g2));
		
		double factor = (fresnelTerm * roughnessTerm * geoAttTerm) / (NdotV * NdotL * Math.PI);
		
		outColor.set(kD)
			 .addMultiple(factor, specularColor);
	}
}
