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
import gui.Gui_Well;
import imagerailio.ImageRail_SDCube;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import midasGUI.Measurement;
import midasGUI.Treatment;
import plots.DotSelectionListener;
import plots.Gate_DotPlot;
import segmentedobject.Cell;
import segmentedobject.CellCoordinates;


public class Model_Well
{
	/** */
	private Model_Plate ThePlate;
	/** */
	public int Row;
	/** */
	public int Column;
	/** */
	public String ID;

	/** */
	private boolean selected;
	/** */
	public boolean processing;

	/** */
	private Model_Field[] TheFields;
	/** */
	public float[] Feature_Means;
	/** */
	public float[] Feature_Integrated;
	/** */
	public float[] Feature_Stdev;

	/** */
	public DotSelectionListener TheDotSelectionListener;

	//MIDAS fields:
	public String name;
	/** */
	public String date;
	/** */
	public String description;
	/** */
	public ArrayList<Treatment> treatments;
	/** */
	public ArrayList<Measurement> measurements;
	/** */
	public String measurementTime;

	/** */
	public int GateCounter;
	/** */
	public ArrayList<Gate_DotPlot> TheGates;
	/** */
	private boolean cellsModified;
	/** */
	private boolean loading;
	/** */
	private Gui_Well ThisGUI;
	
	/** Main Model_Well Constructor*/
	public Model_Well(Model_Plate plate, int row, int col)
	{
		ThePlate = plate;
		Row = row;
		Column = col;
		cellsModified = false;
		treatments = new ArrayList();
		measurements = new ArrayList();
		name = Model_Plate.getRowName(row)+Model_Plate.getColumnName(col);
		TheGates = new ArrayList<Gate_DotPlot>();
		GateCounter = 1;

		selected = false;

	}
	
	public void setGUI(Gui_Well gui) {
		ThisGUI = gui;
	}

	public Gui_Well getGUI() {
		return ThisGUI;
	}

	public void initGUI() {
		ThisGUI = new Gui_Well(this);
	}

	/** Returns the plate that this well belongs to
	 * @author BLM*/
	public Model_Plate getPlate()
	{
		return ThePlate;
	}
	
	/** Returns the fields that exist within this well
	 * @author BLM*/
	public Model_Field[] getFields()
	{
		return TheFields;
	}
	
	/** Sets the fields that exist within this well
	 * @author BLM*/
	public void setTheFields(Model_Field[] fields)
	{
		TheFields = fields;
	}

	/**
	 * If cells have been deleted, this will be triggered... at which point when
	 * the program is closed, it will ask we we want to make these changes
	 * permanent
	 * 
	 * @author BLM
	 */
	public void setCellsModified(boolean boo)
	{
		cellsModified = boo;
		if(boo)
			models.Model_Main.getModel().setCellsModified(true);

	}
	




	public boolean areCellsModified()
	{
		return cellsModified;
	}
	
	/** */
	public void setLoading(boolean boo)
	{
		loading = boo;
	}
	/** */
	public boolean isLoading()
	{
		return loading;
	}
	

	// /** */
	// public MetaDataConnector getMetaDataConnector()
	// {
	// return ThePlate.getMetaDataConnector();
	// }
	

	/** Returns all the cells from all the fields in this well
	 * @author BLM*/
	public synchronized ArrayList<Cell> getCells()
	{
		ArrayList<Cell> arr = new ArrayList<Cell>();
		
		if (TheFields == null || TheFields.length <= 0)
			return null;

		int numF = TheFields.length;
		for (int i = 0; i < numF; i++)
		{
			if (TheFields[i].getCells() != null) {
			ArrayList<Cell> cells = TheFields[i].getCells();
			if (cells!=null)
				arr.addAll(cells);
			}
		}
		
		return arr;
	}
	
