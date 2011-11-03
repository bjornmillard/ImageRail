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

package processors;
import features.Feature;
import gui.MainGUI;
import imagerailio.ImageRail_SDCube;

import java.io.File;

import models.Model_Plate;
import models.Model_Well;
import sdcubeio.H5IO_Exception;
import segmentors.DefaultSegmentor;
import dataSavers.DataSaver_CSV;

public class Processor_WellAverage extends Thread implements Processor
{
	private Model_Well[] WellsToProcess;
	private File ResultsFile;
	private boolean ClusterRun;
	
	public Processor_WellAverage(Model_Well[] wellsToProcess)
	{
		WellsToProcess = wellsToProcess;
		ClusterRun = false;
	}
	
	public void run()
	{
		runProcess();
	}
	
	/** Main Processor Method
	 * @author BLM*/
	public void runProcess()
	{
		int numWells = WellsToProcess.length;
		long StartTime  = System.currentTimeMillis();
		MainGUI.getGUI().setProcessing(true);
		
		
		ImageRail_SDCube io = MainGUI.getGUI().getH5IO();

		// try
		// {
		// String projPath =
		// gui.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
		// // ProjectHDFConnector con = new ProjectHDFConnector(projPath);
		// // con.createProject();
		// io = gui.MainGUI.getGUI().getH5IO();
		// //creating a HDF plate for each plate needed, since the wells could
		// come from different plates
		// int[][] idsAndWells =
		// Processor_SingleCells.getAllUniquePlateIDsAndNumWells(WellsToProcess);
		// for (int i = 0; i < idsAndWells[0].length; i++)
		// {
		// // System.out.println("ids: "+(idsAndWells[0][i]-1)
		// +"   wells: "+idsAndWells[1][i]);
		// con.writePlateSize(idsAndWells[0][i]-1, idsAndWells[1][i]);
		// }
		// }
		// catch (H5IO_Exception e) {e.printStackTrace();}
		
		
		
		
		//Processing all the wells
		for (int w = 0; w < numWells; w++)
		{
			if (!MainGUI.getGUI().shouldStop())
				break;

			Model_Well well = WellsToProcess[w];
			well.processing = true;
			MainGUI.getGUI().getPlateHoldingPanel().updatePanel();
			System.out.println("******** Processing Model_Well: "+ well.name +" ********");
			
			//Initializing mean value storage variables
			float[] totalIntegration = new float[MainGUI.getGUI().getTheFeatures().size()];
			for (int p=0; p< totalIntegration.length; p++)
				totalIntegration[p]=0;
			float[] meanValues = new float[MainGUI.getGUI().getTheFeatures().size()];
			for (int p=0; p< meanValues.length; p++)
				meanValues[p]=0;
			
			int numFields = well.getFields().length;
			int totalPix=0;
			for (int f = 0; f < numFields; f++)
			{
				if (!MainGUI.getGUI().shouldStop())
					break;

				File[] images_oneField = well.getFields()[f].getImageFiles();
				int[][][] Raster_Channels = tools.ImageTools
						.getImageRaster_FromFiles_copy(images_oneField,
								gui.MainGUI.getGUI().getTheChannelNames());
				int[] fieldDimensions = { Raster_Channels.length,
						Raster_Channels[0].length, Raster_Channels[0][0].length };
				try {
					io.createField(well.getID(), well.getPlate().getID(), well
							.getWellIndex(), f, fieldDimensions, gui.MainGUI
							.getGUI().getExpDesignConnector());
				} catch (H5IO_Exception e) {
					System.out
							.println("** Error creating field in HDF5 file **");
					e.printStackTrace();
				}
				// Getting integrated values of the images for each channel
				float[][] tempIntegration = DefaultSegmentor
						.findTotalIntegrationAndTotalPixUsed(Raster_Channels,
								well.TheParameterSet);
				// storing this data
				for (int p = 0; p < tempIntegration.length; p++)
					totalIntegration[p] += tempIntegration[p][0];
				totalPix += tempIntegration[0][1];
				Raster_Channels = null;

			}
			
			
			//Computing the features now
			int numF = MainGUI.getGUI().getTheFeatures().size();
			float[] temp_all = new float[numF];
			int counter = 0;
			int counterInt =0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.Name.indexOf("w")>=0 && f.Name.indexOf("Whole_")>=0 && f.toString().indexOf("(Mean)")>=0)
				{
					temp_all[i] = totalIntegration[counter]/totalPix;
					counter++;
				}
				else if (f.Name.indexOf("w")>=0 && f.Name.indexOf("Whole_")>=0 && f.toString().indexOf("(Integrated)")>=0)
				{
					temp_all[i] = totalIntegration[counterInt];
					counterInt++;
				}
			}
			
