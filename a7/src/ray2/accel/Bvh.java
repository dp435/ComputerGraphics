
package ray2.accel;

import java.util.Arrays;
import java.util.Comparator;

import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.surface.Surface;

/**
 * Class for Axis-Aligned-Bounding-Box to speed up the intersection look up
 * time.
 *
 * @author ss932, pramook
 */
public class Bvh implements AccelStruct {
	/**
	 * A shared surfaces array that will be used across every node in the tree.
	 */
	private Surface[] surfaces;

	/**
	 * A comparator class that can sort surfaces by x, y, or z coordinate. See
	 * the subclass declaration below for details.
	 */
	static MyComparator cmp = new MyComparator();

	/** The root of the BVH tree. */
	BvhNode root;

	public Bvh() {
	}

	/**
	 * Set outRecord to the first intersection of ray with the scene. Return
	 * true if there was an intersection and false otherwise. If no intersection
	 * was found outRecord is unchanged.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @param anyIntersection
	 *            if true, will immediately return when found an intersection
	 * @return true if and intersection is found.
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
		return intersectHelper(root, outRecord, rayIn, anyIntersection);
	}

	/**
	 * A helper method to the main intersect method. It finds the intersection
	 * with any of the surfaces under the given BVH node.
	 * 
	 * @param node
	 *            a BVH node that we would like to find an intersection with
	 *            surfaces under it
	 * @param outRecord
	 *            the output InsersectionMethod
	 * @param rayIn
	 *            the ray to intersect
	 * @param anyIntersection
	 *            if true, will immediately return when found an intersection
	 * @return true if an intersection is found with any surface under the given
	 *         node
	 */
	private boolean intersectHelper(BvhNode node, IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
		// TODO#A7: fill in this function.
		// Hint: For a leaf node, use a normal linear search. Otherwise, search
		// in the left and right children.
		// Another hint: save time by checking if the ray intersects the node
		// first before checking the childrens.

		// Case 1: check if ray intersects node.
		if (!node.intersects(rayIn))
			return false;

		// Case 2: check leaf nodes.
		if (node.isLeaf()) {
			boolean intersectionFound = false;
			IntersectionRecord tmpRecord = new IntersectionRecord();
			// temp ray used to udpate endpoint data.
			Ray tmpRay = new Ray(rayIn);
			for (int i = node.surfaceIndexStart; i < node.surfaceIndexEnd; i++) {
				if (surfaces[i].intersect(tmpRecord, tmpRay) && tmpRecord.t < tmpRay.end) {
					if (anyIntersection)
						return true;
					else {
						intersectionFound = true;
						tmpRay.end = tmpRecord.t;
						if (outRecord != null)
							outRecord.set(tmpRecord);
					}
				}
			}
			return intersectionFound;
		}

		// Case 3: recurse on child nodes.
		if (anyIntersection) {
			return intersectHelper(node.child[0], outRecord, rayIn, anyIntersection)
					|| intersectHelper(node.child[1], outRecord, rayIn, anyIntersection);
		} else {
			IntersectionRecord leftChildRecord = new IntersectionRecord();
			IntersectionRecord rightChildRecord = new IntersectionRecord();
			boolean leftIntersection = intersectHelper(node.child[0], leftChildRecord, rayIn, anyIntersection);
			boolean rightIntersection = intersectHelper(node.child[1], rightChildRecord, rayIn, anyIntersection);

			if (leftChildRecord.surface != null
					&& (leftChildRecord.t < rightChildRecord.t || rightChildRecord.surface == null))
				outRecord.set(leftChildRecord);
			else
				outRecord.set(rightChildRecord);
			return leftIntersection || rightIntersection;
		}

	}

	@Override
	public void build(Surface[] surfaces) {
		this.surfaces = surfaces;
		root = createTree(0, surfaces.length);
	}

	/**
	 * Create a BVH [sub]tree. This tree node will be responsible for storing
	 * and processing surfaces[start] to surfaces[end-1]. If the range is small
	 * enough, this will create a leaf BvhNode. Otherwise, the surfaces will be
	 * sorted according to the axis of the axis-aligned bounding box that is
	 * widest, and split into 2 children.
	 * 
	 * @param start
	 *            The start index of surfaces
	 * @param end
	 *            The end index of surfaces
	 */
	private BvhNode createTree(int start, int end) {
		// TODO#A7: fill in this function.

		Vector3d minB = new Vector3d(Double.POSITIVE_INFINITY);
		Vector3d maxB = new Vector3d(Double.NEGATIVE_INFINITY);
		// ==== Step 1 ====
		// Find out the BIG bounding box enclosing all the surfaces in the range
		// [start, end)
		// and store them in minB and maxB.
		// Hint: To find the bounding box for each surface, use getMinBound()
		// and getMaxBound() */
		for (int i = start; i < end; i++) {
			Vector3d currentMinBound = surfaces[i].getMinBound();
			Vector3d currentMaxBound = surfaces[i].getMaxBound();
			if (currentMinBound.x < minB.x)
				minB.x = currentMinBound.x;
			if (currentMinBound.y < minB.y)
				minB.y = currentMinBound.y;
			if (currentMinBound.z < minB.z)
				minB.z = currentMinBound.z;
			if (currentMaxBound.x > maxB.x)
				maxB.x = currentMaxBound.x;
			if (currentMaxBound.y > maxB.y)
				maxB.y = currentMaxBound.y;
			if (currentMaxBound.z > maxB.z)
				maxB.z = currentMaxBound.z;
		}

		// ==== Step 2 ====
		// Check for the base case.
		// If the range [start, end) is small enough (e.g. less than or equal to
		// 10), just return a new leaf node.
		if (end - start <= 10)
			return new BvhNode(minB, maxB, null, null, start, end);

		// ==== Step 3 ====
		// Figure out the widest dimension (x or y or z).
		// If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z,
		// set widestDim = 2.
		int widestDim = 0;
		double xWidth = maxB.x - minB.x;
		double yWidth = maxB.y - minB.y;
		double zWidth = maxB.z - minB.z;

		if (xWidth > yWidth && xWidth > zWidth)
			widestDim = 0;
		else if (yWidth > xWidth && yWidth > zWidth)
			widestDim = 1;
		else
			widestDim = 2;

		// ==== Step 4 ====
		// Sort surfaces according to the widest dimension.
		cmp.setIndex(widestDim);
		Arrays.sort(surfaces, start, end, cmp);

		// ==== Step 5 ====
		// Recursively create left and right children.
		int midpoint = (start + end) / 2;
		BvhNode leftChild = createTree(start, midpoint);
		BvhNode rightChild = createTree(midpoint, end);

		return new BvhNode(minB, maxB, leftChild, rightChild, start, end);
	}

}

/**
 * A subclass that compares the average position two surfaces by a given axis.
 * Use the setIndex(i) method to select which axis should be considered. i=0 ->
 * x-axis, i=1 -> y-axis, and i=2 -> z-axis.
 *
 */
class MyComparator implements Comparator<Surface> {
	int index;

	public MyComparator() {
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int compare(Surface o1, Surface o2) {
		double v1 = o1.getAveragePosition().get(index);
		double v2 = o2.getAveragePosition().get(index);
		if (v1 < v2)
			return 1;
		if (v1 > v2)
			return -1;
		return 0;
	}

}
