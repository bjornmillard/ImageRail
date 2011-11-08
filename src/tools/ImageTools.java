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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import models.Model_ParameterSet;
import models.Model_Plate;
import models.Model_Well;
import segmentedobject.CellCoordinates;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

public class ImageTools
{
	// static final public int Pixel_maxIntensity = 765;
	static final public int Pixel_maxIntensity = 100;

	/**
	 * Method getWellNamesOfImagesPresent
	 *
	 * @param    maskImageDirectory  a  File
	 *
	 * @return   a  String[]
	 */
	static public String getFilePrefix_woChannel(File file)
	{
		
		if (file.getName().indexOf(".tif")>0 || file.getName().indexOf(".TIF")>0 || file.getName().indexOf(".tiff")>0 || file.getName().indexOf(".TIFF")>0)
		{
			String name = file.getName();
			int index = name.indexOf("_w");
			return name.substring(0, index);
		}
		return null;
	}
	
	
	/**
	 * Takes a set of TIff image files and organizes them into collections of
	 * fields that go together
	 */
	static public ArrayList<File[]> getAllSetsOfCorresponsdingChanneledImageFiles(
			File[] channelImageFiles)
	{
		ArrayList<File[]> arr = new ArrayList<File[]>();
		int num = channelImageFiles.length;
		
		if (num==0)
			return arr;
		byte[] flags = new byte[num];
		for (int i=0; i <num; i++)
			flags[i] = 0;
		
		
		for (int i=0; i <num; i++)
		{
			if (flags[i]==0)
			{
				ArrayList<File> temp = new ArrayList<File>();
				File currFile = (File)channelImageFiles[i];
				temp.add(currFile);
				flags[i] = 1;
				String prefix = tools.ImageTools.getFilePrefix_woChannel(currFile);
				if (prefix!=null)
				{
					for (int j=0; j <num; j++)
					{
						if (flags[j]==0 && ((File)channelImageFiles[j]).getName().indexOf(prefix)>=0)
						{
							flags[j] = 1;
							temp.add(channelImageFiles[j]);
						}
					}
					

					int len = temp.size();
					File[] files = new File[len];
					for (int j=0; j < len; j++)
						files[j] = (File)temp.get(j);
						// Sort by image channel
						Arrays.sort(files, new tools.ImageFileChannelSorter());
					arr.add(files);
				}
			}
			
		}
		
		
		return arr;
	}
	
	static public ArrayList getAllSetsOfCorresponsdingChanneledImageFiles(ArrayList channelImageFiles)
	{
		ArrayList arr = new ArrayList();
		int num = channelImageFiles.size();
		if (num==0)
			return arr;
		byte[] flags = new byte[num];
		for (int i=0; i <num; i++)
			flags[i] = 0;
		
		
		for (int i=0; i <num; i++)
		{
			if (flags[i]==0)
			{
				ArrayList temp = new ArrayList();
				File currFile = (File)channelImageFiles.get(i);
				temp.add(currFile);
				flags[i] = 1;
				String prefix = tools.ImageTools.getFilePrefix_woChannel(currFile);
				if (prefix!=null)
				{
					for (int j=0; j <num; j++)
					{
						if (flags[j]==0 && ((File)channelImageFiles.get(j)).getName().indexOf(prefix)>=0)
						{
							flags[j] = 1;
							temp.add(channelImageFiles.get(j));
						}
					}
					
					
					
					Collections.sort(temp);
					int len = temp.size();
					File[] files = new File[len];
					for (int j=0; j < len; j++)
					{
						files[j] = (File)temp.get(j);
					}
					arr.add(files);
				}
			}
			
		}
		
		
		return arr;
	}
	
	/** Computes the mean intensity value for background pixels lower than the MainGUI.Thresh_Bkgd_Value level in all other
	 * channels and stores them in "backgorunds" array.
	 * @author BLM*/
	static public void computeBackgroundValues(int[][][] raster, float[] backgrounds, Model_ParameterSet pset)
	{
		for (int i = 0; i < backgrounds.length; i++)
			backgrounds[i] = 0;
		
		int height = raster.length;
		int width = raster[0].length;
		int numChannels = raster[0][0].length;
		
		int	bkgdCounter=0;
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (raster[r][c][pset.getParameter_int("Thresh_Cyt_ChannelIndex")] < pset
						.getParameter_float("Thresh_Bkgd_Value"))
				{
					for (int i=0; i < numChannels; i++)
						backgrounds[i]+=raster[r][c][i];
					bkgdCounter++;
				}
		
		if (bkgdCounter>0)
			for (int i=0; i < numChannels; i++)
				backgrounds[i]=backgrounds[i]/(float)bkgdCounter;
		else
			System.out.println("**** WARNING: This field does not have any background pixels -->  Will subtract 0 for background, but this will be inconsistent if other fields have a background >0");
		
//		System.out.println("bkgd: ");
//		for (int i = 0; i < backgrounds.length; i++)
//		{
//			System.out.println(""+backgrounds[i]);
//		}
		
	}
	
	
	
	static public String getWellNameOfImage(String name, String[] wellNames)
	{
		int len = wellNames.length;
		for (int i = 0; i < len; i++)
			if (name.indexOf(wellNames[i])>0)
				return wellNames[i];
		return null;
	}
	
	
	static public Model_Well getWellOfImage(File file, Model_Plate[][] thePlates)
	{
		int nr = thePlates.length;
		int nc = thePlates[0].length;
		for (int rr = 0; rr < nr; rr++)
			for (int cc = 0; cc < nc; cc++)
			{
				Model_Plate thePlate = thePlates[rr][cc];
				
				Model_Well[][] wells = thePlate.getWells();
				int rows = wells.length;
				int cols = wells[0].length;
				String prefix = null;
				
				for (int r = 0; r < rows; r++)
					for (int c = 0; c < cols; c++)
					{
						String wellName = wells[r][c].name;
						if (file.getName().indexOf("_"+wellName+"_")>=0)
							prefix = wellName;
					}
				if (prefix == null)
					return null;
				
				return thePlate.getWell(prefix);
			}
		
		return null;
	}
	
