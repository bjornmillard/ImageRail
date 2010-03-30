/**
 * Size_Cytoplasm.java
 *
 * @author Bjorn Millard
 */

package features;

import us.hms.systemsbiology.segmentedobject.CellCoordinates;

public class Size_Cytoplasm extends Feature
{
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return cell.getComCoordinates("Cytoplasm").length;
	}
	public String toString()
	{
		ChannelName = "Size_cyto";
		return ChannelName;
	}
}
