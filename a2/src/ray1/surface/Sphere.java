package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Vector2d;
import egl.math.Vector3;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3 center = new Vector3();

	public void setCenter(Vector3 center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected float radius = 1.0f;

	public void setRadius(float radius) {
		this.radius = radius;
	}

	protected final double M_2PI = 2 * Math.PI;

	public Sphere() {
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		Vector3d esubcTerm = rayIn.origin.clone().sub(center);
		double A = rayIn.direction.clone().dot(rayIn.direction);
		double B = rayIn.direction.clone().dot(esubcTerm);
		double C = esubcTerm.clone().dot(esubcTerm) - radius * radius;
		double discriminant = B * B - A * C;
		Double t = null;

		if (discriminant < 0)
			return false;
		else if (discriminant == 0) {
			t = -B / A;
		} else {
			double tLow = (-B - Math.sqrt(discriminant)) / A;
			double tHigh = (-B + Math.sqrt(discriminant)) / A;
			if (rayIn.start < tLow && tLow < rayIn.end)
				t = tLow;
			else if (rayIn.start < tHigh && tHigh < rayIn.end)
				t = tHigh;
			else
				return false;
		}

		outRecord.surface = this;
		outRecord.t = t;
		outRecord.location.set(rayIn.origin.clone().add(rayIn.direction.clone().mul(t)));
		outRecord.normal.set((outRecord.location.clone().sub(center)).mul(2)).normalize();

		Vector2d texCoords = new Vector2d();
		Vector3d intersection = (outRecord.location.clone().sub(center)).normalize();
		double xz = Math.atan2(intersection.x, intersection.z);

		texCoords.x = 0.5 + xz / (2 * Math.PI);
		texCoords.y = 0.5 + (Math.asin(intersection.y) / Math.PI);

		outRecord.texCoords.set(texCoords);

		return true;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}