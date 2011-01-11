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
 * Integrated_WholeCell.java
 *
 * @author Bjorn Millard
 */

package features;

import imagerailio.Point;
import segmentedobject.CellCoordinates;

public class Integrated_WholeCell extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		int sum = 0;
		Point[] coords = cell.getComCoordinates_AllUnique();
		int len = coords.length;
		
		for (int i = 0; i < len; i++)
			sum+=raster[coords[i].y][coords[i].x][ChannelIndex]; 	//TODO - need to account for multiple wavelengths
		
		return sum;
	}
	
	public boolean isMultiSpectralFeature()
	{
		return true;
	}
	
	public void setChannelName(String name)
	{
		ChannelName = "Whole_"+name+" (Integrated)";
	}
}
