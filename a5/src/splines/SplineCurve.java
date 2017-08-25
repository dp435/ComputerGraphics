package splines;

import java.util.ArrayList;

import mesh.OBJFace;
import mesh.OBJMesh;
import mesh.OBJMesh_Archive;
import egl.NativeMem;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;

public abstract class SplineCurve {
	private float epsilon;

	// Spline Control Points
	private ArrayList<Vector2> controlPoints;

	// Bezier Curves that make up this Spline
	private ArrayList<CubicBezier> bezierCurves;

	// Whether or not this curve is a closed curve
	private boolean isClosed;

	public static final float DIST_THRESH = 0.15f;
	public static final int MIN_OPEN_CTRL_POINTS = 4, MIN_CLOSED_CTRL_POINTS = 3, MAX_CTRL_POINTS = 20;

	public SplineCurve(ArrayList<Vector2> controlPoints, boolean isClosed, float epsilon)
			throws IllegalArgumentException {
		if (isClosed) {
			if (controlPoints.size() < MIN_CLOSED_CTRL_POINTS)
				throw new IllegalArgumentException("Closed Splines must have at least 3 control points.");
		} else {
			if (controlPoints.size() < MIN_OPEN_CTRL_POINTS)
				throw new IllegalArgumentException("Open Splines must have at least 4 control points.");
		}

		this.controlPoints = controlPoints;
		this.isClosed = isClosed;
		this.epsilon = epsilon;
		setBeziers();
	}

	public boolean isClosed() {
		return this.isClosed;
	}

	public boolean setClosed(boolean closed) {
		if (this.isClosed && this.controlPoints.size() == 3) {
			System.err.println("You must have at least 4 control points to make an open spline.");
			return false;
		}
		this.isClosed = closed;
		setBeziers();
		return true;
	}

	public ArrayList<Vector2> getControlPoints() {
		return this.controlPoints;
	}

	public void setControlPoint(int index, Vector2 point) {
		this.controlPoints.set(index, point);
		setBeziers();
	}

	public boolean addControlPoint(Vector2 point) {
		if (this.controlPoints.size() == MAX_CTRL_POINTS) {
			System.err.println("You can only have " + SplineCurve.MAX_CTRL_POINTS + " control points per spline.");
			return false;
		}
		/*
		 * point= (x0, y0), prev= (x1, y1), curr= (x2,y2)
		 * 
		 * v= [ (y2-y1), -(x2-x1) ]
		 * 
		 * r= [ (x1-x0), (y1-y0) ]
		 * 
		 * distance between point and line prev -> curr is v . r
		 */
		Vector2 curr, prev;
		Vector2 r = new Vector2(), v = new Vector2();
		float distance = Float.POSITIVE_INFINITY;
		int index = -1;
		for (int i = 0; i < controlPoints.size(); i++) {
			curr = controlPoints.get(i);
			if (i == 0) {
				if (isClosed) {
					// add line between first and last ctrl points
					prev = controlPoints.get(controlPoints.size() - 1);
				} else {
					continue;
				}
			} else {
				prev = controlPoints.get(i - 1);
			}
			v.set(curr.y - prev.y, -(curr.x - prev.x));
			v.normalize();
			r.set(prev.x - point.x, prev.y - point.y);
			float newDist = Math.abs(v.dot(r));
			Vector2 v2 = curr.clone().sub(prev);
			v2.mul(1.0f / v2.lenSq());
			float newParam = -v2.dot(r);
			if (newDist < DIST_THRESH && newDist <= distance && 0 < newParam && newParam < 1) {
				distance = newDist;
				index = i;
			}
		}

		if (index >= 0) {
			controlPoints.add(index, point);
			setBeziers();
			return true;
		}
		System.err.println("Invalid location, try selecting a point closer to the spline.");
		return false;
	}

