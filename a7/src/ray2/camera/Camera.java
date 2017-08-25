package ray2.camera;

import ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a camera object. This class is responsible for generating rays that are intersected
 * with the scene.
 */

public abstract class Camera {
	/*
	 * Fields that are read in from the input file to describe the camera.
	 * You'll probably want to store some derived values to make ray generation easy.
	 */
	
	/**
	 * The position of the eye.
	 */
	protected final Vector3d viewPoint = new Vector3d();
	public void setViewPoint(Vector3d viewPoint) { this.viewPoint.set(viewPoint); }
	
	/**
	 * The direction the eye is looking.
	 */
	protected final Vector3d viewDir = new Vector3d(0, 0, -1);
	public void setViewDir(Vector3d viewDir) { this.viewDir.set(viewDir); }
	
	/**
	 * The upwards direction from the viewer's perspective.
	 */
	protected final Vector3d viewUp = new Vector3d(0, 1, 0);
	public void setViewUp(Vector3d viewUp) { this.viewUp.set(viewUp); }
	
	/**
	 * The normal of the image plane. By default this should be set to be the
	 * same as the view direction.
	 */
	protected final Vector3d projNormal = new Vector3d();
	public void setProjNormal(Vector3d projNormal) { this.projNormal.set(projNormal); }

	
	/**
	 * The width of the viewing window.
	 */
	protected double viewWidth = 1.0;
	public void setViewWidth(double viewWidth) { this.viewWidth = viewWidth; }
	
	/**
	 * The height of the viewing window.
	 */
	protected double viewHeight = 1.0;
	public void setViewHeight(double viewHeight) { this.viewHeight = viewHeight; }
	
	/**
	 * Generate a ray that points out into the scene for the given (u,v) coordinate.
	 * This coordinate corresponds to a point on the viewing window, where (0,0) is the
	 * lower left corner and (1,1) is the upper right.
	 * @param outRay A space to return the output ray
	 * @param u The horizontal coordinate (0 is left, 1 is right)
	 * @param v The vertical coordinate (0 is bottom, 1 is top)
	 */
	public abstract void getRay(Ray outRay, double u, double v);
	
	/**
	* Initialize method: initialize the orthonormal basis vectors
	*/
	public abstract void init();
	
	/**
	 * Code for unit testing of cameras.
	 */
	public void testGetRay(Ray correctRay, double u, double v) {
		Ray testRay = new Ray();
		getRay(testRay, u, v);
		if (!raysEquivalent(testRay, correctRay)) {
			 System.err.println("test failed");
			 System.err.println("testRay: " + testRay.origin + " + t * " + testRay.direction);
			 System.err.println("correctRay: " + correctRay.origin + " + t * " + correctRay.direction);
			 System.exit(-1);
		}
	}
	
	private static boolean raysEquivalent(Ray ray1, Ray ray2) {
		Vector3d dir1 = new Vector3d(ray1.direction);
		Vector3d dir2 = new Vector3d(ray2.direction);
		dir1.normalize();
		dir2.normalize();
		dir1.sub(dir2);
		return ray1.origin.dist(ray2.origin) < 1e-6 && dir1.len() < 1e-6; 
	}
	
}
