/**
 * CellSize.java
 *
 * @author Bjorn Millard
 */

package features;

import segmentedObj.CellCoordinates;
import segmentedObj.Point;

public class Size_WholeCell extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getComCoordinates_AllUnique().length;
	}
	public String toString()
	{
		ChannelName = "Size_whole";
		return ChannelName;
	}
}

