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
 * DataSavers_Cells_Midase_wMetaData.java
 *
 * @author Created by BLM
 */

package dataSavers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;
import sdcubeio.ExpDesign_Description;
import sdcubeio.ExpDesign_Model;
import features.Feature;

public class DataSaver_Cells_Midas_wMetaData implements DataSaver
{
	public void save(Feature[] featuresToSave, Model_Main TheMainModel)
	{
		
		//Determining which wells to save
		ArrayList<Model_Well> wells = new ArrayList<Model_Well>();
		File[] files = null;
		Model_Plate[] thePlates = TheMainModel.getPlateRepository().getPlates();
		int numPlates = thePlates.length;
		for (int p = 0; p < numPlates; p++)
		{
			Model_Plate plate = thePlates[p];
			int numC = plate.getNumColumns();
			int numR = plate.getNumRows();
			//printing out each well's value
			for (int r = 0; r < numR; r++)
				for (int c =0; c < numC; c++)
				{
					Model_Well theWell = plate.getWells()[r][c];
					int h5Count = theWell.getHDFcount();
					if (theWell.isSelected() && h5Count>0)
						wells.add(theWell);
				}
		}
		
		
		JFileChooser fc = null;
		if (TheMainModel.getTheDirectory() != null)
			fc = new JFileChooser(TheMainModel.getTheDirectory());
		else
			fc = new JFileChooser();

		File outDir = null;

		fc.setDialogTitle("Save as...");
		int returnVal = fc.showSaveDialog(TheMainModel.getGUI());
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outDir = fc.getSelectedFile();
			outDir.mkdir();
			
			int numF = wells.size();
			files = new File[numF];
			for (int i = 0; i < numF; i++) {
				File f = new File(outDir.getAbsolutePath()+File.separator+"p"+wells.get(i).getPlate().getID()+"_"+wells.get(i).name+".csv");
				files[i] = f;
			}
			
		}
		else
			System.out.println("Open command cancelled by user." );

