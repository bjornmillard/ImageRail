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

package tools;

import java.awt.Point;

public class MathOps
{
	/** Returns bool if all the given strings are some form of number
	 * @author BLM*/
	static public boolean areNumbers(String[] strings)
	{
		int len = strings.length;
		for (int i=0; i < len ;i++)
		{
			boolean thisNumber = false;
			if (isDouble(strings[i])){thisNumber=true;}
			else if (isInt(strings[i])){thisNumber=true;}
			else if (isFloat(strings[i])){thisNumber=true;}
			if (!thisNumber)
				return false;
		}
		return true;
	}
	static public double max(double[] arr)
	{
		double max = Double.NEGATIVE_INFINITY;
		int len = arr.length;
		for (int i = 0; i < len; i++)
			if (arr[i]>max)
				max=arr[i];
		return max;
	}
	static public double min(double[] arr)
	{
		double min = Double.POSITIVE_INFINITY;
		int len = arr.length;
		for (int i = 0; i < len; i++)
			if (arr[i]<min)
				min=arr[i];
		return min;
	}
	
	static public float sum(float[] arr)
	{
		int sum = 0;
		int len = arr.length;
		for (int i = 0; i < len; i++)
			sum+=arr[i];
		return sum;
	}
	
	static public double max(double[][] arr)
	{
		double max = Double.NEGATIVE_INFINITY;
		int len = arr.length;
		for (int r = 0; r < len; r++)
		{
			int num = arr[r].length;
			for (int c = 0; c < num; c++)
			{
				if (arr[r][c]>max)
					max=arr[r][c];
			}
			
		}
		return max;
	}
	static public double min(double[][] arr)
	{
		double min = Double.POSITIVE_INFINITY;
		int len = arr.length;
		for (int r = 0; r < len; r++)
		{
			int num = arr[r].length;
			for (int c = 0; c < num; c++)
			{
				if (arr[r][c]<min)
					min=arr[r][c];
			}
			
		}
		return min;
	}
	
