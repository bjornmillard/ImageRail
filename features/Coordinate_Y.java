/**
 * Coordinate_Y.java
 *
 * @author Created by Omnicore CodeGuide
 */

package features;

import features.Feature;
import us.hms.systemsbiology.segmentedobject.CellCoordinates;

public class Coordinate_Y  extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getCentroid().y;
	}
	public String toString()
	{
		ChannelName = "Coordinate_Y";
		return ChannelName;
	}
	
	
}

