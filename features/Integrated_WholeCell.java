/**
 * Integrated_WholeCell.java
 *
 * @author Created by Omnicore CodeGuide
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import us.hms.systemsbiology.idx2coordinates.Point;

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
