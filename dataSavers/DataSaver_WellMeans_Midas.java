/**
 * DataSaver_WellMeans_Midas.java
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

public class DataSaver_WellMeans_Midas implements DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI)
	{
		JFileChooser fc = new JFileChooser(MainGUI.getGUI().getTheDirectory());
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
			try
			{
				PrintWriter pw = new PrintWriter(outDir);
				
				//Print headers
				ArrayList headers = new ArrayList();
				ArrayList headerValues = new ArrayList();
				headers.add("Well");
				headers.add("Plate");
				
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
				
				
				//Finding the unique measurment headers across all wells
				int numF = featuresToSave.length;
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
				
				ArrayList uniqueM = new ArrayList();
				for (int i=0; i< 2*numF; i++)
					uniqueM.add(meas[i]);
				
				for (int i = 0; i<uniqueM.size(); i++)
					headers.add("DV:"+((Measurement)uniqueM.get(i)).name);
				
				//Printing out the headers
				for(int i =0; i < headers.size()-1; i++)
					pw.print(((String)headers.get(i))+",");
				pw.println((String)headers.get(headers.size()-1));
				
				Plate[] thePlates = TheMainGUI.getPlateHoldingPanel().getThePlates();
				int numPlates = thePlates.length;
				for (int rr = 0; rr < numPlates; rr++)
				{
					Plate plate = thePlates[rr];
					int numC = plate.getNumColumns();
					int numR = plate.getNumRows();
					
					//printing out each well's value
					for (int r = 0; r < numR; r++)
						for (int c =0; c < numC; c++)
						{
							Well theWell = plate.getTheWells()[r][c];
							if (theWell.isSelected())
							{
								
								
								if (theWell.Feature_Means!=null && theWell.Feature_Stdev!=null)
								{
									headerValues = new ArrayList();
									headerValues.add(theWell.name);
									headerValues.add((theWell.getPlate().getTitle()+""));
									//printing out the headerValues
									for(int i =0; i < headerValues.size(); i++)
										if (((String)headerValues.get(i))!=null)
											pw.print(((String)headerValues.get(i))+",");
										else pw.print(",");
									
									
									for (int i=0; i< numF; i++)
										pw.print(theWell.Feature_Means[indices[i]]+",");
									for (int i=0; i< numF; i++)
										pw.print(theWell.Feature_Stdev[indices[i]]+",");
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
