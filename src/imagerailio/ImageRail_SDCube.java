package imagerailio;

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
	public static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";
	private String sdcPath;
	private String hdfPath;
	private String xmlPath;

	/**
	 * Maps the plateIndex,wellIndex of each well in the ImageRail project to
	 * the proper Sample path in the HDF5 file
	 * 
	 * EX1: hashtable.put(Key,Value) --> hashtable.put("p2w12",
	 * pathToSDCube+"/Child_3"); //where p2w12 means plate:2 and well:12
	 * 
	 * EX2: hastable.get(Key) --> pathToSample = hastable.get("p2w12"); //where:
	 * pathToSampe = pathToSDCube+"/Child_3";
	 * */
	private Hashtable<String, String> hashtable_indexToPath;
	private H5IO io;
	private SDCube TheSDCube;

	/**
	 * Constructs and initializes a ImageRail_SDCube object with the project path.
	 * 
	 * @param projectPath
	 *            Path to the ImageRail project; for example "/path/projectName"
	 * @throws H5IO_Exception
	 */
	public ImageRail_SDCube(String sdcPath) throws H5IO_Exception
	{
		System.out.println("*** Creating new ImageRail_IO");
		io = new H5IO();
		this.sdcPath = sdcPath;
		this.hdfPath = sdcPath + "/Data.h5";
		this.xmlPath = sdcPath + "/ExpDesign.xml";
		TheSDCube = new SDCube(sdcPath);
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
			TheSDCube.write();
		} catch (H5IO_Exception e) {
			System.err.println("ERROR writing SDCube: " + TheSDCube.getPath());
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
		String path = (String) hashtable_indexToPath.get(indexKey);
		String fieldID = indexKey + "f" + fieldIndex;
		

		// If not, create a new Sample
		if (path == null) {
			io.openHDF5(hdfPath);

			// Determine how many Samples are currently in the project so we can
			// create a new Child_X where X=numCurrentSamples+1
			int sampleIndex = io.getGroupChildCount(hdfPath, "./Children");

			// create the sample container first
			// String sampleID ="p" + plateIndex + "w" + wellIndex + "_"
			// + getTimeStamp();
			createSample_skeleton(sampleID, sampleIndex, plateIndex, wellIndex);

			// Creating XML sample information
			ExpDesign_Sample expSample = new ExpDesign_Sample(sampleID);
			expDesignModel.addSample(expSample);

			// init this field
			String pathToSample = "./Children/Child_" + sampleIndex;
			createField_skeleton(pathToSample, fieldIndex, fieldID,
					fieldDimensions);


			// Close the HDF5 file to prevent multiple process access
			io.closeHDF5();

		}
		// If so, ask does this field group already exist?
		else {
			io.openHDF5(hdfPath);
			if (!io
					.existsGroup(hdfPath, path + "/Children/Child_"
							+ fieldIndex)) {

				// If not, create field group
				createField_skeleton(path, fieldIndex, fieldID, fieldDimensions);
			} else
			{
				// If exists, overwrite?
				io.removeDataset(path + "/Children/Child_" + fieldIndex);
				createField_skeleton(path, fieldIndex, fieldID, fieldDimensions);
			}
			io.closeHDF5();
		}
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
			String fieldID, int[] fieldDimensions)
			throws H5IO_Exception {
		// Create the Field_X group
		io.createGroup(hdfPath, pathToSample + "/Children/Child_"
				+ fieldIndex);
		String fPath = pathToSample + "/Children/" + "Child_" + fieldIndex;
		// init the Metadata group
		io.createGroup(hdfPath, fPath + "/Meta");
		// init the Data group
		io.createGroup(hdfPath, fPath + "/Data");
		// init the Samples group
		io.createGroup(hdfPath, fPath + "/Children");
		// init the Raw group
		io.createGroup(hdfPath, fPath + "/Raw");

		// Writing Field Dimensions
		io.writeDataset(hdfPath, fPath + "/Meta" + "/"
				+ "Height_Width_Channels",
				fieldDimensions);
		// Writing Field_ID
		String[] fID = { fieldID };
		io.writeDataset(hdfPath, fPath + "/Meta" + "/" + "Field_ID", fID);
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
			int wellIndex) throws H5IO_Exception {

		io.createGroup(hdfPath, "./Children/Child_" + sampleIndex);
		// init the Data group for this sample
		io.createGroup(hdfPath, "./Children/Child_" + sampleIndex
				+ "/Data");
		// init the Metadata group
		io.createGroup(hdfPath, "./Children/Child_" + sampleIndex
				+ "/Meta");
		// init the Raw group
		io.createGroup(hdfPath, "./Children/Child_" + sampleIndex + "/Raw");
		// init the Samples group
		io
				.createGroup(hdfPath, "./Children/Child_" + sampleIndex
						+ "/Children");

		// write the samples plate/well data
		int[] in = { plateIndex, wellIndex };
		io.writeDataset(hdfPath,
 "./Children/Child_" + sampleIndex + "/Meta"
				+ "/" + "Plate_Well", in);
		// Writing Sample_ID
		String indexKey = "p" + plateIndex + "w" + wellIndex;
		String[] str = { sampleID };
		io.writeDataset(hdfPath,
 "./Children/Child_" + sampleIndex + "/Meta"
				+ "/" + "Sample_ID", str);
		// Writing Sample_TYPE
		str[0] = "ImageRail_v1";
		io
.writeDataset(hdfPath, "./Children/Child_" + sampleIndex
 + "/Meta"
				+ "/" + "Sample_TYPE", str);
		
		// Hashing this well index/path upon success
		hashtable_indexToPath.put(indexKey, "./Children/Child_"
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
	 * Returns the hashtable that links the sample index file path to plate/well
	 * indices
	 * 
	 * @author Bjorn Millard
	 * @param void
	 * @return Hashtable<String,String> plateWellKey-->HDF5samplePath hashtable
	 */
	public Hashtable<String, String>getHashtable()
	{
		return hashtable_indexToPath;
	}

	/**
	 * Writes the given data matrix to the desired field within the ImageRail
	 * HDF5 file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param float[][] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, float[][] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName,datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample == null)
 {
			int[] dims = { 1024, 1024, 3 };
			try {
				createField(sampleID, plateIndex, wellIndex, fieldIndex, dims,
						gui.MainGUI.getGUI().getExpDesignConnector());
			} catch (H5IO_Exception e) {
				e.printStackTrace();
			}
		}
		pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample == null) {
			System.out.println("Path still NULL!!!");
			return;
		}

		 try {
			io.writeDataset(hdfPath, pathToSample + "/Children/Child_"
					+ fieldIndex + "/" + datasetName, arr);
			Byte[] ar = (Byte[]) ((Data_1D)io.readArr(hdfPath, pathToSample
					+ "/Children/Child_" + fieldIndex + "/" + datasetName))
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
	 * Writes the given dataset to the desired field within the ImageRail HDF5
	 * file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param float[] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, float[] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}

	/**
	 * Writes the given dataset to the desired field within the ImageRail HDF5
	 * file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param int[][] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, int[][] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName, datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}

	/**
	 * Writes the given dataset to the desired field within the ImageRail HDF5
	 * file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param double[] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, int[] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}

	/**
	 * Writes the given dataset to the desired field within the ImageRail HDF5
	 * file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            datasetName
	 * @param double[][] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, double[][] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName,datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
	}

	/**
	 * Writes the given dataset to the desired field within the ImageRail HDF5
	 * file
	 * 
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param String
	 *            DestinationDatasetName
	 * @param double[] dataToWrite
	 * @author Bjorn Millard
	 * @throws H5IO_Exception
	 */
	public void writeDatasetToField(int plateIndex, int wellIndex,
			int fieldIndex, String datasetName, double[] data)
			throws H5IO_Exception {

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIndex,
				wellIndex));
		if (pathToSample != null) {
			String pathToFieldDataFolder = pathToSample + "/Children/Child_"
					+ fieldIndex + "/Data/";
			io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
					+ datasetName,
					data);
		} else
			System.out.println("***Error*** Sample/Field does not exist!!!");
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
		String pathToSample = hashtable_indexToPath.get(indexKey);

		// Adding this field to the gui hashtable
		String indexKeyField = indexKey + "f" + fieldIdx;
		String pathToField = pathToSample + "/Children/Child_" + fieldIdx;
		hashtable_indexToPath.put(indexKeyField, pathToField);

		String pathToDS = null;
		if (pathToSample != null) {
			try {
				
			String datasetName = "feature_values";
				String pathToFieldDataFolder = pathToSample
						+ "/Children/Child_"
					+ fieldIdx + "/Data/";
			 pathToDS = pathToFieldDataFolder + "feature_values";

				io.closeHDF5();
				io.openHDF5(hdfPath);
				if (io.existsDataset(pathToDS))
					io.removeDataset(pathToDS);
			
				Float[][] dataF = new Float[data.length][data[0].length];
				for (int r = 0; r < data.length; r++) {
					for (int c = 0; c < data[0].length; c++) {
						dataF[r][c] = data[r][c];
					}
				}
				Data_2D data2 = new Data_2D(dataF, "FLOAT", datasetName);
				// io.writeDataset(hdfPath, pathToFieldDataFolder + "/"
				// + datasetName, datasetName, dataF);

				io.writeDataset(hdfPath, pathToFieldDataFolder, datasetName,
						data2);

			// Add dimension names
			io.openHDF5(hdfPath);
			
				io.writeAttribute(pathToDS, "dim0", "cells");
	
			io.writeAttribute(pathToDS, "dim1", "feature_values");
			io.closeHDF5();
			io.closeAll();

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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		String path = null;
		
		System.out.println("loading features0: "+pathToSample+ " p"+plateIdx+"w"+wellIdx+"f"+fieldIdx);
		if (pathToSample != null) {
			
			try {
				
				path = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data/feature_values";

			System.out.println("Loading Features for:");
			System.out.println(hdfPath+"/"+path);
			
			io.openHDF5(hdfPath);
			Data_2D<Float> values = (Data_2D<Float>) io.readDataset(hdfPath,path);
				if (values == null)
					return null;

			Float[][] vals = values.getData();
			int len = vals.length;
			int len2 = vals[0].length;
			float[][] out = new float[len][len2];
			for (int i = 0; i < len; i++)
				for (int j = 0; j < len2; j++)
					out[i][j] = vals[i][j].floatValue();

			io.closeHDF5();
			System.out.println("Successfully loaded features!");
			
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
		
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String pathToFieldMetaFolder = pathToSample + "/Children/Child_"
					+ fieldIdx + "/Meta/";

			// Add dimension names
			io.openHDF5(hdfPath);
			//Remove prior feature name array if already exists
			String pathToDS = hdfPath + pathToFieldMetaFolder + "feature_names";
			if(io.existsDataset(pathToDS));
				io.removeDataset(pathToDS);
			// write feature names
			io.writeDataset(hdfPath, pathToFieldMetaFolder + "/"
					+ "feature_names",
					names);
			io.closeHDF5();

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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		StringBuffer[] names = null;
		if (pathToSample != null) {
			String path = null;
			try {
				path = pathToSample + "/Children/Child_" + fieldIdx
						+ "/Meta/feature_names";

				names = io.readDataset_String(hdfPath, path);
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));

		if (pathToSample != null) {
			try{

			String datasetName = "well_means";
			String pathToDataFolder = pathToSample + "/Data/";
			//check if already exists, delete if so to overwrite
			String path = hdfPath+pathToDataFolder+datasetName;
			if(io.existsDataset(path))
				io.removeDataset(path);
			
				io.writeDataset(hdfPath, pathToDataFolder + "/"
						+ datasetName,
						meanValues);
		
			// Add dimension names
			io.openHDF5(hdfPath);
			String pathToDS = pathToDataFolder + datasetName;
			io.writeAttribute(pathToDS, "dim0", "well");
			io.writeAttribute(pathToDS, "dim1", "feature");
			io.closeHDF5();

			} catch (H5IO_Exception e) {
				try {
				io.closeHDF5();
				io.closeAll();
				}
				catch (H5IO_Exception e2) {	
				e2.printStackTrace();}
				System.out.println("**Failed writing Well Mean values");
				e.printStackTrace();
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {
			try{
			String datasetName = "well_stdevs";
			String pathToDataFolder = pathToSample + "/Data/";
			//check if already exists, delete if so to overwrite
			String path = hdfPath+pathToDataFolder+datasetName;
			if(io.existsDataset(path))
				io.removeDataset(path);
			
				io.writeDataset(hdfPath, pathToDataFolder + "/"
						+ datasetName,
					stdValues);
			// Add dimension names
			io.openHDF5(hdfPath);
			String pathToDS = pathToDataFolder + datasetName;
			io.writeAttribute(pathToDS, "dim0", "well");
			io.writeAttribute(pathToDS, "dim1", "feature");
			io.closeHDF5();

		} catch (H5IO_Exception e) {
			try {
			io.closeHDF5();
			io.closeAll();
			}
			catch (H5IO_Exception e2) {	
			e2.printStackTrace();}
			System.out.println("**Failed writing Well Stdev values");
			e.printStackTrace();
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		
		if (pathToSample != null) {
			try {
			String path = pathToSample + "/Data/well_means";

			// Add dimension names
			io.openHDF5(hdfPath);
			// write feature names
			Data_2D<Float> values;
			
				values = (Data_2D<Float>) io.readDataset(hdfPath, path);
			// (hdfPath, pathToFieldDataFolder,
			// "feature_names", names);
				if (values == null)
					return null;
			Float[][] vals = values.getData();
			int len = vals.length;
			float[] out = new float[len];
			for (int i = 0; i < len; i++)
				out[i] = vals[i][0].floatValue();

			io.closeHDF5();
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {
			try {
				
			String path = pathToSample + "/Data/well_stdevs";

			// Add dimension names
			io.openHDF5(hdfPath);
			// write feature names
			Data_2D<Float> values = (Data_2D<Float>) io.readDataset(hdfPath,
					path);// (hdfPath, pathToFieldDataFolder,
			// "feature_names", names);
				if (values == null)
					return null;
			Float[][] vals = values.getData();
			int len = vals.length;
			float[] out = new float[len];
			for (int i = 0; i < len; i++)
				out[i] = vals[i][0].floatValue();

			io.closeHDF5();
			return out;

		} catch (H5IO_Exception e) {
			System.out.println("***Error*** Problems loading well means!!!");
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
	public int getFieldHeight(int plateIdx, int wellIdx, int fieldIdx)
			throws H5IO_Exception {
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			io.openHDF5(hdfPath);

			String path = pathToSample + "/Children/Child_" + fieldIdx;
			Integer[][] ints = ((Data_2D<Integer>) io.readDataset(hdfPath,
					path + "/Meta/Height_Width_Channels")).getData();

			io.closeHDF5();
			return ints[0][0].intValue();
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
	public int[] getFieldDimensions(int plateIdx, int wellIdx, int fieldIdx)
			throws H5IO_Exception {
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			io.openHDF5(hdfPath);

			String path = pathToSample + "/Children/Child_" + fieldIdx;
			Integer[][] ints = ((Data_2D<Integer>) io.readDataset(hdfPath,
					path + "/Meta/Height_Width_Channels")).getData();

			io.closeHDF5();
			int[] dims = { ints[0][0], ints[1][0], ints[2][0] };
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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String path = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data";

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
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

				if (cellList.size() > 0 && comNames.size() > 0) {
					io.createGroup(hdfPath, path + "/cell_coordinates");
					// cell loop
					for (int i = 0; i < cellList.size(); i++) {
						io.createGroup(hdfPath, path
								+ "/cell_coordinates/cell_" + i);
						io.openHDF5(hdfPath);
						String cellPath = path + "/cell_coordinates/cell_" + i;
						// compartment loop
						for (int j = 0; j < cellList.get(i).getComSize(); j++) {
							Point[] pt = cellList.get(i).getCompartment(j)
									.getCoordinates();
							if (pt.length > 0) {
								Integer[] data = new Integer[pt.length];
								for (int k = 0; k < pt.length; k++)
									data[k] = IdxConverter.point2index(pt[k],
											fieldHeight);

								DataObject dataArray = new Data_1D<Integer>(
										data, Data_1D.INTEGER, cellList.get(i)
												.getCompartment(j)
										.getName());
								String compartmentDS = cellPath
										+ "/"
										+ cellList.get(i).getCompartment(j)
												.getName();
								// dim0 = index
								io.createDataset(compartmentDS, "Integer",
										new long[] { data.length });
								// dim0, offset)
								io.writeArray(compartmentDS, dataArray, 0,
										new long[] { 0 });
								// Add dimension names.
								io.writeAttribute(compartmentDS, "dataType",
										"H5T_NATIVE_INT");
								io.writeAttribute(compartmentDS, "dim0",
										"index");
							}
						}
					}
					// Write compartment names.
					io.writeStringDataset(
path
							+ "/cell_coordinates/compartment_names",
							(StringBuffer[]) comNames
									.toArray(new StringBuffer[0]));

					// Write cell count
					io.createDataset(path + "/cell_coordinates/cell_count",
							"Integer", new long[] { 1 });
					io.writeAttribute(path + "/cell_coordinates/cell_count",
							"dataType", "H5T_NATIVE_INT");
					DataObject cellCount = new Data_1D<Integer>(
							new Integer[] { cellList.size() }, Data_1D.INTEGER,
							"cell_count");
					io.writeArray(path + "/cell_coordinates/cell_count",
							cellCount, 0, new long[] { 0 });
				}

				io.closeHDF5();
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathToCoordsFolder = pathToSample + "/Children/Child_"
					+ fieldIdx
					+ "/Data/cell_coordinates";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

			// Add dimension names
			io.openHDF5(hdfPath);
			if (io.existsDataset(pathToCoordsFolder + "/compartment_names")
					&& io.existsDataset(pathToCoordsFolder + "/cell_count")) {

				cellList = new ArrayList<CellCoordinates>();
				// Get cell count
				// DataObject dataArray = null;
				DataObject dataArray = io.readArray(pathToCoordsFolder
						+ "/cell_count", 0, 0, 1);
				Integer[] cellCount = (Integer[]) ((Data_1D)dataArray).getData();
				// Read compartment names
				io.closeHDF5();

				StringBuffer[] comNames = io.readDataset_String(hdfPath,
						pathToCoordsFolder + "/compartment_names");

				io.openHDF5(hdfPath);

				// cell loop
				for (int i = 0; i < cellCount[0]; i++) {
					ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
					// compartment loop
					for (int j = 0; j < comNames.length; j++) {
						String comName = pathToCoordsFolder + "/cell_" + i
								+ "/"
								+ comNames[j].toString().trim();
						if (io.existsDataset(comName)) {
							// dim0 = index
							long[] counts = io.getDimensions(comName);
							DataObject data = io.readArray(comName, 0, 0,
									counts[0]);
							Integer[] idx = (Integer[]) ((Data_1D)data).getData();
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
			io.closeHDF5();
			io.closeAll();
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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathToDS = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data/cell_bounding_boxes";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

			try {
			// Add dimension names
			io.openHDF5(hdfPath);
			if (io.existsDataset(pathToDS)) 
				io.closeHDF5();
			
				cellList = new ArrayList<CellCoordinates>();
			Integer[][] vals = (Integer[][]) ((Data_2D) io.readDataset(hdfPath,
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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		ArrayList<CellCoordinates> cellList = null;
		if (pathToSample != null) {

			String pathToDS = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data/cell_centroids";
			int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

			// Add dimension names
			io.openHDF5(hdfPath);
			if (io.existsDataset(pathToDS))
 {
			cellList = new ArrayList<CellCoordinates>();
				Data_1D data1d = io.readArr(hdfPath, pathToDS);
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
			io.closeHDF5();

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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String pathData = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data";
			String pathMeta = pathToSample + "/Children/Child_" + fieldIdx
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
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

				io.openHDF5(hdfPath);

				// dim0 = cells; dim1 = compartments; dim2 = index
				long[] maxDims = { cellList.size() };// , comNames.size(), 1 };
				io.createDataset(pathData + "/cell_centroids", "Integer", maxDims);

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
								Data_1D.INTEGER, "cell_centroids");
						io.writeArray(pathData + "/cell_centroids", dataArray, 0,
							new long[] { i });
					// }
				}
				// Add dimension names.
				io.writeAttribute(pathData + "/cell_centroids", "dataType",
						"H5T_NATIVE_INT");
				io.writeAttribute(pathData + "/cell_centroids", "dim0", "cells");
				// io.writeAttribute(path + "/cell_centroids", "dim1",
				// "compartments");
				// io.writeAttribute(path + "/cell_centroids", "dim2", "index");
				// Write compartment names.
				io.writeStringDataset(pathMeta
						+ "/compartment_names_cell_centroids",
						(StringBuffer[]) comNames.toArray(new StringBuffer[0]));

				io.closeHDF5();
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

		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {

			String path = pathToSample + "/Children/Child_" + fieldIdx
					+ "/Data";

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
				int fieldHeight = getFieldHeight(plateIdx, wellIdx, fieldIdx);

				io.openHDF5(hdfPath);

				// dim0 = cells; dim1 = compartments; dim2 = index
				long[] maxDims = { cellList.size(), 2 };
				io.createDataset(path + "/cell_bounding_boxes", "Integer",
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
								Data_1D.INTEGER, "cell_bounding_boxes");
						io.writeArray(path + "/cell_bounding_boxes", dataArray,
 1,
							new long[] { i, 0 });
				}
				// Add dimension names.
				io.writeAttribute(path + "/cell_bounding_boxes", "dataType",
						"H5T_NATIVE_INT");
				io.writeAttribute(path + "/cell_bounding_boxes", "dim0",
						"cells");
				io.writeAttribute(path + "/cell_bounding_boxes", "dim1",
						"index");
				// Write compartment names.
				io.writeStringDataset(path
						+ "/compartment_names_bounding_boxes",
						(StringBuffer[]) comNames.toArray(new StringBuffer[0]));

				io.closeHDF5();
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
		System.out.println("Loading coordinates");
		// Parameters to write: plateIdx, wellIdx, fieldIdx
		ArrayList<CellCoordinates> cellList;
		try {
			
		cellList = readCellCentroids(plateIdx,
					wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readCellBoundingBoxes(plateIdx, wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readWholeCells( plateIdx, wellIdx, fieldIdx);
		
		if(cellList!=null && cellList.size()>0)
			System.out.println("Successfully Loaded Coordinates for: p"+plateIdx +"w"+wellIdx+"f"+fieldIdx);
		else
			System.out.println("**Failed to Loaded Coordinates for: p"+plateIdx +"w"+wellIdx+"f"+fieldIdx);
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

	/**
	 * Writes the segmentaiton parameters used by imagerail to segment the given
	 * well
	 * 
	 * @author Bjorn Millard
	 * @param int plateIdx Index of the plate.
	 * @param int wellIdx Index of the well.
	 * @param int fieldIdx Index of the field.
	 * @param int nuclearThreshold
	 * @param int cytoplasmicThreshold
	 * @param int backgroundThresholod
	 * @throws H5IO_Exception
	 */
	public void writeSegmentationParameters(int plateIdx, int wellIdx, int nucThreshold, int cytThreshold, int bkgdThreshold) throws H5IO_Exception
	{
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {
			String path = pathToSample + "/Meta";
			// remove prior dataset
			if (io.existsDataset(path + "/" + "Segmentation_Parameters"))
				io.removeDataset(path + "/" + "Segmentation_Parameters");
			int[] in = {nucThreshold, cytThreshold, bkgdThreshold};
			io.writeDataset(hdfPath, path + "/" + "Segmentation_Parameters",
					in);
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
		String pathToSample = hashtable_indexToPath.get(getIndexKey(plateIdx,
				wellIdx));
		if (pathToSample != null) {
			String path = pathToSample + "/Meta";
			int[] in = { plateSize };
			//remove prior dataset
			if (io.existsDataset(path + "/" + "Plate_Size"))
				io.removeDataset(path + "/" + "Plate_Size");
			io.writeDataset(hdfPath, path + "/" + "Plate_Size", in);
		}
		
	}

	/**
	 * Writes meta-info about the Plate count and sizes for this imagerail
	 * project
	 * 
	 * @author Bjorn Millard
	 * @param int plateCount
	 * @param int plateSizes
	 * */
	public void writePlateCountAndSizes(int plateCount, int plateSizes)
	{
			//remove prior dataset
		int[] in = { plateCount, plateSizes };
		String path = "./Meta/PlateCount_PlateSize";
		try {
			if (io.existsDataset(path))
				io.removeDataset(path + "/" + "Plate_Size");
		} catch (H5IO_Exception e1) {
			e1.printStackTrace();
		}
		try {
			io.writeDataset(hdfPath, path, in);
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
	public ArrayList<int[]> getPlateSizes()
	{
		//looking at all samples to determine 1) how many plates there are based on sample's plateIDs, and then what the sizes of those plates are
		ArrayList<int[]> idsAndSize = new ArrayList<int[]>();
		// Searching all HDF5 samples
		try {
			Data_2D dat = (Data_2D) io.readDataset(hdfPath,
					"./Meta/PlateCount_PlateSize");
			int plateCount = ((Integer[][]) (dat.getData()))[0][0].intValue();
			int plateSize = ((Integer[][]) (dat.getData()))[1][0].intValue();

			for (int i = 0; i < plateCount; i++) {
				int[] newPlate = { i, plateSize };
				idsAndSize.add(newPlate);
			}

		} catch (H5IO_Exception e) {
			e.printStackTrace();
		}

	return idsAndSize;
	}
	
	/**
	 * Initializes a new SampleIndex-->PlateWellKey hashtable. NOTE: this
	 * hashtable is used to spead up well data for the GUI so we dont have to
	 * search through each sample each time we need data from a single well
	 * 
	 * @author Bjorn Millard
	 * @param null
	 * @return void
	 * */
	public void initHashtable()
	{
		System.out.println("Initializing the Hashtable");
		this.hashtable_indexToPath = new Hashtable<String, String>();
		//iterate through all samples in this project if they currently exist and hash what plate/wells they corresponds to
		try {
			int numSamples = io.getGroupChildCount(hdfPath, "./Children");
			System.out.println("___Found "+numSamples+" Samples in this project___");
			for (int i = 0; i < numSamples; i++) {
				Data_2D dat = (Data_2D) io.readDataset(hdfPath,
						"./Children/Child_" + i + "/Meta/Plate_Well");
				int plateInx = ((Integer[][])(dat.getData()))[0][0].intValue();
				int wellInx = ((Integer[][])(dat.getData()))[1][0].intValue();
				String indexKey = "p"+plateInx+"w"+wellInx;
				String pathToSample = "./Children/Child_" + i;
				hashtable_indexToPath.put(indexKey, pathToSample);

				// Indexing each field if it has single cell data in it
				// If sample exists, seeing if data for this specific field
				// exists

				// Get number of fields in this sample
				int numFields = io.getGroupChildCount(hdfPath, pathToSample
						+ "/Children");
				for (int j = 0; j < numFields; j++) {
					// seeing if this field has a "feature_values" dataset
					String path = pathToSample + "/Children/Child_" + j
							+ "/Data/feature_values";

					try {
						io.openHDF5(hdfPath);
						boolean exists = io.existsDataset(path);
					} catch (H5IO_Exception e) {
						e.printStackTrace();
					}
					String indexKeyField = indexKey + "f" + j;
					String pathToField = pathToSample + "/Children/Child_" + j;
					hashtable_indexToPath.put(indexKeyField, pathToField);
				}

			}
		} catch (H5IO_Exception e) {
			e.printStackTrace();
			try { //trying to close all streams before corruption of file
				io.closeHDF5();
				io.closeAll();
			} catch (H5IO_Exception e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("...Successfuly indexed "
				+ hashtable_indexToPath.size() + " Samples");
		System.out.println("________________");

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

		if (hashtable_indexToPath == null)
			return null;

		String pathToSample = (String) hashtable_indexToPath.get("p" + plateIdx
				+ "w" + wellIdx);

			StringBuffer[] st = null;
			if (pathToSample != null) {
			String path = null;
						StringBuffer[] st2 = null;
							String path2 = null;
							try {
								path2 = pathToSample + "/Meta/Sample_ID";
								st2 = io.readDataset_String(hdfPath, path2);
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
				.parseSamples(xmlPath);
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


}