	static public Model_Well getWellOfImage(File file,Model_Plate thePlate)
	{
		Model_Well[][] wells = thePlate.getWells();
		int rows = wells.length;
		int cols = wells[0].length;
		String prefix = null;
		
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				String wellName = wells[r][c].name;
				if (file.getName().indexOf("_"+wellName+"_")>=0)
					prefix = wellName;
			}
		if (prefix == null)
			return null;
		
		return thePlate.getWell(prefix);
		
	}
	
	static public boolean ArrayListContainsPoint(Pixel p, ArrayList points)
	{
		int len = points.size();
		for (int i=0;i < len; i++)
		{
			Point po = (Point)points.get(i);
			if ((po.x - p.getColumn()==0) && (po.y - p.getRow() ==0))
				return true;
		}
		return false;
	}
	
	static public void displayRaster(int[][] theData) {
		int width = theData[0].length;
		int height = theData.length;
		float max = 0;
		// norm
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (theData[r][c] > max)
					max = theData[r][c];

		float[][] newData = new float[height][width];
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				newData[r][c] = (float) theData[r][c] / max;

		// Create a float data sample model.
		SampleModel sampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_FLOAT, width, height, 1);

		// Create a compatible ColorModel.
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);

		// Create a TiledImage using the float SampleModel.
		TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0,
				sampleModel, colorModel);

		float[] imageDataSingleArray = new float[width * height];
		int count = 0;
		// It is important to have the height/width order here !
		for (int h = 0; h < height; h++)
			for (int w = 0; w < width; w++)
				imageDataSingleArray[count++] = newData[h][w];

		// Create a Data Buffer from the values on the single image array.
		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
				width * height);

		// Create a WritableRaster.
		Raster raster = RasterFactory.createWritableRaster(sampleModel,
				dbuffer, new Point(0, 0));

		// Set the data of the tiled image to be the raster.
		tiledImage.setData(raster);

		JFrame window = new JFrame();
		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width,
				height);
		window.add(panel);

		window.pack();
		window.show();
	}

	static public float min(int[][] theData) {
		int width = theData[0].length;
		int height = theData.length;
		float min = Float.POSITIVE_INFINITY;
		// norm
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (theData[r][c] < min)
					min = theData[r][c];
		return min;
	}

	static public float max(int[][] theData) {
		int width = theData[0].length;
		int height = theData.length;
		float max = Float.NEGATIVE_INFINITY;
		// norm
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (theData[r][c] > max)
					max = theData[r][c];
		return max;
	}

	static public void displayRaster(int[][][] theData)
	{
		int width = theData[0].length;
		int height = theData.length;
		float max = 0;
		//norm
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				if(theData[r][c][0]>max)
					max = theData[r][c][0];
		
		float[][] newData = new float[height][width];
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				newData[r][c] = theData[r][c][0]/max;
		
		// Create a float data sample model.
		SampleModel sampleModel =
			RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,
												  width,
												  height,
												  1);
		
		// Create a compatible ColorModel.
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		
		// Create a TiledImage using the float SampleModel.
		TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,
											   sampleModel,
											   colorModel);
		
		
		float[] imageDataSingleArray = new float[width*height];
		int count = 0;
		// It is important to have the height/width order here !
		for(int h=0;h<height;h++)
			for(int w=0;w<width;w++)
				imageDataSingleArray[count++] = newData[h][w];
		
		// Create a Data Buffer from the values on the single image array.
		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
													  width*height);
		
		
		// Create a WritableRaster.
		Raster raster = RasterFactory.createWritableRaster(sampleModel,dbuffer,
														   new Point(0,0));
		
		// Set the data of the tiled image to be the raster.
		tiledImage.setData(raster);
		
		
		JFrame window = new JFrame();
		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
		window.add(panel);
		
		window.pack();
		window.show();
	}
	
	static public void displayRaster(float[][] theData)
	{
		int width = theData[0].length;
		int height = theData.length;
		float max = 0;
		//norm
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				if(theData[r][c]>max)
					max = theData[r][c];
		
		float[][] newData = new float[height][width];
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				newData[r][c] = theData[r][c]/max;
		
		// Create a float data sample model.
		SampleModel sampleModel =
			RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,
												  width,
												  height,
												  1);
		
		// Create a compatible ColorModel.
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		
		// Create a TiledImage using the float SampleModel.
		TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,
											   sampleModel,
											   colorModel);
		
		
		float[] imageDataSingleArray = new float[width*height];
		int count = 0;
		// It is important to have the height/width order here !
		for(int h=0;h<height;h++)
			for(int w=0;w<width;w++)
				imageDataSingleArray[count++] = newData[h][w];
		
		// Create a Data Buffer from the values on the single image array.
		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
													  width*height);
		
		
		// Create a WritableRaster.
		Raster raster = RasterFactory.createWritableRaster(sampleModel,dbuffer,
														   new Point(0,0));
		
		// Set the data of the tiled image to be the raster.
		tiledImage.setData(raster);
		
		
		JFrame window = new JFrame();
		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
		window.add(panel);
		
		window.pack();
		window.show();
	}
	
	static public void displayRaster(float[][][] theData)
	{
		int width = theData[0].length;
		int height = theData.length;
		float max = 0;
		//norm
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				if(theData[r][c][0]>max)
					max = theData[r][c][0];
		
		float[][] newData = new float[height][width];
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				newData[r][c] = theData[r][c][0]/max;
		
		// Create a float data sample model.
		SampleModel sampleModel =
			RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,
												  width,
												  height,
												  1);
		
		// Create a compatible ColorModel.
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		
		// Create a TiledImage using the float SampleModel.
		TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,
											   sampleModel,
											   colorModel);
		
		
		float[] imageDataSingleArray = new float[width*height];
		int count = 0;
		// It is important to have the height/width order here !
		for(int h=0;h<height;h++)
			for(int w=0;w<width;w++)
				imageDataSingleArray[count++] = newData[h][w];
		
		// Create a Data Buffer from the values on the single image array.
		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
													  width*height);
		
		
		// Create a WritableRaster.
		Raster raster = RasterFactory.createWritableRaster(sampleModel,dbuffer,
														   new Point(0,0));
		
		// Set the data of the tiled image to be the raster.
		tiledImage.setData(raster);
		
		
		JFrame window = new JFrame();
		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
		window.add(panel);
		
		window.pack();
		window.show();
	}
	
	static public RenderedImage getImageFromRaster(int[][][] theData)
	{
		int channel=0;
		int width = theData[0].length;
		int height = theData.length;
		
		//finding maxValue
		int maxVal = 1;
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				if (theData[r][c][channel]>maxVal)
					maxVal=theData[r][c][channel];
		
		float[][] newData = new float[height][width];
		for (int r = 0; r < height; r++)
			for (int c= 0; c < width; c++)
				newData[r][c] = theData[r][c][channel]/maxVal;
		
		// Create a float data sample model.
		SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,width,height,1);
		
		// Create a compatible ColorModel.
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		
		// Create a TiledImage using the float SampleModel.
		TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,sampleModel,colorModel);
		
		
		float[] imageDataSingleArray = new float[width*height];
		int count = 0;
		// It is important to have the height/width order here !
		for(int h=0;h<height;h++)
			for(int w=0;w<width;w++)
				imageDataSingleArray[count++] = newData[h][w];
		
		// Create a Data Buffer from the values on the single image array.
		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
													  width*height);
		
		
		// Create a WritableRaster.
		Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point(0,0));
		// Set the data of the tiled image to be the raster.
		tiledImage.setData(raster);
		return tiledImage;
	}
	
