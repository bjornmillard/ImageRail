/**
 * Treatment.java
 *
 * @author Created by Omnicore CodeGuide
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


