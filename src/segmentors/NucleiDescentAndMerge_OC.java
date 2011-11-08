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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
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

/**
 * @author blm13
 * 
 */
public class NucleiDescentAndMerge_OC implements CellSegmentor {
	private int height;
	private int width;
	private int numChannels;
	private ArrayList<Shape> ROIs;
	private boolean[][] ROIs_raster;
	boolean[] nucleiIsOC;

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

		// Creating a ROI no-grow boundary mapping so cells cant grown into them
		// based on ROI designation
		ROIs_raster = null;
		ROIs_raster = new boolean[height][width];

		if (ROIs != null && ROIs.size() > 0) {
			int len = ROIs.size();
			for (int i = 0; i < len; i++) {
				if (ROIs.get(i) instanceof Polygon) {
					
					//Bresenham's line algorithm around each polygon
					Polygon roi = (Polygon) ROIs.get(i);
					int[] xp = roi.xpoints;
					int[] yp = roi.ypoints;
					int nump = xp.length;
					int lastXval = 0;
					int lastYval = 0;

					for (int n = 0; n < nump; n++) {
						int x1 = 0;
						int x2 = 0;
						int y1 = 0;
						int y2 = 0;
						// This time we wrap around from last point to first
						// point
						if (n == nump - 1) {
							x1 = lastXval;
							x2 = xp[0];
							y1 = lastYval;
							y2 = yp[0];
							System.out.println(x1 + "," + y1 + "," + x2 + ","
									+ y2);
						} else // all other points
						{
							x1 = xp[n];
							x2 = xp[n + 1];
							y1 = yp[n];
							y2 = yp[n + 1];
						}

						if (x1 > 0 && x1 < width && x2 > 0 && x2 < width
								&& y1 > 0 && y1 < width && y2 > 0
								&& y2 < width) {

						ROIs_raster[y1][x1] = true;
						ROIs_raster[y2][x2] = true;

							lastXval = x2;
							lastYval = y2;

						 int xDist = x2 - x1;
						 int yDist = y2 - y1;
						 int yStart = y1;
						 int xStart = x1;
						 // compute slope btw points
						 float m = ((float) yDist) / ((float) xDist);
						
						 if (m != Float.NaN || m != 0) {
							if (Math.abs(m) < 1) {
								for (int x = 0; x < Math.abs(xDist); x++) {

									int xI = x;
									if (xDist < 0)
										xI = -x;

									int xP = xStart + xI;
									int y = (int) (m * xI + yStart);

										if (xP >= 0 && xP < width && y >= 0
												&& y < height)
											ROIs_raster[y][xP] = true;
										if (xP >= 0 && xP < width
												&& (y + 1) >= 0
												&& (y + 1) < height)
										ROIs_raster[y + 1][xP] = true;
								}
							} else {
								for (int y = 0; y < Math.abs(yDist); y++) {

									float yI = y;
									if (yDist < 0)
										yI = -y;

									float yP = (float) yStart + yI;
									int x = (int) (1f / m * yI + xStart);

										if (yP >= 0 && yP < height && x >= 0
												&& x < width)
											ROIs_raster[(int) yP][x] = true;
										if (yP >= 0 && yP < height
												&& (x + 1) >= 0
												&& (x + 1) < width)
										ROIs_raster[(int) yP][x + 1] = true;

								}
							}
		
					}

					}
					}
				}
			}
		}

		// if(false)
		// {
		// float[][] test = new float[height][width];
		// for (int r = 0; r < height; r++) {
		// for (int c = 0; c < width; c++) {
		// if (ROIs_raster[r][c])
		// test[r][c] = 255;
		// }
		// }
		// tools.ImageTools.displayRaster(test);
		// }



		// Step 1: Segmenting the nuclei
		// 1) Identify all nuclei using the DNA channel above threshold in given
		// channel
		CellCompartment[] nuclei = segmentNuclei(Raster_Linear, pixels, pset);

		// Identify OC's:
		// 2) Grow cytoplasms only for nuclei that are aVb3 positive (above a
		// user defined threshold and channel)
		// 3) Don't let cytoplasms grow over regions with high-membrane stain as
		// defined by a user threshold and channel designation (Actin works
		// sometimes, but we should keep exploring better options - you realize
		// that this is a complex problem of merging some cells but not merging
		// others right?... its complicated)
		// 4) Merge all the resulting cytoplasms that are in contact with each
		// other with a common border greater than (X%)