	/** Returns all the cells from all the fields in this well.  If cells are not loaded but there is cell data
	 * within the HDF5 files, then it loads it from disk and then returns the cells
	 * @author BLM*/
	public synchronized ArrayList<Cell> getCells_forceLoad(boolean loadCoords, boolean loadDataVals)
	{
		ArrayList<Cell> arr = new ArrayList<Cell>();		
		if (TheFields == null || TheFields.length <= 0)
			return null;
		
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		int numF = TheFields.length;
		for (int i = 0; i < numF; i++)
		{
			if (TheFields[i].getCells() != null) {
				ArrayList<Cell> cells = TheFields[i].getCells();
				if (cells!=null)
					arr.addAll(cells);
			}
			else // See if we can load them from HDF5
			{
				TheFields[i].loadCells(io, loadCoords, loadDataVals);
				ArrayList<Cell> cells =  TheFields[i].getCells();
				if (cells!=null)
					arr.addAll(cells);
			}
		}
		
		return arr;
	}
	// /** Returns all the cells from all the fields in this well
	// * @author BLM*/
	// public ArrayList<CellCoordinates> getCell_coords()
	// {
	// ArrayList<CellCoordinates> arr = new ArrayList<CellCoordinates>();
	//		
	// int numF = TheFields.length;
	// for (int i = 0; i < numF; i++)
	// {
	// ArrayList<CellCoordinates> cells = TheFields[i].getCellCoords_all();
	// if (cells!=null)
	// arr.addAll(cells);
	// }
	//		
	// return arr;
	// }
	/**
	 * 
	 * Returns all the cells feature values from all the fields in this well
	 * appended into a single float matrix
	 * 
	 * @author BLM
	 */
	public synchronized float[][] getCell_values() {
		ArrayList<Cell> cells = getCells();
		if (cells == null || cells.size() == 0)
			return null;

		int numC = cells.size();
		float[][] arr = new float[numC][cells.get(0).getFeatureValues().length];

		for (int n = 0; n < numC; n++)
			arr[n] = cells.get(n).getFeatureValues();

		return arr;
	}

	/** Returns the index in the well in the plate. INdexing goes top to bottom, right to left
	 * @author BLM*/
	public int getWellIndex()
	{

		return (getPlate().getNumRows()*Column)+Row;
	}
	
	/**
	 * Returns the string Identifier for this well/sample
	 * 
	 * @author Bjorn Millard
	 * @return String ID
	 */
	public String getID() {
		return ID.trim();
	}

	/** Finds all cells that have a NULL value, purges them and recomputes the mean values of the well. Called after cells have been
	 * deleted via dot plot or filtering
	 * @author BLM*/
	public void purgeSelectedCellsAndRecomputeWellMeans()
	{


		//Now deleting selected cells from the CellRepository
		int numF = TheFields.length;
		for (int i = 0; i < numF; i++)
		{
			ArrayList<Cell> cells = TheFields[i].getCells();
			float[][] vals = TheFields[i].getFeatureVals_all();
			if (cells != null && vals != null)
			{
				ArrayList<CellCoordinates> temp_coords = new ArrayList<CellCoordinates> ();
				ArrayList<float[]> temp_vals = new ArrayList<float[]> ();
				ArrayList<Integer> ids = new ArrayList<Integer>();
				int numC = cells.size();

				long time2 = System.currentTimeMillis();

				for (int j = 0; j < numC; j++)
				{
					Cell cell = cells.get(j);
					if (!cell.isSelected())
					{
						temp_coords.add(cell.getCoordinates_copy());
						temp_vals.add(vals[j]);
						ids.add(new Integer(cell.getID()));
					}
			
				}
				
				//converting to right format and then setting them
				float[][] fvals = new float[temp_vals.size()][];
				for (int v = 0; v < temp_vals.size(); v++)
					fvals[v] = temp_vals.get(v);
			

				TheFields[i].getCellRepository()
.setCellData(ids, temp_coords,
						fvals);

			}
		}




		float[][] cellData = getCell_values();
		
		ArrayList<float[][]> tempArr = new ArrayList<float[][]>();
		tempArr.add(cellData);
		//recomputing means and Stdev
		float[][] data = getCellMeansAndStdev_allFeatures(tempArr);
		if (data==null)
			return;
		
		Feature_Means = null;
		Feature_Stdev = null;
		Feature_Means = data[0];
		Feature_Stdev = data[1];


	}
	
	
	/** Sets whether this well is selected in the GUI
	 * @author BLM*/
	public void setSelected(boolean boo)
	{
		selected = boo;
	}
	
	/** Returns whether this well is selected in the GUI
	 * @author BLM*/
	public boolean isSelected()
	{
		return selected;
	}
	
	/** Returns the mean value of selected feature
	 * @author BLM*/
	public float getCurrentValue_Mean()
	{
		if (Feature_Means==null)
			return 0;
		return Feature_Means[models.Model_Main.getModel().getTheSelectedFeature_Index()];
	}
	
