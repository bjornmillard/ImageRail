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

import models.Model_ParameterSet;
import segmentedobject.CellCompartment;
import segmentedobject.CellCoordinates;
import tools.LinearKernels;
import tools.Pixel;
import tools.SpatialFilter;

public class DefaultSegmentor_v1 implements CellSegmentor {
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
		ArrayList<CellCoordinates> cells = growSeedsIntoCells(nuclei,
				Raster_Linear, pixels, pset);

		return cells;
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

		for (int c = 0; c < width; c++)
			for (int r = 0; r < height; r++) {
				pixels[r + c * height].setValue((int) iRaster[r][c]);
				if (pixels[r + c * height].getValue() > 0)
					pixList.add(pixels[r + c * height]);
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
				if (pixels[r + (c * height)].getID() == PEAK) {
					iRaster[r][c] = 255f;

					// Dilating 1x some pixels
					Pixel[] neighs = pixels[r + (c * height)]
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
				if (iRaster[r][c] > 0 && pixels[r + (c * height)].getID() == -1) {
					ArrayList<Pixel> allPixels = new ArrayList<Pixel>();
					pixels[r + (c * height)].setID(allNuclei.size());
					allPixels.add(pixels[r + (c * height)]);
					boolean validNuclei = true;
					try {
						assignAllPositiveNeighbors(pixels[r + (c * height)],
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
									"Nucleus", pixels[r + (c * height)].getID());
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




	public ArrayList<CellCoordinates> growSeedsIntoCells(
			CellCompartment[] nuclei,
 int[] raster, Pixel[] pixels,
			Model_ParameterSet pset) {

		ArrayList<ArrayList<Point>> boundaryPts = new ArrayList<ArrayList<Point>>();

		// Resetting pixel IDs
		pixels[0].resetIDs(pixels);

		int numNuc = nuclei.length;
		ArrayList<CellCoordinates> cells = new ArrayList<CellCoordinates>(
				numNuc);
		ArrayList<ArrayList<Point>> tempCytoPointArrays = new ArrayList<ArrayList<Point>>();
		for (int i = 0; i < numNuc; i++) {
			Point[] pts = nuclei[i].getCoordinates();
			int numPix = pts.length;
			for (int p = 0; p < numPix; p++)
				pixels[pts[p].y + (pts[p].x * height)].setID(i);

			// creating a corresponding cell to go with this nucleus
			ArrayList<CellCompartment> comps = new ArrayList<CellCompartment>();
			comps.add(nuclei[i]);
			cells.add(new CellCoordinates(comps));
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
			boundaryPts.add(nucBoundPts);
			// Saving these to represent the boundary of nucleus
			// Saving these for next growth phase
			tempCytoPointArrays.add(cytoNewPts);
		}

		// now going through the cytoplasmic pixels and dilating only those
		int diameter = 10000;
		// String par = pset.getParameter("AnnulusSize");
		// if (par != null
		// && Integer.parseInt(pset.getParameter("AnnulusSize")) != -1)
		// diameter = Integer.parseInt(pset.getParameter("AnnulusSize"));

		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				ArrayList<Point> arr = tempCytoPointArrays.get(n);
				int numPix = arr.size();
				for (int p = 0; p < numPix; p++) {
					Point po = (Point) arr.get(p);
					Pixel pix = pixels[po.y + (po.x * height)];
					Pixel[] neighbors = pix.getNeighbors(pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& raster[getLinearRasterIndex(neigh.getRow(),
										neigh.getColumn(),
										pset.getParameter_int("Thresh_Cyt_ChannelIndex"))] > pset
										.getParameter_float("Thresh_Cyt_Value")) {
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


		int num = cells.size();
		for (int i = 0; i < num; i++) {
			// Set the cytoplams and initializing the cell boundary pixels now
			ArrayList<Point> bPts = boundaryPts.get(i);
			ArrayList<Point> cytoPts = tempCytoPointArrays.get(i);
			int numPix = cytoPts.size();
			for (int p = 0; p < numPix; p++) {
				Point po = (Point) cytoPts.get(p);
				Pixel pix = pixels[po.y + (po.x * height)];

				Pixel[] neighbors = pix.getNeighbors(pixels);
				int len = neighbors.length;
				for (int j = 0; j < len; j++) {
					Pixel neigh = neighbors[j];
					if (neigh.getID() != pix.getID()) {
						bPts.add(po);
						break;
					}
				}
			}
			cells.get(i).addCompartment(cytoPts, "Cytoplasm");
			cells.get(i).addCompartment(bPts, "Outline");
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
	public ArrayList<CellCompartment> expandNucleiFromSeeds(
			ArrayList<CellCompartment> nucSeeds, Pixel[] pixels,
			int[] raster, Model_ParameterSet pset) {
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
				arr.add(pixels[coords[j].y + (coords[j].x * height)]);
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
				if (raster_[r][c][pset
						.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Cyt_Value")) {
					wholeCounter++;
					for (int i = 0; i < numChannels; i++)
						wholeMeanVals[i] += raster_[r][c][i];
				}
				// is above cell boundary threshold but not part of nucleus
				if (raster_[r][c][pset
						.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Cyt_Value")
						&& raster_[r][c][pset
								.getParameter_int("Thresh_Nuc_ChannelIndex")] < pset
								.getParameter_float("Thresh_Nuc_Value")) {
					cytoCounter++;
					for (int i = 0; i < numChannels; i++)
						cytoMeanVals[i] += raster_[r][c][i];
				}
				// is above nucleus threshold
				else if (raster_[r][c][pset
						.getParameter_int("Thresh_Nuc_ChannelIndex")] > pset
						.getParameter_float("Thresh_Nuc_Value")) {
					nuclearCounter++;
					for (int i = 0; i < numChannels; i++)
						nuclearMeanVals[i] += raster_[r][c][i];
				} else if (raster_[r][c][pset
						.getParameter_int("Thresh_Nuc_ChannelIndex")] < pset
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
				if (rgbRaster[r][c][pset
						.getParameter_int("Thresh_Cyt_ChannelIndex")] > pset
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
	

	@Override
	public void clearROIs() {
		ROIs = null;
	}

	@Override
	public void setROIs(ArrayList<Shape> ROIs) {
		this.ROIs = ROIs;
	}

}
