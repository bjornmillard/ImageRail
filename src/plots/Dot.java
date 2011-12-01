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

package plots;


import java.awt.Point;
import java.awt.Rectangle;

import segmentedobject.Cell;

public class Dot
{
	public Rectangle box;
	private Cell TheCell;
	public Point.Float point;
	
	public Dot(float xVal, float yVal, Cell cell_)
	{
		box = new Rectangle();
		point = new Point.Float(xVal, yVal);
		TheCell = cell_;
	}
	
	/** Sets the underlying cell that represents this dot selected
	 * @author BLM*/
	public void setSelected(boolean boo)
	{
		TheCell.setSelected(boo);
	}
	
	/** Returns whether the underlying cell that represents this dot is selected
	 * @author BLM*/
	public boolean isSelected()
	{
		return TheCell.isSelected();
	}
	
	/**
	 * Returns the cell this dot was originiated from
	 * 
	 * @author BLM
	 * */
	public Cell getCell()
	{
		return TheCell;
	}
	
	/** Kills this dot to free up RAM
	 * @author BLM*/
	public void kill()
	{
		box = null;
		point = null;
	}
}

