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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;



public abstract class JPanel_highlight extends JPanel implements MouseListener, MouseMotionListener
{
	public JPanel ThePanel;
	public Rectangle highlightBox;
	public Point startHighlightPoint;
	public Point startBox_XY;
	public boolean continuallyDisplayBox;
	static final public AlphaComposite translucComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
	private Color highlightColor;
	
	public JPanel_highlight()
	{
		ThePanel = this;
		addMouseListener(this);
		addMouseMotionListener(this);
		highlightColor = Color.white;
		highlightBox = null;
		startHighlightPoint = null;
		continuallyDisplayBox = false;
	}
	
	public void updatePanel()
	{
		validate();
		repaint();
	}
	
	public void continuallyDisplayHighlightBox(boolean b)
	{
		continuallyDisplayBox = b;
	}
	
	public void setHightlightColor(Color color)
	{
		highlightColor = color;
	}
	
	public void paintHighlighting(Graphics2D g2)
	{
		//for the highlighting region
		if (highlightBox!=null)
		{
			g2.setColor(highlightColor);
			Composite orig = g2.getComposite();
			g2.setComposite(translucComposite);
			
			g2.fill(highlightBox);
			g2.draw(highlightBox);
			g2.setComposite(orig);
		}
	}
	
	
	public Rectangle getHighlightBox()
	{
		if (highlightBox!=null && highlightBox.width*highlightBox.height > 20)
		{
			return highlightBox;
		}
			//if no region is selected, return the whole thing
		else return new Rectangle(0,0,ThePanel.getWidth(), ThePanel.getHeight());
	}
	
	
	
	public void mouseClicked(MouseEvent p1)
	{
		if (p1.getClickCount()>=2)
		{
			highlightBox = null;
			startHighlightPoint = null;
		}
		
		updatePanel();
	}
	public void mousePressed(MouseEvent p1)
	{
		startHighlightPoint = p1.getPoint();
		highlightBox = new Rectangle();
		highlightBox.x = startHighlightPoint.x;
		highlightBox.y = startHighlightPoint.y;
		highlightBox.width = 0;
		highlightBox.height = getHeight();
		updatePanel();
	}
	public void mouseReleased(MouseEvent p1)
	{
		
		//reseting the highlightbox
		if (!continuallyDisplayBox)
		{
			highlightBox=null;
			startHighlightPoint = null;
		}
		updatePanel();
	}
	public void mouseEntered(MouseEvent e)
	{
		
	}
	public void mouseExited(MouseEvent p1){}
	
	public void mouseDragged(MouseEvent p1)
	{
		//select region if not directly on something
		int xval = p1.getPoint().x-startHighlightPoint.x;
		if (xval>=0)
			highlightBox.width = xval;
		else
		{
			highlightBox.x = p1.getPoint().x;
			highlightBox.width = -xval;
		}
		
		updatePanel();
	}
	public void mouseMoved(MouseEvent p1)
	{
	}
	
}

