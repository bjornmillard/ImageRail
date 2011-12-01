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
 * NucleusCytoRatio.java
 *
 * @author Bjorn Millard
 */

package features;

import imagerailio.Point;
import segmentedobject.CellCoordinates;

public class Ratio_nucCyt extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		long counter = 0;
		long sum = 0;
		String[] names = cell.getComNames();
		// Accounting for possibility of multiple nuclei
		for (int i = 0; i < names.length; i++)
			if (names[i].indexOf("Nucleus") >= 0) {
				Point[] coords = cell.getComCoordinates(names[i]);
				int len = coords == null ? 0 : coords.length;
				counter += len;
				for (int j = 0; j < len; j++)
					sum += raster[coords[j].y][coords[j].x][ChannelIndex];
			}
		float meanN = ((float) sum) / counter - backgroundValues[ChannelIndex];

		int sumC = 0;
		Point[] coordsC = cell.getComCoordinates("Cytoplasm");
		int len = coordsC.length;
		if (len == 0)
			return 0;
		for (int i = 0; i < len; i++)
			sumC+=raster[coordsC[i].y][coordsC[i].x][ChannelIndex];
		float meanC = (float) sumC / (float) len
				- backgroundValues[ChannelIndex];
		if (meanC == 0)
			return 0;
		
		return meanN/meanC;
	}
	
	public boolean isMultiSpectralFeature()
	{
		return true;
	}
	
	public void setName(String name)
	{
		Name = "Ratio_nuc/cyt_"+name;
	}
}

