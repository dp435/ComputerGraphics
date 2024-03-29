package gl.manip;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blister.input.KeyboardEventDispatcher;
import blister.input.KeyboardKeyEventArgs;
import blister.input.MouseButton;
import blister.input.MouseButtonEventArgs;
import blister.input.MouseEventDispatcher;
import common.Scene;
import common.SceneObject;
import common.UUIDGenerator;
import common.event.SceneTransformationEvent;
import gl.PickingProgram;
import gl.RenderCamera;
import gl.RenderEnvironment;
import gl.RenderObject;
import gl.Renderer;
import form.ControlWindow;
import form.ScenePanel;
import egl.BlendState;
import egl.DepthState;
import egl.IDisposable;
import egl.RasterizerState;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import ext.csharp.ACEventFunc;

public class ManipController implements IDisposable {
	public final ManipRenderer renderer = new ManipRenderer();
	public final HashMap<Manipulator, UUIDGenerator.ID> manipIDs = new HashMap<>();
	public final HashMap<Integer, Manipulator> manips = new HashMap<>();

	private final Scene scene;
	private final ControlWindow propWindow;
	private final ScenePanel scenePanel;
	private final RenderEnvironment rEnv;
	private ManipRenderer manipRenderer = new ManipRenderer();

	private final Manipulator[] currentManips = new Manipulator[3];
	private RenderObject currentObject = null;

	private Manipulator selectedManipulator = null;

	/**
	 * Is parent mode on? That is, should manipulation happen in parent rather
	 * than object coordinates?
	 */
	private boolean parentSpace = false;

	/**
	 * Last seen mouse position in normalized coordinates
	 */
	private final Vector2 lastMousePos = new Vector2();

	public ACEventFunc<KeyboardKeyEventArgs> onKeyPress = new ACEventFunc<KeyboardKeyEventArgs>() {
		@Override
		public void receive(Object sender, KeyboardKeyEventArgs args) {
			if (selectedManipulator != null)
				return;
			switch (args.key) {
			case Keyboard.KEY_T:
				setCurrentManipType(Manipulator.Type.TRANSLATE);
				break;
			case Keyboard.KEY_R:
				setCurrentManipType(Manipulator.Type.ROTATE);
				break;
			case Keyboard.KEY_Y:
				setCurrentManipType(Manipulator.Type.SCALE);
				break;
			case Keyboard.KEY_P:
				parentSpace = !parentSpace;
				break;
			}
		}
	};
	public ACEventFunc<MouseButtonEventArgs> onMouseRelease = new ACEventFunc<MouseButtonEventArgs>() {
		@Override
		public void receive(Object sender, MouseButtonEventArgs args) {
			if (args.button == MouseButton.Right) {
				selectedManipulator = null;
			}
		}
	};

	public ManipController(RenderEnvironment re, Scene s, ControlWindow cw) {
		scene = s;
		propWindow = cw;
		Component o = cw.tabs.get("Object");
		scenePanel = o == null ? null : (ScenePanel) o;
		rEnv = re;

		// Give Manipulators Unique IDs
		manipIDs.put(Manipulator.ScaleX, scene.objects.getID("ScaleX"));
		manipIDs.put(Manipulator.ScaleY, scene.objects.getID("ScaleY"));
		manipIDs.put(Manipulator.ScaleZ, scene.objects.getID("ScaleZ"));
		manipIDs.put(Manipulator.RotateX, scene.objects.getID("RotateX"));
		manipIDs.put(Manipulator.RotateY, scene.objects.getID("RotateY"));
		manipIDs.put(Manipulator.RotateZ, scene.objects.getID("RotateZ"));
		manipIDs.put(Manipulator.TranslateX, scene.objects.getID("TranslateX"));
		manipIDs.put(Manipulator.TranslateY, scene.objects.getID("TranslateY"));
		manipIDs.put(Manipulator.TranslateZ, scene.objects.getID("TranslateZ"));
		for (Entry<Manipulator, UUIDGenerator.ID> e : manipIDs.entrySet()) {
			manips.put(e.getValue().id, e.getKey());
		}

		setCurrentManipType(Manipulator.Type.TRANSLATE);
	}

	@Override
	public void dispose() {
		manipRenderer.dispose();
		unhook();
	}

	private void setCurrentManipType(int type) {
		switch (type) {
		case Manipulator.Type.TRANSLATE:
			currentManips[Manipulator.Axis.X] = Manipulator.TranslateX;
			currentManips[Manipulator.Axis.Y] = Manipulator.TranslateY;
			currentManips[Manipulator.Axis.Z] = Manipulator.TranslateZ;
			break;
		case Manipulator.Type.ROTATE:
			currentManips[Manipulator.Axis.X] = Manipulator.RotateX;
			currentManips[Manipulator.Axis.Y] = Manipulator.RotateY;
			currentManips[Manipulator.Axis.Z] = Manipulator.RotateZ;
			break;
		case Manipulator.Type.SCALE:
			currentManips[Manipulator.Axis.X] = Manipulator.ScaleX;
			currentManips[Manipulator.Axis.Y] = Manipulator.ScaleY;
			currentManips[Manipulator.Axis.Z] = Manipulator.ScaleZ;
			break;
		}
	}

