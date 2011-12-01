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

import features.Feature;
import gui.Gui_Plate;
import imagerailio.ImageRail_SDCube;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;

import sdcubeio.ExpDesign_Description;
import sdcubeio.ExpDesign_IO;
import sdcubeio.H5IO_Exception;


/**
 * The plate object that holds all the images and metadata. The wells it
 * contains also act as a heatmap for well mean data and can contain
 * mini-histograms of distributions of the single-cell features
 *
 * @author BLM
 */
public class Model_Plate
{
	/** Model_Plate Identifier*/
	private int ID;
	/** Handle on itself*/
	private Model_Plate ThePlate;
	/** Matrix of this plate's Wells*/
	private Model_Well[][] TheWells;
	/** Number of Rows of Wells*/
	private int NumRows;
	/** Number of Columns of Wells */
	private int NumCols;
	/** Stores the min/max Feature values for all wells - TODO move this to each well*/
	private float[][] MinMaxFeatureValues;
	/** Stores the log min/max Feature values for all wells - TODO move htis to each well*/
	private float[][] MinMaxFeatureValues_log;
	/** Number of Gaussians to fit to each mini-histogram*/
	private int FitGaussian;
	/** List of all MIDAS treatments */
	private ArrayList<ExpDesign_Description> AllTreatments;
	/** List of all MIDAS measurements */
	private ArrayList<ExpDesign_Description> AllMeasurements;
	/** */
	public Hashtable TheMetaDataHashtable;
	/** */
	public ExpDesign_IO TheMetaDataWriter;
	/** String Title for the plate that will be displayed above if desired */
	private String Title;
	/** */
	private Gui_Plate ThisGUI;
	
	/** Main Model_Plate Constructor
	 * @author BLM*/
	public Model_Plate(int numRows_, int numCols_, int ID_,
			boolean seekSampleIDsFromFiles)
	{
		ID = ID_;
		ThePlate = this;
		AllTreatments = new ArrayList<ExpDesign_Description>();
		AllMeasurements = new ArrayList<ExpDesign_Description>();
		NumRows = numRows_;
		NumCols = numCols_;
		initWells(seekSampleIDsFromFiles);
		FitGaussian = 0;
	}
	
	public void initGUI() {
		setGUI(new Gui_Plate(this));
	}

	/** Sets the GUI plate display associated with this ModelPlate */
	public void setGUI(Gui_Plate theGUI) {
		ThisGUI = theGUI;
	}
	

	// /** Looks for all the unique treatment combinations across all the wells
	// and adds them to a hashtable linked with a color for that treatment
	// combo.
	// * Note it doesnt compare quantitative amounts of treatment, just if that
	// treatment exists in the well. For example, if a well is treated with
	// * EGF and Iressa, the key to the hashtable would be "EGF+Iressa" and the
	// table would get a colore for that combination
	// * @author BLM*/
	// public Hashtable initMetaDataHashtable(Model_Plate plate,
	// ExpDesign_IO meta)
	// {
	// TheMetaDataHashtable = null;
	// int colorCounter = 0;
	// Hashtable hash = new Hashtable();
	// ArrayList<String> uniques = new ArrayList<String>();
	// //Getting treatments
	// for (int r = 0; r < plate.NumRows; r++)
	// for (int c = 0; c < plate.NumCols; c++)
	// {
	// int Type = getGUI().shouldDisplayMetaData();
	// ArrayList<String> arr = new ArrayList<String>();
	// if(Type == 0)
	// arr = meta.getAllTreatmentNames(plate.getPlateIndex(),
	// plate.TheWells[r][c].getWellIndex());
	// else if(Type == 1)
	// arr = meta.getAllMeasurementNames(plate.getPlateIndex(),
	// plate.TheWells[r][c].getWellIndex());
	// else if(Type == 2)
	// {
	// Description des = ((Description)meta.readDescription(
	// plate.TheWells[r][c].getWellIndex()));
	// if (des == null || des.getValue()== null)
	// arr= null;
	// else
	// arr.add(des.getValue());
	// }
	// else if(Type == 3)
	// {
	// Description des = ((Description)meta.readTimePoint(
	// plate.TheWells[r][c].getWellIndex()));
	// if (des == null || des.getValue()== null)
	// arr= null;
	// else
	// arr.add(des.getValue());
	// }
	//				
	//				
	// if (arr!=null && arr.size()>0)
	// {
	// //Now adding it to the hastable
	// String stringCat = "";
	// for (int i = 0; i < arr.size()-1; i++)
	// stringCat+=arr.get(i)+" + ";
	// stringCat+=arr.get(arr.size()-1);
	//					
	// if(hash.get(stringCat)==null)
	// {
	// hash.put(stringCat, tools.ColorRama.getColor(colorCounter));
	// colorCounter++;
	// }
	// }
	// }
	//		
	//		
	// return hash;
	// }
	
