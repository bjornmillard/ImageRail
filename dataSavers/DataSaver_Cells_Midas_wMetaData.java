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
 * DataSavers_Cells_Midase_wMetaData.java
 *
 * @author Created by BLM
 */

package dataSavers;

import features.Feature;
import gui.MainGUI;

public class DataSaver_Cells_Midas_wMetaData implements DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI)
	{
//		JFileChooser fc = null;
//		if (TheMainGUI.getTheDirectory()!=null)
//			fc = new JFileChooser(TheMainGUI.getTheDirectory());
//		else
//			fc = new JFileChooser();
//
//		File outDir = null;
//
//		fc.setDialogTitle("Save as...");
//		int returnVal = fc.showSaveDialog(TheMainGUI);
//		if (returnVal == JFileChooser.APPROVE_OPTION)
//		{
//			outDir = fc.getSelectedFile();
//			outDir = (new File(outDir.getAbsolutePath()+".csv"));
//		}
//		else
//			System.out.println("Open command cancelled by user." );
//
//		if (outDir!=null)
//		{
//			MainGUI.getGUI().setTheDirectory(new File(outDir.getParent()));
//			try
//			{
//				PrintWriter pw = new PrintWriter(outDir);
//
//				//Print headers
//				ArrayList headers = new ArrayList();
//				ArrayList headerValues = new ArrayList();
//				headers.add("Model_Well");
//				headers.add("Date");
//				headers.add("Description");
//
//				int numFeatures = featuresToSave.length;
//				ArrayList uniqueT = new ArrayList();
//				ArrayList uniqueM = new ArrayList();
//				Model_Plate[] plates = TheMainGUI.getPlateHoldingPanel().getThePlates();
//				int numplates = plates.length;;
//				for (int p = 0; p < numplates; p++)
//				{
//					Model_Plate plate = plates[p];
//					int numC = plate.getNumColumns();
//					int numR = plate.getNumRows();
//
//					//Finding the unique treatment headers across all wells
//					for (int r= 0; r < numR; r++)
//						for (int c= 0; c < numC; c++)
//						{
//							if (plate.getTheWells()[r][c].isSelected())
//							{
//								int numTreat = plate.getTheWells()[r][c].treatments.size();
//								for (int n =0; n < numTreat; n++)
//								{
//									boolean unique = true;
//									int len = uniqueT.size();
//									for (int j =0; j < len; j++)
//										if(((Treatment)uniqueT.get(j)).name.equalsIgnoreCase(((Treatment)plate.getTheWells()[r][c].treatments.get(n)).name))
//											unique = false;
//									if (unique)
//									{
//										headers.add("TR:"+((Treatment)plate.getTheWells()[r][c].treatments.get(n)).name);
//										uniqueT.add(((Treatment)plate.getTheWells()[r][c].treatments.get(n)));
//									}
//								}
//							}
//						}
//
//
//
//					//Finding the unique measurment headers across all well
//					Measurement[] meas = new Measurement[numFeatures];
//					for (int i=0; i< numFeatures; i++)
//						meas[i] = new Measurement(featuresToSave[i].ChannelName);
//
//					for (int i=0; i< numFeatures; i++)
//					{
//						headers.add("DA:"+meas[i].name);
//						uniqueM.add(meas[i]);
//					}
//				}
//
//
//				for (int i = 0; i<uniqueM.size(); i++)
//					headers.add("DV:"+((Measurement)uniqueM.get(i)).name);
//
//				//Printing out the headers
//				for(int i =0; i < headers.size()-1; i++)
//					pw.print(((String)headers.get(i))+",");
//				pw.println((String)headers.get(headers.size()-1));
//
//				Model_Plate[] thePlates = MainGUI.getGUI().getPlateHoldingPanel().getThePlates();
//				//Printing out each well's value
//				for (int rr = 0; rr < numplates; rr++)
//				{
//
//					Model_Plate plate = thePlates[rr];
//					int numC = plate.getNumColumns();
//					int numR = plate.getNumRows();
//
//					for (int r = 0; r < numR; r++)
//						for (int c =0; c < numC; c++)
//						{
//							Model_Well theWell = plate.getTheWells()[r][c];
//							if (theWell.isSelected() && theWell.TheCells!=null)
//							{
//								int numCells = 	theWell.TheCells.length;
//								Cell_RAM[] cells = theWell.TheCells;
//
//								for (int j =0; j < numCells; j++)
//								{
//									Cell_RAM cell = cells[j];
//									headerValues = new ArrayList();
//									headerValues.add(theWell.name);
//									headerValues.add(theWell.date);
//									headerValues.add(theWell.description);
//									//For each treatment & measurement here in this well... determine which column to put it in
//									for (int i= 0; i < uniqueT.size(); i++)
//									{
//										int num = theWell.treatments.size();
//										boolean foundIt = false;
//										for (int n= 0; n < num; n++)
//										{
//											if (((Treatment)uniqueT.get(i)).name.equalsIgnoreCase(((Treatment)theWell.treatments.get(n)).name))
//											{
//												headerValues.add(" "+((Treatment)theWell.treatments.get(n)).value);
//												foundIt = true;
//												break;
//											}
//										}
//										if (!foundIt)
//											headerValues.add(" ");
//									}
//
//									//Adding measurement times (numchannels)*3 <-- numcompartments
//									String st = "";
//									if (theWell.measurementTime!=null)
//										st+=theWell.measurementTime;
//									for (int p = 0; p < numFeatures; p++)
//										if (theWell.measurementTime==null)
//											headerValues.add(" ");
//										else
//											headerValues.add(" "+theWell.measurementTime);
//
//									//printing out the headerValues
//									for(int i =0; i < headerValues.size(); i++)
//										if (((String)headerValues.get(i))!=null)
//										{
//											pw.print(" "+((String)headerValues.get(i))+",");
//										}
//										else pw.print(" ,");
//
//									for (int i=0; i< numFeatures; i++)
//										pw.print(" "+featuresToSave[i].getValue(cell)+",");
//
//									pw.println();
//								}
//							}
//						}
//				}
//
//
//				pw.flush();
//				pw.close();
//			}
//			catch (FileNotFoundException e) {System.out.println("Error Printing File");}
//		}
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

