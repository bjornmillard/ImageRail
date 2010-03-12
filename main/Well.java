/**
 * Well.java
 *
 * @author Bjorn L Millard
 */

package main;

import features.Feature;
import hdf.SegmentationHDFConnector;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import midasGUI.Measurement;
import midasGUI.Treatment;
import plots.DotSelectionListener;
import plots.Gate_DotPlot;
import segmentedObj.Cell;
import segmentedObj.CellCoordinates;
import xml_metaData.Description;
import xml_metaData.MetaDataConnector;


public class Well
{
	/** */
	private Plate ThePlate;
	/** */
	public int Row;
	/** */
	public int Column;
	/** */
	public int ID;
	/** */
	public Rectangle outline;
	/** */
	private boolean selected;
	/** */
	public boolean processing;
	/** */
	public Color color;
	/** */
	public Color color_outline;
	/** */
	private Field[] TheFields;
	/** */
	public float[] Feature_Means;
	/** */
	public float[] Feature_Integrated;
	/** */
	public float[] Feature_Stdev;
	/** */
	public DotSelectionListener TheDotSelectionListener;
	/** */
	public ParameterSet TheParameterSet;
	// /** */
	// public WellHistograms TheHistograms;
	/** */
	static final public AlphaComposite translucComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
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
	private Color initColor = Color.black;
	/** */
	public boolean AllowImageCountDisplay;
	/** */
	public int GateCounter;
	/** */
	public ArrayList<Gate_DotPlot> TheGates;
	/** */
	private boolean cellsModified;
	/** */
	private boolean loading;
	
	/** Main Well Constructor*/
	public Well(Plate plate, int row, int col)
	{
		ThePlate = plate;
		Row = row;
		Column = col;
		cellsModified = false;
		color = initColor;
		color_outline = Color.white;
		treatments = new ArrayList();
		measurements = new ArrayList();
		name = Plate.getRowName(row)+Plate.getColumnName(col);
		TheGates = new ArrayList<Gate_DotPlot>();
		GateCounter = 1;
		TheParameterSet = new ParameterSet();
		selected = false;
		outline = new Rectangle();
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();
		AllowImageCountDisplay = true;
	}
	
	/** Returns the plate that this well belongs to
	 * @author BLM*/
	public Plate getPlate()
	{
		return ThePlate;
	}
	
	/** Returns the fields that exist within this well
	 * @author BLM*/
	public Field[] getFields()
	{
		return TheFields;
	}
	
