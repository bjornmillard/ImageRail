package hdf;

import hdf.HDFConnector.HDFConnectorException;
import idx2coordinates.IdxConverter;

import java.io.File;
import java.util.ArrayList;

import segmentedObj.Cell;
import segmentedObj.CellCompartment;
import segmentedObj.CellCoordinates;
import segmentedObj.Point;

/**
 *
 * @author Michael Menden
 *
 */
public class SegmentationHDFConnector
{
	
	private String projPath;
	private String algoName;
	
	
	/**
	 * Constructor.
	 */
	public SegmentationHDFConnector(String projPath, String algoName)
	{
		this.projPath = projPath;
		this.algoName = algoName;
		new File(projPath + "/" + algoName).mkdir();
	}
	
	
	/**
	 * Create the field.
	 * @throws HDFConnectorException
	 */
	public void createField( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		
		// Create directory structure.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize);
		(new File(fName)).mkdirs();
		
		// Create the HDF5 file.
		fName += "/field_" + fieldIdx + ".h5";
		File  file = new File(fName);
		if(file.exists())
			file.delete();
		HDFConnector con = new HDFConnector();
		con.createHDF5(fName);
		con.closeHDF5();
	}
	
	/**
	 * Write features to a field.
	 * @throws HDFConnectorException
	 */
	public void writeFeature( int plateIdx, int wellIdx, int fieldIdx, Data2D<Float> data2D) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		long[] counts = data2D.getCounts();
		if (counts[0] > 0 && counts[1] > 0)
		{
			con.createMDDlimited("features", "Float", counts);
			
			// Parameter: datasetName, data2D, dim0, dim1, offsets
			con.write2DD("features", data2D, 0, 1, new long[]{ 0, 0});
			
			// Add dimension names:
			con.writeAttr("features", "dataType", "H5T_NATIVE_FLOAT");
			con.writeAttr("features", "dim0", "cells");
			con.writeAttr("features", "dim1", "features");
		}
		