//	static public void displayRaster_withCells(int[][][] theData, Cell[] cells, float[] weights, Line2D.Double[] lines)
//	{
//		int width = theData[0].length;
//		int height = theData.length;
//		int numBands = theData[0][0].length;
//
//		//finding maxValue
//		int maxVal_0 = 1;
//		int maxVal_1 = 1;
//		int maxVal_2 = 1;
//		for (int r = 0; r < height; r++)
//			for (int c= 0; c < width; c++)
//			{
//				if (theData[r][c][0]>maxVal_0)
//					maxVal_0=theData[r][c][0];
//				if (theData[r][c][1]>maxVal_1)
//					maxVal_1=theData[r][c][1];
//				if (theData[r][c][2]>maxVal_2)
//					maxVal_2=theData[r][c][2];
//			}
//
//		float[] data = new float[width*height*3];
//		int count =0;
//
//
//
//		DataBufferFloat dbuffer = new DataBufferFloat(data, width*height*3);
//		SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 3);
//
//		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
//		WritableRaster raster = RasterFactory.createWritableRaster(sampleModel, new Point(0,0));
//		float[] pix = new float[3];
//		for (int h =0; h < height; h++)
//			for (int w =0; w < width; w++)
//			{
////				data[count+1] = 255;
//				pix[0] = weights[0]*(float)theData[h][w][0]/(float)maxVal_0;//maxVal_0*255;//(count%2 ==0) ? (byte)255: (byte) 0;
//				pix[1] = weights[1]*(float)theData[h][w][1]/(float)maxVal_1;///maxVal_1*255;
//				pix[2] = weights[2]*(float)theData[h][w][2]/(float)maxVal_2;///maxVal_2*255;//(count%2 ==0) ? (byte)255: (byte) 0;
//				raster.setPixel(w,h,pix);
//			}
//
//
//		TiledImage tiledImage = new TiledImage(0,0,width,height, 0,0, sampleModel, colorModel);
//
//
////		// Create a WritableRaster.
////		Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point(0,0));
//		// Set the data of the tiled image to be the raster.
//		tiledImage.setData(raster);
//
//		JFrame window = new JFrame();
////		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
//		DisplayJAIwithAnnotations pan = new DisplayJAIwithAnnotations(tiledImage);
//		pan.cells = cells;
//		pan.lines = lines;
//		window.add(pan);
//
//		window.pack();
//		window.show();
//	}
	
