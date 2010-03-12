/**
 * Grid_LinePlot.java
 *
 * @author Created by Omnicore CodeGuide
 */

package analysisModules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import main.MainGUI;
import main.Plate;
import main.PlateHoldingPanel;
import main.Well;
import plots.LinePlot;
import features.Feature;

public class Grid_LinePlot extends AnalysisModule_Plate implements KeyListener
{
	Feature[] TheFeatures;
	int NumFeatures;
	int[] xStarts;
	int[] yStarts;
	int plotWidth;
	int plotHeight;
	LinePlot[] TheLinePlots;
	Well[][] wells;
	Color[] colors;
	float[][][] data;
	float[][][] stdev;
	boolean[] selected;
	Rectangle rect = new Rectangle();
	
	public Grid_LinePlot(Plate plate, Feature[] features,  String title, int width, int height)
	{
		super(plate, title, width, height);
		addKeyListener(this);
		this.setFocusable(true);
		requestFocus();
		
		setFeatures(features);
	}
	
	public void updateGraphs()
	{
		NumFeatures = TheFeatures.length;
		TheLinePlots = new LinePlot[NumFeatures];
		xStarts = new int[NumFeatures];
		yStarts = new int[NumFeatures];
		selected = new boolean[NumFeatures];
		for (int i = 0; i < NumFeatures; i++)
			selected[i] = false;
		
		
		wells = null;
		PlateHoldingPanel ThePlatePanel = MainGUI.getGUI().getPlateHoldingPanel();
		
		int PlotType = LinePlot.ROWS;
		if (MainGUI.getGUI().getLinePlot()!=null)
			PlotType = MainGUI.getGUI().getLinePlot().getPlotType();
		
		if (PlotType == LinePlot.ROWS)
			wells = ThePlatePanel.getAllSelectedWells_RowSeries();
		else if (PlotType == LinePlot.COLS)
			wells = ThePlatePanel.getAllSelectedWells_ColumnSeries();
		else if (PlotType == LinePlot.MULTIPLATE)
			wells = ThePlatePanel.getAllSelectedWells_TransPlateSeries();;
		
		if (wells==null)
			return;
		
		//Computing the values
		int numSeries = wells.length;
		data =  new float[NumFeatures][numSeries][];
		float[][][][] varianceBars = new float[NumFeatures][numSeries][][];
		stdev = new float[NumFeatures][numSeries][];
		
		
		for (int f = 0; f < NumFeatures; f++)
		{
			int index = TheFeatures[f].getGUIindex();
			
			for (int r =0; r < numSeries; r++)
			{
				Well[] oneRowSeries = wells[r];
				int numC = oneRowSeries.length;
				data[f][r] = new float[numC];
				varianceBars[f][r] = new float[numC][2];
				stdev[f][r] = new float[numC];
				for (int c =0; c < numC; c++)
				{
					if (oneRowSeries[c].Feature_Means!=null)
						data[f][r][c] = oneRowSeries[c].Feature_Means[index];
					
					if (oneRowSeries[c].getCells() != null
							&& oneRowSeries[c].Feature_Stdev != null)
					{
						float[] minMax = oneRowSeries[c].getMinMaxValue(TheFeatures[f]);
						varianceBars[f][r][c][0] = minMax[0];
						varianceBars[f][r][c][1] = minMax[1];
						
						stdev[f][r][c] = oneRowSeries[c].Feature_Stdev[index];
					}
					else
						varianceBars[f][r][c] = null;
				}
			}
			//Setting up the colors
			colors = new Color[data.length];
			if (PlotType == LinePlot.ROWS)
				for (int r =0; r < numSeries; r++)
					colors[r] = Plate.getRowColor(wells[r][0].name);
			else if (PlotType == LinePlot.COLS)
				for (int r =0; r < numSeries; r++)
					colors[r] = Plate.getColColor(wells[r][0].name);
			else if (PlotType == LinePlot.MULTIPLATE)
				for (int i =0; i < numSeries; i++)
					colors[i] = Color.BLACK;
			
		}
		
	}
	
	public boolean isResizable()
	{
		return true;
	}
	
