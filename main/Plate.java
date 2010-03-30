/**
 * Plate.java
 *
 * @author Bjorn L Millard
 */

package main;

import imPanels.ImageCapturePanel;
import imPanels.JPanel_highlightBox;
import imageViewers.FieldViewer_Frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import plots.Legend;
import plots.LinePlot;
import tools.PanelDropTargetListener;
import tools.SVG_writer;
import us.hms.systemsbiology.data.HDFConnectorException;
import us.hms.systemsbiology.data.SegmentationHDFConnector;
import us.hms.systemsbiology.metadata.Description;
import us.hms.systemsbiology.metadata.MetaDataConnector;
import dialogs.CaptureImage_Dialog;
import features.Feature;


/**
 * The plate object that holds all the images and metadata. The wells it
 * contains also act as a heatmap for well mean data and can contain
 * mini-histograms of distributions of the single-cell features
 *
 * @author BLM
 */
public class Plate extends JPanel_highlightBox implements ImageCapturePanel
{
	/** Plate Identifier*/
	private int ID;
	/** Handle on itself*/
	private Plate ThePlate;
	/** Matrix of this plate's Wells*/
	private Well[][] TheWells;
	/** Number of Rows of Wells*/
	private int NumRows;
	/** Number of Columns of Wells */
	private int NumCols;
	/** The X-coordinate where plate is started to be drawn*/
	private int Xstart;
	/** The Y-coordinate of the upper left start point of the plate*/
	private int Ystart;
	/** Array of colors for the border of the plate rendering*/
	private Color[] BorderColors;
	/** Generic color array holder*/
	private float[] color = new float[4];
	/** Boolean to say whether to display Row Color legends*/
	private boolean DisplayRowLegend;
	/** The Toolbar on top*/
	private JToolBar TheToolBar;
	/** String Title for the plate that will be displayed above if desired*/
	private String Title;
	/** Depricated - Used to allow modified plate heat maps such as linear slope displays*/
	private int DisplayType;
	private static int NORMAL = 0;
	private static int DISPLAYSLOPES = 1;
	/** If true, this will place a blocking rectangle on top of the plate*/
	private boolean Block;
	private Rectangle TheBlockingRectangle;
	/** Stores the min/max Feature values for all wells - TODO move this to each well*/
	private float[][] MinMaxFeatureValues;
	/** Stores the log min/max Feature values for all wells - TODO move htis to each well*/
	private float[][] MinMaxFeatureValues_log;
	/** Buttons for the toolbar*/
	private JButton CaptureImageButton;
	/** TODO - Depricated - Printwriter used to write bimodal fit data to file  */
	private PrintWriter pw;
	/** Number of Gaussians to fit to each mini-histogram*/
	private int FitGaussian;
	/** List of all MIDAS treatments*/
	private ArrayList AllTreatments;
	/** List of all MIDAS measurements*/
	private ArrayList AllMeasurements;
	/** A table containing the current features data*/
	private JTable TheDataTable;
	/*** The Prefereed toolbar position*/
	private String ToolBarPosition;
	/** */
	private Legend TheLegend;
	/** */
	private Point Legend_xy;
	/** */
	private int DisplayMetaData;
	/** */
	private Hashtable TheMetaDataHashtable;
	/** */
	private MetaDataConnector TheMetaDataWriter;
	/** */
	private  final JButton MetaButton  = new JButton(new ImageIcon("icons/meta.png"));
	/** */
	private boolean Dragging;
	
	/** Main Plate Constructor
	 * @author BLM*/
	public Plate(int numRows_, int numCols_, int ID_)
	{
		ID = ID_;
		TheToolBar = new JToolBar();
		ThePlate = this;
		DisplayType = NORMAL;
		setLayout(new BorderLayout());
		DisplayRowLegend = false;
		continuallyDisplayHighlightBox(true);
		setSize(445, 520);
		setPreferredSize(new Dimension(445, 520));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setVisible(true);
		AllTreatments = new ArrayList();
		AllMeasurements = new ArrayList();
		NumRows = numRows_;
		NumCols = numCols_;
		initWells();
		Xstart = 10;
		Ystart = 5;
		Block = false;
		TheBlockingRectangle = null;
		FitGaussian = 0;
		ToolBarPosition = BorderLayout.NORTH;
		Legend_xy = new Point(100, getHeight()-100);
		DisplayMetaData = -1;
		updatePanel();
		Dragging = false;
		initMetaDataConnector();
		
		//Adding the Drag and Drop feature for file loading
		PanelDropTargetListener drop = new PanelDropTargetListener();
		drop.ThePlate = this;
		new DropTarget(this, drop);
		
		BorderColors = new Color[5];
		BorderColors[0] = new Color(200, 200, 200);
		BorderColors[1] = new Color(150, 150, 150);
		BorderColors[2] = new Color(100, 100, 100);
		BorderColors[3] = new Color(80, 80, 80);
		BorderColors[4] = new Color(50, 50, 50);
		
		TheToolBar.setVisible(true);
		ThePanel.setLayout(new BorderLayout());
		ThePanel.add(TheToolBar, BorderLayout.NORTH);
		
		CaptureImageButton = new JButton(new ImageIcon("icons/camera.png"));
		TheToolBar.add(CaptureImageButton);
		CaptureImageButton.setToolTipText("Capture Image of Plot");
		CaptureImageButton.addActionListener(new ActionListener()
											 {
					public void actionPerformed(ActionEvent ae)
					{
						CaptureImage_Dialog s = new CaptureImage_Dialog(ThePlate);
					}
				});
		
		
		if (Title==null)
			Title = "Plate #"+ID;
		
		final JButton titleLabel = new JButton(Title);
		titleLabel.addMouseListener(new MouseAdapter()
									{
					public void mouseClicked(MouseEvent e)
					{
						
						MainGUI.getGUI().getPlateHoldingPanel().setTab(ID);
						if(e.getClickCount()>1)
						{
							String response = JOptionPane.showInputDialog(null,
																		  "Enter New Name of Plate: ",
																		  "Enter New Name of Plate: ",
																		  JOptionPane.QUESTION_MESSAGE);
							if (response==null)
								return;
							
							Title = response;
							titleLabel.setText(Title);
							MainGUI.getGUI().getPlateHoldingPanel().getTheMainPanel().setTitleAt(ID, Title);
						}
						
						
						
					}
				});
		TheToolBar.add(titleLabel);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Helvetca", Font.BOLD, 16));
		titleLabel.setEnabled(true);
		
		TheToolBar.add(MetaButton);
		MetaButton.setToolTipText("Display Experimental Metadata Plate Map");
		MetaButton.addActionListener(new ActionListener()
									 {
					public void actionPerformed(ActionEvent ae)
					{
						DisplayMetaData ++;
						if(DisplayMetaData>3)
							DisplayMetaData = -1;
						updateMetaDataLegend();
						

				// new Plate_MetaDataDisplay(ThePlate, "MetaData Viewer", 600,
				// 600, TheMetaDataWriter);
					}
				});
		
	}
	
