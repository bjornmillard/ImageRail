/**
 * FieldViewer_Frame.java
 *
 * @author Bjorn L Millard
 */

package imageViewers;

import java.awt.event.*;
import javax.swing.*;

import imageViewers.FieldViewer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import main.Field;
import main.MainGUI;
import main.PlateHoldingPanel;
import main.Well;

public class FieldViewer_Frame extends JFrame implements WindowListener, KeyListener
{
	private JSlider MovieFrameSlider;
	private FieldViewer[] TheFieldViewers;
	private FieldViewer TheCurrentViewer;
	private int Type_Shape;
	//CONSTANTS
	static public int RECTANGLE = 0;
	static public int OVAL = 1;
	
	
	
	
	public FieldViewer_Frame()
	{
		super("Image Viewer");
		setResizable(true);
		Type_Shape = RECTANGLE;
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
		for (int i = 0; i < MainGUI.getGUI().getTheImageScalings().length; i++)
		{
			MainGUI.getGUI().getTheImageScalings()[i].addActionListener(new ActionListener()
																		{
						public void actionPerformed(ActionEvent ae)
						{
							
							TheCurrentViewer.updateAllImages();
							MovieFrameSlider.repaint();
						}
					});
			scalingMenu.add(MainGUI.getGUI().getTheImageScalings()[i]);
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
		
		
		
//		item = new JMenuItem("Delete Selected Cells");
//		item.addActionListener(new ActionListener()
//							   {
//					public void actionPerformed(ActionEvent ae)
//					{
//						deleteSelectedCells(TheCurrentViewer.getTheWell());
//					}
//				});
//		OptionsMenu.add(item);
		
		item = new JMenuItem("Save Image Selection");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						if (Type_Shape==RECTANGLE)
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
								if (MainGUI.getGUI().getTheDirectory()!=null)
									fc = new JFileChooser(MainGUI.getGUI().getTheDirectory());
								else
									fc = new JFileChooser();
								
								File outDir = null;
								
								fc.setDialogTitle("Save as...");
								int returnVal = fc.showSaveDialog(MainGUI.getGUI());
								if (returnVal == JFileChooser.APPROVE_OPTION)
								{
									outDir = fc.getSelectedFile();
								}
								else
									System.out.println("Open command cancelled by user." );
								
								if (outDir!=null)
								{
									MainGUI.getGUI().setTheDirectory(new File(outDir.getParent()));
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
		
		JMenu SelectionShapeMenu = new JMenu("Selection Shape...");
		OptionsMenu.add(SelectionShapeMenu);
		JRadioButtonMenuItem square = new JRadioButtonMenuItem("Rectangle");
		square.setSelected(true);
		square.addActionListener(new ActionListener()
								 {
					public void actionPerformed(ActionEvent ae)
					{
						Type_Shape = RECTANGLE;
					}
				});
		SelectionShapeMenu.add(square);
		JRadioButtonMenuItem oval = new JRadioButtonMenuItem("Oval");
		oval.setSelected(false);
		oval.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						Type_Shape = OVAL;
					}
				});
		SelectionShapeMenu.add(oval);
		ButtonGroup bg = new ButtonGroup();
		bg.add(oval);
		bg.add(square);
		
		
		JMenu SelectionMenu = new JMenu("Selection Area...");
		OptionsMenu.add(SelectionMenu);
		
		
		
		
		item = new JMenuItem("Set Selection to all Images");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						if (Type_Shape==RECTANGLE)
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
									TheFieldViewers[i].setShape(new Rectangle(rect.x, rect.y, rect.width, rect.height),RECTANGLE);
									
								}
							}
							
							
						}
						else if (Type_Shape==OVAL)
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
									Type_Shape = OVAL;
									TheFieldViewers[i].setCreateNewBox(false);
								}
							}
						}
					}
				});
		SelectionMenu.add(item);
		
		item = new JMenuItem("Optimize XY to fit hole in each Field");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						if (Type_Shape==RECTANGLE)
						{
							float scale = getScaling();
							Rectangle bounds = (Rectangle)TheCurrentViewer.getScaledSelectionBounds(getScaling());
							if (bounds==null || bounds.width<=2 ||bounds.height<=2) //nothing selected
							{
								JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
															  "",JOptionPane.ERROR_MESSAGE);
								return;
							}
							
							int len = TheFieldViewers.length;
							for (int i = 0; i < len; i++)
							{
								TheFieldViewers[i].setSelectedROI(new Rectangle((int)(bounds.x/scale), (int)(bounds.y/scale), (int)(bounds.width/scale), (int)(bounds.height/scale)));
								Type_Shape = RECTANGLE;
								TheFieldViewers[i].setCreateNewBox(false);
								int channelIndex = 0;
								TheFieldViewers[i].optimizeXYshapeCoordinatesToHole(channelIndex);
							}
							
						}
						else if (Type_Shape==OVAL)
						{
							Rectangle bounds = (Rectangle)TheCurrentViewer.getScaledSelectionBounds(getScaling());
							if (bounds==null || bounds.width<=2 ||bounds.height<=2) //nothing selected
							{
								JOptionPane.showMessageDialog(null,"No selection made. \nPlease highlight a region and try again.",
															  "",JOptionPane.ERROR_MESSAGE);
								return;
							}
							float scale = getScaling();
							int len = TheFieldViewers.length;
							for (int i = 0; i < len; i++)
							{
								TheFieldViewers[i].setSelectedROI(new Ellipse2D.Float((int)(bounds.x/scale), (int)(bounds.y/scale), (int)(bounds.width/scale), (int)(bounds.height/scale)));
								Type_Shape = OVAL;
								TheFieldViewers[i].setCreateNewBox(false);
								int channelIndex = 0;
								TheFieldViewers[i].optimizeXYshapeCoordinatesToHole(channelIndex);
							}
							
						}
					}
				});
		SelectionMenu.add(item);
		
		
		
		OptionsMenu.addSeparator();
		JMenu FieldMenu = new JMenu("Field Options...");
		OptionsMenu.add(FieldMenu);
		
		JMenu SaveROIMenu = new JMenu("Save...");
		JMenu DeleteROIMenu = new JMenu("Delete...");
		JMenu PrintRIOMenu = new JMenu("Print...");
		FieldMenu.add(SaveROIMenu);
		FieldMenu.add(DeleteROIMenu);
		FieldMenu.add(PrintRIOMenu);
		
		item = new JMenuItem("This Field");
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						TheCurrentViewer.getTheField().addROI(TheCurrentViewer.getCopyOfROI());
						TheCurrentViewer.repaint();
					}
				});
		SaveROIMenu.add(item);
		
		item = new JMenuItem("All Fields");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
												   ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener()
							   {
					public void actionPerformed(ActionEvent ae)
					{
						int len = TheFieldViewers.length;
						for (int i = 0; i < len; i++)
							TheFieldViewers[i].getTheField().addROI(TheFieldViewers[i].getCopyOfROI());
						
						TheCurrentViewer.repaint();
					}
				});
		SaveROIMenu.add(item);
		
		
		item = new JMenuItem("This Field");
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
						if (MainGUI.getGUI().getTheDirectory()!=null)
							fc = new JFileChooser(MainGUI.getGUI().getTheDirectory());
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
								Field field = TheFieldViewers[i].getTheField();
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
		MovieFrameSlider.setToolTipText("Change Well or Field");
		add(MovieFrameSlider, BorderLayout.SOUTH);
		
		TheCurrentViewer = TheFieldViewers[0];
		setDisplay(TheFieldViewers[0]);
		
		setVisible(true);
	}

	public void close()
	{
		setVisible(false);
		
		PlateHoldingPanel p = MainGUI.getGUI().getPlateHoldingPanel();
		if (p.isBlocked())
			p.unblock();
		
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
	
	public void setCurrentWell(Well well)
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
	
	/** Bottom slider that changes Image Field*/
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
		for (int i = 0; i < MainGUI.getGUI().getTheImageScalings().length; i++)
		{
			if(MainGUI.getGUI().getTheImageScalings()[i].isSelected())
				return MainGUI.getGUI().getScalingRatios()[i];
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
			TheCurrentViewer.getTheField().addROI(TheCurrentViewer.getCopyOfROI());
			repaint();
		}
		
		
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