	/** Returns the standard deviation value of selected feature
	 * @author BLM*/
	public float getCurrentValue_Stdev()
	{
		if (Feature_Stdev==null)
			return 0;
		return Feature_Stdev[models.Model_Main.getModel().getTheSelectedFeature_Index()];
	}
	
	/** Returns the coefficient of variation value of selected feature
	 * @author BLM*/
	public float getCurrentValue_CV()
	{
		return Feature_Means[models.Model_Main.getModel().getTheSelectedFeature_Index()]/Feature_Means[models.Model_Main.getModel().getTheSelectedFeature_Index()];
	}
	
	/**
	 * Returns the mean value of the feature with the given name
	 * 
	 * @author BLM
	 */
	public float getValue_Mean(String featureName) {
		if (Feature_Means == null)
			return 0;

		return Feature_Means[models.Model_Main.getModel().getFeature_Index(featureName)];
	}
	
	/**
	 * Returns the standard deviation value of the feature with the given name
	 * 
	 * @author BLM
	 */
	public float getValue_Stdev(String featureName) {
		if (Feature_Stdev == null)
			return 0;
		return Feature_Stdev[models.Model_Main.getModel().getFeature_Index(featureName)];
	}
	
	
	/** Determines if this well has cell data loaded into the RAM for display purposes
	 * @author BLM*/
	public boolean containsCellData()
	{
		if (TheFields!=null && TheFields.length>0)
		{
			int len = TheFields.length;
			for (int i = 0; i < len; i++)
				if (TheFields[i].getCellRepository()!=null && TheFields[i].areCellsLoaded())
					return true;
		}
		
		return false;
	}
	


	/** Looks at each field and determines how many HDF files of prior data are available for this well
	 * @author BLM*/
	public int getHDFcount()
	{
		int count = 0;
		if (TheFields==null)
			return 0;
		
		int len = TheFields.length;
		for (int i = 0; i < len; i++)
			if (TheFields[i].doesDataExist(models.Model_Main.getModel()
					.getInputProjectPath()
					+ "/Data.h5"))
 {
				count++;
			}
		return count;
	}
	
	
	/** Loads the cells from the HDF files if they exist for each field
	 * @author BLM*/
	public void loadCells(ImageRail_SDCube io, boolean loadCoords,
			boolean loadDataVals)
	{
		Model_Field[] fields = getFields();
		int numF = fields.length;
		setLoading(true);
		System.out.println();
		for (int z = 0; z < numF; z++)
			fields[z].loadCells(io, loadCoords, loadDataVals);
		setLoading(false);
		updateDataValues();
		getPlate().getGUI().repaint();
	}

	
	
	/** Toggles btw selected and unselected
	 * @author BLM*/
	public void toggleHighlightState()
	{
		selected = !selected;
	}
	
	/** Returns the value of the cell who has the highest and lowest value of this feature
	 * @return float[] min(0) and max(1)
	 * @author BLM*/
	public float[] getMinMaxValue(Feature f)
	{
		//TODO
//
		float[] vals = new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
//		int len = TheCells.length;
//		for (int i = 0; i < len; i++)
//		{
//			float val = (float)f.getValue(TheCells[i]);
//			if (val>vals[1])
//				vals[1] = val;
//			if (val<vals[0])
//				vals[0] = val;
//		}
//		System.out.println("min: "+vals[0]);
//		System.out.println("max: "+vals[1]);
		return vals;
	}
	
	
	
