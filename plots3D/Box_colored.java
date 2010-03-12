/**
 * Box.java
 *
 * @author BLM
 */

package plots3D;

import javax.media.j3d.*;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Box_colored implements Accessory
{
	private QuadArray TheQuadArray;
	private LineArray TheLineArray;
	private int numVert = 24;
	public float xLength;
	public float yLength;
	public float zLength;
	public Point3f Position;
	public Point3f[] ThePoints;
	public Color4f TheColor;
	private Appearance TheAppearance;
	
	public Box_colored(float xlen, float ylen, float height, Point3f position)
	{
		//init with unit size centered @ origin
		xLength = xlen;
		yLength = ylen;
		zLength = height;
		Position = position;
		initGeometryArrays();
	}
	
	public void initGeometryArrays()
	{
		TheQuadArray = new QuadArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_4 );
		Point3f[] p = new Point3f[8];
		ThePoints = p;
		
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
		
		TheQuadArray.setCoordinate(0, p[0]);
		TheQuadArray.setCoordinate(1, p[1]);
		TheQuadArray.setCoordinate(2, p[2]);
		TheQuadArray.setCoordinate(3, p[3]);
		
		TheQuadArray.setCoordinate(4, p[4]);
		TheQuadArray.setCoordinate(5, p[5]);
		TheQuadArray.setCoordinate(6, p[6]);
		TheQuadArray.setCoordinate(7, p[7]);
		
		TheQuadArray.setCoordinate(8, p[0]);
		TheQuadArray.setCoordinate(9, p[1]);
		TheQuadArray.setCoordinate(10, p[5]);
		TheQuadArray.setCoordinate(11, p[4]);
		
		TheQuadArray.setCoordinate(12, p[2]);
		TheQuadArray.setCoordinate(13, p[3]);
		TheQuadArray.setCoordinate(14, p[7]);
		TheQuadArray.setCoordinate(15, p[6]);
		
		TheQuadArray.setCoordinate(16, p[0]);
		TheQuadArray.setCoordinate(17, p[4]);
		TheQuadArray.setCoordinate(18, p[7]);
		TheQuadArray.setCoordinate(19, p[3]);
		
		TheQuadArray.setCoordinate(20, p[6]);
		TheQuadArray.setCoordinate(21, p[5]);
		TheQuadArray.setCoordinate(22, p[1]);
		TheQuadArray.setCoordinate(23, p[2]);
		
		//setting the color
		Color4f color =new Color4f((float)Math.random(),(float)Math.random(),(float)Math.random(), 1);
		for (int i = 0; i < numVert; i++)
			TheQuadArray.setColor(i,color);
		
		
		//creating the outlines of the box
		TheLineArray = new LineArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_4);
		
		float offset = 0.001f;
		Point3f[] pb = new Point3f[8];
		pb[0]  = new Point3f(xZero,yZero-offset,zZero-offset);
		pb[1] = new Point3f(xLen,yZero-offset,zZero-offset);
		pb[2] = new Point3f(xLen,yLen+offset,zZero-offset);
		pb[3] = new Point3f(xZero,yLen+offset,zZero-offset);
		
		pb[4]  = new Point3f(xZero,yZero-offset,zLen+offset);
		pb[5] = new Point3f(xLen,yZero-offset,zLen+offset);
		pb[6] = new Point3f(xLen,yLen+offset,zLen+offset);
		pb[7] = new Point3f(xZero,yLen+offset,zLen+offset);
		
		//left square
		TheLineArray.setCoordinate(0, pb[0]);
		TheLineArray.setCoordinate(1, pb[1]);
		TheLineArray.setCoordinate(2, pb[1]);
		TheLineArray.setCoordinate(3, pb[2]);
		TheLineArray.setCoordinate(4, pb[2]);
		TheLineArray.setCoordinate(5, pb[3]);
		TheLineArray.setCoordinate(6, pb[3]);
		TheLineArray.setCoordinate(7, pb[0]);
		//right square
		TheLineArray.setCoordinate(8, pb[4]);
		TheLineArray.setCoordinate(9, pb[5]);
		TheLineArray.setCoordinate(10, pb[5]);
		TheLineArray.setCoordinate(11, pb[6]);
		TheLineArray.setCoordinate(12, pb[6]);
		TheLineArray.setCoordinate(13, pb[7]);
		TheLineArray.setCoordinate(14, pb[7]);
		TheLineArray.setCoordinate(15, pb[4]);
		//connecting the squares
		TheLineArray.setCoordinate(16, pb[0]);
		TheLineArray.setCoordinate(17, pb[4]);
		TheLineArray.setCoordinate(18, pb[1]);
		TheLineArray.setCoordinate(19, pb[5]);
		TheLineArray.setCoordinate(20, pb[2]);
		TheLineArray.setCoordinate(21, pb[6]);
		TheLineArray.setCoordinate(22, pb[3]);
		TheLineArray.setCoordinate(23, pb[7]);
		
		//setting the line colors
		float grayVal = 0f;
		for (int i = 0; i < numVert; i++)
			TheLineArray.setColor(i,new Color4f(grayVal,grayVal,grayVal, 1));
	}
	
	public TransformGroup getVisualization(Appearance app)
	{
		TheAppearance = app;
		
		Transform3D trans = new Transform3D();
		//translate the box down to include half box in neg region and half in positive region
		trans.setTranslation(new Vector3f(0,0,0));
		TransformGroup tg = new TransformGroup(trans);
		//adding the planes/main box
		tg.addChild(new Shape3D(TheQuadArray, app));
		//adding the ouline box
		tg.addChild(new Shape3D(TheLineArray, app));
		
		return tg;
	}
	
	public Appearance getAppearance()
	{
		return TheAppearance;
	}
	
	
	public TransformGroup getVisualization()
	{
		return null;
	}
	
	
//	public Appearance getAppearance()
//	{
//		//allows for front and back rendering of the surface
//		Appearance app = new Appearance();
//
////		TransparencyAttributes trans = new TransparencyAttributes();
////		trans.setTransparencyMode(TransparencyAttributes.NICEST);
////		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
////		trans.setTransparency(0.1f);
////		app.setTransparencyAttributes(trans);
//
//		PolygonAttributes polyAttrib = new PolygonAttributes();
//		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
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
//	public Appearance getLineAppearance()
//	{
//		Appearance app = new Appearance();
//		PolygonAttributes polyAttrib = new PolygonAttributes();
//		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
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
//		LineAttributes la = new LineAttributes();
//		la.setLineWidth(2);
//		la.setLineAntialiasingEnable(false);
//		app.setLineAttributes(la);
//
//		PolygonAttributes pa = new PolygonAttributes();
//		pa.setPolygonMode(pa.POLYGON_LINE);
//		pa.setCullFace(pa.CULL_NONE);
//		app.setPolygonAttributes(pa);
//
//		return app;
//
//	}
//
//	public Appearance getAppearance_translucent()
//	{
//		//allows for front and back rendering of the surface
//		Appearance app = new Appearance();
//
//		TransparencyAttributes trans = new TransparencyAttributes();
//		trans.setTransparencyMode(TransparencyAttributes.NICEST);
//		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
//		trans.setTransparency(0.8f);
//		app.setTransparencyAttributes(trans);
//
//		PolygonAttributes polyAttrib = new PolygonAttributes();
//		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
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
	
	public void setColor(float red, float green, float blue, float alpha)
	{
		//setting the color
		Color4f color =new Color4f(red,green,blue, alpha);
		for (int i = 0; i < numVert; i++)
			TheQuadArray.setColor(i,color);
		TheColor = color;
	}
	
}

