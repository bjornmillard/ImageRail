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

/** Data structure that holds the line series data
 * @author BLM*/
import java.awt.Color;
import java.awt.Rectangle;

import models.Model_Well;

public class Series
{
	public DataPoint[] TheDataPoints;
	public float Max_real;
	public float Min_real;
	public float Max_plot;
	public float Min_plot;
	
	public Color color;
	public int xStep;
	
	public Series(float[] vals_, Model_Well[] well, Color color_, boolean SmoothData)
	{
		color = color_;

		
		// Should we smooth the data first?
		int numPoints = vals_.length;
		float[] vals_plot = new float[numPoints];
		if (SmoothData)
			vals_plot = tools.MathOps.smoothData(vals_);//tools.MathOps.smoothData_forceMonotonic(vals_orig);
		else
			vals_plot = vals_;
		
		//Constructing the DataPoints
		TheDataPoints = new DataPoint[numPoints];
		for (int i = 0; i < numPoints; i++)
		{
			try
			{
				TheDataPoints[i] = new DataPoint(i, vals_[i], vals_plot[i], well[i]);
			}
			catch (Exception e)
			{
				System.out.println("Error constructing Series!!!: ");e.printStackTrace();
			}
			
		}
		
		initMinMax();
	}
	
//	public float getValue_normalized(int index)
//	{
//		return (float)((TheDataPoints[index].y_real-Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
//	}
//	public float getValue_real(int index)
//	{
//		return TheDataPoints[index].y_real;
//	}
//	public float getValue_plot(int index)
//	{
//		return TheDataPoints[index].y_plot;
//	}
	
	
//	public void draw(Graphics2D g2, boolean plotToSVG)
//	{
//		int maxNumPoints = TheSer();
//		int numP = TheDataPoints.length;
//		xStep = xRange / (maxNumPoints + 1);
//		//Drawing the Series
//		for (int c = 1; c < numP; c++)
//		{
//			DataPoint lastP = TheDataPoints[c-1];
//			DataPoint thisP = TheDataPoints[c];
//			// Drawing the Stdev bars
//			int x1 = (c - 1) * xStep + Xstart;
//			int x2 = c * xStep + Xstart;
//			float lastVal = (float) ((lastP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
//			float thisVal = (float) ((thisP.y_plot - Bounds.Lower) / (Bounds.Upper - Bounds.Lower));
//			int y1 = Ystart - (int) (lastVal * yRange);
//			int y2 = Ystart - (int) (thisVal * yRange);
//
//
//
//			// Drawing the Data Points
//			g2.setColor(color);
//			if (!SigmoidFit)	//only plot the raw line if we dont plot the sigmoid fit -- gets too busy otherwise
//			{
//				g2.setStroke(MainGUI.Stroke_3);
//				if (plotToSVG)
//						((SVG_writer)g2).setStrokeWidth(3);
//				g2.drawLine(x1, y1, x2, y2);
//				if (plotToSVG)
//						((SVG_writer)g2).setStrokeWidth(1);
//				g2.setStroke(MainGUI.Stroke_1);
//			}
//
//
//		}
//
//	}
	
	
	
	
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
	
	

	/** DataPoint structure that comprises the Data Series structure
	 * @author BLM*/
	public class DataPoint
	{
		public float x_plot;
		public float y_plot;
		public float x_real;
		public float y_real;
		public Model_Well TheWell;
		private Rectangle box;
		private Rectangle closeBox;
		private Rectangle bounds_histo;

		public DataPoint(float x, float y, float y_plot_, Model_Well well_)
		{
			TheWell = well_;
			x_real = x;
			y_real = y;
			x_plot = x;
			y_plot = y_plot_;
			box = new Rectangle();
			closeBox = new Rectangle();
			bounds_histo = null;
		}
		public float getY_plot_self()
		{
			return (float)((y_plot-Min_plot)/(Max_plot-Min_plot));
		}
		
		
//		public float getY_plot()
//		{
//			return (float)((y_plot-Bounds.Lower)/(Bounds.Upper-Bounds.Lower));
//		}
//		public float getY_real()
//		{
//			return (float)((y_real-Bounds.Lower)/(Bounds.Upper-Bounds.Lower));
//		}
//		public float getStdev()
//		{
//			return stdev;
//		}
		
	
	
	}
	
	
	
	
}

