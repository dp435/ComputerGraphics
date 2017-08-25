package ray2;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import ray2.camera.Camera;
import ray2.shader.Shader;
import ray2.viewer.QuickViewer;
import egl.math.Colord;

public class RayTracer {

	/**
	 * Turn the display window on or off
	 */
	public static final boolean DISPLAY = true;

	/**
	 * Output HDR image (using openEXR)
	 */
	public static final boolean writeHDR = false;

	/**
	 * The maximum number of recursive tracing calls allowed
	 */
	public static final int MAX_DEPTH = 12;

	//Size of image sub-blocks
	protected static int SUB_WIDTH = 32;
	protected static int SUB_HEIGHT = 32;

	/**
	 * Widget to draw the image spiral.
	 */
	private static final BlockSpiral spiral = new BlockSpiral();

	/**
	 * Useful little display window that shows rendering progress.
	 * The window actually take a bit of time to render itself, so
	 * you can turn it on or off by setting the DISPLAY flag at the
	 * top of the file.
	 */
	private static QuickViewer viewer = null;

	public static class ScenePath {
		/**
		 * The Scene's File
		 */
		public Path file;
		/**
		 * The Folder Containing The Scene
		 */
		public Path sceneRoot;
		/**
		 * The Root Workspace Path
		 */
		public Path root;

		public ScenePath(String r, String f) {
			if(r == null) {
				root = null;
				file = Paths.get(f);
			}
			else  {
				root = Paths.get(r);
				file = root.resolve(f);
			}
			sceneRoot = file.getParent();
		}

		public String getRoot() {
			return root == null ? null : root.toAbsolutePath().toString();
		}
		public String getFile() {
			return file.toAbsolutePath().toString();
		}

		/**
		 * Attempt To Search The Scene And Program Workspace For A File
		 * @param f The File To Search For
		 * @return The Absolute File Path That Is Resolved (Or null)
		 */
		public String resolve(String f) {
			Path p = root != null ? root.resolve(f) : null;
			if(p == null) p = sceneRoot.resolve(f);
			if(p == null) p = Paths.get(f);
			return p == null ? null : p.toAbsolutePath().toString();
		}
	}

	/**
	 * The Workspace For The Scene
	 */
	public static ScenePath sceneWorkspace = null;

	/**
	 * This directory precedes the arguments passed in via the command line.
	 */
	public static final String directory = "data/scenes/ray2";

	/**
	 * The main method takes all the parameters and assumes they are input files
	 * for the ray tracer. It tries to render each one and write it out to a PNG
	 * file named <input_file>.png. A '-p' option may be passed in to change the
	 * path that is prepended to each file that is included.
	 *
	 * @param args
	 */
	public static final void main(String[] args) {
		ArrayList<ScenePath> pathArgs = new ArrayList<>();
		ArrayList<ScenePath> scenesToRender = new ArrayList<>();
		String currentRoot = directory;

		// Use All The Arguments
		for(int i = 0;i < args.length;i++) {
			switch(args[i].toLowerCase()) {
			case "-p":
				// Use A Different Root Path
				i++;
				if(i < args.length) currentRoot = args[i];
				break;
			case "-pnull":
				// Use The CWD
				currentRoot = null;
				break;
			default:
				// This Must Be A File
				pathArgs.add(new ScenePath(currentRoot, args[i]));
				break;
			}
		}

		if(pathArgs.size() < 1) {
			// Attempt To Render All The Scenes
			pathArgs.add(new ScenePath(currentRoot, "."));

			// Display What's Going To Go Down
			printUsage();
			System.out.println("\nAttempting To Render All Scenes");
		}

		// Expand All The Possible Scenes
		for(ScenePath p : pathArgs) {
			// Add All The Files In The
			File f = p.file.toFile();
			if(f.isDirectory()) {
				for(File _f : f.listFiles()) {
					// We Only Want XML Files
					if(!_f.getPath().endsWith(".xml")) continue;

					scenesToRender.add(new ScenePath(p.getRoot(), _f.toPath().toAbsolutePath().toString()));
				}
			}
			else {
				// We Only Want XML Files
				if(!f.getPath().endsWith(".xml")) continue;

				// Just A Single Scene
				scenesToRender.add(p);
			}
		}

		System.out.println("Attempting To Render " + scenesToRender.size() + " Scene(s)");
		RayTracer rayTracer = new RayTracer();
		rayTracer.run(scenesToRender);
	}

	public static void printUsage() {
		System.out.println("Usage: java RayTracer [-p path] [directory1 directory2 ... | file1 file2 ...]");
		System.out.println("List each scene file you would like to render on the command line separated by spaces.");
		System.out.println("You may also specify a directory, and all scene files in that directory will be rendered.");
		System.out.println("By default, all files specified are prepended with a given path. Use the -p option to");
		System.out.println("override this path. The path may be overriden multiple times or -pnull may be provided to set");
		System.out.println("the path to the program's working directory. With no -p argument given, this path is: " + directory);
		System.out.println("NB: the path is relative to the working directory of the application, which is normally the root of the CS4620 project.");
	}

