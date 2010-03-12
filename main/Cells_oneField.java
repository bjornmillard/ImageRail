/**
 * Cells_oneField.java
 *
 * @author Created by Omnicore CodeGuide
 */

package main;

/** This class is a temporary repository in the RAM for cells of one Field that have been loaded from an HDF cache data file.
 * Included here is both the Cell_coords (coordinates) and the float[][] data matrix values
 *
 @author BLM*/
import features.Feature;
import hdf.Data2D;
import hdf.HDFConnector;
import hdf.SegmentationHDFConnector;
import hdf.HDFConnector.HDFConnectorException;

import java.util.ArrayList;

import segmentedObj.Cell;
import segmentedObj.CellCoordinates;

public class Cells_oneField
{
	private Field field;
	private Well well_parent;
	private int plateIndex;
	private int wellIndex;
	private ArrayList<Cell> cells;
	private ArrayList<CellCoordinates> cellCoords;
	private float[][] dataValues;
	private StringBuffer[] featureNames;
	
	public Cells_oneField(Field field_, SegmentationHDFConnector sCon,
			boolean loadCoords, boolean loadDataVals)
	{
		field = field_;
		well_parent = field.getParentWell();
		plateIndex = well_parent.getPlate().getPlateIndex();
		wellIndex = well_parent.getWellIndex();
//		projectPath = projectPath_;
//		algorithmNameUsed = algorithmNameUsed_;
		try
		{
			// Parameters to write: plateIdx, wellIdx, fieldIdx
			if (loadCoords)
				cellCoords = sCon.readCoordinates(plateIndex, wellIndex, field
						.getIndexInWell());
			if (loadDataVals)
				dataValues = tools.MathOps.convertTofloatMatrix(sCon
						.readFeature(plateIndex,
						wellIndex, field.getIndexInWell()));
			initCells(cellCoords, dataValues);

			if (cellCoords != null && dataValues != null)
				System.out.println("Loading ---> Plate: " + plateIndex
						+ "  Well: " + well_parent.name + "  Field: "
						+ field.getIndexInWell() + "   ||  cells:"
						+ cells.size() + "  data_vals: " + dataValues.length
						+ " coords: " + cellCoords.size());
			else if (dataValues != null)
				System.out.println("Loading ---> Plate: " + plateIndex
						+ "  Well: " + well_parent.name + "  Field: "
						+ field.getIndexInWell() + "   ||  cells:"
						+ cells.size() + "  data_vals: " + dataValues.length
						+ " coords:  null");
			// Read feature names.
			featureNames = sCon.readFeatureNames(plateIndex, wellIndex, field.getIndexInWell());

		}
		catch (Exception e)
		{
			System.out.println("No cells found for Well: "+well_parent.name);
		}
		
	}
	
	/**
	 * Once the cellCoordinates have been loaded, we can init the cell objects
	 * that contain the coords
	 * 
	 * @author BLM
	 */
	private void initCells(ArrayList<Integer> ids,
			ArrayList<CellCoordinates> coords, float[][] dataValues) {

		int num = 0;
		if (coords != null)
			num = coords.size();
		else if (dataValues != null)
			num = dataValues.length;
		cells = new ArrayList<Cell>();
		// loaded both coords and data values
		if (coords != null && dataValues != null)
			for (int i = 0; i < num; i++) {
				cells.add(new Cell(ids.get(i).intValue(), coords.get(i),
						dataValues[i]));
			}
		// Didnt load the cell coordinates
		else if (dataValues != null)
			for (int i = 0; i < num; i++) {
				cells.add(new Cell(ids.get(i).intValue(), null, dataValues[i]));
			}
	}

	/**
	 * Once the cellCoordinates have been loaded, we can init the cell objects
	 * that contain the coords
	 * 
	 * @author BLM
	 */
	private void initCells(ArrayList<CellCoordinates> coords,
			float[][] dataValues) {

		int num = 0;
		if (coords != null)
			num = coords.size();
		else if (dataValues != null)
			num = dataValues.length;
		cells = new ArrayList<Cell>();
		// loaded both coords and data values
		if (coords != null && dataValues != null)
			for (int i = 0; i < num; i++) {
				cells.add(new Cell(i, coords.get(i), dataValues[i]));
			}
		// Didnt load the cell coordinates
		else if (coords == null && dataValues != null)
			for (int i = 0; i < num; i++) {
				cells.add(new Cell(i, null, dataValues[i]));
			}
	}



	
	
	/** Returns a list of cell_coords that were selected
	 * @author BLM*/
	public ArrayList<Cell> getSelectedCells()
	{
		if (cells != null)
		{
			int len = cells.size();
			ArrayList<Cell> arr = new ArrayList<Cell>();
			for (int i = 0; i < len; i++)
			{
				Cell cell = cells.get(i);
				if (cell.isSelected())
					arr.add(cell);
			}
			return arr;
		}
		
		return null;
	}
	
