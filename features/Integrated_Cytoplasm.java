/**
 * Integrated_Cytoplasm.java
 *
 * @author Created by Omnicore CodeGuide
 */

package features;

import segmentedObj.CellCoordinates;
import segmentedObj.Point;

public class Integrated_Cytoplasm extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		int sum = 0;
		Point[] coords = cell.getComCoordinates("Cytoplasm");
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
		ChannelName = "Cyto_"+name+" (Integrated)";
	}
}
