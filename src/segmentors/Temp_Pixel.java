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

package segmentors;



public class Temp_Pixel implements Comparable
{
	private int row;
	private int col;
	private int value;
	private int ID;
	
	
	public Temp_Pixel(int r, int c, int id)
	{
		row = r;
		col = c;
		value = -5;
		ID = id;
	}
	public Temp_Pixel(int r, int c, int z_, int id)
	{
		row = r;
		col = c;
		value = z_;
		ID = id;
	}
	
	
	/** Sets the pixel value as an integer
	 * @author BLM*/
	public void setValue(int val)
	{
		value = val;
	}
	
	/** Sets the ID of the Pixel
	 * @author BLM*/
	public void setID(int id)
	{
		ID = id;
	}
	
	/** Returns the pixel value as an integer
	 * @author BLM*/
	public int getValue()
	{
		return value;
	}
	/** Returns the pixel ID number
	 * @author BLM*/
	public int getID()
	{
		return ID;
	}
	/** Returns the pixel row where it is found in the image
	 * @author BLM*/
	public int getRow()
	{
		return row;
	}
	/** Returns the pixel column where it is found in the image
	 * @author BLM*/
	public int getColumn()
	{
		return col;
	}
	
	public double getDistance_L1(Temp_Pixel p)
	{
		double dist = 0;
		dist += Math.abs(p.row-row);
		dist += Math.abs(p.col-col);
		
		return dist;
	}
	
	
	public int compareTo(Object o)
	{
		if(!(o instanceof Temp_Pixel))
			throw new ClassCastException();
		
		Temp_Pixel obj =  (Temp_Pixel) o;
		
		if( obj.value < value )
			return 1;
		
		if( obj.value > value )
			return -1;
		
		return 0;
	}
	
	
	/** Says if this pixels is above an intensity threshold
	 * @author BLM*/
	static public boolean pixelOn(int[] pix)
	{
		int sum = 0;
		int len =  pix.length;
		
		for (int i = 1; i < len; i++)
			sum+=pix[i];
		
		
		if (sum>0)
		{
			
			return true;
		}
		return false;
	}
	
	static public boolean pixelOn(int[] pix, float threshold)
	{
		int sum = 0;
		int len =  pix.length;
		
		for (int i = 1; i < len; i++)
			sum+=pix[i];
		
		if (sum>threshold)
			return true;
		
		return false;
	}
	
	static public void resetIDs(Temp_Pixel[][] pixels)
	{
		int rows = pixels.length;
		int cols = pixels[0].length;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				pixels[r][c].ID = -1;
	}
	
	static public boolean pixelOn(int pix, float threshold)
	{
		int sum = 0;
		
		if (sum>threshold)
			return true;
		
		return false;
	}
	
	static public boolean pixelOn_binary(int[] pix)
	{
		int sum = 0;
		int len =  pix.length;
		
		for (int i = 0; i < len; i++)
			sum+=pix[i];
		
		if (sum>230)
			return true;
		return false;
	}
	
