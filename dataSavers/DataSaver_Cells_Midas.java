/**
 * DataSavers_Cells_Midas.java
 *
 * @author Created by Omnicore CodeGuide
 */

package dataSavers;

import features.Feature;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import main.MainGUI;
import main.Plate;
import main.Well;
import midasGUI.Measurement;
import tempObjects.Cell_RAM;

public class DataSaver_Cells_Midas implements DataSaver
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
				
				//Finding the unique measurment headers across all well
				int numFeatures = featuresToSave.length;
				Measurement[] meas = new Measurement[numFeatures];
				for (int i=0; i< numFeatures; i++)
					meas[i] = new Measurement(featuresToSave[i].ChannelName);
				
				ArrayList uniqueM = new ArrayList();
				for (int i=0; i< numFeatures; i++)
					uniqueM.add(meas[i]);
				
				for (int i = 0; i<uniqueM.size(); i++)
					headers.add("DV:"+((Measurement)uniqueM.get(i)).name);
				
				//Printing out the headers
				for(int i =0; i < headers.size()-1; i++)
					pw.print(((String)headers.get(i))+",");
				pw.println((String)headers.get(headers.size()-1));
					
				Plate[] thePlates = TheMainGUI.getThePlateHoldingPanel().getThePlates();
				int numPlates = thePlates.length;
				for (int p = 0; p < numPlates; p++)
				{
					Plate plate = thePlates[p];
					int numC = plate.getNumColumns();
					int numR = plate.getNumRows();
					
					
					//Printing out each well's value
					for (int r = 0; r < numR; r++)
						for (int c =0; c < numC; c++)
						{
							Well theWell = plate.getTheWells()[r][c];
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
			if (featuresToPrint[i].ChannelName.equalsIgnoreCase(f.ChannelName))
				return true;
		}
		return false;
	}
}

