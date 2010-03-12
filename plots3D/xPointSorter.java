/**
 * xPointSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

/** Sorter for sorting the points according to their xValues
 * @author BLM*/
import java.util.Comparator;
import javax.vecmath.Point3d;

public class xPointSorter implements Comparator
{
	public int compare(Object p1, Object p2)
	{
		Point3d p_1 = (Point3d) p1;
		Point3d p_2 = (Point3d) p2;
		if (p_1.x==p_2.x)
			return 1;
		return (int)((1000000)*(p_1.x-p_2.x));
	}
}

