/**  
   ImageRail:
   Software for high-throughput microscopy image analysis

   Copyright (C) 2011 Bjorn Millard <bjornmillard@gmail.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package imageViewers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Field;
import models.Model_PlateRepository;
import models.Model_Well;

public class FieldViewer_Frame extends JFrame implements WindowListener, KeyListener
{
	private JSlider MovieFrameSlider;
	private FieldViewer[] TheFieldViewers;
	private FieldViewer TheCurrentViewer;
	private JCheckBoxMenuItem PlotBackgroundCheckBox;
	private int Type_Shape;
	//CONSTANTS
	static public int SHAPE_RECTANGLE = 0;
	static public int SHAPE_OVAL = 1;
	static public int SHAPE_POLYGON = 2;
	static public int SHAPE_LINE = 3;
	
	
	public FieldViewer_Frame()
	{
		super("Image Viewer");
		setResizable(true);
		Type_Shape = SHAPE_RECTANGLE;
		setLayout(new BorderLayout());

		//setting up the menubar
		JMenuBar TheMenuBar = new JMenuBar();
		JMenu FileMenu = new JMenu("File");
		TheMenuBar.add(FileMenu);
		JMenu OptionsMenu = new JMenu("Options");
		TheMenuBar.add(OptionsMenu);
		addWindowListener(this);
		setJMenuBar(TheMenuBar);
		setVisible(true);
		
		addKeyListener(this);
		this.setFocusable(true);
		requestFocus();
		
		
		//Setting up the ImageScalings
		JMenu scalingMenu = new JMenu("Image Scale");
		for (int i = 0; i < models.Model_Main.getModel().getGUI()
				.getTheImageScalings().length; i++)
		{
			models.Model_Main.getModel().getGUI().getTheImageScalings()[i]
					.addActionListener(new ActionListener()
																		{
						public void actionPerformed(ActionEvent ae)
						{
							
							TheCurrentViewer.updateAllImages();
							MovieFrameSlider.repaint();
						}
					});
			scalingMenu.add(models.Model_Main.getModel().getGUI()
					.getTheImageScalings()[i]);
		}
		
		FileMenu.add(scalingMenu);
		FileMenu.addSeparator();
		
		
		JMenuItem item = new JMenuItem("Close");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
												   ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						close();
					}
				});
		FileMenu.add(item);
		
		
		
		item = new JMenuItem("Threshold Wizard");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (Type_Shape == SHAPE_RECTANGLE) {
					Rectangle rect = (Rectangle) TheCurrentViewer
							.getScaledSelectionBounds(getScaling());
					if (rect == null || rect.width <= 2 || rect.height <= 2) // nothing
																				// selected
					{
						JOptionPane
								.showMessageDialog(
										null,
										"No selection made. \nPlease highlight a region and try again.",
										"Screen Shot",
										JOptionPane.ERROR_MESSAGE);
					} else {
						TiledImage t = new TiledImage(TheCurrentViewer
								.getTheDisplayedImage(), true);
						TiledImage ts = t.getSubImage(rect.x, rect.y,
								rect.width, rect.height);
						BufferedImage bf = ts.getAsBufferedImage();

						TiledImage t2 = new TiledImage(TheCurrentViewer
								.getTheCurrentImage(), true);
						TiledImage ts2 = t2.getSubImage(rect.x, rect.y,
								rect.width, rect.height);
						BufferedImage bf2 = ts2.getAsBufferedImage();

						ThresholdWizard tw = new ThresholdWizard(bf,
 bf2
								.getData());

						}

				}
			}
		});
		OptionsMenu.add(item);
		OptionsMenu.addSeparator();
		item = new JMenuItem("Delete Selected Cells");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				deleteSelectedCells(TheCurrentViewer.getTheWell());
				TheCurrentViewer.updatePanel();
				TheCurrentViewer.updateAllImages();
			}
		});
		OptionsMenu.add(item);
		OptionsMenu.addSeparator();

		item = new JMenuItem("Save Image Selection");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
				if (Type_Shape == SHAPE_RECTANGLE)
						{
							Rectangle rect = (Rectangle)TheCurrentViewer.getScaledSelectionBounds(getScaling());
							if (rect==null || rect.width<=2 ||rect.height<=2) //nothing selected
							{
								JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
															  "Screen Shot",JOptionPane.ERROR_MESSAGE);
							}
							else
							{
								TiledImage t = new TiledImage(TheCurrentViewer.getTheDisplayedImage(), true);
								TiledImage ts = t.getSubImage(rect.x, rect.y, rect.width, rect.height);
								BufferedImage bf = ts.getAsBufferedImage();
								
								JFileChooser fc = null;
								if (models.Model_Main.getModel().getTheDirectory()!=null)
									fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
								else
									fc = new JFileChooser();
								
								File outDir = null;
								
								fc.setDialogTitle("Save as...");
						int returnVal = fc.showSaveDialog(models.Model_Main
								.getModel().getGUI());
								if (returnVal == JFileChooser.APPROVE_OPTION)
								{
									outDir = fc.getSelectedFile();
								}
								else
									System.out.println("Open command cancelled by user." );
								
								if (outDir!=null)
								{
									models.Model_Main.getModel().setTheDirectory(new File(outDir.getParent()));
									//
									//		PNG image format
									//
									try
									{
										outDir = (new File(outDir.getAbsolutePath()+".png"));
										ImageIO.write(bf, "png", outDir);
									}
									catch (IOException e) {}
								}
							}
						}
					}
				});
		OptionsMenu.add(item);
		OptionsMenu.addSeparator();
		
		 item = new JMenuItem("Create RGB Image");
		 item.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent ae) {
			
//				 PlanarImage[] inputs = new PlanarImage[args.length]; 
//				 for(int im=0;im<args.length;im++)
//					 inputs[im] = JAI.create("fileload", args[im]); 
//				 
//				 int numF = 3;
//				 
//				 ParameterBlock pb = new ParameterBlock(); 
//				 for(int im=0;im<numF;im++)
//					 pb.setSource(inputs[im], im); 
//				 PlanarImage result = JAI.create("bandmerge",pb,null); 
//				 
//				 JAI.create("filestore",result,"multiband.tiff","TIFF");

				 
//			 // Getting rasters to print to RGB
//			 int[][][] im = TheCurrentViewer.getImageRaster_Banded();
//			 int width = im.length;
//			 int height = im[0].length;
//			 int numChannels = im[0][0].length;
//			
//			 // We need a sample model for color images where the pixels are
//			 // bytes, with three bands.
//			 SampleModel sampleModel = RasterFactory
//			 .createBandedSampleModel(DataBuffer.TYPE_BYTE, width,
//			 height, numChannels);
//			 // Create a TiledImage using the SampleModel.
//			 TiledImage tiledImage = new TiledImage(0, 0, width, height, 0,
//			 0, sampleModel, null);
//			 // Get a raster for the single tile.
//			 WritableRaster raster = tiledImage.getWritableTile(0, 0);
//			 int[] pixVal = new int[3];
//			 for (int w = 0; w < width; w++) {
//			 for (int h = 0; h < height; h++) {
//			 for (int c = 0; c < numChannels; c++)
//			 pixVal[c] = im[w][h][c];
//			 raster.setPixel(w, h, pixVal);
//			 }
//			 }
//			
//			 // Save the image on a file.
//			 JAI.create("filestore", tiledImage, "jairgb.png", "PNG");
			 }
		 });
//		 OptionsMenu.add(item);
//		 OptionsMenu.addSeparator();

		JMenu SelectionShapeMenu = new JMenu("Selection Shape...");
		OptionsMenu.add(SelectionShapeMenu);
		JRadioButtonMenuItem square = new JRadioButtonMenuItem("Rectangle");
		square.setSelected(true);
		square.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
				Type_Shape = SHAPE_RECTANGLE;
					}
				});
		SelectionShapeMenu.add(square);
		JRadioButtonMenuItem oval = new JRadioButtonMenuItem("Oval");
		oval.setSelected(false);
		oval.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
				Type_Shape = SHAPE_OVAL;
					}
				});
		SelectionShapeMenu.add(oval);
		JRadioButtonMenuItem poly = new JRadioButtonMenuItem("Freeform Polygon");
		poly.setSelected(false);
		poly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Type_Shape = SHAPE_POLYGON;
			}
		});
		SelectionShapeMenu.add(poly);
		JRadioButtonMenuItem line = new JRadioButtonMenuItem("Freeform Line");
		line.setSelected(false);
		line.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Type_Shape = SHAPE_LINE;
			}
		});
		SelectionShapeMenu.add(line);

		ButtonGroup bg = new ButtonGroup();
		bg.add(poly);
		bg.add(oval);
		bg.add(square);
		bg.add(line);
		
		

		JMenu SelectionMenu = new JMenu("Selection Area...");
		OptionsMenu.add(SelectionMenu);
		
		
		
		
		item = new JMenuItem("Set Selection to all Image Fields");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
				if (Type_Shape == SHAPE_RECTANGLE)
						{
							Rectangle rect = (Rectangle)TheCurrentViewer.getSelectedROI();
							if (rect==null || rect.width<=2 ||rect.height<=2) //nothing selected
							{
								JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
															  "",JOptionPane.ERROR_MESSAGE);
							}
							else
							{
								int len = TheFieldViewers.length;
								for (int i = 0; i < len; i++)
								{
							TheFieldViewers[i].setShape(new Rectangle(rect.x,
									rect.y, rect.width, rect.height),
									SHAPE_RECTANGLE);
									
								}
							}
							
							
						}
 else if (Type_Shape == SHAPE_OVAL)
						{
							Ellipse2D.Float oval = (Ellipse2D.Float)TheCurrentViewer.getSelectedROI();
							if (oval==null || oval.width<=2 ||oval.height<=2) //nothing selected
							{
								JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
															  "",JOptionPane.ERROR_MESSAGE);
							}
							else
							{
								int len = TheFieldViewers.length;
								for (int i = 0; i < len; i++)
								{
									TheFieldViewers[i].setSelectedROI(new Ellipse2D.Float(oval.x, oval.y, oval.width, oval.height));
							Type_Shape = SHAPE_OVAL;
									TheFieldViewers[i].setCreateNewBox(false);
								}
							}
						}
 else if (Type_Shape == SHAPE_POLYGON)
	{
	 	Polygon poly = (Polygon) TheCurrentViewer.getSelectedROI();
		if (poly==null || poly.npoints<=0) //nothing selected
		{
			JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
										  "",JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			int len = TheFieldViewers.length;
			for (int i = 0; i < len; i++)
			{
				int nps = poly.npoints;
				int[] xps = new int[nps];
				int[] yps = new int[nps];
				for (int j = 0; j < nps; j++)
				{
					xps[j] = poly.xpoints[j];
					yps[j] = poly.ypoints[j];
				}
				
				TheFieldViewers[i].setSelectedROI( new Polygon(xps, yps, nps));
				Type_Shape = SHAPE_POLYGON;
				TheFieldViewers[i].setCreateNewBox(false);
			}
		}
	}
					}
				});
		SelectionMenu.add(item);
		  
		
		OptionsMenu.addSeparator();
		JMenu FieldMenu = new JMenu("ROI Options...");
		OptionsMenu.add(FieldMenu);
		
		JMenu SaveROIMenu = new JMenu("Set ROI...");
		JMenu DeleteROIMenu = new JMenu("Delete ROI...");
		JMenu PrintRIOMenu = new JMenu("Export...");
		FieldMenu.add(SaveROIMenu);
		FieldMenu.add(DeleteROIMenu);
		FieldMenu.add(PrintRIOMenu);
		
		item = new JMenuItem("This Field");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
				TheCurrentViewer.getTheField().addROI(
						TheCurrentViewer.getCopyOfROI(),
						TheCurrentViewer.getFieldDimensions());
						TheCurrentViewer.repaint();
					}
				});
		SaveROIMenu.add(item);
		
		item = new JMenuItem("All Fields");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						int len = TheFieldViewers.length;
						for (int i = 0; i < len; i++)
					TheFieldViewers[i].getTheField().addROI(
							TheFieldViewers[i].getCopyOfROI(),
							TheCurrentViewer.getFieldDimensions());
						
						TheCurrentViewer.repaint();
					}
				});
		SaveROIMenu.add(item);
		
		
		item = new JMenuItem("This Field");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						TheCurrentViewer.getTheField().deleteROIs();
						TheCurrentViewer.repaint();
					}
				});
		DeleteROIMenu.add(item);
		
		item = new JMenuItem("All Fields");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						int len = TheFieldViewers.length;
						for (int i = 0; i < len; i++)
							TheFieldViewers[i].getTheField().deleteROIs();
						
						TheCurrentViewer.repaint();
					}
				});
		DeleteROIMenu.add(item);
		
		item = new JMenuItem("Cells Bound by ROI");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						//Open Printwriter
						JFileChooser fc = null;
						if (models.Model_Main.getModel().getTheDirectory()!=null)
							fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
						else
							fc = new JFileChooser();
						
						File outDir = null;
						
						fc.setDialogTitle("Save as...");
						int returnVal = fc.showSaveDialog(TheCurrentViewer);
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							outDir = fc.getSelectedFile();
							outDir = (new File(outDir.getAbsolutePath()+".csv"));
						}
						else
							System.out.println("Open command cancelled by user." );
						
						PrintWriter pw = null;
						try
						{
							pw = new PrintWriter(outDir);
						}
						catch (FileNotFoundException e) {System.out.println("error creating pw");}
						
						if (pw!=null)
						{
					pw.println("Well, Field, # Cells Bound by ROIs");
							//Getting all fields and printing number of cells bound by ROI
							int len = TheFieldViewers.length;
							for (int i = 0; i < len; i++)
							{
								Model_Field field = TheFieldViewers[i].getTheField();
								int numROIs = field.getROIs().size();
								pw.print(field.getParentWell().name+","+field.getIndexInWell()+",");
								for (int j = 0; j < numROIs; j++)
								{
									int num = field.getCellsBoundByROI(j);
									pw.print(num+",");
								}
								pw.println();
							}
							pw.flush();
							pw.close();
							pw = null;
						}
						
					}
				});
		PrintRIOMenu.add(item);

	}

	/** 
	 * Sets the FieldViewers for this frame to be displayed
	 * */
	public void setImageViewers(FieldViewer[] viewers_horiz)
	{
		//setting this as the parent container for all the sub-image containers
		for (int i = 0; i < viewers_horiz.length; i++)
			viewers_horiz[i].setParentContainer(this);
		TheFieldViewers = viewers_horiz;
		setPreferredSize(new Dimension(viewers_horiz[0].getTheCurrentImage().getWidth()+1000, viewers_horiz[0].getTheCurrentImage().getHeight()+170));
		setSize(viewers_horiz[0].getTheCurrentImage().getWidth()+1000, viewers_horiz[0].getTheCurrentImage().getHeight()+170);
		
		//adding the slider
		MovieFrameSlider = new MovieFrameSlider(JSlider.HORIZONTAL, 0, TheFieldViewers.length-1, 0);
		MovieFrameSlider.setToolTipText("Change Model_Well or Model_Field");
		add(MovieFrameSlider, BorderLayout.SOUTH);
		
		TheCurrentViewer = TheFieldViewers[0];
		setDisplay(TheFieldViewers[0]);
		
		setVisible(true);
	}

	public void close()
	{
		setVisible(false);
		
		Model_PlateRepository p = models.Model_Main.getModel()
				.getPlateRepository();

		if (p.getGUI().isBlocked())
			p.getGUI().unblock();
		
		System.gc();
	}
	
	private void setDisplay(FieldViewer v)
	{
		TheCurrentViewer.copySettings(TheCurrentViewer, v);
		TheCurrentViewer = v;
		TheCurrentViewer.updateAllImages();
		
		getContentPane().removeAll();
		getContentPane().add(v,BorderLayout.CENTER);
		getContentPane().add(MovieFrameSlider,BorderLayout.SOUTH);
		getContentPane().validate();
		getContentPane().repaint();
	}
	
	public void setCurrentWell(Model_Well well)
	{
		int len = TheFieldViewers.length;
		for (int i = 0; i < len; i++)
		{
			//Making sure it came from the same plate and then if the well Name is the same
			if (TheFieldViewers[i].getTheWell().getPlate().getID()==well.getPlate().getID() && TheFieldViewers[i].getTheWell().name.equalsIgnoreCase(well.name))
			{
				setDisplay(TheFieldViewers[i]);
				MovieFrameSlider.setValue(i);
				break;
			}
		}
	}
	
	/** Bottom slider that changes Image Model_Field*/
	private class MovieFrameSlider extends JSlider implements ChangeListener
	{
		public MovieFrameSlider(int orientation, int min,int max, int initVal)
		{
			setOrientation(orientation);
			setMinimum( min);
			setMaximum(max);
			setValue(initVal);
			setSnapToTicks(true);
			setMajorTickSpacing(1);
			setPaintTicks(true);
			addChangeListener(this);
		}
		
		public void stateChanged(ChangeEvent e)
		{
			MovieFrameSlider source = (MovieFrameSlider)e.getSource();
			if (source.getValue() < TheFieldViewers.length)
				setDisplay(TheFieldViewers[source.getValue()]);
			repaint();
			validate();
		}
	}
	
	/** Returns the type of shape that is currently going to be drawn upon dragging
	 * @author BLM*/
	public int getShapeType()
	{
		return Type_Shape;
	}
	/** Sets the type of shape that is currently going to be drawn upon dragging
	 * @author BLM*/
	public void setShapeType(int type)
	{
		Type_Shape = type;
	}
	
	static public float getScaling()
	{
		for (int i = 0; i < models.Model_Main.getModel().getGUI()
				.getTheImageScalings().length; i++)
		{
			if (models.Model_Main.getModel().getGUI().getTheImageScalings()[i]
					.isSelected())
				return models.Model_Main.getModel().getScalingRatios()[i];
		}
		return 1f;
	}
	
	public void windowOpened(WindowEvent p1){}
	public void windowClosing(WindowEvent p1)
	{
		close();
		System.gc();
	}
	public void windowClosed(WindowEvent p1){}
	{
		close();
		System.gc();
	}
	public void windowIconified(WindowEvent p1){}
	public void windowDeiconified(WindowEvent p1){}
	public void windowActivated(WindowEvent p1){}
	public void windowDeactivated(WindowEvent p1){}
	
	public void keyTyped(KeyEvent e)
	{
	}
	public void keyReleased(KeyEvent e)
	{
	}
	
	
	public void keyPressed(KeyEvent e)
	{
		int keyVal = e.getKeyCode ();
		if (keyVal == KeyEvent.VK_DELETE||keyVal==KeyEvent.VK_BACK_SPACE)
		{
			if (TheCurrentViewer.getTheField().getNumberOfROIs()>0)
			{
				int len = TheCurrentViewer.getTheField().getNumberOfROIs();
				for (int i = 0; i < len; i++)
				{
					if (TheCurrentViewer.getTheField().isROIselected(i))
					{
						TheCurrentViewer.getTheField().deleteROI(i);
					}
				}
			}
			repaint();
		}
		
		if (keyVal == KeyEvent.VK_SPACE)
		{
			TheCurrentViewer.getTheField().addROI(
					TheCurrentViewer.getCopyOfROI(),
					TheCurrentViewer.getFieldDimensions());
			repaint();
		}
		
		
	}
	
	/**
	 * Deletes all cells that are currently highlighted in the image and saves
	 * these changes to the HDF5 file
	 * 
	 * @author Bjorn Millard
	 * @param Model_Well
	 *            well
	 */
	private void deleteSelectedCells(Model_Well well) {
		well.setCellsModified(true);
		well.purgeSelectedCellsAndRecomputeWellMeans();
		models.Model_Main.getModel().getPlateRepository()
				.updateMinMaxValues();
	}

	/** Free up RAM
	 * @author BLM*/
	public void kill()
	{
		MovieFrameSlider = null;
		for (int i = 0; i < TheFieldViewers.length; i++)
			TheFieldViewers[i].kill();
		TheCurrentViewer = null;
		System.gc();
	}
}