//	static public void displayRaster_withCells(int[][][] theData, Cell[] cells, float[] weights)
//	{
//		int width = theData[0].length;
//		int height = theData.length;
//		int numBands = theData[0][0].length;
//
//		//finding maxValue
//		int maxVal_0 = 1;
//		int maxVal_1 = 1;
//		int maxVal_2 = 1;
//		for (int r = 0; r < height; r++)
//			for (int c= 0; c < width; c++)
//			{
//				if (theData[r][c][0]>maxVal_0)
//					maxVal_0=theData[r][c][0];
//				if (theData[r][c][1]>maxVal_1)
//					maxVal_1=theData[r][c][1];
//				if (theData[r][c][2]>maxVal_2)
//					maxVal_2=theData[r][c][2];
//			}
//
//		float[] data = new float[width*height*3];
//		int count =0;
//
//
//
//		DataBufferFloat dbuffer = new DataBufferFloat(data, width*height*3);
//		SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 3);
//
//		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
//		WritableRaster raster = RasterFactory.createWritableRaster(sampleModel, new Point(0,0));
//		float[] pix = new float[3];
//		for (int h =0; h < height; h++)
//			for (int w =0; w < width; w++)
//			{
////				data[count+1] = 255;
//				pix[0] = weights[0]*(float)theData[h][w][0]/(float)maxVal_0;//maxVal_0*255;//(count%2 ==0) ? (byte)255: (byte) 0;
//				pix[1] = weights[1]*(float)theData[h][w][1]/(float)maxVal_1;///maxVal_1*255;
//				pix[2] = weights[2]*(float)theData[h][w][2]/(float)maxVal_2;///maxVal_2*255;//(count%2 ==0) ? (byte)255: (byte) 0;
//				raster.setPixel(w,h,pix);
//			}
//
//
//		TiledImage tiledImage = new TiledImage(0,0,width,height, 0,0, sampleModel, colorModel);
//
//
////		// Create a WritableRaster.
////		Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point(0,0));
//		// Set the data of the tiled image to be the raster.
//		tiledImage.setData(raster);
//
//		JFrame window = new JFrame();
////		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
//		DisplayJAIwithAnnotations pan = new DisplayJAIwithAnnotations(tiledImage);
//		pan.cells = cells;
//		window.add(pan);
//
//		window.pack();
//		window.show();
//	}
	
