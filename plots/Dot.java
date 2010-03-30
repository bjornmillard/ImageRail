/**
 * Dot.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots;


import java.awt.Point;
import java.awt.Rectangle;

import us.hms.systemsbiology.segmentedobject.Cell;

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

