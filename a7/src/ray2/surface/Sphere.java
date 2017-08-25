package ray2.surface;

import ray2.IntersectionRecord;
import ray2.Ray;

import java.util.ArrayList;

import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
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
	  	//transform the ray to object space
	  	//transform the resulting intersection point and normal to world space

		//transform the ray into object space
		Ray ray = untransformRay(rayIn);
		
		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		double qz = o.z - c.z;
		double dd = d.lenSq();
		double qd = qx * d.x + qy * d.y + qz * d.z;
		double qq = qx * qx + qy * qy + qz * qz;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray
		// (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;
		double t = 0;
		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		} else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		} else {
			return false; // Neither intersection was in the ray's half line.
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			outRecord.surface = this;
			outRecord.normal.set(outRecord.location).sub(center).normalize();
			double theta = Math.asin(outRecord.normal.y);
			double phi = Math.atan2(outRecord.normal.x, outRecord.normal.z);
			double u = (phi + Math.PI) / (2 * Math.PI);
			double v = (theta - Math.PI / 2) / Math.PI;
			outRecord.texCoords.set(u, v);
			
			//transform location and normal back to world space
			tMat.mulPos(outRecord.location);
			tMatTInv.mulDir(outRecord.normal);
		}

		return true;
	}

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		minBound = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		maxBound = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		ArrayList<Vector3d> points = new ArrayList<Vector3d>();
		points.add(center.clone().add(new Vector3d(radius)));
		points.add(center.clone().add(new Vector3d(-radius)));
		points.add(center.clone().add(new Vector3d(-radius, radius, radius)));
		points.add(center.clone().add(new Vector3d(radius, -radius, radius)));
		points.add(center.clone().add(new Vector3d(radius, radius, -radius)));
		points.add(center.clone().add(new Vector3d(-radius, -radius, radius)));
		points.add(center.clone().add(new Vector3d(-radius, radius, -radius)));
		points.add(center.clone().add(new Vector3d(radius, -radius, -radius)));

		for (Vector3d point : points) {
			tMat.mulPos(point);
			if (point.x < minBound.x)
				minBound.x = point.x;
			if (point.y < minBound.y)
				minBound.y = point.y;
			if (point.z < minBound.z)
				minBound.z = point.z;
			if (point.x > maxBound.x)
				maxBound.x = point.x;
			if (point.y > maxBound.y)
				maxBound.y = point.y;
			if (point.z > maxBound.z)
				maxBound.z = point.z;
		}
		
		averagePosition = tMat.mulPos(new Vector3d(center));
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}