			//Subtracting background if desired
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.Name.indexOf("w")>=0 && f.Name.indexOf("Whole_")>=0 && f.toString().indexOf("(Mean)")>=0)
				{
					float bkgd = well.TheParameterSet.getThreshold_Background();
					temp_all[i] = temp_all[i] - bkgd;
				}
			}
			
			
			well.setMeanFluorescentValues(temp_all);
			well.processing = false;
			
			if(!ClusterRun)
			{
				MainGUI.getGUI().getPlateHoldingPanel().updatePanel();
				MainGUI.getGUI().updateAllPlots();
			}
			
			try
			{
				//Trying to write mean value data to file
				int wellIndex = (well.getPlate().getNumRows()*well.Column)+well.Row;
				int plateIndex = well.getPlate().getID();
				Feature[] features = gui.MainGUI.getGUI().getFeatures();
				StringBuffer[] featureNames = null;
				if(features!=null && features.length>0)
				{
					featureNames = new StringBuffer[features.length];
					for (int i = 0; i < features.length; i++)
						featureNames[i] = new StringBuffer(features[i].toString());
				}
				
				if(well.Feature_Means!=null && io!=null)
				{
					io
							.writeWellMeans(plateIndex, wellIndex,
									well.Feature_Means);
					// if(featureNames!=null)
					// io.writeMeanFeatureNames(plateIndex, featureNames);
				}
				if(well.Feature_Stdev!=null && io!=null)
					io.writeWellStdDevs(plateIndex, wellIndex,
							well.Feature_Stdev);
				// Writing HDF5 well sample metadata
				int totNumWells = well.getPlate().getNumRows()
						* well.getPlate().getNumColumns();
				io.writeParentPlateInfo(plateIndex, wellIndex, totNumWells);
				io.writeSegmentationParameters(plateIndex, wellIndex,
						(int) well.getParameterSet().getThreshold_Nucleus(),
						(int) well.getParameterSet().getThreshold_Cytoplasm(),
						(int) well.getParameterSet().getThreshold_Background());
				
			}
			catch (Exception e)
			{
				// Handle this exception!!!
				System.out.println("ERROR: writing to well mean/stdev HDF file");
			}
		}
		
		if (ResultsFile!=null)
		{
			System.out.println("***Finished****");
			new DataSaver_CSV().save(MainGUI.getGUI(), ResultsFile);
		}
		
		System.out.println("*** Finished: "+ (System.currentTimeMillis()-StartTime));
		MainGUI.getGUI().setProcessing(false);
	}
	
	/** If this file is set, then the Processor will immediately write results here after run is complete. USed mainly for the
	 * clusterRun option
	 * @author BLM*/
	public void setResultsFile(File f)
	{
		ResultsFile = f;
	}
	
	public Model_Well getWellForGivenImage(String fileName)
	{
		for (int p = 0; p < MainGUI.getGUI().getPlateHoldingPanel().getModel()
				.getNumPlates(); p++)
		{
			Model_Plate plate = MainGUI.getGUI().getPlateHoldingPanel()
					.getModel().getPlates()[p];
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
	
	/** Determines if the given file name is one to be processed
	 * @author BLM*/
	public boolean isWellToProcess(String fileName)
	{
		int num = WellsToProcess.length;
		for (int i =0; i < num; i++)
			if (fileName.indexOf(WellsToProcess[i].name)>0)
				return true;
		return false;
	}
	
	/** Toggles cluster run option
	 * @author BLM*/
	public void setClusterRun(boolean boo)
	{
		ClusterRun = boo;
	}
}


