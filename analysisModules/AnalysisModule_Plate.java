/**
 * PlateGraph.java
 *
 * @author BLM
 */

package analysisModules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import main.MainGUI;
import main.Plate;
import main.Well;
import plots.LinePlot;

public abstract class AnalysisModule_Plate extends Plate implements AnalysisModule
{
	public  Plate TheParentPlate;
	public AnalysisModule_Plate ThePlateGraph;
	public String Title;
	public JFrame TheFrame;
	
	public AnalysisModule_Plate(Plate parentPlate, String title, int width, int height)
	{
		super(parentPlate.getNumRows(), parentPlate.getNumColumns(), parentPlate.getID());
		ThePlateGraph = this;
		TheParentPlate = parentPlate;
		setToolBarPosition(BorderLayout.SOUTH);
		
		
		JButton but = new JButton(tools.Icons.Icon_Record);
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						JFileChooser fc = null;
						if (MainGUI.getGUI().getTheDirectory()!=null)
							fc = new JFileChooser(MainGUI.getGUI().getTheDirectory());
						else
							fc = new JFileChooser();
						
						File outDir = null;
						
						fc.setDialogTitle("Save as...");
						int returnVal = fc.showSaveDialog(ThePlateGraph);
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							outDir = fc.getSelectedFile();
							outDir = (new File(outDir.getAbsolutePath()+".csv"));
						}
						else
							System.out.println("Open command cancelled by user." );
						
						PrintWriter pw = null;
						try
						{
							pw = new PrintWriter(outDir);
						}
						catch (FileNotFoundException e) {System.out.println("error creating pw");}
						
						if (pw!=null)
						{
							exportData(pw);
						}
						
					}
				});
		getTheToolBar().add(but);
		
		
		setSize(width,height);
		setYstart(55);
		setTitle(title);
		setVisible(true);
		
		//Converting the Wells --> wellGraphs
		int nR = parentPlate.getNumRows();
		int nC = parentPlate.getNumColumns();
		for (int r = 0; r < nR; r++)
			for (int c = 0; c < nC; c++)
				ThePlateGraph.getTheWells()[r][c] = new WellGraph(ThePlateGraph, r,c);
		
		updatePanel();
	}
	

	public boolean isResizable()
	{
		return false;
	}
	
	/** Handles mouse clicks on the plate
	 * @author BLM*/
	public void mouseClicked(MouseEvent p1)
	{
		if (p1.getClickCount() >= 2)
		{
			highlightBox = null;
			startHighlightPoint = null;
			unHighlightAllWells();
		}
		
		int[] inWell = getWellIndex(p1);
		
		if (inWell != null)
		{
			if (p1.getClickCount() != 1)
				return;
			Well well = getTheWells()[inWell[0]][inWell[1]];
			well.toggleHighlightState();
			
			inWell = null;
		}
	}
	
	public void exportData(PrintWriter pw)
	{
	}
	
	
	/** Handles mouse drag events
	 * @author BLM*/
	public void mouseDragged(MouseEvent p1)
	{
	}
	/** Handles mouse release events
	 * @author BLM*/
	public void mouseReleased(MouseEvent p)
	{
	}
	
	//Overide the normal plate plotting to show bimodal fits
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		//Drawing the Title String on top
		if (Title!=null)
		{
			int len = Title.length();
			g2.setFont(MainGUI.Font_18);
			g2.drawString(Title, getWidth()/2-(len*15f/2f),MainGUI.Font_18.getSize()+10);
			g2.setFont(MainGUI.Font_8);
		}
		// drawing the wells
		for (int r = 0; r < getNumRows(); r++)
			for (int c = 0; c < getNumColumns(); c++)
				getTheWells()[r][c].draw(g2);
		
		// drawing the box borders
		g2.setColor(Color.gray);
		g2.fillRect((getXstart() - 3), (getYstart() - 3), (getPlateWidth() - 3), (getPlateHeight() + 8));
		
		// drawing a graded border around it
		for (int i = 0; i < 5; i++)
		{
			g2.setColor(getBorderColors()[i]);
			g2.drawRect((getXstart() - 3 - i), (getYstart() - 3 - i),
							(getPlateWidth() - 2 + 2 * i),
							(getPlateHeight() + 8 + 2 * i));
		}
		
		updateWellDisplayColors();
		
		PrintWriter pw = getThePrintWriter();
		// drawing the wells
		for (int r = 0; r < getNumRows(); r++)
			for (int c = 0; c < getNumColumns(); c++)
			{
				//For when printing off bimodal/monomodal fit data --> end of row hard return
				if (c==getNumColumns()-1)
					if (pw!=null)
						getThePrintWriter().println();
				
				getTheWells()[r][c].draw(g2);
			}
		//For when printing off bimodal/monomodal fit data --> end of row hard return
		if (pw!=null)
		{
			pw.flush();
			pw.close();
			pw = null;
		}
		
		if (shouldDisplayRowLegend())
		{
			if (MainGUI.getGUI().getLinePlot().getPlotType() == LinePlot.ROWS)
			{
				for (int i = 0; i < getNumRows(); i++)
				{
					int y = (int) (getYstart() + i
									   * (getTheWells()[i][0].outline.width + 3)
									   + getTheWells()[i][0].outline.width / 2f - 3);
					int x = (int) (getXstart() + getNumColumns()
									   * (getTheWells()[i][0].outline.width + 3));
					
					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			}
			else
			{
				for (int i = 0; i < getNumColumns(); i++)
				{
					int y = getYstart() - 15;
					int x = (int) (getXstart() + i
									   * (getTheWells()[0][i].outline.width + 3)
									   + getTheWells()[0][i].outline.width / 2f - 3);
					
					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			}
		}
		
		

	}
	
	
	
}