	/**
	 * The run method takes all the parameters and assumes they are input files
	 * for the ray tracer. It tries to render each one and write it out to a PNG
	 * file named <input_file>.png.
	 *
	 * @param args
	 */
	public void run(ArrayList<ScenePath> args) {
		Parser parser = new Parser();
		for (ScenePath p : args) {
			// Set The Current Workspace For The Scene
			sceneWorkspace = p;

			// Parse the input file
			Scene scene = (Scene) parser.parse(sceneWorkspace.getFile(), Scene.class);

			// Initialize the scene
			scene.init();
			
			// Render the scene
			renderImage(scene);

			// Write the image out
			if (writeHDR)
				scene.getImage().writeHDR(sceneWorkspace.getFile() + ".exr");
			else
				scene.getImage().write(sceneWorkspace.getFile() + ".png");
		}
	}

	/**
	 * The renderImage method renders the entire scene.
	 *
	 * @param scene The scene to be rendered
	 */
	public void renderImage(Scene scene) {

		// Get the output image
		Image image = scene.getImage();

		// Setup viewer
		if(DISPLAY)
			viewer = QuickViewer.createImageViewer(image);

		System.err.print("Starting render...");

		// Set the camera aspect ratio to match output image
		int width = image.getWidth();
		int height = image.getHeight();

		//Setup the sub-block spiral
		spiral.initSubblockSpiral(width, height);

		// Timing counters
		long startTime = System.currentTimeMillis();

		//Loop over all blocks and render
		int offsetX, offsetY, sizeX, sizeY;
		for(int i = 0; i < spiral.totalSubblocks; i++) {

			//Increment the block counter
			spiral.incrementSublockSpiral();
			offsetX = spiral.curSubX*SUB_WIDTH;
			offsetY = spiral.curSubY*SUB_HEIGHT;
			sizeX = Math.min(width-offsetX,SUB_WIDTH);
			sizeY = Math.min(height-offsetY,SUB_HEIGHT);

			renderBlock(scene, image, offsetX, offsetY, sizeX, sizeY);

			//Update display
			if(DISPLAY)
				viewer.setImage(image, offsetX, offsetY, offsetX+sizeX, offsetY+sizeY);

			System.out.println("finished " + (i+1) + "/" + spiral.totalSubblocks + " blocks");

		}

		// Output time
		long totalTime = (System.currentTimeMillis() - startTime);
		System.out.println("Done.  Total rendering time: "
				+ (totalTime / 1000.0) + " seconds");
	}


	/**
	 * This method returns the color along a single ray in outColor.
	 *
	 * @param outColor output space
	 * @param scene the scene
	 * @param ray the ray to shade
	 */
	public static void shadeRay(Colord outColor, Scene scene, Ray ray, int depth) {

		outColor.setZero();

		if(depth > MAX_DEPTH)
			return;

		IntersectionRecord intersectionRecord = new IntersectionRecord();

		if (!scene.getFirstIntersection(intersectionRecord, ray)) {
			if(scene.cubeMap != null)
				scene.cubeMap.evaluate(ray.direction, outColor);
			else
				outColor.set(scene.getBackColor());

			return;
		}

		Shader shader = intersectionRecord.surface.getShader();
		shader.shade(outColor, scene, ray, intersectionRecord, depth);

	}

	/**
	 * Render one block of the output image.
	 *
	 * @param scene The scene data
	 * @param outImage the output image (write the output pixels here)
	 * @param offsetX the startingX value of the block
	 * @param offsetY the startingY value of the block
	 * @param sizeX the width of the block
	 * @param sizeY the height of the block
	 */
	public static void renderBlock(Scene scene, Image outImage, int offsetX, int offsetY, int sizeX, int sizeY) {


		// Do some basic setup
		Ray ray = new Ray();
		Colord pixelColor = new Colord();
		Colord rayColor = new Colord();

		// Set the camera aspect ratio to match output image
		int width = outImage.getWidth();
		int height = outImage.getHeight();

		int samples = scene.getSamples();
		double sInv = 1.0/samples;
		double sInvD2 = sInv / 2;
		double sInvSqr = sInv * sInv;
		double exposure = scene.getExposure();

		Camera cam = scene.getCamera();

		for(int x = offsetX; x < (offsetX + sizeX); x++) {
			for(int y = offsetY; y < (offsetY + sizeY); y++) {

				pixelColor.setZero();

				// TODO#A7 Implement supersampling for antialiasing.
				// Each pixel should have (samples*samples) subpixels.
				
				for (int i = 0; i < samples; i++) {
					for (int j = 0; j < samples; j++) {
						rayColor.setZero();
						double rx = (x + (i + 0.5) / samples) / width;
						double ry = (y + (j + 0.5) / samples) / height;
						cam.getRay(ray, rx, ry);
						shadeRay(rayColor, scene, ray, 1);
						pixelColor.add(rayColor);
					}
				}
				pixelColor.mul(sInvSqr).mul(exposure);
				outImage.setPixelColor(pixelColor, x, y);

			}
		}
	}
}
