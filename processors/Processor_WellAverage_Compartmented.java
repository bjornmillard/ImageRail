/**
 * Task_processDirectory_PrintMIDAS_TIFF.java
 *
 * @author Created by Omnicore CodeGuide
 */

package processors;

import features.Feature;
import gui.MainGUI;

import java.io.File;

import models.Model_Plate;
import models.Model_Well;
import segmentors.DefaultSegmentor;

public class Processor_WellAverage_Compartmented extends Thread implements Processor
{

	private Model_Well[] WellsToProcess;
	private boolean ClusterRun=false;
	
	public void run()
	{
		runProcess();
	}
	
	/*** Sets the wells that are supposed to be processed by this processor
	 * @author BLM*/
	public void setWellsToProcess(Model_Well[] wells)
	{
		WellsToProcess = wells;
	}
									  
	
	public void runProcess()
	{
		
		int numWells = WellsToProcess.length;
		MainGUI.getGUI().setProcessing(true);
		for (int b = 0; b < numWells; b++)
		{
			Model_Well well = WellsToProcess[b];
			well.processing = true;
			MainGUI.getGUI().getPlateHoldingPanel().updatePanel();
			System.out.println("******** Processing Model_Well: "+ well.name +" ********");
			
//			ArrayList allSetsOfChanneledImages = well.getAllSetsOfCorresponsdingChanneledImageFiles();
			
			int numFields = well.getFields().length;
			
			//getting ready to store mean values
			// 0 - nuc, 1 - cyto, 2 - whole
			float[][] meanValues = new float[3][MainGUI.getGUI().getNumberOfChannels()];
			for (int i=0; i < 3; i++)
				for (int p=0; p< meanValues.length; p++)
					meanValues[i][p]=0;
			
			for (int f = 0; f < numFields; f++)
			{
				File[] set = well.getFields()[f].getImageFiles();
				int[][][] Raster_Channels = tools.ImageTools.getImageRaster_FromFiles_copy(set);
				//segmenting the cells based on the binary image
				float[][] tempMeans = DefaultSegmentor.findWellAverageOnly_Compartments(Raster_Channels, well.TheParameterSet);
				//storing this data
				for (int i=0; i < 3; i++)
					for (int p=0; p< tempMeans.length; p++)
						meanValues[i][p]+=tempMeans[i][p];
				Raster_Channels = null;
			}
			
			//doing final average and then setting the data
			for (int i=0; i < 3; i++)
				for (int p=0; p< meanValues.length; p++)
					meanValues[i][p]=meanValues[i][p]/numFields;
			
			
			int numF = MainGUI.getGUI().getTheFeatures().size();
			//Setting the nuclear values
			float[] temp = new float[numF];
			int counter = 0;
			//Setting the whole values
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Whole_")>=0)
				{
					temp[i] = meanValues[2][counter];
					counter++;
				}
			}
			counter = 0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Nucleus_")>=0)
				{
					temp[i] = meanValues[0][counter];
					counter++;
				}
				
			}
			//Setting the cyto values
			counter = 0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Cyto_")>=0)
				{
					temp[i] = meanValues[1][counter];
					counter++;
				}
			}
			//Setting the ratios
			counter = 0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("CytNucRatio_")>=0)
				{
					temp[i] = meanValues[1][counter]/meanValues[0][counter];
					counter++;
				}
			}
			counter = 0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("NucCytRatio_")>=0)
				{
					temp[i] = meanValues[0][counter]/meanValues[1][counter];
					counter++;
				}
			}
			
			
			well.setMeanFluorescentValues(temp);
			
			
			well.processing = false;
			if(!ClusterRun)
			{
				MainGUI.getGUI().getThePlateHoldingPanel().getGUI()
						.updatePanel();
				MainGUI.getGUI().updateAllPlots();
			}
		}
		MainGUI.getGUI().setProcessing(false);
	}
	
	public Model_Well getWellForGivenImage(String fileName)
	{
		for (int p = 0; p < MainGUI.getGUI().getThePlateHoldingPanel().getNumPlates(); p++)
		{
			Model_Plate plate = MainGUI.getGUI().getThePlateHoldingPanel().getPlates()[p];
			int rows = plate.getNumRows();
			int cols = plate.getNumColumns();
			for (int r = 0; r< rows; r++)
				for (int c= 0; c < cols; c++)
				{
					if (fileName.indexOf(plate.getWells()[r][c].name)>0)
						return plate.getWells()[r][c];
				}
		}
		
		return null;
	}
	
	public boolean isWellToProcess(String fileName)
	{
		int num = WellsToProcess.length;
		for (int i =0; i < num; i++)
			if (fileName.indexOf(WellsToProcess[i].name)>0)
				return true;
		return false;
	}
	
	
	public void setClusterRun(boolean boo)
	{
		ClusterRun = boo;
	}
}


