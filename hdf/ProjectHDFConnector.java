package hdf;

import hdf.HDFConnector.HDFConnectorException;
import java.io.File;

/**
 *
 * @author Michael Menden
 *
 */
public class ProjectHDFConnector
{
	
	private String path;
	
	/**
	 * Constructor.
	 */
	public ProjectHDFConnector(String projPath)
	{
		path = projPath;
		new File(path).mkdir();
	}
	
	/**
	 * Create a new project if it does not allready exist.
	 * @throws HDFConnectorException
	 */
	public void createProject() throws HDFConnectorException
	{
		HDFConnector con = new HDFConnector();
		if (!con.existHDF5(path + "/project.h5"))
		{
			con.createHDF5(path + "/project.h5");
			con.openHDF5(path + "/project.h5");
			// dim0 = plate; dim1 = well; dim2 = field
			long chunk[] = { 1, 96, 4};
			con.createMDD("field_height", "Integer", chunk);
			con.writeAttr("field_height", "dataType", "H5T_NATIVE_INT");
			// dim0 = plate
			chunk = new long[]{1};
			con.createMDD("plate_size", "Integer", chunk);
			con.writeAttr("plate_size", "dataType", "H5T_NATIVE_INT");
			con.closeHDF5();
		}
	}
	
	/**
	 * Stores the size of a specific plate (necessary to store project structure).
	 * @throws HDFConnectorException
	 */
	public void writePlateSize( int plateIdx, int plateSize) throws HDFConnectorException
	{
		HDFConnector con = new HDFConnector();
		File f = new File(path + "/project.h5");
		if(!f.exists())
			createProject();
		
		con.openHDF5(path + "/project.h5");
		DataOneDim dataArray = new Data1D<Integer>(new Integer[] {plateSize});
		// Parameters: datasetName, data1D, dim0, offsets
		con.write1DD("plate_size", dataArray, 0, new long[]{plateIdx});
		con.closeHDF5();
	}
	
	/**
	 * Read the size of a specific plate (necessary to store project structure).
	 * @throws HDFConnectorException
	 */
	public int readPlateSize( int plateIdx) throws HDFConnectorException
	{
		HDFConnector con = new HDFConnector();
		
		File f = new File(path + "/project.h5");
		if(!f.exists())
			createProject();
		
		con.openHDF5(f.getAbsolutePath());
		DataOneDim plateSz = con.read1DD("plate_size", 0, new long[]{ plateIdx}, 1);
		Integer[] plateSize = (Integer[]) plateSz.getData();
		con.closeHDF5();
		return plateSize[0];
	}
	
	/**
	 * Stores the height of a specific field (necessary to convert from index2point).
	 * @throws HDFConnectorException
	 */
	public void writeFieldHeight( int plateIdx, int wellIdx, int fieldIdx, int height) throws HDFConnectorException
	{
		HDFConnector con = new HDFConnector();
		File f = new File(path + "/project.h5");
		if(!f.exists())
			createProject();
		
		con.openHDF5(path + "/project.h5");
		// Parameters: datasetName, data1D, dim0, offsets
		con.write1DD("field_height", new Data1D<Integer>(new Integer[] {height}),
					 2, new long[] { plateIdx, wellIdx, fieldIdx});
		con.closeHDF5();
	}
	
	/**
	 * Read the height of a specific field (necessary to convert from index2point).
	 * @throws HDFConnectorException
	 */
	public int readFieldHeight( int plateIdx, int wellIdx, int fieldIdx) throws HDFConnectorException
	{
		HDFConnector con = new HDFConnector();
		File f = new File(path + "/project.h5");
		if(!f.exists())
			createProject();
		
		con.openHDF5(path + "/project.h5");
		// parameter: datasetName, dim0, dim1, offsets, count0, count1
		DataTwoDim result = con.read2DD("field_height",
										1, 2,
										new long[]{ plateIdx, wellIdx, fieldIdx},
										1, 1);
		con.closeHDF5();
		return (Integer) result.getElem(0, 0);
	}
}
