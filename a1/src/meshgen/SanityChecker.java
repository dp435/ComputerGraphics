package meshgen;

import java.io.IOException;

import meshgen.OBJMesh.OBJFileFormatException;

/**
 * This class is performs a sanity check.
 * 
 * @author Daniel Park (dp435)
 */

public class SanityChecker {

	public static void main(String[] args) throws OBJFileFormatException, IOException {
		testAll();
	}

	/**
	 * This method compares the generated .obj files against the reference
	 * solutions.
	 */
	private static void testAll() throws OBJFileFormatException, IOException {
		Cylinder cylinderMesh = new Cylinder(32);
		cylinderMesh.generateMesh();
		OBJMesh cylinderReference = new OBJMesh();
		cylinderReference.parseOBJ("data/cylinder-reference.obj");
		System.out.println("CYLINDER TEST PASSED: " + OBJMesh.compare(cylinderReference, cylinderMesh.getMesh(), true));

		Sphere sphereMesh = new Sphere(32, 16);
		sphereMesh.generateMesh();
		OBJMesh sphereReference = new OBJMesh();
		sphereReference.parseOBJ("data/sphere-reference.obj");
		System.out.println("SPHERE TEST PASSED: " + OBJMesh.compare(sphereReference, sphereMesh.getMesh(), true));

		Torus torusMesh = new Torus(0.25, 32, 16);
		torusMesh.generateMesh();
		OBJMesh torusReference = new OBJMesh();
		torusReference.parseOBJ("data/torus-reference.obj");
		System.out.println("TORUS TEST PASSED: " + OBJMesh.compare(torusReference, torusMesh.getMesh(), true));

		NormalCalculator bunnyCalculator = new NormalCalculator("data/bunny-nonorms.obj");
		bunnyCalculator.calculateNormals();
		OBJMesh bunnyReference = new OBJMesh();
		bunnyReference.parseOBJ("data/bunny-norms-reference.obj");
		System.out.println("BUNNY TEST PASSED: " + OBJMesh.compare(bunnyReference, bunnyCalculator.getMesh(), true));

		// NormalCalculator horseCalculator = new
		// NormalCalculator("data/horse-nonorms.obj");
		// horseCalculator.calculateNormals();
		// OBJMesh horseReference = new OBJMesh();
		// horseReference.parseOBJ("data/horse-norms-reference.obj");
		// System.out.println("HORSE TEST PASSED: " +
		// OBJMesh.compare(horseReference, horseCalculator.getMesh(), true));
	}

}
