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

package imagerailio;

import java.awt.Polygon;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import sdcubeio.DataObject;
import sdcubeio.Data_1D;
import sdcubeio.Data_2D;
import sdcubeio.ExpDesign_IO;
import sdcubeio.ExpDesign_Model;
import sdcubeio.ExpDesign_Sample;
import sdcubeio.H5IO;
import sdcubeio.H5IO_Exception;
import sdcubeio.SDCube;
import segmentedobject.Cell;
import segmentedobject.CellCompartment;
import segmentedobject.CellCoordinates;

/**
 * This class is responsible for writing/reading HDF5 SDCubes in an ImageRail
 * project.
 * 
 * @author Bjorn Millard & Michael Menden
 * 
 */
public class ImageRail_SDCube
{
	/**
	 * Maps the plateIndex,wellIndex of each well in the ImageRail project to
	 * the proper Sample path in the HDF5 file
	 * 
	 * EX1: hashtable.put(Key,Value) --> hashtable.put("p2w12",
	 * pathToSDCube+"Children/3"); //where p2w12 means plate:2 and well:12
	 * 
	 * EX2: hastable.get(Key) --> pathToSample = hastable.get("p2w12"); //where:
	 * pathToSampe = pathToSDCube+"Children/3";
	 * */

	public static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
	private Hashtable<String, String> hashtable_indexToPath_in;
	private Hashtable<String, String> hashtable_indexToPath_out;
	private H5IO io;
	private SDCube TheSDCube_in;
	private SDCube TheSDCube_out;
	private String sdcPath_in;
	private String hdfPath_in;
	private String xmlPath_in;
	private String sdcPath_out;
	private String hdfPath_out;
	private String xmlPath_out;
	static public int INPUT = 0;
	static public int OUTPUT = 1;

	/**
	 * Constructs and initializes a ImageRail_SDCube object with the project path.
	 * 
	 * @param projectPath
	 *            Path to the ImageRail project; for example "/path/projectName"
	 * @throws H5IO_Exception
	 */
	public ImageRail_SDCube(String sdcPath_input, String sdcPath_output)
			throws H5IO_Exception
	{
		System.out.println("*** Creating new ImageRail_IO");
		io = new H5IO();
		this.sdcPath_in = sdcPath_input;
		this.sdcPath_out = sdcPath_output;

		this.hdfPath_in = sdcPath_in + "/Data.h5";
		this.hdfPath_out = sdcPath_out + "/Data.h5";

		this.xmlPath_in = sdcPath_in + "/ExpDesign.xml";
		this.xmlPath_out = sdcPath_out + "/ExpDesign.xml";

		TheSDCube_in = new SDCube(sdcPath_in);
		TheSDCube_out = new SDCube(sdcPath_out);

		// // Check to see both input and output projects directories exist
		createProject();
	}

