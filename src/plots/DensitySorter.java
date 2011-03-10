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