		nucleiIsOC = new boolean[nuclei.length];
		ArrayList<CellCoordinates> cellCoords_OCs = growOsteoclastNucleiIntoCells(
				nuclei,
				Raster_Linear, pixels, pset);

		// Precursor identification:
		// 1) Go back to remaining non-OC nuclei and grow those out to their
		// cytoplasm border (User defined cyto/bkg threshold in a defined
		// channel), but dont let precursor cells merge with other cells. (this
		// is just like the old ImageRail algorithm).

		int counter = 0;
		// Getting leftover nuclei that belong to precursor cells
		for (int i = 0; i < nucleiIsOC.length; i++)
			if (!nucleiIsOC[i])
				counter++;
		CellCompartment[] pNucs = new CellCompartment[counter];
		counter = 0;
		for (int i = 0; i < nucleiIsOC.length; i++)
			if (!nucleiIsOC[i]) {
				pNucs[counter] = nuclei[i];
				counter++;
			}

		// Resetting precursor cell IDs in the Pixel matrix
		int rs = pixels.length;
		for (int r = 0; r < rs; r++)
			if (pixels[r].getID() != -1 && !nucleiIsOC[pixels[r].getID()])
				pixels[r].setID(-1);

		// Now growing the precursor cells
		ArrayList<CellCoordinates> cellCoords_precursors = growPrecursorNucleiIntoCells(
				pNucs, Raster_Linear, pixels, pset);

		// Combinging all cells into single ArrayList
		ArrayList<CellCoordinates> cellCoords = new ArrayList<CellCoordinates>();
		for (int i = 0; i < cellCoords_OCs.size(); i++)
			cellCoords.add(cellCoords_OCs.get(i));
		 for (int i = 0; i < cellCoords_precursors.size(); i++)
			cellCoords.add(cellCoords_precursors.get(i));
		// Clean up
		cellCoords_OCs = null;
		cellCoords_precursors = null;

