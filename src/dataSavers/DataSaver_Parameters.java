/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

/**
 * DataSaver_Parameters.java
 *
 * @author BLM
 */

package dataSavers;

import gui.MainGUI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import models.Model_Plate;

public class DataSaver_Parameters
{
	static public void save(MainGUI TheMainGUI, File outDir)
	{
		try
		{
			PrintWriter pw = new PrintWriter(outDir);
			
			pw.println("<?xml version='1.0' encoding='UTF-8'?>");
			pw.println();
			pw.println("<Parameters>");
			pw.println();
			
			Model_Plate[] thePlates = TheMainGUI.getPlateHoldingPanel()
					.getModel().getPlates();
			int numPlates = thePlates.length;
			for (int rr = 0; rr < numPlates; rr++)
			{
				Model_Plate plate = thePlates[rr];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				
				//printing out each well's parameter set
				for (int r = 0; r < numR; r++)
					for (int c =0; c < numC; c++)
						if (plate.getWells()[r][c].TheParameterSet
								.isModified())
						{
							System.out
									.println(plate.getWells()[r][c].TheParameterSet
											.getProcessType()
											+ "");
							plate.getWells()[r][c].printParameterSet(pw);
						}
			}
			
			pw.println();
			pw.println("</Parameters>");
			pw.println();
			pw.println();
			pw.println();
			
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
	}
}

