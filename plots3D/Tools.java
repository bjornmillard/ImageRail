/**
 * j3d.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;

public class Tools
{
	//  Well known colors, positions, and directions
	public final static Color3f White    = new Color3f( 1.0f, 1.0f, 1.0f );
	public final static Color3f Gray     = new Color3f( 0.7f, 0.7f, 0.7f );
	public final static Color3f DarkGray = new Color3f( 0.2f, 0.2f, 0.2f );
	public final static Color3f Black    = new Color3f( 0.0f, 0.0f, 0.0f );
	public final static Color3f Red      = new Color3f( 1.0f, 0.0f, 0.0f );
	public final static Color3f DarkRed  = new Color3f( 0.3f, 0.0f, 0.0f );
	public final static Color3f Yellow   = new Color3f( 1.0f, 1.0f, 0.0f );
	public final static Color3f DarkYellow=new Color3f( 0.3f, 0.3f, 0.0f );
	public final static Color3f Green    = new Color3f( 0.0f, 1.0f, 0.0f );
	public final static Color3f DarkGreen= new Color3f( 0.0f, 0.3f, 0.0f );
	public final static Color3f Cyan     = new Color3f( 0.0f, 1.0f, 1.0f );
	public final static Color3f Blue     = new Color3f( 0.0f, 0.0f, 1.0f );
	public final static Color3f DarkBlue = new Color3f( 0.0f, 0.0f, 0.3f );
	public final static Color3f Magenta  = new Color3f( 1.0f, 0.0f, 1.0f );
	
	public final static Vector3f PosX = new Vector3f(  1.0f,  0.0f,  0.0f );
	public final static Vector3f NegX = new Vector3f( -1.0f,  0.0f,  0.0f );
	public final static Vector3f PosY = new Vector3f(  0.0f,  1.0f,  0.0f );
	public final static Vector3f NegY = new Vector3f(  0.0f, -1.0f,  0.0f );
	public final static Vector3f PosZ = new Vector3f(  0.0f,  0.0f,  1.0f );
	public final static Vector3f NegZ = new Vector3f(  0.0f,  0.0f, -1.0f );
	public final static Appearance RedAppearance = getAppearanceOfColor(Red);
	public final static Appearance BlueAppearance = getAppearanceOfColor(Blue);
	
	
	public static double getMagnitude(Vector3d vect)
	{
		double sum = vect.x;
		sum+=vect.y;
		sum+=vect.z;
		return Math.sqrt(sum*sum);
	}
	
	/** Allows open access to this group*/
	public static void allBranchGroupCapabilities(Group g)
	{
		g.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		g.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		g.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		g.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	/** Adds fog to this group*/
	public static void addFogToGroup(Group g)
	{
		ExponentialFog myFog = new ExponentialFog( );
		myFog.setColor( new Color3f( 1.0f, 1.0f, 1.0f ) );
		myFog.setDensity( 1.0f );
		BoundingSphere myBounds = new BoundingSphere(new Point3d( ), 1000.0 );
		myFog.setInfluencingBounds( myBounds );
		g.addChild(myFog);
	}
	
	/** Allows open access to this group*/
	public static void allTransformGroupCapabilities(Group g)
	{
		g.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		g.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	}
	
	/** Add key controls to this branch group*/
	public static void addKeyControlsToBranchGroup(BranchGroup bg, SimpleUniverse su)
	{
		//Key control
		TransformGroup vpTrans = su.getViewingPlatform().getViewPlatformTransform();
		KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(vpTrans);
		keyNavBeh.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000));
		bg.addChild(keyNavBeh);
	}
	
	/** Add mouse controls to this transform group*/
	public static void addMouseControlsToBranchGroup(TransformGroup tg)
	{
		//Mouse control
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		
		MouseRotate myMouseRotate = new MouseRotate();
		myMouseRotate.setTransformGroup(tg);
		myMouseRotate.setSchedulingBounds(new BoundingSphere());
		tg.addChild(myMouseRotate);
		
		MouseTranslate myMouseTranslate = new MouseTranslate();
		myMouseTranslate.setTransformGroup(tg);
		myMouseTranslate.setSchedulingBounds(new BoundingSphere());
		tg.addChild(myMouseTranslate);
		
		MouseZoom myMouseZoom = new MouseZoom();
		myMouseZoom.setTransformGroup(tg);
		myMouseZoom.setSchedulingBounds(new BoundingSphere());
		tg.addChild(myMouseZoom);
		
	}
	
	public static void addDirectionalLightToBranchGroup(BranchGroup bg)
	{
		// Create a red light that shines for 100m from the origin
		Color3f light1Color = new Color3f(1f, 1f, 1f);
		BoundingSphere bounds =
			new BoundingSphere(new Point3d(0.0,0.0,0.0), 2000.0);
		Vector3f light1Direction  = new Vector3f(4.0f, -7.0f, -12.0f);
		DirectionalLight light1
			= new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);
		bg.addChild(light1);
	}
	
	public static void addAmbientLightToBranchGroup(BranchGroup bg)
	{
		Color3f light1Color = new Color3f(1f, 1f, 1f);
		BoundingSphere bounds =new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
		AmbientLight light1 = new AmbientLight(light1Color);
		light1.setEnable(true);
		light1.setInfluencingBounds(bounds);
		bg.addChild(light1);
	}
	
	public static void addBackground(BranchGroup bg, JPanel panel)
	{
		//		// Create application bounds
		BoundingSphere worldBounds = new BoundingSphere(
			new Point3d( 0.0, 0.0, 0.0 ),  // Center
			1000.0 );                      // Extent
		
		// Set the background color and its application bounds
		Background backG = new Background();
		
		backG.setCapability(Background.ALLOW_IMAGE_WRITE);
		backG.setApplicationBounds(worldBounds);
//		texImage = new TextureLoader("starscape.jpg", this).getTexture();
//		backG.setColor(MainFrame.White);
		ImageComponent2D myImage = new TextureLoader("blueThing.jpg", panel).getImage( );
		backG.setImage(myImage);
		bg.addChild(backG);
		
	}
	
	public static Appearance getRedAppearance()
	{
		Appearance app = new Appearance( );
		ColoringAttributes myCA = new ColoringAttributes( );
		myCA.setColor( 1.0f, 0.0f, 0.0f );
		myCA.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
		app.setColoringAttributes( myCA );
		
		Material myMat = new Material();
		myMat.setAmbientColor(  0.3f, 0.3f, 0.3f );
		myMat.setDiffuseColor(  1.0f, 0.0f, 0.0f );
		myMat.setEmissiveColor( 0.0f, 0.0f, 0.0f );
		myMat.setSpecularColor( 1.0f, 1.0f, 1.0f );
		myMat.setShininess( 64.0f );
		myMat.setLightingEnable(true);
		app.setMaterial( myMat );
		
//		TransparencyAttributes myTA = new TransparencyAttributes();
//		myTA.setTransparency( 0.0f );
//		myTA.setTransparencyMode(TransparencyAttributes.BLEND_ONE);
//		app.setTransparencyAttributes(myTA);
		
		return app;
	}
	
	public static Appearance getBlueAppearance()
	{
		Appearance app = new Appearance( );
		ColoringAttributes myCA = new ColoringAttributes( );
		myCA.setColor( 0.0f, 0.0f, 1.0f );
		myCA.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
		app.setColoringAttributes( myCA );
		
		Material myMat = new Material();
		myMat.setAmbientColor(  0.3f, 0.3f, 0.3f );
		myMat.setDiffuseColor(  0.0f, 0.0f, 1.0f );
		myMat.setEmissiveColor( 0.0f, 0.0f, 0.0f );
		myMat.setSpecularColor( 1.0f, 1.0f, 1.0f );
		myMat.setShininess( 64.0f );
		myMat.setLightingEnable(true);
		app.setMaterial( myMat );
		
//		TransparencyAttributes myTA = new TransparencyAttributes();
//		myTA.setTransparency( 0.0f );
//		myTA.setTransparencyMode(TransparencyAttributes.BLEND_ONE);
//		app.setTransparencyAttributes(myTA);
		
		return app;
	}
	
	public static Appearance getAppearanceOfColor(Color3f color)
	{
		Appearance app = new Appearance( );
		ColoringAttributes myCA = new ColoringAttributes( );
		myCA.setColor( color );
		myCA.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
		app.setColoringAttributes( myCA );
		
		Material myMat = new Material();
		myMat.setAmbientColor(  0.3f, 0.3f, 0.3f );
		myMat.setDiffuseColor(color);
		myMat.setEmissiveColor( 0.0f, 0.0f, 0.0f );
		myMat.setSpecularColor( 1.0f, 1.0f, 1.0f );
		myMat.setShininess( 64.0f );
		myMat.setLightingEnable(true);
		app.setMaterial( myMat );
		
//		TransparencyAttributes myTA = new TransparencyAttributes();
//		myTA.setTransparency( 0.0f );
//		myTA.setTransparencyMode(TransparencyAttributes.BLEND_ONE);
//		app.setTransparencyAttributes(myTA);
		
		return app;
	}
	
	public static Appearance getAppearanceOfColor_Transparent(Color3f color, float transparency)
	{
		Appearance app = new Appearance( );
		ColoringAttributes myCA = new ColoringAttributes( );
		myCA.setColor( color );
		myCA.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
		app.setColoringAttributes( myCA );
		
		Material myMat = new Material();
		myMat.setAmbientColor(  0.3f, 0.3f, 0.3f );
		myMat.setDiffuseColor(color);
		myMat.setEmissiveColor( 0.0f, 0.0f, 0.0f );
		myMat.setSpecularColor( 1.0f, 1.0f, 1.0f );
		myMat.setShininess( 64.0f );
		myMat.setLightingEnable(true);
		app.setMaterial( myMat );
		
		TransparencyAttributes myTA = new TransparencyAttributes();
		myTA.setTransparency( transparency );
		myTA.setTransparencyMode(TransparencyAttributes.BLEND_ONE);
		app.setTransparencyAttributes(myTA);
		
		return app;
	}
	
	/** Generates and returns a 3D double identidy matrix
	 * @author BLM*/
	public static Matrix3d getIdentity_Matrix3d()
	{
		Matrix3d mat = new Matrix3d();
		mat.m00 = 1;
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m10 = 0;
		mat.m11 = 1;
		mat.m12 = 0;
		mat.m20 = 0;
		mat.m21 = 0;
		mat.m22 = 1;
		
		return mat;
	}
	
	
	
	
	/** Generates a random 3D double matrix
	 * @author BLM*/
	public static Matrix3d getRandomRotationMatrix()
	{
		Matrix3d mat = new Matrix3d();
		mat.m00 = Math.random();
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m10 = 0;
		mat.m11 = Math.random();
		mat.m12 = 0;
		mat.m20 = 0;
		mat.m21 = 0;
		mat.m22 = Math.random();
		
		return mat;
	}
	
	/** Generates a random 3D double matrix
	 * @author BLM*/
	public static Matrix3d getRandomMatrix()
	{
		Matrix3d mat = new Matrix3d();
		mat.m00 = Math.random();
		mat.m01 = Math.random();
		mat.m02 = Math.random();
		mat.m10 = Math.random();
		mat.m11 = Math.random();
		mat.m12 = Math.random();
		mat.m20 = Math.random();
		mat.m21 = Math.random();
		mat.m22 = Math.random();
		
		return mat;
	}
	
	public static Color3f getRandomColor3f()
	{
		return new Color3f((float)Math.random(), (float)Math.random(), (float)Math.random());
	}
	
	
	/** Returns a random number between -1 and +1
	 * @author BLM*/
	static public double nRandom()
	{
		double val = Math.random();
		if (Math.random()>0.5)
			return -val;
		return val;
	}
	
	public static void normalizePoints(Point3d[] points, double[][] axisRanges)
	{
		double epsilon = 0.0000001d;
		int len  = points.length;
		float zOffset=0;
		//used to normalize data
		double xMax = axisRanges[0][1];//getXMax(points);
		double yMax = axisRanges[1][1];//getYMax(points);
		double zMax = axisRanges[2][1];//getZMax(points);
		
		double xMin = axisRanges[0][0];//getXMin(points);
		double yMin = axisRanges[1][0];//getYMin(points);
		double zMin = axisRanges[2][0];//getZMin(points);
		
		double xNorm = Math.abs(xMin);
		double yNorm = Math.abs(yMin);
		
		if(Math.abs(xMax)>Math.abs(xMin))
			xNorm = Math.abs(xMax);
		else xNorm = Math.abs(xMin);
		if(Math.abs(yMax)>Math.abs(yMin))
			yNorm = Math.abs(yMax);
		else yNorm = Math.abs(yMin);
		
		//use this if you want a real proportion of x/y ratios... otherwise both axis' are diplayed as one unit
//		double xyNormVal = Math.abs(yNorm);
//		if (Math.abs(xNorm)>xyNormVal)
//			xyNormVal=Math.abs(xNorm);
		
		//normalizing the data now
		for (int r= 0; r < len; r++)
		{
			//normalize
			Point3d p = points[r];
			//x
			if (xNorm>epsilon)
				p.x= (p.x-xMin)/(xMax-xMin);
			//y
			if (yNorm>epsilon)
				p.y= (p.y-yMin)/(yMax-yMin);
			//z
			if (Math.abs(zMax-zMin)>epsilon)
			{
				double val =  (p.z-zMin)/(zMax-zMin) - zOffset;
				p.z = val;
			}
		}
		
	}
	
	/** Returns true if arg[0] > arg[1], else false
	 * @author BLM*/
	static public boolean isBigger(double val1, double val2)
	{
		if (val1>val2)
			return true;
		return false;
	}
	
	static public double getXMax(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double max = Double.MIN_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.x>max)
					max = p.x;
			}
		return max;
	}
	static public double getYMax(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double max = Double.MIN_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.y>max)
					max = p.y;
			}
		return max;
	}
	static public double getZMax(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double max = Double.MIN_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.z>max)
					max = p.z;
			}
		return max;
	}
	
	static public double getXMin(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double min = Double.MAX_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.x<min)
					min = p.x;
			}
		return min;
	}
	static public double getYMin(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double min = Double.MAX_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.y<min)
					min = p.y;
			}
		return min;
	}
	static public double getZMin(Point3f[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double min = Double.MAX_VALUE;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				Point3f p = data[r][c];
				if (p.z<min)
					min = p.z;
			}
		return min;
	}
	
	
	static public double getXMax(Point3d[] data)
	{
		int rows = data.length;
		
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.x>max)
				max = p.x;
		}
		return max;
	}
	static public double getYMax(Point3d[] data)
	{
		int rows = data.length;
		
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.y>max)
				max = p.y;
		}
		return max;
	}
	static public double getZMax(Point3d[] data)
	{
		int rows = data.length;
		
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.z>max)
				max = p.z;
		}
		return max;
	}
	
	
	static public double getXMin(Point3d[] data)
	{
		int rows = data.length;
		
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.x<min)
				min = p.x;
		}
		return min;
	}
	static public double getYMin(Point3d[] data)
	{
		int rows = data.length;
		
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.y<min)
				min = p.y;
		}
		return min;
	}
	static public double getZMin(Point3d[] data)
	{
		int rows = data.length;
		
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < rows; r++)
		{
			Point3d p = data[r];
			if (p.z<min)
				min = p.z;
		}
		return min;
	}
	
	/*** Returns the min value in the data matrix
	 * @author BLM*/
	static public double getMin(double[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				double val = data[r][c];
				if (val<min)
					min = val;
			}
		return min;
	}
	/*** Returns the max value in the data matrix
	 * @author BLM*/
	static public double getMax(double[][] data)
	{
		int rows = data.length;
		int cols = data[0].length;
		
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				double val = data[r][c];
				if (val>max)
					max = val;
			}
		return max;
	}
	
	/** Creates a polygon mesh appearance
	 * @author BLM*/
	static public Appearance getAppearance_PolyMesh()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		app.setMaterial(mat);
		
		return app;
	}
	
	/** Computes the rotation matrix from the AxisAngles
	 * @author BLM*/
	static public Matrix3d matrixFromAxisAngle(AxisAngle4d a1)
	{
		
		double c = Math.cos(a1.angle);
		double s = Math.sin(a1.angle);
		double t = 1.0 - c;
		Matrix3d mat = new Matrix3d();
		mat.m00 = c + a1.x*a1.x*t;
		mat.m11 = c + a1.y*a1.y*t;
		mat.m22 = c + a1.z*a1.z*t;
		
		
		double tmp1 = a1.x*a1.y*t;
		double tmp2 = a1.z*s;
		mat.m10 = tmp1 + tmp2;
		mat.m01 = tmp1 - tmp2;
		tmp1 = a1.x*a1.z*t;
		tmp2 = a1.y*s;
		mat.m20 = tmp1 - tmp2;
		mat.m02 = tmp1 + tmp2;    tmp1 = a1.y*a1.z*t;
		tmp2 = a1.x*s;
		mat.m21 = tmp1 + tmp2;
		mat.m12 = tmp1 - tmp2;
		return mat;
	}
	
	
	/** Creates a polygon surface appearance
	 * @author BLM*/
	static public Appearance getAppearance_PolySurface()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		ColoringAttributes myCA = new ColoringAttributes( );
		myCA.setColor( new Color3f(1f,1f,1f) );
		myCA.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
		app.setColoringAttributes( myCA );
		
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
//		TransparencyAttributes trans = new TransparencyAttributes();
//		trans.setTransparencyMode(TransparencyAttributes.FASTEST);
//		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
//		app.setTransparencyAttributes(trans);
		
		Material mat = new Material();
		
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.0f,0.0f,0.0f));
		mat.setSpecularColor(new Color3f(0.0f,0.0f,0.0f));
		app.setMaterial(mat);
		
		return app;
	}
	/** Creates a Point appearance
	 * @author BLM*/
	static public Appearance getAppearance_PolyPoint()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
		
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		app.setMaterial(mat);
		
		return app;
	}
	
	static public boolean isInt(String val)
	{
		try
		{
			Integer.parseInt(val);
			return true;
			
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	static public boolean isFloat(String val)
	{
		try
		{
			Float.parseFloat(val);
			return true;
			
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	static public boolean isDouble(String val)
	{
		try
		{
			Double.parseDouble(val);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	
	/** Returns bool if all the given strings are some form of number
	 * @author BLM*/
	static public boolean areNumbers(String[] strings)
	{
		int len = strings.length;
		for (int i=0; i < len ;i++)
		{
			boolean thisNumber = false;
			if (isDouble(strings[i])){thisNumber=true;}
			else if (isInt(strings[i])){thisNumber=true;}
			else if (isFloat(strings[i])){thisNumber=true;}
			if (!thisNumber)
				return false;
		}
		return true;
	}
	
	static public NumberFormat getScientificNumberFormatter()
	{
		NumberFormat formatter = new DecimalFormat();
		
//		int maxinteger = Integer.MAX_VALUE;
//		System.out.println(maxinteger);    // 2147483647
		
		formatter = new DecimalFormat("0.######E0");
//		System.out.println(formatter.format(maxinteger)); // 2,147484E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(maxinteger)); // 2.14748E9
		
		
//		int mininteger = Integer.MIN_VALUE;
//		System.out.println(mininteger);    // -2147483648
		
		formatter = new DecimalFormat("0.######E0");
//		System.out.println(formatter.format(mininteger)); // -2.147484E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(mininteger)); // -2.14748E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(d)); // 1.2345E-1
		
		formatter = new DecimalFormat("000000E0");
//		System.out.println(formatter.format(d)); // 12345E-6
		
		return formatter;
	}
	
	public double toScientific(double d)
	{
		NumberFormat formatter = new DecimalFormat();
		
		int maxinteger = Integer.MAX_VALUE;
//		System.out.println(maxinteger);    // 2147483647
		
		formatter = new DecimalFormat("0.######E0");
//		System.out.println(formatter.format(maxinteger)); // 2,147484E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(maxinteger)); // 2.14748E9
		
		
		int mininteger = Integer.MIN_VALUE;
//		System.out.println(mininteger);    // -2147483648
		
		formatter = new DecimalFormat("0.######E0");
//		System.out.println(formatter.format(mininteger)); // -2.147484E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(mininteger)); // -2.14748E9
		
		formatter = new DecimalFormat("0.#####E0");
//		System.out.println(formatter.format(d)); // 1.2345E-1
		
		formatter = new DecimalFormat("000000E0");
//		System.out.println(formatter.format(d)); // 12345E-6
		
		return Double.parseDouble((formatter.format(d)));
	}
}

