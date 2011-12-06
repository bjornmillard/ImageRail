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
import java.awt.Polygon;
import java.util.ArrayList;

import segmentedobject.Cell;
import features.Feature;

public class Gate_DotPlot
{
	public int ID;
	public String Name;
	public double[][] xyPoints;
	public boolean selected;
	public Feature featureX;
	public Feature featureY;
	private int featureX_index;
	private int featureY_index;
	
	public Gate_DotPlot(double[][] xyPoints_, Feature featureX_, Feature featureY_,int id_, String name_)
	{
		ID = id_;
		Name = name_;
		featureX = featureX_;
		featureY = featureY_;
		
		featureX_index = 0;
		featureY_index = 0;
		Feature[] features = models.Model_Main.getModel().getFeatures();
		for (int i = 0; i < features.length; i++)
		{
			if(features[i].toString().equalsIgnoreCase(featureX.toString()))
				featureX_index = i;
			if(features[i].toString().equalsIgnoreCase(featureY.toString()))
				featureY_index = i;
		}
		
		xyPoints = removePointsThatWereDuplicated(xyPoints_);
		
		selected = false;
	}
	
	public void setName(String name_)
	{
		Name = name_;
	}
	
	public String getName()
	{
		return Name;
	}
	
	public Color getColor()
	{
		if (selected)
			return Color.magenta;
		return Color.green;
	}
	
	public Polygon getPolygonGate_toDraw(double xStart, double yStart, double LowerBounds_X, double UpperBounds_X, double LowerBounds_Y, double UpperBounds_Y, double axisLenX, double axisLength_Y, boolean logScaleX, boolean logScaleY)
	{
		int len = xyPoints[0].length;
		Polygon poly = new Polygon();
		
		
		//For each point, scale it appropriately
		for (int i = 0; i < len; i++)
		{
			double x = xyPoints[0][i];
			double y = xyPoints[1][i];
			
			if (!logScaleX)
				x = xStart + (x-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX;
			else
				x = xStart + (tools.MathOps.log(x)-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX;
			if (!logScaleY)
				y = yStart - (y-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y;
			else
				y = yStart - (tools.MathOps.log(y)-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y;
			
			
			poly.addPoint((int)x,(int)y);
			
		}
		
		
		
		return poly;
	}
	
	public float getFractionOfCellsBound(float[][] cellValues)
	{
		int multiplier = 10000;
		int numCells = cellValues.length;
		if (numCells==0)
			return 0;
		
		int count = 0;
		double valX = 0;
		double valY = 0;
		Polygon poly = new Polygon();
		//Creating a polygon... scaled a bit due to possible rounding error for requ that poly's are integer based
		int num = xyPoints[0].length;
		for (int i = 0; i < num; i++)
			poly.addPoint((int)(multiplier*xyPoints[0][i]), (int)(multiplier*xyPoints[1][i]));
		poly.npoints = num;
		for (int i = 0; i < numCells; i++)
		{
			valX = (int)(multiplier*cellValues[i][featureX_index]);
			valY = (int)(multiplier*cellValues[i][featureY_index]);
			if (poly.contains(valX, valY))
				count++;
		}
		return (float)count/(float)numCells;
	}
	
	public boolean isBound(Cell cell) {
		if (cell == null)
			return false;
		float[] cellValues = cell.getFeatureValues();
		int multiplier = 10000;
		double valX = 0;
		double valY = 0;
		Polygon poly = new Polygon();
		// Creating a polygon... scaled a bit due to possible rounding error for
		// requ that poly's are integer based
		int num = xyPoints[0].length;
		for (int i = 0; i < num; i++)
			poly.addPoint((int) (multiplier * xyPoints[0][i]),
					(int) (multiplier * xyPoints[1][i]));
		poly.npoints = num;

		valX = (int) (multiplier * cellValues[featureX_index]);
		valY = (int) (multiplier * cellValues[featureY_index]);
		if (poly.contains(valX, valY))
			return true;

		return false;
	}
	
	public ArrayList<Cell> getCellsBound(ArrayList<Cell> cells)
	{
		if(cells==null)
			return null;
		int numC = cells.size();
		float[][] cellValues = new float[numC][];
		for (int i = 0; i < numC; i++) {
			cellValues[i] = cells.get(i).getFeatureValues();
		}
		int multiplier = 10000;
		int numCells = cellValues.length;
		if (numCells==0)
			return null;
		
		int count = 0;
		double valX = 0;
		double valY = 0;
		Polygon poly = new Polygon();
		//Creating a polygon... scaled a bit due to possible rounding error for requ that poly's are integer based
		int num = xyPoints[0].length;
		for (int i = 0; i < num; i++)
			poly.addPoint((int)(multiplier*xyPoints[0][i]), (int)(multiplier*xyPoints[1][i]));
		poly.npoints = num;
		
		ArrayList<Cell> bound = new ArrayList<Cell>();
		for (int i = 0; i < numCells; i++)
		{
			valX = (int)(multiplier*cellValues[i][featureX_index]);
			valY = (int)(multiplier*cellValues[i][featureY_index]);
			if (poly.contains(valX, valY))
				bound.add(cells.get(i));
		}
		return bound;
	
	}
	
	public float getBoundCellsFeatures_Mean(ArrayList<Cell> cells, String featureName)
	{
		ArrayList<Cell> bound = getCellsBound(cells);
		if(bound==null || bound.size()==0)
			return 0;
		float sum = 0;
		int count = 0;
		int index = models.Model_Main.getModel().getFeature_Index(featureName);

		int len = bound.size();
		for (int i = 0; i < len; i++) {
			Cell cell = bound.get(i);
			float[] vals = cell.getFeatureValues();
			if(vals!=null && vals.length>index)
			{
				sum+=vals[index];
				count++;
			}
		}
		if(count==0)
			return 0;
	
		return sum/(float)count;
	}
	
	public float getBoundCellsFeatures_Integrated(ArrayList<Cell> cells, String featureName)
	{
		ArrayList<Cell> bound = getCellsBound(cells);
		if(bound==null || bound.size()==0)
			return 0;
		float count = 0;
		int index = models.Model_Main.getModel().getFeature_Index(featureName);

		int len = bound.size();
		for (int i = 0; i < len; i++) {
			Cell cell = bound.get(i);
			float[] vals = cell.getFeatureValues();
			if(vals!=null && vals.length>index)
				count+=vals[index];
		}
		
		return count;
	}
	
	
	public float getTotalNumberOfCellsBound(ArrayList<Cell> cells)
	{
		ArrayList<Cell> bound = getCellsBound(cells);
		if(bound==null || bound.size()==0)
			return 0;
		
		return bound.size();
	}
	
	
	private double[][] removePointsThatWereDuplicated(double[][] points)
	{
		//Means we have a box and those points never are duplicated
		if (points[0].length ==4)
			return points;
		
		
		int count=0;
		for (int i = 0; i < points[0].length-1; i++)
		{
			if (points[0][i] != points[0][i+1] && points[1][i] != points[0][i+1])
				count++;
		}
		double[][] points2 = new double[2][count];
		count = 0;
		for (int i = 0; i < points[0].length-1; i++)
		{
			if (points[0][i] != points[0][i+1] && points[1][i] != points[0][i+1])
			{
				points2[0][count] = points[0][i];
				points2[1][count] = points[1][i];
				count++;
			}
		}
		return points2;
		
		
	}
	
	
}

