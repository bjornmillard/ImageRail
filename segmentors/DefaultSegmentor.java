/**
 * MST_binary_segmentor.java
 *
 * @author Created by Bjorn Millard
 */

package segmentors;

import java.util.ArrayList;
import java.util.Collections;

import main.ParameterSet;
import segmentedObj.CellCompartment;
import segmentedObj.CellCoordinates;
import segmentedObj.Point;
import tools.LinearKernals;
import tools.SpatialFilter;

public class DefaultSegmentor implements CellSegmentor {
	private Temp_Pixel[][] pixels;
	private int height;
	private int width;

	private ArrayList<Temp_Nucleus> allNuclei;
	private Temp_Cell[] cells;

	/**
	 * The Basic ImageRail Segmentation occurs in two parts:
	 * 
	 * (1) First segments the nuclei (2) Second it grows the nuclei outward to
	 * the cytoplasm bounds
	 * 
	 * */
	public ArrayList<CellCoordinates> segmentCells(int[][][] raster,
			ParameterSet pset) {
		// Reinitializing the variables in case they are used in prior
		// segmentation
		pixels = null;
		height = -1;
		width = -1;
		allNuclei = new ArrayList<Temp_Nucleus>();

		// Step 1: Segmenting the nuclei
		segmentImage_nucleusThresholding_watershed(raster, pset);
		// Step 2: Growing the nuclear seeds into Cells
		cells = growSeedsIntoCells(getNuclei(), raster, pset);

		// Step3: Since I converted this algorithm from a prior legacy version,
		// we need to convert the temp_Cells to Cells_coordinates
		int numC = cells.length;
		ArrayList<CellCoordinates> coordCells = new ArrayList<CellCoordinates>();
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

			coordCells.add(new CellCoordinates(allCompartments));
		}

