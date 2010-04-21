/**
 * DataSaver.java
 *
 * @author Created by Omnicore CodeGuide
 */

package dataSavers;

import features.Feature;
import gui.MainGUI;

public interface DataSaver
{
	public void save(Feature[] featuresToSave, MainGUI TheMainGUI);
	public boolean shouldPrint(Feature f, Feature[] featuresToPrint);
}

