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
 * DataSaver_WellMeans_Midas_wMetaData.java
 *
 * @author BLM
 */

package dataSavers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import midasGUI.Measurement;
import midasGUI.Treatment;
import models.Model_Plate;
import models.Model_Well;
import features.Feature;
import gui.MainGUI;

public class DataSaver_WellMeans_Midas_wMetaData implements DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI)
	{
		JFileChooser fc = null;
		if (MainGUI.getGUI().getTheDirectory()!=null)
			fc = new JFileChooser(MainGUI.getGUI().getTheDirectory());
		else
			fc = new JFileChooser();
		
		File outDir = null;
		
		fc.setDialogTitle("Save as...");
		int returnVal = fc.showSaveDialog(TheMainGUI);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outDir = fc.getSelectedFile();
			outDir = (new File(outDir.getAbsolutePath()+".csv"));
		}
		else
			System.out.println("Open command cancelled by user." );
		
		if (outDir!=null)
		{
			MainGUI.getGUI().setTheDirectory(new File(outDir.getParent()));
			try
			{
				PrintWriter pw = new PrintWriter(outDir);
				
				//Print headers
				ArrayList headers = new ArrayList();
				ArrayList headerValues = new ArrayList();
				headers.add("Well");
				headers.add("Plate");
				headers.add("Date");
				headers.add("Description");
				
				//Need to get indicies of the features we want to print
				ArrayList arr = TheMainGUI.getTheFeatures();
				int counter = 0;
				int num = arr.size();
				int len = featuresToSave.length;
				for (int i = 0; i < num; i++)
				{
					Feature f = ((Feature)arr.get(i));
					for (int j = 0; j < len; j++)
						if(f.ChannelName.equalsIgnoreCase(featuresToSave[j].ChannelName))
							counter++;
				}
				int[] indices = new int[counter];
				counter = 0;
				for (int i = 0; i < num; i++)
				{
					Feature f = ((Feature)arr.get(i));
					for (int j = 0; j < len; j++)
						if(f.ChannelName.equalsIgnoreCase(featuresToSave[j].ChannelName))
						{
							indices[counter] = i;
							counter++;
						}
				}
				
				//Finding the unique treatment headers across all wells of all plates
				ArrayList uniqueT = new ArrayList();
				ArrayList uniqueM = new ArrayList();
				int numF = featuresToSave.length;
				Model_Plate[] thePlates = TheMainGUI.getPlateHoldingPanel()
						.getModel().getPlates();
				int numPlates = thePlates.length;
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
								int numTreat = plate.getWells()[r][c].treatments.size();
								for (int n =0; n < numTreat; n++)
								{
									boolean unique = true;
									len = uniqueT.size();
									for (int j =0; j < len; j++)
										if(((Treatment)uniqueT.get(j)).name.equalsIgnoreCase(((Treatment)plate.getWells()[r][c].treatments.get(n)).name))
											unique = false;
									if (unique)
									{
										headers.add("TR:"+((Treatment)plate.getWells()[r][c].treatments.get(n)).name);
										uniqueT.add(((Treatment)plate.getWells()[r][c].treatments.get(n)));
									}
								}
							}
						}
				}
				
				//Finding the unique measurment headers across all wells - NOTE --> Stealing them all from plate 1
				Model_Plate plate = thePlates[0];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				Measurement[] meas = new Measurement[numF*2];
				for (int i=0; i< numF; i++)
				{
					Feature f = featuresToSave[i];
					meas[i] = new Measurement(f.ChannelName);
				}
				//Adding the Stdev
				for (int i=0; i< numF; i++)
				{
					Feature f = featuresToSave[i];
					meas[i+numF] = new Measurement("Stdev_"+f.ChannelName);
				}
				for (int i=0; i< 2*numF; i++)
				{
					headers.add("DA:"+meas[i].name);
					uniqueM.add(meas[i]);
				}
				
				
				
				for (int i = 0; i<uniqueM.size(); i++)
					headers.add("DV:"+((Measurement)uniqueM.get(i)).name);
				
				//Printing out the headers
				for(int i =0; i < headers.size()-1; i++)
					pw.print(((String)headers.get(i))+",");
				pw.println((String)headers.get(headers.size()-1));
				
				for (int p = 0; p < numPlates; p++)
				{
					plate = thePlates[p];
					numC = plate.getNumColumns();
					numR = plate.getNumRows();
					
					//printing out each well's value
					for (int r = 0; r < numR; r++)
						for (int c =0; c < numC; c++)
						{
							Model_Well theWell = plate.getWells()[r][c];
							if (theWell.isSelected())
							{
								headerValues = new ArrayList();
								headerValues.add(theWell.name);
								headerValues.add((theWell.getPlate().getTitle()+""));
								headerValues.add(theWell.date);
								headerValues.add(theWell.description);
								//for each treatment & measurement here in this well... determine which column to put it in
								for (int i= 0; i < uniqueT.size(); i++)
								{
									num = theWell.treatments.size();
									boolean foundIt = false;
									for (int n= 0; n < num; n++)
									{
										if (((Treatment)uniqueT.get(i)).name.equalsIgnoreCase(((Treatment)theWell.treatments.get(n)).name))
										{
											headerValues.add(""+((Treatment)theWell.treatments.get(n)).value);
											foundIt = true;
											break;
										}
									}
									if (!foundIt)
										headerValues.add("");
								}
								
								//adding measurement times (numchannels)*3 <-- numcompartments
								String st = "";
								if (theWell.measurementTime!=null)
									st+=theWell.measurementTime;
								for (int j = 0; j < numF*2; j++)
									if (theWell.measurementTime==null)
										headerValues.add("");
									else
										headerValues.add(""+theWell.measurementTime);
								
								
								if (theWell.Feature_Means!=null && theWell.Feature_Stdev!=null)
								{
									//printing out the headerValues
									for(int i =0; i < headerValues.size(); i++)
										if (((String)headerValues.get(i))!=null)
											pw.print(((String)headerValues.get(i))+",");
										else pw.print(",");
									
									
									for (int i=0; i< numF; i++)
										pw.print(theWell.Feature_Means[indices[i]]+",");
									for (int i=0; i< numF; i++)
										pw.print(theWell.Feature_Stdev[indices[i]]+",");
								}
								pw.println();
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
			if (featuresToPrint[i].ChannelName.equalsIgnoreCase(f.ChannelName))
				return true;
		}
		return false;
	}
}

