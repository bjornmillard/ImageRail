/**
 * PlateGraph_bimodalFit.java
 *
 * @author BLM
 */

package analysisModules;
import features.Feature;
import java.awt.Color;
import java.awt.Polygon;
import java.io.PrintWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import main.MainGUI;
import main.Plate;
import tempObjects.Cell_RAM;
import tools.GaussianDistribution;

public class Plate_bimodalFit extends AnalysisModule_Plate
{
	private GaussianDistribution[][][] distributions;
	public JFrame TheFrame;
	
	public Plate_bimodalFit(Plate plate, String title, int width, int height)
	{
		super(plate, title, width, height);
		
		distributions = new GaussianDistribution[getNumRows()][getNumColumns()][2];
		//Now adding graphics to each wellgraph
		
		for (int r = 0; r < getNumRows(); r++)
			for (int c = 0; c < getNumColumns(); c++)
			{
				int[] bins = addHistogram(r,c,50);
				addGaussians(r,c,1,bins);
			}
		
	}

	
	public boolean isResizable()
	{
		return false;
	}
	
	public JPanel getPanel()
	{
		return this;
	}
	
	private int[] addHistogram(int r, int c, int numBins)
	{
//		Plate plate = TheParentPlate;
//		Feature feature = MainGUI.getGUI().getTheSelectedFeature();
//		double minVal = 0;
//		double maxVal = 1;
//
//		if (!MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
//		{
//			minVal = plate.getMinMaxFeatureValues()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
//			maxVal = plate.getMinMaxFeatureValues()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
//		}
//		else
//		{
//			if (plate.getMinMaxFeatureValues_log()!=null)
//			{
//				minVal = plate.getMinMaxFeatureValues_log()[0][MainGUI.getGUI().getTheSelectedFeature_Index()];
//				maxVal = plate.getMinMaxFeatureValues_log()[1][MainGUI.getGUI().getTheSelectedFeature_Index()];
//			}
//		}
//
//
//		//Getting the orig cells from master plate
//		Cell_RAM[] cells = plate.getTheWells()[r][c].TheCells;
//		if (cells!=null && cells.length>0)
//		{
//			int len = cells.length;
//			//Getting the display well that we are going to plot into
//			WellGraph well = (WellGraph)ThePlateGraph.getTheWells()[r][c];
//
//			float dX = (float)well.xLen/(float)numBins;
//			int[] bins = new int[numBins];
//
//			//Binning values
//			for (int i = 0; i < len; i++)
//			{
//				double val = feature.getValue(cells[i]);
//				if (MainGUI.getGUI().getPlateHoldingPanel().isLogScaled())
//				{
//					if (val<=1)
//						val = 1;
//					val = tools.MathOps.log(val);
//				}
//				else
//					if (val<=0)
//						val = 0;
//
//				if (val<1000000000 && val>=1)
//				{
//					double normVal = (val-minVal)/(maxVal-minVal);
//					int ind = (int)(numBins*normVal);
//					if (ind>0&&ind<numBins-1)
//						bins[ind]++;
//				}
//			}
//
//			//Finding max bin value so we can normalize the histogram
//			float maxBinVal = 0;
//			for (int i = 0; i < numBins; i++)
//				if (bins[i]>maxBinVal)
//					maxBinVal=bins[i];
//
//			//Creating the polygon
//			if(maxBinVal>0)
//			{
//				Polygon p = new Polygon();
//				for (int i = 0; i < numBins; i++)
//					p.addPoint((int)(well.xStart+dX*i), (int)((well.yStart+well.yLen)-well.yLen*bins[i]/maxBinVal));
//
//				p.addPoint((int)(well.xStart+dX*(numBins-1)), well.yStart+well.yLen);
//				p.addPoint(well.xStart, well.yStart+well.yLen);
//				p.npoints = numBins+2;
//				well.addPolygonToDraw(p, Color.white);
//			}
//			return bins;
//		}
		return null;
	}
	
	
	
	
	private void addGaussians(int r, int c, int numGaussians, int[] bins)
	{
		//TODO
//
//		if (bins==null)
//			return;
//
//		Plate plate = TheParentPlate;
//		//Getting the orig cells from master plate
//		Cell_RAM[] cells = plate.getTheWells()[r][c].TheCells;
//		if (cells!=null && cells.length>0)
//		{
//			int len = cells.length;
//			//Getting the display well that we are going to plot into
//			WellGraph well = (WellGraph)ThePlateGraph.getTheWells()[r][c];
//
//			int numBins = bins.length;
//			float dX = (float)well.xLen/(float)numBins;
//
//			GaussianDistribution[] mono = tools.GaussianDistribution.fitGaussians(bins, 1);
//			GaussianDistribution[] bi = tools.GaussianDistribution.fitGaussians(bins, 2);
//
//
//			//Getting a real distribution out of the fitted parameters so we can plot it
//			int numB = bins.length;
//			double[][] monomodalResults_histo = new double[mono.length][numB];
//			tools.GaussianDistribution.getGaussian(mono[0].Parameters, monomodalResults_histo[0]);
//			//Getting real bimnodal distributions
//			double[][]  bimodalResults_histo = new double[bi.length][numB];
//			tools.GaussianDistribution.getGaussian(bi[0].Parameters, bimodalResults_histo[0]);
//			tools.GaussianDistribution.getGaussian(bi[1].Parameters, bimodalResults_histo[1]);
//
//			double error_mono = tools.GaussianDistribution.getPercentageError_L1Dist(monomodalResults_histo, bins);
//			double error_bi = tools.GaussianDistribution.getPercentageError_L1Dist(bimodalResults_histo, bins);
//
//			//Now finally deciding if we should use 1 or 2 modes
//			double[][] results = null;
//
//			if (Math.abs(bi[0].Parameters[1]-bi[1].Parameters[1])<0.05f || bi[1].Parameters[0]/bi[0].Parameters[0] <0.1 || bi[1].Parameters[1] > 1)
//			{
//				//If these 2 modes are very close, let us just converge them into a single mode
//				results = monomodalResults_histo;
//				distributions[r][c][0] = mono[0];
//				distributions[r][c][1] = null;
//			}
//			else
//			{
//				//else, take the results that gives us the smallest error
//				if (error_bi<error_mono)
//				{
//					System.out.println("BIMODAL DIST: "+Math.abs(bi[0].Parameters[1]-bi[1].Parameters[1]));
//					results = bimodalResults_histo;
//
//					distributions[r][c] = bi;
//				}
//				else
//				{
//					results = monomodalResults_histo;
//					distributions[r][c][0] = mono[0];
//					distributions[r][c][1] = null;
//				}
//			}
//
//			//Finding max bin value so we can normalize the histogram
//			float maxBinVal = 0;
//			for (int i = 0; i < numBins; i++)
//				if (bins[i]>maxBinVal)
//					maxBinVal=bins[i];
//
//			//NOW PLOTTING
//			int numGauss=results.length;
//			for (int i = 0; i < numGauss; i++)
//			{
//				Polygon p = new Polygon();
//				for (int j = 0; j < numBins; j++)
//					p.addPoint((int)(well.xStart+dX*j), (int)((well.yStart+well.yLen)-well.yLen*results[i][j]/maxBinVal));
//
//				p.addPoint((int)(well.xStart+dX*(numBins-1)), well.yStart+well.yLen);
//				p.addPoint(well.xStart, well.yStart+well.yLen);
//				p.npoints = numBins+2;
//
//				well.addPolygonToDraw(p, tools.ColorRama.getColor(i));
//			}
//
//		}
	}
	
