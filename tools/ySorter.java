/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

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

