/**
 * DataSaver_Parameters.java
 *
 * @author Created by Omnicore CodeGuide
 */

package dataSavers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import main.MainGUI;
import main.Plate;

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
			
			Plate[] thePlates = TheMainGUI.getPlateHoldingPanel().getThePlates();
			int numPlates = thePlates.length;
			for (int rr = 0; rr < numPlates; rr++)
			{
				Plate plate = thePlates[rr];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				
				//printing out each well's parameter set
				for (int r = 0; r < numR; r++)
					for (int c =0; c < numC; c++)
						if (plate.getTheWells()[r][c].TheParameterSet
								.isModified())
						{
							System.out
									.println(plate.getTheWells()[r][c].TheParameterSet
											.getProcessType()
											+ "");
							plate.getTheWells()[r][c].printParameterSet(pw);
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

