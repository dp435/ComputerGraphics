package ray2.light;

import java.util.Random;

import egl.math.Vector3d;

/**
 * This class represents an area source that is rectangular, specified by a
 * frame in the same way as a camera. It has constant radiance across the whole
 * surface.
 *
 * @author srm, zechenz
 */
public class RectangleLight extends Light {

	/** Where the light is located in space. */
	public final Vector3d position = new Vector3d();

	public void setPosition(Vector3d position) {
		this.position.set(position);
	}

	/** The direction the light is facing. */
	protected final Vector3d normalDir = new Vector3d(0, 0, -1);

	public void setNormalDir(Vector3d normalDir) {
		this.normalDir.set(normalDir);
	}

	/** The upwards direction, which is aligned with the light's height axis. */
	protected final Vector3d upDir = new Vector3d(0, 1, 0);

	public void setUpDir(Vector3d upDir) {
		this.upDir.set(upDir);
	}

	/** The height of the source, in world units. */
	protected double height = 1.0;

	public void setHeight(double height) {
		this.height = height;
	}

	/** The width of the source, in world units. */
	protected double width = 1.0;

	public void setWidth(double width) {
		this.width = width;
	}

	/*
	 * TODO#A7: declare necessary variables e.g. the orthonormal basis vectors
	 * for the rect area light
	 */
	protected final Vector3d U = new Vector3d();
	protected final Vector3d V = new Vector3d();
	protected final Vector3d W = new Vector3d();
	private boolean isInitialized = false;

	/**
	 * Initialize the derived view variables to prepare for using the camera.
	 */
	public void init() {
		// TODO#A7: Fill in this function
		// 1) Set the 3 basis vectors in the orthonormal basis,
		// based on normalDir and upDir
		// 2) Set up the helper variables if needed
		W.set(normalDir.clone().negate()).normalize();
		U.set(normalDir.clone().cross(upDir)).normalize();
		V.set(W.clone().cross(U));
		isInitialized = true;
	}

	/**
	 * Sample the illumination due to this light source at a given shading
	 * point. A rectantle light provides illumination from a range of
	 * directions; a direction is chosen by selecting a point on the source. A
	 * rectangle source is one-sided; it provides no illumination to points that
	 * are behind it.
	 *
	 * lRec.direction is the direction from the shading point to the source
	 * lRec.distance is the distance between the shading point and the source
	 * lRec.attenuation is the inverse square of the distance to the source
	 * lRec.probability is a probability density over the source's area.
	 *
	 * @param record
	 *            the record where the output is written:
	 * @param shadingPoint
	 *            the surface point where illumination is being computed
	 */
	@Override
	public void sample(LightSamplingRecord lRec, Vector3d shadingPoint) {
		// TODO#A7: Fill in this function
		// 1. sample light source point on the rectangle area light in
		// uniform-random fashion
		// 2. compute the l vector, i.e. the direction the light incidents on
		// the shading point
		// 3. compute the distance between light point and shading point, and
		// get attenuation
		// 4. compute the probablity this light point is sampled, which is used
		// for Monte-Carlo integration
		// 5. write relevant info to LightSamplingRecord object
		if (!isInitialized)
			init();

		double max_X = width / 2.0;
		double min_X = -width / 2.0;

		double max_Y = height / 2.0;
		double min_Y = -height / 2.0;

		Random rand = new Random();
		double randX = min_X + (max_X - min_X) * rand.nextDouble();
		double randY = min_Y + (max_Y - min_Y) * rand.nextDouble();

		Vector3d sampledPoint = new Vector3d(position).addMultiple(randY, U).addMultiple(randX, V);

		lRec.direction.set(sampledPoint).sub(shadingPoint);
		lRec.attenuation = (1.0 / shadingPoint.distSq(sampledPoint)) * (lRec.direction.clone().normalize().dot(W));
		lRec.distance = lRec.direction.len();
		lRec.probability = 1.0 / (height * width);
	}

	/**
	 * Default constructor. Produces a unit square light at the origin facing
	 * -z.
	 */
	public RectangleLight() {
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {

		return "RectangleLight: " + width + "x" + height + " @ " + position + " " + intensity + "; normal " + normalDir
				+ "; up " + upDir;
	}
}