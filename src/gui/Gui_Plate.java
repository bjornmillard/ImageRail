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

package gui;

import imPanels.ImageCapturePanel;
import imPanels.JPanel_highlightBox;
import imageViewers.FieldViewer_Frame;
import imageViewers.ImageMontage;

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

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

import midasGUI.MetadataParser;
import models.Model_Plate;
import models.Model_Well;
import plots.Legend;
import plots.LinePlot;
import sdcubeio.ExpDesign_IO;
import sdcubeio.ExpDesign_Model;
import sdcubeio.ExpDesign_Sample;
import tools.PanelDropTargetListener;
import tools.SVG_writer;
import dialogs.CaptureImage_Dialog;
import features.Feature;

public class Gui_Plate extends JPanel_highlightBox implements ImageCapturePanel {
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
	/** Depricated - Used to allow modified plate heat maps such as linear slope displays*/
	private int DisplayType;
	private static int NORMAL = 0;
	/** If true, this will place a blocking rectangle on top of the plate*/
	private boolean Block;
	private Rectangle TheBlockingRectangle;
	/** Buttons for the toolbar*/
	private JButton CaptureImageButton;
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
	private final JButton MetaButton = new JButton(new ImageIcon(
			"icons/meta.png"));

	/** */
	private boolean Dragging;
	/** The Model_plate for reference*/
	private Model_Plate TheModel;
	private Gui_Plate ThisGUI;


	// /** Matrix of this plate's Wells */
	// private Gui_Well[][] TheWells;
	
