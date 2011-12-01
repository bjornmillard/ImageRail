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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
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

import midasGUI.MidasInputPanel;
import models.Model_Field;
import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;
import plots.DotPlot;
import plots.HistogramPlot;
import plots.LinePlot;
import segmentedobject.Cell;
import dataSavers.DataSaver_CSV;
import dataSavers.DataSaver_Cells_Midas_wMetaData;
import dataSavers.DataSaver_WellMeans_Midas_wMetaData;
import dialogs.PlateInputDialog;
import dialogs.SaveFeatures_Dialog;
import dialogs.ThresholdingBoundsInputDialog_BatchRun;
import dialogs.ThresholdingBoundsInputDialog_Nuclei;
import dialogs.ThresholdingBoundsInputDialog_SingleCells;
import dialogs.ThresholdingBoundsInputDialog_SingleCells_Osteo;
import dialogs.ThresholdingBoundsInputDialog_WellMeans;
import dialogs.ZScoreFilterDialog;
import features.Feature;
import filters.FeatureDetector;


public class MainGUI extends JFrame {

	/** The GUI object */
	private static MainGUI TheMainGUI;
	private static Model_Main TheMainModel;
	private static MainStartupDialog TheStartupDialog;
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
	private final JCheckBoxMenuItem SelectAllCheckBox = new JCheckBoxMenuItem(
			"Select All Wells");
	private JCheckBoxMenuItem DisplayNumberLoadedImagesCheckBox;
	private JCheckBoxMenuItem DisplayAvailableHDFfiles;
	private JToolBar TheToolBar;
	private LinePlot TheLinePlot;
	private DotPlot TheDotPlot;
	private HistogramPlot TheHistogram;
	private JCheckBoxMenuItem LoadCellsImmediatelyCheckBox;
	private JCheckBoxMenuItem MultithreadCheckBox;
	private JSplitPane TheMainPanel;
	private Gui_PlateRepository ThePlatePanel;
	private int LeftPanelDisplayed;
	private JRadioButtonMenuItem[] TheImageScalings;
	private JTabbedPane TheInputPanel_Container;

	/**
	 * The ImageRail GUI Constructor
	 * 
	 * @author BLM
	 */
	public MainGUI(Model_Main TheMainModel_) {
		super("ImageRail");
		setResizable(true);
		int height = 700;
		int width = 1300;
		setSize(width, height);

		TheMainGUI = this;
		TheMainModel = TheMainModel_;
		TheMainModel.setGUI(this);

		// Setting up the GUI for the plate repository
		setPlateRepositoryGUI(new Gui_PlateRepository(
				TheMainModel.getPlateRepository()));

		setTheInputPanel_Container(new JTabbedPane());

		Model_Plate[] plates = TheMainModel.getPlateRepository()
				.getPlates();
		int len = plates.length;
		for (int i = 0; i < len; i++) {
			getTheInputPanel_Container().addTab(
					"Plate #" + plates[i].getID(),
					new MidasInputPanel(plates[i], TheMainModel
							.getExpDesignConnector()));
		}

		/*
		 * 
		 * 
		 */

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
				- height / 2);

		Container pane = getContentPane();

		// Create a split pane with the two scroll panes in it.
		TheMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		TheMainPanel.setPreferredSize(new Dimension(width, height));
		TheMainPanel.setOneTouchExpandable(true);
		TheMainPanel.setDividerLocation((int) (width / 2f));

		pane.setLayout(new BorderLayout());
		pane.add(TheMainPanel, BorderLayout.CENTER);

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

				TheMainModel.shutDownAndExit();
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
				TheMainModel.shutDown();
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


