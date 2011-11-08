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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import models.Model_ParameterSet;
import segmentedobject.CellCompartment;
import segmentedobject.CellCoordinates;
import tools.LinearKernels;
import tools.SpatialFilter;

public class Segmentor_Osteo_v1 implements CellSegmentor {
	private Pixel[][] pixels;
	private int height;
	private int width;
	private ArrayList<Shape> ROIs;
	private ArrayList<Nucleus> allNuclei;
	private Cell[] cells;

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
		pixels = null;
		height = raster.length;
		width = raster[0].length;
		allNuclei = new ArrayList<Nucleus>();
		Point2D.Float p = new Point2D.Float(0, 0);
		// int[][][] iRaster = new int[raster.length][raster[0].length][1];

		// Step 1: Segmenting the nuclei
		segmentImage_nucleusThresholding_watershed(raster, pset);

		// Step 2: Determine Osteoclast cytoplasm bounds via the user defined
		// ROIs
		byte[][] osteoFlags = new byte[height][width];
		ArrayList<CellCompartment> allOsteoCytos = new ArrayList<CellCompartment>();
		if (ROIs != null) {
			int numROIs = ROIs.size();
			for (int i = 0; i < numROIs; i++) {
				Shape roi = ROIs.get(i);
				if (roi != null) {
				ArrayList<Point> cytoPts = new ArrayList<Point>();
				Rectangle bounds = roi.getBounds();
				for (int r = bounds.y; r < (bounds.height + bounds.y); r++)
					for (int c = bounds.x; c < (bounds.width + bounds.x); c++) {
						// Check Raster bounds
						if (r >= 0 && c >= 0 && r < height && c < width) {
							p.x = c;
							p.y = r;
							// If this point is contained within the ROI but
							// less than nucleus threshold, then add it to the
							// cytoplasm of this osteoclast
							if (roi.contains(p)
									&& raster[r][c][pset
											.getParameter_int("Thresh_Nuc_ChannelIndex")] < pset
											.getParameter_float("Thresh_Nuc_Value")) {
								cytoPts.add(new Point(c, r));
									osteoFlags[r][c] = 1;
							}
						}
					}
					allOsteoCytos
							.add(new CellCompartment(cytoPts, "Cytoplasm"));
				}
			}
		}
		System.out.println("osteoCytos: " + allOsteoCytos.size());
		
		// Step 3: Compile osteoclasts (put nuclei and cytos together)
		ArrayList<CellCoordinates> allCellCoords = new ArrayList<CellCoordinates>();
		int numR = ROIs.size();
		int numN = allNuclei.size();
		// Dont include osteo-nuclei in second round segmentation later for
		// non-osteos
		boolean[] inclusionList = new boolean[numN];
		for (int i = 0; i < numN; i++)
			inclusionList[i] = true;

		if(ROIs!=null && ROIs.size()>0)
			for (int roi = 0; roi < numR; roi++) {
				ArrayList<Point> outlinePts = new ArrayList<Point>();
				ArrayList<CellCompartment> allCompartments = new ArrayList<CellCompartment>();
				for (int n = 0; n < numN; n++) {
					Point2D.Double cen = allNuclei.get(n).getCentroid();
					if (ROIs.get(roi) != null && ROIs.get(roi).contains(cen))
					{
						// This nuclei lies within an osteo cytoplasm
						inclusionList[n] = false;
						Nucleus nucleus = allNuclei.get(n);
						Point[] pts = nucleus.getAllPixelCoordinates();
						CellCompartment nuc = new CellCompartment(pts,
								"Nucleus_" + allCompartments.size());

						allCompartments.add(nuc);
						// adding these outline points to the final outline list
						Point[] boundaryPts = nucleus.getBoundaryPoints();

						int num = boundaryPts.length;
						for (int i = 0; i < num; i++)
 {
							outlinePts.add(boundaryPts[i]);
							// iRaster[boundaryPts[i].y][boundaryPts[i].x][0] =
							// 255;
						}
					}

				}

				// Adding the cytoplasm points
				allCompartments.add(allOsteoCytos.get(roi));

				// Adding the cytoplasm ROI outline points to this osteo's
				// outline compartment for visualization
				Polygon poly = ((Polygon) ROIs.get(roi));
				int num = poly.npoints;
				for (int i = 0; i < num; i++)
					if (poly.xpoints[i] >= 0 && poly.ypoints[i] >= 0
							&& poly.ypoints[i] < height
							&& poly.xpoints[i] < width) {

							outlinePts.add(new Point(poly.xpoints[i],
								poly.ypoints[i]));
						// iRaster[poly.ypoints[i]][poly.xpoints[i]][0] = 255;

					}
					
				allCompartments.add(new CellCompartment(outlinePts, "Outline"));

				// Adding this osteoclast
				if (allCompartments != null && allCompartments.size() > 0) {

					CellCoordinates osteo = new CellCoordinates(allCompartments);
					allCellCoords.add(osteo);
				}
			}
		

		// tools.ImageTools.displayRaster(iRaster);


		 // Step 4: Growing the non-osteoclast nuclei seeds into Cells
		cells = growSeedsIntoCells(getNuclei(inclusionList), raster, pset,
				osteoFlags);
		osteoFlags = null;

