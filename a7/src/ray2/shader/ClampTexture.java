package ray2.shader;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * A Texture class that treats UV-coordinates outside the [0.0, 1.0] range as if they
 * were at the nearest image boundary.
 * @author eschweic
 *
 */
public class ClampTexture extends Texture {

	public Colord getTexColor(Vector2d texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colord();
		}
				
				
		int x = (int) (texCoord.x * image.getWidth() + 0.5);
		int y = (int) ((1.0 - texCoord.y) * image.getHeight() + 0.5);
		
		x = Math.max(0, Math.min(image.getWidth()-1, x));
		y = Math.max(0, Math.min(image.getHeight()-1, y));
			
		Color c = Color.fromIntRGB(image.getRGB(x, y));
		return new Colord(c);
	}

}
