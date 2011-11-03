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

import imageViewers.FieldViewer;
import imageViewers.FieldViewer_Frame;
import imageViewers.HTMLViewer;
import imagerailio.ImageRail_SDCube;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import midasGUI.MidasInputPanel;
import models.Model_Field;
import models.Model_ParameterSet;
import models.Model_Plate;
import models.Model_PlateRepository;
import models.Model_Well;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import plots.DotPlot;
import plots.HistogramPlot;
import plots.LinePlot;
import processors.Processor_SingleCells;
import processors.Processor_WellAverage;
import sdcubeio.ExpDesign_Model;
import sdcubeio.H5IO_Exception;
import segmentedobject.Cell;
import segmentors.DefaultSegmentor;
import dataSavers.DataSaver_CSV;
import dataSavers.DataSaver_Cells_Midas_wMetaData;
import dataSavers.DataSaver_WellMeans_Midas_wMetaData;
import dialogs.PlateInputDialog;
import dialogs.SaveFeatures_Dialog;
import dialogs.ThresholdingBoundsInputDialog_SingleCells;
import dialogs.ThresholdingBoundsInputDialog_SingleCells_Osteo;
import dialogs.ThresholdingBoundsInputDialog_WellMeans;
import dialogs.ZScoreFilterDialog;
import features.Feature;
import features.FeatureSorter;
import filters.DotFilterQueue;
import filters.FeatureDetector;
import filters.FilterManager;


public class MainGUI extends JFrame {
	/** The GUI object */
	private static MainGUI TheMainGUI;
	private static MainStartupDialog TheStartupDialog;
	public static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";

	static public int MAXPIXELVALUE = 65535;
	static public final int MIDASINPUT = 0;
	static public final int LINEGRAPH = 1;
	static public final int DOTPLOT = 2;
	static public final int HISTOGRAM = 3;
	static final public Font Font_6 = new Font("Helvetica", Font.PLAIN, 6);
	static final public Font Font_8 = new Font("Helvetica", Font.PLAIN, 8);
	static final public Font Font_9 = new Font("Helvetica", Font.PLAIN, 9);
	static final public Font Font_12 = new Font("Helvetica", Font.BOLD, 12);
	static final public Font Font_14 = new Font("Helvetica", Font.BOLD, 14);
	static final public Font Font_16 = new Font("Helvetica", Font.BOLD, 16);
	static final public Font Font_18 = new Font("Helvetica", Font.BOLD, 18);
	static public NumberFormat nf = new DecimalFormat("0.##");
	static public BasicStroke Stroke_1 = new BasicStroke(1);
	static public BasicStroke Stroke_2 = new BasicStroke(2);
	static public BasicStroke Stroke_3 = new BasicStroke(3);
	static public BasicStroke Stroke_4 = new BasicStroke(4);

	private ArrayList<Feature> TheFeatures;
	private Feature TheSelectedFeature;
	private int TheSelectedFeature_Index;
	private JCheckBoxMenuItem DisplayNumberLoadedImagesCheckBox;
	private JCheckBoxMenuItem DisplayAvailableHDFfiles;
	private String[] ChannelNames;
	private JCheckBoxMenuItem WellMeanOrIntegratedIntensityCheckBox;
	private JCheckBoxMenuItem StoreCytoAndNuclearWellMeans;
	private final JCheckBoxMenuItem SelectAllCheckBox = new JCheckBoxMenuItem(
			"Select All Wells");
	private JCheckBoxMenuItem WatershedNucleiCheckBox;
	private JCheckBoxMenuItem LoadCellsImmediatelyCheckBox;
	private JCheckBoxMenuItem MultithreadCheckBox;
	private JCheckBoxMenuItem CytoplasmAnnulusCheckBox;
	private JRadioButtonMenuItem[] TheImageScalings;
	private boolean SubtractBackground;
	private float[] ScalingRatios = { 1.0f, 0.75f, 0.5f, 0.25f, 0.1f };
	private double[] MaxValues_ImageDisplay;
	private double[] MinValues_ImageDisplay;
	private File ImageDirectory;
	private File TheDirectory;
	private final String URLIntro = "file:///";
	private final String DefaultHelpURLString = URLIntro
	+ System.getProperty("user.dir") + "/doc/Intro.html";
	private int TheColorMap;
	private FilterManager TheFilterManager;
	private ArrayList TheFilters;
	private JSplitPane TheMainPanel;
	private JToolBar TheToolBar;
	private JTabbedPane TheInputPanel_Container;
	private LinePlot TheLinePlot;
	private DotPlot TheDotPlot;
	private HistogramPlot TheHistogram;
	private int LeftPanelDisplayed;
	private boolean Processing;
	private boolean StopProcessing;
	private Gui_PlateRepository ThePlatePanel;
	private File TheProjectDirectory;
	private ImageRail_SDCube TheImageRail_H5IO;
	private boolean areDataSetsModified;
	private DotFilterQueue TheFilterQueue;
	private ExpDesign_Model TheExpDesign_Model;

	/**
	 * The ImageRail GUI Constructor
	 * 
	 * @author BLM
	 */
	public MainGUI() {
		super("ImageRail");
		setResizable(true);
		int height = 700;
		int width = 1300;
		setSize(width, height);

		TheMainGUI = this;
		TheFeatures = new ArrayList<Feature>();
		TheFilters = new ArrayList();
		TheColorMap = tools.ColorMaps.FIRE;
		SubtractBackground = false;
		Processing = false;
		StopProcessing = false;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
				- height / 2);

		TheSelectedFeature_Index = 0;
		Container pane = getContentPane();

		// Create a split pane with the two scroll panes in it.
		TheMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		TheMainPanel.setPreferredSize(new Dimension(width, height));
		TheMainPanel.setOneTouchExpandable(true);
		TheMainPanel.setDividerLocation((int) (width / 2f));

		pane.setLayout(new BorderLayout());
		pane.add(TheMainPanel, BorderLayout.CENTER);

		// Initialize with single 96-well plate
		Model_Plate[] plates = new Model_Plate[1];
		plates[0] = new Model_Plate(8, 12, 0, true);
		plates[0].initGUI();
		ThePlatePanel = new Gui_PlateRepository(new Model_PlateRepository(
				plates));
		int numplates = plates.length;
		TheInputPanel_Container = new JTabbedPane();
		for (int i = 0; i < numplates; i++)
			TheInputPanel_Container.addTab("Plate #" + (i + 1),
					new MidasInputPanel(plates[0], TheExpDesign_Model));

		TheMainPanel.setLeftComponent(TheInputPanel_Container);
		TheMainPanel.setRightComponent(ThePlatePanel);
		TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
		TheMainPanel.validate();
		TheMainPanel.repaint();
		TheMainGUI.repaint();

		TheImageScalings = new JRadioButtonMenuItem[5];
		TheImageScalings[0] = new JRadioButtonMenuItem("100%");
		TheImageScalings[1] = new JRadioButtonMenuItem("75%");
		TheImageScalings[2] = new JRadioButtonMenuItem("50%");
		TheImageScalings[3] = new JRadioButtonMenuItem("25%");
		TheImageScalings[4] = new JRadioButtonMenuItem("10%");
		ButtonGroup g = new ButtonGroup();
		TheImageScalings[2].setSelected(true);
		for (int i = 0; i < TheImageScalings.length; i++)
			g.add(TheImageScalings[i]);

		// TopXPixelsCheckBox = new
		// JCheckBoxMenuItem("Use Top X Brightest Pixels");
		// TopXPixelsCheckBox.setSelected(false);

		//
		// Setting up the Menubar
		//
		JMenuBar TheMenuBar = new JMenuBar();
		JMenu FileMenu = new JMenu("File");
		JMenu OptionsMenu = new JMenu("Options");
		JMenu ToolsMenu = new JMenu("Tools");
		JMenu ProcessMenu = new JMenu("Process");
		JMenu DisplayMenu = new JMenu("Display");
		JMenu HelpMenu = new JMenu("Help");
		JMenu AnalysisModulesMenu = new JMenu("Analysis Plugins...");

		TheMenuBar.add(FileMenu);
		TheMenuBar.add(ProcessMenu);
		TheMenuBar.add(OptionsMenu);
		TheMenuBar.add(ToolsMenu);
		TheMenuBar.add(DisplayMenu);
		TheMenuBar.add(HelpMenu);
		setJMenuBar(TheMenuBar);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {

				shutDownAndExit();
			}
		});

		JMenuItem item = new JMenuItem("New Project");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				new PlateInputDialog(null);
			}
		});
		FileMenu.add(item);

		item = new JMenuItem("Load Project");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getGUI().shutDown();
				getGUI().setVisible(false);
				TheStartupDialog.setVisible(true);
			}
		});
		FileMenu.add(item);
		FileMenu.addSeparator();


		JMenu menuI = new JMenu("Save as MIDAS..");
		FileMenu.add(menuI);

		item = new JMenuItem("Well Means");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SaveFeatures_Dialog s = new SaveFeatures_Dialog(
						new DataSaver_WellMeans_Midas_wMetaData());
			}
		});
		menuI.add(item);

		// item = new JMenuItem("Model_Well Means (w/MetaData)");
		// item.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// SaveFeatures_Dialog s = new SaveFeatures_Dialog(new
		// DataSaver_WellMeans_Midas_wMetaData());
		// }
		// });
		// menuI.add(item);
		// item.setEnabled(true);
		// menuI.addSeparator();

		item = new JMenuItem("Cells");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SaveFeatures_Dialog s = new SaveFeatures_Dialog(
						new DataSaver_Cells_Midas_wMetaData());
			}
		});
		menuI.add(item);

		// item = new JMenuItem("Cells (w/MetaData)");
		// item.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// SaveFeatures_Dialog s = new SaveFeatures_Dialog(new
		// DataSaver_Cells_Midas_wMetaData());
		// }
		// });
		// menuI.add(item);
		// item.setEnabled(false);

		item = new JMenuItem("Save as CSV..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SaveFeatures_Dialog s = new SaveFeatures_Dialog(
						new DataSaver_CSV());
			}
		});
		FileMenu.add(item);

		FileMenu.addSeparator();

		item = new JMenuItem("Close");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				shutDownAndExit();
			}
		});
		FileMenu.add(item);

		item = new JMenuItem("Display Images");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				displayImages_SelectedWells();
			}
		});
		DisplayMenu.add(item);
		DisplayMenu.addSeparator();

		// Displays the Filter manager dialog so you can filter the cells
		// appropriately
