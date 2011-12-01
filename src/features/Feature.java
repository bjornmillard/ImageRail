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

/**
 * Feature.java
 *
 * @author Bjorn Millard
 */

package features;

import gui.MainGUI;

import java.util.ArrayList;

import segmentedobject.CellCoordinates;

public abstract class Feature
{
	/**
	 * Some features require extra indices, such as, what channel or compartment to draw the information from
	 *
	 * @author BLM
	 * */
	public int ChannelIndex;
	public String Name;
	public String ChannelName;
	
	public Feature()
	{
		 ChannelIndex=0;
		Name = "no name";
		ChannelName = "no channel";
	}
	
	
	public boolean isMultiSpectralFeature()
	{
		return false;
	}
	
	public float getValue(CellCoordinates cell, int[][][] raster, float[] backgroundValues)
	{
		return -1;
	}
	
	public void setChannelIndex(int index) {
		ChannelIndex = index;
	}

	public int getChannelIndex() {
		return ChannelIndex;
	}
	
	public void setChannelName(String channelName) {
		ChannelName = channelName;
	}

	public String getChannelName() {
		return ChannelName;
	}

	public void setName(String name)
	{
		Name = name;
	}
	public String getName()
	{
		return Name;
	}
	
	public String toString()
	{
		if (Name == null || Name.equalsIgnoreCase(""))
			return "Feature has no name";
		return Name;
	}
	
	public boolean isSameFeature(Feature f)
	{
		if (f.Name.equalsIgnoreCase(Name))
			return true;
		return false;
	}
	
	/** Returns the index found in the static MainGUI array
	 * @author BLM*/
	public int getGUIindex()
	{
		ArrayList<Feature> features = models.Model_Main.getModel().getTheFeatures();
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

