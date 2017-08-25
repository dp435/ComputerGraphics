package meshgen;

import math.Vector2;
import math.Vector3;

/**
 * This class is responsible for generating a torus mesh.
 * 
 * @author Daniel Park (dp435)
 */

public class Torus {
	private double minorRadius;
	private double majorRadius;
	private int numCylindricalSlices;
	private int numCylinderFaces;
	private double theta;
	private double phi;
	private OBJMesh mesh;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;

	/**
	 * Constructor for torus with a major radius of 1.
	 * 
	 * @param minorRadius
	 *            the minor radius.
	 * @param n
	 *            number of laditudinal divisions.
	 * @param m
	 *            number of longitudinal divisions.
	 */
	public Torus(Double minorRadius, int n, int m) {
		if (minorRadius == null)
			this.minorRadius = 0.25;
		else
			this.minorRadius = minorRadius;
		majorRadius = 1.0;
		numCylindricalSlices = n;
		numCylinderFaces = m;
		theta = (2.0 * Math.PI) / numCylindricalSlices;
		phi = (2.0 * Math.PI) / numCylinderFaces;
		mesh = new OBJMesh();
	}

	/**
	 * Method to generate the torus mesh.
	 */
	public void generateMesh() {
		generateVertices();
		generateFaces();
	}

	/**
	 * Getter method for the mesh.
	 * 
	 * @return the mesh.
	 */
	public OBJMesh getMesh() {
		return mesh;
	}

	/**
	 * Helper method to generate the vertices, the normals, and the texture
	 * coordinates.
	 */
	private void generateVertices() {
		for (int thetaIdx = 0; thetaIdx < numCylindricalSlices; thetaIdx++) {
			for (int phiIdx = 0; phiIdx < numCylinderFaces; phiIdx++) {
				float[] coordinateArray = convertToCartesian(Math.PI / 2.0 - thetaIdx * theta, Math.PI - phiIdx * phi);
				mesh.positions.add(new Vector3(coordinateArray[X], coordinateArray[Y], coordinateArray[Z]));
				float[] normalArray = calculateNormal(Math.PI / 2.0 - thetaIdx * theta, Math.PI - phiIdx * phi);
				mesh.normals.add(new Vector3(normalArray[X], normalArray[Y], normalArray[Z]));
			}
		}

		double height = 1.0 / numCylinderFaces;
		double width = 1.0 / numCylindricalSlices;

		for (int xIdx = 0; xIdx <= numCylindricalSlices; xIdx++) {
			for (int yIdx = 0; yIdx < numCylinderFaces; yIdx++) {
				mesh.uvs.add(new Vector2((float) (width * xIdx), (float) (height * yIdx)));
			}
			mesh.uvs.add(new Vector2((float) (width * xIdx), 1.0f));
		}
	}

