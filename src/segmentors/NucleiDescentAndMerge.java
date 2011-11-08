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

package segmentors;

import imagerailio.Point;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import models.Model_ParameterSet;
import segmentedobject.CellCompartment;
import segmentedobject.CellCoordinates;
import tools.LinearKernels;
import tools.Pixel;
import tools.SpatialFilter;

public class NucleiDescentAndMerge implements CellSegmentor {
	private int height;
	private int width;
	private int numChannels;
	private ArrayList<Shape> ROIs;

	/**
	 * The Basic ImageRail Segmentation occurs in two parts:
	 * 
	 * (1) First segments the nuclei (2) Second it grows the nuclei outward to
	 * the cytoplasm bounds
	 * 
	 * */
	public ArrayList<CellCoordinates> segmentCells(int[][][] raster,
			Model_ParameterSet pset) {
		// Reinitializing the variables in case they are used in prior
		// segmentation
		System.out.println("**Running Nuclei Descent Algorithm**");

		height = raster.length;
		width = raster[0].length;
		numChannels = raster[0][0].length;
		int[] Raster_Linear = convertToLinearRaster(raster);
		raster = null;

		// Init all pixels to un-touched
		Pixel[] pixels = new Pixel[height * width];
		for (int c = 0; c < width; c++)
			for (int r = 0; r < height; r++)
				pixels[r + c * height] = new Pixel(r, c, -1, height, width);

		// Step 1: Segmenting the nuclei
		CellCompartment[] nuclei = segmentNuclei(Raster_Linear, pixels, pset);
		// Step 2: Growing the nuclear seeds into Cells
		ArrayList<CellCoordinates> cellCoords = growSeedsIntoCells(nuclei,
				Raster_Linear, pixels, pset);

		return cellCoords;
	}

	/**
	 * Converts the 3D raster to a 1D linear raster
	 * */
	private int[] convertToLinearRaster(int[][][] raster) {
		height = raster.length;
		width = raster[0].length;
		numChannels = raster[0][0].length;
		int[] linear = new int[height * width * numChannels];
		for (int i = 0; i < numChannels; i++)
			for (int c = 0; c < width; c++)
				for (int r = 0; r < height; r++)
					linear[r + (c * height) + (i * height * width)] = raster[r][c][i];
		return linear;
	}

	/**
	 * Converts the row, column and channelIndex into the linear array index
	 * */
	public int getLinearRasterIndex(int r, int c, int channelIndex) {
		return (r + c * height + (channelIndex * width * height));
	}

	/**
	 * Converts the row, column into the linear array index
	 * */
	public int getLinearRasterIndex(int r, int c) {
		return (r + c * height);
	}

