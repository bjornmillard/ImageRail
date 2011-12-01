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

import gui.Gui_PlateRepository;

import java.util.ArrayList;
import java.util.Hashtable;

import plots.Gate_DotPlot;

public class Model_PlateRepository
{

	/** The Plates that this plate repository holds */
	private Model_Plate[] ThePlates;

	/** The visual GUI representation of this plateRepository model */
	private Gui_PlateRepository ThisGUI;

	/** Basic constructor */
	public Model_PlateRepository(Model_Plate[] thePlates) {
		ThePlates = thePlates;
	}
	
	/** Sets the associated gui to this model */
	public void setGUI(Gui_PlateRepository gui) {
		ThisGUI = gui;
	}
	
	/** Returns the gui that represents this Model */
	public Gui_PlateRepository getGUI() {
		return ThisGUI;
	}

	/** Returns well with given indices */
	public Model_Well getWell(int plateIndex, int wellIndex) {
		return ThePlates[plateIndex].getWell(wellIndex);
	}

	/** Returns all wells in this project */
	public ArrayList<Model_Well> getAllWells() {
		ArrayList<Model_Well> arrAll = new ArrayList<Model_Well>();
		for (int p = 0; p < getNumPlates(); p++) {
			Model_Plate plate = ThePlates[p];
			ArrayList<Model_Well> arr = new ArrayList<Model_Well>();
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
					arr.add(plate.getWells()[r][c]);
			arrAll.addAll(arr);
		}

		return arrAll;
	}

	public ArrayList<Model_Well> getSelectedWells_horizOrder()
	{
		ArrayList<Model_Well> arrAll = new ArrayList<Model_Well>();
		for (int p = 0; p < getNumPlates(); p++)
		{
			Model_Plate plate = ThePlates[p];
			ArrayList<Model_Well> arr = new ArrayList<Model_Well>();
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
					if (plate.getWells()[r][c].isSelected())
						arr.add(plate.getWells()[r][c]);
			
			arrAll.addAll(arr);
		}
		
		return arrAll;
	}
	
	/** Made originally for the lineplot, this method will return an array of arrays of Wells that were selected in all the plates.  It is of
	 * dimension [numRows selected from each plate seperately][numColumns in this row series]
	 * @author BLM */
	public Model_Well[][] getAllSelectedWells_RowSeries()
	{
		ArrayList<ArrayList<Model_Well>> rows = new ArrayList<ArrayList<Model_Well>>();
		for (int p = 0; p < getNumPlates(); p++)
		{
			//For this plate, check if it has any wells highlighted in a row format
			Model_Plate plate = ThePlates[p];
			for (int r = 0; r < plate.getNumRows(); r++)
			{
				boolean boo = false;
				ArrayList<Model_Well> col = null;
				for (int c = 0; c < plate.getNumColumns(); c++)
					if (plate.getWells()[r][c].isSelected())
					{
						if (!boo)
						{
							col = new ArrayList<Model_Well>();
							boo = true;
						}
						col.add(plate.getWells()[r][c]);
					}
				if (boo)
					rows.add(col);
			}
		}
		//Now have an ArrayList of ArrayLists
		
		
		// Transfering over to Array Format
		int numRows = rows.size();
		if (numRows == 0)
			return null;
		
		
		Model_Well[][] wells = new Model_Well[numRows][];
		for (int r = 0; r < numRows; r++)
		{
			ArrayList<Model_Well> series = (ArrayList<Model_Well>) rows.get(r);
			int numCols = series.size();
			
			wells[r] = new Model_Well[numCols];
			for (int c = 0; c < numCols; c++)
			{
				
				wells[r][c] = (Model_Well)series.get(c);
			}
		}
		return wells;
	}
	
