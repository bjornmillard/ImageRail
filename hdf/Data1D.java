package hdf;

import ncsa.hdf.hdf5lib.HDF5Constants;

/**
 * 
 * @author Michael Menden
 *
 */
public class Data1D<T> implements DataOneDim{
	
	private T[] dataArray;
	private int hdfType;

	/** 
	 * Constructor.
	 */
	public Data1D( T[] dataArray) {
		this.dataArray = dataArray;
		// type of data
		String tmp = dataArray.getClass().getSimpleName();
		String type = tmp.substring(0, tmp.indexOf('['));
		if (type.toUpperCase().compareTo("INT") == 0 || type.toUpperCase().compareTo("INTEGER") == 0)
			hdfType = HDF5Constants.H5T_NATIVE_INT;
		else if (type.toUpperCase().compareTo("SHORT") == 0)
			hdfType = HDF5Constants.H5T_NATIVE_SHORT;
		else if (type.toUpperCase().compareTo("FLOAT") == 0)
			hdfType = HDF5Constants.H5T_NATIVE_FLOAT;
		else if (type.toUpperCase().compareTo("DOUBLE") == 0)
			hdfType = HDF5Constants.H5T_NATIVE_DOUBLE;
		else if (type.toUpperCase().compareTo("BYTE") == 0)
			hdfType = HDF5Constants.H5T_NATIVE_CHAR;
	}
	
	/** 
	 * Get the data array.
	 */
	public T[] getData() {
		return dataArray;
	}
	
	/** 
	 * Get the HDF type.
	 */
	public int getHDFType() {
		return hdfType;
	}
	
	/** 
	 * Get the count.
	 */
	public int getCount() {
		return dataArray.length;
	}
}
