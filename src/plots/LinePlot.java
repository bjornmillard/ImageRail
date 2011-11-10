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

package plots;

import gui.MainGUI;
import imPanels.ImageCapturePanel;
import imPanels.JPanel_highlightBox;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import models.Model_Well;
import tools.SVG_writer;
import dialogs.AxisBoundsInputDialog;
import dialogs.CaptureImage_Dialog;

/**
 * The main Line Plot used to plot the mean values of the wells of the plate
 *
 * @author BLM
 */
public class LinePlot extends JPanel_highlightBox implements ImageCapturePanel
{
	private AlphaComposite transComposite = AlphaComposite.getInstance(
		AlphaComposite.SRC_OVER, 0.15f);
	
	private int panel_width;
	private int panel_height;
	private  int XMARGIN_LEFT = 45;
	private  int XMARGIN_RIGHT = 20;
	private  int YMARGIN_BOTTOM = 170;
	private  int YMARGIN_TOP = 70;
	private int Type_yAxisScale;
	private int Xstart;
	private int Ystart;
	private int Xstart_override;
	private int Ystart_override;
	private int Width_override;
	private int Height_override;
	private int xRange;
	private int yRange;
	private JToolBar TheToolBar;
	
	private Bound Bounds;
	private Rectangle yAxisLabelBox;
	
	
	private int PlotType;
	static public final int ROWS = 0;
	static public final int COLS = 1;
	static public final int MULTIPLATE = 2;
	public boolean Display_NumCells;
	public boolean Display_StdevBars;
	public boolean Display_DoseResponseRange;
	public boolean Display_CV;
	
	protected JCheckBox DrawLinesCheckBox;
	protected JCheckBox DrawPointsCheckBox;
	protected JCheckBox PlotAllTimecoursesCheckBox;
	protected JCheckBox LowPassFilterCheckBox;
	public JButton LogScaleButton;
	
	static final Color Gray_1 = new Color(0.95f, 0.95f, 0.95f);
	static final Color Gray_2 = new Color(0.9f, 0.9f, 0.9f);
	private Color[] colors;
	private MainGUI TheMainGUI;
	private Rectangle HistogramBox_Selected;
	
	private double MaxValue;
	private double MinValue;
	private double AbsoluteMaxValue;
	private double AbsoluteMinValue;
	private LinePlot TheLinePlot;
	private Series[] TheSeries;
	private boolean SigmoidFit;
	private boolean SmoothData;
	private boolean CrossHairs;
	private String MEANVALUES = "Mean Values";
	private String CV = "CV's";
//	private MetaDataConnector TheMetaDataConnector;
	private String[] xLabels;
	private Legend TheLegend;
	/** */
	private Point Legend_xy;
	private JComboBox[] ComboBoxes;
	
	/**
	 * Basic constructor
	 *
	 * @author BLM
	 * @param MainGUI
	 *
	 */
	public LinePlot()
	{
		CrossHairs = false;
		TheLinePlot = this;
		SigmoidFit = false;
		SmoothData = false;
		Display_DoseResponseRange = false;
		Display_CV = false;
		Bounds = new Bound(this);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setLayout(new BorderLayout());
		setVisible(true);
		panel_width = 330;
		panel_height = 450;
		setSize(panel_width, panel_height);
		colors = new Color[4];
		colors[0] = new Color(0.1f, 0.1f, 0.1f);
		colors[1] = new Color(0.3f, 0.3f, 0.3f);
		colors[2] = new Color(0.5f, 0.5f, 0.5f);
		colors[3] = new Color(0.7f, 0.7f, 0.7f);
		Type_yAxisScale = 0;
		TheMainGUI = models.Model_Main.getModel().getGUI();
		yAxisLabelBox = new Rectangle();
		Xstart_override = -1;
		Ystart_override = -1;
		Width_override = -1;
		Height_override = -1;
		
		Display_NumCells = false;
		Display_StdevBars = true;
		Bounds.Upper = 100000;
		Bounds.Lower = 0;
		
		TheToolBar = new JToolBar();
		ThePanel.add(TheToolBar, BorderLayout.NORTH);
		
		
		LogScaleButton = new JButton(tools.Icons.Icon_Log);
		LogScaleButton.setToolTipText( "Log Scale");
		LogScaleButton.setSelected(false);
		LogScaleButton.addActionListener(new ActionListener()
										 {
					public void actionPerformed(ActionEvent ae)
					{
						LogScaleButton.setSelected(!LogScaleButton.isSelected());
						if(LogScaleButton.isSelected())
							LogScaleButton.setIcon(tools.Icons.Icon_Log_selected);
						else
							LogScaleButton.setIcon(tools.Icons.Icon_Log);
						
						TheMainGUI.updateLineGraph();
					}
				});
		TheToolBar.add(LogScaleButton);
		
		
		final JButton but = new JButton(tools.Icons.Icon_horizLines_selected);
		but.setToolTipText("Row Data Series");
		final JButton but2 = new JButton(tools.Icons.Icon_vertLines);
		but2.setToolTipText("Column Data Series");
		final JButton but12 = new JButton(tools.Icons.Icon_MultiPlate);
		but12.setToolTipText("Trans-Plates Data Series");
		
		but.setSelected(true);
		PlotType = ROWS;
		TheToolBar.add(but);
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						but.setSelected(true);
						PlotType = ROWS;
						but.setIcon(tools.Icons.Icon_horizLines_selected);
						but2.setIcon(tools.Icons.Icon_vertLines);
						but12.setIcon(tools.Icons.Icon_MultiPlate);
						
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
						updateCombosAndLegend();
					}
				});
		
		but2.setSelected(false);
		TheToolBar.add(but2);
		but2.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but2.setSelected(true);
						PlotType = COLS;
						but.setIcon(tools.Icons.Icon_horizLines);
						but2.setIcon(tools.Icons.Icon_vertLines_selected);
						but12.setIcon(tools.Icons.Icon_MultiPlate);
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
						updateCombosAndLegend();
					}
				});
		
		TheToolBar.add(but12);
		but12.addActionListener(new ActionListener()
								{
					public void actionPerformed(ActionEvent ae)
					{
						but12.setSelected(true);
						PlotType = MULTIPLATE;
						but.setIcon(tools.Icons.Icon_horizLines);
						but2.setIcon(tools.Icons.Icon_vertLines);
						but12.setIcon(tools.Icons.Icon_MultiPlate_selected);
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
						updateCombosAndLegend();
					}
				});
		
		
		

		//
		// TODO - took the 3D plotting out as of October 6, 2010 - Did not find
		// it useful and was clutter
		//
		// JButton but11 = new JButton(tools.Icons.Icon_3d);
		// but11.setToolTipText("Launch 3D Plotter");
		// TheToolBar.add(but11);
		// but11.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		//						
		// if (TheSeries==null)
		// return;
		//						
		// Object response = JOptionPane.showInputDialog(TheMainGUI,
		// "What should the coloring represent?",
		// "Color Mapping", JOptionPane.QUESTION_MESSAGE,
		// null, new String[] { MEANVALUES, CV },MEANVALUES);
		// if (response==null)
		// return;
		//						
		//						
		//						
		// //Collating all mean and stdev date into arrayLists
		// int len = TheSeries.length;
		//						
		// ArrayList arr = new ArrayList();
		// ArrayList arr2 = new ArrayList();
		// for (int i = 0; i < len; i++)
		// {
		// int num = TheSeries[i].TheDataPoints.length;
		// for (int j = 0; j < num; j++)
		// {
		// arr.add(new Point3d(i,j,TheSeries[i].getValue_plot(j)));
		// float val = TheSeries[i].TheDataPoints[j].getStdev();
		// arr2.add(new Double(val/TheSeries[i].getValue_real(j)));
		// }
		// }
		// //Transfering to arrays
		// int num = arr.size();
		// Point3d[] points = new Point3d[num];
		// double[] colors = new double[num];
		//						
		// if (((String)response).equalsIgnoreCase(MEANVALUES))
		// for (int i = 0; i < num; i++)
		// {
		// points[i] = (Point3d) arr.get(i);
		// colors[i] = points[i].z;
		// }
		// else if (((String)response).equalsIgnoreCase(CV))
		// for (int i = 0; i < num; i++)
		// {
		// points[i] = (Point3d) arr.get(i);
		// colors[i] = ((Double)arr2.get(i)).doubleValue();
		// }
		// ImageRail3D_Frame.load(points, colors);
		//						
		//						
		// validate();
		// repaint();
		// updatePanel();
		// }
		// });
		
		
		// adding the numCells buttons
