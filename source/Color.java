package color;

import java.lang.Math;
import java.util.Arrays;

public class Color {

	private String name;
	private double[] channels;
	private int dimension;
	private String symbol = "";
	private int stitches;

	public Color(String n, double[] c) {
		name = n;
		channels = c;
		dimension = c.length;
	}

	public Color(double[] c) {
		name = "Anonymous Color";
		channels = c;
		dimension = c.length;
	}

	public Color(int[] c) {
		channels = new double[c.length];
		for (int i = 0; i < c.length; i++) {
			channels[i] = (double) c[i];
		}
		name = "Anonymous Color";
		dimension = c.length;
	}

	public String getName() {
		return name;
	}

	public void setSymbol(String newSymbol) {
		symbol = newSymbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public void incrementStitchNum() {
		stitches += 1;
	}

	public int getStitchNum() {
		return stitches;
	}

	public double get(int index) {
		if (index >= dimension) {
			throw new IndexOutOfBoundsException("Cannot get " + index + " channel of " + dimension + " dimensional Color.");
		} else {
			return channels[index];
		}
	}

	public int getDims() {
		return dimension;
	}

	public double distance2(Color other) {
		if (dimension != other.getDims()) {
			throw new IllegalArgumentException("Cannot calculate distance between " + dimension + " dimensional color and " + other.getDims() + "dimensional Color.");
		} else {
			double dist = 0;
			for (int i = 0; i < dimension; i++) {
				dist += Math.pow(channels[i] - other.get(i), 2);
			}
			return dist;
		}
	}

	public String toString() {
		return name + ": " + Arrays.toString(channels);
	}

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