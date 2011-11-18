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
		// System.out.println(name + " , " + val);
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
		if (TheParameters == null || TheParameters.get(name.trim()) == null)
			return -1;
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
		if (TheParameters == null || TheParameters.get(name.trim()) == null)
			return -1;
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
		if (TheParameters == null || TheParameters.get(name.trim()) == null)
			return -1;

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
		if (TheParameters == null || TheParameters.get(name.trim()) == null)
			return null;
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
		if (TheParameters == null || TheParameters.get(name.trim()) == null)
			return false;

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

		ImageRail_SDCube imagerail_io = models.Model_Main.getModel().getH5IO();
		H5IO h5 = imagerail_io.getH5IO();

		Hashtable<String, String> hashtable_indexToPath = imagerail_io
				.getHashtable_out();
		String pathToSample = (String) hashtable_indexToPath.get(imagerail_io
				.getIndexKey(plateID, wellID));

		if (pathToSample != null) {
			h5.openHDF5(hdfPath);
			String path = pathToSample + "/Children/" + fieldID + "/Meta";

			h5.writeDataset(hdfPath, path + "/"
					+ "Segmentation_Parameters_Names",
					pNames);

			h5.writeDataset(hdfPath, path + "/"
					+ "Segmentation_Parameters_Values", pVals);
			h5.closeHDF5();
		}
	}

	/**
	 * Checks if given parameter exists
	 * 
	 * @author BLM
	 * @param String
	 *            ParameterName
	 * @return boolean exists
	 */
	public boolean exists(String pname) {

		if (TheParameters == null)
			return false;
		if (TheParameters.get(pname) == null)
			return false;
		return true;
	}

	/**
	 * Checks to see if the given Parameter set is the same as the current one
	 * 
	 * @author BLM
	 * @param Model_ParameterSet
	 *            pset1, Model_ParameterSet pset2
	 * @return boolean same
	 */
	static public boolean isSame(Model_ParameterSet p1, Model_ParameterSet p2) {
		String[][] thisPars = p1.getParameters();
		String[][] thatPars = p2.getParameters();


		if (thisPars == null || thatPars == null || thisPars.length != 2
				|| thatPars.length != 2)
 {
			// System.out.println("pars null: " + thisPars + "," + thatPars);
			return false;
		}
		if (thisPars[0].length != thatPars[0].length || thisPars[0].length == 0
				|| thatPars[0].length == 0)
 {
			// System.out.println("pars diff len or 0: " + thisPars[0].length
			// + "," + thatPars[0].length);

			return false;
		}
		int len = thisPars[0].length;

		for (int c = 0; c < len; c++) {
			String par = thisPars[0][c].trim();
			String val = thisPars[1][c].trim();
			// find this par in the other pset
			boolean foundItAndMatch = false;
			for (int j = 0; j < len; j++)
				if (par.equalsIgnoreCase(thatPars[0][j].trim())
						&& val.equalsIgnoreCase(
								thatPars[1][j].trim())) {
					foundItAndMatch = true;
					break;
				}

			if (!foundItAndMatch)
 {
				// System.out.println("couldnt find: " + par + "=" + val);
				return false;
			}

		}

		return true;
	}

	/**
	 * Checks to see if the given wells have the same parameter sets, if so,
	 * then it returns a copy of the common parameter set... else null.
	 * 
	 * @author BLM
	 * @param Model_Well
	 *            [] wells
	 * @return Model_ParameterSett commonPset
	 */
	static public Model_ParameterSet doWellsHaveSameParameterSet(
			Model_Well[] wells) {
		Model_ParameterSet pCommon = null;
		if (wells != null && wells.length > 0 && wells[0].getFields() != null
				&& wells[0].getFields().length > 0
				&& wells[0].getFields()[0] != null)
			pCommon = wells[0].getFields()[0].getParameterSet();
		
		if (pCommon == null)
			return null;
		
		int len = wells.length;
		for (int i = 0; i < len; i++) {
			Model_Field[] fields = wells[i].getFields();
			for (int j = 0; j < fields.length; j++) {
				Model_ParameterSet pset = fields[j].getParameterSet();
				if (pset == null)
					return null;
				if (!isSame(pCommon, pset))
					return null;
			}
		}

		return pCommon;
	}

	public String toString() {

		String[][] thisPars = getParameters();

		if (thisPars == null || thisPars.length != 2)
			return "**Null Model_ParameterSet";

		String st = "*******PSET*********";
		int len = thisPars[0].length;
		for (int c = 0; c < len; c++)
			st += "\n  " + thisPars[0][c] + " = " + thisPars[1][c];
		st += "\n********************";
		return st;
	}

	/**
	 * Attempts to read and init a parameter set located at the given relative
	 * path within the given HDF5 file path. NOTE that intra-H5 path points to
	 * the parent group that contains the two String arrays:
	 * "Segmentation_Parameters_Names" and "Segmentation_Parameters_Values".
	 * Returns a new ParameterSet if fails to load.
	 * 
	 * @author BLM
	 * @param String
	 *            pathToH5File, String
	 *            pathToParentDirContainingParamNamesAndValues
	 * */
	public void load(String H5Path, String pathToParentDir) {
		ImageRail_SDCube io = models.Model_Main.getModel()
.getImageRailio();
		io.openHDF5(io.INPUT);
		Hashtable<String, String> hash = io.readParameterSet(H5Path,
				pathToParentDir);
		if (hash != null)
			TheParameters = hash;
		io.closeHDF5();
	}
}