//		final JButton but5 = new JButton(tools.Icons.Icon_SigmoidFit);
//		but5.setToolTipText("Sigmoid Fit");
//		but5.setSelected(false);
//		SigmoidFit = false;
//		tbar.add(but5);
//		but5.addActionListener(new ActionListener()
//							   {
//					public void actionPerformed(ActionEvent ae)
//					{
//						but5.setSelected(!but5.isSelected());
//						if (but5.isSelected())
//						{
//							but5.setIcon(tools.Icons.Icon_SigmoidFit_selected);
//							SigmoidFit = true;
//						}
//						else
//						{
//							but5.setIcon(tools.Icons.Icon_SigmoidFit);
//							SigmoidFit = false;
//						}
//
//						TheMainGUI.updateLineGraph();
//					}
//				});
		
		// adding the numCells buttons
		final JButton but3 = new JButton(tools.Icons.Icon_numCells);
		but3.setToolTipText("Cells per Model_Well");
		but3.setSelected(false);
		PlotType = ROWS;
		TheToolBar.add(but3);
		but3.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but3.setSelected(!but3.isSelected());
						if (but3.isSelected())
						{
							but3.setIcon(tools.Icons.Icon_numCells_selected);
							Display_NumCells = true;
						}
						else
						{
							but3.setIcon(tools.Icons.Icon_numCells);
							Display_NumCells = false;
						}
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
					}
				});
		
		// adding the numCells buttons
		final JButton but4 = new JButton(tools.Icons.Icon_DrawStdevBars);
		but4.setToolTipText("Display Cell Population Standard Deviations");
		but4.setSelected(false);
		Display_StdevBars = false;
		PlotType = ROWS;
		TheToolBar.add(but4);
		but4.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but4.setSelected(!but4.isSelected());
						if (but4.isSelected())
						{
							but4.setIcon(tools.Icons.Icon_DrawStdevBars_selected);
							Display_StdevBars = true;
						}
						else
						{
							but4.setIcon(tools.Icons.Icon_DrawStdevBars);
							Display_StdevBars = false;
						}
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
					}
				});
		
