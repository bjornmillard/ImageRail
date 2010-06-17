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
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.io.File;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.PointLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class ThreeDPanel_SurfacePlot extends JPanel
{
	public Canvas3D_ScreenShotable TheCanvas;
	public SimpleUniverse TheUniverse;
	static public TransformGroup MainTransformGroup;
	public DataSet Data;
	public Background TheBackground;
	public HeatMapLegend TheHeatMap;
	
	
	
	
	public void initializePanel(DataSet data)
	{
		Data = data;
		
		setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		TheCanvas = new Canvas3D_ScreenShotable(config, Canvas3D_ScreenShotable.SURFACEPLOT);
		TheCanvas.setBackground(Color.white);
		
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
	
	
	/** Given an arbitrary set of euler angles, sets the camera angles
	 * @author BLM*/
	public void setView(Matrix3d rotation, Vector3d translation)
	{
		TheUniverse.getViewingPlatform().setNominalViewingTransform();
		MainTransformGroup.setTransform(new Transform3D(rotation, translation, 1));
	}
	
	public  PlatformGeometry createPlatformGeometry()
	{
		TransformGroup ptTG = new TransformGroup ();
		TheHeatMap = new HeatMapLegend(SurfacePlotMainPanel.getColorMap(), Data.getDataMin_Z(), Data.getDataMax_Z());
		ptTG.addChild(TheHeatMap.getVisualization());
		
		//Since we want the polygon to be just in front of the user, we add a Transform3D
		Transform3D pt3d = new Transform3D();
		pt3d.setTranslation (new Vector3d (-0.4, 1.1, -3.0));
		ptTG.setTransform (pt3d);
		
		PlatformGeometry geo = new PlatformGeometry();
		geo.addChild(ptTG);
		
		return geo;
	}
	
	
	public BranchGroup createSceneGraph(SimpleUniverse su)
	{
		
		BoundingSphere bounds =	new BoundingSphere (new Point3d (0, 0, 0), 100);
		
		BranchGroup objRoot = new BranchGroup();
		objRoot.setBounds(bounds);
		
		TheBackground = new Background(0f, 0f, 0f);
		TheBackground.setApplicationBounds(new BoundingSphere());
		TheBackground.setCapability(Background.ALLOW_COLOR_READ);
		TheBackground.setCapability(Background.ALLOW_COLOR_WRITE);
		objRoot.addChild(TheBackground);
		
		
//
		Color3f lightColor = new Color3f (1.0f, 1.0f, 1.0f);
//		Vector3f light1Direction = new Vector3f (-0.1f, 0.1f, -1f);
//		DirectionalLight light1  = new DirectionalLight (lightColor, light1Direction);
//		light1.setInfluencingBounds (bounds);
//		objRoot.addChild (light1);
		
		PointLight light2  = new PointLight (lightColor, new Point3f(2, 2, 2f), new Point3f( 0, 0.2f, 0 ));
		light2.setInfluencingBounds (bounds);
		objRoot.addChild (light2);
		
		PointLight light1  = new PointLight (lightColor, new Point3f(-2, -3, 3f), new Point3f( 0, 0.2f, 0 ));
		light1.setInfluencingBounds (bounds);
		objRoot.addChild (light1);
		
//		Vector3f light2Direction = new Vector3f (-1f, 0, -1f);
//		DirectionalLight light2  = new DirectionalLight (lightColor, light2Direction);
//		light2.setInfluencingBounds (bounds);
//		objRoot.addChild (light2);
//
		AmbientLight ambientLightNode = new AmbientLight (lightColor);
		ambientLightNode.setInfluencingBounds (bounds);
		objRoot.addChild (ambientLightNode);
		
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
		float val = 0.0f;
		Background backg = new Background(val,val,val);
		backg.setApplicationBounds(new BoundingSphere(new Point3d(), 100.0));
		objRoot.addChild(backg);
		
		
		
		//rotating it so it the origin is facing the viewplatform
		resetView();
		objRoot.compile();
		
//		objRoot.compile();
		return objRoot;
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

