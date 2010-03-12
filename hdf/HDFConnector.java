package hdf;

import java.util.logging.Level;
import java.util.logging.Logger;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 *
 * @author Michael Menden
 *
 */
public class HDFConnector {
	
	private static final Logger logger = Logger.getLogger("hdf.HDFConnector");
	
	private int file_id      = -1;
	private int group_id     = -1;
	private int dataset_id   = -1;
	private int attr_id      = -1;
	private int memtype_id   = -1;
	private int filetype_id  = -1;
	private int filespace_id = -1;
	private int memspace_id  = -1;
	private int dcpl_id      = -1;
	
	/**
	 * Creates the HDF5 file.
	 * @throws HDFConnectorException
	 */
	public void createHDF5(String fName) throws HDFConnectorException {
		try {
			file_id = H5.H5Fcreate(fName, HDF5Constants.H5F_ACC_TRUNC,
								   HDF5Constants.H5P_DEFAULT,
								   HDF5Constants.H5P_DEFAULT);
		}
		catch (HDF5LibraryException ex) {
			logger.log(Level.SEVERE, "Cannot create an HDF5file", ex);
			throw new HDFConnectorException("Cannot create an HDF5file: " + ex.getMessage());
		}
		finally {
			closeHDF5();
		}
	}
	
	/**
	 * Open the file fName.
	 * @throws HDFConnectorException
	 */
	public void openHDF5(String fName) throws HDFConnectorException {
		try {
			// Open file using the default properties.
			file_id = H5.H5Fopen(fName, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);
		}
		catch (HDF5LibraryException ex) {
			logger.log(Level.SEVERE, "Not able to open " + fName, ex);
			throw new HDFConnectorException("Not able to open " + fName + ": ");
		}
	}
	
	/**
	 * Checks if the given HDF file name exists
	 */
	public boolean existHDF5(String fName){
		boolean result = false;
		try {
			// Open file using the default properties.
			file_id = H5.H5Fopen(fName, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);
			result = true;
		}
		catch (HDF5LibraryException ex) {}
		return result;
	}
	
	/**
	 * Close the current file.
	 * @throws HDFConnectorException
	 */
	public void closeHDF5() throws HDFConnectorException {
		try {
			if (file_id >= 0) {
				H5.H5Fclose(file_id);
				file_id = -1;
			}
		}
		catch (HDF5LibraryException ex) {
			logger.log(Level.SEVERE, "Not able to close the file", ex);
			throw new HDFConnectorException("Not able to close the file: " + ex.getMessage());
		}
	}
	
