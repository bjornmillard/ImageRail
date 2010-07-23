/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

package plots3D;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Point3d;

/** Stores a data set of points and some important information about the set, such as the XYZ bounds
 * @author BLM*/
public class DataSet
{
	final private Point3d[] TheData;
	private double[] TheColors; // [0-->1]
	private double[][] DataRanges;
	private int[] AxisScaleTypes;
	private double[][] AxisRanges;
	
	public DataSet(Point3d[] data, double[] colors, double[][] axisRanges, int[] axisScaleTypes)
	{
		//Preprocessing the data points
		//
		// (1) clipping data outside of the ranges
		// (2) changing to log-scale if needed
		AxisRanges = axisRanges;
		AxisScaleTypes = axisScaleTypes;
		
		
		//adding the points to the dataset if the points lay within the ranges
		ArrayList temp = new ArrayList();
		int len = data.length;
		if (AxisRanges!=null)
		{
			for (int i=0; i < len; i++)
			{
				boolean pointOK = true;
				if (data[i].x<axisRanges[0][0] || data[i].x>axisRanges[0][1])
					pointOK = false;
				if (data[i].y<axisRanges[1][0] || data[i].y>axisRanges[1][1])
					pointOK = false;
				if (data[i].z<axisRanges[2][0] || data[i].z>axisRanges[2][1])
					pointOK = false;
				
				if (pointOK)
					temp.add(data[i]);
			}
			len = temp.size();
			
			TheData = new Point3d[len];
			for (int i=0; i < len; i++)
				TheData[i] = (Point3d)temp.get(i);
			
			TheColors = colors;
			
			initBounds();
		}
		else
		{
			TheData = data;
			initBounds();
			AxisRanges = new double[3][2];
			for (int r = 0; r < 3; r++)
				for (int c=0; c< 2; c++)
					AxisRanges[r][c] = DataRanges[r][c];
		}
		
		//sorting the points according to their xValue
		Arrays.sort(TheData, new xPointSorter());
		
	}
	
	public DataSet(Point3d[] data, int[] axisScaleTypes)
	{
		TheData = data;
		AxisScaleTypes = axisScaleTypes;
		//sorting the points according to their xValue
		Arrays.sort(TheData, new xPointSorter());
		initBounds();
	}
	
	public double[][] getAxisRanges()
	{
		if (AxisRanges!=null)
			return AxisRanges;
		return DataRanges;
	}
	
	public Point3d[] getPoints()
	{
		return TheData;
	}
	public double[] getColors()
	{
		return TheColors;
	}
	
	public DataSet getNormalizedDataSet()
	{
		Point3d[] points = getCopyOfData();
		Tools.normalizePoints(points, AxisRanges);
		return new DataSet(points, AxisScaleTypes);
	}
	
//	public float getYRangeFraction()
//	{
//		double xMax = getXMax();
//		double xMin = getXMin();
//		double yMax = getYMax();
//		double yMin = getYMin();
//		double xRange = (xMax-xMin);
//		double yRange = (yMax-yMin);
//		float yFract = 1;
//
//		if (xRange>yRange)
//			yFract = (float)(yRange/xRange);
//
//		return yFract;
//	}
//
//	public float getXRangeFraction()
//	{
//		double xMax = getXMax();
//		double xMin = getXMin();
//		double yMax = getYMax();
//		double yMin = getYMin();
//		double xRange = (xMax-xMin);
//		double yRange = (yMax-yMin);
//		float xFract = 1;
//
//		if (yRange>xRange)
//			xFract = (float)(xRange/yRange);
//
//		return xFract;
//	}
	
