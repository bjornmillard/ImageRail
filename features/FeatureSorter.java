/**
 * FeatureSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package features;

import java.awt.Point;
import java.util.Comparator;

public class FeatureSorter implements Comparator
{
	public int compare(Object p1, Object p2)
	{
		Feature f1 = (Feature) p1;
		Feature f2 = (Feature) p2;
		
		String f1n = f1.toString();
		String f2n = f2.toString();
		
		if (f1n.indexOf("Whole")>f2n.indexOf("Whole") )
			return -1;
		
		return 1;
	}
}