		con.closeHDF5();
	}
	
	/**
	 * Read features of a specific field.
	 * @throws HDFConnectorException
	 */
	public Float[][] readFeature( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		
		// Create 4 dimensional data cube.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		Float[][] features = new Float[][] {{0f}};
		if (con.isMember("features"))
		{
			long[] count = con.getDimsMDD("features");
			// Parameter: datasetName, dim0, dim1, offsets, count0, count1
			features = (Float[][]) con.read2DD("features", 0, 1, new long[]{ 0, 0}, count[0], count[1]).getData();
		}
		con.closeHDF5();
		return features;
	}
	
	/**
	 * Write feature names to a field.
	 * @throws HDFConnectorException
	 */
	public void writeFeatureNames( int plateIdx, int wellIdx, int fieldIdx, StringBuffer[] names) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		// write feature names
		con.createSD("feature_names", names);
		
		con.closeHDF5();
	}
	
	/**
	 * Read feature names to a field.
	 * @throws HDFConnectorException
	 */
	public StringBuffer[] readFeatureNames( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		// write feature names
		StringBuffer[] names = con.readSD("feature_names");
		
		con.closeHDF5();
		return names;
	}
	

	
	/**
	 * Write mean values of a well.
	 * @throws HDFConnectorException
	 */
	public void writeWellMeanValues( int plateIdx, int wellIdx, float[] meanValues) throws HDFConnectorException
	{
		writeWellValues( plateIdx, wellIdx, meanValues, 0);
	}
	/**
	 * Write standard deviation values of a well.
	 * @throws HDFConnectorException
	 */
	public void writeWellStdDevValues( int plateIdx, int wellIdx, float[] stdValues) throws HDFConnectorException
	{
		writeWellValues( plateIdx, wellIdx, stdValues, 1);
	}
	
	/**
	 * Read mean values of a well.
	 * @throws HDFConnectorException
	 */
	public float[] readWellMeanValues( int plateIdx, int wellIdx) throws HDFConnectorException
	{
		return readWellValues( plateIdx, wellIdx, 0);
	}
	/**
	 * Read standard deviation values of a well.
	 * @throws HDFConnectorException
	 */
	public float[] readWellStdDevValues( int plateIdx, int wellIdx) throws HDFConnectorException
	{
		return readWellValues( plateIdx, wellIdx, 1);
	}
	
	
	/**
	 * Write mean and standard deviation values of a well.
	 * @throws HDFConnectorException
	 */
	private void writeWellValues( int plateIdx, int wellIdx, float[] values, int valueIdx) throws HDFConnectorException
	{
		String directory = projPath + "/" + algoName + "/plate_" + plateIdx;
		new File(directory).mkdir();
		// file name
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/wellMeans.h5";
		HDFConnector con = new HDFConnector();
		if (con.existHDF5(fName))
		{
			con.openHDF5(fName);
		}
		else
		{
			// plate size and field height are necessary to convert Cartesian coordinates to index.
			ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
			int plateSize = projCon.readPlateSize(plateIdx);
			// create multidimensional cub
			con.createHDF5(fName);
			con.openHDF5(fName);
			// dim0 = well; dim1 = value (mean; std.); dim2 = feature
			long[] chunk = new long[] { plateSize, 2, values.length};
			con.createMDD("wellMeans", "float", chunk);
			con.writeAttr("wellMeans", "dataType", "H5T_NATIVE_FLOAT");
			con.writeAttr("wellMeans", "dim0", "well");
			con.writeAttr("wellMeans", "dim1", "value (mean; std.)");
			con.writeAttr("wellMeans", "dim2", "feature");
			
		}
		
		//		//Converting to Float objects
		Float[] vals  = new Float[values.length];
		for (int i = 0; i < values.length; i++)
			vals[i]= new Float(values[i]);
		
		// write values
		long[] offsets = new long[] { wellIdx, valueIdx, 0};
		DataOneDim val =  new Data1D<Float>(vals);
		con.write1DD("wellMeans", val, 2, offsets);
		con.closeHDF5();
	}
	
	
	/**
	 * Read mean and standard deviation values of a well.
	 * @throws HDFConnectorException
	 */
	private float[] readWellValues( int plateIdx, int wellIdx, int valueIdx) throws HDFConnectorException
	{
		// file name
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/wellMeans.h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		long[] count = con.getDimsMDD("wellMeans");
		Float[] result = null;
		if (count[0] > wellIdx)
		{
			// read result
			long[] offsets = new long[] { wellIdx, valueIdx, 0};
			DataOneDim values = con.read1DD("wellMeans", 2, offsets, count[2]);
			// Minus one to get the correct value
			result = (Float[]) values.getData();
		}
		else
		{
			// generate result
			result = new Float[(int) count[2]];
			for (int i=0; i<result.length; i++)
			{
				result[i] = new Float(0);
			}
		}
		
		int len = result.length;
		float[] temp = new float[len];
		for (int i = 0; i < len; i++)
			temp[i] = result[i].floatValue();
		
		con.closeHDF5();
		return temp;
	}
	
	/**
	 * Write bounding boxes of the cells to the field.
	 * @throws HDFConnectorException
	 */
	public void writeCellBoundingBoxes( int plateIdx, int wellIdx, int fieldIdx, ArrayList<CellCoordinates> cellList) throws HDFConnectorException
	{
		// plate size and field height are necessary to convert Cartesian coordinates to index.
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		
		// Get the compartment names.
		ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
		for (int i=0; i<cellList.size(); i++)
		{
			ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
			for (int j=0; j<cellList.get(i).getComSize(); j++)
			{
				tmpComNames.add( new StringBuffer(cellList.get(i).getCompartment(j).getName()));
			}
			if (tmpComNames.size() > comNames.size())
			{
				comNames = tmpComNames;
			}
		}
		
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		if (cellList.size() > 0  && comNames.size() > 0)
		{
			// dim0 = cells; dim1 = compartments; dim2 = index
			long[] maxDims = { cellList.size(), comNames.size(), 2};
			con.createMDDlimited("cell_bounding_boxes", "Integer", maxDims);
			
			// cell loop
			for (int i=0; i<cellList.size(); i++)
			{
				// compartment loop
				for (int j=0; j<cellList.get(i).getComSize(); j++)
				{
					Point[] pt = cellList.get(i).getCompartment(j).getCoordinates();
					Integer[] data = new Integer[] { -1, -1};
					for (int k=0; k<pt.length; k++)
					{
						data[k] = IdxConverter.point2index(pt[k], fieldHeight);
					}
					DataOneDim dataArray =  new Data1D<Integer>(data);
					// write1DD( hypercubeName, data, dim0, offset)
					con.write1DD("cell_bounding_boxes", dataArray, 2, new long[] {i,j,0});
				}
			}
			// Add dimension names.
			con.writeAttr("cell_bounding_boxes", "dataType", "H5T_NATIVE_INT");
			con.writeAttr("cell_bounding_boxes", "dim0", "cells");
			con.writeAttr("cell_bounding_boxes", "dim1", "compartments");
			con.writeAttr("cell_bounding_boxes", "dim2", "index");
			
			// Write compartment names.
			con.createSD("compartment_names_bounding_boxes", (StringBuffer[]) comNames.toArray(new StringBuffer[0]));
		}
		con.closeHDF5();
	}
	
	
	/**
	 * Read bonding boxes of the cells of a field.
	 * @throws HDFConnectorException
	 */
	public ArrayList<CellCoordinates> readCellBoundingBoxes( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		// read field height
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		// Create directory structure.
		String fName = projPath + "/" + algoName
			+ "/plate_" + plateIdx
			+ "/well_" + IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		// Open HDF file.
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		ArrayList<CellCoordinates> cellList = null;
		if (con.isMember("cell_bounding_boxes"))
		{
			cellList = new ArrayList<CellCoordinates>();
			// dim0 = cells; dim1 = components; dim2 = index
			long[] counts = con.getDimsMDD("cell_bounding_boxes");
			
			// Read compartment names
			StringBuffer[] comNames = con.readSD("compartment_names_bounding_boxes");
			
			// cell loop
			for (int i=0; i<counts[0]; i++)
			{
				ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
				// compartment loop
				for (int j=0; j<counts[1]; j++)
				{
					// parameter: datasetName, dim0, offsets, count0
					DataOneDim data = con.read1DD("cell_bounding_boxes",
												  2,
												  new long[] {i,j,0},
												  counts[2]);
					ArrayList<Point> coordinates = new ArrayList<Point>();
					Integer[] idx = (Integer[]) data.getData();
					// point loop
					for (int k=0; k<idx.length && idx[k]>=0; k++)
					{
						coordinates.add( IdxConverter.index2point(idx[k], fieldHeight));
					}
					comArray.add( new CellCompartment( coordinates, comNames[j].toString().trim()));
				}
				CellCoordinates cell = new CellCoordinates( comArray);
				cellList.add( cell);
			}
		}
		con.closeHDF5();
		return cellList;
	}
	
	
	/**
	 * Write whole cells to the field.
	 * @throws HDFConnectorException
	 */
	public void writeWholeCells( int plateIdx, int wellIdx, int fieldIdx, ArrayList<CellCoordinates> cellList) throws HDFConnectorException
	{
		// plate size and field height are necessary to convert Cartesian coordinates to index.
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		// Get the compartment names.
		ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
		// cell loop
		for (int i=0; i<cellList.size(); i++)
		{
			ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
			// compartment loop
			for (int j=0; j<cellList.get(i).getComSize(); j++)
			{
				tmpComNames.add( new StringBuffer(cellList.get(i).getCompartment(j).getName()));
			}
			if (tmpComNames.size() > comNames.size())
			{
				comNames = tmpComNames;
			}
		}
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		con.createGroup("whole_cells");
		if (cellList.size() > 0  && comNames.size() > 0)
		{
			// cell loop
			for (int i=0; i<cellList.size(); i++)
			{
				con.createGroup("whole_cells/cell_" + i);
				// compartment loop
				for (int j=0; j<cellList.get(i).getComSize(); j++)
				{
					Point[] pt = cellList.get(i).getCompartment(j).getCoordinates();
					if (pt.length > 0)
					{
						Integer[] data = new Integer[pt.length];
						for (int k=0; k<pt.length; k++)
						{
							data[k] = IdxConverter.point2index(pt[k], fieldHeight);
						}
						DataOneDim dataArray =  new Data1D<Integer>(data);
						String compartmentDS = "whole_cells/cell_" + i + "/"+ cellList.get(i).getCompartment(j).getName();
						// dim0 = index
						con.createMDDlimited( compartmentDS, "Integer", new long[]{ data.length});
						// write1DD( hypercubeName, data, dim0, offset)
						con.write1DD( compartmentDS, dataArray, 0, new long[] {0});
						// Add dimension names.
						con.writeAttr(compartmentDS, "dataType", "H5T_NATIVE_INT");
						con.writeAttr( compartmentDS, "dim0", "index");
					}
				}
			}
			// Write compartment names.
			con.createSD( "whole_cells/compartment_names_whole_cells",
							 (StringBuffer[]) comNames.toArray(new StringBuffer[0]));
			
			// Write cell count
			con.createMDDlimited("whole_cells/cell_count", "Integer", new long[]{1});
			con.writeAttr("whole_cells/cell_count", "dataType", "H5T_NATIVE_INT");
			DataOneDim cellCount =  new Data1D<Integer>( new Integer[] { cellList.size()});
			con.write1DD("whole_cells/cell_count", cellCount, 0, new long[]{0});
		}
		con.closeHDF5();
	}
	
	/**
	 * Read whole cells of a field.
	 * @throws HDFConnectorException
	 */
	public ArrayList<CellCoordinates> readWholeCells( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		// read field height
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		
		// Create directory structure.
		String fName = projPath + "/" + algoName
			+ "/plate_" + plateIdx
			+ "/well_" + IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		
		System.out.println("fNAME: "+fName);
		// Open HDF file.
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		ArrayList<CellCoordinates> cellList = null;
		if (con.isMember("whole_cells/compartment_names_whole_cells")
			&&  con.isMember("whole_cells/cell_count"))
		{
			cellList = new ArrayList<CellCoordinates>();
			// Get cell count
			DataOneDim dataArray = con.read1DD("whole_cells/cell_count", 0, new long[] {0}, 1);
			Integer[] cellCount = (Integer[]) dataArray.getData();
			// Read compartment names
			StringBuffer[] comNames = con.readSD("whole_cells/compartment_names_whole_cells");
			
			// cell loop
			for (int i=0; i<cellCount[0]; i++)
			{
				ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
				// compartment loop
				for (int j=0; j<comNames.length; j++)
				{
					String comName  = "whole_cells/cell_" + i + "/" + comNames[j].toString().trim();
					if( con.isMember(comName))
					{
						// dim0 = index
						long[] counts = con.getDimsMDD( comName);
						// parameter: datasetName, dim0, offsets, count0
						DataOneDim data = con.read1DD( comName, 0, new long[] {0}, counts[0]);
						Integer[] idx = (Integer[]) data.getData();
						// point loop
						ArrayList<Point> coordinates = new ArrayList<Point>();
						for (int k=0; k<idx.length; k++)
						{
							coordinates.add( IdxConverter.index2point(idx[k], fieldHeight));
						}
						comArray.add( new CellCompartment( coordinates, comNames[j].toString().trim()));
					}
				}
				CellCoordinates cell = new CellCoordinates( comArray);
				cellList.add( cell);
			}
		}
		con.closeHDF5();
		return cellList;
	}
	
	
	
	/**
	 * Read centroids of the cells of a field.
	 * @throws HDFConnectorException
	 */
	public ArrayList<CellCoordinates> readCellCentroids( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// get size of the plate
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		// read field height
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		// Create directory structure.
		String fName = projPath + "/" + algoName
			+ "/plate_" + plateIdx
			+ "/well_" + IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		// Open HDF file.
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		ArrayList<CellCoordinates> cellList = null;
		if (con.isMember("cell_centroids"))
		{
			cellList = new ArrayList<CellCoordinates>();
			// dim0 = cells; dim1 = components; dim2 = index
			long[] counts = con.getDimsMDD("cell_centroids");
			
			// Read compartment names
			StringBuffer[] comNames = con.readSD("compartment_names_cell_centroids");
			
			// cell loop
			for (int i=0; i<counts[0]; i++)
			{
				ArrayList<CellCompartment> comArray = new ArrayList<CellCompartment>();
				// compartment loop
				for (int j=0; j<counts[1]; j++)
				{
					// parameter: datasetName, dim0, offsets, count0
					DataOneDim data = con.read1DD("cell_centroids",
												  2,
												  new long[] {i,j,0},
												  1);
					ArrayList<Point> coordinates = new ArrayList<Point>();
					Integer[] idx = (Integer[]) data.getData();
					// point loop
					for (int k=0; k<idx.length && idx[k]>=0; k++)
					{
						coordinates.add( IdxConverter.index2point(idx[k], fieldHeight));
					}
					comArray.add( new CellCompartment( coordinates, comNames[j].toString().trim()));
				}
				CellCoordinates cell = new CellCoordinates( comArray);
				cellList.add( cell);
			}
		}
		con.closeHDF5();
		return cellList;
	}
	
	/**
	 * Write centroids of the cells to the field.
	 * @throws HDFConnectorException
	 */
	public void writeCellCentroids( int plateIdx, int wellIdx, int fieldIdx, ArrayList<CellCoordinates> cellList) throws HDFConnectorException
	{
		// plate size and field height are necessary to convert Cartesian coordinates to index.
		ProjectHDFConnector projCon = new ProjectHDFConnector(projPath);
		int plateSize = projCon.readPlateSize(plateIdx);
		int fieldHeight = projCon.readFieldHeight(plateIdx, wellIdx, fieldIdx);
		
		// Get the compartment names.
		ArrayList<StringBuffer> comNames = new ArrayList<StringBuffer>();
		for (int i=0; i<cellList.size(); i++)
		{
			ArrayList<StringBuffer> tmpComNames = new ArrayList<StringBuffer>();
			for (int j=0; j<cellList.get(i).getComSize(); j++)
			{
				tmpComNames.add( new StringBuffer(cellList.get(i).getCompartment(j).getName()));
			}
			if (tmpComNames.size() > comNames.size())
			{
				comNames = tmpComNames;
			}
		}
		
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/well_"
			+ IdxConverter.index2well(wellIdx, plateSize)
			+ "/field_" + fieldIdx + ".h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		if (cellList.size() > 0  && comNames.size() > 0)
		{
			// dim0 = cells; dim1 = compartments; dim2 = index
			long[] maxDims = { cellList.size(), comNames.size(), 1};
			con.createMDDlimited("cell_centroids", "Integer", maxDims);
			// cell loop
			for (int i=0; i<cellList.size(); i++)
			{
				// compartment loop
				for (int j=0; j<cellList.get(i).getComSize(); j++)
				{
					Point[] pt = cellList.get(i).getCompartment(j).getCoordinates();
					Integer[] data = new Integer[1];
					for (int k=0; k<pt.length; k++)
					{
						data[k] = IdxConverter.point2index(pt[k], fieldHeight);
					}
					DataOneDim dataArray =  new Data1D<Integer>(data);
					// write1DD( hypercubeName, data, dim0, offset)
					con.write1DD("cell_centroids", dataArray, 2, new long[] {i,j,0});
				}
			}
			// Add dimension names.
			con.writeAttr("cell_centroids", "dataType", "H5T_NATIVE_INT");
			con.writeAttr("cell_centroids", "dim0", "cells");
			con.writeAttr("cell_centroids", "dim1", "compartments");
			con.writeAttr("cell_centroids", "dim2", "index");
			
			// Write compartment names.
			con.createSD("compartment_names_cell_centroids", (StringBuffer[]) comNames.toArray(new StringBuffer[0]));
		}
		con.closeHDF5();
	}

	/**
	 * Reads the coords and values for the given field and returns complete
	 * cells that contain both their coordinates and values
	 * 
	 * @author BLM
	 * @throws HDFConnectorException
	 */
	public ArrayList<Cell> readCells(int plateIdx, int wellIdx, int fieldIdx)
			throws HDFConnectorException {

		ArrayList<Cell> cells = new ArrayList<Cell>();

		ArrayList<CellCoordinates> coords = readCoordinates(plateIdx, wellIdx,
				fieldIdx);
		float[][] vals = convertTofloatMatrix(readFeature(
				plateIdx, wellIdx, fieldIdx));
		int len = vals.length;
		for (int i = 0; i < len; i++) {
			cells.add(new Cell(i, coords.get(i), vals[i]));
		}

		return cells;
	}

	/**
	 * Read cells from HDF project. Note that it first checks what sort of data
	 * is being stored, either: (1) BoundingBoxes (2) Centroids or (3) Multiple
	 * compartments. Note that BoundingBoxes and Centroids can be
	 * written/read/stored very efficiently in a single dataset whereas the more
	 * complex data structures require the use of seperate HDF groups for each
	 * cell.
	 * 
	 * @throws HDFConnectorException
	 */
	public ArrayList<CellCoordinates> readCoordinates( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// Parameters to write: plateIdx, wellIdx, fieldIdx
		ArrayList<CellCoordinates>cellList = readCellBoundingBoxes( plateIdx, wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readCellCentroids( plateIdx, wellIdx, fieldIdx);
		if(cellList==null)
			cellList = readWholeCells( plateIdx, wellIdx, fieldIdx);
		return cellList ;
	}
	
	/**
	 * Write feature names to well means.
	 * @throws HDFConnectorException
	 */
	public void writeMeanFeatureNames( int plateIdx, StringBuffer[] names) throws HDFConnectorException
	{
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/wellMeans.h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		// write feature names
		if(!con.isMember("feature_names"))
			con.createSD("feature_names", names);
		
		con.closeHDF5();
	}
	
	/**
	 * Read feature names of the mean values
	 * @throws HDFConnectorException
	 */
	public StringBuffer[] readMeanFeatureNames( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		// Open file.
		String fName = projPath + "/" + algoName + "/plate_" + plateIdx + "/wellMeans.h5";
		HDFConnector con = new HDFConnector();
		con.openHDF5(fName);
		
		// write feature names
		StringBuffer[] names = con.readSD("feature_names");
		
		con.closeHDF5();
		return names;
	}

	/**
	 * Converts the default Float[][] object values to float primitives
	 * 
	 * @author BLM
	 */
	static public float[][] convertTofloatMatrix(Float[][] data) {
		float[][] fVals = new float[data.length][data[0].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				fVals[i][j] = data[i][j].floatValue();
		data = null;
		return fVals;
	}
}
