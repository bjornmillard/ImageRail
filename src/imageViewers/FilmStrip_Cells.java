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

package imageViewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.media.jai.TiledImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import segmentedobject.Cell;


public class FilmStrip_Cells  extends JFrame implements WindowListener
{
	public FilmStrip_Cells TheFrame;
	public DisplayPanel TheDisplayPanel;
	private Cell[] cells;
	public BufferedImage[][] TheImageArray;
	private TiledImage CurrIm;
	private String LastImagePath;
	
	public FilmStrip_Cells(Cell[] cells)
	{
		TheFrame = this;
		TheDisplayPanel = new DisplayPanel(cells);
		JScrollPane scrollPane = new JScrollPane(TheDisplayPanel);
		getContentPane().add(scrollPane,BorderLayout.CENTER);
		
		TheFrame.setVisible(true);
		addWindowListener(TheFrame);
	}
	
	private class DisplayPanel extends JPanel
	{
		
		public int numC;
		private int yMax;
		private int xTotalLen;
		private int[] widths;
		
		public DisplayPanel(Cell[] cells_)
		{
			cells = cells_;
			int xOffset = 0;
			int num = cells_.length;
			TheDisplayPanel = this;
			TheDisplayPanel.setBackground(Color.black);
			numC = models.Model_Main.getModel().getNumberOfChannels();
			TheImageArray = new BufferedImage[numC][num];
			yMax = 0;
			xTotalLen = 0;
			int counter =0;
			widths = new int[num];
			//For each channel
			for (int c = 0; c < numC; c++)
			{
				
				//Look at each cell
				for (int i = 0; i < num; i++)
				{
					
					Rectangle roi = cells[i].getCoordinates().getBoundingBox();
					
					if (roi!=null)
					{
						counter++;
						if (c==0)
						{
							if (roi.height>yMax)
								yMax = roi.height;
							widths[i] = roi.width;
							xTotalLen+=roi.width;
						}
						
						//TODO - fix filesOfOrigin for each cell
//						File[] files = cells[i].getFilesOfOrigin();
//
//						if (!files[c].getAbsolutePath().equalsIgnoreCase(LastImagePath))
//						{
//							LastImagePath = files[c].getAbsolutePath();
//							RenderedImage im = JAI.create("fileload", LastImagePath);
//
////						//Rescaling max value wrt parameters set in ImageViewer
////						double[] subtract = new double[1];
////						subtract[0] = 0;
////						double[] divide   = new double[1];
////						divide[0]   = (double)Model_Main.MAXPIXELVALUE/(MainGUI.TheMainGUI.MaxValues_ImageDisplay[c]);
////						// Now we can rescale the pixels gray levels:
////						ParameterBlock pbRescale = new ParameterBlock();
////						pbRescale.add(divide);
////						pbRescale.add(subtract);
////						pbRescale.addSource(im);
//							RenderedImage im2 = FieldViewer.rescale(im,c);//JAI.create("rescale", pbRescale,null);
//							//
//
//							CurrIm = new TiledImage(im2, true);
//						}
						
						TiledImage ts = CurrIm.getSubImage(roi.x, roi.y, roi.width, roi.height);
						if (ts!=null)
						{
							
							
							
							BufferedImage bf = ts.getAsBufferedImage();
							
							//todo - this isnt scaling properly
//						WritableRaster raster = bf.getRaster();
//
//						int width = raster.getWidth();
//						int height = raster.getHeight();
//
//						int numBands = raster.getNumBands();
//						int[] pix = new int[raster.getNumBands()];
//						for (int r =0; r < height; r++)
//							for (int rr =0; rr < width; rr++)
//							{
//								raster.getPixel(rr,r,pix);
//								for (int p = 0; p < numBands; p++)
//								{
//									pix[p] = (int)(((float)pix[p])/((float)MainGUI.TheMainGUI.MaxValues_ImageDisplay[p])*255f);
//									raster.setPixel(rr,r,pix);
//								}
//
//							}
							//
							
							TheImageArray[c][i] = bf;
							
						}
					}
				}
			}
			System.out.println("cells.length: "+counter/numC);
			setPreferredSize(new Dimension(xTotalLen, (3*yMax)));
			int xLen = 600;
			
			Dimension dim = new Dimension(xLen, (int)(4.5f*yMax));
			TheFrame.setPreferredSize(dim);
			TheFrame.setSize(dim);
		}
		
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.black);
			g2.fillRect(0,0,getWidth(), getHeight());
			int counter = 0;
			int xOffset = 0;
			int len = TheImageArray[0].length;
			for (int i = 0; i < len; i++)
			{
				
				for (int c = 0; c < numC; c++)
				{
					if (TheImageArray[c][i]!=null)
					{
						g2.drawImage(TheImageArray[c][i], null, xOffset, c*yMax);
					}
				}
				xOffset+= widths[i];
				
			}
//			System.out.println("counter: "+counter);
		}
	}
	
	public void windowOpened(WindowEvent p1){}
	public void windowClosing(WindowEvent p1)
	{
		killAll();
		System.gc();
	}
	public void windowClosed(WindowEvent p1){}
	public void windowIconified(WindowEvent p1){}
	public void windowDeiconified(WindowEvent p1){}
	public void windowActivated(WindowEvent p1){}
	public void windowDeactivated(WindowEvent p1){}
	
	public void killAll()
	{
		int len = TheImageArray.length;
		int num = TheImageArray[0].length;
		for (int i = 0; i < len; i++)
			for (int j = 0; j < num; j++)
				TheImageArray[i][j] = null;
		TheImageArray = null;
		CurrIm = null;
		LastImagePath = null;
	}
}
