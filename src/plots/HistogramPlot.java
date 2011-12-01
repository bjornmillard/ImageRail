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

import features.Feature;
import gui.MainGUI;
import imPanels.ImageCapturePanel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import models.Model_Well;
import segmentedobject.Cell;
import tools.SVG_writer;
import dialogs.CaptureImage_Dialog;

public class HistogramPlot extends JPanel implements ImageCapturePanel
{
	private double[][] TheData;
	private Bar[][] TheBars;
	private double[][] bins;
	private HistogramPlot TheHistogram;
	static final int XMARGIN = 20;
	static final int YMARGIN = 60;
	private int axisLength_Y;
	private int	borderLenght_x;
	private int	XStart;
	private int	XEnd;
	private int	YStart;
	private int	YEnd;
	private int	XLen;
	private int	ZStart;
	private int[] maxBinValues;
	private int NumBins;
	private int BarWidth;
	private boolean Init;
	private AlphaComposite barComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	private int panel_width;
	private int panel_height;
	private Color[] colors;
	private Color BufferColor;
	private	double LowerBounds;
	private double UpperBounds;
	private JButton DrawBoxes;
	private JButton DrawLines;
	private JButton SmoothData;
	private JButton ResetViewButton;
	public JButton LogScaleButton;
	public JButton FitGaussians;
	private MainPanel ThePanel;
	private double[][] valuesToPlot;
	//For the 3d rotational viewing of multiple plots
	private double[][] ProjectionMatrix;
	private double[][] RotationMatrix;
	private Point lastPoint;
	private Feature TheFeature;
	private Model_Well[] TheWells;
	/** Model_Well name legend correlated with plot color*/
	private Legend TheLegend;
	/** Last point where the legend was so we can keep it there even if have to create new*/
	private Point Legend_xy;
	/** */
//	private float xLabel_min = -1;
//	private float xLabel_max = -1;
	
	
	public HistogramPlot(Model_Well[] wells, Feature feature)
	{
		//finding only those wells that actually have data
		int len = wells.length;
		ArrayList<Model_Well> arr = new ArrayList<Model_Well>();
		for (int i = 0; i < len; i++)
 {
			ArrayList<Cell> cells = wells[i].getCells();
			if (cells != null && cells.size() > 0)
				arr.add(wells[i]);
		}
		len = arr.size();
		TheWells = new Model_Well[len];
		for (int i = 0; i < len; i++)
			TheWells[i] = (Model_Well)arr.get(i);

		TheFeature = feature;
		TheHistogram = this;
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setLayout(new BorderLayout());
		lastPoint = new Point(0,0);
		
		panel_width = 330;
		panel_height = 450;
		setPreferredSize(new Dimension(panel_width, panel_height));
		setSize(panel_width,panel_height);
		
		colors = new Color[4];
		colors[0] = new Color(0.1f, 0.1f, 0.1f);
		colors[1] = new Color(0.3f, 0.3f, 0.3f);
		colors[2] = new Color(0.5f, 0.5f, 0.5f);
		colors[3] = new Color(0.7f, 0.7f, 0.7f);
		BufferColor = new Color(0.9f,0.9f,0.9f);
		
		Init = true;
		
		
		
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,6));
		TheHistogram.add(topPanel, BorderLayout.NORTH);
		
		
		
		JToolBar tbar = new JToolBar();
		add(tbar, BorderLayout.NORTH);
		
		LogScaleButton = new JButton(tools.Icons.Icon_Log_selected);
		LogScaleButton.setToolTipText( "Log Scale");
		LogScaleButton.setSelected(true);
		LogScaleButton.addActionListener(new ActionListener()
										 {
					public void actionPerformed(ActionEvent ae)
					{
						LogScaleButton.setSelected(!LogScaleButton.isSelected());
						if(LogScaleButton.isSelected())
							LogScaleButton.setIcon(tools.Icons.Icon_Log_selected);
						else
							LogScaleButton.setIcon(tools.Icons.Icon_Log);
						updateDataAndBins(TheWells, models.Model_Main.getModel().getTheSelectedFeature_Index());
						updateHistograms_wBounds(TheData);
						repaint();
					}
				});
		tbar.add(LogScaleButton);
		
		DrawBoxes = new JButton(tools.Icons.Icon_Fill_selected);
		DrawBoxes.setToolTipText( "Fill Histogram");
		DrawBoxes.setSelected(true);
		DrawBoxes.addActionListener(new ActionListener()
									{
					public void actionPerformed(ActionEvent ae)
					{
						DrawBoxes.setSelected(!DrawBoxes.isSelected());
						if(DrawBoxes.isSelected())
							DrawBoxes.setIcon(tools.Icons.Icon_Fill_selected);
						else
							DrawBoxes.setIcon(tools.Icons.Icon_Fill);
						repaint();
					}
				});
		DrawBoxes.setSelected(true);
		tbar.add(DrawBoxes);
		
		
		SmoothData = new JButton(tools.Icons.Icon_Smooth_selected);
		SmoothData.setToolTipText( "Smooth Histogram");
		SmoothData.setSelected(true);
		SmoothData.addActionListener(new ActionListener()
									 {
					public void actionPerformed(ActionEvent ae)
					{
						SmoothData.setSelected(!SmoothData.isSelected());
						if(SmoothData.isSelected())
							SmoothData.setIcon(tools.Icons.Icon_Smooth_selected);
						else
							SmoothData.setIcon(tools.Icons.Icon_Smooth);
						
						updateHistograms_wBounds(TheData);
						repaint();
					}
				});
		SmoothData.setSelected(true);
		tbar.add(SmoothData);
		
		
		
		ResetViewButton = new JButton(tools.Icons.Icon_Reset);
		ResetViewButton.setToolTipText( "Reset Histogram View");
		ResetViewButton.addActionListener(new ActionListener()
										  {
					public void actionPerformed(ActionEvent ae)
					{
						
						initViewingBasis();
						repaint();
					}
				});
		tbar.add(ResetViewButton);
		
		JButton but = new JButton(tools.Icons.Icon_Record);
		but.setToolTipText("Export Bin Values");
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						if (TheWells!=null && TheBars!=null)
						{
							JFileChooser fc = null;
							if (models.Model_Main.getModel().getTheDirectory()!=null)
								fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
							else
								fc = new JFileChooser();
							
							File outDir = null;
							
							fc.setDialogTitle("Save as...");
							int returnVal = fc.showSaveDialog(ThePanel);
							if (returnVal == JFileChooser.APPROVE_OPTION)
							{
								outDir = fc.getSelectedFile();
								outDir = (new File(outDir.getAbsolutePath()+".csv"));
							}
							else
								System.out.println("Open command cancelled by user." );
							
							if (outDir!=null)
							{
								models.Model_Main.getModel().setTheDirectory( new File(outDir.getParent()));
								PrintWriter pw = null;
								try
								{
									pw = new PrintWriter(outDir);
								}
								catch (FileNotFoundException e) {}
								if (pw!=null)
								{
									int numWells = TheWells.length;
									int numBins = bins[0].length;
									pw.println("Histogram Bin Values,");
									pw.println(""+TheFeature.Name);
									
									pw.print(",");
									for (int i = 0; i < numWells; i++)
										pw.print(TheWells[i].name+",");
									pw.println();
									
									for (int j = 0; j < numBins; j++)
									{
										//Printing what each bin represents
										String val = MainGUI.nf.format((float)((float)j/(float)(numBins)*(UpperBounds-LowerBounds)+LowerBounds));
										pw.print(val+",");
										//Now printing the number of cells in this bin
										for (int i = 0; i < numWells; i++)
											pw.print(bins[i][j]+",");
										
										pw.println();
									}
									
								}
								pw.flush();
								pw.close();
							}
							
							repaint();
						}
					}
				});
		tbar.add(but);
		
		
		
		
		JButton but5 = new JButton(tools.Icons.Icon_Camera);
		tbar.add(but5);
		but5.setToolTipText("Capture Image of Plot");
		but5.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						CaptureImage_Dialog s = new CaptureImage_Dialog(TheHistogram);
					}
				});
		
		
		
		// constructing the data from the wells based on the desired feature
		NumBins = 100;
		
		int feature_index = models.Model_Main.getModel().getTheSelectedFeature_Index();
		TheHistogram.updateBins(TheHistogram.constructData(TheWells, feature_index));
		updateDataAndBins(TheWells, feature_index);
		
		ThePanel = new MainPanel();
		
		add(ThePanel, BorderLayout.CENTER);
		updateLegend();
	}
	
	private void updateLegend()
	{
		updateDimensions();
		String[] speciesNames = new String[TheWells.length];
		Color[] col = new Color[TheWells.length];
		for (int i = 0; i < TheWells.length; i++)
		{
			speciesNames[i] = TheWells[i].name;
			col[i] = tools.ColorRama.getColor(i+1);
		}
		if(Legend_xy==null)
			Legend_xy = new Point(40, 40);
		TheLegend = new Legend("Well:", speciesNames, col, Legend_xy.x,
				Legend_xy.y);
	}
	
	public void updateDataAndBins(Model_Well[] wells, int feature_index)
	{
		float[][] data = constructData(wells, feature_index);
		updateBins(data);
	}
	
	public void updateBins(float[][] data)
	{
		if (data==null)
			return;
		
		int numPlots = data.length;
		bins = new double[numPlots][NumBins];
		for (int p = 0; p < numPlots; p++)
			for (int i = 0; i < NumBins; i++)
				bins[p][i] = 0;
		
		if (Init)
		{
			initBinData(data);
			Init = false;
		}
		else
			initBinData(data);
		
		
		
		validate();
		repaint();
	}
	
	
	public void updateHistograms_wBounds(double[][] data)
	{
		if (data==null)
			return;
		
		int numPlots = data.length;
		bins = new double[numPlots][NumBins];
		for (int p = 0; p < numPlots; p++)
			for (int i = 0; i < NumBins; i++)
				bins[p][i] = 0;
		
		
		reBinData(data);
		
		validate();
		repaint();
		
	}
	
	public float[][] constructData(Model_Well[] wells, int feature_index)
	{
		//finding only those wells that actually have data
		int len = wells.length;
		ArrayList<Model_Well> arr = new ArrayList<Model_Well>();
		for (int i = 0; i < len; i++)
 {
			ArrayList<Cell> cells = wells[i].getCells();

			if (cells != null && cells.size() > 0)
				arr.add(wells[i]);
		}
		
		len = arr.size();
		TheWells = new Model_Well[len];
		for (int i = 0; i < len; i++)
			TheWells[i] = (Model_Well)arr.get(i);
		
		float[][] data = null;
		int numWells = TheWells.length;
		
		LowerBounds = Double.POSITIVE_INFINITY;
		UpperBounds = Double.NEGATIVE_INFINITY;
		
		if (numWells>0)
		{
			data = new float[numWells][];
			for (int w = 0; w < numWells; w++)
			{
				float[][] allCells = TheWells[w].getCell_values();

				int numCells = allCells.length;
				
				data[w] = new float[numCells];
				int counter=0;
				for (int i = 0; i < numCells; i++)
				{
					float val = allCells[i][feature_index];
					if (LogScaleButton.isSelected())
						val = (float)tools.MathOps.log(val);
					if (val>Float.NEGATIVE_INFINITY && val< Float.POSITIVE_INFINITY)
					{
						data[w][counter] = val;
						counter++;
						if (val<LowerBounds)
							LowerBounds = val;
						if (val>UpperBounds)
							UpperBounds = val;
					}
				}
			}
		}
		
		return data;
	}
	
	public void copySettings(HistogramPlot oldPlot)
	{
		DrawBoxes.setSelected(oldPlot.DrawBoxes.isSelected());
//		DrawLines.setSelected(oldPlot.DrawLines.isSelected());
		SmoothData.setSelected(oldPlot.SmoothData.isSelected());
		LogScaleButton.setSelected(oldPlot.LogScaleButton.isSelected());
		
		if (oldPlot.ProjectionMatrix!=null && oldPlot.RotationMatrix!=null)
		{
			ProjectionMatrix = oldPlot.ProjectionMatrix;
			RotationMatrix = oldPlot.RotationMatrix;
		}
	}
	
	public void updateDimensions()
	{
		XStart = 100;
		XEnd = 800;
		ZStart = 0;
		XLen = XEnd-XStart;
		
		YStart = (int)((float)TheHistogram.getHeight())-145;
		YEnd = YStart-30;
		
		BarWidth = (int)((float)XLen/(float)NumBins);
	}
	
	public void initBinData(float[][] data)
	{
		if (data==null)
			return;
		int numPlots = data.length;
		maxBinValues = new int[numPlots];
		
		for (int p = 0; p < numPlots; p++)
			for (int i = 0; i < data[p].length; i++)
			{
				int index = (int)((((float)data[p][i]-LowerBounds)/(float)(UpperBounds-LowerBounds))*NumBins);
				if (index>=NumBins)
					index = NumBins-1;
				else if (index<0)
					index=0;
				bins[p][index]++;
				//keeping track of max bin value
				if (bins[p][index]>maxBinValues[p])
					maxBinValues[p] = (int)bins[p][index];
			}
	}
	
	public void reBinData(double[][] data)
	{
		int numPlots = data.length;
		for (int p = 0; p < numPlots; p++)
		{
			int numCells = data[p].length;
			for (int i = 0; i < numCells; i++)
			{
				int index = (int)((((float)data[p][i]-LowerBounds)/(float)(UpperBounds-LowerBounds))*NumBins);
				if (index>=NumBins)
					index = NumBins-1;
				else if (index<0)
					index=0;
				
				bins[p][index]++;
			}
		}
	}
	
	/** Smooth the data by simply doing a 5-point running average
	 * @author BLM*/
	private double[][] lowPassFilter(double[][] data, int filterLen)
	{
		if (data==null || data.length == 0 || data[0] == null)
			return null;
		
		int numPlots = data.length;
		int len = NumBins;
		
		double[][] avgData = new double[numPlots][NumBins];
		double val =0;
		
		for (int p = 0; p < numPlots; p++)
		{
			avgData[p][len-1] = data[p][len-1];
			for (int i = 0; i < len-1; i++)
			{
				if (i<(filterLen/2f))
				{
					for (int j = i; j < (i+filterLen); j++)
						val+=data[p][j];
					val=val/(filterLen);
					avgData[p][i] = val;
				}
				else if(i > len-(int)(filterLen/2f))
				{
					for (int j = i; j < len; j++)
						val+=data[p][j-i];
					val=val/(filterLen);
					avgData[p][i] = val;
				}
				else
				{
					for (int j = (i-(int)(filterLen/2f)); j < (i+(int)(filterLen/2f)); j++)
						val+=data[p][j];
					
					val=val/(1+filterLen);
					avgData[p][i] = val;
				}
				
			}
		}
		return avgData;
	}
	
	// /** Constructs a normalized histogram of numBins bins given the cells and
	// a feature of interest
	// * @author BLM*/
	// static public float[] getHistogram_normalized(Cell_RAM[] cells , Feature
	// feature, int numBins)
	// {
	// int numC = cells.length;
	// float max = Float.NEGATIVE_INFINITY;
	// float min = Float.POSITIVE_INFINITY;
	// for (int p = 0; p < numC; p++)
	// {
	// //TODO
	// // float val = (float)feature.getValue(cells[p]);
	// // if (val > max)
	// // max = val;
	// // if (val < min)
	// // min = val;
	// }
	// float range = max-min;
	// float step = range/numBins;
	// float[] bins = new float[numBins];
	// for (int p = 0; p < numC; p++)
	// {
	// //TODO
	// // float val = (float)feature.getValue(cells[p]);
	// // int ind = (int)((val-min)/(max-min)*numBins);
	// // System.out.println("ind: "+ind);
	// // if (ind<0)
	// // ind = 0;
	// // if (ind>numBins-1)
	// // ind = numBins-1;
	// // bins[ind]++;
	// }
	// for (int i = 0; i < numBins; i++)
	// bins[i] = bins[i]/(float)numC;
	//
	// return bins;
	// }
	
	public void captureSVG(PrintWriter pw)
	{
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle("Histogram Plot");
		
		updateDimensions();
		
		ThePanel.draw(g2,true);
		
		g2.printEnd();
		pw.flush();
		pw.close();
	}
	
	public void captureImage(File file, String imageType)
	{
		int width = getWidth();
		int height = getHeight();
		
		BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)im.getGraphics();
		
		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r,c,Color.WHITE.getRGB());
		
		ThePanel.draw(g2, false);
		
		try
		{
			ImageIO.write(im, imageType, file);
		}
		catch (IOException e) {System.out.println("**Error Printing Image**");}
	}
	
	/** The Main graphing panel for thise histogram
	 * @author BLM*/
	public class MainPanel extends JPanel implements MouseListener, MouseMotionListener
	{
		
		public MainPanel()
		{
			setBackground(Color.white);
			addMouseListener(this);
			addMouseMotionListener(this);
			validate();
			repaint();
		}
		
		
		
		
		
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			
			if (bins==null)
				return;
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			
			draw(g2, false);
		}
		
		public void draw(Graphics2D g2, boolean plotToSVG)
		{
			drawHisto(g2, ProjectionMatrix, RotationMatrix, plotToSVG);
			
			if(TheLegend!=null)
				TheLegend.draw(g2, plotToSVG);
		}
		
		public double getOffsetX()
		{
			return (getSize().width / 2) -100 ;
		}
		public double getOffsetY()
		{
			return (getSize().height / 2) - 100;
		}
		
		public void mouseClicked(MouseEvent p1)
		{
			if (p1.getClickCount()>=2)
			{
				
			}
		}
		public void mousePressed(MouseEvent p1)
		{
			if(TheLegend!=null && TheLegend.contains(p1.getPoint()))
			{
				TheLegend.setDragging(true);
			}
			else
				lastPoint = p1.getPoint();
		}
		public void mouseDragged(MouseEvent p1)
		{
			//First check to see if dragging legend
			if(TheLegend!=null&& TheLegend.isDragging())
			{
				TheLegend.setX((int)(p1.getPoint().x-TheLegend.getWidth()/2f));
				TheLegend.setY((int)(p1.getPoint().y-TheLegend.getHeight()/2f));
				Legend_xy.x = TheLegend.getX();
				Legend_xy.y = TheLegend.getY();
				repaint();
				return;
			}
			else if (p1.isShiftDown())			/** MetaDown means the user wants move coord system*/
			{
//				System.out.println("Zooming**");
				//zoom
				double dzoom = lastPoint.y-p1.getY();
				for (int i =0; i < 3; i ++)
					ProjectionMatrix[i][i] += -dzoom*0.01;
				
				repaint();
			}
			else if (p1.isMetaDown())
			{
//				System.out.println("Rotating**");
				//rotation
				double sensitivity = 0.02;
				int dy = lastPoint.y-p1.getY();
				int dx = lastPoint.x-p1.getX();
				
				//calculating the rotation axis that is perp to the drag vector
				double mag = Math.sqrt(Math.pow(dy,2)+Math.pow(dx,2));
				double[] dragVec = {dx/mag,dy/mag};
				
				double[] rotAxis = tools.MathOps.rotate2Dvector90degrees(dragVec);
				double[][] rotMatrix = calculateRotationMatrix(rotAxis, sensitivity);
				
				RotationMatrix = tools.MathOps.multiply(rotMatrix,RotationMatrix);
				repaint();
			}
			else
			{
//				System.out.println("Translating**");
				//translation
				int sensitivity = 1;
				int dy = p1.getY()-lastPoint.y;
				int dx = p1.getX()-lastPoint.x;
				
				ProjectionMatrix[1][3] += sensitivity*dy;
				ProjectionMatrix[0][3] += sensitivity*dx;
				
				repaint();
			}
			lastPoint = p1.getPoint();
		}
		public void mouseReleased(MouseEvent p1)
		{
			if(TheLegend!=null)
				TheLegend.setDragging(false);
		}
		public void mouseMoved(MouseEvent p1){}
		public void mouseEntered(MouseEvent p1){}
		public void mouseExited(MouseEvent p1){}
		
	}
	
	
	/** The Histogram should draw itself along with all its contents
	 * @author BLM */
	public void drawHisto(Graphics2D g2,double[][] projMatrix ,double[][] rotMatrix , boolean plotToSVG)
	{
		g2.clearRect(0, 0, ThePanel.getWidth(), ThePanel.getHeight());
		
		int yStart =0;
		borderLenght_x = getWidth()-2*XMARGIN-10;
		yStart = getHeight()-YMARGIN;
		axisLength_Y = yStart-10;
		
		
		g2.setColor(Color.white);
		if (!plotToSVG)
			g2.fillRect(XMARGIN+1, (yStart-axisLength_Y)+1 , borderLenght_x-1, axisLength_Y-1);
		
		updateDimensions();
		
		
		valuesToPlot = bins;
		if (SmoothData.isSelected())
		{
			valuesToPlot = lowPassFilter(valuesToPlot, 3);
			updateBars(valuesToPlot);
		}
		else
			updateBars(valuesToPlot);
		int numPlots = bins.length;
		
		//drawing histogram bars
		//drawing the axis for this histogram
		
		if (TheBars!=null && numPlots>0)
		{
			float numLines = 8;
			g2.setColor(Color.LIGHT_GRAY);
			int longestLabel = 0;
			double range = UpperBounds-LowerBounds;
			for (int j =0; j < numLines+1; j++)
			{
				g2.setColor(Color.black);
				double x = j*XLen/numLines+XStart;
				double[] axis_Start = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{x,YStart,ZStart});
				double[] axis_End = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{x,YStart,ZStart+TheBars[numPlots-1][0].z});
				
				g2.drawLine((int)axis_Start[0], (int)axis_Start[1], (int)axis_End[0], (int)axis_End[1]);
				//Drawing tick lines
				
				double[] y2 = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{x,YStart+20,ZStart});
				double[] textPoint = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{x,YStart+40,ZStart});
				
				g2.drawLine((int)axis_Start[0],  (int)axis_Start[1], (int)y2[0], (int)y2[1]);
				g2.setColor(Color.black);
				g2.setFont(gui.MainGUI.Font_12);
				Font oldFont = g2.getFont();
				if(!plotToSVG)
				{
					Font f = oldFont.deriveFont(AffineTransform.getRotateInstance(Math.PI/2));
					g2.setFont(f);
				}
				String label = ""+ MainGUI.nf.format((float)(j/(numLines)*range+LowerBounds));
				if (label.length()>longestLabel)
					longestLabel=label.length();
				
				g2.drawString(label, (int)textPoint[0], (int)textPoint[1]);
				g2.setFont(oldFont);
				
			}
			double x = XLen/3+XStart;
			double[] titlePoint = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{x,YStart+200,ZStart});
			
			if (TheFeature != null) {
				String xLab = TheFeature.toString();
				if (LogScaleButton.isSelected())
					xLab = "Log(" + xLab + ")";
				g2.drawString(xLab, (int) titlePoint[0], (int) titlePoint[1]);
			}
			
			
			double[] axis_Start = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart,ZStart});
			double[] axis_End = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart,ZStart+TheBars[numPlots-1][0].z});
			
			//Drawing the vertical axiz
