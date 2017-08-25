package ray2.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import egl.math.Colord;
import ray2.Image;


/**
 * JPanel for the display of images
 *
 * @author srm, nsavva
 */
public class PanelDisplay extends JPanel {
	
	/** An internal image for display* */
	private BufferedImage bufferedImage;
	
	/** The type of local image buffer* */
	private static final int BUFFER_TYPE = BufferedImage.TYPE_3BYTE_BGR;
	
	/** window to call when size changes* */
	final Window repackAncestor;
	
	/**
	 * Default constructor
	 */
	public PanelDisplay() {
		
		this(null);
	}
	
	/**
	 * Constructor sets the ancerstor window
	 *
	 * @param ancestor
	 */
	public PanelDisplay(java.awt.Window ancestor) {
		
		repackAncestor = ancestor;
		bufferedImage = new BufferedImage(128, 128, BUFFER_TYPE);
	}
	
	/**
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		
		return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
	}
	
	/**
	 * Saves the currently displayed image to the supplied file
	 *
	 * @param inFilename
	 * @throws java.io.IOException if the file cannot be processed
	 */
	public synchronized void saveCurrentImage(File inFilename) throws java.io.IOException {
		
		String fileName = inFilename.getName();
		String type = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
		ImageIO.write(bufferedImage, type, inFilename);
	}
	
	/**
	 * Set the currently displayed image
	 *
	 * @param inImage the new image to display
	 */
	public void setImage(Image inImage, int xl, int yl, int xh, int yh) {
		
		bufferImage(inImage, xl, yl, xh, yh);
		repaint(); // schedule repainting by event dispatching thread
		Thread.yield(); // give the update thread a chance to run
	}
	
	public void setImage(Image inImage) {
		setImage(inImage, 0, 0, inImage.getWidth(), inImage.getHeight());
	}
	
	
	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		
		synchronized (this) { // make sure image doesn't change while we are drawing
			// it
			g.drawImage(bufferedImage, 0, 0, null);
		}
	}
	
	/**
	 * Copy the input image into the internal buffered image used for display
	 * @param image the image to place in the buffer
	 */
	private synchronized void bufferImage(Image image, int xl, int yl, int xh, int yh) {
		
		if (bufferedImage.getWidth() != image.getWidth() ||
			bufferedImage.getHeight() != image.getHeight()) {
			bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BUFFER_TYPE);
		}
		
		//int w = image.getWidth();
		int h = image.getHeight();
		Colord pixelColor = new Colord();
		for (int y = h-yh; y < h-yl; y++) {
			for (int x = xl; x < xh; x++) {
				image.getPixelColor(pixelColor,x,h-y-1);
				pixelColor.gammaCorrect(2.2);
				int rgb = pixelColor.toColor().toIntRGB();
				bufferedImage.setRGB(x,y,rgb);
			}
		}
	}
	
}