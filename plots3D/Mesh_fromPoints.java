/**
 * Mesh_fromPoints.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.Shape3D;


public class Mesh_fromPoints extends SpecialGraph
{
	/** Accepts a given data object and returns an appropriate Shape3D
	 * @author BLM*/
	public Shape3D getVisualization(DataSet data)
	{
		DelauneyTriangulator dt = new DelauneyTriangulator();
		return 	new Shape3D(dt.getIndexedTriangleArray(data),Tools.getAppearance_PolySurface());
	}
	
	public IndexedGeometryArray getIndexedTriangleArray(DataSet data)
	{
		DelauneyTriangulator dt = new DelauneyTriangulator();
		return 	dt.getIndexedTriangleArray(data);
	}
}

