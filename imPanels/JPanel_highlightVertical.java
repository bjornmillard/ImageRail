/**
 * JPanel_highlightBox.java
 *
 * @author Bjorn L Millard
 */

package imPanels;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public abstract class JPanel_highlightVertical extends JPanel_highlight
{
	public boolean CreateNewBox;
	public int dX;
	
	public void mousePressed(MouseEvent p1)
	{
		
		if (highlightBox!=null && highlightBox.contains(p1.getPoint()))
		{
			dX = p1.getX()-highlightBox.x;
			CreateNewBox=false;
		}
		else
		{
			startBox_XY = p1.getPoint();
			CreateNewBox=true;
			
			startHighlightPoint = new Point();
			startHighlightPoint.x = p1.getX();
			highlightBox = new Rectangle();
			highlightBox.x = startHighlightPoint.x;
			highlightBox.y = 0;
			highlightBox.width = 0;
			highlightBox.height = getHeight();
		}
		
		updatePanel();
	}
	
	public void mouseDragged(MouseEvent p1)
	{
		if (CreateNewBox)
		{
			int xval = p1.getPoint().x-startHighlightPoint.x;
			if (xval>=0)
				highlightBox.width = xval;
			else
			{
				highlightBox.x = p1.getPoint().x;
				highlightBox.width = -xval;
			}
		}
		else
			highlightBox.x = p1.getX()-dX;
		
		updatePanel();
	}
}
