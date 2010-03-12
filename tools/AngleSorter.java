/**
 * angleSorter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package tools;


import java.awt.geom.Point2D;
import java.util.Comparator;

public class AngleSorter implements Comparator
{
	Point2D.Double refPoint;
	
	public AngleSorter(Point2D.Double point)
	{
		refPoint = point;
	}
	
	public int compare(Object p1, Object p2)
	{
		Point2D.Double p_1 = (Point2D.Double) p1;
		Point2D.Double p_2 = (Point2D.Double) p2;
		
		double opposite = p_1.y-refPoint.y;
		double adjacent = Math.abs(p_1.x-refPoint.x);
		double angle1 = Math.atan(opposite/adjacent);
		
		opposite = (p_2.y-refPoint.y);
		adjacent = Math.abs(p_2.x-refPoint.x);
		double angle2 = Math.atan(opposite/adjacent);
		
		return (int)((100000000)*(angle1-angle2));
	}
}