//			axis_Start = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart,ZStart+TheBars[numPlots-1][0].z});
//			axis_End = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart-YEnd,ZStart+TheBars[numPlots-1][0].z});
//			g2.drawLine((int)axis_Start[0], (int)axis_Start[1], (int)axis_End[0], (int)axis_End[1]);
		}
		
		
		//Drawing all the plots now
		for (int p = numPlots-1; p >=0; p--)
		{
			int lastY = -1;
			int lastX = -1;
			
			//drawing the axis for this histogram
			g2.setColor(Color.LIGHT_GRAY);
			double[] axis_Start = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart,ZStart+TheBars[p][0].z});
			double[] axis_End = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XEnd,YStart,ZStart+TheBars[p][0].z});
			g2.drawLine((int)axis_Start[0], (int)axis_Start[1], (int)axis_End[0], (int)axis_End[1]);
			
			
			int num = TheBars[p].length;
			ArrayList<Point> arr = new ArrayList<Point>();
			for (int i = 0; i < num; i++)
			{
				//drawing connected lines at the peaks
				int x = XStart+TheBars[p][i].x+TheBars[p][i].width/2;
				int y = YStart-TheBars[p][i].y;
				int z = ZStart+TheBars[p][i].z;
				
				double[] vector = {x,y,z};
				double[] temp = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , vector);
				
				if (i==0)
				{
					double[] temp2 = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , new double[]{XStart,YStart,ZStart+TheBars[p][0].z});
					lastX = (int)(temp2[0]);
					lastY = (int)(temp2[1]);
					arr.add(new Point(lastX, lastY));
				}
				
				
				if (lastY!=-1 && lastX!=-1)
				{
					g2.setStroke(MainGUI.Stroke_2);
					if (plotToSVG)
							((SVG_writer)g2).setStrokeWidth(2);
					
					Point point = new Point((int)temp[0], (int)temp[1]);
					arr.add(point);
					
					g2.setColor(TheBars[p][i].color);
					g2.setStroke(MainGUI.Stroke_1);
					if (plotToSVG)
							((SVG_writer)g2).setStrokeWidth(1);
				}
				lastX = (int)temp[0];
				lastY = (int)temp[1];
				
				if (i==num-1)
				{
					vector[1] = YStart;
					double[] temp2 = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , vector);
					arr.add(new Point((int)temp2[0], (int)temp2[1]));
				}
			}
			
			
			//constructing the polygon for this single histogram
			int len = arr.size();
			int[] xPts = new int[len];
			int[] yPts = new int[len];
			for (int i = 0; i < len; i++)
			{
				Point point = arr.get(i);
				xPts[i] = point.x;
				yPts[i] = point.y;
			}
			if(DrawBoxes.isSelected())
			{
				g2.fillPolygon(xPts, yPts, len);
				g2.setColor(Color.black);
				g2.drawPolygon(xPts, yPts,len);
			}
			else
			{
				g2.setStroke(MainGUI.Stroke_3);
				g2.setColor(tools.ColorRama.getColor(p+1));
				g2.drawPolygon(xPts, yPts,len);
				g2.setStroke(MainGUI.Stroke_1);
			}
			
			
			//drawing border
			if (!plotToSVG)
			{
				for (int i =0; i < 4; i++)
				{
					g2.setColor(colors[i]);
					g2.drawRect(XMARGIN-i, (yStart-axisLength_Y)-i , borderLenght_x+2*i, axisLength_Y+2*i);
				}
				
				//border buffers to make boundaries
				g2.setColor(BufferColor);
				g2.fillRect(0, 0 , XMARGIN-3, ThePanel.getHeight());
				g2.fillRect(XMARGIN+borderLenght_x+3, 0 , XMARGIN+6, ThePanel.getHeight());
				
				g2.fillRect(0, 0 , ThePanel.getWidth(), (yStart-axisLength_Y)-3);
				g2.fillRect(0, yStart+3 , ThePanel.getWidth(), ThePanel.getHeight());
			}
			validate();
		}
	}
	
	public void updateBars(double[][] values)
	{
		int interPlotDist = 150;
		if (values==null)
			return;
		int numPlots = values.length;
		int num = maxBinValues.length;
//		if (num!=numPlots)
//			System.out.println("num: "+num+"  numPlots: "+numPlots);
		
		
		TheBars = new Bar[numPlots][];
		for (int p = 0; p < numPlots; p++)
		{
			int len = values[p].length;
			TheBars[p] = new Bar[len];
			Color color = tools.ColorRama.getColor(p+1);
			for (int i = 0; i < len; i++)
			{
				Bar b = new Bar();
				b.color = color;
				TheBars[p][i] = b;
				
				b.x = (int)((float)i*(float)BarWidth);
				b.y = (int)((float)YEnd*(float)values[p][i]/(float)maxBinValues[p]);
				b.z = p*interPlotDist;
				b.width = BarWidth;
			}
			
		}
		
	}
	
	
	
	public class Bar
	{
		public int x;
		public int y;
		public int z;
		public int width;
		public Color color;
		private double[][] Boundaries;
		
		public Bar()
		{
			x = 0;
			y = 0;
			z = 0;
			width = 0;
			Boundaries = new double[4][3];
		}
		
		public void draw(Graphics2D g2,double[][] projMatrix ,double[][] rotMatrix )
		{
			if (DrawBoxes.isSelected())
				if (y>0)
				{
					Polygon p = getPolygon(projMatrix, rotMatrix);
					
					g2.setColor(color);
					Composite orig = g2.getComposite();
					g2.setComposite(barComposite);
					g2.fillPolygon(p);
					g2.drawPolygon(p);
					g2.setComposite(orig);
					
				}
		}
		
		public Polygon getPolygon(double[][] projMatrix ,double[][] rotMatrix)
		{
			int[] xpoints = new int[4];
			int[] ypoints = new int[4];
			
			Boundaries[0][0] = XStart+x;
			Boundaries[0][1] = YStart-y;
			Boundaries[0][2] = ZStart+z;
			
			Boundaries[1][0] = XStart+x+width;
			Boundaries[1][1] = YStart-y;
			Boundaries[1][2] = ZStart+z;
			
			Boundaries[2][0] = XStart+x+width;
			Boundaries[2][1] = YStart;
			Boundaries[2][2] = ZStart+z;
			
			Boundaries[3][0] = XStart+x;
			Boundaries[3][1] = YStart;
			Boundaries[3][2] = ZStart+z;
			
			
			for (int i = 0; i < 4; i++)
			{
				//transforming all the boundary points of this tile
				double[] temp = tools.MathOps.transformPoint3D(rotMatrix ,projMatrix , Boundaries[i]);
				xpoints[i] = (int)(temp[0]);
				ypoints[i] = (int)(temp[1]);
			}
			
			return new Polygon(xpoints, ypoints, 4);
		}
		/** Free up RAM
		 * @author BLM*/
		public void kill()
		{
			color = null;
			Boundaries = null;
		}
	}
	
	
	
	/** Since we have to draw a 3 dimensional object on a 2 dim screen we need a projection
	 * matrix and a rotation matrix*/
	public void initViewingBasis()
	{
		//setting up the ViewingBasisFrame
		ProjectionMatrix = new double[3][4];
		RotationMatrix = new double[3][3];
		for (int i =0; i < 3; i ++)
			for (int j = 0; j < 4; j++)
				ProjectionMatrix[i][j] = 0;
		ProjectionMatrix[2][3] = 1;
		
		//setting scale
		for (int j = 0; j < 3; j++)
			ProjectionMatrix[j][j] = 0.5;
		
		ProjectionMatrix[1][3] = 160;
		ProjectionMatrix[0][3] = 50;
		
		//setting up the rotation frame
		double[] rotAxis = {0,1};
		RotationMatrix  = calculateRotationMatrix(rotAxis, 0);
	}
	
	
	/** Recalculates the rotation matrix*/
	public double[][] calculateRotationMatrix(double[] vector, double angle)
	{
		double x = vector[0];
		double y = vector[1];
		double z = 0;
		double[][] rotMatrix = new double[3][3];
		rotMatrix[0][0] = Math.cos(angle)+(1-Math.cos(angle))*Math.pow(x,2);
		rotMatrix[0][1] = (1-Math.cos(angle))*x*y-Math.sin(angle)*z;
		rotMatrix[0][2] = (1-Math.cos(angle))*x*z+Math.sin(angle)*y;
		
		rotMatrix[1][0] = (1-Math.cos(angle))*y*x+	Math.sin(angle)*z;
		rotMatrix[1][1] = Math.cos(angle)+ (1-Math.cos(angle))*Math.pow(y,2);
		rotMatrix[1][2] = (1-Math.cos(angle))*y*z-Math.sin(angle)*x;
		
		rotMatrix[2][0] = (1-Math.cos(angle))*z*x-	Math.sin(angle)*y;
		rotMatrix[2][1] = (1-Math.cos(angle))*z*y+ Math.sin(angle)*x;
		rotMatrix[2][2] = Math.cos(angle)+(1-Math.cos(angle))*Math.pow(z,2);
		return rotMatrix;
	}
	
	
	public void kill()
	{
		TheData = null;
		if(TheBars!=null)
		{
			int num = TheBars.length;
			for (int i = 0; i < num; i++)
			{
				int len = TheBars[i].length;
				for (int j = 0; j < len; j++)
					TheBars[i][j].kill();
			}
		}
		bins = null;
		TheHistogram = null;
		maxBinValues = null;
		barComposite = null;
		colors = null;
		BufferColor = null;
		DrawBoxes = null;
		DrawLines = null;
		SmoothData = null;
		ResetViewButton = null;
		LogScaleButton = null;
		FitGaussians = null;
		ThePanel = null;
		valuesToPlot = null;
		ProjectionMatrix = null;
		RotationMatrix = null;
		lastPoint = null;
		TheFeature = null;
		TheWells = null;;
		Legend_xy = null;
		TheLegend.kill();
	}
}