	public void hook() {
		KeyboardEventDispatcher.OnKeyPressed.add(onKeyPress);
		MouseEventDispatcher.OnMouseRelease.add(onMouseRelease);
	}

	public void unhook() {
		KeyboardEventDispatcher.OnKeyPressed.remove(onKeyPress);
		MouseEventDispatcher.OnMouseRelease.remove(onMouseRelease);
	}

	/**
	 * Get the transformation that should be used to draw <manip> when it is
	 * being used to manipulate <object>.
	 * 
	 * This is just the object's or parent's frame-to-world transformation, but
	 * with a rotation appended on to orient the manipulator along the correct
	 * axis. One problem with the way this is currently done is that the
	 * manipulator can appear very small or large, or very squashed, so that it
	 * is hard to interact with.
	 * 
	 * @param manip
	 *            The manipulator to be drawn (one axis of the complete widget)
	 * @param mViewProjection
	 *            The camera (not needed for the current, simple implementation)
	 * @param object
	 *            The selected object
	 * @return
	 */
	public Matrix4 getTransformation(Manipulator manip, RenderCamera camera, RenderObject object) {
		Matrix4 mManip = new Matrix4();

		switch (manip.axis) {
		case Manipulator.Axis.X:
			Matrix4.createRotationY((float) (Math.PI / 2.0), mManip);
			break;
		case Manipulator.Axis.Y:
			Matrix4.createRotationX((float) (-Math.PI / 2.0), mManip);
			break;
		case Manipulator.Axis.Z:
			mManip.setIdentity();
			break;
		}
		if (parentSpace) {
			if (object.parent != null)
				mManip.mulAfter(object.parent.mWorldTransform);
		} else
			mManip.mulAfter(object.mWorldTransform);

		return mManip;
	}

	/**
	 * Apply a transformation to <b>object</b> in response to an interaction
	 * with <b>manip</b> in which the user moved the mouse from
	 * <b>lastMousePos</b> to <b>curMousePos</b> while viewing the scene through
	 * <b>camera</b>. The manipulation happens differently depending on the
	 * value of ManipController.parentMode; if it is true, the manipulator is
	 * aligned with the parent's coordinate system, or if it is false, with the
	 * object's local coordinate system.
	 * 
	 * @param manip
	 *            The manipulator that is active (one axis of the complete
	 *            widget)
	 * @param camera
	 *            The camera (needed to map mouse motions into the scene)
	 * @param object
	 *            The selected object (contains the transformation to be edited)
	 * @param lastMousePos
	 *            The point where the mouse was last seen, in normalized [-1,1]
	 *            x [-1,1] coordinates.
	 * @param curMousePos
	 *            The point where the mouse is now, in normalized [-1,1] x
	 *            [-1,1] coordinates.
	 */
	public void applyTransformation(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos,
			Vector2 curMousePos) {

		// There are three kinds of manipulators; you can tell which kind you
		// are dealing with by looking at manip.type.
		// Each type has three different axes; you can tell which you are
		// dealing with by looking at manip.axis.

		// For rotation, you just need to apply a rotation in the correct space
		// (either before or after the object's current
		// transformation, depending on the parent mode this.parentSpace).

		// For translation and scaling, the object should follow the mouse.
		// Following the assignment writeup, you will achieve
		// this by constructing the viewing rays and the axis in world space,
		// and finding the t values *along the axis* where the
		// ray comes closest (not t values along the ray as in ray tracing). To
		// do this you need to transform the manipulator axis
		// from its frame (in which the coordinates are simple) to world space,
		// and you need to get a viewing ray in world coordinates.

		// There are many ways to compute a viewing ray, but perhaps the
		// simplest is to take a pair of points that are on the ray,
		// whose coordinates are simple in the canonical view space, and map
		// them into world space using the appropriate matrix operations.

		// You may find it helpful to structure your code into a few helper
		// functions; ours is about 150 lines.

		// TODO#A3#Part 4
		switch (manip.type) {
		case Manipulator.Type.TRANSLATE:
			applyTranslation(manip, camera, object, lastMousePos, curMousePos);
			break;
		case Manipulator.Type.ROTATE:
			applyRotation(manip, camera, object, lastMousePos, curMousePos);
			break;
		case Manipulator.Type.SCALE:
			applyScaling(manip, camera, object, lastMousePos, curMousePos);
			break;
		}
	}

