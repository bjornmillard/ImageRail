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
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class Plane implements Accessory
{
	public float xLength;
	public float yLength;
	public Color3f color;
	public int XYZ = 2;
	static public int X = 0;
	static public int Y = 1;
	static public int Z = 2;
	public Point3f Position;
	
	public Plane(int xyz, float xLength_, float yLength_)
	{
		XYZ = xyz;
		//init with unit size centered @ origin
		xLength = xLength_;
		yLength = yLength_;
		color = new Color3f(0.3f,0.3f,0.3f);
		Position = new Point3f(0,0,0);
	}
	
	public TransformGroup getVisualization()
	{
		int numVert = 4;
		QuadArray qa = new QuadArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		
		Point3f[] p = new Point3f[4];
		float zOffset = 0f;
		
		if (XYZ == X)
		{
			p[0]  = new Point3f(Position.x,Position.y,Position.z-zOffset);
			p[1] = new Point3f(Position.x,Position.y+yLength,Position.z-zOffset);
			p[2] = new Point3f(Position.x,Position.y+yLength,Position.z+yLength-zOffset);
			p[3] = new Point3f(Position.x,Position.y,Position.z+yLength-zOffset);
		}
		else if (XYZ == Y)
		{
			p[0]  = new Point3f(Position.x,Position.y,Position.z-zOffset);
			p[1] = new Point3f(Position.x+xLength,Position.y,Position.z-zOffset);
			p[2] = new Point3f(Position.x+xLength,Position.y,Position.z+yLength-zOffset);
			p[3] = new Point3f(Position.x,Position.y,Position.z+yLength-zOffset);
		}
		else if (XYZ == Z)
		{
			p[0]  = new Point3f(Position.x,Position.y,Position.z);
			p[1] = new Point3f(Position.x+xLength,Position.y,Position.z);
			p[2] = new Point3f(Position.x+xLength,Position.y+yLength,Position.z);
			p[3] = new Point3f(Position.x,Position.y+yLength,Position.z);
		}
		
		//left square
		qa.setCoordinate(0, p[0]);
		qa.setCoordinate(1, p[1]);
		qa.setCoordinate(2, p[2]);
		qa.setCoordinate(3, p[3]);
		
		
		for (int i = 0; i < numVert; i++)
			qa.setColor(i,color);
		
		Transform3D trans = new Transform3D();
//		trans.setTranslation(new Vector3f(0,0,-0.5f));
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(new Shape3D(qa, getAppearance()));
		
		return tg;
	}
	
	public Appearance getAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		TransparencyAttributes trans = new TransparencyAttributes();
		trans.setTransparencyMode(TransparencyAttributes.NICEST);
		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
		trans.setTransparency(0.85f);
		app.setTransparencyAttributes(trans);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
		
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