	/** This method will return an array of arrays of Wells that were selected.  It will find wells in each plate that were highlighted, then go across
	 * all the plates to retrieve a series of wells "through" plate where each series contains one well from each plate
	 *
	 * @author BLM */
	public Model_Well[][] getAllSelectedWells_TransPlateSeries()
	{
		Hashtable hash = new Hashtable();
		ArrayList arr = new ArrayList();
		ArrayList oneSeries = null;
		for (int p = 0; p < getNumPlates(); p++)
		{
			Model_Plate plate = ThePlates[p];
			for (int c = 0; c < plate.getNumColumns(); c++)
				for (int r = 0; r < plate.getNumRows(); r++)
				{
					if (plate.getWells()[r][c].isSelected())
					{
						//If we haven't already made a series out of this Model_Well Series... create one
						if (hash.get(plate.getWells()[r][c].name)==null)
						{
							oneSeries = new ArrayList();
							for (int i = 0; i < getNumPlates(); i++)
								oneSeries.add(ThePlates[i].getWells()[r][c]);
							arr.add(oneSeries);
							hash.put(plate.getWells()[r][c].name, oneSeries);
						}
					}
				}
		}
		
		
		// Transfering over to Array Format
		int numSeries = arr.size();
		if (numSeries == 0)
			return null;
		
		Model_Well[][] wells = new Model_Well[numSeries][];
		for (int r = 0; r < numSeries; r++)
		{
			ArrayList series =(ArrayList)arr.get(r);
			int seriesSize = series.size();
			
			wells[r] = new Model_Well[seriesSize];
			for (int c = 0; c < seriesSize; c++)
				wells[r][c] = (Model_Well)series.get(c);
			
		}
		return wells;
	}
	
	/***
	 * 	Return the Plates
	 * */
	public Model_Plate[] getPlates()
	{
		return ThePlates;
	}
	
	
	
	/** Made originally for the lineplot, this method will return an array of arrays of Wells that were selected in all the plates.  It is of
	 * dimension [numCols selected from each plate seperately][numRows in this col series]
	 * @author BLM */
	public Model_Well[][] getAllSelectedWells_ColumnSeries()
	{
		ArrayList cols = new ArrayList();
		for (int p = 0; p < getNumPlates(); p++)
		{
			//For this plate, check if it has any wells highlighted in a col format
			Model_Plate plate = ThePlates[p];
			for (int c = 0; c < plate.getNumColumns(); c++)
			{
				boolean boo = false;
				ArrayList row = null;
				for (int r = 0; r < plate.getNumRows(); r++)
					if (plate.getWells()[r][c].isSelected())
					{
						if (!boo)
						{
							row = new ArrayList();
							boo = true;
						}
						row.add(plate.getWells()[r][c]);
					}
				if (boo)
					cols.add(row);
			}
		}
		//Now have an ArrayList of ArrayLists
		
		
		// Transfering over to Array Format
		int numSeries = cols.size();
		if (numSeries == 0)
			return null;
		
		Model_Well[][] wells = new Model_Well[numSeries][];
		for (int r = 0; r < numSeries; r++)
		{
			ArrayList series =(ArrayList)cols.get(r);
			int numRows = series.size();
			
			wells[r] = new Model_Well[numRows];
			for (int c = 0; c < numRows; c++)
			{
				wells[r][c] = (Model_Well)series.get(c);
			}
		}
		return wells;
	}
	
	

	
	/** Returns the min/max values of the given cells for all features loaded into the MainGUI
	 * @author BLM
	 * @return float[][] where float[].length = 2 (min/max) and float[][].length = numFeatures*/
	static public float[][] getCellMinMaxValues_allFeatures(float[][] values)
	{
		if (values==null || values.length<=0)
			return null;
		
		int numF = models.Model_Main.getModel().getTheFeatures().size();
		
		int numCells = values.length;
		float[][] data = new float[2][numF];
		float[] min = new float[numF];
		float[] max = new float[numF];
		for (int i = 0; i < numF; i++)
		{
			min[i] = Float.POSITIVE_INFINITY;
			max[i] = Float.NEGATIVE_INFINITY;
		}
		
		for (int j = 0; j < numCells; j++)
			for (int i = 0; i < numF; i++)
			{
				float val = values[j][i];
				if (val > Float.NEGATIVE_INFINITY && val < min[i])
					min[i] = val;
				if (val < Float.POSITIVE_INFINITY && val > max[i])
					max[i] = val;
			}
		

		// for (int i = 0; i < data[0].length; i++) {
		// System.out.println("MINMAX: " + data[0][i] + "   " + data[1][i]);
		//
		// }
		
		data[0] = min;
		data[1] = max;
		return data;
	}
	
