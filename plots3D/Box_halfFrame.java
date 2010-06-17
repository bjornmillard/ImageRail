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
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Box_halfFrame implements Accessory
{
	public float xLength;
	public float yLength;
	public float zLength;
	public Point3f Position;
	public int numVert;
	private LineArray la;
	private Appearance TheAppearance;
	
	public Box_halfFrame()
	{
		//init with unit size centered @ origin
		xLength = 1;
		yLength = 1;
		zLength = 1;
		Position = new Point3f(0,0,0);
		
	}
	
	public TransformGroup getVisualization()
	{
		return null;
	}
	
	public TransformGroup getVisualization(Appearance theAppearance)
	{
		TheAppearance = theAppearance;
		int numBaseStructs = 5;
		int numZlines1 = 30;
		int numZlines2 = 30;
		int numFinalLines = 6;
		numVert = numBaseStructs+numZlines1+numZlines2+numFinalLines;
		la = new LineArray(2*numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		la.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		
		Point3f[] p = new Point3f[numVert];
		
		float xZero = Position.x;
		float xLen = xZero+xLength;
		float yZero = Position.y;
		float yLen = yZero+yLength;
		float zZero = Position.z;
		float zLen = zZero+zLength;
		
		//bottom square
		p[0]  = new Point3f(xZero,yZero,zZero);
		p[1] = new Point3f(xLen,yZero,zZero);
		p[2] = new Point3f(xLen,yLen,zZero);
		p[3] = new Point3f(xZero,yLen,zZero);
		//top vertical line point
		p[4]  = new Point3f(xZero,yZero,zLen);
		
		int counter = numBaseStructs;
		//the z-line points
		for (int i = 0; i < numZlines1; i=i+2)
		{
			p[counter] = new Point3f(xZero, yZero+yLen, zZero+((float)i/(float)numZlines1));
			counter++;
			p[counter] = new Point3f(xZero+xLen, yZero+yLen, zZero+((float)i/(float)numZlines1));
			counter++;
		}
		
		counter = numZlines1+numBaseStructs;
		//the z-line points
		for (int i = 0; i < numZlines2; i=i+2)
		{
			p[counter] = new Point3f(xZero, yZero, zZero+((float)i/(float)(numZlines2)));
			counter++;
			p[counter] = new Point3f(xZero, yZero+yLen, zZero+((float)i/(float)numZlines2));
			counter++;
		}
		//final lines to clean up
		p[counter] = new Point3f(xZero, yZero, zZero+zLen);
		counter++;
		p[counter] = new Point3f(xZero, yZero+yLen, zZero+zLen);
		counter++;
		
		p[counter] = new Point3f(xZero, yZero+yLen, zZero+zLen);
		counter++;
		p[counter] = new Point3f(xZero+xLen, yZero+yLen, zZero+zLen);
		counter++;
		
		p[counter] = new Point3f(xZero+xLen, yZero+yLen, zZero);
		counter++;
		p[counter] = new Point3f(xZero+xLen, yZero+yLen, zZero+zLen);
		counter++;
		
		//Bottom square
		la.setCoordinate(0, p[0]);
		la.setCoordinate(1, p[1]);
		la.setCoordinate(2, p[1]);
		la.setCoordinate(3, p[2]);
		la.setCoordinate(4, p[2]);
		la.setCoordinate(5, p[3]);
		la.setCoordinate(6, p[3]);
		la.setCoordinate(7, p[0]);
		
		la.setCoordinate(8, p[0]);
		la.setCoordinate(9, p[4]);
		
		
		//the z-line points
		counter = 2*numBaseStructs;
		for (int i = 0; i < ((float)numZlines1); i=i+2)
		{
			la.setCoordinate(counter, p[numBaseStructs+i]);
			counter++;
			la.setCoordinate(counter, p[numBaseStructs+i+1]);
			counter++;
		}
		//the z-line points
		counter = 2*(numBaseStructs+numZlines1);
		for (int i = 0; i < ((float)numZlines2); i=i+2)
		{
			la.setCoordinate(counter, p[numBaseStructs+numZlines1+i]);
			counter++;
			la.setCoordinate(counter, p[numBaseStructs+numZlines1+i+1]);
			counter++;
		}
		
		//final 3 lines
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2]);
		counter++;
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2+1]);
		counter++;
		
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2+2]);
		counter++;
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2+3]);
		counter++;
		
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2+4]);
		counter++;
		la.setCoordinate(counter, p[numBaseStructs+numZlines1+numZlines2+5]);
		counter++;
		
		
		float grayVal = 0.6f;
		for (int i = 0; i < 2*numVert; i++)
			la.setColor(i,new Color3f(grayVal,grayVal,grayVal));
		
		Transform3D trans = new Transform3D();
		//translate the box down to include half box in neg region and half in positive region
		trans.setTranslation(new Vector3f(0,0,0));
		TransformGroup tg = new TransformGroup(trans);
		Shape3D shape = new Shape3D(la, theAppearance);
		tg.addChild(shape);
		
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		
		return tg;
	}
	
	public void setColor(Color3f color)
	{
		
		for (int i = 0; i < 2*numVert; i++)
			la.setColor(i,color);
	}
	
	
	public Appearance getAppearance()
	{
		return TheAppearance;
	}
}
