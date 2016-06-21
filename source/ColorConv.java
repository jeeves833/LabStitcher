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
		System.out.println("Beginning color conversion");
		BufferedImage newImage = replaceColors(img, reader.getColors(), useDither);

		String saveName = c.readLine("Save file as: ");
		try {
			ImageIO.write(newImage, "png", new File("../images/" + saveName));
		} catch (IOException e) {
			System.out.println("Unable to save to file");
		}
	}

	private static BufferedImage replaceColors(BufferedImage img, Color[] colors, Boolean dither){
		// Create empty BufferedImage the same dimensions as IMG
		int height = img.getHeight();
		int width = img.getWidth();
		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Create empty error matrix
		double[][][] error = new double[height][width][3];

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
				double[] lab = RGBtoLAB(rgb);
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
				Color replacement = searchTree.search(new Color(lab));
				// System.out.println(replacement.getName());

				// Calculate new error.
				double[] newLab = new double[]{replacement.get(0), replacement.get(1), replacement.get(2)};

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
				double[] newRgb = LABtoRGB(newLab);
				// System.out.println(Arrays.toString(newRgb));
				newImg.setRGB(x, y, toRGBint(newRgb));

			}
		}

		// Return image
		return newImg;
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

	private static BufferedImage replaceColors(BufferedImage img, String ditherType, String pl) {
		int height = img.getHeight();
		int width = img.getWidth();
		double[][][] labData = new double[width][height][3];
		WritableRaster imgData = img.getRaster();
		for (int x = 0; x < width; x ++) {
			for (int y = 0; y < height; y ++ ){
				labData[x][y] = Arrays.copyOfRange(RGBtoLAB(imgData.getPixel(x, y, new double[4])), 0, 3);
			}
		}
		int[] palatte;
		if (pl.equals("rgb")) {
			palatte = new int[]{
				(255 << 24) | (255 << 16) | (0 << 8) | 0,
				(255 << 24) | (0 << 16) | (255 << 8) | 0,
				(255 << 24) | (0 << 16) | (0 << 8) | 255,
				(255 << 24) | (0 << 16) | (0 << 8) | 0,
				(255 << 24) | (255 << 16) | (255 << 8) | 255
			};
		} else if (pl.equals("rgbcmy")) {
			palatte = new int[]{
				(255 << 24) | (255 << 16) | (0 << 8) | 0,
				(255 << 24) | (0 << 16) | (255 << 8) | 0,
					(255 << 24) | (255 << 16) | (255 << 8) | 0, (255 << 24) | (0 << 16) | (0 << 8) | 255,
					(255 << 24) | (255 << 16) | (0 << 8) | 255, (255 << 24) | (0 << 16) | (255 << 8) | 255,
					(255 << 24) | (0 << 16) | (0 << 8) | 0, (255 << 24) | (255 << 16) | (255 << 8) | 255 };
		} else {
			palatte = new int[] { (255 << 24) | (0 << 16) | (0 << 8) | 0, (255 << 24) | (100 << 16) | (0 << 8) | 0 };
		}
		double[][][] err = new double[height][width][3];
		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// for (int i = 0; i < height; i++) {
		// for (int j = 0; j < width; j++) {
		// int pix = img.getRGB(j, i);
		// double[] currErr = err[i][j];
		// double blue = Math.max(Math.min(((double)(pix & 0b11111111) +
		// (currErr[0])), 255), 0);
		// double green = Math.max(Math.min(((double)((pix >> 8) & 0b11111111) +
		// (currErr[1])), 255), 0);
		// double red = Math.max(Math.min(((double)((pix >> 16) & 0b11111111) +
		// (currErr[2])), 255), 0);
		// int newPix = closestColor(red, green, blue, palatte);
		// double newred = (double)((newPix >> 16) & 0b11111111);
		// double newgreen = (double)((newPix >> 8) & 0b11111111);
		// double newblue = (double)(newPix & 0b11111111);

		// // Propagate Error to adjacent pixels
		// if (ditherType.equals("fsd")) {
		// double redErr = (red - newred) / 16;
		// double greenErr = (green - newgreen) / 16;
		// double blueErr = (blue - newblue) / 16;
		// if (j + 1 < width) {
		// err[i][j+1][0] += redErr * 7;
		// err[i][j+1][1] += greenErr * 7;
		// err[i][j+1][2] += blueErr * 7;
		// if (i + 1 < height) {
		// err[i+1][j+1][0] += redErr;
		// err[i+1][j+1][1] += greenErr;
		// err[i+1][j+1][2] += blueErr;
		// }
		// }
		// if (i + 1 < height) {
		// if (j - 1 > -1) {
		// err[i+1][j-1][0] += redErr * 3;
		// err[i+1][j-1][1] += greenErr * 3;
		// err[i+1][j-1][2] += blueErr * 3;
		// }
		// err[i+1][j][0] += redErr * 5;
		// err[i+1][j][1] += greenErr * 5;
		// err[i+1][j][2] += blueErr * 5;
		// }
		// }
		// newImg.setRGB(j, i, newPix);
		// }
		// }

		return img;
	}

	// private static int closestColor(double r, double g, double b, int[] palatte) {
	// 	double minDist = Double.POSITIVE_INFINITY;
	// 	int retCol = palatte[0];
	// 	for (int color : palatte) {
	// 		double red = (double) ((color >> 16) & 0b11111111);
	// 		double green = (double) ((color >> 8) & 0b11111111);
	// 		double blue = (double) (color & 0b11111111);
	// 		double dist = Math.sqrt(Math.pow(r - red, 2) + Math.pow(b - blue, 2) + Math.pow(g - green, 2));
	// 		if (dist < minDist) {
	// 			retCol = color;
	// 			minDist = dist;
	// 		}
	// 	}
	// 	return retCol;
	// }

	private static double Yn = 1.0;
	private static double Xn = 0.95047;
	private static double Zn = 1.08883;

	private static double linearize(double c) {
		if (c > 0.04045) {
			return Math.pow((c + .055) / 1.055, 2.4);
		} else {
			return c / 12.92;
		}
	}

	private static double f(double t) {
		if (t > 216.0 / 24389.0) {
			return Math.pow(t, 1.0/3.0);
		} else {
			return (841.0/108.0) * t + (4.0/29.0);
		}
	}

	public static double[] RGBtoLAB(double[] rgb) {
		double Rl = linearize(rgb[0]/255.0);
		double Gl = linearize(rgb[1]/255.0);
		double Bl = linearize(rgb[2]/255.0);
		double Y = Rl * 0.2126 + Gl * 0.7152 + Bl * 0.0722;
		double X = Rl * 0.4124 + Gl * 0.3576 + Bl * 0.1805;
		double Z = Rl * 0.0193 + Gl * 0.1192 + Bl * 0.9505;
		double L = 116.0 * f(Y/Yn) - 16.0;
		double a = 500.0*(f(X/Xn) - f(Y/Yn));
		double b = 200.0 * (f(Y/Yn) - f(Z/Zn));
		return new double[]{L, a, b};
	}

	public static double[] LABtoRGB(double[] lab) {
		double epsilon = 0.008856;
		double kappa = 903.3;
		double fy = (lab[0] + 16)/116.0;
		double fx = lab[1]/500.0 + fy;
		double fz = fy - lab[2]/200.0;
		double xr;
		if (Math.pow(fx, 3.0) > epsilon) {
			xr = Math.pow(fx, 3.0);
		} else {
			xr = (116 * fx - 16) / kappa;
		}
		double yr;
		if (lab[0] > kappa * epsilon) {
			yr = Math.pow(fy, 3.0);
		} else {
			yr = lab[0] / kappa;
		}
		double zr;
		if (Math.pow(fz, 3.0) > epsilon) {
			zr = Math.pow(fz, 3.0);
		} else {
			zr = (116 * fz - 16) / kappa;
		}
		double X = xr * Xn;
		double Y = yr * Yn;
		double Z = zr * Zn;

		double rl = X * 3.2404542 + Y * -1.5371385 + Z * -0.4985314;
		double gl = X * -0.9692660 + Y * 1.8760108 + Z * 0.0415560;
		double bl = X * 0.0556434 + Y * -0.2040259 + Z * 1.0572252;
		double[] rgbLinear = new double[]{rl, gl, bl};
		double[] rgb = new double[3];
		for (int i = 0; i < 3; i++) {
			if (rgbLinear[i] > 0.0031308) {
				rgb[i] = 1.055 * Math.pow(rgbLinear[i], 1/2.4) - 0.055;
			} else {
				rgb[i] = rgbLinear[i] * 12.92;
			}
			rgb[i] = Math.min(Math.max(rgb[i]*255, 0), 255);
		}
		return rgb;
	}
}