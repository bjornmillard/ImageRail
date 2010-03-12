package hdf;

import ncsa.hdf.hdf5lib.HDF5Constants;

/**
 * 
 * @author Michael Menden
 *
 */
public class Data2D <T> implements DataTwoDim{

	private T[][] data;
	private int hdfType;
	
	/** 
	 * Constructor.
	 */
	public Data2D( T[][] data) { //, int count0, int count1) {
		this.data = data;
		// type of data
		String tmp = data.getClass().getSimpleName();
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
	 * Get the data matrix.
	 */
	public T[][] getData() {
		return data;
	}
	
	/** 
	 * Get a single element of the data matrix.
	 */
	public T getElem( int i, int j) {
		return data[i][j];
	}
	
	/** 
	 * Get the counts.
	 */
	public long[] getCounts() {
		return new long[] {data.length, data[0].length};
	}

	/** 
	 * Get the HDF type.
	 */
	public int getHDFType() {
		return hdfType;
	}
}
