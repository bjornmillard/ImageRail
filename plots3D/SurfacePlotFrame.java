/**
 * MainFrame.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;


import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

public class SurfacePlotFrame extends JFrame
{
	static public File TheFile;
	
	public SurfacePlotFrame(Point3d[] points, double[] colors)
	{
		super("3D plotter");
		setResizable(true);
		setSize(900,900);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Setting up the colormap menu
//		JMenu ViewMenu_colormaps = new JMenu("Color Map");
//		ViewMenu_colormaps.setEnabled(false);
//		ViewMenu.add(ViewMenu_colormaps);
//		final JRadioButtonMenuItem jrb_cm_standard = new JRadioButtonMenuItem("Standard");
//		final JRadioButtonMenuItem jrb_cm_pseudocolor = new JRadioButtonMenuItem("Pseudocolor");
//		jrb_cm_standard.setSelected(true);
//		ButtonGroup bg1 = new ButtonGroup();
//		bg1.add(jrb_cm_standard);
//		bg1.add(jrb_cm_pseudocolor);
//
//		jrb_cm_standard.addActionListener(new ActionListener()
//										  {
//					public void actionPerformed(ActionEvent ae)
//					{
//						if (jrb_cm_standard.isSelected())
//						{
//							ThePanel.assignNewColors(ColorMaps.COLORMAP_STANDARD);
//						}
//					}
//				});
//		ViewMenu_colormaps.add(jrb_cm_standard);
//		jrb_cm_pseudocolor.addActionListener(new ActionListener()
//											 {
//					public void actionPerformed(ActionEvent ae)
//					{
//						if (jrb_cm_pseudocolor.isSelected())
//						{
//							ThePanel.assignNewColors(ColorMaps.COLORMAP_PSEUDOCOLOR);
//						}
//					}
//				});
//		ViewMenu_colormaps.add(jrb_cm_pseudocolor);
		
		String[] labels = new String[3];
		labels[0] = "axis_1";
		labels[1] = "axis_2";
		labels[2] = "axis_3";
		
		
		int[] scales = new int[3];
		scales[0] = SurfacePlotMainPanel.LINEAR;
		scales[1] = SurfacePlotMainPanel.LINEAR;
		scales[2] = SurfacePlotMainPanel.LINEAR;
		
	    int colormap = ColorMaps.RGB_1;
//		int colormap = colormaps.ColorMaps.FIRE;
//		int colormap = colormaps.ColorMaps.COLORMAP_STANDARD3;
		getContentPane().add(new SurfacePlotMainPanel(points, colors, labels , scales, colormap));
	}
	
	
	public static void main(String[] args)
	{
		load();
	}
	
	static public void load()
	{
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(new JPanel());
		TheFile = null;
		Point3d[] points = null;
		double[] colors = null;
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			TheFile = fc.getSelectedFile();
			try
			{
				points  = SurfacePlotMainPanel.parsePoints(TheFile);
				colors = SurfacePlotMainPanel.parseColors(TheFile, "colorValue");
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
			
		}
		else
			System.out.println("Save command cancelled by user." );
		
		if (colors!=null && points!=null)
		{
			SurfacePlotFrame f = new SurfacePlotFrame(points, colors);
			f.setVisible(true);
		}
		else
		{
			System.out.println("ERROR parsing file");
		}
	}
	
	
}
