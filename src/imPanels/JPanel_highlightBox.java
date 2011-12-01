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


import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public abstract class JPanel_highlightBox extends JPanel_highlight
{
	public boolean CreateNewBox;
	public int dX;
	public int dY;
	
	public void mousePressed(MouseEvent p1)
	{
		
		if (highlightBox!=null && highlightBox.contains(p1.getPoint()))
		{
			dX = p1.getX()-highlightBox.x;
			dY = p1.getY()-highlightBox.y;
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
			
			highlightBox = new Rectangle();
			highlightBox.x = startHighlightPoint.x;
			highlightBox.y = startHighlightPoint.y;
			highlightBox.width = 0;
			highlightBox.height = getHeight();
			
			if (xval>=0)
				highlightBox.width = xval;
			else
			{
				highlightBox.x = p1.getPoint().x;
				highlightBox.width = -xval;
			}
			if (yval>=0)
				highlightBox.height = yval;
			else
			{
				highlightBox.y = p1.getPoint().y;
				highlightBox.height = -yval;
			}
		}
		else
		{
			highlightBox.x = p1.getX()-dX;
			highlightBox.y = p1.getY()-dY;
		}
		updatePanel();
	}
}


