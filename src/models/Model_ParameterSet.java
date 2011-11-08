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

package models;

import imagerailio.ImageRail_SDCube;

import java.util.Enumeration;
import java.util.Hashtable;

import sdcubeio.H5IO;
import sdcubeio.H5IO_Exception;

public class Model_ParameterSet {
	// Hash of all ParameterNames-->ParameterValue(encoded as String)
	private Hashtable<String, String> TheParameters;

	public Model_ParameterSet() {
		TheParameters = new Hashtable<String, String>();
	}

	
	/**
	 * Returns the names of the parameters stored in this model
	 * 
	 * @author BLM
	 * */
	public String[] getParameterNames() {
		String[] keys = null;
		Enumeration<String> e =  TheParameters.keys();
		if(e!=null)
		{
			int count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         String key = en.nextElement();
		         count++;
		  }
		  keys = new String[count];
		  count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         keys[count] = en.nextElement().trim();
		         count++;
		  }
		}
		return keys;
	}

	/**
	 * Returns the values of the parameters stored in this model
	 * 
	 * @author BLM
	 * */
	public String[] getParameterValues() {
		String[] vals = null;
		Enumeration<String> e =  TheParameters.keys();
		if(e!=null)
		{
			int count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         String key = en.nextElement();
		         count++;
		  }
		  vals = new String[count];
		  count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         String key = en.nextElement();
		         vals[count] = TheParameters.get(key.trim()).trim();
		         count++;
		  }
		}
		return vals;
	}

	/**
	 * Returns the names and values of the parameters stored in this model.
	 * Note: the dimensions of the return value are: 2xLenParameters where the
	 * first dimension is the names of the parameters and the second dimension
	 * are the values of the correspondingly indexed parameters encoded as
	 * Strings
	 * 
	 * @author BLM
	 * @param void
	 * @return String[][] [paramNames,paramVals]
	 * */
	public String[][] getParameters() {
		String[][] keyVals = null;
		Enumeration<String> e =  TheParameters.keys();
		if(e!=null)
		{
			int count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         String key = en.nextElement();
		         count++;
		  }
		  keyVals = new String[2][count];
		  count = 0;
		  for (Enumeration<String> en = TheParameters.keys() ; en.hasMoreElements() ;) 
		  {
		         String key = en.nextElement();
		         keyVals[0][count] = key.trim();
		         keyVals[1][count] = TheParameters.get(key.trim()).trim();
		         count++;
		  }
		}
		return keyVals;
	}

	/**
	 * Sets the value of the parameter of the given name. The value is 
	 * encoded as a String 
	 * @author BLM
	 * @param String parameterName, String paramVal
	 *  */
	public void setParameter(String name, String val) {
		System.out.println(name + " , " + val);
		TheParameters.put(name.trim(), val.trim());
	}

	/**
	 * Returns the value of the parameter of the given name. The value is 
	 * encoded as a String 
	 * @author BLM
	 * @param String parameterName
	 * @return String paramValue
	 *  */
	public String getParameter(String name) {
		return TheParameters.get(name.trim()).trim();
	}
	/**
	 * Returns the value of the parameter of the given name. The value is 
	 * encoded as a String but cast as float
	 * @author BLM
	 * @param String parameterName
	 * @return float paramValue
	 *  */
	public float getParameter_float(String name) {
		return(Float.parseFloat(TheParameters.get(name.trim()).trim()));
	}
	/**
	 * Returns the value of the parameter of the given name. The value is 
	 * encoded as a String but cast as int
	 * @author BLM
	 * @param String parameterName
	 * @return float paramValue
	 *  */
	public int getParameter_int(String name) {
		return(Integer.parseInt(TheParameters.get(name.trim()).trim()));
	}
	/**
	 * Returns the value of the parameter of the given name. The value is 
	 * encoded as a String but cast as double
	 * @author BLM
	 * @param String parameterName
	 * @return float paramValue
	 *  */
	public double getParameter_double(String name) {
		return(Double.parseDouble(TheParameters.get(name.trim()).trim()));
	}
	/**
	 * Returns the value of the parameter of the given name. The value is 
	 * encoded as a String but cast as double
	 * @author BLM
	 * @param String parameterName
	 * @return float paramValue
	 *  */
	public String getParameter_String(String name) {
		return TheParameters.get(name.trim()).trim();
	}

/**
 * Returns the value of the parameter of the given name. The value is 
 * encoded as a String but cast as boolean
 * @author BLM
 * @param String parameterName
 * @return float paramValue
 *  */
public boolean getParameter_boolean(String name) {
	String p = TheParameters.get(name.trim()).trim();
	if(p.equalsIgnoreCase("TRUE"))
		return true;
	return false;
}

	/**
	 * Writes the segmentation parameters used by ImageRail to segment the given
	 * well
	 * 
	 * @author Bjorn Millard
	 * @param Model_ParameterSet
	 *            pset
	 * @throws H5IO_Exception
	 */
	public void writeParameters(String hdfPath, int plateID, int wellID,
			int fieldID) throws H5IO_Exception {
		
		String[] pNames = getParameterNames();
		String[] pVals = getParameterValues();

		ImageRail_SDCube imagerail_io = gui.MainGUI.getGUI().getH5IO();
		H5IO h5 = imagerail_io.getH5IO();

		Hashtable<String, String> hashtable_indexToPath = imagerail_io
				.getHashtable();
		String pathToSample = (String) hashtable_indexToPath.get(imagerail_io
				.getIndexKey(plateID, wellID));
		if (pathToSample != null) {
			String path = pathToSample + "/Children/" + fieldID + "/Meta";
			// Remove prior dataset
			if (h5.existsDataset(path + "/Segmentation_Parameters_Names"))
				h5.removeDataset(path + "/Segmentation_Parameters_Names");
			if (h5.existsDataset(path + "/Segmentation_Parameters_Values"))
				h5.removeDataset(path + "/Segmentation_Parameters_Values");

			h5.openHDF5(hdfPath);
			h5.writeStringDataset(path + "/" + "Segmentation_Parameters_Names",
					pNames);
			h5.closeHDF5();

			h5.writeDataset(hdfPath, path + "/"
					+ "Segmentation_Parameters_Values", pVals);

		}
	}


}
