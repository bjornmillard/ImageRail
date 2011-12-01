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


public class ColorMaps
{
	//ColorMap Types

	static final public int FIRE = 0;
	static final public int JET = 1;
	static final public int RGB = 2;
	static final public int ACID = 3;
	static final public int BEACH = 4;
	static final public int GRAYS = 5;
	static final public int GREEN = 6;
	static final public int BLUE = 7;
	static final public int RED = 8;
	static final public int WHITEPURPLE = 9;
	static final public int WHITERED = 10;
	// static final public int SMOKE = 9;
	// static final public int POSNEG = -1;

	
//	static final public int ROYGBIV = 6;
//	static final public int SMOKE = 10;
	
	static final public String[] colorMapNames = { "FIRE", "JET", "RGB",
			"ACID", "BEACH", "GRAYS", "GREEN", "BLUE", "RED" };// ,
																		// "SMOKE"};//
																		// "ROYGBIV"};
	
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
			
			//green
			float green = 0;
			green = 1-((float)Math.pow(coeff*(absVal-0.5f),2));
			
			//red
			float red = 0;
			red = 1-((float)Math.pow(coeff*(absVal-1f),2));
			
			
//			System.out.println(value);
			color[0]=  red; color[1] = green; color[2] = blue; color[3] = 1f;
			
		}
		else if (colorMapType==RGB)
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
			
			if (red<0)
				red = 0;
			if (blue<0)
				blue = 0;
			if (green<0)
				green = 0;
			color[0]=  red; color[1] = green; color[2] = blue;  color[3] = 1f;
			
		}
		else if (colorMapType==JET)
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
			
			color[0]=  red; color[1] = green; color[2] = blue;  color[3] = 1f;
			
//			System.out.println("COlor[0]: "+red);
//			System.out.println("COlor[1]: "+green);
//			System.out.println("COlor[2]: "+blue);
		}
			
		else if (colorMapType==FIRE)
		{
			double val =  (value-minVal)/(maxVal-minVal);
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
			color[3] = 1f;
		}
		else if (colorMapType==ACID)
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
			color[3] = 1f;
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
			color[3] = 1f;
		}
		else if (colorMapType==BLUE)
		{
			// normalize to [0,1]
			double val =  (value-minVal)/(maxVal-minVal);
			
			double B = val;
			double R  = 0;
			double G= 0;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
			color[3] = 1f;
		}
		else if (colorMapType==RED)
		{
			// normalize to [0,1]
			double val =  (value-minVal)/(maxVal-minVal);
			
			double B = 0;
			double R  = val;
			double G= 0;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
			color[3] = 1f;
		}
		else if (colorMapType==GRAYS)
		{
			// normalize to [0,1]
			double val =  (value-minVal)/(maxVal-minVal);
			
			double B = val;
			double R  = val;
			double G= val;
			color[0]  = (float)R;
			color[1] = (float)G;
			color[2] = (float)B;
			color[3] = 1f;
		}
		// else if (colorMapType==POSNEG)
		// {
		// double R = 0;
		// double G = 0;
		// double B = 0;
		//			
		// if (value<0)
		// {
		// B = value/minVal;
		// }
		//				
		// else if (value>=0)
		// {
		// R = value/maxVal;
		// }
		//			
		// if (maxVal<=0 ) //all values are negative
		// {
		// // normalize to [0,1]
		// double val = Math.abs((value-minVal)/(maxVal-minVal));
		//				
		// B = val;
		// R = 0;
		// G= 0;
		//				
		// }
		// else if (minVal>=0 ) // all values are positive
		// {
		// // normalize to [0,1]
		// double val = (value-minVal)/(maxVal-minVal);
		//				
		// B = 0;
		// R = val;
		// G= 0;
		//				
		// }
		// else if (minVal<0 && maxVal>0) // all values are positive
		// {
		// if (value<0)
		// {
		// System.out.println("val: "+value + "   minVal: "+minVal);
		// // normalize to [0,1]
		// double val = value/minVal;
		// B = val;
		// R = 0;
		// G= 0;
		// }
		// else
		// {
		// // normalize to [0,1]
		// double val = value/(maxVal);
		// B = 0;
		// R = val;
		// G= 0;
		//					
		// }
		// }
		// System.out.println("B: "+B);
		//			
		//			
		// color[0] = (float)R;
		// color[1] = (float)G;
		// color[2] = (float)B;
		// color[3] = 1f;
		// }
		// else if (colorMapType==SMOKE)
		// {
		// // normalize to [0,1]
		// double val = (value-minVal)/(maxVal-minVal);
		// val = (1f-val)/2f;
		// color[0] = (float)val;
		// color[1] = (float)val;
		// color[2] = (float)val;
		// color[3] = 0.5f+(float)(1-val)/2f;
		// }
 else if (colorMapType == WHITEPURPLE) {
			double tMin = (maxVal - minVal) / 1.4f;// 2.7f;
			if (value < tMin) {
				color[0] = 1;
				color[1] = 1;
				color[2] = 1;
				return;
			}

			double val1 = (value - tMin) / (maxVal - tMin);

			double B = 1;
			double R = 1 - val1;
			double G = 1 - val1;

			color[0] = (float) R;
			color[1] = (float) G;
			color[2] = (float) B;
		}
 else if (colorMapType == WHITERED) {
			double tMin = (maxVal - minVal) / 2.7f;
			if (value < tMin) {
				color[0] = 1;
				color[1] = 1;
				color[2] = 1;
				return;
			}

			double val1 = (value - tMin) / (maxVal - tMin);

			double B = 1 - val1;
			double R = 1;
			double G = 1 - val1;
			color[0] = (float) R;
			color[1] = (float) G;
			color[2] = (float) B;
		}
		
		
		//		else if (colorMapType==ROYGBIV)
//		{
//			System.out.println("Roygbiv");
//			// normalize to [0,1]
//			double val =  (value-minVal)/(maxVal-minVal);
//			System.out.println("val: "+val);
//
//			//Violet
//			if (val>=0 && val < 0.143f)
//			{
//				Color c =Color.blue.darker();
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Indigo
//			else if (val>=(0.143f*1f) && val < (0.143f*2f))
//			{
//				Color c =Color.blue.brighter();
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Blue
//			else if (val>=(0.143f*2f) && val < (0.143f*3f))
//			{
//				Color c =Color.blue;
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Green
//			else if (val>=(0.143f*3f) && val < (0.143f*4f))
//			{
//				Color c =Color.green;
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Yellow
//			else if (val>=(0.143f*4f) && val < (0.143f*5f))
//			{
//				Color c =Color.yellow;
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Orange
//			else if (val>=(0.143f*5f) && val < (0.143f*6f))
//			{
//				Color c =Color.orange;
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//				//Red
//			else if (val>=(0.143f*6f) && val <= 1f)
//			{
//				Color c =Color.red;
//				color[0] = c.getBlue();
//				color[1] = c.getGreen();
//				color[2] = c.getRed();
//				color[3] = 1f;
//			}
//			System.out.println("colors: ");
//			System.out.println(""+color[0]);
//			System.out.println(""+color[1]);
//			System.out.println(""+color[2]);
//			System.out.println(""+color[3]);
//		}
		
		
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