//		item = new JMenuItem("Show Filter Manager");
//		item.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				TheFilterManager = new FilterManager();
//				TheFilterManager.setVisible(true);
//			}
//		});
//		ToolsMenu.add(item);

		item = new JMenuItem("Z-Score Filter");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				ArrayList<Model_Well> wells = ThePlatePanel.getModel().getSelectedWells_horizOrder();
				int len = wells.size();
				Model_Well[] ws = new Model_Well[len];
				for (int i = 0; i < len; i++)
					ws[i] = wells.get(i);

				ZScoreFilterDialog dia = new ZScoreFilterDialog();
				String operator = dia.Operater;
				float zScore_threshold = dia.zScore_threshold;
				String zScore_channel = dia.zScore_channel;

				len = wells.size();
				for (int i = 0; i < len; i++) {
					Model_Well well = wells.get(i);
					int numH = well.getHDFcount();
					if (numH > 0) {
						well.loadCells(TheImageRail_H5IO, true, true);
						Model_Field[] fields = well.getFields();
						int num = fields.length;
						boolean foundOne = false;
						for (int f = 0; f < num; f++) {
							if (FeatureDetector
									.containsCellsWithZScoreCriteria(fields[f],
											zScore_channel, zScore_threshold,
											operator)) {
								foundOne = true;
							}
						}
						if (!foundOne) {
							well.setSelected(false);
							well.clearCellData();
						} else
							well.setSelected(true);

					} else
						well.setSelected(false);
				}
				ThePlatePanel.updatePanel();
				// Opening an image viewer showing the resulting wells
				displayImages_SelectedWells();

			}
		});
		ToolsMenu.add(item);
		
		
		/**
		 * Adding the Possible options for the leftPanelDisplay
		 * 
		 * @author BLM
		 */
		JRadioButtonMenuItem[] LeftPanelOptions = new JRadioButtonMenuItem[4];
		ButtonGroup bg = new ButtonGroup();
		LeftPanelOptions[0] = new JRadioButtonMenuItem("MetaData input");
		LeftPanelOptions[1] = new JRadioButtonMenuItem("Line Plot");
		LeftPanelOptions[2] = new JRadioButtonMenuItem("Dot Plot");
		LeftPanelOptions[3] = new JRadioButtonMenuItem("Histogram");

		bg.add(LeftPanelOptions[0]);
		bg.add(LeftPanelOptions[1]);
		bg.add(LeftPanelOptions[2]);
		bg.add(LeftPanelOptions[3]);
		LeftPanelOptions[0].setSelected(true);

		//
		// Adding the toolbar at top of main GUI
		//
		TheToolBar = new JToolBar();
		TheToolBar.setSize(30, 30);
		getContentPane().add(TheToolBar, BorderLayout.NORTH);

		JButton but = new JButton(new ImageIcon("icons/midas.png"));
		TheToolBar.add(but);
		but.setToolTipText("Midas Meta-data Input");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LeftPanelDisplayed = MIDASINPUT;
				TheMainPanel.setLeftComponent(TheInputPanel_Container);
				TheMainPanel.setDividerLocation(TheMainPanel
						.getDividerLocation());
				TheMainPanel.validate();
				TheMainPanel.repaint();

				ThePlatePanel.setDisplayRowLegends(false);

				TheMainGUI.repaint();
				validate();
				repaint();
			}
		});

		but = new JButton(new ImageIcon("icons/linePlot.png"));
		TheToolBar.add(but);
		but.setToolTipText("Line Plot");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Trying to create a metaDataconnector to pass metadata to the
				// linegraph
				// so we can have axis labeling and the such
				// String projPath =
				// main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath();
				// MetaDataConnector meta = null;
				// try
				// {
				// meta = new MetaDataConnector(projPath);
				// }
				// catch(Exception e)
				// {
				// System.out.println("------* Error creating MetaData XML writer *------");
				// }

				LeftPanelDisplayed = LINEGRAPH;
				LinePlot lp = new LinePlot();
				if (TheLinePlot != null)
					lp.copySettings(TheLinePlot);
				TheLinePlot = lp;

				TheMainPanel.setLeftComponent(TheLinePlot);
				TheMainPanel.setRightComponent(ThePlatePanel);
				TheMainPanel.setDividerLocation(TheMainPanel
						.getDividerLocation());

				TheMainPanel.validate();
				TheMainPanel.repaint();
				ThePlatePanel.setDisplayRowLegends(true);

				ThePlatePanel.updatePanel();
				TheLinePlot.updatePanel();
				TheMainGUI.updateAllPlots();

				validate();
				repaint();
			}
		});

		but = new JButton(new ImageIcon("icons/dotPlot.png"));
		TheToolBar.add(but);
		but.setToolTipText("Dot Plot");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LeftPanelDisplayed = DOTPLOT;
				updateDotPlot();
				validate();
				repaint();
			}
		});

		but = new JButton(new ImageIcon("icons/histogram.png"));
		TheToolBar.add(but);
		but.setToolTipText("Histogram Plot");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LeftPanelDisplayed = HISTOGRAM;
				updateHistogramPlot();

				validate();
				repaint();
			}
		});

		LoadCellsImmediatelyCheckBox = new JCheckBoxMenuItem(
		"Load cells into RAM after segmentation");
		LoadCellsImmediatelyCheckBox.setSelected(false);

		MultithreadCheckBox = new JCheckBoxMenuItem(
				"Multi-thread (splits work evenly):");
		MultithreadCheckBox.setSelected(false);
		// StoreNeighborsCheckBox = new JCheckBoxMenuItem("Store Neighbors");
		// StoreNeighborsCheckBox.setSelected(false);

		// StorePixelInformationCheckBox = new
		// JCheckBoxMenuItem("Store Detailed Pixel Coords");
		// StorePixelInformationCheckBox.setSelected(false);

		WellMeanOrIntegratedIntensityCheckBox = new JCheckBoxMenuItem(
		"Mean (Checked) vs Integrated Intensity");
		WellMeanOrIntegratedIntensityCheckBox.setSelected(true);

		// StoreMembranesCheckBox = new
		// JCheckBoxMenuItem("Store Membrane Region");
		// StoreMembranesCheckBox.setSelected(false);

		// FindNeighborsCheckBox = new JCheckBoxMenuItem("Store Neighbors");
		// FindNeighborsCheckBox.setSelected(false);

		DisplayNumberLoadedImagesCheckBox = new JCheckBoxMenuItem(
		"Show Number of Loaded Images");
		DisplayNumberLoadedImagesCheckBox.setSelected(true);
		DisplayNumberLoadedImagesCheckBox
		.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ThePlatePanel.updatePanel();
			}
		});
		DisplayMenu.add(DisplayNumberLoadedImagesCheckBox);

		DisplayAvailableHDFfiles = new JCheckBoxMenuItem(
				"Show available HDF files");
		DisplayAvailableHDFfiles.setSelected(true);
		DisplayAvailableHDFfiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ThePlatePanel.updatePanel();
			}
		});
		DisplayMenu.add(DisplayAvailableHDFfiles);

		//
		// Options Menu

		JMenu loadMenu = new JMenu("Load Cells");
		OptionsMenu.add(loadMenu);

		item = new JMenuItem("Data");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Model_Plate[] plates = getThePlateHoldingPanel().getPlates();
				Model_Plate.loadCellData(plates, false, true);
			}
		});
		loadMenu.add(item);

		item = new JMenuItem("Data & Coords");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Model_Plate[] plates = getThePlateHoldingPanel().getPlates();
				Model_Plate.loadCellData(plates, true, true);
			}
		});
		loadMenu.add(item);

		SelectAllCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		SelectAllCheckBox.setSelected(false);
		SelectAllCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (SelectAllCheckBox.isSelected())
					ThePlatePanel.selectAllWells(true);
				else
					ThePlatePanel.selectAllWells(false);
			}
		});
		OptionsMenu.add(SelectAllCheckBox);

		item = new JMenuItem("Clear memory");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Model_Plate[] plates = getThePlateHoldingPanel().getPlates();
				for (int i = 0; i < plates.length; i++)
					plates[i].clearCellData();
				updateAllPlots();
				System.gc();
			}
		});
		OptionsMenu.add(item);
		OptionsMenu.addSeparator();

		JMenu colorMapsMenu = new JMenu("ColorMaps");
		final String[] names = tools.ColorMaps.colorMapNames;
		int num = names.length;
		ButtonGroup bg2 = new ButtonGroup();
		for (int i = 0; i < num; i++) {
			item = new JCheckBoxMenuItem("" + names[i]);
			final String name = names[i];
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					TheColorMap = tools.ColorMaps.getColorMapIndex(name);
					if (TheDotPlot != null) {
						TheDotPlot.UpdatePlotImage = true;
						TheDotPlot.repaint();
					}
				}
			});
			colorMapsMenu.add(item);
			bg2.add(item);
			if (i == TheMainGUI.TheColorMap)
				item.setSelected(true);
		}
		OptionsMenu.add(colorMapsMenu);

		//
		// Processing menu
		//
		item = new JMenuItem("Well Means");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				// Finding which wells were selected
				ArrayList<Model_Well> arr = ThePlatePanel.getModel()
						.getSelectedWells_horizOrder();
				int num = arr.size();

				Model_Well[] wells = new Model_Well[num];
				for (int n = 0; n < num; n++)
					wells[n] = (Model_Well) arr.get(n);

				ThresholdingBoundsInputDialog_WellMeans d = new ThresholdingBoundsInputDialog_WellMeans(
						wells);
			}

		});

		ProcessMenu.add(item);

		JMenu cellMenu = new JMenu("Single Cells");
		ProcessMenu.add(cellMenu);

		item = new JMenuItem("Classic");
		// item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.META_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				// Finding which wells were selected
				ArrayList<Model_Well> arr = ThePlatePanel.getModel()
						.getSelectedWells_horizOrder();
				int num = arr.size();

				Model_Well[] wells = new Model_Well[num];
				for (int n = 0; n < num; n++)
					wells[n] = (Model_Well) arr.get(n);

				ThresholdingBoundsInputDialog_SingleCells s = new ThresholdingBoundsInputDialog_SingleCells(
						wells);
			}

		});
		cellMenu.add(item);

		item = new JMenuItem("Osteoclasts");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				// Finding which wells were selected
				ArrayList<Model_Well> arr = ThePlatePanel.getModel()
						.getSelectedWells_horizOrder();
				int num = arr.size();

				Model_Well[] wells = new Model_Well[num];
				for (int n = 0; n < num; n++)
					wells[n] = (Model_Well) arr.get(n);

				ThresholdingBoundsInputDialog_SingleCells_Osteo s = new ThresholdingBoundsInputDialog_SingleCells_Osteo(
						wells);

			}

		});
		cellMenu.add(item);

		// item = new JMenuItem("Nuclei");
		// item.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent ae) {
		//
		// // Finding which wells were selected
		// ArrayList<Model_Well> arr = ThePlatePanel.getModel()
		// .getSelectedWells_horizOrder();
		// int num = arr.size();
		//
		// Model_Well[] wells = new Model_Well[num];
		// for (int n = 0; n < num; n++)
		// wells[n] = (Model_Well) arr.get(n);
		//
		// ThresholdingBoundsInputDialog_SingleCells_Osteo s = new
		// ThresholdingBoundsInputDialog_SingleCells_Osteo(
		// wells);
		// }
		//
		// });
		// cellMenu.add(item);

		ProcessMenu.addSeparator();
		item = new JMenuItem("Stop");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (Processing = true)
					StopProcessing = true;
				else
					StopProcessing = false;
			}

		});
		ProcessMenu.add(item);

		item = new JMenuItem("About ImageRail...");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					String text = "ImageRail (Version 1.2.3)\n"
							+ "Software for high-throughput microscopy image analysis \n\n"
							+ "Copyright (c) 2011 Bjorn Millard <bjornmillard@gmail.com> \n\n"
							+ "This program is free software: you can redistribute it and/or \n"
							+ "modify it under the terms of the GNU General Public License as \n"
							+ "published by the Free Software Foundation, either version 3 of \n"
							+ "the License, or (at your option) any later version.\n\n"
							+ "This program is distributed in the hope that it will be useful,\n"
							+ "but WITHOUT ANY WARRANTY; without even the implied warranty of \n"
							+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the \n"
							+ "GNU General Public License for more details. \n\n"
							+ "You should have received a copy of the GNU General Public License \n"
							+ "along with this program.  If not, see <http://www.gnu.org/licenses/>\n";

					JOptionPane.showMessageDialog(null, text, "About",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		HelpMenu.add(item);

		item = new JMenuItem("Help Browser");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					URL url = new URL(DefaultHelpURLString);
					HTMLViewer v = new HTMLViewer(url);
					v.setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			}
		});
		// HelpMenu.add(item);

		updateFeatures();
		ThePlatePanel.updatePanel();
		ThePlatePanel.repaint();
		TheMainPanel.repaint();
		TheMainGUI.repaint();
		validate();
		repaint();
		setVisible(TheMainGUI.isVisible());
	}

	/**
	 * Returns the singleton instance of The GUI
	 * 
	 * @author BLM
	 */
	public static MainGUI getGUI() {
		return TheMainGUI;
	}

	/**
	 * Returns the ExpDesignConnector so we can manage the XML data
	 * 
	 * @author Bjorn Millard
	 * @return ExpDesign_IO
	 */
	public ExpDesign_Model getExpDesignConnector() {
		return TheExpDesign_Model;
	}


	public ImageRail_SDCube getH5IO() {
		return TheImageRail_H5IO;
	}

	/**
	 * Constructs a new ImageRail_IO HDF project connector with the currently
	 * loaded project directory
	 * 
	 * @author BLM
	 */
	public ImageRail_SDCube initH5IO() {
		String projPath = gui.MainGUI.getGUI().getProjectDirectory()
		.getAbsolutePath();
		TheImageRail_H5IO = null;
		try {
			TheImageRail_H5IO = new ImageRail_SDCube(projPath);
		} catch (Exception e) {
			System.out.println("Error creating the ImageRail_SDCube: "); e.printStackTrace();
		}

		return TheImageRail_H5IO;
	}

	

	/**
	 * Returns the HDF project connector with the currently loaded project
	 * directory
	 * 
	 * @author BLM
	 */
	public ImageRail_SDCube getImageRailio() {
		return TheImageRail_H5IO;
	}

	/**
	 * Adding all the features you desire to measure
	 * */
	public void initFeatures(String[] channelNames) {

		ArrayList<Feature> arr = new ArrayList<Feature>();
		try {
            // Try to load features from src tree, otherwise try deployed location 
			File f = new File("./src/features");
			if (!f.exists())
				f = new File("./features");
			File[] fs = f.listFiles();

			int len = fs.length;

			for (int i = 0; i < len; i++) {
				if (fs[i].getAbsolutePath().indexOf(".java") > 0
						&& !fs[i].getName().equalsIgnoreCase("Feature.java")
						&& !fs[i].getName().equalsIgnoreCase(
						"FeatureSorter.java")) {
					String path = fs[i].getName();
					int ind = path.indexOf(".java");
					path = path.substring(0, ind);
					// System.out.println("Loading Feature: "+ path);
					Class c = Class.forName("features." + path);
					arr.add((Feature) c.newInstance());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int len = arr.size();
		TheFeatures = new ArrayList<Feature>();
		// System.out.println("Found "+len +" Features");
		for (int i = 0; i < len; i++) {
			Feature f = (arr.get(i));
			f.Name = f.getClass().toString();

			if (f.isMultiSpectralFeature() && channelNames != null) {
				for (int w = 0; w < channelNames.length; w++) {
					try {
						Feature fn = f.getClass().newInstance();
						fn.setChannelIndex(w);
						fn.setChannelName(channelNames[w]);
						fn.setName(channelNames[w]);
						TheFeatures.add(fn);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else
				TheFeatures.add(f);
		}

		FeatureSorter sorter = new FeatureSorter();
		Collections.sort(TheFeatures, sorter);

	}

	/**
	 * When creating a new project, this inits the project directory and the
	 * plates
	 */
	public boolean initNewPlates(int numPlates, int numRows, int numCols) {
		File dir = getTheDirectory();
		JFileChooser fc = null;
		if (dir != null)
			fc = new JFileChooser(dir);
		else
			fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			setTheDirectory(new File(file.getParent()));
			File newF = new File(file.getAbsolutePath() + ".sdc");
			//
			newF.mkdir();
			setProjectDirectory(newF);

			Model_Plate[] plates = new Model_Plate[numPlates];
			for (int p = 0; p < plates.length; p++) {
				plates[p] = new Model_Plate(numRows, numCols, p, true);
				plates[p].setTitle("Plate #" + p);
				plates[p].initGUI();
			}

			ThePlatePanel = new Gui_PlateRepository(new Model_PlateRepository(
					plates));


			// Reinit the ExpDesignModel
				TheExpDesign_Model = new ExpDesign_Model(TheProjectDirectory
						.getAbsolutePath());

			TheInputPanel_Container = new JTabbedPane();
			for (int i = 0; i < numPlates; i++)
				TheInputPanel_Container.addTab("Plate #" + (i + 1),
						new MidasInputPanel(plates[i], TheExpDesign_Model));

			initH5IO();
			try {
				TheImageRail_H5IO.createProject();
			} catch (H5IO_Exception e) {
				e.printStackTrace();
			}

			//Attempt To init the hashtable
			TheImageRail_H5IO.initHashtable();
			TheImageRail_H5IO.writePlateCountAndSizes(numPlates, plates[0]
					.getNumColumns()
					* plates[0].getNumRows());

			// Writing plate Names
			for (int p = 0; p < plates.length; p++)
				TheImageRail_H5IO.writePlateCountAndSizes(numPlates,
						plates[0].getNumColumns() * plates[0].getNumRows());

			TheMainPanel.setLeftComponent(TheInputPanel_Container);
			TheMainPanel.setRightComponent(ThePlatePanel);
			TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
			TheMainPanel.validate();
			TheMainPanel.repaint();
			TheMainGUI.repaint();
			return true;
		}

		return false;
	}


	/**
	 * Returns the dot filter queue manager for this project
	 * 
	 * @author BLM
	 */
	public DotFilterQueue getFilterQueue() {
		if (TheFilterQueue == null)
			TheFilterQueue = new DotFilterQueue();
		return TheFilterQueue;
	}

	/**
	 * Prompts the user to select directory where new project will be saved
	 * 
	 * @author BLM
	 */
	public void saveNewProject() {
		File dir = getTheDirectory();
		JFileChooser fc = null;
		if (dir != null)
			fc = new JFileChooser(dir);
		else
			fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			TheDirectory = new File(file.getParent());
			File newF = new File(file.getAbsolutePath() + ".sdc");
			TheProjectDirectory.renameTo(newF);
			TheProjectDirectory = newF;

			Model_Plate[] plates = ThePlatePanel.getModel().getPlates();
			ThePlatePanel = new Gui_PlateRepository(new Model_PlateRepository(
					plates));
			int numplates = plates.length;
			TheInputPanel_Container = new JTabbedPane();
			for (int i = 0; i < numplates; i++)
				TheInputPanel_Container.addTab("Plate #" + (i + 1),
						new MidasInputPanel(plates[i], TheExpDesign_Model));

//			initH5IOAndPlates(plates);
			TheMainPanel.setLeftComponent(TheInputPanel_Container);
			TheMainPanel.setRightComponent(ThePlatePanel);
			TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
			TheMainPanel.validate();
			TheMainPanel.repaint();
			TheMainGUI.repaint();

		} else
			System.out.println("Open command cancelled by user.");
	}

	/** 
	 * Displays all the images from the selected wells from all plates in the project.
	 * @author BLM
	 * */
	public void displayImages_SelectedWells()
	{
		// Horiz ordering
		ArrayList<Model_Well> wells = new ArrayList<Model_Well>();
		Model_Plate[] plates = getThePlateHoldingPanel().getPlates();
		int numPlates = plates.length;
		for (int p = 0; p < numPlates; p++)
			wells.addAll(plates[p].getSelectedWells_horizOrder());

		int len = wells.size();
		if (len == 0)
			return;
		ArrayList<FieldViewer> temp = new ArrayList<FieldViewer>();

		FieldViewer_Frame imageViewerFrame = new FieldViewer_Frame();
		for (int i = 0; i < len; i++) {
			Model_Well w = (Model_Well) wells.get(i);
			if (w.getFields() != null) {
				int numFields = w.getFields().length;
				for (int j = 0; j < numFields; j++) {
					if (w.getFields()[j].getNumberOfChannels() > 0) {
						FieldViewer d = new FieldViewer(
								imageViewerFrame, w, w.getFields()[j]);
						temp.add(d);
						if (d != null) {
							if (TheDotPlot != null
									&& TheDotPlot.TheDotSelectionListener != null) {
								d
								.setDotSelectionListener(TheDotPlot.TheDotSelectionListener);
								TheDotPlot.TheDotSelectionListener
								.addListener(d);
							}
						}
					}
				}
			}
		}
		if (temp.size() == 0)
			return;
		int num = temp.size();
		FieldViewer[] arr_horiz = new FieldViewer[num];
		for (int i = 0; i < num; i++)
			arr_horiz[i] = (FieldViewer) temp.get(i);

		imageViewerFrame.setImageViewers(arr_horiz);
		imageViewerFrame.setVisible(true);

		// Trying to block the plate so user doesnt change it until done
		// veiwing images
		TheMainGUI.ThePlatePanel.block(imageViewerFrame);	
	
	}
	
	/**
	 * Prompts the user to select directory where new project will be created
	 * 
	 * @author BLM
	 */
	public void createNewProject() {
		File dir = getTheDirectory();
		JFileChooser fc = null;
		if (dir != null)
			fc = new JFileChooser(dir);
		else
			fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			file.renameTo(new File(file.getAbsolutePath() + ".sdc"));
			TheDirectory = new File(file.getParent());
			file.mkdir();
			TheProjectDirectory = file;

			Model_Plate[] plates = new Model_Plate[1];
			plates[0] = new Model_Plate(8, 12, 0, true);
			plates[0].initGUI();
			ThePlatePanel = new Gui_PlateRepository(new Model_PlateRepository(
					plates));
			int numplates = plates.length;

			TheInputPanel_Container = new JTabbedPane();
			for (int i = 0; i < numplates; i++)
				TheInputPanel_Container.addTab("Plate #" + (i + 1),
						new MidasInputPanel(plates[i], TheExpDesign_Model));
			try {
				initH5IO();
				if(TheImageRail_H5IO!=null)
					TheImageRail_H5IO.createProject();
			} catch (Exception e) {e.printStackTrace();}
				
				
			TheMainPanel.setLeftComponent(TheInputPanel_Container);
			TheMainPanel.setRightComponent(ThePlatePanel);
			TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
			TheMainPanel.validate();
			TheMainPanel.repaint();
			TheMainGUI.repaint();

		} else
			System.out.println("Open command cancelled by user.");
	}

	/**
	 * Updating all applicable plots
	 * 
	 * @author BLM
	 * */
	public void updateAllPlots() {
		if (LeftPanelDisplayed == MIDASINPUT)
			updateMidasInputPanel();
		 if (LeftPanelDisplayed == LINEGRAPH)
			updateLineGraph();
		else if (LeftPanelDisplayed == DOTPLOT)
			updateDotPlot();
		else if (LeftPanelDisplayed == HISTOGRAM)
			updateHistogramPlot();

		ThePlatePanel.updatePanel();
	}

	/**
	 * Returns the current project directory
	 * 
	 * @author BLM
	 */
	public File getProjectDirectory() {

		return TheProjectDirectory;
	}

	/**
	 * Sets the current project directory
	 * 
	 * @author BLM
	 */
	public void setProjectDirectory(File dir) {
		setTitle("Project: " + dir.getAbsolutePath());
		TheProjectDirectory = dir;

		TheExpDesign_Model = new ExpDesign_Model(TheProjectDirectory
				.getAbsolutePath());
	}

	/**
	 * Update the midas input panel with current data
	 * 
	 * @author BLM
	 */
	public void updateMidasInputPanel() {
		Model_Plate[] ThePlates = TheMainGUI.getThePlateHoldingPanel()
				.getPlates();
		int numplates = ThePlates.length;
		for (int i = 0; i < numplates; i++)
			((MidasInputPanel) TheInputPanel_Container.getComponentAt(i))
					.updateInputPanel();

		TheInputPanel_Container.setSelectedIndex((ThePlatePanel
				.getSelectedPlateID()));

		TheInputPanel_Container.validate();
		TheInputPanel_Container.repaint();
	}

	/**
	 * Updates the Line Plot
	 * 
	 * @author BLM
	 */
	public void updateLineGraph() {
		if (TheLinePlot == null)
			return;

		Model_Well[][] wells = null;
		if (TheLinePlot.getPlotType() == LinePlot.ROWS) // Rows
			wells = ThePlatePanel.getModel().getAllSelectedWells_RowSeries();
		else if (TheLinePlot.getPlotType() == LinePlot.COLS)
			wells = ThePlatePanel.getModel().getAllSelectedWells_ColumnSeries();
		else if (TheLinePlot.getPlotType() == LinePlot.MULTIPLATE)
			wells = ThePlatePanel.getModel()
					.getAllSelectedWells_TransPlateSeries();
		;

		if (wells == null)
			return;

		int numSeries = wells.length;
		if (TheLinePlot != null) {
			if (TheLinePlot.Display_NumCells) {
				float[][] data = new float[numSeries][];
				int counter = 0;
				for (int i = 0; i < numSeries; i++) {
					Model_Well[] oneSeries = wells[i];
					int numC = oneSeries.length;
					data[i] = new float[numC];
					for (int c = 0; c < numC; c++) {
						ArrayList<Cell> cells = oneSeries[c].getCells();
						if (cells != null)
							data[i][c] = cells.size();
						else
							data[i][c] = 0;
						counter++;
					}
				}

				Color[] colors = new Color[data.length];
				if (TheLinePlot.getPlotType() == LinePlot.ROWS)
					for (int i = 0; i < numSeries; i++)
						colors[i] = Model_Plate.getRowColor(wells[i][0].name);
				else if (TheLinePlot.getPlotType() == LinePlot.COLS)
					for (int i = 0; i < numSeries; i++)
						colors[i] = Model_Plate.getColColor(wells[i][0].name);
				else if (TheLinePlot.getPlotType() == LinePlot.MULTIPLATE)
					for (int i = 0; i < numSeries; i++)
						colors[i] = Color.BLACK;

				TheLinePlot.updatePlot(data, null, colors, wells);
			} else {
				// do we display variance?
				float[][] data = new float[numSeries][];
				float[][][] varianceBars = new float[numSeries][][];
				float[][] stdev = new float[numSeries][];

				for (int r = 0; r < numSeries; r++) {
					Model_Well[] oneRowSeries = wells[r];
					int numC = oneRowSeries.length;
					data[r] = new float[numC];
					varianceBars[r] = new float[numC][2];
					stdev[r] = new float[numC];
					for (int c = 0; c < numC; c++) {
						if (oneRowSeries[c].Feature_Means != null) {
							data[r][c] = oneRowSeries[c].Feature_Means[TheSelectedFeature_Index];
							// TODO - see if we need to "getCells"
							if (oneRowSeries[c].getCells() != null
									&& oneRowSeries[c].Feature_Stdev != null) {
								float[] minMax = oneRowSeries[c]
								                              .getMinMaxValue(TheSelectedFeature);
								varianceBars[r][c][0] = minMax[0];
								varianceBars[r][c][1] = minMax[1];

								stdev[r][c] = oneRowSeries[c].Feature_Stdev[TheSelectedFeature_Index];
							}
						} else
							varianceBars[r][c] = null;
					}
				}

				Color[] colors = new Color[data.length];
				if (TheLinePlot.getPlotType() == LinePlot.ROWS)
					for (int r = 0; r < numSeries; r++)
						colors[r] = Model_Plate.getRowColor(wells[r][0].name);
				else if (TheLinePlot.getPlotType() == LinePlot.COLS)
					for (int r = 0; r < numSeries; r++)
						colors[r] = Model_Plate.getColColor(wells[r][0].name);
				else if (TheLinePlot.getPlotType() == LinePlot.MULTIPLATE)
					for (int i = 0; i < numSeries; i++)
						colors[i] = Color.BLACK;

				TheLinePlot.updatePlot(data, stdev, colors, wells);
			}
		}

	}

	/**
	 * Updates the Dot Plot
	 * 
	 * @author BLM
	 */
	public void updateDotPlot() {

		long time = System.currentTimeMillis();

		ArrayList<Model_Well> arr = ThePlatePanel.getModel()
				.getSelectedWells_horizOrder();
		int numWells = arr.size();
		Model_Well[] wells = new Model_Well[numWells];
		for (int i = 0; i < numWells; i++)
			wells[i] = (Model_Well) arr.get(i);

		Feature featureX = null;
		Feature featureY = null;
		if (TheDotPlot != null) {
			featureX = null;
			featureY = null;
			if (TheDotPlot.ComboBoxes != null
					&& TheDotPlot.ComboBoxes[0] != null
					&& TheDotPlot.ComboBoxes[1] != null) {
				featureX = (Feature) TheDotPlot.ComboBoxes[0].getSelectedItem();
				featureY = (Feature) TheDotPlot.ComboBoxes[1].getSelectedItem();
			}
			boolean yLog = TheDotPlot.LogScaleButton_Y.isSelected();
			boolean xLog = TheDotPlot.LogScaleButton_X.isSelected();
			boolean densityP = TheDotPlot.shouldDisplayColorMaps();
			int type = TheDotPlot.PlotType;
			float percentToPlot = TheDotPlot.getPercentToPlot();
			TheDotPlot.kill();
			TheDotPlot = new DotPlot(TheMainGUI, wells, featureX, featureY,
					xLog, yLog, type, densityP, percentToPlot);
		} else {
			TheDotPlot = new DotPlot(TheMainGUI, wells, featureX, featureY,
					true, true, DotPlot.SIDEBYSIDE, true, 1);
		}

		TheMainPanel.setLeftComponent(TheDotPlot);
		TheMainPanel.setRightComponent(ThePlatePanel);
		TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());

		TheMainPanel.validate();
		TheMainPanel.repaint();
		ThePlatePanel.updatePanel();
		TheMainGUI.repaint();

		// System.out
		// .println("Time to Update"
		// + (System.currentTimeMillis() - time));

	}

	/**
	 * Updates the Histogram Plot
	 * 
	 * @author BLM
	 */
	public void updateHistogramPlot() {
		ArrayList<Model_Well> arr = ThePlatePanel.getModel()
				.getSelectedWells_horizOrder();
		int numWells = arr.size();
		Model_Well[] wells = new Model_Well[numWells];
		for (int i = 0; i < numWells; i++)
			wells[i] = (Model_Well) arr.get(i);
		if (wells == null)
			return;

		// finding only those wells that actually have data
		int len = wells.length;
		arr = new ArrayList<Model_Well>();
		for (int i = 0; i < len; i++)
		{
			ArrayList<Cell> cells = wells[i].getCells();
			if (wells != null && cells != null
					&& wells[i].getCells().size() > 0)
				arr.add(wells[i]);
		}
		len = arr.size();
		Model_Well[] wells2 = new Model_Well[len];
		for (int i = 0; i < len; i++)
			wells2[i] = (Model_Well) arr.get(i);

		HistogramPlot newHist = new HistogramPlot(wells, TheSelectedFeature);
		if (TheHistogram != null)
			newHist.copySettings(TheHistogram);
		else
			newHist.initViewingBasis();
		// Killing old one to free up RAM
		if (TheHistogram != null)
			TheHistogram.kill();
		TheHistogram = newHist;
		TheHistogram.updateBins(TheHistogram.constructData(wells2, MainGUI
				.getGUI().getTheSelectedFeature_Index()));

		TheMainPanel.setLeftComponent(TheHistogram);
		TheMainPanel.setRightComponent(ThePlatePanel);
		TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
		TheMainPanel.validate();
		TheMainPanel.repaint();
		ThePlatePanel.updatePanel();
		TheMainGUI.repaint();
	}

	/**
	 * Returns the current line plot
	 * 
	 * @author BLM
	 */
	public LinePlot getLinePlot() {
		return TheLinePlot;
	}

	/**
	 * Returns the current dot plot
	 * 
	 * @author BLM
	 */
	public DotPlot getDotPlot() {
		return TheDotPlot;
	}

	/**
	 * Returns the left display panel
	 * 
	 * @author BLM
	 */
	public int getLeftDisplayPanelType() {
		return LeftPanelDisplayed;
	}

	/**
	 * Returns the plate holding panel
	 * 
	 * @author BLM
	 */
	public Gui_PlateRepository getPlateHoldingPanel() {
		return ThePlatePanel;
	}

	/**
	 * Updates the Features comboboxes
	 * 
	 * @author BLM
	 */
	public void updateFeatures() {
		/** Features comboBox */
		ArrayList<Feature> list = new ArrayList<Feature>();
		int len = TheFeatures.size();

		for (int i = 0; i < len; i++)
			list.add((TheFeatures.get(i)));

		Object[] obX = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		final JComboBox featuresComboBox = new JComboBox(obX);
		featuresComboBox.setToolTipText("Features");

		if (TheSelectedFeature == null && TheFeatures != null
				&& TheFeatures.size() > 0 && TheFeatures.get(0) != null)
			TheSelectedFeature = (Feature) TheFeatures.get(0);

		ThePlatePanel.getTheToolBar().removeAll();
		ThePlatePanel.addToolbarComponents();
		if (obX.length > 0)
			ThePlatePanel.getTheToolBar().add(featuresComboBox);

		ThePlatePanel.updatePanel();
		ThePlatePanel.getTheToolBar().validate();
		ThePlatePanel.getTheToolBar().repaint();

		featuresComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				TheSelectedFeature = (Feature) TheFeatures.get(featuresComboBox
						.getSelectedIndex());
				TheSelectedFeature_Index = featuresComboBox.getSelectedIndex();

				updateAllPlots();
				validate();
				repaint();
			}
		});

	}

	/**
	 * Loads the given parameter XML file into the given plate
	 * 
	 * @author BLM
	 */
	public void loadParameterFile(File file, Model_Plate plate) {
		try {
			ArrayList Wells = new ArrayList();
			DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
			}

			Document document = null;
			document = builder.parse(file);

			// parsing the nodes
			NodeList nl = document.getElementsByTagName("Well");
			Wells = new ArrayList();

			int len = nl.getLength();
			System.out.println("numWells: " + len);

			// loading the nodes
			for (int i = 0; i < len; i++) {
				org.w3c.dom.Node node = nl.item(i);
				if (node.getNodeName().equalsIgnoreCase("Well")) {
					String wellName = null;
					String threshold_nuc_channel = "";
					String threshold_cyto_channel = "";
					float threshold_nucleus = 0;
					float threshold_cell = 0;
					float threshold_bkgd = 0;
					float topXBrightestPixels = -1;
					int annulusSize = -1;
					String meanOrIntegrated = "MEAN";
					String processType = Model_ParameterSet.UNPROCESSED;

					int length = (node.getAttributes() != null) ? node
							.getAttributes().getLength() : 0;
							for (int loopIndex = 0; loopIndex < length; loopIndex++) {
								Attr att = (Attr) node.getAttributes().item(loopIndex);
								if (att.getNodeName().equalsIgnoreCase("name"))
									wellName = att.getNodeValue().trim();
								else if (att.getNodeName()
										.equalsIgnoreCase("Processed"))
									processType = att.getNodeValue().trim();
								else if (att.getNodeName().equalsIgnoreCase(
								"ThresholdChannel_nuc"))
									threshold_nuc_channel = att.getNodeValue().trim();
								else if (att.getNodeName().equalsIgnoreCase(
								"ThresholdChannel_cyto"))
									threshold_cyto_channel = att.getNodeValue().trim();
								else if (att.getNodeName().equalsIgnoreCase(
								"Threshold_Nucleus"))
									threshold_nucleus = Float.parseFloat(att
											.getNodeValue().trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"Threshold_Cell"))
									threshold_cell = Float.parseFloat(att
											.getNodeValue().trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"Threshold_Background"))
									threshold_bkgd = Float.parseFloat(att
											.getNodeValue().trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"TopXBrightestPixels"))
									topXBrightestPixels = Float.parseFloat(att
											.getNodeValue().trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"AnnulusSize"))
									annulusSize = Integer.parseInt(att.getNodeValue()
											.trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"MeanOrIntegrated"))
									meanOrIntegrated = (att.getNodeValue().trim());
								else if (att.getNodeName().equalsIgnoreCase(
								"StoreCells"))
									LoadCellsImmediatelyCheckBox.setSelected(Boolean
											.parseBoolean(att.getNodeValue().trim()));
								// else if
								// (att.getNodeName().equalsIgnoreCase("StorePixelInfo"))
								// StorePixelInformationCheckBox.setSelected(Boolean.parseBoolean(att.getNodeValue().trim()));
								// else if
								// (att.getNodeName().equalsIgnoreCase("StoreMembraneRegion"))
								// StoreMembranesCheckBox.setSelected(Boolean.parseBoolean(att.getNodeValue().trim()));
							}

							System.out.println("well: " + wellName + "  "
									+ threshold_nucleus + "  " + meanOrIntegrated);

							// TODO
							Model_Well well = plate.getWell(wellName);
							Wells.add(well);
							Model_ParameterSet p = well.TheParameterSet;
							p.setModified(true);
							p.setWellName(wellName);
							p.setProcessType(processType);
							p.setThreshold_Background(threshold_bkgd);
							p.setThreshold_Cytoplasm(threshold_cell);
							p.setThreshold_Nucleus(threshold_nucleus);
							p.setAnnulusSize(annulusSize);
							p.setMeanOrIntegrated(meanOrIntegrated);
							// p.TopXBrightPix = topXBrightestPixels;
							p.setThresholdChannel_nuc_Name(threshold_nuc_channel);
							p.setThresholdChannel_cyto_Name(threshold_cyto_channel);
							// Finding the index of this channel name
							for (int j = 0; j < TheMainGUI.getTheChannelNames().length; j++)
								if (TheMainGUI.getTheChannelNames()[j]
								                                    .equalsIgnoreCase(threshold_nuc_channel))
									p.setThresholdChannel_nuc_Index(j);
							// Finding the index of this channel name
							for (int j = 0; j < TheMainGUI.getTheChannelNames().length; j++)
								if (TheMainGUI.getTheChannelNames()[j]
								                                    .equalsIgnoreCase(threshold_cyto_channel))
									p.setThresholdChannel_cyto_Index(j);

							System.out.println(p);
				}
			}
			//
			// Processing the wells
			//
			len = Wells.size();
			ArrayList ss = new ArrayList();
			ArrayList wm = new ArrayList();
			for (int i = 0; i < len; i++) {
				Model_Well w = (Model_Well) Wells.get(i);
				System.out
				.println("procTYPE: "
						+ w.TheParameterSet.getProcessType());

				if (w.TheParameterSet.getProcessType()
						.equalsIgnoreCase(Model_ParameterSet.SINGLECELL))
					ss.add(w);
				else if (w.TheParameterSet.getProcessType()
						.equalsIgnoreCase(Model_ParameterSet.WELLMEAN))
					wm.add(w);
			}
			// Processing the SingleCell Wells
			if (ss.size() > 0) {
				int num = ss.size();
				Model_Well[] wellsToProcess = new Model_Well[num];
				for (int i = 0; i < num; i++)
					wellsToProcess[i] = (Model_Well) ss.get(i);

				// Starting the Processor
				Processor_SingleCells tasker = new Processor_SingleCells(
						wellsToProcess, new DefaultSegmentor());
				tasker.start();
			}
			// Processing the WellMean Wells
			if (wm.size() > 0) {
				int num = wm.size();
				Model_Well[] wellsToProcess = new Model_Well[num];
				for (int i = 0; i < num; i++)
					wellsToProcess[i] = (Model_Well) wm.get(i);
				// Starting the Processor
				Processor_WellAverage tasker = new Processor_WellAverage(
						wellsToProcess);
				tasker.start();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

	}

	/**
	 * Loads the plate with the TIFF images in the given directory
	 * 
	 * @author BLM
	 * @throws H5IO_Exception 
	 */
	public void loadImageDirectory(File ImageDir_, Model_Plate plate,
			boolean copyImages) throws H5IO_Exception {

		File dir = ImageDir_;

		// Storing the images in the ImageDirectory
		File Images = new File(gui.MainGUI.getGUI().getProjectDirectory()
				+ File.separator + "Images");
		if (!Images.exists())
			Images.mkdir();

		// Option of copying or moving the image directory into the project

		if (copyImages)// copy
		{
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// Copying images to new ImageDirectory
			File newDir = new File(Images.getAbsolutePath() + File.separator
					+ "plate_" + (plate.getID() - 1));
			newDir.mkdir();
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = new File(newDir.getAbsolutePath() + File.separator
						+ files[i].getName());
				try {
					tools.ImageTools.copyFile(files[i], file);
				} catch (IOException e) {
					System.out.println("----*ERROR copying TIFF files*----");
					e.printStackTrace();
				}
			}
			dir = newDir;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else // Move images
		{

			File newFile = new File(Images.getAbsolutePath() + File.separator
					+ "plate_" + (plate.getID()));
			boolean result = dir.renameTo(newFile);
			if (!result) {
				System.out.println("Error moving images into project.");
				return;
			}
			dir = newFile;

			// Trying to write images to HDF5
			// try {
			// TheImageRail_H5IO.dumpAndReadImagesInProject(dir.listFiles());
			// } catch (Exception e1) {
			// e1.printStackTrace();
			// }
		}

		// Waiting a second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("Error Loading Images: ");
			e.printStackTrace();
		}



		for (int r = 0; r < plate.getWells().length; r++)
			for (int c = 0; c < plate.getWells()[0].length; c++) {
				// Getting all files tagged for this well
				File[] allFiles = tools.ImageTools.getFilesForGivenWell(dir,
						plate.getWells()[r][c]);
				// Organizing the images into sets of File[] in a an arraylist
				// where each element of the arrList is a File[] of each
				// wavelength for each field
				ArrayList<File[]> allSets = tools.ImageTools
				.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
				int numFields = allSets.size();

				Model_Well well = plate.getWells()[r][c];
				well.setTheFields(new Model_Field[numFields]);
				for (int i = 0; i < numFields; i++) {
					plate.getWells()[r][c].getFields()[i] = new Model_Field(
							((File[]) allSets.get(i)), i, well);

				}
			}

		// Getting Number of uniquie channel names and adding Features based off
		// of wavelength - TODO features should be added better
		ChannelNames = tools.ImageTools.getNameOfUniqueChannels(dir
				.getParentFile());

		// Init Scaling parameters
		initScalingParameters();
		initFeatures(ChannelNames);

		if (LeftPanelDisplayed == DOTPLOT) {
			TheDotPlot = null;
			updateDotPlot();
		} else if (LeftPanelDisplayed == HISTOGRAM) {
			TheHistogram = null;
			updateHistogramPlot();
		}

		// creating a project directory if doesn't exist
		String projPath = gui.MainGUI.getGUI().getProjectDirectory()
		.getAbsolutePath();
		File f = new File(projPath);
		if (!f.exists())
			f.mkdir();

		// Trying to load the well mean data from the HDF file if exists
		plate.loadWellMeanAndStdevData();

		updateFeatures();
		updateAllPlots();
		ThePlatePanel.updatePanel();
		plate.getGUI().updatePanel();
		validate();
		repaint();

	}

	/**
	 * Init Scaling parameters
	 * @author BLM
	 * */
	public void initScalingParameters() {
		MaxValues_ImageDisplay = new double[getNumberOfChannels()];
		MinValues_ImageDisplay = new double[getNumberOfChannels()];
		for (int j = 0; j < MaxValues_ImageDisplay.length; j++)
		{
			MaxValues_ImageDisplay[j] = (double) MainGUI.MAXPIXELVALUE;
		}
	}

	/**
	 * Loads the plate with the TIFF images in the given directory
	 * 
	 * @author BLM
	 */
	public void loadProject(File ProjectDir) {

		long sTime = System.currentTimeMillis();
		try {
			System.out.println("Loading Project: " + ProjectDir.getName());
			TheDirectory = new File(ProjectDir.getParent());
			setProjectDirectory(ProjectDir);
			//Initialized the HDF5 I/O and creates the sample/well hash index
			initH5IO();
			TheImageRail_H5IO.initHashtable();

			// Looking for what sort of plates were loaded in this prior project
			ArrayList<int[]> plateSizes = TheImageRail_H5IO.getPlateSizes();
			//We will create X plates where X == maxPlateID+1;
			int max = 0;
			int pSize = 96;
			for (int i = 0; i < plateSizes.size(); i++) {
				int[] one =  plateSizes.get(i);
				int id = one[0];
				if(id>max)
					max = id;
				if(i == 0)
				pSize = one[1];
				else
					if(pSize!=one[1])
						System.out.println("Project contains plates of different sizes*** This is currently not supported by ImageRail");
				}
			max++;

			
			ArrayList<Model_Plate> arr = new ArrayList<Model_Plate>();
			for (int i = 0; i < max; i++) {
				int numR = (int) Math.sqrt(pSize / 1.5f);
				int numC = pSize / numR;
				arr.add(new Model_Plate(numR, numC, i, true));
			}


			// Creating the new plate holder with new plates
			Model_Plate[] plates = new Model_Plate[arr.size()];
			for (int p = 0; p < plates.length; p++)
 {
				plates[p] = arr.get(p);
				plates[p].initGUI();
			}
			ThePlatePanel = new Gui_PlateRepository(new Model_PlateRepository(
					plates));

			int numplates = plates.length;
			TheInputPanel_Container = new JTabbedPane();
			for (int i = 0; i < numplates; i++) {
				Model_Plate plate = plates[i];
				TheInputPanel_Container.addTab("Plate #" + plate.getID(),
						new MidasInputPanel(plate, TheExpDesign_Model));
				
				
				// Trying to load the well mean data from the HDF file if exists
				plate.loadWellMeanAndStdevData();

				File dir = new File(TheProjectDirectory.getAbsolutePath()
						+ File.separator + "Images" + File.separator + "plate_"
						+ i);

				// Looking for images for this plate in the projPath/Images
				// directory

				// Getting Number of unique channel names and adding Features
				// based off of wavelength - TODO features should be added
				// better

				String[] names = tools.ImageTools.getNameOfUniqueChannels(dir
						.getParentFile());

				if (names != null && names.length > 0)
					ChannelNames = names;

				if (dir != null && dir.exists()) {
					for (int r = 0; r < plate.getNumRows(); r++)
						for (int c = 0; c < plate.getNumColumns(); c++) {
							// Getting all files tagged for this well
							File[] allFiles = tools.ImageTools
							.getFilesForGivenWell(dir, plate
									.getWells()[r][c]);
							// Organizing the images into sets of File[] in a an
							// arraylist where each element of the arrList is a
							// File[] of each wavelength for each field
							ArrayList<File[]> allSets = tools.ImageTools
							.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
							int numFields = allSets.size();

							Model_Well well = plate.getWells()[r][c];
							well.setTheFields(new Model_Field[numFields]);
							for (int j = 0; j < numFields; j++)
								plate.getWells()[r][c].getFields()[j] = new Model_Field(
										((File[]) allSets.get(j)), j, well);
						}
				}

			}

			initScalingParameters();
			initFeatures(ChannelNames);
			updateFeatures();
			loadFieldROIs();
			updateAllPlots();

			//
			//Checking if loaded project features match up 
			StringBuffer[] fNames = new StringBuffer[TheFeatures.size()];
			for (int j = 0; j < TheFeatures.size(); j++) {
				fNames[j] = new StringBuffer(((Feature)TheFeatures.get(j)).toString());
			}

			// // loading plate names
			// StringBuffer[] plateNames = TheImageRail_H5IO.readPlateNames();
			// for (int i = 0; i < plateNames.length; i++) {
			// System.out.println(plateNames[i]);
			// }
			// if (plateNames != null) {
			// for (int i = 0; i < plates.length; i++)
			// plates[i].setTitle(plateNames[i] + "");
			// }

			TheMainPanel.setLeftComponent(TheInputPanel_Container);
			TheMainPanel.setRightComponent(ThePlatePanel);
			TheMainPanel.setDividerLocation(TheMainPanel.getDividerLocation());
			TheMainPanel.validate();
			ThePlatePanel.updatePanel();
			TheMainPanel.repaint();
			TheMainGUI.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If fields already exist int he data.h5 file, then see if we should load
	 * ROIs
	 * 
	 * @author Bjorn Millard
	 * */
	public void loadFieldROIs() {
		String h5path = TheProjectDirectory.getAbsolutePath() + File.separator
				+ "Data.h5";
		// Iterating through all fields and checking if they have ROIs to load
		Enumeration<String> keys = TheImageRail_H5IO.getHashtable().keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.indexOf("f") >= 0) {
				int plateIndex = Integer.parseInt((key.substring(1, key
						.indexOf("w"))));
				int wellIndex = Integer.parseInt(key.substring(
						key.indexOf("w") + 1, key.indexOf("f")));
				int fieldIndex = Integer.parseInt(key.substring(key
						.indexOf("f") + 1, key.length()));

				Model_PlateRepository rep = ThePlatePanel.getModel();
				Model_Well well = rep.getWell(plateIndex, wellIndex);



				String fieldPath = TheImageRail_H5IO.getHashtable().get(key);
				ArrayList<Polygon> rois = TheImageRail_H5IO.readROIs(h5path,
						fieldPath);
				if (rois != null) {
					int num = rois.size();
					for (int i = 0; i < num; i++) {
						well.getFields()[fieldIndex].setROI(rois.get(i));
					}
				}
			}

		}
	}

	/** */
	public boolean containsFile(File dir, String name) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].getName().indexOf(name) >= 0)
				return true;
		}
		return false;
	}

	/**
	 * Loads the given file directory of images into the given Model_Plate
	 * 
	 * @author BLM
	 */
	public void load(File f, Model_Plate plate) {
		if (f.isDirectory()) {
			// Loading a project
			if (f.getName().indexOf(".sdc") > 0) {
				loadProject(f);
			} else // Only load images
			{
				System.out.println(" 	---> Loading Image Directory");
				ImageDirectory = f;
				TheDirectory = new File(f.getParent());
				
				try {
					loadImageDirectory(f, plate, false);
				} catch (H5IO_Exception e) {
					System.out.println("Error loading project directory: ");
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * For packages that are extensible (ex: features and segmentors), this
	 * method looks at a given package for all .java files present and compiles
	 * un-compiled files into java Class files. It takes in the splash screen
	 * for status updates if desired
	 * 
	 * @author BLM
	 */
	static public void findAndCompileNewJavaFiles(String packageName,
			MainSplash splash) throws ClassNotFoundException {

		File f = new File("./" + packageName);
		File[] fs = f.listFiles();
		int len = fs.length;

		for (int i = 0; i < len; i++) {
			if (fs[i].getAbsolutePath().indexOf(".java") > 0) {
				String name = fs[i].getName();
				int ind = name.indexOf(".java");
				name = name.substring(0, ind);
				name = packageName + "." + name;
				String message = "Loading: " + name;
				if (splash != null)
					splash.setMessage(message);
				// else
				// System.out.println(message);

				String fileStub = name.replace('.', '/');
				// Build objects pointing to the source code (.java) and object
				// code (.class)

				String javaFilename = fileStub + ".java";
				String classFilename = fileStub + ".class";

				File javaFile = new File(javaFilename);
				File classFile = new File(classFilename);

				// System.out.println( "j "+javaFile.lastModified()+" c "+
				// classFile.lastModified() );
				// First, see if we want to try compiling. We do if (a) there
				// is source code, and either (b0) there is no object code,
				// or (b1) there is object code, but it's older than the source
				if (javaFile.exists()
						&& (!classFile.exists() || javaFile.lastModified() > classFile
								.lastModified())) {
					try {
						// Try to compile it. If this doesn't work, then
						// we must declare failure. (It's not good enough to use
						// and already-existing, but out-of-date, classfile)
						if (!compile(javaFilename, splash)
								|| !classFile.exists()) {
							throw new ClassNotFoundException("Compile failed: "
									+ javaFilename);
						}
					} catch (IOException ie) {
						// Another place where we might come to if we fail
						// to compile
						throw new ClassNotFoundException(ie.toString());
					}
				}
			}
		}

	}

	// Spawn a process to compile the java source code file
	// specified in the 'javaFile' parameter. Return a true if
	// the compilation worked, false otherwise.
	static public boolean compile(String javaFile, MainSplash splash)
	throws IOException {
		// Let the user know what's going on
		String message = "Compiling " + javaFile + "...";
		if (splash != null)
			splash.setMessage(message);
		else
			System.out.println(message);
		// get the classpath and make sure to pass it on to the compiler invocation
		String classPath = System.getProperty("java.class.path");
		String compileCommand = "javac -cp " + classPath + " " + javaFile;
		System.out.println("running command: " + compileCommand);
		// Start up the compiler
		Process p = Runtime.getRuntime().exec(compileCommand);
		// Wait for it to finish running
		try {
			p.waitFor();
		} catch (InterruptedException ie) {
			System.out.println(ie);
		}
		// Check the return code, in case of a compilation error
		int ret = p.exitValue();
		if (ret != 0) {
			java.io.BufferedReader errorStream = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream()));
			String line;
			do {
				line = errorStream.readLine();
				System.out.println(line);
			} while (line != null);
		}
		
		// Tell whether the compilation worked
		return ret == 0;
	}

	/**
	 * Initializes the filtermanager. Note, this needs to be init after the
	 * MainGUI is initialized
	 * 
	 * @author BLM
	 * */
	public void initFilterManager() {
		TheFilterManager = new FilterManager();
	}


	/**
	 * Sets whether the GUI is running a processor
	 * 
	 * @author BLM
	 */
	public void setProcessing(boolean boo) {
		Processing = boo;
		StopProcessing = false;
	}

	/**
	 * Returns whether the user wants to stop the current image processing
	 * 
	 * @author BLM
	 * */
	public boolean shouldStop() {
		return !StopProcessing;
	}

	/**
	 * Determines if the GUI is running a processor
	 * 
	 * @author BLM
	 */
	public boolean isProcessing() {
		return Processing;
	}

	/**
	 * Returns all features in an Array format
	 * 
	 * @author BLM
	 */
	public Feature[] getFeatures() {
		int len = TheFeatures.size();
		Feature[] f = new Feature[len];
		for (int i = 0; i < len; i++)
			f[i] = (Feature) TheFeatures.get(i);
		return f;
	}

	/**
	 * Returns the index of the feature with the given name
	 * 
	 * @author BLM
	 */
	public int getFeature_Index(String name) {
		int len = TheFeatures.size();
		for (int i = 0; i < len; i++)
			if (((Feature) TheFeatures.get(i)).getName().equalsIgnoreCase(name))
				return i;
			return -1;
	}

	/**
	 * Returns the number of channels represented in the loaded images
	 * 
	 * @author BLM
	 * */
	public int getNumberOfChannels() {
		if (getTheChannelNames() == null)
			return 0;
		return getTheChannelNames().length;
	}



	/**
	 * Returns the current ColorMap index that is selected
	 * 
	 * @author BLM
	 * */
	public int getTheColorMapIndex() {
		return TheColorMap;
	}

	/**
	 * Returns the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public double[] getMaxValues_ImageDisplay() {
		return MaxValues_ImageDisplay;
	}

	/**
	 * Returns the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public double[] getMinValues_ImageDisplay() {
		return MinValues_ImageDisplay;
	}

	/**
	 * Sets the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public void setMaxValues_ImageDisplay(double[] vals) {
		MaxValues_ImageDisplay = vals;
	}

	/**
	 * Sets the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public void setMinValues_ImageDisplay(double[] vals) {
		MinValues_ImageDisplay = vals;
	}

	/**
	 * Returns the ArrayList of Features that are currently loaded
	 * 
	 * @author BLM
	 * */
	public ArrayList<Feature> getTheFeatures() {
		return TheFeatures;
	}

	/**
	 * Returns the selected Feature
	 * 
	 * @author BLM
	 * */
	public Feature getTheSelectedFeature() {
		return TheSelectedFeature;
	}

	/**
	 * Returns the selected Feature index
	 * 
	 * @author BLM
	 * */
	public int getTheSelectedFeature_Index() {
		return TheSelectedFeature_Index;
	}

	/**
	 * Returns the names of the channels that are loaded
	 * 
	 * @author BLM
	 * */
	public String[] getTheChannelNames() {
		return ChannelNames;
	}

	/**
	 * Returns the number of pixels to include
	 * 
	 * @author BLM
	 * */
	// public float getNumberOfPixelsToInclude()
	// {
	// return NumberOfPixelsToInclude;
	// }

	/**
	 * Sets the number of pixels to include
	 * 
	 * @author BLM
	 * */
	// public void setNumberOfPixelsToInclude(Float val)
	// {
	// NumberOfPixelsToInclude = val;
	// }

	/**
	 * Returns boolean whether to subtract image background or not
	 * 
	 * @author BLM
	 * */
	public boolean getBackgroundSubtract() {
		return SubtractBackground;
	}

	/**
	 * Sets boolean whether to subtract image background or not
	 * 
	 * @author BLM
	 * */
	public void setBackgroundSubtract(boolean boo) {
		SubtractBackground = boo;
	}

	/**
	 * Returns the default file directory to make file choosers more convenient
	 * 
	 * @author BLM
	 * */
	public File getTheDirectory() {
		return TheDirectory;
	}

	/**
	 * Sets the default file directory to make file choosers more convenient
	 * 
	 * @author BLM
	 * */
	public void setTheDirectory(File dir) {
		TheDirectory = dir;
	}

	/**
	 * Returns the directory where images came from
	 * 
	 * @author BLM
	 * */
	public File getImageDirectory() {
		return ImageDirectory;
	}

	/**
	 * Returns the Panel that holds all the plates
	 * 
	 * @author BLM
	 * */
	public Model_PlateRepository getThePlateHoldingPanel() {
		if (ThePlatePanel != null)
			return ThePlatePanel.getModel();
		return null;
	}

	//
	// CheckBox Section
	//

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getTheDisplayNumberLoadedImagesCheckBox() {
		return DisplayNumberLoadedImagesCheckBox;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	// public JCheckBoxMenuItem getFindNeighborsCheckBox()
	// {
	// return FindNeighborsCheckBox;
	// }

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	// public JCheckBoxMenuItem getStorePixelInformationCheckBox()
	// {
	// return StorePixelInformationCheckBox;
	// }

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getWellMeanOrIntegratedIntensityCheckBox() {
		return WellMeanOrIntegratedIntensityCheckBox;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getDisplayNumberLoadedImagesCheckBox() {
		return DisplayNumberLoadedImagesCheckBox;
	}

	/**
	 * Returns whether we should display the mini-HDF file icons in the wells
	 * 
	 * @author BLM
	 * */
	public boolean shouldDisplayHDFicons() {
		return DisplayAvailableHDFfiles.isSelected();
	}

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	public void setDisplayNumberLoadedImagesCheckBox(JCheckBoxMenuItem box) {
		DisplayNumberLoadedImagesCheckBox = box;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	// public JCheckBoxMenuItem getTopXPixelsCheckBox()
	// {
	// return TopXPixelsCheckBox;
	// }

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	// public void setTopXPixelsCheckBox(JCheckBoxMenuItem box)
	// {
	// TopXPixelsCheckBox = box;
	// }

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getStoreCytoAndNuclearWellMeans() {
		return StoreCytoAndNuclearWellMeans;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getWatershedNucleiCheckBox() {
		return WatershedNucleiCheckBox;
	}

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	public void setWatershedNucleiCheckBox(JCheckBoxMenuItem box) {
		WatershedNucleiCheckBox = box;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getLoadCellsImmediatelyCheckBox() {
		return LoadCellsImmediatelyCheckBox;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getMultithreadCheckBox() {
		return MultithreadCheckBox;
	}

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	public void setLoadCellsImmediatelyCheckBox(JCheckBoxMenuItem box) {
		LoadCellsImmediatelyCheckBox = box;
	}

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	// public JCheckBoxMenuItem getStoreNeighborsCheckBox()
	// {
	// return StoreNeighborsCheckBox;
	// }

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	// public void setStoreNeighborsCheckBox(JCheckBoxMenuItem box)
	// {
	// StoreNeighborsCheckBox = box;
	// }

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	// public JCheckBoxMenuItem getStoreMembranesCheckBox()
	// {
	// return StoreMembranesCheckBox;
	// }

	/**
	 * Returns check box
	 * 
	 * @author BLM
	 * */
	public JCheckBoxMenuItem getCytoplasmAnnulusCheckBox() {
		return CytoplasmAnnulusCheckBox;
	}

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	public void setCytoplasmAnnulusCheckBox(JCheckBoxMenuItem box) {
		CytoplasmAnnulusCheckBox = box;
	}

	//
	//
	//

	/**
	 * Sets check box
	 * 
	 * @author BLM
	 * */
	public void setStoreCytoAndNuclearWellMeans(JCheckBoxMenuItem box) {
		StoreCytoAndNuclearWellMeans = box;
	}

	/**
	 * Returns the scaling ratios available
	 * 
	 * @author BLM
	 * */
	public float[] getScalingRatios() {
		return ScalingRatios;

	}

	/**
	 * Returns the scaling ratios available
	 * 
	 * @author BLM
	 * */
	public JRadioButtonMenuItem[] getTheImageScalings() {
		return TheImageScalings;
	}

	/**
	 * If cells have been deleted, this will be triggered... at which point when
	 * the program is closed, it will ask we we want to make these changes
	 * permenant
	 * 
	 * @author BLM
	 */
	public void setCellsModified(boolean cellsModified) {
		areDataSetsModified = cellsModified;
	}

	/**
	 * Default shutdown method. Checks if cells from the datasets have been
	 * removed; if so, asks if we want to resave the current state of the data
	 * set --> removing those cells from the source HDF5 files
	 * 
	 * @author BLM
	 */
	private void shutDown() {
		if (areDataSetsModified) {
			int result = JOptionPane
			.showConfirmDialog(
					(Component) null,
					"\nCells have been Deleted \n\n Would you like to save these changes \n in your project files?\n",
					"alert", JOptionPane.YES_NO_OPTION);
			System.out.println("result: " + result);

			if (result == 0) {
				resaveCells();
			}
		}
	}

	/**
	 * Shuts down and exits completely.
	 * 
	 * @author JLM
	 */
	private void shutDownAndExit() {
		shutDown();
		System.exit(0);
	}
	
	/**
	 * Resaves the current state of the data set by removing those cells from
	 * the source HDF5 files
	 * 
	 * @author BLM
	 */
	private void resaveCells() {
		ImageRail_SDCube io = MainGUI.getGUI().getH5IO();

		Model_Plate[] plates = getPlateHoldingPanel().getModel().getPlates();
		int numP = plates.length;
		System.out.println("*** Saving Changes to Wells:");

		Feature[] features = getFeatures();
		StringBuffer[] featureNames = null;
		if (features != null && features.length > 0) {
			featureNames = new StringBuffer[features.length];
			for (int i = 0; i < features.length; i++)
				featureNames[i] = new StringBuffer(features[i].toString());
		}

		for (int i = 0; i < numP; i++) {
			Model_Well[][] wells = plates[i].getWells();
			for (int r = 0; r < wells.length; r++) {
				for (int c = 0; c < wells[0].length; c++) {
					Model_Well well = wells[r][c];
					if (well.areCellsModified()) {
						System.out.println("Well: " + wells[r][c].name);
						try {
							Model_Field[] fields = wells[r][c].getFields();
							for (int j = 0; j < fields.length; j++)
								fields[j].resaveCells(io);

						} catch (Exception e) {
							System.out
							.println("**Error Writing Cells during resave of HDF5 files");
							e.printStackTrace();
						}
					
							if (well.Feature_Means != null && io != null) {						
								io.writeWellMeans(plates[i].getPlateIndex(), well.getWellIndex(), well.Feature_Means);
							}
							if (well.Feature_Stdev != null && io != null)
								io
										.writeWellStdDevs(plates[i]
								                                  .getPlateIndex(), well.getWellIndex(),
								                                  well.Feature_Stdev);
						
						well.setCellsModified(false);
					}

				}
			}
		}
		setCellsModified(false);
	}
	
	
	/**
	 * Main ImageRail GUI start call
	 * 
	 * @author BLM
	 * 
	 * */
	public static void main(String[] args) {
			if (args.length == 0) {
			try {
				// Throw a nice little title page up on the screen first
				MainSplash splash = new MainSplash(2000);
				// Normally, we'd call splash.showSplash() and get on with the
				// program.
				// But, since this is only a test...
				splash.showSplashAndExit();

				new MainGUI();
				MainGUI gui = MainGUI.getGUI();
				gui.setVisible(false);
				gui.initFilterManager();

				// Loading all new plugin files
				// MainGUI.findAndCompileNewJavaFiles("features", splash);
				// Hiding the splash, now that we have loaded everything

				splash.setVisible(false);
				// init the project with the startupdialog
				TheStartupDialog = new MainStartupDialog();

			} catch (Exception e) {

				e.printStackTrace();
				System.exit(1);
			}
			}


	}
	// /** Gets the current time/date stamp to for unique sample IDs */
	// public String getTimestamp() {
	// Calendar cal = Calendar.getInstance();
	// SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	// return sdf.format(cal.getTime());
	// }

	/** Gets the current time/date stamp to for unique sample IDs */
	public String getTimestamp() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}



}