	public JPanel getPanel()
	{
		return ThePanel;
	}
	
	/** Dont actually want to draw a plate here.. want to  draw a bunch of line plots in a grid
	 * @author BLM*/
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		if (NumFeatures==0)
			return;
		
		//Computing all the xStarts/yStarts, width/heights
		int pWidth = getWidth();
		int pHeight = getHeight();
		
		int nCols =  (int)Math.ceil(Math.sqrt(NumFeatures));
		int nRows = nCols;
		
		int bufferX = 50;
		int bufferY = 100;
		
		plotWidth = (pWidth-bufferX)/nCols-10;
		plotHeight = (pHeight-bufferY)/nRows-10;
		
		int counter = 0;
		for (int r = 0; r < nRows; r++)
		{
			for (int c = 0; c < nRows; c++)
			{
				xStarts[counter] = (int)(bufferX/2f)+(c)*(plotWidth+10);
				yStarts[counter] = (int)(bufferY/2f)+(r+1)*(plotHeight+10);
				counter++;
				if (counter>=(NumFeatures))
					break;
			}
			if (counter>=(NumFeatures))
				break;
		}
		
		boolean smooth = false;
		if (MainGUI.getGUI().getLinePlot()!=null)
			smooth = MainGUI.getGUI().getLinePlot().shouldSmoothData();
		
		//Now Drawing
		for (int i = 0; i < NumFeatures; i++)
		{
			TheLinePlots[i] = new LinePlot();
			
			TheLinePlots[i].setSmoothData(smooth);
			TheLinePlots[i].updatePlot(data[i], stdev[i], colors, wells);
			TheLinePlots[i].setWidthHeight(plotWidth, plotHeight);
			TheLinePlots[i].setXYstart(xStarts[i], yStarts[i]);
			
			TheLinePlots[i].draw(g2, plotToSVG);
			
			g2.setColor(Color.black);
			g2.setFont(MainGUI.getGUI().Font_8);
			g2.drawString(""+TheFeatures[i], xStarts[i]+2, yStarts[i]+9-plotHeight);
			
			//If selected, draw a red bounding box around the plot
			if (selected[i])
			{
				g2.setStroke(MainGUI.getGUI().Stroke_2);
				g2.setColor(Color.RED);
				g2.drawRect(xStarts[i], yStarts[i]-plotHeight, plotWidth, plotHeight);
				g2.setStroke(MainGUI.Stroke_1);
			}
		}
	}
	

	
	/** Handles mouse clicks on the plate
	 * @author BLM*/
	public void mouseClicked(MouseEvent p1)
	{
		
		int pIndex = getPlotIndex(p1.getPoint());
		
		if (pIndex != -1)
		{
			//Remove feature of index==pIndex and replot
			selected[pIndex] = !selected[pIndex];
		}
		repaint();
	}
	
	public int getPlotIndex(Point p)
	{
		for (int i = 0; i < NumFeatures; i++)
		{
			rect.x = xStarts[i];
			rect.y = yStarts[i]-plotHeight;
			rect.width = plotWidth;
			rect.height = plotHeight;
			if (rect.contains(p))
				return i;
		}
		return -1;
	}
	
	
	public void keyTyped(KeyEvent e)
	{
	}
	public void keyReleased(KeyEvent e)
	{
	}
	
	
	public void keyPressed(KeyEvent e)
	{
		int keyVal = e.getKeyCode ();
		if (keyVal == KeyEvent.VK_DELETE||keyVal==KeyEvent.VK_BACK_SPACE)
		{
			int counter = 0;
			for (int i = 0; i < NumFeatures; i++)
				if(!selected[i])
					counter++;
			
			Feature[] temp = new Feature[counter];
			counter=0;
			for (int i = 0; i < NumFeatures; i++)
				if(!selected[i])
				{
					temp[counter] = TheFeatures[i];
					counter++;
				}
			
			setFeatures(temp);
			repaint();
			
		}
		
	}
	
	public void setFeatures(Feature[] features)
	{
		TheFeatures = features;
		updateGraphs();
		
	}
	
}

