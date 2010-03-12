/**
 * MatrixPlotter_2D.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

import javax.media.j3d.*;

import java.util.Hashtable;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class MatrixPlotter_2D extends SpecialGraph
{
	/** Accepts a 2D matrix of values and plots a terrain of it
	 * @author BLM*/
	public Shape3D getVisualization(Object data)
	{
		double[][] matrix = (double[][])data;
		int width = matrix[0].length;
		int height = matrix.length;
		float spatialScale = 100f;
		int resolution = 1;
		
		int numS = 0;
		for (int r = 0; r < height; r+=resolution)
			for (int c = 0; c < width; c+=resolution)
				numS++;
		
		
		//converting the graphics objects based on matrix coords
		IndexedQuadArray Gi = new IndexedQuadArray(numS, GeometryArray.COORDINATES | GeometryArray.COLOR_3, numS*4);
		Gi.setCapability(IndexedQuadArray.ALLOW_COORDINATE_WRITE);
		Gi.setCapability(IndexedQuadArray.ALLOW_COLOR_WRITE);
		
		
		
		//indexing all hte pixel points
		double[] coords = new double[3];
		int counter = 0;
		Hashtable hash = new Hashtable(numS);
		
		
		for (int r = 0; r < height; r+=resolution)
			for (int c = 0; c < width; c+=resolution)
			{
				
				coords = new double[3];
				coords[0] = (float)r/spatialScale;
				coords[1] = (float)c/spatialScale;
				coords[2] = matrix[r][c];
				
				Gi.setCoordinate(counter, coords);
				hash.put(r+","+c, new Integer(counter));
				
				Gi.setColor(counter, new Color3f((float)matrix[r][c], (float)Math.random(), (float)(256f*Math.exp(-(matrix[r][c]/256f)))));
				counter++;
			}
		
		counter =0 ;
		
		for (int r = 0; r < height-(resolution); r+=resolution)
			for (int c = 0; c < width-(resolution); c+=resolution)
			{
				Integer ind = (Integer)hash.get(r+","+c);
				int index= ind.intValue();
				Gi.setCoordinateIndex(counter, index);
				Gi.setColorIndex(counter,index);
				counter++;
				
				ind = (Integer)hash.get((r+resolution)+","+c);
				index= ind.intValue();
				Gi.setCoordinateIndex(counter, index);
				Gi.setColorIndex(counter,index);
				counter++;
				
				ind = (Integer)hash.get((r+resolution)+","+(c+resolution));
				index= ind.intValue();
				Gi.setCoordinateIndex(counter, index);
				Gi.setColorIndex(counter,index);
				counter++;
				
				ind = (Integer)hash.get(r+","+(c+resolution));
				index= ind.intValue();
				Gi.setCoordinateIndex(counter, index);
				Gi.setColorIndex(counter,index);
				counter++;
			}
		

		return new Shape3D(Gi, getAppearance());
	}
	
}

