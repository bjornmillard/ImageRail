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
