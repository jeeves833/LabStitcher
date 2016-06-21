package color;

import java.lang.Math;
import java.util.Arrays;

public class Color {

	private String name;
	private double[] channels;
	private int dimension;

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
}