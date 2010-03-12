/**
 * PlateHoldingPanel.java
 *
 * @author Created by BLM
 */

package main;

import imageViewers.FieldViewer_Frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plots.Gate_DotPlot;

public class PlateHoldingPanel extends JPanel
{
	/** */
	private JToolBar TheToolBar;
	/** */
	private Plate[] ThePlates;
	/** */
	private boolean blocked;
	/** */
	private JTabbedPane TheMainPanel;
	/** Buttons for the toolbar*/
	private JButton DisplayHistogramsButton;
	/** */
	private JButton LogScaleButton;
	/** */
	private JButton DataViewButton;
	/** */
	private boolean Grid;
	/** */
	private int LastTouched_PlateID;
	

	public PlateHoldingPanel(Plate[] plates)
	{
		blocked = false;
		ThePlates = plates;
		int sqNumPlates = (int)Math.ceil(Math.sqrt(getNumPlates()));
		Grid = true;
		
		
		//Adding the grid tab panel
		LastTouched_PlateID = 1;
		TheMainPanel = new JTabbedPane();
		final JPanel GridPanel = new JPanel();
		GridPanel.setLayout(new GridLayout(sqNumPlates, sqNumPlates));
		int counter =0;
		for (int r = 0; r < getNumPlates(); r++)
		{
			GridPanel.add(ThePlates[r], counter);
			counter++;
		}
		TheMainPanel.addTab("All", GridPanel);
		
		//Adding the single plate tabs
		for (int p = 0; p < getNumPlates(); p++)
		{
			if (ThePlates[p].getTitle()==null)
				ThePlates[p].setTitle("Plate #"+ThePlates[p].getID());
			TheMainPanel.addTab(ThePlates[p].getTitle(), null);
		}
		
		
		TheMainPanel.addChangeListener(new ChangeListener()
									   {
					public void stateChanged(ChangeEvent p1)
					{
						int i = TheMainPanel.getSelectedIndex();
						if (i == 0) //All plates view
						{
							int counter =0;
							GridPanel.removeAll();
							for (int p = 0; p < getNumPlates(); p++)
							{
								GridPanel.add(ThePlates[p], counter);
								counter++;
							}
							TheMainPanel.setComponentAt(i, GridPanel);
							
							//Adding the single plate tabs
							for (int p = 0; p < getNumPlates(); p++)
								TheMainPanel.addTab(ThePlates[p].getTitle(), null);
							Grid = true;
						}
						else
						{
							int counter = 0;
							for (int p = 0; p < getNumPlates(); p++)
							{
								counter++;
								TheMainPanel.setComponentAt(counter, ThePlates[p]);
							}
							Grid = false;
						}
					}
				});
		
		setLayout(new BorderLayout());
		add(TheMainPanel, BorderLayout.CENTER);
		updatePanel();
		
		
		TheToolBar = new JToolBar();
		add(TheToolBar, BorderLayout.NORTH);
		addToolbarComponents();
		
	}
	
	/**
	 * States whether we should normalized across all plates or just singles.
	 * This is because sometimes the panel can display all plates in a grid or
	 * only view one plate at a time
	 * 
	 * @author BLM
	 */
	public boolean shouldNormalizeAcrossAllPlates() {
		return Grid;
	}

	/**
	 * Returns the tabbed pane
	 * 
	 * @athor BLM
	 */
	public JTabbedPane getTheMainPanel() {
		return TheMainPanel;
	}

	/**
	 * Returns boolean whether we should display the data table or not
	 * 
	 * @author BLM
	 */
	public boolean showData() {
		return DataViewButton.isSelected();
	}

	public int getSelectedPlateID()
	{
		return LastTouched_PlateID;
	}
	public void setLastTouched_PlateID(int id)
	{
		LastTouched_PlateID = id;
	}
	
	public void setTab(int index)
	{
		TheMainPanel.setSelectedIndex(index);
		updatePanel();
	}
	
	
	public void updatePanel()
	{
		for (int p = 0; p < getNumPlates(); p++)
			ThePlates[p].updatePanel();
	}
	
	public ArrayList<Well> getSelectedWells_horizOrder()
	{
		ArrayList<Well> arrAll = new ArrayList<Well>();
		for (int p = 0; p < getNumPlates(); p++)
		{
			Plate plate = ThePlates[p];
			ArrayList<Well> arr = new ArrayList<Well>();
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
					if (plate.getTheWells()[r][c].isSelected())
						arr.add(plate.getTheWells()[r][c]);
			
			arrAll.addAll(arr);
		}
		
		return arrAll;
	}
	