	/** Sets the fields that exist within this well
	 * @author BLM*/
	public void setTheFields(Field[] fields)
	{
		TheFields = fields;
	}
	/** If cells have been deleted, this will be triggered... at which point when the program is closed, it will ask we we want to make
	 * these changes permenant
	 * @author BLM*/
	public void setCellsModified(boolean boo)
	{
		cellsModified = boo;
		if(boo)
			MainGUI.getGUI().setCellsModified(true);

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
	
	/** */
	public MetaDataConnector getMetaDataConnector()
	{
		return ThePlate.getMetaDataConnector();
	}
	

	/** Returns all the cells from all the fields in this well
	 * @author BLM*/
	public ArrayList<Cell> getCells()
	{
		ArrayList<Cell> arr = new ArrayList<Cell>();
		
		int numF = TheFields.length;
		for (int i = 0; i < numF; i++)
		{
			ArrayList<Cell> cells = TheFields[i].getCells();
			if (cells!=null)
				arr.addAll(cells);
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
	public float[][] getCell_values() {
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
	
	/** Finds all cells that have a NULL value, purges them and recomputes the mean values of the well. Called after cells have been
	 * deleted via dot plot or filtering
	 * @author BLM*/
	public void purgeSelectedCellsAndRecomputeWellMeans()
	{
		
//		int totCells = getCell_coords().size();
//		System.out.println("TOTcells: "+totCells);
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
		
//		System.out.println("selected Cells: "+counter);

		
		//
		//
		//
		//Adding unselected cells to the keep list
//		ArrayList<CellCoordinates> cells = getCell_coords();
		float[][] cellData = getCell_values();
//		if (cells==null)
//			return;
//		int len = cells.size();
//		ArrayList<float[]> temp= new ArrayList<float[]>();
//		for (int i = 0; i < len; i++)
//			if (!cells.get(i).isSelected())
//			{
//				temp.add(cellData[i]);
//			}
//			else
//				System.out.println("Selected_1");
//		int num = temp.size();
//		if(num<1)
//			return;
		
		//copying keeper cells' feature array values
//		float[][] newVals = new float[num][temp.get(0).length];
//		for (int i = 0; i < num; i++)
//		{
//			float[] oneCellVals = temp.get(i);
//			for (int j = 0; j < oneCellVals.length; j++)
//				newVals[i][j] = oneCellVals[j];
//		}
		
		ArrayList<float[][]> tempArr = new ArrayList<float[][]>();
		tempArr.add(cellData);
		//recomputing means and Stdev
		float[][] data = getCellMeansAndStdev_allFeatures(tempArr);
		if (data==null)
			return;
		
		
//		float val = Feature_Means[0];
		Feature_Means = null;
		Feature_Stdev = null;
		Feature_Means = data[0];
		Feature_Stdev = data[1];
//		System.out.println("Means: "+val+"   "+Feature_Means[0]);

//		tempArr = null;
//		temp_vals = null;
//		temp = null;
//		newVals = null;
	}
	
//	/** Returns the mean and stdev values of the given cells for all features loaded into the MainGUI
//	 * @author BLM
//	 * @return float[][] where float[].length = 2 (mean/stdev) and float[][].length = numFeatures*/
//	public float[][] getCellMeansAndStdev_allFeatures(Cell_coords[] cells)
//	{
//		if (cells==null || cells.length<=0)
//			return null;
//
//		int numF = MainGUI.getGUI().getTheFeatures().size();
//
//		int numCells = cells.length;
//		float[][] data = new float[2][numF];
//		float[] mean = new float[numF];
//		float[] stdev = new float[numF];
//
//		int[] counter = new int[numF];
//		for (int j = 0; j < numCells; j++)
//			for (int i = 0; i < numF; i++)
//			{
//				Feature f = (Feature)MainGUI.getGUI().getTheFeatures().get(i);
//				double val = f.getValue(cells[j]);
//				if (val>-1000000000 && val<1000000000)
//				{
//					mean[i] += val;
//					counter[i]++;
//				}
//			}
//		for (int i = 0; i < numF; i++)
//			mean[i] = (mean[i]/(float)counter[i]);
//
//		//computing the stdev
//		for (int j = 0; j < numCells; j++)
//			for (int i = 0; i < numF; i++)
//			{
//				Feature f = (Feature)MainGUI.getGUI().getTheFeatures().get(i);
//				double val = f.getValue(cells[j]);
//				if (val>-1000000000 && val<1000000000)
//					stdev[i] += Math.pow(val - mean[i],2);
//			}
//
//		for (int i = 0; i < numF; i++)
//			stdev[i] = (float)Math.sqrt((1f/(counter[i]-1f)*stdev[i]));
//
//
//		data[0] = mean;
//		data[1] = stdev;
//		return data;
//
//	}
	
	
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
		return Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()];
	}
	
	/** Returns the standard deviation value of selected feature
	 * @author BLM*/
	public float getCurrentValue_Stdev()
	{
		if (Feature_Stdev==null)
			return 0;
		return Feature_Stdev[MainGUI.getGUI().getTheSelectedFeature_Index()];
	}
	
	/** Returns the coefficient of variation value of selected feature
	 * @author BLM*/
	public float getCurrentValue_CV()
	{
		return Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()]/Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()];
	}
	
	
	
	/** Computes the width of this Well in the GUI
	 * @author BLM*/
	public int getWidth()
	{
		int val = 5;
		if (ThePlate.getNumColumns() == 24)
			val = 4;
		if (ThePlate.getNumColumns() == 12)
			val = 5;
		if (ThePlate.getNumColumns() == 6)
			val = 7;
		if (ThePlate.getNumColumns() == 4)
			val = 10;
		if (ThePlate.getNumColumns()== 3)
			val = 12;
		
		int bufferX = 0;
		int bufferY = 50;
		float panelW = ThePlate.getWidth()-bufferX;
		float panelH = ThePlate.getHeight()-bufferY;
		float ratio = panelW/panelH;
		float desiredRatio = 12f/8f;
		if (ratio<desiredRatio)
			return (int)(panelW/ThePlate.getNumColumns()-val);
		return (int)(panelH/ThePlate.getNumRows()-val);
	}
	
