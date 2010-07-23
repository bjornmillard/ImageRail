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
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Box implements Accessory
{
	public float xLength;
	public float yLength;
	public float zLength;
	public Point3f Position;
	
	public Box()
	{
		//init with unit size centered @ origin
		xLength = 1;
		yLength = 1;
		zLength = 1;
		Position = new Point3f(0,0,0);
		
	}
	
	public TransformGroup getVisualization()
	{
		int numVert = 24;
		LineArray la = new LineArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		Point3f[] p = new Point3f[8];
		
		float xZero = Position.x;
		float xLen = xZero+xLength;
		float yZero = Position.y;
		float yLen = yZero+yLength;
		float zZero = Position.z;
		float zLen = zZero+zLength;
		
		p[0]  = new Point3f(xZero,yZero,zZero);
		p[1] = new Point3f(xLen,yZero,zZero);
		p[2] = new Point3f(xLen,yLen,zZero);
		p[3] = new Point3f(xZero,yLen,zZero);
		
		p[4]  = new Point3f(xZero,yZero,zLen);
		p[5] = new Point3f(xLen,yZero,zLen);
		p[6] = new Point3f(xLen,yLen,zLen);
		p[7] = new Point3f(xZero,yLen,zLen);
		
		//left square
		la.setCoordinate(0, p[0]);
		la.setCoordinate(1, p[1]);
		la.setCoordinate(2, p[1]);
		la.setCoordinate(3, p[2]);
		la.setCoordinate(4, p[2]);
		la.setCoordinate(5, p[3]);
		la.setCoordinate(6, p[3]);
		la.setCoordinate(7, p[0]);
		//right square
		la.setCoordinate(8, p[4]);
		la.setCoordinate(9, p[5]);
		la.setCoordinate(10, p[5]);
		la.setCoordinate(11, p[6]);
		la.setCoordinate(12, p[6]);
		la.setCoordinate(13, p[7]);
		la.setCoordinate(14, p[7]);
		la.setCoordinate(15, p[4]);
		//connecting the squares
		la.setCoordinate(16, p[0]);
		la.setCoordinate(17, p[4]);
		la.setCoordinate(18, p[1]);
		la.setCoordinate(19, p[5]);
		la.setCoordinate(20, p[2]);
		la.setCoordinate(21, p[6]);
		la.setCoordinate(22, p[3]);
		la.setCoordinate(23, p[7]);
		
		float grayVal = 0.5f;
		for (int i = 0; i < numVert; i++)
			la.setColor(i,new Color3f(grayVal,grayVal,grayVal));
		
		Transform3D trans = new Transform3D();
		//translate the box down to include half box in neg region and half in positive region
		trans.setTranslation(new Vector3f(0,0,-0.5f));
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(new Shape3D(la, getAppearance()));
		
		return tg;
	}
	
	public Appearance getAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		
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
}

