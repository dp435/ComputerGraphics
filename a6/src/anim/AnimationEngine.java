package anim;

import java.util.HashMap;
import java.util.Iterator;

import common.Scene;
import common.SceneObject;
import common.event.SceneTransformationEvent;
import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Matrix3;
import egl.math.Quat;

/**
 * A Component Resting Upon Scene That Gives Animation Capabilities
 * 
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * Enum for the mode of rotation
	 */
	private enum RotationMode {
		EULER, QUAT_LERP, QUAT_SLERP;
	};

	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/*
	 * Rotation Mode
	 */
	private RotationMode rotationMode = RotationMode.EULER;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * 
	 * @param s
	 *            The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}

	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * 
	 * @param start
	 *            First Frame
	 * @param end
	 *            Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if (end < start) {
			int buf = end;
			end = start;
			start = buf;
		}

		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}

	/**
	 * Add An Animating Object
	 * 
	 * @param oName
	 *            Object Name
	 * @param o
	 *            Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}

	/**
	 * Remove An Animating Object
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global
	 * Timeline)
	 * 
	 * @param f
	 *            Desired Frame
	 */
	public void moveToFrame(int f) {
		if (f < frameStart)
			f = frameStart;
		else if (f > frameEnd)
			f = frameEnd;
		curFrame = f;
	}

	/**
	 * Looping Forwards Play
	 * 
	 * @param n
	 *            Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if (curFrame > frameEnd)
			curFrame = frameStart + (curFrame - frameEnd - 1);
	}

	/**
	 * Looping Backwards Play
	 * 
	 * @param n
	 *            Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if (curFrame < frameStart)
			curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}

	public int getFirstFrame() {
		return frameStart;
	}

	public int getLastFrame() {
		return frameEnd;
	}

	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame Using The Object's
	 * Transformation - (CONVENIENCE METHOD)
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if (tl == null)
			return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}

	/**
	 * Removes A Keyframe For An Object At The Current Frame Using The Object's
	 * Transformation - (CONVENIENCE METHOD)
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if (tl == null)
			return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}

	/**
	 * Toggles rotation mode that will be applied to all animated objects.
	 */
	public void toggleRotationMode() {
		switch (this.rotationMode) {
		case EULER:
			this.rotationMode = RotationMode.QUAT_LERP;
			break;
		case QUAT_LERP:
			this.rotationMode = RotationMode.QUAT_SLERP;
			break;
		case QUAT_SLERP:
			this.rotationMode = RotationMode.EULER;
			break;
		default:
			break;
		}
		System.out.println("Now in rotation mode " + this.rotationMode.name());
	}

	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations
	 * To The Current Frame - For Each Updated Transformation, An Event Has To
	 * Be Sent Through The Scene Notifying Everyone Of The Change
	 */

	public void updateTransformations() {
		// Loop Through All The Timelines
		// And Update Transformations Accordingly
		// (You WILL Need To Use this.scene)

		// get pair of surrounding frames
		// (function in AnimTimeline)

		// get interpolation ratio

		// interpolate translations linearly

		// polar decompose axis matrices

		// interpolate rotation matrix (3 modes of interpolation) and linearly
		// interpolate scales

		// combine interpolated R,S,and T

		for (AnimTimeline timeline : timelines.values()) {
			AnimKeyframe[] surroundingFrames = new AnimKeyframe[2];
			timeline.getSurroundingFrames(curFrame, surroundingFrames);
			if (surroundingFrames[0] == null)
				timeline.object.transformation.set(surroundingFrames[1].transformation);
			else if (surroundingFrames[1] == null)
				timeline.object.transformation.set(surroundingFrames[0].transformation);
			else {

				float ratio = getRatio(surroundingFrames[0].frame, surroundingFrames[1].frame, curFrame);

				Vector3 T1 = surroundingFrames[0].transformation.getTrans();
				Vector3 T2 = surroundingFrames[1].transformation.getTrans();

				Matrix3 RS1 = new Matrix3(surroundingFrames[0].transformation);
				Matrix3 RS2 = new Matrix3(surroundingFrames[1].transformation);

				Matrix3 R1 = new Matrix3();
				Matrix3 R2 = new Matrix3();
				Matrix3 S1 = new Matrix3();
				Matrix3 S2 = new Matrix3();

				RS1.polar_decomp(R1, S1);
				RS2.polar_decomp(R2, S2);

				Vector3 interpT = T1.lerp(T2, ratio);
				Matrix3 interpS = new Matrix3();
				interpS.interpolate(S1, S2, ratio);

				Matrix3 interpR = new Matrix3();
				switch (this.rotationMode) {
				case EULER:
					Vector3 euler1 = eulerDecomp(R1);
					Vector3 euler2 = eulerDecomp(R2);
					Vector3 interpEuler = euler1.lerp(euler2, ratio);
					interpR.set(Matrix3.createRotationX(interpEuler.x).mulAfter(Matrix3.createRotationY(interpEuler.y))
							.mulAfter(Matrix3.createRotationZ(interpEuler.z)));
					break;
				case QUAT_LERP: {
					Quat Q1 = new Quat(R1);
					Quat Q2 = new Quat(R2);
					Quat Q = (Q1.scale(1f - ratio)).add(Q2.scale(ratio));
					Q.toRotationMatrix(interpR);
				}
					break;
				case QUAT_SLERP: {
					Quat Q1 = new Quat(R1);
					Quat Q2 = new Quat(R2);
					Quat Q = Quat.slerp(Q1, Q2, ratio);
					Q.toRotationMatrix(interpR);
				}
					break;
				default:
					break;
				}

				Matrix4 interpTransformation = new Matrix4(interpR.clone().mulBefore(interpS))
						.mulAfter(Matrix4.createTranslation(interpT));
				timeline.object.transformation.set(interpTransformation);

			}
			this.scene.sendEvent(new SceneTransformationEvent(timeline.object));
		}

	}

	public static float getRatio(int min, int max, int cur) {
		if (min == max)
			return 0f;
		float total = max - min;
		float diff = cur - min;
		return diff / total;
	}

	/**
	 * Takes a rotation matrix and decomposes into Euler angles. Returns a
	 * Vector3 containing the X, Y, and Z degrees in radians. Formulas from
	 * http://nghiaho.com/?page_id=846
	 */
	public static Vector3 eulerDecomp(Matrix3 mat) {
		double theta_x = Math.atan2(mat.get(2, 1), mat.get(2, 2));
		double theta_y = Math.atan2(-mat.get(2, 0), Math.sqrt(Math.pow(mat.get(2, 1), 2) + Math.pow(mat.get(2, 2), 2)));
		double theta_z = Math.atan2(mat.get(1, 0), mat.get(0, 0));

		return new Vector3((float) theta_x, (float) theta_y, (float) theta_z);
	}
}
