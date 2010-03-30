/**
 * NucleusCytoRatio.java
 *
 * @author Bjorn Millard
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import us.hms.systemsbiology.segmentedobject.Point;

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