	/**
	 * Helper method to construct the faces of the mesh.
	 */
	private void generateFaces() {
		Integer previousLeftTextureOffset = null;
		Integer previousRightTextureOffset = null;

		for (int cylinderIdx = 0; cylinderIdx < numCylindricalSlices - 1; cylinderIdx++) {

			int leftVertexOffset = cylinderIdx * numCylinderFaces;
			int rightVertexOffset = (cylinderIdx + 1) * numCylinderFaces;

			int leftTextureOffset = cylinderIdx * numCylinderFaces + 1 * cylinderIdx;
			int rightTextureOffset = (cylinderIdx + 1) * numCylinderFaces + 1 * (cylinderIdx + 1);

			for (int faceIdx = 0; faceIdx < numCylinderFaces - 1; faceIdx++) {
				OBJFace lowerTriangle = new OBJFace(3, true, true);
				lowerTriangle.setVertex(0, leftVertexOffset + faceIdx + 1, leftTextureOffset + faceIdx + 1,
						leftVertexOffset + faceIdx + 1);
				lowerTriangle.setVertex(1, leftVertexOffset + faceIdx, leftTextureOffset + faceIdx,
						leftVertexOffset + faceIdx);
				lowerTriangle.setVertex(2, rightVertexOffset + faceIdx, rightTextureOffset + faceIdx,
						rightVertexOffset + faceIdx);
				mesh.faces.add(lowerTriangle);

				OBJFace upperTriangle = new OBJFace(3, true, true);
				upperTriangle.setVertex(0, leftVertexOffset + faceIdx + 1, leftTextureOffset + faceIdx + 1,
						leftVertexOffset + faceIdx + 1);
				upperTriangle.setVertex(1, rightVertexOffset + faceIdx, rightTextureOffset + faceIdx,
						rightVertexOffset + faceIdx);
				upperTriangle.setVertex(2, rightVertexOffset + faceIdx + 1, rightTextureOffset + faceIdx + 1,
						rightVertexOffset + faceIdx + 1);
				mesh.faces.add(upperTriangle);

				previousLeftTextureOffset = leftTextureOffset + faceIdx + 1;
				previousRightTextureOffset = rightTextureOffset + faceIdx + 1;
			}

			OBJFace lowerTriangle = new OBJFace(3, true, true);
			lowerTriangle.setVertex(0, leftVertexOffset, previousLeftTextureOffset + 1, leftVertexOffset);
			lowerTriangle.setVertex(1, leftVertexOffset + numCylinderFaces - 1, previousLeftTextureOffset,
					leftVertexOffset + numCylinderFaces - 1);
			lowerTriangle.setVertex(2, rightVertexOffset + numCylinderFaces - 1, previousRightTextureOffset,
					rightVertexOffset + numCylinderFaces - 1);
			mesh.faces.add(lowerTriangle);

			OBJFace upperTriangle = new OBJFace(3, true, true);
			upperTriangle.setVertex(0, leftVertexOffset, previousLeftTextureOffset + 1, leftVertexOffset);
			upperTriangle.setVertex(1, rightVertexOffset + numCylinderFaces - 1, previousRightTextureOffset,
					rightVertexOffset + numCylinderFaces - 1);
			upperTriangle.setVertex(2, rightVertexOffset, previousRightTextureOffset + 1, rightVertexOffset);
			mesh.faces.add(upperTriangle);

			previousLeftTextureOffset += 1;
			previousRightTextureOffset += 1;
		}

		int lastOffset = (numCylindricalSlices - 1) * numCylinderFaces;

		for (int faceIdx = 0; faceIdx < numCylinderFaces - 1; faceIdx++) {
			OBJFace lowerTriangle = new OBJFace(3, true, true);
			lowerTriangle.setVertex(0, lastOffset + faceIdx + 1, faceIdx + previousLeftTextureOffset + 2,
					lastOffset + faceIdx + 1);
			lowerTriangle.setVertex(1, lastOffset + faceIdx, faceIdx + previousLeftTextureOffset + 1,
					lastOffset + faceIdx);
			lowerTriangle.setVertex(2, faceIdx, faceIdx + previousRightTextureOffset + 1, faceIdx);
			mesh.faces.add(lowerTriangle);

			OBJFace upperTriangle = new OBJFace(3, true, true);
			upperTriangle.setVertex(0, lastOffset + faceIdx + 1, faceIdx + previousLeftTextureOffset + 2,
					lastOffset + faceIdx + 1);
			upperTriangle.setVertex(1, faceIdx, faceIdx + previousRightTextureOffset + 1, faceIdx);
			upperTriangle.setVertex(2, faceIdx + 1, faceIdx + previousRightTextureOffset + 2, faceIdx + 1);
			mesh.faces.add(upperTriangle);
		}

		OBJFace lowerTriangle = new OBJFace(3, true, true);
		lowerTriangle.setVertex(0, lastOffset, previousLeftTextureOffset + numCylinderFaces + 1, lastOffset);
		lowerTriangle.setVertex(1, lastOffset + numCylinderFaces - 1, previousLeftTextureOffset + numCylinderFaces,
				lastOffset + numCylinderFaces - 1);
		lowerTriangle.setVertex(2, numCylinderFaces - 1, previousRightTextureOffset + numCylinderFaces,
				numCylinderFaces - 1);
		mesh.faces.add(lowerTriangle);

		OBJFace upperTriangle = new OBJFace(3, true, true);
		upperTriangle.setVertex(0, lastOffset, previousLeftTextureOffset + numCylinderFaces + 1, lastOffset);
		upperTriangle.setVertex(1, numCylinderFaces - 1, previousRightTextureOffset + numCylinderFaces,
				numCylinderFaces - 1);
		upperTriangle.setVertex(2, 0, previousRightTextureOffset + numCylinderFaces + 1, 0);
		mesh.faces.add(upperTriangle);

	}

	/**
	 * A helper function used to convert to Cartesian coordinates.
	 * 
	 * @param theta
	 *            the radial angle.
	 * @param phi
	 *            the polar angle.
	 * @return float[] containing the Cartesian coordinates.
	 */
	private float[] convertToCartesian(double theta, double phi) {
		float[] coordinateArray = new float[3];
		coordinateArray[X] = (float) (-(majorRadius + minorRadius * Math.cos(phi)) * Math.cos(theta));
		coordinateArray[Y] = (float) (-minorRadius * Math.sin(phi));
		coordinateArray[Z] = (float) (-(majorRadius + minorRadius * Math.cos(phi)) * Math.sin(theta));
		return coordinateArray;
	}

	/**
	 * A helper function used to calculate the normals.
	 * 
	 * @param theta
	 *            the radial angle.
	 * @param phi
	 *            the polar angle.
	 * @return float[] containing the calculated normals.
	 */
	private float[] calculateNormal(double theta, double phi) {
		float[] normalArray = new float[3];
		normalArray[X] = (float) (-Math.cos(phi) * Math.cos(theta));
		normalArray[Y] = (float) (-Math.sin(phi));
		normalArray[Z] = (float) (-Math.cos(phi) * Math.sin(theta));
		return normalArray;
	}

}