	/** Initializes the min and max values for each feature across all plates
	 * @author BLM*/
	public void updateMinMaxValues()
	{
		int largeInt = Integer.MAX_VALUE;
		for (int p = 0; p < getNumPlates(); p++)
		{
			Model_Plate plate = ThePlates[p];
			plate.initMinMaxFeatureValues();
			float[][] plateMinMax = plate.getMinMaxFeatureValues();
			float[][] plateMinMax_log =  plate.getMinMaxFeatureValues_log();
			int numF = models.Model_Main.getModel().getTheFeatures().size();
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
				{
					if (plate.getWells()[r][c].containsCellData())
					{
						float[][] data = getCellMinMaxValues_allFeatures(plate.getWells()[r][c].getCell_values());
						
						if (data != null)
							for (int f = 0; f < numF; f++)
							{
								
								if (data[0][f] < largeInt && data[0][f] >= 0 && data[1][f] < largeInt && data[1][f] >= 0)
								{
									
									if (data[0][f] < plateMinMax[0][f])
										plateMinMax[0][f] = data[0][f];
									if (data[1][f] > plateMinMax[1][f])
										plateMinMax[1][f] = data[1][f];
								}
								
								
								// log values
								double val_low = tools.MathOps.log(data[0][f]);
								double val_hi = tools.MathOps.log(data[1][f]);
								
								if (val_low < largeInt && val_low >= 0 && val_hi < largeInt && val_hi >= 0)
								{
									if (val_low < plateMinMax_log[0][f])
										plateMinMax_log[0][f] = (float) val_low;
									if (val_hi > plateMinMax_log[1][f])
										plateMinMax_log[1][f] = (float) val_hi;

								}
								
							}
					}
				}

			if (ThisGUI != null)
				ThisGUI.updatePanel();
		}
	}
	
	/**
	 * Returns the total number of extant gates. Used for assigning unique
	 * identifiers for each new gate
	 *
	 * @author BLM
	 */
	public int getUniqueGateID()
	{
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (int p = 0; p < getNumPlates(); p++)
		{
			Model_Plate plate = ThePlates[p];
			int rows = plate.getNumRows();
			int cols = plate.getNumColumns();
			
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					Model_Well well = plate.getWells()[r][c];
					int len = well.TheGates.size();
					
					for (int i = 0; i < len; i++)
					{
						Gate_DotPlot g = (Gate_DotPlot) well.TheGates.get(i);
						int id = g.ID;
						boolean unique = true;
						// Checking to see if this is a unique ID, if so add it to
						// the array
						int num = arr.size();
						for (int j = 0; j < num; j++)
						{
							Integer in = arr.get(j);
							if (in.intValue() == id)
							{
								unique = false;
								break;
							}
						}
						if (unique)
							arr.add(new Integer(id));
					}
				}
		}
		
		// Return the next largest integer ID
		int len = arr.size();
		int valToReturn = 0;
		for (int i = 0; i < len; i++)
		{
			int id = arr.get(i).intValue();
			if (id >= valToReturn)
				valToReturn = id + 1;
		}
		return valToReturn;
	}
	
	
	/** Returns the number of plates in this holding panel
	 * @author BLM*/
	public int getNumPlates()
	{
		return ThePlates.length;
	}
	
	

}

