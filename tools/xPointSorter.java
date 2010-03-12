/**
 * xPointSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package tools;

/** Sorter for sorting the points according to their xValues
 * @author BLM*/
import java.awt.geom.Point2D;
import java.util.Comparator;

public class xPointSorter implements Comparator
{
	public int compare(Object p1, Object p2)
	{
		Point2D.Double p_1 = (Point2D.Double) p1;
		Point2D.Double p_2 = (Point2D.Double) p2;
		return (int)((100000000)*(p_1.x-p_2.x));
	}
}
