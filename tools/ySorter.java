/**
 * ySorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package tools;

import java.awt.Point;
import java.util.Comparator;

public class ySorter  implements Comparator
{
	public int compare(Object p1, Object p2)
	{
		Point p_1 = (Point) p1;
		Point p_2 = (Point) p2;
		return (int)((100000000)*(p_1.y-p_2.y));
	}
}

