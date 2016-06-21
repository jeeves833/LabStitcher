package color;

import java.awt.color.ColorSpace;
import java.lang.Math;

public class LAB extends ColorSpace{

	private double Yn = 1.0;
	private double Xn = 0.95047;
	private double Zn = 1.08883;

	public LAB() {
		super(ColorSpace.TYPE_Lab, 3);
	}

	private double f(double t) {
		if (t > 216.0/24389.0) {
			return Math.pow(t, 1.0/3.0);
		} else {
			return (841.0/108.0) * t + (4.0/29.0);
		}
	}

	private double fInverse(double t) {
		if (t > 6.0/29.0) {
			return t*t*t;
		} else {
			return 3 * (36.0/841.0) * (t - 4.0/29.0);
		}
	}

	private double linearize(double c) {
		if (c > 0.04045) {
			return Math.pow((c + .055)/1.055, 2.4);
		} else {
			return c/12.92;
		}
	}

	private double unlinearize(double c) {
		if (c > 0.0031308) {
			return 1.055 * Math.pow(c, 1/2.4) - 0.055;
		} else {
			return 12.92 * c;
		}
	}

	public float[] fromCIEXYZ(float[] colorvalue) {
		// System.out.println("fromCIEXYZ");
		double Y = (double)colorvalue[1];
		return new float[]{(float)(116.0 * f(Y/Yn) - 16), (float)(500*(f((double)colorvalue[0]/Xn) - f(Y/Yn))), (float)(200 * (f(Y/Yn) - f((double)colorvalue[2]/Zn)))};
	}

	public float[] toCIEXYZ(float[] colorvalue) {
		// System.out.println("toCIEXYZ");
		double L = (double)colorvalue[0];
		return new float[]{(float)(Xn * fInverse((L + 16)/116.0 + (double)colorvalue[1]/500.0)), 
			(float)(Yn * fInverse((L + 16)/116.0)), 
			(float)(Zn * fInverse((L + 16)/116.0 - (double)colorvalue[2]/200.0))};
	}

	public float[] fromRGB(float[] colorvalue) {
		// System.out.println("fromRGB");
		double Rl = linearize((double)colorvalue[0]);
		double Gl = linearize((double)colorvalue[1]);
		double Bl = linearize((double)colorvalue[2]);
		return fromCIEXYZ(new float[]{(float)(Rl * 0.4124 + Gl * 0.3576 + Bl * 0.1805), 
			(float)(Rl * 0.2126 + Gl * 0.7152 + Bl * 0.0722), 
			(float)(Rl * 0.0193 + Gl * 0.1192 + Bl * 0.9505)});
	}

	public float[] toRGB(float[] colorvalue) {
		// System.out.println("toRGB");
		float[] newvalues = toCIEXYZ(colorvalue);
		double X = (double)newvalues[0];
		double Y = (double)newvalues[1];
		double Z = (double)newvalues[2];
		return new float[]{(float)unlinearize(X * 3.2406 + Y * -1.5372 + Z * -0.4986), 
			(float)unlinearize(X * -0.9689 + Y * 1.8758 + Z * 0.0415), 
			(float)unlinearize(X * 0.0557 + Y * -0.2040 + Z * 1.0570)};
	}

}