	/** Main Model_Plate Constructor
	 * @author BLM*/
	public Gui_Plate(Model_Plate plateModel)
	{
		plateModel.setGUI(this);
		ThisGUI = this;
		initWells(plateModel);
		TheToolBar = new JToolBar();
		TheModel = plateModel;
		DisplayType = NORMAL;
		setLayout(new BorderLayout());
		DisplayRowLegend = false;
		continuallyDisplayHighlightBox(true);
		setSize(445, 520);
		setPreferredSize(new Dimension(445, 520));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setVisible(true);
		Xstart = 10;
		Ystart = 5;
		Block = false;
		TheBlockingRectangle = null;
		ToolBarPosition = BorderLayout.NORTH;
		Legend_xy = new Point(100, getHeight()-100);
		DisplayMetaData = -1;
		updatePanel();
		Dragging = false;

		
		//Adding the Drag and Drop feature for file loading
		PanelDropTargetListener drop = new PanelDropTargetListener();
		drop.ThePlate = TheModel;
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
				CaptureImage_Dialog s = new CaptureImage_Dialog(ThisGUI);
					}
				});
		
		
		if (TheModel.getTitle() == null)
			TheModel.setTitle("Plate #" + TheModel.getID());
		
		final JButton titleLabel = new JButton(TheModel.getTitle());
		titleLabel.addMouseListener(new MouseAdapter()
									{
					public void mouseClicked(MouseEvent e)
					{
						
				gui.MainGUI.getGUI().getPlateHoldingPanel()
						.setTab(TheModel.getID()+1);
						if(e.getClickCount()>1)
						{
							String response = JOptionPane.showInputDialog(null,
																		  "Enter New Name of Model_Plate: ",
																		  "Enter New Name of Model_Plate: ",
																		  JOptionPane.QUESTION_MESSAGE);
							if (response==null)
								return;
							
					TheModel.setTitle(response);
					titleLabel.setText(TheModel.getTitle());
					gui.MainGUI
							.getGUI()
							.getPlateHoldingPanel()
							.getTheMainPanel()
							.setTitleAt(TheModel.getID() + 1,
									TheModel.getTitle());
						}
						
						
						
					}
				});
		TheToolBar.add(titleLabel);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Helvetica", Font.BOLD, 16));
		titleLabel.setEnabled(true);
		
		final JButton montageButton = new JButton(tools.Icons.Icon_Montage);
		montageButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				ImageMontage m = new ImageMontage(TheModel, 0, 0);

			}
		});
		TheToolBar.add(montageButton);
		
		 TheToolBar.add(MetaButton);
		 MetaButton.setToolTipText("Import IncuCyte Metadata file");
		 MetaButton.addActionListener(new ActionListener()
		 {
			 public void actionPerformed(ActionEvent ae)
			 {
				 //FileChooser
					File dir = models.Model_Main.getModel().getTheDirectory();
					JFileChooser fc = null;
					if (dir != null)
						fc = new JFileChooser(dir);
					else
						fc = new JFileChooser();

					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						models.Model_Main.getModel().setTheDirectory(new File(file.getParent()));

						//Make sure its the correct file format
						// NOTE: currently we only support the IncuCyte XML metadata format
						if(file.getName().indexOf(".PlateMap")<=0)
						{
							JOptionPane.showMessageDialog(null,"Invalid Metadata File Type! \n\n Please try again","Invalid File",JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						ExpDesign_Model model = MetadataParser.parse_IncuCyteXML(file.getAbsolutePath(),
								TheModel, models.Model_Main.getModel().getExpDesignConnector());
						ExpDesign_IO.write(model);
						
						System.out.println("model: "+model.getSamples().size());
						ArrayList<ExpDesign_Sample> samples = model.getSamples();
						for(int i =0; i < samples.size(); i++)
						{
							System.out.println(samples.get(i));
						}
					}
					
			 }
		 });

	}

	public void updateMetaDataLegend()
	{
		if(Dragging)
			return;
		//Trying to create a metaDataconnector to pass metadata to the linegraph
		// so we can have axis labeling and the such
		

		// if (getModel().TheMetaDataWriter == null)
		// {
		// String projPath =
		// models.Model_Main.getModel().getProjectDirectory().getAbsolutePath();
		// try
		// {
		// getModel().TheMetaDataWriter = new MetaDataConnector(projPath);
		// }
		// catch(Exception e)
		// {
		// System.out.println("------* Error creating MetaData XML writer *------");
		// }
		// }
		

		// if(DisplayMetaData>-1)
		// {
		// MetaButton.setIcon(new ImageIcon("icons/meta_selected.png"));
		//			
		// //Getting all unique meta data treatment combinations
		// getModel().TheMetaDataHashtable = getModel().initMetaDataHashtable(
		// getModel(),
		// getModel().TheMetaDataWriter);
		// String[] names = new String[getModel().TheMetaDataHashtable.size()];
		// Color[] colors = new Color[getModel().TheMetaDataHashtable.size()];
		// int counter = 0;
		// //First Legend entry will be the Title:
		// String title = "";
		// if(DisplayMetaData==0)
		// title = "Treatments:";
		// else if(DisplayMetaData==1)
		// title = "Measurements:";
		// else if(DisplayMetaData==2)
		// title = "Descriptions:";
		// else if(DisplayMetaData==3)
		// title = "Time Points:";
		//			
		// for (Enumeration e = getModel().TheMetaDataHashtable.keys(); e
		// .hasMoreElements();)
		// {
		// names[counter] = (String)(e.nextElement());
		// colors[counter] = (Color) getModel().TheMetaDataHashtable
		// .get(names[counter]);
		// counter++;
		// }
		//			
		// TheLegend = new Legend(title, names, colors, Legend_xy.x,
		// Legend_xy.y);
		// }
		else
		{
			getModel().TheMetaDataHashtable = null;
			MetaButton.setIcon(new ImageIcon("icons/meta.png"));
			TheLegend = null;
		}
		
		updatePanel();
		repaint();
	}


	/**
	 * Initialize the GUI wells
	 * 
	 * @author BLM
	 */
	public void initWells(Model_Plate plateModel) {
		int NumRows = plateModel.getNumRows();
		int NumCols = plateModel.getNumColumns();
		// TheWells = new Gui_Well[NumRows][NumCols];
		Model_Well[][] wellsModel = plateModel.getWells();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				wellsModel[r][c].initGUI();
	}

	/**
	 * Updates the Model_Plate Panel by first re-initializing the height and
	 * width
	 * 
	 * @author BLM
	 */
	public void updatePanel() {
		removeAll();
		add(TheToolBar, ToolBarPosition);
		if (models.Model_Main.getModel().getPlateRepository_GUI() != null
				&& gui.MainGUI.getGUI().getPlateHoldingPanel().showData()) {
			add(getDataTable(), BorderLayout.CENTER);
		}

		getPlateHeight();
		getPlateWidth();
		validate();
		repaint();
	}


	/**
	 * Returns a scrollpane containging the data table for the currently
	 * selected Feature
	 * 
	 * @author BLM
	 */
	private JScrollPane getDataTable() {
		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();
		// Constructing the data table at bottom
		String[] headers = new String[NumCols + 1];
		headers[0] = "";
		for (int i = 1; i < headers.length; i++)
			headers[i] = "";
		int rowCounter = 0;
		int rowBuffer = 4;
		// Setting up the table
		Object[][] dat = new Object[NumRows * 2 + rowBuffer + 3][headers.length];
		// Column names
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i] = "" + Model_Plate.getColumnName(i - 1);
		rowCounter++;

		dat[rowCounter][0] = "MEANS:";
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i] = "";
		rowCounter++;
		Model_Well[][] TheWells = getModel().getWells();
		// Data values
		for (int r = 0; r < NumRows; r++) {
			dat[rowCounter][0] = Model_Plate.getRowName(r);
			for (int c = 1; c < dat[r].length; c++) {
				float val = TheWells[r][c - 1]
						.getCurrentValue_Mean();
				dat[rowCounter][c] = new Float(val);
			}
			rowCounter++;
		}
		// Buffer
		for (int r = 0; r < rowBuffer; r++) {
			for (int c = 0; c < dat[r].length; c++)
				dat[rowCounter][c] = "";
			rowCounter++;
		}
		dat[rowCounter][0] = "STDEVs:";
		for (int i = 1; i < headers.length; i++)
			dat[rowCounter][i] = "";
		rowCounter++;
		// Stdevs
		for (int r = 0; r < NumRows; r++) {
			dat[rowCounter][0] = Model_Plate.getRowName(r);
			for (int c = 1; c < dat[r].length; c++) {
				float val = TheWells[r][c - 1]
						.getCurrentValue_Stdev();
				dat[rowCounter][c] = new Float(val);
			}
			rowCounter++;
		}
		// Setting colors of certain cells
		TheDataTable = new JTable(dat, headers) {
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if ((vColIndex == 0 || rowIndex == 0)
						&& !isCellSelected(rowIndex, vColIndex))
					c.setBackground(tools.ColorRama.Gray_veryLight1);
				else if (isCellSelected(rowIndex, vColIndex))
					c.setBackground(tools.ColorRama.Yellow_Light);
				else
					c.setBackground(getBackground());

				return c;
			}
		};

		// Setting column widths
		TheDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int c = 0; c < TheDataTable.getColumnCount(); c++)
			packColumn(TheDataTable, c, 2);

		// Setting user selection preferences
		TheDataTable.setCellSelectionEnabled(true);
		TheDataTable.setColumnSelectionAllowed(true);
		TheDataTable.setRowSelectionAllowed(true);

		TheDataTable.setPreferredSize(new Dimension(getWidth(), getHeight()));
		return JTable.createScrollPaneForTable(TheDataTable);
	}

	/**
	 * Packs and formats the data table columns
	 * 
	 * @author BLM
	 */
	private void packColumn(JTable table, int vColIndex, int margin) {
		TableModel model = table.getModel();
		DefaultTableColumnModel colModel = (DefaultTableColumnModel) table
				.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);
		int width = 0;

		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null) {
			renderer = table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(table, col
				.getHeaderValue(), false, false, 0, 0);
		width = comp.getPreferredSize().width;

		// Get maximum width of column data
		for (int r = 0; r < table.getRowCount(); r++) {
			renderer = table.getCellRenderer(r, vColIndex);
			comp = renderer.getTableCellRendererComponent(table, table
					.getValueAt(r, vColIndex), false, false, r, vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}

		// Add margin
		width += 2 * margin;

		// Set the width
		col.setPreferredWidth(width);
	}

	/**
	 * Draws the Model_Plate
	 * 
	 * @author BLM
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		draw(g2, false);

		super.paintHighlighting(g2);
	}

	/**
	 * Blocks the plate with a blocking panel
	 * 
	 * @author BLM
	 */
	public void block(FieldViewer_Frame im) {
		Block = true;
	}

	/**
	 * Unblocks the plate from the blocking panel
	 * 
	 * @author BLM
	 */
	public void unblock() {
		Block = false;
		TheBlockingRectangle = null;
		updatePanel();
	}

	/**
	 * Checks if the plate is currently getting blocked
	 * 
	 * @author BLM
	 */
	public boolean isBlocked() {
		return Block;
	}

	public void updateDimensions() {
		Model_Well[][] TheWells = getModel().getWells();
		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].getGUI().updateDimensions();
	}

	/**
	 * Returns boolean whether to display the color boxes representing the row
	 * legend
	 * 
	 * @author BLM
	 */
	public boolean shouldDisplayRowLegend() {
		return DisplayRowLegend;
	}

	/**
	 * Sets boolean whether to display the color boxes representing the row
	 * legend
	 * 
	 * @author BLM
	 */
	public void setDisplayRowLegend(boolean boo) {
		DisplayRowLegend = boo;
	}

	/**
	 * Re-initializes the heat mapping colors of the wells
	 * 
	 * @author BLM
	 */
	public void updateWellDisplayColors() {
		if (DisplayType == NORMAL) {
			int channel = 0;

			Feature f = models.Model_Main.getModel().getTheSelectedFeature();
			if (f != null)
 {
				channel = 0;
				String[] cnames = models.Model_Main.getModel().getTheChannelNames();
				if (cnames != null)
					for (int i = 0; i < cnames.length; i++) {
						if (f.getName().indexOf(cnames[i]) > 0)
							channel = i;
					}
			}

			// getting the selected feature index
			int fIndex = models.Model_Main.getModel().getTheSelectedFeature_Index();

			int MeanOrCV = 0;
			if (shouldDisplayCV())
				MeanOrCV = 1;

			float[] minMax = TheModel
.getMinMaxAcrossPlates(gui.MainGUI
					.getGUI()
					.getPlateHoldingPanel()
.shouldNormalizeAcrossAllPlates(),
					MeanOrCV);


			Model_Well[][] TheWells = getModel().getWells();
			int NumRows = TheModel.getNumRows();
			int NumCols = TheModel.getNumColumns();
			for (int r = 0; r < NumRows; r++)
				for (int c = 0; c < NumCols; c++)
					if (TheWells[r][c].Feature_Means != null
							&& tools.MathOps
.sum(TheWells[r][c].Feature_Means) != 0
							&& TheWells[r][c].Feature_Means.length > models.Model_Main
									.getModel().getTheSelectedFeature_Index()) {
						float val = 0;

						// Option to plot CV vs Mean Value
						if (!shouldDisplayCV()) // Mean
							val = TheWells[r][c].Feature_Means[fIndex];
						else
							// CV
							val = TheWells[r][c].Feature_Stdev[fIndex]
									/ TheWells[r][c].Feature_Means[fIndex];

						if (!shouldDisplayCV())
 {
						if (channel == 0)
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color, tools.ColorMaps.BLUE);
						else if (channel == 1)
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color,
												tools.ColorMaps.GREEN);
						else
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color,
 tools.ColorMaps.RED);
						} else {
							if (channel == 0)
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color, tools.ColorMaps.BLUE);
						else if (channel == 1)
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color,
									tools.ColorMaps.WHITEPURPLE);
						else
							tools.ColorMaps.getColorValue(val, minMax[0],
									minMax[1], color,
									tools.ColorMaps.WHITEPURPLE);
						}

						TheWells[r][c].getGUI().color = new Color(color[0],
								color[1],
								color[2]);
					}
		}

	}

	/***
	 * States whether the plate is displaying CV or Mean
	 * 
	 * @author BLM
	 */
	public boolean shouldDisplayCV() {
		return gui.MainGUI.getGUI().getPlateHoldingPanel().shouldDisplayCV();
	}

	/**
	 * Returns the array of colors used to define the colors of the plate border
	 * 
	 * @author BLM
	 */
	public Color[] getBorderColors() {
		return BorderColors;
	}

	/**
	 * Sets the y-value in the panel where the plate will be begun to be drawn
	 * 
	 * @author BLM
	 */
	public void setYstart(int val) {
		Ystart = val;
	}

	/**
	 * sets the x-value in the panel where the plate will be begun to be drawn
	 * 
	 * @author BLM
	 */
	public void setXstart(int val) {
		Xstart = val;
	}

	/**
	 * Gets the y-value in the panel where the plate will be begun to be drawn
	 * 
	 * @author BLM
	 */
	public int getYstart() {
		return Ystart;
	}

	/**
	 * Gets the x-value in the panel where the plate will be begun to be drawn
	 * 
	 * @author BLM
	 */
	public int getXstart() {
		return Xstart;
	}

	/**
	 * Returns the toolbar associated with the plate
	 * 
	 * @author BLM
	 */
	public JToolBar getTheToolBar() {
		return TheToolBar;
	}

	/**
	 * Sets the position of the Toolbar
	 * 
	 * @author BLM
	 */
	public void setToolBarPosition(String position) {
		ToolBarPosition = position;
	}

	/**
	 * Returns the plate height
	 * 
	 * @author BLM
	 */
	public int getPlateHeight() {
		Model_Well[][] TheWells = getModel().getWells();
		int NumRows = TheModel.getNumRows();
		return (TheWells[NumRows - 1][0].getGUI().getYpos() + TheWells[NumRows - 1][0]
				.getGUI()
				.getHeight())
				- TheWells[0][0].getGUI().getYpos();
	}

	/**
	 * Returns the plate width
	 * 
	 * @author BLM
	 */
	public int getPlateWidth() {
		Model_Well[][] TheWells = getModel().getWells();
		int NumCols = TheModel.getNumColumns();
		return TheWells[0][NumCols - 1].getGUI().getXpos()
				+ TheWells[0][NumCols - 1].getGUI().getWidth() + 5;
	}

	/**
	 * If there was a click event, this returns the index of the appropriate
	 * well that was clicked on
	 * 
	 * @author BLM
	 */
	public int[] getWellIndex(MouseEvent p1) {
		Model_Well[][] TheWells = getModel().getWells();
		Point p = p1.getPoint();
		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				if (TheWells[r][c].getGUI().outline.contains(p)) {
					int[] ind = new int[2];
					ind[0] = r;
					ind[1] = c;
					return ind;
				}
		return null;
	}

	public void mousePressed(MouseEvent p1) {

		if (TheLegend != null && TheLegend.contains(p1.getPoint())) {
			TheLegend.setDragging(true);
		} else {

			if (highlightBox != null && highlightBox.contains(p1.getPoint())) {
				dX = p1.getX() - highlightBox.x;
				dY = p1.getY() - highlightBox.y;
				CreateNewBox = false;
			} else {
				startBox_XY = p1.getPoint();
				startHighlightPoint = p1.getPoint();
				CreateNewBox = true;
			}
		}
		updatePanel();

	}

	/**
	 * Handles mouse clicks on the plate
	 * 
	 * @author BLM
	 */
	public void mouseClicked(MouseEvent p1) {

		if (Block) {
			Point p = p1.getPoint();

			if (TheBlockingRectangle.contains(p))
				unblock();

			return;
		}

		if (p1.getClickCount() >= 2) {
			highlightBox = null;
			startHighlightPoint = null;
			unHighlightAllWells();
		}

		gui.MainGUI.getGUI().updateMidasInputPanel();

		int[] inWell = getWellIndex(p1);

		if (inWell != null) {
			if (p1.getClickCount() != 1)
				return;
			Model_Well[][] TheWells = getModel().getWells();
			Model_Well well = TheWells[inWell[0]][inWell[1]];
			well.toggleHighlightState();
			gui.MainGUI.getGUI().updateMidasInputPanel();
			inWell = null;

		}

		updatePanel();
		if (gui.MainGUI.getGUI().getDotPlot() != null)
			if (gui.MainGUI.getGUI().getLeftDisplayPanelType() == models.Model_Main
					.getModel().DOTPLOT)
				gui.MainGUI.getGUI().getDotPlot().UpdatePlotImage = true;

		gui.MainGUI.getGUI().updateAllPlots();
	}

	/**
	 * Handles mouse release events
	 * 
	 * @author BLM
	 */
	public void mouseReleased(MouseEvent p) {
		Dragging = false;
		if (gui.MainGUI.getGUI().getDotPlot() != null)
			if (gui.MainGUI.getGUI().getLeftDisplayPanelType() == gui.MainGUI
					.getGUI().DOTPLOT)
				gui.MainGUI.getGUI().getDotPlot().UpdatePlotImage = true;

		if (TheLegend != null)
			TheLegend.setDragging(false);

		gui.MainGUI.getGUI().getPlateHoldingPanel()
				.setLastTouched_PlateID(
				TheModel.getID());
		gui.MainGUI.getGUI().updateAllPlots();
	}

	/**
	 * Handles mouse drag events
	 * 
	 * @author BLM
	 */
	public void mouseDragged(MouseEvent p1) {
		Dragging = true;
		if (Block)
			return;

		if (TheLegend != null && TheLegend.isDragging()) {
			TheLegend.setX((int) (p1.getPoint().x - TheLegend.getWidth() / 2f));
			TheLegend
					.setY((int) (p1.getPoint().y - TheLegend.getHeight() / 2f));
			Legend_xy.x = TheLegend.getX();
			Legend_xy.y = TheLegend.getY();
			updatePanel();
			return;
		}
		if (CreateNewBox) {
			int xval = p1.getPoint().x - startHighlightPoint.x;
			int yval = p1.getPoint().y - startHighlightPoint.y;

			highlightBox = new Rectangle();
			highlightBox.x = startHighlightPoint.x;
			highlightBox.y = startHighlightPoint.y;
			highlightBox.width = 0;
			highlightBox.height = getHeight();

			if (xval >= 0)
				highlightBox.width = xval;
			else {
				highlightBox.x = p1.getPoint().x;
				highlightBox.width = -xval;
			}
			if (yval >= 0)
				highlightBox.height = yval;
			else {
				highlightBox.y = p1.getPoint().y;
				highlightBox.height = -yval;
			}
		} else {
			if (p1 == null || highlightBox == null)
				return;
			highlightBox.x = p1.getX() - dX;
			highlightBox.y = p1.getY() - dY;
		}

		highlightWells(highlightBox, p1.isShiftDown());
		updatePanel();

		if (gui.MainGUI.getGUI().getDotPlot() != null)
			if (gui.MainGUI.getGUI().getLeftDisplayPanelType() == gui.MainGUI
					.getGUI().DOTPLOT) {
				gui.MainGUI.getGUI().getDotPlot().UpdatePlotImage = false;
				gui.MainGUI.getGUI().getDotPlot().repaint();
			} else if (gui.MainGUI.getGUI().getLeftDisplayPanelType() == MainGUI
					.getGUI().MIDASINPUT) {

			} else
				gui.MainGUI.getGUI().updateAllPlots();

	}

	/**
	 * Unhighlights all the wells
	 * 
	 * @author BLM
	 */
	public void unHighlightAllWells() {
		gui.MainGUI.getGUI().getPlateHoldingPanel().unHighlightAllWells();
	}

	/**
	 * Highlights the wells enclosed by the given highlight box. You must also
	 * tell it if you want to keep old highlighting
	 * 
	 * @author BLM
	 */
	private void highlightWells(Rectangle highlightBox,
			boolean keepOldHighlights) {
		if (!keepOldHighlights)
			unHighlightAllWells();

		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();
		Model_Well[][] TheWells = getModel().getWells();

		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++) {
				int x = (int) TheWells[r][c].getGUI().outline.getCenterX();
				int y = (int) TheWells[r][c].getGUI().outline.getCenterY();
				if (highlightBox.contains(new Point(x, y)))
					TheWells[r][c].getGUI().getModel().setSelected(true);
				else if (!keepOldHighlights)
					TheWells[r][c].getGUI().getModel().setSelected(false);
			}
	}

	/** */
	public int shouldDisplayMetaData() {
		return DisplayMetaData;
	}

	/**
	 * Main Drawing method for graphical representation of the plate
	 * 
	 * @author BLM
	 */
	public void draw(Graphics2D g2, boolean plotToSVG) {
		// finding the Ystart
		Ystart = 50;

		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();

		// drawing the wells
		Model_Well[][] TheWells = getModel().getWells();
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].getGUI().draw(g2, plotToSVG);

		// drawing the box borders
		g2.setColor(Color.gray);
		g2.fillRect((Xstart - 3), (Ystart - 3), (getPlateWidth() - 3),
				(getPlateHeight() + 8));

		// drawing a graded border around it
		for (int i = 0; i < 5; i++) {
			g2.setColor(BorderColors[i]);
			g2.drawRect((Xstart - 3 - i), (Ystart - 3 - i),
					(getPlateWidth() - 2 + 2 * i),
					(getPlateHeight() + 8 + 2 * i));
		}

		updateWellDisplayColors();

		// drawing the wells
		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].getGUI().draw(g2, plotToSVG);


		if (DisplayRowLegend) {
			if (gui.MainGUI.getGUI().getLinePlot().getPlotType() == LinePlot.ROWS) {
				for (int i = 0; i < NumRows; i++) {
					int y = (int) (Ystart + i
							* (TheWells[i][0].getGUI().outline.width + 3)
							+ TheWells[i][0].getGUI().outline.width / 2f - 3);
					int x = (int) (Xstart + NumCols
							* (TheWells[i][0].getGUI().outline.width + 3));

					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			} else {
				for (int i = 0; i < NumCols; i++) {
					int y = Ystart - 15;
					int x = (int) (Xstart + i
							* (TheWells[0][i].getGUI().outline.width + 3)
							+ TheWells[0][i].getGUI().outline.width / 2f - 3);

					g2.setColor(getRowColor(i));
					g2.fillRect(x, y, 5, 5);
					g2.setColor(Color.black);
					g2.drawRect(x, y, 5, 5);
				}
			}
		}

		//
		// If want to block the plate
		if (Block) {
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

		if (DisplayMetaData > -1 && TheLegend != null)
			TheLegend.draw(g2, plotToSVG);

	}

	/**
	 * Gets the color of the given row index
	 * 
	 * @author BLM
	 */
	static public Color getRowColor(int index) {
		return tools.ColorRama.getColor(index);
	}

	/**
	 * Sets whether to display image counts inside the wells
	 * 
	 * @author BLM
	 */
	public void allowImageCountDisplay(boolean boo) {
		int NumRows = TheModel.getNumRows();
		int NumCols = TheModel.getNumColumns();
		Model_Well[][] TheWells = getModel().getWells();

		for (int r = 0; r < NumRows; r++)
			for (int c = 0; c < NumCols; c++)
				TheWells[r][c].getGUI().AllowImageCountDisplay = boo;
	}

	/** Returns the Plate model that this gui represents */
	public Model_Plate getModel() {
		return TheModel;
	}

	/**
	 * Captures a scalable vector graphic of the plate
	 * 
	 * @author BLM
	 */
	public void captureSVG(PrintWriter pw) {
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle(TheModel.getTitle());

		draw(g2, true);

		g2.printEnd();
		pw.flush();
		pw.close();
	}

	/**
	 * Caputures a PNG or JPG of the plate
	 * 
	 * @author BLM
	 */
	public void captureImage(File file, String imageType) {
		int width = getWidth();
		int height = getHeight();

		BufferedImage im = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();

		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r, c, Color.WHITE.getRGB());

		draw(g2, true);

		try {
			ImageIO.write(im, imageType, file);
		} catch (IOException e) {
			System.out.println("**Error Printing Image**");
		}
	}


}
