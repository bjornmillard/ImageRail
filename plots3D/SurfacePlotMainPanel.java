/**
 * MainFrame.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SurfacePlotMainPanel extends JPanel
{
	static public File OutputDirectory;
	static public SurfacePlotPanel ThePlotPanel;
	static public ControlPanel TheControlPanel;
	static public DataSet TheOriginalData;
	public  static int TheColorMap;
	public Vector3d TheCurrentTranslation;
	static public double[][] AxisRanges;
	static public int[] AxisScaleTypes;
	static public int LINEAR;
	static public int LOG;
	
	public SurfacePlotMainPanel(Point3d[] points, double[] colors, String[] axisLabels ,int[] axisScaleTypes, int colormap)
	{
		setPreferredSize(new Dimension(1000,1000));
		setVisible(true);
		setLayout(new BorderLayout());
		TheColorMap  = colormap;
		TheCurrentTranslation = new Vector3d();
		AxisScaleTypes = axisScaleTypes;
		TheOriginalData = new DataSet(points, axisScaleTypes);
		
		loadDataSet(points,colors, axisLabels, TheOriginalData.getAxisRanges(), AxisScaleTypes);
		TheControlPanel = new ControlPanel(this, ControlPanel.SURFACEPLOT);
		
		add("Center", ThePlotPanel);
		add("West", TheControlPanel);
		
		validate();
		repaint();
	}
	
	
	/** Lets other classes access what is the colormap to color the surface plot
	 * @author BLM*/
	public static int getColorMap()
	{
		return TheColorMap;
	}
	
	public void resetView()
	{
		ThePlotPanel.resetView();
	}
	public void setView(Matrix3d v, Vector3d trans)
	{
		ThePlotPanel.setView(v, trans);
	}
	
	/** Allows for removal of current plot and addition of the given point set and sets the given axis labels where label[0] = xAxis, label[1] = y, and label[2] = z
	 * @author BLM*/
	public void loadDataSet(Point3d[] points, double[] colors, String[] axisLabels, double[][] axisRanges ,int[] axisScaleTypes)
	{
		if (points!=null)
		{
			SurfacePlotPanel newPanel = new SurfacePlotPanel(points, colors, axisLabels, axisRanges,axisScaleTypes);
			if (ThePlotPanel!=null)
				remove(ThePlotPanel);
			ThePlotPanel = newPanel;
			add("Center", ThePlotPanel);
			validate();
			repaint();
		}
	}
	
	/** Sets the OutputDirectory for where to start the filechooose in the ScreenShot function
	 * @author BLM */
	public void setOutputDirectory(File out)
	{
		OutputDirectory = out;
	}
	
