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

import java.util.ArrayList;
import java.util.Arrays;

public class SpatialFilter
{
	static public boolean FoundOne;
	
	public SpatialFilter()
	{
		
	}
	
	static public int[][][] nonLinear_Averaging(int[][][] inputRaster)
	{
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int halfKernal = 5;
		for (int r = halfKernal; r < nRows-halfKernal; r++)
			for (int c = halfKernal; c < nCols-halfKernal; c++)
			{
				//for this pixel, we calculate the local 11x11 pixel average, but w/o the zero points
				int sum = 0;
				int num = 0;
				for (int n = r-halfKernal; n < r+halfKernal; n++)
					for (int k = c-halfKernal; k < c+halfKernal; k++)
					{
						
						int val = tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						
						if (val>10)
						{
							sum+=val;
							num++;
						}
					}
				
				//calculating the local average
				float avg = 0;
				if (num>0)
				{
					avg = (float)sum/(float)num;
					tools.ImageTools.setIntensityToPixel(temp[r][c], (int)avg);
				}
				else
					tools.ImageTools.setPixelBlack(temp[r][c]);
				
			}
		
		return temp;
	}
	
	
	static public int[][][] stdevWindow(int[][][] inputRaster, int width,
			int index) {
		int kernalWidth = width;
		int halfKernalWidth = (int) (((float) kernalWidth - 1f) / 2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int halfway = (int) ((float) (width * width) / 2f);
		float sum = 0;
		for (int r = halfKernalWidth; r < nRows - halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols - halfKernalWidth; c++) {
				int counter = 0;
				for (int n = r - halfKernalWidth; n < r + halfKernalWidth; n++)
					for (int k = c - halfKernalWidth; k < c + halfKernalWidth; k++) {
						sum = tools.ImageTools
								.getPixelIntensity(inputRaster[n][k]);
						counter++;
					}

				float mean = (float) sum / (float) counter;
				sum = 0;
				for (int n = r - halfKernalWidth; n < r + halfKernalWidth; n++) {
					for (int k = c - halfKernalWidth; k < c + halfKernalWidth; k++) {
						sum += Math.pow((tools.ImageTools
								.getPixelIntensity(inputRaster[n][k]) - mean),
								2);
					}
				}
				double stdev = Math.sqrt(1f / (float) counter * sum);
				temp[r][c][index] = (int) (stdev * 100);
			}
		return temp;
	}
	
	static public int[][][] erosion(int[][][] inputRaster)
	{
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int thresh = 80;
		//if one of this guys neighbors is off... then this guy is off too
		for (int r = 1; r < nRows-1; r++)
			for (int c = 1; c < nCols-1; c++)
			{
				//checking his neighbors
				if (tools.ImageTools.getPixelIntensity(inputRaster[r-1][c])<thresh)
					tools.ImageTools.setPixelBlack(temp[r][c]);
				else if (tools.ImageTools.getPixelIntensity(inputRaster[r][c-1])<thresh)
					tools.ImageTools.setPixelBlack(temp[r][c]);
				else if (tools.ImageTools.getPixelIntensity(inputRaster[r][c+1])<thresh)
					tools.ImageTools.setPixelBlack(temp[r][c]);
				else if(tools.ImageTools.getPixelIntensity(inputRaster[r+1][c])<thresh)
					tools.ImageTools.setPixelBlack(temp[r][c]);
			}
		
		
		return temp;
	}
	
	static public float[][][] erode(float[][][] inputRaster, Pixel[] pixels,
			int index)
	{
		final int MARKER = 10;
		int height = inputRaster.length;
		int width = inputRaster[0].length;
		float[][][] temp = tools.ImageTools.copyRaster(inputRaster);  //TODO - may want to not create this
		float thresh = 0;
		boolean change = false;
		//if one of this guys' neighbors is off... then this guy is off too
		for (int r = 1; r < height - 1; r++)
			for (int c = 1; c < width - 1; c++)
			{
				//checking his neighbors
				if(inputRaster[r][c][index]>thresh && inputRaster[r][c][index]<MARKER)
				{
					int numNeighborsOff = getNumNeighborsOff(r,c, inputRaster, index, thresh);
					if (numNeighborsOff>0)
					{
						ArrayList<Pixel> path = new ArrayList<Pixel>();
						FoundOne = false;
						hasAtLeastOneNeighborLandlocked(
								pixels[r + (c * height)], pixels, inputRaster,
								index, thresh, path);
						if(FoundOne)
						{
							change = true;
							temp[r][c][index] = 0;
						}
						else
							temp[r][c][index] = MARKER;
						//resetting the path IDs
						int size = path.size();
						for (int i = 0; i < size; i++)
								((Pixel)path.get(i)).setID(-1);
					}
				}
			}
		if (!change)
			return null;
		return temp;
	}
	
	static public void hasAtLeastOneNeighborLandlocked(Pixel pix,
			Pixel[] pixels, float[][][] raster, int index, float threshold,
			ArrayList<Pixel> path)
	{
		if (pix.getID()!=-1)
			return;
		if (FoundOne)
			return;
		
		path.add(pix);
		pix.setID(1);
		
		
		Pixel[] neighs = pix.getNeighbors(pixels);
		int len = neighs.length;
		if (len!=8)
			return;
		
		
		int counter = 0;
		for (int i = 0; i < len; i++)
		{
			if(raster[neighs[i].getRow()][neighs[i].getColumn()][index]>threshold)
				counter++;
			else
				break;
		}
		
		if (counter==len)
		{
			FoundOne = true;
			return;
		}
		else
			for (int i = 0; i < len; i++) //checking this pixels neighbors to see if "their" neighbors are landlocked
				if(raster[neighs[i].getRow()][neighs[i].getColumn()][index]>threshold)
					hasAtLeastOneNeighborLandlocked(neighs[i], pixels, raster, index, threshold, path);
	}
	
	static public int getNumNeighborsOff(int r, int c, float[][][] inputRaster, int index, float thresh)
	{
		int counter = 0;
		if (inputRaster[r-1][c][index]==0)
			counter++;
		if (inputRaster[r][c-1][index]==0)
			counter++;
		if (inputRaster[r][c+1][index]==0)
			counter++;
		if(inputRaster[r+1][c][index]==0)
			counter++;
		
		if (inputRaster[r-1][c-1][index]==0)
			counter++;
		if (inputRaster[r+1][c-1][index]==0)
			counter++;
		if (inputRaster[r-1][c+1][index]==0)
			counter++;
		if(inputRaster[r+1][c+1][index]==0)
			counter++;
		
		return counter;
	}
	
	static public float[][][] findUltimateErodedPoints(float[][][] inputRaster,
			Pixel[] pixels, int index)
	{
		
		//TODO - make copy of raster in temp, that is 2x high, then alternate between the two instead of making new raster each erosion
		float[][][] temp = inputRaster;
		int count = 0;
		while(true)
		{
			count++;
			System.out.println("count: "+count);
			if (count%6==0)
				tools.ImageTools.displayRaster(temp);
			
			float[][][] temp2 = erode(temp, pixels, index);
			if (temp2 == null)
				break;
			else
				temp = temp2;
		}
		return temp;
	}
	
	static public int[][][] dialate(int[][][] inputRaster)
	{
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int thresh = 80;
		//if one of this guys neighbors is on... then this guy is on too
		for (int r = 1; r < nRows-1; r++)
			for (int c = 1; c < nCols-1; c++)
			{
				//checking his neighbors
				if (tools.ImageTools.getPixelIntensity(inputRaster[r-1][c])>thresh)
					tools.ImageTools.setPixelWhite(temp[r][c]);
				else if (tools.ImageTools.getPixelIntensity(inputRaster[r][c-1])>thresh)
					tools.ImageTools.setPixelWhite(temp[r][c]);
				else if (tools.ImageTools.getPixelIntensity(inputRaster[r][c+1])>thresh)
					tools.ImageTools.setPixelWhite(temp[r][c]);
				else if(tools.ImageTools.getPixelIntensity(inputRaster[r+1][c])>thresh)
					tools.ImageTools.setPixelWhite(temp[r][c]);
			}
		
		
		return temp;
	}
	
	
	static public int[][][] addSaltAndPepper(int[][][] inputRaster, float frequency)
	{
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int numPix = nRows*nCols;
		int numSnP = (int)(frequency*numPix);
		
		for (int i = 0; i < numSnP; i++)
		{
			int row = (int)(Math.random()*nRows);
			int col = (int)(Math.random()*nCols);
			if (row>=nRows)
				row = nRows-1;
			if (col>=nCols)
				col = nCols-1;
			if (Math.random()>0.5) //add salt
				tools.ImageTools.setIntensityToPixel(inputRaster[row][col], tools.ImageTools.Pixel_maxIntensity);
			else //add pepper
				tools.ImageTools.setIntensityToPixel(inputRaster[row][col], 0);
		}
		
		return inputRaster;
	}
	
	static public float[][] linearFilter(float[][] inputRaster, float[][] kernal)
	{
		int kernalWidth = kernal.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		float[][] temp = new float[nRows][nCols];
		
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				float sum = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth+1; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth+1; k++)
					{
						float w = kernal[kcol][krow];
						sum += w * inputRaster[n][k];
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
				temp[r][c] = sum;
			}
		
		return temp;
		
	}
	