	/** If an HDF file of cell data exists for this plate and each well within, this method trys to load it into the RAM
	 * @author BLM*/
	static public void loadCellData(Model_Plate[] allPlates, boolean loadCoords,
			boolean loadDataVals)
	{
		ArrayList<Model_Well> selectedWells = new ArrayList<Model_Well>();
		for (int p = 0; p < allPlates.length; p++)
			for (int i = 0; i < allPlates[p].NumRows; i++)
				for (int j = 0; j < allPlates[p].NumCols; j++)
					if(allPlates[p].TheWells[i][j].isSelected())
						selectedWells.add(allPlates[p].TheWells[i][j]);
		
		CellLoader loader = new CellLoader(selectedWells, loadCoords,
				loadDataVals);
		loader.start();
	}
	

	// /** Each plate will have a single metadata connector to represent its
	// meta data. This method creates it
	// * @author BLM*/
	// private void initMetaDataConnector()
	// {
	// if(models.Model_Main.getModel().getProjectDirectory()!=null)
	// {
	// String projPath =
	// models.Model_Main.getModel().getProjectDirectory().getAbsolutePath();
	// TheMetaDataWriter = null;
	// try
	// {
	// System.out.println("METAcon Path: " + projPath);
	// TheMetaDataWriter = new MetaDataConnector(projPath);
	// }
	// catch(Exception e)
	// {
	// System.out.println("------* Error creating MetaData XML writer *------");
	// }
	// }
	// }
	
	/** Clears the RAM of all cell data across all plates in the program
	 * @author BLM*/
	public void clearCellData()
	{
		for (int i = 0; i < NumRows; i++)
			for (int j = 0; j < NumCols; j++)
			{
				Model_Field[] fields = TheWells[i][j].getFields();
				if(fields!=null)
				{
					int numF = fields.length;
					for (int z = 0; z < numF; z++)
						fields[z].killCells();
				}
			}
	}
	
	
	/** Returns the GUI representation of this plate */
	public Gui_Plate getGUI() {
		return ThisGUI;
	}
	
	
	/** Initialize the wells
	 * @author BLM*/
	public void initWells(boolean seekSampleIDsFromFiles)
	{
		// Creating hash's of all samples in both the hdf and xml files to help
		// speed extant sampleID search

		// Hashtable xmlHassh = new Hashtable();
		// Hashtable hdfHash = null;
		// if (models.Model_Main.getModel().getImageRailio() != null) {
		// hdfHash = models.Model_Main.getModel().getImageRailio()
		// .getHashtable();
		// }

		// sampleID = models.Model_Main.getModel().getImageRailio()
		// .readSampleID_fromHDF5(ID, counter);
		//		
		// sampleID = models.Model_Main.getModel().getImageRailio()
		// .readSampleID_fromXML(ID, counter);
		//		
		// long hdfTime = 0;
		// long xmlTime = 0;
		TheWells = new Model_Well[NumRows][NumCols];
		int counter = 0;
		for (int c = 0; c < NumCols; c++)
		for (int r = 0; r < NumRows; r++)
			{
				Model_Well well = new Model_Well(this, r, c);
				
				//Need to check if the HDF5 or XML files already have these wells such that we need to 
				//get the sampleID from them rather than create new ones
				String sampleID = null;

				if (seekSampleIDsFromFiles
						&& models.Model_Main.getModel().getImageRailio() != null) {

					// long sTime = System.currentTimeMillis();
					sampleID = models.Model_Main.getModel()
.getImageRailio()
						.readSampleID_fromHDF5(ID, counter);

					// String path = (String)hdfHash.get("p"+ID+"w"+counter);
					// if(path!=null)
					// {
					// sampleID =
					// }

					// hdfTime += (System.currentTimeMillis() - sTime);
					// sTime = System.currentTimeMillis();
					// System.out.println("hdfDone");
				if(sampleID==null) //Trying to get from XML if not in HDF5
						sampleID = models.Model_Main.getModel()
								.getImageRailio()
							.readSampleID_fromXML(ID, counter);
					// System.out.println("xmlDone");
					// xmlTime += (System.currentTimeMillis() - sTime);
				}

				if (sampleID == null)
					sampleID = "p" + ID + "w" + counter + "_t"
							+ imagerailio.ImageRail_SDCube.getTimeStamp();

				well.ID = sampleID;
				
				TheWells[r][c] = well;
				counter++;
			}
		// System.out.println("hdf: " + hdfTime + " xml: " + xmlTime);

	}
	