	public double getBimodalDistance(Feature f)
	{
		//TODO
//		int numBins = 50;
//		int len = TheCells.length;
//		if (len<=1)
//			return 0;
//
//		int[] bins = new int[numBins];
//		Feature feature = f;
//
//		double minVal = 0;
//		double maxVal = 1;
//
//		if (!models.Model_Main.getModel().getPlateHoldingPanel().isLogScaled())
//		{
//			minVal = ThePlate.getMinMaxFeatureValues()[0][models.Model_Main.getModel().getTheSelectedFeature_Index()];
//			maxVal = ThePlate.getMinMaxFeatureValues()[1][models.Model_Main.getModel().getTheSelectedFeature_Index()];
//		}
//		else
//		{
//			if (ThePlate.getMinMaxFeatureValues_log()!=null)
//			{
//				minVal = ThePlate.getMinMaxFeatureValues_log()[0][models.Model_Main.getModel().getTheSelectedFeature_Index()];
//				maxVal = ThePlate.getMinMaxFeatureValues_log()[1][models.Model_Main.getModel().getTheSelectedFeature_Index()];
//			}
//		}
//
//		//Binning values
//		for (int i = 0; i < len; i++)
//		{
//			double val = feature.getValue(TheCells[i]);
//			if (models.Model_Main.getModel().getPlateHoldingPanel().isLogScaled())
//			{
//				if (val<=1)
//					val = 1;
//				val = tools.MathOps.log(val);
//			}
//			else
//				if (val<=0)
//					val = 0;
//
//			if (val<10000000 && val>=1)
//			{
//				double normVal = (val-minVal)/(maxVal-minVal);
//				int ind = (int)(numBins*normVal);
//				if (ind>0&&ind<numBins-1)
//					bins[ind]++;
//			}
//		}
//		//Finding max bin value so we can normalize the histogram
//		float maxBinVal = 0;
//		for (int i = 0; i < numBins; i++)
//			if (bins[i]>maxBinVal)
//				maxBinVal=bins[i];
//
//		if (bins==null)
//			return -1;
//		if (bins.length<1)
//			return -1;
//
//		GaussianDistribution[] mono = tools.GaussianDistribution.fitGaussians(bins, 1);
//		GaussianDistribution[] bi = tools.GaussianDistribution.fitGaussians(bins, 2);
//
//		//Getting a real distribution out of the fitted parameters so we can plot it
//		int numB = bins.length;
//		double[][] monomodalResults_histo = new double[mono.length][numB];
//		tools.GaussianDistribution.getGaussian(mono[0].Parameters, monomodalResults_histo[0]);
//		//Getting real bimnodal distributions
//		double[][]  bimodalResults_histo = new double[bi.length][numB];
//		tools.GaussianDistribution.getGaussian(bi[0].Parameters, bimodalResults_histo[0]);
//		tools.GaussianDistribution.getGaussian(bi[1].Parameters, bimodalResults_histo[1]);
//
//		double error_mono = tools.GaussianDistribution.getPercentageError_L1Dist(monomodalResults_histo, bins);
//		double error_bi = tools.GaussianDistribution.getPercentageError_L1Dist(bimodalResults_histo, bins);
//
//		if (Math.abs(bi[0].Parameters[1]-bi[1].Parameters[1])<0.05f || bi[1].Parameters[0]/bi[0].Parameters[0] <0.1 || bi[1].Parameters[1] > 1)
//			return 0;
//		else
//			if (error_bi<error_mono)
//				return Math.abs(bi[0].Parameters[1]-bi[1].Parameters[1]);
		return 0;
	}
	
	/** Returns the mean and stdev values of the given data value Matrix, Rows of matrix = number of features, COls = number of cells
	 * @author BLM
	 * @return float[][] where float[].length = 2 (mean/stdev) and float[][].length = numFeatures*/
	private float[][] getCellMeansAndStdev_allFeatures(ArrayList<float[][]> dataSets)
	{
		if (dataSets==null || dataSets.size()==0)
			return null;
		int numDataSets = dataSets.size();
		
		float[][] dataMatrix = dataSets.get(0);
		if (dataMatrix==null || dataMatrix.length==0 || dataMatrix[0].length==0)
			return null;
		
		
		int numF = dataMatrix[0].length;
		float[][] data = new float[2][numF];
		float[] mean = new float[numF];
		float[] stdev = new float[numF];
		int[] counter = new int[numF];
		//Computing all the sums for the means, then divide by sample number
		for (int z = 0; z < numDataSets; z++)
		{
			dataMatrix = dataSets.get(z);
			if (dataMatrix==null || dataMatrix.length==0 || dataMatrix[0].length==0)
				return null;
			
			
			int numCells = dataMatrix.length;
			
			for (int j = 0; j < numCells; j++)
				for (int i = 0; i < numF; i++)
				{
					double val = dataMatrix[j][i];
					if (val>-1000000000 && val<1000000000)
					{
						mean[i] += val;
						counter[i]++;
					}
				}
		}
		//Dividing sums by sample number to create real Mean Value
		for (int i = 0; i < numF; i++)
			mean[i] = (mean[i]/(float)counter[i]);
		
		//Now computing the STDEV. Needed the means for this computation
		for (int z = 0; z < numDataSets; z++)
		{
			dataMatrix = dataSets.get(z);
			if (dataMatrix==null || dataMatrix.length==0 || dataMatrix[0].length==0)
				return null;
			
			int numCells = dataMatrix.length;
			//computing the stdev
			for (int j = 0; j < numCells; j++)
				for (int i = 0; i < numF; i++)
				{
					double val = dataMatrix[j][i];
					if (val>-1000000000 && val<1000000000)
						stdev[i] += Math.pow(val - mean[i],2);
				}
		}
		//Now dividing after all data sets have been accounted for
		for (int i = 0; i < numF; i++)
			stdev[i] = (float)Math.sqrt((1f/(counter[i]-1f)*stdev[i]));
		
		data[0] = mean;
		data[1] = stdev;
		
		return data;
	}
	