		return cellCoords;
	}

	/**
	 * Tests whether the neighbor points of the given point are contained within
	 * the given polygon
	 * */
	private boolean allNeighborsWithinROI(Point2D.Float p, Polygon poly,
			Pixel[] pixels) {
		Pixel pix = pixels[getLinearRasterIndex((int) p.y, (int) p.x)];
		Pixel[] neighs = pix.getNeighbors(pixels);
		for (int i = 0; i < neighs.length; i++) {
			if (!poly.contains(neighs[i].getColumn(), neighs[i].getRow()))
				return false;
		}
		return true;
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

		// tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/beforedt.tif");
		iRaster = SpatialFilter.distanceTransform(iRaster);
		// tools.ImageTools.displayRaster(iRaster);
		// tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/afterdt.tif");
		iRaster = SpatialFilter.linearFilter(iRaster,
				LinearKernels.getLinearSmoothingKernal(5));
		// tools.ImageTools.displayRaster(iRaster);
		// tools.ImageTools.raster2tiff(iRaster, 0, "/tmp/afterlsk.tif");

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c] > max)
					max = iRaster[r][c];
		pixels[0].resetIDs(pixels);
		ArrayList<Pixel> pixList = new ArrayList<Pixel>(width * height);
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
							+ (neighs[i].getColumn() * height)].getValue() > thisVal) {
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
							.getNeighbors(pixels);
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
								pixels, allPixels, iRaster);
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
 {
			temp[i] = (CellCompartment) allNuclei.get(i);
			temp[i].setID(i);
		}

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
			if (p.getID() == -1 && raster[p.getRow()][p.getColumn()] > 0) {
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

	public ArrayList<CellCoordinates> growOsteoclastNucleiIntoCells(
			CellCompartment[] nuclei, int[] raster, Pixel[] pixels,
			Model_ParameterSet pset) {

		// The Array of CellCoordinate objects to return
		ArrayList<CellCoordinates> cells_final = new ArrayList<CellCoordinates>();

		// Hashing neighbor cell's relative border pixel count
		// EX: will hash "Cell_ID1-Cell_ID2" -->
		// numberPixelsThatTheseShareInCommon
		Hashtable<String, Integer> hash_neighborsBorderLength = new Hashtable<String, Integer>();

		pixels[0].resetIDs(pixels);

		int numNuc = nuclei.length;
		ArrayList<CellCoordinates> cells = new ArrayList<CellCoordinates>(
				numNuc);

		// Only operating on the OC nucs == nuclei with mean OC stain above
		// threshold
		CellCompartment[] nucs_OC = getNuceliWithOCstaining(nuclei, raster,
				pset);
		int numN = nucs_OC.length;
		ArrayList<ArrayList<Point>> tempCytoPointArrays = new ArrayList<ArrayList<Point>>();

		for (int i = 0; i < numN; i++) {

			Point[] pts = nucs_OC[i].getCoordinates();
			int numPix = pts.length;
			for (int p = 0; p < numPix; p++)
				pixels[pts[p].y + (pts[p].x * height)]
						.setID(nucs_OC[i].getID());

			// creating a corresponding cell to go with this nucleus
			ArrayList<CellCompartment> comps = new ArrayList<CellCompartment>();
			comps.add(nucs_OC[i]);
			cells.add(new CellCoordinates(comps, nucs_OC[i].getID()));

			nucleiIsOC[nucs_OC[i].getID()] = true;

		}




		// initially dialating nuclear pixels and calling them the first
		// cytoplasmic pixels of that cell
		for (int n = 0; n < numN; n++) {
			// NOTE cytoplasm needs to be the second compartment after nucleus
			ArrayList<Point> cytoNewPts = new ArrayList<Point>();
			ArrayList<Point> nucBoundPts = new ArrayList<Point>();
			Point[] nucPts = nucs_OC[n].getCoordinates();
			int numPix = nucPts.length;
			for (int p = 0; p < numPix; p++) {
				Pixel pix = pixels[nucPts[p].y + (nucPts[p].x * height)];
				Pixel[] neighbors = pix.getNeighbors(pixels);
				int len = neighbors.length;
				for (int i = 0; i < len; i++) {
					Pixel neigh = neighbors[i];
					
					// Dont grow into ROI boundary regions
					if (ROIs_raster != null) {
						if (!ROIs_raster[neigh.getRow()][neigh
									.getColumn()]) {
								if (neigh.getID() == -1) {
									Point pt = new Point(neigh.getColumn(), neigh.getRow());
									cytoNewPts.add(pt);
									nucBoundPts.add(pt);
									neigh.setID(pix.getID());
								} else if (neigh.getID() != pix.getID())
									nucBoundPts
											.add(new Point(pix.getColumn(), pix.getRow()));
						}
					}
 else // No ROI regions exist
						{
							if (neigh.getID() == -1) {
								Point pt = new Point(neigh.getColumn(), neigh.getRow());
								cytoNewPts.add(pt);
								nucBoundPts.add(pt);
								neigh.setID(pix.getID());
							} else if (neigh.getID() != pix.getID())
								nucBoundPts
										.add(new Point(pix.getColumn(), pix.getRow()));
						}
				}
			}
			CellCompartment nucBoundary = new CellCompartment(nucBoundPts,
					"NucBoundary");
			cells.get(n).addCompartment(nucBoundary);
			// Saving these to represent the boundary of nucleus
			// Saving these for next growth phase
			tempCytoPointArrays.add(cytoNewPts);
		}


		// Now going through the cytoplasmic pixels and dilating only those
		while (true) {
			boolean change = false;
			for (int n = 0; n < numN; n++) {
				ArrayList<Point> arr = tempCytoPointArrays.get(n);
				int numPix = arr.size();
				for (int p = 0; p < numPix; p++) {
					Point po = (Point) arr.get(p);
					Pixel pix = pixels[po.y + po.x * height];
					Pixel[] neighbors = pix.getFourNeighbors(pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& raster[getLinearRasterIndex(neigh.getRow(),
										neigh.getColumn(),
										pset.getParameter_int("ThreshChannel_marker_Index"))] > pset
										.getParameter_float("Thresh_Marker")) {
							// Adding restraints on whether the cell should keep
							// grown (ex: Membrane detection)
							if (raster[getLinearRasterIndex(neigh.getRow(),
									neigh.getColumn(),
									pset.getParameter_int("ThreshChannel_membrane_Index"))] < pset
									.getParameter_float("Thresh_Membrane")) {
								// In case we have ROI boundaries that we dont
								// want to grow into
								if (ROIs_raster != null) {
									if (!ROIs_raster[neigh.getRow()][neigh
												.getColumn()]) {
										change = true;
										arr.add(new Point(neigh.getColumn(),
												neigh.getRow()));
										neigh.setID(pix.getID());
									}
								} else {
									change = true;
									arr.add(new Point(neigh.getColumn(), neigh
											.getRow()));
									neigh.setID(pix.getID());
								}

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
					(neigh.getID() != -1 && neigh.getID() != pix.getID() && nucleiIsOC[neigh.getID()] && nucleiIsOC[pix.getID()]) {
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
			int size1 = 0;
			int size2 = 0;
			if (cell1 != null && cell2 != null
					&& cell1.getComCoordinates("CytoBoundary") != null
					&& cell2.getComCoordinates("CytoBoundary") != null) {
				size1 = cell1.getComCoordinates("CytoBoundary").length;
				size2 = cell2.getComCoordinates("CytoBoundary").length;
			}
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

			}

		}
		// Now adding the rest of the cells that didn't have any neighbors:
		// NOTE: need to create the outline compartment
		for (int i = 0; i < cells.size(); i++) {
			CellCoordinates cell = cells.get(i);
			if (hash_id.get(cell.getID()) == null && nucleiIsOC[cell.getID()])
				cells_final.add(cell);

			Point[] NucB = cell.getComCoordinates("NucBoundary");
			Point[] CytB = cell.getComCoordinates("CytoBoundary");
			numN = NucB.length;
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
			CellCoordinates cellMerge = CellCoordinates.mergeCells(arrC,
					pixels, height);
			cells_final.add(cellMerge);
		}


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
			ArrayList<CellCompartment> nucSeeds, Pixel[] pixels, int[] raster,
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
			for (int j = 0; j < num; j++) {
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
					Pixel[] neighbors = pix.getNeighbors(pixels);
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
				for (int j = 0; j < size; j++) {
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
	
	
	public ArrayList<CellCoordinates> growPrecursorNucleiIntoCells(
			CellCompartment[] nuclei, int[] raster, Pixel[] pixels,
			Model_ParameterSet pset) {

		// The Array of CellCoordinate objects to return
		ArrayList<CellCoordinates> cells_final = new ArrayList<CellCoordinates>();

		int numNuc = nuclei.length;
		ArrayList<CellCoordinates> cells = new ArrayList<CellCoordinates>(
				numNuc);

		ArrayList<ArrayList<Point>> tempCytoPointArrays = new ArrayList<ArrayList<Point>>();
		for (int i = 0; i < numNuc; i++) {
			Point[] pts = nuclei[i].getCoordinates();
			int numPix = pts.length;
			for (int p = 0; p < numPix; p++) {
				pixels[pts[p].y + (pts[p].x * height)].setID(nuclei[i].getID());
			}
			// creating a corresponding cell to go with this nucleus
			ArrayList<CellCompartment> comps = new ArrayList<CellCompartment>();
			comps.add(nuclei[i]);
			cells.add(new CellCoordinates(comps, nuclei[i].getID()));
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
					} else if (neigh.getID() != pix.getID())
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


		// Now going through the cytoplasmic pixels and dilating only those
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
							change = true;
							arr.add(new Point(neigh.getColumn(), neigh.getRow()));
							neigh.setID(pix.getID());

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
		}

		// Now adding the rest of the cells that didn't have any neighbors:
		// NOTE: need to create the outline compartment
		for (int i = 0; i < cells.size(); i++) {
			CellCoordinates cell = cells.get(i);
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

		return cells_final;
	}

	/**
	 * Returns a the list of nuclei that have an mean OC staining above the OC
	 * threshold
	 * */
	private CellCompartment[] getNuceliWithOCstaining(
			CellCompartment[] allNucs, int[] raster_linear,
			Model_ParameterSet pset) {
		ArrayList<CellCompartment> nucs = new ArrayList<CellCompartment>();
		int len = allNucs.length;
		for (int i = 0; i < len; i++) {
			Point[] pts = allNucs[i].getCoordinates();
			int num = pts.length;
			int sum = 0;
			for (int j = 0; j < num; j++)
				sum += raster_linear[getLinearRasterIndex(pts[j].y, pts[j].x,
						pset.getParameter_int("ThreshChannel_marker_Index"))];
			if (sum / num > pset.getParameter_float("Thresh_Marker"))
				nucs.add(allNucs[i]);
		}
		int num = nucs.size();
		CellCompartment[] arr = new CellCompartment[num];
		for (int i = 0; i < num; i++)
			arr[i] = nucs.get(i);
		return arr;
	}

}
