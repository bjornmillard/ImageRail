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
import imageViewers.FieldViewer;

import java.util.ArrayList;

public class DotSelectionListener
{
	public DotPlot TheDotPlot;
	private ArrayList listeners;
	
	public DotSelectionListener(DotPlot dotplot)
	{
		TheDotPlot = dotplot;
		listeners = new ArrayList();
	}
	
	public DotSelectionListener()
	{
		
	}
	
	public void setDotPlot(DotPlot d)
	{
		TheDotPlot = d;
	}
	
	public void resetAllDots()
	{
		if (TheDotPlot==null)
			return;
		
		Dot[][] dots = TheDotPlot.TheDots;
		if (dots==null)
			return;
		int numPlots = dots.length;
		for (int p = 0; p < numPlots; p++)
		{
			int numD = dots[p].length;
			for (int i = 0; i < numD; i++)
			{
				Dot dot = dots[p][i];
				dot.setSelected(false);
			}
		}
		TheDotPlot.repaint();
	}
	
	public void updateListenerImages()
	{
		int num = listeners.size();
		for (int i =0; i < num; i++)
				((FieldViewer)listeners.get(i)).repaint();
	}
	
	public void addListener(FieldViewer d)
	{
		listeners.add(d);
	}
	public void removeListener(FieldViewer d)
	{
		int num = listeners.size();
		for (int i = 0; i < num; i++)
		{
			if (((FieldViewer)listeners.get(i)).getID() == d.getID())
			{
				listeners.remove(i);
				break;
			}
		}
	}
	public void updateDotPlot()
	{
	//	if (TheDotPlot!=null)
		//	TheDotPlot.TheGraphPanel.repaint();
	}
	
	public Dot[][] getDots()
	{
		return TheDotPlot.getDots();
	}
	
	public void kill()
	{
		TheDotPlot = null;
		listeners = null;
	}
	
	
}
