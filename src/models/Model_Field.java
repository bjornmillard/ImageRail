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

/** Holder for images and objects pertinent to a single field aquired in a well
 * @author BLM*/

import imagerailio.ImageRail_SDCube;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.ArrayList;

import sdcubeio.H5IO_Exception;
import segmentedobject.Cell;

public class Model_Field {
	private Model_Well parentWell;
	private int indexInWell;
	private File[] ImageFiles;
	private ArrayList<Shape> ROIs;
	private ArrayList<Boolean> ROIs_selected;
	private Model_FieldCellRepository TheCellRepository;
	private float[] backgroundValues;
	private Model_ParameterSet TheParameterSet;

	public Model_Field(File[] imageFiles, int index, Model_Well parentWell_) {
		parentWell = parentWell_;
		indexInWell = index;
		ROIs = new ArrayList<Shape>();
		ROIs_selected = new ArrayList<Boolean>();
		ImageFiles = imageFiles;
		loadParameterSet();
	}

	public int getIndexInWell() {
		return indexInWell;
	}

	public Model_Well getParentWell() {
		return parentWell;
	}

	/**
	 * Adds this ROI to the Model_Fields ROIs arraylist without writing it to
	 * the HDF5 file. Used for loading ROIs already read from the HDF5 file
	 * 
	 * @author Bjorn Millard
	 * */
	public void setROI(Shape roi) {
		if (ROIs == null)
			ROIs = new ArrayList<Shape>();
		ROIs.add(roi);
		ROIs_selected.add(new Boolean(false));
	}

	/**
	 * Adds this ROI to the Model_Fields ROIs arraylist and also writes it to
	 * the HDF5 file
	 * 
	 * @author Bjorn Millard
	 * */
	public void addROI(Shape roi, int[] fieldDimensions) {
		if (roi instanceof Polygon) {
		//Check this is not a duplicate ROI
			int len = ROIs.size();
			for (int i = 0; i < len; i++) {
				if (ROIs.get(i) instanceof Polygon) {
					Polygon polyIn = (Polygon) roi;
					Polygon polyRef = (Polygon) ROIs.get(i);
					int numRef = polyRef.npoints;
					int numIn = polyIn.npoints;
					if (numRef == numIn) {
						boolean allPtsSame = true;
						for (int j = 0; j < numIn; j++) {
							if (polyRef.xpoints[j] != polyIn.xpoints[j]
									|| polyRef.ypoints[j] != polyIn.ypoints[j]) {
								allPtsSame = false;
								break;
							}
						}
						// If all points in this polygon already there, then
						// return without adding new ROI
						if (allPtsSame)
							return;
					}
				}
			}
			// Writing ROI To HDF5 file
			Polygon polyIn = (Polygon) roi;
			ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();

			String pathToField = io.getHashtable_in().get(
					io.getIndexKey(getParentWell().getPlate().getID(),
							getParentWell().getWellIndex())
							+ "f" + getIndexInWell());
			// Sample doesnt exist yet, so create a sample and field skeleton
			if (pathToField == null) {
				try {
					io.createField(getParentWell().getID(), getParentWell()
							.getPlate().getID(),
							getParentWell().getWellIndex(), getIndexInWell(),
							fieldDimensions, models.Model_Main.getModel()
									.getExpDesignConnector());
				} catch (H5IO_Exception e) {
					System.out
							.println("ERROR creating field in the HDF5 file in order to store ROI***");
					e.printStackTrace();
				}
			}
			// Try again
			String key = io.getIndexKey(getParentWell().getPlate().getID(),
					getParentWell().getWellIndex())
					+ "f" + getIndexInWell();
			System.out.println("using key: " + key);
			pathToField = io.getHashtable_in().get(key);
			System.out.println(pathToField);
			if (pathToField != null)
				io.writeROI(pathToField, polyIn, ROIs.size());
			else
				System.out
						.println("ERROR - did not create field properly such that the ROI could be encoded in HDF5");
		}
		else if (roi instanceof Rectangle)
		{
			int len = ROIs.size();
			for (int i = 0; i < len; i++) {
				if (ROIs.get(i) instanceof Rectangle) {
					Rectangle polyIn = (Rectangle) roi;
					Rectangle polyRef = (Rectangle) ROIs.get(i);
					if(polyIn.x==polyRef.x && polyIn.y==polyRef.y && polyIn.width==polyRef.width && polyIn.height==polyRef.height)
						return;
				}
			}
		}
		else if (roi instanceof Ellipse2D.Float)
		{
			int len = ROIs.size();
			for (int i = 0; i < len; i++) {
				if (ROIs.get(i) instanceof Ellipse2D.Float) {
					Ellipse2D.Float polyIn = (Ellipse2D.Float) roi;
					Ellipse2D.Float polyRef = (Ellipse2D.Float) ROIs.get(i);
					if(polyIn.x==polyRef.x && polyIn.y==polyRef.y && polyIn.width==polyRef.width && polyIn.height==polyRef.height)
						return;
				}
			}
		}
		

		ROIs.add(roi);
		System.out.println("Adding ROI");
		ROIs_selected.add(new Boolean(false));
	}

	public ArrayList<Shape> getROIs() {
		return ROIs;
	}

	public void setBackgroundValues(float[] bkgd) {
		backgroundValues = bkgd;
	}

	public float[] getBackgroundValues() {
		return backgroundValues;
	}

	public void setImageFiles(File[] files) {
		ImageFiles = files;
	}

	public File getImageFile(int index) {
		return ImageFiles[index];
	}

	public File[] getImageFiles() {
		return ImageFiles;
	}

