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

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Fifty_marker implements Accessory
{
	private QuadArray TheQuadArray;
	private LineArray TheLineArray;
	private int numVert;
	
	public Point3f Position;
	public int NumBoxes;
	public Box_colored[][] Data;
	public int TYPE;
	public final int XRIBBON = 0;
	public final int YRIBBON = 1;
	public double[] IC50;
	public int numSeries;
	
	
	public Fifty_marker(Box_colored[][] data, double[] ic50)
	{
		IC50 = ic50;
		Data = data;
		
		numSeries = data[0].length;
		
		
		numVert = (numSeries-1)*2+2;
		
		
		
		initGeometryArrays();
	}
	
	public void initGeometryArrays()
	{
		TheQuadArray = new QuadArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_3 );
//		int numLineVerts = NumBoxes*8;
//		TheLineArray = new LineArray(numLineVerts, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		
	
		
		
		int counterL = 0;
		int counter = 0;
		for (int r = 0; r < numSeries-1; r++)
		{
			//IC50 value
			double IC_this = IC50[r];
			double IC_next = IC50[r+1];
			System.out.println("********");
			System.out.println("this: "+IC_this);
			System.out.println("next: "+IC_next);
			
			int ind_start = (int)IC_this;
			int int_end = (int)IC_next;
			
			
//			Box_colored b = Data[r];
//			//Top of this box
//			Point3f point = b.ThePoints[4];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y,(point.z+0.001f)));
//			counter++;
			
			
//
//			point = b.ThePoints[7];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			point = b.ThePoints[6];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			point = b.ThePoints[5];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			//LineOutline
//			point = b.ThePoints[4];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//			point = b.ThePoints[5];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//
//			point = b.ThePoints[6];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//			point = b.ThePoints[7];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//
//
//			//Interconnecting to next box
//			Box_colored bNext = Data[r+1];
//			point = b.ThePoints[6];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			point = b.ThePoints[5];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			point = bNext.ThePoints[4];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			point = bNext.ThePoints[7];
//			TheQuadArray.setCoordinate(counter, new Point3f(point.x, point.y, (point.z+0.001f)));
//			counter++;
//
//			//LineOutline
//			point = b.ThePoints[6];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//			point = bNext.ThePoints[7];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//
//			point = b.ThePoints[5];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
//			point = bNext.ThePoints[4];
//			TheLineArray.setCoordinate(counterL, new Point3f(point.x, point.y, (point.z+0.002f)));
//			counterL++;
			
		}
		
		
		//setting the color
		Color3f color =new Color3f(0f,0f,0f);
		for (int i = 0; i < numVert; i++)
			TheQuadArray.setColor(i,color);
		
		
	}
	
	public TransformGroup getVisualization()
	{
		Transform3D trans = new Transform3D();
		//translate the box down to include half box in neg region and half in positive region
		trans.setTranslation(new Vector3f(0,0,0));
		TransformGroup tg = new TransformGroup(trans);
		//adding the planes/main box
		tg.addChild(new Shape3D(TheQuadArray, getAppearance()));
		//adding the ouline box
//		tg.addChild(new Shape3D(TheLineArray, getAppearance_translucent()));
		
		return tg;
	}
	
	public Appearance getAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		TransparencyAttributes trans = new TransparencyAttributes();
		trans.setTransparencyMode(TransparencyAttributes.NICEST);
		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
		trans.setTransparency(0.4f);
		app.setTransparencyAttributes(trans);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		app.setMaterial(mat);
		
		return app;
	}
	
	public Appearance getAppearance_translucent()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		TransparencyAttributes trans = new TransparencyAttributes();
		trans.setTransparencyMode(TransparencyAttributes.NICEST);
		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
		trans.setTransparency(0.8f);
		app.setTransparencyAttributes(trans);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		app.setMaterial(mat);
		
		return app;
	}
	
	public void setColor(float red, float green, float blue)
	{
		//setting the color
		Color3f color =new Color3f(red,green,blue);
		for (int i = 0; i < numVert; i++)
			TheQuadArray.setColor(i,color);
	}
	
}

