/**
 * MainPanel.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;



import javax.media.j3d.*;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


public class ImageRail3D_PlotPanel extends ThreeDPanel_Histogram
{
	public ArrayList TheAxisLabelsTG;
	public ArrayList TheAxisLabels;
	public String[] TheAxisLabelText;
	public TransformGroup TheBoxFrameTG;
	public Box_halfFrame TheBoxFrame;
	public double[][] TheColorValues;
	
	public 	Appearance Render_Frame;
	public 	Appearance Render_Boxes;
	public 	Appearance Render_Ribbons;
	
	public ImageRail3D_PlotPanel(double[][] data, double[][] colorValues)
	{
		TheColorValues = colorValues;
		TheAxisLabelText = new String[3];
		TheAxisLabelText[0] = "Axis_X";
		TheAxisLabelText[1] = "Axis_Y";
		TheAxisLabelText[2] = "Axis_Z";
		initializePanel(data, colorValues);
	}
	
	
	
	public void addObjects(TransformGroup tg)
	{
		float scale = 1f;
		float minVal = (float)getMinValue_data();
		float maxVal = (float)getMaxValue_data();
		
		int nRows = TheData.length;
		int nCols = TheData[0].length;
		
		//Getting max number of columns in each series
		for (int i = 0; i < nRows; i++)
			if (TheData[i].length>nCols)
				nCols = TheData[i].length;
		
		float bigger = 0;
		if (nCols>nRows)
			bigger = nCols;
		else
			bigger = nRows;
		//normalizing xy scales to have the largest axis == 1 length in the virtual world
		float xLen = (float)nRows/bigger;
		float yLen = (float)nCols/bigger;
		
		
		
		//creating a box the size of the xMax, yMax
		Render_Frame = getToggleAppearance();
		TheBoxFrame = new Box_halfFrame();
		TheBoxFrame.xLength = xLen;
		TheBoxFrame.yLength = yLen;
		TheBoxFrame.zLength*=scale;
		TheBoxFrameTG = TheBoxFrame.getVisualization(Render_Frame);
		tg.addChild(TheBoxFrameTG);
		
		
		Plane p = new Plane(Plane.Z, xLen, yLen);
		tg.addChild(p.getVisualization());
		
		float interbarDist = 0.05f;
		float widthOfBarX = xLen/(float)nRows-interbarDist;
		float widthOfBarY = yLen/(float)nCols-interbarDist;
		if (widthOfBarX<0.01)
		{
			widthOfBarX = xLen/(float)nRows;
			widthOfBarY = yLen/(float)nCols;
			interbarDist = 0;
		}
		
		
		//creating all the histogram boxes
		Render_Boxes = getToggleAppearance();
		float[] color = new float[4];
		float zZero = 0;
		if (minVal<0)
			zZero = Math.abs(minVal)/(maxVal-minVal);
		Box_colored[][] boxes = new Box_colored[nRows][nCols];
		double minColor = tools.MathOps.min(TheColorValues);
		double maxColor = tools.MathOps.max(TheColorValues);
//		System.out.println("min: "+minColor);
//		System.out.println("max: "+maxColor);
		for(int r =0; r < nRows; r++)
			for (int c= 0; c < nCols; c++)
			{
				Point3f pos = new Point3f((widthOfBarX+interbarDist)*(r), (widthOfBarY+interbarDist)*c, zZero);
				//normalize height
				float val = (float)TheData[r][c];
				float normHeight = 0;
				
				normHeight = (val-minVal)/(maxVal-minVal);
				normHeight*=scale;
				Box_colored b = new Box_colored(widthOfBarX, widthOfBarY,normHeight-zZero , pos);
				//setting the color
				
				tools.ColorMaps.getColorValue(TheColorValues[r][c], minColor, maxColor ,color, ImageRail3D_Frame.getColorMap());
				b.setColor(color[0], color[1], color[2], color[3]);
				boxes[r][c] = b;
				tg.addChild(b.getVisualization(Render_Boxes));
			}
		
		
		//Adding Ribbons
		Render_Ribbons = getToggleAppearance();
		
		for (int r = 0; r < nRows; r++)
		{
			Box_colored[] series = new Box_colored[boxes[r].length];
			for(int c =0; c < nCols; c++)
				series[c] = boxes[r][c];
			RibbonCurve rc = new RibbonCurve(series);
			tg.addChild(rc.getVisualization(Render_Ribbons));
		}
		
		
		
		//adding light
		BoundingSphere bounds =	new BoundingSphere (new Point3d (0, 0.0, 5), 5.0);
		Color3f lightColor = new Color3f (1.0f, 1.0f, 1.0f);
		Vector3f light1Direction = new Vector3f (0.5f, 0f, -1f);
		DirectionalLight light1  = new DirectionalLight (lightColor, light1Direction);
		light1.setInfluencingBounds (bounds);
		tg.addChild (light1);
		
		
		
//		//Letting us access the transform group later
//		TheAxisLabelsTG = new ArrayList();
//		TheAxisLabels  = new ArrayList();
//		AxisLabel label = new AxisLabel(AxisLabel.Xaxis);
//		label.setX(TheBoxFrame.xLength/2f);
//		TheAxisLabels.add(label);
//		label.setLabel(TheAxisLabelText[0]);
//		TransformGroup labelX_tg = label.getVisualization();
//		tg.addChild(labelX_tg);
//		TheAxisLabelsTG.add(labelX_tg);
//
//		label = new AxisLabel(AxisLabel.Yaxis);
//		label.setX(TheBoxFrame.xLength+0.2f);
//		label.setY(TheBoxFrame.yLength/2f);
//		TheAxisLabels.add(label);
//		label.setLabel(TheAxisLabelText[1]);
//		TransformGroup labelY_tg = label.getVisualization();
//		tg.addChild(labelY_tg);
//		TheAxisLabelsTG.add(labelY_tg);
//
//
//		label = new AxisLabel(AxisLabel.Zaxis);
//		TheAxisLabels.add(label);
//		label.setLabel(TheAxisLabelText[2]);
//		TransformGroup labelZ_tg = label.getVisualization();
//		tg.addChild(labelZ_tg);
//		TheAxisLabelsTG.add(labelZ_tg);
		
		
	}
	
	
	
	
	public void takeScreenShot()
	{
		JFileChooser fc = null;
		if (SurfacePlotMainPanel.OutputDirectory!=null)
			fc = new JFileChooser(SurfacePlotMainPanel.OutputDirectory);
		else
			fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			captureImage(file);
		}
		else return;
		fc.setVisible(false);
		fc = null;
	}
	
	public void setAxisText_X(String text)
	{
		AxisLabel label = ((AxisLabel)TheAxisLabels.get(0));
		label.setText(text);
		TheAxisLabelText[0] = text;
	}
	
	public void setAxisText_Y(String text)
	{
		AxisLabel label = ((AxisLabel)TheAxisLabels.get(1));
		label.setText(text);
		TheAxisLabelText[1] = text;
	}
	
	public void setAxisText_Z(String text)
	{
		AxisLabel label = ((AxisLabel)TheAxisLabels.get(2));
		label.setText(text);
		TheAxisLabelText[2] = text;
	}
	
	private Appearance getToggleAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance TheAppearance = new Appearance();
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		
		polyAttrib.setBackFaceNormalFlip(true);
		TheAppearance.setPolygonAttributes(polyAttrib);
		
		TheAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		TheAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
		RenderingAttributes render = new RenderingAttributes();
		TheAppearance.setRenderingAttributes(render);
		render.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		TheAppearance.setMaterial(mat);
		
		return TheAppearance;
	}
	
}

