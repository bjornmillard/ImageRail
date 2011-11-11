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

package run;

import java.util.ArrayList;

import models.Model_Main;
import models.Model_Well;
import processors.Processor_SingleCells;
import segmentors.DefaultSegmentor_v1;

public class Segment {

	/** For commandline segmentation */
	public static void main(String[] args) {
		try {
			if (args.length == 2) {

				// Reading commandline parameters
				// ARG_0 --- Project name that we want to process
				// ARG_1 ---> Path desired for new HDF5 file for stored data
				// ARG_2 ---> Plate start
				// ARG_3 ---> Well start
				// ARG_4 ---> Plate end
				// ARG_5 ---> Well end


				Model_Main TheModel = new Model_Main();
				// ARG_0 --> Input SDC to process
				if (args[0] != null)
					TheModel.loadProject(args[0], args[1]);


				ArrayList<Model_Well> TheWells = TheModel.getPlateRepository()
						.getAllWells();


				// // Only getting wells with Images that we can process
				int numWells = TheWells.size();
				ArrayList<Model_Well> wellsWIm = new ArrayList<Model_Well>();
				for (int i = 0; i < numWells; i++)
					if (TheWells.get(i).getFields() != null
							&& TheWells.get(i).getFields().length > 0)
						wellsWIm.add(TheWells.get(i));
				int numW = wellsWIm.size();
				Model_Well[] wellsWithImages = new Model_Well[numW];
				for (int i = 0; i < numW; i++)
					wellsWithImages[i] = wellsWIm.get(i);


				Processor_SingleCells tasker = new Processor_SingleCells(
						wellsWithImages, new DefaultSegmentor_v1());

				tasker.start();

			} else {
				String st = "\n\n\n\n";
				st += "***********************************************\n";
				st += "***********************************************\n";
				st += " ERROR:  " + args.length
						+ " --> Incorrect number of parameters\n\n";
				st += "Provide the following arguments for commandline processing:\n\n"
						+ "ARG_0 --- (String) Path name that we want to process\n"
						+ "ARG_1 ---> (String) Path desired for new HDF5 file for stored data\n"
						+ "ARG_2 ---> (int) Plate Index\n"
						+ "ARG_3 ---> (int) Well Index\n"
						+ "ARG_4 ---> (int) Field Index\n"
						+ "ARG_5 ---> (int) Channel to use for nuc segmentation\n"
						+ "ARG_6 ---> (int) Channel to use for Cyto segmentation\n"
						+ "ARG_7 ---> (int) Nucleus Threshold\n"
						+ "ARG_8 ---> (int) Cyto Threshold\n"
						+ "ARG_9 ---> (int) Bkgd Threshold\n"
						+ "ARG_10 ---> (String) CoordsToSave-> Centroid, BoundingBox, Outlines, Everything \n";
				st += "***********************************************\n";
				st += "***********************************************\n\n\n\n";
				System.out.println(st);
			}


		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * Loads the plate with the TIFF images in the given directory
	 * 
	 * @author BLM
	 */
	// static public void loadProject(String ProjectPath) {
	//
	// long sTime = System.currentTimeMillis();
	// try {
	// Model_Main TheMainModel = new Model_Main();
	// System.out.println("Loading Project: " + ProjectPath);
	// TheMainModel.setProjectDirectory(new File(ProjectPath));
	//
	// //Initialized the HDF5 I/O and creates the sample/well hash index
	// TheMainModel.initH5IO();
	// TheMainModel.getImageRailio().initHashtable();
	//
	// /*
	// * INIT MODEL_PLATES AND GUIs
	// */
	// // Looking for what sort of plates were loaded in this prior project
	// ArrayList<int[]> plateSizes = TheMainModel.getImageRailio()
	// .getPlateSizes();
	// //We will create X plates where X == maxPlateID+1;
	// int max = 0;
	// int pSize = 96;
	// for (int i = 0; i < plateSizes.size(); i++) {
	// int[] one = plateSizes.get(i);
	// int id = one[0];
	// if(id>max)
	// max = id;
	// if(i == 0)
	// pSize = one[1];
	// else
	// if(pSize!=one[1])
	// System.out.println("Project contains plates of different sizes*** This is currently not supported by ImageRail");
	// }
	// max++;
	// ArrayList<Model_Plate> arr = new ArrayList<Model_Plate>();
	// for (int i = 0; i < max; i++) {
	// int numR = (int) Math.ceil(Math.sqrt(pSize / 1.5f));
	// int numC = pSize / numR;
	// arr.add(new Model_Plate(numR, numC, i, true));
	// }
	//
	// Model_Plate[] plates = new Model_Plate[arr.size()];
	// for (int p = 0; p < plates.length; p++)
	// {
	// plates[p] = arr.get(p);
	// plates[p].initGUI();
	// }
	// // Creating the new plate holder with new plates
	// TheMainModel.setPlateRepository(new Model_PlateRepository(plates));
	//
	// String[] ChannelNames = null;
	// int numplates = plates.length;
	// for (int i = 0; i < numplates; i++) {
	//
	// Model_Plate plate = plates[i];
	//
	// File dir = new File(TheMainModel.getProjectDirectory()
	// .getAbsolutePath()
	// + File.separator + "Images" + File.separator + "plate_"
	// + i);
	//
	// // Looking for images for this plate in the projPath/Images
	// // directory
	//
	// // Getting Number of unique channel names and adding Features
	// // based off of wavelength - TODO features should be added
	// // better
	//
	// ChannelNames = tools.ImageTools
	// .getNameOfUniqueChannels(dir
	// .getParentFile());
	//
	//
	// /*
	// * Organizing the images and Initializing each Model_Field
	// */
	// if (dir != null && dir.exists()) {
	// for (int r = 0; r < plate.getNumRows(); r++)
	// for (int c = 0; c < plate.getNumColumns(); c++) {
	// // Getting all files tagged for this well
	// File[] allFiles = tools.ImageTools
	// .getFilesForGivenWell(dir, plate
	// .getWells()[r][c]);
	// // Organizing the images into sets of File[] in a an
	// // arraylist where each element of the arrList is a
	// // File[] of each wavelength for each field
	// ArrayList<File[]> allSets = tools.ImageTools
	// .getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
	// int numFields = allSets.size();
	//
	// Model_Well well = plate.getWells()[r][c];
	// well.setTheFields(new Model_Field[numFields]);
	// for (int j = 0; j < numFields; j++)
	// plate.getWells()[r][c].getFields()[j] = new Model_Field(
	// ((File[]) allSets.get(j)), j, well);
	// }
	// }
	//
	//
	// }
	//
	// TheMainModel.initFeatures(ChannelNames);
	//
	// // Trying to load the well mean data from the HDF file if
	// // exists
	//
	// for (int i = 0; i < numplates; i++)
	// plates[i].loadWellMeanAndStdevData();
	//
	// TheMainModel.loadFieldROIs();
	// TheMainModel.initScalingParameters();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	//
	// }
	

}
