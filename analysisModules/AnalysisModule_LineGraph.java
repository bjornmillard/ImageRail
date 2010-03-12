/**
 * AnalysisModule_LineGraph.java
 *
 * @author Created by Omnicore CodeGuide
 */

package analysisModules;



import javax.swing.*;

import dialogs.CaptureImage_Dialog;
import imPanels.ImageCapturePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import main.MainGUI;
import plots.LinePlot;
import tools.SVG_writer;

public class AnalysisModule_LineGraph extends JPanel implements AnalysisModule, ImageCapturePanel
{
	public JFrame TheFrame;
	public  LinePlot TheParentLinePlot;
	public AnalysisModule_LineGraph TheGraph;
	public String Title;
	public int Ystart;
	public JToolBar TheToolBar;
	public int Width;
	public int Height;
	
	public AnalysisModule_LineGraph(LinePlot parentPlot, String title, int width, int height)
	{
		if (parentPlot==null)
			return;
		
		TheGraph = this;
		TheParentLinePlot = parentPlot;
		setLayout(new BorderLayout());
		TheToolBar = new JToolBar();
		add(TheToolBar, BorderLayout.NORTH);
		
		JButton but = new JButton(new ImageIcon("icons/camera.png"));
		TheToolBar.add(but);
		but.setToolTipText("Capture Image of Plot");
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						CaptureImage_Dialog s = new CaptureImage_Dialog(TheGraph);
					}
				});
		
		but = new JButton(tools.Icons.Icon_Record);
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
						int returnVal = fc.showSaveDialog(TheGraph);
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
		TheToolBar.add(but);
		
		Width = width;
		Height = height;
		
		setSize(width,height);
		Ystart =  80;
		Title = title;
		
		repaint();
	}
	
	/** Sets the container for this panel
	 * @author BLM*/
	public void setParentFrame(AnalysisModuleFrame frame)
	{
		TheFrame  = frame;
	}
	
	public void setFrame(AnalysisModuleFrame f)
	{
		TheFrame = f;
	}
	public boolean isResizable()
	{
		return false;
	}
	
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g2, false);
	}
	
	public int getWidth(){return Width;}
	public int getHeight(){return Height;}
	
	public void exportData(PrintWriter pw)
	{
		
	}
	
	
	//Overide
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		TheParentLinePlot.draw(g2, plotToSVG);
	}
	
	
	
	public void mouseClicked(MouseEvent p1)
	{
		
	}
	
	public void mouseMoved(MouseEvent p1)
	{
		
	}
	
	public void mouseReleased(MouseEvent p1)
	{
		
	}
	
	// Updating dragging
	public void mousePressed(MouseEvent p1)
	{
		
	}
	
	public void mouseDragged(MouseEvent p1)
	{
		
	}
	
	public JPanel getPanel()
	{
		return this;
	}
	
	/** Caputures a PNG or JPG of the plate
	 * @author BLM*/
	public void captureImage(File file, String imageType)
	{
		int width = getWidth();
		int height = getHeight();
		
		BufferedImage im = new BufferedImage(width, height,
											 BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();
		
		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r, c, Color.WHITE.getRGB());
		
		draw(g2, true);
		
		try
		{
			ImageIO.write(im, imageType, file);
		}
		catch (IOException e)
		{
			System.out.println("**Error Printing Image**");
		}
	}
	
	/** Captures a scalable vector graphic of the plate
	 * @author BLM */
	public void captureSVG(PrintWriter pw)
	{
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle(""+Title);
		
		draw(g2, true);
		
		g2.printEnd();
		pw.flush();
		pw.close();
	}
	
}