	/** */

	/** Returns an ArrayList of selected Wells ordered from left to right, top to bottom
	 * @author BLM*/
	public ArrayList<Model_Well> getSelectedWells_horizOrder()
	{
		ArrayList<Model_Well> arr = new ArrayList<Model_Well>();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].isSelected())
					arr.add(TheWells[r][c]);
		return arr;
	}
	
	
	/** Returns an ArrayList of selected Wells ordered from top to bottom, left to right
	 * @author BLM*/
	public ArrayList getSelectedWells_verticalOrder()
	{
		ArrayList arr = new ArrayList();
		for (int c = 0; c < NumCols; c++)
			for (int r = 0; r < NumRows; r++)
				if (TheWells[r][c].isSelected())
					arr.add(TheWells[r][c]);
		return arr;
	}
	
	/** Returns the selected Wells ordered from left to right, top to bottom in a Model_Well[] instead of ArrayList*/
	public Model_Well[] getAllSelectedWells()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].isSelected())
					temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Model_Well[] wells = new Model_Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Model_Well) temp.get(i);
		return wells;
	}
	
	/** Returns the Wells ordered from left to right, top to bottom in a Model_Well[] instead of ArrayList*/
	public Model_Well[] getAllWells()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Model_Well[] wells = new Model_Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Model_Well) temp.get(i);
		return wells;
	}
	
	/** Returns the Wells that contain images ordered from left to right, top to bottom in a Model_Well[] instead of ArrayList*/
	public Model_Well[] getAllWells_wImages()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].containsImages())
					temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Model_Well[] wells = new Model_Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Model_Well) temp.get(i);
		return wells;
	}

	/**
	 * Finds the min/max value across either allPlates or just this plate
	 * depending on the argument
	 * 
	 * MEAN == 0 CV == 1
	 * 
	 * @author BLM
	 */
	public float[] getMinMaxAcrossPlates(boolean normalizeAcrossAllPlates,
			int MeanOrCV)
	{
		float[] arr = new float[2];
		arr[0] = Float.POSITIVE_INFINITY;
		arr[1] = Float.NEGATIVE_INFINITY;
		
		if (normalizeAcrossAllPlates)
		{
			Model_Plate[] thePlates = models.Model_Main.getModel().getPlateRepository_GUI()
					.getModel().getPlates();
			int numPlates = thePlates.length;
			for (int p = 0; p < numPlates; p++)
			{
				Model_Plate plate = thePlates[p];
				for (int r = 0; r < plate.NumRows; r++)
					for (int c = 0; c < plate.NumCols; c++)
						if (plate.TheWells[r][c].Feature_Means != null && tools.MathOps.sum(plate.TheWells[r][c].Feature_Means)!=0 && plate.TheWells[r][c].Feature_Means.length > models.Model_Main.getModel().getTheSelectedFeature_Index())
						{
							if (MeanOrCV == 0) { // MEAN
							float val = 0;
							val = plate.TheWells[r][c].Feature_Means[models.Model_Main.getModel().getTheSelectedFeature_Index()];
							
							if (val < arr[0])
								arr[0] = val;
							if (val > arr[1])
								arr[1] = val;
							}
 else if (MeanOrCV == 1) { // CV
								float val = 0;
								val = plate.TheWells[r][c].Feature_Stdev[models.Model_Main
										.getModel()
										.getTheSelectedFeature_Index()]
										/ plate.TheWells[r][c].Feature_Means[models.Model_Main
												.getModel()
												.getTheSelectedFeature_Index()];

								if (val < arr[0])
									arr[0] = val;
								if (val > arr[1])
									arr[1] = val;
							}
						}

			}
		}
		else //Just this plate
		{
			Model_Plate plate = this;
			for (int r = 0; r < plate.NumRows; r++)
				for (int c = 0; c < plate.NumCols; c++)
					if (plate.TheWells[r][c].Feature_Means != null && tools.MathOps.sum(plate.TheWells[r][c].Feature_Means)!=0 && plate.TheWells[r][c].Feature_Means.length > models.Model_Main.getModel().getTheSelectedFeature_Index())
					{
						if (MeanOrCV == 0) {
						float val = 0;
						val = plate.TheWells[r][c].Feature_Means[models.Model_Main.getModel().getTheSelectedFeature_Index()];
						
						if (val < arr[0])
							arr[0] = val;
						if (val > arr[1])
							arr[1] = val;
						} else if (MeanOrCV == 1) { // CV
							float val = 0;

							val = plate.TheWells[r][c].Feature_Stdev[models.Model_Main
									.getModel().getTheSelectedFeature_Index()]
									/ plate.TheWells[r][c].Feature_Means[models.Model_Main
											.getModel()
											.getTheSelectedFeature_Index()];

							if (val < arr[0])
								arr[0] = val;
							if (val > arr[1])
								arr[1] = val;
						}
					}
		}
		
		return arr;
	}
	

	
	/** Returns the index in the plate holding panel of this plate, where the first plate index == 0
	 * @author BLM*/
	public int getPlateIndex()
	{
		return (getID());
	}
	
	
	/** Returns all the measurements taken on this plate
	 * @author BLM*/
	public ArrayList getAllMeasurements()
	{
		return AllMeasurements;
	}
	/** Sets all the measurements taken on this plate */
	public void setAllMeasurements(ArrayList measurements)
	{
		AllMeasurements = measurements;
	}
	/** Returns all the Treatments used on this plate
	 * @author BLM*/
	public ArrayList getAllTreatments()
	{
		return AllTreatments;
	}
	/** Sets all the treatments used on this plate */
	public void setAllTreatments(ArrayList treatments)
	{
		AllTreatments = treatments;
	}
	
	/** For each of N-features, returns a float matrix of 2xN where each element 0,1 is the min/max respectively and each element N
	 * is a different feature.  This array helps with the plate self normalization across features
	 * @author BLM*/
	public float[][] getMinMaxFeatureValues()
	{
		return MinMaxFeatureValues;
	}
	/** For each of N-features, returns a LOG(float matrix) of 2xN where each element 0,1 is the min/max respectively and each element N
	 * is a different feature.  This array helps with the plate self normalization across features
	 * @author BLM*/
	public float[][] getMinMaxFeatureValues_log()
	{
		return MinMaxFeatureValues_log;
	}
	
	
	/** Returns the wells that this plate is composed of
	 * @author BLM*/
	public Model_Well[][] getWells()
	{
		return TheWells;
	}
	
	/** Sets the Title of the plate
	 * @author BLM */
	public void setTitle(String title)
	{
		Title = title;
		// // writing to HDF file
		// if (models.Model_Main.getModel() == null
		// || models.Model_Main.getModel().getThePlateHoldingPanel() == null
		// || models.Model_Main.getModel().getThePlateHoldingPanel().getPlates() ==
		// null)
		// {
		// System.out.println("GUI and/or Plates are null: "
		// + models.Model_Main.getModel()
		// + ","
		// + models.Model_Main.getModel().getThePlateHoldingPanel()
		// + ","
		// + models.Model_Main.getModel().getThePlateHoldingPanel()
		// .getPlates());
		// return;
		// }
		// Model_Plate[] ps = models.Model_Main.getModel().getThePlateHoldingPanel()
		// .getPlates();
		//
		// int len = ps.length;
		// String[] names = new String[len];
		// for (int i = 0; i < len; i++)
		// names[i] = ps[i].getTitle();
		// names[getID()] = Title;
		//
		// ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		// if (io != null) {
		// io.writePlateNames(names);
		// System.out.println("Reading newly written plate names: ");
		// StringBuffer[] ns = io.readPlateNames();
		// len = ns.length;
		// for (int i = 0; i < len; i++) {
		// System.out.println(ns[i]);
		// }
		// }
	}
	/** Gets the Title of the plate
	 * @author BLM */
	public String getTitle()
	{
		// // loading plate names
		// ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		// if (io == null)
		// return "Plate #" + getID();
		//
		// StringBuffer[] plateNames = io.readPlateNames();
		// if (plateNames != null) {
		// // for (int i = 0; i < plateNames.length; i++) {
		// // System.out.println(plateNames[i]);
		// // }
		// if (plateNames != null && plateNames.length > 0)
		// return plateNames[getID()] + "";
		// }
		return Title;
	}
	
	/** Returns the plate ID
	 * @author BLM*/
	public int getID()
	{
		return ID;
	}
	
	/** Returns the number of columns in the plate
	 * 	 * @author BLM*/
	public int getNumColumns()
	{
		return TheWells[0].length;
	}
	
	/** Returns the number of rows in the plate
	 * @author BLM*/
	public int getNumRows()
	{
		return TheWells.length;
	}
	

	
	/** Returns a String[] containing all the names of the wells in the plate (ex: A01, A02, etc..)
	 * @author BLM*/
	public String[] getAllWellNames()
	{
		int counter = 0;
		String[] temp = new String[NumRows * NumCols];
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
			{
				temp[counter] = TheWells[r][c].name;
				counter++;
			}
		return temp;
	}
	

	
	/** Produces a copy of this plate
	 * @author BLM*/
	public Model_Plate copy()
	{
		Model_Plate plate = new Model_Plate(NumRows, NumCols, ID, false);
		plate.initGUI();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				plate.TheWells[r][c] = TheWells[r][c].copy(plate);
		
		return plate;
	}
	
	/** Returns the well within this plate with the given name (ex: name= A01)
	 * @author BLM*/
	public Model_Well getWell(String st)
	{
		Model_Well well = null;
		
		int c = getColumnIndex(st);
		int r = getRowIndex(st);
		
		return TheWells[r - 1][c - 1];
	}
	
	/**
	 * Returns the well within this plate with the given index
	 * 
	 * @author BLM
	 */
	public Model_Well getWell(int index) {
		int cols = getNumColumns();
		int rows = getNumRows();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (TheWells[i][j].getWellIndex() == index)
					return TheWells[i][j];
			}
		}
		return null;
	}

	/** Initializes the min/max values of all features within the plate
	 * @author BLM*/
	public void initMinMaxFeatureValues()
	{
		if (models.Model_Main.getModel().getTheFeatures() == null)
			return;
		
		int len = models.Model_Main.getModel().getTheFeatures().size();
		MinMaxFeatureValues = new float[2][len];
		for (int i = 0; i < len; i++)
		{
			MinMaxFeatureValues[0][i] = Float.POSITIVE_INFINITY;
			MinMaxFeatureValues[1][i] = Float.NEGATIVE_INFINITY;
		}
		MinMaxFeatureValues_log = new float[2][len];
		for (int i = 0; i < len; i++)
		{
			MinMaxFeatureValues_log[0][i] = Float.POSITIVE_INFINITY;
			MinMaxFeatureValues_log[1][i] = Float.NEGATIVE_INFINITY;
		}
	}
	
	public double[][] getBimodalDistance(Feature f)
	{
		double[][] dist = new double[NumRows][NumCols];
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				dist[r][c] = TheWells[r][c].getBimodalDistance(f);
		return dist;
	}
	
	
	
	/** Looks for HDF5 data containing the well means and stdevs for all wells in this plate and tries to laod it into the GUI 
	 * @throws H5IO_Exception */
	public void loadWellMeanAndStdevData() throws H5IO_Exception
	{
		System.out.println("Loading means and stdevs for plate: " + getID());
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		//Trying to write mean value data to file
		io.openHDF5(io.INPUT);

		int numRows = TheWells.length;
		int numCols = TheWells[0].length;
		int wellIndex =0;
		for (int  c = 0;  c < numCols;  c++) 
		for (int r = 0; r < numRows; r++) 
		{
				String pathToSample = io.getHashtable_in().get(
						io.getIndexKey(getID(), wellIndex));
			if (pathToSample != null) {

				float[] means = io.readWellMeans(getID(), wellIndex);
				float[] stdevs = io.readWellStdevs(getID(), wellIndex);
				if(means!=null & stdevs!=null)
 {
					TheWells[r][c].setWellMeansAndStdevs(means, stdevs);
					}
				else
 {
					System.out.println("**Error: found data for path: "+pathToSample + "  r,c: "+r +","+c +"  but could not load it properly");
					}
			}
			 wellIndex++;
		}

		if (ThisGUI != null)
			ThisGUI.repaint();

		io.closeHDF5();
	}
	
	



	// /** */
	// public MetaDataConnector getMetaDataConnector()
	// {
	// initMetaDataConnector();
	// return TheMetaDataWriter;
	// }
	/** */
	public Hashtable getMetaDataHashtable()
	{
		return TheMetaDataHashtable;
	}
	
	
	


	static public String getRowName(String wellName)
	{
		if (wellName.indexOf("A") >= 0)
			return "A";
		else if (wellName.indexOf("B") >= 0)
			return "B";
		else if (wellName.indexOf("C") >= 0)
			return "C";
		else if (wellName.indexOf("D") >= 0)
			return "D";
		else if (wellName.indexOf("E") >= 0)
			return "E";
		else if (wellName.indexOf("F") >= 0)
			return "F";
		else if (wellName.indexOf("G") >= 0)
			return "G";
		else if (wellName.indexOf("H") >= 0)
			return "H";
		else if (wellName.indexOf("I") >= 0)
			return "I";
		else if (wellName.indexOf("J") >= 0)
			return "J";
		else if (wellName.indexOf("K") >= 0)
			return "K";
		else if (wellName.indexOf("L") >= 0)
			return "L";
		else if (wellName.indexOf("M") >= 0)
			return "M";
		else if (wellName.indexOf("N") >= 0)
			return "N";
		else if (wellName.indexOf("O") >= 0)
			return "O";
		else if (wellName.indexOf("P") >= 0)
			return "P";
		
		return null;
	}
	
	static public String getRowName(int ind)
	{
		switch (ind)
		{
			case 0:
				return "A";
			case 1:
				return "B";
			case 2:
				return "C";
			case 3:
				return "D";
			case 4:
				return "E";
			case 5:
				return "F";
			case 6:
				return "G";
			case 7:
				return "H";
				
			case 8:
				return "I";
			case 9:
				return "J";
			case 10:
				return "K";
			case 11:
				return "L";
			case 12:
				return "M";
			case 13:
				return "N";
			case 14:
				return "O";
			case 15:
				return "P";
				
			default:
				return "";
		}
	}
	
	static public String getColumnName(int ind)
	{
		switch (ind)
		{
			case 0:
				return "01";
			case 1:
				return "02";
			case 2:
				return "03";
			case 3:
				return "04";
			case 4:
				return "05";
			case 5:
				return "06";
			case 6:
				return "07";
			case 7:
				return "08";
			case 8:
				return "09";
			case 9:
				return "10";
			case 10:
				return "11";
			case 11:
				return "12";
				
			case 12:
				return "13";
			case 13:
				return "14";
			case 14:
				return "15";
			case 15:
				return "16";
			case 16:
				return "17";
			case 17:
				return "18";
			case 18:
				return "19";
			case 19:
				return "20";
			case 20:
				return "21";
			case 21:
				return "22";
			case 22:
				return "23";
			case 23:
				return "24";
				
			default:
				return "" + (ind + 1);
		}
	}
	
	static public int getColumnIndex(String name)
	{
		if (name.indexOf("01") >= 0)
			return 1;
		else if (name.indexOf("02") >= 0)
			return 2;
		else if (name.indexOf("03") >= 0)
			return 3;
		else if (name.indexOf("04") >= 0)
			return 4;
		else if (name.indexOf("05") >= 0)
			return 5;
		else if (name.indexOf("06") >= 0)
			return 6;
		else if (name.indexOf("07") >= 0)
			return 7;
		else if (name.indexOf("08") >= 0)
			return 8;
		else if (name.indexOf("09") >= 0)
			return 9;
		else if (name.indexOf("10") >= 0)
			return 10;
		else if (name.indexOf("11") >= 0)
			return 11;
		else if (name.indexOf("12") >= 0)
			return 12;
		
		if (name.indexOf("13") >= 0)
			return 13;
		else if (name.indexOf("14") >= 0)
			return 14;
		else if (name.indexOf("15") >= 0)
			return 15;
		else if (name.indexOf("16") >= 0)
			return 16;
		else if (name.indexOf("17") >= 0)
			return 17;
		else if (name.indexOf("18") >= 0)
			return 18;
		else if (name.indexOf("19") >= 0)
			return 19;
		else if (name.indexOf("20") >= 0)
			return 20;
		else if (name.indexOf("21") >= 0)
			return 21;
		else if (name.indexOf("22") >= 0)
			return 22;
		else if (name.indexOf("23") >= 0)
			return 23;
		else if (name.indexOf("24") >= 0)
			return 24;
		
		return -1;
	}
	
	static public int getRowIndex(String name)
	{
		
		if (name.indexOf("A") >= 0)
			return 1;
		else if (name.indexOf("B") >= 0)
			return 2;
		else if (name.indexOf("C") >= 0)
			return 3;
		else if (name.indexOf("D") >= 0)
			return 4;
		else if (name.indexOf("E") >= 0)
			return 5;
		else if (name.indexOf("F") >= 0)
			return 6;
		else if (name.indexOf("G") >= 0)
			return 7;
		else if (name.indexOf("H") >= 0)
			return 8;
		else if (name.indexOf("I") >= 0)
			return 9;
		else if (name.indexOf("J") >= 0)
			return 10;
		else if (name.indexOf("K") >= 0)
			return 11;
		else if (name.indexOf("L") >= 0)
			return 12;
		else if (name.indexOf("M") >= 0)
			return 13;
		else if (name.indexOf("N") >= 0)
			return 14;
		else if (name.indexOf("O") >= 0)
			return 15;
		else if (name.indexOf("P") >= 0)
			return 16;
		
		return -1;
	}
	
	static public Color getRowColor(String name)
	{
		if (name.indexOf("A") >= 0)
			return tools.ColorRama.getColor(0);
		else if (name.indexOf("B") >= 0)
			return tools.ColorRama.getColor(1);
		else if (name.indexOf("C") >= 0)
			return tools.ColorRama.getColor(2);
		else if (name.indexOf("D") >= 0)
			return tools.ColorRama.getColor(3);
		else if (name.indexOf("E") >= 0)
			return tools.ColorRama.getColor(4);
		else if (name.indexOf("F") >= 0)
			return tools.ColorRama.getColor(5);
		else if (name.indexOf("G") >= 0)
			return tools.ColorRama.getColor(6);
		else if (name.indexOf("H") >= 0)
			return tools.ColorRama.getColor(7);
			
		else if (name.indexOf("I") >= 0)
			return tools.ColorRama.getColor(8);
		else if (name.indexOf("J") >= 0)
			return tools.ColorRama.getColor(9);
		else if (name.indexOf("K") >= 0)
			return tools.ColorRama.getColor(10);
		else if (name.indexOf("L") >= 0)
			return tools.ColorRama.getColor(11);
		else if (name.indexOf("M") >= 0)
			return tools.ColorRama.getColor(12);
		else if (name.indexOf("N") >= 0)
			return tools.ColorRama.getColor(13);
		else if (name.indexOf("O") >= 0)
			return tools.ColorRama.getColor(14);
		else if (name.indexOf("P") >= 0)
			return tools.ColorRama.getColor(15);
		
		return null;
	}
	
	static public Color getColColor(String name)
	{
		if (name.indexOf("01") >= 0)
			return tools.ColorRama.getColor(0);
		else if (name.indexOf("02") >= 0)
			return tools.ColorRama.getColor(1);
		else if (name.indexOf("03") >= 0)
			return tools.ColorRama.getColor(2);
		else if (name.indexOf("04") >= 0)
			return tools.ColorRama.getColor(3);
		else if (name.indexOf("05") >= 0)
			return tools.ColorRama.getColor(4);
		else if (name.indexOf("06") >= 0)
			return tools.ColorRama.getColor(5);
		else if (name.indexOf("07") >= 0)
			return tools.ColorRama.getColor(6);
		else if (name.indexOf("08") >= 0)
			return tools.ColorRama.getColor(7);
		else if (name.indexOf("09") >= 0)
			return tools.ColorRama.getColor(8);
		else if (name.indexOf("10") >= 0)
			return tools.ColorRama.getColor(9);
		else if (name.indexOf("11") >= 0)
			return tools.ColorRama.getColor(10);
		else if (name.indexOf("12") >= 0)
			return tools.ColorRama.getColor(11);
			
		else if (name.indexOf("13") >= 0)
			return tools.ColorRama.getColor(12);
		else if (name.indexOf("14") >= 0)
			return tools.ColorRama.getColor(13);
		else if (name.indexOf("15") >= 0)
			return tools.ColorRama.getColor(14);
		else if (name.indexOf("16") >= 0)
			return tools.ColorRama.getColor(15);
		else if (name.indexOf("17") >= 0)
			return tools.ColorRama.getColor(16);
		else if (name.indexOf("18") >= 0)
			return tools.ColorRama.getColor(17);
		else if (name.indexOf("19") >= 0)
			return tools.ColorRama.getColor(18);
		else if (name.indexOf("20") >= 0)
			return tools.ColorRama.getColor(19);
		else if (name.indexOf("21") >= 0)
			return tools.ColorRama.getColor(20);
		else if (name.indexOf("22") >= 0)
			return tools.ColorRama.getColor(21);
		else if (name.indexOf("23") >= 0)
			return tools.ColorRama.getColor(22);
		else if (name.indexOf("24") >= 0)
			return tools.ColorRama.getColor(23);
		
		return null;
	}
	
	/** Inner class thread runner*/
	static public class CellLoader implements Runnable
	{
		private Thread thread;
		private ArrayList<Model_Well> wells;
		private boolean loadCoords;
		private boolean loadDataVals;
		
		public CellLoader(ArrayList<Model_Well> wellsToLoad, boolean loadCoords,
				boolean loadDataVals)
		{
			this.loadDataVals = loadDataVals;
			this.loadCoords = loadCoords;
			wells = wellsToLoad;
		}
		
		public void start()
		{
			thread = new Thread(this);
			thread.start();
		}
		
		public void run()
		{
			// Project name
			// String projectPath =
			// models.Model_Main.getModel().getProjectDirectory().getAbsolutePath();
			ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
			if(wells!=null && wells.size()>0)
				for (int i = 0; i < wells.size(); i++)
					wells.get(i).loadCells(io, loadCoords, loadDataVals);
		}
		
	}

	/**
	 * Returns the max number of fields across all wells within this plate
	 * 
	 * @author BLM
	 */
	public int getMaxNumberOfFields() {
		int max = 0;
		for (int r = 0; r < NumRows; r++) {
			for (int c = 0; c < NumCols; c++) {
				Model_Field[] fields = TheWells[r][c].getFields();
				if (fields != null)
					if (max < fields.length)
						max = fields.length;
			}
		}
		return max;
	}
}

