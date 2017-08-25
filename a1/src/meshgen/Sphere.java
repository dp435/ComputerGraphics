package meshgen;

import java.util.ArrayList;

import math.Vector2;
import math.Vector3;

/**
 * This class is responsible for generating a sphere mesh.
 * 
 * @author Daniel Park (dp435)
 */

public class Sphere {
	private static final int RADIUS = 1;
	private int numPartitions;
	private int numStrips;
	private OBJMesh mesh;
	private Integer northPoleIdx;
	private Integer southPoleIdx;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private double latitudinalAngularDifference;
	private double longitudinalAngularDifference;

	/**
	 * Constructor for Sphere.
	 * 
	 * @param nDivisions
	 *            number of laditudinal divisions.
	 * @param mDivisions
	 *            number of longitudinal divisions.
	 */
	public Sphere(int nDivisions, int mDivisions) {
		numPartitions = nDivisions;
		numStrips = mDivisions;
		mesh = new OBJMesh();
		latitudinalAngularDifference = 2.0 * Math.PI / nDivisions;
		longitudinalAngularDifference = Math.PI / numStrips;
		northPoleIdx = null;
		southPoleIdx = null;
	}

	/**
	 * Method to generate the sphere mesh.
	 */
	public void generateMesh() {
		generateVertices();
		generateTextureCoordinates();
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
	 * Helper method to generate the vertices and the normals.
	 */
	private void generateVertices() {
		Vector3 northPole = new Vector3(0.0f, 1.0f, 0.0f);
		mesh.positions.add(northPole);
		mesh.normals.add(northPole);
		northPoleIdx = mesh.positions.size() - 1;

		for (int longitudeIdx = 1; longitudeIdx < numStrips; longitudeIdx++) {
			for (int latitudeIdx = 0; latitudeIdx < numPartitions; latitudeIdx++) {
				double[] coordinateArray = sphericalToCartesian(RADIUS, latitudinalAngularDifference * latitudeIdx,
						longitudinalAngularDifference * longitudeIdx);
				Vector3 vertex = new Vector3((float) coordinateArray[X], (float) coordinateArray[Y],
						(float) coordinateArray[Z]);
				mesh.positions.add(vertex);
				mesh.normals.add(vertex);
			}
		}

		Vector3 southPole = new Vector3(0.0f, -1.0f, 0.0f);
		mesh.positions.add(southPole);
		mesh.normals.add(southPole);
		southPoleIdx = mesh.positions.size() - 1;
	}

	/**
	 * Helper method to generate the texture coordinates.
	 */
	private void generateTextureCoordinates() {
		final double partitionWidth = 1.0 / numPartitions;
		final double partitionHeight = 1.0 / numStrips;

		for (int latitudeIdx = 0; latitudeIdx < numPartitions; latitudeIdx++) {
			mesh.uvs.add(new Vector2((float) (partitionWidth * latitudeIdx), 1.0f));
		}

		for (int longitudeIdx = 1; longitudeIdx < numStrips; longitudeIdx++) {
			for (int latitudeIdx = 0; latitudeIdx < numPartitions; latitudeIdx++) {
				mesh.uvs.add(new Vector2((float) (partitionWidth * latitudeIdx),
						(float) (1.0 - partitionHeight * longitudeIdx)));
			}
			mesh.uvs.add(new Vector2(1.0f, (float) (1.0 - partitionHeight * longitudeIdx)));
		}

		for (int latitudeIdx = 1; latitudeIdx < numPartitions; latitudeIdx++) {
			mesh.uvs.add(new Vector2((float) (partitionWidth * latitudeIdx), 0.0f));
		}
		mesh.uvs.add(new Vector2(1.0f, 0.0f));
	}

	/**
	 * Helper method to construct the faces of the mesh.
	 */
	private void generateFaces() {
		// Generate the north cap.
		for (int latitudinalIdx = 1; latitudinalIdx < numPartitions; latitudinalIdx++) {
			OBJFace face = new OBJFace(3, true, true);
			face.setVertex(0, northPoleIdx, latitudinalIdx - 1, northPoleIdx);
			face.setVertex(1, latitudinalIdx, numPartitions + latitudinalIdx - 1, latitudinalIdx);
			face.setVertex(2, latitudinalIdx + 1, numPartitions + latitudinalIdx, latitudinalIdx + 1);
			mesh.faces.add(face);
		}
		OBJFace face = new OBJFace(3, true, true);
		face.setVertex(0, northPoleIdx, numPartitions - 1, northPoleIdx);
		face.setVertex(1, numPartitions, 2 * numPartitions - 1, numPartitions);
		face.setVertex(2, 1, 2 * numPartitions, 1);
		mesh.faces.add(face);

		// Generate the sides.
		ArrayList<Integer> upperVertices = new ArrayList<Integer>();
		ArrayList<Integer> lowerVertices = new ArrayList<Integer>();
		int poleOffset = numPartitions;
		for (int longitudinalIdx = 1; longitudinalIdx < numStrips - 1; longitudinalIdx++) {
			upperVertices = new ArrayList<Integer>();
			lowerVertices = new ArrayList<Integer>();

			for (int latitudinalIdx = 1; latitudinalIdx <= numPartitions; latitudinalIdx++) {
				int upperVertexPosition = latitudinalIdx + (longitudinalIdx - 1) * numPartitions;
				int lowerVertexPosition = latitudinalIdx + (longitudinalIdx) * numPartitions;
				upperVertices.add(upperVertexPosition);
				lowerVertices.add(lowerVertexPosition);
			}

			for (int partitionIdx = 0; partitionIdx < numPartitions; partitionIdx++) {
				OBJFace lowerTriangle = new OBJFace(3, true, true);
				lowerTriangle.setVertex(0, upperVertices.get(partitionIdx % numPartitions),
						poleOffset + (longitudinalIdx - 1) * (numPartitions + 1) + partitionIdx,
						upperVertices.get(partitionIdx % numPartitions));
				lowerTriangle.setVertex(1, lowerVertices.get(partitionIdx % numPartitions),
						poleOffset + longitudinalIdx * (numPartitions + 1) + partitionIdx,
						lowerVertices.get(partitionIdx % numPartitions));
				lowerTriangle.setVertex(2, lowerVertices.get((partitionIdx + 1) % numPartitions),
						poleOffset + longitudinalIdx * (numPartitions + 1) + partitionIdx + 1,
						lowerVertices.get((partitionIdx + 1) % numPartitions));
				mesh.faces.add(lowerTriangle);

				OBJFace upperTriangle = new OBJFace(3, true, true);
				upperTriangle.setVertex(0, upperVertices.get(partitionIdx % numPartitions),
						poleOffset + (longitudinalIdx - 1) * (numPartitions + 1) + partitionIdx,
						upperVertices.get(partitionIdx % numPartitions));
				upperTriangle.setVertex(1, lowerVertices.get((partitionIdx + 1) % numPartitions),
						poleOffset + longitudinalIdx * (numPartitions + 1) + partitionIdx + 1,
						lowerVertices.get((partitionIdx + 1) % numPartitions));
				upperTriangle.setVertex(2, upperVertices.get((partitionIdx + 1) % numPartitions),
						poleOffset + (longitudinalIdx - 1) * (numPartitions + 1) + partitionIdx + 1,
						upperVertices.get((partitionIdx + 1) % numPartitions));
				mesh.faces.add(upperTriangle);
			}
		}

		// Generate the south cap.
		for (int idx = 0; idx < lowerVertices.size() - 1; idx++) {
			face = new OBJFace(3, true, true);
			face.setVertex(0, lowerVertices.get(idx) + 1, poleOffset + (numStrips - 2) * (numPartitions + 1) + idx + 1,
					lowerVertices.get(idx) + 1);
			face.setVertex(1, lowerVertices.get(idx), poleOffset + (numStrips - 2) * (numPartitions + 1) + idx,
					lowerVertices.get(idx));
			face.setVertex(2, southPoleIdx, poleOffset + (numStrips - 1) * (numPartitions + 1) + idx, southPoleIdx);
			mesh.faces.add(face);
		}
		face = new OBJFace(3, true, true);
		face.setVertex(0, lowerVertices.get(0), poleOffset + (numStrips - 2) * (numPartitions + 1) + numPartitions,
				lowerVertices.get(0));
		face.setVertex(1, lowerVertices.get(numPartitions - 1),
				poleOffset + (numStrips - 2) * (numPartitions + 1) + numPartitions - 1,
				lowerVertices.get(numPartitions - 1));
		face.setVertex(2, southPoleIdx, poleOffset + (numStrips - 1) * (numPartitions + 1) + numPartitions - 1,
				southPoleIdx);
		mesh.faces.add(face);
	}

	/**
	 * A function to convert spherical coordinates into Cartesian coordinates.
	 * 
	 * @param radius
	 *            the radius of the sphere.
	 * @param horizontalAngle
	 *            the radial angle.
	 * @param verticalAngle
	 *            the polar angle.
	 * @return double[] containing the Cartesian coordinates.
	 */
	private double[] sphericalToCartesian(int radius, double horizontalAngle, double verticalAngle) {
		double[] coordinateArray = new double[3];
		coordinateArray[X] = -radius * Math.sin(verticalAngle) * Math.sin(horizontalAngle);
		coordinateArray[Y] = radius * Math.cos(verticalAngle);
		coordinateArray[Z] = -radius * Math.sin(verticalAngle) * Math.cos(horizontalAngle);
		return coordinateArray;
	}

}
