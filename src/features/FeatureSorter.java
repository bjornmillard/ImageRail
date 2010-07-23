/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

/**
 * FeatureSorter.java
 *
 * @author Bjorn Millard
 */

package features;

import java.util.Comparator;

public class FeatureSorter implements Comparator
{
	public int compare(Object p1, Object p2)
	{
		Feature f1 = (Feature) p1;
		Feature f2 = (Feature) p2;
		
		String f1n = f1.toString();
		String f2n = f2.toString();
		
		if (f1n.indexOf("Whole")>f2n.indexOf("Whole") )
			return -1;
		
		return 1;
	}
}

