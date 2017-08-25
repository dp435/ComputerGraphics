package ray2.shader;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import ray2.RayTracer;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * This class represents a simple 2D texture implementation for a shader. It stores and
 * reads from a BufferedImage which is read in from an arbitrary image file on disk.
 * 
 * @author eschweickart
 *
 */
public abstract class Texture {
	/** The image used when looking up UV coordinates. */
	protected BufferedImage image;
	/** Return the BufferedImage used for lookup. */
	public BufferedImage getImage() { return image; }
	/** Set the BufferedImage from a given file on disk. */
	public void setImage(String filename) {
		System.out.println("Loading: " + RayTracer.sceneWorkspace.resolve(filename));
		try {
			File f = new File(RayTracer.sceneWorkspace.resolve(filename));
			image = ImageIO.read(f);
		} catch (Exception e) {
			System.err.println("Error loading texture: " + e);
			System.exit(1);
		}
	}
	
	/** Default constructor. Creates an empty Texture object. */
	public Texture() {	}
	
	/**
	 * Get the texture color at a given UV coordinate.
	 * 
	 * @param texCoord The UV texture coordinates.
	 * @return The color at the given point.
	 */
	public abstract Colord getTexColor(Vector2d texCoord); 
}