	/** Made originally for the lineplot, this method will return an array of arrays of Wells that were selected in all the plates.  It is of
	 * dimension [numRows selected from each plate seperately][numColumns in this row series]
	 * @author BLM */
	public Well[][] getAllSelectedWells_RowSeries()
	{
		ArrayList rows = new ArrayList();
		for (int p = 0; p < getNumPlates(); p++)
		{
			//For this plate, check if it has any wells highlighted in a row format
			Plate plate = ThePlates[p];
			for (int r = 0; r < plate.getNumRows(); r++)
			{
				boolean boo = false;
				ArrayList col = null;
				for (int c = 0; c < plate.getNumColumns(); c++)
					if (plate.getTheWells()[r][c].isSelected())
					{
						if (!boo)
						{
							col = new ArrayList();
							boo = true;
						}
						col.add(plate.getTheWells()[r][c]);
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
		
		
		Well[][] wells = new Well[numRows][];
		for (int r = 0; r < numRows; r++)
		{
			ArrayList series =(ArrayList)rows.get(r);
			int numCols = series.size();
			
			wells[r] = new Well[numCols];
			for (int c = 0; c < numCols; c++)
			{
				
				wells[r][c] = (Well)series.get(c);
			}
		}
		return wells;
	}
	
	/** This method will return an array of arrays of Wells that were selected.  It will find wells in each plate that were highlighted, then go across
	 * all the plates to retrieve a series of wells "through" plate where each series contains one well from each plate
	 *
	 * @author BLM */
	public Well[][] getAllSelectedWells_TransPlateSeries()
	{
		Hashtable hash = new Hashtable();
		ArrayList arr = new ArrayList();
		ArrayList oneSeries = null;
		for (int p = 0; p < getNumPlates(); p++)
		{
			Plate plate = ThePlates[p];
			for (int c = 0; c < plate.getNumColumns(); c++)
				for (int r = 0; r < plate.getNumRows(); r++)
				{
					if (plate.getTheWells()[r][c].isSelected())
					{
						//If we haven't already made a series out of this Well Series... create one
						if (hash.get(plate.getTheWells()[r][c].name)==null)
						{
							oneSeries = new ArrayList();
							for (int i = 0; i < getNumPlates(); i++)
								oneSeries.add(ThePlates[i].getTheWells()[r][c]);
							arr.add(oneSeries);
							hash.put(plate.getTheWells()[r][c].name, oneSeries);
						}
					}
				}
		}
		
		
		// Transfering over to Array Format
		int numSeries = arr.size();
		if (numSeries == 0)
			return null;
		
		Well[][] wells = new Well[numSeries][];
		for (int r = 0; r < numSeries; r++)
		{
			ArrayList series =(ArrayList)arr.get(r);
			int seriesSize = series.size();
			
			wells[r] = new Well[seriesSize];
			for (int c = 0; c < seriesSize; c++)
				wells[r][c] = (Well)series.get(c);
			
		}
		return wells;
	}
	
	/***
	 * 	Return the Plates
	 * */
	public Plate[] getThePlates()
	{
		return ThePlates;
	}
	
	
	
	/** Made originally for the lineplot, this method will return an array of arrays of Wells that were selected in all the plates.  It is of
	 * dimension [numCols selected from each plate seperately][numRows in this col series]
	 * @author BLM */
	public Well[][] getAllSelectedWells_ColumnSeries()
	{
		ArrayList cols = new ArrayList();
		for (int p = 0; p < getNumPlates(); p++)
		{
			//For this plate, check if it has any wells highlighted in a col format
			Plate plate = ThePlates[p];
			for (int c = 0; c < plate.getNumColumns(); c++)
			{
				boolean boo = false;
				ArrayList row = null;
				for (int r = 0; r < plate.getNumRows(); r++)
					if (plate.getTheWells()[r][c].isSelected())
					{
						if (!boo)
						{
							row = new ArrayList();
							boo = true;
						}
						row.add(plate.getTheWells()[r][c]);
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
		
		Well[][] wells = new Well[numSeries][];
		for (int r = 0; r < numSeries; r++)
		{
			ArrayList series =(ArrayList)cols.get(r);
			int numRows = series.size();
			
			wells[r] = new Well[numRows];
			for (int c = 0; c < numRows; c++)
			{
				wells[r][c] = (Well)series.get(c);
			}
		}
		return wells;
	}
	
	
	
	public void setDisplayRowLegends(boolean boo)
	{
		for (int p = 0; p < getNumPlates(); p++)
			ThePlates[p].setDisplayRowLegend(boo);
	}
	
	public void addToolbarComponents()
	{
		LogScaleButton = new JButton(tools.Icons.Icon_Log_selected);
		LogScaleButton.setSelected(true);
		LogScaleButton.setToolTipText("Log Scale");
		TheToolBar.add(LogScaleButton);
		LogScaleButton.addActionListener(new ActionListener()
										 {
					public void actionPerformed(ActionEvent ae)
					{
						LogScaleButton.setSelected(!LogScaleButton.isSelected());
						if (LogScaleButton.isSelected())
							LogScaleButton.setIcon(tools.Icons.Icon_Log_selected);
						else
							LogScaleButton.setIcon(tools.Icons.Icon_Log);
						validate();
						repaint();
						updatePanel();
					}
				});
		
		DisplayHistogramsButton = new JButton(
			tools.Icons.Icon_PlateHistogram_selected);
		DisplayHistogramsButton.setSelected(true);
		DisplayHistogramsButton.setToolTipText("Mini-Histograms");
		TheToolBar.add(DisplayHistogramsButton);
		DisplayHistogramsButton.addActionListener(new ActionListener()
												  {
					public void actionPerformed(ActionEvent ae)
					{
						DisplayHistogramsButton.setSelected(!DisplayHistogramsButton
																.isSelected());
						if (DisplayHistogramsButton.isSelected())
							DisplayHistogramsButton
								.setIcon(tools.Icons.Icon_PlateHistogram_selected);
						else
							DisplayHistogramsButton
								.setIcon(tools.Icons.Icon_PlateHistogram);
						
						updatePanel();
					}
				});
		
		TheToolBar.add(LogScaleButton);
		TheToolBar.add(DisplayHistogramsButton);
		
		
		DataViewButton = new JButton(tools.Icons.Icon_Data);
		DataViewButton.setToolTipText("Toggle Data Tables/Plate View");
		TheToolBar.add(DataViewButton);
		DataViewButton.addActionListener(new ActionListener()
										 {
					public void actionPerformed(ActionEvent ae)
					{
						DataViewButton.setSelected(!DataViewButton.isSelected());
						if (DataViewButton.isSelected())
							DataViewButton.setIcon(tools.Icons.Icon_Data_selected);
						else
							DataViewButton.setIcon(tools.Icons.Icon_Data);
						validate();
						repaint();
						updatePanel();
					}
				});
		
		
//		final JButton  but  = new JButton("Mono");
//		but.setSelected(false);
//		but.setToolTipText("Fit Single Gaussian");
//		TheToolBar.add(but);
//		but.addActionListener(new ActionListener()
//							  {
//					public void actionPerformed(ActionEvent ae)
//					{
//						but.setSelected(!but.isSelected());
//						if (but.isSelected())
//							FitGaussian = 1;
//						else
//							FitGaussian = 0;
//						validate();
//						repaint();
//						updatePanel();
//					}
//				});
//
		
		
		
		
		
	}
	
	
	/** Returns the min/max values of the given cells for all features loaded into the MainGUI
	 * @author BLM
	 * @return float[][] where float[].length = 2 (min/max) and float[][].length = numFeatures*/
	static public float[][] getCellMinMaxValues_allFeatures(float[][] values)
	{
		if (values==null || values.length<=0)
			return null;
		
		int numF = MainGUI.getGUI().getTheFeatures().size();
		
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
		int largeInt = 999999999;
		for (int p = 0; p < getNumPlates(); p++)
		{
			Plate plate = ThePlates[p];
			plate.initMinMaxFeatureValues();
			float[][] plateMinMax = plate.getMinMaxFeatureValues();
			float[][] plateMinMax_log =  plate.getMinMaxFeatureValues_log();
			int numF = MainGUI.getGUI().getTheFeatures().size();
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
				{
					if (plate.getTheWells()[r][c].containsCellData())
					{
						float[][] data = getCellMinMaxValues_allFeatures(plate.getTheWells()[r][c].getCell_values());
						
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
			updatePanel();
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
			Plate plate = ThePlates[p];
			int rows = plate.getNumRows();
			int cols = plate.getNumColumns();
			
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					Well well = plate.getTheWells()[r][c];
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
	
	
	public boolean isBlocked()
	{
		return blocked;
	}
	
	/** Returns the main toolbar for this panel
	 * @author BLM*/
	public JToolBar getTheToolBar()
	{
		return TheToolBar;
	}
	
	public void unblock()
	{
		for (int p = 0; p < getNumPlates(); p++)
		{
			ThePlates[p].unblock();
			ThePlates[p].updatePanel();
		}
		blocked = false;
	}
	
	public void block(FieldViewer_Frame im)
	{
		for (int p = 0; p < getNumPlates(); p++)
		{
			ThePlates[p].block(im);
			ThePlates[p].updatePanel();
		}
		blocked = true;
	}
	
	/** Returns whether the mini-histograms should be displayed
	 * @athor BLM*/
	public boolean shouldDisplayHistograms()
	{
		return DisplayHistogramsButton.isSelected();
	}
	/** Returns whether the data is log transformed for the mini-histograms
	 * @athor BLM*/
	public boolean isLogScaled()
	{
		return LogScaleButton.isSelected();
	}
	
	/** Returns the number of plates in this holding panel
	 * @author BLM*/
	public int getNumPlates()
	{
		return ThePlates.length;
	}
	
	public void unHighlightAllWells()
	{
		for (int p = 0; p < getNumPlates(); p++)
		{
			Plate plate = ThePlates[p];
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
					plate.getTheWells()[r][c].setSelected(false);
		}
		updatePanel();
	}
	
}

