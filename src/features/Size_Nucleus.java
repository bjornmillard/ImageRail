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

/**
 * Size_Nucleus.java
 *
 * @author Bjorn Millard
 */

package features;


import imagerailio.Point;
import segmentedobject.CellCoordinates;

public class Size_Nucleus extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		int sum = 0;
		String[] names = cell.getComNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].indexOf("Nucleus") >= 0) {
				Point[] pList = cell.getComCoordinates(names[i]);
				if (pList != null)
					sum += pList.length;
			}
		}
		return sum;
	}
	public String toString()
	{
		Name = "Size_nucleus";
		return Name;
	}
}
