package ray2.shader;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * A Texture class that repeats the texture image as necessary for UV-coordinates
 * outside the [0.0, 1.0] range.
 * 
 * @author eschweic
 *
 */
public class RepeatTexture extends Texture {

	public Colord getTexColor(Vector2d texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colord();
		}
							
		int x = (int) (texCoord.x * image.getWidth() + 0.5);
		int y = (int) ((1.0 - texCoord.y) * image.getHeight() + 0.5);
		
		x = x % image.getWidth();
		if (x < 0) x += image.getWidth();
		y = y % image.getHeight();
		if (y < 0) y += image.getHeight();
		
		Color c = Color.fromIntRGB(image.getRGB(x, y));
		return new Colord(c);
	}

}
