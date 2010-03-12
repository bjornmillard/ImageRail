/**
 * HeatMapLegend.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.Text2D;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


/** Initializes the array of squares that are going to represent the rectangle that is
 * the heatmap
 * @author BLM*/
public class HeatMapLegend implements Accessory
{
	public Point3f Position;
	private int TheColorMap;
	private float width = 0.05f;
	private float height = 0.3f;
	private int NumVerts;
	private TriangleStripArray TheTriangleStripArray;
	private LineStripArray TheLineArray;
	private int NumberOfSamples;
	private double maxValue;
	private double minValue;
	public ArrayList TheText;
	
	
	public HeatMapLegend(int colorMap, double minVal, double maxVal)
	{
		TheColorMap = colorMap;
		maxValue = maxVal;
		minValue = minVal;
		Position = new Point3f(-0.5f, -0.4f, 0.5f);
		NumberOfSamples = 50;
		NumVerts = 3+NumberOfSamples;
		initGeometry();
	}
	
	/** Initializes the array of triangles that are going to represent the rectangle that is
	 * the heatmap
	 * @author BLM*/
	private void initGeometry()
	{
		initTheMap();
	}
	
	
	private void initTheMap()
	{
		float deltaY = height/NumberOfSamples;
		
		int[] stripVerCount = {NumVerts};
		TheTriangleStripArray = new TriangleStripArray(NumVerts, GeometryArray.COORDINATES | GeometryArray.COLOR_4,stripVerCount);
		
		//Setting the foundation 2 points to start off this whole strip upwards
		TheTriangleStripArray.setCoordinate(0, new Point3f(0,0,0));
		TheTriangleStripArray.setCoordinate(1,  new Point3f(width,0,0));
		
		float currentY = deltaY;
		for (int j = 2; j < NumVerts-1; j++)
		{
			if (j%2==0)
				TheTriangleStripArray.setCoordinate(j, new Point3f(0, currentY, 0));
			else
				TheTriangleStripArray.setCoordinate(j, new Point3f(width, currentY, 0));
			
			currentY+=deltaY;
		}
		//adding the last point to finish it off
		TheTriangleStripArray.setCoordinate(NumVerts-1, new Point3f(0, currentY-deltaY, 0));
		
		//setting the color of bottom base points
		float[] color = new float[4];
		tools.ColorMaps.getColorValue(minValue, minValue, maxValue, color, TheColorMap);
		TheTriangleStripArray.setColor(0,new Color4f(color[0], color[1], color[2], color[3]));
		TheTriangleStripArray.setColor(1,new Color4f(color[0], color[1], color[2], color[3]));
		
		//setting the color of main middle points
		float deltaVal = (float)(maxValue-minValue);
		deltaVal = deltaVal/(float)NumberOfSamples;
		float currVal = (float)minValue;
		
		for (int i = 2; i < NumVerts; i++)
		{
			tools.ColorMaps.getColorValue(currVal, minValue, maxValue, color, TheColorMap);
			TheTriangleStripArray.setColor(i,new Color4f(color[0], color[1], color[2], color[3]));
			currVal+=deltaVal;
		}
		//adding the last point which is
		TheTriangleStripArray.setColor(NumVerts-1,new Color4f(color[0], color[1], color[2],  color[3]));
		
		//creating the outline of the box
		int[] stripVerCount2 = {5};
		TheLineArray = new LineStripArray(5, GeometryArray.COORDINATES | GeometryArray.COLOR_4, stripVerCount2);
		
		float offset = 0.000001f;
		TheLineArray.setCoordinate(0, new Point3f(0,0,offset));
		TheLineArray.setCoordinate(1, new Point3f(width, 0, offset));
		TheLineArray.setCoordinate(2, new Point3f(width, height, offset));
		TheLineArray.setCoordinate(3, new Point3f(0, height, offset));
		TheLineArray.setCoordinate(4, new Point3f(0,0,offset));
		//setting the line colors
		float grayVal = 0.3f;
		for (int i = 0; i < 5; i++)
			TheLineArray.setColor(i,new Color4f(grayVal,grayVal,grayVal,1));
	}
	
