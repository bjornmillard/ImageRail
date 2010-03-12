/**
 * CellSegmentor.java
 *
 * @author Created by BLM
 */

package segmentors;
import java.util.ArrayList;
import main.ParameterSet;
import segmentedObj.CellCoordinates;

public interface CellSegmentor
{
	public ArrayList<CellCoordinates>  segmentCells(int[][][] raster, ParameterSet pset);
}

