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

import imagerailio.ImageRail_SDCube;

import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import models.Model_Field;
import models.Model_Plate;
import models.Model_PlateRepository;
import models.Model_Well;
import segmentedobject.CellCoordinates;
import features.Feature;
import features.FeatureSorter;

public class Segment2 {
	static public ArrayList<Feature> features;
	static public File TheProjectDirectory;
	static public ImageRail_SDCube TheImageRail_H5IO;
	static public Model_PlateRepository ThePlateRepository;

	/** For commandline segmentation */
	public static void main(String[] args) {
		try {
			if (args.length == 1) {

				// Reading commandline parameters
				// ARG_0 --- Project name that we want to process
				// ARG_1 ---> Path desired for new HDF5 file for stored data
				// ARG_2 ---> Plate Index
				// ARG_3 ---> Well Index
				// ARG_4 ---> Field Index



				// ARG_0 --> Input SDC to process
				if (args[0] != null)
					loadProject(args[0]);

				// if (false) {
				// // ARG_1 ---> Path desired for new HDF5 file for stored data
				// File f_out = new File(args[1]);
				// File newF = new File(f_out.getAbsolutePath() + ".sdc");
				// newF.mkdir();
				// ImageRail_SDCube io_out = new ImageRail_SDCube(
				// newF.getAbsolutePath());
				// try {
				// io_out.createProject();
				// } catch (H5IO_Exception e) {
				// e.printStackTrace();
				// }
				// // Attempt To init the hashtable
				// io_out.initHashtable();
				//
				//
				//
				// // (2) Converting the images files to a raster
				// int[][][] raster = tools.ImageTools
				// .getImageRaster_FromFiles_copy(files, channelNames);
				// // (3) Computing the background from each channel
				// float[] backgroundValues = new float[numChannels];
				// if (BkgdThreshold > 0)
				// tools.ImageTools.computeBackgroundValues(raster,
				// backgroundValues, pset);
				// // (4) Getting Cell Coordinates (segmenting the cells)
				// ArrayList<CellCoordinates> cellCoords = new
				// DefaultSegmentor_v1()
				// .segmentCells(raster, pset);
				//
				// // (5) Initializing all the data values calculated via the
				// Cell
				// // coordinates, the Raster, and the loaded Feature objects
				// // EX: Now that we have the pixel coordinates that make up
				// each
				// // cell we need to look at the
				// // image and extract the proper values
				// System.out.println("-->> Performing Feature Computations");
				// float[][] cellFeatureMatrix =
				// computeFeatureValues(cellCoords,
				// raster, backgroundValues);
				//
				// System.out.println(cellFeatureMatrix.length + "x"
				// + cellFeatureMatrix[0].length);
				//
				// if (cellFeatureMatrix != null && cellFeatureMatrix.length >
				// 0) {
				//
				// //
				// // Now writing Cell coordinate data to HDF file
				// System.out
				// .println("------------ Caching cell data Matrix and Coordinates to HDF file: ------------");
				// long time = System.currentTimeMillis();
				//
				// // -------------- Store cells in HDF5
				// // -------------------------------------------------------
				// try {
				// int[] fieldDimensions = { raster.length,
				// raster[0].length, raster[0][0].length };
				// String well_ID = "p" + plateIndex + "w" + wellIndex
				// + "_t"
				// + imagerailio.ImageRail_SDCube.getTimeStamp();
				// io.createField(well_ID, plateIndex, wellIndex,
				// fieldIndex, fieldDimensions, null);
				// io.writePlateCountAndSizes(1, 96);
				//
				// // Writing data matrix to HDF
				// io.writeFeatures(plateIndex, wellIndex, fieldIndex,
				// cellFeatureMatrix);
				// // Writing the feature names to file
				//
				// String[] fNames = new String[features.size()];
				// for (int i = 0; i < features.size(); i++)
				// fNames[i] = new String(features.get(i).toString());
				// io.writeFeatureNames(plateIndex, wellIndex, fieldIndex,
				// fNames);
				//
				// String whatToSave =
				// pset.getParameter_String("CoordsToSaveToHDF");
				// if (whatToSave.equalsIgnoreCase("BoundingBox")) {
				// // Only save the cell BoundingBoxes to file
				// ArrayList<CellCoordinates> bbox =
				// segmentedobject.CellCoordinates
				// .getBoundingBoxOfCoordinates(cellCoords);
				// io.writeCellBoundingBoxes(plateIndex, wellIndex,
				// fieldIndex, bbox);
				//
				// killCellCoordinates(bbox);
				// killCellCoordinates(cellCoords);
				// } else if (whatToSave.equalsIgnoreCase("Centroid")) {
				// // Only save the cell Centroids to file
				// ArrayList<CellCoordinates> centroids =
				// segmentedobject.CellCoordinates
				// .getCentroidOfCoordinates(cellCoords);
				// io.writeCellCentroids(plateIndex, wellIndex,
				// fieldIndex, centroids);
				//
				// killCellCoordinates(centroids);
				// killCellCoordinates(cellCoords);
				// } else if (whatToSave.equalsIgnoreCase("Outlines")) {
				// // Only save the cell outlines to file
				// ArrayList<CellCoordinates> outlines =
				// segmentedobject.CellCoordinates
				// .getSingleCompartmentCoords(cellCoords,
				// "Outline");
				// io.writeWholeCells(plateIndex, wellIndex,
				// fieldIndex, outlines);
				//
				// killCellCoordinates(outlines);
				// killCellCoordinates(cellCoords);
				// } else if (whatToSave.equalsIgnoreCase("Everything")) {
				// io.writeWholeCells(plateIndex, wellIndex,
				// fieldIndex, cellCoords);
				// killCellCoordinates(cellCoords);
				// }
				//
				// if (Math.random() > 0.7)
				// System.gc();
				// } catch (Exception e) {
				// // Handle this exception!!!
				// e.printStackTrace();
				// }
				// System.out.println("Done writing: "
				// + (System.currentTimeMillis() - time));
				// time = System.currentTimeMillis();
				//
				// } else
				// System.out
				// .println("-----**No Cells Found in this well with the given parameter **-----");
				//
				// raster = null;
				// System.gc();
				// }
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

	/** */
	static void initFeatures(String[] channelNames) {

		ArrayList<Feature> arr = new ArrayList<Feature>();
		try {
			// Try to load features from src tree, otherwise try deployed
			// location
			File f = new File("./src/features");
			if (!f.exists())
				f = new File("./features");
			File[] fs = f.listFiles();

			int len = fs.length;

			for (int i = 0; i < len; i++) {
				if (fs[i].getAbsolutePath().indexOf(".java") > 0
						&& !fs[i].getName().equalsIgnoreCase("Feature.java")
						&& !fs[i].getName().equalsIgnoreCase(
								"FeatureSorter.java")) {
					String path = fs[i].getName();
					int ind = path.indexOf(".java");
					path = path.substring(0, ind);
					// System.out.println("Loading Feature: "+ path);
					Class c = Class.forName("features." + path);
					arr.add((Feature) c.newInstance());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int len = arr.size();
		features = new ArrayList<Feature>();
		// System.out.println("Found "+len +" Features");
		for (int i = 0; i < len; i++) {
			Feature f = (arr.get(i));
			f.Name = f.getClass().toString();

			if (f.isMultiSpectralFeature() && channelNames != null) {
				for (int w = 0; w < channelNames.length; w++) {
					try {
						Feature fn = f.getClass().newInstance();
						fn.setChannelIndex(w);
						fn.setChannelName(channelNames[w]);
						fn.setName(channelNames[w]);
						features.add(fn);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else
				features.add(f);
		}

		FeatureSorter sorter = new FeatureSorter();
		Collections.sort(features, sorter);

	}

	/**
	 * Computes the main data matrix of size NumCells x numFeatures.
	 * 
	 * @param ArrayList
	 *            <Cell_coords> cells
	 * @param int[][][] raster
	 * @author BLM
	 */
	static float[][] computeFeatureValues(ArrayList<CellCoordinates> cells,
			int[][][] raster, float[] backgroundValues) {

		int numFeatures = features.size();

		if (cells == null || cells.size() == 0)
			return null;

		int numC = cells.size();
		float[][] data = new float[numC][numFeatures];

		long[] intTime = new long[numFeatures];

		for (int n = 0; n < numC; n++) {
			CellCoordinates cell = cells.get(n);
			for (int f = 0; f < numFeatures; f++) {
				long time = System.currentTimeMillis();
				data[n][f] = features.get(f).getValue(cell, raster,
						backgroundValues);
				intTime[f] += (System.currentTimeMillis() - time);
			}
		}

		return data;
	}

	/**
	 * Attempts to free up all the memory that was consumed by the given cells
	 * 
	 * @author BLM
	 */
	static public void killCellCoordinates(ArrayList<CellCoordinates> cells) {
		int len = cells.size();
		for (int i = 0; i < len; i++) {
			CellCoordinates cell = cells.get(i);
			for (int j = 0; j < cell.getComSize(); j++) {
				imagerailio.Point[] pts = cell.getComCoordinates(j);
				for (int z = 0; z < pts.length; z++)
					pts[z] = null;

				pts = null;
			}
			cell = null;
		}
		cells = null;
	}

	/**
	 * Loads the given SDC project file
	 * 
	 * @author BLM
	 */
	static public void loadProject(String projPath) {

		long sTime = System.currentTimeMillis();
		try {
			System.out.println("Loading Project: " + projPath);
			TheProjectDirectory = new File(projPath);
			// setProjectDirectory(ProjectDir);
			// Initialized the HDF5 I/O and creates the sample/well hash index
			initH5IO();
			TheImageRail_H5IO.initHashtable();

			// Looking for what sort of plates were loaded in this prior project
			ArrayList<int[]> plateSizes = TheImageRail_H5IO.getPlateSizes();
			// We will create X plates where X == maxPlateID+1;
			int max = 0;
			int pSize = 96;
			for (int i = 0; i < plateSizes.size(); i++) {
				int[] one = plateSizes.get(i);
				int id = one[0];
				if (id > max)
					max = id;
				if (i == 0)
					pSize = one[1];
				else if (pSize != one[1])
					System.out
							.println("Project contains plates of different sizes*** This is currently not supported by ImageRail");
			}
			max++;

			ArrayList<Model_Plate> arr = new ArrayList<Model_Plate>();
			for (int i = 0; i < max; i++) {
				int numR = (int) Math.sqrt(pSize / 1.5f);
				int numC = pSize / numR;
				arr.add(new Model_Plate(numR, numC, i, false));
			}

			// Creating the new plate holder with new plates
			Model_Plate[] plates = new Model_Plate[arr.size()];
			for (int p = 0; p < plates.length; p++) {
				plates[p] = arr.get(p);
			}

			ThePlateRepository = new Model_PlateRepository(plates);

			String[] ChannelNames = null;
			int numplates = plates.length;
			for (int i = 0; i < numplates; i++) {
				Model_Plate plate = plates[i];

				// Trying to load the well mean data from the HDF file if exists
				// plate.loadWellMeanAndStdevData();

				// Looking for images for this plate in the projPath/Images
				// directory
				File dir = new File(TheProjectDirectory.getAbsolutePath()
						+ File.separator + "Images" + File.separator + "plate_"
						+ i);

				// Getting Number of unique channel names and adding Features
				// based off of wavelength
				ChannelNames = tools.ImageTools.getNameOfUniqueChannels(dir
						.getParentFile());

				if (dir != null && dir.exists()) {
					for (int r = 0; r < plate.getNumRows(); r++)
						for (int c = 0; c < plate.getNumColumns(); c++) {
							// Getting all files tagged for this well
							File[] allFiles = tools.ImageTools
									.getFilesForGivenWell(dir,
											plate.getWells()[r][c]);
							// Organizing the images into sets of File[] in a an
							// arraylist where each element of the arrList is a
							// File[] of each wavelength for each field
							ArrayList<File[]> allSets = tools.ImageTools
									.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
							int numFields = allSets.size();

							Model_Well well = plate.getWells()[r][c];
							well.setTheFields(new Model_Field[numFields]);
							for (int j = 0; j < numFields; j++)
								plate.getWells()[r][c].getFields()[j] = new Model_Field(
										((File[]) allSets.get(j)), j, well);
						}
				}

			}

			initFeatures(ChannelNames);
			loadFieldROIs();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * If fields already exist int he data.h5 file, then see if we should load
	 * ROIs
	 * 
	 * @author Bjorn Millard
	 * */
	static public void loadFieldROIs() {
		String h5path = TheProjectDirectory.getAbsolutePath() + File.separator
				+ "Data.h5";
		// Iterating through all fields and checking if they have ROIs to load
		Enumeration<String> keys = TheImageRail_H5IO.getHashtable().keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.indexOf("f") >= 0) {
				int plateIndex = Integer.parseInt((key.substring(1,
						key.indexOf("w"))));
				int wellIndex = Integer.parseInt(key.substring(
						key.indexOf("w") + 1, key.indexOf("f")));
				int fieldIndex = Integer.parseInt(key.substring(
						key.indexOf("f") + 1, key.length()));

				Model_PlateRepository rep = ThePlateRepository;
				Model_Well well = rep.getWell(plateIndex, wellIndex);

				String fieldPath = TheImageRail_H5IO.getHashtable().get(key);
				ArrayList<Polygon> rois = TheImageRail_H5IO.readROIs(h5path,
						fieldPath);
				if (rois != null) {
					int num = rois.size();
					for (int i = 0; i < num; i++) {
						well.getFields()[fieldIndex].setROI(rois.get(i));
					}
				}
			}

		}
	}

	/**
	 * Constructs a new ImageRail_IO HDF project connector with the currently
	 * loaded project directory
	 * 
	 * @author BLM
	 */
	static public ImageRail_SDCube initH5IO() {
		String projPath = TheProjectDirectory.getAbsolutePath();
		TheImageRail_H5IO = null;
		try {
			TheImageRail_H5IO = new ImageRail_SDCube(projPath);
		} catch (Exception e) {
			System.out.println("Error creating the ImageRail_SDCube: ");
			e.printStackTrace();
		}

		return TheImageRail_H5IO;
	}
}