	/*** Exports the modal information and the distance of the 2 modes
	 * @author BLM*/
	public void exportData(PrintWriter pw)
	{
		if (pw!=null)
		{
			pw.println("Aplitudes:,");
			for (int r = 0; r < distributions.length; r++)
				for (int i = 0; i < 3; i++)
				{
					if (i<2)
						for (int c = 0; c < distributions[0].length; c++)
						{
							if (distributions[r][c][i]!=null)
								pw.print(distributions[r][c][i].Parameters[0]+",");
							else
								pw.print("0,");
							
							if(c==ThePlateGraph.getNumColumns()-1)
							{
								pw.print(",");
								pw.print(",");
								pw.print(",");
							}
						}
					else
					{
						for (int c = 0; c < distributions[0].length; c++)
						{
							if (distributions[r][c][0]!=null && distributions[r][c][1]!=null)
								pw.print(Math.abs(distributions[r][c][0].Parameters[0]-distributions[r][c][1].Parameters[0])+",");
							else
								pw.print("0,");
							
							if (c==ThePlateGraph.getNumColumns()-1)
								pw.println(",");
						}
					}
				}
		}
		pw.println(",");
		pw.println("Means:,");
		for (int r = 0; r < distributions.length; r++)
			for (int i = 0; i < 3; i++)
			{
				if (i<2)
					for (int c = 0; c < distributions[0].length; c++)
					{
						if (distributions[r][c][i]!=null)
							pw.print(distributions[r][c][i].Parameters[1]+",");
						else
							pw.print("0,");
						
						if (i==1 && c==ThePlateGraph.getNumColumns()-1)
							pw.println(",");
						else if(c==ThePlateGraph.getNumColumns()-1)
						{
							pw.print(",");
							pw.print(",");
							pw.print(",");
						}
					}
				else
				{
					for (int c = 0; c < distributions[0].length; c++)
					{
						if (distributions[r][c][0]!=null && distributions[r][c][1]!=null)
							pw.print(Math.abs(distributions[r][c][0].Parameters[1]-distributions[r][c][1].Parameters[0])+",");
						else
							pw.print("0,");
						
						if (c==ThePlateGraph.getNumColumns()-1)
							pw.println(",");
					}
				}
				
			}
		
		pw.println(",");
		pw.println("Width:,");
		for (int r = 0; r < distributions.length; r++)
			for (int i = 0; i < 3; i++)
			{
				if(i<2)
					for (int c = 0; c < distributions[0].length; c++)
					{
						if (distributions[r][c][i]!=null)
							pw.print(distributions[r][c][i].Parameters[2]+",");
						else
							pw.print("0,");
						
						if (i==1 && c==ThePlateGraph.getNumColumns()-1)
							pw.println(",");
						else if(c==ThePlateGraph.getNumColumns()-1)
						{
							pw.print(",");
							pw.print(",");
							pw.print(",");
						}
					}
				else
				{
					for (int c = 0; c < distributions[0].length; c++)
					{
						if (distributions[r][c][0]!=null && distributions[r][c][1]!=null)
							pw.print(Math.abs(distributions[r][c][0].Parameters[2]-distributions[r][c][1].Parameters[2])+",");
						else
							pw.print("0,");
						
						if (c==ThePlateGraph.getNumColumns()-1)
							pw.println(",");
					}
				}
			}
		
		
		pw.flush();
		
		validate();
		repaint();
		updatePanel();
	}
	
}

