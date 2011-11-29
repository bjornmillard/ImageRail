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
			if (args.length == 6) {

				// Reading commandline parameters
				// ARG_0 --- Project name that we want to process
				// ARG_1 ---> Path desired for new HDF5 file for stored data
				// ARG_2 ---> Plate start index
				// ARG_3 ---> Well start index
				// ARG_4 ---> Plate end index
				// ARG_5 ---> Well end index


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
							if (TheWells.get(i).getFields()[0]
.getParameterSet() != null
								&& TheWells.get(i).getFields()[0]
										.getParameterSet().getParameterNames() != null
								&& TheWells.get(i).getFields()[0]
										.getParameterSet().getParameterNames().length > 0) // Only
																							// process
																// wells with
																// psets
						{
							if (args.length == 6) // Means we want to limit the
													// range of cells to process
					{
								int plateStartIndex = Integer.parseInt(args[2]
										.trim());
								int plateEndIndex = Integer.parseInt(args[4]
										.trim());
								int wellStartIndex = Integer.parseInt(args[3]
										.trim());
								int wellEndIndex = Integer.parseInt(args[5]
										.trim());

						Model_Well well = TheWells.get(i);
						int pIndex = well.getPlate().getID();
						int wIndex = well.getWellIndex();
						if (pIndex >= plateStartIndex
								&& pIndex <= plateEndIndex
								&& wIndex >= wellStartIndex
								&& wIndex <= wellEndIndex)
						wellsWIm.add(TheWells.get(i));

					}
 else if (args.length == 2) // only added input and
															// output path so
															// process all wells
								wellsWIm.add(TheWells.get(i));
						}
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
						+ "ARG_2 ---> (int) Plate Start Index\n"
						+ "ARG_3 ---> (int) Well Start Index\n"
						+ "ARG_4 ---> (int) Plate End Index\n"
						+ "ARG_5 ---> (int) Well End Index\n";

				st += "***********************************************\n";
				st += "***********************************************\n\n\n\n";
				System.out.println(st);
			}


		} catch (Exception e) {

			e.printStackTrace();
		}
	}



}
