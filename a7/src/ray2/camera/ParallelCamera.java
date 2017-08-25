package ray2.camera;

import ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a camera with a parallel view.
 * For this camera, all rays should start at the viewing window and have a direction
 * parallel to viewDir. The viewing window's center is defined by viewPoint; all rays
 * will have their origins offset from this point. Note that the viewing window's normal
 * is defined by projNormal, which may be different than the viewing direction.
 *
 */
public class ParallelCamera extends Camera {
	
	/*
	 * Derived values that are computed before ray generation.
	 * basisU, basisV, and basisW form an orthonormal basis.
	 * 
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

    	if (projNormal.len() == 0) {
    		projNormal.set(viewDir);
    	}
	    basisW.set(projNormal);
	    if (basisW.dot(viewDir) > 0)
	    	basisW.negate();
	    basisW.normalize();
	    
	    basisU.set(viewUp).cross(basisW).normalize();
	    basisV.set(basisW).cross(basisU).normalize();
	    
	    centerDir.set(viewDir).normalize();
	    
    }


    /**
     * Set outRay to be a ray originating from a point in the image.
     *
     * @param outRay The output ray (not normalized)
     * @param inU The u coord of the image point (range [0,1])
     * @param inV The v coord of the image point (range [0,1])
     */
	@Override
	public void getRay(Ray outRay, double inU, double inV) {

	    double u = inU * 2 - 1;
	    double v = inV * 2 - 1;
	    
	    // Set the output ray
	    outRay.origin.set(viewPoint)
	    		     .addMultiple(u * viewWidth / 2, basisU)
	    		     .addMultiple(v * viewHeight /2, basisV);

	    outRay.direction.set(centerDir);
	    
	    outRay.makeOffsetRay();
	}
	
}