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

import java.awt.Color;

/** Allows for random color generation
 * @author BLM*/
public class ColorRama
{
	//ColorMap Types
	static final public int RGB_1 = 0;
	static final public int RGB_2 = 1;
	static final public int BEACH = 2;
	static final public int RAINBOW = 3;
	static final public int FIRE = 4;
	static final public int GREEN_RED = 5;
	static final public int RED = 6;
	static final public int GREEN = 7;
	static final public Color Gray_veryDark = new Color(0.2f, 0.2f, 0.2f);
	static final public Color Gray_veryLight1 = new Color(0.9f, 0.9f, 0.9f);
	static final public Color Gray_veryLight2 = new Color(0.7f, 0.7f, 0.7f);
	static final public Color Yellow_Light = new Color(250,250,100);

	
//	static public Color getColor(int j, float[] color)
//	{
//		if (j<23)
//		{
//			Color[] colors = new Color[23];
//			colors[0] = Color.blue;
//			colors[1] = Color.cyan;
//			colors[2] = Color.green;
//			colors[3] = Color.magenta;
//			colors[4] = Color.orange;
//			colors[5] = Color.pink;
//			colors[6] = Color.red;
//			colors[7] = Color.gray;
//			colors[8] = Color.yellow;
//			colors[9] = Color.getHSBColor(0.1f, 0.5f,0.2f);
//			colors[10] = Color.getHSBColor(0.2f, 0.6f,0.3f);
//			colors[11] = Color.getHSBColor(0.3f, 0.7f,0.4f);
//			colors[12] = Color.getHSBColor(0.4f, 0.8f,0.5f);
//			colors[13] = Color.getHSBColor(0.5f, 0.9f,0.6f);
//			colors[14] = Color.getHSBColor(0.6f, 0.1f,0.7f);
//			colors[15] = Color.getHSBColor(0.7f, 0.9f,0.8f);
//			colors[16] = Color.getHSBColor(0.8f, 0.3f,0.6f);
//			colors[17] = Color.getHSBColor(0.9f, 0.6f,0.4f);
//			colors[18] = Color.getHSBColor(0.1f, 0.5f,0.2f);
//			colors[19] = Color.getHSBColor(0.2f, 0.6f,0.6f);
//			colors[20] = Color.getHSBColor(0.3f, 0.4f,0.8f);
//			colors[21] = Color.getHSBColor(0.4f, 0.3f,0.1f);
//			colors[22] = Color.getHSBColor(0.5f, 0.9f,0.9f);
//			return colors[j];
//		}
//		return getPseudoRandomColor();
//	}
	
	static public Color getColor(int j)
	{
//		Color color  = Color.cyan;
//		for (int i = 0; i < j; i++)
//		{
//			color = color.darker();
//		}
		
//		return color;
		
		
		if (j<24)
		{
			Color[] colors = new Color[24];
			colors[0] = Color.blue;
			colors[1] = Color.magenta;
			colors[2] = Color.green;
			colors[3] = Color.cyan;
			colors[4] = Color.orange;
			colors[5] = Color.pink;
			colors[6] = Color.red;
			colors[7] = Color.gray;
			colors[8] = Color.yellow;
			colors[9] = Color.getHSBColor(0.1f, 0.5f,0.8f);
			colors[10] = Color.getHSBColor(0.8f, 0.1f,0.5f);
			colors[11] = Color.getHSBColor(0.5f, 0.8f,0.1f);
			colors[12] = Color.getHSBColor(0.7f, 0.7f,0.3f);
			colors[13] = Color.getHSBColor(0.7f, 0.3f,0.7f);
			colors[14] = Color.getHSBColor(0.6f, 0.1f,0.7f);
			colors[15] = Color.getHSBColor(0.7f, 0.9f,0.8f);
			colors[16] = Color.getHSBColor(0.8f, 0.3f,0.6f);
			colors[17] = Color.getHSBColor(0.9f, 0.6f,0.4f);
			colors[18] = Color.getHSBColor(0.1f, 0.5f,0.2f);
			colors[19] = Color.getHSBColor(0.2f, 0.6f,0.6f);
			colors[20] = Color.getHSBColor(0.3f, 0.4f,0.8f);
			colors[21] = Color.getHSBColor(0.4f, 0.3f,0.1f);
			colors[22] = Color.getHSBColor(0.5f, 0.9f,0.9f);
			colors[23] = Color.getHSBColor(0.6f, 0.3f,0.3f);
			return colors[j];
		}
		return getPseudoRandomColor();
	}
	