	/** Computes the height of this Well in the GUI
	 * @author BLM*/
	public int getHeight()
	{
		int val = 5;
		if (ThePlate.getNumRows() == 16)
			val = 4;
		if (ThePlate.getNumRows() == 8)
			val = 5;
		if (ThePlate.getNumRows() == 4)
			val = 7;
		if (ThePlate.getNumRows() == 2)
			val = 10;
		if (ThePlate.getNumRows()== 2)
			val = 12;
		
		int bufferX = 0;
		int bufferY = 50;
		float panelW = ThePlate.getWidth()-bufferX;
		float panelH = ThePlate.getHeight()-bufferY;
		float ratio = panelW/panelH;
		float desiredRatio = 12f/8f;
		if (ratio<desiredRatio)
			return (int)(panelW/ThePlate.getNumColumns()-val);
		return (int)(panelH/ThePlate.getNumRows()-val);
	}
	
	/** Computes the xPosition of this Well in the GUI
	 * @author BLM*/
	public int getXpos()
	{
		return (int)(ThePlate.getXstart()+Column*(outline.width+3));
	}
	
	/** Computes the yPosition of this Well in the GUI
	 * @author BLM*/
	public int getYpos()
	{
		return (int)(ThePlate.getYstart()+Row*(outline.height+3));
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
	
	/** Draws this Well in the GUI Plate
	 * @author BLM*/
	public void draw(Graphics2D g2)
	{
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();
		
		if(color!=null)
			g2.setColor(color);
		
		//Seeing if we want to plot well according to metadata
		if(ThePlate.shouldDisplayMetaData()>-1)
		{
			int Type = ThePlate.shouldDisplayMetaData();
			ArrayList<String> arr = new ArrayList<String>();
			if(Type == 0) //Treatments
				arr =  ThePlate.getMetaDataConnector().getAllTreatmentNames(ThePlate.getPlateIndex(), getWellIndex());
			else if(Type == 1) //Measurements
				arr =  ThePlate.getMetaDataConnector().getAllMeasurementNames(ThePlate.getPlateIndex(), getWellIndex());
			else if(Type == 2) //Descriptions
			{
				Description des = ((Description)ThePlate.getMetaDataConnector().readDescription( getWellIndex()));
				if (des == null || des.getValue()== null)
					arr= null;
				else
					arr.add(des.getValue());
			}
			else if(Type == 3)
			{
				Description des = ((Description)ThePlate.getMetaDataConnector().readTimePoint(getWellIndex()));
				if (des == null || des.getValue()== null)
					arr= null;
				else
					arr.add(des.getValue());
			}
			
			
			if (arr!=null && arr.size()>0)
			{
				Hashtable hash = ThePlate.getMetaDataHashtable();
				//Now adding it to the hastable
				String treatsCat = "";
				for (int i = 0; i < arr.size()-1; i++)
					treatsCat+=arr.get(i)+" + ";
				treatsCat+=arr.get(arr.size()-1);
				
				Color color2 = (Color)hash.get(treatsCat);
				if(color2!=null)
					g2.setColor(color2);
			}
		}
		
		
		g2.fillRect(outline.x, outline.y, outline.width, outline.height);
		
		
		//384 well plates get too busy to have a white border
		
		if (processing)
			g2.setColor(Color.green);
		else if (!selected)
			g2.setColor(color_outline);
		else if (selected)
			g2.setColor(Color.red);
		g2.drawRect(outline.x, outline.y, outline.width, outline.height);
		
		if (!selected&&!processing)
			g2.setColor(color_outline);
		
		
		//Drawing the number of images text display
		if (AllowImageCountDisplay && MainGUI.getGUI().getDisplayNumberLoadedImagesCheckBox()!=null &&
			MainGUI.getGUI().getDisplayNumberLoadedImagesCheckBox().isSelected() && TheFields!=null)
		{
			g2.setFont(new Font("Helvetca", Font.PLAIN, 9));
			if (TheFields.length>0)
				g2.drawString((TheFields.length+"x"+TheFields[0].getNumberOfChannels()), (outline.x+outline.width/2f-9),(outline.y+outline.height/2f+5));
		}
		
		//Checking to see if any HDF files are available for this well
		if (MainGUI.getGUI().shouldDisplayHDFicons())
		{
			int numHDF = getHDFcount();
			if (numHDF>0)
			{
				int xS = outline.x+5;
				int yS = outline.y+5;
				int hdfIco_width = 5;
				int hdfIco_height = 8;
				int offset = 3;
				int xCounter = 0;
				int yCounter = 0;
				for (int i = 0; i < numHDF; i++)
				{
					int xStart = xS+xCounter*(hdfIco_width+offset);
					int yStart = yS+yCounter*(hdfIco_height+offset);
					
					if ((xStart+hdfIco_width)>(outline.x+outline.width))
					{
						xCounter = 0;
						yCounter++;
						xStart = xS+xCounter*(hdfIco_width+offset);
						yStart = yS+yCounter*(hdfIco_height+offset);
					}
					
					int xEnd = xStart+hdfIco_width;
					int yEnd = yStart+hdfIco_height;
					drawHDFicon(g2, xStart, yStart, xEnd, yEnd);
//					g2.setColor(Color.white);
//					g2.fillOval(xStart, yStart, 3,3);
					xCounter++;
				}
			}
		}
		
		
//		String projPath = main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
//		MetaDataConnector TheMetaDataWriter = null;
//		try
//		{
//			TheMetaDataWriter = new MetaDataConnector(projPath);
//		}
//		catch(Exception e)
//		{
//			System.out.println("------* Error creating MetaData XML writer *------");
//		}
//		if (TheMetaDataWriter!=null)
//		{
//			Description[] arr  = TheMetaDataWriter.readTreatments(getPlate().getPlateIndex(), getWellIndex());
//			int len = arr.length;
//			g2.setColor(Color.cyan);
//			for (int i = 0; i < len; i++)
//			{
//				if (arr[i].getName().equalsIgnoreCase("gefitinib"))
//				{
////				g2.drawString(arr[i].getName(), outline.x+5 , 10+outline.y+i*10);
//					float val = Float.parseFloat(arr[i].getValue());
//
////					float norm = (float)(val/1.6e-8);
//					float norm = (float)(val/3.2);
//					g2.setColor(new Color(norm, norm, norm));
//					g2.fillRect(outline.x+5 , 10+outline.y+i*10, 5, 5);
//				}
//			}
//		}
		
		
		
		
		//drawing the histograms if desired
		if (!isLoading() && MainGUI.getGUI().getPlateHoldingPanel().shouldDisplayHistograms() && containsCellData() && getCell_values()!=null)
		{
			// int xStart = outline.x+1;
			// int yStart = outline.y+1;
			// int xLen = outline.width-1;
			// int yLen = outline.height-2;
			// int numBins = 50;
			// float[][] values = getCell_values();
			// int numCells = values.length;
			// float dX = (float)xLen/(float)numBins;
			// int[] bins = new int[numBins];
			// int feature_index =
			// MainGUI.getGUI().getTheSelectedFeature_Index();
			//			
			// double minVal = 0;
			// double maxVal = 1;
			//			
			// if (!MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
			// {
			// float[][] vals = ThePlate.getMinMaxFeatureValues();
			// if (vals!=null)
			// {
			// minVal = vals[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
			// maxVal = vals[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
			// }
			// }
			// else
			// {
			// if (ThePlate.getMinMaxFeatureValues_log()!=null)
			// {
			// minVal =
			// ThePlate.getMinMaxFeatureValues_log()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
			// maxVal =
			// ThePlate.getMinMaxFeatureValues_log()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
			// }
			// }
			//			
			// //Binning values
			// for (int i = 0; i < numCells; i++)
			// {
			// double val = values[i][feature_index];
			// if (MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
			// {
			// if (val<=1)
			// val = 1;
			// val = tools.MathOps.log(val);
			// }
			// else
			// if (val<=0)
			// val = 0;
			//				
			// if (val>Double.NEGATIVE_INFINITY && val<
			// Double.POSITIVE_INFINITY)
			// {
			//					
			// double normVal = (val-minVal)/(maxVal-minVal);
			// int ind = (int)(numBins*normVal);
			// if (ind>0&&ind<numBins-1)
			// bins[ind]++;
			// }
			// }
			// //Finding max bin value so we can normalize the histogram
			// float maxBinVal = 0;
			// for (int i = 0; i < numBins; i++)
			// if (bins[i]>maxBinVal)
			// maxBinVal=bins[i];
			//			
			// //Creating the polygon
			// if(maxBinVal>0)
			// {
			// Polygon p = new Polygon();
			// for (int i = 0; i < numBins; i++)
			// p.addPoint((int)(xStart+dX*i),
			// (int)((yStart+yLen)-yLen*bins[i]/maxBinVal));
			//				
			// p.addPoint((int)(xStart+dX*(numBins-1)), yStart+yLen);
			// p.addPoint(xStart, yStart+yLen);
			// p.npoints = numBins+2;
			//				
			// g2.setColor(Color.black);
			// g2.drawPolygon(p);
			// g2.setColor(Color.white);
			// g2.fillPolygon(p);
			// }

			int x = outline.x + 1;
			int y = outline.y + 1;
			int width = outline.width - 1;
			int height = outline.height - 2;
			Polygon histo = getHistogram(x, y, width, height);
			if (histo != null)
 {
				g2.setColor(Color.white);
				g2.fill(histo);

			}
		}

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
			if(TheFields[i].doesHDFexist(main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath(), "Data"))
				count++;
		
		return count;
	}
	
	
	/** Loads the cells from the HDF files if they exist for each field
	 * @author BLM*/
	public void loadCells(SegmentationHDFConnector sCon, boolean loadCoords,
			boolean loadDataVals)
	{
		Field[] fields = getFields();
		int numF = fields.length;
		setLoading(true);
		System.out.println();
		for (int z = 0; z < numF; z++)
			fields[z].loadCells(sCon, loadCoords, loadDataVals);
		setLoading(false);
		updateDataValues();
		ThePlate.repaint();
	}

	/** Draws the mini-HDF file icon
	 * @author BLM*/
	private void drawHDFicon(Graphics2D g2, int xStart, int yStart, int xEnd, int yEnd)
	{
		// int[] xVals = new int[6];
		// int[] yVals = new int[6];

		int width = xEnd-xStart-1;
		int height = yEnd-yStart-1;

//		xVals[0] = xStart;
//		yVals[0] = yStart;
//
//		xVals[1] = (int)(xStart+width*0.7);
//		yVals[1] = yStart;
//
//		xVals[2] = (int)(xStart+width*0.7);
//		yVals[2] = (int)(yStart+height*0.3);
//
//		xVals[3] = (int)(xStart+width);
//		yVals[3] = (int)(yStart+height*0.3);
//
//		xVals[4] = (int)(xStart+width);
//		yVals[4] = (int)(yStart+height);
//
//		xVals[5] = xStart;
//		yVals[5] = yEnd;
		
		
		g2.setColor(Color.white);
		g2.fillRect(xStart, yStart, width, height);
		g2.setColor(Color.gray);
		g2.drawRect(xStart, yStart, width, height);
	}
	
//	/** Draws the mini-HDF file icon
//	 * @author BLM*/
//	private void drawHDFicon(Graphics2D g2, int xStart, int yStart, int xEnd, int yEnd)
//	{
//		int[] xVals = new int[6];
//		int[] yVals = new int[6];
//
//		int width = xEnd-xStart;
//		int height = yEnd-yStart;
//
//		xVals[0] = xStart;
//		yVals[0] = yStart;
//
//		xVals[1] = (int)(xStart+width*0.7);
//		yVals[1] = yStart;
//
//		xVals[2] = (int)(xStart+width*0.7);
//		yVals[2] = (int)(yStart+height*0.3);
//
//		xVals[3] = (int)(xStart+width);
//		yVals[3] = (int)(yStart+height*0.3);
//
//		xVals[4] = (int)(xStart+width);
//		yVals[4] = (int)(yStart+height);
//
//		xVals[5] = xStart;
//		yVals[5] = yEnd;
//
//
//		g2.setColor(Color.white);
//		g2.fillPolygon(xVals, yVals, xVals.length);
//		g2.setColor(Color.gray);
//		g2.drawPolygon(xVals, yVals, xVals.length);
//		g2.drawLine((int)(xStart+width*0.7), yStart, (int)(xStart+width), (int)(yStart+height*0.3));
//	}
	
	public Polygon getHistogram(int x, int y, int width, int height)
	{
		int xStart = x+1;
		int yStart = y+1;
		int xLen = width-1;
		int yLen = height-2;
		int numBins = 50;
		float[][] cells = getCell_values();
		if (cells==null)
			return null;
		int numCells = cells.length;
		float dX = (float)xLen/(float)numBins;
		int[] bins = new int[numBins];
		int feature_index = MainGUI.getGUI().getTheSelectedFeature_Index();
		
		double minVal = Double.POSITIVE_INFINITY;
		double maxVal = Double.NEGATIVE_INFINITY;
		
		//Getting the min/max pre-stored values
		if (!MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
		{
			if (ThePlate.getMinMaxFeatureValues()!=null)
			{
				minVal = ThePlate.getMinMaxFeatureValues()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
				maxVal = ThePlate.getMinMaxFeatureValues()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
				// System.out.println("LINEAR - minMax: " + minVal + " , "
				// + maxVal);

			}
		}
		else
		{
			if (ThePlate.getMinMaxFeatureValues_log()!=null)
			{
				minVal = ThePlate.getMinMaxFeatureValues_log()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
				maxVal = ThePlate.getMinMaxFeatureValues_log()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
				// System.out.println("LOG - minMax: " + minVal + " , " +
				// maxVal);
			}
		}
		
		
		//Binning values
		for (int i = 0; i < numCells; i++)
		{
			double val = cells[i][feature_index];
			if (MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
			{
				if (val<=1)
					val = 1;
				val = tools.MathOps.log(val);
			}
			else
				if (val<=0)
					val = 0;
			
			
			if (val>Double.NEGATIVE_INFINITY && val< Double.POSITIVE_INFINITY)
			{
				double normVal = (val-minVal)/(maxVal-minVal);
				int ind = (int)(numBins*normVal);
				if (ind>0&&ind<numBins-1)
					bins[ind]++;
			}
		}
		//Finding max bin value so we can normalize the histogram
		float maxBinVal = 0;
		for (int i = 0; i < numBins; i++)
			if (bins[i]>maxBinVal)
				maxBinVal=bins[i];
		
		//Creating the polygon
		if(maxBinVal>0)
		{
			Polygon p = new Polygon();
			for (int i = 0; i < numBins; i++)
				p.addPoint((int)(xStart+dX*i), (int)((yStart+yLen)-yLen*bins[i]/maxBinVal));
			
			p.addPoint((int)(xStart+dX*(numBins-1)), yStart+yLen);
			p.addPoint(xStart, yStart+yLen);
			p.npoints = numBins+2;
			
			return p;
		}
		
		return null;
	}
	
	public void updateDimensions()
	{
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();
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
//		if (!MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
//		{
//			minVal = ThePlate.getMinMaxFeatureValues()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
//			maxVal = ThePlate.getMinMaxFeatureValues()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
//		}
//		else
//		{
//			if (ThePlate.getMinMaxFeatureValues_log()!=null)
//			{
//				minVal = ThePlate.getMinMaxFeatureValues_log()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
//				maxVal = ThePlate.getMinMaxFeatureValues_log()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
//			}
//		}
//
//		//Binning values
//		for (int i = 0; i < len; i++)
//		{
//			double val = feature.getValue(TheCells[i]);
//			if (MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
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
		MainGUI.getGUI().getPlateHoldingPanel().updateMinMaxValues();
		
	}
	
	
	public void setDataValues(ArrayList<float[][]> allData)
	{
		float[][] data = getCellMeansAndStdev_allFeatures(allData);
		if (data==null)
			return;
		
		Feature_Means = data[0];
		Feature_Stdev = data[1];
		
		//updating the min/max of plate date
		MainGUI.getGUI().getPlateHoldingPanel().updateMinMaxValues();
		
	}
	
	
//	public void setDataValues(Cell_RAM[] cells)
//	{
//		TheCells = cells;
//		System.out.println("	>> NumCells in Well: "+cells.length);
//
//		float[][] data = Cell_RAM.getCellMeansAndStdev_allFeatures(cells);
//		if (data==null)
//			return;
//
//		Feature_Means = data[0];
//		Feature_Stdev = data[1];
//
//
//		//updating the min/max of plate date
//		MainGUI.getGUI().getPlateHoldingPanel().updateMinMaxValues();
//
//
//		if (!MainGUI.getGUI().getStoreCellsCheckBox().isSelected())
//		{
//			if (TheCells!=null)
//			{
//				System.out.println("killing cells");
//				int len = TheCells.length;
//				for (int i=0; i < len; i++)
//					TheCells[i].kill();
//				TheCells = null;
//			}
//		}
//
//
//
////		float[][] data2 = Cell.getCellMinMaxValues_allFeatures(cells);
////		if (ThePlate.MinMaxFeatureValues==null)
////			ThePlate.initMinMaxFeatureValues();
////		int numF = data[0].length;
////		for (int i = 0; i < numF; i++)
////		{
////			if(data2[0][i]<ThePlate.MinMaxFeatureValues[0][i])
////				ThePlate.MinMaxFeatureValues[0][i]=data2[0][i];
////			if(data2[1][i]>ThePlate.MinMaxFeatureValues[1][i])
////				ThePlate.MinMaxFeatureValues[1][i]=data2[1][i];
////		}
//	}
	
	/** Set the Mean/std values of the well directly
	 * @author BLM*/
	public void setMeanFluorescentValues(float[] val)
	{
		Feature_Means = val;
		int numChannels = val.length;
		Feature_Stdev = new float[numChannels];
		for (int i =0; i < numChannels; i++)
		{
			Feature_Stdev[i] = 0;
		}
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
	
	public Well copy(Plate plate)
	{
		Well well = new Well(plate, Row, Column);
		well.ID = ID;
		well.color = color;
		well.Feature_Means = Feature_Means;
		well.Feature_Stdev = Feature_Stdev;
		//TODO - note, I am not copying over the Image Files
		
		
		return well;
	}
	
	
	public void printParameterSet(PrintWriter pw) {
		pw.println("<Well  name='" + name
				+ "' "
				+ "  Processed='"
				+ TheParameterSet.getProcessType()
				+ "' "
				+ "  ThresholdChannel_nuc='"
				+ TheParameterSet.getThresholdChannel_nuc_Name()
				+ "' "
				+ "  ThresholdChannel_cyto='"
				+ TheParameterSet.getThresholdChannel_cyto_Name()
				+ "' "
				+ "  Threshold_Nucleus='"
				+ TheParameterSet.getThreshold_Nucleus()
				+ "' "
				+ "  Threshold_Cell='"
				+ TheParameterSet.getThreshold_Cell()
				+ "' "
				+ "  Threshold_Background='"
				+ TheParameterSet.getThreshold_Background()
				+ "' "
				+
				// "  TopXBrightestPixels='"+TheParameterSet.TopXBrightPix+"' "
				// +
				"  AnnulusSize='"
				+ TheParameterSet.getAnnulusSize()
				+ "' "
				+ "  MeanOrIntegrated='"
				+ TheParameterSet.getMeanOrIntegrated()
				+ "' "
				+ "  StoreCells='"
				+ MainGUI.getGUI().getLoadCellsImmediatelyCheckBox()
						.isSelected() + "' " +
				// "  StorePixelInfo='"+MainGUI.getGUI().getStorePixelInformationCheckBox().isSelected()+"' "
				// +
				// "  StoreMembraneRegion='"+MainGUI.getGUI().getStoreMembranesCheckBox().isSelected()+"' "
				// +
				" />");
	}

	public void deleteSelectedCells() {
		purgeSelectedCellsAndRecomputeWellMeans();
		MainGUI.getGUI().getPlateHoldingPanel().updateMinMaxValues();
		MainGUI.getGUI().updateAllPlots();
	}

	public void clearOldData() {
		Field[] fields = getFields();
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

		ThePlate.updatePanel();
		System.gc();

	}

	// /**
	// *
	// * */
	// public class WellHistograms
	// {
	// public float[][] bins;
	// public int numBins = 20;
	// public float[][] minMaxs;
	// public int numCells;
	// public float[][] displayBins;
	//		
	//		
	// public WellHistograms(float[][] cellValues)
	// {
	//
	// numCells = cellValues.length;
	// int numFeatures = MainGUI.getGUI().getTheFeatures().size();
	// bins = new float[numFeatures][numBins];
	//			
	// //finding min/max
	// minMaxs = new float[numFeatures][2];
	// for (int j = 0; j < numFeatures; j++)
	// {
	// minMaxs[j][0] = Float.POSITIVE_INFINITY;
	// minMaxs[j][1] = Float.NEGATIVE_INFINITY;
	// }
	// for (int j = 0; j < numFeatures; j++)
	// {
	// for (int i = 0; i < numCells; i++)
	// {
	// float val = cellValues[i][j];
	// if (val<minMaxs[j][0])
	// minMaxs[j][0]=val;
	// if (val>minMaxs[j][1])
	// minMaxs[j][1]=val;
	// }
	// }
	//			
	// //resetting bins
	// for (int j = 0; j < numFeatures; j++)
	// for (int i = 0; i < numBins; i++)
	// bins[j][i] = 0;
	// // createBins
	// for (int j = 0; j < numFeatures; j++)
	// {
	// float range = minMaxs[j][1]-minMaxs[j][0];
	// for (int i = 0; i < numCells; i++)
	// {
	// float val = cellValues[i][j];
	// if (val > 10000000 || val < -10000000)
	// System.out.println("val: " + val);
	//
	// float valN = (val-minMaxs[j][0])/range;
	// int index = (int)(valN*numBins);
	// if (index>=numBins)
	// index = numBins-1;
	//					
	// bins[j][index]++;
	// }
	// }
	//			
	//			
	// updateBounds(minMaxs);
	//			
	// }
	//		
	//		
	// public void updateBounds(float[][] newMinMax)
	// {
	// int numFeatures = MainGUI.getGUI().getTheFeatures().size();
	// displayBins = new float[numFeatures][numBins];
	// for (int j = 0; j < numFeatures; j++)
	// for (int i = 0; i < numBins; i++)
	// displayBins[j][i] = 0;
	//			
	// for (int j = 0; j < numFeatures; j++)
	// for (int i = 0; i < numBins; i++)
	// {
	// //backtracking the data
	// float numInBin = bins[j][i];
	// float binVal =
	// (float)(i+1)/(float)numBins*(minMaxs[j][1]-minMaxs[j][0])+minMaxs[j][0];
	//					
	//					
	// //rebinning into new bounded system
	// float valN = (binVal-newMinMax[j][0])/(newMinMax[j][1]-newMinMax[j][0]);
	// int index = (int)(valN*numBins);
	// if (index>=numBins)
	// index = numBins-1;
	// if (index<0)
	// index = 0;
	// displayBins[j][index]+=numInBin;
	// }
	//			
	//			
	// //norm the bins
	// float[][] barMinMax = new float[numFeatures][2];
	// for (int j = 0; j < numFeatures; j++)
	// {
	// barMinMax[j][0] = Float.POSITIVE_INFINITY;
	// barMinMax[j][1] = Float.NEGATIVE_INFINITY;
	// }
	//			
	// for (int j = 0; j < numFeatures; j++)
	// for (int i = 0; i < numBins; i++)
	// {
	// displayBins[j][i] = displayBins[j][i]/(float)numCells;
	// if (displayBins[j][i] < barMinMax[j][0])
	// barMinMax[j][0] = displayBins[j][i];
	// if (displayBins[j][i] > barMinMax[j][1])
	// barMinMax[j][1] = displayBins[j][i];
	// }
	// for (int j = 0; j < numFeatures; j++)
	// for (int i = 0; i < numBins; i++)
	// {
	// displayBins[j][i] =
	// (displayBins[j][i]-barMinMax[j][0])/(float)(barMinMax[j][1]-barMinMax[j][0]);
	// }
	// }
	//
	// }
}


