/**  
   ImageRail:
   Software for high-throughput microscopy image analysis

   Copyright (C) 2011 Bjorn Millard <bjornmillard@gmail.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package models;


/** This class is a temporary repository in the RAM for cells of one Model_Field that have been loaded from an HDF cache data file.
 * Included here is both the Cell_coords (coordinates) and the float[][] data matrix values
 *
 @author BLM*/
import features.Feature;
import imagerailio.ImageRail_SDCube;

import java.util.ArrayList;

import sdcubeio.H5IO_Exception;
import segmentedobject.Cell;
import segmentedobject.CellCoordinates;

public class Model_FieldCellRepository
{
	private Model_Field field;
	private Model_Well well_parent;
	private int plateIndex;
	private int wellIndex;
	private ArrayList<Cell> cells;
	private ArrayList<CellCoordinates> cellCoords;
	private float[][] dataValues;
	private StringBuffer[] featureNames;
	
	public Model_FieldCellRepository(Model_Field field_, ImageRail_SDCube io,
			boolean loadCoords, boolean loadDataVals)
	{
		field = field_;
		well_parent = field.getParentWell();
		plateIndex = well_parent.getPlate().getPlateIndex();
		wellIndex = well_parent.getWellIndex();
		
		try
		{
			io.openHDF5(io.INPUT);
			if (loadDataVals)
				dataValues = io.readFeatures(plateIndex, wellIndex, field
						.getIndexInWell());
			if (loadCoords)
				cellCoords = io.readCoordinates(plateIndex, wellIndex, field
						.getIndexInWell());
					
			initCells(cellCoords, dataValues);

			if (cellCoords != null && dataValues != null)
				System.out.println("Loaded ---> Plate: " + plateIndex
						+ "  Well: " + well_parent.name + " Field: "
						+ field.getIndexInWell() + "   ||  cells:"
						+ cells.size() + "  data_vals: " + dataValues.length
						+ " coords: " + cellCoords.size());
			else if (dataValues != null)
				System.out.println("Loaded ---> Plate: " + plateIndex
						+ "  Well: " + well_parent.name + "  Field: "
						+ field.getIndexInWell() + "   ||  cells:"
						+ cells.size() + "  data_vals: " + dataValues.length
						+ " coords:  null");
			// Read feature names.
			if (cells != null)
				featureNames = io.readFeatureNames(plateIndex, wellIndex, field
						.getIndexInWell());
			io.closeHDF5();

		}
		catch (Exception e)
		{
			System.out.println("No cells found for Model_Well: "
					+ well_parent.name);
			e.printStackTrace();
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
				cells.add(new Cell(ids.get(i).intValue(), null,
						dataValues[i]));
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
	public void resaveCells(ImageRail_SDCube io)
	{
		try
		{


			if (cells != null && cells.size() > 0)
 {
				// Need to read in the coordinates for the cells left so we
				// can save them out again
				if (cells.get(0).getCoordinates() == null) {
					cellCoords = new ArrayList<CellCoordinates>();
					ArrayList<CellCoordinates> coords = io
							.readCoordinates(
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
				io.openHDF5(io.OUTPUT);
				io.createField(field.getParentWell().getID(), plateIndex,
						wellIndex, field.getIndexInWell(),
 io
								.readFieldDimensions(plateIndex, wellIndex,
										field
.getIndexInWell(),
										ImageRail_SDCube.OUTPUT),
						models.Model_Main.getModel()
								.getExpDesignConnector());
				writeCoordinates(io);
				io.closeHDF5();

			}

			//Writing the feature values
			if (dataValues != null && dataValues.length > 0) {
				io.openHDF5(io.OUTPUT);

				io.writeFeatures(plateIndex, wellIndex,
 field.getIndexInWell(),
						dataValues);

				// Writing the feature names to file
				Feature[] features = models.Model_Main.getModel().getFeatures();
				String[] fNames = new String[features.length];
				for (int i = 0; i < features.length; i++)
					fNames[i] = features[i].toString();
				io.writeFeatureNames(plateIndex, wellIndex, field
						.getIndexInWell(), fNames);
				io.closeHDF5();

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
		catch (H5IO_Exception e) {e.printStackTrace();}
	}

	/** */
	private void writeCoordinates(ImageRail_SDCube io)
			throws H5IO_Exception {
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
				io.writeCellBoundingBoxes(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Centroid")) {
				io.writeCellCentroids(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Outline")) {
				io.writeWholeCells(plateIndex, wellIndex, field
						.getIndexInWell(), cellCoords);
			}
		} else // Each cell has more than 1 compartment: ex: Nucleus, cytoplasm
				// etc...
		{

			io.writeWholeCells(plateIndex, wellIndex, field.getIndexInWell(),
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

	}
	
	
}

