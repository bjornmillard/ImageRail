/**
 * DotSelectionListener.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots;
import imageViewers.FieldViewer;
import java.util.ArrayList;

import models.Model_Field;
import plots.DotPlot;

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
