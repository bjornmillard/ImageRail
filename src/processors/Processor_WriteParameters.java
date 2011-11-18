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

package processors;


import imagerailio.ImageRail_SDCube;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import models.Model_Field;
import models.Model_Plate;
import models.Model_Well;
import sdcubeio.H5IO_Exception;
import segmentors.CellSegmentor;

public class Processor_WriteParameters extends Thread implements Processor {
	private Model_Well[] WellsToProcess;
	private CellSegmentor TheSegmentor;

	public Processor_WriteParameters(Model_Well[] wellsToProcess,
			CellSegmentor segmentor) {
		// These are the wells we will process
		WellsToProcess = wellsToProcess;
		// The segmentor that is going to be used to segment the cells
		TheSegmentor = segmentor;
	}

	// When this thread starts, we run...
	public void run() {
		runProcess();
	}

	public void runProcess() {
		// Make sure there are some wells to process, else return
		if (WellsToProcess == null || WellsToProcess.length == 0)
			return;

		// Updating Model_Well colors to indicate they are in the queue to be
		// processed
		for (int i = 0; i < WellsToProcess.length; i++)
			WellsToProcess[i].getGUI().color_outline = Color.gray;

		//
		// Main Processing call
		//
		System.out.println("Number of Wells to Process: "
				+ WellsToProcess.length);
		processWells(WellsToProcess, TheSegmentor);
		//
		//

		// Updating Model_Well colors to indicate we are finished
		for (int i = 0; i < WellsToProcess.length; i++)
			WellsToProcess[i].getGUI().color_outline = Color.white;

	}

	/**
	 * Returns the number of unique plate IDs that the wells of the given array
	 * came from, also returns the number of wells in each unique plate
	 * 
	 * @returns int[2][numUniquePlateIDs] where [0][] --> plateID and [1][] -->
	 *          number of Wells
	 * @author BLM
	 */
	static public int[][] getAllUniquePlateIDsAndNumWells(Model_Well[] wells) {
		ArrayList<Integer> uniquesIDs = new ArrayList<Integer>();
		ArrayList<Integer> uniquesWells = new ArrayList<Integer>();
		for (int i = 0; i < wells.length; i++) {
			int id = wells[i].getPlate().getID();
			boolean foundIt = false;
			for (int j = 0; j < uniquesIDs.size(); j++) {
				int val = (uniquesIDs.get(j).intValue());
				if (id == val) {
					foundIt = true;
					break;
				}
			}
			if (!foundIt) {
				uniquesIDs.add(new Integer(id));
				uniquesWells.add(new Integer(wells[i].getPlate()
						.getNumColumns() * wells[i].getPlate().getNumRows()));
			}
		}

		int[][] arr = new int[2][uniquesIDs.size()];
		for (int i = 0; i < uniquesIDs.size(); i++) {
			arr[0][i] = uniquesIDs.get(i).intValue();
			arr[1][i] = uniquesWells.get(i).intValue();
		}

		return arr;
	}

