package splines;

import java.util.ArrayList;

import egl.math.Vector2;
/*
 * Cubic Bezier class for the splines assignment
 */

public class CubicBezier {

	// This Bezier's control points
	public Vector2 p0, p1, p2, p3;

	// Control parameter for curve smoothness
	float epsilon;

	// The points on the curve represented by this Bezier
	private ArrayList<Vector2> curvePoints;

	// The normals associated with curvePoints
	private ArrayList<Vector2> curveNormals;

	// The tangent vectors of this bezier
	private ArrayList<Vector2> curveTangents;

	/**
	 * 
	 * Cubic Bezier Constructor
	 * 
	 * Given 2-D BSpline Control Points correctly set self.{p0, p1, p2, p3},
	 * self.uVals, self.curvePoints, and self.curveNormals
	 * 
	 * @param bs0
	 *            First Bezier Spline Control Point
	 * @param bs1
	 *            Second Bezier Spline Control Point
	 * @param bs2
	 *            Third Bezier Spline Control Point
	 * @param bs3
	 *            Fourth Bezier Spline Control Point
	 * @param eps
	 *            Maximum angle between line segments
	 */
	public CubicBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps) {
		curvePoints = new ArrayList<Vector2>();
		curveTangents = new ArrayList<Vector2>();
		curveNormals = new ArrayList<Vector2>();
		epsilon = eps;

		this.p0 = new Vector2(p0);
		this.p1 = new Vector2(p1);
		this.p2 = new Vector2(p2);
		this.p3 = new Vector2(p3);

		tessellate();
	}

	/**
	 * Approximate a Bezier segment with a number of vertices, according to an
	 * appropriate smoothness criterion for how many are needed. The points on
	 * the curve are written into the array self.curvePoints, the tangents into
	 * self.curveTangents, and the normals into self.curveNormals. The final
	 * point, p3, is not included, because cubic Beziers will be "strung
	 * together".
	 */
	private void tessellate() {
		// TODO A5
		
		// Calculate tangent & normal for p0.
		Vector2 tangent = p1.clone().sub(p0).normalize();
		Vector2 normal = new Vector2(tangent.y, -tangent.x).normalize();
		curvePoints.add(p0);
		curveTangents.add(tangent);
		curveNormals.add(normal);
		
		// Recursively subdivide.
		subdivide(p0, p1, p2, p3, 0);
	}

	private void subdivide(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, int depth) {
		// Max recursion allowed set at 10.
		if (depth >= 10)
			return;
		
		Vector2 vect01 = p1.clone().sub(p0).normalize();
		Vector2 vect12 = p2.clone().sub(p1).normalize();
		Vector2 vect23 = p3.clone().sub(p2).normalize();

		double theta012 = Math.abs(Math.acos(vect01.clone().dot(vect12)));
		double theta123 = Math.abs(Math.acos(vect12.clone().dot(vect23)));

		// Subdivide if either angles are greater than epsilon.
		if (theta012 > epsilon / 2f || theta123 > epsilon / 2f) {
			Vector2 p10 = p1.clone().lerp(p0, 0.5f);
			Vector2 p11 = p2.clone().lerp(p1, 0.5f);
			Vector2 p12 = p3.clone().lerp(p2, 0.5f);
			Vector2 p20 = p11.clone().lerp(p10, 0.5f);
			Vector2 p21 = p12.clone().lerp(p11, 0.5f);
			Vector2 p30 = p21.clone().lerp(p20, 0.5f);

			Vector2 tangent = p21.clone().sub(p20).normalize();
			Vector2 normal = new Vector2(tangent.y, -tangent.x).normalize();
			
			subdivide(p0, p10, p20, p30, depth + 1);
			
			curvePoints.add(p30);
			curveTangents.add(tangent);
			curveNormals.add(normal);
			
			subdivide(p30, p21, p12, p3, depth + 1);
		}
	}

	/**
	 * @return The points on this cubic bezier
	 */
	public ArrayList<Vector2> getPoints() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curvePoints)
			returnList.add(p.clone());
		return returnList;
	}

	/**
	 * @return The tangents on this cubic bezier
	 */
	public ArrayList<Vector2> getTangents() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curveTangents)
			returnList.add(p.clone());
		return returnList;
	}

	/**
	 * @return The normals on this cubic bezier
	 */
	public ArrayList<Vector2> getNormals() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curveNormals)
			returnList.add(p.clone());
		return returnList;
	}

	/**
	 * @return The references to points on this cubic bezier
	 */
	public ArrayList<Vector2> getPointReferences() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curvePoints)
			returnList.add(p);
		return returnList;
	}

	/**
	 * @return The references to tangents on this cubic bezier
	 */
	public ArrayList<Vector2> getTangentReferences() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curveTangents)
			returnList.add(p);
		return returnList;
	}

	/**
	 * @return The references to normals on this cubic bezier
	 */
	public ArrayList<Vector2> getNormalReferences() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (Vector2 p : curveNormals)
			returnList.add(p);
		return returnList;
	}
}