	/**
	 * Converts the default Float[][] object values to float primitives
	 * 
	 * @author BLM
	 */
	static public float[][] convertTofloatMatrix(Float[][] data) {
		float[][] fVals = new float[data.length][data[0].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				fVals[i][j] = data[i][j].floatValue();
		data = null;
		return fVals;
	}

	static public boolean isNumber(String string)
	{
		boolean thisNumber = false;
		if (isDouble(string)){thisNumber=true;}
		else if (isInt(string)){thisNumber=true;}
		else if (isFloat(string)){thisNumber=true;}
		if (!thisNumber)
			return false;
		return true;
	}
	
	/** Smooth the data by simply doing a 3-point running average
	 * @author BLM*/
	static public float[] smoothData(float[] data)
	{
		if (data==null || data.length < 3)
			return null;
		
		int len = data.length;
		float[] avgData = new float[len];
		
		//First Point
		avgData[0] = (data[0]+data[1])/2f;
		//Last Point
		avgData[len-1] = (data[len-1]+data[len-2])/2f;
		
		
		for (int i = 1; i < len-1; i++)
			avgData[i] = (data[i-1]+data[i]+data[i+1])/3f;
		
		return avgData;
	}
	
	
	/** Smooth the data by simply doing a 3-point running average
	 * @author BLM*/
	static public float[] smoothData_forceMonotonic(float[] data)
	{
		if (data==null || data.length < 3)
			return null;
		
		int len = data.length;
		float[] avgData = new float[len];
		
		//First Point
		avgData[0] = (data[0]+data[1])/2f;
		//Last Point
		avgData[len-1] = (data[len-1]+data[len-2])/2f;
		
		//Averaging it
		for (int i = 1; i < len-1; i++)
			avgData[i] = (data[i-1]+data[i]+data[i+1])/3f;
		
		//Forcing it to be monotonic
		int counter =0;
		for (int i = 0; i < len-1; i++)
		{
			if (avgData[i]<avgData[i+1])
				counter++;
		}
		float fractionPositiveSlope = (float)counter/(float)len;
		if (fractionPositiveSlope>=0.5f)
		{
//			System.out.println("Forcing Line to be POSITIVE Monotonic: "+fractionPositiveSlope);
			for (int i = 0; i < len-1; i++)
				if (avgData[i]>avgData[i+1])
					avgData[i+1] = avgData[i];
		}
		else if (fractionPositiveSlope<0.5f )
		{
//			System.out.println("Forcing Line to be NEGATIVE Monotonic:"+fractionPositiveSlope);
			for (int i = 0; i < len-1; i++)
				if (avgData[i]<avgData[i+1])
					avgData[i+1] = avgData[i];
		}
		
		// Now going back to avg Data and re-averaging starting from center and going to bounds
		// This will make sure we do not have more than a single 1 and 0 value
		for (int i = len/2; i > 0; i--)
			avgData[i] = (avgData[i-1]+avgData[i]+avgData[i+1])/3f;
		for (int i = (len/2+1); i < (len-1); i++)
			avgData[i] = (avgData[i-1]+avgData[i]+avgData[i+1])/3f;
		
		return avgData;
	}
	
	
	static public boolean isInt(String val)
	{
		try
		{
			Integer.parseInt(val);
			return true;
			
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	static public boolean isFloat(String val)
	{
		try
		{
			Float.parseFloat(val);
			return true;
			
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	static public boolean isDouble(String val)
	{
		try
		{
			Double.parseDouble(val);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	
	/** Multiplies a 2D matrix and a 1D vector
	 * @author BLM
	 * @param double[][] matrixToBeCopied
	 * @return double[][] theCopy*/
	static public double[] multiply(double[][] mat1, double[] vec)
	{
		int rows1 = mat1.length;
		int cols1 = mat1[0].length;
		int vecLen = vec.length;
		if (cols1!=vecLen)
		{
			System.out.println("**Error in MatrixOps.multiply() - Matrix dimensions not compatible!!!");
			return null;
		}
		double[] ans = new double[rows1];
		double[] v1 = new double[cols1];
		double[] v2 = new double[cols1];
		for (int r1 = 0; r1 < rows1; r1++)
		{
			for (int i =0; i < cols1; i++)
				v1[i] = mat1[r1][i];
			ans[r1] = dot(v1,vec);
		}
		return ans;
	}
	
	/** Multiplies two 2D matrices together
	 * @author BLM
	 * @param double[][] matrixToBeCopied
	 * @return double[][] theCopy*/
	static public double[][] multiply(double[][] mat1, double[][] mat2)
	{
		int rows1 = mat1.length;
		int cols1 = mat1[0].length;
		int rows2 = mat2.length;
		int cols2 = mat2[0].length;
		
		if (cols1!=rows2)
		{
			System.out.println("**Error in MatrixOps.multiply() - Matrix dimensions not compatible!!!");
			return null;
		}
		
		double[][] ans = new double[rows1][cols2];
		double[] v1 = new double[cols1];
		double[] v2 = new double[cols1];
		for (int r1 = 0; r1 < rows1; r1++)
		{
			for (int c2 = 0; c2 < cols2; c2++)
			{
				for (int i =0; i < cols1; i++)
				{
					v1[i] = mat1[r1][i];
					v2[i] = mat2[i][c2];
				}
				ans[r1][c2] = dot(v1,v2);
			}
		}
		return ans;
	}
	
	
	/** Transforms the given point with the given projection and rotation matrices */
	static public double[] transformPoint3D(double[][] rotationMatrix, double[][] projectionMatrix, double[] vector)
	{
		int dim = vector.length;
		double[] temp = new double[dim+1];
		for (int n = 0; n < dim; n++)
			temp[n]=vector[n];
		temp[dim] = 1;
		return tools.MathOps.multiply(rotationMatrix,tools.MathOps.multiply(projectionMatrix, temp));
	}
	
	/** Dot product of two given vectors
	 * @author BLM
	 * @param double[] vector1
	 * @param double[] vector2
	 * @return double dotProduct*/
	static public double dot(double[] v1, double[] v2)
	{
		int len1 = v1.length;
		int len2 = v2.length;
		if (len1!=len2)
		{
			System.out.println("**Error in MatrixOps.dot() - Vector dimensions not equal!!!");
			return -1;
		}
		double ans = 0;;
		for (int i=0; i < len1; i++)
			ans+=v1[i]*v2[i];
		return ans;
	}
	
	
	/** Rotates a given 2D vector 90Degrees clockwise to make it orthogonal
	 * @author BLM
	 * @param double[] vector
	 * @return double[] rotatedVector*/
	static public double[] rotate2Dvector90degrees(double[] vect)
	{
		// constructing the rotation matrix
		double[][] rotMat = new double[2][2];
		rotMat[0][0] = 0;
		rotMat[0][1] = -1;
		rotMat[1][0] = 1;
		rotMat[1][1] = 0;
		
		return multiply(rotMat,vect);
	}
	
	
	/** Returns an array containing: [slope, y-Intercept, xMin, xMax, yMin, yMax]*/
	static public double[] computeLinearRegression(Point.Double[] points)
	{
		double[] data = new double[7];
		int len = points.length;
		double tot = 0;
		double x = 0;
		double y = 0;
		double xy = 0;
		double x2 = 0;
		double y2 = 0;
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < len; i++)
		{
			if (points[i]==null)
				return null;
			
			double xp = points[i].x;
			double yp = points[i].y;
			
//			if (xp<100000&&xp>=0 &&yp<100000 && yp>=0)
			{
				if (xp>xMax)
					xMax=xp;
				if (xp<xMin)
					xMin = xp;
				
				if (yp>yMax)
					yMax=yp;
				if (yp<yMin)
					yMin = yp;
				
				tot++;
				x+=xp;
				y+=yp;
				xy+=(xp*yp);
				x2+=(xp*xp);
				y2+=(yp*yp);
			}
		}
		
		//perpendicular offsets:
		double iTot = (1d/(double)tot);
		double B = 0.5d*((y2-(iTot*(y*y)))-(x2-iTot*x*x))/(iTot*x*y-xy);
		double mPlus = -B + Math.sqrt(B*B+1);
		double mMinus = -B - Math.sqrt(B*B+1);
		
		double x0 = x/tot;
		double y0 = y/tot;
		
		double bPlus = (y0-mPlus*x0);
		double bMinus = (y0-mMinus*x0);
		
		//creating line+
		double xsP = xMin;
		double xeP = xMax;
		double ysP = (mPlus*xMin+bPlus);
		double yeP = (mPlus*xMax+bPlus);
		//creating line-
		double xsM = xMin;
		double xeM = xMax;
		double ysM = (mMinus*xMin+bMinus);
		double yeM = (mMinus*xMax+bMinus);
		
		double varPlus = 0;
		double varMinus = 0;
		
		//computing the variance from line:
		for (int i = 0; i < len; i++)
		{
			double xp = points[i].x;
			double yp = points[i].y;
			
//			if (xp<100000&&xp>=0 &&yp<100000 && yp>=0)
			{
				double dP = Math.abs((xeP-xsP)*(ysP-yp)-(xsP-xp)*(yeP-ysP))/(Math.sqrt((xeP-xsP)*(xeP-xsP)+(yeP-ysP)*(yeP-ysP)));
				double dM = Math.abs((xeM-xsM)*(ysM-yp)-(xsM-xp)*(yeM-ysM))/(Math.sqrt((xeM-xsM)*(xeM-xsM)+(yeM-ysM)*(yeM-ysM)));
				
				varPlus+=dP;
				varMinus+=dM;
			}
		}
		
		double m = 0;
		double b = 0;
		if (varPlus<=varMinus)
		{
			m = mPlus;
			b = bPlus;
		}
		else
		{
			m = mMinus;
			b = bMinus;
		}
		
		data[0] = m;
		data[1] = b;
		data[2] = tot;
		data[3] = xMin;
		data[4] = xMax;
		data[5] = yMin;
		data[6] = yMax;
		return data;
	}
	
	/** Returns an array containing: [slope, y-Intercept, xMin, xMax, yMin, yMax]*/
	static public double computeCorrelationCoefficient(Point.Double[] points)
	{
		double coeff = 0;
		int len = points.length;
		double N = 0;
		double x = 0;
		double y = 0;
		double xy = 0;
		double x2 = 0;
		double y2 = 0;
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < len; i++)
		{
			double xp = points[i].x;
			double yp = points[i].y;
			
			if (xp<100000&&xp>=0 &&yp<100000 && yp>=0)
			{
				if (xp>xMax)
					xMax=xp;
				if (xp<xMin)
					xMin = xp;
				
				if (yp>yMax)
					yMax=yp;
				if (yp<yMin)
					yMin = yp;
				
				N++;
				x+=xp;
				y+=yp;
				xy+=(xp*yp);
				x2+=(xp*xp);
				y2+=(yp*yp);
			}
		}
		
		coeff = (N*xy-x*y)/(Math.sqrt(N*x2-(x*x))*Math.sqrt(N*y2-(y*y)));
		System.out.println("coef: "+coeff);
		return coeff;
	}
	
	static public double log(double in)
	{
		return Math.log10(in);
	}
	
	static public double exp(double in)
	{
		return Math.pow(10, in);
	}
	
	/** Returns the max length of each row*/
	static public int getMaxRowLength(double[][] data)
	{
		int max = 0;
		int len = data.length;
		for (int i = 0; i < len; i++)
		{
			int val = data[i].length;
			if (val>max)
				max=val;
		}
		return max;
	}
	
	
}

