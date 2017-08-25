package ray2.camera;

import ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a camera with perspective view.
 * For this camera, the view window corresponds to a rectangle on a
 * plane perpendicular to viewDir but at distance projDistance from
 * viewPoint in the direction of viewDir. A ray with its origin at viewPoint
 * going in the direction of viewDir should intersect the center
 * of the image plane. Given u and v, you should compute a point on the
 * rectangle corresponding to (u,v), and create a ray from viewPoint that
 * passes through the computed point.
 */
public class PerspectiveCamera extends Camera {
  
  protected double projDistance = 1.0;
  public void setprojDistance(double projDistance) { this.projDistance = projDistance; }
  
  /*
   * Derived values that are computed before ray generation.
   * basisU, basisV, and basisW form an orthonormal basis.
   * basisW is parallel to projNormal.
   */
  protected final Vector3d basisU = new Vector3d();
  protected final Vector3d basisV = new Vector3d();
  protected final Vector3d basisW = new Vector3d();
  protected final Vector3d centerDir = new Vector3d();
  
  /**
   * Initialize the derived view variables to prepare for using the camera.
   */
  public void init() {
  	basisW.set(viewDir).negate().normalize();
    basisU.set(viewUp).cross(basisW).normalize();
    basisV.set(basisW).cross(basisU).normalize();
    centerDir.set(viewDir).normalize().mul(projDistance);  
  }
  
  /**
   * Set outRay to be a ray from the camera through a point in the image.
   *
   * @param outRay The output ray (not normalized)
   * @param inU The u coord of the image point (range [0,1])
   * @param inV The v coord of the image point (range [0,1])
   */
  public void getRay(Ray outRay, double inU, double inV) {

    double u = inU * 2 - 1;
    double v = inV * 2 - 1;
    
    // Set the output ray
    outRay.origin.set(viewPoint);
    outRay.direction.set(centerDir)
     				.addMultiple(u * viewWidth / 2, basisU)
     				.addMultiple(v * viewHeight /2, basisV)
     				.normalize();
    
    outRay.makeOffsetRay();
  }
}