		// Step3: Since I converted this algorithm from a prior legacy version,
		// we need to convert the temp_Cells to Cells_coordinates
		int numC = cells.length;
		for (int i = 0; i < numC; i++) {
			// Transfering nucleus points
			Point[] nucPoints = cells[i].getNucleus().getAllPixelCoordinates();

			// Transfering cytoplasm points
			ArrayList<Point> arr = cells[i].getCytoplasm()
					.getPixelCoordinates();
			Point[] cytPoints = new Point[arr.size()];
			for (int j = 0; j < arr.size(); j++)
				cytPoints[j] = arr.get(j);

			// Transfering outline points
			arr = cells[i].getBoundaryPoints();
			Point[] outlinePts = new Point[arr.size()];
			for (int j = 0; j < arr.size(); j++)
				outlinePts[j] = arr.get(j);
		
		 // Now constructing the final Cell_coords
			ArrayList<CellCompartment> allCompartments = new ArrayList<CellCompartment>();
			CellCompartment nucleus = new CellCompartment(nucPoints, "Nucleus");
			allCompartments.add(nucleus);
			CellCompartment cytoplasm = new CellCompartment(cytPoints,
					"Cytoplasm");
			allCompartments.add(cytoplasm);
			CellCompartment outline = new CellCompartment(outlinePts, "Outline");
			allCompartments.add(outline);
		
		 allCellCoords.add(new CellCoordinates(allCompartments));
		}

		
		return allCellCoords;

	}

	/**
	 * Since the ROI is an incomplete boundary set of points we need to
	 * interpolate between points to get complete set
	 * 
	 * @athor BLM
	 */
	private ArrayList<Point> interpolateROIpoints(Polygon poly) {
		ArrayList<Point> allPts = new ArrayList<Point>();
		int numP = poly.npoints;
		for (int i = 0; i < numP - 1; i++) {

			Point p1 = new Point(poly.xpoints[i], poly.ypoints[i]);
			Point p2 = new Point(poly.xpoints[i + 1], poly.ypoints[i + 1]);

			int xDist = p2.x - p1.x;
			int yDist = p2.y - p1.y;
			int yStart = p1.y;
			int xStart = p1.x;
			// compute slope btw points
			float m = ((float) yDist) / ((float) xDist);

			// Need to invert line tracking if slope too
			// vertical
			// (ex: walk along y axis not x axis)
			if (m != Float.NaN || m != 0) {
				if (Math.abs(m) < 1) {
					for (int x = 0; x < Math.abs(xDist); x++) {
						int xI = x;
						if (xDist < 0)
							xI = -x;
						int xP = xStart + xI;
						int y = (int) (m * xI + yStart);
						allPts.add(new Point(xP, y));
					}
				}

			} else {
				for (int y = 0; y < Math.abs(yDist); y++) {

					float yI = y;
					if (yDist < 0)
						yI = -y;
					float yP = (float) yStart + yI;
					int x = (int) (1f / m * yI + xStart);
					allPts.add(new Point(x, (int) yP));
				}
			}
		}
		return allPts;
	}

	private void mergeMultiNucleateCells(int[][][] raster,
			Model_ParameterSet pset, ArrayList<CellCoordinates> cells) {

		int len = cells.size();
		ArrayList<int[]> mergers = new ArrayList<int[]>();
		for (int c = 0; c < len; c++) {
			imagerailio.Point p1 = cells.get(c).getCentroid();
			for (int r = 0; r < len; r++) {
				if (c != r) {
					imagerailio.Point p2 = cells.get(r).getCentroid();

					int xDist = p2.x - p1.x;
					int yDist = p2.y - p1.y;
					int yStart = p1.y;
					int xStart = p1.x;
					// compute slope btw points
					float m = ((float) yDist) / ((float) xDist);

					boolean staysAboveBkgd = true;
					int bkgd = (int) pset.getParameter_float("Thresh_Bkgd_Value");
					// Need to invert line tracking if slope too vertical
					// (ex: walk along y axis not x axis)
					if (m != Float.NaN || m != 0) {
						// st = "";
						if (Math.abs(m) < 1) {
							for (int x = 0; x < Math.abs(xDist); x++) {

								int xI = x;
								if (xDist < 0)
									xI = -x;

								int xP = xStart + xI;
								int y = (int) (m * xI + yStart);

								int val = raster[y][xP][pset
										.getParameter_int("Thresh_Cyt_ChannelIndex")];

								// printing out this trace
								// st += pix[0] + ",";
								if (val < bkgd) {
									staysAboveBkgd = false;
									break;
								}
							}

						} else {
							for (int y = 0; y < Math.abs(yDist); y++) {

								float yI = y;
								if (yDist < 0)
									yI = -y;

								float yP = (float) yStart + yI;
								int x = (int) (1f / m * yI + xStart);

								int val = raster[(int) yP][x][pset
										.getParameter_int("Thresh_Cyt_ChannelIndex")];
								// printing out this trace
								// st += pix[0] + ",";

								if (val < bkgd) {
									staysAboveBkgd = false;
									break;
								}
							}
						}
						// pw.println(st);
						if (staysAboveBkgd)
 {
							int[] pair = new int[] { r, c };
							mergers.add(pair);

						}
					}

				}
			}
		}

		// Hash of Integer cell index pointing to what group it belongs too
		// (represented as a ArrayList<Integer>)
		// Hashtable<Integer, ArrayList<Integer>> hash = new Hashtable<Integer,
		// ArrayList<Integer>>();
		// ArrayList<Integer> g = null;
		// len = mergers.size();
		// for (int i = 0; i < len; i++) {
		// int[] arr = (int[]) mergers.get(i);
		// System.out.println(arr[0] + "," + arr[1]);
		// // See if this pair is in a group
		// Integer int0 = new Integer(arr[0]);
		// Integer int1 = new Integer(arr[1]);
		// ArrayList<Integer> group_0 = hash.get(int0);
		// ArrayList<Integer> group_1 = hash.get(int1);
		//
		// g = new ArrayList<Integer>();
		// if (group_0 != null) {
		// int gSize = group_0.size();
		// for (int j = 0; j < gSize; j++)
		// g.add(group_0.get(j));
		// group_0 = null;
		// } else
		// g.add(int0);
		// if (group_1 != null) {
		// int gSize = group_1.size();
		// for (int j = 0; j < gSize; j++)
		// g.add(group_1.get(j));
		// group_1 = null;
		// }
		// else
		// g.add(int1);
		//
		//
		// hash.remove(int0);
		// hash.put(int0, g);
		// hash.remove(int1);
		// hash.put(int1, g);
		// }

	// Enumeration e = hash.elements();
	// while (e.hasMoreElements())
	// {
	// ArrayList<Integer> arr = (ArrayList<Integer>) e.nextElement();
	// int num = arr.size();
	// String st = "";
	// for (int j = 0; j < num; j++) {
	// st += arr.get(j).toString() + ",";
	// }
	// System.out.println(st);
	// }
	}

	/***
	 * Nuclear Watershed segmentation of a binary/thresholded imaged.
	 * 
	 * @author BLM
	 */
	private void segmentImage_nucleusThresholding_watershed(int[][][] raster,
			Model_ParameterSet pset) {

		Long time = System.currentTimeMillis();

		height = raster.length;
		width = raster[0].length;
		pixels = new Pixel[height][width];

		// Init all pixels to un-touched
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				pixels[r][c] = new Pixel(r, c, -1);

		float[][] iRaster = new float[height][width];
		float max = 0;

		//
		// Step 1: Computing and sorting the Euclidean Distances
		//
		System.out.println("Step 1: Computing Euclidean Maps");

		// Computing distance and Smoothing data with 7x7 kernal
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (raster[r][c][pset.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Nuc_Value"))
					iRaster[r][c] = 1e20f;
		iRaster = SpatialFilter.distanceTransform(iRaster);
		iRaster = SpatialFilter.linearFilter(iRaster, LinearKernels
.getLinearSmoothingKernal(5));
		// tools.ImageTools.displayRaster(iRaster);

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c] > max)
					max = iRaster[r][c];
		Pixel.resetIDs(pixels);
		ArrayList<Pixel> pixList = new ArrayList<Pixel>(width * height);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				pixels[r][c].setValue((int) iRaster[r][c]);
				if (pixels[r][c].getValue() > 0)
					pixList.add(pixels[r][c]);
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
				Pixel[] neighs = Pixel.getNeighbors(pix, pixels);
				int num = neighs.length;
				boolean hasUphillNeighbor = false;
				for (int i = 0; i < num; i++)
					if (pixels[neighs[i].getRow()][neighs[i].getColumn()]
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
				if (pixels[r][c].getID() == PEAK) {
					iRaster[r][c] = 255f;

					// Dilating 1x some pixels
					Pixel[] neighs = Pixel.getNeighbors(pixels[r][c], pixels);
					int num = neighs.length;
					for (int i = 0; i < num; i++) {
						Pixel[] neighs2 = Pixel.getNeighbors(neighs[i], pixels);
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

		Pixel.resetIDs(pixels);
		// Growing the Ultimate Points outward till the nuclei threshold to
		// create nuclei
		// for each pixel that has not been visited, find all of its neighbors
		// that are turned on and directly connecting
		// and call them the same group
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c] > 0 && pixels[r][c].getID() == -1) {
					ArrayList<Pixel> allPixels = new ArrayList<Pixel>();
					pixels[r][c].setID(allNuclei.size());
					allPixels.add(pixels[r][c]);
					boolean validNuclei = true;
					try {
						assignAllPositiveNeighbors(pixels[r][c], allPixels,
								iRaster);
					} catch (StackOverflowError e) {
						validNuclei = false;
					}

					// only want nuclei > certain size to prevent noise being
					// classified as a nuclei
					if (validNuclei)
						if (allPixels.size() > 0) {
							// creating a new cell object & storing its centroid
							Nucleus nuc = new Nucleus(allPixels, pixels[r][c]
									.getID());
							allNuclei.add(nuc);
						}
				}
		allNuclei = expandNucleiFromSeeds(allNuclei, pixels, raster, pset);
		System.out.println("Num Nuclei: " + allNuclei.size());
		for (int i = 0; i < allNuclei.size(); i++)
			(allNuclei.get(i)).initBoundaryPoints(pixels);

		// Cleanning up the big mess in memory we just created
		pixels = null;
		iRaster = null;
		pixList = null;

		// System.out.println("Elapsed Time: "
		// + (System.currentTimeMillis() - time));
	}

	private void assignAllPositiveNeighbors(Pixel pix,
			ArrayList<Pixel> allPixelsInGroup, float[][] raster) {
		Pixel[] neighbors = Pixel.getNeighbors(pix, pixels);
		int len = neighbors.length;
		for (int i = 0; i < len; i++) {
			Pixel p = neighbors[i];
			if (p.getID() == -1
 && raster[p.getRow()][p.getColumn()] > 0) {
				allPixelsInGroup.add(p);
				p.setID(pix.getID());
				assignAllPositiveNeighbors(p, allPixelsInGroup, raster);
			}
		}
	}

	/**
	 * Returns the nuclei objects except for those with a "false" in the
	 * inclusionList index, since those are osteoclasts already used
	 * 
	 * @author BLM
	 */
	private Nucleus[] getNuclei(boolean[] inclusionList) {
		int num = allNuclei.size();
		int counter = 0;
		for (int i = 0; i < num; i++)
			if (inclusionList[i])
				counter++;
		Nucleus[] temp = new Nucleus[counter];
		counter = 0;
		for (int i = 0; i < num; i++)
			if (inclusionList[i])
 {
				temp[counter] = (Nucleus) allNuclei.get(i);
				counter++;
			}
		return temp;
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

	public Cell[] growSeedsIntoCells(Nucleus[] nuclei, int[][][] Raster,
			Model_ParameterSet pset, byte[][] osteoFlags) {

		int height = Raster.length;
		int width = Raster[0].length;

		Pixel[][] pixels = new Pixel[height][width];
		int id = -1;
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
			{
				if (osteoFlags[r][c] != 1)
					id = -1;
				else
					id = -2;
				pixels[r][c] = new Pixel(r, c, id);
			}
				
		int numNuc = nuclei.length;
		cells = new Cell[numNuc];
		for (int i = 0; i < numNuc; i++) {
			int numPix = nuclei[i].getNumPixels();
			for (int p = 0; p < numPix; p++) {
				Point po = nuclei[i].getPixelCoordinate(p);
				pixels[po.y][po.x].setID(i);
			}
			// creating a corresponding cell to go with this nucleus
			cells[i] = new Cell(nuclei[i], new Cytoplasm());
		}

		// initially dialating nuclear pixels and calling them the first
		// cytoplasmic pixels of that cell
		for (int n = 0; n < numNuc; n++) {
			Nucleus nuc = nuclei[n];
			int numPix = nuc.getNumPixels();
			for (int p = 0; p < numPix; p++) {
				Point po = nuc.getPixelCoordinate(p);
				Pixel pix = pixels[po.y][po.x];

				Pixel[] neighbors = Pixel.getFourNeighbors(pix, pixels);
				int len = neighbors.length;
				for (int i = 0; i < len; i++) {
					Pixel neigh = neighbors[i];
					if (neigh.getID() == -1) {
						cells[n].getCytoplasm().getPixelCoordinates().add(
								new Point(neigh.getColumn(), neigh.getRow()));
						neigh.setID(pix.getID());
					}
				}
			}

			int num = cells[n].getCytoplasm().getPixelCoordinates().size();
		}

		// now going through the cytoplasmic pixels and dilating only those
		int counter = 0;
		// if (pset.getAnnulusSize() != -1)
		// diameter = pset.getAnnulusSize();
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				Cell cell = cells[n];
				ArrayList<Point> arr = cell.getCytoplasm().getPixelCoordinates();
				int numPix = arr.size();

				for (int p = 0; p < numPix; p++) {
					Point po = (Point) arr.get(p);
					Pixel pix = pixels[po.y][po.x];

					Pixel[] neighbors = Pixel.getNeighbors(pix, pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& Raster[neigh.getRow()][neigh.getColumn()][pset
										.getParameter_int("Thresh_Cyt_ChannelIndex")] > pset
										.getParameter_float("Thresh_Cyt_Value")) {
							change = true;
							Point point = new Point(neigh.getColumn(), neigh
									.getRow());
							cell.getCytoplasm().getPixelCoordinates()
									.add(point);
							neigh.setID(pix.getID());
						}
						
					}
				}

			}
			counter++;

			if (!change) 
				break;
		}


		for (int i = 0; i < cells.length; i++) {
			// init the cell boundary pixels now
			ArrayList<Point> arr = cells[i].getCytoplasm().getPixelCoordinates();
			int numPix = arr.size();
			cells[i].getCytoplasm().setNumPixels(numPix);

			ArrayList boundaryPixels = new ArrayList();
			for (int p = 0; p < numPix; p++) {
				Point po = (Point) arr.get(p);
				Pixel pix = pixels[po.y][po.x];

				Pixel[] neighbors = Pixel.getNeighbors(pix, pixels);
				int len = neighbors.length;
				for (int j = 0; j < len; j++) {
					Pixel neigh = neighbors[j];
					if (neigh.getID() != pix.getID()) {
						boundaryPixels.add(po);
						break;
					}
				}
			}
			cells[i].initBoundary(boundaryPixels);
		}

		return cells;
	}

	/***
	 * takes in an arrayList of nulcei object seeds, expends them to the real
	 * nuclei boundary, then returns a new set of nuclei with the expanded
	 * boudnaries
	 * 
	 * @author BLM
	 */
	static public ArrayList<Nucleus> expandNucleiFromSeeds(
			ArrayList<Nucleus> nucSeeds, Pixel[][] pixels, int[][][] rgbRaster,
			Model_ParameterSet pset) {
		int numNuc = nucSeeds.size();

		// Getting all the nucPoints and adding them to arrLists for convenience
		ArrayList arrAllNuc = new ArrayList(numNuc);
		for (int i = 0; i < numNuc; i++) {
			ArrayList arr = new ArrayList();
			Nucleus nuc = nucSeeds.get(i);
			Point[] coords = nuc.getAllPixelCoordinates();
			if (coords == null) {
				System.out
						.println("**Error: nuclei did not have coordinate array");
				return null;
			}
			int num = coords.length;
			for (int j = 0; j < num; j++)
				arr.add(pixels[coords[j].y][coords[j].x]);
			arrAllNuc.add(arr);
			nuc.kill(); // clean it up a bit
		}

		int counter = 0;
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				ArrayList nuc = (ArrayList) arrAllNuc.get(n);
				int numPix = nuc.size();
				for (int p = 0; p < numPix; p++) {
					Pixel pix = (Pixel) nuc.get(p);
					Pixel[] neighbors = Pixel.getNeighbors(pix, pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& rgbRaster[neigh.getRow()][neigh.getColumn()][pset
										.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
										.getParameter_float("Thresh_Nuc_Value")) {
							change = true;
							nuc.add(pixels[neigh.getRow()][neigh.getColumn()]);
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
		ArrayList<Nucleus> arr = new ArrayList<Nucleus>(len);
		for (int i = 0; i < len; i++) {
			if (((ArrayList) arrAllNuc.get(i)).size() > 10) {
				ArrayList ar = (ArrayList) arrAllNuc.get(i);
				int size = ar.size();
				for (int j = 0; j < size; j++)
					((Pixel) ar.get(j)).setID(counter);
				arr.add(new Nucleus((ArrayList) arrAllNuc.get(i), counter));
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

	public static class Cell {

		private int ID;
		private int FieldNumber;
		private Nucleus nucleus;
		private Cytoplasm cytoplasm;
		private ArrayList<Point> boundaryPoints;

		private int SourceImage_Width;
		private int SourceImage_Height;

		public Cell(Nucleus nucleus_, Cytoplasm cytoplasm_) {
			ID = (int) (Math.random() * 100000000);
			nucleus = nucleus_;
			cytoplasm = cytoplasm_;
		}

		/**
		 * Returns the boundary points in an arrayList demarking the cytoplasmic
		 * boundary of the cell
		 * 
		 * @author BLM
		 */
		public ArrayList<Point> getBoundaryPoints() {
			Point[] pts = nucleus.getBoundaryPoints();
			ArrayList<Point> arr = new ArrayList<Point>();
			for (int i = 0; i < pts.length; i++)
				arr.add(pts[i]);
			for (int i = 0; i < boundaryPoints.size(); i++)
				arr.add(boundaryPoints.get(i));
			return arr;
		}

		/**
		 * Sets the boundary points in an arrayList demarking the cytoplasmic
		 * boundary of the cell
		 * 
		 * @author BLM
		 */
		public void setBoundaryPoints(ArrayList<Point> points) {
			boundaryPoints = points;
		}

		/**
		 * Returns the cytoplasm object of this cell
		 * 
		 * @author BLM
		 */
		public Cytoplasm getCytoplasm() {
			return cytoplasm;
		}

		/**
		 * Sets the cytoplasm object of this cell
		 * 
		 * @author BLM
		 */
		public void setCytoplasm(Cytoplasm cytoplasm_) {
			cytoplasm = cytoplasm_;
		}

		/**
		 * Returns the nucleus object of this cell
		 * 
		 * @author BLM
		 */
		public Nucleus getNucleus() {
			return nucleus;
		}

		/**
		 * Sets the nucleus object of this cell
		 * 
		 * @author BLM
		 */
		public void setNucleus(Nucleus nucleus_) {
			nucleus = nucleus_;
		}

		public void setSourceImageWidth(int width) {
			SourceImage_Width = width;
		}

		public int getSourceImageWidth() {
			return SourceImage_Width;
		}

		public void setSourceImageHeight(int height) {
			SourceImage_Height = height;
		}

		public int getSourceImageHeight() {
			return SourceImage_Height;
		}

		/**
		 * Sets the Model_Field number that this cell came from
		 * 
		 * @author BLM
		 */
		public void setFieldNumber(int field_) {
			FieldNumber = field_;
		}

		/**
		 * Returns the field number that this cell came from
		 * 
		 * @author BLM
		 */
		public int getFieldNumber() {
			return FieldNumber;
		}

		/**
		 * Returns Cell ID
		 * 
		 * @author BLM
		 */
		public int getID() {
			return ID;
		}

		/**
		 * Sets Cell ID
		 * 
		 * @author BLM
		 */
		public void setID(int id) {
			ID = id;
		}

		/**
		 * Clears the pixel coordinates representing this cell
		 * 
		 * @author BLM
		 */
		public void clearPixelData() {
			boundaryPoints = null;
			cytoplasm.clearPixelData();
			nucleus.clearPixelData();
		}


		/**
		 * Sets up the mean value float[] arrays (length==numChannels) to store
		 * values for this cell
		 * 
		 * @author BLM
		 */
		public void initNumChannels(int numChannels) {
			cytoplasm.initNumChannels(numChannels);
		}

		
		
		/**
		 * Returns the whole cell pixel area (numPixels) of this cell
		 * 
		 * @author BLM
		 */
		public int getPixelArea_wholeCell() {
			return (getPixelArea_cytoplasmic() + getPixelArea_nuclear());
		}

		/**
		 * Returns the cytoplasmic pixel area (numPixels) of this cell
		 * 
		 * @author BLM
		 */
		public int getPixelArea_cytoplasmic() {
			return cytoplasm.getNumPixels();
		}

		/**
		 * Returns the nuclear pixel area (numPixels) of this cell
		 * 
		 * @author BLM
		 */
		public int getPixelArea_nuclear() {
			return nucleus.getNumPixels();
		}

		
		
		/**
		 * Given the arrayList of points, this method sets the boundary points
		 * and bounding box of the cell
		 * 
		 * @author BLM
		 */
		public void initBoundary(ArrayList points) {
			boundaryPoints = points;
			if (boundaryPoints == null)
				return;

			float xMin = Float.POSITIVE_INFINITY;
			float xMax = Float.NEGATIVE_INFINITY;
			float yMin = Float.POSITIVE_INFINITY;
			float yMax = Float.NEGATIVE_INFINITY;
			int len = boundaryPoints.size();
			for (int i = 0; i < len; i++) {
				Point p = (Point) points.get(i);
				if (p.x < xMin)
					xMin = p.x;
				if (p.x > xMax)
					xMax = p.x;
				if (p.y < yMin)
					yMin = p.y;
				if (p.y > yMax)
					yMax = p.y;
			}

		}

		/**
		 * Given a arrayList of all coordinate points of this cell, and the
		 * original pixel matrix from the image, this will search all the
		 * coordinates and determine which pixels are the boundary pixels and
		 * then initialize this cell with those boundaries
		 * 
		 * @author BLM
		 */
		public void findAndInitBoundary(ArrayList allPoints, Pixel[][] pixels) {
			// init the cell boundary pixels now
			ArrayList arr = allPoints;
			int numPix = arr.size();
			cytoplasm.setNumPixels(numPix);

			ArrayList boundaryPixels = new ArrayList();
			for (int pi = 0; pi < numPix; pi++) {
				Point po = (Point) arr.get(pi);
				Pixel pix = pixels[po.y][po.x];

				Pixel[] neighbors = Pixel.getNeighbors(pix, pixels);
				int len = neighbors.length;
				for (int j = 0; j < len; j++) {
					Pixel neigh = neighbors[j];
					if (neigh.getID() != pix.getID()) {
						boundaryPixels.add(po);
						break;
					}
				}
			}
			initBoundary(boundaryPixels);
		}

		/**
		 * In order to save memory, this method tries to clear every trace of
		 * this cell
		 * 
		 * @author BLM
		 */
		public void kill() {
			cytoplasm.kill();
			nucleus.kill();
			if (boundaryPoints != null) {
				Iterator iter = boundaryPoints.iterator();
				while (iter.hasNext()) {
					Point element = (Point) iter.next();
					element = null;
				}
				boundaryPoints = null;
			}
		}
	}


	public static class Cytoplasm {
		private int numPixels;
		private float[] channelValues_integrated;
		private ArrayList<Point> PixelCoordinates;

		public Cytoplasm() {
			PixelCoordinates = new ArrayList<Point>();
		}

		public void clearPixelData() {
			PixelCoordinates = null;
		}

		/**
		 * Returns the number of pixels that compose the nucleus (ex: nuclear
		 * area in pixel units)
		 * 
		 * @author BLM
		 */
		public int getNumPixels() {
			return numPixels;
		}

		/**
		 * Sets the number of pixels that compose the nucleus (ex: nuclear area
		 * in pixel units)
		 * 
		 * @author BLM
		 */
		public void setNumPixels(int numPix) {
			numPixels = numPix;
		}

		/**
		 * Returns the ArrayList of Points that represent the coordinates of the
		 * of the pixels that compose this cytoplasm
		 * 
		 * @author BLM
		 */
		public ArrayList<Point> getPixelCoordinates() {
			return PixelCoordinates;
		}

		/**
		 * Returns the integrated float values of the features computed for this
		 * cytoplasm. The array of values is of length==numFeatures
		 * 
		 * @author BLM
		 */
		public float[] getChannelValues_integrated() {
			return channelValues_integrated;
		}

		public void initNumChannels(int numChannels) {
			channelValues_integrated = new float[numChannels];
		}


		public void kill() {
			if (PixelCoordinates != null) {
				Iterator iter = PixelCoordinates.iterator();
				while (iter.hasNext()) {
					Point element = (Point) iter.next();
					element = null;
				}
				PixelCoordinates = null;
			}
			channelValues_integrated = null;
			clearPixelData();
		}
	}


	public static class Nucleus {
		private int ID;
		private int numPixels;
		private Point[] pixelCoordinates;
		private Point[] boundaryPoints;
		private Point2D.Double Centroid;

		public Nucleus(int id) {
			ID = id;
		}

		public Nucleus(ArrayList pixels_, int id) {
			ID = id;
			numPixels = pixels_.size();
			pixelCoordinates = new Point[numPixels];
			for (int i = 0; i < numPixels; i++)
				pixelCoordinates[i] = new Point(((Pixel) pixels_.get(i))
						.getColumn(), ((Pixel) pixels_.get(i)).getRow());
			Centroid = getCentroid();
		}

		/**
		 * Returns the ID of the cell
		 * 
		 * @author BLM
		 */
		public int getID() {
			return ID;
		}

		/**
		 * Sets the number of pixels that compse this nucleus
		 * 
		 * @author BLM
		 */
		public void setNumPixels(int numPix) {
			numPixels = numPix;
		}

		/**
		 * Returns the array of coordinates that denote the outline of the
		 * nucleus
		 * 
		 * @author BLM
		 */
		public Point[] getBoundaryPoints() {
			return boundaryPoints;
		}

		public void clearPixelData() {
			boundaryPoints = null;
			pixelCoordinates = null;
		}

		public Point[] getAllPixelCoordinates() {
			return pixelCoordinates;
		}

		/** returns the requested indexed pixel */
		public Point getPixelCoordinate(int i) {
			return pixelCoordinates[i];
		}

		/**
		 * Returns the number of pixels that compose the nucleus (ex: nuclear
		 * area in pixel units)
		 * 
		 * @author BLM
		 */
		public int getNumPixels() {
			return numPixels;
		}

		/**
		 * Computes the pixel_XY location of the centroid of the cell
		 * 
		 * @author BLM
		 */
		public Point2D.Double getCentroid() {
			if (Centroid == null && pixelCoordinates != null) {
				Centroid = new Point2D.Double();
				int col = 0;
				int row = 0;
				for (int i = 0; i < numPixels; i++) {
					row += pixelCoordinates[i].y;
					col += pixelCoordinates[i].x;
				}
				Centroid.y = row / numPixels;
				Centroid.x = col / numPixels;
			}
			return Centroid;
		}

		public void initBoundaryPoints(Pixel[][] pixels) {
			if (pixelCoordinates == null || pixelCoordinates.length == 0)
				return;
			// For each pixel in the nucleus, See if any of the neighbors do not
			// belong to this group; if so, then its a boundary pixel
			ArrayList arr = new ArrayList();
			int len = pixelCoordinates.length;
			for (int i = 0; i < len; i++) {
				Point po = pixelCoordinates[i];
				Pixel p = pixels[po.y][po.x];
				Pixel[] ne = Pixel.getNeighbors(p, pixels);
				int num = ne.length;
				for (int j = 0; j < num; j++) {
					if (p.getID() != ne[j].getID()) {
						arr.add(po);
						break;
					}
				}
			}
			len = arr.size();
			boundaryPoints = new Point[len];
			for (int i = 0; i < len; i++)
				boundaryPoints[i] = (Point) arr.get(i);
		}

		public void kill() {
			if (pixelCoordinates != null) {
				for (int i = 0; i < pixelCoordinates.length; i++)
					pixelCoordinates[i] = null;
				pixelCoordinates = null;
			}
		}
	}


	public static class Pixel implements Comparable {
		private int row;
		private int col;
		private int value;
		private int ID;

		public Pixel(int r, int c, int id) {
			row = r;
			col = c;
			value = -5;
			ID = id;
		}

		public Pixel(int r, int c, int z_, int id) {
			row = r;
			col = c;
			value = z_;
			ID = id;
		}

		/**
		 * Sets the pixel value as an integer
		 * 
		 * @author BLM
		 */
		public void setValue(int val) {
			value = val;
		}

		/**
		 * Sets the ID of the Pixel
		 * 
		 * @author BLM
		 */
		public void setID(int id) {
			ID = id;
		}

		/**
		 * Returns the pixel value as an integer
		 * 
		 * @author BLM
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Returns the pixel ID number
		 * 
		 * @author BLM
		 */
		public int getID() {
			return ID;
		}

		/**
		 * Returns the pixel row where it is found in the image
		 * 
		 * @author BLM
		 */
		public int getRow() {
			return row;
		}

		/**
		 * Returns the pixel column where it is found in the image
		 * 
		 * @author BLM
		 */
		public int getColumn() {
			return col;
		}

		public double getDistance_L1(Pixel p) {
			double dist = 0;
			dist += Math.abs(p.row - row);
			dist += Math.abs(p.col - col);

			return dist;
		}

		public int compareTo(Object o) {
			if (!(o instanceof Pixel))
				throw new ClassCastException();

			Pixel obj = (Pixel) o;

			if (obj.value < value)
				return 1;

			if (obj.value > value)
				return -1;

			return 0;
		}

		/**
		 * Says if this pixels is above an intensity threshold
		 * 
		 * @author BLM
		 */
		static public boolean pixelOn(int[] pix) {
			int sum = 0;
			int len = pix.length;

			for (int i = 1; i < len; i++)
				sum += pix[i];

			if (sum > 0) {

				return true;
			}
			return false;
		}

		static public boolean pixelOn(int[] pix, float threshold) {
			int sum = 0;
			int len = pix.length;

			for (int i = 1; i < len; i++)
				sum += pix[i];

			if (sum > threshold)
				return true;

			return false;
		}

		static public void resetIDs(Pixel[][] pixels) {
			int rows = pixels.length;
			int cols = pixels[0].length;
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
					pixels[r][c].ID = -1;
		}

		static public boolean pixelOn(int pix, float threshold) {
			int sum = 0;

			if (sum > threshold)
				return true;

			return false;
		}

		static public boolean pixelOn_binary(int[] pix) {
			int sum = 0;
			int len = pix.length;

			for (int i = 0; i < len; i++)
				sum += pix[i];

			if (sum > 230)
				return true;
			return false;
		}

		/**
		 * Checks if the given pixel that has the given Euclidean Distance is
		 * Surrounded by pixels at a higher level
		 * 
		 * @author BLM
		 **/
		static public float getLargestNeighborEuclidDist(Pixel pix,
				Pixel[][] pixels, float[][][] distanceMap) {
			Pixel[] neigh = Pixel.getNeighbors(pix, pixels);
			int len = neigh.length;
			float max = -1;
			for (int i = 0; i < len; i++) {
				Pixel p = neigh[i];
				float val = distanceMap[p.row][p.col][0];
				if (val > max)
					max = val;
			}

			return max;
		}

		/**
		 * Returns an array of pixels of the given pixel, top to bottom, L->R
		 * starting at r-1, c-1
		 * 
		 * @author BLM
		 */
		static public Pixel[] getNeighbors(Pixel centerPixel, Pixel[][] pixels) {
			int height = pixels.length;
			int width = pixels[0].length;
			Pixel[] pixs = null;
			// top row
			if (centerPixel.row == 0) {
				if (centerPixel.col == 0) {
					// top left corner
					pixs = new Pixel[3];
					pixs[0] = pixels[centerPixel.row][centerPixel.col + 1];
					pixs[1] = pixels[centerPixel.row + 1][centerPixel.col + 1];
					pixs[2] = pixels[centerPixel.row + 1][centerPixel.col];
				} else if (centerPixel.col == width - 1) {
					// top right corner
					pixs = new Pixel[3];
					pixs[0] = pixels[centerPixel.row][centerPixel.col - 1];
					pixs[1] = pixels[centerPixel.row + 1][centerPixel.col - 1];
					pixs[2] = pixels[centerPixel.row + 1][centerPixel.col];
				}

				else {
					// general top row
					pixs = new Pixel[5];
					pixs[0] = pixels[centerPixel.row][centerPixel.col - 1];

					pixs[1] = pixels[centerPixel.row + 1][centerPixel.col - 1];
					pixs[2] = pixels[centerPixel.row + 1][centerPixel.col];
					pixs[3] = pixels[centerPixel.row + 1][centerPixel.col + 1];

					pixs[4] = pixels[centerPixel.row][centerPixel.col + 1];
				}
			}
			// bottom row pixels
			else if (centerPixel.row == height - 1) {
				if (centerPixel.col == 0) {
					// bottom left corner
					pixs = new Pixel[3];
					pixs[0] = pixels[centerPixel.row - 1][centerPixel.col];
					pixs[1] = pixels[centerPixel.row - 1][centerPixel.col + 1];
					pixs[2] = pixels[centerPixel.row][centerPixel.col + 1];
				} else if (centerPixel.col == width - 1) {
					// bottom right corner
					pixs = new Pixel[3];
					pixs[0] = pixels[centerPixel.row][centerPixel.col - 1];
					pixs[1] = pixels[centerPixel.row - 1][centerPixel.col - 1];
					pixs[2] = pixels[centerPixel.row - 1][centerPixel.col];
				} else {
					// general bottom row
					pixs = new Pixel[5];
					pixs[0] = pixels[centerPixel.row][centerPixel.col - 1];

					pixs[1] = pixels[centerPixel.row - 1][centerPixel.col - 1];
					pixs[2] = pixels[centerPixel.row - 1][centerPixel.col];
					pixs[3] = pixels[centerPixel.row - 1][centerPixel.col + 1];

					pixs[4] = pixels[centerPixel.row][centerPixel.col + 1];
				}
			}

			// general left column
			else if (centerPixel.col == 0) {
				pixs = new Pixel[5];
				pixs[0] = pixels[centerPixel.row - 1][centerPixel.col];

				pixs[1] = pixels[centerPixel.row - 1][centerPixel.col + 1];
				pixs[2] = pixels[centerPixel.row][centerPixel.col + 1];
				pixs[3] = pixels[centerPixel.row + 1][centerPixel.col + 1];

				pixs[4] = pixels[centerPixel.row + 1][centerPixel.col];
			}
			// far right column pixels
			else if (centerPixel.col == width - 1) {
				pixs = new Pixel[5];
				pixs[0] = pixels[centerPixel.row - 1][centerPixel.col];

				pixs[1] = pixels[centerPixel.row - 1][centerPixel.col - 1];
				pixs[2] = pixels[centerPixel.row][centerPixel.col - 1];
				pixs[3] = pixels[centerPixel.row + 1][centerPixel.col - 1];

				pixs[4] = pixels[centerPixel.row + 1][centerPixel.col];
			}

			else {
				// general body pixels
				pixs = new Pixel[8];
				pixs[0] = pixels[centerPixel.row - 1][centerPixel.col - 1];
				pixs[1] = pixels[centerPixel.row - 1][centerPixel.col];
				pixs[2] = pixels[centerPixel.row - 1][centerPixel.col + 1];

				pixs[3] = pixels[centerPixel.row][centerPixel.col - 1];
				pixs[4] = pixels[centerPixel.row][centerPixel.col + 1];

				pixs[5] = pixels[centerPixel.row + 1][centerPixel.col - 1];
				pixs[6] = pixels[centerPixel.row + 1][centerPixel.col];
				pixs[7] = pixels[centerPixel.row + 1][centerPixel.col + 1];
			}
			return pixs;
		}

		static public Pixel[] getFourNeighbors(Pixel centerPixel,
				Pixel[][] pixels) {
			int height = pixels.length;
			int width = pixels[0].length;
			Pixel[] pixs = null;

			if (centerPixel.row >= height - 1 || centerPixel.col >= width - 1) {
				pixs = new Pixel[0];
				return pixs;
			} else if (centerPixel.row <= 1 || centerPixel.col <= 1) {
				pixs = new Pixel[0];
				return pixs;
			}

			// general body pixels
			pixs = new Pixel[4];

			pixs[0] = pixels[centerPixel.row - 1][centerPixel.col];
			pixs[1] = pixels[centerPixel.row][centerPixel.col - 1];
			pixs[2] = pixels[centerPixel.row][centerPixel.col + 1];
			pixs[3] = pixels[centerPixel.row + 1][centerPixel.col];

			return pixs;
		}

		public static byte[] convertToByteArray(float[][][] im) {
			int rows = im.length;
			int cols = im[0].length;
			int counter = 0;
			byte[] arr = new byte[rows * cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					arr[counter] = (byte) (im[r][c][0] - 128);
					counter++;
				}

			return arr;
		}

		public static byte[] convertToByteArray(int[][][] im) {
			int rows = im.length;
			int cols = im[0].length;
			int counter = 0;
			byte[] arr = new byte[rows * cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					arr[counter] = (byte) (im[r][c][0] - 128);
					counter++;
				}

			return arr;
		}
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
