/**
 * TriangleArrayFromMatrix.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

import javax.media.j3d.*;

import java.util.Hashtable;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class TriangleArrayFromMatrix
{
	
	
//	public Shape3D initTriangleArray(Point3f[][] points)
//	{
//		int height  = points.length;
//		int width = points[0].length;
//
//		j3d.Tools.normalizePoints(points);
//
//
//		int numTriangles = (2*width-2)*(height-1);
//		IndexedTriangleArray TheTriangleArray = new IndexedTriangleArray(height*width, GeometryArray.COORDINATES | GeometryArray.COLOR_3 , numTriangles*3);
////		TheTriangleArray.setCapability(IndexedTria ngleArray.ALLOW_COORDINATE_WRITE);
////		TheTriangleArray.setCapability(IndexedTriangleArray.ALLOW_COLOR_WRITE);
//
//		//setting up all the coordinates
//		Hashtable hash = new Hashtable(width*height);
//		float[] colorTemp = new float[3];
//		int counter =0;
//		for (int r= 0; r < height; r++)
//			for (int c= 0; c < width; c++)
//			{
//				TheTriangleArray.setCoordinate(counter, points[r][c]);
//				colormaps.ColorMaps.getColorValue(points[r][c].z,  colorTemp, colormaps.ColorMaps.COLORMAP_STANDARD);
//				TheTriangleArray.setColor(counter,colorTemp);
//				hash.put(r+","+c, new Integer(counter));
//				counter++;
//			}
//
//
//		//now linking all the coords to create triangles that share vertices
//		counter = 0;
//		int vertexCounter =0;
//		for (int r= 0; r < height-1; r++)
//			for (int c= 0; c < width-1; c++)
//			{
//
//				if (r%2==0)
//				{
//					//making 2 triangles at a time
//
//					Integer ind = (Integer)hash.get(r+","+c);
//					int index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+c);
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//
//
//					ind = (Integer)hash.get(r+","+c);
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get(r+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//				}
//				else
//				{
//					Integer ind = (Integer)hash.get(r+","+c);
//					int index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get(r+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+(c));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					//making 2 triangles at a time
//					ind = (Integer)hash.get(r+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+(c+1));
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//
//					ind = (Integer)hash.get((r+1)+","+c);
//					index= ind.intValue();
//					TheTriangleArray.setCoordinateIndex(counter, index);
//					TheTriangleArray.setColorIndex(counter,index);
//					counter++;
//				}
//				vertexCounter++;
//			}
//
//		return new Shape3D(TheTriangleArray, getAppearance());
//	}
//
//	public Appearance getAppearance()
//	{
//		//allows for front and back rendering of the surface
//		Appearance app = new Appearance();
//
////		TransparencyAttributes trans = new TransparencyAttributes();
////		trans.setTransparencyMode(TransparencyAttributes.FASTEST);
////		trans.setSrcBlendFunction(TransparencyAttributes.SCREEN_DOOR);
////		trans.setTransparency(0.7f);
////		app.setTransparencyAttributes(trans);
//
//		PolygonAttributes polyAttrib = new PolygonAttributes();
//		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
////		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
////		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
//
//		polyAttrib.setBackFaceNormalFlip(true);
//		app.setPolygonAttributes(polyAttrib);
//
//		Material mat = new Material();
//		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
//		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
//		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
//		app.setMaterial(mat);
//
//		return app;
//	}
//
	
	
}

