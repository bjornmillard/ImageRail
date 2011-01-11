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

package tempObjects;


import java.util.ArrayList;
import java.util.Iterator;

import imagerailio.Point;

public class Cytoplasm
{
	private int numPixels;
	private float[] channelValues_integrated;
	private ArrayList<Point> PixelCoordinates;
	
	public Cytoplasm()
	{
		PixelCoordinates = new ArrayList<Point>();
	}
	
	
	
	public void clearPixelData()
	{
		PixelCoordinates = null;
	}
	
	
	/** Returns the number of pixels that compose the nucleus (ex: nuclear area in pixel units)
	 * @author BLM*/
	public int getNumPixels()
	{
		return numPixels;
	}
	/** Sets the number of pixels that compose the nucleus (ex: nuclear area in pixel units)
	 * @author BLM*/
	public void setNumPixels(int numPix)
	{
		 numPixels = numPix;
	}
	
	/** Returns the ArrayList of Points that represent the coordinates of the  of the pixels that compose this cytoplasm
	 * @author BLM */
	public ArrayList<Point> getPixelCoordinates()
	{
		return PixelCoordinates;
	}
	
	/** Returns the integrated float values of the features computed for this cytoplasm.  The array of values is of length==numFeatures
	 * @author BLM*/
	public float[] getChannelValues_integrated()
	{
		return channelValues_integrated;
	}
	
	
	public void initNumChannels(int numChannels)
	{
		channelValues_integrated = new float[numChannels];
	}
	
	public void setChannelValue(float val, int index, int TYPE)
	{
		if (TYPE == Cell_RAM.INTEGRATED)
			channelValues_integrated[index] = val;
	}
	
	public double getChannelValue(int index, int TYPE)
	{
		if (TYPE == Cell_RAM.MEAN)
		{
			if (index>=channelValues_integrated.length)
				return 0;
			return channelValues_integrated[index]/(float)numPixels;
		}
		else if (TYPE == Cell_RAM.INTEGRATED)
		{
			if (index>=channelValues_integrated.length)
				return 0;
			return channelValues_integrated[index];
		}
		return 0;
	}
	
	
	
	public void kill()
	{
		if (PixelCoordinates!=null)
		{
			Iterator iter = PixelCoordinates.iterator();
			while (iter.hasNext())
			{
				Point element = (Point)iter.next();
				element = null;
			}
			PixelCoordinates = null;
		}
		channelValues_integrated = null;
		clearPixelData();
	}
}

