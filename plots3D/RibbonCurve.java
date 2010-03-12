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

public class RibbonCurve implements Accessory
{
	private QuadArray TheQuadArray;
	private LineArray TheLineArray;
	private int numVert;
	
	public Point3f Position;
	public int NumBoxes;
	public Box_colored[] Series;
	public int TYPE;
	public final int XRIBBON = 0;
	public final int YRIBBON = 1;
	private Appearance TheAppearance;
	
	public RibbonCurve(Box_colored[] series)
	{
		Series = series;
		NumBoxes = series.length;
		numVert = NumBoxes*4*2;
		
		
		initGeometryArrays();
	}
	
	public void initGeometryArrays()
	{
		TheQuadArray = new QuadArray(numVert, GeometryArray.COORDINATES | GeometryArray.COLOR_4 );
		int numLineVerts = NumBoxes*8;
		TheLineArray = new LineArray(numLineVerts, GeometryArray.COORDINATES | GeometryArray.COLOR_4);
		
		int counterL = 0;
		int counter = 0;
		for (int i = 0; i < NumBoxes-1; i++)
		{
			Box_colored b = Series[i];
			
			Box_colored bNext = Series[i+1];
			Color4f color = b.TheColor;
			Color4f color2 = bNext.TheColor;
			
			Point3f point11 = b.ThePoints[4];
			Point3f point12 = b.ThePoints[5];
			Point3f point21 = bNext.ThePoints[4];
			Point3f point22 = bNext.ThePoints[5];
			float yH1 = (point11.y+point12.y)/2f;
			float yH2 = (point21.y+point22.y)/2f;
			
			Point3f point = b.ThePoints[7];
			TheQuadArray.setCoordinate(counter, new Point3f(point.x, yH1,  (point.z+0.001f)));
			TheQuadArray.setColor(counter,color);
			counter++;
			
			point = b.ThePoints[5];
			TheQuadArray.setCoordinate(counter, new Point3f(point.x,yH1,   (point.z+0.001f)));
			TheQuadArray.setColor(counter,color);
			counter++;
			
			point = bNext.ThePoints[5];
			TheQuadArray.setCoordinate(counter, new Point3f(point.x, yH2, (point.z+0.001f)));
			TheQuadArray.setColor(counter,color2);
			counter++;
			
			point = bNext.ThePoints[7];
			TheQuadArray.setCoordinate(counter, new Point3f(point.x, yH2,   (point.z+0.001f)));
			TheQuadArray.setColor(counter,color2);
			counter++;
			
			
			//LineOutline
			point = b.ThePoints[4];
			TheLineArray.setCoordinate(counterL, new Point3f(point.x,yH1, (point.z+0.002f)));
			counterL++;
			point = bNext.ThePoints[4];
			TheLineArray.setCoordinate(counterL, new Point3f(point.x, yH2, (point.z+0.002f)));
			counterL++;
			
			point = b.ThePoints[7];
			TheLineArray.setCoordinate(counterL, new Point3f(point.x,yH1, (point.z+0.002f)));
			counterL++;
			point = bNext.ThePoints[7];
			TheLineArray.setCoordinate(counterL, new Point3f(point.x, yH2, (point.z+0.002f)));
			counterL++;
		}
		
		
		//setting the line colors
		float grayVal = 0f;
		for (int i = 0; i < numLineVerts; i++)
			TheLineArray.setColor(i,new Color4f(grayVal,grayVal,grayVal,1));
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
	
	public TransformGroup getVisualization()
	{
		return null;
//		Transform3D trans = new Transform3D();
//		//translate the box down to include half box in neg region and half in positive region
//		trans.setTranslation(new Vector3f(0,0,0));
//		TransformGroup tg = new TransformGroup(trans);
//		//adding the planes/main box
//		tg.addChild(new Shape3D(TheQuadArray, initAppearance()));
//		//adding the ouline box
//		tg.addChild(new Shape3D(TheLineArray, getLineAppearance()));
//
//		return tg;
	}
	
	public Appearance getAppearance()
	{
		return TheAppearance;
	}
//
//	public Appearance initAppearance()
//	{
//		//allows for front and back rendering of the surface
//		TheAppearance = new Appearance();
//
////		TransparencyAttributes trans = new TransparencyAttributes();
////		trans.setTransparencyMode(TransparencyAttributes.NICEST);
////		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
////		trans.setTransparency(0.4f);
////		app.setTransparencyAttributes(trans);
//
//		TheAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
//		TheAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
//		RenderingAttributes render = new RenderingAttributes();
//		TheAppearance.setRenderingAttributes(render);
//		render.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
//
//		PolygonAttributes polyAttrib = new PolygonAttributes();
//		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
//
//		polyAttrib.setBackFaceNormalFlip(true);
//		TheAppearance.setPolygonAttributes(polyAttrib);
//
//		Material mat = new Material();
//		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
//		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
//		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
//		TheAppearance.setMaterial(mat);
//
//		return TheAppearance;
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
	}
	
	
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
	
}

