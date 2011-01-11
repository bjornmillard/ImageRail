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

package run;

import gui.MainGUI;

import java.io.File;

import models.Model_ParameterSet;
import models.Model_Plate;
import models.Model_Well;
import processors.Processor_SingleCells;
import segmentors.DefaultSegmentor;

public class Segment {

	/** For commandline segmentation */
	public static void main(String[] args) {
		try {
			if (args.length == 7) {
				// Reading commandline parameters

				// ARG_0 --- Project name that we want to process
				File InputDir = null;
				try {
					InputDir = new File(args[0]);
				System.out.println("*****Processing Input Directory: "
						+ InputDir.getName());
				} catch (Exception e) {
					System.out
							.println("*****ERROR:  Problem parsing project path given ****** ");
					e.printStackTrace();
					System.exit(0);
				}

				// ARG_1
				int plateIndex = Integer.parseInt(args[1]);
				int wellIndex = Integer.parseInt(args[2]);

				// ARG_X-->Z ---> Input parameters for the segmentor being used
				float NucleusThreshold = Float.parseFloat(args[3]);
				float CytoThreshold = Float.parseFloat(args[4]);
				float BkgdThreshold = Float.parseFloat(args[5]);
				float CoordsToSave = Float.parseFloat(args[6]);


				System.out.println("     ------->    Nucleus Threshold: "
						+ NucleusThreshold);
				System.out.println("     ------->    Cyto Threshold: "
						+ CytoThreshold);
				System.out.println("     ------->    Bkgd Threshold: "
						+ BkgdThreshold);
				System.out.println("     ------->    CoordsToSave: "
						+ CoordsToSave);

				// Creating a new project for this job
				new MainGUI();
				MainGUI theGUI = MainGUI.getGUI();
				theGUI.setVisible(false);
				theGUI.initFilterManager();
				// Loading all new plugin files
				MainGUI.findAndCompileNewJavaFiles("features", null);

				/** Trying to load the project */
				try {

					theGUI.loadProject(InputDir);
					System.out.println("--->> SUCCESS LOADING PROJECT: "
							+ InputDir.getAbsolutePath());
					

					// theGUI.getPlateHoldingPanel().getThePlates()[0].getTheWells()[wellIndex];
					
				} catch (Exception e) {
					System.out
							.println("*****ERROR:  Problem Loading the given Project ****** ");
					e.printStackTrace();
					System.exit(0);
				}


				Model_Plate plate = gui.MainGUI.getGUI()
						.getThePlateHoldingPanel()
						.getPlates()[plateIndex];
					Model_Well well = plate.getAllWells()[wellIndex];

					// Now process the images
					processWell(well, NucleusThreshold,
							CytoThreshold, BkgdThreshold, (int) CoordsToSave);

			}
 else {
				String st = "\n\n\n\n";
				st += "***********************************************\n";
				st += "***********************************************\n";
				st += " ERROR:  Incorrect number of parameters\n\n";
				st += "		Provide the following arguments for commandline processing:\n\n"
						+ "[PathToProject   PlateIndex   WellIndex  ... \n"
						+ "	NuclearThreshold   CytoplasmicThreshold  BkgdThreshold ...\n"
						+ "		CoordsToSave(0=centroids,1=bbox,2=outlines, 3=everything)]\n";
				st += "***********************************************\n";
				st += "***********************************************\n\n\n\n";
				System.out.println(st);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * For commandline image directory processing
	 * 
	 * @author BLM
	 * 
	 * */
	static public void processWell(Model_Well well,
			float Threshold_Nucleus, float Threshold_CellBoundary,
			float Threshold_Background, int CoordsToSave) {

		//
		// INIT parameters
		//	
		int NucBoundaryChannel = 0;
			int CytoBoundaryChannel = 0;
				Model_ParameterSet pset = well.TheParameterSet;
				pset.setModified(true);
				// ProcessType
				pset.setProcessType(Model_ParameterSet.SINGLECELL);
				// Threshold Channel Nucleus
				pset.setThresholdChannel_nuc_Name(MainGUI.getGUI()
						.getTheChannelNames()[NucBoundaryChannel]);
				// Threshold Channel Cytoplasm
				pset.setThresholdChannel_cyto_Name(MainGUI.getGUI()
						.getTheChannelNames()[CytoBoundaryChannel]);
				// Nuc bound threshold
				pset.setThreshold_Nucleus(Threshold_Nucleus);
				// Cell bound Threshold
				pset.setThreshold_Cell(Threshold_CellBoundary);
				// Bkgd threshold
				pset.setThreshold_Background(Threshold_Background);

				if (CoordsToSave == 0)
					pset.setCoordsToSaveToHDF("Centroid");
				else if (CoordsToSave == 1)
					pset.setCoordsToSaveToHDF("BoundingBox");
				else if (CoordsToSave == 2)
					pset.setCoordsToSaveToHDF("Outlines");
				else if (CoordsToSave == 3)
					pset.setCoordsToSaveToHDF("Everything");

				well.TheParameterSet
						.setMeanOrIntegrated(well.TheParameterSet.MEAN);

				// Finding the index of this channel name
				for (int j = 0; j < MainGUI.getGUI().getTheChannelNames().length; j++)
					if (MainGUI.getGUI().getTheChannelNames()[j]
							.equalsIgnoreCase(pset
									.getThresholdChannel_nuc_Name()))
						pset.setThresholdChannel_nuc_Index(j);
				// Finding the index of this channel name
				for (int j = 0; j < MainGUI.getGUI().getTheChannelNames().length; j++)
					if (MainGUI.getGUI().getTheChannelNames()[j]
							.equalsIgnoreCase(pset
									.getThresholdChannel_cyto_Name()))
						pset.setThresholdChannel_cyto_Index(j);

			if (Threshold_Background > 0)
				MainGUI.getGUI().setBackgroundSubtract(true);
		//
		//
		//

		// Adding the well to process
		Model_Well[] allWells = new Model_Well[1];
		allWells[0] = well;

		Processor_SingleCells tasker = new Processor_SingleCells(allWells,
				new DefaultSegmentor());
		tasker.start();

		// Now just wait for the tasker Thread to finish its job
		while (tasker.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
