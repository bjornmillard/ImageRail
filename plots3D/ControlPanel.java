/**
 * ControlPanel_1.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

import java.awt.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Enumeration;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

public class ControlPanel extends JPanel
{
	public SurfacePlotMainPanel TheSurfacePanel;
	public ImageRail3D_MainContainer TheHistogramPanel;
	public ControlPanel TheControlPanel;
	public Transform3D t3d;
	public Matrix3d mat;
	public AxisAngle4d angles;
	public Color[] grays;
	public Font font;
	public NumberFormat nf = NumberFormat.getInstance();
	public int Type;
	static public final int SURFACEPLOT = 0;
	static public final int HISTOGRAMPLOT = 1;
	
	public ControlPanel(SurfacePlotMainPanel theMainPanel, int type)
	{
		Type = type;
		TheSurfacePanel = theMainPanel;
		TheControlPanel = this;
		setPreferredSize(new Dimension(130,100));
		setLayout(new GridLayout(8,0));
		t3d = new Transform3D();
		mat = new Matrix3d();
		angles = new AxisAngle4d();
		updateAngles();
		
		grays = new Color[5];
		grays[0] = new Color(0.4f, 0.4f, 0.4f);
		grays[1] = new Color(0.5f, 0.5f, 0.5f);
		grays[2] = new Color(0.7f, 0.7f, 0.7f);
		grays[3] = new Color(0.8f, 0.8f, 0.8f);
		grays[4] = new Color(0.9f, 0.9f, 0.9f);
		font = new Font("Helvetica", Font.BOLD, 12);
		
		nf.setMaximumFractionDigits(2);
		
	
		
		add(new GraphTypePanel());
		add(new AxisLabelsPanel());
		add(new ViewAnglePanel());
		add(new ColorPanel());
		add(new ColormapPanel());
		add(new SnapshotButtonPanel());
//		add(new LogAxisPanel());
		
	}
	
	
	public ControlPanel(ImageRail3D_MainContainer theMainPanel, int type)
	{
		Type = type;
		TheHistogramPanel = theMainPanel;
		TheControlPanel = this;
		setPreferredSize(new Dimension(130,100));
		setLayout(new GridLayout(8,0));
		t3d = new Transform3D();
		mat = new Matrix3d();
		angles = new AxisAngle4d();
		updateAngles();
		
		font = new Font("Helvetica", Font.BOLD, 12);
		
		nf.setMaximumFractionDigits(2);
		
		add(new AxisLabelsPanel());
		add(new ColorPanel());
		add(new SnapshotButtonPanel());
	}
	
	
	
	public void updateAngles()
	{
		if (Type == SURFACEPLOT)
		{
			ThreeDPanel_SurfacePlot.MainTransformGroup.getTransform(t3d);
			t3d.get(mat);
			t3d.get(TheSurfacePanel.TheCurrentTranslation);
		}
		else if (Type == HISTOGRAMPLOT)
		{
			ThreeDPanel_Histogram.MainTransformGroup.getTransform(t3d);
			t3d.get(mat);
			t3d.get(TheHistogramPanel.TheCurrentTranslation);
		}
		
		
		double trace = 0;
		trace = mat.m00 + mat.m11 + mat.m22;
		double angle = Math.acos((trace-1f)/2f);
		Vector3d axis = new Vector3d();
		axis.x  = 1f/(2f*Math.sin(angle))*(mat.m21-mat.m12);
		axis.y  = 1f/(2f*Math.sin(angle))*(mat.m02-mat.m20);
		axis.z  = 1f/(2f*Math.sin(angle))*(mat.m10-mat.m01);
		
		angles.angle = angle;
		angles.x = axis.x;
		angles.y = axis.y;
		angles.z = axis.z;
	}
	
	/** Button that lets the user to take a snapshot of the Graph
	 * @author BLM*/
	public class SnapshotButtonPanel extends JPanel
	{
		public SnapshotButtonPanel()
		{
			JButton button = new JButton("Screenshot");
			button.addActionListener(new ActionListener()
									 {
						public void actionPerformed(ActionEvent ae)
						{
							
							if (Type == SURFACEPLOT)
							{
								TheSurfacePanel.ThePlotPanel.takeScreenShot();
							}
							else if (Type == HISTOGRAMPLOT)
							{
								TheHistogramPanel.ThePlotPanel.takeScreenShot();
							}
							
							
						}
					});
			add("Center", button);
		}
		
	}
	
	
	public class AxisLabelsPanel extends JPanel
	{
		private JTextField[] textField;
		
		public AxisLabelsPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Axis Labels"));
			setLayout(new GridLayout(3,0));
			
			if (Type == SURFACEPLOT)
			{
				textField = new JTextField[3];
				textField[0] = new JTextField();
				textField[0].setText(TheSurfacePanel.ThePlotPanel.TheAxisLabelText[0]);
				textField[0].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[0].getText();
								TheSurfacePanel.ThePlotPanel.setAxisText_X(text);
								textField[0].setText(text);
							}
						});
				add(textField[0]);
				
				textField[1] = new JTextField();
				textField[1].setText(TheSurfacePanel.ThePlotPanel.TheAxisLabelText[1]);
				textField[1].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[1].getText();
								TheSurfacePanel.ThePlotPanel.setAxisText_Y(text);
								textField[1].setText(text);
							}
						});
				add(textField[1]);
				
				textField[2] = new JTextField();
				textField[2].setText(TheSurfacePanel.ThePlotPanel.TheAxisLabelText[2]);
				textField[2].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[2].getText();
								TheSurfacePanel.ThePlotPanel.setAxisText_Z(text);
								textField[2].setText(text);
							}
						});
				add(textField[2]);
			}
			else if (Type == HISTOGRAMPLOT)
			{
				textField = new JTextField[3];
				textField[0] = new JTextField();
				textField[0].setText(TheHistogramPanel.ThePlotPanel.TheAxisLabelText[0]);
				textField[0].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[0].getText();
								TheHistogramPanel.ThePlotPanel.setAxisText_X(text);
								textField[0].setText(text);
							}
						});
				add(textField[0]);
				
				textField[1] = new JTextField();
				textField[1].setText(TheHistogramPanel.ThePlotPanel.TheAxisLabelText[1]);
				textField[1].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[1].getText();
								TheHistogramPanel.ThePlotPanel.setAxisText_Y(text);
								textField[1].setText(text);
							}
						});
				add(textField[1]);
				
				textField[2] = new JTextField();
				textField[2].setText(TheHistogramPanel.ThePlotPanel.TheAxisLabelText[2]);
				textField[2].addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								String text = textField[2].getText();
								TheHistogramPanel.ThePlotPanel.setAxisText_Z(text);
								textField[2].setText(text);
							}
						});
				add(textField[2]);
			}
			
		}
	}
	
	
	private class BackgroundColorPanel extends JPanel
	{
		private JColorChooser tcc;
		private BackgroundColorPanel()
		{
			tcc = new JColorChooser();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			tcc.setLocation((int)(d.width/2f)-tcc.getWidth()/2,(int)(d.height/2f)-tcc.getHeight()/2);
			JButton button = new JButton("Background");
//			button.setPreferredSize(new Dimension(100,30));
			button.setFont(font);
			button.addActionListener(new ActionListener()
									 {
						public void actionPerformed(ActionEvent ae)
						{
							Color newColor = tcc.showDialog(TheControlPanel, "Background Color", Color.black);
							if (newColor != null)
							{
								if (Type == SURFACEPLOT)
								{
									TheSurfacePanel.ThePlotPanel.TheBackground.setColor(new Color3f(newColor));
								}
								else if (Type == HISTOGRAMPLOT)
								{
									TheHistogramPanel.ThePlotPanel.TheBackground.setColor(new Color3f(newColor));
								}
								
							}
							
						}
					});
			add( button);
		}
	}
	
	private class TextColorPanel extends JPanel
	{
		private JColorChooser tcc;
		private TextColorPanel()
		{
			tcc = new JColorChooser();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			tcc.setLocation((int)(d.width/2f)-tcc.getWidth()/2,(int)(d.height/2f)-tcc.getHeight()/2);
			JButton button = new JButton("Text Color");
//			button.setPreferredSize(new Dimension(100,30));
			button.setFont(font);
			button.addActionListener(new ActionListener()
									 {
						public void actionPerformed(ActionEvent ae)
						{
							Color newColor = tcc.showDialog(TheControlPanel, "Text Color", Color.black);
							if(newColor!=null)
							{
								Color3f newColor3f= new Color3f(newColor);
								if (newColor != null)
								{
									if (Type == SURFACEPLOT)
									{
										//
										// Changing the axis label colors
										//
										int len  = TheSurfacePanel.ThePlotPanel.TheAxisLabelsTG.size();
										for (int i =0; i < len ;i++)
										{
											Enumeration elem  = (((TransformGroup)TheSurfacePanel.ThePlotPanel.TheAxisLabelsTG.get(i)).getAllChildren());
											for (Enumeration e2 = elem ; e2.hasMoreElements() ;)
											{
												((OrientedShape3D)e2.nextElement()).setAppearance(Tools.getAppearanceOfColor(newColor3f));
											}
											
										}
										//
										// Changing the Frame color
										//
										Enumeration elem  = ((TheSurfacePanel.ThePlotPanel.TheBoxFrameTG).getAllChildren());
										for (Enumeration e = elem ; e.hasMoreElements() ;)
										{
											TheSurfacePanel.ThePlotPanel.TheBoxFrame.setColor(newColor3f);
											((Shape3D)e.nextElement()).setAppearance(Tools.getAppearanceOfColor(newColor3f));
										}
										
										
										//
										// Changing the Tick label colors
										//
										for(int i =0; i < 3; i++)
										{
											AxisTicks ticks = TheSurfacePanel.ThePlotPanel.TheTicks[i];
											ticks.setColor(newColor3f);
											ticks.TheLineArrayShape.setAppearance((Tools.getAppearanceOfColor(newColor3f)));
											//Changing the tick text colors
											int num = ticks.TheTextShapes.size();
											for (int j = 0; j < num; j++)
													((OrientedShape3D)ticks.TheTextShapes.get(j)).setAppearance((Tools.getAppearanceOfColor(newColor3f)));
										}
									}
									else if (Type == HISTOGRAMPLOT)
									{
										//
										// Changing the axis label colors
										//
										int len  = TheHistogramPanel.ThePlotPanel.TheAxisLabelsTG.size();
										for (int i =0; i < len ;i++)
										{
											Enumeration elem  = (((TransformGroup)TheHistogramPanel.ThePlotPanel.TheAxisLabelsTG.get(i)).getAllChildren());
											for (Enumeration e2 = elem ; e2.hasMoreElements() ;)
											{
												((OrientedShape3D)e2.nextElement()).setAppearance(Tools.getAppearanceOfColor(newColor3f));
											}
											
										}
										//
										// Changing the Frame color
										//
										Enumeration elem  = ((TheHistogramPanel.ThePlotPanel.TheBoxFrameTG).getAllChildren());
										for (Enumeration e = elem ; e.hasMoreElements() ;)
										{
											TheHistogramPanel.ThePlotPanel.TheBoxFrame.setColor(newColor3f);
											((Shape3D)e.nextElement()).setAppearance(Tools.getAppearanceOfColor(newColor3f));
										}
										
									}
								}
							}
							
						}
					});
			add(button);
		}
	}
	
	private class FillerPanel extends JPanel
	{
		private int height = 30;
		private FillerPanel()
		{
			setPreferredSize(new Dimension(getWidth(), height));
			
		}
	}
	
	
	
	
	public class ColorPanel extends JPanel
	{
		public ColorPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Colors"));
			setLayout(new GridLayout(2,0));
			add(new TextColorPanel());
			add(new BackgroundColorPanel());
		}
	}
	
	
	
	
	
	
	/** Button that lets the user reset the viewing angles
	 * @author BLM*/
	public class ResetViewButton extends JButton
	{
		private Rectangle[] boxes;
		private int numBoxes = 4;
		
		
		public ResetViewButton()
		{
			setFont(font);
			setText("Reset View");
			boxes = new Rectangle[numBoxes];
			for (int i = 0; i < numBoxes; i++)
			{
				boxes[i] = new Rectangle();
				boxes[i].height = 22-2*i ;
				boxes[i].width = getText().length()*5+3-2*i +2*4;
			}
		}
		
		
	}
	
	/** Buttons that display the view angles and also allow for user to double click and change the angles
	 * @author BLM*/
	public class AngleBox extends JButton
	{
		private String message;
		public AngleBox()
		{
			setSize(TheControlPanel.getWidth(), 50);
			Font newFont = new Font("Helvetica", Font.BOLD, 9);
			setFont(newFont);
			updateMessage();
			
		}
		
		public void updateMessage()
		{
			message ="["+nf.format(angles.x)+" , "+nf.format(angles.y)+" , "+nf.format(angles.z)+"]";
			setText(message);
		}
		
	}
	/** Buttons that display the view angles and also allow for user to double click and change the angles
	 * @author BLM*/
	public class RotationBox extends JButton
	{
		private String message;
		
		public RotationBox()
		{
			Font newFont = new Font("Helvetica", Font.BOLD, 9);
			setFont(newFont);
			updateMessage();
		}
		
		public void updateMessage()
		{
			message= "Radians = "+nf.format(angles.angle);
			setText(message);
		}
		
		
	}
	
	
	
	public class ViewAnglePanel extends JPanel
	{
		private AngleBox axisBox;
		private RotationBox rotationBox;
		private ResetViewButton resetViewBox;
		
		public ViewAnglePanel()
		{
			if (Type == SURFACEPLOT)
			{
				setBorder(BorderFactory.createTitledBorder("View Angle"));
				setPreferredSize(new Dimension(TheSurfacePanel.getWidth(), 100));
				setLayout(new GridLayout(3,0));
				
				//making the resetViewButton
				resetViewBox = new ResetViewButton();
				resetViewBox.addActionListener(new ActionListener()
											   {
							public void actionPerformed(ActionEvent ae)
							{
								TheSurfacePanel.resetView();
							}
						});
				add(resetViewBox,0);
				
				axisBox = new AngleBox();
				axisBox.addActionListener(new ActionListener()
										  {
							public void actionPerformed(ActionEvent ae)
							{
								Dialog_NewAxisInput di = new Dialog_NewAxisInput(TheSurfacePanel);
								di.setVisible(true);
							}
						});
				add(axisBox,1);
				
				rotationBox = new RotationBox();
				rotationBox.addActionListener(new ActionListener()
											  {
							public void actionPerformed(ActionEvent ae)
							{
								Dialog_NewAngleInput di = new Dialog_NewAngleInput(TheSurfacePanel);
								di.setVisible(true);
							}
						});
				add(rotationBox,2);
			}
			
			
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			
			updateAngles();
			axisBox.updateMessage();
			rotationBox.updateMessage();
		}
	}
	
	
	/** Button that lets the user change the axis-bounds of the Plot
	 * @author BLM*/
	private class AxisBoundsPanel extends JPanel
	{
		public AxisBoundsPanel()
		{
//			setBorder(BorderFactory.createTitledBorder("Take Screenshot"));
			JButton button = new JButton("Change Bounds");
			button.addActionListener(new ActionListener()
									 {
						public void actionPerformed(ActionEvent ae)
						{
							Dialog_NewAxisBoundsInput di = new Dialog_NewAxisBoundsInput(TheSurfacePanel);
							di.setVisible(true);
						}
					});
			add(button);
		}
	}
	
	/** Button that lets the user change the colormap
	 * @author BLM*/
	private class ColormapPanel extends JPanel
	{
		public ColormapPanel()
		{
//			setBorder(BorderFactory.createTitledBorder("Take Screenshot"));
			JButton button = new JButton("ColorMap");
			button.addActionListener(new ActionListener()
									 {
						public void actionPerformed(ActionEvent ae)
						{
							Dialog_NewColorMap di = new Dialog_NewColorMap(TheSurfacePanel);
							di.setVisible(true);
						}
					});
			add(button);
		}
		
	}
	
	
	private class GraphTypePanel extends JPanel
	{
		final JRadioButton jrb_surface = new JRadioButton("Surface");
		final JRadioButton jrb_mesh = new JRadioButton("Mesh");
		final JRadioButton jrb_points = new JRadioButton("Points");
		
		private GraphTypePanel()
		{
			setBorder(BorderFactory.createTitledBorder("Display Type"));
			setLayout(new GridLayout(3,0));
			//adding the raidobuttons for the graph type and a label at top
			
			
			jrb_surface.setSelected(true);
			ButtonGroup bg0 = new ButtonGroup();
			bg0.add(jrb_mesh);
			bg0.add(jrb_surface);
			bg0.add(jrb_points);
			
			jrb_mesh.setFont(font);
			jrb_points.setFont(font);
			jrb_surface.setFont(font);
			add(jrb_points);
			add(jrb_mesh);
			add(jrb_surface);
			
			//adding functionality to the radiobuttons
			jrb_surface.addActionListener(new ActionListener()
										  {
						public void actionPerformed(ActionEvent ae)
						{
							if (jrb_surface.isSelected())
							{
								TheSurfacePanel.ThePlotPanel.setView(SurfacePlotPanel.TYPE_SURFACEPLOT);
							}
						}
					});
			jrb_mesh.addActionListener(new ActionListener()
									   {
						public void actionPerformed(ActionEvent ae)
						{
							if (jrb_mesh.isSelected())
							{
								TheSurfacePanel.ThePlotPanel.setView(SurfacePlotPanel.TYPE_MESHPLOT);
							}
						}
					});
			jrb_points.addActionListener(new ActionListener()
										 {
						public void actionPerformed(ActionEvent ae)
						{
							if (jrb_points.isSelected())
							{
								TheSurfacePanel.ThePlotPanel.setView(SurfacePlotPanel.TYPE_POINTPLOT);
							}
						}
					});
			
			
		}
		
	}
	
	
	
	
}
