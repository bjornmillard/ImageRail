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



import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

public class ProgressBar_ImageRail extends JPanel implements MouseListener,  Runnable
{
	static final public Font SmallFont = new Font("Helvetca", Font.BOLD, 9);
	private Rectangle TheBounds;
	private float progress;
	private NumberFormat nf = new DecimalFormat("0.##");
	private Color color1;
	private Color color2;
	public boolean visible;
	private ProgressBar_ImageRail TheParentPanel;
	private String message;
	private float lastProgress;
	private boolean stop;
	private Thread animation;
	private boolean stopThread;
	private float ValueToPaint;
	
	public ProgressBar_ImageRail()
	{
		TheParentPanel = this;
		TheBounds = new Rectangle(0,0, 100, 20);
		TheParentPanel = this;
		stopThread = false;
		addMouseListener(this);
		lastProgress = 0;
		progress = 0;
		stop = false;
		color1 = Color.gray;
		color2 = Color.lightGray;
		visible = true;
		ValueToPaint = 0;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		setColors(Color.gray, Color.white);
		int scale = 4;
		//Making a gradient for this whole panel for stylistic purposes
		GradientPaint gradient = new GradientPaint(0, 0, Color.white, 0,
				getHeight() / 1.2f, Color.darkGray, true);
		g2.setPaint(gradient);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		ValueToPaint = progress;
		draw(g2);
		
	}
	
	public void start()
	{
		setVisible(true);
		System.out.println("starting");
		animation = new Thread(TheParentPanel);
		animation.start();
	}
	public void stopThread()
	{
		stopThread = true;
		animation = null;
		setVisible(false);
	}
	
	public void run()
	{
		float val = progress - lastProgress;
		float inc = val / 10f;

		while (true) {
			setVisible(true);
			addProgress(inc);
			System.out.println("repainting: " + progress);
			// force JPanel to repaint
			repaint();

			try {
				Thread.sleep(500);
			} catch (Exception ie) {
				break;
			}
			if (stopThread)
				break;
			if (getProgress() >= progress)
				break;
		}
		
		
	}
	
	public void setMessage(String mess)
	{
		message = mess;
	}
	
	public void setParentPanel(ProgressBar_ImageRail panel)
	{
		TheParentPanel = panel;
	}
	public void stop(boolean boo)
	{
		stop = boo;
	}
	public boolean shouldStop()
	{
		return stop;
	}
	public void setColor(Color color)
	{
		color1 = color;
		color2 = color;
	}
	public void setColors(Color color_1, Color color_2)
	{
		color1 = color_1;
		color2 = color_2;
	}
	public void setProgress(float val)
	{
		lastProgress = progress;
		progress = val;
		repaint();
	}
	public void addProgress(float val)
	{
		lastProgress = progress;
		progress+=val;
//		repaint();
	}
	public float getProgress()
	{
		return progress;
	}
	public float getLastProgress()
	{
		return lastProgress;
	}
	
	public void draw(Graphics g)
	{
		if (visible)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			
			
			if (progress>1f)
				progress=1;
			
			int arc = 3;
			int width = 100;
			int xStart = getWidth()-width-20;
			int yStart = 5;
			int height = 10;
			
			//Drawing the empty progress bar
			GradientPaint gradient = new GradientPaint(xStart, yStart, Color.white, xStart, yStart+height/2.5f, Color.lightGray, true);
			g2.setPaint(gradient);
			g2.fillRoundRect(xStart, yStart, (int)(width*1f), height, arc, arc);
			g2.setColor(Color.black);
			g2.drawRoundRect(xStart, yStart, (int)(width*1f), height,arc, arc);
			//Drawing the progress rectangle
			gradient = new GradientPaint(xStart, yStart, color1, xStart, yStart+height/2.5f, color2, true);
			g2.setPaint(gradient);
			if (progress>0)
			{
				g2.fillRoundRect(xStart, yStart, (int)(width*progress), height, arc, arc);
				g2.setColor(Color.black);
				g2.drawRoundRect(xStart, yStart, (int)(width*progress), height,arc, arc);
			}
			g2.setColor(Color.black);
			g2.drawRoundRect(xStart, yStart, width, height, arc,arc);
			
			TheBounds.x = xStart;
			TheBounds.y = yStart;
			TheBounds.width = width;
			TheBounds.height = height;
			
			g2.setFont(SmallFont);
			g2.setColor(Color.black);
//				if (message==null)
//			g2.drawString((int)(100f*ValueToPaint)+"%", TheBounds.x+TheBounds.width/2f-20, TheBounds.y+TheBounds.height/2f+4);
//		else
			g2.drawString("Click to Cancel",  TheBounds.x+TheBounds.width/2f-35, TheBounds.y+TheBounds.height/2f+4);
			
			
		}
	}
	
	
	//Catch and prevent all mouse events to prevent user interacting with rest of GUI
	public void mouseClicked(MouseEvent p1)
	{
		if (TheBounds.contains(p1.getPoint()))
			stop = true;
	}
	public void mousePressed(MouseEvent p1){}
	public void mouseReleased(MouseEvent p1){}
	public void mouseEntered(MouseEvent p1){}
	public void mouseExited(MouseEvent p1){}
	
}
