/**
 * ChannelMean_wholeCell.java
 *
 * @author Bjorn Millard
 */

package features;

import segmentedObj.CellCoordinates;
import segmentedObj.Point;

public class Mean_WholeCell extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		float sum = 0;
		Point[] coords = cell.getComCoordinates_AllUnique();
		int len = coords.length;
		if (len == 0)
			return 0;
		
		for (int i = 0; i < len; i++)
			sum+=raster[coords[i].y][coords[i].x][ChannelIndex];
		sum = sum/len;
		//Subtracting precomputed background for this set of field images
		sum = sum-backgroundValues[ChannelIndex];
		
		return sum;
	}
	
	public boolean isMultiSpectralFeature()
	{
		return true;
	}
	
	public void setChannelName(String name)
	{
		ChannelName = "Whole_"+name+" (Mean)";
	}
}
