package meshgen;

import java.io.IOException;
import java.util.ArrayList;

import math.Vector3;
import meshgen.OBJMesh.OBJFileFormatException;

/**
 * This class is responsible for calculate the normals for a given mesh.
 * 
 * @author Daniel Park (dp435)
 */

public class NormalCalculator {

	private String infile;
	private OBJMesh mesh;

	/**
	 * Constructor for NormalCalculator.
	 * 
	 * @param infile
	 *            the mesh file.
	 */
	public NormalCalculator(String infile) throws OBJFileFormatException, IOException {
		this.infile = infile;
		mesh = new OBJMesh();
		mesh.parseOBJ(infile);
	}

	/**
	 * Method to calculate the normals.
	 */
	public void calculateNormals() {
		// initialize all normals as zeroes.
		mesh.normals.clear();
		for (int normalIdx = 0; normalIdx < mesh.positions.size(); normalIdx++) {
			mesh.normals.add(new Vector3(0.0f, 0.0f, 0.0f));
		}

		for (OBJFace triangle : mesh.faces) {

			// set normal index as same as position index.
			triangle.normals = triangle.positions;

			// calculate the normals at each vertex.
			Vector3 U = mesh.getPosition(triangle, 1).clone().sub(mesh.getPosition(triangle, 0));
			Vector3 V = mesh.getPosition(triangle, 2).clone().sub(mesh.getPosition(triangle, 0));
			Vector3 product = U.clone().cross(V).normalize();

			mesh.getNormal(triangle, 0).add(product.x, product.y, product.z);
			mesh.getNormal(triangle, 1).add(product.x, product.y, product.z);
			mesh.getNormal(triangle, 2).add(product.x, product.y, product.z);
		}

		// normalize all normal arrays.
		for (Vector3 normalArray : mesh.normals) {
			normalArray.normalize();
		}
	}

	/**
	 * Getter method for the mesh.
	 * 
	 * @return the mesh.
	 */
	public OBJMesh getMesh() {
		return mesh;
	}

}
