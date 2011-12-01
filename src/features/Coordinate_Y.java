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
 * Coordinate_Y.java
 *
 * @author Bjorn Millard
 */

package features;

import segmentedobject.CellCoordinates;

public class Coordinate_Y  extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getCentroid().y;
	}
	public String toString()
	{
		Name = "Coordinate_Y";
		return Name;
	}
	
	
}