	/**
	 * Create a new project, if it does not already exist.
	 * 
	 * @author Bjorn Millard
	 * @param null
	 * @return void
	 * @throws H5IO_Exception
	 */
	public void createProject() throws H5IO_Exception {

		// Trying to write it out
		try {
			File fin = new File(sdcPath_in);
			File fout = new File(sdcPath_out);

			if (!fin.exists() && sdcPath_in.equalsIgnoreCase(sdcPath_out))
				TheSDCube_in.write();
			else {
				if (!fin.exists())
					TheSDCube_in.write();
				if (!fout.exists())
					TheSDCube_out.write();
			}
		} catch (H5IO_Exception e) {
			System.err.println("ERROR writing SDCube: in: "
					+ TheSDCube_in.getPath() + " , out: "
					+ TheSDCube_out.getPath());
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets the current time/date stamp to for unique sample IDs
	 * 
	 * @author Bjorn Millard
	 * @param void
	 * @return String DateTimestamp
	 */
	static public String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * Initializes the desired field within the currently opened ImageRail HDF5
	 * project file
	 * 
	 * @author Bjorn Millard
	 * @param int plateIndex
	 * @param int wellIndex
	 * @param int fieldIndex
	 * @param int[] fieldDimensions [height, width, numChannels]
	 * @throws H5IO_Exception
	 */
	public void createField(String sampleID, int plateIndex, int wellIndex,
			int fieldIndex,
			int[] fieldDimensions, ExpDesign_Model expDesignModel)
			throws H5IO_Exception {
		// Does Sample for this well exist already?
		String indexKey = "p" + plateIndex + "w" + wellIndex;
		String path = null;

		if (hashtable_indexToPath_out != null)
			path = (String) hashtable_indexToPath_out.get(indexKey);
		String fieldID = indexKey + "f" + fieldIndex;

		// If not, create a new Sample
		if (path == null) {

			// Determine how many Samples are currently in the project so we can
			// create a new Child_X where X=numCurrentSamples+1
			int sampleIndex = io.getGroupChildCount(hdfPath_out, "./Children");

			// create the sample container first
			// String sampleID ="p" + plateIndex + "w" + wellIndex + "_"
			// + getTimeStamp();
			createSample_skeleton(sampleID, sampleIndex, plateIndex, wellIndex,
					true, true, true, false);

			// Creating XML sample information
			if (expDesignModel != null) {
				ExpDesign_Sample expSample = new ExpDesign_Sample(sampleID);
				expDesignModel.addSample(expSample);
			}

			// Now checking for the path
			if (hashtable_indexToPath_out != null)
				path = (String) hashtable_indexToPath_out.get(indexKey);

			// init this field
			String pathToSample = "./Children/" + sampleIndex;
			createField_skeleton(pathToSample, fieldIndex, fieldID,
					fieldDimensions, true, true, true, false);

		}
		// If so, ask does this field group already exist?
		else {
			//Sample exists, but must check for proper sample meta info
			if (!io.existsGroup(hdfPath_out, path + "/Meta/Plate_Well")) 
				{
					int[] in = { plateIndex, wellIndex };
						io.writeDataset(hdfPath_out,
								path + "/Meta/Plate_Well", in);
				}
				if (!io.existsGroup(hdfPath_out, path + "/Meta/Sample_ID")) 
				{
					// Writing Sample_ID
					String[] str = { sampleID };
						io.writeDataset(hdfPath_out,
			 path + "/Meta/Sample_ID", str);
				}
				if (!io.existsGroup(hdfPath_out, path + "/Meta/Samlpe_TYPE")) 
				{
					// Writing Sample_TYPE
					String[] str = {"ImageRail_v1"};
					io
			.writeDataset(hdfPath_out, path
			 + "/Meta/Sample_TYPE", str);
				}

			//Writing the field
			if (!io.existsGroup(hdfPath_out, path + "/Children/"
							+ fieldIndex)) {

				// If not, create field group
				createField_skeleton(path, fieldIndex, fieldID,
						fieldDimensions, false, true, true, false);
			} else
			{
				// If exists, overwrite?
				io.removeDataset(path + "/Children/" + fieldIndex);
				createField_skeleton(path, fieldIndex, fieldID,
						fieldDimensions, false, true, true, false);
			}
		}

		// Hashing this field index/path upon success
		// write the samples plate/well data
		hashtable_indexToPath_out
				.put(fieldID, path + "/Children/" + fieldIndex);
	}

	/**
	 * Creates the basic infrastruture needed for each ImageRail Field
	 * 
	 * @author Bjorn Millard
	 * @param String
	 *            pathToSample
	 * @param int fieldIndex
	 * @param int fieldID
	 * @param int[] fieldDimensions [height, width, numChannels]
	 * @throws H5IO_Exception
	 * */
	public void createField_skeleton(String pathToSample, int fieldIndex,
			String fieldID, int[] fieldDimensions, boolean includeChildren,
			boolean includeData, boolean includeMeta, boolean includeRaw)
			throws H5IO_Exception {

		// Create the Field_X group

		io.createGroup(hdfPath_out, pathToSample + "/Children/"
				+ fieldIndex);
		String fPath = pathToSample + "/Children/" + fieldIndex;
		// init the Metadata group
		if (includeMeta)
			io.createGroup(hdfPath_out, fPath + "/Meta");
		// init the Data group
		if (includeData)
			io.createGroup(hdfPath_out, fPath + "/Data");
		// init the Samples group
		if(includeChildren)
			io.createGroup(hdfPath_out, fPath + "/Children");
		// init the Raw group
		if(includeRaw)
			io.createGroup(hdfPath_out, fPath + "/Raw");

		if(includeMeta)
 {
		// Writing Field Dimensions
			io.writeDataset(hdfPath_out, fPath + "/Meta" + "/"
				+ "Height_Width_Channels",
				fieldDimensions);
	}
		// Writing Field_ID
		String[] fID = { fieldID };
		io.writeDataset(hdfPath_out, fPath + "/Meta" + "/" + "Field_ID", fID);

	}

	/**
	 * Creates the basic infrastruture needed for each ImageRail Sample
	 * 
	 * @author Bjorn Millard
	 * @param int SampleIndex
	 * @param int plateIndex
	 * @param int wellIndex
	 * @throws H5IO_Exception
	 * */
	public void createSample_skeleton(String sampleID, int sampleIndex,
			int plateIndex,
 int wellIndex, boolean includeChildren,
			boolean includeData, boolean includeMeta, boolean includeRaw)
			throws H5IO_Exception {

		io.createGroup(hdfPath_out, "./Children/" + sampleIndex);
		// init the Data group for this sample
		if (includeData)
			io.createGroup(hdfPath_out, "./Children/" + sampleIndex
				+ "/Data");
		// init the Metadata group
		if (includeMeta)
			io.createGroup(hdfPath_out, "./Children/" + sampleIndex
				+ "/Meta");
		// init the Raw group
		if (includeRaw)
			io.createGroup(hdfPath_out, "./Children/" + sampleIndex + "/Raw");
		// init the Samples group
		if (includeChildren)
			io.createGroup(hdfPath_out, "./Children/" + sampleIndex
						+ "/Children");


		// write the samples plate/well data
		String indexKey = "p" + plateIndex + "w" + wellIndex;

		if (includeMeta) {
		int[] in = { plateIndex, wellIndex };
			io.writeDataset(hdfPath_out,
 "./Children/" + sampleIndex + "/Meta"
				+ "/" + "Plate_Well", in);
		// Writing Sample_ID

		String[] str = { sampleID };
			io.writeDataset(hdfPath_out,
 "./Children/" + sampleIndex + "/Meta"
				+ "/" + "Sample_ID", str);
		// Writing Sample_TYPE
		str[0] = "ImageRail_v1";
		io
.writeDataset(hdfPath_out, "./Children/" + sampleIndex
 + "/Meta"
				+ "/" + "Sample_TYPE", str);
		}
		
		// Hashing this well index/path upon success
		hashtable_indexToPath_out.put(indexKey, "./Children/"
					+ sampleIndex);

	}

	/**
	 * Convenience method to go from int plate/well indices to the String key
	 * needed for hashtable EX: getIndexKey(plateInx, wellInx) --> "p2w23" if
	 * plateindex==2 and wellindex==23
	 * 
	 * @author Bjorn Millard
	 * @param int plateIndex
	 * @param int wellIndex
	 * @return String hashKeyForThisPlate/Wellcombo
	 */
	public String getIndexKey(int plateIndex, int wellIndex) {
		return "p" + plateIndex + "w" + wellIndex;
	}

	/**
	 * Returns the hashtable for the input project that links the sample index
	 * file path to plate/well indices
	 * 
	 * @author Bjorn Millard
	 * @param void
	 * @return Hashtable<String,String> plateWellKey-->HDF5samplePath hashtable
	 */
	public Hashtable<String, String> getHashtable_in()
	{
		return hashtable_indexToPath_in;
	}

	/**
	 * Returns the hashtable for the output project that links the sample index
	 * file path to plate/well indices
	 * 
	 * @author Bjorn Millard
	 * @param void
	 * @return Hashtable<String,String> plateWellKey-->HDF5samplePath hashtable
	 */
	public Hashtable<String, String> getHashtable_out() {
		return hashtable_indexToPath_out;
	}

	/**
	 * Writes the arbitrary file to the designated field
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param File
	 *            fileToEncodeInHDF5
	 * @author Bjorn Millard
	 */
	public void writeRawDataToField(String sampleID, int plateIndex,
			int wellIndex,
			int fieldIndex, String datasetName, File raw) {

		// Convert File to Bytestream
		byte[] arr = H5IO.toByteArray(raw);

		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIndex,
				wellIndex));
		if (pathToSample == null)
 {
			int[] dims = { 1024, 1024, 3 };
			try {
				createField(sampleID, plateIndex, wellIndex, fieldIndex, dims,
						models.Model_Main.getModel().getExpDesignConnector());
			} catch (H5IO_Exception e) {
				e.printStackTrace();
			}
		}
		pathToSample = hashtable_indexToPath_out.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample == null) {
			System.out.println("Path still NULL!!!");
			return;
		}

