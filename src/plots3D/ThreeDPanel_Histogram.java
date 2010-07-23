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


import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.io.File;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.PointLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;

public abstract class ThreeDPanel_Histogram extends JPanel
{
	public Canvas3D_ScreenShotable TheCanvas;
	public SimpleUniverse TheUniverse;
	static public TransformGroup MainTransformGroup;
	public double[][] TheData;
	public double[][] TheColorValues;
	public Background TheBackground;
//	public double MinVal;
//	public double MaxVal;
	
	public void initializePanel(double[][] data, double[][] colorValues)
	{
		TheData = data;
		TheColorValues = colorValues;
		
		
		setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		TheCanvas = new Canvas3D_ScreenShotable(config, Canvas3D_ScreenShotable.HISTOGRAMPLOT);
		
		TheUniverse = new SimpleUniverse(TheCanvas);
		BranchGroup scene = createSceneGraph(TheUniverse);
		
		scene.compile();
		TheUniverse.getViewingPlatform().setNominalViewingTransform();
		TheUniverse.getViewingPlatform().setPlatformGeometry(createPlatformGeometry());
		
		TheUniverse.addBranchGraph(scene);
		
		add("Center", TheCanvas);
	}
	
	
	/** Rotates the view so the origin is facing the viewplatform
	 * @author BLM*/
	public void resetView()
	{
		TheUniverse.getViewingPlatform().setNominalViewingTransform();
		Transform3D t3d = new Transform3D();
		t3d.lookAt(new Point3d(0.9, -0.3, 0.5), new Point3d(0.5,0.5,0), new Vector3d(0,0,1d));
		MainTransformGroup.setTransform(t3d);
	}
	
	public BranchGroup createSceneGraph(SimpleUniverse su)
	{
		
		BoundingSphere bounds =	new BoundingSphere (new Point3d (0, 0, 0), 100);
		
		BranchGroup objRoot = new BranchGroup();
		objRoot.setBounds(bounds);
		
		TheBackground = new Background(1f, 1f, 1f);
		TheBackground.setApplicationBounds(new BoundingSphere());
		TheBackground.setCapability(Background.ALLOW_COLOR_READ);
		TheBackground.setCapability(Background.ALLOW_COLOR_WRITE);
		objRoot.addChild(TheBackground);
		
		
		Color3f lightColor = new Color3f (1.0f, 1.0f, 1.0f);
		
		PointLight light2  = new PointLight (lightColor, new Point3f(3, 3, 2f), new Point3f( 0, 0.2f, 0 ));
		light2.setInfluencingBounds (bounds);
		objRoot.addChild (light2);
		
		PointLight light1  = new PointLight (lightColor, new Point3f(-3, -3, -2f), new Point3f( 0, 0.2f, 0 ));
		light1.setInfluencingBounds (bounds);
		objRoot.addChild (light1);
		
		//Mouse control
		MainTransformGroup = new TransformGroup();
		MainTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		MainTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		
		objRoot.addChild(MainTransformGroup);
		
		MouseRotate myMouseRotate = new MouseRotate();
		myMouseRotate.setTransformGroup(MainTransformGroup);
		myMouseRotate.setSchedulingBounds(new BoundingSphere());
		objRoot.addChild(myMouseRotate);
		
		MouseTranslate myMouseTranslate = new MouseTranslate();
		myMouseTranslate.setTransformGroup(MainTransformGroup);
		myMouseTranslate.setSchedulingBounds(new BoundingSphere());
		objRoot.addChild(myMouseTranslate);
		
		MouseZoom myMouseZoom = new MouseZoom();
		myMouseZoom.setTransformGroup(MainTransformGroup);
		myMouseZoom.setSchedulingBounds(new BoundingSphere());
		objRoot.addChild(myMouseZoom);
		
		addObjects(MainTransformGroup);
		
		//setting the background color
		float val = 0.05f;
		Background backg = new Background(val,val,val);
		backg.setApplicationBounds(new BoundingSphere(new Point3d(), 100.0));
		objRoot.addChild(backg);
		
		resetView();
		objRoot.compile();
		
		return objRoot;
	}
	
	public double getMinValue_data()
	{
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < TheData.length; r++)
			for (int c =0; c < TheData[0].length; c++)
			{
				if (TheData[r][c]<min)
					min = TheData[r][c];
			}
		return min;
	}

	public double getMaxValue_data()
	{
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < TheData.length; r++)
			for (int c =0; c < TheData[0].length; c++)
			{
				if (TheData[r][c]>max)
					max = TheData[r][c];
			}
		return max;
	}
	
	
	public double getMinValue_colors()
	{
		double min = Double.POSITIVE_INFINITY;
		for (int r = 0; r < TheColorValues.length; r++)
			for (int c =0; c < TheColorValues[0].length; c++)
			{
				if (TheColorValues[r][c]<min)
					min = TheColorValues[r][c];
			}
		return min;
	}
	
	public double getMaxValue_colors()
	{
		double max = Double.NEGATIVE_INFINITY;
		for (int r = 0; r < TheColorValues.length; r++)
			for (int c =0; c < TheData[0].length; c++)
			{
				if (TheColorValues[r][c]>max)
					max = TheColorValues[r][c];
			}
		return max;
	}
	
	public  PlatformGeometry createPlatformGeometry()
	{
		TransformGroup ptTG = new TransformGroup ();
		HeatMapLegend heat = new HeatMapLegend(ImageRail3D_Frame.getColorMap(), getMinValue_colors(), getMaxValue_colors());
		ptTG.addChild(heat.getVisualization());
		
		//Since we want the polygon to be just in front of the user, we add a Transform3D
		Transform3D pt3d = new Transform3D();
		pt3d.setTranslation (new Vector3d (0, 0, -1.82));
		ptTG.setTransform (pt3d);
		
		PlatformGeometry geo = new PlatformGeometry();
		geo.addChild(ptTG);
		
		return geo;
	}
	
	public void addObjects(TransformGroup tg)
	{
		//TODO in child classes
	}
	
	/** Saves an image of this frame given a file in which to save it to
	 * @author BLM*/
	public void captureImage(File fileToWriteTo)
	{
		TheCanvas.setFileOutput(fileToWriteTo);
		TheCanvas.printJPEG = true;
		TheCanvas.repaint();
    }
	
	
	
}

