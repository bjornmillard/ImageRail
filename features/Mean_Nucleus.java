/**
 * ChannelMean_nucleus.java
 *
 * @author Bjorn Millard
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import us.hms.systemsbiology.idx2coordinates.Point;

public class Mean_Nucleus extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		float sum = 0;
		Point[] coords = cell.getComCoordinates("Nucleus");
		int len = coords.length;
		
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
		ChannelName = "Nucleus_"+name+" (Mean)";
	}
}