	/** Helper function to apply the rotation. */
	private void applyRotation(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos,
			Vector2 curMousePos) {
		float scaleFactor = 1.5f;
		float yDisplacement = scaleFactor * (curMousePos.y - lastMousePos.y);

		// Apply transformation.
		switch (manip.axis) {
		case Manipulator.Axis.X:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createRotationX(yDisplacement));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createRotationX(yDisplacement));
			break;
		case Manipulator.Axis.Y:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createRotationY(yDisplacement));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createRotationY(yDisplacement));
			break;
		case Manipulator.Axis.Z:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createRotationZ(yDisplacement));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createRotationZ(yDisplacement));
			break;
		}
	}

	/** Helper function to apply the translation. */
	private void applyTranslation(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos,
			Vector2 curMousePos) {

		// Calculate manipulator ray.
		Vector3 manipDir = null;
		Vector3 manipOrigin;
		switch (manip.axis) {
		case Manipulator.Axis.X:
			manipDir = new Vector3(1, 0, 0);
			break;
		case Manipulator.Axis.Y:
			manipDir = new Vector3(0, 1, 0);
			break;
		case Manipulator.Axis.Z:
			manipDir = new Vector3(0, 0, 1);
			break;
		}
		if (parentSpace) {
			manipDir.set(object.parent.mWorldTransform.clone().mulDir(manipDir));
			manipOrigin = object.parent.mWorldTransform.clone().mulPos(new Vector3(0, 0, 0));
		} else {
			manipDir.set(object.mWorldTransform.clone().mulDir(manipDir));
			manipOrigin = object.mWorldTransform.clone().mulPos(new Vector3(0, 0, 0));
		}

		// Calculate mouse rays.
		Vector3 lastMouseDir = getMouseDir(lastMousePos, camera.mViewProjection.clone().invert());
		Vector3 lastMouseOrigin = getMouseOrigin(lastMousePos, camera.mViewProjection.clone().invert());
		Vector3 curMouseDir = getMouseDir(curMousePos, camera.mViewProjection.clone().invert());
		Vector3 curMouseOrigin = getMouseOrigin(curMousePos, camera.mViewProjection.clone().invert());

		// Calculate T's.
		float lastT = calculateT(lastMouseDir, lastMouseOrigin, manipDir, manipOrigin);
		float curT = calculateT(curMouseDir, curMouseOrigin, manipDir, manipOrigin);
		float displacement = (curT - lastT);

		// Apply transformation.
		switch (manip.axis) {
		case Manipulator.Axis.X:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createTranslation(displacement, 0, 0));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createTranslation(displacement, 0, 0));
			break;
		case Manipulator.Axis.Y:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createTranslation(0, displacement, 0));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createTranslation(0, displacement, 0));
			break;
		case Manipulator.Axis.Z:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createTranslation(0, 0, displacement));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createTranslation(0, 0, displacement));
			break;
		}
	}

	/** Helper function to apply the scaling. */
	private void applyScaling(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos,
			Vector2 curMousePos) {

		// Calculate manipulator ray.
		Vector3 manipDir = null;
		Vector3 manipOrigin;
		switch (manip.axis) {
		case Manipulator.Axis.X:
			manipDir = new Vector3(1, 0, 0);
			break;
		case Manipulator.Axis.Y:
			manipDir = new Vector3(0, 1, 0);
			break;
		case Manipulator.Axis.Z:
			manipDir = new Vector3(0, 0, 1);
			break;
		}
		if (parentSpace) {
			manipDir.set(object.parent.mWorldTransform.clone().mulDir(manipDir));
			manipOrigin = object.parent.mWorldTransform.clone().mulPos(new Vector3(0, 0, 0));
		} else {
			manipDir.set(object.mWorldTransform.clone().mulDir(manipDir));
			manipOrigin = object.mWorldTransform.clone().mulPos(new Vector3(0, 0, 0));
		}

		// Calculate mouse rays.
		Vector3 lastMouseDir = getMouseDir(lastMousePos, camera.mViewProjection.clone().invert());
		Vector3 lastMouseOrigin = getMouseOrigin(lastMousePos, camera.mViewProjection.clone().invert());
		Vector3 curMouseDir = getMouseDir(curMousePos, camera.mViewProjection.clone().invert());
		Vector3 curMouseOrigin = getMouseOrigin(curMousePos, camera.mViewProjection.clone().invert());

		// Calculate T's.
		float lastT = calculateT(lastMouseDir, lastMouseOrigin, manipDir, manipOrigin);
		float curT = calculateT(curMouseDir, curMouseOrigin, manipDir, manipOrigin);
		float scaleRatio = (curT / lastT);

		// Apply transformation.
		switch (manip.axis) {
		case Manipulator.Axis.X:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createScale(scaleRatio, 1, 1));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createScale(scaleRatio, 1, 1));
			break;
		case Manipulator.Axis.Y:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createScale(1, scaleRatio, 1));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createScale(1, scaleRatio, 1));
			break;
		case Manipulator.Axis.Z:
			if (parentSpace)
				object.sceneObject.transformation.mulAfter(Matrix4.createScale(1, 1, scaleRatio));
			else
				object.sceneObject.transformation.mulBefore(Matrix4.createScale(1, 1, scaleRatio));
			break;
		}
	}

	/** Helper function to get the mouse direction. */
	private Vector3 getMouseDir(Vector2 mousePosition, Matrix4 inverseVP) {
		Vector3 nearPoint = new Vector3(mousePosition.x, mousePosition.y, -1);
		nearPoint.set(inverseVP.clone().mulPos(nearPoint));
		Vector3 farPoint = new Vector3(mousePosition.x, mousePosition.y, 1);
		farPoint.set(inverseVP.clone().mulPos(farPoint));

		return farPoint.sub(nearPoint);
	}

	/** Helper function to get the mouse origin. */
	private Vector3 getMouseOrigin(Vector2 mousePosition, Matrix4 inverseVP) {
		Vector3 origin = new Vector3(mousePosition.x, mousePosition.y, -1);
		origin.set(inverseVP.clone().mulPos(origin));
		return origin;
	}

	/** Helper function to calculate t-values. */
	private float calculateT(Vector3 mouseDir, Vector3 mouseOrigin, Vector3 manipDir, Vector3 manipOrigin) {
		Vector3 basis1 = mouseDir.clone();
		Vector3 basis2 = manipDir.clone().cross(basis1).normalize();
		Vector3 normal = basis1.clone().cross(basis2);

		float distNumer = (mouseOrigin.clone().sub(manipOrigin)).dot(normal);
		float distDenom = manipDir.clone().dot(normal);
		return distNumer / distDenom;
	}

	public void checkMouse(int mx, int my, RenderCamera camera) {
		Vector2 curMousePos = new Vector2(mx, my).add(0.5f).mul(2).div(camera.viewportSize.x, camera.viewportSize.y)
				.sub(1);
		if (curMousePos.x != lastMousePos.x || curMousePos.y != lastMousePos.y) {
			if (selectedManipulator != null && currentObject != null) {
				applyTransformation(selectedManipulator, camera, currentObject, lastMousePos, curMousePos);
				scene.sendEvent(new SceneTransformationEvent(currentObject.sceneObject));
			}
			lastMousePos.set(curMousePos);
		}
	}

	public void checkPicking(Renderer renderer, RenderCamera camera, int mx, int my) {
		if (camera == null)
			return;

		// Pick An Object
		renderer.beginPickingPass(camera);
		renderer.drawPassesPick();
		if (currentObject != null) {
			// Draw Object Manipulators
			GL11.glClearDepth(1.0);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

			DepthState.DEFAULT.set();
			BlendState.OPAQUE.set();
			RasterizerState.CULL_NONE.set();

			drawPick(camera, currentObject, renderer.pickProgram);
		}
		int id = renderer.getPickID(Mouse.getX(), Mouse.getY());

		selectedManipulator = manips.get(id);
		if (selectedManipulator != null) {
			// Begin Manipulator Operations
			System.out.println("Selected Manip: " + selectedManipulator.type + " " + selectedManipulator.axis);
			return;
		}

		SceneObject o = scene.objects.get(id);
		if (o != null) {
			System.out.println("Picked An Object: " + o.getID().name);
			if (scenePanel != null) {
				scenePanel.select(o.getID().name);
				propWindow.tabToForefront("Object");
			}
			currentObject = rEnv.findObject(o);
		} else if (currentObject != null) {
			currentObject = null;
		}
	}

	public RenderObject getCurrentObject() {
		return currentObject;
	}

	public void draw(RenderCamera camera) {
		if (currentObject == null)
			return;

		DepthState.NONE.set();
		BlendState.ALPHA_BLEND.set();
		RasterizerState.CULL_CLOCKWISE.set();

		for (Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, currentObject);
			manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
		}

		DepthState.DEFAULT.set();
		BlendState.OPAQUE.set();
		RasterizerState.CULL_CLOCKWISE.set();

		for (Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, currentObject);
			manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
		}

	}

	public void drawPick(RenderCamera camera, RenderObject ro, PickingProgram prog) {
		for (Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, ro);
			prog.setObject(mTransform, manipIDs.get(manip).id);
			manipRenderer.drawCall(manip.type, prog.getPositionAttributeLocation());
		}
	}

}
