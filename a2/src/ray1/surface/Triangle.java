package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Vector2;
import egl.math.Vector3;
import ray1.shader.Shader;
import ray1.OBJFace;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
	/**
	 * The normal vector of this triangle, if vertex normals are not specified
	 */
	Vector3 norm;

	/** The mesh that contains this triangle */
	Mesh owner;

	/** The face that contains this triangle */
	OBJFace face = null;

	double a, b, c, d, e, f;

	public Triangle(Mesh owner, OBJFace face, Shader shader) {
		this.owner = owner;
		this.face = face;

		Vector3 v0 = owner.getMesh().getPosition(face, 0);
		Vector3 v1 = owner.getMesh().getPosition(face, 1);
		Vector3 v2 = owner.getMesh().getPosition(face, 2);

		if (!face.hasNormals()) {
			Vector3 e0 = new Vector3(), e1 = new Vector3();
			e0.set(v1).sub(v0);
			e1.set(v2).sub(v0);
			norm = new Vector3();
			norm.set(e0).cross(e1).normalize();
		}

		a = v0.x - v1.x;
		b = v0.y - v1.y;
		c = v0.z - v1.z;

		d = v0.x - v2.x;
		e = v0.y - v2.y;
		f = v0.z - v2.z;

		this.setShader(shader);
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param rayIn
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		Vector3 v0 = owner.getMesh().getPosition(face, 0);
		double g = rayIn.direction.x;
		double h = rayIn.direction.y;
		double i = rayIn.direction.z;
		double j = v0.x - rayIn.origin.x;
		double k = v0.y - rayIn.origin.y;
		double l = v0.z - rayIn.origin.z;
		double M = a * (e * i - h * f) + b * (g * f - d * i) + c * (d * h - e * g);

		double t = -(f * (a * k - j * b) + e * (j * c - a * l) + d * (b * l - k * c)) / M;
		if (t < rayIn.start || t > rayIn.end)
			return false;

		double gamma = (i * (a * k - j * b) + h * (j * c - a * l) + g * (b * l - k * c)) / M;
		if (gamma < 0 || gamma > 1)
			return false;

		double beta = (j * (e * i - h * f) + k * (g * f - d * i) + l * (d * h - e * g)) / M;
		if (beta < 0 || beta > 1 - gamma)
			return false;

		outRecord.surface = this;
		outRecord.t = t;
		outRecord.location.set(rayIn.origin.clone().add(rayIn.direction.clone().mul(t)));

		double alpha = 1 - beta - gamma;
		if (norm == null) {
			Vector3 n0 = owner.getMesh().getNormal(face, 0).clone().mul((float) alpha);
			Vector3 n1 = owner.getMesh().getNormal(face, 1).clone().mul((float) beta);
			Vector3 n2 = owner.getMesh().getNormal(face, 2).clone().mul((float) gamma);
			Vector3 interpolatedNormal = new Vector3();
			interpolatedNormal.set(n0.add(n1.add(n2)));
			outRecord.normal.set(interpolatedNormal).normalize();
		} else
			outRecord.normal.set(norm).normalize();

		if (face.hasUVs()) {
			Vector2 uv0 = owner.getMesh().getUV(face, 0).clone().mul((float) alpha);
			Vector2 uv1 = owner.getMesh().getUV(face, 1).clone().mul((float) beta);
			Vector2 uv2 = owner.getMesh().getUV(face, 2).clone().mul((float) gamma);
			Vector2 interpolatedTexture = new Vector2();
			interpolatedTexture.set(uv0.add(uv1.add(uv2)));
			outRecord.texCoords.set(interpolatedTexture);
		}
		return true;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Triangle ";
	}
}