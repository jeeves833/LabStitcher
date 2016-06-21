package color;

import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.lang.Math;

public class KdTree {

	private int dims;
	private Node root;

	private class Node {

		private Color value;
		private Node leftChild = null;
		private Node rightChild = null;

		public Node(Color c) {
			value = c;
		}

		public void setLeftChild(Node n) {
			leftChild = n;
		}

		public void setRightChild(Node n) {
			rightChild = n;
		}

		public Node getLeftChild() {
			return leftChild;
		}

		public Node getRightChild() {
			return rightChild;
		}

		public Boolean isLeaf() {
			return (leftChild == null) && (rightChild == null);
		}

		public Color getColor() {
			return value;
		}
	}

	static private class PointComparator implements Comparator<Color> {

		private int axis;

		public PointComparator(int ax) {
			axis = ax;
		}

		public int compare(Color c1, Color c2) {
			return (int) Math.signum(c1.get(axis) - c2.get(axis));
		}
	}

	public KdTree(Color[] points) {
		// Extract number of dimensions
		dims = points[0].getDims();

		// Sort points into DIMS sorted lists
		Color[][] sortedPoints = new Color[dims][points.length];
		for (int i = 0; i < dims; i++) {
			sortedPoints[i] = Arrays.copyOf(points, points.length);
			Arrays.sort(sortedPoints[i], new PointComparator(i));
			// System.out.println(Arrays.toString(sortedPoints[i]));
		}
		root = nodeCreator(sortedPoints, 0);
	}

	private Node nodeCreator(Color[][] points, int depth) {
		// If number of points is == 1
		if (points[0].length == 1) {
			// Make a leaf node
			return new Node(points[0][0]);
		}

		// Choose split axis
		int axis = depth;

		// Select pivot point (median)
		Color pivotColor = points[axis][points[0].length/2];
		Node node = new Node(pivotColor);
		double pivot = pivotColor.get(axis);

		// Split sorted lists by median (Maintaining sort order)
		int leftSize = points[0].length/2;
		int rightSize = points[0].length - leftSize;
		Color[][] leftPoints = new Color[dims][leftSize];
		Color[][] rightPoints = new Color[dims][rightSize];
		for (int d = 0; d < dims; d++) {
			for (int i = 0, l = 0, r = 0; i < points[d].length; i++) {
				Color currPoint = points[d][i];
				if (currPoint.get(axis) < pivot) {
					leftPoints[d][l] = currPoint;
					l++;
				} else {
					rightPoints[d][r] = currPoint;
					r++;
				}
			}
		}

		// Make children trees
		node.setLeftChild(nodeCreator(leftPoints, (depth+1)%dims));
		node.setRightChild(nodeCreator(rightPoints, (depth+1)%dims));
		return node;
	}

	public Color search(Color query) {
		return search(query, root, 0);
	}

	private Color search(Color query, Node root, int depth) {
		// If ROOT is a leaf node
		if (root.isLeaf()) {
			// return its color
			return root.getColor();
		}

		// Get split axis
		int axis = depth;

		// Get pivot point from node
		Color pivotColor = root.getColor();
		double pivot = pivotColor.get(axis);
		Node primary;
		Node secondary;
		// If QUERY is left of the pivot
		if (query.get(axis) < pivot) {
			// Primary subtree is left tree
			primary = root.getLeftChild();

			// Secondary subtree is right tree
			secondary = root.getRightChild();
		}
		// If QUERY is right of the pivot
		else {
			// Primary subtree is right tree
			primary = root.getRightChild();

			// Secondary subtree is left tree
			secondary = root.getLeftChild();
		}
		// Get closest point in primary subtree
		Color primaryClosest = search(query, primary, (depth+1)%dims);

		// If pivot plane is closer to QUERY than closest point
		double primaryDist = query.distance2(primaryClosest);
		if (primaryDist > Math.pow(query.get(axis) - pivot, 2)) {
			// Get closest point in secondary subtree
			Color secondaryClosest = search(query, secondary, (depth+1)%dims);

			// Return closer of primary-closest and secondary-closest points
			double secondaryDist = query.distance2(secondaryClosest);
			if (primaryDist < secondaryDist) {
				return primaryClosest;
			} else {
				return secondaryClosest;
			}
		}
		// Return primary-closest point
		return primaryClosest;
	}

}