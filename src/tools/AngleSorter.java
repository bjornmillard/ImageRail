/**  
   ImageRail:
   Software for high-throughput microscopy image analysis

   Copyright (C) 2011 Bjorn Millard <bjornmillard@gmail.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
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