	static public int[] getRandomRGB()
	{
		int[] rgb = new int[3];
		for (int i =0; i < 3; i++)
			rgb[i] = (int)(255d*Math.random());
		return rgb;
	}
	
	/** Returns a random color that tries not to be black/white/gray*/
	static public Color getPseudoRandomColor()
	{
		float red = 0;
		float green = 0;
		float blue = 0;
		
		//keep trying to make a color until it is not close to zero total color==black
		while ((red+green+blue)<0.3)
		{
			double rand = Math.random();
			if (rand<0.3)
			{
				blue = (float)Math.random()*0.3f;
				red = (float)Math.random();
				green = (float)Math.random()*0.6f;
			}
			else if (rand>=0.3 && rand<0.6)
			{
				red = (float)Math.random()*0.4f;
				blue = (float)Math.random();
				green = (float)Math.random()*0.9f;
			}
			else
			{
				red = (float)Math.random()*0.9f;
				blue = (float)Math.random()*0.8f;
				green = (float)Math.random();
			}
		}
		return new Color(red,green,blue);
	}
	/** This allows for user to color nodes with the specification that a prechosen
	 *  node should influence if the color of this cell state is more Redish, or more greenish, or
	 * yellow of both of those nodes are simultaneously on. This method returns a psuedo-
	 * random color weighted red if the red node value is in an on config, and the green is off;
	 * a green color if the weight green is on and red is off, and yellowish color if both are on.
	 * @author BLM */
	static public Color getRedGreenRandomColor(boolean red, boolean green,int numNodes, int numNodesInHighState)
	{
		float r = 0;
		float b = 0;
		float g = 0;
		
		float fractionOn = (numNodesInHighState-2)/(numNodes-2);
		if (red&&green)
		{
			/** if nodeRed is in the high state and nodeGreen is also in the high state, then b=0 and
			 r=g= (1 + f)/2
			 This will give every such node a yellow color with white mixed
			 in in proportion to how many other nodes are high.
			 break;*/
			g=(1+2*fractionOn)/3f;
			r=(1+2*fractionOn)/3f;
		}
		else if (red&&!green)
		{
			/*** if nr is in the high state and ng is in the low state, then
			 r = (1 + f)/2
			 g = b = (.5+f)/2
			 Note this will give each node a reddish hue, with white mixed in in
			 in proportion to how many other nodes are high. r will be between
			 .5 and 1.0.  g and b will be between .250 and 0.75, and g and b will
			 be 0.25 less than r.*/
			r = (1 + 2*fractionOn)/3f;
			g =  (0.5f+2*fractionOn)/3f;
			b = (0.5f+2*fractionOn)/3f;
		}
		else if (!red&&green)
		{
			/**  if ng is in the high state and nr is in the low state, then
			 g = (1 + f)/2
			 r = b = (.5+f)/2
			 Note this will give each node a greenish hue, with white mixed in in
			 in proportion to how many other nodes are high. g will be between
			 .5 and 1.0.  g and b will be between .250 and 0.75, and r and b will
			 be 0.25 less than g.*/
			g = (1 + 2*fractionOn)/3f;
			r = (0.5f+2*fractionOn)/3f;
			b = (0.5f+2*fractionOn)/3f;
		}
		else//set blue to value 1.0 with random green and red values less than 0.5
		{
			/**The only possibility left is that neither nr nor ng is in the high state. Then set
			 b = (1+f)/2
			 g =r = (.5+f)/2
			 Note this will give each node a bluish hue, with white mixed in in
			 in proportion to how many nodes are high. b will be between
			 .5 and 1.0.  g and r will be between .250 and 0.75, and g and r will
			 be 0.25 less than b.*/
			b = (1+2*fractionOn)/3f;
			g = (0.5f+2*fractionOn)/3f;
			r = (0.5f+2*fractionOn)/3f;
		}
		
		return new Color(r,g,b) ;
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
			
//			System.out.println(val);
			
		}
		else if (colorMapType==GREEN_RED)
		{
			// normalize to [0,1]
			double normVal = Math.abs(maxVal);
			if (Math.abs(minVal)>Math.abs(maxVal))
				normVal = Math.abs(minVal);
			
			if (value>0)
			{
				double val =  (value)/normVal;
				double B = 0;
				double R  = 0;
				double G= val;
				color[0]  = (float)R;
				color[1] = (float)G;
				color[2] = (float)B;
			}
			else
			{
				double val =  (Math.abs(value))/normVal;
				double B = 0;
				double R  = val;
				double G= 0;
				color[0]  = (float)R;
				color[1] = (float)G;
				color[2] = (float)B;
			}
		}
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

