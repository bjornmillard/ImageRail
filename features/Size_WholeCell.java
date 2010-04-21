/**
 * CellSize.java
 *
 * @author Bjorn Millard
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import us.hms.systemsbiology.idx2coordinates.Point;

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