//		final JButton but9 = new JButton(tools.Icons.Icon_DoseResponseRange);
//		but9.setToolTipText("Display Dose Response Range (10-50-90%)");
//		but9.setSelected(false);
//		TheToolBar.add(but9);
//		but9.addActionListener(new ActionListener()
//							   {
//					public void actionPerformed(ActionEvent ae)
//					{
//						but9.setSelected(!but9.isSelected());
//						if (but9.isSelected())
//						{
//							but9.setIcon(tools.Icons.Icon_DoseResponseRange_selected);
//							Display_DoseResponseRange = true;
//						}
//						else
//						{
//							but9.setIcon(tools.Icons.Icon_DoseResponseRange);
//							Display_DoseResponseRange = false;
//						}
//
//						TheMainGUI.updateLineGraph();
//						TheMainGUI.getPlateHoldingPanel().updatePanel();
//					}
//				});
		
		final JButton but0 = new JButton(tools.Icons.Icon_CV);
		but0.setToolTipText("Display Coefficient of Variations of Data Series");
		but0.setSelected(false);
		TheToolBar.add(but0);
		but0.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but0.setSelected(!but0.isSelected());
						if (but0.isSelected())
						{
							but0.setIcon(tools.Icons.Icon_CV_selected);
							Display_CV = true;
						}
						else
						{
							but0.setIcon(tools.Icons.Icon_CV);
							Display_CV = false;
						}
						
						TheMainGUI.updateLineGraph();
						TheMainGUI.getPlateHoldingPanel().updatePanel();
					}
				});
		
		// adding the numCells buttons
		final JButton but7 = new JButton(tools.Icons.Icon_Smooth);
		but7.setToolTipText("Smooth Data");
		but7.setSelected(false);
		TheToolBar.add(but7);
		but7.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but7.setSelected(!but7.isSelected());
						if (but7.isSelected())
						{
							but7.setIcon(tools.Icons.Icon_Smooth_selected);
							SmoothData = true;
						}
						else
						{
							but7.setIcon(tools.Icons.Icon_Smooth);
							SmoothData = false;
						}
						
						TheMainGUI.updateLineGraph();
					}
				});
		
		final JButton but8 = new JButton(tools.Icons.Icon_CrossHair);
		but8.setToolTipText("Line Tracking");
		but8.setSelected(false);
		CrossHairs = false;
		TheToolBar.add(but8);
		but8.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						but8.setSelected(!but8.isSelected());
						if (but8.isSelected())
						{
							but8.setIcon(tools.Icons.Icon_CrossHair_selected);
							CrossHairs = true;
						}
						else
						{
							but8.setIcon(tools.Icons.Icon_CrossHair);
							CrossHairs = false;
						}
						
						TheMainGUI.updateLineGraph();
					}
				});
		
		
		
		JButton but6 = new JButton(new ImageIcon("icons/camera.png"));
		TheToolBar.add(but6);
		but6.setToolTipText("Capture Image of Plot");
		but6.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						CaptureImage_Dialog s = new CaptureImage_Dialog(TheLinePlot);
					}
				});
		
		
		
		
		
		validate();
		repaint();
	}
	
	
	private String[] getXLabels(String name)
	{
		int numS = getNumSeries();
		if (numS==0)
			return null;
		
		
		String[] labels = new String[getLargestSeriesLength()];
		for (int i = 0; i < TheSeries.length; i++)
		{
			int len = TheSeries[i].TheDataPoints.length;
			for (int j = 0; j < len; j++)
			{
				// TODO - MetaCon
				//
				// Model_Well well = TheSeries[i].TheDataPoints[j].TheWell;
				// Description[] treats =
				// well.getMetaDataConnector().readTreatments(
				// well.getWellIndex());
				// for (int z = 0; z < treats.length; z++)
				// {
				// if(treats[z].getName().equalsIgnoreCase(name))
				// labels[j] = treats[z].getValue()+" "+treats[z].getUnits();
				// }
			}
		}
		return labels;
		
		
	}
	
	/** Returns the well indices within each plate
	 * @author BLM*/
	private ArrayList<Integer> getWellIndices(ArrayList<Model_Well> wells)
	{
		ArrayList<Integer> wellIndices = new ArrayList<Integer>(wells.size());
		for (int i = 0; i < wells.size(); i++)
			wellIndices.add(wells.get(i).getWellIndex());
		return wellIndices;
	}
	
	/** */
	private ArrayList <String> getValidSeriesLegendNames()
	{
		int numS = getNumSeries();
		if (numS==0)
			return null;
		
		//get list of all wells represented in this plot
		ArrayList<Model_Well> wells = new ArrayList<Model_Well>();
		for (int i = 0; i < TheSeries.length; i++)
			for (int j = 0; j < TheSeries[i].TheDataPoints.length; j++)
				wells.add(TheSeries[i].TheDataPoints[j].TheWell);
		
		
		int longestSeries = getLargestSeriesLength();
		ArrayList<String> uniqueNames =  getUniqueTreatments(wells);
		
		String[][] labels = new String[uniqueNames.size()][longestSeries];
		ArrayList<String> passList = new ArrayList<String>();
		
		String[][] firstPoint = new String[uniqueNames.size()][getNumSeries()];
		for (int i = 0; i < uniqueNames.size(); i++)
			for (int j = 0; j < TheSeries.length; j++)
				firstPoint[i][j] = "";
		
		
		for (int i = 0; i < uniqueNames.size(); i++)
			for (int j = 0; j < TheSeries.length; j++)
			{
				// TODO - MetaCon
				// Model_Well well = TheSeries[j].TheDataPoints[0].TheWell;
				// //Then we check specifically for our treatment
				// Description treat = getTreatmentFromWell(uniqueNames.get(i),
				// well.getPlate().getPlateIndex(), well.getWellIndex() ,
				// well.getMetaDataConnector());
				// if(treat!=null)
				// firstPoint[i][j] = treat.getValue()+" "+treat.getUnits();
				// else
				// firstPoint[i][j] = "";
			}
		
		boolean[] areSame = new boolean[uniqueNames.size()];
		
		//For each unique treatment name in all wells, determine if
		for (int i = 0; i < uniqueNames.size(); i++)
		{
			String name = uniqueNames.get(i);
			areSame[i] = true;
			
			for (int j = 0; j < TheSeries.length; j++)
			{
				
				for (int d = 1; d < TheSeries[j].TheDataPoints.length; d++)
				{
					// TODO - MetaCon
					//
					// Model_Well well = TheSeries[j].TheDataPoints[d].TheWell;
					//					
					// //Then we check specifically for our treatment
					// Description treat = getTreatmentFromWell(name,
					// well.getPlate().getPlateIndex(), well.getWellIndex() ,
					// well.getMetaDataConnector());
					// if(treat!=null)
					// {
					// String val = treat.getValue()+" "+treat.getUnits();
					// if (!val.equalsIgnoreCase(firstPoint[i][j]))
					// {
					// areSame[i] = false;
					// break;
					// }
					// }
				}
				if(!areSame[i])
					break;
			}
		}
		
		
		
		for (int i = 0; i < areSame.length; i++)
		{
			if (areSame[i])
				passList.add(uniqueNames.get(i));
		}
		
		
		return passList;
		
	}
	
	/** Looks for all treatments in the given wells and returns a list of unqiue treatments
	 * @author BLM*/
	private  ArrayList<String> getUniqueTreatments(ArrayList<Model_Well> wells)
	{
		int numW = wells.size();
		
		ArrayList<String> unique = new ArrayList<String>();
		for (int i = 0; i < numW; i++)
		{
			// TODO - MetaCon
			//
			// Description[] arr =
			// wells.get(i).getMetaDataConnector().readTreatments(i);
			// for (int j = 0; j < arr.length; j++)
			// {
			// boolean foundIt = false;
			// for (int z = 0; z < unique.size(); z++)
			// {
			// if (unique.get(z).equalsIgnoreCase(arr[j].getName()))
			// {
			// foundIt = true;
			// break;
			// }
			// }
			// if(!foundIt)
			// unique.add(arr[j].getName());
			// }
		}
		
		return unique;
	}
	
	private ArrayList<String> getNamesOfDescriptionsThatAreSameForEachX()
	{
		int numS = getNumSeries();
		if (numS==0)
			return null;
		
		
		//get list of all wells represented in this plot
		ArrayList<Model_Well> wells = new ArrayList<Model_Well>();
		for (int i = 0; i < TheSeries.length; i++)
			for (int j = 0; j < TheSeries[i].TheDataPoints.length; j++)
				wells.add(TheSeries[i].TheDataPoints[j].TheWell);
		
		
		int longestSeries = getLargestSeriesLength();
		ArrayList<String> uniqueNames = getUniqueTreatments(wells);
		
		
		String[][] labels = new String[uniqueNames.size()][longestSeries];
		ArrayList<String> passList = new ArrayList<String>();
		
		for (int i = 0; i < uniqueNames.size(); i++)
			for (int d = 0; d < longestSeries; d++)
				labels[i][d] = "";
		
		//For each unique treatment name in all wells, determine if
		for (int i = 0; i < uniqueNames.size(); i++)
		{
			//Make an initial
			for (int d = 0; d < TheSeries[0].TheDataPoints.length; d++)
			{
				// TODO - MetaCon
				//
				// //See if this well has this treatment name
				// Model_Well well = TheSeries[0].TheDataPoints[d].TheWell;
				// Description treat = getTreatmentFromWell(uniqueNames.get(i),
				// well.getPlate().getPlateIndex(), well.getWellIndex() ,
				// well.getMetaDataConnector());
				// if(treat!=null)
				// labels[i][d] = treat.getValue()+" "+treat.getUnits();
				// else
				// labels[i][d] = "";
			}
		}
		
		
		//For each unique treatment name in all wells, determine if
		boolean[] areSame = new boolean[uniqueNames.size()];
		for (int i = 0; i < uniqueNames.size(); i++)
		{
			//Now looking at other series besides the first one
			areSame[i] = true;
			for (int j = 1; j < TheSeries.length; j++)
			{
				for (int d = 0; d < TheSeries[j].TheDataPoints.length; d++)
				{
					// TODO - MetaCon
					//
					// Model_Well well = TheSeries[j].TheDataPoints[d].TheWell;
					// Description treat =
					// getTreatmentFromWell(uniqueNames.get(i),
					// well.getPlate().getPlateIndex(), well.getWellIndex() ,
					// well.getMetaDataConnector());
					// if(treat!=null)
					// {
					// String valUnits = treat.getValue()+" "+treat.getUnits();
					// if(labels[i][d]==null ||
					// labels[i][d].equalsIgnoreCase(""))
					// {
					// labels[i][d] = valUnits;
					// }
					// else if(!valUnits.equalsIgnoreCase(labels[i][d]))
					// {
					// areSame[i] = false;
					// }
					// }
					
				}
			}
			
		}
		
		for (int i = 0; i < areSame.length; i++)
			if (areSame[i])
			{
				passList.add(uniqueNames.get(i));
			}
		
		return passList;
	}
	
	private void updateXLabels()
	{
		String name = (String)ComboBoxes[1].getSelectedItem();
		xLabels = getXLabels(name);
		
		updatePanel();
		repaint();
	}
	
	private void updateLegend()
	{
		if(ComboBoxes!=null&&ComboBoxes[0]!=null)
		{
			updateDimensions();
			int ys = panel_height - YMARGIN_BOTTOM;
			yRange = ys - YMARGIN_TOP;
			String name = (String)ComboBoxes[0].getSelectedItem();
			if(name!=null)
			{
				String[] speciesNames = new String[getNumSeries()];
				Color[] colors = new Color[getNumSeries()];
				for (int i = 0; i < TheSeries.length; i++)
				{
					// TODO - MetaCon
					//
					// String text = "Error Reading Legend Species";
					// Description treat = getTreatmentFromWell(name,
					// TheSeries[i].TheDataPoints[0].TheWell.getPlate().getPlateIndex(),
					// TheSeries[i].TheDataPoints[0].TheWell.getWellIndex() ,
					// TheSeries[i].TheDataPoints[0].TheWell.getMetaDataConnector());
					// if(treat!=null)
					// {
					// text =
					// treat.getName()+" ("+treat.getValue()+" "+treat.getUnits()+")";
					// speciesNames[i] = text;
					// colors[i] = TheSeries[i].color;
					// }
				}
				if(Legend_xy==null)
					Legend_xy = new Point(XMARGIN_LEFT+10, ys-yRange+10);
				TheLegend = new Legend("Treatment:", speciesNames, colors, Legend_xy.x, Legend_xy.y);
			}
		}
	}
	
	
	private void updateCombosAndLegend()
	{
		updateDimensions();
		int ys = panel_height - YMARGIN_BOTTOM;
		yRange = ys - YMARGIN_TOP;
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2,2));
		ThePanel.removeAll();
		ThePanel.add(TheToolBar, BorderLayout.NORTH);
		ThePanel.add(bottomPanel, BorderLayout.SOUTH);
		
		//Setting up the comboboxes
		ArrayList<String> arr = getNamesOfDescriptionsThatAreSameForEachX();
		
		ArrayList<String> listX = new ArrayList<String>();
		for (int i = 0; i < arr.size(); i++)
			listX.add(arr.get(i));
		Object[] obX = new Object[listX.size()];
		if (listX.size()>0)
			for (int i = 0; i < listX.size(); i++)
				obX[i] = listX.get(i);
		
		arr = getValidSeriesLegendNames();
		ArrayList<String> listL = new ArrayList<String>();
		for (int i = 0; i < arr.size(); i++)
			listL.add(arr.get(i));
		Object[] obL = new Object[listL.size()];
		if (listL.size()>0)
			for (int i = 0; i < listL.size(); i++)
				obL[i] = listL.get(i);
		
		
		JLabel label = new JLabel("Legend:");
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		bottomPanel.add(label, 0);
		
		label = new JLabel("X-Labels:");
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		bottomPanel.add(label, 1);
		
		
		ComboBoxes = new JComboBox[2];
		ComboBoxes[0] = new JComboBox(obL);
		ComboBoxes[0].setToolTipText("Legend Components");
		
		ComboBoxes[0].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						updateLegend();
						updatePanel();
						validate();
						repaint();
					}
					
				});
		bottomPanel.add(ComboBoxes[0],2);
		
		ComboBoxes[1] = new JComboBox(obX);
		ComboBoxes[1].setToolTipText("X-Axis Labels");
		
		ComboBoxes[1].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						updateXLabels();
						updatePanel();
						validate();
						repaint();
						
					}
				});
		bottomPanel.add(ComboBoxes[1],3);
		
		//Setting up the Legend
		updateLegend();
		//Setting up the X-labels
		updateXLabels();
		updatePanel();
		repaint();
	}
	
	

	// /** Looks in the given plate/well for the given treatment with the given
	// name
	// * @author BLM*/
	// private ExpDesign_Description getTreatmentFromWell(String name, int
	// pIndex,
	// int wIndex, ExpDesign_IO meta)
	// {
	// Description[] d = meta.readTreatments(wIndex);
	// for (int i = 0; i < d.length; i++)
	// {
	// if (d[i].getName().equalsIgnoreCase(name))
	// return d[i];
	// }
	// return null;
	// }
	
	
	
	public void updatePlot(float[][] vals, float[][] stdev, Color[] colors_, Model_Well[][] wells)
	{
		//Check if there is any data
		int numSeries = vals.length;
		if ( numSeries == 0)
			return;
		
		//Displaying the Coeff of Variation
		if (Display_CV && stdev!=null)
			for (int n = 0; n < numSeries; n++)
				for (int p = 0; p < vals[n].length; p++)
					vals[n][p] = (float)stdev[n][p]/vals[n][p];
		
		//Log transform if desired
		if (LogScaleButton.isSelected())
		{
			for (int n = 0; n < numSeries; n++)
			{
				int numPoints = vals[n].length;
				for (int p = 0; p < numPoints; p++)
				{
					vals[n][p] = (float)tools.MathOps.log(vals[n][p]);
					stdev[n][p] = (float)tools.MathOps.log(stdev[n][p]);
				}
			}
		}
		
		//Constructing the data series to plot
		TheSeries = new Series[numSeries];
		for (int i = 0; i < numSeries; i++)
		{
			float[] stdev_val = null;
			if (stdev!=null)
				stdev_val = stdev[i];
			
			TheSeries[i] = new Series(vals[i], stdev_val, colors_[i], i, wells[i]);
		}
		
		//Computing the multi-series Min/Max
		MaxValue = Double.NEGATIVE_INFINITY;
		MinValue = Double.POSITIVE_INFINITY;
		/* find the minimum and maximum values */
		if (!Display_StdevBars) // Need to account for Stdev Bar if want to display it
			for (int i = 0; i < numSeries; i++)
			{
				if (TheSeries[i].Max_real>MaxValue)
					MaxValue = TheSeries[i].Max_real;
				if (TheSeries[i].Min_real<MinValue)
					MinValue = TheSeries[i].Min_real;
			}
		else if (stdev != null && vals != null)
			for (int i = 0; i < numSeries; i++)
			{
				int numPoints = vals[i].length;
				for (int p = 0; p < numPoints; p++)
				{
					Series.DataPoint point = TheSeries[i].TheDataPoints[p];
					if ((point.y_real+point.stdev/2f) > MaxValue)
						MaxValue = point.y_real+point.stdev/2f;
					if ((point.y_real-point.stdev/2f)  < MinValue)
						MinValue = point.y_real-point.stdev/2f;
				}
			}
		
		Bounds.Upper = (float) MaxValue;
		Bounds.Lower = (float) MinValue;
		AbsoluteMaxValue = MaxValue;
		AbsoluteMinValue = MinValue;
		
		
		
		
		
		updateCombosAndLegend();
		
		updatePanel();
		repaint();
	}
	
	public void setXYstart(int x, int y)
	{
		Xstart_override = x;
		Ystart_override = y;
	}
	
	public void setWidthHeight(int w, int h)
	{
		Width_override = w;
		Height_override = h;
	}
	
	public void captureSVG(PrintWriter pw)
	{
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle("Line Plot");
		
		updateDimensions();
		
		draw(g2, true);
		
		g2.printEnd();
		pw.flush();
		pw.close();
	}
	
	public void captureImage(File file, String imageType)
	{
		int width = getWidth();
		int height = getHeight();
		
		BufferedImage im = new BufferedImage(width, height,
											 BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();
		
		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r, c, Color.WHITE.getRGB());
		
		draw(g2, false);
		
		try
		{
			ImageIO.write(im, imageType, file);
		}
		catch (IOException e)
		{
			System.out.println("**Error Printing Image**");
		}
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g2, false);
	}
	
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		if ( getNumSeries() == 0)
			return;
		
		updateDimensions();
		int whiteBkgdBeginX = 0;
		//
		//
		if(Xstart_override!=-1)
			Xstart = Xstart_override;
		else
			Xstart = XMARGIN_LEFT + yAxisLabelBox.width;
		
		if(Ystart_override!=-1)
			Ystart = Ystart_override;
		else
			Ystart = panel_height - YMARGIN_BOTTOM;
		
		if(Width_override!=-1)
			panel_width = Width_override;
		
		if(Height_override!=-1)
			panel_height = Height_override;
		
		if(Xstart_override==-1 && Ystart_override==-1 && Width_override==-1 && Height_override==-1)
		{
			updateDimensions();
			yRange = Ystart - YMARGIN_TOP;
			xRange = panel_width - XMARGIN_LEFT - XMARGIN_RIGHT;
			whiteBkgdBeginX = 0;
			
		}
		else
		{
			
			XMARGIN_LEFT = 0;
			XMARGIN_RIGHT =0;
			
			yRange = panel_height;
			xRange = panel_width;
			
			whiteBkgdBeginX = Xstart;
		}
		//
		//
		
		// Drawing border
		for (int i = 0; i < 4; i++)
		{
			g2.setColor(colors[i]);
			g2.drawRect(whiteBkgdBeginX+XMARGIN_LEFT - i, (Ystart - yRange) - i,
						xRange + 2 * i, yRange + 2 * i);
		}
		
		g2.setColor(Color.white);
		g2.fillRect(whiteBkgdBeginX+XMARGIN_LEFT + 1, (Ystart - yRange) + 1, xRange - 1,
					yRange - 1);
		
		// drawing gridlines
		g2.setColor(Gray_1);
		int numLines = 10;
		for (int i = 0; i < numLines; i++)
		{
			if (i % 2 == 0)
				g2.setColor(Gray_1);
			else
				g2.setColor(Gray_2);
			
			g2.drawLine(whiteBkgdBeginX+XMARGIN_LEFT + 1, (int) (Ystart - ((float) i / (float) numLines * yRange)),
							(whiteBkgdBeginX+panel_width - XMARGIN_RIGHT - 2),(int) (Ystart - ((float) i / (float) numLines * yRange)));
		}
		// drawing Vertical gridlines
		int maxNumPoints = getLargestSeriesLength();
		int xStep = xRange / (maxNumPoints + 1);
		for (int i = 0; i < numLines; i++)
		{
			g2.setColor(Gray_2);
			int x = i * xStep + Xstart ;
			int y1 = Ystart;
			int y2 = Ystart - yRange;
			
			g2.drawLine(x, y1, x, y2);
		}
		
		//
		// Drawing the yAxisLabelBox
		int numTicks = 10;
		int tickLen = 4;
		if(Xstart_override==-1 && Ystart_override==-1 && Width_override==-1 && Height_override==-1)
		{
			g2.setColor(Color.lightGray);
			Composite co = g2.getComposite();
			g2.setComposite(transComposite);
			yAxisLabelBox.x = XMARGIN_LEFT - 45;
			yAxisLabelBox.y = Ystart - yRange + 1;
			yAxisLabelBox.width = 45;
			yAxisLabelBox.height = yRange - 1;
			// g2.fill(yAxisLabelBox);
			// g2.setColor(Color.gray);
			// g2.draw(yAxisLabelBox);
			
			// Drawing the right border lines
			// for (int i =0; i < 4; i++)
			// {
			// g2.setColor(colors[i]);
			// g2.drawLine(yAxisLabelBox.x+yAxisLabelBox.width+i, yAxisLabelBox.y ,
			// yAxisLabelBox.x+yAxisLabelBox.width+i,
			// yAxisLabelBox.y+yAxisLabelBox.height);
			// }
			
			// Drawing handles on yAxisLabelBox
			for (int i = 0; i < 4; i++)
			{
				g2.setColor(Color.black);
				g2.drawLine(yAxisLabelBox.x + 8, yAxisLabelBox.y + 8 + 2 * i,
							yAxisLabelBox.x + yAxisLabelBox.width - 10, yAxisLabelBox.y
								+ 8 + 2 * i);
				g2.drawLine(yAxisLabelBox.x + 8, yAxisLabelBox.y
								+ yAxisLabelBox.height - 8 - 2 * i, yAxisLabelBox.x
								+ yAxisLabelBox.width - 10, yAxisLabelBox.y
								+ yAxisLabelBox.height - 8 - 2 * i);
			}
			g2.setComposite(co);
			// Drawing the axis ticks and labels:
			//
			
			
			
			
			// Y-axis
			g2.setColor(Color.DARK_GRAY);
			
			// ticks
			for (int i = 0; i < numTicks; i++)
				g2.drawLine(XMARGIN_LEFT - 1, (int) (Ystart - ((float) i  / (float) numTicks * yRange)), (XMARGIN_LEFT - tickLen),
								(int) (Ystart - ((float) i / (float) numTicks * yRange)));
			// labels
			if (Bounds.Upper != 0 && MaxValue != 0)
			{
				g2.setFont(MainGUI.Font_8);
				for (int i = 0; i < numTicks; i++)
					if ((i + 1) % 2 == 0 && i != numTicks) // odd ticks, but not top
					{
						if (Bounds.Upper < 10) // float display
						{
							float val = (float) i / (float) numTicks* ((float) Bounds.Upper - (float) Bounds.Lower)+ (float) Bounds.Lower;
							g2.drawString("" + MainGUI.nf.format(val),(XMARGIN_LEFT - tickLen - 26),(int) (Ystart- ((float) i / (float) numTicks * yRange) + 3));
						}
						else // integer display
						{
							int val = (int) ((float) i / (float) numTicks* (Bounds.Upper - Bounds.Lower) + Bounds.Lower);
							if (val < 0)
								g2.setColor(Color.red);
							g2.drawString("" + val,(XMARGIN_LEFT - tickLen - 28),(int) (Ystart - ((float) i / (float) numTicks * yRange) + 3));
							g2.setColor(Color.black);
						}
					}
			}
		}
		//
		//
		
		// X-axis
