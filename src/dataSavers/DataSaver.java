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
 * DataSaver.java
 *
 * @author BLM
 */

package dataSavers;

import features.Feature;
import gui.MainGUI;

public interface DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI);
	public boolean shouldPrint(Feature f, Feature[] featuresToPrint);
}
