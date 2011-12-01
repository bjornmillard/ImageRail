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

/**
 * DataSavers_Cells_Midas.java
 *
 * @author Created by BLM
 */

package dataSavers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import midasGUI.Measurement;
import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;
import features.Feature;

public class DataSaver_Cells_Midas implements DataSaver
{
	public void save(Feature[] featuresToSave, Model_Main TheMainModel)
	{
		JFileChooser fc = null;
		if (models.Model_Main.getModel().getTheDirectory()!=null)
			fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
		else
			fc = new JFileChooser();
		
		File outDir = null;
		
		fc.setDialogTitle("Save as...");
		int returnVal = fc.showSaveDialog(TheMainModel.getGUI());
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outDir = fc.getSelectedFile();
			outDir = (new File(outDir.getAbsolutePath()+".csv"));
		}
		else
			System.out.println("Open command cancelled by user." );
		
		if (outDir!=null)
		{
			models.Model_Main.getModel().setTheDirectory(new File(outDir.getParent()));
			try
			{
				PrintWriter pw = new PrintWriter(outDir);
				
				//Print headers
				ArrayList headers = new ArrayList();
				ArrayList headerValues = new ArrayList();
				headers.add("Well");
				headers.add("Plate");
				
				//Finding the unique measurment headers across all well
				int numFeatures = featuresToSave.length;
				Measurement[] meas = new Measurement[numFeatures];
				for (int i=0; i< numFeatures; i++)
					meas[i] = new Measurement(featuresToSave[i].Name);
				
				ArrayList uniqueM = new ArrayList();
				for (int i=0; i< numFeatures; i++)
					uniqueM.add(meas[i]);
				
				for (int i = 0; i<uniqueM.size(); i++)
					headers.add("DV:"+((Measurement)uniqueM.get(i)).name);
				
				//Printing out the headers
				for(int i =0; i < headers.size()-1; i++)
					pw.print(((String)headers.get(i))+",");
				pw.println((String)headers.get(headers.size()-1));
					
				Model_Plate[] thePlates = models.Model_Main.getModel()
						.getPlateRepository().getPlates();
				int numPlates = thePlates.length;
				for (int p = 0; p < numPlates; p++)
				{
					Model_Plate plate = thePlates[p];
					int numC = plate.getNumColumns();
					int numR = plate.getNumRows();
					
					
					//Printing out each well's value
					for (int r = 0; r < numR; r++)
						for (int c =0; c < numC; c++)
						{
							Model_Well theWell = plate.getWells()[r][c];
							if (theWell.isSelected() && theWell.getCell_values()!=null)
							{
								int numCells = 	theWell.getCell_values().length;
								float[][] fVals = theWell.getCell_values();
								for (int j =0; j < numCells; j++)
								{
									headerValues = new ArrayList();
									headerValues.add(theWell.name);
									headerValues.add((theWell.getPlate().getTitle()+""));
//									headerValues.add(" "+theWell.measurementTime);
									
									//printing out the headerValues
									for(int i =0; i < headerValues.size(); i++)
										if (((String)headerValues.get(i))!=null)
										{
											pw.print(" "+((String)headerValues.get(i))+",");
										}
										else pw.print(" ,");
									
										
									for (int i=0; i< featuresToSave.length; i++)
										pw.print(" "+fVals[j][featuresToSave[i].getGUIindex()]+",");
									
									pw.println();
								}
							}
						}
				}
				pw.flush();
				pw.close();
			}
			catch (FileNotFoundException e) {System.out.println("Error Printing File");}
		}
	}
	
	public boolean shouldPrint(Feature f, Feature[] featuresToPrint)
	{
		int len = featuresToPrint.length;
		for (int i = 0; i < len; i++)
		{
			if (featuresToPrint[i].Name.equalsIgnoreCase(f.Name))
				return true;
		}
		return false;
	}
}

