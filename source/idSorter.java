package color;

import java.util.Comparator;

public class idSorter implements Comparator<Color> {
	
	public int compare(Color c1, Color c2) {
		return c1.getName().compareTo(c2.getName());
	}
}