	/***
	 * Nuclear Watershed segmentation of a binary/thresholded imaged.
	 * 
	 * @author BLM
	 */
	private CellCompartment[] segmentNuclei(int[] raster, Pixel[] pixels,
			Model_ParameterSet pset) {

		ArrayList<CellCompartment> allNuclei = new ArrayList<CellCompartment>();


		float[][] iRaster = new float[height][width];
		float max = 0;

		//
		// Step 1: Computing and sorting the Euclidean Distances
		//
		System.out.println("Step 1: Computing Euclidean Maps");

		// Computing distance and Smoothing data with 7x7 kernal
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (raster[getLinearRasterIndex(r, c,
						pset.getParameter_int("Thresh_Nuc_ChannelIndex"))] > pset
						.getParameter_float("Thresh_Nuc_Value"))
					iRaster[r][c] = 1e20f;

		//tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/beforedt.tif");
		iRaster = SpatialFilter.distanceTransform(iRaster);
		//tools.ImageTools.displayRaster(iRaster);		
		//tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/afterdt.tif");
		iRaster = SpatialFilter.linearFilter(iRaster, LinearKernels
.getLinearSmoothingKernal(5));
		//tools.ImageTools.displayRaster(iRaster);
		//tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/afterlsk.tif");

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c] > max)
					max = iRaster[r][c];
		pixels[0].resetIDs(pixels);
		ArrayList<Pixel> pixList = new ArrayList<Pixel>(width
				* height);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				pixels[r + (c * height)].setValue((int) iRaster[r][c]);
				if (pixels[r + (c * height)].getValue() > 0)
					pixList.add(pixels[r + (c * height)]);
			}
		Collections.sort(pixList);
		//
		// Step 2: Finding the Ultimate Eroded Points
		//
		System.out.println("Step 2: Finding Ultimate Eroded Points");
		int NOT_PEAK = 1;
		int PEAK = 2;
		int len = pixList.size();
		for (int h = 0; h < len; h++) {
			Pixel pix = (Pixel) pixList.get(h);
			int thisVal = pix.getValue();
			if (thisVal != 0) {
				Pixel[] neighs = pix.getNeighbors(pixels);
				int num = neighs.length;
				boolean hasUphillNeighbor = false;
				for (int i = 0; i < num; i++)
					if (pixels[neighs[i].getRow()
							+ (neighs[i].getColumn() * height)]
							.getValue() > thisVal) {
						hasUphillNeighbor = true;
						break;
					}
				if (hasUphillNeighbor)
					pix.setID(NOT_PEAK);
				else
					pix.setID(PEAK);
			}
		}
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (pixels[r + c * height].getID() == PEAK) {
					iRaster[r][c] = 255f;

					// Dilating 1x some pixels
					Pixel[] neighs = pixels[r + c * height]
							.getNeighbors(
							pixels);
					int num = neighs.length;
					for (int i = 0; i < num; i++) {
						Pixel[] neighs2 = neighs[i].getNeighbors(pixels);
						int size = neighs.length;
						for (int n = 0; n < size; n++) {
							if (neighs2[n].getRow() != r
									&& neighs2[n].getColumn() != c)
								if (iRaster[neighs2[n].getRow()][neighs2[n]
										.getColumn()] == 255)
									iRaster[neighs[i].getRow()][neighs[i]
											.getColumn()] = 255f;
						}

					}
				} else if (iRaster[r][c] != 255)
					iRaster[r][c] = 0;

		// tools.ImageTools.displayRaster(iRaster);

		pixels[0].resetIDs(pixels);
		// Growing the Ultimate Points outward till the nuclei threshold to
		// create nuclei
		// for each pixel that has not been visited, find all of its neighbors
		// that are turned on and directly connecting
		// and call them the same group

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c] > 0 && pixels[r + c * height].getID() == -1) {
					ArrayList<Pixel> allPixels = new ArrayList<Pixel>();
					pixels[r + c * height].setID(allNuclei.size());
					allPixels.add(pixels[r + c * height]);
					boolean validNuclei = true;
					try {
						assignAllPositiveNeighbors(pixels[r + c * height],
								pixels, allPixels,
								iRaster);
					} catch (StackOverflowError e) {
						validNuclei = false;
					}


					// only want nuclei > certain size to prevent noise being
					// classified as a nuclei
					if (validNuclei)
						if (allPixels.size() > 0) {
							// creating a new cell object & storing its centroid
							int num = allPixels.size();
							Point[] pts = new Point[num];
							for (int i = 0; i < num; i++)
								pts[i] = new Point(
										allPixels.get(i).getColumn(), allPixels
												.get(i).getRow());

							CellCompartment nuc = new CellCompartment(pts,
									"Nucleus", pixels[r + c * height].getID());
							allNuclei.add(nuc);
						}
				}
		allNuclei = expandNucleiFromSeeds(allNuclei, pixels, raster, pset);
		System.out.println("Num Nuclei: " + allNuclei.size());

		// Converting to an array from list
		int num = allNuclei.size();
		CellCompartment[] temp = new CellCompartment[num];
		for (int i = 0; i < num; i++)
			temp[i] = (CellCompartment) allNuclei.get(i);
		;

		// Cleanning up the big mess in memory we just created
		pixels = null;
		iRaster = null;
		pixList = null;

		return temp;
	}

	private void assignAllPositiveNeighbors(Pixel pix, Pixel[] pixels,
			ArrayList<Pixel> allPixelsInGroup, float[][] raster) {
		Pixel[] neighbors = pix.getNeighbors(pixels);
		int len = neighbors.length;
		for (int i = 0; i < len; i++) {
			Pixel p = neighbors[i];
			if (p.getID() == -1
 && raster[p.getRow()][p.getColumn()] > 0) {
				allPixelsInGroup.add(p);
				p.setID(pix.getID());

				assignAllPositiveNeighbors(p, pixels, allPixelsInGroup, raster);
			}
		}
	}



	public float[][] getMeanChannelValuesOverMask_Compartmented(
			int[][][] raster_, Model_ParameterSet pset) {
		height = raster_.length;
		width = raster_[0].length;
		int numChannels = raster_[0][0].length;

		float[] nuclearMeanVals = new float[numChannels];
		float[] cytoMeanVals = new float[numChannels];
		float[] bkgdMeans = new float[numChannels];
		float[] wholeMeanVals = new float[numChannels];
		int bkgdCounter = 0;
		int nuclearCounter = 0;
		int cytoCounter = 0;
		int wholeCounter = 0;

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				// if part of cell
				if (raster_[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Cyt_Value")) {
					wholeCounter++;
					for (int i = 0; i < numChannels; i++)
						wholeMeanVals[i] += raster_[r][c][i];
				}
				// is above cell boundary threshold but not part of nucleus
				if (raster_[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Cyt_Value")
						&& raster_[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] < pset
								.getParameter_float("Thresh_Nuc_Value")) {
					cytoCounter++;
					for (int i = 0; i < numChannels; i++)
						cytoMeanVals[i] += raster_[r][c][i];
				}
				// is above nucleus threshold
				else if (raster_[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Nuc_Value")) {
					nuclearCounter++;
					for (int i = 0; i < numChannels; i++)
						nuclearMeanVals[i] += raster_[r][c][i];
				} else if (raster_[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] < pset
						.getParameter_float("Thresh_Bkgd_Value")) {
					for (int i = 0; i < numChannels; i++)
						bkgdMeans[i] += raster_[r][c][i];
					bkgdCounter++;
				}
			}

		if (bkgdCounter > 0)
			for (int i = 0; i < numChannels; i++)
				bkgdMeans[i] = bkgdMeans[i] / (float) bkgdCounter;

		for (int i = 0; i < numChannels; i++) {
			cytoMeanVals[i] = cytoMeanVals[i] / (float) cytoCounter
					- bkgdMeans[i];
			nuclearMeanVals[i] = nuclearMeanVals[i] / (float) nuclearCounter
					- bkgdMeans[i];
			wholeMeanVals[i] = wholeMeanVals[i] / (float) wholeCounter
					- bkgdMeans[i];
		}

		float[][] vals = new float[3][];
		vals[0] = nuclearMeanVals;
		vals[1] = cytoMeanVals;
		vals[2] = wholeMeanVals;

		return vals;
	}

	public float[][] getIntegratedChannelValuesOverMask_wPixelCount(
			int[][][] rgbRaster, Model_ParameterSet pset) {
		height = rgbRaster.length;
		width = rgbRaster[0].length;
		int numChannels = rgbRaster[0][0].length;

		float[][] integValues = new float[numChannels][2];
		int pixelCounter = 0;

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				if (rgbRaster[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Cyt_Value")) {
					pixelCounter++;
					for (int i = 0; i < numChannels; i++)
						integValues[i][0] += rgbRaster[r][c][i];
				}
			}

		for (int i = 0; i < numChannels; i++)
			integValues[i][1] = pixelCounter;

		return integValues;
	}

	public ArrayList<CellCoordinates> growSeedsIntoCells(
			CellCompartment[] nuclei,
 int[] raster, Pixel[] pixels,
			Model_ParameterSet pset) {

		// The Array of CellCoordinate objects to return
		ArrayList<CellCoordinates> cells_final = new ArrayList<CellCoordinates>();

		// Hashing neighbor cell's relative border pixel count
		// EX: will hash "Cell_ID1-Cell_ID2" --> numberPixelsThatTheseShareInCommon
		Hashtable<String, Integer> hash_neighborsBorderLength = new Hashtable<String, Integer>();

		pixels[0].resetIDs(pixels);

		int numNuc = nuclei.length;
		ArrayList<CellCoordinates> cells = new ArrayList<CellCoordinates>(
				numNuc);

		ArrayList<ArrayList<Point>> tempCytoPointArrays = new ArrayList<ArrayList<Point>>();
		for (int i = 0; i < numNuc; i++) {
			int ID = i;
			Point[] pts = nuclei[i].getCoordinates();
			int numPix = pts.length;
			for (int p = 0; p < numPix; p++) {
				pixels[pts[p].y + (pts[p].x * height)].setID(ID);
			}
			// creating a corresponding cell to go with this nucleus
			ArrayList<CellCompartment> comps = new ArrayList<CellCompartment>();
			comps.add(nuclei[i]);
			cells.add(new CellCoordinates(comps, ID));
		}

		// initially dialating nuclear pixels and calling them the first
		// cytoplasmic pixels of that cell
		for (int n = 0; n < numNuc; n++) {
			// NOTE cytoplasm needs to be the second compartment after nucleus
			ArrayList<Point> cytoNewPts = new ArrayList<Point>();
			ArrayList<Point> nucBoundPts = new ArrayList<Point>();
			Point[] nucPts = nuclei[n].getCoordinates();
			int numPix = nucPts.length;
			for (int p = 0; p < numPix; p++) {
				Pixel pix = pixels[nucPts[p].y + (nucPts[p].x * height)];
				Pixel[] neighbors = pix.getNeighbors(pixels);
				int len = neighbors.length;
				for (int i = 0; i < len; i++) {
					Pixel neigh = neighbors[i];
					if (neigh.getID() == -1) {
						Point pt = new Point(neigh.getColumn(), neigh.getRow());
						cytoNewPts.add(pt);
						nucBoundPts.add(pt);
						neigh.setID(pix.getID());
					}
 else if (neigh.getID() != pix.getID())
						nucBoundPts
								.add(new Point(pix.getColumn(), pix.getRow()));
				}
			}
			CellCompartment nucBoundary = new CellCompartment(nucBoundPts,
					"NucBoundary");
			cells.get(n).addCompartment(nucBoundary);
			// Saving these to represent the boundary of nucleus
			// Saving these for next growth phase
			tempCytoPointArrays.add(cytoNewPts);
		}

		// System.out.println(pset);
		//Getting the Sobel edge detected image
		// int[][] edge = tools.SpatialFilter.sobelEdgeDetector(Raster,
		// pset.getParameter_int("Thresh_Cyt_ChannelIndex"),
		// tools.LinearKernels.getSobel_h(),
		// tools.LinearKernels.getSobel_h(),
		// tools.LinearKernels.getSobel_d1(),
		// tools.LinearKernels.getSobel_d2());

		// tools.ImageTools.displayRaster(ras);
		// System.out.println("min: " + tools.ImageTools.min(ras) + " max: "
		// + tools.ImageTools.max(ras));

		// now going through the cytoplasmic pixels and dilating only those
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				ArrayList<Point> arr = tempCytoPointArrays.get(n);
				int numPix = arr.size();
				for (int p = 0; p < numPix; p++) {
					Point po = (Point) arr.get(p);
					Pixel pix = pixels[po.y + po.x * height];
					Pixel[] neighbors = pix.getNeighbors(pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& raster[getLinearRasterIndex(neigh.getRow(),
										neigh.getColumn(),
										pset.getParameter_int("Thresh_Cyt_ChannelIndex"))] > pset
										.getParameter_float("Thresh_Cyt_Value")) {
							// Adding restraints on whether the cell should keep
							// grown (ex: Membrane detection)
							if (raster[getLinearRasterIndex(neigh.getRow(),
									neigh.getColumn(),
									pset.getParameter_int("ThreshChannel_membrane_Index"))] < pset
									.getParameter_float("Thresh_Membrane"))
 {
								// System.out.println(edge[neigh.getRow()][neigh
								// .getColumn()]);
								change = true;
								arr.add(new Point(neigh.getColumn(), neigh
										.getRow()));
								neigh.setID(pix.getID());
							}
						}

					}
				}

			}

			if (!change) 
				break;
		}


		// init the Cytoplasm boundary pixels now
		for (int i = 0; i < cells.size(); i++) {
			ArrayList<Point> cytoBoundary = new ArrayList<Point>();
			ArrayList<Point> cytoPts = tempCytoPointArrays.get(i);
			int numPix = cytoPts.size();
			for (int p = 0; p < numPix; p++) {
				Point po = (Point) cytoPts.get(p);
				Pixel pix = pixels[po.y + po.x * height];

				Pixel[] neighbors = pix.getNeighbors(pixels);
				int len = neighbors.length;
				for (int j = 0; j < len; j++) {
					Pixel neigh = neighbors[j];
					if (neigh.getID() != pix.getID()) {
						cytoBoundary.add(po);
						break;
					}
				}
			}

			cells.get(i).addCompartment(cytoPts, "Cytoplasm");
			cells.get(i).addCompartment(cytoBoundary, "CytoBoundary");

			// Now looking for neighbors
			// System.out.println("Cellboundary: cellID: " + cells[i].ID);
			// System.out.println(cells[i].boundaryPoints.size());
			int numB = cytoBoundary.size();
			for (int j = 0; j < numB; j++) {
				Point p0 = cytoBoundary.get(j);
				Pixel pix = pixels[p0.y + p0.x * height];
				Pixel[] neighbors = pix.getNeighbors(pixels);
				for (int n = 0; n < neighbors.length; n++) {
					Pixel neigh = neighbors[n];
					if // Got a neighbor - keeping track of how big their border
						// is to each other
					(neigh.getID() != -1 && neigh.getID() != pix.getID()) {
						int thisID = pix.getID();
						int thatID = neigh.getID();
						int minID = -1;
						int maxID = -1;
						if (thisID < thatID) {
							minID = thisID;
							maxID = thatID;
						} else {
							minID = thatID;
							maxID = thisID;
						}
						assert (minID != -1 && maxID != -1);

						// Only want one string variant of this key (min_max)
						String key = minID + "_" + maxID;
						Integer nCounter = hash_neighborsBorderLength.get(key);
						if (nCounter != null) {
							// updating the integerCount by 1
							nCounter = new Integer(nCounter.intValue() + 1);
							hash_neighborsBorderLength.put(key, nCounter);

						} else {
							// First time these cells have bumped into each
							// other
							hash_neighborsBorderLength.put(key, new Integer(1));
						}
						break;
					}
				}
			}


		}
		
		/**
		 * 
		 * 
		 * 
		 * Now merging cells that share borders greater than some percentage of
		 * their total border size
		 * 
		 * 
		 * 
		 */
		Hashtable<Integer, CellCoordinates> hash_IDcell = new Hashtable<Integer, CellCoordinates>();
		Hashtable<Integer, ArrayList<Integer>> hash_idLinks = new Hashtable<Integer, ArrayList<Integer>>();
		Hashtable<Integer, Boolean> hash_id = new Hashtable<Integer, Boolean>();

		// Hashtable<String, String> hash_id = new Hashtable<String, String>();
		float k = pset.getParameter_float("MergeFactor");
		System.out.println("**Merging neighbors w/larger relative borders: "
				+ k * 100 + "%");
		Enumeration<String> enu = hash_neighborsBorderLength.keys();
		// For each pairwise neighbor interaction, check if they share a large
		// enough border to merge
		while (enu.hasMoreElements()) {
			String key = enu.nextElement();
			// System.out.println(key);
			// System.out.println(hash_neighbors.get(key).intValue());
			int ind = key.indexOf("_");

			// Getting ID pair linkage
			int id1 = Integer.parseInt(key.substring(0, ind).trim());
			int id2 = Integer.parseInt(key.substring(ind + 1, key.length())
					.trim());
			Integer ID1 = new Integer(id1);
			Integer ID2 = new Integer(id2);

			// Getting corresponding cells to IDs
			CellCoordinates cell1 = null;
			CellCoordinates cell2 = null;
			for (int j = 0; j < cells.size(); j++) {

				if (cells.get(j).getID() == ID1)
					cell1 = cells.get(j);
				else if (cells.get(j).getID() == ID2)
					cell2 = cells.get(j);
			}
			assert (cell1 != null && cell2 != null);

			// Getting neighbor border size
			int size0 = hash_neighborsBorderLength.get(key).intValue();
			int size1 = cell1.getComCoordinates("CytoBoundary").length;
			int size2 = cell2.getComCoordinates("CytoBoundary").length;
			float fraction1 = (float) size0 / (float) size1;
			float fraction2 = (float) size0 / (float) size2;

			// Merging the cells and adding the final mergedCells to the list
			if (fraction1 > k || fraction2 > k) {

				// Remembering that we already worked with these cells
				hash_id.put(ID1, true);
				hash_id.put(ID2, true);

				// Hashing these IDs to Cells
				if (hash_IDcell.get(ID1) == null)
					hash_IDcell.put(ID1, cell1);
				if (hash_IDcell.get(ID2) == null)
					hash_IDcell.put(ID2, cell2);

				//
				//
				// Updating linkage matrix for ID1 (via hashtables)
				ArrayList<Integer> idList1 = hash_idLinks.get(ID1);
				if (idList1 == null) {
					hash_idLinks.put(ID1, new ArrayList<Integer>());
					idList1 = hash_idLinks.get(ID1);
				}
				int len = idList1.size();
				// Don't want to add ID2 twice, so only add if not exist
				boolean foundIt = false;
				for (int i = 0; i < len; i++)
					if (idList1.get(i).intValue() == ID2.intValue())
						foundIt = true;
				if (!foundIt)
					idList1.add(ID2);

				// Updating linkage matrix for ID2 (via hashtables)
				ArrayList<Integer> idList2 = hash_idLinks.get(ID2);
				if (idList2 == null) {
					hash_idLinks.put(ID2, new ArrayList<Integer>());
					idList2 = hash_idLinks.get(ID2);
				}
				len = idList2.size();
				// Don't want to add ID2 twice, so only add if not exist
				foundIt = false;
				for (int i = 0; i < len; i++)
					if (idList2.get(i).intValue() == ID1.intValue())
						foundIt = true;
				if (!foundIt)
					idList2.add(ID1);

				//
				// System.out.println("Merging cells with ID's: " +
				// cell1.getID()
				// + "," + cell2.getID());
				// System.out.println("fx: " + fraction1 + "," + fraction2);
				// System.out.println("x: " + size0 + "," + size1 + "," +
				// size2);

			}


		}
		// Now adding the rest of the cells that didn't have any neighbors:
		// NOTE: need to create the outline compartment
		for (int i = 0; i < cells.size(); i++) {
			CellCoordinates cell = cells.get(i);
			if (hash_id.get(cell.getID()) == null)
				cells_final.add(cell);

			Point[] NucB = cell.getComCoordinates("NucBoundary");
			Point[] CytB = cell.getComCoordinates("CytoBoundary");
			int numN = NucB.length;
			int numC = CytB.length;
			Point[] outlinePts = new Point[numN + numC];
			for (int j = 0; j < numN; j++)
				outlinePts[j] = NucB[j];
			for (int j = 0; j < numC; j++)
				outlinePts[j + numN] = CytB[j];
			cell.addCompartment(new CellCompartment(outlinePts, "Outline"));
		}

		// Single-linkage clustering of the pairwise interactions (RECURSIVE
		// AlGORITHM)
		ArrayList<ArrayList<CellCoordinates>> allGroups = new ArrayList<ArrayList<CellCoordinates>>();
		Hashtable<Integer, Boolean> hash_IDtouched = new Hashtable<Integer, Boolean>();
		Enumeration<Integer> en = hash_idLinks.keys();
		while (en.hasMoreElements()) {
			Integer ID = en.nextElement();
			// Havent touched this cell yet
			if (hash_IDtouched.get(ID) == null) {
				// Creating a new merge group
				ArrayList<CellCoordinates> group = new ArrayList<CellCoordinates>();
				// Recursive method:
				followIDpath(ID, group, hash_IDcell, hash_idLinks,
						hash_IDtouched);
				// Adding this group to the master list
				if (group.size() > 0)
					allGroups.add(group);
			}
		}
		
		
		// Now merging groups of cells
		int numMergedCells = allGroups.size();
		for (int i = 0; i < numMergedCells; i++) {
			// Doing the actual merging now
			ArrayList<CellCoordinates> arrC = allGroups.get(i);
			CellCoordinates cellMerge = CellCoordinates
.mergeCells(arrC,
					pixels, height);
			cells_final.add(cellMerge);
		}


		//
		//
		// Step3: Since I converted this algorithm from a prior legacy version,
		// we need to convert the temp_Cells to Cells_coordinates
		// int numC = cells_final.size();
		// for (int i = 0; i < numC; i++) {
		// CellCoordinates cell = cells_final.get(i);
		// // Transfering nucleus points
		// Point[] nucPoints = cell.getNucleus().getAllPixelCoordinates();
		//
		// // Transfering cytoplasm points
		// ArrayList<Point> arr = cell.getCytoplasm().getPixelCoordinates();
		// Point[] cytPoints = new Point[arr.size()];
		// for (int j = 0; j < arr.size(); j++)
		// cytPoints[j] = arr.get(j);
		//
		// // Transfering outline points
		// arr = cell.getOutlinePoints();
		// Point[] outlinePts = new Point[arr.size()];
		// for (int j = 0; j < arr.size(); j++)
		// outlinePts[j] = arr.get(j);
		//
		// // Now constructing the final Cell_coords
		// ArrayList<CellCompartment> allCompartments = new
		// ArrayList<CellCompartment>();
		// CellCompartment nucleus = new CellCompartment(nucPoints, "Nucleus");
		// allCompartments.add(nucleus);
		// CellCompartment cytoplasm = new CellCompartment(cytPoints,
		// "Cytoplasm");
		// allCompartments.add(cytoplasm);
		// CellCompartment outline = new CellCompartment(outlinePts, "Outline");
		// allCompartments.add(outline);
		//
		// cellCoords.add(new CellCoordinates(allCompartments));
		// }

		return cells_final;
	}

	/**
	 * 
	 * Single-linkage clustering to merge pairwise linked groups
	 * 
	 * @author Bjorn Millard
	 * 
	 */
	public void followIDpath(Integer ID, ArrayList<CellCoordinates> group,
			Hashtable<Integer, CellCoordinates> hash_IDtoCells,
			Hashtable<Integer, ArrayList<Integer>> hash_IDlinks,
			Hashtable<Integer, Boolean> hash_IDtouched) {
		// If dealt with this ID already, then return
		if (hash_IDtouched.get(ID) != null)
			return;
		// Adding the cell with this ID to the group
		CellCoordinates cell = hash_IDtoCells.get(ID);
		if (cell == null)
			return;
		group.add(cell);
		// Updating that this ID has now been touched
		hash_IDtouched.put(ID, true);
		// Getting all IDs linked to current ID and follow them
		ArrayList<Integer> IDsLinkedToThisID = hash_IDlinks.get(ID);
		if (IDsLinkedToThisID == null)
			return;
		int len = IDsLinkedToThisID.size();
		for (int i = 0; i < len; i++) {
			Integer nextID = IDsLinkedToThisID.get(i);
			if (nextID != null)
				followIDpath(nextID, group, hash_IDtoCells, hash_IDlinks,
						hash_IDtouched);
		}
	}

	/***
	 * takes in an arrayList of nulcei object seeds, expends them to the real
	 * nuclei boundary, then returns a new set of nuclei with the expanded
	 * boudnaries
	 * 
	 * @author BLM
	 */
	public ArrayList<CellCompartment> expandNucleiFromSeeds(
			ArrayList<CellCompartment> nucSeeds, Pixel[] pixels,
 int[] raster,
			Model_ParameterSet pset) {
		int numNuc = nucSeeds.size();

		// Getting all the nucPoints and adding them to arrLists for convenience
		ArrayList<ArrayList<Pixel>> arrAllNuc = new ArrayList<ArrayList<Pixel>>(
				numNuc);
		for (int i = 0; i < numNuc; i++) {
			ArrayList<Pixel> arr = new ArrayList<Pixel>();
			CellCompartment nuc = nucSeeds.get(i);
			Point[] coords = nuc.getCoordinates();
			if (coords == null) {
				System.out
						.println("**Error: nuclei did not have coordinate array");
				return null;
			}
			int num = coords.length;
			for (int j = 0; j < num; j++)
 {
				arr.add(pixels[coords[j].y + (coords[j].x * height)]);
			}
			arrAllNuc.add(arr);
			nuc.kill(); // clean it up a bit
		}

		int counter = 0;
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				ArrayList<Pixel> nuc = (ArrayList<Pixel>) arrAllNuc.get(n);
				int numPix = nuc.size();
				for (int p = 0; p < numPix; p++) {
					Pixel pix = (Pixel) nuc.get(p);
					Pixel[] neighbors = pix.getNeighbors(
							pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& raster[getLinearRasterIndex(neigh.getRow(),
										neigh.getColumn(),
										pset.getParameter_int("Thresh_Nuc_ChannelIndex"))] > pset
										.getParameter_float("Thresh_Nuc_Value")) {
							change = true;
							nuc.add(pixels[neigh.getRow()
									+ (neigh.getColumn() * height)]);
							neigh.setID(pix.getID());
						}
					}
				}
			}
			counter++;
			if (!change)// || counter > 300)
				break;
		}

		// Now creating a new set of Nuclei
		int len = arrAllNuc.size();
		counter = 0;
		ArrayList<CellCompartment> arr = new ArrayList<CellCompartment>(len);
		for (int i = 0; i < len; i++) {
			if (((ArrayList<Pixel>) arrAllNuc.get(i)).size() > 10) {
				ArrayList<Pixel> ar = (ArrayList<Pixel>) arrAllNuc.get(i);
				int size = ar.size();
				ArrayList<Point> nucPts = new ArrayList<Point>(size);
				for (int j = 0; j < size; j++)
 {
					Pixel px = ((Pixel) ar.get(j));
					px.setID(counter);
					nucPts.add(new Point(px.getColumn(), px.getRow()));
				}
				arr.add(new CellCompartment(nucPts, "Nucleus"));
				counter++;
			}
		}

		return arr;
	}

	static public float[][] findTotalIntegrationAndTotalPixUsed(
			int[][][] rgbRaster, Model_ParameterSet pset) {
		float[][] vals = null;
		DefaultSegmentor_v1 theSegmentor = new DefaultSegmentor_v1();
		vals = theSegmentor.getIntegratedChannelValuesOverMask_wPixelCount(
				rgbRaster, pset);

		return vals;
	}

	static public float[][] findWellAverageOnly_Compartments(
			int[][][] rgbRaster, Model_ParameterSet pset) {
		DefaultSegmentor_v1 theSegmentor = new DefaultSegmentor_v1();
		float[][] vals = theSegmentor
				.getMeanChannelValuesOverMask_Compartmented(rgbRaster, pset);
		return vals;
	}

	

	@Override
	public void clearROIs() {
		ROIs = null;
	}

	@Override
	public void setROIs(ArrayList<Shape> ROIs) {
		this.ROIs = ROIs;
	}

}