//	static public void displayRaster(int[][][] theData)
//	{
//		int channel=0;
//		int width = theData[0].length;
//		int height = theData.length;
//
//		//finding maxValue
//		int maxVal = 1;
//		for (int r = 0; r < height; r++)
//			for (int c= 0; c < width; c++)
//				if (theData[r][c][channel]>maxVal)
//					maxVal=theData[r][c][channel];
//
//		float[][] newData = new float[height][width];
//		for (int r = 0; r < height; r++)
//			for (int c= 0; c < width; c++)
//				newData[r][c] = theData[r][c][channel]/maxVal;
//
//		// Create a float data sample model.
//		SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,width,height,1);
//
//		// Create a compatible ColorModel.
//		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
//
//		// Create a TiledImage using the float SampleModel.
//		TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,sampleModel,colorModel);
//
//
//		float[] imageDataSingleArray = new float[width*height];
//		int count = 0;
//		// It is important to have the height/width order here !
//		for(int h=0;h<height;h++)
//			for(int w=0;w<width;w++)
//				imageDataSingleArray[count++] = newData[h][w];
//
//		// Create a Data Buffer from the values on the single image array.
//		DataBufferFloat dbuffer = new DataBufferFloat(imageDataSingleArray,
//													  width*height);
//
//
//		// Create a WritableRaster.
//		Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point(0,0));
//		// Set the data of the tiled image to be the raster.
//		tiledImage.setData(raster);
//
//		JFrame window = new JFrame();
////		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
//		window.add(new DisplayJAIwithAnnotations(tiledImage));
//
//		window.pack();
//		window.show();
//	}
	
	
	
	static public TiledImage createRenderedImageFromRaster(RenderedImage img, int[][][] raster)
	{
		Raster rast =  img.getData();
		WritableRaster outR = rast.createCompatibleWritableRaster();
		
		int height = raster.length;
		int width = raster[0].length;
		int numBands = rast.getNumBands();
		int[] pixel = new int[numBands];
		for (int h =0; h < height; h++)
			for (int w= 0;w < width ; w++)
			{
				for (int i = 0; i < numBands; i++)
					pixel[i] = raster[h][w][i];
				outR.setPixel(h,w,pixel);
			}
		
		TiledImage ti = new TiledImage(img, 1,1);
		ti.setData(outR);
		return ti;
	}
	
	/**returns a list of well names that the given directory contained image files for
	 @author BLM
	 * **/
	static public String[] getWellNamesOfImagesPresent(File dir, String[] allWellNames)
	{
		File[] tiffs = getAllTifFiles(dir);
		int len = tiffs.length;
		int num = allWellNames.length;
		ArrayList arr = new ArrayList();
		for (int i =0; i < len; i ++)
			for (int j =0; j < num; j ++)
				if (tiffs[i].getName().indexOf(allWellNames[j])>0)
				{
					arr.add(allWellNames[j]);
					break;
				}
		String[] names = new String[arr.size()];
		len = names.length;
		for (int i =0; i < len; i ++)
			names[i] = (String)arr.get(i);
		return names;
	}
	
	static public File[] getFilesForGivenWell(File dir, Model_Well well)
	{
		File[] tiffs = getAllTifFiles(dir);
		int len = tiffs.length;
		ArrayList arr = new ArrayList();
		
		for (int i =0; i < len; i ++)
			if (tiffs[i].getName().indexOf("_"+well.name+"_")>0)
				arr.add(tiffs[i]);
		
		File[] files = new File[arr.size()];
		len = files.length;
		for (int i =0; i < len; i ++)
			files[i] = (File)arr.get(i);
		return files;
	}
	
	
	static public String[] getNameOfUniqueChannels(File dir)
	{
		ArrayList<String> uniques = new ArrayList<String>();
		// This is the parent directory for all plates' images
		// Each file here is a directory: Plate_0, Plate_1, etc...
		File[] dirs = dir.listFiles();
		if (dirs == null)
			return null;
		
		int numD = dirs.length;
		for (int i = 0; i < numD; i++)
		{
			// Get each plate successively
			File[] files = dirs[i].listFiles();
			if (files != null && files.length > 0)
			{
				// Checking Plate[i]
				int numF = files.length;
				for (int f = 0; f < numF; f++) {

					// getting channel name
					String name = files[f].getName();

					// Trying to find the index of the start of the wavelength
					// identifier
					int ind = 0;
					for (int j = 0; j < 10; j++) {

						ind = name.indexOf(("_w" + j));
						if (ind > 0)
							break;
					}

					// Trying to find the index of the start of the Tiff
					// extension
					int ind2 = name.indexOf(".tif");
					if (ind2 <= 0)
						ind2 = name.indexOf(".TIF");
					if (ind2 <= 0)
						ind2 = name.indexOf(".Tif");
				
					if (ind > 0) {
						String subString = name.substring(ind + 2, ind2);
						// seeing if unique
						boolean unique = true;
						for (int j = 0; j < uniques.size(); j++) {
							String uniq = (String) uniques.get(j);
							if (uniq.equalsIgnoreCase(subString)) {
								unique = false;
								break;
							}
							}
						if (unique) {
							uniques.add(subString);
						}
					}
			}
			}
		}
		
		Collections.sort(uniques);
		
		int len = uniques.size();
		
		String[] arr = new String[len];
		for (int i=0; i < uniques.size(); i++)
			arr[i]= ("w"+(String)uniques.get(i));
		// Trying to sort alphanumerically
		Arrays.sort(arr);
		return arr;
	}
	
	
	static public int getNumUniqueChannels(ArrayList<File> files)
	{
		ArrayList<String> uniques = new ArrayList<String>();
		
		int numF = files.size();
		for(int i=0;i < numF; i++)
		{
			//getting channel name
			String name = ((File)files.get(i)).getName();
			
			int ind = 0;
			for (int j=0;j<10;j++)
			{
				
				ind = name.indexOf(("_w"+j));
				if (ind>0)
					break;
			}
			
			int ind2 = name.indexOf(".tif");
			if (ind2<=0)
				ind2 = name.indexOf(".TIF");
			if (ind2<=0)
				ind2 = name.indexOf(".Tif");
			
			
			if (ind>0)
			{
				String subString = name.substring(ind+2, ind2);
				//seeing if unique
				boolean unique = true;
				for (int j = 0;  j < uniques.size(); j++)
				{
					String uniq = (String) uniques.get(j);
					if (uniq.equalsIgnoreCase(subString))
					{
						unique = false;
						break;
					}
				}
				if (unique)
				{
					uniques.add(subString);
				}
			}
		}
		
		
		return uniques.size();
		
	}
	
	
	static public File[] getFilesForGivenWells(File[] tiffs, Model_Well[] desiredWells)
	{
		int len = tiffs.length;
		int num = desiredWells.length;
		ArrayList arr = new ArrayList();
		
		for (int j =0; j < num; j ++)
			for (int i =0; i < len; i ++)
				if (tiffs[i].getName().indexOf("_"+(desiredWells[j]).name+"_")>0)
					arr.add(tiffs[i]);
		
		File[] files = new File[arr.size()];
		len = files.length;
		for (int i =0; i < len; i ++)
			files[i] = (File)arr.get(i);
		return files;
	}
	
	static public File[] getFilesForGivenWells(File dir, ArrayList desiredWells)
	{
		File[] tiffs = getAllTifFiles(dir);
		int len = tiffs.length;
		int num = desiredWells.size();
		ArrayList arr = new ArrayList();
		
		for (int j =0; j < num; j ++)
			for (int i =0; i < len; i ++)
				if (tiffs[i].getName().indexOf("_"+((Model_Well)desiredWells.get(j)).name+"_")>0)
					arr.add(tiffs[i]);
		
		File[] files = new File[arr.size()];
		len = files.length;
		for (int i =0; i < len; i ++)
			files[i] = (File)arr.get(i);
		return files;
	}
	
	static public File[] getAllTifFiles(File dir)
	{
		File[] files = dir.listFiles();
		int len  = files.length;
		ArrayList arr = new ArrayList();
		
		for (int i =0; i < len; i ++)
			if (files[i].getName().indexOf(".tif")>0 || files[i].getName().indexOf(".TIF")>0|| files[i].getName().indexOf(".TIFF")>0 ||files[i].getName().indexOf(".tiff")>0)
				arr.add(files[i]);
		len = arr.size();
		File[] f = new File[len];
		for (int i =0; i < len; i ++)
			f[i] = (File)arr.get(i);
		return f;
	}
	
	static public File[] getAllTifFilesContaining(File dir, String str)
	{
		File[] files = dir.listFiles();
		int len  = files.length;
		ArrayList arr = new ArrayList();
		
		for (int i =0; i < len; i ++)
			if ((files[i].getName().indexOf(".tif")>0 || files[i].getName().indexOf(".TIF")>0 || files[i].getName().indexOf(".TIFF")>0 ||files[i].getName().indexOf(".tiff")>0) && files[i].getName().indexOf(str)>=0)
				arr.add(files[i]);
		len = arr.size();
		File[] f = new File[len];
		for (int i =0; i < len; i ++)
			f[i] = (File)arr.get(i);
		return f;
	}
	
	
	
