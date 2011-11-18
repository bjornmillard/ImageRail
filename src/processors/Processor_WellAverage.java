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
import imagerailio.ImageRail_SDCube;

import java.io.File;

import models.Model_Field;
import models.Model_Plate;
import models.Model_Well;
import sdcubeio.H5IO_Exception;
import segmentors.DefaultSegmentor_v1;

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
		models.Model_Main.getModel().setProcessing(true);
		String[] featureNames = null;

		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		
		if (io != null)
			try {

				Feature[] features = models.Model_Main.getModel().getFeatures();
				if (features != null && features.length > 0) {
					featureNames = new String[features.length];
					for (int i = 0; i < features.length; i++)
						featureNames[i] = features[i].toString();

				}
		//Processing all the wells
		for (int w = 0; w < numWells; w++)
		{
			if (!models.Model_Main.getModel().shouldStop())
				break;

			Model_Well well = WellsToProcess[w];
			well.processing = true;
			models.Model_Main.getModel().getPlateRepository_GUI().updatePanel();
			System.out.println("******** Processing Model_Well: "+ well.name +" ********");
			
			//Initializing mean value storage variables
			float[] totalIntegration = new float[models.Model_Main.getModel().getTheFeatures().size()];
			for (int p=0; p< totalIntegration.length; p++)
				totalIntegration[p]=0;
			float[] meanValues = new float[models.Model_Main.getModel().getTheFeatures().size()];
			for (int p=0; p< meanValues.length; p++)
				meanValues[p]=0;
			
			int numFields = well.getFields().length;
			int totalPix=0;
			for (int f = 0; f < numFields; f++)
			{
				if (!models.Model_Main.getModel().shouldStop())
					break;

				Model_Field field = well.getFields()[f];
				File[] images_oneField = field.getImageFiles();
				int[][][] Raster_Channels = tools.ImageTools
						.getImageRaster_FromFiles_copy(images_oneField,
								models.Model_Main.getModel().getTheChannelNames());
				int[] fieldDimensions = { Raster_Channels.length,
						Raster_Channels[0].length, Raster_Channels[0][0].length };
				try {
					io.openHDF5(io.OUTPUT);
					io.createField(well.getID(), well.getPlate().getID(), well
.getWellIndex(), f,
									fieldDimensions, models.Model_Main
											.getModel().getExpDesignConnector());
					io.closeHDF5();
				} catch (H5IO_Exception e) {
					System.out
							.println("** Error creating field in HDF5 file **");
					e.printStackTrace();
				}
				// Getting integrated values of the images for each channel
				float[][] tempIntegration = DefaultSegmentor_v1
						.findTotalIntegrationAndTotalPixUsed(Raster_Channels,
								field.getParameterSet());
				// storing this data
				for (int p = 0; p < tempIntegration.length; p++)
					totalIntegration[p] += tempIntegration[p][0];
				totalPix += tempIntegration[0][1];
				Raster_Channels = null;

						// Storing Parameters used to process this field
						String hdfPath = models.Model_Main.getModel()
								.getOutputProjectPath()
								+ "/Data.h5";
						field.getParameterSet().writeParameters(hdfPath,
								well.getPlate().getID(), well.getWellIndex(),
								field.getIndexInWell());

						// Storing the feature names computed for this field
						if (featureNames != null)
						io.writeFeatureNames(well.getPlate().getID(),
								well.getWellIndex(), field.getIndexInWell(),
								featureNames);

			}
			
			
			//Computing the features now
			int numF = models.Model_Main.getModel().getTheFeatures().size();
			float[] temp_all = new float[numF];
			int counter = 0;
			int counterInt =0;
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)models.Model_Main.getModel().getTheFeatures().get(i);
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
			// for (int i = 0; i < numF; i++)
			// {
			// Feature f = (Feature)models.Model_Main.getModel().getTheFeatures().get(i);
			// if (f.Name.indexOf("w")>=0 && f.Name.indexOf("Whole_")>=0 &&
			// f.toString().indexOf("(Mean)")>=0)
			// {
			// float bkgd =
			// field.getParameterSet().getParameter("Thresh_Bkgd_Value");
			// temp_all[i] = temp_all[i] - bkgd;
			// }
			// }
			
			
			well.setMeanFluorescentValues(temp_all);
			well.processing = false;
			
			if(models.Model_Main.getModel().getGUI()!=null)
			{
				models.Model_Main.getModel().getPlateRepository_GUI().updatePanel();
						models.Model_Main.getModel().getGUI().updateAllPlots();
			}
			
			try
			{
				//Trying to write mean value data to file
				int wellIndex = (well.getPlate().getNumRows()*well.Column)+well.Row;
				int plateIndex = well.getPlate().getID();

				io.openHDF5(io.OUTPUT);
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
				io.closeHDF5();


			}
			catch (Exception e)
			{
				// Handle this exception!!!
				System.out.println("ERROR: writing to well mean/stdev HDF file");
			}
		}
		
		
			} catch (Exception e) {
				// Make sure the HDF5 file is closed to prevent corruption
				try {
					io.getH5IO().closeAll();
				} catch (H5IO_Exception e1) {
					System.out
							.println("**ERROR closing HFD5 file during crash");
					e1.printStackTrace();
				}
			}

		System.out.println("*** Finished: "+ (System.currentTimeMillis()-StartTime));
		models.Model_Main.getModel().setProcessing(false);
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
		for (int p = 0; p < models.Model_Main.getModel().getPlateRepository_GUI().getModel()
				.getNumPlates(); p++)
		{
			Model_Plate plate = models.Model_Main.getModel().getPlateRepository_GUI()
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


