package ray1.shader;

import ray1.shader.Texture;
import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector2;

/**
 * A Texture class that repeats the texture image as necessary for
 * UV-coordinates outside the [0.0, 1.0] range.
 * 
 * @author eschweic zz335
 *
 */
public class RepeatTexture extends Texture {

	public Colorf getTexColor(Vector2 texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colorf();
		}

		// TODO#A2 Fill in this function.
		// 1) Convert the input texture coordinates to integer pixel
		// coordinates. Adding 0.5
		// before casting a double to an int gives better nearest-pixel
		// rounding.
		// 2) If these coordinates are outside the image boundaries, modify them
		// to read from
		// the correct pixel on the image to give a repeating-tile effect.
		// 3) Create a Color object based on the pixel coordinate (use
		// Color.fromIntRGB
		// and the image object from the Texture class), convert it to a Colorf,
		// and return it.
		// NOTE: By convention, UV coordinates specify the lower-left corner of
		// the image as the
		// origin, but the ImageBuffer class specifies the upper-left corner as
		// the origin.
		int x = (int) (texCoord.x * image.getWidth() + 0.5);
		if (x > image.getWidth() - 1)
			x = x % (image.getWidth());
		else if (x < 0)
			x = (x % image.getWidth() + image.getWidth()) % image.getWidth();

		int y = (int) ((1.0 - texCoord.y) * image.getHeight() + 0.5);
		if (y > image.getHeight() - 1)
			y = y % (image.getHeight());
		else if (y < 0)
			y = (y % image.getHeight() + image.getHeight()) % image.getHeight();

		return new Colorf(Color.fromIntRGB(image.getRGB(x, y)));
	}

}
