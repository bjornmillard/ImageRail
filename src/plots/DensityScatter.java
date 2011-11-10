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

import java.awt.Color;
import java.util.Hashtable;

public class DensityScatter
{
	private int SmoothSize = 20;
	private int numBins = 50;
	private float[][] bins;
	private float minValX;
	private float maxValX;
	private float minValY;
	private float maxValY;
	private float maxBinValue;
	private float[] color;
	private Hashtable TheHashTable;
	
	public DensityScatter(float[] xVals, float[] yVals)
	{
//		System.out.println("Makinng new density map");
		int lenX = xVals.length;
		int lenY = yVals.length;
		if (lenX!=lenY)
			return;
		color = new float[4];
		bins = new float[numBins][numBins];
		minValX = Float.POSITIVE_INFINITY;
		maxValX = Float.NEGATIVE_INFINITY;
		minValY = Float.POSITIVE_INFINITY;
		maxValY = Float.NEGATIVE_INFINITY;
		maxBinValue = 0;
		TheHashTable = new Hashtable();
		
		//Finding Min/Max Vals
		for (int i = 0; i < lenX; i++)
		{
			//X
			if (xVals[i]>Float.NEGATIVE_INFINITY && xVals[i]<Float.POSITIVE_INFINITY &&
				yVals[i]>Float.NEGATIVE_INFINITY && yVals[i]<Float.POSITIVE_INFINITY)
			{
				if (xVals[i]<minValX)
					minValX = xVals[i];
				else if (xVals[i]>maxValX)
					maxValX = xVals[i];
				//Y
				if (yVals[i]<minValY)
					minValY = yVals[i];
				else if (yVals[i]>maxValY)
					maxValY = yVals[i];
			}
		}
		
		//Binning
		for (int i = 0; i < lenX; i++)
		{
			int indX = (int)(numBins*((xVals[i]-minValX)/(maxValX-minValX)));
			int indY = (int)(numBins*((yVals[i]-minValY)/(maxValY-minValY)));
			if (indX>=0 && indY>=0)
			{
				if (indX >= numBins)
					indX = numBins-1;
				if (indY >= numBins)
					indY = numBins-1;
				bins[indX][indY] += 1f;
			}
		}
		
		
		//Smoothing the density plot with a 2D Smoothing Filter
		tools.ImageTools.linearFilter(bins, tools.LinearKernels.getLinearSmoothingKernal(SmoothSize));
		
		
		for (int x = 0; x < numBins; x++)
			for (int y = 0; y < numBins; y++)
			{
				if (bins[x][y]>maxBinValue)
					maxBinValue = bins[x][y];
			}
		
		//Normalizing the bins
		for (int x = 0; x < numBins; x++)
			for (int y = 0; y < numBins; y++)
			{
				if (bins[x][y]!=0)
					bins[x][y] = bins[x][y]/maxBinValue;
			}
	}
	
	/** Allows user to set number of bins for the density calculations
	 * @author BLM*/
	public DensityScatter(float[] xVals, float[] yVals, int numBins_)
	{
		numBins = numBins_;
//		System.out.println("Makinng new density map");
		int lenX = xVals.length;
		int lenY = yVals.length;
		if (lenX!=lenY)
			return;
		color = new float[4];
		bins = new float[numBins][numBins];
		minValX = Float.POSITIVE_INFINITY;
		maxValX = Float.NEGATIVE_INFINITY;
		minValY = Float.POSITIVE_INFINITY;
		maxValY = Float.NEGATIVE_INFINITY;
		maxBinValue = 0;
		TheHashTable = new Hashtable();
		
		//Finding Min/Max Vals
		for (int i = 0; i < lenX; i++)
		{
			//X
			if (xVals[i]>Float.NEGATIVE_INFINITY && xVals[i]<Float.POSITIVE_INFINITY &&
				yVals[i]>Float.NEGATIVE_INFINITY && yVals[i]<Float.POSITIVE_INFINITY)
			{
				if (xVals[i]<minValX)
					minValX = xVals[i];
				else if (xVals[i]>maxValX)
					maxValX = xVals[i];
				//Y
				if (yVals[i]<minValY)
					minValY = yVals[i];
				else if (yVals[i]>maxValY)
					maxValY = yVals[i];
			}
		}
		
		//Binning
		for (int i = 0; i < lenX; i++)
		{
			int indX = (int)(numBins*((xVals[i]-minValX)/(maxValX-minValX)));
			int indY = (int)(numBins*((yVals[i]-minValY)/(maxValY-minValY)));
			if (indX>=0 && indY>=0)
			{
				if (indX >= numBins)
					indX = numBins-1;
				if (indY >= numBins)
					indY = numBins-1;
				bins[indX][indY] += 1f;
			}
		}
		
		
		//Smoothing the density plot with a 2D Smoothing Filter
		tools.ImageTools.linearFilter(bins, tools.LinearKernels.getLinearSmoothingKernal(SmoothSize));
		
		
		for (int x = 0; x < numBins; x++)
			for (int y = 0; y < numBins; y++)
			{
				if (bins[x][y]>maxBinValue)
					maxBinValue = bins[x][y];
			}
		
		//Normalizing the bins
		for (int x = 0; x < numBins; x++)
			for (int y = 0; y < numBins; y++)
			{
				if (bins[x][y]!=0)
					bins[x][y] = bins[x][y]/maxBinValue;
			}
	}
	
	public boolean shouldPlot(float valX, float valY)
	{
		int X = getIndX(valX);
		int Y = getIndY(valY);
		if (TheHashTable.get(X+","+Y)!=null)
			return false;
		return true;
	}
	
	public void dontPlot(float valX, float valY)
	{
		int X = getIndX(valX);
		int Y = getIndY(valY);
		TheHashTable.put((X+","+Y), "");
	}
	
	public int getNumInBin(float valX, float valY)
	{
		int indX = getIndX(valX);
		int indY = getIndY(valY);
		if (indX<0 ||  indY<0  || indX>=numBins || indY>=numBins)
			return 0;
		return (int)(bins[indX][indY]*maxBinValue);
	}
	
	public float getDensityValue(float valX, float valY)
	{
		int indX = getIndX(valX);
		int indY = getIndY(valY);
		if (indX<0 ||  indY<0  || indX>=numBins || indY>=numBins)
			return 0;
		return bins[indX][indY];
	}
	
	public float[][] getAllDensityValues()
	{
		float[][] vals = new float[bins[0].length][bins.length];
		int rows = vals.length;
		int cols = vals[0].length;

		for (int r = 0; r < rows; r++)
			for (int c = 0; c<cols; c++)
				vals[r][c] = bins[c][r];


		return vals;
	}
	
	private int getIndX(float valX)
	{
		return (int)(numBins*((valX-minValX)/(maxValX-minValX)));
	}
	
	private int getIndY(float valY)
	{
		return (int)(numBins*((valY-minValY)/(maxValY-minValY)));
	}
	
	public float[][] getBins()
	{
		return bins;
	}
	
	
	
	public Color getColor(float x, float y)
	{
		tools.ColorMaps.getColorValue(getDensityValue(x,y), 0, 1, color, models.Model_Main.getModel().getTheColorMapIndex());
		for (int i = 0; i < 3; i++)
		{
			if (color[i]<0)
				color[i] = 0;
			if (color[i]>1)
				color[i] = 1;
		}
		return new Color(color[0], color[1], color[2], color[3]);
	}
}