	public void updateDataValues()
	{
		int numF = TheFields.length;
		ArrayList<float[][]> vals  = new ArrayList<float[][]>();
		for (int i = 0; i < numF; i++)
		{
			if (TheFields[i].areCellsLoaded())
				vals.add(TheFields[i].getFeatureVals_all());
		}
		
		float[][] data = getCellMeansAndStdev_allFeatures(vals);
		if (data==null)
			return;
		
		Feature_Means = data[0];
		Feature_Stdev = data[1];
		
		//updating the min/max of plate date
		models.Model_Main.getModel().getPlateRepository_GUI().getModel().updateMinMaxValues();
		
	}
	
	/** Takes in a bunch of cell float values, computes and sets the means and stdevs of the distributions across all features for all fields given
	 * @author BLM*/
	public void setDataValues(ArrayList<float[][]> allData)
	{
		float[][] data = getCellMeansAndStdev_allFeatures(allData);
		if (data==null)
			return;
		
		Feature_Means = data[0];
		Feature_Stdev = data[1];
		
		//updating the min/max of plate date
		models.Model_Main.getModel().getPlateRepository().updateMinMaxValues();
	}
	
	/** Sets this wells means and stdevs with the given float arrays
	 * @author BLM*/
	public void setWellMeansAndStdevs(float[] means, float[] stdevs)
	{
		Feature_Means = means;
		Feature_Stdev = stdevs;
		
		//updating the min/max of plate date
		models.Model_Main.getModel().getPlateRepository()
				.updateMinMaxValues();
	}
	
	
	
	/** Set the Mean/std values of the well directly
	 * @author BLM*/
	public void setMeanFluorescentValues(float[] val)
	{
		Feature_Means = val;
		int numChannels = val.length;
		Feature_Stdev = new float[numChannels];
		for (int i =0; i < numChannels; i++)
			Feature_Stdev[i] = 0;
	}
	
	/** Determines if there are any images in this well
	 * @author BLM*/
	public boolean containsImages()
	{
		if(TheFields!=null
		   && TheFields.length>0)
			return true;
		
		
		return false;
	}
	
	/** Set the Mean/std values of the well directly
	 * @author BLM*/
//	public void setIntegratedFluorescentValues(float[] val)
//	{
//		Feature_Integrated = val;
//	}
	
//	public ArrayList getChannelImageFiles(String channelName)
//	{
//		ArrayList arr = new ArrayList();
//		int num = channelImageFiles.size();
//		for (int i=0; i <num; i++)
//			if (((File)channelImageFiles.get(i)).getName().indexOf(channelName)>0)
//				arr.add(channelImageFiles.get(i));
//		return arr;
//	}
	
//	public ArrayList getAllSetsOfCorresponsdingChanneledImageFiles()
//	{
//		ArrayList arr = new ArrayList();
//		int num = channelImageFiles.size();
//		if (num==0)
//			return arr;
//		byte[] flags = new byte[num];
//		for (int i=0; i <num; i++)
//			flags[i] = 0;
//
//
//		String[] names = MainGUI.TheMainGUI.ChannelNames;
//		int numChannels = names.length;
////		System.out.println("numChannels: "+numChannels);
////		for (int i = 0; i < numChannels; i++)
////			System.out.println(""+names[i]);
//
//		for (int i=0; i <num; i++)
//		{
//			if (flags[i]==0)
//			{
//				ArrayList temp = new ArrayList();
//				File currFile = (File)channelImageFiles.get(i);
//				temp.add(currFile);
//				flags[i] = 1;
//				String prefix = tools.ImageTools.getFilePrefix_woChannel(currFile);
//
//				for (int j=0; j <num; j++)
//				{
//					if (flags[j]==0 &&  ((File)channelImageFiles.get(j)).getName().indexOf(prefix)>=0)
//					{
//						flags[j] = 1;
//
//						temp.add(channelImageFiles.get(j));
//					}
//				}
//
//				temp = tools.ImageTools.sortFilesByChannels_lowToHigh(temp, names);
//
//				int len = temp.size();
//				File[] files = new File[len];
//				for (int j=0; j < len; j++)
//					files[j] = (File)temp.get(j);
//				arr.add(files);
//			}
//
//		}
//
//		return arr;
//	}
	
