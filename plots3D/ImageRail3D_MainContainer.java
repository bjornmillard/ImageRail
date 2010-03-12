/**
 * MainFrame.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class ImageRail3D_MainContainer extends JPanel
{
	static public File OutputDirectory;
	static public ImageRail3D_PlotPanel ThePlotPanel;
	static public ControlPanel TheControlPanel;
	static public JToolBar TheToolBar;
	static public boolean ShowBoxes;
	static public boolean ShowRibbons;
	static public boolean ShowFrame;
	public Vector3d TheCurrentTranslation;
	public double[][] TheData;
	public double[][] TheColorValues;
	
	public ImageRail3D_MainContainer(double[][] data, double[][] colorValues)
	{
		setPreferredSize(new Dimension(1000,1000));
		setVisible(true);
		setLayout(new BorderLayout());
		TheCurrentTranslation = new Vector3d();
		
		TheData = data;
		TheColorValues = colorValues;
		
		ShowBoxes = false;
		ShowFrame = true;
		ShowRibbons = true;
		
		
		ThePlotPanel  = new ImageRail3D_PlotPanel(data, colorValues);
		TheToolBar = new JToolBar();
		
		
		ThePlotPanel.Render_Frame.getRenderingAttributes().setVisible(ShowFrame);
		ThePlotPanel.Render_Boxes.getRenderingAttributes().setVisible(ShowBoxes);
		ThePlotPanel.Render_Ribbons.getRenderingAttributes().setVisible(ShowRibbons);
		
		//Show Wire Axis frame button
		JButton button = new JButton(tools.Icons.Icon_Frame);
		button.setToolTipText("Show Axis");
		button.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						ShowFrame = !ShowFrame;
						ThePlotPanel.Render_Frame.getRenderingAttributes().setVisible(ShowFrame);
					}
				});
		TheToolBar.add(button);
		
		//Show 3D histogram Boxes
		button = new JButton(tools.Icons.Icon_Histo3d);
		button.setToolTipText("Histogram Bars");
		button.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						ShowBoxes = !ShowBoxes;
						ThePlotPanel.Render_Boxes.getRenderingAttributes().setVisible(ShowBoxes);
						repaint();
					}
				});
		TheToolBar.add(button);
		
		//Show Ribbon Plots
		button = new JButton(tools.Icons.Icon_Ribbons3d);
		button.setToolTipText("Ribbon Plot");
		button.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						ShowRibbons = !ShowRibbons;
						ThePlotPanel.Render_Ribbons.getRenderingAttributes().setVisible(ShowRibbons);
						repaint();
					}
				});
		TheToolBar.add(button);
		
		//Inverts the data across axis and replots it in new frame
		button = new JButton("Invert");
		button.setToolTipText("Invert Data Series");
		button.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						int drow = TheData.length;
						int dcol = TheData[0].length;
						double[][] newData = new double[drow][dcol];
						double[][] newColors = new double[drow][dcol];
						for (int r = 0; r < drow; r++)
							for (int c = 0; c < dcol; c++)
							{
								newData[r][c] = TheData[drow-1-r][c];
								newColors[r][c] = TheColorValues[drow-1-r][c];
							}
						
						ImageRail3D_Frame.load(newData, newColors);
					}
				});
		TheToolBar.add(button);
		
		//Capture Image of plot
		button = new JButton(tools.Icons.Icon_Camera);
		button.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						ThePlotPanel.takeScreenShot();
					}
				});
		TheToolBar.add(button);
		TheToolBar.setOrientation(JToolBar.VERTICAL);
		
		add("Center", ThePlotPanel);
		add("West", TheToolBar);
		
		validate();
		repaint();
	}
	
	
	
	public void resetView()
	{
		ThePlotPanel.resetView();
	}
	
	
//	/** Allows for removal of current plot and addition of the given point set and sets the given axis labels where label[0] = xAxis, label[1] = y, and label[2] = z
//	 * @author BLM*/
//	public void loadDataSet(Point3d[] points, String[] axisLabels, double[][] axisRanges ,int[] axisScaleTypes)
//	{
//		if (points!=null)
//		{
//			SurfacePlotPanel newPanel = new SurfacePlotPanel(points, axisLabels, axisRanges,axisScaleTypes);
//			if (ThePlotPanel!=null)
//				remove(ThePlotPanel);
//			ThePlotPanel = newPanel;
//			add("Center", ThePlotPanel);
//			validate();
//			repaint();
//		}
//	}
	
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
//		TheOriginalData = new DataSet(SurfacePlotMainPanel.getPointsFromFile(), AxisScaleTypes);
//		loadDataSet(TheOriginalData.getPoints(), labels, AxisRanges, AxisScaleTypes);
//	}
//
}
