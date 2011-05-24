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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import models.Model_ParameterSet;
import sdcubeio.H5IO_Exception;
import segmentedobject.CellCoordinates;
import segmentors.DefaultSegmentor;
import features.Feature;
import features.FeatureSorter;

public class Segment {
	static ArrayList<Feature> features;

	/** For commandline segmentation */
	public static void main(String[] args) {
		try {
			if (args.length == 11) {

				// Reading commandline parameters
				// ARG_0 --- Project name that we want to process
				// ARG_1 ---> Path desired for new HDF5 file for stored data
				// ARG_2 ---> Plate Index
				// ARG_3 ---> Well Index
				// ARG_4 ---> Field Index
				// ARG_5 ---> Channel to use for nuc segmentation
				// ARG_6 ---> Channel to use for Cyto segmentation
				// ARG_7 ---> Nucleus Threshold
				// ARG_8 ---> Cyto Threshold
				// ARG_9 ---> Bkgd Threshold
				// ARG_10 ---> CoordsToSave-> "Centroid", "BoundingBox",
				// "Outlines", "Everything"



				// ARG_0 --- Project name that we want to process
				File in = null;
				try {
					in = new File(args[0]);
					System.out.println("*****Processing Input Directory: "
							+ in.getName());
				} catch (Exception e) {
					System.out
							.println("*****ERROR:  Problem parsing project path given ****** ");
					e.printStackTrace();
					System.exit(0);
				}

				// INIT PSET for segmentation
				Model_ParameterSet pset = new Model_ParameterSet();
				// (0) init a pset
				pset.setModified(true);
				// ProcessType
				pset.setProcessType(Model_ParameterSet.SINGLECELL);

				// ARG_1 ---> Path desired for new HDF5 file for stored data
				File f_out = new File(args[1]);
				File newF = new File(f_out.getAbsolutePath() + ".sdc");
				newF.mkdir();
				ImageRail_SDCube io = new ImageRail_SDCube(newF
						.getAbsolutePath());
				try {
					io.createProject();
				} catch (H5IO_Exception e) {
					e.printStackTrace();
				}
				// Attempt To init the hashtable
				io.initHashtable();

				// ARG_2 ---> Plate Index
				int plateIndex = Integer.parseInt(args[2]);
				// ARG_3 ---> Well Index
				int wellIndex = Integer.parseInt(args[3]);
				// ARG_4 ---> Field Index
				int fieldIndex = Integer.parseInt(args[4]);
				// ARG_5 ---> Channel to use for nuc segmentation
				int nucChannel_Index = Integer.parseInt(args[5]);
				pset.setThresholdChannel_nuc_Index(nucChannel_Index);
				// ARG_6 ---> Channel to use for Cyto segmentation
				int cytoChannel_Index = Integer.parseInt(args[6]);
				pset.setThresholdChannel_cyto_Index(cytoChannel_Index);
				// ARG_7 ---> Nucleus Threshold
				float NucleusThreshold = Float.parseFloat(args[7]);
				pset.setThreshold_Nucleus(NucleusThreshold);
				// ARG_8 ---> Cyto Threshold
				float CytoThreshold = Float.parseFloat(args[8]);
				pset.setThreshold_Cell(CytoThreshold);
				// ARG_9 ---> Bkgd Threshold
				float BkgdThreshold = Float.parseFloat(args[9]);
				pset.setThreshold_Background(BkgdThreshold);
				// ARG_10 ---> CoordsToSave-> "Centroid", "BoundingBox"
				// ,"Outlines", "Everything"
				String CoordsToSave = args[10];
				pset.setCoordsToSaveToHDF(CoordsToSave);


				// (1) Getting all the channel images for this field
				File[] allFiles = in.listFiles();
				// Calling this method to make sure wavelengths sorted
				ArrayList<File[]> allFields = tools.ImageTools
						.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
				File[] files = allFields.get(0);
				int numChannels = files.length;

				// Init Feature files
				String[] channelNames = new String[numChannels];
				for (int i = 0; i < numChannels; i++) {
					String name = files[i].getName();
					channelNames[i] = name.substring(name.indexOf("w"), name
							.indexOf(".tif"));
					System.out.println(channelNames[i]);
				}
				initFeatures(channelNames);

				// (2) Converting the images files to a raster
				int[][][] raster = tools.ImageTools
						.getImageRaster_FromFiles_copy(files);
				// (3) Computing the background from each channel
				float[] backgroundValues = new float[numChannels];
				if (BkgdThreshold > 0)
					tools.ImageTools.computeBackgroundValues(raster,
							backgroundValues, pset);
				// (4) Getting Cell Coordinates (segmenting the cells)
				ArrayList<CellCoordinates> cellCoords = new DefaultSegmentor()
						.segmentCells(raster, pset);

				// (5) Initializing all the data values calculated via the Cell
				// coordinates, the Raster, and the loaded Feature objects
				// EX: Now that we have the pixel coordinates that make up each
				// cell we need to look at the
				// image and extract the proper values
				System.out.println("-->> Performing Feature Computations");
				float[][] cellFeatureMatrix = computeFeatureValues(cellCoords,
						raster, backgroundValues);

				System.out.println(cellFeatureMatrix.length + "x"
						+ cellFeatureMatrix[0].length);

				if (cellFeatureMatrix != null && cellFeatureMatrix.length > 0) {

					//
					// Now writing Cell coordinate data to HDF file
					System.out
							.println("------------ Caching cell data Matrix and Coordinates to HDF file: ------------");
					long time = System.currentTimeMillis();

					// -------------- Store cells in HDF5
					// -------------------------------------------------------
					try {
						int[] fieldDimensions = { raster.length,
								raster[0].length, raster[0][0].length };
						String well_ID = "p" + plateIndex + "w" + wellIndex
								+ "_t"
								+ imagerailio.ImageRail_SDCube.getTimeStamp();
						io.createField(well_ID, plateIndex, wellIndex,
								fieldIndex,
 fieldDimensions, null);
						io.writePlateCountAndSizes(1, 96);

						// Writing data matrix to HDF
						io.writeFeatures(plateIndex, wellIndex, fieldIndex,
								cellFeatureMatrix);
						// Writing the feature names to file

						String[] fNames = new String[features.size()];
						for (int i = 0; i < features.size(); i++)
							fNames[i] = new String(features.get(i).toString());
						io.writeFeatureNames(plateIndex, wellIndex, fieldIndex,
								fNames);

						String whatToSave = pset
								.getCoordsToSaveToHDF();
						if (whatToSave.equalsIgnoreCase("BoundingBox")) {
							// Only save the cell BoundingBoxes to file
							ArrayList<CellCoordinates> bbox = segmentedobject.CellCoordinates
									.getBoundingBoxOfCoordinates(cellCoords);
							io.writeCellBoundingBoxes(plateIndex, wellIndex,
									fieldIndex,
									bbox);

							killCellCoordinates(bbox);
							killCellCoordinates(cellCoords);
						} else if (whatToSave.equalsIgnoreCase("Centroid")) {
							// Only save the cell Centroids to file
							ArrayList<CellCoordinates> centroids = segmentedobject.CellCoordinates
									.getCentroidOfCoordinates(cellCoords);
							io.writeCellCentroids(plateIndex, wellIndex,
									fieldIndex,
									centroids);

							killCellCoordinates(centroids);
							killCellCoordinates(cellCoords);
						} else if (whatToSave.equalsIgnoreCase("Outlines")) {
							// Only save the cell outlines to file
							ArrayList<CellCoordinates> outlines = segmentedobject.CellCoordinates
									.getSingleCompartmentCoords(cellCoords,
											"Outline");
							io.writeWholeCells(plateIndex, wellIndex,
									fieldIndex,
									outlines);

							killCellCoordinates(outlines);
							killCellCoordinates(cellCoords);
						} else if (whatToSave.equalsIgnoreCase("Everything")) {
							io.writeWholeCells(plateIndex, wellIndex,
									fieldIndex,
									cellCoords);
							killCellCoordinates(cellCoords);
						}

						if (Math.random() > 0.7)
							System.gc();
					} catch (Exception e) {
						// Handle this exception!!!
						e.printStackTrace();
					}
					System.out.println("Done writing: "
							+ (System.currentTimeMillis() - time));
					time = System.currentTimeMillis();

				} else
					System.out
							.println("-----**No Cells Found in this well with the given parameter **-----");

				raster = null;
				System.gc();
				}
 else {
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

		features = new ArrayList<Feature>();

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
			f.ChannelName = f.getClass().toString();

			if (f.isMultiSpectralFeature() && channelNames != null) {
				for (int w = 0; w < channelNames.length; w++) {
					try {
						Feature fn = f.getClass().newInstance();
						fn.setChannelIndex(w);
						fn.setChannelName(channelNames[w]);
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

}