	/***
	 * Main processing method
	 * 
	 * @author BLM
	 */
	public void processWells(Model_Well[] wells, CellSegmentor theSegmentor) {
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();
		if (io != null)
			try {


				int numWells = wells.length;
				long StartTime = System.currentTimeMillis();
				models.Model_Main.getModel().setProcessing(true);

				for (int w = 0; w < numWells; w++) {
					if (!models.Model_Main.getModel().shouldStop())
						break;

					Model_Well well = wells[w];

					String thisWell = well.name;
					System.out
							.println("______________________________________");
					System.out
							.println("______________________________________");
					System.out.println(" Well: " + thisWell);

					// Making the current well green
					well.processing = true;
					models.Model_Main.getModel().getPlateRepository_GUI()
							.updatePanel();

					int wellIndex = (well.getPlate().getNumRows() * well.Column)
							+ well.Row;
					int plateIndex = well.getPlate().getID();

					// Now processing all the fields for this well
					int numFields = well.getFields().length;
					for (int f = 0; f < numFields; f++) {
						if (!models.Model_Main.getModel().shouldStop())
							break;

						System.out.println("	Field: " + (f + 1));

						// (1) Getting all the channel images for this field
						Model_Field field = well.getFields()[f];
						File[] images_oneField = field.getImageFiles();

						// (2) Converting the images files to a raster
						// Raster = tools.ImageTools
						// .getImageRaster_FromFiles_copy(images_oneField,
						// models.Model_Main.getModel()
						// .getTheChannelNames());

						// int[] dim =
						// tools.ImageTools.getImageDimensions(images_oneField[0]);


						// (5) Initializing all the data values calculated via
						// the Cell coordinates, the Raster, and the loaded
						// Feature objects
						// EX: Now that we have the pixel coordinates that make
						// up each cell we need to look at the
						// image and extract the proper values
						long time = System.currentTimeMillis();
						System.out
								.println("-->> Performing Feature Computations");
						// Compute
						int numChannels = models.Model_Main.getModel()
								.getNumberOfChannels();

						//
						// Now writing Cell coordinate data to HDF file
						System.out
								.println("------------ Caching cell data Matrix and Coordinates to HDF file: ------------");
						time = System.currentTimeMillis();

						// -------------- Store cells in HDF5
						// -------------------------------------------------------
						try {
							int[] fieldDimensions = { -1, -1, numChannels };
							// int[] fieldDimensions = { Raster.length,
							// Raster[0].length, numChannels };
							io.openHDF5(io.OUTPUT);
							io.createField(well.getID(), plateIndex, wellIndex,
									f, fieldDimensions, models.Model_Main
											.getModel().getExpDesignConnector());
							io.closeHDF5();

						} catch (Exception e) {
							// Handle this exception!!!
							e.printStackTrace();
						}
						System.out.println("Done writing: "
								+ (System.currentTimeMillis() - time));
						time = System.currentTimeMillis();

						// Storing Parameters used to process this field
						String hdfPath = models.Model_Main.getModel()
								.getOutputProjectPath()
								+ "/Data.h5";
						field.getParameterSet().writeParameters(hdfPath,
								well.getPlate().getID(), well.getWellIndex(),
								field.getIndexInWell());

						// Cleaning up

						well.getPlate().getGUI().repaint();

						System.gc();
					}

					if (gui.MainGUI.getGUI().getLoadCellsImmediatelyCheckBox()
							.isSelected())
						well.loadCells(io, true, true);

					well.processing = false;

					// Writing HDF5 well sample metadata
					int totNumWells = well.getPlate().getNumRows()
							* well.getPlate().getNumColumns();
					io.openHDF5(io.OUTPUT);
					io.writeParentPlateInfo(plateIndex, wellIndex, totNumWells);
					io.closeHDF5();
				}

				System.out.println("*** Finished: "
						+ (System.currentTimeMillis() - StartTime));
				models.Model_Main.getModel().setProcessing(false);
				System.gc();

			} // END writing HDF data to project
			catch (Exception e) {
				// Make sure the HDF5 file is closed to prevent corruption
				try {
					io.getH5IO().closeAll();
				} catch (H5IO_Exception e1) {
					System.out
							.println("**ERROR closing HFD5 file during crash");
					e1.printStackTrace();
				}
				// Handle this exception!!!
				e.printStackTrace();
			}
	}

	public Model_Well getWellForGivenImage(String fileName) {
		for (int p = 0; p < models.Model_Main.getModel()
				.getPlateRepository_GUI().getModel().getNumPlates(); p++) {
			Model_Plate plate = models.Model_Main.getModel()
					.getPlateRepository_GUI().getModel().getPlates()[p];
			int rows = plate.getNumRows();
			int cols = plate.getNumColumns();
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					if (fileName.indexOf(plate.getWells()[r][c].name) > 0)
						return plate.getWells()[r][c];
				}
		}

		return null;
	}

	/**
	 * Determines if the given file name is one to be processed
	 * 
	 * @author BLM
	 */
	public boolean isWellToProcess(String fileName) {
		int num = WellsToProcess.length;
		for (int i = 0; i < num; i++)
			if (fileName.indexOf(WellsToProcess[i].name) > 0)
				return true;
		return false;
	}

}

