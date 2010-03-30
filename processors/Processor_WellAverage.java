/**
 * Task_processDirectory_PrintMIDAS_TIFF.java
 *
 * @author Created by Omnicore CodeGuide
 */

package processors;
import java.io.File;

import main.MainGUI;
import main.Plate;
import main.Well;
import segmentors.DefaultSegmentor;
import us.hms.systemsbiology.data.HDFConnectorException;
import us.hms.systemsbiology.data.ProjectHDFConnector;
import us.hms.systemsbiology.data.SegmentationHDFConnector;
import dataSavers.DataSaver_CSV;
import features.Feature;

public class Processor_WellAverage extends Thread implements Processor
{
	private Well[] WellsToProcess;
	private File ResultsFile;
	private boolean ClusterRun;
	
	public Processor_WellAverage(Well[] wellsToProcess)
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
		
		
		SegmentationHDFConnector sCon = null;
		try
		{
			String projPath = main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
			ProjectHDFConnector con = new ProjectHDFConnector(projPath);
			con.createProject();
			sCon = new SegmentationHDFConnector(projPath);
			//creating a HDF plate for each plate needed, since the wells could come from different plates
			int[][] idsAndWells = Processor_SingleCells.getAllUniquePlateIDsAndNumWells(WellsToProcess);
			for (int i = 0; i < idsAndWells[0].length; i++)
			{
//				System.out.println("ids: "+(idsAndWells[0][i]-1) +"   wells: "+idsAndWells[1][i]);
				con.writePlateSize(idsAndWells[0][i]-1, idsAndWells[1][i]);
			}
		}
		catch (HDFConnectorException e) {e.printStackTrace();}
		
		
		
		
		//Processing all the wells
		for (int w = 0; w < numWells; w++)
		{
			Well well = WellsToProcess[w];
			well.processing = true;
			MainGUI.getGUI().getPlateHoldingPanel().updatePanel();
			System.out.println("******** Processing Well: "+ well.name +" ********");
			
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
				File[] images_oneField = well.getFields()[f].getImageFiles();
				int[][][] Raster_Channels = tools.ImageTools.getImageRaster_FromFiles_copy(images_oneField);
				//Getting integrated values of the images for each channel
				float[][] tempIntegration = DefaultSegmentor.findTotalIntegrationAndTotalPixUsed(Raster_Channels, well.TheParameterSet);
				//storing this data
				for (int p=0; p< tempIntegration.length; p++)
					totalIntegration[p]+=tempIntegration[p][0];
				totalPix+=tempIntegration[0][1];
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
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Whole_")>=0 && f.toString().indexOf("(Mean)")>=0)
				{
					temp_all[i] = totalIntegration[counter]/totalPix;
					counter++;
				}
				else if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Whole_")>=0 && f.toString().indexOf("(Integrated)")>=0)
				{
					temp_all[i] = totalIntegration[counterInt];
					counterInt++;
				}
			}
			
			//Subtracting background if desired
			for (int i = 0; i < numF; i++)
			{
				Feature f =	 (Feature)MainGUI.getGUI().getTheFeatures().get(i);
				if (f.ChannelName.indexOf("w")>=0 && f.ChannelName.indexOf("Whole_")>=0 && f.toString().indexOf("(Mean)")>=0)
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
				int plateIndex = well.getPlate().getID()-1;
				Feature[] features = main.MainGUI.getGUI().getFeatures();
				StringBuffer[] featureNames = null;
				if(features!=null && features.length>0)
				{
					featureNames = new StringBuffer[features.length];
					for (int i = 0; i < features.length; i++)
						featureNames[i] = new StringBuffer(features[i].toString());
				}
				
				if(well.Feature_Means!=null && sCon!=null)
				{
					sCon.writeWellMeanValues(plateIndex, wellIndex, well.Feature_Means);
					if(featureNames!=null)
						sCon.writeMeanFeatureNames(plateIndex, featureNames);
				}
				if(well.Feature_Stdev!=null && sCon!=null)
					sCon.writeWellStdDevValues(plateIndex, wellIndex, well.Feature_Stdev);
				
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
	
	public Well getWellForGivenImage(String fileName)
	{
		for (int p = 0; p < MainGUI.getGUI().getPlateHoldingPanel().getNumPlates(); p++)
		{
			Plate plate = MainGUI.getGUI().getPlateHoldingPanel().getThePlates()[p];
			int rows = plate.getNumRows();
			int cols = plate.getNumColumns();
			for (int r = 0; r< rows; r++)
				for (int c= 0; c < cols; c++)
				{
					if (fileName.indexOf(plate.getTheWells()[r][c].name)>0)
						return plate.getTheWells()[r][c];
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


