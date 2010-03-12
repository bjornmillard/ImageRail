/**
 * DataSavers_CSV.java
 *
 * @author Created by Omnicore CodeGuide
 */

package dataSavers;

import features.Feature;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.swing.JFileChooser;
import main.MainGUI;
import main.Plate;

public class DataSaver_XML implements DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI)
	{
		JFileChooser fc = null;
		if (TheMainGUI.getTheDirectory()!=null)
			fc = new JFileChooser(TheMainGUI.getTheDirectory());
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
			TheMainGUI.setTheDirectory(new File(outDir.getParent()));
			printXML(outDir, true, TheMainGUI, featuresToSave);
		}
	}
	
	/** Saves the feature out but also saves the cooresponding ten-fifty-ninty range for each data series
	 * @author BLM*/
	public void save(Feature featureToSave, float[][] tenFiftyNinty, MainGUI TheMainGUI, boolean log)
	{
		JFileChooser fc = null;
		if (TheMainGUI.getTheDirectory()!=null)
			fc = new JFileChooser(TheMainGUI.getTheDirectory());
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
			TheMainGUI.setTheDirectory(new File(outDir.getParent()));
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
			

			Plate plate = TheMainGUI.getPlateHoldingPanel().getThePlates()[0];
			
			//printing out each well's value
			int numF  = TheMainGUI.getTheFeatures().size();
			for (int i = 0; i < numF; i++)
			{
				Feature feature = ((Feature)TheMainGUI.getTheFeatures().get(i));
				//Only Print Certain Features
				if (shouldPrint(feature, featuresToSave))
				{
					
					pw.println(feature.ChannelName);
					for (int r = 0; r < plate.getNumRows(); r++)
					{
						for (int c =0; c < plate.getNumColumns(); c++)
							if (plate.getTheWells()[r][c].Feature_Means!=null)
								pw.print(plate.getTheWells()[r][c].Feature_Means[i]+",");
							else
								pw.print("0,");
						
						if(printStdev)
						{
							pw.print(", , , ");
							for (int c =0; c < plate.getNumColumns(); c++)
								if (plate.getTheWells()[r][c].Feature_Stdev!=null)
									pw.print(plate.getTheWells()[r][c].Feature_Stdev[i]+",");
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
			MainGUI TheMainGUI =  MainGUI.getGUI();
			PrintWriter pw = new PrintWriter(outDir);
			pw.println("<?xml version='1.0' encoding='UTF-8'?>");
			pw.println("<parsedData>");
			
			Plate plate = TheMainGUI.getPlateHoldingPanel().getThePlates()[0];
			//printing out each well's value
			int numF  = TheMainGUI.getTheFeatures().size();
			for (int i = 0; i < numF; i++)
			{
				Feature feature = ((Feature)TheMainGUI.getTheFeatures().get(i));
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
							if ( plate.getTheWells()[r][c].Feature_Stdev!=null)
							{
								normDispersions[r][c] = (Math.pow( plate.getTheWells()[r][c].Feature_Stdev[i],1)/ plate.getTheWells()[r][c].Feature_Means[i]);
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
							if ( plate.getTheWells()[r][c].Feature_Means!=null)
							{
								normMeans[r][c] =  plate.getTheWells()[r][c].Feature_Means[i];
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
							if ( plate.getTheWells()[r][c].Feature_Means!=null)
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
			if (featuresToPrint[i].ChannelName.equalsIgnoreCase(f.ChannelName))
				return true;
		}
		return false;
	}
	
	public boolean shouldPrint(Feature f, Feature featuresToPrint)
	{
		if (featuresToPrint.ChannelName.equalsIgnoreCase(f.ChannelName))
			return true;
		
		return false;
	}
	
	
}