	public TransformGroup getVisualization()
	{
		Transform3D trans = new Transform3D();
		//translate the box down to include half box in neg region and half in positive region
		trans.setTranslation(new Vector3f(Position.x,Position.y,Position.z));
		TransformGroup tg = new TransformGroup(trans);
		tg.addChild(new Shape3D(TheTriangleStripArray, getAppearance()));
		
		
		//adding the ouline box
//		tg.addChild(new OrientedShape3D(TheLineArray, getAppearance(), OrientedShape3D.ROTATE_ABOUT_POINT, new Point3f(width/2f,height/2f,0)));
		tg.addChild(new Shape3D(TheLineArray, getAppearance()));
		
		//
		// adding text
		//
		TheText = new ArrayList();
		Color3f fontColor = new Color3f(0f, 0f, 0f);
		// Create a Text2D leaf node, add it to the scene graph.
		int fontSize = 12;
		String fontName = "Helvetica";
//		NumberFormat nf = NumberFormat.getInstance();
//		nf.setMaximumFractionDigits(1000);
		NumberFormat nf = new DecimalFormat("0.##E0");
		Text2D text2D = new Text2D(""+Float.parseFloat(nf.format(maxValue)),
								   fontColor,
								   fontName, fontSize, Font.BOLD);
		TheText.add(text2D);
		
		float scale = 0.65f;
		//moving the text to the MaxVal Position
		Transform3D ty_2 = new Transform3D();
		ty_2.setTranslation(new Vector3f(width+width/5f,height-height/10f,0f)); //all the way up
		ty_2.setScale(scale);
		TransformGroup tg_max = new TransformGroup(ty_2);
		tg_max.addChild(text2D);
		tg.addChild(tg_max);
		
		
		// Create a Text2D leaf node, add it to the scene graph.
		text2D = new Text2D(""+Float.parseFloat(nf.format(minValue)),
							fontColor,
							fontName, fontSize, Font.BOLD);
		TheText.add(text2D);
		
		
		
		//moving the text to the MinVal Position
		Transform3D ty_3 = new Transform3D();
		ty_3.setTranslation(new Vector3f(width+width/5f,0,0f)); //at the bottom
		ty_3.setScale(scale);
		TransformGroup tg_min = new TransformGroup(ty_3);
		tg_min.addChild(text2D);
		tg.addChild(tg_min);
		
		//if the Zero marker is between the min/max then add zero as the middle marker, otherwise add range/2f
		float fractionHeight = -(float)(minValue/(maxValue-minValue));
		if (minValue<0 && maxValue>0 && fractionHeight>0.1f && fractionHeight<0.82f)
		{
			// Create a Text2D leaf node, add it to the scene graph.
			text2D = new Text2D("0.0",
								fontColor,
								fontName, fontSize, Font.BOLD);
			TheText.add(text2D);
			
			
			//moving the text to the Zero Position
			Transform3D ty_1 = new Transform3D();
			ty_1.setTranslation(
				new Vector3f(width+width/5f,height*fractionHeight-height/50f,0f));
			ty_1.setScale(scale);
			TransformGroup tg_zero = new TransformGroup(ty_1);
			tg_zero.addChild(text2D);
			tg.addChild(tg_zero);
		}
		else
		{
			// Create a Text2D leaf node, add it to the scene graph.
			text2D = new Text2D(""+(Float.parseFloat(nf.format(minValue+(maxValue-minValue)/2f))),
								fontColor,
								fontName, fontSize, Font.BOLD);
			TheText.add(text2D);
			
			
			//moving the text to the Zero Position
			Transform3D ty_1 = new Transform3D();
			ty_1.setTranslation(new Vector3f(width+width/5f,height/2f-height/50f,0f)); //halfway up
			ty_1.setScale(scale);
			TransformGroup tg_zero = new TransformGroup(ty_1);
			tg_zero.addChild(text2D);
			tg.addChild(tg_zero);
		}
		
		int len = TheText.size();
		for (int i =0; i < len; i++)
		{
			Text2D text = (Text2D)TheText.get(i);
			text.setCapability(Text2D.ALLOW_APPEARANCE_WRITE);
			text.setCapability(Text2D.ALLOW_APPEARANCE_READ);
			text.getAppearance().setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		}
		
		return tg;
	}
	
	
	static public Appearance getAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
//		TransparencyAttributes trans = new TransparencyAttributes();
//		trans.setTransparencyMode(TransparencyAttributes.NICEST);
//		trans.setSrcBlendFunction(TransparencyAttributes.BLENDED);
//		trans.setTransparency(0.0f);
//		app.setTransparencyAttributes(trans);
		
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

