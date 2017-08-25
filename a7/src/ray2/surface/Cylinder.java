package ray2.surface;

import ray2.IntersectionRecord;
import ray2.Ray;

import java.util.ArrayList;

import egl.math.Vector3d;

public class Cylinder extends Surface {

	/** The center of the bottom of the cylinder x , y ,z components. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the cylinder. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	/** The height of the cylinder. */
	protected double height = 1.0;

	public void setHeight(double height) {
		this.height = height;
	}

	public Cylinder() {
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

		  	//transform the resulting intersection point and normal to world space
		  
		    Ray ray = untransformRay(rayIn);

		    // Rename the common vectors so I don't have to type so much
		    Vector3d d = ray.direction;
		    Vector3d c = center;
		    Vector3d o = ray.origin;

		    double tMin = ray.start, tMax = ray.end;
		    // Compute some factors used in computation
		    double qx = o.x - c.x;
		    double qy = o.y - c.y;
		    //double qz = o.z - c.z;
		    double rr = radius * radius;

		    double dd = d.x * d.x + d.y *d.y;
		    double qd = d.x * qx + d.y * qy;
		    double qq =  qx * qx + qy * qy;

		    double t = 0, td1=0, td2=0;
		    double zMin = c.z - height/2;
		    double zMax = c.z + height/2;

		    // z-plane cap calculations
		    if (d.z >= 0) {
		      td1 = (zMin- o.z) / d.z;
		      td2 = (zMax - o.z) / d.z;
		    }
		    else {
		      td1 = (zMax - o.z) / d.z;
		      td2 = (zMin - o.z) / d.z;
		    }
		    if (tMin > td2 || td1 > tMax)
		      return false;
		    if (td1 > tMin)
		      tMin = td1;
		    if (td2 < tMax)
		      tMax = td2;

		    // solving the quadratic equation for t at the pts of intersection
		    // dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		    double discriminantsqr = (qd * qd - dd * (qq - rr));

		    // If the discriminant is less than zero, there is no intersection
		    if (discriminantsqr < 0) {
		      return false;
		    }

		    // Otherwise check and make sure that the intersections occur on the ray (t
		    // > 0) and return the closer one
		    double discriminant = Math.sqrt(discriminantsqr);
		    double t1 = (-qd - discriminant) / dd;
		    double t2 = (-qd + discriminant) / dd;

		    if (t1 > ray.start && t1 < ray.end) {
		      t = t1;
		    }
		    else if (t2 > ray.start && t2 < ray.end) {
		      t = t2;
		    }

		    Vector3d thit1 = new Vector3d(0); 
		    ray.evaluate(thit1, tMin);
		    Vector3d thit2 = new Vector3d(0); 
		    ray.evaluate(thit2, tMax);

		    double dx1 = thit1.x-c.x;  
		    double dy1 = thit1.y-c.y; 
		    double dx2 = thit2.x-c.x;  
		    double dy2 = thit2.y-c.y; 

		    if ((t < tMin || t > tMax) && dx1 * dx1 + dy1 * dy1 > rr && dx2 * dx2 + dy2 * dy2 > rr) {
		      return false;
		    }

		    // There was an intersection, fill out the intersection record
		    if (outRecord != null) {
		      double tside =Math.min( td1, td2);

		      if (t <tside) {
		        outRecord.t = tside;
		        ray.evaluate(outRecord.location, tside);
		        outRecord.normal.set(0, 0, 1);
		      }
		      else {
		        outRecord.t = t;
		        ray.evaluate(outRecord.location, t);        
		        outRecord.normal.set(outRecord.location.x, outRecord.location.y, 0).sub(c.x, c.y, 0);
		      }

		      if (outRecord.normal.dot(ray.direction) > 0)
		        outRecord.normal.negate();

		      outRecord.surface = this;

		      tMat.mulPos(outRecord.location);
		      tMatTInv.mulDir(outRecord.normal).normalize();
		    }

		    return true;
		  }

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box may be transformed by a transformation matrix.
		minBound = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		maxBound = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		ArrayList<Vector3d> points = new ArrayList<Vector3d>();
		points.add(new Vector3d(center).add(new Vector3d(radius, radius, height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(-radius, radius, height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(radius, -radius, height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(-radius, -radius, height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(radius, radius, -height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(-radius, radius, -height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(radius, -radius, -height/2.0)));
		points.add(new Vector3d(center).add(new Vector3d(-radius, -radius, -height/2.0)));

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
		return "Cylinder " + center + " " + radius + " " + height + " "
				+ shader + " end";
	}
}
