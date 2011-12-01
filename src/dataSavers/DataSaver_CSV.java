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

import imagerailio.ImageRail_SDCube;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

import sdcubeio.DataObject;
import sdcubeio.Data_1D;
import sdcubeio.Data_2D;
import sdcubeio.H5IO;
import sdcubeio.H5IO_Exception;

import models.Model_Field;
import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;
import features.Feature;
import gui.MainGUI;

public class DataSaver_CSV implements DataSaver
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
			printCSV(outDir, true, TheMainModel.getGUI(), featuresToSave);
		}
	}
	
	public void save(Model_Main TheMainModel, File outFile)
	{
		System.out.println("Saving results to: "+outFile.getAbsolutePath());
		File outDir = outFile;
		if (outDir!=null)
		{
			models.Model_Main.getModel().setTheDirectory(new File(outDir.getParent()));
			printCSV(outDir, true, TheMainModel.getGUI(), models.Model_Main
					.getModel()
					.getFeatures());
		}
		else
		{
			System.out.println("***Error Printing Results to File***");
		}
	}
	
	/** Saves the feature out but also saves the cooresponding ten-fifty-ninty range for each data series
	 * @author BLM*/
	public void save(Feature featureToSave, float[][] tenFiftyNinty, MainGUI TheMainGUI, boolean log)
	{
		JFileChooser fc = null;
		if (models.Model_Main.getModel().getTheDirectory()!=null)
			fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
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
			models.Model_Main.getModel().setTheDirectory(new File(outDir.getParent()));
			printCSV(outDir, true, featureToSave, tenFiftyNinty, log);
		}
	}
	
	
	private void printCSV(File outDir, boolean printStdev, MainGUI TheMainGUI, Feature[] featuresToSave)
	{
		try
		{
			PrintWriter pw = new PrintWriter(outDir);
			String tab = ", , , ";
			
			Model_Plate[] thePlates = TheMainGUI.getPlateHoldingPanel()
					.getModel().getPlates();
			int numPlates = thePlates.length;
			for (int p = 0; p < numPlates; p++)
			{
				Model_Plate plate = thePlates[p];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				pw.println("PlateID: "+plate.getTitle());
				
				
				String headerTab = "";
				headerTab+=tab;
				for (int c = 0; c < numC; c++)
					headerTab+=",";
				
				
				//printing out each well's value
				int numF = models.Model_Main.getModel().getTheFeatures().size();
				for (int i = 0; i < numF; i++)
				{
					Feature feature = ((Feature) models.Model_Main.getModel()
							.getTheFeatures().get(i));
					//Only Print Certain Features
					if (shouldPrint(feature, featuresToSave))
					{
						
						pw.println(feature.Name+headerTab+"Stdev_"+feature.Name);
						for (int r = 0; r < numR; r++)
						{
							for (int c =0; c < numC; c++)
								if (plate.getWells()[r][c].Feature_Means!=null)
									pw.print(plate.getWells()[r][c].Feature_Means[i]+",");
								else
									pw.print("0,");
							
							if(printStdev)
							{
								pw.print(tab);
								for (int c =0; c < numC; c++)
									if (plate.getWells()[r][c].Feature_Stdev!=null)
										pw.print(plate.getWells()[r][c].Feature_Stdev[i]+",");
									else
										pw.print("0,");
							}
							pw.println();
						}
						pw.println();
						pw.println();
						
						
					}
				}
				
				
				Model_Main model = models.Model_Main.getModel();
				H5IO io = model.getH5IO().getH5IO();
				String h5path = model.getOutputProjectPath()+File.separator+"Data.h5";
				
				//Number of cells --> if segmented
				pw.println("Number of Cells:");
				for (int r = 0; r < numR; r++)
				{
					for (int c =0; c < numC; c++)
					{
						String st = null;
						float totCells = 0;

						Model_Well well = plate.getWells()[r][c];
						Model_Field[] fields = well.getFields();
						if (fields!=null && fields.length>0)
						{
							String pathToSample = model.getH5IO().getHashtable_out().get(model.getH5IO().getIndexKey(
									well.getPlate().getID(),
									well.getWellIndex()));
							
							if(pathToSample!=null)
							for (int i = 0; i < fields.length; i++) {
								
								// Get cell count
								DataObject dataArray;
								try {
									
									String pathDS = pathToSample + "/Children/" + fields[i].getIndexInWell() + "/Data/feature_values";
									io.openHDF5(h5path);
									
									dataArray = io.readDataset(h5path, pathDS);
									if(dataArray!=null)
									{
										Float[][] fVals = (Float[][]) ((Data_2D) dataArray).getData();
										if(fVals!=null)
											totCells+=(int)(fVals.length);									
									}
									io.closeHDF5();
								} catch (H5IO_Exception e) {
									System.out.println("Error reading Cell Count: ");
									e.printStackTrace();
								}
									
								
							}
							

						}
					
						pw.print(totCells+",");
					}
					pw.println();
					
				}
				pw.println();
				pw.println();
				pw.println();
			}
			
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
	}
	
	private void printCSV(File outDir, boolean printStdev, Feature featuresToSave, float[][] tenFiftyNintyRange, boolean LogScaleValues)
	{
		try
		{
			MainGUI TheMainGUI = models.Model_Main.getModel().getGUI();
			PrintWriter pw = new PrintWriter(outDir);
			
			Model_Plate[] thePlates = TheMainGUI.getPlateHoldingPanel()
					.getModel().getPlates();
			int numPlates = thePlates.length;
			for (int p = 0; p < numPlates; p++)
			{
				Model_Plate plate = thePlates[p];
				int numC = plate.getNumColumns();
				int numR = plate.getNumRows();
				pw.println("PlateID: "+plate.getTitle());
				
				
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
						for (int r = 0; r < numR; r++)
						{
							for (int c =0; c < numC; c++)
								if (plate.getWells()[r][c].Feature_Means!=null)
									if(!LogScaleValues)
										pw.print(plate.getWells()[r][c].Feature_Means[i]+",");
									else
										pw.print(tools.MathOps.log(plate.getWells()[r][c].Feature_Means[i])+",");
								else
									pw.print("0,");
							
							if(printStdev)
							{
								pw.print(", , , ");
								for (int c =0; c < numC; c++)
									if (plate.getWells()[r][c].Feature_Stdev!=null)
										if(!LogScaleValues)
											pw.print(plate.getWells()[r][c].Feature_Stdev[i]+",");
										else
											pw.print(tools.MathOps.log(plate.getWells()[r][c].Feature_Stdev[i])+",");
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
				
				int num = tenFiftyNintyRange.length;
				pw.println("X10 , X50 , X90 ");
				for (int i = 0; i < num; i++)
				{
					pw.println(tenFiftyNintyRange[i][0] +","+tenFiftyNintyRange[i][1] +","+tenFiftyNintyRange[i][2]);
				}
				pw.println();
				pw.println();
				pw.println();
				
				//Number of cells --> if segmented
				//TODO
//				pw.println("Number of Cells:");
//				for (int r = 0; r < numR; r++)
//				{
//					for (int c =0; c < numC; c++)
//						if (plate.getTheWells()[r][c].TheCells!=null)
//							pw.print(plate.getTheWells()[r][c].TheCells.length+",");
//						else
//							pw.print("0,");
//					pw.println();
//				}
//
				
			}
			
			
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
	}
	
	public boolean shouldPrint(Feature f, Feature[] featuresToPrint)
	{
		if(featuresToPrint==null)
			return false;
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
	
//	/*** For printing out CSV files of the data that is normalized across each row*/
//	private void printCSVnorm_row(File outDir)
//	{
//		try
//		{
//			PrintWriter pw = new PrintWriter(outDir);
//			//printing out each well's value
//			int numF  = TheFeatures.size();
//			double[][] minVals = new double[ThePlate.numRows][numF];
//			double[][] maxVals = new double[ThePlate.numRows][numF];
//
//			for (int i = 0; i < numF; i++)
//			{
//				for (int r = 0; r < ThePlate.numRows; r++)
//				{
//					//getting the min/max vals for the normalization
//					minVals[r][i] = Double.POSITIVE_INFINITY;
//					maxVals[r][i] = Double.NEGATIVE_INFINITY;
//					for (int c =0; c < ThePlate.numCols; c++)
//					{
//						double val = ThePlate.TheWells[r][c].Feature_Means[i];
//						if (val<minVals[r][i])
//							minVals[r][i]=val;
//						if (val>maxVals[r][i])
//							maxVals[r][i]=val;
//					}
//				}
//
//				Feature feature = ((Feature)TheFeatures.get(i));
//				pw.println(feature.Name);
//				for (int r = 0; r < ThePlate.numRows; r++)
//					for (int c =0; c < ThePlate.numCols; c++)
//						if (ThePlate.TheWells[r][c].Feature_Means!=null)
//							if (c>=(ThePlate.numCols-1))
//								pw.println((ThePlate.TheWells[r][c].Feature_Means[i]-minVals[r][i])/(maxVals[r][i]-minVals[r][i]));
//							else
//								pw.print((ThePlate.TheWells[r][c].Feature_Means[i]-minVals[r][i])/(maxVals[r][i]-minVals[r][i])+",");
//
//				pw.println();
//				pw.println();
//				pw.println();
//			}
//
//			pw.flush();
//			pw.close();
//		}
//		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
//	}
//
//	/*** For printing out CSV files of the data that is normalized across each row*/
//	private void printCSVnorm_col(File outDir)
//	{
//		try
//		{
//			PrintWriter pw = new PrintWriter(outDir);
//			//printing out each well's value
//			int numF  = TheFeatures.size();
//			double[][] minVals = new double[ThePlate.numCols][numF];
//			double[][] maxVals = new double[ThePlate.numCols][numF];
//
//			for (int i = 0; i < numF; i++)
//			{
//				for (int c = 0; c < ThePlate.numCols; c++)
//				{
//					//getting the min/max vals for the normalization
//					minVals[c][i] = Double.POSITIVE_INFINITY;
//					maxVals[c][i] = Double.NEGATIVE_INFINITY;
//					for (int r =0; r < ThePlate.numRows; r++)
//					{
//						if (ThePlate.TheWells[r][c].Feature_Means!=null)
//						{
//							double val = ThePlate.TheWells[r][c].Feature_Means[i];
//							if (val<minVals[c][i])
//								minVals[c][i]=val;
//							if (val>maxVals[c][i])
//								maxVals[c][i]=val;
//						}
//					}
//				}
//
//				Feature feature = ((Feature)TheFeatures.get(i));
//				pw.println(feature.Name);
//				for (int r = 0; r < ThePlate.numRows; r++)
//					for (int c =0; c < ThePlate.numCols; c++)
////						if (ThePlate.TheWells[r][c].Feature_Means!=null)
//						if (c>=(ThePlate.numCols-1))
//							pw.println((ThePlate.TheWells[r][c].Feature_Means[i]-minVals[c][i])/(maxVals[c][i]-minVals[c][i]));
//						else
//							pw.print((ThePlate.TheWells[r][c].Feature_Means[i]-minVals[c][i])/(maxVals[c][i]-minVals[c][i])+",");
//
//				pw.println();
//				pw.println();
//				pw.println();
//			}
//
//			pw.flush();
//			pw.close();
//		}
//		catch (FileNotFoundException e) {System.out.println("Error Printing File");}
//	}
	
}

