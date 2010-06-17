/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */


package plots3D;
import java.util.Hashtable;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;



public class Cube_Quad extends SpecialGraph
{
	
	/** Accepts a given data object and returns an appropriate Shape3D
	 * @author BLM*/
	public Shape3D getVisualization(Object data)
	{
		double[][][] matrix = (double[][][])data;
		int x_len = matrix.length;
		int y_len = matrix[0].length;
		int z_len = matrix[0][0].length;
		
		float spatialScale = 200f;
		int resolution = 4;
		
		int numS = 0;
		for (int x = 0; x < y_len; x+=resolution)
			for (int y = 0; y < y_len; y+=resolution)
				for (int z = 0; z < z_len; z+=resolution)
					numS++;
		
		
		//converting the graphics objects based on matrix coords
		IndexedQuadArray Gi = new IndexedQuadArray(numS, GeometryArray.COORDINATES | GeometryArray.COLOR_3, (numS*4)*2);
	
		//indexing all hte pixel points
		double[] coords = new double[3];
		int counter = 0;
		Hashtable hash = new Hashtable(numS);
		
		
		//init the hash table that allows easy access to the points and their absoluteIndexNumber --> coordinate(xyz)
		// ... and most import ... creating the points and their colors
		for (int x = 0; x < x_len; x+=resolution)
			for (int y = 0; y < y_len; y+=resolution)
				for (int z = 0; z < z_len; z+=resolution)
				{
					coords = new double[3];
					coords[0] = (float)x/spatialScale;
					coords[1] = (float)y/spatialScale;
					coords[2] = (float)z/spatialScale;;
					
					Gi.setCoordinate(counter, coords);
					hash.put(x+","+y+","+z, new Integer(counter));
					
					Gi.setColor(counter, new Color3f((float)matrix[x][y][z], 0, 0));
					counter++;
				}
		
		counter =0 ;
		
		//now connecting all the points
		for (int x = 0; x < x_len-(resolution); x+=resolution)
			for (int y = 0; y < y_len-(resolution); y+=resolution)
				for (int z = 0; z < z_len-(resolution); z+=resolution)
				{
					//horizontalIndex
					Integer ind = (Integer)hash.get(x+","+y+","+z);
					int index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get((x+resolution)+","+y+","+z);
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get((x+resolution)+","+(y+resolution)+","+z);
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get(x+","+(y+resolution)+","+z);
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					//vertical panels
					ind = (Integer)hash.get(x+","+y+","+z);
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get((x+resolution)+","+y+","+z);
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get((x+resolution)+","+y+","+(z+resolution));
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
					
					ind = (Integer)hash.get(x+","+y+","+(z+resolution));
					index= ind.intValue();
					Gi.setCoordinateIndex(counter, index);
					Gi.setColorIndex(counter,index);
					counter++;
				}
		
		
		return new Shape3D(Gi, getAppearance());
	}
	
	
	
}

