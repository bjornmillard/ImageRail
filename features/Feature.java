/**
 * Feature.java
 *
 * @author Bjorn Millard
 */

package features;

import gui.MainGUI;

import java.util.ArrayList;
import us.hms.systemsbiology.segmentedobject.CellCoordinates;

public abstract class Feature
{
	/**
	 * Some features require extra indices, such as, what channel or compartment to draw the information from
	 *
	 * @author BLM
	 * */
	public int ChannelIndex;
	public String ChannelName;
	
	public Feature()
	{
		ChannelIndex=0;
		ChannelName="no name";
	}
	
	
	public boolean isMultiSpectralFeature()
	{
		return false;
	}
	
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return -1;
	}
	
	public void setChannelIndex(int index)
	{
		ChannelIndex = index;
	}
	
	public void setChannelName(String name)
	{
		ChannelName = name;
	}
	public String getChannelName()
	{
		return ChannelName;
	}
	
	public String toString()
	{
		if (ChannelName==null || ChannelName.equalsIgnoreCase(""))
			return "Feature has no name";
		return ChannelName;
	}
	
	public boolean isSameFeature(Feature f)
	{
		if (f.ChannelName.equalsIgnoreCase(ChannelName))
			return true;
		return false;
	}
	
	/** Returns the index found in the static MainGUI array
	 * @author BLM*/
	public int getGUIindex()
	{
		ArrayList features = MainGUI.getGUI().getTheFeatures();
		int len = features.size();
		for (int i = 0; i < len; i++)
		{
			Feature f = (Feature)features.get(i);
			if(f.toString().equalsIgnoreCase(this.toString()))
				return i;
		}
		return -1;
	}
}

