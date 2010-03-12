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
	
	public Treatment(String name_, float val, String units_)
	{
		name = name_;
		value = val;
		units = units_;
		ID = (int)System.currentTimeMillis();
	}
	
	public String toString()
	{
		return (name+" - "+value+" "+units);
	}
	
}


