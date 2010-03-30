/**
 * Integrated_Nucleus.java
 *
 * @author BLM
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import us.hms.systemsbiology.segmentedobject.Point;

public class Integrated_Nucleus extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		int sum = 0;
		Point[] coords = cell.getComCoordinates("Nucleus");
		int len = coords.length;
		if (len == 0)
			return 0;
		
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
		ChannelName = "Nucleus_"+name+" (Integrated)";
	}
}
