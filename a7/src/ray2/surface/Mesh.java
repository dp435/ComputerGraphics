package ray2.surface;

import java.io.IOException;
import java.util.ArrayList;

import ray2.mesh.OBJFace;
import ray2.mesh.OBJMesh;
import ray2.mesh.OBJMesh.OBJFileFormatException;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.RayTracer;

/**
 * An interface between a MeshData and the ray tracer. When the Scene calls
 * appendRenderableSurfaces on this object, it appends all Triangles on the
 * mesh onto the given ArrayList. This way, the Scene has direct access to
 * all intersectable Surfaces in the scene.
 * 
 * @author eschweic
 *
 */
public class Mesh extends Surface {

	/** The underlying data of this Mesh. */
	private OBJMesh mesh = null;

	/**
	 * Default constructor; creates an empty mesh.
	 */
	public Mesh() { }

	/**
	 * Construct a Mesh from an existing MeshData.
	 * @param newMesh an existing MeshData.
	 */
	public Mesh(OBJMesh newMesh) {
		mesh = newMesh;
	}
	
	/**
	 * Set the data in this mesh to the data of a mesh on disk.
	 * @param fileName the name of a .obj file on disk.
	 */
	public void setData(String fileName) throws OBJFileFormatException, IOException {
		System.out.println("Loading " + RayTracer.sceneWorkspace.resolve(fileName));
		this.mesh = new OBJMesh(RayTracer.sceneWorkspace.resolve(fileName));
	}
	
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {	return false; }
	public void computeBoundingBox() {	}

	
	public void appendRenderableSurfaces (ArrayList<Surface> in) {

		for (OBJFace f : mesh.faces) {
			Triangle t = new Triangle(this,f,shader);
			t.setShader(this.shader);
			t.setTransformation(this.tMat, this.tMatInv, this.tMatTInv);
			
			t.computeBoundingBox();
			in.add(t);
		}
	} 
	
	public OBJMesh getMesh() {
		return this.mesh;
	}
}