	/** When done with this temporary cell data, this clears all the memory where this  data was stored
	 * @author BLM*/
	public void kill()
	{
		if (cells != null)
		{
			int numCells = cells.size();
			for (int i = 0; i < numCells; i++)
				cells.get(i).kill();
		}
		cellCoords = null;
		dataValues = null;
	}
	
	/** Returns the indexed names of the features that this dataset contains
	 * @author BLM*/
	public StringBuffer[] getFeatureNames()
	{
		return featureNames;
	}
	
	/** Returns the data values for the features computed for the given cell index
	 * @author BLM*/
	// public float[] getFeatureVals(int indexOfCell)
	// {
	// return dataValues[indexOfCell];
	// }
	/**
	 * Returns all the data values for the features computed
	 * 
	 * @author BLM
	 */
	public float[][] getFeatureVals_all() {
		return dataValues;
	}
	
	/**
	 * Returns the cell for the given index
	 * 
	 * @author BLM
	 */
	public Cell getCell(int indexOfCell)
	{
		return cells.get(indexOfCell);
	}

	/**
	 * Returns all the cells
	 * 
	 * @author BLM
	 */
	public ArrayList<Cell> getCells()
	{
		return cells;
	}
	
	/** Overwrites the current cells in RAM for this field in the master HDF5 file
	 * @author BLM*/
	public void resaveCells(SegmentationHDFConnector sCon)
	{
		try
		{


			if (cells != null && cells.size() > 0)
 {
				// Need to read in the coordinates for the cells left so we
				// can save them out again
				if (cells.get(0).getCoordinates() == null) {
					cellCoords = new ArrayList<CellCoordinates>();
					ArrayList<CellCoordinates> coords = sCon.readCoordinates(
							plateIndex, wellIndex, field.getIndexInWell());

					System.out.println("Loading coordinates so we can resave: "
							+ coords.size());
					int len = cells.size();
					for (int i = 0; i < len; i++) {
						Cell cell = cells.get(i);
						cellCoords.add(coords.get(cell.getID()));
					}
				}
				// Now all cells should have both data and coordinates
				// Project name
				sCon.createField(plateIndex, wellIndex, field.getIndexInWell());
				writeCoordinates(sCon);
			}

			//Writing the feature values
			if (dataValues != null && dataValues.length > 0) {
				Float[][] feature = new Float[dataValues.length][dataValues[0].length];
				for (int i = 0; i < dataValues.length; i++)
					for (int j = 0; j < dataValues[0].length; j++)
						feature[i][j] = new Float(dataValues[i][j]);
				Data2D<Float> cellFeature = new Data2D<Float>(feature);
				sCon.writeFeature(plateIndex, wellIndex,
						field.getIndexInWell(), cellFeature);

				// Writing the feature names to file
				Feature[] features = MainGUI.getGUI().getFeatures();
				StringBuffer[] fNames = new StringBuffer[features.length];
				for (int i = 0; i < features.length; i++)
					fNames[i] = new StringBuffer(features[i].toString());
				sCon.writeFeatureNames(plateIndex, wellIndex, field
						.getIndexInWell(), fNames);
			} else {
				System.out
						.println("**Error trying to resave cells:  DataValue Array:  "
								+ dataValues);
				if (dataValues != null)
					System.out
							.println("**Error NumCells in DataVal Array:  DataValues.length:  "
									+ dataValues.length);

			}
		}
		catch (HDFConnector.HDFConnectorException e) {e.printStackTrace();}
	}

	/** */
	private void writeCoordinates(SegmentationHDFConnector sCon)
			throws HDFConnectorException {
		// Assuming all cells in this cell bank are stored with the same
		// structure
		CellCoordinates cell = cellCoords.get(0);
		int numC = cell.getComSize();
		String comName = "";
		if (numC > 0)
			comName = cell.getComNames()[0];
		if (numC == 1) {
			comName = cell.getComNames()[0];
			if (comName.trim().equalsIgnoreCase("BoundingBox")) {
				// System.out.println("___Saving BoundingBoxes");
				sCon.writeCellBoundingBoxes(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Centroid")) {
				// System.out.println("___Saving Centroids");
				sCon.writeCellCentroids(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Outline")) {
				// System.out.println("___Saving All Outlines");
				sCon.writeWholeCells(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			}
		} else // Each cell has more than 1 compartment: ex: Nucleus, cytoplasm
				// etc...
		{

			// System.out.println("___Saving All Coordinates");
			sCon.writeWholeCells(plateIndex, wellIndex, field.getIndexInWell(),
					cellCoords);
		}
	}

	/** */
	
	/** Manually set the data
	 * @author BLM*/
	public void setCellData(ArrayList<Integer> ids,
			ArrayList<CellCoordinates> coords_, float[][] vals)
	{
		kill();
		cellCoords = coords_;
		dataValues = vals;
		initCells(ids, cellCoords, dataValues);
		System.gc();
	}
	
	
}