		 try {
			io.writeDataset(hdfPath_out, pathToSample + "/Children/"
					+ fieldIndex + "/" + datasetName, arr);
			Byte[] ar = (Byte[]) ((Data_1D) io.readArr(hdfPath_out,
					pathToSample
					+ "/Children/" + fieldIndex + "/" + datasetName))
					.getData();
			byte[] arrout = new byte[ar.length];
			System.out.println("ArrLength: " + ar.length);
			for (int i = 0; i < ar.length; i++)
				arrout[i] = ar[i];

			try {
				FileOutputStream out = new FileOutputStream(new File(
						"/Users/blm13/Desktop/TestTIFF.tif"));
				out.write(arrout);
				out.flush();
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}


		} catch (H5IO_Exception e) {
			e.printStackTrace();
		}



	}


	/**
	 * Write Single-cell features to a field.
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param float[][] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public synchronized void writeFeatures(int plateIdx, int wellIdx, int fieldIdx, float[][] data)  {

		String indexKey = getIndexKey(plateIdx, wellIdx);
		String pathToSample = hashtable_indexToPath_out.get(indexKey);

		// Adding this field to the gui hashtable
		String indexKeyField = indexKey + "f" + fieldIdx;
		String pathToField = pathToSample + "/Children/" + fieldIdx;
		hashtable_indexToPath_out.put(indexKeyField, pathToField);

		String pathToDS = null;
		if (pathToSample != null) {
			try {
				
			String datasetName = "feature_values";
				String pathToFieldDataFolder = pathToSample
 + "/Children/"
						+ fieldIdx + "/Data";
				pathToDS = pathToFieldDataFolder + "/feature_values";

				if (io.existsDataset(pathToDS))
					io.removeDataset(pathToDS);
			
				Float[][] dataF = new Float[data.length][data[0].length];
				for (int r = 0; r < data.length; r++) {
					for (int c = 0; c < data[0].length; c++) {
						dataF[r][c] = data[r][c];
					}
				}
				Data_2D data2 = new Data_2D(dataF, "FLOAT", datasetName);
				io.writeDataset(hdfPath_out, pathToDS,
						datasetName,
						data2);

				// Add dimension names
				io.writeAttribute(pathToDS, "dim0", "cells");
				io.writeAttribute(pathToDS, "dim1", "feature_values");

		} 	catch (H5IO_Exception e) {
			System.out.println("**Failed to write feature values to: "+pathToDS);
			e.printStackTrace();
		}
		
		}
	}

	/**
	 * Read features of a specific field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return float[][] Returns the cell features. The first dimension are the
	 *         cells; the second dimension are the features.
	 * @throws H5IO_Exception
	 */
	public synchronized float[][] readFeatures(int plateIdx, int wellIdx,
			int fieldIdx)
	{

		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		String path = null;
		

		// System.out.println("loading features0: "+pathToSample+
		// " p"+plateIdx+"w"+wellIdx+"f"+fieldIdx);
		if (pathToSample != null) {
			
			try {
				
				path = pathToSample + "/Children/" + fieldIdx
					+ "/Data/feature_values";

				// System.out.println("Loading Features for:");
				// System.out.println(hdfPath+"/"+path);
			
				Data_2D<Float> values = (Data_2D<Float>) io.readDataset(
						hdfPath_in, path);
				if (values == null)
					return null;

			Float[][] vals = values.getData();
			int len = vals.length;
			int len2 = vals[0].length;
			float[][] out = new float[len][len2];
			for (int i = 0; i < len; i++)
				for (int j = 0; j < len2; j++)
					out[i][j] = vals[i][j].floatValue();

				// System.out.println("Successfully loaded features!");
			
			return out;
			
			} catch (H5IO_Exception e) {
				System.out.println("**Error: Faild to load feature values for path: "+path);
				e.printStackTrace();
			}
			
		} 
			
		return null;
	}

	/**
	 * Write feature names to a field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            int * [] names The names of the features.
	 * @throws H5IO_Exception
	 */
	public synchronized void writeFeatureNames(int plateIdx, int wellIdx,
			int fieldIdx, String[] names) throws H5IO_Exception
	{
		
		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {
			openHDF5(OUTPUT);

			String pathToFieldMetaFolder = pathToSample + "/Children/"
					+ fieldIdx + "/Meta/";

			// Add dimension names
			//Remove prior feature name array if already exists
			String pathToDS = hdfPath_out + pathToFieldMetaFolder
					+ "feature_names";
			if(io.existsDataset(pathToDS));
				io.removeDataset(pathToDS);
			// write feature names
			io.writeDataset(hdfPath_out, pathToFieldMetaFolder + "/"
					+ "feature_names",
					names);
			closeHDF5();

		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}


	/**
	 * Read feature names of a field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return StringBuffer[] Returns the names of the the features.
	 * @throws H5IO_Exception
	 */
	public synchronized StringBuffer[] readFeatureNames(int plateIdx,
			int wellIdx, int fieldIdx)
	{
		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		StringBuffer[] names = null;
		if (pathToSample != null) {
			String path = null;
			try {
				path = pathToSample + "/Children/" + fieldIdx
						+ "/Meta/feature_names";

				names = io.readDataset_String(hdfPath_in, path);
				return names;
			} catch (H5IO_Exception e) {
				System.out.println("**Failed to load feature names for: "+path);
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Write mean values of a well.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int meanValues The mean values of the features.
	 * @throws H5IO_Exception
	 */
	public synchronized void writeWellMeans(int plateIdx, int wellIdx,
			float[] meanValues) 
	{
		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));

		if (pathToSample != null) {
			try{

			String datasetName = "well_means";
				String pathToDataFolder = pathToSample + "/Data";
			//check if already exists, delete if so to overwrite
				String path = hdfPath_out + pathToDataFolder + "/"
						+ datasetName;
			if(io.existsDataset(path))
				io.removeDataset(path);
			
				io.writeDataset(hdfPath_out, pathToDataFolder + "/"
						+ datasetName,
						meanValues);
		
			// Add dimension names
				String pathToDS = pathToDataFolder + "/" + datasetName;
				io.writeAttribute(pathToDS, "dim0", "feature");

			} catch (Exception e) {

				System.out.println("**Failed writing Well Mean values");
//				e.printStackTrace();
//			}
		}
		}
	}

	/**
	 * Write standard deviation values of a well.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int stdValues The standard deviation values of the features.
	 * @throws H5IO_Exception
	 */
	public void writeWellStdDevs(int plateIdx, int wellIdx, float[] stdValues)
	{

		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {
			try{

			String datasetName = "well_stdevs";
				String pathToDataFolder = pathToSample + "/Data";
			//check if already exists, delete if so to overwrite
				String path = hdfPath_out + pathToDataFolder + "/"
						+ datasetName;
			if(io.existsDataset(path))
				io.removeDataset(path);
			
				io.writeDataset(hdfPath_out, pathToDataFolder + "/"
						+ datasetName,
					stdValues);
			// Add dimension names
				String pathToDS = pathToDataFolder + "/" + datasetName;
				io.writeAttribute(pathToDS, "dim0", "feature");

		} catch (H5IO_Exception e) {

				System.out.println("**Failed writing Well Stdev values");
		}
		}
	}

	/**
	 * Read mean values of a well.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @return float[] Returns the mean values of the features.
	 * @throws H5IO_Exception
	 */
	public synchronized float[] readWellMeans(int plateIdx, int wellIdx)	
	{
		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		
		if (pathToSample != null) {
			try {
			String path = pathToSample + "/Data/well_means";

			float[] out = null;
			try{
				Data_1D<Float> values = (Data_1D<Float>) io.readDataset(
						hdfPath_in, path);
				if (values == null)
					return null;
				Float[] vals = (Float[]) values.getData();
				int len = vals.length;
				out = new float[len];
				for (int i = 0; i < len; i++)
					out[i] = vals[i].floatValue();
			}
			catch(Exception e) //TODO --> this can be deleted once everyone converts to new project format
			{
				Data_2D<Float> values = (Data_2D<Float>) io.readDataset(
						hdfPath_in, path);
				if (values == null)
					return null;
				Float[][] vals = (Float[][]) values.getData();
				int len = vals[0].length;
				out = new float[len];
				for (int i = 0; i < len; i++)
					out[i] = vals[0][i].floatValue();
			}
			if(out==null)	
				System.out.println("***Error reading well means");
			
			
			return out;
			
			} catch (H5IO_Exception e) {
				System.out.println("***Error*** Problems loading well means!!!");
				e.printStackTrace();
			}
		} 
		return null;
	}

	/**
	 * Read standard deviation values of a well.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @return float[] Returns the standard deviation values of the features.
	 * @throws H5IO_Exception
	 */
	public synchronized float[] readWellStdevs(int plateIdx, int wellIdx)
			throws H5IO_Exception
	{
		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {
			try {
				
			String path = pathToSample + "/Data/well_stdevs";


			float[] out = null;
			try{
				// write feature names
				Data_1D<Float> values = (Data_1D<Float>) io.readDataset(
						hdfPath_in,
					path);
				if (values == null)
					return null;
				Float[] vals = (Float[]) values.getData();
				int len = vals.length;
				out = new float[len];
				for (int i = 0; i < len; i++)
					out[i] = vals[i].floatValue();
			}
			catch(Exception e) //TODO --> this can be deleted once everyone converts to new project format
			{
				Data_2D<Float> values = (Data_2D<Float>) io.readDataset(
						hdfPath_in, path);
				if (values == null)
					return null;
				Float[][] vals = (Float[][]) values.getData();
				int len = vals[0].length;
				out = new float[len];
				for (int i = 0; i < len; i++)
					out[i] = vals[0][i].floatValue();
			}
			if(out==null)	
				System.out.println("***Error reading well stdevs");

			return out;

		} catch (H5IO_Exception e) {
			System.out.println("***Error*** Problems loading well stdevs!!!");
			e.printStackTrace();
		}
		
		} 
		return null;
	}
	

	/**
	 * Returns the height of the images in the desired field
	 * 
	 * @author Bjorn Millard
	 * @param int plateIndex
	 * @param int wellIndex
	 * @param int fieldIndex
	 * @return int FieldHeight
	 * */
	public int getFieldHeight(int plateIdx, int wellIdx, int fieldIdx,
			int fromInputOrOutput)
			throws H5IO_Exception {
		String h5path = null;
		String pathToSample = null;
		if (fromInputOrOutput == INPUT) {
			pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
			h5path = hdfPath_in;
		} else {
			pathToSample = hashtable_indexToPath_out.get(getIndexKey(
					plateIdx, wellIdx));
			h5path = hdfPath_out;
		}
		if (pathToSample != null) {

			String path = pathToSample + "/Children/" + fieldIdx;
			int val = -1;
			try{
				Integer[] ints = ((Data_1D<Integer>) io.readDataset(h5path,
					path + "/Meta/Height_Width_Channels")).getData();
				val = ints[0].intValue();
			}
			catch(Exception e)
			{
				Integer[][] ints = ((Data_2D<Integer>) io.readDataset(h5path,
						path + "/Meta/Height_Width_Channels")).getData();
					val = ints[0][0].intValue();
			}

			return val;
		}
		return -1;
	}
	
	/**
	 * Returns the dimensions as int[height, width, numChannels] of the desired
	 * field
	 * 
	 * @author Bjorn Millard
	 * @param int plateIndex
	 * @param int wellIndex
	 * @param int fieldIndex
	 * @return int[] fieldDimensions-->height,width,numChannels
	 * 
	 * */
	public int[] readFieldDimensions(int plateIdx, int wellIdx, int fieldIdx,
			int readFromInputOrOutput)
			throws H5IO_Exception {
		String pathToSample = null;
		String h5path = null;
		if (readFromInputOrOutput == INPUT)
 {
			pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
			h5path = hdfPath_in;
		}
		else
 {
			pathToSample = hashtable_indexToPath_out.get(getIndexKey(plateIdx,
					wellIdx));
			h5path = hdfPath_out;
		}

		if (pathToSample != null) {

			String path = pathToSample + "/Children/" + fieldIdx;
			Integer[] ints = ((Data_1D<Integer>) io.readDataset(hdfPath_in,
					path + "/Meta/Height_Width_Channels")).getData();

			int[] dims = {ints[0],ints[1],ints[2]};
			return dims;
		}
		return null;
	}



	/**
	 * Write whole cells to the field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param int cellList List of cell coordinates.
	 * @throws H5IO_Exception
	 */
	public synchronized void writeWholeCells(int plateIdx, int wellIdx,
			int fieldIdx, ArrayList<CellCoordinates> cellList)
			throws H5IO_Exception
	{

		long time = System.currentTimeMillis();
		// System.out.println("Starting writing whole cell coords:");

		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String pathMeta = pathToSample + "/Children/" + fieldIdx
					+ "/Meta";
			String pathData = "";
			// String pathData = pathToSample + "/Children/" + fieldIdx +
			// "/Data";
			String pathChild = pathToSample + "/Children/" + fieldIdx
					+ "/Children";

			// Get the compartment names.
			ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
			for (int i = 0; i < cellList.size(); i++) {
				ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
				for (int j = 0; j < cellList.get(i).getComSize(); j++) {
					tmpComNames.add(new StringBuffer(cellList.get(i)
							.getCompartment(j).getName()));
				}
				if (tmpComNames.size() > comNames.size()) {
					comNames = tmpComNames;
				}
			}

			// Iterating through all cells and writing compartment coordinate
			// data
			if (cellList.size() > 0 && comNames.size() > 0) {
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx,
						OUTPUT);

				if (cellList.size() > 0 && comNames.size() > 0) {

					// cell loop
					for (int i = 0; i < cellList.size(); i++) {
						// create cell folder
						pathData = pathChild + "/" + i + "/Data";
						io.createGroup(hdfPath_out, pathData);

						// compartment loop
						int numCom = cellList.get(i).getComSize();
						for (int j = 0; j < numCom; j++) {
							Point[] pt = cellList.get(i).getCompartment(j)
									.getCoordinates();
							if (pt.length > 0) {
								int[] data = new int[pt.length];
								for (int k = 0; k < pt.length; k++)
									data[k] = IdxConverter.point2index(pt[k],
											fieldHeight);

								// writing compartments into cell/data folder to
								// improve performance rather than
								// creating an entirely new tier
								String compartmentName = cellList.get(i).getCompartment(j).getName();
								String dsPath = pathData + "/coords_"
										+ compartmentName;


								io.writeDataset(hdfPath_out, dsPath, data);
								io.writeAttribute(dsPath, "dim0",
										"index");

							}
						}
						// Write compartment names.
						io.writeStringDataset(pathMeta + "/compartment_names",
								(StringBuffer[]) comNames
										.toArray(new StringBuffer[0]));

					}

					// Write cell count
					int[] cellCount = new int[] { cellList.size() };
					io.writeDataset(hdfPath_out, pathMeta + "/cell_count",
							cellCount);

					// io.createDataset(pathMeta + "/cell_count",
					// "Integer", new long[] { 1 });
					// io.writeAttribute(pathMeta + "/cell_count",
					// "dataType", "H5T_NATIVE_INT");
					// DataObject cellCount = new Data_1D<Integer>(
					// new Integer[] { cellList.size() }, Data_1D.INTEGER,
					// "cell_count");
					// io.writeArray(pathMeta + "/cell_count",
					// cellCount, 0, new long[] { 0 });
				}

				// System.out.println("Done writing whole cells: "
				// + ((System.currentTimeMillis() - time) / 1000f));

			}
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}

	/**
	 * Read whole cells of a field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return ArrayList<CellCoordinates> Returns the cell coordinates of this
	 *         field.
	 * @throws H5IO_Exception
	 */
	public synchronized ArrayList<CellCoordinates> readWholeCells(int plateIdx,
			int wellIdx, int fieldIdx) throws H5IO_Exception
	{
		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathMeta = pathToSample + "/Children/" + fieldIdx
					+ "/Meta";
			String pathData = "";// pathToSample + "/Children/" + fieldIdx +
									// "/Data";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx, INPUT);

			// Add dimension names
			if (io.existsDataset(pathMeta + "/compartment_names")
					&& io.existsDataset(pathMeta + "/cell_count")) {

				cellList = new ArrayList<CellCoordinates>();
				// Get cell count
				DataObject dataArray = io.readDataset(hdfPath_in, pathMeta
						+ "/cell_count");

				Integer[] cellCount = (Integer[]) ((Data_1D) dataArray)
						.getData();
				// Read compartment names

				StringBuffer[] comNames = io.readDataset_String(hdfPath_in,
						pathMeta + "/compartment_names");
				
				// cell loop
				for (int i = 0; i < cellCount[0]; i++) {
					ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
					// compartment loop
					int numCom = comNames.length;
					for (int j = 0; j < numCom; j++) {
						String comPath = pathToSample + "/Children/" + fieldIdx
								+ "/Children/" + i + "/Data/coords_"
								+ comNames[j].toString().trim();
						if (io.existsDataset(comPath)) {
							// dim0 = index
							long[] counts = io.getDimensions(comPath);
							DataObject data = io.readDataset(hdfPath_in,
									comPath);
							Integer[] idx = (Integer[]) ((Data_1D) data)
									.getData();

							// point loop
							ArrayList<Point> coordinates = new ArrayList<Point>();
							for (int k = 0; k < idx.length; k++) {
								coordinates.add(IdxConverter.index2point(
										idx[k], fieldHeight));
							}
							comArray.add(new CellCompartment(coordinates,
									comNames[j].toString().trim()));
						}
					}
					CellCoordinates cell = new CellCoordinates(comArray);
					cellList.add(cell);

				}
			}

		}

		return cellList;


	}

	/**
	 * Read bonding boxes of the cells of a field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return ArrayList<CellCoordinates> Returns the cell coordinates of this
	 *         field.
	 * @throws H5IO_Exception
	 */
	public synchronized ArrayList<CellCoordinates> readCellBoundingBoxes(
			int plateIdx, int wellIdx, int fieldIdx) throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathToDS = pathToSample + "/Children/" + fieldIdx
					+ "/Data/coords_bounding_boxes";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx, INPUT);

			try {
			// Add dimension names
			
				cellList = new ArrayList<CellCoordinates>();
				Integer[][] vals = (Integer[][]) ((Data_2D) io.readDataset(
						hdfPath_in,
					pathToDS)).getData();

				int numC = vals.length;
				for (int i = 0; i < numC; i++) {
					ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
						ArrayList<Point> coordinates = new ArrayList<Point>();
					// adding first point
					coordinates.add(IdxConverter.index2point(vals[i][0],
									fieldHeight));
					// adding second point
					coordinates.add(IdxConverter.index2point(vals[i][1],
							fieldHeight));
						comArray.add(new CellCompartment(coordinates,
								"BoundingBox"));
					CellCoordinates cell = new CellCoordinates(comArray);
					cellList.add(cell);
				}
			}
 catch (Exception e) {
				return null;
			}
		}

		return cellList;
	}

	/**
	 * Read centroids of the cells of a field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return ArrayList<CellCoordinates> Returns the cell coordinates of this
	 *         field.
	 * @throws H5IO_Exception
	 */
	public synchronized ArrayList<CellCoordinates> readCellCentroids(
			int plateIdx, int wellIdx, int fieldIdx)
 throws H5IO_Exception
	{

		String pathToSample = hashtable_indexToPath_in.get(getIndexKey(
				plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathToDS = pathToSample + "/Children/" + fieldIdx
					+ "/Data/coords_centroids";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx, INPUT);

			// Add dimension names
			if (io.existsDataset(pathToDS))
 {
			cellList = new ArrayList<CellCoordinates>();
				Data_1D data1d = io.readArr(hdfPath_in, pathToDS);
				Integer[] idx = (Integer[]) (data1d).getData();
				for (int k = 0; k < idx.length; k++) {
					ArrayList<Point> coordinates = new ArrayList<Point>();
					coordinates.add(IdxConverter.index2point(idx[k],
							fieldHeight));
					ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
					comArray.add(new CellCompartment(coordinates, "Centroid"));
					CellCoordinates cell = new CellCoordinates(comArray);
					cellList.add(cell);
				}
		}

		}

		return cellList;

	}

	/**
	 * Write centroids of the cells to the field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param ArrayList
	 *            <CellCoorinates> cellList List of cell coordinates.
	 * @return void
	 * @throws H5IO_Exception
	 */
	public synchronized void writeCellCentroids(int plateIdx, int wellIdx,
			int fieldIdx, ArrayList<CellCoordinates> cellList)
			throws H5IO_Exception
	{

		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String pathData = pathToSample + "/Children/" + fieldIdx
					+ "/Data";
			String pathMeta = pathToSample + "/Children/" + fieldIdx
					+ "/Meta";

			// Get the compartment names.
			ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
			for (int i = 0; i < cellList.size(); i++) {
				ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
				for (int j = 0; j < cellList.get(i).getComSize(); j++) {
					tmpComNames.add(new StringBuffer(cellList.get(i)
							.getCompartment(j).getName()));
				}
				if (tmpComNames.size() > comNames.size()) {
					comNames = tmpComNames;
				}
			}

			if (cellList.size() > 0 && comNames.size() > 0) {
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx,
						OUTPUT);

				// dim0 = cells; dim1 = compartments; dim2 = index
				long[] maxDims = { cellList.size() };// , comNames.size(), 1 };
				io.createDataset(pathData + "/coords_centroids", "Integer",
						maxDims);

				// cell loop
				for (int i = 0; i < cellList.size(); i++) {
					// compartment loop
					// for (int j = 0; j < cellList.get(i).getComSize(); j++) {
					Point[] pt = cellList.get(i).getCompartment(0)
								.getCoordinates();
						Integer[] data = new Integer[] { -1 };

						for (int k = 0; k < pt.length; k++) {
							data[k] = IdxConverter.point2index(pt[k],
									fieldHeight);
						}
						DataObject dataArray = new Data_1D<Integer>(data,
							Data_1D.INTEGER, "coords_centroids");
					io.writeArray(pathData + "/coords_centroids", dataArray, 0,
							new long[] { i });
					// }
				}
				// Add dimension names.
				io.writeAttribute(pathData + "/coords_centroids", "dataType",
						"H5T_NATIVE_INT");
				io.writeAttribute(pathData + "/coords_centroids", "dim0",
						"cells");
				// io.writeAttribute(path + "/cell_centroids", "dim1",
				// "compartments");
				// io.writeAttribute(path + "/cell_centroids", "dim2", "index");
				// Write compartment names.
				io.writeStringDataset(pathMeta
 + "/compartment_names",
						(StringBuffer[]) comNames.toArray(new StringBuffer[0]));

			}
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");

		
	}

	/**
	 * Write bounding boxes of the cells to the field.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param ArrayList
	 *            <CellCoordinates> cellList List of cell coordinates.
	 * @throws H5IO_Exception
	 */
	public synchronized void writeCellBoundingBoxes(int plateIdx, int wellIdx,
			int fieldIdx, ArrayList<CellCoordinates> cellList)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String pathData = pathToSample + "/Children/" + fieldIdx
					+ "/Data";
			String pathMeta = pathToSample + "/Children/" + fieldIdx
			+ "/Meta";
			
			// Get the compartment names.
			ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
			for (int i = 0; i < cellList.size(); i++) {
				ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
				for (int j = 0; j < cellList.get(i).getComSize(); j++) {
					tmpComNames.add(new StringBuffer(cellList.get(i)
							.getCompartment(j).getName()));
				}
				if (tmpComNames.size() > comNames.size()) {
					comNames = tmpComNames;
				}
			}

			if (cellList.size() > 0 && comNames.size() > 0) {
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx,
						OUTPUT);

				// dim0 = cells; dim1 = compartments; dim2 = index
				long[] maxDims = { cellList.size(), 2 };
				io.createDataset(pathData + "/coords_bounding_boxes",
						"Integer",
						maxDims);

				// cell loop
				for (int i = 0; i < cellList.size(); i++) {

					Point[] pt = cellList.get(i).getCompartment(0)
							.getCoordinates();
						Integer[] data = new Integer[] { -1, -1 };

						for (int k = 0; k < pt.length; k++) {
							data[k] = IdxConverter.point2index(pt[k],
									fieldHeight);
						}
						DataObject dataArray = new Data_1D<Integer>(data,
							Data_1D.INTEGER, "coords_bounding_boxes");
					io.writeArray(pathData + "/coords_bounding_boxes",
							dataArray,
 1,
							new long[] { i, 0 });
				}

				// Add dimension names.
				io.writeAttribute(pathData + "/coords_bounding_boxes",
						"dataType",
						"H5T_NATIVE_INT");
				io.writeAttribute(pathData + "/coords_bounding_boxes", "dim0",
						"cells");
				io.writeAttribute(pathData + "/coords_bounding_boxes", "dim1",
						"index");
				// Write compartment names.
				io.writeStringDataset(pathMeta
 + "/compartment_names",
						(StringBuffer[]) comNames.toArray(new StringBuffer[0]));

			}
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");

	}

	/**
	 * Reads the coords and values for the given field and returns complete
	 * cells that contain both their coordinates and values
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return Returns the cell coordinates of this field.
	 * @throws H5IO_Exception
	 */
	public synchronized ArrayList<Cell> readCells(int plateIdx, int wellIdx,
			int fieldIdx)
			throws H5IO_Exception {

		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		float[][] vals = readFeatures(plateIdx, wellIdx, fieldIdx);
		ArrayList<CellCoordinates> coords = readCoordinates(plateIdx, wellIdx,
				fieldIdx);
		
		int len = vals.length;
		for (int i = 0; i < len; i++) 
			cells.add(new Cell(i, coords.get(i), vals[i]));
		

		return cells;
	}

	/**
	 * Read cells from HDF project. Note that it first checks what sort of
	 * coordinates are being stored, either: (1) Centroids (2) BoundingBoxes or
	 * (3) Multiple compartments. Note that BoundingBoxes and Centroids can be
	 * written/read/stored very efficiently in a single dataset whereas the more
	 * complex cellCoordinate structures require the use of separate HDF groups
	 * for each cell.
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @return ArrayList<CellCoordinates> Returns the cell coordinates of this
	 *         field.
	 * @throws H5IO_Exception
	 */
	public synchronized ArrayList<CellCoordinates> readCoordinates(
			int plateIdx, int wellIdx, int fieldIdx)
	{
		// System.out.println("Loading coordinates");
		// Parameters to write: plateIdx, wellIdx, fieldIdx
		ArrayList<CellCoordinates> cellList;
		try {
			
		cellList = readCellCentroids(plateIdx,
					wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readCellBoundingBoxes(plateIdx, wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readWholeCells( plateIdx, wellIdx, fieldIdx);
		
			if (cellList == null || cellList.size() == 0)
			System.out.println("**Failed to Loaded Coordinates for: p"+plateIdx +"w"+wellIdx+"f"+fieldIdx);
			// else
			// System.out.println("Successfully Loaded Coordinates for: p"+plateIdx
			// +"w"+wellIdx+"f"+fieldIdx);
		return cellList ;
		
		} catch (H5IO_Exception e) {
			System.out.println("**Failed loading coordinates for: p"+plateIdx +"w"+wellIdx+"f"+fieldIdx);
			e.printStackTrace();
		}
	return null;
	}

	/**
	 * Converts the default Float[][] object values to float primitives
	 * 
	 * @author Bjorn Millard
	 * @param Float
	 *            [][] The Float matrix which should be converted.
	 * @return float[][] int Returns the primitive float matrix.
	 */
	static public float[][] convertTofloatMatrix(Float[][] data) {
		float[][] fVals = new float[data.length][data[0].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				fVals[i][j] = data[i][j].floatValue();
		data = null;
		return fVals;
	}


	public void openHDF5(int inputOrOutput) {
		try {
			if (inputOrOutput == OUTPUT)
				io.openHDF5(hdfPath_out);
			else if (inputOrOutput == INPUT)
				io.openHDF5(hdfPath_in);
		} catch (H5IO_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeHDF5() {
		try {
			io.closeHDF5();
		} catch (H5IO_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Writes meta-info about the parent plate (Size)
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @throws H5IO_Exception
	 */
	public void writeParentPlateInfo(int plateIdx, int wellIdx, int plateSize) throws H5IO_Exception
	{
		String pathToSample = hashtable_indexToPath_out.get(getIndexKey(
				plateIdx,
				wellIdx));
		if (pathToSample != null) {
			String path = pathToSample + "/Meta";
			int[] in = { plateSize };
			//remove prior dataset
			if (io.existsDataset(path + "/" + "Plate_Size"))
				io.removeDataset(path + "/" + "Plate_Size");
			io.writeDataset(hdfPath_out, path + "/" + "Plate_Size", in);
		}
		
	}

	/**
	 * Writes plate name to HDF file
	 * 
	 * @author Bjorn Millard
	 * @param String
	 *            [] names
	 * */
	public void writePlateNames(String[] names) {
		// remove prior dataset

		String path = "./Meta/PlateNames";
		try {
			if (io.existsDataset(path))
				io.removeDataset(path);
		} catch (H5IO_Exception e1) {
			e1.printStackTrace();
		}
		try {
			io.writeDataset(hdfPath_out, path, names);
		} catch (H5IO_Exception e) {
			System.err.println("** ERROR writing PlateCount and PlateSizes");
		}
	}

	/**
	 * Reads plate name from HDF file
	 * 
	 * @author Bjorn Millard
	 * @param String
	 *            [] names
	 * */
	public StringBuffer[] readPlateNames() {
		// remove prior dataset
		String path = "./Meta/PlateNames";
		StringBuffer[] names = null;
		try {
			names = io.readDataset_String(hdfPath_in, path);
			if (names == null)
				return null;
			return names;
		} catch (H5IO_Exception e) {
			System.out.println("**Failed to load Plate names for: " + path);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Writes meta-info about the Plate count and sizes for this imagerail
	 * project
	 * 
	 * @author Bjorn Millard
	 * @param int plateCount
	 * @param int plateSizes
	 * */
	public void writePlateCountAndSizes(int plateCount, int plateSizes, int inputOrOutput)
	{
		String h5path = null;
		if (inputOrOutput == INPUT) {
			h5path = hdfPath_in;
			openHDF5(INPUT);
		} else if (inputOrOutput == OUTPUT) {
			h5path = hdfPath_out;
			openHDF5(OUTPUT);
		}
		
			//remove prior dataset
		int[] in = { plateCount, plateSizes };
		String path = "./Meta/PlateCount_PlateSize";
		try {
			if (io.existsDataset(path))
				io.removeDataset(path );
		} catch (H5IO_Exception e1) {
			e1.printStackTrace();
		}
		try {
			io.writeDataset(h5path, path, in);
		} catch (H5IO_Exception e) {
			System.err.println("** ERROR writing PlateCount and PlateSizes");
		}

	}

	/**
	 * Returns an int[] where arr.length == number of plates and the elements
	 * are the number of wells found for each plate
	 * 
	 * @author Bjorn Millard
	 * @param null
	 * @return ArrayList<int[]> numberOfWellsInEachPlate
	 * */
	public ArrayList<int[]> getPlateSizes(int inputOrOutput)
	{
		String h5path = null;
		if (inputOrOutput == INPUT) {
			h5path = hdfPath_in;
			openHDF5(INPUT);
		} else if (inputOrOutput == OUTPUT) {
			h5path = hdfPath_out;
			openHDF5(OUTPUT);
		}

		//looking at all samples to determine 1) how many plates there are based on sample's plateIDs, and then what the sizes of those plates are
		ArrayList<int[]> idsAndSize = new ArrayList<int[]>();
		// Searching all HDF5 samples
		try {
			
			int plateCount = -1;
			int plateSize = -1;
			try{
				
				Data_1D dat = (Data_1D) io.readDataset(h5path,
						"./Meta/PlateCount_PlateSize");
				plateCount = ((Integer[]) (dat.getData()))[0].intValue();
				plateSize = ((Integer[]) (dat.getData()))[1].intValue();
			}
			catch(Exception e) //TODO --> this can be deleted once everyone converts to new project format
			{
				Data_2D dat = (Data_2D) io.readDataset(h5path,
						"./Meta/PlateCount_PlateSize");
				plateCount = ((Integer[][]) (dat.getData()))[0][0].intValue();
				plateSize = ((Integer[][]) (dat.getData()))[1][0].intValue();
			}
			if(plateCount==-1 || plateSize==-1)	
				System.out.println("***Error reading PlateSize and PlateCount: "+plateCount+","+plateSize);

				
			for (int i = 0; i < plateCount; i++) {
				int[] newPlate = { i, plateSize };
				idsAndSize.add(newPlate);
			}
		} catch (H5IO_Exception e) {
			e.printStackTrace();

		}
		closeHDF5();

	return idsAndSize;
	}
	
	/**
	 * Initializes a new SampleIndex-->PlateWellKey hashtable. NOTE: this
	 * hashtable is used to speed up well data for the GUI so we dont have to
	 * search through each sample each time we need data from a single well
	 * 
	 * @author Bjorn Millard
	 * @param null
	 * @return void
	 * */
	public void initHDF5ioSampleHash()
	{
		System.out.println("Initializing the IO_Hashtables...");
		this.hashtable_indexToPath_in = new Hashtable<String, String>();
		this.hashtable_indexToPath_out = new Hashtable<String, String>();

		// INPUT HDF5 IO - iterate through all samples in this project if they
		// currently exist and hash what plate/wells they corresponds to
		try {
			openHDF5(INPUT);
			int numSamples = 0;
			String[] gNames = io.getGroupChildNames(hdfPath_in, "./Children");
			if (gNames != null)
				numSamples = gNames.length;
			System.out.println("___Found " + numSamples
					+ " Fields in this project___");
			for (int i = 0; i < numSamples; i++) {
				int plateInx = -1;
				int wellInx = -1;
				try{
					Data_1D dat = (Data_1D) io.readDataset(hdfPath_in,
						"./Children/" + gNames[i] + "/Meta/Plate_Well");
						 plateInx = ((Integer[]) (dat.getData()))[0].intValue();
						 wellInx = ((Integer[]) (dat.getData()))[1].intValue();
				}
				catch(Exception e) //TODO --> this can be deleted once everyone converts to new project format
				{
					Data_2D dat = (Data_2D) io.readDataset(hdfPath_in,
							"./Children/" + gNames[i] + "/Meta/Plate_Well");
						 plateInx = ((Integer[][]) (dat.getData()))[0][0].intValue();
						 wellInx = ((Integer[][]) (dat.getData()))[1][0].intValue();
				}

				if(wellInx == -1 || plateInx == -1)
					System.out.println("***ERROR loading plateInx or wellInx = "+plateInx +","+wellInx);
				
				String indexKey = "p" + plateInx + "w" + wellInx;				
				String pathToSample = "./Children/" + gNames[i];
				hashtable_indexToPath_in.put(indexKey, pathToSample);

				// Indexing each field if it has single cell data in it
				// If sample exists, seeing if data for this specific field
				// exists

				// Get number of fields in this sample
				int numFields = io.getGroupChildCount(hdfPath_in, pathToSample
						+ "/Children");
				boolean exists = false;
				for (int j = 0; j < numFields; j++) {
					// seeing if this field has a "feature_values" dataset
					// String path = pathToSample + "/Children/" + j
					// + "/Data/feature_values";

					// seeing if this field has a "feature_values" dataset
					String path = pathToSample + "/Children/" + j
							+ "/Meta/Height_Width_Channels";
					try {
						exists = io.existsDataset(path);
					} catch (H5IO_Exception e) {
						e.printStackTrace();
					}
					if (exists) {
						String indexKeyField = indexKey + "f" + j;
						String pathToField = pathToSample + "/Children/" + j;
						hashtable_indexToPath_in
								.put(indexKeyField, pathToField);
					}

				}

			}
			closeHDF5();

		} catch (H5IO_Exception e) {
			e.printStackTrace();
		}

		if (hdfPath_in.equalsIgnoreCase(hdfPath_out)) {
			// Input and output projects are the same
			hashtable_indexToPath_out = hashtable_indexToPath_in;
		} else // We are writing to a directory different than the input
				// directory
		{
		// OUTPUT HDF5 IO - iterate through all samples in this project if they
		// currently exist and hash what plate/wells they corresponds to
		try {
				openHDF5(OUTPUT);
				int numSamples = 0;
				String[] gNames = io.getGroupChildNames(hdfPath_out,
						"./Children");
				if (gNames != null)
					numSamples = gNames.length;

				System.out.println("___Found " + numSamples
						+ " Fields in this project___");
				for (int i = 0; i < numSamples; i++) {
					Data_1D dat = (Data_1D) io.readDataset(hdfPath_out,
							"./Children/" + gNames[i] + "/Meta/Plate_Well");
					int plateInx = ((Integer[]) (dat.getData()))[0].intValue();
					int wellInx = ((Integer[]) (dat.getData()))[1].intValue();
				String indexKey = "p"+plateInx+"w"+wellInx;
					String pathToSample = "./Children/" + gNames[i];
				hashtable_indexToPath_out.put(indexKey, pathToSample);

				// Indexing each field if it has single cell data in it
				// If sample exists, seeing if data for this specific field
				// exists

				// Get number of fields in this sample
				int numFields = io.getGroupChildCount(hdfPath_out, pathToSample
						+ "/Children");
				boolean exists = false;
				for (int j = 0; j < numFields; j++) {
					// seeing if this field has a "feature_values" dataset
					String path = pathToSample + "/Children/" + j
							+ "/Meta/Height_Width_Channels";
					try {
						exists = io.existsDataset(path);
					} catch (H5IO_Exception e) {
						e.printStackTrace();
					}
					if (exists) {
						String indexKeyField = indexKey + "f" + j;
						String pathToField = pathToSample + "/Children/"
								+ j;
						hashtable_indexToPath_out.put(indexKeyField,
								pathToField);
					}

				}

			}
		} catch (H5IO_Exception e) {
			e.printStackTrace();

		}
			closeHDF5();

		}

		System.out.println("...Successfuly indexed "
				+ hashtable_indexToPath_in.size()
				+ " Samples in the Input H5 file");
		System.out.println("...Successfuly indexed "
				+ hashtable_indexToPath_out.size()
				+ " Samples in the Output H5 file");
		System.out.println("________________");

	}

	/**
	 * Checks that the project being loaded has the same Features as the version
	 * of imagerail being used to open it.
	 * 
	 * @author Bjorn Millard
	 * @param null
	 * @return void
	 * */
	public StringBuffer[] validateFeaturesUsedInProject(String[] featureNames)
	{
		System.out.println("Validating Features...");
		try {
			int numSamples = io.getGroupChildCount(hdfPath_in, "./Children");
			for (int i = 0; i < numSamples; i++) {

				String pathToSample = "./Children/" + i;

				// Indexing each field if it has single cell data in it
				// If sample exists, seeing if data for this specific field
				// exists

				// Get number of fields in this sample
				int numFields = io.getGroupChildCount(hdfPath_in, pathToSample
						+ "/Children");
				for (int j = 0; j < numFields; j++) {
					
					// seeing if this field has a "feature_values" dataset
					String path = pathToSample + "/Children/" + j
							+ "/Meta/feature_names";
					try {
						StringBuffer[] names = (StringBuffer[]) io
								.readDataset_String(hdfPath_in, path);
						int len = featureNames.length;
						
						if (names == null || len != names.length)
							return names;
						
						for (int p = 0; p < len; p++) {
							// System.out
							// .println(featureNames[p] + "," + names[p]);
							if (!((names[p] + "").trim()
									.equalsIgnoreCase(featureNames[p].trim())))
								return names;
						}
						
					} catch (H5IO_Exception e) {
						e.printStackTrace();
						return null;
					}
				
					
					
				}

			}
		} catch (H5IO_Exception e) {
			e.printStackTrace();

		}
		System.out.println("...Successfuly validated features ");
		System.out.println("________________");

		return null;
	}

	/**
	 * Returns the H5IO object used to connect ImageRail to the HDF5 file
	 * 
	 * @author Bjorn Millard
	 * @throws H5IO
	 */
	public H5IO getH5IO() {
		return io;
	}

	/**
	 * Read SampleID if sample already exists in HDF.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @return String SampleID
	 * @throws H5IO_Exception
	 */
	public String readSampleID_fromHDF5(int plateIdx, int wellIdx) {

		if (hashtable_indexToPath_in == null)
			return null;

		String pathToSample = (String) hashtable_indexToPath_in.get("p"
				+ plateIdx
				+ "w" + wellIdx);

			StringBuffer[] st = null;
			if (pathToSample != null) {
			String path = null;
						StringBuffer[] st2 = null;
							String path2 = null;
							try {
								path2 = pathToSample + "/Meta/Sample_ID";
				st2 = io.readDataset_String(hdfPath_in, path2);
								if (st2 == null)
									return null;
								String id = st2[0].toString();
								return id;
							} catch (H5IO_Exception e) {
								System.out
										.println("**Failed to load Sample_ID for: "
												+ path);
								e.printStackTrace();
							}
			}

		return null;

	}

	/**
	 * Read SampleID for given plate/well ID if sample exists in XML.
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @return String SampleID
	 * @throws H5IO_Exception
	 */
	public String readSampleID_fromXML(int plateIdx, int wellIdx) {

		ArrayList<ExpDesign_Sample> samples = ExpDesign_IO
				.parseSamples(xmlPath_in);

		int len = samples.size();
		for (int i = 0; i < len; i++) {
			String id = samples.get(i).getId().trim();

			int w_start = id.indexOf("w") + 1;
			int w_end = id.trim().indexOf("_t");
			int p_start = 1;
			int p_end = w_start - 1;
			//

			// parsing plate from id
			int pI = Integer.parseInt(id.substring(p_start, p_end));
			// parsing well from id
			int wI = Integer.parseInt(id.substring(w_start, w_end));
			if (pI == plateIdx && wI == wellIdx)
				return id;
		}
		return null;
	}

	/**
	 * Writes the 2D points of a polygon ROI to the metdata group of the given
	 * field path
	 * 
	 * @author Bjorn Millard
	 * @param String
	 *            pathToDestinationField
	 * @param Polygon
	 *            roi
	 * @param int ID of ROI
	 * */
	public void writeROI(String pathToField, Polygon roi, int ID)
	{
		int[][] in = new int[roi.npoints][2];
		for (int i = 0; i < roi.npoints; i++) {
			in[i][0] = roi.xpoints[i];
			in[i][1] = roi.ypoints[i];
		}
		try {
			String path = pathToField + "/Meta/roi_" + ID;
			System.out.println(path);
			boolean boo = io.existsDataset(path);
			if (!boo) {
				io.writeDataset(hdfPath_out, path, in);
			} else
				System.out
						.println("ERROR: could not write ROI to HDF5 since dataset already exists at: "
								+ path);
		} catch (H5IO_Exception e) {
			System.out.println("ERROR writing ROI to HDF5 file");
			e.printStackTrace();
		}

	}

	/**
	 * Reads ROIs from the HDF5 file and returns and ArrayLIst<Polygon>. Note
	 * all ROIs are stored as polygons even if they were originally Rectangles
	 * or Ellipses
	 * 
	 * @author Bjorn Millard
	 * */
	public ArrayList<Polygon> readROIs(String h5path, String fieldPath) {
		ArrayList<Polygon> rois = new ArrayList<Polygon>();

		try {	
			String metaPath = fieldPath + "/Meta";
			String[] names = io.getGroupChildNames(h5path, metaPath);
			int len = names.length;
			for (int i = 0; i < len; i++) {
				if(names[i].indexOf("roi_")>=0)
				{
					String roiPath = metaPath+"/"+names[i];
					Data_2D<Integer> values = (Data_2D<Integer>) io
							.readDataset(hdfPath_in, roiPath);
					if (values != null)
					{
						Integer[][] vals = values.getData();
						int numP = vals.length;
						int[] xpoints = new int[numP];
						int[] ypoints = new int[numP];

						for (int j = 0; j < numP; j++) {
							xpoints[j] = (int) vals[j][0].floatValue();
							ypoints[j] = (int) vals[j][1].floatValue();
						}
						Polygon poly = new Polygon(xpoints, ypoints, numP);
						rois.add(poly);
					}
				}

			}

		} catch (H5IO_Exception e) {
			System.out.println("ERROR writing ROI to HDF5 file");
			e.printStackTrace();
			closeHDF5();
		}
		return rois;
	}

	/**
	 * Removes the 2D points of a polygon ROI to the metdata group of the given
	 * field path and ROI index
	 * 
	 * @author Bjorn Millard
	 * @param String
	 *            pathToDestinationField
	 * @param int ID of ROI
	 * */
	public void removeROI(String pathToField, int roi_ID) {

		try {
			String path = pathToField + "/Meta/roi_" + roi_ID;
			boolean boo = io.existsDataset(path);
			if (boo)
				io.removeDataset(path);
		} catch (H5IO_Exception e) {
			System.out.println("ERROR writing ROI to HDF5 file");
			e.printStackTrace();
		}

	}

	/**
	 * Attempts to read and init a parameter set located at the given relative
	 * path within the given HDF5 file path. NOTE that intra-H5 path points to
	 * the parent group that contains the two String arrays:
	 * "Segmentation_Parameters_Names" and "Segmentation_Parameters_Values".
	 * 
	 * @author BLM
	 * @param String
	 *            pathToH5File, String
	 *            pathToParentDirContainingParamNamesAndValues
	 * @return Hashtable<String, String> theParametersReferenceTable
	 * */
	public Hashtable<String, String> readParameterSet(String hdfPath,
			String pathToParentDir) {

		if (hdfPath != null && pathToParentDir != null) {
			StringBuffer[] st1 = null;
			StringBuffer[] st2 = null;
			String path1 = null;
			String path2 = null;
			try {
				path1 = pathToParentDir + "/Segmentation_Parameters_Names";
				path2 = pathToParentDir + "/Segmentation_Parameters_Values";
				st1 = io.readDataset_String(hdfPath, path1);
				st2 = io.readDataset_String(hdfPath, path2);
				if (st1 == null || st2 == null)
					return null;
				if (st1.length != st2.length)
					return null;
				Hashtable<String, String> hash = new Hashtable<String, String>();
				int len = st1.length;
				for (int i = 0; i < len; i++)
					hash.put(st1[i].toString().trim(), st2[i].toString().trim());
				return hash;

			} catch (H5IO_Exception e) {
				System.out.println("**Failed to load Model_ParameterSet for: "
						+ pathToParentDir);
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Merges the SDCubes of the given file paths. The second path is the
	 * destination path. Note that all paths refer to the parent ".sdc"
	 * directory path.
	 * 
	 * @author BLM
	 * @param String
	 *            [] pathsIn
	 * @param String
	 *            mergePathOut
	 * */
	static public void mergeSDCubes(String[] pathsIn, String pathOut) {
		int numSDCubes = pathsIn.length;
		ArrayList<File> files = new ArrayList<File>();
		System.out.println("***Merging SDCubes:");
		for (int i = 0; i < numSDCubes; i++) {
			File f = new File(pathsIn[i]);
			if (f.getName().indexOf(".sdc") >= 0)
 {
				System.out.println(f.getAbsolutePath());
				files.add(f);
			}
			else
				System.out.println("**Path: " + pathsIn[i]
						+ " not an SDCube and will be ignored");
		}
		System.out.println("to ----->");
		System.out.println(pathOut);
		System.out.println("********");
		
		for (int i = 0; i < pathsIn.length; i++) {

			long time = System.currentTimeMillis();
			System.out.println("**Merging: " + pathsIn[i]);
			try {

				SDCube sdc = new SDCube(pathsIn[i]);
				sdc.load();
				sdc.write(pathOut);

			} catch (H5IO_Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("dT: " + (System.currentTimeMillis() - time));
		}

		System.out.println("**DONE WITH MERGE**");
	}
}