	/** Checks if the given pixel that has the given Euclidean Distance is Surrounded by pixels at a higher level
	 * @author BLM**/
	static public float getLargestNeighborEuclidDist(Temp_Pixel pix, Temp_Pixel[][] pixels,float[][][] distanceMap)
	{
		Temp_Pixel[] neigh = Temp_Pixel.getNeighbors(pix, pixels);
		int len = neigh.length;
		float max = -1;
		for (int i = 0; i < len; i++)
		{
			Temp_Pixel p = neigh[i];
			float val = distanceMap[p.row][p.col][0];
			if (val>max)
				max = val;
		}
		
		return max;
	}
	
	
	/** Returns an array of pixels of the given pixel, top to bottom, L->R starting at r-1, c-1
	 * @author BLM*/
	static public Temp_Pixel[] getNeighbors(Temp_Pixel centerPixel, Temp_Pixel[][] pixels)
	{
		int height = pixels.length;
		int width = pixels[0].length;
		Temp_Pixel[] pixs = null;
		//top row
		if (centerPixel.row==0)
		{
			if (centerPixel.col == 0)
			{
				//top left corner
				pixs = new Temp_Pixel[3];
				pixs[0] = pixels[centerPixel.row][centerPixel.col+1];
				pixs[1] = pixels[centerPixel.row+1][centerPixel.col+1];
				pixs[2] = pixels[centerPixel.row+1][centerPixel.col];
			}
			else if (centerPixel.col == width-1)
			{
				//top right corner
				pixs = new Temp_Pixel[3];
				pixs[0] = pixels[centerPixel.row][centerPixel.col-1];
				pixs[1] = pixels[centerPixel.row+1][centerPixel.col-1];
				pixs[2] = pixels[centerPixel.row+1][centerPixel.col];
			}
				
			else
			{
				//general top row
				pixs = new Temp_Pixel[5];
				pixs[0] = pixels[centerPixel.row][centerPixel.col-1];
				
				pixs[1] = pixels[centerPixel.row+1][centerPixel.col-1];
				pixs[2] = pixels[centerPixel.row+1][centerPixel.col];
				pixs[3] = pixels[centerPixel.row+1][centerPixel.col+1];
				
				pixs[4] = pixels[centerPixel.row][centerPixel.col+1];
			}
		}
			//bottom row pixels
		else if (centerPixel.row == height-1)
		{
			if (centerPixel.col == 0)
			{
				//bottom left corner
				pixs = new Temp_Pixel[3];
				pixs[0] = pixels[centerPixel.row-1][centerPixel.col];
				pixs[1] = pixels[centerPixel.row-1][centerPixel.col+1];
				pixs[2] = pixels[centerPixel.row][centerPixel.col+1];
			}
			else if (centerPixel.col == width-1)
			{
				//bottom right corner
				pixs = new Temp_Pixel[3];
				pixs[0] = pixels[centerPixel.row][centerPixel.col-1];
				pixs[1] = pixels[centerPixel.row-1][centerPixel.col-1];
				pixs[2] = pixels[centerPixel.row-1][centerPixel.col];
			}
			else
			{
				//general bottom row
				pixs = new Temp_Pixel[5];
				pixs[0] = pixels[centerPixel.row][centerPixel.col-1];
				
				pixs[1] = pixels[centerPixel.row-1][centerPixel.col-1];
				pixs[2] = pixels[centerPixel.row-1][centerPixel.col];
				pixs[3] = pixels[centerPixel.row-1][centerPixel.col+1];
				
				pixs[4] = pixels[centerPixel.row][centerPixel.col+1];
			}
		}
			
			
			
			//general left column
		else if (centerPixel.col==0)
		{
			pixs = new Temp_Pixel[5];
			pixs[0] = pixels[centerPixel.row-1][centerPixel.col];
			
			pixs[1] = pixels[centerPixel.row-1][centerPixel.col+1];
			pixs[2] = pixels[centerPixel.row][centerPixel.col+1];
			pixs[3] = pixels[centerPixel.row+1][centerPixel.col+1];
			
			pixs[4] = pixels[centerPixel.row+1][centerPixel.col];
		}
			//far right column pixels
		else if (centerPixel.col == width-1)
		{
			pixs = new Temp_Pixel[5];
			pixs[0] = pixels[centerPixel.row-1][centerPixel.col];
			
			pixs[1] = pixels[centerPixel.row-1][centerPixel.col-1];
			pixs[2] = pixels[centerPixel.row][centerPixel.col-1];
			pixs[3] = pixels[centerPixel.row+1][centerPixel.col-1];
			
			pixs[4] = pixels[centerPixel.row+1][centerPixel.col];
		}
			
			
		else
		{
			//general body pixels
			pixs = new Temp_Pixel[8];
			pixs[0] = pixels[centerPixel.row-1][centerPixel.col-1];
			pixs[1] = pixels[centerPixel.row-1][centerPixel.col];
			pixs[2] = pixels[centerPixel.row-1][centerPixel.col+1];
			
			pixs[3] = pixels[centerPixel.row][centerPixel.col-1];
			pixs[4] = pixels[centerPixel.row][centerPixel.col+1];
			
			pixs[5] = pixels[centerPixel.row+1][centerPixel.col-1];
			pixs[6] = pixels[centerPixel.row+1][centerPixel.col];
			pixs[7] = pixels[centerPixel.row+1][centerPixel.col+1];
		}
		return pixs;
	}
	
	
	static public Temp_Pixel[] getFourNeighbors(Temp_Pixel centerPixel, Temp_Pixel[][] pixels)
	{
		int height = pixels.length;
		int width = pixels[0].length;
		Temp_Pixel[] pixs = null;
		
		if (centerPixel.row>=height-1 || centerPixel.col>=width-1)
		{
			pixs = new Temp_Pixel[0];
			return pixs;
		}
		else if (centerPixel.row<=1 || centerPixel.col<=1)
		{
			pixs = new Temp_Pixel[0];
			return pixs;
		}
		
		//general body pixels
		pixs = new Temp_Pixel[4];
		
		pixs[0] = pixels[centerPixel.row-1][centerPixel.col];
		pixs[1] = pixels[centerPixel.row][centerPixel.col-1];
		pixs[2] = pixels[centerPixel.row][centerPixel.col+1];
		pixs[3] = pixels[centerPixel.row+1][centerPixel.col];
		
		return pixs;
	}
	
	
	
	public static byte[] convertToByteArray(float[][][] im)
	{
		int rows = im.length;
		int cols = im[0].length;
		int counter = 0;
		byte[] arr = new byte[rows*cols];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				arr[counter] = (byte)(im[r][c][0]-128);
				counter++;
			}
		
		return arr;
	}
	
	public static byte[] convertToByteArray(int[][][] im)
	{
		int rows = im.length;
		int cols = im[0].length;
		int counter = 0;
		byte[] arr = new byte[rows*cols];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				arr[counter] = (byte)(im[r][c][0]-128);
				counter++;
			}
		
		return arr;
	}
}

