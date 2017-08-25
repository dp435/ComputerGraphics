package ray2;

import egl.math.Colord;

/**
 * Represents a ray that must be traced from a surface
 * and the factor by which to scale its contribution.
 * 
 * @author pramook
 */
public class RayRecord {
	/**
	 * The additional ray to be traced.
	 */
	public final Ray ray;
	
	/**
	 * The factor by which to scale the ray's contribution.
	 */
	public final Colord factor;
	
	public RayRecord()
	{
		ray = new Ray();
		factor = new Colord();
	}	
}