//		numTicks = getLargestSeriesLength();
//		for (int i = 0; i < numTicks; i++)
//			g2.drawLine((int) (Xstart + ((float) i / (float) numTicks * xRange)),Ystart - 1,(int) (Xstart + ((float) i / (float) numTicks * xRange)), (Ystart - tickLen));
		
		//
		// Drawing the data
		for (int s = 0; s < getNumSeries(); s++)
			TheSeries[s].draw(g2, plotToSVG);
		
		for (int s = 0; s < getNumSeries(); s++)
			TheSeries[s].drawMiniHistograms(g2, plotToSVG);
		
		//Drawing x-labels
		if (xLabels!=null)
		{
			maxNumPoints = getLargestSeriesLength();
			xStep = xRange / (maxNumPoints + 1);
			
			int num = xLabels.length;
			g2.setColor(Color.black);
			for (int c = 0; c < num; c++)
			{
				if (xLabels[c]!=null)
				{
					// Drawing the Stdev bars
					int x1 = (int)(c * xStep + Xstart - xLabels[c].length()*7*Math.cos(-Math.PI/4f)-3);
					int y1 = Ystart + (int)(xLabels[c].length()*7*Math.cos(-Math.PI/4f)+26);
					
					if(!plotToSVG)
					{
						g2.setFont(gui.MainGUI.Font_12);
						Font oldFont = g2.getFont();
						Font f = oldFont.deriveFont(AffineTransform.getRotateInstance(-Math.PI / 4.0));
						
						g2.setFont(f);
						g2.drawString(xLabels[c], x1, y1);
						g2.setFont(oldFont);
					}
					else
					{
						g2.drawString(xLabels[c], x1, y1);
					}
					
				}
			}
			String name = (String)ComboBoxes[1].getSelectedItem();
			if(name!=null)
				g2.drawString(""+name, (xRange-Xstart)/2f+10,panel_height-60);
		}
		
		//Drawing the Legend
		if (TheLegend!=null)
			TheLegend.draw(g2, plotToSVG);
		
	}
	
	public int getLargestSeriesLength()
	{
		int numS = TheSeries.length;
		int max = 0;
		for (int i = 0; i < numS; i++)
		{
			if(TheSeries[i].TheDataPoints.length>max)
				max = TheSeries[i].TheDataPoints.length;
		}
		return max;
	}
	
	
	/** Depending on if Rows or COls are selected, will return an appropriately sized float[x][3] array of x Series, and a 10-50-90 (if we want 10%-50%-90% activation
	 * range of the series
	 * @author BLM*/
	public float[][] get_LoMidHi_RangeValues(float low, float mid, float hi)
	{
		float[][] vals = new float[getNumSeries()][3];
		for (int n = 0; n < getNumSeries(); n++)
		{
			Series s = TheSeries[n];
			vals[n][0] = s.getXValofGivenYfraction(low);
			vals[n][1] =  s.getXValofGivenYfraction(mid);
			vals[n][2] =  s.getXValofGivenYfraction(hi);
		}
		return vals;
	}
	
	
	private double getSigmoidValue_y(double x, float beta, float mu)
	{
		return (1d / (1d + Math.exp(beta * (x - mu))));
	}
	
	/**
	 * Getting the inverse (x) value of a given y-value (normlized 0-1). This is
	 * used to extract EC or IC50 values from a curve. If you want, for example,
	 * the EC90 value, you would input 0.9d along with appropriate beta and mu
	 * values to construct the function
	 *
	 * @author BLM
	 */
	private double getSigmoidValue_x(double y, double beta, double mu)
	{
		// System.out.println("y = "+y);
		// System.out.println("b = "+beta);
		// System.out.println("m = "+mu);
		// System.out.println("l = "+Math.log(1d/y-1d));
		
		return mu + tools.MathOps.log(1d / y - 1d) / beta;
	}
	
	
	private void updateDimensions()
	{
		panel_width = ThePanel.getWidth();
		panel_height = ThePanel.getHeight();
		
	}
	
	public void copySettings(LinePlot p)
	{
		PlotType = p.PlotType;
	}
	
	
	/** Given the coordinate point on teh screen, and all the data series, returns the datapoint clicked on if applicable, else returns null
	 * @author BLM*/
	public Series.DataPoint getDataPointClicked(Point p, Series[] series )
	{
		int len = series.length;
		for (int i = 0; i < len; i++)
		{
			Series s = series[i];
			int num = s.TheDataPoints.length;
			for (int n = 0; n < num; n++)
			{
				Series.DataPoint d = s.TheDataPoints[n];
				if (d.box.contains(p))
					return d;
			}
		}
		return null;
	}
	public Series.DataPoint getDataPointClicked_closeBox(Point p, Series[] series )
	{
		int len = series.length;
		for (int i = 0; i < len; i++)
		{
			Series s = series[i];
			int num = s.TheDataPoints.length;
			for (int n = 0; n < num; n++)
			{
				Series.DataPoint d = s.TheDataPoints[n];
				if (d.closeBox.contains(p))
					return d;
			}
		}
		return null;
	}
	public Series.DataPoint getDataPointClicked_histoBox(Point p, Series[] series )
	{
		if (series == null)
			return null;
		int len = series.length;
		for (int i = 0; i < len; i++)
		{
			Series s = series[i];
			int num = s.TheDataPoints.length;
			for (int n = 0; n < num; n++)
			{
				Series.DataPoint d = s.TheDataPoints[n];
				if (d.bounds_histo!=null)
					if (d.bounds_histo.contains(p))
						return d;
			}
		}
		return null;
	}
	
	public void mouseClicked(MouseEvent p1)
	{
		if (p1.getClickCount() >= 2)
		{
			//Checking if we clicked on a datapoint marker
			Series.DataPoint d = getDataPointClicked(p1.getPoint(), TheSeries);
			if (d!=null)
				d.drawMiniHistogram(!d.DrawMiniHistogram);
			
			
			//Checking the axis bounds
			AxisBoundsInputDialog s = null;
			if (yAxisLabelBox.contains(p1.getPoint()))
				s = new AxisBoundsInputDialog(Bounds, TheMainGUI);
		}
		
		//Checking if we clicked on a datapoint CLOSE marker
		Series.DataPoint d = getDataPointClicked_closeBox(p1.getPoint(), TheSeries);
		if (d!=null)
			d.drawMiniHistogram(false);
		
		
		updatePanel();
	}
	
	public void mouseMoved(MouseEvent p1)
	{
		if (!CrossHairs || TheSeries==null)
			return;
		
		Point p = p1.getPoint();
		int xPos = p.x;//p.x-xStart;
		int yPos = p.y;//yStart+p.y;
		
		int num = TheSeries.length;
		for (int i = 0; i < num; i++)
		{
			if (TheSeries[i]==null)
				return;
			
			TheSeries[i].TheCrossHair.x = xPos;
			TheSeries[i].TheCrossHair.y = Ystart-TheSeries[i].ID_SERIES*30;
		}
		
		updatePanel();
	}
	
	public void mouseReleased(MouseEvent p1)
	{
		HistogramBox_Selected = null;
		if(TheLegend!=null)
			TheLegend.setDragging(false);
	}
	
	// Updating dragging
	public void mousePressed(MouseEvent p1)
	{
		startHighlightPoint = p1.getPoint();
		
		
		//Checking if we clicked on a datapoint marker
		Series.DataPoint d = getDataPointClicked_histoBox(p1.getPoint(), TheSeries);
		if (d!=null)
			HistogramBox_Selected = d.bounds_histo;
		
		
		
		if (yAxisLabelBox.contains(p1.getPoint()))
		{
			// setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			// At bottom of rectangle
			if (p1.getPoint().y < (yAxisLabelBox.y + yAxisLabelBox.height / 2f))
				Type_yAxisScale = 1;
				// At top of rectangle
			else if (p1.getPoint().y >= (yAxisLabelBox.y + yAxisLabelBox.height / 2f))
				Type_yAxisScale = -1;
		}
		else
			Type_yAxisScale = 0;
		
		if(TheLegend!=null && TheLegend.contains(startHighlightPoint))
		{
			TheLegend.setDragging(true);
		}
		
		updatePanel();
	}
	
	public void mouseDragged(MouseEvent p1)
	{
		if (startHighlightPoint == null)
			return;
		
		
		if(TheLegend!=null&& TheLegend.isDragging())
		{
			TheLegend.setX((int)(p1.getPoint().x-TheLegend.getWidth()/2f));
			TheLegend.setY((int)(p1.getPoint().y-TheLegend.getHeight()/2f));
			Legend_xy.x = TheLegend.getX();
			Legend_xy.y = TheLegend.getY();
			updatePanel();
			return;
		}
		
		if (HistogramBox_Selected!=null)
		{
			Point p = p1.getPoint();
			HistogramBox_Selected.x = p.x-HistogramBox_Selected.width/2;
			HistogramBox_Selected.y = p.y-HistogramBox_Selected.height/2;
		}
		
		int yval = p1.getPoint().y - startHighlightPoint.y;
		
		if (Type_yAxisScale == 1)
		{
			double test = Bounds.Upper + 5 * yval;
			if (test > AbsoluteMaxValue)
				Bounds.Upper += 5 * yval;
			else
				Bounds.Upper = (float) AbsoluteMaxValue;
		}
		else if (Type_yAxisScale == -1)
		{
			double test = Bounds.Lower + 5 * yval;
			if (test < AbsoluteMinValue)
				Bounds.Lower += 5 * yval;
			else
				Bounds.Lower = (float) AbsoluteMinValue;
		}
		
		
		if(TheLegend!=null&& TheLegend.isDragging())
		{
			TheLegend.setX((int)(p1.getPoint().x-TheLegend.getWidth()/2f));
			TheLegend.setY((int)(p1.getPoint().y-TheLegend.getHeight()/2f));
		}
		
		startHighlightPoint = p1.getPoint();
		updatePanel();
	}
	
	/** Data structure that holds the line series data
	 * @author BLM*/
	public class Series
	{
		public int ID_SERIES;
		public int ID_PLATE;
		public DataPoint[] TheDataPoints;
		public float Max_real;
		public float Min_real;
		public float Max_plot;
		public float Min_plot;
		public float[] sigmoidalFitCoeffs_plot;
		public float[] sigmoidalFitCoeffs_real;
		public Color color;
		public CrossHairPoint TheCrossHair;
		public Polygon dummyPolygon;
		public int xStep;
		
		public Series(float[] vals_orig, float[] stdev, Color color_, int ID_SERIES_, Model_Well[] well)
		{
			ID_SERIES = ID_SERIES_;
			ID_PLATE = well[0].getPlate().getID();
			color = color_;
			TheCrossHair = new CrossHairPoint();
			dummyPolygon = new Polygon();
			dummyPolygon.xpoints = new int[4];
			dummyPolygon.ypoints = new int[4];
			
			// Should we smooth the data first?
			int numPoints = vals_orig.length;
			float[] vals_plot = new float[numPoints];
			if (SmoothData)
				vals_plot = tools.MathOps.smoothData(vals_orig);//tools.MathOps.smoothData_forceMonotonic(vals_orig);
			else
				vals_plot = vals_orig;
			
			//Constructing the DataPoints
			TheDataPoints = new DataPoint[numPoints];
			for (int i = 0; i < numPoints; i++)
			{
				try
				{
					TheDataPoints[i] = new DataPoint(i, vals_orig[i], stdev[i], vals_plot[i], well[i]);
				}
				catch (Exception e)
				{
					TheDataPoints[i] = new DataPoint(i, vals_orig[i], 0, vals_plot[i], well[i]);
				}
				
			}
			
			initMinMax();
			
			if (SigmoidFit)
				curveFit();
		}
		
		public float getValue_normalized(int index)
		{
			return (float)((TheDataPoints[index].y_real-Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
		}
		public float getValue_real(int index)
		{
			return TheDataPoints[index].y_real;
		}
		public float getValue_plot(int index)
		{
			return TheDataPoints[index].y_plot;
		}
		
		public void drawMiniHistograms(Graphics2D g2, boolean plotToSVG)
		{
			int len = TheDataPoints.length;
			for (int i = 0; i < len; i++)
				TheDataPoints[i].drawMiniHistogram(g2);
		}
		
		public void draw(Graphics2D g2, boolean plotToSVG)
		{
			int maxNumPoints = getLargestSeriesLength();
			int numP = TheDataPoints.length;
			xStep = xRange / (maxNumPoints + 1);
			//Drawing the Series
			for (int c = 1; c < numP; c++)
			{
				DataPoint lastP = TheDataPoints[c-1];
				DataPoint thisP = TheDataPoints[c];
				// Drawing the Stdev bars
				int x1 = (c - 1) * xStep + Xstart;
				int x2 = c * xStep + Xstart;
				float lastVal = (float) ((lastP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
				float thisVal = (float) ((thisP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
				int y1 = Ystart - (int) (lastVal * yRange);
				int y2 = Ystart - (int) (thisVal * yRange);
				
				if (Display_StdevBars)
				{
					float topStd_1 = (float) (((((lastP.y_plot+lastP.stdev/2f) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower))));
					float botStd_1 = (float) ((((lastP.y_plot-lastP.stdev/2f) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)));
					int y1_t = Ystart - (int) (topStd_1 * yRange);
					int y1_b = Ystart - (int) (botStd_1 * yRange);
					
					float topStd_2 = (float) (((thisP.y_plot+thisP.stdev/2f) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
					float botStd_2 = (float) ((((thisP.y_plot-thisP.stdev/2f) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)));
					
					int y2_t = Ystart - (int) (topStd_2 * yRange);
					int y2_b = Ystart - (int) (botStd_2 * yRange);
					
					// Drawing the stdev bars
					g2.setColor(Color.gray);
					g2.drawLine(x2, y2_t, x2, y2_b);
					// Drawing the wings
					g2.drawLine(x2 - 2, y2_t, x2 + 2, y2_t);
					g2.drawLine(x2 - 2, y2_b, x2 + 2, y2_b);
					
					// drawing a filled polygon for better visualization
					dummyPolygon.xpoints[0] = x1;
					dummyPolygon.xpoints[1] = x2;
					dummyPolygon.xpoints[2] = x2;
					dummyPolygon.xpoints[3] = x1;
					dummyPolygon.ypoints[0] = y1_t;
					dummyPolygon.ypoints[1] = y2_t;
					dummyPolygon.ypoints[2] = y2_b;
					dummyPolygon.ypoints[3] = y1_b;
					dummyPolygon.npoints = 4;
					
					Composite comp = g2.getComposite();
					g2.setComposite(transComposite);
					g2.setColor(color);
					g2.fill(dummyPolygon);
					g2.setComposite(comp);
				}
				
				
				// Drawing the Data Points
				g2.setColor(color);
				if (!SigmoidFit)	//only plot the raw line if we dont plot the sigmoid fit -- gets too busy otherwise
				{
					g2.setStroke(MainGUI.Stroke_3);
					if (plotToSVG)
							((SVG_writer)g2).setStrokeWidth(3);
					g2.drawLine(x1, y1, x2, y2);
					if (plotToSVG)
							((SVG_writer)g2).setStrokeWidth(1);
					g2.setStroke(MainGUI.Stroke_1);
				}
				
				
				
				if (c == numP - 1) // drawing the first oval
				{
					if (Display_StdevBars)
					{
						// and drawing the first std bars
						thisP = TheDataPoints[0];
						x1 = 0 * xStep + Xstart;
						x2 = 0 * xStep + Xstart;
						float topStd = (float) (((thisP.y_plot+thisP.stdev/2f ) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
						float botStd = (float) (((thisP.y_plot-thisP.stdev/2f) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
						int y1_s = Ystart - (int) (topStd * yRange);
						int y2_s = Ystart - (int) (botStd * yRange);
						// Drawing the stdev bars
						g2.setColor(Color.gray);
						g2.drawLine(x2, y1_s, x2, y2_s);
						// Drawing the wings
						g2.drawLine(x2 - 2, y1_s, x2 + 2, y1_s);
						g2.drawLine(x2 - 2, y2_s, x2 + 2, y2_s);
					}
				}
				
			}
			
			//Drawing the Markers, after the lines so they will be on top
			int dotDiameter = 12;
			if (plotToSVG)
				dotDiameter = 4;
			for (int c = 1; c < numP; c++)
			{
				DataPoint thisP = TheDataPoints[c];
				// Drawing the Stdev bars
				float thisVal_orig = (float) ((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
				
				g2.setColor(color);
				if (plotToSVG)
				{
					//for some reason, the Illustrator doesnt have the dots in the same spot as the Java rendering
					if (PlotType==MULTIPLATE)
						TheDataPoints[c].drawMarker_Rectangle(g2, (c * xStep + Xstart - dotDiameter/2),  Ystart - (int) (thisVal_orig * yRange) - dotDiameter/2, dotDiameter, TheDataPoints[c].TheWell.name);
					else
						TheDataPoints[c].drawMarker_Circle(g2, (c*xStep + Xstart),   (Ystart - (int) (((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)) * yRange)), dotDiameter, ID_PLATE+"");
					g2.setColor(Color.black);
				}
				else
				{
					if (PlotType==MULTIPLATE)
						TheDataPoints[c].drawMarker_Rectangle(g2, (c * xStep + Xstart - dotDiameter/2),  Ystart - (int) (thisVal_orig * yRange) - dotDiameter/2, dotDiameter, TheDataPoints[c].TheWell.name);
					else
						TheDataPoints[c].drawMarker_Circle(g2, (c * xStep + Xstart - dotDiameter/2),  Ystart - (int) (thisVal_orig * yRange) - dotDiameter/2, dotDiameter, ID_PLATE+"");
				}
			}
			// drawing the first oval
			DataPoint thisP = TheDataPoints[0];
			g2.setColor(color);
			if (plotToSVG)
			{
				//for some reason, the Illustrator doesnt have the dots in the same spot as the Java rendering
				if (PlotType==MULTIPLATE)
					TheDataPoints[0].drawMarker_Rectangle(g2, (0*xStep + Xstart), Ystart- (int) (((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)) * yRange), dotDiameter, TheDataPoints[0].TheWell.name);
				else
					TheDataPoints[0].drawMarker_Circle(g2, (0*xStep + Xstart), Ystart  - (int) (((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)) * yRange), dotDiameter, ID_PLATE+"");
				g2.setColor(Color.black);
				
			}
			else
			{
				if (PlotType==MULTIPLATE)
					TheDataPoints[0].drawMarker_Rectangle(g2, (0*xStep + Xstart - dotDiameter/2), Ystart- (int) (((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)) * yRange)- dotDiameter/2, dotDiameter, TheDataPoints[0].TheWell.name);
				else
					TheDataPoints[0].drawMarker_Circle(g2, (0*xStep + Xstart - dotDiameter/2), Ystart- (int) (((thisP.y_real - Bounds.Lower) / (Bounds.Upper - Bounds.Lower)) * yRange)- dotDiameter/2, dotDiameter, ID_PLATE+"");
				
			}
			
			
			
			
			
			// Draw the sigmoidal fit if desired
//			if (SigmoidFit && sigmoidalFitCoeffs_plot != null)
//			{
//				float minVal_thisNorm = (float) ((Min_real - MinValue) / (MaxValue-MinValue));
//				float maxVal_thisNorm = (float) (((Max_real - MinValue) / (MaxValue - MinValue)));
//				float maxVal_scale = (float)((maxVal_thisNorm-minVal_thisNorm));
//
//				int num = (numP - 1) * 10;
//				// double d = (double)(numPoints)/(double)(num);
//				double maxReal = tools.MathOps.max(xVals_real);
//				double minReal = tools.MathOps.min(xVals_real);
//				double xRange_real = maxReal - minReal;
//				// System.out.println("xRange: "+minReal+" - "+maxReal);
//				// double d_real = xRange_real/num;
//				ArrayList<Point.Double> realValues = new  ArrayList<Point.Double>();
//
//				if (sigmoidalFitCoeffs_plot != null)
//					for (int c = 1; c < num; c++)
//					{
//						// Drawing the Stdev bars
//						g2.setColor(Color.gray);
//						double xVal1_real = (double) (c - 1) / (double) (num)	* xRange_real + minReal;
//						double xVal2_real = (double) c / (double) (num)* xRange_real + minReal;
//
//						double xVal1_plot = ((double) c - 1f) / (double) (num)* (double) (numP - 1);
//						double xVal2_plot = ((double) c / (double) num * (double) (numP - 1));
//
//						int x1_plot = (int) ((xVal1_plot)/ (double) (numP + 1) * xRange + xStart);
//						int x2_plot = (int) ((xVal2_plot)/ (double) (numP + 1) * xRange + xStart);
//
//						double lastVal_plot = (((Max_plot-Min_plot)*getSigmoidValue_y(xVal1_plot,sigmoidalFitCoeffs_plot[0], sigmoidalFitCoeffs_plot[1])+ Min_plot)- Bounds.Lower) / (Bounds.Upper - Bounds.Lower);
//						double thisVal_plot  = (((Max_plot-Min_plot)*getSigmoidValue_y(xVal2_plot, sigmoidalFitCoeffs_plot[0], sigmoidalFitCoeffs_plot[1])+ Min_plot) - Bounds.Lower) / (Bounds.Upper - Bounds.Lower);
//
//						double lastVal_real = maxVal_scale* (getSigmoidValue_y(xVal1_real, sigmoidalFitCoeffs_real[0], sigmoidalFitCoeffs_real[1]));
//						double thisVal_real = maxVal_scale* (getSigmoidValue_y(xVal2_real,sigmoidalFitCoeffs_real[0],sigmoidalFitCoeffs_real[1]));
//
//						Point.Double p = new Point.Double(xVal2_real, thisVal_real);
//						realValues.add(p);
////						System.out.println(""+xVal2_real+ "  ,  "+thisVal_real+ "  ,  "+xVal2_plot+"  ,"+thisVal_plot);
//
//						int y1 = yStart - (int) (lastVal_plot * yRange);
//						int y2 = yStart - (int) (thisVal_plot * yRange);
//
//						g2.setColor(color);
//						g2.setStroke(MainGUI.Stroke_4);
//						g2.drawLine(x1_plot, y1, x2_plot, y2);
//						g2.setStroke(MainGUI.Stroke_1);
//
//					}
//
////				System.out.println("xVals_real: ");
////				for (int i = 0; i < realValues.size(); i++)
////				{
////					Point.Double p = realValues.get(i);
////					System.out.println(""+p.x);
////				}
////				System.out.println("yVals_real: ");
////				for (int i = 0; i < realValues.size(); i++)
////				{
////					Point.Double p = realValues.get(i);
////					System.out.println(""+p.y);
////				}
//
//			}
			
			//				if (SigmoidFit)
//				{
//					float minVal_thisNorm = (float) ((Min_real - MinValue) / (MaxValue-MinValue));
//					float maxVal_thisNorm = (float) (((Max_real - MinValue) / (MaxValue - MinValue)));
//					float maxVal_scale = (float)((maxVal_thisNorm-minVal_thisNorm));
//					int num = (numP - 1) * 10;
//					// double d = (double)(numPoints)/(double)(num);
////					double maxReal = tools.MathOps.max(xVals_real);
////					double minReal = tools.MathOps.min(xVals_real);
////					double xRange_real = maxReal - minReal;
//					// Drawing the EC50/IC90 value
//					int[] arr = new int[3];
//					double xPlot = getSigmoidValue_x(0.1d,sigmoidalFitCoeffs_plot[0], sigmoidalFitCoeffs_plot[1]);
//					double xReal_10 =  getSigmoidValue_x(0.1d,sigmoidalFitCoeffs_real[0], sigmoidalFitCoeffs_real[1]);
//					xPlot = (int) ((xPlot) / (double) (numP + 1) * xRange + xStart);
//					double y = maxVal_scale * 0.1d;
//					int y2 = yStart - (int) (y * yRange) - 3;
//					g2.setColor(Color.black);
//					// g2.fillRect((int)xPlot-3, y2-3, 6,6);
//					arr[0] = (int) xPlot;
//					// IC50 value
//					xPlot = getSigmoidValue_x(0.5d, sigmoidalFitCoeffs_plot[0], sigmoidalFitCoeffs_plot[1]);
//					double xReal_50 =  getSigmoidValue_x(0.5d,sigmoidalFitCoeffs_real[0], sigmoidalFitCoeffs_real[1]);
//					xPlot = (int) ((xPlot) / (double) (numP + 1) * xRange + xStart);
//					y = maxVal_scale * 0.5d;
//					y2 = yStart - (int) (y * yRange) - 3;
//					g2.setColor(Color.black);
//					// g2.fillRect((int)xPlot-3, y2-3, 6,6);
//					arr[1] = (int) xPlot;
//					// IC10 value
//					xPlot = getSigmoidValue_x(0.9d, sigmoidalFitCoeffs_plot[0], sigmoidalFitCoeffs_plot[1]);
//					double xReal_90 =  getSigmoidValue_x(0.9d,sigmoidalFitCoeffs_real[0], sigmoidalFitCoeffs_real[1]);
//					xPlot = (int) ((xPlot) / (double) (numP + 1) * xRange + xStart);
//					y = maxVal_scale * 0.9d;
//					y2 = yStart - (int) (y * yRange);
//					g2.setColor(Color.black);
//					// g2.fillRect((int)xPlot-3, y2-3, 6,6);
//					arr[2] = (int) xPlot;
//
//
//					// Plotting the whisker plot at bottom
//					GradientPaint gradient = new GradientPaint(arr[0], (yStart + ID* 10 + 7), Color.black, arr[2], (yStart + ID * 10 + 7),color);
//					g2.setPaint(gradient);
//					if (arr[0] < arr[2])
//						g2.fillRect(arr[0], (yStart + ID * 10 + 7), Math.abs(arr[2]	- arr[0]), 2);
//					else
//						g2.fillRect(arr[2], (yStart + ID * 10 + 7), Math.abs(arr[2]- arr[0]), 2);
//					g2.setColor(color);
//					g2.fillRect(arr[0], (yStart + ID * 10 + 5), 5, 5);
//					g2.fillRect(arr[1], (yStart + ID * 10 + 5), 5, 5);
//					g2.fillRect(arr[2], (yStart + ID * 10 + 5), 5, 5);
//					g2.setColor(Color.black);
//					g2.drawRect(arr[0], (yStart + ID * 10 + 5), 5, 5);
//					g2.drawRect(arr[1], (yStart +ID * 10 + 5), 5, 5);
//					g2.drawRect(arr[2], (yStart + ID * 10 + 5), 5, 5);
//					//Writing the values
//					g2.drawString(""+MainGUI.nf.format(xReal_10), arr[0], (yStart + ID * 10 + 5));
//					g2.drawString(""+MainGUI.nf.format(xReal_50), arr[1], (yStart + ID * 10 + 5));
//					g2.drawString(""+MainGUI.nf.format(xReal_90), arr[2], (yStart + ID * 10 + 5));
//				}
//				else
			
			//Drawing the CrossHairs if applicable
			TheCrossHair.draw(g2);
			
			
			//Drawing the DoseResponse range (10-50-90%)
//			if (Display_DoseResponseRange)
//				drawDoseResponseRanges(g2, plotToSVG);
			
			
		}
		
		
		
		
		/** Given a yValue fraction (ex: 0.5, equals 50% activation), returns the first X value to give that value
		 * @author BLM*/
		public float getXValofGivenYfraction(float yFraction)
		{
			float x = 0;
			int numPoints = TheDataPoints.length;
			//Scale it
			for (int i = 0; i < numPoints-1; i++)
			{
				DataPoint p0 = TheDataPoints[i];
				DataPoint p1 = TheDataPoints[i+1];
				float y0_norm = p0.getY_plot_self();
				float y1_norm = p1.getY_plot_self();
				float x0 = p0.x_plot;
				float x1 = p1.x_plot;
				
				
				
				// Is this point bound by the two points?
				if ((yFraction > y0_norm && yFraction < y1_norm)||(yFraction < y0_norm && yFraction > y1_norm))
				{
					
					float m = (y1_norm-y0_norm)/(x1-x0);
					x = (yFraction-y0_norm)/m + x0;
					break;
				}
				
			}
			return x;
		}
		
		/** Initializes the Min and Max of the series
		 * @author BLM*/
		private void initMinMax()
		{
			DataPoint[] points = TheDataPoints;
			
			Max_real = Float.NEGATIVE_INFINITY;
			Min_real = Float.POSITIVE_INFINITY;
			Max_plot = Float.NEGATIVE_INFINITY;
			Min_plot = Float.POSITIVE_INFINITY;
			int numPoints = TheDataPoints.length;
			for (int p = 0; p < numPoints; p++)
			{
				//Real Values
				if (points[p].y_real > Max_real)
					Max_real = points[p].y_real;
				if (points[p].y_real < Min_real)
					Min_real = points[p].y_real;
				
				//Plot Values
				if (points[p].y_plot > Max_plot)
					Max_plot = points[p].y_plot;
				if (points[p].y_plot < Min_plot)
					Min_plot = points[p].y_plot;
			}
			
		}
		
		/** Attemps to fit a simple sigmoid curve to the series
		 * @author BLM*/
		public void curveFit()
		{
			// Should we compute the Sigmoid fit? If so, need to compute and store
			// them
			sigmoidalFitCoeffs_plot = null;
			sigmoidalFitCoeffs_real = null;
			int numPoints = TheDataPoints.length;
			if (SigmoidFit && numPoints > 3)
			{
				// System.out.println("Computing Sigmoid Fit");
				sigmoidalFitCoeffs_plot = new float[2];
				sigmoidalFitCoeffs_real = new float[2];
				
				// For each series, find the best fitting sigmoid based on equation
				// y=1/(1+exp(beta(x-mu)))
				
				// init temp sigmoid values
				for (int p = 0; p < 2; p++)
				{
					sigmoidalFitCoeffs_plot[p] = 0;
					sigmoidalFitCoeffs_real[p] = 0;
				}
				
				Point.Double[] points_plot = null;
				Point.Double[] points_real = null;
				
				//Dont want min and max to be 0 and 1 respectively, want to force them to be 0.01 and 0.99, prevents singularities later on
				double range = Max_plot-Min_plot;
				double onePercent = 0.01d*range;
				Max_plot+=onePercent;
				Min_plot-=onePercent;
				//
				
				points_plot = new Point.Double[numPoints];
				points_real = new Point.Double[numPoints];
				for (int p = 0; p < numPoints; p++)
				{
					double val = 0;
					val = ((TheDataPoints[p].y_plot - Min_plot) / (Max_plot - Min_plot));
					points_plot[p] = new Point.Double((double)p, (float) tools.MathOps.log(1d / val - 1d));
					System.out.println(TheDataPoints[p].y_plot +"   "+Min_plot+"   "+Max_plot);
				}
				
				for (int i = 0; i < points_plot.length; i++)
					System.out.println(points_plot[i].x);
				System.out.println("____");
				for (int i = 0; i < points_plot.length; i++)
					System.out.println(points_plot[i].y);
				
				double[] lineData_plot = tools.MathOps.computeLinearRegression(points_plot);
				if (lineData_plot == null )
				{
					sigmoidalFitCoeffs_plot = null;
					sigmoidalFitCoeffs_real = null;
				}
				else
				{
					sigmoidalFitCoeffs_plot[0] = (float) lineData_plot[0];
					sigmoidalFitCoeffs_plot[1] = (float) (lineData_plot[1] / -lineData_plot[0]);
				}
				
				
				
			}
		}
		
		/** Crosshair object that tracks the yvalues of each data series when moused over
		 * @author BLM*/
		private class CrossHairPoint
		{
			public int x;
			public int y;
			
			public CrossHairPoint(int x_, int y_)
			{
				x = x_;
				y = y_;
			}
			
			public CrossHairPoint()
			{
				x = -10;
				y = -10;
			}
			
			public void draw(Graphics2D g2)
			{
				if (x<0 || y<0)
					return;
				
				//Scale it
				float xp = x-Xstart;
				float xPlot = xp/(float)xStep;
				float xf = (float)Math.floor(xPlot);
				float xc = (float)Math.ceil(xPlot);
				float xResidual = xPlot-xf;
				
				int numPoints = TheDataPoints.length;
				if (xc>=numPoints||xf<0)
					return;
				
				DataPoint p0 = TheDataPoints[(int)xf];
				DataPoint p1 = TheDataPoints[(int)xc];
				
				
				float yp0 = p0.y_plot;
				float yp1 = p1.y_plot;
				float slope = (float)(yp1-yp0)/(float)(xc-xf);
				float yReal = (int)(slope*xResidual)+yp0;
				
				int y0 = Ystart - (int) (p0.getY_plot() * yRange);
				int y1 = Ystart - (int) (p1.getY_plot() * yRange);
				
				slope = (float)(y1-y0)/(float)(xc-xf);
				int yp = (int)(slope*xResidual)+y0;
				
				g2.setColor(Color.DARK_GRAY);
				int len = 8;
				g2.drawLine(x-2, yp, x-len, yp);
				g2.drawLine(x+2, yp, x+len, yp);
				g2.drawLine(x, yp-2, x,yp-len);
				g2.drawLine(x, yp+2, x, yp+len);
				g2.drawOval(x-2, yp-2, 4,4);
				
				
				g2.setColor(Color.black);
				g2.drawString("["+MainGUI.nf.format(xPlot) +"  ,  "+MainGUI.nf.format(yReal)+"]", x+5, yp-5);
			}
		}
		/** DataPoint structure that comprises the Data Series structure
		 * @author BLM*/
		public class DataPoint
		{
			public float x_plot;
			public float y_plot;
			public float x_real;
			public float y_real;
			public float stdev;
			public Model_Well TheWell;
			private Rectangle box;
			private Rectangle closeBox;
			private Rectangle bounds_histo;
			private boolean DrawMiniHistogram;
			private Polygon MiniHistogram;
			
			public DataPoint(float x, float y, float stdev_, float y_plot_, Model_Well well_)
			{
				TheWell = well_;
				x_real = x;
				y_real = y;
				x_plot = x;
				y_plot = y_plot_;
				stdev = stdev_;
				box = new Rectangle();
				closeBox = new Rectangle();
				bounds_histo = null;
			}
			public float getY_plot_self()
			{
				return (float)((y_plot-Min_plot)/(Max_plot-Min_plot));
			}
			public float getY_plot()
			{
				return (float)((y_plot-Bounds.Lower)/(Bounds.Upper-Bounds.Lower));
			}
			public float getY_real()
			{
				return (float)((y_real-Bounds.Lower)/(Bounds.Upper-Bounds.Lower));
			}
			public float getStdev()
			{
				return stdev;
			}
			
			public void drawMiniHistogram(boolean  boo)
			{
				DrawMiniHistogram = boo;
				if (!boo)
					bounds_histo=null;
				repaint();
			}
			
			public void drawMarker_Rectangle(Graphics2D g2, int x, int y, int size, String name)
			{
				float nLen = name.length();
				if (nLen < 2)
					nLen = 1.5f;
				Color color = (g2.getColor());
				g2.setColor(Color.white);
				
				box.x = x+1;
				box.y = y-3;
				box.width = (int)(nLen*10);
				box.height = size+3;
				
				g2.fill(box);
				g2.setColor(Color.black);
				g2.draw(box);
				g2.drawString(name+"",x+4,y+10);
				g2.setColor(color);
			}
			
			public void drawMarker_Circle(Graphics2D g2, int x, int y, int size, String name)
			{
				float nLen = name.length();
				if (nLen < 2)
					nLen = 1.5f;
				
				
				box.x = x+1;
				box.y = y-3;
				box.width = (int)(nLen*10);
				box.height = size+3;
				
				g2.setColor(color);
				int val = (int)(box.height*0.7f);
				g2.fillOval(box.x, box.y+4, val, val);
				
			}
			
			public void drawMiniHistogram(Graphics2D g2)
			{
				if (DrawMiniHistogram)
				{
					int height = 50;
					int width = 50;
					
					//Setting the current position of the histobox if null
					if (bounds_histo==null)
						bounds_histo = new Rectangle(box.x+10, box.y-height, width, height);
					
					Point p = new Point(bounds_histo.x+10, bounds_histo.y-height);
					
					closeBox.x = bounds_histo.x+width-12;
					closeBox.y = bounds_histo.y+4;
					closeBox.width = 7;
					closeBox.height = 7;
					
					
					// Drawing border
					g2.setColor(Color.white);
					g2.fillRect(bounds_histo.x, bounds_histo.y, width, height);
					for (int i = 0; i < 4; i++)
					{
						g2.setColor(colors[i]);
						g2.drawRect(bounds_histo.x - i, bounds_histo.y - i,width+2*i, height+2*i);
					}
					
					//Drawing the Histogram
					g2.setColor(Color.black);
					MiniHistogram = TheWell.getGUI().getHistogram(
							bounds_histo.x, bounds_histo.y, width, height);
					if (MiniHistogram!=null)
					{
						g2.setColor(Color.black);
						g2.fillPolygon(MiniHistogram);
					}
					Font f = g2.getFont();
					g2.setFont(MainGUI.Font_8);
					g2.setColor(Color.lightGray);
					g2.drawString("x", closeBox.x+1, closeBox.y+7);
					g2.setFont(f);
				}
				else
				{
					closeBox.x = 0;
					closeBox.y = 0;
					closeBox.width = 0;
					closeBox.height = 0;
				}
			}
		}
		
		
		
		
	}
	
	
	/** Returns the plot type integer identifier which defines whether the line series are going to represent Rows, Columns, or transplate
	 * @author BLM*/
	public int getPlotType()
	{
		return PlotType;
	}
	
	/** Sets whether to smooth the data in the line plot viewer or not
	 * @author BLM*/
	public void setSmoothData(boolean boo)
	{
		SmoothData = boo;
	}
	/** Returns boolean whether to smooth the data in the line plot viewer or not
	 * @author BLM*/
	public boolean shouldSmoothData()
	{
		return SmoothData;
	}
	/** Returns the color[] representing the colors for the data series
	 * @author BLM*/
	public Color[] getSeriesColors()
	{
		return colors;
	}
	/** Returns the number of series in this line plot
	 * @author BLM*/
	public int getNumSeries()
	{
		if (TheSeries==null)
			return 0;
		return TheSeries.length;
	}
	/** Returns the Series
	 * @author BLM*/
	public Series[] getTheSeries()
	{
		return TheSeries;
	}
	/** Returns the x-range for plotting the line plots
	 * @author BLM*/
	public int getXRange()
	{
		return xRange;
	}
	/** Returns the x-coordinate where the plots begin
	 * @author BLM*/
	public int getXstart()
	{
		return Xstart;
	}
	/** Returns the Bounds of this plot
	 * @author BLM*/
	public Bound getBound()
	{
		return Bounds;
	}
	/** Returns the minimum value of all the data series
	 * @author BLM*/
	public double getMinValue()
	{
		return MinValue;
	}
	/** Returns the maximum value of all the data series
	 * @author BLM*/
	public double getMaxValue()
	{
		return MaxValue;
	}
}