	/** Returns the requested Data bounds
	 * @author BLM*/
	public double getDataMin_X()
	{
		return DataRanges[0][0];
	}
	public double getDataMax_X()
	{
		return DataRanges[0][1];
	}
	public double getDataMin_Y()
	{
		return DataRanges[1][0];
	}
	public double getDataMax_Y()
	{
		return DataRanges[1][0];
	}
	public double getDataMin_Z()
	{
		return DataRanges[2][0];
	}
	public double getDataMax_Z()
	{
		return DataRanges[2][1];
	}
	/** Returns the requested Axis range bound
	 * @author BLM*/
	public double getAxisMin_X()
	{
		if (AxisRanges!=null)
			return AxisRanges[0][0];
		return DataRanges[0][0];
	}
	public double getAxisMax_X()
	{
		if (AxisRanges!=null)
			return AxisRanges[0][1];
		return DataRanges[0][1];
	}
	public double getAxisMin_Y()
	{
		if (AxisRanges!=null)
			return AxisRanges[1][0];
		return DataRanges[1][0];
	}
	public double getAxisMax_Y()
	{
		if (AxisRanges!=null)
			return AxisRanges[1][1];
		return DataRanges[1][1];
	}
	public double getAxisMin_Z()
	{
		if (AxisRanges!=null)
			return AxisRanges[2][0];
		return DataRanges[2][0];
	}
	public double getAxisMax_Z()
	{
		if (AxisRanges!=null)
			return AxisRanges[2][1];
		return DataRanges[2][1];
	}
	
	/** Returns a single Point3d from the dataSet
	 * @author BLM*/
	public Point3d getPoint(int index)
	{
		return TheData[index];
	}
	
	/** Returns the number of data points
	 * @author BLM*/
	public int getNumPoints()
	{
		return TheData.length;
	}
	
	/** Returns a copy of all the data points in order never to modify the original data
	 * @author BLM*/
	public Point3d[] getCopyOfData()
	{
		int len = getNumPoints();
		Point3d[] copy = new Point3d[len];
		for (int i = 0; i < len; i++)
		{
			Point3d p = new Point3d();
			p.x = TheData[i].x;
			p.y = TheData[i].y;
			p.z	= TheData[i].z;
			copy[i] = p;
		}
		return copy;
	}
	
	/** Initializes XYZ Min and Max
	 * @author BLM*/
	private void initBounds()
	{
		DataRanges = new double[3][2];
		
		DataRanges[0][0] = Double.POSITIVE_INFINITY;
		DataRanges[1][0] = Double.POSITIVE_INFINITY;
		DataRanges[2][0] = Double.POSITIVE_INFINITY;
		DataRanges[0][1] = Double.NEGATIVE_INFINITY;
		DataRanges[1][1] = Double.NEGATIVE_INFINITY;
		DataRanges[2][1] = Double.NEGATIVE_INFINITY;
		
		//finding the min/max in all 3 dimensions of the data
		int len = TheData.length;
		for (int i =0; i < len; i++)
		{
			Point3d p = TheData[i];
			//x
			if(p.x<DataRanges[0][0])
				DataRanges[0][0]=p.x;
			else if (p.x>DataRanges[0][1])
				DataRanges[0][1]=p.x;
			//y
			if(p.y<DataRanges[1][0])
				DataRanges[1][0]=p.y;
			else if (p.y>DataRanges[1][1])
				DataRanges[1][1]=p.y;
			//z
			if(p.z<DataRanges[2][0])
				DataRanges[2][0]=p.z;
			else if (p.z>DataRanges[2][1])
				DataRanges[2][1]=p.z;
			
		}
		
		DataRanges[2][1] = 4*DataRanges[2][1];
		
	}
	
//	/** Returns the normalzed Z-Value of the point given by the index
//	 * @author BLM*/
//	public double getNormalizedZValue(int index)
//	{
//		double normVal = 0;
//		if (Math.abs(ZMinMax[0])>Math.abs(ZMinMax[1]))
//			normVal = Math.abs(ZMinMax[0]);
//		else
//			normVal = Math.abs(ZMinMax[1]);
//
//		Point3d p = TheData[index];
//		if (normVal < 0.000001d)
//			return 0d;
////		System.out.println(p.z);
//		return p.z/normVal;
//
//		//			System.out.println(Math.cos(y*10f)*Math.cos(Math.sin(x*5f)));
//	}
}