		return coordCells;
	}

	/***
	 * Nuclear Watershed segmentation of a binary/thresholded imaged.
	 * 
	 * @author BLM
	 */
	private void segmentImage_nucleusThresholding_watershed(int[][][] raster,
			ParameterSet pset) {

		Long time = System.currentTimeMillis();

		height = raster.length;
		width = raster[0].length;
		pixels = new Temp_Pixel[height][width];

		// Init all pixels to un-touched
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				pixels[r][c] = new Temp_Pixel(r, c, -1);

		float[][][] iRaster = new float[height][width][1];
		float max = 0;

		//
		// Step 1: Computing and sorting the Euclidean Distances
		//
		System.out.println("Step 1: Computing Euclidean Maps");

		// Computing distance and Smoothing data with 7x7 kernal
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (raster[r][c][pset.getThresholdChannel_nuc_Index()] > pset
						.getThreshold_Nucleus())
					iRaster[r][c][0] = 1e20f;
		iRaster = SpatialFilter.distanceTransform(iRaster);
		iRaster = SpatialFilter.linearFilter(iRaster, LinearKernals
				.getLinearSmoothingKernal(5), 0);
		// tools.ImageTools.displayRaster(iRaster);

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c][0] > max)
					max = iRaster[r][c][0];
		Temp_Pixel.resetIDs(pixels);
		ArrayList<Temp_Pixel> pixList = new ArrayList<Temp_Pixel>(width
				* height);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				pixels[r][c].setValue((int) iRaster[r][c][0]);
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
			Temp_Pixel pix = (Temp_Pixel) pixList.get(h);
			int thisVal = pix.getValue();
			if (thisVal != 0) {
				Temp_Pixel[] neighs = Temp_Pixel.getNeighbors(pix, pixels);
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
					iRaster[r][c][0] = 255f;

					// Dilating 1x some pixels
					Temp_Pixel[] neighs = Temp_Pixel.getNeighbors(pixels[r][c],
							pixels);
					int num = neighs.length;
					for (int i = 0; i < num; i++) {
						Temp_Pixel[] neighs2 = Temp_Pixel.getNeighbors(
								neighs[i], pixels);
						int size = neighs.length;
						for (int n = 0; n < size; n++) {
							if (neighs2[n].getRow() != r
									&& neighs2[n].getColumn() != c)
								if (iRaster[neighs2[n].getRow()][neighs2[n]
										.getColumn()][0] == 255)
									iRaster[neighs[i].getRow()][neighs[i]
											.getColumn()][0] = 255f;
						}

					}
				} else if (iRaster[r][c][0] != 255)
					iRaster[r][c][0] = 0;

		// tools.ImageTools.displayRaster(iRaster);

		Temp_Pixel.resetIDs(pixels);
		// Growing the Ultimate Points outward till the nuclei threshold to
		// create nuclei
		// for each pixel that has not been visited, find all of its neighbors
		// that are turned on and directly connecting
		// and call them the same group
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (iRaster[r][c][0] > 0 && pixels[r][c].getID() == -1) {
					ArrayList<Temp_Pixel> allPixels = new ArrayList<Temp_Pixel>();
					pixels[r][c].setID(allNuclei.size());
					allPixels.add(pixels[r][c]);
					boolean validNuclei = true;
					try {
						assignAllOnNeighbors_nucleusThresholding(pixels[r][c],
								allPixels, iRaster, 0, 0);
					} catch (StackOverflowError e) {
						validNuclei = false;
					}

					// only want nuclei > certain size to prevent noise being
					// classified as a nuclei
					if (validNuclei)
						if (allPixels.size() > 0) {
							// creating a new cell object & storing its centroid
							Temp_Nucleus nuc = new Temp_Nucleus(allPixels,
									pixels[r][c].getID());
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

		System.out.println("TIME: " + (System.currentTimeMillis() - time));
	}

	private void assignAllOnNeighbors_nucleusThresholding(Temp_Pixel pix,
			ArrayList allPixelsInGroup, float[][][] raster,
			int index_thresholdingChannel, int threshold) {
		Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix, pixels);
		int len = neighbors.length;
		for (int i = 0; i < len; i++) {
			Temp_Pixel p = neighbors[i];
			if (p.getID() == -1
					&& raster[p.getRow()][p.getColumn()][index_thresholdingChannel] > threshold) {
				allPixelsInGroup.add(p);
				p.setID(pix.getID());

				assignAllOnNeighbors_nucleusThresholding(p, allPixelsInGroup,
						raster, index_thresholdingChannel, threshold);
			}
		}
	}

	/**
	 * Returns the segmented cells
	 * 
	 * @author BLM
	 */
	private Temp_Nucleus[] getNuclei() {
		int num = allNuclei.size();
		Temp_Nucleus[] temp = new Temp_Nucleus[num];
		for (int i = 0; i < num; i++)
			temp[i] = (Temp_Nucleus) allNuclei.get(i);
		return temp;
	}

	public float[][] getMeanChannelValuesOverMask_Compartmented(
			int[][][] raster_, ParameterSet pset) {
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
				if (raster_[r][c][pset.getThresholdChannel_nuc_Index()] > pset
						.getThreshold_Cell()) {
					wholeCounter++;
					for (int i = 0; i < numChannels; i++)
						wholeMeanVals[i] += raster_[r][c][i];
				}
				// is above cell boundary threshold but not part of nucleus
				if (raster_[r][c][pset.getThresholdChannel_nuc_Index()] > pset
						.getThreshold_Cell()
						&& raster_[r][c][pset.getThresholdChannel_nuc_Index()] < pset
								.getThreshold_Nucleus()) {
					cytoCounter++;
					for (int i = 0; i < numChannels; i++)
						cytoMeanVals[i] += raster_[r][c][i];
				}
				// is above nucleus threshold
				else if (raster_[r][c][pset.getThresholdChannel_nuc_Index()] > pset
						.getThreshold_Nucleus()) {
					nuclearCounter++;
					for (int i = 0; i < numChannels; i++)
						nuclearMeanVals[i] += raster_[r][c][i];
				} else if (raster_[r][c][pset.getThresholdChannel_nuc_Index()] < pset
						.getThreshold_Background()) {
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
			int[][][] rgbRaster, ParameterSet pset) {
		height = rgbRaster.length;
		width = rgbRaster[0].length;
		int numChannels = rgbRaster[0][0].length;

		float[][] integValues = new float[numChannels][2];
		int pixelCounter = 0;

		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++) {
				if (rgbRaster[r][c][pset.getThresholdChannel_nuc_Index()] > pset
						.getThreshold_Cell()) {
					pixelCounter++;
					for (int i = 0; i < numChannels; i++)
						integValues[i][0] += rgbRaster[r][c][i];
				}
			}

		for (int i = 0; i < numChannels; i++)
			integValues[i][1] = pixelCounter;

		return integValues;
	}

	public Temp_Cell[] growSeedsIntoCells(Temp_Nucleus[] nuclei,
			int[][][] Raster, ParameterSet pset) {

		int height = Raster.length;
		int width = Raster[0].length;

		Temp_Pixel[][] pixels = new Temp_Pixel[height][width];
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				pixels[r][c] = new Temp_Pixel(r, c, -1);

		int numNuc = nuclei.length;
		cells = new Temp_Cell[numNuc];
		for (int i = 0; i < numNuc; i++) {
			int numPix = nuclei[i].getNumPixels();
			for (int p = 0; p < numPix; p++) {
				Point po = nuclei[i].getPixelCoordinate(p);
				pixels[po.y][po.x].setID(i);
			}
			// creating a corresponding cell to go with this nucleus
			cells[i] = new Temp_Cell(nuclei[i], new Temp_Cytoplasm());
		}

		// initially dialating nuclear pixels and calling them the first
		// cytoplasmic pixels of that cell
		for (int n = 0; n < numNuc; n++) {
			Temp_Nucleus nuc = nuclei[n];
			int numPix = nuc.getNumPixels();
			for (int p = 0; p < numPix; p++) {
				Point po = nuc.getPixelCoordinate(p);
				Temp_Pixel pix = pixels[po.y][po.x];

				Temp_Pixel[] neighbors = Temp_Pixel.getFourNeighbors(pix,
						pixels);
				int len = neighbors.length;
				for (int i = 0; i < len; i++) {
					Temp_Pixel neigh = neighbors[i];
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
		int diameter = 20;
		if (pset.getAnnulusSize() != -1)
			diameter = pset.getAnnulusSize();
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				Temp_Cell cell = cells[n];
				ArrayList arr = cell.getCytoplasm().getPixelCoordinates();
				int numPix = arr.size();

				for (int p = 0; p < numPix; p++) {
					Point po = (Point) arr.get(p);
					Temp_Pixel pix = pixels[po.y][po.x];

					Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix,
							pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Temp_Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& Raster[neigh.getRow()][neigh.getColumn()][pset
										.getThresholdChannel_cyto_Index()] > pset
										.getThreshold_Cell()) {
							change = true;
							Point point = new Point(neigh.getColumn(), neigh
									.getRow());
							cell.getCytoplasm().getPixelCoordinates()
									.add(point);
							neigh.setID(pix.getID());
						}
						// else if
						// (MainGUI.getGUI().getStoreNeighborsCheckBox().isSelected()
						// && neigh.getID()!=-1 && neigh.getID()!=pix.getID())
						// {
						// cell.addNeighborID(neigh.getID());
						// }
					}
				}

			}
			counter++;

			if (!change || counter > diameter)
				break;
		}

		// setting neighbors if desired
		// if (MainGUI.getGUI().getStoreNeighborsCheckBox().isSelected())
		// for (int n =0; n < numNuc; n++)
		// cells[n].setNeighborsFromCellIDs(cells);

		for (int i = 0; i < cells.length; i++) {
			// init the cell boundary pixels now
			ArrayList arr = cells[i].getCytoplasm().getPixelCoordinates();
			int numPix = arr.size();
			cells[i].getCytoplasm().setNumPixels(numPix);

			ArrayList boundaryPixels = new ArrayList();
			for (int p = 0; p < numPix; p++) {
				Point po = (Point) arr.get(p);
				Temp_Pixel pix = pixels[po.y][po.x];

				Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix, pixels);
				int len = neighbors.length;
				for (int j = 0; j < len; j++) {
					Temp_Pixel neigh = neighbors[j];
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
	static public ArrayList<Temp_Nucleus> expandNucleiFromSeeds(
			ArrayList<Temp_Nucleus> nucSeeds, Temp_Pixel[][] pixels,
			int[][][] rgbRaster, ParameterSet pset) {
		int numNuc = nucSeeds.size();

		// Getting all the nucPoints and adding them to arrLists for convenience
		ArrayList arrAllNuc = new ArrayList(numNuc);
		for (int i = 0; i < numNuc; i++) {
			ArrayList arr = new ArrayList();
			Temp_Nucleus nuc = nucSeeds.get(i);
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
			nuc.kill(); // clean it up a bith
		}

		int counter = 0;
		while (true) {
			boolean change = false;
			for (int n = 0; n < numNuc; n++) {
				ArrayList nuc = (ArrayList) arrAllNuc.get(n);
				int numPix = nuc.size();
				for (int p = 0; p < numPix; p++) {
					Temp_Pixel pix = (Temp_Pixel) nuc.get(p);
					Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix,
							pixels);
					int len = neighbors.length;
					for (int i = 0; i < len; i++) {
						Temp_Pixel neigh = neighbors[i];
						if (neigh.getID() == -1
								&& rgbRaster[neigh.getRow()][neigh.getColumn()][pset
										.getThresholdChannel_nuc_Index()] > pset
										.getThreshold_Nucleus()) {
							change = true;
							nuc.add(pixels[neigh.getRow()][neigh.getColumn()]);
							neigh.setID(pix.getID());
						}
					}
				}
			}
			counter++;
			if (!change || counter > 300)
				break;
		}

		// Now creating a new set of Nuclei
		int len = arrAllNuc.size();
		counter = 0;
		ArrayList<Temp_Nucleus> arr = new ArrayList<Temp_Nucleus>(len);
		for (int i = 0; i < len; i++) {
			if (((ArrayList) arrAllNuc.get(i)).size() > 10) {
				ArrayList ar = (ArrayList) arrAllNuc.get(i);
				int size = ar.size();
				for (int j = 0; j < size; j++)
					((Temp_Pixel) ar.get(j)).setID(counter);
				arr
						.add(new Temp_Nucleus((ArrayList) arrAllNuc.get(i),
								counter));
				counter++;
			}
		}

		return arr;
	}

	static public float[][] findTotalIntegrationAndTotalPixUsed(
			int[][][] rgbRaster, ParameterSet pset) {
		float[][] vals = null;
		DefaultSegmentor theSegmentor = new DefaultSegmentor();
		vals = theSegmentor.getIntegratedChannelValuesOverMask_wPixelCount(
				rgbRaster, pset);

		return vals;
	}

	static public float[][] findWellAverageOnly_Compartments(
			int[][][] rgbRaster, ParameterSet pset) {
		DefaultSegmentor theSegmentor = new DefaultSegmentor();
		float[][] vals = theSegmentor
				.getMeanChannelValuesOverMask_Compartmented(rgbRaster, pset);
		return vals;
	}
}
