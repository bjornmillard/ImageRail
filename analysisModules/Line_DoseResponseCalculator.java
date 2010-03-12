/**
 * Line_DoseResponseCalculator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package analysisModules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import main.MainGUI;
import plots.Bound;
import plots.LinePlot;
import tools.SVG_writer;

public class Line_DoseResponseCalculator extends AnalysisModule_LineGraph
{
	private float lo;
	private float mid;
	private float hi;
	public JCheckBox checkBox = new JCheckBox("Plot Graph");
	
	public Line_DoseResponseCalculator(LinePlot parentLinePlot, String title, int width, int height)
	{
		
		super(parentLinePlot, title, width, height);
		
		if (parentLinePlot==null)
			return;
		
		
		lo = 0.3f;
		mid = 0.5f;
		hi = 0.7f;
		
		checkBox.setSelected(true);
		checkBox.addActionListener(new ActionListener()
								   {
					public void actionPerformed(ActionEvent ae)
					{
						repaint();
					}
				});
		
		final JTextField f1 = new JTextField(""+lo);
		//When text field changes, it automatically modifies the bounds
		f1.getDocument().addDocumentListener(new DocumentListener()
											 {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f1.getText();
						if(tools.MathOps.isNumber(string))
						{
							lo = Float.parseFloat(string);
							repaint();
						}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f1.getText();
						if(tools.MathOps.isNumber(string))
						{
							lo = Float.parseFloat(string);
							repaint();
						}
						
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f1.getText();
						if(tools.MathOps.isNumber(string))
						{
							lo = Float.parseFloat(string);
							repaint();
						}
					}
				});
		
		
		final JTextField f2 = new JTextField(""+mid);
		f2.getDocument().addDocumentListener(new DocumentListener()
											 {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f2.getText();
						
						if(tools.MathOps.isNumber(string))
						{
							mid = Float.parseFloat(string);
							repaint();
						}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f2.getText();
						
						if(tools.MathOps.isNumber(string))
						{
							mid = Float.parseFloat(string);
							repaint();
						}
						
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f2.getText();
						
						if(tools.MathOps.isNumber(string))
						{
							mid = Float.parseFloat(string);
							repaint();
						}
					}
				});
		final JTextField f3 = new JTextField(""+hi);
		f3.getDocument().addDocumentListener(new DocumentListener()
											 {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f3.getText();
						if(tools.MathOps.isNumber(string))
						{
							hi = Float.parseFloat(string);
							repaint();
						}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f3.getText();
						if(tools.MathOps.isNumber(string))
						{
							hi = Float.parseFloat(string);
							repaint();
						}
						
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = f3.getText();
						if(tools.MathOps.isNumber(string))
						{
							hi = Float.parseFloat(string);
							repaint();
						}
					}
				});
		
		TheToolBar.add(f1);
		TheToolBar.add(f2);
		TheToolBar.add(f3);
		TheToolBar.add(checkBox);
		
		repaint();
	}
	
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		if (!plotToSVG)
		{
			int MARGIN = 10;
			int yRange = getHeight()-8*MARGIN;
			int xRange = getWidth()-2*MARGIN;
			int ys = getHeight()-4*MARGIN;
			
			// Drawing border
			for (int i = 0; i < 4; i++)
			{
				g2.setColor(TheParentLinePlot.getSeriesColors()[i]);
				g2.drawRect(MARGIN - i, (ys - yRange) - i,
							xRange + 2 * i, yRange + 2 * i);
			}
			g2.setColor(Color.white);
			g2.fillRect(MARGIN + 1, (ys - yRange) + 1, xRange - 1, yRange - 1);
		}
		
		//Drawing the Title String on top
		if (Title!=null)
		{
			g2.setColor(Color.black);
			int len = Title.length();
			g2.setFont(MainGUI.Font_18);
			g2.drawString(Title, getWidth()/2-(len*12f/2f),MainGUI.Font_18.getSize()+50);
			g2.setFont(MainGUI.Font_8);
		}
		
		if (checkBox.isSelected())
			for (int s = 0; s < TheParentLinePlot.getNumSeries(); s++)
				drawSeries(TheParentLinePlot, TheParentLinePlot.getTheSeries()[s], g2, plotToSVG);
		
		// Drawing the data
		for (int s = 0; s < TheParentLinePlot.getNumSeries(); s++)
			drawDoseResponseRanges(TheParentLinePlot.getTheSeries()[s], g2, plotToSVG);
		
		
		//Drawing the x-axis
		
	}
	
	public void drawSeries(LinePlot plot, LinePlot.Series series, Graphics2D g2, boolean plotToSVG)
	{
		int maxNumPoints = plot.getLargestSeriesLength();
		int numP = series.TheDataPoints.length;
		
		int xStep = series.xStep;
		int xRange = plot.getXRange();
		int yRange = getHeight()-140;
		int xStart = plot.getXstart();
		int yStart = getHeight()-70;//Ystart;
		
		xStep = xRange / (maxNumPoints + 1);
		//Drawing the Series
		for (int c = 1; c < numP; c++)
		{
			Bound Bounds = plot.getBound();
			
			LinePlot.Series.DataPoint lastP = series.TheDataPoints[c-1];
			LinePlot.Series.DataPoint thisP = series.TheDataPoints[c];
			// Drawing the Stdev bars
			int x1 = (c - 1) * xStep + plot.getXstart();
			int x2 = c * xStep + xStart;
			float lastVal = (float) ((lastP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
			float thisVal = (float) ((thisP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
			int y1 = yStart - (int) (lastVal * yRange);
			int y2 = yStart - (int) (thisVal * yRange);
			
			
			// Drawing the Data Points
			g2.setColor(series.color);
			
			g2.setStroke(MainGUI.Stroke_3);
			if (plotToSVG)
					((SVG_writer)g2).setStrokeWidth(3);
			g2.fillOval(x1-2, y1-4, 8, 8);
			if(c==numP-1)
				g2.fillOval(x2-2, y2-4, 8, 8);
			g2.drawLine(x1, y1, x2, y2);
			if (plotToSVG)
					((SVG_writer)g2).setStrokeWidth(1);
			g2.setStroke(MainGUI.Stroke_1);
			
		}
	}
	
	
	public void drawDoseResponseRanges(LinePlot.Series series, Graphics2D g2, boolean plotToSVG)
	{
		int ID_SERIES = series.ID_SERIES;
		Color color = series.color;
		
		int numP = series.TheDataPoints.length;
		int markerHeight = 10;
		
		//Using the Piecewise linear line plot for the data
		float minVal_thisNorm = (float) ((series.Min_real - TheParentLinePlot.getMinValue()) / (TheParentLinePlot.getMaxValue()-TheParentLinePlot.getMinValue()));
		float maxVal_thisNorm = (float) (((series.Max_real - TheParentLinePlot.getMinValue()) / (TheParentLinePlot.getMaxValue() - TheParentLinePlot.getMinValue())));
		float maxVal_scale = (float)((maxVal_thisNorm-minVal_thisNorm));
		int num = (numP - 1) * 10;
		
		// Drawing the EC50/IC90 value
		int[] arr = new int[3];
		float x10 = series.getXValofGivenYfraction(lo);
		float xPlot = (int) ((x10) / (double) (numP + 1) * TheParentLinePlot.getXRange() + TheParentLinePlot.getXstart());
		g2.setColor(Color.black);
		arr[0] = (int) xPlot;
		// IC50 value
		float x50 = series.getXValofGivenYfraction(mid);
		xPlot = (int) ((x50) / (double) (numP + 1) * TheParentLinePlot.getXRange() + TheParentLinePlot.getXstart());
		
		g2.setColor(Color.black);
		arr[1] = (int) xPlot;
		// IC10 value
		float x90 = series.getXValofGivenYfraction(hi);
		xPlot = (int) ((x90) / (double) (numP + 1) * TheParentLinePlot.getXRange() + TheParentLinePlot.getXstart());
		g2.setColor(Color.black);
		arr[2] = (int) xPlot;
		
		
		// Plotting the ten-fifty-ninty whisker plot at bottom
		g2.setColor(Color.black);
		int height = markerHeight-4;
		int yoffset =15;
		int yInterSeriesOffset = markerHeight*2+5;
		if (arr[0] < arr[2])
			g2.fillRect(arr[0], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset +height/2 ), Math.abs(arr[2]	- arr[0]), height);
		else
			g2.fillRect(arr[2], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset  +height/2 ), Math.abs(arr[2]- arr[0]), height);
		
		g2.setColor(color);
		g2.fillRect(arr[0], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset ), markerHeight, markerHeight);
		g2.fillRect(arr[1], (Ystart + ID_SERIES * yInterSeriesOffset + yoffset), markerHeight, markerHeight);
		g2.fillRect(arr[2], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset), markerHeight, markerHeight);
		g2.setColor(Color.black);
		g2.drawRect(arr[0], (Ystart + ID_SERIES * yInterSeriesOffset + yoffset), markerHeight, markerHeight);
		g2.drawRect(arr[1], (Ystart +ID_SERIES * yInterSeriesOffset + yoffset), markerHeight, markerHeight);
		g2.drawRect(arr[2], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset), markerHeight, markerHeight);
		//Writing the values
		g2.drawString(""+MainGUI.nf.format(x10), arr[0], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset-2));
		g2.drawString(""+MainGUI.nf.format(x50), arr[1], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset-2));
		g2.drawString(""+MainGUI.nf.format(x90), arr[2], (Ystart + ID_SERIES * yInterSeriesOffset +  yoffset-2));
	}
	
	public void exportData(PrintWriter pw)
	{
		pw.println();
		// Drawing the data
		for (int s = 0; s < TheParentLinePlot.getNumSeries(); s++)
		{
			LinePlot.Series series = TheParentLinePlot.getTheSeries()[s];
			// Drawing the EC50/IC90 value
			float x10 = series.getXValofGivenYfraction(lo);
			// IC50 value
			float x50 = series.getXValofGivenYfraction(mid);
			// IC10 value
			float x90 = series.getXValofGivenYfraction(hi);
			
			pw.println(x10+","+x50+","+x90);
			
			
		}
		pw.flush();
		pw.close();
	}
}