	static public int[][] linearFilter(int[][] inputRaster, float[][] kernal)
	{
		int kernalWidth = kernal.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][] temp = new int[nRows][nCols];// tools.ImageTools.copyRaster(inputRaster);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				int sum = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth+1; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth+1; k++)
					{
						float w = kernal[kcol][krow];
						sum += w * (inputRaster[n][k]);
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
				if (sum < min)
					min = sum;
				else if (sum > max)
					max = sum;
				
				temp[r][c] = sum;
			}

		return temp;
	}
	
	static public int[][][] nonlinear_medianFilter(int[][][] inputRaster, int width)
	{
		int kernalWidth = width;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int halfway = (int)((float)(width*width)/2f);
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				int[] vals = new int[width*width];
				int counter = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth; k++)
					{
						vals[counter] = tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						counter++;
					}
				}
				//arranging the values and picking the halfway value
				Arrays.sort(vals);
//				System.out.println(vals[halfway]);
				tools.ImageTools.setIntensityToPixel(temp[r][c], vals[halfway]);
			}
		return temp;
	}
	
	static public int[][][] valleyDetector_erode(int[][][] inputRaster, float[][] kernal_h, float[][] kernal_v, float[][] kernal_d1,float[][] kernal_d2)
	{
		int kernalWidth = kernal_v.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				int sum_h = 0;
				int sum_v = 0;
				int sum_d1 = 0;
				int sum_d2 = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth+1; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth+1; k++)
					{
						float w = kernal_h[kcol][krow];
						sum_h+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_v[kcol][krow];
						sum_v+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d1[kcol][krow];
						sum_d1+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d2[kcol][krow];
						sum_d2+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						
						
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
				sum_h = Math.abs(sum_h);
				sum_v = Math.abs(sum_v);
				sum_d1 = Math.abs(sum_d1);
				sum_d2 = Math.abs(sum_d2);
				
				int sum = sum_h+sum_v+sum_d1+sum_d2;
				
				if (sum < min)
					min = sum;
				else if (sum > max)
					max = sum;
				
				if (sum>300)
					tools.ImageTools.setPixelBlack(temp[r][c]);
				
//				tools.ImageTools.setIntensityToPixel(temp[r][c], sum);
			}
		
		
		
		//now scaling to not have negative numbers
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
			{
				int val = tools.ImageTools.getPixelIntensity(temp[r][c]);
				val = val-min;
				val = (int)((float)val*(float)tools.ImageTools.Pixel_maxIntensity/(float)max);
				if (val < 0)
					val = 0;
				if (val>tools.ImageTools.Pixel_maxIntensity)
					val = tools.ImageTools.Pixel_maxIntensity;
				tools.ImageTools.setIntensityToPixel(temp[r][c], val);
			}
		
		return temp;
	}
	
	static public int[][] sobelEdgeDetector(int[][][] inputRaster,
			int channelIndex, float[][] kernal_h, float[][] kernal_v,
			float[][] kernal_d1, float[][] kernal_d2) {
		int kernalWidth = kernal_v.length;
		int halfKernalWidth = (int) (((float) kernalWidth - 1f) / 2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][] temp = tools.ImageTools.copyRaster_oneChannelOnly(inputRaster,
				channelIndex);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = halfKernalWidth; r < nRows - halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols - halfKernalWidth; c++) {
				int sum_h = 0;
				int sum_v = 0;
				int sum_d1 = 0;
				int sum_d2 = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r - halfKernalWidth; n < r + halfKernalWidth + 1; n++) {
					for (int k = c - halfKernalWidth; k < c + halfKernalWidth
							+ 1; k++) {
						float w = kernal_h[kcol][krow];
						sum_h += w
 * inputRaster[n][k][channelIndex];
						w = kernal_v[kcol][krow];
						sum_v += w
 * inputRaster[n][k][channelIndex];
						w = kernal_d1[kcol][krow];
						sum_d1 += w
 * inputRaster[n][k][channelIndex];
						w = kernal_d2[kcol][krow];
						sum_d2 += w
 * inputRaster[n][k][channelIndex];

						kcol++;
					}
					kcol = 0;
					krow++;
				}

				sum_h = Math.abs(sum_h);
				sum_v = Math.abs(sum_v);
				sum_d1 = Math.abs(sum_d1);
				sum_d2 = Math.abs(sum_d2);

				int sum = sum_h + sum_v + sum_d1 + sum_d2;

				if (sum < min)
					min = sum;
				else if (sum > max)
					max = sum;

				temp[r][c] = sum;
			}

		// now scaling to not have negative numbers
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++) {
				int val = temp[r][c];
				val = val - min;
				val = (int) ((float) val
						* (float) tools.ImageTools.Pixel_maxIntensity / (float) max);
				if (val < 0)
					val = 0;
				if (val > tools.ImageTools.Pixel_maxIntensity)
					val = tools.ImageTools.Pixel_maxIntensity;
				temp[r][c] = val;
			}

		return temp;
	}

	static public int[][][] sobelEdgeDetector(int[][][] inputRaster, float[][] kernal_h, float[][] kernal_v, float[][] kernal_d1,float[][] kernal_d2)
	{
		int kernalWidth = kernal_v.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				int sum_h = 0;
				int sum_v = 0;
				int sum_d1 = 0;
				int sum_d2 = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth+1; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth+1; k++)
					{
						float w = kernal_h[kcol][krow];
						sum_h+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_v[kcol][krow];
						sum_v+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d1[kcol][krow];
						sum_d1+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d2[kcol][krow];
						sum_d2+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						
						
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
				sum_h = Math.abs(sum_h);
				sum_v = Math.abs(sum_v);
				sum_d1 = Math.abs(sum_d1);
				sum_d2 = Math.abs(sum_d2);
				
				int sum = sum_h+sum_v+sum_d1+sum_d2;
				
				if (sum < min)
					min = sum;
				else if (sum > max)
					max = sum;
				
				tools.ImageTools.setIntensityToPixel(temp[r][c], sum);
			}
		
		
		
		//now scaling to not have negative numbers
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
			{
				int val = tools.ImageTools.getPixelIntensity(temp[r][c]);
				val = val-min;
				val = (int)((float)val*(float)tools.ImageTools.Pixel_maxIntensity/(float)max);
				if (val < 0)
					val = 0;
				if (val>tools.ImageTools.Pixel_maxIntensity)
					val = tools.ImageTools.Pixel_maxIntensity;
				tools.ImageTools.setIntensityToPixel(temp[r][c], val);
			}
		
		return temp;
	}
	
	/** If there is an edge, this method replaces it with a White pixel, otherwise, Black pixel
	 * @author BLM*/
	static public int[][][] sobelEdgeDetector_binary(int[][][] inputRaster,
			float[][] kernal_h, float[][] kernal_v, float[][] kernal_d1,
			float[][] kernal_d2, double threshold)
	{
		int kernalWidth = kernal_v.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = halfKernalWidth; r < nRows-halfKernalWidth; r++)
			for (int c = halfKernalWidth; c < nCols-halfKernalWidth; c++)
			{
				int sum_h = 0;
				int sum_v = 0;
				int sum_d1 = 0;
				int sum_d2 = 0;
				int krow = 0;
				int kcol = 0;
				for (int n = r-halfKernalWidth; n < r+halfKernalWidth+1; n++)
				{
					for (int k = c-halfKernalWidth; k < c+halfKernalWidth+1; k++)
					{
						float w = kernal_h[kcol][krow];
						sum_h+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_v[kcol][krow];
						sum_v+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d1[kcol][krow];
						sum_d1+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						w = kernal_d2[kcol][krow];
						sum_d2+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						
						
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
				sum_h = Math.abs(sum_h);
				sum_v = Math.abs(sum_v);
				sum_d1 = Math.abs(sum_d1);
				sum_d2 = Math.abs(sum_d2);
				
				int sum = sum_h+sum_v+sum_d1+sum_d2;
				
				if (sum < min)
					min = sum;
				else if (sum > max)
					max = sum;
				

				//either turn it on or off based on edge thresholding
				if (sum>threshold)
					tools.ImageTools.setIntensityToPixel(temp[r][c], 3000);
				else
					tools.ImageTools.setPixelBlack(temp[r][c]);
				
				
			}
		
		
		
		//now scaling to not have negative numbers
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
			{
				int val = tools.ImageTools.getPixelIntensity(temp[r][c]);
				val = val-min;
				val = (int)((float)val*(float)tools.ImageTools.Pixel_maxIntensity/(float)max);
				if (val < 0)
					val = 0;
				if (val>tools.ImageTools.Pixel_maxIntensity)
					val = tools.ImageTools.Pixel_maxIntensity;
				tools.ImageTools.setIntensityToPixel(temp[r][c], val);
			}
		
		return temp;
	}
	

	
	/* dt of 2d function using squared distance */
	static public float[][] distanceTransform(float[][] im)
	{
		int rows = im.length;
		int cols = im[0].length;
		
		float[] f = null;
		if(rows>cols)
			f = new float[rows];
		else
			f = new float[cols];
		
		// transform along columns
		for (int r = 0; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
				f[c] = im[r][c];
			
			float[] d = dt(f, cols);
			for (int c = 0; c < cols; c++)
				im[r][c] = d[c];
			d = null;
		}
		
		// transform along rows
		for (int c = 0; c < cols; c++)
		{
			for (int r = 0; r < rows; r++)
				f[r] = im[r][c];
			
			float[] d = dt(f, rows);
			for (int r = 0; r < rows; r++)
				im[r][c] = d[r];
		}
		f = null;
		
		
		return im;
	}

	
	/* dt of 1d function using squared distance */
	static float[] dt(float[] f, int n)
	{
		float[] d = new float[n];
		int[] v = new int[n];
		float[] z = new float[n+1];
		int k = 0;
		v[0] = 0;
		z[0] = -1e20f;//-1000000000f;
		z[1] = 1e20f;//1000000000f;
		
		for (int q = 1; q <= n-1; q++)
		{
			float s  = ((f[q]+(float)q*(float)q)-(f[v[k]]+(float)v[k]*(float)v[k]))/(2f*(float)q-2f*(float)v[k]);
			while (s <= z[k])
			{
				k--;
				s  = (( f[q]+(float)q*(float)q)-(f[v[k]]+(float)v[k]*(float)v[k]))/(2f*(float)q-2f*(float)v[k]);
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k+1] = 1e20f;
		}
		
		k = 0;
		for (int q = 0; q <= n-1; q++)
		{
			while (z[k+1] < q)
				k++;
			d[q] = ((float)q-(float)v[k])*((float)q-(float)v[k]) + f[v[k]];
		}
		
		v = null;
		z = null;
		return d;
	}
	
}
