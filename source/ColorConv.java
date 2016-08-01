package color;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.lang.Math;
import java.awt.image.ColorConvertOp;
import java.awt.color.ColorSpace;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.io.Console;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Iterator;

public class ColorConv {
	
	public static void main(String[] args) {
		Console c = System.console();
		if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        String paletteName = c.readLine("Palette file: ");
    	File paletteFile = new File("../palettes/" + paletteName + ".xml");
    	PaletteReader reader = new PaletteReader(paletteFile);
    	System.out.println("Using palette \"" + reader.getName() + "\"");
    	// System.out.println(reader.getColorspace());
    	// System.out.println(Arrays.toString(reader.getColors()));

    	String imageName;
    	File imageFile;
    	BufferedImage img = null;
    	while (img == null) {
	    	try {
	    		imageName = c.readLine("Image file: ");
	    		imageFile = new File("../images/" + imageName);
		    	img = ImageIO.read(imageFile);
		    } catch (IOException e) {
		    	System.out.println("Invalid Image file");
		    }
		}

		System.out.println("Resulting pattern will be " + img.getWidth() + " stitches wide by " 
			+ img.getHeight() + " stitches high.");

		Boolean useDither = c.readLine("Use dithering? (y/n): ").equals("y");
		Boolean singleStitch = c.readLine("Use single-stitch optimization? (y/n): ").equals("y");
		String symbolName = c.readLine("Symbol file: ");
		String saveName = c.readLine("Save file as: ");
		System.out.println("Beginning color conversion");
		BufferedImage newImage = replaceColors(img, reader.getColors(), useDither, saveName, symbolName, singleStitch);

		try {
			ImageIO.write(newImage, "png", new File("../images/" + saveName + ".png"));
		} catch (IOException e) {
			System.out.println("Unable to save to file");
		}
	}

	private static BufferedImage replaceColors(BufferedImage img, Color[] colors, Boolean dither, String saveName, String symbolName, Boolean singleOpt){
		// Create empty BufferedImage the same dimensions as IMG
		int height = img.getHeight();
		int width = img.getWidth();
		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Set containing all the colors used
		HashSet<Color> presentColors = new HashSet<Color>();

		// Create empty error matrix
		double[][][] error = new double[height][width][3];

		Color[][] selectedColors = new Color[height][width];

		double[][] distMatrix = new double[][]{{0,      0,      0},
											   {0,      0,      0.4375},
											   {0.1875, 0.3125, 0.0625}};

		// Create Kd tree out of COLORS
		KdTree searchTree = new KdTree(colors);

		// For every pixel in IMG
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				// Extract RGB data
				int pixData = img.getRGB(x, y);
				double[] rgb = toRGBArray(pixData);

				// Convert RGB data to LAB data
				double[] lab = Color.RGBtoLAB(rgb);
				// System.out.println(Arrays.toString(lab));

				// Get error for current pixel
				double[] currError = error[y][x];

				// Add error to LAB data
				if (dither) {
					for (int i = 0; i < 3; i++) {
						lab[i] += currError[i];
					}
				}

				// Find closest option in the COLORS Kd tree
				Color replacement = KdTree.search(searchTree, new Color(lab));
				// Store replacement color for pattern creation later
				selectedColors[y][x] = replacement;

				// Keep count of how many of each color we've seen
				replacement.incrementStitchNum();

				// Keep track of all the individual colors we've seen
				presentColors.add(replacement);

				// Calculate new error.
				double[] newLab = replacement.getChannels();

				for (int i = 0; i < 3; i++) {
					currError[i] = lab[i] - newLab[i];
				}

				// Distribute to adjacent pixels
				for (int i = 0; i < 3; i++) {
					if (x < width - 1) {
						error[y][x+1][i] += currError[i] * 0.4375;
					}
					if (y < height-1) {
						if (x > 0) {
							error[y+1][x-1][i] += currError[i] * 0.1875;
						}
						error[y+1][x][i] += currError[i] * 0.3125;
						if (x < width - 1) {
							error[y+1][x+1][i] += currError[i] * 0.0625;
						}
					}
				}


				// Convert closest color into RGB and insert into new image
				// double[] newRgb = Color.LABtoRGB(newLab);
				// System.out.println(Arrays.toString(newRgb));
				// newImg.setRGB(x, y, toRGBint(newRgb));

			}
		}

		// Limit color depth to 90 colors

		// Set up priority queue
		Color[] presentArr = new Color[presentColors.size()];
		presentColors.toArray(presentArr);
		PriorityQueue<Color> colorQueue = new PriorityQueue<Color>(presentArr.length, new MostUsedColor());

		// Move elements of PRESENTCOLORS into the queue
		for (int i = 0; i<presentArr.length; i++) {
			colorQueue.offer(presentArr[i]);
		}

		// Set up new Kd-tree
		KdTree presentTree = new KdTree(presentArr);

		System.out.println("Before optimization, " + colorQueue.size() + " colors present.");
		while (colorQueue.size() > 90) {
			Color lowestColor = colorQueue.remove();
			presentTree.delete(lowestColor);
			Color replacement = KdTree.search(presentTree, lowestColor);
			colorQueue.remove(replacement);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (selectedColors[y][x] == lowestColor) {
						selectedColors[y][x] = replacement;
						replacement.incrementStitchNum();
					}
				}
			}
			colorQueue.offer(replacement);
		}

		System.out.println("After optimization, " + colorQueue.size() + " colors present.");

		// Single Stitch removal
		if (singleOpt) {
			removeSingleStitches(selectedColors);	
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width ; x++) {
				newImg.setRGB(x, y, toRGBint(Color.LABtoRGB(selectedColors[y][x].getChannels())));
			}
		}

		// Save pattern
		PatternWriter.write(selectedColors, saveName, symbolName);

		// Return image
		return newImg;
	}

	private static void removeSingleStitches(Color[][] stitches) {
		// Iterate over all stitches
		int height = stitches.length;
		int width = stitches[0].length;
		for (int y = 0; y < height; y++) {
			test:
			for (int x = 0; x < width; x++) {

				// Get color at current space
				Color currColor = stitches[y][x];

				Color bestMatch = currColor;
				double d = Double.POSITIVE_INFINITY;

				// Iterate over local 3x3 square
				for (int localY = Math.max(0, y-1); localY < Math.min(height, y+2); localY++) {
					for (int localX = Math.max(0, x-1); localX < Math.min(width, x+2); localX++) {

						// If we aren't looking at the current space
						if (localY != y || localX != x) {

							Color neighbor = stitches[localY][localX];

							// If an identical matching color is found
							if (currColor == neighbor) {
								// Move to next square
								continue test;
							}
							double currDistance = currColor.distance2(neighbor);

							// If NEIGHBOR is a better match than the current best
							if (currDistance < d) {
								bestMatch = neighbor;
								d = currDistance;	
							}
						}
					}
				}

				// Reaching this line means we have a single stitch and need to replace
				currColor.decrementStitchNum();
				bestMatch.incrementStitchNum();
				stitches[y][x] = bestMatch;
			}
		}
	}

	private static double[] toRGBArray(int data) {
		double b = (double)(data & 255);
		double g = (double)((data >> 8) & 255);
		double r = (double)((data >> 16) & 255);
		return new double[]{r, g, b};
	}

	private static int toRGBint(double[] data) {
		int c = 255 << 24;
		for (int i = 0; i < 3; i++) {
			c += (int)data[i] << ((2-i)*8);
		}
		return c;
	}

}