	public Model_Well copy(Model_Plate plate)
	{
		Model_Well well = new Model_Well(plate, Row, Column);
		well.ID = ID;
		if (getGUI() != null)
			well.initGUI();
		well.Feature_Means = Feature_Means;
		well.Feature_Stdev = Feature_Stdev;
		// Note, I am not copying over the Image Files
		return well;
	}
	
	
	// public void printParameterSet(PrintWriter pw) {
	// pw.println("<Model_Well  name='" + name
	// + "' "
	// + "  Processed='"
	// + TheParameterSet.getProcessType()
	// + "' "
	// + "  ThreshChannel_Nuc='"
	// + TheParameterSet.getParameter_String("Thresh_Nuc_ChannelName")
	// + "' "
	// + "  ThreshChannel_Cyt='"
	// + TheParameterSet.getParameter_String("Thresh_Cyt_ChannelName")
	// + "' "
	// + "  Thresh_Nuc_Value='"
	// + TheParameterSet.getParameter_float("Thresh_Nuc_Value")
	// + "' "
	// + "  Thresh_Cell='"
	// + TheParameterSet.getParameter_float("Thresh_Cyt_Value")
	// + "' "
	// + "  Thresh_Bkgd_Value='"
	// + TheParameterSet.getParameter_float("Thresh_Bkgd_Value")
	// + "' "
	// +
	// // "  TopXBrightestPixels='"+TheParameterSet.TopXBrightPix+"' "
	// // +
	// "  AnnulusSize='"
	// + TheParameterSet.getAnnulusSize()
	// + "' "
	// + "  MeanOrIntegrated='"
	// + TheParameterSet.getMeanOrIntegrated()
	// + "' "
	// + "  StoreCells='"
	// + models.Model_Main.getModel().getLoadCellsImmediatelyCheckBox()
	// .isSelected() + "' " +
	// //
	// "  StorePixelInfo='"+models.Model_Main.getModel().getStorePixelInformationCheckBox().isSelected()+"' "
	// // +
	// //
	// "  StoreMembraneRegion='"+models.Model_Main.getModel().getStoreMembranesCheckBox().isSelected()+"' "
	// // +
	// " />");
	// }

	public void deleteSelectedCells() {
		purgeSelectedCellsAndRecomputeWellMeans();
		models.Model_Main.getModel().getPlateRepository_GUI().getModel().updateMinMaxValues();
		models.Model_Main.getModel().getGUI().updateAllPlots();
	}

	/** Clears the RAM of all cell data in this well, not mean values
	 * @author BLM*/
	public void clearCellData()
	{
				Model_Field[] fields = getFields();
				if(fields!=null)
				{
					int numF = fields.length;
					for (int z = 0; z < numF; z++)
						fields[z].killCells();
				}
	}
	
	public void clearOldData() {
		Model_Field[] fields = getFields();
		if (fields != null) {
			int numF = fields.length;
			for (int z = 0; z < numF; z++)
				fields[z].killCells();
		}

		if (Feature_Means != null)
			for (int i = 0; i < Feature_Means.length; i++)
				Feature_Means[i] = 0;
		if (Feature_Integrated != null)
			for (int i = 0; i < Feature_Integrated.length; i++)
				Feature_Integrated[i] = 0;
		if (Feature_Stdev != null)
			for (int i = 0; i < Feature_Stdev.length; i++)
				Feature_Stdev[i] = 0;

		if (!GraphicsEnvironment.isHeadless())
			ThePlate.getGUI().updatePanel();
		System.gc();

	}


}


