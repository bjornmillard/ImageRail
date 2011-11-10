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

package imPanels;

import gui.MainGUI;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

public class GlassPane_Blocker extends JComponent implements MouseListener
{
	private JComponent ThePanel;
	private boolean started;
	private Area[] ticker;
	private Thread animation;
	private int barsCount = 10;
	private double shield = 0.01;
	private int fps = 10;
	private float rampDelay = 300;
	private float alphaLevel;
	private int Width;
	private int Height;
	private MainGUI TheMainGUI;
	
	public GlassPane_Blocker()
	{
		TheMainGUI = models.Model_Main.getModel().getGUI();
		ThePanel = this;
		Width = TheMainGUI.getWidth(); Height = TheMainGUI.getHeight();
		setSize(Width, Height);
		
		TheMainGUI.getRootPane().setGlassPane(ThePanel);
		start();
		setVisible(true);
//		TheMainGUI.TheMainPanel.remove(1);
//		TheMainGUI.TheMainPanel.add(this,1);
	}
	
	public void start()
	{
		addMouseListener(this);
		setVisible(true);
		ticker = buildTicker();
//		animation = new Thread(new Animator(true));
		animation.start();
	}
	
	
	
	private Area buildPrimitive()
	{
		Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
		Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
		Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);
		Area tick = new Area(body);
		tick.add(new Area(head));
		tick.add(new Area(tail));
		
		return tick;
	}
	
	private Area[] buildTicker()
	{
		Area[] ticker = new Area[barsCount];
		Point2D.Double center = new Point2D.Double((double) Width /2, (double) Height/2);
		double fixedAngle = 2d*Math.PI / ((double) barsCount);
		
		for (double i =0.0; i<(double)barsCount; i++)
		{
			Area primitive = buildPrimitive();
			
			AffineTransform toCenter = AffineTransform.getTranslateInstance(
				center.getX(), center.getY());
			AffineTransform toBorder =
				AffineTransform.getTranslateInstance(45.0, -6.0);
			AffineTransform toCircle =
				AffineTransform.getRotateInstance(-i *fixedAngle,
												  center.getX(), center.getY());
			AffineTransform toWheel = new AffineTransform();
			toWheel.concatenate(toCenter);
			toWheel.concatenate(toBorder);
			
			primitive.transform(toWheel);
			primitive.transform(toCircle);
			
			ticker[(int)i] = primitive;
		}
		
		return ticker;
	}
	
	
//	public void initColors()
//	{
//		colors = new Color[ticker.length];
//		for (int i =0; i < ticker.length; i++)
//		{
//			int channel = 224 - 128 / (i+1);
//			colors[i] = new Color(channel, channel, channel, alphaLevel);
//		}
//		backgroundColor = new Color(255, 255, 255, (int) (alphaLevel * shield));
//	}
	
	public void paintComponent(Graphics g)
	{
		if (started)
		{
			
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2.setColor(new Color(1f,1f,1f, alphaLevel));
			Rectangle r = new Rectangle(0, 0, Width, Height);
			g2.fill(r);
//			g2.draw(r);
			
			
			for (int i =0; i < ticker.length; i++)
			{
				float channel =  (((float)i)/(float)ticker.length);
//				int channel = (int)((float)255 /((i+1)/(float)ticker.length*255));
				g2.setColor(new Color(channel, channel, channel, alphaLevel));
				g2.fill(ticker[i]);
			}
			
			g2.setColor(Color.black);//new Color(0,0,0, alphaLevel));
			g2.drawString("Please wait ...", Width/2f-40, Height/2f+130);
		}
	}
	
	//Catch and prevent all mouse events to prevent user interacting with rest of GUI
	public void mouseClicked(MouseEvent p1){}
	public void mousePressed(MouseEvent p1){}
	public void mouseReleased(MouseEvent p1){}
	public void mouseEntered(MouseEvent p1){}
	public void mouseExited(MouseEvent p1){}
	
	/** Inner class thread runner*/
	private class Animator implements Runnable
	{
		private boolean rampUp;
		public Animator(boolean rampUp_)
		{
			rampUp = rampUp_;
		}
		
		public void run()
		{
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Point2D.Double center = new Point2D.Double((double) Width/ 2,
														   (double) Height/ 2);
			double fixedIncrement = 2.0 *Math.PI / ((double) barsCount);
			AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement,
																		 center.getX(),
																		 center.getY());
			long start = System.currentTimeMillis();
			started = true;
//			if(rampDelay==0)
//				alphaLevel = rampUp ? 255 : 0;
			
			
			boolean inRamp = rampUp;
			boolean outRamp = false;
//			inRamp = false;
			
			while (true)
			{
				if (!outRamp && System.currentTimeMillis()-start>10000)
				{
					outRamp = true;
					inRamp = false;
					start=System.currentTimeMillis();
				}
				
				for (int i =0; i < ticker.length; i++)
					ticker[i].transform(toCircle);
				
				if(inRamp)
				{
					// fade-in/out animation
					long end = System.currentTimeMillis();
					alphaLevel = ((end-start)/rampDelay);
					if (alphaLevel>1)
						alphaLevel=1;
//					System.out.println(alphaLevel);
					
					if ((end-start)	> rampDelay)
					{
//						System.out.println("endRampup");
						inRamp = false;
					}
				}
				else if (outRamp)
				{
					long end = System.currentTimeMillis();
					float elapsedTime = end-start;
					float val = (elapsedTime/rampDelay);
					
					alphaLevel = 1f-val;
//					System.out.println(alphaLevel);
					if (alphaLevel<0)
						alphaLevel=0;
					
					if (elapsedTime	>= rampDelay)
					{
						outRamp = false;
						setVisible(false);
						setCursor(Cursor.getDefaultCursor());
						break;
					}
				}
				
				alphaLevel = 0.75f;
				
//				validate();
				ThePanel.revalidate();
				ThePanel.repaint();
				
				try
				{
					Thread.sleep((1000 / fps));
//					Thread.sleep(outRamp ? 10: (1000 / fps));
				}
				catch (Exception ie)
				{
					break;
				}
				Thread.yield();
			}
		}
	}
}
