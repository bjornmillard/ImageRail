/**
 * AxisTicks.java
 *
 * @author BLM
 */

package plots3D;

import javax.media.j3d.*;

import java.awt.Font;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class AxisTicks implements Accessory
{
	private int Type;
	public LineArray TheLineArray;
	public Shape3D TheLineArrayShape;
	private TransformGroup[] TheTextArray;
	public ArrayList TheTextShapes;
	private double minVal;
	private double maxVal;
	private float Scale;
	static final public float TextScale = 0.03f;
	static final public int Xaxis = 0;
	static final public int Yaxis = 1;
	static final public int Zaxis = 2;
	final private int numTicks;
	private int numPoints;
	private float[] TheTickValues;
	Font font = new Font("Helvetica", Font.PLAIN, 10);
	
	
	public AxisTicks(float[] tickValues, int axisType, float scale)
	{
		TheTickValues = tickValues;
		numTicks = tickValues.length;
		Type = axisType;
		Scale = scale;
		
		minVal = getMinTickValue(tickValues);
		maxVal = getMaxTickValue(tickValues);
		initGeometry();
	}
	
	private float getMinTickValue(float[] tickVals)
	{
		int len = tickVals.length;
		float min = Float.POSITIVE_INFINITY;
		
		for (int i = 0; i < len; i++)
			if (tickVals[i] < min)
				min=tickVals[i];
		
		return min;
	}
	
	private float getMaxTickValue(float[] tickVals)
	{
		int len = tickVals.length;
		float max = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < len; i++)
			if (tickVals[i] > max)
				max=tickVals[i];
		
		return max;
	}
	
	public void initGeometry()
	{
		int totalTicks  = numTicks+1;
		numPoints = totalTicks*2;
		TheLineArray = new LineArray(numPoints, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		TheLineArray.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		float tickLength = 0.02f;
		TheTextArray = new TransformGroup[totalTicks];
		TheTextShapes = new ArrayList();
		
		int counter = 0;
		for (int i=0; i < totalTicks-1; i++)
		{
			
			//creating the text
			Font3D f3d =  new Font3D(new Font("Helvetica",Font.PLAIN,1),new FontExtrusion());
			String text = ""+TheTickValues[i];
			Text3D t3d = new Text3D(f3d , text);
			Point3f point = new Point3f();
			Transform3D ty = new Transform3D();
			float offset = 0.07f;
			float zOffset = 0f;
			if (Type == Xaxis)
			{
				float primAxisDist = (float)((TheTickValues[i] - minVal)/(maxVal-minVal));
				TheLineArray.setCoordinate(counter, new Point3f(primAxisDist*Scale, 0,zOffset));
				counter++;
				TheLineArray.setCoordinate(counter, new Point3f(primAxisDist*Scale, -tickLength,zOffset));
				counter++;
				
				//moving the text to the appropriate position
				if (Type == Xaxis)
				{
					point.x = primAxisDist*Scale; point.y =  -tickLength-offset; point.z = zOffset;
//					textTransform.setTranslation(new Vector3f(new Point3f(primAxisDist, -tickLength-0.05f,-0.5f)));
				}
			}
			else if (Type == Yaxis)
			{
				float primAxisDist = (float)((TheTickValues[i] - minVal)/(maxVal-minVal));
				TheLineArray.setCoordinate(counter, new Point3f(1f, primAxisDist*Scale,zOffset));
				counter++;
				TheLineArray.setCoordinate(counter, new Point3f(1f+tickLength, primAxisDist*Scale,zOffset));
				counter++;
				
				//moving the text to the appropriate position
				if (Type == Yaxis)
				{
					point.x = 1f+tickLength+offset; point.y =  primAxisDist*Scale; point.z = zOffset;
//					textTransform.setTranslation(new Vector3f(new Point3f(1f+tickLength+0.05f, primAxisDist,-0.5f)));
				}
			}
			else if (Type == Zaxis)
			{
				float primAxisDist = (float)((TheTickValues[i] - minVal)/(maxVal-minVal));
				TheLineArray.setCoordinate(counter, new Point3f(0, 0,(primAxisDist+zOffset)*Scale)); //subtract half because zrange=[-0.5,0.5]
				counter++;
				TheLineArray.setCoordinate(counter, new Point3f(-tickLength, -tickLength,(primAxisDist+zOffset)*Scale));
				counter++;
				
				//moving the text to the appropriate position
				if (Type == Zaxis)
				{
					point.x = -tickLength-offset; point.y =  -tickLength-offset; point.z = (primAxisDist+zOffset)*Scale;
//					textTransform.setTranslation(new Vector3f(new Point3f(-tickLength-0.05f, -tickLength-0.05f,primAxisDist-0.5f)));
				}
			}
			ty.setTranslation(new Vector3f(point.x, point.y, point.z));
			ty.setScale(TextScale);
			TransformGroup tg = new TransformGroup(ty);
			OrientedShape3D textShape = new OrientedShape3D(t3d, Tools.getAppearanceOfColor(new Color3f(0.6f,0.6f, 0.6f)),OrientedShape3D.ROTATE_ABOUT_POINT, point);
			textShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			TheTextShapes.add(textShape);
			tg.addChild(textShape);
			TheTextArray[i] = tg;
			
			
//			TransformGroup textTG = new TransformGroup(textTransform);
//			TheTextArray[i] = textTG;
//			textTG.addChild(text2D);
		}
		
		float grayVal = 0.6f;
		for (int i = 0; i < numPoints; i++)
			TheLineArray.setColor(i,new Color3f(grayVal,grayVal,grayVal));
		
	}
	
	/** Returns an appropriate Shape3D
	 * @author BLM*/
	public TransformGroup getVisualization()
	{
		TransformGroup tg = new TransformGroup();
		TheLineArrayShape = new Shape3D(TheLineArray, Tools.getAppearance_PolyMesh());
		tg.addChild(TheLineArrayShape);
		
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		TheLineArrayShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		
		
		int len = TheTextArray.length;
		for (int i =0; i < len; i++)
			tg.addChild(TheTextArray[i]);
		
		
		
		return tg;
	}
	
	public void setColor(Color3f color)
	{
		for (int i = 0; i < numPoints; i++)
			TheLineArray.setColor(i,color);
	}
	
}



