/**
 * DensitySorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots;

import java.util.Comparator;

public class DensitySorter implements Comparator
{
	public DensityScatter densityScatter;
	
	public int compare(Object p1, Object p2)
	{
		
		Dot dot1 = (Dot) p1;
		Dot dot2 = (Dot) p2;
		
		return (int)((100000000)*(densityScatter.getDensityValue(dot1.point.x, dot1.point.y)-densityScatter.getDensityValue(dot2.point.x, dot2.point.y)));
	}
	
	
}

