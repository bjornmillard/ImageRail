/**
 * Size_Nucleus.java
 *
 * @author Bjorn Millard
 */

package features;


import segmentedObj.CellCoordinates;

public class Size_Nucleus extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getComCoordinates("Nucleus").length;
	}
	public String toString()
	{
		ChannelName = "Size_nucleus";
		return ChannelName;
	}
}
