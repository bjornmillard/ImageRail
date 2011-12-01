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
 * Integrated_Cytoplasm.java
 *
 * @author Bjorn Millard
 */

package features;

import imagerailio.Point;
import segmentedobject.CellCoordinates;

public class Integrated_Cytoplasm extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		long sum = 0;
		Point[] coords = cell.getComCoordinates("Cytoplasm");
		int len = coords == null ? 0 : coords.length;
		if (len == 0)
			return 0f;


		for (int i = 0; i < len; i++)
			sum += raster[coords[i].y][coords[i].x][getChannelIndex()]; // TODO
																		// -
																		// need
																		// to
																		// account
																		// for
																		// multiple
																		// wavelengths
		
		assert sum >= 0;
		
		return sum-(len*backgroundValues[ChannelIndex]);
	}
	
	public boolean isMultiSpectralFeature()
	{
		return true;
	}
	
	public void setName(String name)
	{
		Name = "Cyto_"+name+" (Integrated)";
	}
}
