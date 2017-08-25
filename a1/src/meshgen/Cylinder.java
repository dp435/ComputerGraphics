package meshgen;

import math.Vector2;
import math.Vector3;

/**
 * This class is responsible for generating a cylinder mesh.
 * 
 * @author Daniel Park (dp435)
 */

public class Cylinder {
	private static final int RADIUS = 1; // the radius of the cylinder
	private static final int HEIGHT = 2; // the height of the cylinder
	private int divisionsU; // number of radial divisions
	private double angularDifference;
	private OBJMesh mesh;
	private double texturePartition;
	private int upperCentroidIdx;
	private int upperNormalIdx;
	private int lowerCentroidIdx;
	private int lowerNormalIdx;
	private int topCentroidTextureIdx;
	private int bottomCentroidTextureIdx;
	private int lidTextureIdxStart;

	/**
	 * Constructor for Cylinder.
	 * 
	 * @param divisionsU
	 *            number of radial divisions.
	 */
	public Cylinder(int divisionsU) {
		this.divisionsU = divisionsU;
		angularDifference = 2.0 * Math.PI / divisionsU;
		mesh = new OBJMesh();
		texturePartition = 1.0 / divisionsU;
	}

	/**
	 * Method to generate the cylinder mesh.
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
		for (int vertexIdx = 0; vertexIdx < divisionsU; vertexIdx++) {
			float x = (float) (-RADIUS * Math.sin(vertexIdx * angularDifference));
			float z = (float) (-RADIUS * Math.cos(vertexIdx * angularDifference));

			Vector3 upperVertex = new Vector3(x, (HEIGHT / 2.0f), z);
			Vector3 lowerVertex = new Vector3(x, -(HEIGHT / 2.0f), z);
			Vector3 normal = new Vector3(x, 0.0f, z);

			mesh.positions.add(upperVertex);
			mesh.uvs.add(new Vector2((float) (vertexIdx * texturePartition), 0.5f));
			mesh.positions.add(lowerVertex);
			mesh.uvs.add(new Vector2((float) (vertexIdx * texturePartition), 0.0f));
			mesh.normals.add(normal);
		}
		mesh.uvs.add(new Vector2(1.0f, 0.5f));
		mesh.uvs.add(new Vector2(1.0f, 0.0f));

		Vector3 upperCentroid = new Vector3(0.0f, (HEIGHT / 2.0f), 0.0f);
		mesh.positions.add(upperCentroid);
		Vector3 upperCentroidNormal = new Vector3(0.0f, (HEIGHT / 2.0f), 0.0f);
		mesh.normals.add(upperCentroidNormal);
		upperCentroidIdx = mesh.positions.size() - 1;
		upperNormalIdx = mesh.normals.size() - 1;

		Vector3 bottomCentroid = new Vector3(0.0f, -(HEIGHT / 2.0f), 0.0f);
		mesh.positions.add(bottomCentroid);
		Vector3 lowerCentroidNormal = new Vector3(0.0f, -(HEIGHT / 2.0f), 0.0f);
		mesh.normals.add(lowerCentroidNormal);
		lowerCentroidIdx = mesh.positions.size() - 1;
		lowerNormalIdx = mesh.normals.size() - 1;

		mesh.uvs.add(new Vector2(0.75f, 0.75f));
		topCentroidTextureIdx = mesh.uvs.size() - 1;
		mesh.uvs.add(new Vector2(0.25f, 0.75f));
		bottomCentroidTextureIdx = mesh.uvs.size() - 1;
		lidTextureIdxStart = mesh.uvs.size();

		for (int textureIdx = 0; textureIdx < divisionsU; textureIdx++) {
			float ut = (float) (-0.25 * Math.sin(textureIdx * angularDifference) + 0.75f);
			float vt = (float) (0.25 * Math.cos(textureIdx * angularDifference) + 0.75f);
			mesh.uvs.add(new Vector2(ut, vt));

			float ub = (float) (-0.25 * Math.sin(textureIdx * angularDifference) + 0.25f);
			float vb = (float) (-0.25 * Math.cos(textureIdx * angularDifference) + 0.75f);
			mesh.uvs.add(new Vector2(ub, vb));
		}
	}

	/**
	 * Helper method to construct the faces of the mesh.
	 */
	private void generateFaces() {
		Integer upperEndpoint = null;
		Integer lowerEndpoint = null;
		Integer endpointNormal = null;

		// Generate the sides.
		for (int idx = 0; idx < divisionsU - 1; idx++) {
			OBJFace leftTriangle = new OBJFace(3, true, true);
			leftTriangle.setVertex(0, 2 * idx, 2 * idx, idx);
			leftTriangle.setVertex(1, 2 * idx + 1, 2 * idx + 1, idx);
			leftTriangle.setVertex(2, 2 * idx + 2, 2 * idx + 2, idx + 1);
			mesh.faces.add(leftTriangle);

			OBJFace rightTriangle = new OBJFace(3, true, true);
			rightTriangle.setVertex(0, 2 * idx + 2, 2 * idx + 2, idx + 1);
			rightTriangle.setVertex(1, 2 * idx + 1, 2 * idx + 1, idx);
			rightTriangle.setVertex(2, 2 * idx + 3, 2 * idx + 3, idx + 1);
			mesh.faces.add(rightTriangle);

			upperEndpoint = 2 * idx + 2;
			lowerEndpoint = 2 * idx + 3;
			endpointNormal = idx + 1;
		}

		OBJFace leftTriangle = new OBJFace(3, true, true);
		leftTriangle.setVertex(0, upperEndpoint, upperEndpoint, endpointNormal);
		leftTriangle.setVertex(1, lowerEndpoint, lowerEndpoint, endpointNormal);
		leftTriangle.setVertex(2, 0, upperEndpoint + 2, 0);
		mesh.faces.add(leftTriangle);

		OBJFace rightTriangle = new OBJFace(3, true, true);
		rightTriangle.setVertex(0, lowerEndpoint, lowerEndpoint, endpointNormal);
		rightTriangle.setVertex(1, 1, lowerEndpoint + 2, 0);
		rightTriangle.setVertex(2, 0, upperEndpoint + 2, 0);
		mesh.faces.add(rightTriangle);

		// Generate top cap.
		for (int idx = 0; idx < divisionsU; idx++) {
			OBJFace topCap = new OBJFace(3, true, true);
			topCap.setVertex(0, upperCentroidIdx, topCentroidTextureIdx, upperNormalIdx);
			topCap.setVertex(1, (2 * idx) % (divisionsU * 2), lidTextureIdxStart + ((2 * idx) % (divisionsU * 2)),
					upperNormalIdx);
			topCap.setVertex(2, (2 * idx + 2) % (divisionsU * 2),
					lidTextureIdxStart + ((2 * idx + 2) % (divisionsU * 2)), upperNormalIdx);
			mesh.faces.add(topCap);
		}

		// Generate bottom cap.
		for (int idx = 0; idx < divisionsU; idx++) {
			OBJFace topCap = new OBJFace(3, true, true);
			topCap.setVertex(2, lowerCentroidIdx, bottomCentroidTextureIdx, lowerNormalIdx);
			topCap.setVertex(1, (2 * idx + 1) % (divisionsU * 2), lidTextureIdxStart + (2 * idx + 1) % (divisionsU * 2),
					lowerNormalIdx);
			topCap.setVertex(0, (2 * idx + 3) % (divisionsU * 2), lidTextureIdxStart + (2 * idx + 3) % (divisionsU * 2),
					lowerNormalIdx);
			mesh.faces.add(topCap);
		}
	}

}
