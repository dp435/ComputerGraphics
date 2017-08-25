package meshgen;

import java.io.IOException;
import java.util.Arrays;

import meshgen.OBJMesh.OBJFileFormatException;

/**
 * The top-level class.
 * 
 * @author Daniel Park (dp435)
 */

public class MeshGen {

	/**
	 * The main method for MeshGen.
	 * 
	 * This method is responsible for reading in the system arguments,
	 * generating the necessary meshes, and writing the results back to disk.
	 */
	public static void main(String[] args) throws IOException {
		OBJMesh result = null;
		Parser parser = new Parser(args);

		if (parser.mode.equals("GENERATE")) {
			if (parser.shape.equals("cylinder")) {
				Cylinder cylinderMesh = new Cylinder(parser.divisionsU);
				cylinderMesh.generateMesh();
				result = cylinderMesh.getMesh();
			} else if (parser.shape.equals("sphere")) {
				Sphere sphereMesh = new Sphere(parser.divisionsU, parser.divisionsV);
				sphereMesh.generateMesh();
				result = sphereMesh.getMesh();
			} else if (parser.shape.equals("torus")) {
				Torus torusMesh = new Torus(parser.minorRadius, parser.divisionsU, parser.divisionsV);
				torusMesh.generateMesh();
				result = torusMesh.getMesh();
			}
		} else if (parser.mode.equals("CALCULATE")) {
			NormalCalculator calculator = new NormalCalculator(parser.infile);
			calculator.calculateNormals();
			result = calculator.getMesh();
		}

		result.writeOBJ(parser.outfile);
	}
}
