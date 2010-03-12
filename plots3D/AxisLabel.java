/**
 * AxisMarker.java
 *
 * @author BLM
 */

package plots3D;

import javax.media.j3d.*;

import java.awt.Font;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class AxisLabel implements Accessory
{
	private String Label;
	private Point3f Position;
	private int Type;
	private Text3D TheText;
	static final public float TextScale = 0.05f;
	static final public int Xaxis = 0;
	static final public int Yaxis = 1;
	static final public int Zaxis = 2;
	
	public AxisLabel(int Type)
	{
		Position = new Point3f();
		if (Type == Xaxis)
		{	Position.x = 0.5f; Position.y = -0.3f; Position.z = 0f;}
		else if (Type == Yaxis)
		{	Position.x = 1.3f; Position.y = 0.5f; Position.z = 0f;}
		else if (Type == Zaxis)
		{	Position.x = -0.25f; Position.y = -0.25f; Position.z = 0.5f;}
	}
	
	/** Returns an appropriate Shape3D
	 * @author BLM*/
	public TransformGroup getVisualization()
	{
		//creating the text
		Font3D f3d =  new Font3D(new Font("Helvetica",Font.PLAIN,1),new FontExtrusion());
		String text = "";
		if (Label!=null)
			text=Label;
		TheText = new Text3D(f3d , text);
		TheText.setCapability(Text3D.ALLOW_STRING_WRITE);
		//moving the text to the correct position
		Transform3D ty = new Transform3D();
		
		ty.setTranslation(new Vector3f(Position.x, Position.y, Position.z));
		ty.setScale(TextScale);
		TransformGroup tg = new TransformGroup(ty);
		OrientedShape3D shape = new OrientedShape3D(TheText, Tools.getAppearanceOfColor(new Color3f(0.6f,0.6f, 0.6f)),OrientedShape3D.ROTATE_ABOUT_POINT, Position);
		tg.addChild(shape);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		shape.setCapability(OrientedShape3D.ALLOW_APPEARANCE_WRITE);
		
		return tg;
	}
	
	/** Sets the axis type for this label:  Xaxis == 0; Yaxis == 1;, Zaxis == 2
	 * @author BLM*/
	public void setType(int type)
	{
		Type = type;
	}
	/** Returns the type of axis of this label: Xaxis == 0; Yaxis == 1;, Zaxis == 2
	 * @author BLM*/
	public int getType()
	{
		return Type;
	}
	
	
	public void setX(float val)
	{
		Position.x = val;
	}
	public void setY(float val)
	{
		Position.y = val;
	}
	public void setZ(float val)
	{
		Position.z = val;
	}
	
	/** Sets the position of this label
	 * @author BLM*/
	public void setPosition(Point3f position)
	{
		Position = position;
	}
	/** Gets the position of this label
	 * @author BLM*/
	public Point3f getPosition()
	{
		return Position;
	}
	
	/** Sets the text for this label
	 * @author BLM*/
	public void setLabel(String label)
	{
		Label = label;
	}
	
	/** Sets the text for this label
	 * @author BLM*/
	public void setText(String text)
	{
		TheText.setString(text);
	}
	
	/** Gets the text from this label
	 * @author BLM*/
	public String getLabel()
	{
		return Label;
	}
	
	/** If the axis is not of length 1... then the fraction will scale according to the middle of the real axis length
	 * @author BLM */
	public void setPositionScale(float fraction)
	{
		if (Type == Xaxis)
		{
			Position.y = Position.y*fraction;
		}
		else if (Type == Yaxis)
		{
			Position.x = Position.x*fraction;
		}
		else if (Type == Zaxis)
		{
			Position.z = Position.z*fraction;
		}
	}
	
}

