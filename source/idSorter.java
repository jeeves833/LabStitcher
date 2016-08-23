package color;

import java.util.Comparator;
import java.lang.NumberFormatException;

public class idSorter implements Comparator<Color> {
	
	public int compare(Color c1, Color c2) {
		String name1 = c1.getName();
		String name2 = c2.getName();
		Boolean isInt1, isInt2;
		int id1 = 0, id2 = 0;
		try {
			id1 = Integer.parseInt(name1);
			isInt1 = true;
		} catch (NumberFormatException e) {
			isInt1 = false;
		}
		try {
			id2 = Integer.parseInt(name2);
			isInt2 = true;
		} catch (NumberFormatException e) {
			isInt2 = false;
		}

		if (isInt1) {
			if (isInt2) {
				return id1 - id2;
			} else {
				return -1;
			}
		} else {
			if (isInt2) {
				return 1;
			} else {
				return name1.compareTo(name2);
			}
		}
	}
}