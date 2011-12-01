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
