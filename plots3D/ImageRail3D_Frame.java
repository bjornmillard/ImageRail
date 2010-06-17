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

package plots3D;


import gui.MainGUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.vecmath.Point3d;

public class ImageRail3D_Frame extends JFrame
{
	public ImageRail3D_MainContainer ThePanel;
	static public File TheFile;
	public  static final int TheColorMap = ColorMaps.RGB_2;
	final static public int ROWS = 0;
	final static public int COLS = 1;
	
	
	public ImageRail3D_Frame(double[][] data, double[][] colorValues)
	{
		super("3D Plotter");
		setResizable(true);
		int width = 800;
		int height = 800;
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		setVisible(true);
		setResizable(true);
		
		
		
		
		//Constructing the data table at bottom
		int maxRowLen = tools.MathOps.getMaxRowLength(data);
		String[] headers = new String[maxRowLen+1];
		String[] headers2 = new String[maxRowLen+1];
		headers[0] = "Series #";
		headers2[0] = "Series #";
		for (int i = 1; i < maxRowLen+1; i++)
		{
			headers[i]=("value_"+i);
			headers2[i]=("value_"+i);
		}
		
		
		int numSeries = data.length;
		//Setting up the color value tabel
		Object[][] dat = new Object[numSeries+1][maxRowLen+1];
		for (int i = 0; i < maxRowLen+1; i++)
			dat[0][i] = headers[i];
		for (int r = 1; r < numSeries+1; r++)
		{
			//Series Number, then data
			dat[r][0] = ""+(r);
			int maxThis = data[r-1].length;
			for (int c = 1; c < maxRowLen+1; c++)
			{
				if (c<=maxThis)
					dat[r][c] = new Float(data[r-1][c-1]);
				else
					dat[r][c] = new Float(0);
			}
		}
		//Setting up the color value tabel
		Object[][] dat2 = new Object[numSeries+1][maxRowLen+1];
		for (int i = 0; i < maxRowLen+1; i++)
			dat2[0][i] = headers[i];
		for (int r = 1; r < numSeries+1; r++)
		{
			//Series Number, then data
			dat2[r][0] = ""+(r);
			int maxThis = colorValues[r-1].length;
			for (int c = 1; c < maxRowLen+1; c++)
			{
				if (c<=maxThis)
					dat2[r][c] = new Float(colorValues[r-1][c-1]);
				else
					dat2[r][c] = new Float(0);
			}
		}
		
		
		
		//Main Plot panel on top
		ThePanel  = new ImageRail3D_MainContainer(data, colorValues);
		//Data table at bottom
		JTable table = new JTable(dat, headers);
		JTable table2 = new JTable(dat2, headers2);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Mean Values",table);
		tabbedPane.addTab("Color Values",table2);
		JScrollPane scrollPane = new JScrollPane(tabbedPane);
		
		//Create a split pane with the two scroll panes in it.
		Container pane = getContentPane();
		JSplitPane TheMainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		TheMainPanel.setPreferredSize(new Dimension(width, height));
		TheMainPanel.setOneTouchExpandable(true);
		TheMainPanel.setDividerLocation((int)(height/1.25f));
		TheMainPanel.setTopComponent(ThePanel);
		TheMainPanel.setBottomComponent(scrollPane);
		TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
		TheMainPanel.validate();
		TheMainPanel.repaint();
		
		
		
		pane.setLayout(new BorderLayout());
		pane.add(TheMainPanel, BorderLayout.CENTER);
		
		
		
		
		validate();
		repaint();
		
	}
	
	
	static public void load(Point3d[] points, double[] colors)
	{
		if (colors!=null && points!=null)
		{
			double[][] ps = organizePointsInGrid(points);
			double[][] colorGrid = organizeColorsInGrid(colors, ps);
			ImageRail3D_Frame f = new ImageRail3D_Frame(ps, colorGrid);
			f.setVisible(true);
		}
		else
		{
			System.out.println("**-- Error in data format sent to 3D plotter --**");
		}
		
	}
	
	static public void load(double[][] points, double[][] colors)
	{
		if (colors!=null && points!=null)
		{
			ImageRail3D_Frame f = new ImageRail3D_Frame(points, colors);
			f.setVisible(true);
		}
		else
		{
			System.out.println("**-- Error in data format sent to 3D plotter --**");
		}
		
	}
	
	
	/** Lets other classes access what is the colormap to color the surface plot
	 * @author BLM*/
	public static int getColorMap()
	{
		return MainGUI.getGUI().getTheColorMapIndex();
	}
	
	public static void main(String[] args)
	{
		loadXML();
	}
	
	
	
	static public void loadXML()
	{
		//creating a practice matrix
		int counter= 1;
		
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(new JPanel());
		TheFile = null;
		Point3d[] points = null;
		double[] colors = null;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			TheFile = fc.getSelectedFile();
			try
			{
				points  = SurfacePlotMainPanel.parsePoints(TheFile);
				colors = SurfacePlotMainPanel.parseColors(TheFile, "colorValue");
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
			
		}
		else
			System.out.println("Save command cancelled by user." );
		
		if (colors!=null && points!=null)
		{
			double[][] ps = organizePointsInGrid(points);
			double[][] colorGrid = organizeColorsInGrid(colors, ps);
			
			ImageRail3D_Frame f = new ImageRail3D_Frame(ps, colorGrid);
			f.setVisible(true);
		}
		else
		{
			System.out.println("ERROR parsing file");
		}
		
		
	}
	
	static public double[][] organizePointsInGrid(Point3d[] points)
	{
		double[][] grid = null;
		int len = points.length;
		
		double xMin = 1000000000;
		double xMax = -100000000;
		double yMin = 1000000000;
		double yMax = -1000000000;
		
		for (int i = 0; i < len; i++)
		{
			Point3d p = points[i];
			if (p.x<xMin)
				xMin = p.x;
			if (p.x>xMax)
				xMax = p.x;
			if (p.y<yMin)
				yMin = p.y;
			if (p.y>yMax)
				yMax = p.y;
		}
		int numRows = (int)(xMax-xMin)+1;
		int numCols = (int)(yMax-yMin)+1;
		
		
		grid = new double[numRows][numCols];
		
		int counter =0;
		for (int r = 0; r < numRows; r++)
		{
			for (int c = 0; c < numCols; c++)
			{
				grid[r][c] = points[counter].z;
			    counter++;
			}
		}
		
		return grid;
	}
	
	
	static public double[][] organizeColorsInGrid(double[] colors, double[][] points)
	{
		int rows = points.length;
		int cols = points[0].length;
		
		double[][] grid = new double[rows][cols];
		int counter =0;
		for (int r = 0; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
			{
				grid[r][c] = colors[counter];
				counter++;
			}
		}
		
		
		return grid;
	}
	
	
	
	
}
