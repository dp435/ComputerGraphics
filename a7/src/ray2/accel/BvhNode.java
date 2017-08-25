package ray2.accel;

import ray2.Ray;
import egl.math.Vector3d;

/**
 * A class representing a node in a bounding volume hierarchy.
 * 
 * @author pramook 
 */
public class BvhNode {

	/** The current bounding box for this tree node.
	 *  The bounding box is described by 
	 *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z).
	 */
	public final Vector3d minBound, maxBound;
	
	/**
	 * The array of children.
	 * child[0] is the left child.
	 * child[1] is the right child.
	 */
	public final BvhNode child[];

	/**
	 * The index of the first surface under this node. 
	 */
	public int surfaceIndexStart;
	
	/**
	 * The index of the surface next to the last surface under this node.	 
	 */
	public int surfaceIndexEnd; 
	
	/**
	 * Default constructor
	 */
	public BvhNode()
	{
		minBound = new Vector3d();
		maxBound = new Vector3d();
		child = new BvhNode[2];
		child[0] = null;
		child[1] = null;		
		surfaceIndexStart = -1;
		surfaceIndexEnd = -1;
	}
	
	/**
	 * Constructor where the user can specify the fields.
	 * @param minBound
	 * @param maxBound
	 * @param leftChild
	 * @param rightChild
	 * @param start
	 * @param end
	 */
	public BvhNode(Vector3d minBound, Vector3d maxBound, BvhNode leftChild, BvhNode rightChild, int start, int end) 
	{
		this.minBound = new Vector3d();
		this.minBound.set(minBound);
		this.maxBound = new Vector3d();
		this.maxBound.set(maxBound);
		this.child = new BvhNode[2];
		this.child[0] = leftChild;
		this.child[1] = rightChild;		   
		this.surfaceIndexStart = start;
		this.surfaceIndexEnd = end;
	}
	
	/**
	 * @return true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return child[0] == null && child[1] == null; 
	}
	
	/** 
	 * Check if the ray intersects the bounding box.
	 * @param ray
	 * @return true if ray intersects the bounding box
	 */
	public boolean intersects(Ray ray) {
		// TODO#A7: fill in this function.
		Vector3d minPt = new Vector3d(minBound);
		Vector3d maxPt = new Vector3d(maxBound);
		
		Vector3d origin = ray.origin;
		Vector3d dir = ray.direction;
		
		double tMin = ray.start;
		double tMax = ray.end;
		
		double a;
		double txMin, txMax;
		
		a = 1/dir.x;
		if (a >= 0) {
			txMin = a * (minPt.x - origin.x);
			txMax = a * (maxPt.x - origin.x);
		} else {
			txMin = a * (maxPt.x - origin.x);
			txMax = a * (minPt.x - origin.x);
		}
		if (tMin > txMax || txMin > tMax)
			return false;
		if (txMin > tMin)
			tMin = txMin;
		if (txMax < tMax)
			tMax = txMax;
		
		double tyMin, tyMax;
		a = 1/dir.y;
		if (a >= 0) {
			tyMin = a * (minPt.y - origin.y);
			tyMax = a * (maxPt.y - origin.y);
		} else {
			tyMin = a * (maxPt.y - origin.y);
			tyMax = a * (minPt.y - origin.y);
		}
		if (tMin > tyMax || tyMin > tMax)
			return false;
		if (tyMin > tMin)
			tMin = tyMin;
		if (tyMax < tMax)
			tMax = tyMax;
		
		double tzMin, tzMax;
		a = 1/dir.z;
		if (a >= 0) {
			tzMin = a * (minPt.z - origin.z);
			tzMax = a * (maxPt.z - origin.z);
		} else {
			tzMin = a * (maxPt.z - origin.z);
			tzMax = a * (minPt.z - origin.z);
		}
		if (tMin > tzMax || tzMin > tMax)
			return false;
		if (tzMin > tMin)
			tMin = tzMin;
		if (tzMax < tMax)
			tMax = tzMax;
		
		return true;
	}
}