	public void deleteROIs() {
		ROIs = new ArrayList<Shape>();
		ROIs_selected = new ArrayList<Boolean>();

		// Deleting ROIs from HDF5 file
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		String pathToField = io.getHashtable_in().get(
				io.getIndexKey(getParentWell().getPlate().getID(),
						getParentWell().getWellIndex())
						+ "f" + getIndexInWell());
		if (pathToField != null)
			for (int i = 0; i < ROIs.size(); i++)
				io.removeROI(pathToField, i);
	}

	public void deleteROI(int index) {
		ROIs.remove(index);
		ROIs_selected.remove(index);

		// Deleting ROI from HDF5 file
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		String pathToField = io.getHashtable_in().get(
				io.getIndexKey(getParentWell().getPlate().getID(),
						getParentWell().getWellIndex())
						+ "f" + getIndexInWell());
		if (pathToField != null)
			io.removeROI(pathToField, index);
	}

	public int getNumberOfChannels() {
		return ImageFiles.length;
	}

	public int getNumberOfROIs() {
		return ROIs.size();
	}

	public void setROIselected(int index, boolean boo) {
		if (index > getNumberOfROIs() - 1)
			return;

		Boolean val = ROIs_selected.get(index);
		boolean value = val.booleanValue();

		ROIs_selected.remove(index);
		ROIs_selected.add(index, new Boolean(boo));
	}

	public boolean isROIselected(int index) {
		if (index > getNumberOfROIs() - 1)
			return false;

		return ROIs_selected.get(index).booleanValue();
	}

	/**
	 * Checks if this field has a corresponding single-cell data within the HDF5
	 * file
	 * 
	 * @author BLM
	 */
	public boolean doesDataExist(String hdfPath) {
		// Seeing if path to this sample(well) exists
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		String pathToField = io.getHashtable_in().get(
				io.getIndexKey(getParentWell().getPlate().getID(),
						getParentWell().getWellIndex())
						+ "f" + getIndexInWell());
		if (pathToField != null)
			return true;
		return false;
	}

	/**
	 * Loads the cells from the HDF file on the hardrive into RAM if they exist
	 * 
	 * @author BLM
	 */
	public void loadCells(ImageRail_SDCube io,
			boolean loadCoords, boolean loadDataVals) {
			try {
				TheCellRepository = new Model_FieldCellRepository(this, io, loadCoords,
						loadDataVals);
			} catch (Exception e) {
			// }
			;
		}
	}

	/** */
	public void resaveCells(ImageRail_SDCube io) {

		if (TheCellRepository != null)
			TheCellRepository.resaveCells(io);
	}

	/**
	 * Clears the cells from the RAM if they were loaded, but the cell data is
	 * still located within the HDF file and can be retrieved again from the
	 * hardrive by calling the "loadCells" method
	 * 
	 * @author BLM
	 */
	public void killCells() {
		if (TheCellRepository != null)
			TheCellRepository.kill();
		TheCellRepository = null;
	}

	/**
	 * Returns all the cell feature values computed for all cells
	 * 
	 * @author BLM
	 */
	public float[][] getFeatureVals_all() {
		if (TheCellRepository == null)
			return null;
		return TheCellRepository.getFeatureVals_all();
	}
	//	
	// /** Returns all the cell coordinates for all cells
	// * @author BLM*/
	// public ArrayList<CellCoordinates> getCellCoords_all()
	// {
	// if(TheCellRepository==null)
	// return null;
	// return TheCellRepository.getCoords_all();
	// }

	/**
	 * Returns all the cells
	 * 
	 * @author BLM
	 */
	public synchronized ArrayList<Cell> getCells()
	{
		if(TheCellRepository==null)
			return null;
		return TheCellRepository.getCells();
	}
	
	/**
	 * Returns the cell repository for this field
	 * 
	 * @author BLM
	 */
	public synchronized Model_FieldCellRepository getCellRepository() {
		return TheCellRepository;
	}
	
	/** Returns whether any cells have been loaded into the RAM for this field
	 * @author BLM*/
	public boolean areCellsLoaded()
	{
		if (TheCellRepository != null && TheCellRepository.getCells() != null)
			return true;
		return false;
	}
	
	/** Returns the number of cells in the field that are bound by the ROI with the given ROI index
	 * @author BLM*/
	public int getCellsBoundByROI(int roiIndex)
	{
		int counter = 0;
		Shape roi = getROIs().get(roiIndex);
		ArrayList<Cell> cells = TheCellRepository.getCells();
		int numC = cells.size();
		for (int i = 0; i < numC; i++)
		{
			imagerailio.Point p = cells.get(i).getCoordinates()
					.getComCoordinates(0)[0];
			if(roi.contains(p.x,p.y))
				counter++;
		}
		return counter;
	}

	/**
	 * Returns the Parameter set used to process this Field
	 * 
	 * @author BLM
	 */
	public Model_ParameterSet getParameterSet() {
		return TheParameterSet;
	}

	/**
	 * Sets the Parameter set used to process this Field
	 * 
	 * @author BLM
	 */
	public void setParameterSet(Model_ParameterSet pset) {
		TheParameterSet = pset;
		/* Write the parameter set to the HDF5 file for this well */

	}

	/**
	 * Tries to load the parameter set from the projects HDF5 file
	 * 
	 * @author BLM
	 * */
	public void loadParameterSet() {
		ImageRail_SDCube io = models.Model_Main.getModel().getImageRailio();
		TheParameterSet = new Model_ParameterSet();
		String h5path = models.Model_Main.getModel().getInputProjectPath()
				+ "/Data.h5";

		String pathToSample = io.getHashtable_in().get(
				io.getIndexKey(getParentWell().getPlate().getID(),
						getParentWell()
						.getWellIndex()));
		if (pathToSample != null)
			TheParameterSet.load(h5path, ("/" + pathToSample + "/Children/"
					+ getIndexInWell() + "/Meta"));

	}
}

