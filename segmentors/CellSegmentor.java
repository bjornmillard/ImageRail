/**
 * CellSegmentor.java
 *
 * @author Created by BLM
 */

package segmentors;
import java.util.ArrayList;

import models.Model_ParameterSet;
import us.hms.systemsbiology.segmentedobject.CellCoordinates;

public interface CellSegmentor
{
	public ArrayList<CellCoordinates>  segmentCells(int[][][] raster, Model_ParameterSet pset);
}

