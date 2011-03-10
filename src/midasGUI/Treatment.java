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

package midasGUI;


public class Treatment
{
	public int ID;
	public String name;
	public float value;
	public String units;
	public float timeValue;
	public String timeUnits;
	
	public Treatment(String name_, float val, String units_, float timeValue_,
			String timeUnits_)
	{
		name = name_;
		value = val;
		units = units_;
		ID = (int)System.currentTimeMillis();
		timeValue = timeValue_;
		timeUnits = timeUnits_;
	}
	
	public String toString()
	{
		return (name+" - "+value+" "+units);
	}
	
}


