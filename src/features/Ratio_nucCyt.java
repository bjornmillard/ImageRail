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
		int sumN = 0;
		Point[] coordsN = cell.getComCoordinates("Nucleus");
		int len = coordsN.length;
		if (len == 0)
			return 0;
		for (int i = 0; i < len; i++)
			sumN+=raster[coordsN[i].y][coordsN[i].x][ChannelIndex];
		float meanN =  sumN/len-backgroundValues[ChannelIndex];
		
		int sumC = 0;
		Point[] coordsC = cell.getComCoordinates("Cytoplasm");
		len = coordsC.length;
		if (len == 0)
			return 0;
		for (int i = 0; i < len; i++)
			sumC+=raster[coordsC[i].y][coordsC[i].x][ChannelIndex];
		float meanC = sumC/len-backgroundValues[ChannelIndex];
		if (meanC == 0)
			return 0;
		
		return meanN/meanC;
	}
	
	public boolean isMultiSpectralFeature()
	{
		return true;
	}
	
	public void setChannelName(String name)
	{
		ChannelName = "Ratio_nuc/cyt_"+name;
	}
}

