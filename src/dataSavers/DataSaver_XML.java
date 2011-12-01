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
 * DataSavers_CSV.java
 *
 * @author BLM
 */

package dataSavers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

import models.Model_Main;
import models.Model_Plate;
import features.Feature;
import gui.MainGUI;

public class DataSaver_XML implements DataSaver
{
	public void save(Feature[] featuresToSave, Model_Main TheMainModel)
	{
		JFileChooser fc = null;
		if (models.Model_Main.getModel().getTheDirectory() != null)
			fc = new JFileChooser(models.Model_Main.getModel()
					.getTheDirectory());
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
			models.Model_Main.getModel().setTheDirectory(
					new File(outDir.getParent()));
			printXML(outDir, true, TheMainModel.getGUI(), featuresToSave);
		}
	}
	
	/** Saves the feature out but also saves the cooresponding ten-fifty-ninty range for each data series
	 * @author BLM*/
	public void save(Feature featureToSave, float[][] tenFiftyNinty, MainGUI TheMainGUI, boolean log)
	{
		JFileChooser fc = null;
		if (models.Model_Main.getModel().getTheDirectory() != null)
			fc = new JFileChooser(models.Model_Main.getModel()
					.getTheDirectory());
		else
			fc = new JFileChooser();
		
		File outDir = null;
		
		fc.setDialogTitle("Save as...");
		int returnVal = fc.showSaveDialog(TheMainGUI);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outDir = fc.getSelectedFile();
			outDir = (new File(outDir.getAbsolutePath()+".xml"));
		}
		else
			System.out.println("Open command cancelled by user." );
		
		if (outDir!=null)
		{
			models.Model_Main.getModel().setTheDirectory(
					new File(outDir.getParent()));
			printXML(outDir, true, featureToSave, tenFiftyNinty, log);
		}
	}
	
	
	private void printXML(File outDir, boolean printStdev, MainGUI TheMainGUI, Feature[] featuresToSave)
	{
		try
		{
			PrintWriter pw = new PrintWriter(outDir);
			pw.println("<?xml version='1.0' encoding='UTF-8'?>");
			pw.println("<parsedData>");
			

			Model_Plate plate = TheMainGUI.getPlateHoldingPanel().getModel()
					.getPlates()[0];
			
			//printing out each well's value
			int numF = models.Model_Main.getModel().getTheFeatures().size();
			for (int i = 0; i < numF; i++)
			{
				Feature feature = ((Feature) models.Model_Main.getModel()
						.getTheFeatures().get(i));
				//Only Print Certain Features
				if (shouldPrint(feature, featuresToSave))
				{
					
					pw.println(feature.Name);
					for (int r = 0; r < plate.getNumRows(); r++)
					{
						for (int c =0; c < plate.getNumColumns(); c++)
							if (plate.getWells()[r][c].Feature_Means!=null)
								pw.print(plate.getWells()[r][c].Feature_Means[i]+",");
							else
								pw.print("0,");
						
						if(printStdev)
						{
							pw.print(", , , ");
							for (int c =0; c < plate.getNumColumns(); c++)
								if (plate.getWells()[r][c].Feature_Stdev!=null)
									pw.print(plate.getWells()[r][c].Feature_Stdev[i]+",");
								else
									pw.print("0,");
						}
						pw.println();
					}
					pw.println();
					pw.println();
					pw.println();
					
				}
			}
			
			pw.println("/>");
			pw.println("</parsedData>");
			
			
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
	}
	
	private void printXML(File outDir, boolean printStdev, Feature featuresToSave, float[][] tenFiftyNintyRange, boolean LogScaleValues)
	{
		
		try
		{
			MainGUI TheMainGUI = models.Model_Main.getModel().getGUI();
			PrintWriter pw = new PrintWriter(outDir);
			pw.println("<?xml version='1.0' encoding='UTF-8'?>");
			pw.println("<parsedData>");
			
			Model_Plate plate = TheMainGUI.getPlateHoldingPanel().getModel()
					.getPlates()[0];
			//printing out each well's value
			int numF = models.Model_Main.getModel().getTheFeatures().size();
			for (int i = 0; i < numF; i++)
			{
				Feature feature = ((Feature) models.Model_Main.getModel()
						.getTheFeatures().get(i));
				double[][] bimodalDistance = plate.getBimodalDistance(feature);
				//Only Print Certain Features
				if (shouldPrint(feature, featuresToSave))
				{
					
					//Calculating the normalized Dispersion
					double minDisp = Double.POSITIVE_INFINITY;
					double maxDisp = Double.NEGATIVE_INFINITY;
					double[][] normDispersions = new double[plate.getNumRows()][plate.getNumColumns()];
					for (int r = 0; r <  plate.getNumRows(); r++)
						for (int c =0; c <  plate.getNumColumns(); c++)
							if ( plate.getWells()[r][c].Feature_Stdev!=null)
							{
								normDispersions[r][c] = (Math.pow( plate.getWells()[r][c].Feature_Stdev[i],1)/ plate.getWells()[r][c].Feature_Means[i]);
								if (normDispersions[r][c]<minDisp)
									minDisp=normDispersions[r][c];
								if (normDispersions[r][c]>maxDisp)
									maxDisp = normDispersions[r][c];
							}
					//norming
					System.out.println("min: "+minDisp  +"   max: "+maxDisp);
					for (int r = 0; r <  plate.getNumRows(); r++)
						for (int c =0; c <  plate.getNumColumns(); c++)
							normDispersions[r][c] = (normDispersions[r][c]-minDisp)/(maxDisp-minDisp);
					//
					//
					//Calculating the normalized Dispersion
					double minMean = Double.POSITIVE_INFINITY;
					double maxMean = Double.NEGATIVE_INFINITY;
					double[][] normMeans = new double[ plate.getNumRows()][ plate.getNumColumns()];
					for (int r = 0; r <  plate.getNumRows(); r++)
						for (int c =0; c <  plate.getNumColumns(); c++)
							if ( plate.getWells()[r][c].Feature_Means!=null)
							{
								normMeans[r][c] =  plate.getWells()[r][c].Feature_Means[i];
								if (normMeans[r][c]<minMean)
									minMean=normMeans[r][c];
								if (normMeans[r][c]>maxMean)
									maxMean = normMeans[r][c];
							}
					//norming
					for (int r = 0; r <  plate.getNumRows(); r++)
						for (int c =0; c <  plate.getNumColumns(); c++)
							normMeans[r][c] = (normMeans[r][c]-minMean)/(maxMean-minMean);
					//
					//
					
					
					
					
					for (int r = 0; r < plate.getNumRows(); r++)
					{
						for (int c =0; c <  plate.getNumColumns(); c++)
							if ( plate.getWells()[r][c].Feature_Means!=null)
							{
								
								pw.println("<point x='"+r+"'  y='"+c+"'   z='"+ normMeans[r][c]+"'  colorValue1='"+normDispersions[r][c] +"'  colorValue2='"+bimodalDistance[r][c]+"'/>");
//								pw.println("<point x='"+r+"'  y='"+c+"'   z='"+ normDispersions[r][c] +"'  colorValue='"+normMeans[r][c]+"'/>");
								
//								if (r%2==0 && r!=0 && TheMainGUI.ThePlate.TheWells[r-1][c].Feature_Means!=null)
//								{
//									double thisX = r;
//									double lastX = r-1;
//
//									double thisZ = TheMainGUI.ThePlate.TheWells[r][c].Feature_Means[i];
//									double lastZ = TheMainGUI.ThePlate.TheWells[r-1][c].Feature_Means[i];
//
//									double thisD = normDispersions[r][c];
//									double lastD = normDispersions[r-1][c];
//
//									int numSteps = 20;
//									for (int j = 0; j < numSteps; j++)
//									{
//										double fraction = ((float)j/(float)numSteps);;
//										double rVal =(lastX)+fraction;
//										double zVal = lastZ + (thisZ-lastZ)*fraction;
//										double dVal = lastD+(thisD-lastD)*fraction;
//
//										pw.println("<point x='"+rVal+"'  y='"+c+"'   z='"+ zVal+"'  colorValue='"+dVal+"'/>");
//									}
//								}
//								if (c%2==0 && c!=0 && TheMainGUI.ThePlate.TheWells[r][c-1].Feature_Means!=null)
//								{
//									double thisY = c;
//									double lastY = c-1;
//
//									double thisZ = TheMainGUI.ThePlate.TheWells[r][c].Feature_Means[i];
//									double lastZ = TheMainGUI.ThePlate.TheWells[r][c-1].Feature_Means[i];
//
//									double thisD = normDispersions[r][c];
//									double lastD = normDispersions[r][c-1];
//
//									int numSteps = 20;
//									for (int j = 0; j < numSteps; j++)
//									{
//										double fraction = ((float)j/(float)numSteps);;
//										double cVal =(lastY)+fraction;
//										double zVal = lastZ + (thisZ-lastZ)*fraction;
//										double dVal = lastD+(thisD-lastD)*fraction;
//
//										pw.println("<point x='"+r+"'  y='"+cVal+"'   z='"+ zVal+"'  colorValue='"+dVal+"'/>");
//									}
//								}
							}
						
					}
					pw.println();
					pw.println();
					pw.println();
				}
			}
			
			
			
			pw.println("</parsedData>");
			
			
			
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
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
	
	public boolean shouldPrint(Feature f, Feature featuresToPrint)
	{
		if (featuresToPrint.Name.equalsIgnoreCase(f.Name))
			return true;
		
		return false;
	}
	
	
}