//		static public void copyFile(File in, File out) throws Exception
//	{
//		FileInputStream fis  = new FileInputStream(in);
//		FileOutputStream fos = new FileOutputStream(out);
//		byte[] buf = new byte[1024];
//		int i = 0;
//		while((i=fis.read(buf))!=-1)
//		{
//			fos.write(buf, 0, i);
//		}
//		fis.close();
//		fos.close();
//	}
	
	
	public static void copyFile(File in, File out)
		throws IOException
	{
		FileChannel inChannel = new
			FileInputStream(in).getChannel();
		FileChannel outChannel = new
			FileOutputStream(out).getChannel();
		try
		{
			inChannel.transferTo(0, inChannel.size(),
								 outChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}
	
	
	
	
	
	
	static public int[][][] getImageRaster_FromFile_copy(File file)
	{
		SeekableStream ss = null;
		PlanarImage op = null;
		try
		{
			ss = new FileSeekableStream(file);
			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
		}
		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
		
		//now creating the image
		
		//gettng the raster
		Raster ras = op.getData();
		int width = ras.getWidth();
		int height = ras.getHeight();
		int numBands = ras.getNumBands();
		int[][][] rast = new int[height][width][numBands];
		
		int[] pix = new int[numBands];
		for (int r =0; r < height; r++)
			for (int c =0; c < width; c++)
			{
				ras.getPixel(c,r,pix);
				for (int i =0; i < numBands; i++)
					rast[r][c][i] = pix[i];
			}
		return rast;
	}
	
	static public int[][][] getImageRaster_FromFile_copy_bandsFirst(File file)
	{
		SeekableStream ss = null;
		PlanarImage op = null;
		try
		{
			ss = new FileSeekableStream(file);
			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
		}
		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
		
		//now creating the image
		
		//gettng the raster
		Raster ras = op.getData();
		int width = ras.getWidth();
		int height = ras.getHeight();
		int numBands = ras.getNumBands();
		int[][][] rast = new int[numBands][height][width];
		
		int[] pix = new int[numBands];
		for (int i =0; i < numBands; i++)
			for (int r =0; r < height; r++)
				for (int c =0; c < width; c++)
				{
					ras.getPixel(c,r,pix);
					rast[i][r][c] = pix[i];
				}
		return rast;
	}
	
	static public int[][][] getImageRaster_FromFiles_copy(File[] inFiles,
			String[] requiredChannelNames)
	{
		//finding all tiff files only
		int numFiles = inFiles.length;
		ArrayList<File> arr = new ArrayList<File>();
		for (int i =0; i < numFiles; i++)
		{
			File f = inFiles[i];
			
			if (f.getName().indexOf(".tif")>0||f.getName().indexOf(".TIF")>0)
				arr.add(f);
		}
		numFiles = arr.size();
		File[] files = new File[numFiles];
		for (int i =0; i < numFiles; i++)
			files[i] = (File)arr.get(i);
		
		
		//init Raster dimensions with first file... then auto do others
		SeekableStream ss = null;
		PlanarImage op = null;
		if (files.length==0)
		{
			System.out.println("**Not able to get rasters from channeled Images properly**");
			return null;
		}
		try
		{
			ss = new FileSeekableStream(files[0]);
			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
		}
		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
		

		// for (int r =0; r < height; r++)
		// for (int c =0; c < width; c++)
		// {
		// ras.getPixel(c,r,pix);
		// rast[r][c][0] = (int)((float)pix[0]);
		// }
		
		// getting the raster
		Raster ras = op.getData();
		int width = ras.getWidth();
		int height = ras.getHeight();
		int[][][] rast = new int[height][width][requiredChannelNames.length];
		int[] pix = new int[3];
		//Now doing other files
		for (int i = 0; i < numFiles; i++)
		{
			try
			{
				ss = new FileSeekableStream(files[i]);
				ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
				op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
			}
			catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
			// get channel index
			String[] rNames = requiredChannelNames;
			int thisIndex = -1;
			for (int j = 0; j < rNames.length; j++) {
				if (files[i].getName().indexOf(rNames[j]) > 0)
					thisIndex = j;
			}
			
			// getting the raster
			ras = op.getData();
			for (int r =0; r < height; r++)
				for (int c =0; c < width; c++)
				{
					ras.getPixel(c,r,pix);
					rast[r][c][thisIndex] = pix[0];
				}
		}
		
		
		return rast;
	}
	
	
	/** given a single pixel array where elem 0 = alpha, 1 = red value, 2 = green val, 3 = blue
	 * this sums up the RGB values to get a sum*/
	static public int getPixelIntensity(int[] pixel)
	{
		int len = pixel.length;
		int sum = 0;
		for (int i = 0; i < len; i++)
			sum+=pixel[i];
		return sum;
	}
	
	
	/** Makes and returns a copy of the given raster
	 * @author BLM*/
	static public int[][][] copyRaster(int[][][] raster)
	{
		int nRows = raster.length;
		int nCols = raster[0].length;
		int len = raster[0][0].length;
		int[][][] temp = new int[nRows][nCols][len];
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
				for (int i =0 ;i < len; i++)
					temp[r][c][i] = raster[r][c][i];
		return temp;
	}
	
	static public int[][] copyRaster_oneChannelOnly(int[][][] raster,
			int index) {
		int nRows = raster.length;
		int nCols = raster[0].length;
		int[][] temp = new int[nRows][nCols];
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
				temp[r][c] = raster[r][c][index];
		return temp;
	}

	static public float[][][] copyRaster(float[][][] raster)
	{
		int nRows = raster.length;
		int nCols = raster[0].length;
		int len = raster[0][0].length;
		float[][][] temp = new float[nRows][nCols][len];
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
				for (int i =0 ;i < len; i++)
					temp[r][c][i] = raster[r][c][i];
		return temp;
	}
	
	static public float[][] copyRaster(float[][] raster)
	{
		int nRows = raster.length;
		int nCols = raster[0].length;
		
		float[][] temp = new float[nRows][nCols];
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
				temp[r][c] = raster[r][c];
		return temp;
	}
	
	
	/** divides the given intesnity up into 3 parts and shares it equally btw the RGB of the given pixel
	 * @author BLM*/
	static public void setIntensityToPixel(int[] pixel, int intensity)
	{
		int len = pixel.length;
		int val = (int)((float)intensity/(float)len);
		
		for (int i = 0; i < len; i++)
			pixel[i]=val;
		
	}
	
	static public void setPixelBlack(int[] pixel)
	{
		int len = pixel.length;
		for (int i = 0; i < len; i++)
			pixel[i] = 0;
		
	}
	static public void setPixelWhite(int[] pixel)
	{
		int len = pixel.length;
		for (int i = 0; i < len; i++)
			pixel[i] = 255;
	}
	
	
//	static public void DisplayTiff(File image)
//	{
//		System.out.println(image.getName() + " *****" );
//
//		if (image.getName().indexOf(".tif")<0 && image.getName().indexOf(".TIF")<0)
//			return;
//
//		SeekableStream ss = null;
//		PlanarImage op = null;
//		try
//		{
//			ss = new FileSeekableStream(image);
//			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
//			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
//		}
//		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
//
//
//
//		JFrame window = new JFrame();
////		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
//		DisplayJAIwithAnnotations pan = new DisplayJAIwithAnnotations(op);
//		window.add(pan);
//
//		window.pack();
//		window.show();
//	}
	
//	static public void displayTIff_withCells(File image, Cell[] cells)
//	{
//		System.out.println(image.getName() + " *****" );
//
//		if (image.getName().indexOf(".tif")<0 && image.getName().indexOf(".TIF")<0)
//			return;
//
//		SeekableStream ss = null;
//		PlanarImage op = null;
//		try
//		{
//			ss = new FileSeekableStream(image);
//			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
//			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
//		}
//		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
//
//
//
//
////		ScrollingImagePanel panel = new ScrollingImagePanel(tiledImage, width, height);
//		DisplayJAIwithAnnotations pan = new DisplayJAIwithAnnotations(op);
//		pan.cells = cells;
//
//		JFrame window = new JFrame();
//		window.add(pan);
//		window.pack();
//		window.show();
//	}
	
//	static public MultiChanneledViewer_wCells getMultiChanneledViewer_wCells(File[] imageFiles, Cell[][] cells)
//	{
//
////		if (imageFiles.getName().indexOf(".tif")<0 && imageFiles.getName().indexOf(".TIF")<0)
////			return null;
//
////		SeekableStream ss = null;
////		PlanarImage op = null;
////		try
////		{
////			ss = new FileSeekableStream(imageFiles);
////			ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
////			op = new NullOpImage(decoder.decodeAsRenderedImage(),null,OpImage.OP_IO_BOUND,null);
////		}
////		catch (IOException e) {System.out.println("error Loading image: ");e.printStackTrace();}
////
////
//
//		MultiChanneledViewer_wCells pan = new MultiChanneledViewer_wCells(imageFiles, cells);
//		return pan;
//
//	}
//
	
	
	static public ArrayList sortFilesByChannels_lowToHigh(ArrayList arr, String[] names)
	{
		ArrayList arr2 = new ArrayList();
		int len = arr.size();
		int numChannels = names.length;
		
		for (int n = 0; n < numChannels; n++)
		{
			for (int i = 0; i < len; i++)
			{
				String name = ((File)arr.get(i)).getName();
				if (name.indexOf(names[n])>=0)
					arr2.add(arr.get(i));
			}
		}
		
//		for (int i = 0; i < arr2.size(); i++)
//		{
//			System.out.println(((File)arr2.get(i)).getName());
//		}
		
		
		return arr2;
	}
	
	static public int[][][] linearFilter(int[][][] inputRaster, float[][] kernal)
	{
		int kernalWidth = kernal.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		int[][][] temp = tools.ImageTools.copyRaster(inputRaster);
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
//						System.out.println(n+"  "+kcol +" - "+ krow+"  "+k);
						float w = kernal[kcol][krow];
						sum+=w*tools.ImageTools.getPixelIntensity(inputRaster[n][k]);
						kcol++;
					}
					kcol = 0;
					krow++;
				}
				
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
	
	static public float[][] linearFilter(float[][] inputRaster, float[][] kernal)
	{
		int kernalWidth = kernal.length;
		int halfKernalWidth = (int)(((float)kernalWidth-1f)/2f);
		int nRows = inputRaster.length;
		int nCols = inputRaster[0].length;
		float[][] temp = tools.ImageTools.copyRaster(inputRaster);
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
//						System.out.println(n+"  "+kcol +" - "+ krow+"  "+k);
						float w = kernal[kcol][krow];
						sum+=w*inputRaster[n][k];
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
		
		
		//now scaling to not have negative numbers
		for (int r = 0; r < nRows; r++)
			for (int c = 0; c < nCols; c++)
			{
				float val = temp[r][c];
				val = val-min;
				val = (int)((float)val*(float)tools.ImageTools.Pixel_maxIntensity/(float)max);
				if (val < 0)
					val = 0;
				if (val>tools.ImageTools.Pixel_maxIntensity)
					val = tools.ImageTools.Pixel_maxIntensity;
				temp[r][c] = val;
			}
		
		return temp;
	}
	
	public static void writeImageToJPG (File file, BufferedImage bufferedImage) throws IOException
    {
		ImageIO.write(bufferedImage,"jpg",file);
    }
	
	public static void writeImageToPNG (File file, BufferedImage bufferedImage) throws IOException
    {
		ImageIO.write(bufferedImage,"png",file);
    }
	
	static public BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm
				.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable properties = new Hashtable();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster,
				isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}

	/**
	 * Given a list of cells
	 * */
	static public void printInterCellLineProfiles(
			ArrayList<CellCoordinates> cells, int[][][] raster, int channelIndex) {

		File f = new File("/Users/blm13/Desktop/out_"
				+ System.currentTimeMillis() + ".csv");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR creating printwriter for file: "
					+ f.getAbsolutePath());
			e.printStackTrace();
			return;
		}
		int len = cells.size();
		for (int c = 0; c < len; c++) {
			CellCoordinates cell = cells.get(c);
			for (int r = 0; r < len; r++) {
				if (c != r) {

					CellCoordinates cell2 = cells.get(r);
					imagerailio.Point p1 = cell.getCentroid();
					imagerailio.Point p2 = cell2.getCentroid();
					// // compute slope btw points
					// float m = ((float) p2.y - (float) p1.y)
					// / ((float) p2.x - (float) p1.x);
					// int xDist = p2.x - p1.x;
					// int yStart = p1.y;
					// int xStart = p1.x;
					// if (xDist > 0) {
					// String st = "";
					// for (int x = 0; x < xDist; x++) {
					// int y = (int) (m * x + yStart);
					// int val = raster[y][xStart + x][channelIndex];
					// st += val + ",";
					// }
					// pw.println(st);
					// pw.flush();
					//
					// }

					// compute slope btw points
					float m = ((float) p2.y - (float) p1.y)
							/ ((float) p2.x - (float) p1.x);
					int xDist = p2.x - p1.x;
					int yStart = p1.y;
					int xStart = p1.x;

					String st = "";
					for (int x = 0; x < Math.abs(xDist); x++) {

						int xI = x;
						if (xDist < 0)
							xI = -x;

						int xP = xStart + xI;

						int y = (int) (m * xI + yStart);
						// g2.drawLine((int) (scalingFactor * xP),
						// (int) (scalingFactor * y),
						// (int) (scalingFactor * xP),
						// (int) (scalingFactor * y));

						int val = raster[y][xP][channelIndex];
						st += val + ",";
					}
					pw.println(st);
				}
			}
		}
		pw.flush();
		pw.close();
		System.exit(0);

	}
	
	/**
	 * Accepts image raster and number of bins desired and returns a 2xnumBins 
	 * matrix where first dimensions contains the bin range values and the second the bin count
	 * @author BLM
	 * @param int[][] raster, int numBins
	 * @return int[][] histogram (2xnumBins)
	 */
	public static int[][] getHistogram(int[][] raster, int numBins)
	{
		//2xnumBins matrix 
		int[][] bins = new int[2][numBins];
		int width = raster.length;
		int height = raster[0].length;
		//Find min and max values
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if(raster[r][c]<min)
					min = raster[r][c];
				if(raster[r][c]>max)
					max = raster[r][c];
			}
		}
		//Init the bin range values
		int dx = (int)((float)(max-min)/(float)numBins);
		for (int i = 0; i < numBins; i++)
			bins[0][i] = (int)(min+dx*i);
		//Binning
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				
				int index = (int)((((float)raster[r][c]-min)/(float)(max-min))*numBins);
				if (index>=numBins)
					index = numBins-1;
				else if (index<0)
					index=0;
				
				bins[1][index]++;
			}
		}
		return bins;
	}


	/**
	 * Accepts image raster and number of bins desired and returns a 2xnumBins 
	 * matrix where first dimensions contains the bin range values and the second the bin count
	 * Normalizes the bin count from 0-1.
	 * @author BLM
	 * @param int[][] raster, int numBins
	 * @return float[][] histogram (2xnumBins)
	 */
	public static float[][] getHistogram_norm(int[][] raster, int numBins)
	{
		//2xnumBins matrix 
		float[][] bins = new float[2][numBins];
		int width = raster.length;
		int height = raster[0].length;
		//Find min and max values
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if(raster[r][c]<min)
					min = raster[r][c];
				if(raster[r][c]>max)
					max = raster[r][c];
			}
		}
		//Init the bin range values
		int dx = (int)((float)(max-min)/(float)numBins);
		for (int i = 0; i < numBins; i++)
			bins[0][i] = (int)(min+dx*i);
		//Binning
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				
				int index = (int)((((float)raster[r][c]-min)/(float)(max-min))*numBins);
				if (index>=numBins)
					index = numBins-1;
				else if (index<0)
					index=0;
				
				bins[1][index]++;
			}
		}
		//Normalizing
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
		for (int i = 0; i < numBins; i++)
		{
			if(bins[1][i]<min)
				min = (int)bins[1][i];
			if(bins[1][i]>max)
				max = (int)bins[1][i];
		}