	/**
	 * Creates a group.
	 * @throws HDFConnectorException
	 */
	public void createGroup(String groupName) throws HDFConnectorException{
		try {
			try {
				group_id = H5.H5Gopen(file_id, "/" + groupName);
			}
			// Group does not exist till now.
			catch (Exception e) {
				group_id = H5.H5Gcreate(file_id, "/" + groupName, HDF5Constants.H5P_DEFAULT);
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot create the '" + groupName + "' folder", ex);
			throw new HDFConnectorException("Cannot create the '" + groupName
					                     + "' folder: " + ex.getMessage());
		}
		// Close Group.
		finally {
			try {
				if (group_id >= 0) {
					H5.H5Gclose(group_id);
					group_id = -1;
				}
			}
			catch (Exception ex) {
				logger.log(Level.SEVERE, "Cannot close the folder", ex);
				throw new HDFConnectorException("Cannot close the folder: " + ex.getMessage());
			}
		}
	}
	
	/**
	 * Creates a String data set.
	 * @throws HDFConnectorException
	 */
	public void createSD(String datasetName, StringBuffer[] str_data) throws HDFConnectorException {
		try {
			int dim0 = str_data.length;
			// maximal size of String
			int dim1 = 0;
			for (int i = 0; i < dim0; i++) {
				if (str_data[i].length() > dim1)
					dim1 = str_data[i].length() + 1;
			}
			long[] dims = { dim0 };
			byte[][] dset_data = new byte[dim0][dim1];
			// Create file and memory data types. We will save
			// the strings as FORTRAN strings, therefore they do not need space
			// for the null terminator in the file.
			filetype_id = H5.H5Tcopy(HDF5Constants.H5T_FORTRAN_S1);
			H5.H5Tset_size(filetype_id, dim1 - 1);
			memtype_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
			H5.H5Tset_size(memtype_id, dim1);
			// Create memory space. Setting maximum size to NULL sets the maximum
			// size to be the current size.
			memspace_id = H5.H5Screate_simple(dims.length, dims, null);
			// Create the String data set
			dataset_id = H5.H5Dcreate(file_id, datasetName, filetype_id,
									  memspace_id, HDF5Constants.H5P_DEFAULT);
			// Write the data to the data set.
			for (int indx = 0; indx < dim0; indx++) {
				for (int jndx = 0; jndx < dim1; jndx++) {
					if (jndx < str_data[indx].length())
						dset_data[indx][jndx] = (byte) str_data[indx].charAt(jndx);
					else
						dset_data[indx][jndx] = 0;
				}
			}
			H5.H5Dwrite(dataset_id, memtype_id, HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, dset_data);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot create '" + datasetName, ex);
			throw new HDFConnectorException("Cannot create '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
			closeDataset();
			// Terminate access to the memory space.
			closeMemSpace();
			// Terminate access to the file type.
			closeFileType();
			// Terminate access to the memory type.
			closeMemType();
		}
	}
	
	/**
	 * Read a String data set.
	 * @throws HDFConnectorException
	 */
	public StringBuffer[] readSD(String datasetName) throws HDFConnectorException {
		StringBuffer[] str_data;
		try {
			// Open an existing data set.
			dataset_id = H5.H5Dopen(file_id, datasetName);
			// Get data space.
			memspace_id = H5.H5Dget_space(dataset_id);
			long dim0 = H5.H5Sget_simple_extent_npoints(memspace_id);
			// Get the data type
			filetype_id = H5.H5Dget_type(dataset_id);
			// Get the size of the data type.
			// +1 make room for null terminator.
			int dim1 = H5.H5Tget_size(filetype_id) + 1;
			// Allocate space for data.
			byte[][] dset_data;
			dset_data = new byte[(int) dim0][dim1];
			str_data = new StringBuffer[(int) dim0];
			// Create the memory data type.
			memtype_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
			H5.H5Tset_size(memtype_id, dim1);
			// Read data.
			if ((dataset_id >= 0) && (memtype_id >= 0))
				H5.H5Dread(dataset_id, memtype_id, HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, dset_data);
			byte[] tempbuf = new byte[dim1];
			for (int indx = 0; indx < (int) dim0; indx++) {
				for (int jndx = 0; jndx < dim1; jndx++) {
					tempbuf[jndx] = dset_data[indx][jndx];
				}
				str_data[indx] = new StringBuffer(new String(tempbuf));
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot read " + datasetName, ex);
			throw new HDFConnectorException("Cannot read '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
			closeDataset();
			// Terminate access to the memory space.
			closeMemSpace();
			// Terminate access to the file type.
			closeFileType();
			// Terminate access to the memory type.
			closeMemType();
		}
		return str_data;
	}
	
	/**
	 * Creates a multidimensional data set with unlimited dimensions.
	 * @throws HDFConnectorException
	 */
	public void createMDD(String datasetName, String type, long chunk[]) throws HDFConnectorException {
		try {
			long maxDims[] = new long[chunk.length];
			long counts[] = new long[chunk.length];
			long offsets[] = new long[chunk.length];
			for (int i=0; i<chunk.length; i++) {
				maxDims[i] = HDF5Constants.H5S_UNLIMITED;
				counts[i] = 1;
				offsets[i] = 0;
			}
			// Create the memory space with unlimited dimensions.
			memspace_id = H5.H5Screate_simple (chunk.length, counts, maxDims);
			// Modify data set creation properties and enable chunking
			dcpl_id = H5.H5Pcreate (HDF5Constants.H5P_DATASET_CREATE);
			H5.H5Pset_chunk ( dcpl_id, chunk.length, chunk);
			// Select the HDF type. If wrong values are stored in hyper cube, this may the reason!
			int hdfType = -1;
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
		    // Create a new data set within the file using properties list.
		    dataset_id = H5.H5Dcreate (file_id, datasetName, hdfType, memspace_id, dcpl_id);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot create '" + datasetName, ex);
			throw new HDFConnectorException("Cannot create '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			closeDataset();
		    closeMemSpace();
		    closeFileSpace();
		    closePropertyList();
		}
	}
	
	/**
	 * Creates a multidimensional data set with limited dimensions.
	 * @throws HDFConnectorException
	 */
	public void createMDDlimited(String datasetName, String type, long[] maxDims) throws HDFConnectorException {
		try {
			long counts[] = new long[maxDims.length];
			long offsets[] = new long[maxDims.length];
			for (int i=0; i<maxDims.length; i++) {
				counts[i] = 1;
				offsets[i] = 0;
			}
			// Create the memory space with limited dimensions.
			memspace_id = H5.H5Screate_simple (maxDims.length, maxDims, null);
			// Select the HDF type. If wrong values are stored in hyper cube, this may the reason!
			int hdfType = -1;
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
		    // Create a new data set within the file using properties list.
		    dataset_id = H5.H5Dcreate (file_id, datasetName, hdfType, memspace_id, HDF5Constants.H5P_DEFAULT);
		}
		catch (Exception ex) {
			// logger.log(Level.SEVERE, "Cannot create '" + datasetName, ex);
			throw new HDFConnectorException("Cannot create '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			closeDataset();
		    closeMemSpace();
		    closeFileSpace();
		    closePropertyList();
		}
	}
	
	/**
	 * Writes a one dimensional data set to multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public void write1DD(String datasetName, DataOneDim data1D, int dim0, long[] offsets) throws HDFConnectorException {
		try {
			// Open an existing multidimensional data set.
			dataset_id = H5.H5Dopen(file_id, datasetName);
			// Get the file space
			filespace_id = H5.H5Dget_space(dataset_id);
			// Get the dimensions of the multidimensional data set
			long rankMD = H5.H5Sget_simple_extent_ndims(filespace_id);
			long[] dimsMD = new long[(int)rankMD];
			H5.H5Sget_simple_extent_dims(filespace_id, dimsMD, null);
		    // Set counts for multidimensional data set
			long count[] = new long[(int)rankMD];
			for (int i=0; i<rankMD; i++) {
				count[i] = 1;
			}
			count[dim0] = data1D.getCount();
			// Check if necessary to extend multidimensional data set.
			boolean extend = false;
			long[] size1D = new long[]{offsets[dim0] + data1D.getCount()};
			// dim0 has to grow.
			if (dimsMD[dim0] < size1D[0]) {
				dimsMD[dim0] = size1D[0];
				extend = true;
			}
			// Another dimension of the multidimensional data set has to grow.
			for (int i=0; i<rankMD; i++) {
				if (dimsMD[i] < offsets[i] + 1) {
					dimsMD[i] = offsets[i] + 1;
					extend = true;
				}
			}
			// Extend the multidimensional data set if necessary.
			if (extend) {
				H5.H5Dextend (dataset_id, dimsMD);
				closeFileSpace();
			    filespace_id = H5.H5Dget_space (dataset_id);
			}
			// Select hyperslab.
		    H5.H5Sselect_hyperslab (filespace_id, HDF5Constants.H5S_SELECT_SET, offsets, null,
		    					   	count, null);
		    
		    // Define memory space
		    long[] count1D = new long[] {data1D.getCount()};
		    memspace_id = H5.H5Screate_simple (1, count1D, null);
		    //Write the data from hyperslab to file.
		    H5.H5Dwrite (dataset_id, data1D.getHDFType(), memspace_id, filespace_id,
		                 HDF5Constants.H5P_DEFAULT, data1D.getData());

		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot write to " + datasetName, ex);
			throw new HDFConnectorException("Cannot write to '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// Close resources
			closeDataset();
		    closeMemSpace();
		    closeFileSpace();
		}
	}
	
	/**
	 * Read an one dimensional data out of the multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public DataOneDim read1DD(String datasetName, int dim0, long[] offsets, long count0) throws HDFConnectorException{
		DataOneDim result = null;
		try {
			// Open an existing data set.
		    dataset_id = H5.H5Dopen (file_id, datasetName);
		    filespace_id = H5.H5Dget_space (dataset_id);
		   
		    int rankMD = offsets.length;
		    long count[] = new long[rankMD];
			for (int i=0; i<rankMD; i++) {
				count[i] = 1;
			}
			count[dim0] = count0;
		  
		    // select hyperslab
		    H5.H5Sselect_hyperslab(filespace_id,  HDF5Constants.H5S_SELECT_SET,
		    		offsets, null, count, null);
		    
		    // Define memory space
		    memspace_id = H5.H5Screate_simple (1, new long[]{count0}, null);
 
			filetype_id = H5.H5Dget_type(dataset_id);
			if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_FLOAT) {
		    	// Float
				if (H5.H5Tget_size(filetype_id) == 4) {
		    		Float data_out[] = new Float [(int) count0];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data1D<Float>(data_out);
		    	}
				// Double
		    	else if (H5.H5Tget_size(filetype_id) == 8) {
		    		Double data_out[] = new Double [(int) count0];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data1D<Double>(data_out);
		    	}
		    }
		    else if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_INTEGER) {
		    	// Byte
		    	if (H5.H5Tget_size(filetype_id) == 1) {
		    		Byte data_out[] = new Byte [(int) count0];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data1D<Byte>(data_out);
		    	}
		    	// Short
		    	else if (H5.H5Tget_size(filetype_id) == 2) {
		    		Short data_out[] = new Short [(int) count0];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data1D<Short>(data_out);
		    	}
		    	// Integer
		    	else if (H5.H5Tget_size(filetype_id) == 4) {
		    		Integer data_out[] = new Integer [(int) count0];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data1D<Integer>(data_out);
		    	}
		    }
		}
		catch (Exception ex) {
//			logger.log(Level.SEVERE, "Cannot read " + datasetName, ex);
			throw new HDFConnectorException("Cannot read '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
		    closeDataset();
		    closeFileSpace();
		    closeFileType();
		    closeMemSpace();
		}
		return result;
	}
	
	/**
	 * Writes a two dimensional data set to multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public void write2DD(String datasetName, DataTwoDim data2D, int dim0, int dim1, long[] offsets) throws HDFConnectorException {
		try {
			// Open an existing multidimensional data set.
			dataset_id = H5.H5Dopen(file_id, datasetName);
			// Get the file space
			filespace_id = H5.H5Dget_space(dataset_id);
			// Get the dimensions of the multidimensional data set
			long rankMD = H5.H5Sget_simple_extent_ndims(filespace_id);
			long[] dimsMD = new long[(int)rankMD];
			H5.H5Sget_simple_extent_dims(filespace_id, dimsMD, null);
		    // Set counts for multidimensional data set
			long count[] = new long[(int)rankMD];
			for (int i=0; i<rankMD; i++) {
				count[i] = 1;
			}
			long[] count2D = data2D.getCounts();
			count[dim0] = count2D[0];
			count[dim1] = count2D[1];
			// Check if necessary to extend multidimensional data set.
			boolean extend = false;
			long size2D[] = new long[2];
			// long[] offsetMD = dc.getOffsets();
			size2D[0] = offsets[dim0] + count2D[0];
			size2D[1] = offsets[dim1] + count2D[1];
			// dim0 has to grow.
			if (dimsMD[dim0] < size2D[0]) {
				dimsMD[dim0] = size2D[0];
				extend = true;
			}
			// dim1 has to grow.
			if (dimsMD[dim1] < size2D[1]) {
				dimsMD[dim1] = size2D[1];
				extend = true;
			}
			// Another dimension of the multidimensional data set has to grow.
			for (int i=0; i<rankMD; i++) {
				if (dimsMD[i] < offsets[i] + 1) {
					dimsMD[i] = offsets[i] + 1;
					extend = true;
				}
			}
			// Extend the multidimensional data set if necessary.
			if (extend) {
				H5.H5Dextend (dataset_id, dimsMD);
				closeFileSpace();
			    filespace_id = H5.H5Dget_space (dataset_id);
			}
			// Select hyperslab.
		    H5.H5Sselect_hyperslab (filespace_id, HDF5Constants.H5S_SELECT_SET, offsets, null,
		    					   	count, null);
		    // Define memory space
		    memspace_id = H5.H5Screate_simple (2, data2D.getCounts(), null);
		    //Write the data from hyperslab to file.
		    H5.H5Dwrite (dataset_id, data2D.getHDFType(), memspace_id, filespace_id,
		                 HDF5Constants.H5P_DEFAULT, data2D.getData());

		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot write to " + datasetName, ex);
			throw new HDFConnectorException("Cannot write to '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// Close resources
			closeDataset();
		    closeMemSpace();
		    closeFileSpace();
		}
	}
	
	/**
	 * Read a two dimensional data out of the multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public DataTwoDim read2DD(String datasetName, int dim0, int dim1, long[] offsets, long count0, long count1) throws HDFConnectorException{
		DataTwoDim result = null;
		try {
			// Open an existing data set.
		    dataset_id = H5.H5Dopen (file_id, datasetName);
		    filespace_id = H5.H5Dget_space (dataset_id);
		   
		    int rankMD = offsets.length;
		    long count[] = new long[rankMD];
			for (int i=0; i<rankMD; i++) {
				count[i] = 1;
			}
			count[dim0] = count0;
			count[dim1] = count1;
		  
		    // select hyperslab
		    H5.H5Sselect_hyperslab(filespace_id,  HDF5Constants.H5S_SELECT_SET,
		    		offsets, null, count, null);
		    
		    // Define memory space
		    long dims2D[] = { count0, count1};
		    memspace_id = H5.H5Screate_simple (2, dims2D, null);
 
			filetype_id = H5.H5Dget_type(dataset_id);
			if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_FLOAT) {
		    	// Float
				if (H5.H5Tget_size(filetype_id) == 4) {
		    		Float data_out[][] = new Float [(int) count0][(int) count1];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data2D<Float>(data_out);
		    	}
				// Double
		    	else if (H5.H5Tget_size(filetype_id) == 8) {
		    		Double data_out[][] = new Double [(int) count0][(int) count1];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data2D<Double>(data_out);
		    	}
		    }
		    else if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_INTEGER) {
		    	// Byte
		    	if (H5.H5Tget_size(filetype_id) == 1) {
		    		Byte data_out[][] = new Byte [(int) count0][(int) count1];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data2D<Byte>(data_out);
		    	}
		    	// Short
		    	else if (H5.H5Tget_size(filetype_id) == 2) {
		    		Short data_out[][] = new Short [(int) count0][(int) count1];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data2D<Short>(data_out);
		    	}
		    	// Integer
		    	else if (H5.H5Tget_size(filetype_id) == 4) {
		    		Integer data_out[][] = new Integer [(int) count0][(int) count1];
			    	H5.H5Dread (dataset_id, filetype_id, memspace_id, filespace_id,
					  		HDF5Constants.H5P_DEFAULT, data_out);
			    	result = new Data2D<Integer>(data_out);
		    	}
		    }
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot read " + datasetName, ex);
			throw new HDFConnectorException("Cannot read '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
		    closeDataset();
		    closeFileSpace();
		    closeFileType();
		    closeMemSpace();
		}
		return result;
	}
		
	/**
	 * Writes an attribute to a data set.
	 * @throws HDFConnectorException
	 */
	public void writeAttr(String datasetName, String attrName, String attrDesc) throws HDFConnectorException {
		try {
			// Open an existing data set.
			dataset_id = H5.H5Dopen(file_id, datasetName);
			// create memory space
			long[] tmp = { 1};
			memspace_id = H5.H5Screate_simple (1, tmp, null);
			// Create memory data types.
			memtype_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
			H5.H5Tset_size(memtype_id, attrDesc.length());
			// Create attribute.
			attr_id = H5.H5Acreate(dataset_id, attrName, memtype_id, memspace_id, HDF5Constants.H5P_DEFAULT);
			// Write attribute to data set.
			byte[] attr = new byte[attrDesc.length()];
			for (int i = 0; i<attrDesc.length(); i++) {
				attr[i] = (byte) attrDesc.charAt(i);
			}
			H5.H5Awrite(attr_id, memtype_id, attr);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot write " + attrName + " to " + datasetName, ex);
			throw new HDFConnectorException("Cannot write " + attrName + " to " + datasetName+ "': " + ex.getMessage());
		}
		finally {
			// Close resources
			closeDataset();
			closeMemSpace();
		    closeMemType();
		    closeAttr();
		}
	}
	
	/**
	 * Read attributes from a data set.
	 * @throws HDFConnectorException
	 */
	public String[][] readAttr(String datasetName) throws HDFConnectorException {
		String[][] attributes = null;
		try {
			// Open an existing data set.
			dataset_id = H5.H5Dopen(file_id, datasetName);
			// Iterates over all attributes.
			attributes = new String[H5.H5Aget_num_attrs(dataset_id)][2];
			for (int i=0; i<H5.H5Aget_num_attrs(dataset_id); i++) {
				// Get attribute.
				attr_id = H5.H5Aopen_idx(dataset_id, i);
				// Create memory data types.
				memtype_id = H5.H5Aget_type( attr_id);
				// Get the name of the attribute.
				String[] attrName = new String[1];
				// WARNING: 200 is the max length of an attribute
				//          name which could be read...
				H5.H5Aget_name(attr_id, 200, attrName);
				attributes[i][0] = attrName[0];
				// Read an Attribute.
				byte[] attr = new byte[H5.H5Tget_size(memtype_id)];
				H5.H5Aread(attr_id, memtype_id, attr);
				attributes[i][1] = new String(attr);
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot attributes from " + datasetName, ex);
			throw new HDFConnectorException("Cannot attributes from " + datasetName + ": " + ex.getMessage());
		}
		finally {
			// Close resources
			closeDataset();
		    closeMemType();
		    closeAttr();
		}
		return attributes;
	}
	
	/**
	 * Get the dimensions of a multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public long[] getDimsMDD(String datasetName) throws HDFConnectorException {
		long dimsMD[];
		try {
			// Open an existing data set.
		    dataset_id = H5.H5Dopen (file_id, datasetName);
		    filespace_id = H5.H5Dget_space (dataset_id);
		    // Get the dimensions.
		    int rank = H5.H5Sget_simple_extent_ndims (filespace_id);
		    dimsMD = new long[rank];
		    H5.H5Sget_simple_extent_dims (filespace_id, dimsMD, null);
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "No access " + datasetName, ex);
			throw new HDFConnectorException("No access to '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
		    closeDataset();
		    closeFileSpace();
		}
	    return dimsMD;
	}
	
	/**
	 * Get the java data type of the multidimensional data set.
	 * @throws HDFConnectorException
	 */
	public String getDataTypeMDD( String datasetName) throws HDFConnectorException {
		String result = "no suggestion";
		try {
			dataset_id = H5.H5Dopen (file_id, datasetName);
			filetype_id = H5.H5Dget_type(dataset_id);
		    if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_FLOAT) {
		    	if (H5.H5Tget_size(filetype_id) == 4) {
		    		result = "Float";
		    	}
		    	else if (H5.H5Tget_size(filetype_id) == 8) {
		    		result = "Double";
		    	}
		    }
		    else if (H5.H5Tget_class(filetype_id) == HDF5Constants.H5T_INTEGER) {
		    	if (H5.H5Tget_size(filetype_id) == 1) {
		    		result = "Byte";
		    	}
		    	else if (H5.H5Tget_size(filetype_id) == 2) {
		    		result = "Short";
		    	}
		    	else if (H5.H5Tget_size(filetype_id) == 4) {
		    		result = "Integer";
		    	}
		    }
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot read " + datasetName, ex);
			throw new HDFConnectorException("Cannot read '" + datasetName + "': " + ex.getMessage());
		}
		finally {
			// End access to the data set and release resources used by it.
		    closeDataset();
		    closeFileType();
		}
		return result;
	}
	
	/**
	 * Check if data set is member of the HDF file.
	 * @throws HDFConnectorException
	 */
	public boolean isMember( String datasetName) throws HDFConnectorException {
		boolean result = true;
		try {
			dataset_id = H5.H5Dopen (file_id, datasetName);
		}
		catch (Exception ex) {
			result = false;
		}
		finally {
			closeDataset();
		}
		return result;
	}
		
	/**
	 * Drops data set.
	 */
	public void dropD(String datasetName) {
		try {
			H5.H5Gunlink(file_id, datasetName);
			logger.log(Level.CONFIG, "Successfully unlinked/droped" + datasetName);
		}
		// Data set does not exist.
		catch (HDF5LibraryException ex) {
			logger.log(Level.CONFIG, "Dataset currently not exist...");
		}
	}
	
	/**
	 * Close the data set.
	 * @throws HDFConnectorException
	 */
	private void closeDataset() throws HDFConnectorException {
		try {
			if (dataset_id >= 0){
				H5.H5Dclose(dataset_id);
				dataset_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the dataset", ex);
			throw new HDFConnectorException("Cannot close the dataset: " + ex.getMessage());
		}
	}
	
	/**
	 * Close the memory type.
	 * @throws HDFConnectorException
	 */
	private void closeMemType() throws HDFConnectorException {
		try {
			if (memtype_id >= 0) {
				H5.H5Tclose(memtype_id);
				memtype_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the memtype", ex);
			throw new HDFConnectorException("Cannot close the memtype: " + ex.getMessage());
		}
	}

	/**
	 * Close the file type.
	 * @throws HDFConnectorException
	 */
	private void closeFileType() throws HDFConnectorException {
		try {
			if (filetype_id >= 0) {
				H5.H5Tclose(filetype_id);
				filetype_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the filetype", ex);
			throw new HDFConnectorException("Cannot close the filetype: " + ex.getMessage());
		}
	}
	
	/**
	 * Close the file space.
	 * @throws HDFConnectorException
	 */
	private void closeFileSpace() throws HDFConnectorException {
		try {
			if (filespace_id >= 0) {
				H5.H5Sclose(filespace_id);
				filespace_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the file space", ex);
			throw new HDFConnectorException("Cannot close the file space: " + ex.getMessage());
		}
	}
	
	/**
	 * Close the memory space.
	 * @throws HDFConnectorException
	 */
	private void closeMemSpace() throws HDFConnectorException {
		try {
			if (memspace_id >= 0) {
				H5.H5Sclose(memspace_id);
				memspace_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the property list", ex);
			throw new HDFConnectorException("Cannot close the property list: " + ex.getMessage());
		}
	}
	
	/**
	 * Close the property list.
	 * @throws HDFConnectorException
	 */
	private void closePropertyList() throws HDFConnectorException {
		try {
			if (dcpl_id >= 0) {
				H5.H5Pclose(dcpl_id);
				dcpl_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the property list", ex);
			throw new HDFConnectorException("Cannot close the property list: " + ex.getMessage());
		}
	}
	
	/**
	 * Close the attribute
	 * @throws HDFConnectorException
	 */
	private void closeAttr() throws HDFConnectorException {
		try {
			if (attr_id >= 0) {
				H5.H5Aclose(attr_id);
				attr_id = -1;
			}
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Cannot close the attribut", ex);
			throw new HDFConnectorException("Cannot close the attribut: " + ex.getMessage());
		}
	}
	
	/**
	 * Nested class.
	 */
	public class HDFConnectorException extends Exception{
		/**
		 * For serializing the Class
		 */
		private static final long serialVersionUID = 5532262515086067648L;

		HDFConnectorException(String msg) {
			super("HDFConnectorException: " + msg);
		}
	}
	

}