	public void updateMetaDataLegend()
	{
		if(Dragging)
			return;
		//Trying to create a metaDataconnector to pass metadata to the linegraph
		// so we can have axis labeling and the such
		
		if(TheMetaDataWriter==null)
		{
			String projPath = main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
			try
			{
				TheMetaDataWriter = new MetaDataConnector(projPath, getPlateIndex());
			}
			catch(Exception e)
			{
				System.out.println("------* Error creating MetaData XML writer *------");
			}
		}
		
		if(DisplayMetaData>-1)
		{
			MetaButton.setIcon(new ImageIcon("icons/meta_selected.png"));
			
			//Getting all unique meta data treatment combinations
			TheMetaDataHashtable = initMetaDataHashtable(ThePlate, TheMetaDataWriter);
			String[] names = new String[TheMetaDataHashtable.size()];
			Color[] colors = new Color[TheMetaDataHashtable.size()];
			int counter = 0;
			//First Legend entry will be the Title:
			String title = "";
			if(DisplayMetaData==0)
				title = "Treatments:";
			else if(DisplayMetaData==1)
				title = "Measurements:";
			else if(DisplayMetaData==2)
				title = "Descriptions:";
			else if(DisplayMetaData==3)
				title = "Time Points:";
			
			for (Enumeration e = TheMetaDataHashtable.keys() ; e.hasMoreElements() ;)
			{
				names[counter] = (String)(e.nextElement());
				colors[counter] = (Color)TheMetaDataHashtable.get(names[counter]);
				counter++;
			}
			
			TheLegend = new Legend(title, names, colors, Legend_xy.x, Legend_xy.y);
		}
		else
		{
			TheMetaDataHashtable = null;
			MetaButton.setIcon(new ImageIcon("icons/meta.png"));
			TheLegend = null;
		}
		
		updatePanel();
		repaint();
	}
	
	/** Looks for all the unique treatment combinations across all the wells and adds them to a hashtable linked with a color for that treatment combo.
	 * Note it doesnt compare quantitative amounts of treatment, just if that treatment exists in the well. For example, if a well is treated with
	 * EGF and Iressa, the key to the hashtable would be "EGF+Iressa" and the table would get a colore for that combination
	 * @author BLM*/
	private Hashtable initMetaDataHashtable(Plate plate, MetaDataConnector meta)
	{
		TheMetaDataHashtable = null;
		int colorCounter = 0;
		Hashtable hash = new Hashtable();
		ArrayList<String> uniques = new ArrayList<String>();
		//Getting treatments
		for (int r = 0; r < plate.NumRows; r++)
			for (int c = 0; c < plate.NumCols; c++)
			{
				int Type = shouldDisplayMetaData();
				ArrayList<String> arr = new ArrayList<String>();
				if(Type == 0)
					arr = meta.getAllTreatmentNames(plate.getPlateIndex(), plate.TheWells[r][c].getWellIndex());
				else if(Type == 1)
					arr = meta.getAllMeasurementNames(plate.getPlateIndex(), plate.TheWells[r][c].getWellIndex());
				else if(Type == 2)
				{
					Description des = ((Description)meta.readDescription( plate.TheWells[r][c].getWellIndex()));
					if (des == null || des.getValue()== null)
						arr= null;
					else
						arr.add(des.getValue());
				}
				else if(Type == 3)
				{
					Description des = ((Description)meta.readTimePoint( plate.TheWells[r][c].getWellIndex()));
					if (des == null || des.getValue()== null)
						arr= null;
					else
						arr.add(des.getValue());
				}
				
				
				if (arr!=null && arr.size()>0)
				{
					//Now adding it to the hastable
					String stringCat = "";
					for (int i = 0; i < arr.size()-1; i++)
						stringCat+=arr.get(i)+" + ";
					stringCat+=arr.get(arr.size()-1);
					
					if(hash.get(stringCat)==null)
					{
						hash.put(stringCat, tools.ColorRama.getColor(colorCounter));
						colorCounter++;
					}
				}
			}
		
		
		return hash;
	}
	
	/** If an HDF file of cell data exists for this plate and each well within, this method trys to load it into the RAM
	 * @author BLM*/
	static public void loadCellData(Plate[] allPlates, boolean loadCoords,
			boolean loadDataVals)
	{
		ArrayList<Well> selectedWells = new ArrayList<Well>();
		for (int p = 0; p < allPlates.length; p++)
			for (int i = 0; i < allPlates[p].NumRows; i++)
				for (int j = 0; j < allPlates[p].NumCols; j++)
					if(allPlates[p].TheWells[i][j].isSelected())
						selectedWells.add(allPlates[p].TheWells[i][j]);
		
		CellLoader loader = new CellLoader(selectedWells, loadCoords,
				loadDataVals);
		loader.start();
	}
	
	/** Each plate will have a single metadata connector to represent its meta data.  This method creates it
	 * @author BLM*/
	private void initMetaDataConnector()
	{
		if(main.MainGUI.getGUI().getProjectDirectory()!=null)
		{
			String projPath = main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
			TheMetaDataWriter = null;
			try
			{
				TheMetaDataWriter = new MetaDataConnector(projPath, getPlateIndex());
			}
			catch(Exception e)
			{
				System.out.println("------* Error creating MetaData XML writer *------");
			}
		}
	}
	