//	public void clearAndReload()
//	{
//		String[] labels = new String[3];
//		labels[0] = "axis_1";
//		labels[1] = "axis_2";
//		labels[2] = "axis_3";
//
//		Point3d[] points = null;
//		double[] colors = null;
//		try
//		{
//			points  = SurfacePlotMainPanel.parsePoints(SurfacePlotFrame.TheFile);
//			colors = SurfacePlotMainPanel.parseColors(SurfacePlotFrame.TheFile);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace(System.err);
//		}
//
//		TheOriginalData = new DataSet(points, colors, AxisScaleTypes);
//		loadDataSet(TheOriginalData.getPoints(), TheOriginalData.getColors(), labels, AxisRanges, AxisScaleTypes);
//	}
	
	
	
	
	/** Given an xml formated point file, this method parses out the data and returns an array of point3d's
	 * @author BLM*/
	static public Point3d[] parsePoints(File file) throws SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {}
		
		Document document = null;
		document = builder.parse(file);
		
		//parsing the points
		ArrayList points = new ArrayList();
		NodeList nl = document.getElementsByTagName("parsedData");
		org.w3c.dom.Node n = nl.item(0); //should only be one parsedData tag
		NodeList thePoints = n.getChildNodes();
		
		for (int i =0; i < thePoints.getLength(); i++)
		{
			org.w3c.dom.Node node = thePoints.item(i);
			//loading points
			if (node.getNodeName().equalsIgnoreCase("point"))
			{
				Point3d p = new Point3d();
				int length = (node.getAttributes() != null) ? node.getAttributes().getLength() : 0;
				for (int loopIndex = 0; loopIndex < length; loopIndex++)
				{
					Attr att =  (Attr)node.getAttributes().item(loopIndex);
					
					if (att.getNodeName().equalsIgnoreCase("x"))
					{
						String f = att.getNodeValue().trim();
						p.x = Double.parseDouble(f);
					}
					else if (att.getNodeName().equalsIgnoreCase("y"))
					{
						String f = att.getNodeValue().trim();
						p.y = Double.parseDouble(f);
					}
					else if (att.getNodeName().equalsIgnoreCase("z"))
					{
						String f = att.getNodeValue().trim();
						p.z = Double.parseDouble(f);
					}
				}
				points.add(p);
			}
		}
		//compiling points into an array
		int num = points.size();
		Point3d[] ps = new Point3d[num];
		for (int j = 0; j < num; j++)
			ps[j] = (Point3d)points.get(j);
		
		return ps;
	}
	
	/** Given an xml formated point file, this method parses out the data and returns an array of point3d's
	 * @author BLM*/
	static public double[] parseColors(File file, String colorTag) throws SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {}
		
		Document document = null;
		document = builder.parse(file);
		
		//parsing the points
		ArrayList colors = new ArrayList();
		NodeList nl = document.getElementsByTagName("parsedData");
		org.w3c.dom.Node n = nl.item(0); //should only be one parsedData tag
		NodeList thePoints = n.getChildNodes();
		
		for (int i =0; i < thePoints.getLength(); i++)
		{
			org.w3c.dom.Node node = thePoints.item(i);
			//loading points
			if (node.getNodeName().equalsIgnoreCase("point"))
			{
				Double d  = null;
				int length = (node.getAttributes() != null) ? node.getAttributes().getLength() : 0;
				for (int loopIndex = 0; loopIndex < length; loopIndex++)
				{
					Attr att =  (Attr)node.getAttributes().item(loopIndex);
					
					if (att.getNodeName().equalsIgnoreCase(colorTag))
					{
						String f = att.getNodeValue().trim();
						d = new Double(Double.parseDouble(f));
					}
				}
				colors.add(d);
			}
		}
		//compiling points into an array
		int num = colors.size();
		double[] ps = new double[num];
		for (int j = 0; j < num; j++)
			ps[j] = ((Double)colors.get(j)).doubleValue();
		
		return ps;
	}
	
	/** Given an xml formated point file, this method parses out the data and returns an array of point3d's
	 * @author BLM*/
	static public double[] parseIC50s(File file) throws SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {}
		
		Document document = null;
		document = builder.parse(file);
		
		//parsing the points
		ArrayList values = new ArrayList();
		NodeList nl = document.getElementsByTagName("parsedData");
		org.w3c.dom.Node n = nl.item(0); //should only be one parsedData tag
		NodeList thePoints = n.getChildNodes();
		
		for (int i =0; i < thePoints.getLength(); i++)
		{
			org.w3c.dom.Node node = thePoints.item(i);
			//loading points
			if (node.getNodeName().equalsIgnoreCase("IC50"))
			{
				Double d  = null;
				int length = (node.getAttributes() != null) ? node.getAttributes().getLength() : 0;
				for (int loopIndex = 0; loopIndex < length; loopIndex++)
				{
					Attr att =  (Attr)node.getAttributes().item(loopIndex);
					
					if (att.getNodeName().equalsIgnoreCase("value"))
					{
						String f = att.getNodeValue().trim();
						d = new Double(Double.parseDouble(f));
					}
				}
				values.add(d);
			}
		}
		//compiling points into an array
		int num = values.size();
		double[] ps = new double[num];
		for (int j = 0; j < num; j++)
		{
			ps[j] = ((Double)values.get(j)).doubleValue();
			System.out.println("IC: "+ps[j]);
		}
		
		return ps;
	}
	
	
}
