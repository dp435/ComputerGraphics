package ray2.surface;

import ray2.IntersectionRecord;
import ray2.Ray;

import java.util.ArrayList;

import egl.math.Vector3d;
import ray2.shader.Shader;
import ray2.mesh.OBJFace;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
	/** The normal vector of this triangle, if vertex normals are not specified */
	Vector3d norm;

	/** The mesh that contains this triangle */
	Mesh owner;

	/** The face that contains this triangle */
	OBJFace face = null;

	double a, b, c, d, e, f;

	  public Triangle(Mesh owner, OBJFace face, Shader shader) {
		    this.owner = owner;
		    this.face = face;

		    Vector3d v0 = new Vector3d(owner.getMesh().getPosition(face,0));
		    Vector3d v1 = new Vector3d(owner.getMesh().getPosition(face,1));
		    Vector3d v2 = new Vector3d(owner.getMesh().getPosition(face,2));
		    
		    if (!face.hasNormals()) {
		      Vector3d e0 = new Vector3d(), e1 = new Vector3d();
		      e0.set(v1).sub(v0);
		      e1.set(v2).sub(v0);
		      norm = new Vector3d();
		      norm.set(e0).cross(e1).normalize();
		    }

		    a = v0.x-v1.x;
		    b = v0.y-v1.y;
		    c = v0.z-v1.z;
		    
		    d = v0.x-v2.x;
		    e = v0.y-v2.y;
		    f = v0.z-v2.z;
		    
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

	  	//transform the resulting intersection point and normal to world space

		//transform ray into object space
		Ray ray = untransformRay(rayIn);		
		
		Vector3d v0 = new Vector3d(owner.getMesh().getPosition(face,0));
		
		double g = ray.direction.x;
		double h = ray.direction.y;
		double i = ray.direction.z;
		double j = v0.x - ray.origin.x;
		double k = v0.y - ray.origin.y;
		double l = v0.z - ray.origin.z;
		double M = a * (e * i - h * f) + b * (g * f - d * i) + c
				* (d * h - e * g);

		double ei_hf = e * i - h * f;
		double gf_di = g * f - d * i;
		double dh_eg = d * h - e * g;
		double ak_jb = a * k - j * b;
		double jc_al = j * c - a * l;
		double bl_kc = b * l - k * c;

		double t = -(f * (ak_jb) + e * (jc_al) + d * (bl_kc)) / M;
		if (t > ray.end || t < ray.start)
			return false;

		double beta = (j * (ei_hf) + k * (gf_di) + l * (dh_eg)) / M;
		if (beta < 0 || beta > 1)
			return false;

		double gamma = (i * (ak_jb) + h * (jc_al) + g * (bl_kc)) / M;
		if (gamma < 0 || gamma + beta > 1)
			return false;

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			
			//transform back into world space
			tMat.mulPos(outRecord.location);		
			
			outRecord.surface = this;

			if (norm != null) {
				outRecord.normal.set(norm);
			} else {
				outRecord.normal
						.setZero()
						.addMultiple(1 - beta - gamma, owner.getMesh().getNormal(face,0))
						.addMultiple(beta, owner.getMesh().getNormal(face,1))
						.addMultiple(gamma, owner.getMesh().getNormal(face,2));
			}
			
			tMatTInv.mulDir(outRecord.normal);
			
			outRecord.normal.normalize();
			if (face.hasUVs()) {
				outRecord.texCoords.setZero()
						.addMultiple(1 - beta - gamma, owner.getMesh().getUV(face,0))
						.addMultiple(beta, owner.getMesh().getUV(face,1))
						.addMultiple(gamma, owner.getMesh().getUV(face,2));
			}
		}

		return true;

	}

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		minBound = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		maxBound = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		ArrayList<Vector3d> points = new ArrayList<Vector3d>();
		points.add(new Vector3d(owner.getMesh().getPosition(face,0)));
		points.add(new Vector3d(owner.getMesh().getPosition(face,1)));
		points.add(new Vector3d(owner.getMesh().getPosition(face,2)));
		averagePosition = new Vector3d();
		
		for (Vector3d point : points) {
			tMat.mulPos(point);
			averagePosition.add(point);
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
		
		averagePosition.div(3.0);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Triangle ";
	}
}