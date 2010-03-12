/**
 * Coordinate_X.java
 *
 * @author Created by Omnicore CodeGuide
 */


package features;

import features.Feature;
import segmentedObj.CellCoordinates;

public class Coordinate_X extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getCentroid().x;
	}
	public String toString()
	{
		ChannelName = "Coordinate_X";
		return ChannelName;
	}
	
}

