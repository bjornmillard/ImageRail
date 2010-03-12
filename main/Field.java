/**
 * Field.java
 *
 * @author Bjorn Millard
 */

package main;

/** Holder for images and objects pertinent to a single field aquired in a well
 * @author BLM*/
import hdf.SegmentationHDFConnector;
import idx2coordinates.IdxConverter;

import java.awt.Shape;
import java.io.File;
import java.util.ArrayList;

import segmentedObj.Cell;

public class Field {
	private Well parentWell;
	private int indexInWell;
	private File[] ImageFiles;
	private ArrayList<Shape> ROIs;
	private ArrayList<Boolean> ROIs_selected;
	private Cells_oneField TheCellRepository;
	private float[] backgroundValues;

	public Field(File[] imageFiles, int index, Well parentWell_) {
		parentWell = parentWell_;
		indexInWell = index;
		ImageFiles = imageFiles;
		ROIs = new ArrayList<Shape>();
		ROIs_selected = new ArrayList<Boolean>();
	}

	public int getIndexInWell() {
		return indexInWell;
	}

	public Well getParentWell() {
		return parentWell;
	}

	public void addROI(Shape roi) {
		ROIs.add(roi);
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
	}

	public void deleteROI(int index) {
		ROIs.remove(index);
		ROIs_selected.remove(index);
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
	 * Checks if this field has a cooresponding HDF data file for it
	 * 
	 * @author BLM
	 */
	public boolean doesHDFexist(String projPath, String algoName) {
		int plateIdx = parentWell.getPlate().getID() - 1;
		int wellIdx = (parentWell.getPlate().getNumRows() * parentWell.Column)
				+ parentWell.Row;
		int plateSize = (parentWell.getPlate().getNumRows() * parentWell
				.getPlate().getNumColumns());

		// Create directory structure.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx
				+ "/well_" + IdxConverter.index2well(wellIdx, plateSize)
				+ "/field_" + getIndexInWell() + ".h5";

		File f = new File(fName);

		// + "/field_" + getIndexInWell() + ".h5"
		return f.exists();
	}

	/**
	 * Loads the cells from the HDF file on the hardrive into RAM if they exist
	 * 
	 * @author BLM
	 */
	public synchronized void loadCells(SegmentationHDFConnector sCon,
			boolean loadCoords, boolean loadDataVals) {
		if (doesHDFexist(main.MainGUI.getGUI().getProjectDirectory()
				.getAbsolutePath(), "Data")) {
			try {
				TheCellRepository = new Cells_oneField(this, sCon, loadCoords,
						loadDataVals);
			} catch (Exception e) {
			}
			;
		}
	}

	/** */
	public void resaveCells(SegmentationHDFConnector sCon) {

		if (TheCellRepository != null)
			TheCellRepository.resaveCells(sCon);
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
	public ArrayList<Cell> getCells()
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
	public Cells_oneField getCellRepository() {
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
			segmentedObj.Point p = cells.get(i).getCoordinates()
					.getComCoordinates(0)[0];
			if(roi.contains(p.x,p.y))
				counter++;
		}
		return counter;
	}
}

