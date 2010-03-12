/**
 * MainPanel.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;



import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.media.j3d.Appearance;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFileChooser;
import javax.vecmath.Point3d;


public class SurfacePlotPanel extends ThreeDPanel_SurfacePlot
{
	//Graph Types
	static final public int TYPE_SURFACEPLOT = 0;
	static final public int TYPE_MESHPLOT = 1;
	static final public int TYPE_POINTPLOT = 2;
	static final public Appearance Appearance_Surface= Tools.getAppearance_PolySurface();
	static final public Appearance Appearance_Mesh= Tools.getAppearance_PolyMesh();
	static final public Appearance Appearance_Point= Tools.getAppearance_PolyPoint();
	
	public Shape3D ThePlot;
	public IndexedGeometryArray ThePlot_Geometry;
	
	public ArrayList TheAxisLabelsTG;
	public ArrayList TheAxisLabels;
	public String[] TheAxisLabelText;
	public ArrayList TheTickLabelsTG;
	public AxisTicks[] TheTicks;
	public TransformGroup TheBoxFrameTG;
	public Box_halfFrame TheBoxFrame;
	
	
	public SurfacePlotPanel(Point3d[] points, double[] colors, String[] axisLabels, double[][] ranges, int[] axisScaleTypes)
	{
		TheAxisLabelText = axisLabels;
		initializePanel(new DataSet(points, colors, ranges,axisScaleTypes));
	}
	
	public void addObjects(TransformGroup tg)
	{
		double xMax =Data.getAxisMax_X();
		double xMin = Data.getAxisMin_X();
		double yMax =Data.getAxisMax_Y();
		double yMin = Data.getAxisMin_Y();
		double zMax =Data.getAxisMax_Z();
		double zMin = Data.getAxisMin_Z();
		
		//creating a box the size of the xMax, yMax
		TheBoxFrame = new Box_halfFrame();
		TheBoxFrameTG = TheBoxFrame.getVisualization();
		tg.addChild(TheBoxFrameTG);
		float zOffset = 0f;
		
		//if the bounds span zero, then should include a translucent pane to appropriate location
		if (xMax>0 && xMin < 0)
		{
			Plane p = new Plane(Plane.X,1,1);
			p.Position.x = (float)(-xMin/(xMax-xMin));
			tg.addChild(p.getVisualization());
		}
		if (yMax>0 && yMin < 0)
		{
			Plane p = new Plane(Plane.Y,1,1);
			p.Position.y = (float)(-yMin/(yMax-yMin));
			tg.addChild(p.getVisualization());
		}
		if (zMax>0 && zMin <0)
		{
			Plane p = new Plane(Plane.Z, 1,1);
			p.Position.z = (float)(-zMin/(zMax-zMin) - zOffset);
			tg.addChild(p.getVisualization());
		}
		
		//Letting us access the transform group later
		TheAxisLabelsTG = new ArrayList();
		TheAxisLabels  = new ArrayList();
		TheTickLabelsTG = new ArrayList();
		TheTicks = new AxisTicks[3];
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		
		AxisLabel label = new AxisLabel(AxisLabel.Xaxis);
		TheAxisLabels.add(label);
		label.setLabel(TheAxisLabelText[0]);
		TransformGroup labelX_tg = label.getVisualization();
		tg.addChild(labelX_tg);
		TheAxisLabelsTG.add(labelX_tg);
		
		label = new AxisLabel(AxisLabel.Yaxis);
		TheAxisLabels.add(label);
		label.setLabel(TheAxisLabelText[1]);
		TransformGroup labelY_tg = label.getVisualization();
		tg.addChild(labelY_tg);
		TheAxisLabelsTG.add(labelY_tg);
		
		
		label = new AxisLabel(AxisLabel.Zaxis);
		TheAxisLabels.add(label);
		label.setLabel(TheAxisLabelText[2]);
		TransformGroup labelZ_tg = label.getVisualization();
		tg.addChild(labelZ_tg);
		TheAxisLabelsTG.add(labelZ_tg);
		
		//adding the tick marks
		float[] xticksVals = getTickValues(xMin, xMax, 6);
		AxisTicks ticksX = new AxisTicks(xticksVals,AxisTicks.Xaxis,1f);
		TransformGroup ticksX_tg = ticksX.getVisualization();
		tg.addChild(ticksX_tg);
		TheTickLabelsTG.add(ticksX_tg);
		TheTicks[0] = ticksX;
		
		float[] yticksVals = getTickValues(yMin, yMax, 6);
		AxisTicks ticksY = new AxisTicks(yticksVals,AxisTicks.Yaxis, 1f);
		TransformGroup ticksY_tg = ticksY.getVisualization();
		tg.addChild(ticksY_tg);
		TheTickLabelsTG.add(ticksY_tg);
		TheTicks[1] = ticksY;
		
		float[] zticksVals = getTickValues(zMin, zMax, 6);
		AxisTicks ticksZ = new AxisTicks(zticksVals,AxisTicks.Zaxis, 1f);
		TransformGroup ticksZ_tg  = ticksZ.getVisualization();
		tg.addChild(ticksZ_tg);
		TheTickLabelsTG.add(ticksZ_tg);
		TheTicks[2] = ticksZ;
		
		Mesh_fromPoints mesh = new Mesh_fromPoints();
		//Getting a handle on the triangleArray so the User can interact with it
		ThePlot = mesh.getVisualization(Data);
		ThePlot.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		ThePlot_Geometry = (IndexedGeometryArray)ThePlot.getGeometry();
		ThePlot_Geometry.setCapability(IndexedGeometryArray.ALLOW_COLOR_WRITE);
		tg.addChild(ThePlot);
	}
	
	public float[] getTickValues(double min, double max, float numTicks)
	{
		ArrayList vals = new ArrayList();
		
		float inc = (float)(max-min)/(numTicks-1f);
		float delta = 0;
		for (int i =0; i < numTicks; i++)
		{
			double val = min+delta;
//			NumberFormat nf = NumberFormat.getInstance();
//			nf.setMaximumFractionDigits(3);
			NumberFormat nf = new DecimalFormat("0.##E0");
			vals.add(new Float(nf.format(val)));
			delta+=inc;
		}
		
		int len = vals.size();
		float[] arr = new float[len];
		for (int i = 0; i < len; i++)
			arr[i] = ((Float)vals.get(i)).floatValue();
		
		return arr;
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
	
	
	/** Allows user to toggle btw mesh and surface plot of the data
	 * @author BLM*/
	public void setView(int type)
	{
		if (type == TYPE_SURFACEPLOT)
			ThePlot.setAppearance(Appearance_Surface);
		else if (type == TYPE_MESHPLOT)
			ThePlot.setAppearance(Appearance_Mesh);
		else if (type == TYPE_POINTPLOT)
			ThePlot.setAppearance(Appearance_Point);
	}
	
	/** When assinging a new colorMap, this method goes through each point in the TriangleArray
	 * and changes its color
	 * @author BLM*/
//	public void assignNewColors(int type)
//	{
//		int len = SurfacePlotMainPanel.TheDataSet.getNumPoints();
//		float[] colorTemp = new float[3];
//		for (int i =0; i < len; i++)
//		{
//			ColorMaps.getColorValue(SurfacePlotMainPanel.TheDataSet.getPoint(i).z, SurfacePlotMainPanel.TheDataSet.getZMin(), SurfacePlotMainPanel.TheDataSet.getZMax() , colorTemp, type);
//			ThePlot_Geometry.setColor(i,colorTemp);
//		}
//	}
	
	
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
	
	
//	public void setLogAxis()
//	{
//		TheCanvas.getView().stopView();
//		Point3f point = new Point3f(0,0,0);
//		int len  = ThePlot_Geometry.getVertexCount();
//		for (int i =0; i < len; i++)
//		{
//			ThePlot_Geometry.getCoordinate(i,point);
//		}
//		TheCanvas.getView().startView();
//	}
}

