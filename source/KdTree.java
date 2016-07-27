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

	private Node getRoot() {
		return root;
	}

	public int getDims() {
		return dims;
	}

	private Node nodeCreator(Color[][] points, int depth) {
		// If number of points is == 0
		if (points[0].length == 0) {
			// Return a null node
			return null;
		}

		// Choose split axis
		int axis = depth;

		// Select pivot point (median)
		Color pivotColor = points[axis][points[0].length/2];

		// Make node out of current color
		Node node = new Node(pivotColor);

		// Chose pivot plane value
		double pivot = pivotColor.get(axis);

		// Split sorted lists by median (Maintaining sort order)
		int leftSize = points[0].length/2;
		int rightSize = points[0].length - leftSize - 1;
		Color[][] leftPoints = new Color[dims][leftSize];
		Color[][] rightPoints = new Color[dims][rightSize];
		for (int d = 0; d < dims; d++) {
			for (int i = 0, l = 0, r = 0; i < points[d].length; i++) {
				Color currPoint = points[d][i];

				// Do not keep the current node value in children lists
				if (currPoint.get(axis) < pivot) {
					leftPoints[d][l] = currPoint;
					l++;
				} else if (currPoint != pivotColor) {
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

	private static Color best;
	private static double best_dist;

	public static Color search(KdTree tree, Color query) {
		// Initialize BEST and BEST_DIST for beginning of search
		best = null;
		best_dist = Double.POSITIVE_INFINITY;

		// Search tree (result will be stored in BEST)
		search(tree.getRoot(), query, 0);
		return best;
	}

	// Helper function for nearest neighbor search that sets BEST and BEST_DIST according to its findings
	private static void search(Node root, Color query, int depth) {
		// If the node is null, we don't change anything
		if (root == null) {
			return;
		}

		// Get split axis
		int axis = depth;

		// Get pivot point from ROOT
		Color pivotColor = root.getColor();

		// Compare QUERY point to the ROOT color
		double currDist = query.distance2(pivotColor);
		if (currDist < best_dist) {
			best = pivotColor;
			best_dist = currDist;
		}

		// Get pivot plane value
		double pivot = pivotColor.get(axis);

		// Primary and Secondary nodes denote the search order
		Node primary, secondary;

		// If QUERY is left of the pivot plane
		if (query.get(axis) < pivot) {
			primary = root.getLeftChild();
			secondary = root.getRightChild();
		}
		// If QUERY is left of the pivot plane
		else {
			primary = root.getRightChild();
			secondary = root.getLeftChild();
		}

		// Get dimensionality of tree
		int dims = pivotColor.getDims();

		// Search the first subtree
		search(primary, query, (depth+1)%dims);

		// If second subtree is within BEST_DIST of the query
		if (best_dist > Math.pow(query.get(axis) - pivot, 2)) {
			// Search the second subtree
			search(secondary, query, (depth+1)%dims);
		}
	}

	public Color findMin(int dim) {
		if (dim >= dims) {
			throw new IllegalArgumentException("Tree has " + dims + " dimensions.  Cannot search for max item in dimension " + dim);
		}
		return findMin(root, dim, 0);
	}

	private Color findMin(Node root, int dim, int depth) {
		// Check if ROOT is null
		if (root == null) {
			return null;
		}

		// If the current node splits the space in the dimension we want
		if (depth == dim) {

			// Look down the left subtree
			// If there is no left subtree
			if (root.getLeftChild() == null) {

				// Return the color in the root
				return root.getColor();

			} else {

				// Search the left subtree for the minimum
				return findMin(root.getLeftChild(), dim, (depth+1)%dims);
			}
		} else {

			// Need to look down both subtrees
			return KdTree.min(KdTree.min(findMin(root.getLeftChild(), dim, (depth+1)%dims), 
				findMin(root.getRightChild(), dim, (depth+1)%dims), dim), 
				root.getColor(), dim);
		}
	}

	private static Color min(Color c1, Color c2, int dim) {
		if (c1 == null) {
			return c2;
		} else if (c2 == null) {
			return c1;
		}
		PointComparator comp = new PointComparator(dim);
		if (comp.compare(c1, c2) > 0) {
			return c2;
		} else {
			return c1;
		}
	}

	public void delete(Color element) {
		root = delete(root, element, 0);
	}

	private Node delete(Node root, Color element, int depth) {
		// Reached the end of the tree
		if (root == null) {
			return null;
		}

		// Current node is the one we want to delete
		if (element == root.getColor()) {
			// If the right subtree exists
			if (root.getRightChild() != null) {
				// Find minimum color in right subtree
				Color newValue = findMin(root.getRightChild(), depth, (depth+1)%dims);
				// Set current node's color to the found minimum
				Node newRoot = new Node(newValue);
				// Delete found color from the right subtree
				newRoot.setLeftChild(root.getLeftChild());
				newRoot.setRightChild(delete(root.getRightChild(), newValue, (depth+1)%dims));
				// Return root
				return newRoot;
			} else 
			// If the left subtree exists instead
			if (root.getLeftChild() != null) {
				// Set right subtree to the left subtree
				root.setRightChild(root.getLeftChild());
				// Find minimum color in right subtree
				Color newValue = findMin(root.getRightChild(), depth, (depth+1)%dims);
				// Set current node's color to the found minimum
				Node newRoot = new Node(newValue);
				// Delete found color from right subtree
				newRoot.setLeftChild(null);
				newRoot.setRightChild(delete(root.getRightChild(), newValue, (depth+1)%dims));
				// Return root
				return newRoot;
			} else 
			// The current node is a leaf
			{
				return null;
			}
		} else 
		// Need to continue searching 
		{
			// If desired node is in left subtree
			if (element.get(depth) < root.getColor().get(depth)) {
				// Delete from left subtree
				root.setLeftChild(delete(root.getLeftChild(), element, (depth+1)%dims));
			} else 
			// Desired node is in right subtree
			{
				// Delete from right subtree
				root.setRightChild(delete(root.getRightChild(), element, (depth+1)%dims));
			}
		}
		return root;
	}

}