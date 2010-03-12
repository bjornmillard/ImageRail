/**
 * JPanel_highlightBoxOval.java
 *
 * @author Bjorn L Millard
 */

package imPanels;


import java.awt.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

public abstract class JPanel_highlightBoxOval extends JPanel  implements MouseListener, MouseMotionListener
{
	public boolean CreateNewBox;
	public int dX;
	public int dY;
	
	public int Type_Shape;
	public int RECTANGLE = 0;
	public int OVAL = 1;
	
	private Rectangle tempRect;
	private Ellipse2D.Float tempOval;
	public JPanel ThePanel;
	public Shape highlightBox;
	public Point startHighlightPoint;
	public Point startBox_XY;
	public int x_shape;
	public int y_shape;
	public int width_shape;
	public int height_shape;
	public boolean continuallyDisplayBox;
	static final public AlphaComposite translucComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
	private Color highlightColor;
	
	
	public JPanel_highlightBoxOval()
	{
		Type_Shape = OVAL;
		ThePanel = this;
		addMouseListener(this);
		addMouseMotionListener(this);
		highlightColor = Color.white;
		highlightBox = null;
		width_shape = 0;
		height_shape = 0;
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
	
	
	public Shape getHighlightBox()
	{
		if (highlightBox!=null && width_shape*height_shape > 20)
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
	
	public void mouseMoved(MouseEvent p1)
	{
	}
	public void mousePressed(MouseEvent p1)
	{
		
		if (highlightBox!=null && highlightBox.contains(p1.getPoint()))
		{
			dX = p1.getX()-x_shape;
			dY = p1.getY()-y_shape;
			CreateNewBox=false;
		}
		else
		{
			startBox_XY = p1.getPoint();
			startHighlightPoint = p1.getPoint();
			CreateNewBox=true;
		}
		updatePanel();
	}
	
	public void mouseDragged(MouseEvent p1)
	{
		if (CreateNewBox)
		{
			int xval = p1.getPoint().x-startHighlightPoint.x;
			int yval = p1.getPoint().y-startHighlightPoint.y;
			
			if (Type_Shape == RECTANGLE)
			{
				tempRect = new Rectangle();
				tempRect.x = startHighlightPoint.x;
				tempRect.y = startHighlightPoint.y;
				tempRect.width = 0;
				tempRect.height = getHeight();
				
				
				
				if (xval>=0)
					tempRect.width = xval;
				else
				{
					tempRect.x = p1.getPoint().x;
					tempRect.width = -xval;
				}
				if (yval>=0)
					tempRect.height = yval;
				else
				{
					tempRect.y = p1.getPoint().y;
					tempRect.height = -yval;
				}
				
				x_shape = tempRect.x;
				y_shape = tempRect.y;
				width_shape = tempRect.width;
				height_shape = tempRect.height;
				
				highlightBox = tempRect;
			}
			else if (Type_Shape == OVAL)
			{
				tempOval = new Ellipse2D.Float();
				tempOval.x = startHighlightPoint.x;
				tempOval.y = startHighlightPoint.y;
				tempOval.width = 0;
				tempOval.height = getHeight();
				
				
				
				if (xval>=0)
					tempOval.width = xval;
				else
				{
					tempOval.x = p1.getPoint().x;
					tempOval.width = -xval;
				}
				if (yval>=0)
					tempOval.height = yval;
				else
				{
					tempOval.y = p1.getPoint().y;
					tempOval.height = -yval;
				}
				
				x_shape = (int)tempOval.x;
				y_shape = (int)tempOval.y;
				width_shape = (int)tempOval.width;
				height_shape = (int)tempOval.height;
				
				highlightBox = tempOval;
			}
		}
		else //Just translate current shape
		{
			if (Type_Shape == OVAL)
			{
				tempOval = (Ellipse2D.Float)highlightBox;
				tempOval.x = p1.getX()-dX;
				tempOval.y = p1.getY()-dY;
			}
			else if (Type_Shape == RECTANGLE)
			{
				tempRect = (Rectangle)highlightBox;
				tempRect.x = p1.getX()-dX;
				tempRect.y = p1.getY()-dY;
			}
			
			
		}
		updatePanel();
	}
	
}


