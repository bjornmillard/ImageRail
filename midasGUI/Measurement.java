/**
 * Measurement.java
 *
 * @author Created by Omnicore CodeGuide
 */

package midasGUI;

public class Measurement
{
	public int ID;
	public String name;

	
	public Measurement(String name_)
	{
		name = name_;
		ID = (int)(Math.random()*100000000);
	}
	
	public String toString()
	{
		return name;
	}
}