		item = new JMenuItem("Cells");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SaveFeatures_Dialog s = new SaveFeatures_Dialog(
						new DataSaver_Cells_Midas_wMetaData());
			}
		});
		menuI.add(item);


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
				TheMainModel.shutDownAndExit();
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
						well.loadCells(TheMainModel.getImageRailio(),
								true,
								true);
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
				// main.models.Model_Main.getModel().getProjectDirectory().getAbsolutePath();
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
				Model_Plate[] plates = TheMainModel.getPlateRepository().getPlates();
				Model_Plate.loadCellData(plates, false, true);
			}
		});
		loadMenu.add(item);

		item = new JMenuItem("Data & Coords");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Model_Plate[] plates = TheMainModel.getPlateRepository().getPlates();
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
				Model_Plate[] plates = TheMainModel.getPlateRepository().getPlates();
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
					TheMainModel.setColorMap(tools.ColorMaps.getColorMapIndex(name));
					if (TheDotPlot != null) {
						TheDotPlot.UpdatePlotImage = true;
						TheDotPlot.repaint();
					}
				}
			});
			colorMapsMenu.add(item);
			bg2.add(item);
			if (i == TheMainModel.getColorMap())
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

		item = new JMenuItem("Standard (v1.0)");
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

		item = new JMenuItem("Osteoclasts (v1.0)");
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

		item = new JMenuItem("Nuclei (v1.0)");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				// Finding which wells were selected
				ArrayList<Model_Well> arr = ThePlatePanel.getModel()
						.getSelectedWells_horizOrder();
				int num = arr.size();

				Model_Well[] wells = new Model_Well[num];
				for (int n = 0; n < num; n++)
					wells[n] = (Model_Well) arr.get(n);

				ThresholdingBoundsInputDialog_Nuclei s = new ThresholdingBoundsInputDialog_Nuclei(
						wells);
			}

		});
		cellMenu.add(item);

		ProcessMenu.addSeparator();
		item = new JMenuItem("Set Parameters for Batch Run");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				// Finding which wells were selected
				ArrayList<Model_Well> arr = ThePlatePanel.getModel()
						.getSelectedWells_horizOrder();
				int num = arr.size();

				Model_Well[] wells = new Model_Well[num];
				for (int n = 0; n < num; n++)
					wells[n] = (Model_Well) arr.get(n);

				ThresholdingBoundsInputDialog_BatchRun s = new ThresholdingBoundsInputDialog_BatchRun(
						wells);
			}

		});
		ProcessMenu.add(item);

		ProcessMenu.addSeparator();
		item = new JMenuItem("Stop");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (TheMainModel.isProcessing())
					TheMainModel.stopProcessing(true);
				else
					TheMainModel.stopProcessing(false);
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
	 * Returns the singleton instance of The Main Model
	 * 
	 * @author BLM
	 */
	public static Model_Main getModel() {
		return TheMainModel;
	}

	/** 
	 * Displays all the images from the selected wells from all plates in the project.
	 * @author BLM
	 * */
	public void displayImages_SelectedWells()
	{
		// Horiz ordering
		ArrayList<Model_Well> wells = new ArrayList<Model_Well>();
		Model_Plate[] plates = TheMainModel.getPlateRepository().getPlates();
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
	 * Update the midas input panel with current data
	 * 
	 * @author BLM
	 */
	public void updateMidasInputPanel() {
		Model_Plate[] ThePlates = TheMainModel.getPlateRepository()
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
							data[r][c] = oneRowSeries[c].Feature_Means[TheMainModel
									.getTheSelectedFeature_Index()];
							// TODO - see if we need to "getCells"
							if (oneRowSeries[c].getCells() != null
									&& oneRowSeries[c].Feature_Stdev != null) {
								float[] minMax = oneRowSeries[c]
										.getMinMaxValue(TheMainModel
												.getTheSelectedFeature());
								varianceBars[r][c][0] = minMax[0];
								varianceBars[r][c][1] = minMax[1];

								stdev[r][c] = oneRowSeries[c].Feature_Stdev[TheMainModel
										.getTheSelectedFeature_Index()];
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

		HistogramPlot newHist = new HistogramPlot(wells,
				TheMainModel.getTheSelectedFeature());
		if (TheHistogram != null)
			newHist.copySettings(TheHistogram);
		else
			newHist.initViewingBasis();
		// Killing old one to free up RAM
		if (TheHistogram != null)
			TheHistogram.kill();
		TheHistogram = newHist;
		TheHistogram.updateBins(TheHistogram.constructData(wells2, TheMainModel.getTheSelectedFeature_Index()));

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


	//
	// CheckBox Section
	//


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
	 * Updates the Features comboboxes
	 * 
	 * @author BLM
	 */
	public void updateFeatures() {
		/** Features comboBox */
		final Feature[] TheFeatures = TheMainModel.getFeatures();
		Feature TheSelectedFeature = TheMainModel.getTheSelectedFeature();
		ArrayList<Feature> list = new ArrayList<Feature>();
		if (TheFeatures == null)
			return;

		int len = TheFeatures.length;

		for (int i = 0; i < len; i++)
			list.add((TheFeatures[i]));

		Object[] obX = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		final JComboBox featuresComboBox = new JComboBox(obX);
		featuresComboBox.setToolTipText("Features");

		if (TheSelectedFeature == null && TheFeatures != null
				&& TheFeatures.length > 0 && TheFeatures[0] != null)
			TheSelectedFeature = (Feature) TheFeatures[0];

		ThePlatePanel.getTheToolBar().removeAll();
		ThePlatePanel.addToolbarComponents();
		if (obX.length > 0)
			ThePlatePanel.getTheToolBar().add(featuresComboBox);

		ThePlatePanel.updatePanel();
		ThePlatePanel.getTheToolBar().validate();
		ThePlatePanel.getTheToolBar().repaint();

		featuresComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				TheMainModel.setTheSelectedFeature_Index(featuresComboBox
						.getSelectedIndex());
				TheMainModel.setTheSelectedFeature(TheFeatures[featuresComboBox
						.getSelectedIndex()]);

				if (TheMainGUI != null) {
					TheMainGUI.updateAllPlots();
					TheMainGUI.validate();
					TheMainGUI.repaint();
				}
			}
		});
		TheMainModel.setTheSelectedFeature(TheFeatures[featuresComboBox
				.getSelectedIndex()]);
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
				splash.showSplashAndExit();
				splash.setVisible(false);
				// init the project with the startupdialog
				TheStartupDialog = new MainStartupDialog();

			} catch (Exception e) {

				e.printStackTrace();
				System.exit(1);
			}
			}


	}

	/**
	 * Sets the PlateRepository GUI
	 * 
	 * @param gui
	 */
	public void setPlateRepositoryGUI(Gui_PlateRepository gui) {
		ThePlatePanel = gui;
	}

	/**
	 * Returns the integer identifier of what is being displayed in the left
	 * panel
	 */
	public int getLeftPanelDisplayed() {
		return LeftPanelDisplayed;
	}

	/**
	 * Sets the dot plot
	 */
	public void setDotPlot(DotPlot dp) {
		TheDotPlot = dp;
	}

	/**
	 * Sets the histogram plot
	 */
	public void setHistogram(HistogramPlot hi) {
		TheHistogram = hi;
	}

	/**
	 * Returns the main JSplitPane in the GUI
	 * 
	 */
	public JSplitPane getTheMainPanel() {
		return TheMainPanel;
	}

	/**
	 * Returns the main JTabbedPane in the Metadata GUI
	 * 
	 */
	public JTabbedPane getTheInputPanel_Container() {
		return TheInputPanel_Container;
	}

	/**
	 * Sets the main JTabbedPane in the Metadata GUI
	 * 
	 */
	public void setTheInputPanel_Container(JTabbedPane jt) {
		TheInputPanel_Container = jt;
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
	 * Returns the gui for the plate holder panel
	 * */
	public Gui_PlateRepository getPlateHoldingPanel() {
		return TheMainModel.getPlateRepository_GUI();
	}
}
