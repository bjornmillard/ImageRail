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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Legend
{
	private ArrayList<Species> TheSpecies;
	private Rectangle outline;
	private int boxWidth = 8;
	private boolean dragging;
	private String title;
	
	public Legend(String title_, String[] names, Color[] colors, int x, int y)
	{
		//Init the species
		title = title_;
		int len = names.length;
		TheSpecies = new ArrayList<Species>(len);
		int longestSpeciesName = 0;
		for (int i = 0; i < len; i++)
		{
			TheSpecies.add(new Species(names[i], colors[i]));
			if (names!=null && names[i]!=null && names[i].length()>longestSpeciesName)
				longestSpeciesName = names[i].length();
		}
		if (title!=null && title.length()>longestSpeciesName)
			longestSpeciesName = title.length();
		
		//Init the bounding box
		outline = new Rectangle(x, y, boxWidth+20+longestSpeciesName*7, (len+1)*20+5);
		dragging = false;
	}
	
	
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		if (TheSpecies==null || TheSpecies.size()==0)
			return;
		g2.setFont(gui.MainGUI.Font_12);
		g2.setColor(Color.white);
		g2.fillRect(outline.x, outline.y, outline.width, outline.height);
		g2.setColor(Color.black);
		g2.drawRect(outline.x, outline.y, outline.width, outline.height);
		g2.drawString(title,outline.x+10,outline.y+18);
		for (int i = 0; i < TheSpecies.size(); i++)
			TheSpecies.get(i).draw(g2, outline.x+5, outline.y+10+20*(i+1));
		
	}
	
	public int getWidth()
	{
		return outline.width;
	}
	
	public int getHeight()
	{
		return outline.height;
	}
	
	public int getY()
	{
		return outline.y;
	}
	
	public int getX()
	{
		return outline.x;
	}
	
	public void setDragging(boolean boo)
	{
		dragging = boo;
	}
	
	public boolean isDragging()
	{
		return dragging;
	}
	
	public boolean contains(Point p)
	{
		if (outline!=null && outline.contains(p))
			return true;
		return false;
	}
	
	public void setX(int x)
	{
		outline.x = x;
	}
	
	public void setY(int y)
	{
		outline.y = y;
	}
	
	
	/** The legend is composed of a list of species where each species is composed
	 * of a name and color for the color box
	 * @author BLM*/
	private class Species
	{
		private Color color;
		private String name;
		
		private Species(String name, Color color)
		{
			this.name = name;
			this.color = color;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Color getColor()
		{
			return color;
		}
		
		public void draw(Graphics2D g2, int x, int y)
		{
			g2.setColor(color);
			g2.fillRect(x, y, boxWidth,boxWidth);
			g2.setColor(Color.black);
			g2.drawRect(x,y, boxWidth,boxWidth);
			
			g2.setFont(gui.MainGUI.Font_12);
			if(name!=null)
				g2.drawString(name, x + boxWidth + 6, y+boxWidth);
		}
		/** Free up RAM
		 * @author BLM*/
		public void kill()
		{
			color = null;
			name = null;
		}
	}
	
	/** Free up RAM
	 * @author BLM*/
	public void kill()
	{
		for (int i = 0; i < TheSpecies.size(); i++)
			TheSpecies.get(i).kill();
		outline = null;
	}
}

