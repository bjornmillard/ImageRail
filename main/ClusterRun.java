/**
 * ClusterRun.java
 *
 * @author Created by Omnicore CodeGuide
 */

package main;

import java.io.File;

import segmentors.DefaultSegmentor;

public class ClusterRun
{
	/** Main gui start call
	 * @param File DirectoryToProcess
	 * @param String ProcessType--> "WellMeans" or "SingleCells"
	 * @param float NucleusThreshold
	 * @param float CytoThreshold
	 * @param float BkgdThreshold
	 *
	 * @author BLM
	 * */
	public static void main(String[] args)
	{
		try
		{
			File InputDir = new File(args[0]);
			File OutputDir = new File(InputDir.getAbsolutePath()+File.separator+"Output");
			OutputDir.mkdir();
			float wellsPerPlate = Float.parseFloat(args[1]);
			float NucleusThreshold = Float.parseFloat(args[2]);
			float CytoThreshold = Float.parseFloat(args[3]);
			float BkgdThreshold = Float.parseFloat(args[4]);
			
			System.out.println("*****Processing Input Directory: "+InputDir.getName());
			System.out.println("     ------->    Wells per Plate: "
					+ wellsPerPlate);
			System.out.println("     ------->    Nucleus Threshold: "+NucleusThreshold);
			System.out.println("     ------->    Cyto Threshold: "+CytoThreshold);
			System.out.println("     ------->    Bkgd Threshold: "+BkgdThreshold);
			
			
			//
			

			// if (ProcessType.equalsIgnoreCase("SingleCell"))
			if (true)
			{
				DefaultSegmentor segmentor = new DefaultSegmentor();
				File[] subDirs = InputDir.listFiles();
				int len = subDirs.length;
				int counter=0;
				for (int i = 0; i < len; i++)
					if (shouldProcess(subDirs[i].getName()))
						counter++;
				System.out.println("Number of Plate Directories Found: "
						+ counter);
				
				// Init the project and plates
				int numRows = 0;
				int numCols = 0;
				if (wellsPerPlate == 96) {
					numRows = 8;
					numCols = 12;
				} else if (wellsPerPlate == 384) {
					numRows = 16;
					numCols = 24;
				}

				// // Creating a new project for this job
				// new main.MainGUI();
				// main.MainGUI.getGUI().setVisible(false);
				//				
				// boolean worked = main.MainGUI.getGUI()
				// .initNewPlates_ClusterRun(counter,
				// numRows, numCols);
				// if (worked) {
				// main.MainGUI.getGUI().setVisible(true);
				// }


				// for (int i = 0; i < len; i++)
				// {
				// String name = subDirs[i].getName();
				// if (shouldProcess(name))
				// {
				// MainGUI gui = MainGUI.getGUI();
				// Plate plate =
				// gui.getThePlateHoldingPanel().getThePlates()[0];
				// File f = subDirs[i];
				// System.out.println("***Processing: "+f.getAbsolutePath());
				//						
				// gui.load(f, plate);
				// Well[] wells = plate.getAllWells_wImages();
				// setParameters(wells, 0,0, NucleusThreshold, CytoThreshold,
				// BkgdThreshold);
				//						
				// Processor_SingleCells processor = new
				// Processor_SingleCells(wells, segmentor);
				// processor.setResultsFile(new
				// File(OutputDir.getAbsolutePath()+File.separator+f.getName()+"_results_"+System.currentTimeMillis()+".csv"));
				// System.out.println("***Starting**** "+f.getName());
				// processor.start();
				//						
				// }
				// }
			}
			// else if (ProcessType.equalsIgnoreCase("WellMeans"))
			// {
			// File[] subDirs = InputDir.listFiles();
			// int len = subDirs.length;
			// int counter=0;
			// for (int i = 0; i < len; i++)
			// if (subDirs[i].getName().indexOf("DS")<0)
			// counter++;
			//				
			// System.out.println("Number of Input Directories Found: "+counter);
			// MainGUI gui = MainGUI.getGUI();
			// for (int i = 0; i < len; i++)
			// {
			// String name = subDirs[i].getName();
			// if (shouldProcess(name))
			// {
			// Plate plate = gui.getPlateHoldingPanel().getThePlates()[0];
			// File f = subDirs[i];
			// System.out.println("***Processing: "+f.getAbsolutePath());
			//						
			// gui.load(f, plate);
			// Well[] wells = plate.getAllWells_wImages();
			// setParameters(wells, 0,0, NucleusThreshold, CytoThreshold,
			// BkgdThreshold);
			//						
			// Processor_WellAverage processor = new
			// Processor_WellAverage(wells);
			//						
			//						
			// processor.setResultsFile(new
			// File(OutputDir.getAbsolutePath()+File.separator+f.getName()+"_results_"+System.currentTimeMillis()+".csv"));
			// System.out.println("***Starting**** "+f.getName());
			// processor.start();
			// gui.setProcessing(true);
			// }
			// //Waiting till the processor is done with this plate
			// while (gui.isProcessing())
			// Thread.sleep(1000);
			//				
			// }
			// }
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	
	static public void setParameters(Well[] TheWells, int NucBoundaryChannel,
			int CytoBoundaryChannel, float Threshold_Nucleus,
			float Threshold_CellBoundary, float Threshold_Background)
	{
		//Storing the Parameters for each Well
		MainGUI gui = MainGUI.getGUI();
		int len = TheWells.length;
		for (int i = 0; i < len; i++)
		{
			Well well = TheWells[i];
			ParameterSet pset = well.TheParameterSet;
			pset.setModified(true);
			//ProcessType
			pset.setProcessType ( ParameterSet.SINGLECELL);
			//Threshold Channel Nucleus
			pset.setThresholdChannel_nuc_Name( gui.getTheChannelNames()[NucBoundaryChannel]);
			//Threshold Channel Cytoplasm
			pset.setThresholdChannel_cyto_Name (gui.getTheChannelNames()[CytoBoundaryChannel]);
			//Nuc bound threshold
			pset.setThreshold_Nucleus(Threshold_Nucleus);
			//Cell bound Threshold
			pset.setThreshold_Cell( Threshold_CellBoundary);
			//Bkgd threshold
			pset.setThreshold_Background ( Threshold_Background);
			
			well.TheParameterSet.setMeanOrIntegrated( well.TheParameterSet.MEAN);
			
			//Finding the index of this channel name
			for (int j = 0; j < gui.getTheChannelNames().length; j++)
				if (gui.getTheChannelNames()[j].equalsIgnoreCase(pset.getThresholdChannel_nuc_Name()))
					pset.setThresholdChannel_nuc_Index ( j);
			//Finding the index of this channel name
			for (int j = 0; j < gui.getTheChannelNames().length; j++)
				if (gui.getTheChannelNames()[j].equalsIgnoreCase(pset.getThresholdChannel_cyto_Name()))
					pset.setThresholdChannel_cyto_Index (j);
		}
		if (Threshold_Background>0)
			gui.setBackgroundSubtract(true);
	}
	
	
	static public boolean shouldProcess(String fileName)
	{
		if (fileName.indexOf("DS")<0
			&& fileName.indexOf("Ignore")<0
			&& fileName.indexOf("Output")<0)
			return true;
		
		return false;
		
	}
	
}

