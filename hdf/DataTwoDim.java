package hdf;

/**
 * 
 * @author Michael Menden
 *
 */
public interface DataTwoDim {
	public Object[][] getData();
	public Object getElem( int i, int j);
	public long[] getCounts();
	int getHDFType();
}
