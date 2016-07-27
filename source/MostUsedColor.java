package color;

import java.util.Comparator;

public class MostUsedColor implements Comparator<Color> {
	
	public int compare(Color c1, Color c2) {
		return c1.getStitchNum() - c2.getStitchNum();
	}
}