	public boolean removeControlPoint(int index) {
		if (this.isClosed) {
			if (this.controlPoints.size() == MIN_CLOSED_CTRL_POINTS) {
				System.err.println("You must have at least " + MIN_CLOSED_CTRL_POINTS + " for a closed Spline.");
				return false;
			}
		} else {
			if (this.controlPoints.size() == MIN_OPEN_CTRL_POINTS) {
				System.err.println("You must have at least " + MIN_OPEN_CTRL_POINTS + " for an open Spline.");
				return false;
			}
		}
		this.controlPoints.remove(index);
		setBeziers();
		return true;
	}

	public void modifyEpsilon(float newEps) {
		epsilon = newEps;
		setBeziers();
	}

	public float getEpsilon() {
		return epsilon;
	}

	/**
	 * Returns the sequence of 2D vertices on this Spline specified by the
	 * sequence of Bezier curves
	 */
	public ArrayList<Vector2> getPoints() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (CubicBezier b : bezierCurves)
			for (Vector2 p : b.getPoints())
				returnList.add(p.clone());
		return returnList;
	}

	/**
	 * Returns the sequence of normals on this Spline specified by the sequence
	 * of Bezier curves
	 */
	public ArrayList<Vector2> getNormals() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (CubicBezier b : bezierCurves)
			for (Vector2 p : b.getNormals())
				returnList.add(p.clone());
		return returnList;
	}

	/**
	 * Returns the sequence of tangents on this Spline specified by the sequence
	 * of Bezier curves
	 */
	public ArrayList<Vector2> getTangents() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for (CubicBezier b : bezierCurves)
			for (Vector2 p : b.getTangents())
				returnList.add(p.clone());
		return returnList;
	}

	/**
	 * Using this.controlPoints, create the CubicBezier objects that make up
	 * this curve and save them to this.bezierCurves. Assure that the order of
	 * the Bezier curves that you add to bezierCurves is the order in which the
	 * overall Spline is chained together. If the spline is closed, include
	 * additional CubicBeziers to account for this.
	 */
	private void setBeziers() {
		// TODO A5

		int numControlPoints = this.controlPoints.size();
		bezierCurves = new ArrayList<CubicBezier>();

		if (isClosed) {
			// Starting from segment 1, wrapping around to segment 0.
			for (int idx = 0; idx < numControlPoints; idx++) {
				Vector2 p0 = this.controlPoints.get(idx % numControlPoints);
				Vector2 p1 = this.controlPoints.get((idx + 1) % numControlPoints);
				Vector2 p2 = this.controlPoints.get((idx + 2) % numControlPoints);
				Vector2 p3 = this.controlPoints.get((idx + 3) % numControlPoints);
				CubicBezier bezierSpline = this.toBezier(p0, p1, p2, p3, epsilon);
				this.bezierCurves.add(bezierSpline);
			}
		} else {
			// Starting from segment 1; no wraparound.
			for (int idx = 0; idx < numControlPoints - 3; idx++) {
				Vector2 p0 = this.controlPoints.get(idx);
				Vector2 p1 = this.controlPoints.get(idx + 1);
				Vector2 p2 = this.controlPoints.get(idx + 2);
				Vector2 p3 = this.controlPoints.get(idx + 3);
				CubicBezier bezierSpline = this.toBezier(p0, p1, p2, p3, epsilon);
				this.bezierCurves.add(bezierSpline);
			}
		}

	}

	/**
	 * Reverses the tangents and normals associated with this Spline
	 */
	public void reverseNormalsAndTangents() {
		for (CubicBezier b : bezierCurves) {
			for (Vector2 p : b.getNormalReferences())
				p.mul(-1);
			for (Vector2 p : b.getTangentReferences())
				p.mul(-1);
		}
	}

	// Debug code
	public double getMaxAngle() {
		ArrayList<Vector2> myPoints = getPoints();
		double max = 0;
		for (int i = 0; i < myPoints.size() - 2; ++i) {
			Vector2 A = myPoints.get(i);
			Vector2 B = myPoints.get(i + 1);
			Vector2 C = myPoints.get(i + 2);

			Vector2 v1 = B.clone().sub(A);
			Vector2 v2 = C.clone().sub(B);

			v1.normalize();
			v2.normalize();

			double cur = Math.acos(v1.dot(v2));
			if (cur > max)
				max = cur;
		}
		return max;
	}

	public abstract CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps);

	/**
	 * Given a curve that defines the cross section along the axis, fill the
	 * three GLBuffer objects appropriately. Here, we revolve the crossSection
	 * curve about the positive Z-axis.
	 * 
	 * @param crossSection,
	 *            the 2D spline for which every point defines the cross section
	 *            of the surface
	 * @param data,
	 *            a MeshData where we will output our triangle mesh
	 * @param scale
	 *            > 0, parameter that controls how much the resulting surface
	 *            should be scaled
	 * @param sliceTolerance
	 *            > 0, the maximum angle in radians between adjacent vertical
	 *            slices.
	 */
	public static void build3DRevolution(SplineCurve crossSection, OBJMesh mesh, float scale, float sliceTolerance) {
		// TODO A5

		// Populates position, normal and UV arrays of the input mesh.
		double numSlices = Math.ceil(2 * Math.PI / sliceTolerance);
		double delta = 2 * Math.PI / numSlices;
		Matrix3 scaleMatrix = new Matrix3(Matrix4.createScale(scale));
		for (double rotationAngle = 0f; rotationAngle <= 2f * Math.PI; rotationAngle += delta) {
			Matrix3 rotationMatrix = Matrix3.createRotationZ((float) (rotationAngle));
			Matrix3 transformationMatrix = rotationMatrix.clone().mulBefore(scaleMatrix);
			for (Vector2 point : crossSection.getPoints()) {
				mesh.positions.add(transformationMatrix.mul(new Vector3(point.x, 0f, point.y)));
			}

			for (Vector2 normal : crossSection.getNormals()) {
				mesh.normals.add(rotationMatrix.mul(new Vector3(normal.x, 0f, normal.y)));
				mesh.uvs.add(new Vector2());
			}
			
			mesh.positions.add(transformationMatrix
					.mul(new Vector3(crossSection.getPoints().get(0).x, 0f, crossSection.getPoints().get(0).y)));
			mesh.normals.add(rotationMatrix
					.mul(new Vector3(crossSection.getNormals().get(0).x, 0f, crossSection.getNormals().get(0).y)));
			mesh.uvs.add(new Vector2());
		}

		// Generate faces of the mesh.
		int numVertices = crossSection.getPoints().size();
		for (int sliceIdx = 0; sliceIdx < numSlices; sliceIdx++) {
			for (int vertexIdx = 0; vertexIdx < numVertices; vertexIdx++) {
				// prevent merge with vertex 0 and n-1.
				if (!crossSection.isClosed && vertexIdx >= numVertices-1)
					break;
				
				OBJFace lowerRightTriangle = new OBJFace(3, true, true);
				lowerRightTriangle.setVertex(2, sliceIdx * (numVertices + 1) + vertexIdx,
						sliceIdx * (numVertices + 1) + vertexIdx, sliceIdx * (numVertices + 1) + vertexIdx);
				lowerRightTriangle.setVertex(1, sliceIdx * (numVertices + 1) + vertexIdx + 1,
						sliceIdx * numVertices + vertexIdx + 1, sliceIdx * numVertices + vertexIdx + 1);
				lowerRightTriangle.setVertex(0, (sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1,
						(sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1,
						(sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1);
				mesh.faces.add(lowerRightTriangle);

				OBJFace upperLeftTriangle = new OBJFace(3, true, true);
				upperLeftTriangle.setVertex(2, (sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1,
						(sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1,
						(sliceIdx + 1) * (numVertices + 1) + vertexIdx + 1);
				upperLeftTriangle.setVertex(1, (sliceIdx + 1) * (numVertices + 1) + vertexIdx,
						(sliceIdx + 1) * (numVertices + 1) + vertexIdx, (sliceIdx + 1) * (numVertices + 1) + vertexIdx);
				upperLeftTriangle.setVertex(0, sliceIdx * (numVertices + 1) + vertexIdx,
						sliceIdx * (numVertices + 1) + vertexIdx, sliceIdx * (numVertices + 1) + vertexIdx);
				mesh.faces.add(upperLeftTriangle);
			}

		}
	}

}
