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


public class ColorMaps
{
	//ColorMap Types
	static final public int RGB_1 = 0;
	static final public int RGB_2 = 1;
	static final public int BEACH = 2;
	static final public int RAINBOW = 3;
	static final public int FIRE = 4;
	static final public int GREEN = 5;
	static final public int GRAYS = 6;
	
	static final public String[] colorMapNames  = {"RGB_1", "RGB_2", "BEACH", "RAINBOW", "FIRE", "GREEN", "GRAYS"};
	
	static public int getColorMapIndex(String text)
	{
		for (int i = 0; i < colorMapNames.length; i++)
		{
			if (text.equalsIgnoreCase(colorMapNames[i]))
				return i;
		}
		return 0;
	}
	
	/** Returns a color given a double value to be converted to a float[3] color through
	 * the given ColorMapType
	 * @author BLM*/
	static public void getColorValue(double value, double minVal, double maxVal, float[] color, int colorMapType)
	{
		/** Standard coloring */
		if (colorMapType==BEACH)
		{
			double val =  (value-minVal)/(maxVal-minVal);
			double absVal = Math.abs(val);
			double coeff = 1f;
			
			//blue
			float blue = 0;
			blue = 1-((float)Math.pow(coeff*(absVal),2));
//			blue = 1-(float)Math.pow(4f*absVal,2);
			
			//green
			float green = 0;
			green = 1-((float)Math.pow(coeff*(absVal-0.5f),2));
			
			//red
			float red = 0;
			red = 1-((float)Math.pow(coeff*(absVal-1f),2));
//			red = 1-(float)Math.pow(1f+4f*absVal,2);
//			red = 1-(float)Math.pow(1+absVal,2);
//			if (absVal>0.5f)
//				red = 2f*(float)absVal;
			
//			System.out.println(value);
			color[0]=  red; color[1] = green; color[2] = blue;
			
		}
		else if (colorMapType==RGB_2)
		{
			double val =  (value-minVal)/(maxVal-minVal);
			double absVal = Math.abs(val);
			double coeff = 2f;
			
			//blue
			float blue = 0;
			blue = 1-((float)Math.pow(coeff*(absVal+0.1),2));
//			blue = 1-(float)Math.pow(4f*absVal,2);
			
			//green
			float green = 0;
			green = 1-((float)Math.pow(coeff*(absVal-0.5f),2));
			
			//red
			float red = 0;
			red = 1-((float)Math.pow(coeff*(absVal-0.9f),2));
//			red = 1-(float)Math.pow(1f+4f*absVal,2);
//			red = 1-(float)Math.pow(1+absVal,2);
//			if (absVal>0.5f)
//				red = 2f*(float)absVal;
			
//			System.out.println(value);
			color[0]=  red; color[1] = green; color[2] = blue;
			
		}
		else if (colorMapType==RGB_1)
		{
			double val =  (value-minVal)/(maxVal-minVal);
			double spread = 0.21f;
			
			//red
			float red = 0;
			red = ((float)Math.exp(-((val-0.7)*(val-0.7)/(spread*spread))));
			
			//green
			float green = 0;
			green = ((float)Math.exp(-((val-0.5)*(val-0.5)/(spread*spread))));
			
			//blue
			float blue = 0;
			blue = ((float)Math.exp(-((val-0.3)*(val-0.3)/(spread*spread))));
			
			color[0]=  red; color[1] = green; color[2] = blue;
		}
			
		else if (colorMapType==FIRE)
		{
			double tMin = (maxVal-minVal)/2.5f;
			if (value<tMin)
			{
				color[0]  = (float)0;
				color[1] = (float)0;
				color[2] = (float)0;
				return;
			}
			
			
			double val =  (value-tMin)/(maxVal-tMin);
			
			
			
//			double val =  (value-minVal)/(maxVal-minVal);
			double intensity = val;
			double val2 = 85f/256f;
			double R = min(intensity,val2) * 3f;
			intensity = max(intensity-val2,0);
			double G = min(intensity,val2) * 3f;
			intensity = max(intensity-val2,0);
			double B = min(intensity,val2) * 3f;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
		}
		else if (colorMapType==RAINBOW)
		{
			
			// normalize to [0,1]
			double val =  (value-minVal)/(maxVal-minVal);
			
//			double x = 0.5d;
			double B = min((max((4d*(0.75-val)), 0)), 1d);
			double R  = min(max((4d*(val-0.25)), 0), 1d);
			double G= min(max((4d*Math.abs(val-0.5d)-1d), 0d), 1d);
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
		}
		else if (colorMapType==GREEN)
		{
			// normalize to [0,1]
			double val =  (value-minVal)/(maxVal-minVal);
			
			double B = 0;
			double R  = 0;
			double G= val;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
		}
			
		else if (colorMapType==GRAYS)
		{
			double tMin = (maxVal-minVal)/2.2f;
			if (value<tMin)
			{
				color[0] = 1;
				color[1] = 1;
				color[2] = 1;
				return;
			}
			
			
			double val1 =  (value-tMin)/(maxVal-tMin);
			
			double B = 1;
			double R  = 1-val1;
			double G= 1-val1;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
		}
	}
	
	
	
	static public void getColorValue(double value, double value2, double minVal, double maxVal, float[] color, int colorMapType)
	{
		
		double tMin = (maxVal-minVal)/2.2f;
		if (value<tMin)
		{
			color[0] = 1;
			color[1] = 1;
			color[2] = 1;
			return;
		}
		
		
		double val1 =  (value-tMin)/(maxVal-tMin);
		
		double B = 1;
		double R  = 1-val1;
		double G= 1-value2;
		color[0]  = (float)R;
		color[1] = (float)G;
		color[2] = (float)B;
		
	}
	
	
	
	static public double min(double v1, double v2)
	{
		if (v1>v2)
			return v2;
		return v1;
	}
	static public double max(double v1, double v2)
	{
		if (v1<v2)
			return v2;
		return v1;
	}
	
	
	
	
	
	
	
}