	/** Clears the RAM of all cell data across all plates in the program
	 * @author BLM*/
	public void clearCellData()
	{
		for (int i = 0; i < NumRows; i++)
			for (int j = 0; j < NumCols; j++)
			{
				Field[] fields = TheWells[i][j].getFields();
				if(fields!=null)
				{
					int numF = fields.length;
					for (int z = 0; z < numF; z++)
						fields[z].killCells();
				}
			}
	}
	
	
	
	
	/** Initialize the wells
	 * @author BLM*/
	public void initWells()
	{
		TheWells = new Well[NumRows][NumCols];
		int counter = 0;
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
			{
				Well well = new Well(this, r, c);
				well.ID = counter;
				
				TheWells[r][c] = well;
				counter++;
			}
	}
	
	/** Returns an ArrayList of selected Wells ordered from left to right, top to bottom
	 * @author BLM*/
	public ArrayList getSelectedWells_horizOrder()
	{
		ArrayList arr = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].isSelected())
					arr.add(TheWells[r][c]);
		return arr;
	}
	
	
	/** Returns an ArrayList of selected Wells ordered from top to bottom, left to right
	 * @author BLM*/
	public ArrayList getSelectedWells_verticalOrder()
	{
		ArrayList arr = new ArrayList();
		for (int c = 0; c < NumCols; c++)
			for (int r = 0; r < NumRows; r++)
				if (TheWells[r][c].isSelected())
					arr.add(TheWells[r][c]);
		return arr;
	}
	
	/** Returns the selected Wells ordered from left to right, top to bottom in a Well[] instead of ArrayList*/
	public Well[] getAllSelectedWells()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].isSelected())
					temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Well[] wells = new Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Well) temp.get(i);
		return wells;
	}
	
	/** Returns the Wells ordered from left to right, top to bottom in a Well[] instead of ArrayList*/
	public Well[] getAllWells()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Well[] wells = new Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Well) temp.get(i);
		return wells;
	}
	
	/** Returns the Wells that contain images ordered from left to right, top to bottom in a Well[] instead of ArrayList*/
	public Well[] getAllWells_wImages()
	{
		ArrayList temp = new ArrayList();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].containsImages())
					temp.add(TheWells[r][c]);
		
		int len = temp.size();
		Well[] wells = new Well[len];
		for (int i = 0; i < len; i++)
			wells[i] = (Well) temp.get(i);
		return wells;
	}
	
	/** Updates the Plate Panel by first re-initializing the height and width
	 * @author BLM*/
	public void updatePanel()
	{
		removeAll();
		add(TheToolBar, ToolBarPosition);
		if (MainGUI.getGUI().getPlateHoldingPanel()!=null && MainGUI.getGUI().getPlateHoldingPanel().showData())
		{
			add(getDataTable(), BorderLayout.CENTER);
		}
		
		getPlateHeight();
		getPlateWidth();
		validate();
		repaint();
	}
	
	/** Returns a scrollpane containging the data table for the currently selected Feature
	 * @author BLM*/
	private JScrollPane getDataTable()
	{
		//Constructing the data table at bottom
		String[] headers = new String[NumCols+1];
		headers[0]="";
		for (int i = 1; i < headers.length; i++)
			headers[i]="";
		int rowCounter = 0;
		int rowBuffer = 4;
		//Setting up the table
		Object[][] dat = new Object[NumRows*2+rowBuffer+3][headers.length];
		//Column names
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i]=""+getColumnName(i-1);
		rowCounter++;
		
		dat[rowCounter][0]="MEANS:";
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i]="";
		rowCounter++;
		
		//Data values
		for (int r = 0; r <NumRows; r++)
		{
			dat[rowCounter][0] = getRowName(r);
			for (int c = 1; c < dat[r].length; c++)
			{
				float val = TheWells[r][c-1].getCurrentValue_Mean();
				dat[rowCounter][c] = new Float(val);
			}
			rowCounter++;
		}
		//Buffer
		for (int r = 0; r <rowBuffer; r++)
		{
			for (int c = 0; c < dat[r].length; c++)
				dat[rowCounter][c] = "";
			rowCounter++;
		}
		dat[rowCounter][0]="STDEVs:";
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i]="";
		rowCounter++;
		//Stdevs
		for (int r = 0; r <NumRows; r++)
		{
			dat[rowCounter][0] = getRowName(r);
			for (int c = 1; c < dat[r].length; c++)
			{
				float val = TheWells[r][c-1].getCurrentValue_Stdev();
				dat[rowCounter][c] = new Float(val);
			}
			rowCounter++;
		}
		//Setting colors of certain cells
		TheDataTable = new JTable(dat, headers)
		{
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
			{
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if ((vColIndex == 0 || rowIndex==0) && !isCellSelected(rowIndex, vColIndex))
					c.setBackground(tools.ColorRama.Gray_veryLight1);
				else if (isCellSelected(rowIndex, vColIndex))
					c.setBackground(tools.ColorRama.Yellow_Light);
				else
					c.setBackground(getBackground());
				
				return c;
			}
		};
		
		//Setting column widths
		TheDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int c=0; c<TheDataTable.getColumnCount(); c++)
			packColumn(TheDataTable, c, 2);
		
		//Setting user selection preferences
		TheDataTable.setCellSelectionEnabled(true);
		TheDataTable.setColumnSelectionAllowed(true);
		TheDataTable.setRowSelectionAllowed(true);
		
		TheDataTable.setPreferredSize(new Dimension(getWidth(), getHeight()));
		return JTable.createScrollPaneForTable(TheDataTable);
	}
	
	/** Packs and formats the data table columns
	 * @author BLM*/
	private void packColumn(JTable table, int vColIndex, int margin)
	{
		TableModel model = table.getModel();
		DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);
		int width = 0;
		
		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null)
		{
			renderer = table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(
			table, col.getHeaderValue(), false, false, 0, 0);
		width = comp.getPreferredSize().width;
		
		// Get maximum width of column data
		for (int r=0; r<table.getRowCount(); r++)
		{
			renderer = table.getCellRenderer(r, vColIndex);
			comp = renderer.getTableCellRendererComponent(
				table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}
		
		// Add margin
		width += 2*margin;
		
		// Set the width
		col.setPreferredWidth(width);
	}
	
	
	/**Draws the Plate
	 * @author BLM */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		
		draw(g2, false);
		
		super.paintHighlighting(g2);
	}
	
	/** Blocks the plate with a blocking panel
	 * @author BLM*/
	public void block(FieldViewer_Frame im)
	{
		Block = true;
	}
	
	/** Unblocks the plate from the blocking panel
	 * @author BLM*/
	public void unblock()
	{
		Block = false;
		TheBlockingRectangle = null;
		updatePanel();
	}
	
	/** Checks if the plate is currently getting blocked
	 * @author BLM*/
	public boolean isBlocked()
	{
		return Block;
	}
	
	/** Finds the  min/max value across either allPlates or just this plate depending on the argument
	 * @author BLM*/
	private float[] getMinMaxAcrossPlates(boolean normalizeAcrossAllPlates)
	{
		float[] arr = new float[2];
		arr[0] = Float.POSITIVE_INFINITY;
		arr[1] = Float.NEGATIVE_INFINITY;
		
		if (normalizeAcrossAllPlates)
		{
			Plate[] thePlates = MainGUI.getGUI().getPlateHoldingPanel().getThePlates();
			int numPlates = thePlates.length;
			for (int p = 0; p < numPlates; p++)
			{
				Plate plate = thePlates[p];
				for (int r = 0; r < plate.NumRows; r++)
					for (int c = 0; c < plate.NumCols; c++)
						if (plate.TheWells[r][c].Feature_Means != null && tools.MathOps.sum(plate.TheWells[r][c].Feature_Means)!=0 && plate.TheWells[r][c].Feature_Means.length > MainGUI.getGUI().getTheSelectedFeature_Index())
						{
							float val = 0;
							val = plate.TheWells[r][c].Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()];
							
							if (val < arr[0])
								arr[0] = val;
							if (val > arr[1])
								arr[1] = val;
						}
			}
		}
		else //Just this plate
		{
			Plate plate = this;
			for (int r = 0; r < plate.NumRows; r++)
				for (int c = 0; c < plate.NumCols; c++)
					if (plate.TheWells[r][c].Feature_Means != null && tools.MathOps.sum(plate.TheWells[r][c].Feature_Means)!=0 && plate.TheWells[r][c].Feature_Means.length > MainGUI.getGUI().getTheSelectedFeature_Index())
					{
						float val = 0;
						val = plate.TheWells[r][c].Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()];
						
						if (val < arr[0])
							arr[0] = val;
						if (val > arr[1])
							arr[1] = val;
					}
		}
		
		return arr;
	}
	
	public void updateDimensions()
	{
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].updateDimensions();
	}
	
	/** Returns the index in the plate holding panel of this plate, where the first plate index == 0
	 * @author BLM*/
	public int getPlateIndex()
	{
		return (getID()-1);
	}
	
	/** Re-initializes the heat mapping colors of the wells
	 * @author BLM*/
	public void updateWellDisplayColors()
	{
		if (DisplayType == NORMAL)
		{
			int channel = 0;
			
			Feature f = MainGUI.getGUI().getTheSelectedFeature();
			if (f != null)
				channel = f.ChannelIndex;
			
			float[] minMax = getMinMaxAcrossPlates(MainGUI.getGUI().getPlateHoldingPanel().shouldNormalizeAcrossAllPlates());
			
			
			for (int r = 0; r < NumRows; r++)
				for (int c = 0; c < NumCols; c++)
					if (TheWells[r][c].Feature_Means != null && tools.MathOps.sum(TheWells[r][c].Feature_Means)!=0  && TheWells[r][c].Feature_Means.length > MainGUI.getGUI().getTheSelectedFeature_Index())
					{
						float val = 0;
						val = TheWells[r][c].Feature_Means[MainGUI.getGUI().getTheSelectedFeature_Index()];
						if (channel == 0)
							tools.ColorMaps.getColorValue(val, minMax[0],
														  minMax[1], color, tools.ColorMaps.BLUE);
						else if (channel == 1)
							tools.ColorMaps.getColorValue(val, minMax[0],
														  minMax[1], color, tools.ColorMaps.GREEN);
						else
							tools.ColorMaps.getColorValue(val, minMax[0],minMax[1], color, tools.ColorMaps.RED);
						
						TheWells[r][c].color = new Color(color[0], color[1],color[2]);
					}
		}
		
	}
	
	/** Returns all the measurements taken on this plate
	 * @author BLM*/
	public ArrayList getAllMeasurements()
	{
		return AllMeasurements;
	}
	/** Sets all the measurements taken on this plate */
	public void setAllMeasurements(ArrayList measurements)
	{
		AllMeasurements = measurements;
	}
	/** Returns all the Treatments used on this plate
	 * @author BLM*/
	public ArrayList getAllTreatments()
	{
		return AllTreatments;
	}
	/** Sets all the treatments used on this plate */
	public void setAllTreatments(ArrayList treatments)
	{
		AllTreatments = treatments;
	}
	
	/** For each of N-features, returns a float matrix of 2xN where each element 0,1 is the min/max respectively and each element N
	 * is a different feature.  This array helps with the plate self normalization across features
	 * @author BLM*/
	public float[][] getMinMaxFeatureValues()
	{
		return MinMaxFeatureValues;
	}
	/** For each of N-features, returns a LOG(float matrix) of 2xN where each element 0,1 is the min/max respectively and each element N
	 * is a different feature.  This array helps with the plate self normalization across features
	 * @author BLM*/
	public float[][] getMinMaxFeatureValues_log()
	{
		return MinMaxFeatureValues_log;
	}
	/** Returns boolean whether to display the color boxes representing the row legend
	 * @author BLM*/
	public boolean shouldDisplayRowLegend()
	{
		return DisplayRowLegend;
	}
	/** Sets boolean whether to display the color boxes representing the row legend
	 * @author BLM*/
	public void setDisplayRowLegend(boolean boo)
	{
		DisplayRowLegend = boo;
	}
	
	/** Returns the PrintWriter used to write data to file
	 * @author BLM*/
	public PrintWriter getThePrintWriter()
	{
		return pw;
	}
	
	/** Returns the array of colors used to define the colors of the plate border
	 * @author BLM*/
	public Color[] getBorderColors()
	{
		return BorderColors;
	}
	/** Returns the wells that this plate is composed of
	 * @author BLM*/
	public Well[][] getTheWells()
	{
		return TheWells;
	}
	
	/** Sets the Title of the plate
	 * @author BLM */
	public void setTitle(String title)
	{
		Title = title;
	}
	/** Gets the Title of the plate
	 * @author BLM */
	public String getTitle()
	{
		return Title;
	}
	
	/** Sets the y-value in the panel where the plate will be begun to be drawn
	 * @author BLM */
	public void setYstart(int val)
	{
		Ystart = val;
	}
	/** sets the x-value in the panel where the plate will be begun to be drawn
	 * @author BLM */
	public void setXstart(int val)
	{
		Xstart = val;
	}
	/** Gets the y-value in the panel where the plate will be begun to be drawn
	 * @author BLM */
	public int getYstart()
	{
		return Ystart;
	}
	/** Gets the x-value in the panel where the plate will be begun to be drawn
	 * @author BLM */
	public int getXstart()
	{
		return Xstart;
	}
	
	/** Returns the toolbar associated with the plate
	 * @author BLM*/
	public JToolBar getTheToolBar()
	{
		return TheToolBar;
	}
	
	/** Sets the position of the Toolbar
	 * @author BLM */
	public void setToolBarPosition(String position)
	{
		ToolBarPosition = position;
	}
	
	/** Returns the plate ID
	 * @author BLM*/
	public int getID()
	{
		return ID;
	}
	
	/** Returns the number of columns in the plate
	 * 	 * @author BLM*/
	public int getNumColumns()
	{
		return TheWells[0].length;
	}
	
	/** Returns the number of rows in the plate
	 * @author BLM*/
	public int getNumRows()
	{
		return TheWells.length;
	}
	
	/** Returns the plate height
	 * @author BLM*/
	public int getPlateHeight()
	{
		return (TheWells[NumRows - 1][0].getYpos() + TheWells[NumRows - 1][0].getHeight()) - TheWells[0][0].getYpos();
	}
	
	/** Returns hte plate width
	 * @author BLM*/
	public int getPlateWidth()
	{
		return TheWells[0][NumCols - 1].getXpos() + TheWells[0][NumCols - 1].getWidth() + 5;
	}
	
	/** If there was a click event, this returns the index of the appropriate well that was clicked on
	 * @author BLM*/
	public int[] getWellIndex(MouseEvent p1)
	{
		Point p = p1.getPoint();
		
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].outline.contains(p))
				{
					int[] ind = new int[2];
					ind[0] = r;
					ind[1] = c;
					return ind;
				}
		return null;
	}
	
	public void mousePressed(MouseEvent p1)
	{
		
		if(TheLegend!=null && TheLegend.contains(p1.getPoint()))
		{
			TheLegend.setDragging(true);
		}
		else
		{
			
			if (highlightBox!=null && highlightBox.contains(p1.getPoint()))
			{
				dX = p1.getX()-highlightBox.x;
				dY = p1.getY()-highlightBox.y;
				CreateNewBox=false;
			}
			else
			{
				startBox_XY = p1.getPoint();
				startHighlightPoint = p1.getPoint();
				CreateNewBox=true;
			}
		}
		updatePanel();
		
	}
	
	/** Handles mouse clicks on the plate
	 * @author BLM*/
	public void mouseClicked(MouseEvent p1)
	{
		
		if (Block)
		{
			Point p = p1.getPoint();
			
			if (TheBlockingRectangle.contains(p))
				unblock();
			
			return;
		}
		
		if (p1.getClickCount() >= 2)
		{
			highlightBox = null;
			startHighlightPoint = null;
			unHighlightAllWells();
		}
		
		MainGUI.getGUI().updateMidasInputPanel();
		
		int[] inWell = getWellIndex(p1);
		
		if (inWell != null)
		{
			if (p1.getClickCount() != 1)
				return;
			Well well = TheWells[inWell[0]][inWell[1]];
			well.toggleHighlightState();
			
			MainGUI.getGUI().updateMidasInputPanel();
			inWell = null;
			
		}
		
		updatePanel();
		if (MainGUI.getGUI().getDotPlot() != null)
			if (MainGUI.getGUI().getLeftDisplayPanelType() == MainGUI.getGUI().DOTPLOT)
				MainGUI.getGUI().getDotPlot().UpdatePlotImage = true;
		
		MainGUI.getGUI().updateAllPlots();
	}
	
	/** Handles mouse release events
	 * @author BLM*/
	public void mouseReleased(MouseEvent p)
	{
		Dragging = false;
		if (MainGUI.getGUI().getDotPlot() != null)
			if (MainGUI.getGUI().getLeftDisplayPanelType() == MainGUI.getGUI().DOTPLOT)
				MainGUI.getGUI().getDotPlot().UpdatePlotImage = true;
		
		if(TheLegend!=null)
			TheLegend.setDragging(false);
		
		MainGUI.getGUI().getPlateHoldingPanel().setLastTouched_PlateID(ID);
		MainGUI.getGUI().updateAllPlots();
	}
	
	/** Handles mouse drag events
	 * @author BLM*/
	public void mouseDragged(MouseEvent p1)
	{
		Dragging = true;
		if (Block)
			return;
		
		if(TheLegend!=null&& TheLegend.isDragging())
		{
			TheLegend.setX((int)(p1.getPoint().x-TheLegend.getWidth()/2f));
			TheLegend.setY((int)(p1.getPoint().y-TheLegend.getHeight()/2f));
			Legend_xy.x = TheLegend.getX();
			Legend_xy.y = TheLegend.getY();
			updatePanel();
			return;
		}
		if (CreateNewBox)
		{
			int xval = p1.getPoint().x - startHighlightPoint.x;
			int yval = p1.getPoint().y - startHighlightPoint.y;
			
			highlightBox = new Rectangle();
			highlightBox.x = startHighlightPoint.x;
			highlightBox.y = startHighlightPoint.y;
			highlightBox.width = 0;
			highlightBox.height = getHeight();
			
			if (xval >= 0)
				highlightBox.width = xval;
			else
			{
				highlightBox.x = p1.getPoint().x;
				highlightBox.width = -xval;
			}
			if (yval >= 0)
				highlightBox.height = yval;
			else
			{
				highlightBox.y = p1.getPoint().y;
				highlightBox.height = -yval;
			}
		}
		else
		{
			if (p1 == null || highlightBox == null)
				return;
			highlightBox.x = p1.getX() - dX;
			highlightBox.y = p1.getY() - dY;
		}
		
		highlightWells(highlightBox, p1.isShiftDown());
		updatePanel();
		
		if (MainGUI.getGUI().getDotPlot() != null)
			if (MainGUI.getGUI().getLeftDisplayPanelType() == MainGUI.getGUI().DOTPLOT)
			{
				MainGUI.getGUI().getDotPlot().UpdatePlotImage = false;
				MainGUI.getGUI().getDotPlot().repaint();
			}
			else if (MainGUI.getGUI().getLeftDisplayPanelType() == MainGUI.getGUI().MIDASINPUT)
			{
				
			}
			else
				MainGUI.getGUI().updateAllPlots();
		
		
		
	}
	
	/** Unhighlights all the wells
	 * @author BLM*/
	public void unHighlightAllWells()
	{
		MainGUI.getGUI().getPlateHoldingPanel().unHighlightAllWells();
	}
	
	/** Highlights the wells enclosed by the given highlight box.  You must also tell it if you want to keep old highlighting
	 * @author BLM*/
	private void highlightWells(Rectangle highlightBox, boolean keepOldHighlights)
	{
		if (!keepOldHighlights)
			unHighlightAllWells();
		
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
			{
				int x = (int) TheWells[r][c].outline.getCenterX();
				int y = (int) TheWells[r][c].outline.getCenterY();
				if (highlightBox.contains(new Point(x, y)))
					TheWells[r][c].setSelected(true);
				else if (!keepOldHighlights)
					TheWells[r][c].setSelected(false);
			}
	}
	
	/** Returns a String[] containing all the names of the wells in the plate (ex: A01, A02, etc..)
	 * @author BLM*/
	public String[] getAllWellNames()
	{
		int counter = 0;
		String[] temp = new String[NumRows * NumCols];
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
			{
				temp[counter] = TheWells[r][c].name;
				counter++;
			}
		return temp;
	}
	
	/** Gets the color of the given row index
	 * @author BLM*/
	static public Color getRowColor(int index)
	{
		return tools.ColorRama.getColor(index);
	}
	
	/** Sets whether to display image counts inside the wells
	 * @author BLM*/
	public void allowImageCountDisplay(boolean boo)
	{
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].AllowImageCountDisplay = boo;
	}
	
	/** Produces a copy of this plate
	 * @author BLM*/
	public Plate copy()
	{
		Plate plate = new Plate(NumRows, NumCols, ID);
//		plate.the
		plate.Ystart = Ystart;
		plate.Xstart = Xstart;
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				plate.TheWells[r][c] = TheWells[r][c].copy(plate);
		
		return plate;
	}
	
	/** Returns the well within this plate with the given name (ex: name= A01)
	 * @author BLM*/
	public Well getWell(String st)
	{
		Well well = null;
		
		int c = getColumnIndex(st);
		int r = getRowIndex(st);
		
		return TheWells[r - 1][c - 1];
	}
	
	/** Initializes the min/max values of all features within the plate
	 * @author BLM*/
	public void initMinMaxFeatureValues()
	{
		if (MainGUI.getGUI().getTheFeatures() == null)
			return;
		
		int len = MainGUI.getGUI().getTheFeatures().size();
		MinMaxFeatureValues = new float[2][len];
		for (int i = 0; i < len; i++)
		{
			MinMaxFeatureValues[0][i] = Float.POSITIVE_INFINITY;
			MinMaxFeatureValues[1][i] = Float.NEGATIVE_INFINITY;
		}
		MinMaxFeatureValues_log = new float[2][len];
		for (int i = 0; i < len; i++)
		{
			MinMaxFeatureValues_log[0][i] = Float.POSITIVE_INFINITY;
			MinMaxFeatureValues_log[1][i] = Float.NEGATIVE_INFINITY;
		}
	}
	
	public double[][] getBimodalDistance(Feature f)
	{
		double[][] dist = new double[NumRows][NumCols];
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				dist[r][c] = TheWells[r][c].getBimodalDistance(f);
		return dist;
	}
	
	/** Captures a scalable vector graphic of the plate
	 * @author BLM */
	public void captureSVG(PrintWriter pw)
	{
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle("Plate");
		
		draw(g2, true);
		
		g2.printEnd();
		pw.flush();
		pw.close();
	}
	
	/** Caputures a PNG or JPG of the plate
	 * @author BLM*/
	public void captureImage(File file, String imageType)
	{
		int width = getWidth();
		int height = getHeight();
		
		BufferedImage im = new BufferedImage(width, height,
											 BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();
		
		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r, c, Color.WHITE.getRGB());
		
		draw(g2, true);
		
		try
		{
			ImageIO.write(im, imageType, file);
		}
		catch (IOException e)
		{
			System.out.println("**Error Printing Image**");
		}
	}
	
	public void loadWellMeanAndStdevData()
	{
		//Trying to write mean value data to file
		String projPath = main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
		
		File f = new File(projPath+File.separator+"Data"+File.separator+"plate_"+(getID()-1)+File.separator+"wellMeans.h5");
//		System.out.println("f: "+f.getAbsolutePath());
		if (!f.exists())
			return;
		
		int plateIndex = getID()-1;
		SegmentationHDFConnector sCon = new SegmentationHDFConnector(projPath, "Data");
		try
		{
			for (int i = 0; i < NumRows; i++)
			{
				for (int j = 0; j < NumCols; j++)
				{
					int wellIndex = (TheWells[i][j].getPlate().getNumRows()*TheWells[i][j].Column)+TheWells[i][j].Row;
					float[] arr = sCon.readWellMeanValues(plateIndex, wellIndex);
					if (arr!=null)
						TheWells[i][j].Feature_Means = arr;
					arr = sCon.readWellStdDevValues(plateIndex, wellIndex);
					if (arr!=null)
						TheWells[i][j].Feature_Stdev = arr;
				}
			}
		}
		catch (HDFConnectorException e) {System.out.println("ERROR: reading HDF well mean/stdev file");}
		repaint();
	}
	
	/** */
	public int shouldDisplayMetaData()
	{
		return DisplayMetaData;
	}
	
	
	/** */
	public MetaDataConnector getMetaDataConnector()
	{
		initMetaDataConnector();
		return TheMetaDataWriter;
	}
	/** */
	public Hashtable getMetaDataHashtable()
	{
		return TheMetaDataHashtable;
	}
	
	/** Main Drawing method for graphical representation of the plate
	 * @author BLM*/
	public void draw(Graphics2D g2, boolean plotToSVG)
	{
		// finding the Ystart
		Ystart =  50;
		
		// drawing the wells
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].draw(g2);
		
		// drawing the box borders
		g2.setColor(Color.gray);
		g2.fillRect((Xstart - 3), (Ystart - 3), (getPlateWidth() - 3), (getPlateHeight() + 8));
		
		// drawing a graded border around it
		for (int i = 0; i < 5; i++)
		{
			g2.setColor(BorderColors[i]);
			g2.drawRect((Xstart - 3 - i), (Ystart - 3 - i),
							(getPlateWidth() - 2 + 2 * i),
							(getPlateHeight() + 8 + 2 * i));
		}
		
		updateWellDisplayColors();
		
		// drawing the wells
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
			{
				//For when printing off bimodal/monomodal fit data --> end of row hard return
				if (c==NumCols-1)
					if (pw!=null)
						pw.println();
				
				TheWells[r][c].draw(g2);
			}
		//For when printing off bimodal/monomodal fit data --> end of row hard return
		if (pw!=null)
		{
			pw.flush();
			pw.close();
			pw = null;
		}
		
		if (DisplayRowLegend)
		{
			if (MainGUI.getGUI().getLinePlot().getPlotType()  == LinePlot.ROWS)
			{
				for (int i = 0; i < NumRows; i++)
				{
					int y = (int) (Ystart + i
									   * (TheWells[i][0].outline.width + 3)
									   + TheWells[i][0].outline.width / 2f - 3);
					int x = (int) (Xstart + NumCols
									   * (TheWells[i][0].outline.width + 3));
					
					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			}
			else
			{
				for (int i = 0; i < NumCols; i++)
				{
					int y = Ystart - 15;
					int x = (int) (Xstart + i
									   * (TheWells[0][i].outline.width + 3)
									   + TheWells[0][i].outline.width / 2f - 3);
					
					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			}
		}
		
		//
		// If want to block the plate
		if (Block)
		{
			highlightBox = null;
			Composite comp = g2.getComposite();
			g2.setComposite(translucComposite);
			g2.setColor(Color.black);
			TheBlockingRectangle = new Rectangle(0, 0, getWidth(), getHeight());
			g2.fill(TheBlockingRectangle);
			g2.setComposite(comp);
			
			g2.setColor(Color.red);
			Font f = g2.getFont();
			g2.setFont(MainGUI.Font_18);
			String st = "Plate Locked while Viewing Images";
			g2.drawString(st, getWidth() / 2f - (st.length() * 5),
						  getHeight() / 2f);
			st = "(Click to Unlock)";
			g2.drawString(st, getWidth() / 2f - (st.length() * 5),
						  getHeight() / 2f + 27);
			g2.setFont(f);
		}
		
		
		if(DisplayMetaData>-1 && TheLegend!=null)
			TheLegend.draw(g2, plotToSVG);
		
	}
	
	
	
	static public String getRowName(String wellName)
	{
		if (wellName.indexOf("A") >= 0)
			return "A";
		else if (wellName.indexOf("B") >= 0)
			return "B";
		else if (wellName.indexOf("C") >= 0)
			return "C";
		else if (wellName.indexOf("D") >= 0)
			return "D";
		else if (wellName.indexOf("E") >= 0)
			return "E";
		else if (wellName.indexOf("F") >= 0)
			return "F";
		else if (wellName.indexOf("G") >= 0)
			return "G";
		else if (wellName.indexOf("H") >= 0)
			return "H";
		else if (wellName.indexOf("I") >= 0)
			return "I";
		else if (wellName.indexOf("J") >= 0)
			return "J";
		else if (wellName.indexOf("K") >= 0)
			return "K";
		else if (wellName.indexOf("L") >= 0)
			return "L";
		else if (wellName.indexOf("M") >= 0)
			return "M";
		else if (wellName.indexOf("N") >= 0)
			return "N";
		else if (wellName.indexOf("O") >= 0)
			return "O";
		else if (wellName.indexOf("P") >= 0)
			return "P";
		
		return null;
	}
	
	static public String getRowName(int ind)
	{
		switch (ind)
		{
			case 0:
				return "A";
			case 1:
				return "B";
			case 2:
				return "C";
			case 3:
				return "D";
			case 4:
				return "E";
			case 5:
				return "F";
			case 6:
				return "G";
			case 7:
				return "H";
				
			case 8:
				return "I";
			case 9:
				return "J";
			case 10:
				return "K";
			case 11:
				return "L";
			case 12:
				return "M";
			case 13:
				return "N";
			case 14:
				return "O";
			case 15:
				return "P";
				
			default:
				return "";
		}
	}
	
	static public String getColumnName(int ind)
	{
		switch (ind)
		{
			case 0:
				return "01";
			case 1:
				return "02";
			case 2:
				return "03";
			case 3:
				return "04";
			case 4:
				return "05";
			case 5:
				return "06";
			case 6:
				return "07";
			case 7:
				return "08";
			case 8:
				return "09";
			case 9:
				return "10";
			case 10:
				return "11";
			case 11:
				return "12";
				
			case 12:
				return "13";
			case 13:
				return "14";
			case 14:
				return "15";
			case 15:
				return "16";
			case 16:
				return "17";
			case 17:
				return "18";
			case 18:
				return "19";
			case 19:
				return "20";
			case 20:
				return "21";
			case 21:
				return "22";
			case 22:
				return "23";
			case 23:
				return "24";
				
			default:
				return "" + (ind + 1);
		}
	}
	
	static public int getColumnIndex(String name)
	{
		if (name.indexOf("01") >= 0)
			return 1;
		else if (name.indexOf("02") >= 0)
			return 2;
		else if (name.indexOf("03") >= 0)
			return 3;
		else if (name.indexOf("04") >= 0)
			return 4;
		else if (name.indexOf("05") >= 0)
			return 5;
		else if (name.indexOf("06") >= 0)
			return 6;
		else if (name.indexOf("07") >= 0)
			return 7;
		else if (name.indexOf("08") >= 0)
			return 8;
		else if (name.indexOf("09") >= 0)
			return 9;
		else if (name.indexOf("10") >= 0)
			return 10;
		else if (name.indexOf("11") >= 0)
			return 11;
		else if (name.indexOf("12") >= 0)
			return 12;
		
		if (name.indexOf("13") >= 0)
			return 13;
		else if (name.indexOf("14") >= 0)
			return 14;
		else if (name.indexOf("15") >= 0)
			return 15;
		else if (name.indexOf("16") >= 0)
			return 16;
		else if (name.indexOf("17") >= 0)
			return 17;
		else if (name.indexOf("18") >= 0)
			return 18;
		else if (name.indexOf("19") >= 0)
			return 19;
		else if (name.indexOf("20") >= 0)
			return 20;
		else if (name.indexOf("21") >= 0)
			return 21;
		else if (name.indexOf("22") >= 0)
			return 22;
		else if (name.indexOf("23") >= 0)
			return 23;
		else if (name.indexOf("24") >= 0)
			return 24;
		
		return -1;
	}
	
	static public int getRowIndex(String name)
	{
		
		if (name.indexOf("A") >= 0)
			return 1;
		else if (name.indexOf("B") >= 0)
			return 2;
		else if (name.indexOf("C") >= 0)
			return 3;
		else if (name.indexOf("D") >= 0)
			return 4;
		else if (name.indexOf("E") >= 0)
			return 5;
		else if (name.indexOf("F") >= 0)
			return 6;
		else if (name.indexOf("G") >= 0)
			return 7;
		else if (name.indexOf("H") >= 0)
			return 8;
		else if (name.indexOf("I") >= 0)
			return 9;
		else if (name.indexOf("J") >= 0)
			return 10;
		else if (name.indexOf("K") >= 0)
			return 11;
		else if (name.indexOf("L") >= 0)
			return 12;
		else if (name.indexOf("M") >= 0)
			return 13;
		else if (name.indexOf("N") >= 0)
			return 14;
		else if (name.indexOf("O") >= 0)
			return 15;
		else if (name.indexOf("P") >= 0)
			return 16;
		
		return -1;
	}
	
	static public Color getRowColor(String name)
	{
		if (name.indexOf("A") >= 0)
			return tools.ColorRama.getColor(0);
		else if (name.indexOf("B") >= 0)
			return tools.ColorRama.getColor(1);
		else if (name.indexOf("C") >= 0)
			return tools.ColorRama.getColor(2);
		else if (name.indexOf("D") >= 0)
			return tools.ColorRama.getColor(3);
		else if (name.indexOf("E") >= 0)
			return tools.ColorRama.getColor(4);
		else if (name.indexOf("F") >= 0)
			return tools.ColorRama.getColor(5);
		else if (name.indexOf("G") >= 0)
			return tools.ColorRama.getColor(6);
		else if (name.indexOf("H") >= 0)
			return tools.ColorRama.getColor(7);
			
		else if (name.indexOf("I") >= 0)
			return tools.ColorRama.getColor(8);
		else if (name.indexOf("J") >= 0)
			return tools.ColorRama.getColor(9);
		else if (name.indexOf("K") >= 0)
			return tools.ColorRama.getColor(10);
		else if (name.indexOf("L") >= 0)
			return tools.ColorRama.getColor(11);
		else if (name.indexOf("M") >= 0)
			return tools.ColorRama.getColor(12);
		else if (name.indexOf("N") >= 0)
			return tools.ColorRama.getColor(13);
		else if (name.indexOf("O") >= 0)
			return tools.ColorRama.getColor(14);
		else if (name.indexOf("P") >= 0)
			return tools.ColorRama.getColor(15);
		
		return null;
	}
	
	static public Color getColColor(String name)
	{
		if (name.indexOf("01") >= 0)
			return tools.ColorRama.getColor(0);
		else if (name.indexOf("02") >= 0)
			return tools.ColorRama.getColor(1);
		else if (name.indexOf("03") >= 0)
			return tools.ColorRama.getColor(2);
		else if (name.indexOf("04") >= 0)
			return tools.ColorRama.getColor(3);
		else if (name.indexOf("05") >= 0)
			return tools.ColorRama.getColor(4);
		else if (name.indexOf("06") >= 0)
			return tools.ColorRama.getColor(5);
		else if (name.indexOf("07") >= 0)
			return tools.ColorRama.getColor(6);
		else if (name.indexOf("08") >= 0)
			return tools.ColorRama.getColor(7);
		else if (name.indexOf("09") >= 0)
			return tools.ColorRama.getColor(8);
		else if (name.indexOf("10") >= 0)
			return tools.ColorRama.getColor(9);
		else if (name.indexOf("11") >= 0)
			return tools.ColorRama.getColor(10);
		else if (name.indexOf("12") >= 0)
			return tools.ColorRama.getColor(11);
			
		else if (name.indexOf("13") >= 0)
			return tools.ColorRama.getColor(12);
		else if (name.indexOf("14") >= 0)
			return tools.ColorRama.getColor(13);
		else if (name.indexOf("15") >= 0)
			return tools.ColorRama.getColor(14);
		else if (name.indexOf("16") >= 0)
			return tools.ColorRama.getColor(15);
		else if (name.indexOf("17") >= 0)
			return tools.ColorRama.getColor(16);
		else if (name.indexOf("18") >= 0)
			return tools.ColorRama.getColor(17);
		else if (name.indexOf("19") >= 0)
			return tools.ColorRama.getColor(18);
		else if (name.indexOf("20") >= 0)
			return tools.ColorRama.getColor(19);
		else if (name.indexOf("21") >= 0)
			return tools.ColorRama.getColor(20);
		else if (name.indexOf("22") >= 0)
			return tools.ColorRama.getColor(21);
		else if (name.indexOf("23") >= 0)
			return tools.ColorRama.getColor(22);
		else if (name.indexOf("24") >= 0)
			return tools.ColorRama.getColor(23);
		
		return null;
	}
	
	/** Inner class thread runner*/
	static public class CellLoader implements Runnable
	{
		private Thread thread;
		private ArrayList<Well> wells;
		private boolean loadCoords;
		private boolean loadDataVals;
		
		public CellLoader(ArrayList<Well> wellsToLoad, boolean loadCoords,
				boolean loadDataVals)
		{
			this.loadDataVals = loadDataVals;
			this.loadCoords = loadCoords;
			wells = wellsToLoad;
		}
		
		public void start()
		{
			thread = new Thread(this);
			thread.start();
		}
		
		public void run()
		{
			// Project name
			String projectPath =  main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
			String algorithmNameUsed = "Data";
			SegmentationHDFConnector sCon = new SegmentationHDFConnector(projectPath, algorithmNameUsed);
			if(wells!=null && wells.size()>0)
				for (int i = 0; i < wells.size(); i++)
					wells.get(i).loadCells(sCon, loadCoords, loadDataVals);
		}
		
	}
}