		if (outDir!=null)
		{
			TheMainModel.setTheDirectory(new File(outDir.getParent()));
			try
			{
				//Print headers
				ArrayList<String> headers = new ArrayList<String>();
				ArrayList<String> headerValues = new ArrayList<String>();
				headers.add("Well");
				headers.add("Plate");
				headers.add("Date");
				headers.add("Description");
				
				//Need to get indicies of the features we want to print
				ArrayList<Feature> arr = TheMainModel.getTheFeatures();
				int counter = 0;
				int num = arr.size();
				int len = featuresToSave.length;
				for (int i = 0; i < num; i++)
				{
					Feature f = ((Feature)arr.get(i));
					for (int j = 0; j < len; j++)
						if(f.Name.equalsIgnoreCase(featuresToSave[j].Name))
							counter++;
				}
				int[] indices = new int[counter];
				counter = 0;
				for (int i = 0; i < num; i++)
				{
					Feature f = ((Feature)arr.get(i));
					for (int j = 0; j < len; j++)
						if(f.Name.equalsIgnoreCase(featuresToSave[j].Name))
						{
							indices[counter] = i;
							counter++;
						}
				}
				
				//Finding the unique treatment headers across all wells of all plates
				ArrayList<ExpDesign_Description> uniqueT = new ArrayList<ExpDesign_Description>();
				ArrayList<ExpDesign_Description> uniqueM = new ArrayList<ExpDesign_Description>();
				int numF = featuresToSave.length;
			
				ExpDesign_Model io = TheMainModel.getExpDesignConnector();

				for (int p = 0; p < numPlates; p++)
				{
					Model_Plate plate = thePlates[p];
					int numC = plate.getNumColumns();
					int numR = plate.getNumRows();
					
					for (int r= 0; r < numR; r++)
						for (int c= 0; c < numC; c++)
						{
							if (plate.getWells()[r][c].isSelected())
							{
								ExpDesign_Description[] treats = io
										.getTreatments(plate.getWells()[r][c]
												.getID());

								 for (int n =0; n < treats.length; n++)
								 {
									 boolean unique = true;
									 len = uniqueT.size();
									 for (int j =0; j < len; j++)
										if (((ExpDesign_Description) uniqueT
												.get(j)).getName()
												.equalsIgnoreCase(
														(treats[n].getName())))
									 unique = false;
									 if (unique)
									 {
										 headers.add("TR:"+(treats[n].getName()));
										 uniqueT.add(treats[n]);
									 }
								 }
							}
						}				
				}

				//Finding the unique measurement headers across all wells - NOTE --> Stealing them all from plate 1 since IR requires same features for all plates/wells
				Model_Plate plate = thePlates[0];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				ExpDesign_Description[] meas = new ExpDesign_Description[numF];
				for (int i=0; i< numF; i++)
				{
					Feature f = featuresToSave[i];
					ExpDesign_Description exd = new ExpDesign_Description();
					exd.setName(f.Name);
					meas[i] = exd;
				}
				//Adding the Stdev
				// for (int i=0; i< numF; i++)
				// {
				// Feature f = featuresToSave[i];
				// ExpDesign_Description exd = new ExpDesign_Description();
				// exd.setName("Stdev_"
				// + f.ChannelName);
				//
				// meas[i + numF] = exd;
				// }
				for (int i = 0; i < numF; i++)
				{
					headers.add("DA:" + meas[i].getName());
					uniqueM.add(meas[i]);
				}
			
				
				for (int i = 0; i<uniqueM.size(); i++)
					headers.add("DV:"
							+ ((ExpDesign_Description) uniqueM.get(i))
									.getName());

				
				//Now printing out a file for each well
				boolean clearCellData = false;
				int numWells = wells.size();
				for(int w = 0; w<numWells; w++)
				{

					PrintWriter pw = new PrintWriter(files[w]);
					//Printing out the headers
					for(int i =0; i < headers.size()-1; i++)
						pw.print(((String)headers.get(i))+",");
					pw.println((String)headers.get(headers.size()-1));
				
				
				//
				// Printing out each cell's value
				//
								Model_Well theWell = wells.get(w);
								clearCellData = false;
								int numCells = 0;
								float[][] fvals = theWell.getCell_values();
								if(fvals!=null)
									numCells = fvals.length;
								//See if cells already loaded, else try to load them
								if(numCells==0)
								{
									theWell.getCells_forceLoad(false, true);
									clearCellData = true;
									fvals = theWell.getCell_values();
									if(fvals!=null)
										numCells = fvals.length;
								}
								if(numCells>0 && fvals!=null)
								{
										 ExpDesign_Description[] treats = io.getTreatments(theWell.getID());
										 ExpDesign_Description date = io.getDate(theWell.getID());
										 ExpDesign_Description desc = io.getDescription(theWell.getID(), "Description");
										 ExpDesign_Description time = io.getTimePoint(theWell.getID());
					
										 headerValues = new ArrayList<String>();
										 headerValues.add(theWell.name);
										 headerValues.add((theWell.getPlate().getTitle()+""));
										if (date != null && date.getValue() != null)
											headerValues.add(date.getValue());
										else
											headerValues.add("");
										if (desc != null && desc.getValue() != null)
											headerValues.add(desc.getValue());
										else
											headerValues.add("");
										// if (time != null && time.getValue() != null)
										// headerValues.add(time.getTimeValue());
																		
										 //for each treatment & measurement here in
										 //this well... determine which column to put it in
										 for (int i= 0; i < uniqueT.size(); i++)
										 {
										 num = treats.length;
										 boolean foundIt = false;
										 for (int n= 0; n < num; n++)
										 {
												if (((ExpDesign_Description) uniqueT
														.get(i)).getName()
														.equalsIgnoreCase(
																(treats[n].getName())))
										 {
										 headerValues.add(""+treats[n].getValue());
										 foundIt = true;
										 break;
										 }
										 }
										 if (!foundIt)
										 headerValues.add("");
										 }
										
										// Adding measurement times
										 String st = "";
										 if (time!=null)
											st += time.getTimeValue();
																		
										for (int n = 0; n < numF; n++)
										 if (time==null)
												headerValues.add("");
										 else
												headerValues.add("" + st);
		
		
										for (int j =0; j < numCells; j++)
										{
											//printing out the headerValues
											if (theWell.Feature_Means!=null && theWell.Feature_Stdev!=null)
											{
												//printing out the headerValues
												for(int i =0; i < headerValues.size(); i++)
													if (((String)headerValues.get(i))!=null)
		 {
														pw.print(((String)headerValues.get(i))+",");
													}
		 else
														pw.print(",");
		
												for (int i=0; i< featuresToSave.length; i++)
		 {
													pw.print(" "+fvals[j][featuresToSave[i].getGUIindex()]+",");
												}
		
											pw.println();
										}
									}
								
						
		
						
		
									pw.flush();
									pw.close();
								}
								if(clearCellData)
									theWell.clearCellData();
			}
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