//		System.out.println("*************");
		for (int i = 0; i < numBins; i++)
		{
			bins[1][i] = ((bins[1][i]-min)/(max-min));
//			System.out.println(bins[1][i]);
		}
		return bins;
	}
	
	/**
	 * Accepts image raster and number of bins desired and returns a 2xnumBins 
	 * matrix where first dimensions contains the bin range values and the second the bin count
	 * Normalizes the bin count from 0-1.
	 * @author BLM
	 * @param int[][] raster, int numBins
	 * @return float[][] histogram (2xnumBins)
	 */
	public static float[][] getHistogram_bounds_norm(int[][] raster, int numBins, int minRange, int maxRange)
	{
		//2xnumBins matrix 
		float[][] bins = new float[2][numBins];
		int width = raster.length;
		int height = raster[0].length;
		//Find min and max values
		float min = minRange;
		float max = maxRange;
		
		//Init the bin range values
		int dx = (int)((float)(max-min)/(float)numBins);
		for (int i = 0; i < numBins; i++)
			bins[0][i] = (int)(min+dx*i);
		//Binning
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				
				int index = (int)((((float)raster[r][c]-min)/(float)(max-min))*numBins);
				if (index>=numBins)
					index = numBins-1;
				else if (index<0)
					index=0;
				
				bins[1][index]++;
			}
		}
		//Normalizing
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
		for (int i = 0; i < numBins; i++)
		{
			if(bins[1][i]<min)
				min = (int)bins[1][i];
			if(bins[1][i]>max)
				max = (int)bins[1][i];
		}
		for (int i = 0; i < numBins; i++)
		{
			bins[1][i] = ((bins[1][i]-min)/(max-min));
		}
		return bins;
	}
}


