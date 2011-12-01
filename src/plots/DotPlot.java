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

package plots;

import features.Feature;
import filters.DotFilter;
import gui.MainGUI;
import imPanels.ImageCapturePanel;
import imPanels.JPanel_highlightBox;
import imageViewers.FilmStrip_Cells;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Plate;
import models.Model_PlateRepository;
import models.Model_Well;
import sdcubeio.H5IO_Exception;
import segmentedobject.Cell;
import segmentedobject.CellCoordinates;
import tools.ColorRama;
import tools.SVG_writer;
import dialogs.AxisBoundsInputDialog;
import dialogs.CaptureImage_Dialog;

public class DotPlot extends JPanel implements ImageCapturePanel {
	private AlphaComposite transComposite = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.70f);
	private Font SmallFont = new Font("Helvetca", Font.BOLD, 10);
	private Font StandardFont = new Font("Helvetca", Font.PLAIN, 8);
	private JSlider transparencySlider;
	private JSlider percentToPlotSlider;
	private int panel_width;
	private int panel_height;
	private int XMARGIN_LEFT = 45;
	private int XMARGIN_RIGHT = 0;
	private int YMARGIN = 127;
	private Color Gray_1 = new Color(0.8f, 0.8f, 0.8f);
	private Color[] colors;
	private int numPlots;
	private boolean MousePressed;
	private float FractionToDraw;
	private int PlotPressedIn;

	private MainGUI TheMainGUI;
	public Dot[][] TheDots;
	private Model_Well[] TheWells;
	private int NumDots_Total;
	private int NumDots_Selected;

	private Bound Bounds_Y;
	private Bound Bounds_X;
	private Rectangle yAxisLabelBox;
	private Rectangle xAxisLabelBox;

	private double xStart = XMARGIN_LEFT;
	private int yStart = panel_height - YMARGIN - 20;
	private int axisLength_Y;
	private int axisLength_X;

	private double MaxValue_X;
	private double MinValue_X;
	private double MaxValue_Y;
	private double MinValue_Y;
	private int Type_AxisScale;

	private BufferedImage ThePlotImage;
	public boolean UpdatePlotImage;

	public JButton LogScaleButton_X;
	public JButton LogScaleButton_Y;

	private JButton PlotType_SideBySide;
	private JButton PlotType_Overlay;
	public static JButton Button_linearRegression;
	public static JButton Button_slopeDisplay;
	static public JCheckBoxMenuItem AxisBounds_CheckBox;
	static private JButton HighlightInOutButton;
	static private JButton ContourPlotButton;

	private DotPlot ThePlot;
	public GraphPanel TheGraphPanel;
	private Feature FeatureX;
	private Feature FeatureY;

	private boolean AddFilter;
	private boolean PolygonGate;
	private Polygon ThePolygonGate;
	private ArrayList<Point> ThePolygonGate_Points;

	public int PlotType;
	private static final int OVERLAY = 1;
	public static int SIDEBYSIDE = 2;
	private static int CONTOUR = 3;
	private boolean displayDensityMap;
	private float percentToPlot;

	public DotSelectionListener TheDotSelectionListener;
	public JComboBox[] ComboBoxes = new JComboBox[2];
	private NumberFormat nf = new DecimalFormat("0.##");
	private NumberFormat nf2 = new DecimalFormat("0.#");
	private DensitySorter TheDensitySorter;
	public ArrayList<float[][]> TheDataValues;
	public ArrayList<ArrayList<Cell>> TheCells;
	private ArrayList<DotFilter> TheDotFilters;

	public DotPlot(MainGUI mainGUI, Model_Well[] wells, Feature featureX,
			Feature featureY, boolean logX, boolean logY, int plotType_,
			boolean densityPlot, float percentToPlot_) {
		ThePlot = this;
		TheMainGUI = mainGUI;
		StandardFont = new Font("Helvetca", Font.PLAIN, 8);
		PlotType = plotType_;
		FeatureX = featureX;
		FeatureY = featureY;
		setFont(StandardFont);
		if (FeatureX == null)
			if (models.Model_Main.getModel().getTheFeatures() != null
					&& models.Model_Main.getModel().getTheFeatures().size() > 0)
				FeatureX = (Feature) models.Model_Main.getModel().getTheFeatures().get(0);
		if (FeatureY == null)
			if (models.Model_Main.getModel().getTheFeatures() != null
					&& models.Model_Main.getModel().getTheFeatures().size() > 0)
				FeatureY = (Feature) models.Model_Main.getModel().getTheFeatures().get(0);

		PlotType_SideBySide = new JButton(new ImageIcon(
				"icons/sideBySideDots.png"));
		PlotType_Overlay = new JButton(new ImageIcon("icons/overLayDots.png"));
		Button_linearRegression = new JButton(new ImageIcon(
				"icons/linearRegression.png"));
		Button_slopeDisplay = new JButton(new ImageIcon(
				"icons/slopeDisplay.png"));
		AxisBounds_CheckBox = new JCheckBoxMenuItem("Auto-Bound");
		HighlightInOutButton = new JButton(
				new ImageIcon("icons/cropDotsIn.png"));
		ContourPlotButton = new JButton(new ImageIcon("icons/contourPlot.png"));
		percentToPlot = percentToPlot_;
		PlotPressedIn = 0;
		TheDensitySorter = new DensitySorter();
		TheDotSelectionListener = new DotSelectionListener(this);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		TheWells = wells;
		AddFilter = false;
		TheDotFilters = new ArrayList<DotFilter>();

		UpdatePlotImage = true;
		MousePressed = false;
		FractionToDraw = 1f;
		Bounds_X = new Bound(this);
		Bounds_Y = new Bound(this);
		Bounds_X.Lower = 0;
		Bounds_X.Upper = 0;
		Bounds_Y.Lower = 0;
		Bounds_Y.Upper = 0;
		yAxisLabelBox = new Rectangle();
		xAxisLabelBox = new Rectangle();
		Type_AxisScale = 0;
		displayDensityMap = densityPlot;

		panel_width = 500;
		panel_height = 800;
		setSize(panel_width, panel_height);
		colors = new Color[4];
		colors[0] = new Color(0.1f, 0.1f, 0.1f);
		colors[1] = new Color(0.3f, 0.3f, 0.3f);
		colors[2] = new Color(0.5f, 0.5f, 0.5f);
		colors[3] = new Color(0.7f, 0.7f, 0.7f);

		LogScaleButton_X = new JButton(tools.Icons.Icon_LogX_selected);
		LogScaleButton_X.setToolTipText("Log Scale (x-axis)");
		LogScaleButton_X.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LogScaleButton_X.setSelected(!LogScaleButton_X.isSelected());
				if (LogScaleButton_X.isSelected())
					LogScaleButton_X.setIcon(tools.Icons.Icon_LogX_selected);
				else
					LogScaleButton_X.setIcon(tools.Icons.Icon_LogX);
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
			}
		});
		LogScaleButton_X.setSelected(logX);

		LogScaleButton_Y = new JButton(tools.Icons.Icon_LogY_selected);
		LogScaleButton_Y.setToolTipText("Log Scale (y-axis)");
		LogScaleButton_Y.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LogScaleButton_Y.setSelected(!LogScaleButton_Y.isSelected());
				if (LogScaleButton_Y.isSelected())
					LogScaleButton_Y.setIcon(tools.Icons.Icon_LogY_selected);
				else
					LogScaleButton_Y.setIcon(tools.Icons.Icon_LogY);
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
			}
		});
		LogScaleButton_Y.setSelected(logY);

		if (FeatureX != null && FeatureY != null && wells != null)
			updatePlot(wells, FeatureX.toString(), FeatureY.toString());

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 2));
		ThePlot.add(bottomPanel, BorderLayout.SOUTH);

		TheGraphPanel = new GraphPanel();
		ThePlot.add(TheGraphPanel, BorderLayout.CENTER);

		ArrayList<Feature> listX = new ArrayList<Feature>();
		int len = models.Model_Main.getModel().getTheFeatures().size();
		for (int i = 0; i < len; i++)
			listX.add((Feature) models.Model_Main.getModel().getTheFeatures().get(i));
		Object[] obX = new Object[listX.size()];
		if (listX.size() > 0)
			for (int i = 0; i < listX.size(); i++)
				obX[i] = listX.get(i);

		JLabel label = new JLabel("X-axis:");
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		bottomPanel.add(label, 0);

		label = new JLabel("Y-axis:");
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		bottomPanel.add(label, 1);

		int num = models.Model_Main.getModel().getNumberOfChannels();
		ComboBoxes[0] = new JComboBox(obX);
		ComboBoxes[0].setToolTipText("X-Axis Feature");
		int index = 0;
		if (num > 1) {
			int leng = ComboBoxes[0].getItemCount();
			for (int i = 0; i < leng; i++) {
				if (((Feature) ComboBoxes[0].getItemAt(i)).Name
						.equalsIgnoreCase(FeatureX.Name)) {
					index = i;
					break;
				}
			}
			ComboBoxes[0].setSelectedIndex(index);
		}
		ComboBoxes[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				FeatureX = (Feature) ComboBoxes[0].getSelectedItem();
				FeatureY = (Feature) ComboBoxes[1].getSelectedItem();
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
				TheMainGUI.getPlateHoldingPanel().getModel()
						.updateMinMaxValues();
			}
		});
		bottomPanel.add(ComboBoxes[0], 2);

		ComboBoxes[1] = new JComboBox(obX);
		ComboBoxes[1].setToolTipText("Y-Axis Feature");
		int leng = ComboBoxes[1].getItemCount();
		for (int i = 0; i < leng; i++) {
			if (((Feature) ComboBoxes[1].getItemAt(i)).Name
					.equalsIgnoreCase(FeatureY.Name)) {
				index = i;
				break;
			}
		}
		if (num > 1)
			ComboBoxes[1].setSelectedIndex(index);
		ComboBoxes[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				FeatureX = (Feature) ComboBoxes[0].getSelectedItem();
				FeatureY = (Feature) ComboBoxes[1].getSelectedItem();
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
				TheMainGUI.getPlateHoldingPanel().getModel()
						.updateMinMaxValues();
			}
		});
		bottomPanel.add(ComboBoxes[1], 3);

		//
		// Setting up the Toolbar
		//
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2, 5));
		ThePlot.add(topPanel, BorderLayout.NORTH);

		JToolBar tbar = new JToolBar();
		// topPanel.add(tbar, BorderLayout.NORTH);
		tbar.setOrientation(JToolBar.HORIZONTAL);
		topPanel.add(tbar, 0);

		tbar.add(LogScaleButton_X);
		tbar.add(LogScaleButton_Y);

		// JButton but = new JButton(new ImageIcon("icons/bounds.png"));
		// but.setEnabled(false);
		// tbar.add(but);
		// but.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// BoundsInputDialog b = new BoundsInputDialog(TheWells);
		//
		// updatePlot(TheWells, FeatureX, FeatureY);
		// TheGraphPanel.updatePanel();
		// validate();
		// repaint();
		// }
		// });
		JButton but = null;

		if (PlotType == SIDEBYSIDE) {
			PlotType_SideBySide.setSelected(true);
			PlotType_Overlay.setSelected(false);
			PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide_selected);
			PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay);
		} else if (PlotType == OVERLAY) {
			PlotType_SideBySide.setSelected(false);
			PlotType_Overlay.setSelected(true);
			PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide);
			PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay_selected);
		} else if (PlotType == CONTOUR) {
			PlotType_SideBySide.setSelected(false);
			PlotType_Overlay.setSelected(false);
			PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide);
			PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay);
			ContourPlotButton.setIcon(tools.Icons.Icon_ContourPlot_selected);
		}

		// PlotType_Overlay.setToolTipText( "Overlay Plots");
		// tbar.add(PlotType_Overlay);
		// PlotType_Overlay.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// PlotType = OVERLAY;
		// PlotType_SideBySide.setSelected(false);
		// PlotType_Overlay.setSelected(true);
		// PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide);
		// PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay_selected);
		// ContourPlotButton.setIcon(tools.Icons.Icon_ContourPlot);
		// ContourPlotButton.setSelected(false);
		//
		// validate();
		// repaint();
		// updatePlot(TheWells,
		// ((Feature)ComboBoxes[0].getSelectedItem()).toString(),
		// ((Feature)ComboBoxes[1].getSelectedItem()).toString());
		// }
		// });
		// PlotType_SideBySide.setToolTipText( "Side-by-Side Plots");
		// tbar.add(PlotType_SideBySide);
		// PlotType_SideBySide.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// PlotType = SIDEBYSIDE;
		// PlotType_SideBySide.setSelected(true);
		// PlotType_Overlay.setSelected(false);
		// PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide_selected);
		// PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay);
		// ContourPlotButton.setIcon(tools.Icons.Icon_ContourPlot);
		// ContourPlotButton.setSelected(false);
		//
		// validate();
		// repaint();
		// updatePlot(TheWells,
		// ((Feature)ComboBoxes[0].getSelectedItem()).toString(),
		// ((Feature)ComboBoxes[1].getSelectedItem()).toString());
		// }
		// });
		//
		// ContourPlotButton.setToolTipText("Contour Plot");
		// ContourPlotButton.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// ContourPlotButton.setSelected(!ContourPlotButton.isSelected());
		// if(ContourPlotButton.isSelected())
		// {
		// PlotType = CONTOUR;
		// ContourPlotButton.setIcon(tools.Icons.Icon_ContourPlot_selected);
		// PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide);
		// PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay);
		// PlotType_SideBySide.setSelected(false);
		// PlotType_Overlay.setSelected(false);
		// }
		// else
		// {
		// PlotType = SIDEBYSIDE;
		// ContourPlotButton.setIcon(tools.Icons.Icon_ContourPlot);
		// PlotType_SideBySide.setIcon(tools.Icons.Icon_SideBySide_selected);
		// PlotType_Overlay.setIcon(tools.Icons.Icon_Overlay);
		// PlotType_SideBySide.setSelected(true);
		// PlotType_Overlay.setSelected(false);
		// }
		// updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
		// validate();
		// repaint();
		// }
		// });
		// if (PlotType == CONTOUR)
		// ContourPlotButton.setSelected(true);
		// else
		// ContourPlotButton.setSelected(false);
		// tbar.add(ContourPlotButton);

		// final JButton buttt = new JButton(tools.Icons.Icon_Bkgd_selected);
		// if (models.Model_Main.getModel().getBackgroundSubtract())
		// {
		// buttt.setIcon(tools.Icons.Icon_Bkgd_selected);
		// buttt.setSelected(true);
		// }
		// else
		// {
		// buttt.setIcon(tools.Icons.Icon_Bkgd);
		// buttt.setSelected(false);
		// }
		// buttt.setToolTipText("Subtract Background");
		// tbar.add(buttt);
		// buttt.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// models.Model_Main.getModel().setBackgroundSubtract(!models.Model_Main.getModel().getBackgroundSubtract());
		// buttt.setSelected(models.Model_Main.getModel().getBackgroundSubtract() );
		// if(models.Model_Main.getModel().getBackgroundSubtract() )
		// buttt.setIcon(tools.Icons.Icon_Bkgd_selected);
		// else
		// buttt.setIcon(tools.Icons.Icon_Bkgd);
		//
		// updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
		// TheMainGUI.getPlateHoldingPanel().repaint();
		// validate();
		// repaint();
		//
		// }
		// });

		Button_linearRegression.setToolTipText("Linear Regression");
		// tbar.add(Button_linearRegression);
		Button_linearRegression.setSelected(false);
		if (Button_linearRegression.isSelected())
			Button_linearRegression
					.setIcon(tools.Icons.Icon_linearRegression_selected);
		else
			Button_linearRegression.setIcon(tools.Icons.Icon_linearRegression);
		Button_linearRegression.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {

				Button_linearRegression.setSelected(!Button_linearRegression
						.isSelected());
				if (Button_linearRegression.isSelected())
					Button_linearRegression
							.setIcon(tools.Icons.Icon_linearRegression_selected);
				else
					Button_linearRegression
							.setIcon(tools.Icons.Icon_linearRegression);

				validate();
				repaint();
				updatePlot(TheWells,
						((Feature) ComboBoxes[0].getSelectedItem()).toString(),
						((Feature) ComboBoxes[1].getSelectedItem()).toString());
			}
		});

		// tbar.add(Button_slopeDisplay);
		// Button_slopeDisplay.setSelected(slopeDisplay);
		// if (Button_slopeDisplay.isSelected())
		// Button_slopeDisplay.setIcon(tools.Icons.Icon_slopeDisplay_selected);
		// else
		// Button_slopeDisplay.setIcon(tools.Icons.Icon_slopeDisplay);
		//
		// Button_slopeDisplay.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// Button_slopeDisplay.setSelected(!Button_slopeDisplay.isSelected());
		// if (Button_slopeDisplay.isSelected())
		// {
		// Button_slopeDisplay.setIcon(tools.Icons.Icon_slopeDisplay_selected);
		// Model_Plate plate = new Model_Plate(TheMainGUI,
		// TheMainGUI.ThePlatePanel.numPlates_Rows,
		// TheMainGUI.ThePlatePanel.numPlates_Cols,
		// "Distribution Slope Display");
		// TheGraphPanel.add(plate);
		// }
		// else
		// {
		// TheGraphPanel.removeAll();
		// Button_slopeDisplay.setIcon(tools.Icons.Icon_slopeDisplay);
		// }
		//
		//
		// updatePlot(TheWells, FeatureX, FeatureY);
		// TheGraphPanel.updatePanel();
		// validate();
		// repaint();
		// }
		// });

		// transparencySlider = new JSlider(0, 100, (int) (transComposite
		// .getAlpha() * 100f));
		transparencySlider = new JSlider(0, 100, 80);

		transparencySlider.addChangeListener(new SliderListener_Alpha());
		transparencySlider.setToolTipText("Dot Transparancey");
		// topPanel.add(transparencySlider,1);

		percentToPlotSlider = new JSlider(0, 100, (int) (percentToPlot * 100));
		percentToPlotSlider
				.addChangeListener(new SliderListener_PercentToPlot());
		percentToPlotSlider.setToolTipText("Percent Dots to Plot");
		topPanel.add(percentToPlotSlider, 1);

		final JButton butt = new JButton(new ImageIcon("icons/polygonGate.png"));
		butt.setToolTipText("Freestyle selection");
		tbar.add(butt);
		butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PolygonGate = !PolygonGate;
				butt.setSelected(PolygonGate);
				if (PolygonGate)
					butt.setIcon(tools.Icons.Icon_polygonGate_selected);
				else
					butt.setIcon(tools.Icons.Icon_polygonGate);
			}
		});

		// tbar.add(HighlightInOutButton);
		// HighlightInOutButton.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// HighlightInOutButton.setSelected(!HighlightInOutButton.isSelected());
		// if (HighlightInOutButton.isSelected())
		// {
		// HighlightInOutButton.setIcon(Icon_cropDotsOut);
		// selectDotsPolygonGate_out();
		// }
		// else
		// {
		// HighlightInOutButton.setIcon(Icon_cropDotsIn);
		// selectDotsPolygonGate_in();
		// }
		//
		// TheGraphPanel.updatePanel();
		// validate();
		// repaint();
		// }
		// });

		but = new JButton(new ImageIcon("icons/addGate.png"));
		but.setToolTipText("Add Gate");
		tbar.add(but);
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (TheDots == null || TheDots.length == 0)
					return;
				int num = TheWells.length;

				updateDimensions();
				axisLength_X = (int) (panel_width - XMARGIN_RIGHT - xStart);
				axisLength_Y = yStart - 20;

				int numPlots = TheDots.length;
				double plotBufferX = 0;

				//Get gate name from user
				String gateName = JOptionPane.showInputDialog(null, "Enter name for new gate : ", "Gate Name Input", 1);

				
				if (PlotType == OVERLAY) {
					if (ThePolygonGate != null) {
						int len = ThePolygonGate.xpoints.length;
						double axisLenX = axisLength_X - plotBufferX;

						int count = 0;
						for (int i = 0; i < len; i++) {
							double x = ThePolygonGate.xpoints[i];
							double y = ThePolygonGate.ypoints[i];

							x = ((x - xStart)
									* (Bounds_X.Upper - Bounds_X.Lower)
									/ (axisLenX) + Bounds_X.Lower);
							y = (Bounds_Y.Lower + (yStart - y)
									* (Bounds_Y.Upper - Bounds_Y.Lower)
									/ axisLength_Y);

							if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
									&& y >= Bounds_Y.Lower
									&& y <= Bounds_Y.Upper)
								count++;
						}
						double[][] xyPoints_bounds = new double[2][len];
						int counter = 0;
						for (int i = 0; i < len; i++) {
							double x = ThePolygonGate.xpoints[i];
							double y = ThePolygonGate.ypoints[i];

							x = ((x - xStart)
									* (Bounds_X.Upper - Bounds_X.Lower)
									/ (axisLenX) + Bounds_X.Lower);
							y = (Bounds_Y.Lower + (yStart - y)
									* (Bounds_Y.Upper - Bounds_Y.Lower)
									/ axisLength_Y);

							if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
									&& y >= Bounds_Y.Lower
									&& y <= Bounds_Y.Upper) {
								xyPoints_bounds[0][counter] = x;
								xyPoints_bounds[1][counter] = y;
								counter++;
							}
						}

						// log scale if needed
						if (LogScaleButton_X.isSelected())
							for (int i = 0; i < counter; i++)
								xyPoints_bounds[0][i] = tools.MathOps
										.exp(xyPoints_bounds[0][i]);
						if (LogScaleButton_Y.isSelected())
							for (int i = 0; i < counter; i++)
								xyPoints_bounds[1][i] = tools.MathOps
										.exp(xyPoints_bounds[1][i]);

						int ID = TheMainGUI.getPlateHoldingPanel().getModel()
								.getUniqueGateID();
						for (int n = 0; n < num; n++) {
							Gate_DotPlot g = new Gate_DotPlot(xyPoints_bounds,
									FeatureX, FeatureY, ID, gateName);
							Model_Well well = TheWells[n];
							well.TheGates.add(g);
							well.GateCounter++;
						}
					} else if (TheGraphPanel.highlightBox != null) {
						double axisLenX = axisLength_X - plotBufferX;
						Rectangle box = TheGraphPanel.highlightBox;

						double[][] xyPoints_bounds = new double[2][4];
						int counter = 0;
						for (int i = 0; i < 4; i++) {
							double x = 0;
							double y = 0;
							if (i == 0) {
								x = box.x;
								y = box.y;
							} else if (i == 1) {
								x = box.x + box.width;
								y = box.y;
							} else if (i == 2) {
								x = box.x + box.width;
								y = box.y + box.height;
							} else if (i == 3) {
								x = box.x;
								y = box.y + box.height;
							}
							x = ((x - xStart)
									* (Bounds_X.Upper - Bounds_X.Lower)
									/ (axisLenX) + Bounds_X.Lower);
							y = (Bounds_Y.Lower + (yStart - y)
									* (Bounds_Y.Upper - Bounds_Y.Lower)
									/ axisLength_Y);

							if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
									&& y >= Bounds_Y.Lower
									&& y <= Bounds_Y.Upper) {
								xyPoints_bounds[0][counter] = x;
								xyPoints_bounds[1][counter] = y;
								counter++;
							}
						}

						// log scale if needed
						if (LogScaleButton_X.isSelected())
							for (int i = 0; i < counter; i++)
								xyPoints_bounds[0][i] = tools.MathOps
										.exp(xyPoints_bounds[0][i]);
						if (LogScaleButton_Y.isSelected())
							for (int i = 0; i < counter; i++)
								xyPoints_bounds[1][i] = tools.MathOps
										.exp(xyPoints_bounds[1][i]);

						int ID = TheMainGUI.getPlateHoldingPanel().getModel()
								.getUniqueGateID();
						for (int n = 0; n < num; n++) {
							Gate_DotPlot g = new Gate_DotPlot(xyPoints_bounds,
									FeatureX, FeatureY, ID, gateName);
						
							Model_Well well = TheWells[n];
							well.TheGates.add(g);
							well.GateCounter++;
						}
					}

				} else // SideBySide
				{
					
					if (ThePolygonGate != null) // Polygon gate
					{
						int len = ThePolygonGate.xpoints.length;
						double axisLenX = axisLength_X / numPlots - plotBufferX;
						double[][] xyPoints_bounds = new double[2][len];
						for (int i = 0; i < len; i++) {
							double x = ThePolygonGate.xpoints[i];
							double y = ThePolygonGate.ypoints[i];

							double v = (x - xStart - plotBufferX) % axisLenX;
							x = (v * (Bounds_X.Upper - Bounds_X.Lower)
									/ (axisLenX) + Bounds_X.Lower);
							y = (Bounds_Y.Lower + (yStart - y)
									* (Bounds_Y.Upper - Bounds_Y.Lower)
									/ axisLength_Y);

							//
							// //Seeing if we went too far left or right
							// int xS = (int)((x-xStart)/axisLenX);
							// if (xS<PlotPressedIn) //Too far left
							// x =
							// (0*(Bounds_X.Upper-Bounds_X.Lower)/(axisLenX)+Bounds_X.Lower);
							// else if (xS>PlotPressedIn) //Too far right
							// x =
							// (axisLenX*(Bounds_X.Upper-Bounds_X.Lower)/(axisLenX)+Bounds_X.Lower);
							// else
							// {
							// v = (x-xStart-plotBufferX)%axisLenX;
							// x =
							// (v*(Bounds_X.Upper-Bounds_X.Lower)/(axisLenX)+Bounds_X.Lower);
							// if (x< Bounds_X.Lower)
							// x = Bounds_X.Lower;
							// }
							//
							// y =
							// (Bounds_Y.Lower+(yStart-y)*(Bounds_Y.Upper-Bounds_Y.Lower)/axisLength_Y);
							// if (y > Bounds_Y.Upper)
							// y = Bounds_Y.Upper;
							// if (y < Bounds_Y.Lower)
							// y = Bounds_Y.Lower;
							//
							//

							xyPoints_bounds[0][i] = x;
							xyPoints_bounds[1][i] = y;
						}

						// log scale if needed
						if (LogScaleButton_X.isSelected())
							for (int i = 0; i < len; i++)
								xyPoints_bounds[0][i] = tools.MathOps
										.exp(xyPoints_bounds[0][i]);
						if (LogScaleButton_Y.isSelected())
							for (int i = 0; i < len; i++)
								xyPoints_bounds[1][i] = tools.MathOps
										.exp(xyPoints_bounds[1][i]);

						int ID = TheMainGUI.getPlateHoldingPanel().getModel()
								.getUniqueGateID();
						for (int n = 0; n < num; n++) {
							Gate_DotPlot g = new Gate_DotPlot(xyPoints_bounds,
									FeatureX, FeatureY, ID, gateName);
							Model_Well well = TheWells[n];
							well.TheGates.add(g);
							well.GateCounter++;
						}
					} else if (TheGraphPanel.highlightBox != null) // We have a
																	// box gate
					{
						int len = 4;
						double axisLenX = axisLength_X / numPlots - plotBufferX;
						double[][] xyPoints_bounds = new double[2][len];
						Rectangle box = TheGraphPanel.highlightBox;

						for (int i = 0; i < len; i++) {
							double x = 0;
							double y = 0;
							if (i == 0) {
								x = box.x;
								y = box.y;
							} else if (i == 1) {
								x = box.x + box.width;
								y = box.y;
							} else if (i == 2) {
								x = box.x + box.width;
								y = box.y + box.height;
							} else if (i == 3) {
								x = box.x;
								y = box.y + box.height;
							}

							// Seeing if we went too far left or right
							int xS = (int) ((x - xStart) / axisLenX);
							if (xS < PlotPressedIn) // Too far left
								x = (0 * (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
							else if (xS > PlotPressedIn) // Too far right
								x = (axisLenX
										* (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
							else {
								double v = (x - xStart - plotBufferX)
										% axisLenX;
								x = (v * (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								if (x < Bounds_X.Lower)
									x = Bounds_X.Lower;
							}

							y = (Bounds_Y.Lower + (yStart - y)
									* (Bounds_Y.Upper - Bounds_Y.Lower)
									/ axisLength_Y);
							if (y > Bounds_Y.Upper)
								y = Bounds_Y.Upper;
							if (y < Bounds_Y.Lower)
								y = Bounds_Y.Lower;

							xyPoints_bounds[0][i] = x;
							xyPoints_bounds[1][i] = y;

						}

						// log scale if needed
						if (LogScaleButton_X.isSelected())
							for (int i = 0; i < len; i++)
								xyPoints_bounds[0][i] = tools.MathOps
										.exp(xyPoints_bounds[0][i]);
						if (LogScaleButton_Y.isSelected())
							for (int i = 0; i < len; i++)
								xyPoints_bounds[1][i] = tools.MathOps
										.exp(xyPoints_bounds[1][i]);

						int ID = TheMainGUI.getPlateHoldingPanel().getModel()
								.getUniqueGateID();
						for (int n = 0; n < num; n++) {
							Gate_DotPlot g = new Gate_DotPlot(xyPoints_bounds,
									FeatureX, FeatureY, ID, gateName);
							Model_Well well = TheWells[n];
							well.TheGates.add(g);
							well.GateCounter++;
						}
					}
				}

				ThePolygonGate = null;
				TheGraphPanel.updatePanel();

				validate();
				repaint();
			}
		});

		but = new JButton(new ImageIcon("icons/addGateToAll.png"));
		but.setToolTipText("Add Gate To All Wells");
		tbar.add(but);
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (TheDots == null || TheDots.length == 0)
					return;
				//Get gate name from user
				String gateName = JOptionPane.showInputDialog(null, "Enter name for new gate : ", "Gate Name Input", 1);

				Model_PlateRepository platePanel = TheMainGUI
						.getPlateHoldingPanel().getModel();
				int numPlates = platePanel.getNumPlates();

				for (int p = 0; p < numPlates; p++) {
					Model_Plate plate = platePanel.getPlates()[p];
					int numC = plate.getNumColumns();
					int numR = plate.getNumRows();

					updateDimensions();
					axisLength_X = (int) (panel_width - XMARGIN_RIGHT - xStart);
					axisLength_Y = yStart - 20;

					int numPlots = TheDots.length;
					double plotBufferX = 0;

					if (PlotType == OVERLAY) {
						if (ThePolygonGate != null) {
							int len = ThePolygonGate.xpoints.length;
							double axisLenX = axisLength_X - plotBufferX;

							int count = 0;
							for (int i = 0; i < len; i++) {
								double x = ThePolygonGate.xpoints[i];
								double y = ThePolygonGate.ypoints[i];

								x = ((x - xStart)
										* (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								y = (Bounds_Y.Lower + (yStart - y)
										* (Bounds_Y.Upper - Bounds_Y.Lower)
										/ axisLength_Y);

								if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
										&& y >= Bounds_Y.Lower
										&& y <= Bounds_Y.Upper)
									count++;
							}
							double[][] xyPoints_bounds = new double[2][len];
							int counter = 0;
							for (int i = 0; i < len; i++) {
								double x = ThePolygonGate.xpoints[i];
								double y = ThePolygonGate.ypoints[i];

								x = ((x - xStart)
										* (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								y = (Bounds_Y.Lower + (yStart - y)
										* (Bounds_Y.Upper - Bounds_Y.Lower)
										/ axisLength_Y);

								if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
										&& y >= Bounds_Y.Lower
										&& y <= Bounds_Y.Upper) {
									xyPoints_bounds[0][counter] = x;
									xyPoints_bounds[1][counter] = y;
									counter++;
								}
							}

							// log scale if needed
							if (LogScaleButton_X.isSelected())
								for (int i = 0; i < counter; i++)
									xyPoints_bounds[0][i] = tools.MathOps
											.exp(xyPoints_bounds[0][i]);
							if (LogScaleButton_Y.isSelected())
								for (int i = 0; i < counter; i++)
									xyPoints_bounds[1][i] = tools.MathOps
											.exp(xyPoints_bounds[1][i]);

							int ID = TheMainGUI.getPlateHoldingPanel()
									.getModel()
									.getUniqueGateID();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Gate_DotPlot g = new Gate_DotPlot(
											xyPoints_bounds, FeatureX,
											FeatureY, ID, gateName);
									Model_Well well = plate.getWells()[r][c];
									well.TheGates.add(g);
									well.GateCounter++;
								}
						} else if (TheGraphPanel.highlightBox != null) {
							double axisLenX = axisLength_X - plotBufferX;
							Rectangle box = TheGraphPanel.highlightBox;

							double[][] xyPoints_bounds = new double[2][4];
							int counter = 0;
							for (int i = 0; i < 4; i++) {
								double x = 0;
								double y = 0;
								if (i == 0) {
									x = box.x;
									y = box.y;
								} else if (i == 1) {
									x = box.x + box.width;
									y = box.y;
								} else if (i == 2) {
									x = box.x + box.width;
									y = box.y + box.height;
								} else if (i == 3) {
									x = box.x;
									y = box.y + box.height;
								}
								x = ((x - xStart)
										* (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								y = (Bounds_Y.Lower + (yStart - y)
										* (Bounds_Y.Upper - Bounds_Y.Lower)
										/ axisLength_Y);

								if (x >= Bounds_X.Lower && x <= Bounds_X.Upper
										&& y >= Bounds_Y.Lower
										&& y <= Bounds_Y.Upper) {
									xyPoints_bounds[0][counter] = x;
									xyPoints_bounds[1][counter] = y;
									counter++;
								}
							}

							// log scale if needed
							if (LogScaleButton_X.isSelected())
								for (int i = 0; i < counter; i++)
									xyPoints_bounds[0][i] = tools.MathOps
											.exp(xyPoints_bounds[0][i]);
							if (LogScaleButton_Y.isSelected())
								for (int i = 0; i < counter; i++)
									xyPoints_bounds[1][i] = tools.MathOps
											.exp(xyPoints_bounds[1][i]);

							int ID = TheMainGUI.getPlateHoldingPanel()
									.getModel()
									.getUniqueGateID();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Gate_DotPlot g = new Gate_DotPlot(
											xyPoints_bounds, FeatureX,
											FeatureY, ID, gateName);
									Model_Well well = plate.getWells()[r][c];
									well.TheGates.add(g);
									well.GateCounter++;
								}

						}
					} else // sideBySide
					{
						if (ThePolygonGate != null) {
							int len = ThePolygonGate.xpoints.length;
							double axisLenX = axisLength_X / numPlots
									- plotBufferX;
							double[][] xyPoints_bounds = new double[2][len];
							for (int i = 0; i < len; i++) {
								double x = ThePolygonGate.xpoints[i];
								double y = ThePolygonGate.ypoints[i];

								double v = (x - xStart - plotBufferX)
										% axisLenX;
								x = (v * (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								y = (Bounds_Y.Lower + (yStart - y)
										* (Bounds_Y.Upper - Bounds_Y.Lower)
										/ axisLength_Y);
								xyPoints_bounds[0][i] = x;
								xyPoints_bounds[1][i] = y;
							}

							// log scale if needed
							if (LogScaleButton_X.isSelected())
								for (int i = 0; i < len; i++)
									xyPoints_bounds[0][i] = tools.MathOps
											.exp(xyPoints_bounds[0][i]);
							if (LogScaleButton_Y.isSelected())
								for (int i = 0; i < len; i++)
									xyPoints_bounds[1][i] = tools.MathOps
											.exp(xyPoints_bounds[1][i]);

							int ID = TheMainGUI.getPlateHoldingPanel()
									.getModel()
									.getUniqueGateID();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Gate_DotPlot g = new Gate_DotPlot(
											xyPoints_bounds, FeatureX,
											FeatureY, ID, gateName);
									Model_Well well = plate.getWells()[r][c];
									well.TheGates.add(g);
									well.GateCounter++;
								}
						} else if (TheGraphPanel.highlightBox != null) // We
																		// have
																		// a box
																		// gate
						{
							int len = 4;
							double axisLenX = axisLength_X / numPlots
									- plotBufferX;
							double[][] xyPoints_bounds = new double[2][len];
							Rectangle box = TheGraphPanel.highlightBox;

							for (int i = 0; i < len; i++) {
								double x = 0;
								double y = 0;
								if (i == 0) {
									x = box.x;
									y = box.y;
								} else if (i == 1) {
									x = box.x + box.width;
									y = box.y;
								} else if (i == 2) {
									x = box.x + box.width;
									y = box.y + box.height;
								} else if (i == 3) {
									x = box.x;
									y = box.y + box.height;
								}

								double v = (x - xStart - plotBufferX)
										% axisLenX;
								x = (v * (Bounds_X.Upper - Bounds_X.Lower)
										/ (axisLenX) + Bounds_X.Lower);
								y = (Bounds_Y.Lower + (yStart - y)
										* (Bounds_Y.Upper - Bounds_Y.Lower)
										/ axisLength_Y);
								xyPoints_bounds[0][i] = x;
								xyPoints_bounds[1][i] = y;

							}

							// log scale if needed
							if (LogScaleButton_X.isSelected())
								for (int i = 0; i < len; i++)
									xyPoints_bounds[0][i] = tools.MathOps
											.exp(xyPoints_bounds[0][i]);
							if (LogScaleButton_Y.isSelected())
								for (int i = 0; i < len; i++)
									xyPoints_bounds[1][i] = tools.MathOps
											.exp(xyPoints_bounds[1][i]);

							int ID = TheMainGUI.getPlateHoldingPanel()
									.getModel()
									.getUniqueGateID();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Gate_DotPlot g = new Gate_DotPlot(
											xyPoints_bounds, FeatureX,
											FeatureY, ID, gateName);
									Model_Well well = plate.getWells()[r][c];
									well.TheGates.add(g);
									well.GateCounter++;
								}
						}
					}

				}

				ThePolygonGate = null;
				TheGraphPanel.updatePanel();

				validate();
				repaint();

			}
		});

		// but = new JButton(new ImageIcon("icons/minusGate.png"));
		// tbar.add(but);
		// but.setToolTipText("Delete Gate");
		// but.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// int numPlates = TheMainGUI.getPlateHoldingPanel().getNumPlates();
		//
		// for (int p = 0; p < numPlates; p++)
		// {
		// Model_Plate plate =
		// Themodels.Model_Main.getModel().getThePlateHoldingPanel().getThePlates()[p];
		// int rows = plate.getNumRows();
		// int cols = plate.getNumColumns();
		// for (int r = 0; r < rows; r++)
		// for (int c = 0; c < cols; c++)
		// {
		// Model_Well well = plate.getTheWells()[r][c];
		// int len = well.TheGates.size();
		//
		// for (int i = 0; i < len; i++)
		// {
		// Gate_DotPlot g = (Gate_DotPlot)well.TheGates.get(i);
		// if (g.selected)
		// {
		// well.TheGates.remove(i);
		// i--;
		// len--;
		// }
		// }
		// }
		//
		// }
		//
		//
		//
		// validate();
		//
		// UpdatePlotImage = false;
		// repaint();
		//
		//
		// }
		// });

		but = new JButton(tools.Icons.Icon_DeleteAllGates);
		tbar.add(but);
		but.setToolTipText("Delete All Gates");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int numPlates = TheMainGUI.getPlateHoldingPanel().getModel()
						.getNumPlates();

				for (int p = 0; p < numPlates; p++) {

					Model_Plate plate = TheMainGUI.getPlateHoldingPanel()
							.getModel()
							.getPlates()[p];
					int rows = plate.getNumRows();
					int cols = plate.getNumColumns();
					for (int r = 0; r < rows; r++)
						for (int c = 0; c < cols; c++) {
							Model_Well well = plate.getWells()[r][c];
							int len = well.TheGates.size();

							for (int i = 0; i < len; i++) {
								Gate_DotPlot g = (Gate_DotPlot) well.TheGates
										.get(i);
								well.TheGates.remove(i);
								i--;
								len--;
							}
						}
				}

				validate();
				UpdatePlotImage = false;
				repaint();

			}
		});

		// Add new Filter
		final JButton buttt = new JButton(tools.Icons.Icon_FilterAdd);
		tbar.add(buttt);
		buttt.setToolTipText("Add Dot Filter");
		buttt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				AddFilter = !AddFilter;
				buttt.setSelected(AddFilter);
				if (AddFilter)
					buttt.setIcon(tools.Icons.Icon_FilterAdd_selected);
				else {
					buttt.setIcon(tools.Icons.Icon_FilterAdd);
					// Delete all filters
					for (int i = 0; i < TheDotFilters.size(); i++) {
						TheDotFilters.get(i).kill();
						TheDotFilters = new ArrayList<DotFilter>();
					}
					TheGraphPanel.repaint();
				}

			}
		});

		but = new JButton(tools.Icons.Icon_Film);
		// tbar.add(but);
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Cell[] cells = getSelectedCells();
				FilmStrip_Cells f = null;
				System.out.println("cells.length: " + cells.length);
				if (cells != null)
					f = new FilmStrip_Cells(cells);
			}
		});

		// final JButton dbut = new JButton(new
		// ImageIcon("icons/colormap.png"));
		// dbut.setToolTipText( "Toggle between Black & Density Plot");
		// tbar.add(dbut);
		// dbut.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// displayDensityMap = !displayDensityMap;
		// UpdatePlotImage = true;
		// repaint();
		// }
		// });

		but = new JButton(new ImageIcon("icons/record.png"));
		tbar.add(but);
		but.setToolTipText("Export Gate Statistics");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fc = null;
				if (models.Model_Main.getModel().getTheDirectory() != null)
					fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
				else
					fc = new JFileChooser();

				File outDir = null;

				fc.setDialogTitle("Save as...");
				int returnVal = fc.showSaveDialog(ThePlot);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					outDir = fc.getSelectedFile();
					outDir = (new File(outDir.getAbsolutePath()));
					outDir.mkdir();
				} else
					System.out.println("Open command cancelled by user.");

				if (outDir != null) {
					models.Model_Main.getModel().setTheDirectory(
							new File(outDir.getParent()));
					try {
						ArrayList<Gate_DotPlot> arr = new ArrayList<Gate_DotPlot>();

						// Finding unique gate IDs for headers
						Model_PlateRepository platePanel = TheMainGUI
								.getPlateHoldingPanel().getModel();
						int numPlates = platePanel.getNumPlates();
						Model_Plate[] plates = platePanel.getPlates();
						for (int i = 0; i < numPlates; i++) {
							Model_Plate plate = platePanel.getPlates()[i];
							int numC = plate.getNumColumns();
							int numR = plate.getNumRows();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Model_Well well = plate.getWells()[r][c];
									int len = well.TheGates.size();
									for (int d = 0; d < len; d++) {
										Gate_DotPlot g = well.TheGates.get(d);
										int ven = arr.size();
										boolean foundOne = false;
										for (int k = 0; k < ven; k++) {
											Gate_DotPlot gate = arr.get(k);
											if (g.ID == gate.ID) {
												foundOne = true;
												break;
											}
										}
										if (!foundOne)
											arr.add(g);
									}
								}
						}

						//
						// Printing
						//
						
						Feature[] features = models.Model_Main.getModel().getFeatures();
						File[] files = null;
						int numFiles = 0;
						if(features!=null && outDir!=null)
						{
							files = new File[features.length+2];
							numFiles = files.length;
							files[0] = new File(outDir.getAbsolutePath()+ File.separator + "Number_Cells.csv");
							files[1] = new File(outDir.getAbsolutePath()+ File.separator + "Number_Nuclei.csv");
							
							for (int i = 0; i < features.length; i++)
							{
								//Checking for "/" in the feature names and removing it since it causes problems in OS file naming
								String fname = features[i].getName();
								int ind = fname.indexOf("/");
								int counter = 0;//safty counter
								while (ind>=0)
								{
									fname = fname.substring(0,ind)+fname.substring(ind+1,fname.length());
									ind = fname.indexOf("/");
									counter++;
									if (counter>100000)
									{
										System.out.println("**ERROR removing '/' from feature name: "+features[i].getName());
										break;
									}
								}
								files[i+2] = new File(outDir.getAbsolutePath()+ File.separator + "GateMeans_"+fname+".csv");
							}
						}
						
						for (int f = 0; f < numFiles; f++) {
							
						
						PrintWriter pw = new PrintWriter(files[f]);
						pw.print("Well, Plate, Total_Cells ");
						int ven = arr.size();
						for (int j = 0; j < ven; j++) {
							Gate_DotPlot g = arr.get(j);
							pw.print(", Gate=" + g.getName());
						}
						pw.println();

						// Printing out Cell counts for each gate in each well:
					
						for (int i = 0; i < numPlates; i++) {
							Model_Plate plate = platePanel.getPlates()[i];
							int numC = plate.getNumColumns();
							int numR = plate.getNumRows();
							for (int r = 0; r < numR; r++)
								for (int c = 0; c < numC; c++) {
									Model_Well well = plate.getWells()[r][c];
									ArrayList<Cell> cells = well.getCells();
									if (cells != null && cells.size() > 0) {
										int len = well.TheGates.size();
										// Model_Well Name... ex: A01
										pw.print(well.name);
										pw.print("," + well.getPlate().getID());
										// Printing Total number of cells:
										pw.print(", " + cells.size());
										// For each unique Gate... see if this
										// well has that gate
										for (int j = 0; j < ven; j++) {
											Gate_DotPlot g2 = arr.get(j);
											for (int k = 0; k < len; k++) {
												Gate_DotPlot g = well.TheGates
														.get(k);
												if (g2.ID == g.ID) {

													//The first thing we print is number of cells bound by each gate
													float val = 0;
													if(f==0)
														val = g
															.getTotalNumberOfCellsBound(well
																	.getCells());
													if(f==1)
													{
														val = g
															.getBoundCellsFeatures_Integrated(well
																	.getCells(),"Num_Nuclei");

													}
													//Else we print out the mean values of all the features for each gate
													// in separate files; one file for each feature
														else if (f > 1)
													{
														val = g
																.getBoundCellsFeatures_Mean(well
.getCells(),
																	features[f - 2]
																			.getName());
													}
													
													
													pw.print(", " + val);
												}
											}
										}
										// fraction ungated
										pw.println();
									}
								}
						}
						
				

							pw.flush();
							pw.close();
						}
					} catch (FileNotFoundException e) {
						System.out.println("Error Printing File: ");
						e.printStackTrace();
					}
				}

			}
		});

		but = new JButton(new ImageIcon("icons/camera.png"));
		tbar.add(but);
		but.setToolTipText("Capture Image of Plot");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				CaptureImage_Dialog s = new CaptureImage_Dialog(ThePlot);
			}
		});

		but = new JButton(new ImageIcon("icons/garbage.png"));
		tbar.add(but);
		but.setToolTipText("Delete Selected Cells");
		but.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (TheDots == null)
					return;

				int numDeleted = deleteSelectedDots();
				if (numDeleted > 0) {

					updatePlot(TheWells, FeatureX.toString(),
							FeatureY.toString());
					validate();
					repaint();

					// models.Model_Main.getModel().updateAllPlots();
					models.Model_Main.getModel().setCellsModified(true);
				}
			}
		});

		TheGraphPanel.updatePanel();

	}

	public void setPercentToPlot(float val) {
		percentToPlot = val;
	}

	public float getPercentToPlot() {
		return percentToPlot;
	}

	public Cell[] getSelectedCells() {
		if (TheDots == null)
			return null;
		ArrayList<Cell> arr = new ArrayList<Cell>();
		int numP = TheDots.length;
		for (int p = 0; p < numP; p++) {
			int num = TheDots[p].length;
			for (int n = 0; n < num; n++) {
				Dot dot = TheDots[p][n];
				if (dot.isSelected())
					arr.add(dot.getCell());
			}
		}

		int len = arr.size();
		Cell[] cells = new Cell[len];
		for (int i = 0; i < len; i++)
			cells[i] = arr.get(i);

		return cells;
	}

	/**
	 * Returns the Dots contained in this dot plot. Length: NumPlots x NumDots
	 * 
	 * @author BLM
	 */
	public Dot[][] getDots() {
		return TheDots;
	}

	//
	//
	//
	public int deleteSelectedDots() {
		long time = System.currentTimeMillis();

		int numToDelete = 0;
		int numP = TheDots.length;
		for (int p = 0; p < numP; p++) {
			int num = TheDots[p].length;
			for (int n = 0; n < num; n++) {
				Dot dot = TheDots[p][n];
				if (dot.isSelected()) {
					dot.getCell().setSelected(true);
					numToDelete++;
				} else {
					dot.getCell().setSelected(false);
				}
			}
			// Tag to note that we should ask to save these changes at closing
			if (numToDelete > 0)
				TheWells[p].setCellsModified(true);
		}

		int num = TheWells.length;
		for (int i = 0; i < num; i++) {
			System.out.println("Processing Well: " + TheWells[i].name);
			TheWells[i].purgeSelectedCellsAndRecomputeWellMeans();
		}
		TheMainGUI.getPlateHoldingPanel().getModel().updateMinMaxValues();

		// Clean up a bit
		System.gc();

		// System.out.println("Time to Delete Dots"
		// + (System.currentTimeMillis() - time));

		return numToDelete;
	}

	//
	//
	//

	private int getFeatureIndex(String fname) {
		Feature[] fs = models.Model_Main.getModel().getFeatures();
		for (int i = 0; i < fs.length; i++) {
			// System.out.println("FeatureName: "+fs[i].toString()
			// +"  vs   "+fname);
			if (fs[i].toString().equalsIgnoreCase(fname))
				return i;
		}
		return -1;
	}

	/**
	 * Clears memory of prior dots
	 * 
	 * @author BLM
	 */
	private void killDots() {
		if (TheDots == null)
			return;
		int len = TheDots.length;
		for (int i = 0; i < len; i++) {
			int num = TheDots[i].length;
			for (int j = 0; j < num; j++)
				TheDots[i][j].kill();
		}
		System.gc();
	}

	/***
	 * Updates the current dot plot with the data from the given wells and plots
	 * the given features against each other
	 * 
	 * @author BLM
	 */
	private void updatePlot(Model_Well[] wells, String featureName_X,
			String featureName_Y) {
		UpdatePlotImage = true;

		ThePlot = this;
		ThePlot.setLayout(new BorderLayout());

		if (FeatureX == null || FeatureY == null)
			return;

		Bounds_X.Lower = Double.POSITIVE_INFINITY;
		Bounds_X.Upper = Double.NEGATIVE_INFINITY;
		Bounds_Y.Lower = Double.POSITIVE_INFINITY;
		Bounds_Y.Upper = Double.NEGATIVE_INFINITY;

		// init the cells
		if (wells != null && wells.length > 0)
		{
			// If one of the wells has no data, then we want to recompile all
			// the data
			for (int i = 0; i < wells.length; i++)
				if (wells[i].getCells() == null)
					TheDataValues = null;
			try {
				// finding which wells have cells
				numPlots = wells.length;

				// Counts how many cells total are in each well --> ex: bc of
				// multiple fields
				int counter = 0;
				if (true) {// TheDataValues == null) {

					// Clear prior data
					TheDataValues = null;
					TheCells = null;
					// Create new data
					TheDataValues = new ArrayList<float[][]>();
					TheCells = new ArrayList<ArrayList<Cell>>();
					for (int p = 0; p < numPlots; p++) {
						ArrayList<Cell> cells = wells[p].getCells();
						float[][] values = wells[p].getCell_values();
						if (cells != null && cells.size() != 0) {
							TheDataValues.add(values);
							TheCells.add(cells);
						}
					}
				}

				// Creating the dots to plot
				killDots();
				numPlots = TheDataValues.size();
				TheDots = new Dot[numPlots][];
				if (TheDataValues != null)
					for (int p = 0; p < numPlots; p++) {
						
						float[][] dat = TheDataValues.get(p);
						ArrayList<Cell> cells = TheCells.get(p);
						if (dat != null && cells != null) {
							int numCells = dat.length;
							TheDots[p] = new Dot[numCells];
							for (int i = 0; i < numCells; i++) {
								float val_X = dat[i][getFeatureIndex(featureName_X)];
								float val_Y = dat[i][getFeatureIndex(featureName_Y)];

								if (LogScaleButton_X.isSelected())
									if (val_X <= 0)
										val_X = 0;
									else
										val_X = (float) tools.MathOps
												.log(val_X);
								if (LogScaleButton_Y.isSelected())
									if (val_Y <= 0)
										val_Y = 0;
									else
										val_Y = (float) tools.MathOps
												.log(val_Y);

								// Init the dot
								TheDots[p][i] = new Dot(val_X, val_Y, cells
										.get(i));

								if (val_X > Float.NEGATIVE_INFINITY
										&& val_X < Float.POSITIVE_INFINITY
										&& val_Y > Float.NEGATIVE_INFINITY
										&& val_Y < Float.POSITIVE_INFINITY) {
									// init the bounds
									if (val_X < Bounds_X.Lower)
										Bounds_X.Lower = val_X;
									if (val_X > Bounds_X.Upper)
										Bounds_X.Upper = val_X;
									if (val_Y < Bounds_Y.Lower)
										Bounds_Y.Lower = val_Y;
									if (val_Y > Bounds_Y.Upper)
										Bounds_Y.Upper = val_Y;
								}
							}
						}
					}
				MaxValue_X = Bounds_X.Upper;
				MinValue_X = Bounds_X.Lower;
				Bounds_X.Upper = (float) MaxValue_X;
				Bounds_X.Lower = (float) MinValue_X;

				MaxValue_Y = Bounds_Y.Upper;
				MinValue_Y = Bounds_Y.Lower;
				Bounds_Y.Upper = (float) MaxValue_Y;
				Bounds_Y.Lower = (float) MinValue_Y;
			} catch (Exception e) {
				System.out.println("Error Creating DotPlot: ");
				e.printStackTrace();
			}
		}

		if (TheGraphPanel != null)
			TheGraphPanel.updatePanel();

	}

	/**
	 * Returns the wells that are being plotted
	 * 
	 * @author BLM
	 * */
	public Model_Well[] getWells() {
		return TheWells;
	}

	public void updateDimensions() {
		panel_width = ThePlot.getWidth() - 40;
		panel_height = ThePlot.getHeight();
		xStart = XMARGIN_LEFT;
		yStart = panel_height - YMARGIN - 20;

	}

	public void captureSVG(PrintWriter pw) {
		SVG_writer g2 = new SVG_writer(pw);
		g2.printHeader();
		g2.printTitle("Line Plot");

		updateDimensions();

		TheGraphPanel.draw(g2, true, false);

		g2.printEnd();
		pw.flush();
		pw.close();
	}

	public void captureImage(File file, String imageType) {
		int width = getWidth();
		int height = getHeight();

		BufferedImage im = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();

		for (int r = 0; r < width; r++)
			for (int c = 0; c < height; c++)
				im.setRGB(r, c, Color.WHITE.getRGB());

		TheGraphPanel.draw(g2, false, true);

		try {
			ImageIO.write(im, imageType, file);
		} catch (IOException e) {
			System.out.println("**Error Printing Image**");
		}
	}

	/**
	 * Returns whether the dot plot should display the colormapping density
	 * plots
	 * 
	 * @author BLM
	 */
	public boolean shouldDisplayColorMaps() {
		return displayDensityMap;
	}

	private class GraphPanel extends JPanel_highlightBox {
		private Color[] borderColors;

		public GraphPanel() {
			borderColors = new Color[3];
			borderColors[0] = new Color(0.2f, 0.2f, 0.2f);
			borderColors[1] = new Color(0.7f, 0.7f, 0.7f);
			borderColors[2] = new Color(0.9f, 0.9f, 0.9f);

			setHightlightColor(Color.gray);
			continuallyDisplayHighlightBox(true);

			validate();
			repaint();
		}

		public void draw(Graphics2D ge, boolean plotToSVG, boolean plotTOImage) {
			updateDimensions();



			if (UpdatePlotImage || plotToSVG || plotTOImage) {
				long time = System.currentTimeMillis();

				ThePlotImage = new BufferedImage(getWidth(), getHeight(),
						BufferedImage.TYPE_INT_RGB);
				int width = ThePlotImage.getWidth();
				int height = ThePlotImage.getHeight();

				for (int r = 0; r < width; r++)
					for (int c = 0; c < height; c++)
						ThePlotImage.setRGB(r, c, ColorRama.Gray_veryLight1
								.getRGB());

				Graphics2D g2 = (Graphics2D) ThePlotImage.getGraphics();

				if (plotToSVG)
					g2 = ge;

				g2.setFont(StandardFont);
				axisLength_X = (int) (panel_width - XMARGIN_RIGHT - xStart);
				axisLength_Y = yStart - 20;

				// Normal Dot Displays
				if (!Button_slopeDisplay.isSelected()) {
					// drawing border
					for (int i = 0; i < 4; i++) {
						g2.setColor(colors[i]);
						g2
								.drawRect(XMARGIN_LEFT - i,
										(yStart - axisLength_Y) - i,
										axisLength_X + 2 * i, axisLength_Y + 2
												* i);
					}

					g2.setColor(Color.white);
					g2.fillRect(XMARGIN_LEFT + 1, (yStart - axisLength_Y) + 1,
							axisLength_X - 1, axisLength_Y - 1);

					// drawing gridlines
					if (PlotType != CONTOUR) {
						g2.setColor(Gray_1);
						int numLines = 10;
						for (int i = 0; i < numLines; i++)
							if (i % 2 != 0)
							g2
									.drawLine(
											XMARGIN_LEFT + 1,
											(int) (yStart - ((float) i
													/ (float) numLines * axisLength_Y)),
											(int) xStart + axisLength_X - 1,
											(int) (yStart - ((float) i
													/ (float) numLines * axisLength_Y)));
					}
					if (TheWells != null && TheWells.length > 0)
						if (PlotType == SIDEBYSIDE)
							drawSideBySide(g2, plotToSVG);
						else if (PlotType == CONTOUR)
							drawContour(g2, plotToSVG);
						else if (PlotType == OVERLAY)
							drawOverLay(g2, plotToSVG);
				}

				drawYAxisBar(g2);
				drawXAxisBar(g2);

				UpdatePlotImage = false;

				// System.out.println("Time to Plot"
				// + (System.currentTimeMillis() - time));

			}

			if (!plotToSVG)
				ge.drawImage(ThePlotImage, 0, 0, null);

			// Drawing the SidebySide Freestyle lines
			if (PlotType == SIDEBYSIDE && numPlots > 0) {
				if (ThePolygonGate_Points != null) {
					ge.setColor(Color.lightGray);
					int len = ThePolygonGate_Points.size();
					if (len > 0) {
						Point p = ThePolygonGate_Points.get(0);
						for (int i = 1; i < len; i++) {
							Point pLast = p;
							p = ThePolygonGate_Points.get(i);
							ge.drawLine(pLast.x, pLast.y, p.x, p.y);
						}
					}
				} else if (ThePolygonGate != null) {
					ge.setColor(Color.lightGray);
					if (ThePolygonGate.xpoints.length == 1)
						ge.drawLine(ThePolygonGate.xpoints[0],
								ThePolygonGate.xpoints[0],
								ThePolygonGate.ypoints[0],
								ThePolygonGate.ypoints[0]);
					else {
						ge.setColor(Color.DARK_GRAY);
						ge.drawPolygon(ThePolygonGate);
						ge.setColor(Color.black);
					}
				}

				int numPlots = 1;
				if (TheDots != null)
					numPlots = TheDots.length;
				double plotBufferX = 0;
				double axisLenX = axisLength_X / numPlots - plotBufferX;


				// Drawing all gates
				int num = TheWells.length;
				int counter = 0;
				for (int n = 0; n < num; n++) {
					Model_Well well = TheWells[n];
					int len = well.TheGates.size();
					ArrayList<Cell> cells = well.getCells();
					if (cells != null && cells.size() > 0) {
						for (int i = 0; i < len; i++) {
							Gate_DotPlot b = ((Gate_DotPlot) well.TheGates
									.get(i));
							if (FeatureX.isSameFeature(b.featureX)
									&& FeatureY.isSameFeature(b.featureY)) {
								ge.setColor(b.getColor());
								Polygon poly = b.getPolygonGate_toDraw((xStart
										+ axisLenX * counter + plotBufferX),
										yStart, Bounds_X.Lower, Bounds_X.Upper,
										Bounds_Y.Lower, Bounds_Y.Upper,
										axisLenX, axisLength_Y,
										LogScaleButton_X.isSelected(),
										LogScaleButton_Y.isSelected());
								ge.drawPolygon(poly);

								// Drawing the fraction selected by this gate
								if (cells != null) {
									float fraction = b
											.getFractionOfCellsBound(well
													.getCell_values());
									ge.setColor(Color.DARK_GRAY);
									ge.fillRect(poly.xpoints[0] - 5,
											poly.ypoints[0] - 14, 44, 18);
									ge.setColor(Color.black);
									ge.drawRect(poly.xpoints[0] - 5,
											poly.ypoints[0] - 14, 44, 18);
									ge.setColor(Color.white);
									if (fraction < 0.01f)
										ge.drawString((nf
												.format(fraction * 100d))
												+ "%", poly.xpoints[0],
												poly.ypoints[0]);
									else
										ge.drawString((nf2
												.format(fraction * 100d))
												+ "%", poly.xpoints[0],
												poly.ypoints[0]);
								}

							}
						}
						counter++;
					}
				}

				// Drawing the DotFilters if exist
				if (TheDotFilters != null && TheDotFilters.size() > 0)
					for (int j = 0; j < TheDotFilters.size(); j++) {
						TheDotFilters.get(j).draw(ge);
					}

			}
			super.paintHighlighting(ge);
			validate();


		}

		/**
		 * Method paintComponent
		 * 
		 * @author BLM
		 * 
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			draw(g2, false, false);
		}

		private void drawXAxisBar(Graphics2D g2) {
			int xRange = axisLength_X;

			if ((PlotType == SIDEBYSIDE || PlotType == CONTOUR) && numPlots > 1)
				xRange = xRange / numPlots;

			// Drawing the yAxisLabelBox
			g2.setColor(Color.lightGray);
			xAxisLabelBox.x = XMARGIN_LEFT;
			xAxisLabelBox.y = yStart + 3;
			xAxisLabelBox.width = xRange;
			xAxisLabelBox.height = 20;

			// Drawing the axis ticks and labels:
			if (PlotType == SIDEBYSIDE || PlotType == CONTOUR)
				for (int p = 0; p < numPlots; p++) {
					if (numPlots > 1) {
						GradientPaint whiteToGray = new GradientPaint(
								xAxisLabelBox.x, yStart, Color.white,
								xAxisLabelBox.x + xRange, yStart, Color.gray);
						g2.setPaint(whiteToGray);
						g2.fillRect(xAxisLabelBox.x, xAxisLabelBox.y,
								xAxisLabelBox.width, xAxisLabelBox.height);
						g2.setColor(Color.lightGray);
						g2.drawRect(xAxisLabelBox.x, xAxisLabelBox.y,
								xAxisLabelBox.width, xAxisLabelBox.height);
					}

					if (p > 0) {
						g2.setColor(tools.ColorRama.Gray_veryLight1);
						g2.drawLine(xAxisLabelBox.x - 3, yStart,
								xAxisLabelBox.x - 3, yStart - axisLength_Y + 1);
						g2.setColor(tools.ColorRama.Gray_veryLight2);
						g2.drawLine(xAxisLabelBox.x - 2, yStart,
								xAxisLabelBox.x - 2, yStart - axisLength_Y + 1);
						g2.setColor(tools.ColorRama.Gray_veryLight1);
						g2.drawLine(xAxisLabelBox.x - 1, yStart,
								xAxisLabelBox.x - 1, yStart - axisLength_Y + 1);
					}

					// X-axis
					g2.setColor(Color.DARK_GRAY);
					int numTicks = 10;
					int tickLen = 4;
					// ticks
					for (int i = 0; i < numTicks; i++)
						g2.drawLine((int) (xAxisLabelBox.x + ((float) i
								/ (float) numTicks * xRange)), yStart + 1,
								(int) (xAxisLabelBox.x + ((float) i
										/ (float) numTicks * xRange)), yStart
										+ 1 + tickLen);

					// labels
					if (numPlots < 5)
					if (Bounds_Y.Upper != 0 && MaxValue_Y != 0) {
						g2.setFont(SmallFont);
						for (int i = 0; i < numTicks; i++)
							if ((i + 1) % 2 == 0 && i != numTicks) // odd ticks,
																	// but not
																	// top
							{
								if (Bounds_Y.Upper != Double.NaN
										&& Bounds_Y.Lower != Double.NaN
										&& (Bounds_Y.Lower != Bounds_Y.Upper)) {
									if (Bounds_Y.Upper < 10) // float display
									{
										float val = (float) i
												/ (float) numTicks
												* ((float) Bounds_X.Upper - (float) Bounds_X.Lower)
												+ (float) Bounds_X.Lower;
										g2
												.drawString(
														"" + nf.format(val),
														(int) (xAxisLabelBox.x + ((float) i
																/ (float) numTicks * xRange)),
														(yStart + tickLen + 10));
									} else // integer display
									{
										int val = (int) ((float) i
												/ (float) numTicks
												* (Bounds_X.Upper - Bounds_X.Lower) + Bounds_X.Lower);
										if (val < 0)
											g2.setColor(Color.red);
										g2
												.drawString(
														"" + nf.format(val),
														(int) (xAxisLabelBox.x + ((float) i
																/ (float) numTicks * xRange)),
														(yStart + tickLen + 17));
										g2.setColor(Color.black);
									}
								}
							}
					}

					xAxisLabelBox.x += xRange;
				}
			else {
				// X-axis
				g2.setColor(Color.DARK_GRAY);
				int numTicks = 10;
				int tickLen = 4;
				// ticks
				for (int i = 0; i < numTicks; i++)
					g2.drawLine((int) (xAxisLabelBox.x + ((float) i
							/ (float) numTicks * xRange)), yStart + 1,
							(int) (xAxisLabelBox.x + ((float) i
									/ (float) numTicks * xRange)), yStart + 1
									+ tickLen);

				// labels
				if (Bounds_Y.Upper != 0 && MaxValue_Y != 0) {
					g2.setFont(SmallFont);
					for (int i = 0; i < numTicks; i++)
						if ((i + 1) % 2 == 0 && i != numTicks) // odd ticks, but
																// not top
						{
							if (Bounds_Y.Upper != Double.NaN
									&& Bounds_Y.Lower != Double.NaN
									&& (Bounds_Y.Lower != Bounds_Y.Upper)) {
								if (Bounds_Y.Upper < 10) // float display
								{
									float val = (float) i
											/ (float) numTicks
											* ((float) Bounds_X.Upper - (float) Bounds_X.Lower)
											+ (float) Bounds_X.Lower;
									g2
											.drawString(
													"" + nf.format(val),
													(int) (xAxisLabelBox.x + ((float) i
															/ (float) numTicks * xRange)),
													(yStart + tickLen + 10));
								} else // integer display
								{
									int val = (int) ((float) i
											/ (float) numTicks
											* (Bounds_X.Upper - Bounds_X.Lower) + Bounds_X.Lower);
									if (val < 0)

										g2.setColor(Color.red);
									g2
											.drawString(
													"" + nf.format(val),
													(int) (xAxisLabelBox.x + ((float) i
															/ (float) numTicks * xRange)),
													(yStart + tickLen + 17));
									g2.setColor(Color.black);
								}
							}
						}
				}
			}
			// Make sure the xAxisBox spans the whole bottom of the graph so
			// clicking works anywhere
			xAxisLabelBox.x = XMARGIN_LEFT;
			xAxisLabelBox.y = yStart + 3;
			xAxisLabelBox.width = axisLength_X;
			xAxisLabelBox.height = 20;

		}

		private void drawYAxisBar(Graphics2D g2) {
			int xRange = axisLength_X;

			if ((PlotType == SIDEBYSIDE || PlotType == CONTOUR) && numPlots > 1)
				xRange = xRange / numPlots;
			axisLength_Y = yStart - 20;

			int yRange = axisLength_Y;
			// Drawing the yAxisLabelBox
			g2.setColor(Color.lightGray);
			yAxisLabelBox.x = XMARGIN_LEFT;
			yAxisLabelBox.y = yStart - yRange + 1;
			yAxisLabelBox.width = 45;
			yAxisLabelBox.height = yRange - 1;

			// Drawing the axis ticks and labels:
			//

			// Y-axis
			g2.setColor(Color.DARK_GRAY);
			int numTicks = 10;
			int tickLen = 4;

			for (int p = 0; p < numPlots; p++) {

				// ticks
				for (int i = 0; i < numTicks; i++)
					g2
							.drawLine(yAxisLabelBox.x - 1,
									(int) (yStart - ((float) i
											/ (float) numTicks * yRange)),
									(yAxisLabelBox.x - 1 - tickLen),
									(int) (yStart - ((float) i
											/ (float) numTicks * yRange)));
				// labels
				if (Bounds_Y.Upper != 0 && MaxValue_Y != 0) {
					g2.setFont(SmallFont);
					for (int i = 0; i < numTicks; i++)
						if ((i + 1) % 2 == 0 && i != numTicks) // odd ticks, but
																// not top
						{
							// Only putting it on the first graph
							if (p == 0)
							if (Bounds_Y.Upper != Double.NaN
									&& Bounds_Y.Lower != Double.NaN
									&& (Bounds_Y.Lower != Bounds_Y.Upper)) {
								if (Bounds_Y.Upper < 10) // float display
								{
									float val = (float) i
											/ (float) numTicks
											* ((float) Bounds_Y.Upper - (float) Bounds_Y.Lower)
											+ (float) Bounds_Y.Lower;
									g2
											.drawString(
													"" + nf.format(val),
													(yAxisLabelBox.x - tickLen - 26),
													(int) (yStart
															- ((float) i
																	/ (float) numTicks * yRange) + 3));
								} else // integer display
								{
									int val = (int) ((float) i
											/ (float) numTicks
											* (Bounds_Y.Upper - Bounds_Y.Lower) + Bounds_Y.Lower);
									if (val < 0)

										g2.setColor(Color.red);
									g2
											.drawString(
													"" + nf.format(val),
													(yAxisLabelBox.x - tickLen - 28),
													(int) (yStart
															- ((float) i
																	/ (float) numTicks * yRange) + 3));
									g2.setColor(Color.black);
								}
							}
						}
				}
				yAxisLabelBox.x += xRange;
			}

			yAxisLabelBox.x = XMARGIN_LEFT - 45;
			yAxisLabelBox.y = yStart - yRange + 1;
			yAxisLabelBox.width = 45;
			yAxisLabelBox.height = yRange - 1;
		}

		private void drawSideBySide(Graphics2D g2, boolean plotToSVG) {
			NumDots_Total = 0;
			NumDots_Selected = 0;
			if (TheDots != null && TheDots.length > 0) {
				// drawing the dots
				int numPlots = TheDots.length;
				double plotBufferX = 0;
				double axisLenX = axisLength_X / numPlots - plotBufferX;

				//
				// Now drawing all the well dots
				//

				float progressIncrement = 1f / (float) numPlots;
				double[] lastCentroid = null;
				for (int p = 0; p < numPlots; p++) {


					Dot[] theseDots = TheDots[p];

					double[] centroid = { 0, 0 };
					int numValidDots = 0;
					int numD = theseDots.length;


					DensityScatter densityMap = null;
					if (displayDensityMap) {
						//
						// Computing the density map
						//
						float[] xVals = new float[numD];
						float[] yVals = new float[numD];
						for (int i = 0; i < numD; i++) {
							xVals[i] = TheDots[p][i].point.x;
							yVals[i] = TheDots[p][i].point.y;
						}
						densityMap = new DensityScatter(xVals, yVals, 50);
						TheDensitySorter.densityScatter = densityMap;
						Arrays.sort(TheDots[p], TheDensitySorter);
					}



					// Drawing how many dots there are
					int xBuffer = (int) ((xStart + axisLenX * p + plotBufferX));
					g2.setColor(Color.gray);
					g2.setFont(SmallFont);
					g2.drawString("# Cells: ", xBuffer + 8, yStart
							- axisLength_Y + 13);
					g2.drawString("" + numD, xBuffer + 9, yStart - axisLength_Y
							+ 23);

					//
					// Drawing the dots
					//

					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					Composite com = g2.getComposite();
					g2.setComposite(transComposite);

					for (int i = 0; i < numD; i++) {

						if (TheDots[p][i].point.x >= Bounds_X.Lower
								&& TheDots[p][i].point.x <= Bounds_X.Upper
								&& TheDots[p][i].point.y >= Bounds_Y.Lower
								&& TheDots[p][i].point.y <= Bounds_Y.Upper) {
							TheDots[p][i].box.x = (int) ((xStart + axisLenX * p + plotBufferX) + (TheDots[p][i].point.x - Bounds_X.Lower)
									/ (Bounds_X.Upper - Bounds_X.Lower)
									* axisLenX);
							TheDots[p][i].box.y = (int) (yStart - (TheDots[p][i].point.y - Bounds_Y.Lower)
									/ (Bounds_Y.Upper - Bounds_Y.Lower)
									* axisLength_Y);

							centroid[0] += TheDots[p][i].point.x;
							centroid[1] += TheDots[p][i].point.y;
							numValidDots++;

							TheDots[p][i].box.height = 7;// 3+7/(1+numPlots);
							TheDots[p][i].box.width = TheDots[p][i].box.height;
							;

							// System.out.println("Percent: "+percentToPlot);
							if (Math.random() < percentToPlot) {

								if (displayDensityMap)
									g2.setColor(densityMap.getColor(
											TheDots[p][i].point.x,
											TheDots[p][i].point.y));
								else
									g2.setColor(Color.black);

								int dotDiameter = TheDots[p][i].box.width;
								if (plotToSVG)
									dotDiameter = 2;

								g2.fillOval(TheDots[p][i].box.x - dotDiameter
										/ 2, TheDots[p][i].box.y - dotDiameter
										/ 2, dotDiameter, dotDiameter);


							}
						}
						// tallying all dots and selected dots to get a
						// percentage selected
						NumDots_Total++;
						if (TheDots[p][i].isSelected())
							NumDots_Selected++;

					}
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_OFF);
					g2.setComposite(com);


					//
					// Computing the centroid and drawing it
					//
					centroid[0] = ((float) centroid[0] / (float) numValidDots);
					centroid[1] = ((float) centroid[1] / (float) numValidDots);
					// Transforming the centroids to the display coordinate
					// system
					double[] centroid_display = new double[2];
					centroid_display[0] = (int) ((xStart + axisLenX * p + plotBufferX) + (centroid[0] - Bounds_X.Lower)
							/ (Bounds_X.Upper - Bounds_X.Lower) * axisLenX);
					centroid_display[1] = (int) (yStart - (centroid[1] - Bounds_Y.Lower)
							/ (Bounds_Y.Upper - Bounds_Y.Lower) * axisLength_Y);
					// draw the centroid of the distribution
					int dotDiameter = 8;
					if (plotToSVG)
						dotDiameter = 4;

					if (!(centroid_display[0] == 0 && centroid_display[0] == 0)) {
						g2.setColor(Color.green);
						g2.fillOval((int) centroid_display[0] - 4,
								(int) centroid_display[1] - 4, dotDiameter,
								dotDiameter);
						g2.setColor(Color.black);
						g2.drawOval((int) centroid_display[0] - 4,
								(int) centroid_display[1] - 4, dotDiameter,
								dotDiameter);
					}

					if (lastCentroid == null)
						lastCentroid = centroid_display;
					else {
						if (!(lastCentroid[0] == 0 && lastCentroid[0] == 0)
								&& !(centroid_display[0] == 0 && centroid_display[0] == 0)) {
							g2.setColor(Color.green);
							g2.drawLine((int) lastCentroid[0],
									(int) lastCentroid[1],
									(int) centroid_display[0],
									(int) centroid_display[1]);
						}
						lastCentroid = centroid_display;
					}


				}



				g2.setColor(Color.green);
			}
		}

		private void drawContour(Graphics2D g2, boolean plotToSVG) {

			NumDots_Total = 0;
			NumDots_Selected = 0;
			if (TheDots != null && TheDots.length > 0) {
				// drawing the dots
				int numPlots = TheDots.length;
				double axisLenX = axisLength_X / numPlots;

				for (int p = 0; p < numPlots; p++) {
					Dot[] theseDots = TheDots[p];
					int numD = theseDots.length;
					//
					// Computing the density map
					//
					// NOTE: adding 2 points to each density plot for min/max of
					// all the plots so we can compute the densities relative to
					// other plots also
					float[] xVals = new float[numD + 2];
					float[] yVals = new float[numD + 2];
					for (int i = 0; i < numD; i++) {
						xVals[i] = TheDots[p][i].point.x;
						yVals[i] = TheDots[p][i].point.y;
					}
					xVals[numD] = (float) MinValue_X;
					xVals[numD + 1] = (float) MaxValue_X;
					yVals[numD] = (float) MinValue_Y;
					yVals[numD + 1] = (float) MaxValue_Y;

					DensityScatter densityMap = new DensityScatter(xVals,
							yVals, 20);
					int xStart2 = (int) (xStart + axisLenX * p);
					ContourPlot cp = new ContourPlot(densityMap
							.getAllDensityValues());
					cp.DrawTheContourPlot(g2, (int) xStart2, (int) yStart,
							(int) axisLenX, axisLength_Y);

					// Draw the Legend only for the first plot
					// if (p==0)
					// DrawColorLegend(g2, (int)xStart, yStart, cp);
				}

			}
		}

		/**
		 * Draws the ColorMap legend for the contour plot
		 * 
		 * @author BLM
		 */
		private void DrawColorLegend(Graphics2D g, int xstart, int ystart,
				ContourPlot cp) {
			int width = 20;
			int height = 100;
			int xs = (int) xstart + 20;
			int ys = ystart - 20;
			int numC = cp.N_CONTOURS;
			int dy = height / numC;

			// Drawing the color swabs
			for (int i = 0; i < numC; i++) {
				cp.SetColour(g, i);
				g.fillRect(xs, ys - i * dy, width, dy);
			}
			g.setColor(Color.black);
			g.drawRect(xs, ys, width, height);
			// Drawing labels
			g.drawString(cp.zMin + "", xs - 5, ys - 0 * dy);
			g.drawString(((cp.zMax) / (cp.zMax - cp.zMin)) + "", xs - 5, ys
					- cp.N_CONTOURS / 2f * dy);
			g.drawString(cp.zMax + "", xs - 5, ys - cp.N_CONTOURS * dy);

		}

		private void drawOverLay(Graphics2D g2, boolean plotToSVG) {
			NumDots_Total = 0;
			NumDots_Selected = 0;
			if (TheDots != null && TheDots.length > 0) {
				// drawing the dots
				int numPlots = TheDots.length;
				double plotBufferX = 0;
				double[] lastCentroid = null;

				for (int p = 0; p < numPlots; p++) {
					Dot[] theseDots = TheDots[p];

					double[] centroid = { 0, 0 };
					int numValidDots = 0;
					int numD = theseDots.length;
					for (int i = 0; i < numD; i++) {
						if (TheDots[p][i].point.x >= Bounds_X.Lower
								&& TheDots[p][i].point.x <= Bounds_X.Upper
								&& TheDots[p][i].point.y >= Bounds_Y.Lower
								&& TheDots[p][i].point.y <= Bounds_Y.Upper) {
							TheDots[p][i].box.x = (int) ((xStart + plotBufferX) + (TheDots[p][i].point.x - Bounds_X.Lower)
									/ (Bounds_X.Upper - Bounds_X.Lower)
									* axisLength_X);
							TheDots[p][i].box.y = (int) (yStart - (TheDots[p][i].point.y - Bounds_Y.Lower)
									/ (Bounds_Y.Upper - Bounds_Y.Lower)
									* axisLength_Y);

							centroid[0] += TheDots[p][i].box.x;
							centroid[1] += TheDots[p][i].box.y;
							numValidDots++;

							TheDots[p][i].box.height = 6;
							TheDots[p][i].box.width = 6;

							// Composite comp = g2.getComposite();
							// g2.setColor(TheDots[p][i].getColor(p));
							// g2.setComposite(transComposite);
							// g2.fill(TheDots[p][i].box);
							// g2.setComposite(comp);

							int dotDiameter = TheDots[p][i].box.width;
							if (plotToSVG)
								dotDiameter = 2;

							Composite comp = g2.getComposite();
							g2.setColor(Color.black);
							g2.setComposite(transComposite);
							g2.fillOval(TheDots[p][i].box.x,
									TheDots[p][i].box.y, dotDiameter,
									dotDiameter);
							g2.setComposite(comp);
						}

						// tallying all dots and selected dots to get a
						// percentage selected
						NumDots_Total++;
						if (TheDots[p][i].isSelected())
							NumDots_Selected++;
					}

					// computing the centroid
					centroid[0] = (int) ((float) centroid[0] / (float) numValidDots);
					centroid[1] = (int) ((float) centroid[1] / (float) numValidDots);

					int dotDiameter = 8;
					if (plotToSVG)
						dotDiameter = 4;

					g2.setColor(Color.green);
					// draw the centroid of the distribution
					g2.fillOval((int) centroid[0], (int) centroid[1],
							dotDiameter, dotDiameter);
					g2.setColor(Color.black);
					g2.drawOval((int) centroid[0], (int) centroid[1],
							dotDiameter, dotDiameter);
					// Drawing label for distribution

					g2.drawString("" + p, (int) centroid[0] - 5,
							(int) centroid[1] - 1);

					if (lastCentroid == null)
						lastCentroid = centroid;
					else {
						g2.setColor(Color.green);
						g2.drawLine((int) lastCentroid[0] + 4,
								(int) lastCentroid[1] + 4,
								(int) centroid[0] + 4, (int) centroid[1] + 4);
						lastCentroid = centroid;
					}

				}

				g2.setColor(Color.green);
				int num = TheWells.length;
				for (int n = 0; n < num; n++) {
					Model_Well well = TheWells[n];
					int len = well.TheGates.size();

					for (int i = 0; i < len; i++) {
						Gate_DotPlot b = well.TheGates.get(i);
						if (FeatureX.isSameFeature(b.featureX)
								&& FeatureY.isSameFeature(b.featureY)) {
							g2.setColor(b.getColor());
							g2.draw(b.getPolygonGate_toDraw(xStart, yStart,
									Bounds_X.Lower, Bounds_X.Upper,
									Bounds_Y.Lower, Bounds_Y.Upper,
									axisLength_X, axisLength_Y,
									LogScaleButton_X.isSelected(),
									LogScaleButton_Y.isSelected()));
						}
					}
				}

				// Drawing Gates, if applicable

				// int num = TheWells.length;
				// for (int n = 0; n < num; n++)
				// {
				// Model_Well well = TheWells[n];
				// int len = well.TheGates.size();
				// for (int i = 0; i < len; i++)
				// {
				// Gate_DotPlot b = ((Gate_DotPlot)well.TheGates.get(i));
				// if (FeatureX.isSameFeature(b.featureX) &&
				// FeatureY.isSameFeature(b.featureY))
				// {
				// if (!LogScaleCheckBox.isSelected())
				// {
				// b.box_toDraw.x =
				// (int)(xStart+(b.bounds[0][0]-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX);
				// b.box_toDraw.y = (int)(yStart -
				// (b.bounds[1][1]-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y);
				// b.box_toDraw.width =
				// (int)(xStart+(b.bounds[0][1]-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX)-b.box_toDraw.x;
				// b.box_toDraw.height =
				// (int)(yStart-(b.bounds[1][0]-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y)-b.box_toDraw.y;
				// }
				// else
				// {
				// b.box_toDraw.x =
				// (int)((xStart)+(Math.log(b.bounds[0][0])-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX);
				// b.box_toDraw.y = (int)(yStart -
				// (Math.log(b.bounds[1][1])-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y);
				// b.box_toDraw.width =
				// (int)((xStart)+(Math.log(b.bounds[0][1])-LowerBounds_X)/(UpperBounds_X-LowerBounds_X)*axisLenX)-b.box_toDraw.x;
				// b.box_toDraw.height =
				// (int)(yStart-(Math.log(b.bounds[1][0])-LowerBounds_Y)/(UpperBounds_Y-LowerBounds_Y)*axisLength_Y)-b.box_toDraw.y;
				// }
				//
				// g2.setColor(b.getColor());
				// g2.draw(b.box_toDraw);
				// }
				// }
				// }

			}
			if (ThePolygonGate_Points != null) {
				int len = ThePolygonGate_Points.size();
				if (len > 0) {
					Point p = ThePolygonGate_Points.get(0);
					for (int i = 1; i < len; i++) {
						Point pLast = p;
						p = ThePolygonGate_Points.get(i);
						g2.drawLine(pLast.x, pLast.y, p.x, p.y);
					}
				}
			} else if (ThePolygonGate != null) {
				g2.setColor(Color.lightGray);
				if (ThePolygonGate.xpoints.length == 1)
					g2.drawLine(ThePolygonGate.xpoints[0],
							ThePolygonGate.xpoints[0],
							ThePolygonGate.ypoints[0],
							ThePolygonGate.ypoints[0]);
				else {
					g2.setColor(Color.DARK_GRAY);
					g2.draw(ThePolygonGate);
					g2.setColor(Color.black);
				}
			}
		}

		private double getScalingIncrement_Y(double fraction) {
			// returns 1% of the range
			double val = MaxValue_Y - MinValue_Y;
			return (fraction * val);
		}

		private double getScalingIncrement_X(double fraction) {
			// returns 1% of the range
			double val = MaxValue_X - MinValue_X;
			return (fraction * val);
		}

		public void mousePressed(MouseEvent p1) {
			MousePressed = true;

			// checking to see if we pressed inside the yScaleBar
			if (yAxisLabelBox.contains(p1.getPoint())) {
				// At bottom of rectangle
				if (p1.getPoint().y < (yAxisLabelBox.y + yAxisLabelBox.height / 2f))
					Type_AxisScale = 1;
				// At top of rectangle
				else if (p1.getPoint().y >= (yAxisLabelBox.y + yAxisLabelBox.height / 2f))
					Type_AxisScale = -1;
			} else
				Type_AxisScale = 0;

			if (PolygonGate) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				ThePolygonGate_Points = new ArrayList<Point>();
				ThePolygonGate_Points.add(p1.getPoint());
			}

			if (TheDotSelectionListener != null)
				TheDotSelectionListener.resetAllDots();

			if (highlightBox != null && highlightBox.contains(p1.getPoint())) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				dX = p1.getX() - highlightBox.x;
				dY = p1.getY() - highlightBox.y;
				CreateNewBox = false;
			} else {
				startBox_XY = p1.getPoint();
				startHighlightPoint = p1.getPoint();
				CreateNewBox = true;
			}

			// Marking with Plot # we pressed inside of if there are more than
			// one
			if (numPlots > 0) {
				double axisLenX = axisLength_X / numPlots;
				PlotPressedIn = (int) ((p1.getPoint().x - xStart) / axisLenX);
			}
			UpdatePlotImage = false;
			updatePanel();

		}

		public void mouseReleased(MouseEvent p1) {
			MousePressed = false;

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (ThePolygonGate_Points != null) {
				ThePolygonGate = new Polygon();
				for (int i = 0; i < ThePolygonGate_Points.size(); i++) {
					Point p = ThePolygonGate_Points.get(i);
					ThePolygonGate.addPoint(p.x, p.y);
				}
				if (HighlightInOutButton.isSelected())
					selectDotsPolygonGate_out();
				else
					selectDotsPolygonGate_in();
				ThePolygonGate_Points = null;
			}

			if (AddFilter) {
				Point p = startHighlightPoint;
				int yval = p1.getPoint().y - startHighlightPoint.y;
				if (Math.abs(yval) > 10) {
					// Computing the pivot point
					float y = p.y;
					y = (float) ((Bounds_Y.Lower + (yStart - y)
							* (Bounds_Y.Upper - Bounds_Y.Lower) / axisLength_Y));
					if (LogScaleButton_Y.isSelected())
						y = (float) tools.MathOps.exp(y);

					if (yval > 0)
						TheDotFilters.add(new DotFilter(ThePlot, FeatureY, y,
								-1, p));
					else
						TheDotFilters.add(new DotFilter(ThePlot, FeatureY,
								y, 1, p));
				}
			}

			// reseting the highlightbox
			if (!continuallyDisplayBox) {
				highlightBox = null;
				startHighlightPoint = null;
			}

			UpdatePlotImage = false;
			repaint();
		}

		public void mouseMoved(MouseEvent p1) {
			if (AddFilter && TheDotFilters != null && TheDotFilters.size() > 0) {
				Point p = p1.getPoint();
				for (int i = 0; i < TheDotFilters.size(); i++) {
					if (TheDotFilters.get(i).mouseOver(p))
						break;
				}
				TheGraphPanel.repaint();
			}
		}

		// Over-riding
		public void mouseDragged(MouseEvent p1) {
			if (startHighlightPoint == null || p1 == null)
				return;

			int yval1 = p1.getPoint().y - startHighlightPoint.y;
			if (Type_AxisScale == 1) {
				double dV = getScalingIncrement_Y(0.01d);
				Bounds_Y.Upper += dV * yval1;
				startHighlightPoint = p1.getPoint();
			} else if (Type_AxisScale == -1) {
				double dV = getScalingIncrement_Y(0.01d);
				Bounds_Y.Lower += dV * yval1;
				startHighlightPoint = p1.getPoint();
			}

			else if (!AddFilter) {
				if (PolygonGate && ThePolygonGate_Points != null
						&& ThePolygonGate_Points.size() > 0) {
					int len = ThePolygonGate_Points.size();
					Point p = ThePolygonGate_Points.get(len - 1);
					if (p1.getPoint().distance(p) > 10) {
						ThePolygonGate_Points.add(p1.getPoint());
					}
				} else if (CreateNewBox || highlightBox == null) {
					if (p1 == null || startHighlightPoint == null)
						return;
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
					highlightBox.x = p1.getX() - dX;
					highlightBox.y = p1.getY() - dY;
				}

				// seeing if box has highlighted any cells
				if (!PolygonGate && TheDots != null) {
					int num = TheDots.length;
					for (int p = 0; p < num; p++) {
						int len = TheDots[p].length;
						for (int i = 0; i < len; i++)
							if (highlightBox.contains(TheDots[p][i].box))
								TheDots[p][i].setSelected(true);
							else if (!p1.isShiftDown())
								TheDots[p][i].setSelected(false);
					}
				}
			}

			TheDotSelectionListener.updateListenerImages();
			UpdatePlotImage = false;

			repaint();

			// Periodically invoking garbage collector
			if (Math.random() > 0.99)
				System.gc();

		}

		public void mouseClicked(MouseEvent p1) {

			if (p1.getClickCount() >= 2) {
				AxisBoundsInputDialog s = null;
				if (yAxisLabelBox.contains(p1.getPoint()))
					s = new AxisBoundsInputDialog(Bounds_Y, TheMainGUI);
				else if (xAxisLabelBox.contains(p1.getPoint()))
					s = new AxisBoundsInputDialog(Bounds_X, TheMainGUI);
			}
			updatePanel();

			if (TheDots == null || numPlots == 0)
				return;
			// Seeing if we want a polygon gate
			highlightBox = null;
			startHighlightPoint = null;
			if (TheDots == null) {
				updatePanel();
				return;
			}
			int len = TheDots.length;
			for (int p = 0; p < numPlots; p++)
 {
				int numD = TheDots[p].length;
				for (int i = 0; i < numD; i++)
					if (TheDots[p][i] != null)
						TheDots[p][i].setSelected(false);
			}

			// Seeing if clicked on a DotFilter
			if (TheDotFilters != null && TheDotFilters.size() > 0) {
				Point p = p1.getPoint();
				for (int i = 0; i < TheDotFilters.size(); i++) {
					DotFilter df = TheDotFilters.get(i);
					if (df.shouldClose(p)) {
						df.kill();
						TheDotFilters.remove(i);
						break;
					} else if (df.shouldExecute_selectedWells(p)) {
						df.execute_selectedWells();
						df.kill();
						TheDotFilters.remove(i);
						break;
					} else if (df.shouldExecute_allWells(p)) {
						df.execute_allWells();
						df.kill();
						TheDotFilters.remove(i);
						break;
					}
					else if (df.shouldExecute_allHDF(p)) {
						try {
							df.execute_allHDF();
							df.kill();
							TheDotFilters.remove(i);
						} catch (H5IO_Exception e) {
							System.out.println("***ERROR filtering Dots***");
							e.printStackTrace();
						}

						break;
					}
					else if (df.shouldAddToFilterQueue(p)) {
						if (models.Model_Main.getModel().getFilterQueue() == null)
							return;
						models.Model_Main.getModel().getFilterQueue()
								.addFilter(df);
						TheDotFilters.remove(i);
						break;
					}
				}



			}

			if (p1.isShiftDown()) // if only one click, then only search for one
									// gate, otherwise get the whole family of
									// same gates
			{
				// Checking the Gates
				int num = TheWells.length;
				boolean foundOne = false;
				for (int n = 0; n < num; n++) {
					int axisLenXK = 0;
					int xStartK = 0;
					int plotBufferX = 4;
					if (PlotType == SIDEBYSIDE) {
						axisLenXK = axisLength_X / numPlots - plotBufferX;
						xStartK = (int) xStart + axisLenXK * n + plotBufferX;
					} else {
						axisLenXK = axisLength_X;
						xStartK = (int) xStart;
					}

					Model_Well well = TheWells[n];
					len = well.TheGates.size();

					for (int i = 0; i < len; i++) {

						Gate_DotPlot g = (Gate_DotPlot) well.TheGates.get(i);
						if (FeatureX.isSameFeature(g.featureX)
								&& FeatureY.isSameFeature(g.featureY)) {
							if (g.getPolygonGate_toDraw(xStartK, yStart,
									Bounds_X.Lower, Bounds_X.Upper,
									Bounds_Y.Lower, Bounds_Y.Upper, axisLenXK,
									axisLength_Y,
									LogScaleButton_X.isSelected(),
									LogScaleButton_Y.isSelected()).contains(
									p1.getPoint())) {
								g.selected = !g.selected;
								foundOne = true;
							}
						}
					}
					if (foundOne)
						break;
				}
			} else {
				// Finding the gate that was clicked on... then find all other
				// well gates with same ID and toggle their gate
				int num = TheWells.length;
				boolean foundOne = false;
				boolean selected = false;
				int id = -1;
				for (int n = 0; n < num; n++) {
					int axisLenXK = 0;
					int xStartK = 0;
					int plotBufferX = 4;
					if (PlotType == SIDEBYSIDE) {
						axisLenXK = axisLength_X / numPlots - plotBufferX;
						xStartK = (int) xStart + axisLenXK * n + plotBufferX;
					} else {
						axisLenXK = axisLength_X;
						xStartK = (int) xStart;
					}

					Model_Well well = TheWells[n];
					len = well.TheGates.size();

					for (int i = 0; i < len; i++) {
						Gate_DotPlot g = well.TheGates.get(i);
						if (FeatureX.isSameFeature(g.featureX)
								&& FeatureY.isSameFeature(g.featureY)) {
							if (g.getPolygonGate_toDraw(xStartK, yStart,
									Bounds_X.Lower, Bounds_X.Upper,
									Bounds_Y.Lower, Bounds_Y.Upper, axisLenXK,
									axisLength_Y,
									LogScaleButton_X.isSelected(),
									LogScaleButton_Y.isSelected()).contains(
									p1.getPoint())) {
								g.selected = !g.selected;
								selected = g.selected;
								id = g.ID;
								foundOne = true;
							}
						}
					}
					if (foundOne)
						break;
				}
				// looking at gates in other well with same ID and giving it the
				// same selection as above
				for (int n = 0; n < num; n++) {
					Model_Well well = TheWells[n];
					len = well.TheGates.size();
					for (int i = 0; i < len; i++) {
						Gate_DotPlot g = (Gate_DotPlot) well.TheGates.get(i);
						if (g.ID == id && FeatureX.isSameFeature(g.featureX)
								&& FeatureY.isSameFeature(g.featureY))
							g.selected = selected;
					}
				}
			}

			updatePanel();
		}

		public void kill() {
			borderColors = null;
			TheGraphPanel = null;
		}
	}



	public void setSelectionListener(DotSelectionListener list) {
		TheDotSelectionListener = list;
	}

	public class SliderListener_PercentToPlot implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (!percentToPlotSlider.getValueIsAdjusting()) {
				percentToPlotSlider = (JSlider) e.getSource();
				percentToPlot = percentToPlotSlider.getValue() / 100f;
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
			}
		}
	}

	public class SliderListener_Alpha implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (!transparencySlider.getValueIsAdjusting()) {
				transparencySlider = (JSlider) e.getSource();
				float alpha = transparencySlider.getValue() / 100f;
				transComposite = AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, alpha);
				updatePlot(TheWells, FeatureX.toString(), FeatureY.toString());
				validate();
				repaint();
			}
		}
	}

	private void selectDotsPolygonGate_out() {
		if (ThePolygonGate == null || TheDots == null)
			return;
		int numP = TheDots.length;
		for (int p = 0; p < numP; p++) {
			int num = TheDots[p].length;
			for (int n = 0; n < num; n++)
				TheDots[p][n].setSelected(!ThePolygonGate
						.contains(TheDots[p][n].box));
		}
	}

	private void selectDotsPolygonGate_in() {
		if (ThePolygonGate == null || TheDots == null)
			return;
		int numP = TheDots.length;
		for (int p = 0; p < numP; p++) {
			int num = TheDots[p].length;
			for (int n = 0; n < num; n++)
				TheDots[p][n].setSelected(ThePolygonGate
						.contains(TheDots[p][n].box));
		}
	}

	// TheDotPlot.getXStart(), TheDotPlot.getYStart() -
	// TheDotPlot.getAxisLength_Y(),
	// TheDotPlot.getAxisLength_X(), TheDotPlot.getYStart() -
	// TheDotPlot.getAxisLength_Y() + yps[0] - 39); // Not

	public double getXStart() {
		return xStart;
	}

	public double getYStart() {
		return yStart;
	}

	public double getAxisLength_Y() {
		return axisLength_Y;
	}

	public double getAxisLength_X() {
		return axisLength_X;
	}

	//
	// The bounds input box
	//
	private class BoundsInputDialog extends JDialog implements ActionListener,
			PropertyChangeListener {

		private String typedText = null;
		private JTextField[] textField;
		private JOptionPane optionPane;
		private String btnString1 = "Set";
		private String btnString2 = "Cancel";

		public BoundsInputDialog(Model_Well[] wells) {
			int width = 400;
			int height = 310;
			setTitle("Input");
			setSize(width, height);

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
					- height / 2);

			TheWells = wells;
			textField = new JTextField[4];
			textField[0] = new JTextField(6);
			textField[1] = new JTextField(6);
			textField[2] = new JTextField(6);
			textField[3] = new JTextField(6);

			AxisBounds_CheckBox.setSelected(false);
			AxisBounds_CheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (AxisBounds_CheckBox.isSelected()) {
						textField[0].setText("" + (float) Bounds_X.Lower);
						textField[1].setText("" + (float) Bounds_X.Upper);
						textField[2].setText("" + (float) Bounds_Y.Lower);
						textField[3].setText("" + (float) Bounds_Y.Upper);
						textField[0].setEnabled(false);
						textField[1].setEnabled(false);
						textField[2].setEnabled(false);
						textField[3].setEnabled(false);
					} else {
						textField[0].setText("" + (float) Bounds_X.Lower);
						textField[1].setText("" + (float) Bounds_X.Upper);
						textField[2].setText("" + (float) Bounds_Y.Lower);
						textField[3].setText("" + (float) Bounds_Y.Upper);
						textField[0].setEnabled(true);
						textField[1].setEnabled(true);
						textField[2].setEnabled(true);
						textField[3].setEnabled(true);
					}

					validate();
					repaint();
				}
			});

			textField[0].setText("" + (float) Bounds_X.Lower);
			textField[1].setText("" + (float) Bounds_X.Upper);
			textField[2].setText("" + (float) Bounds_Y.Lower);
			textField[3].setText("" + (float) Bounds_Y.Upper);
			if (AxisBounds_CheckBox.isSelected()) {
				textField[0].setEnabled(false);
				textField[1].setEnabled(false);
				textField[2].setEnabled(false);
				textField[3].setEnabled(false);
			} else {
				textField[0].setEnabled(true);
				textField[1].setEnabled(true);
				textField[2].setEnabled(true);
				textField[3].setEnabled(true);
			}

			// Create an array of the text and components to be displayed.
			String[] mess = new String[5];
			mess[0] = "Enter Bounds:";
			mess[1] = "X - Lower Bounds";
			mess[2] = "X - Upper Bounds";
			mess[3] = "Y - Lower Bounds";
			mess[4] = "Y - Upper Bounds";

			Object[] array = { mess[0], mess[1], textField[0], mess[2],
					textField[1], mess[3], textField[2], mess[4], textField[3],
					AxisBounds_CheckBox };

			// Create an array specifying the number of dialog buttons
			// and their text.
			Object[] options = { btnString1, btnString2 };

			// Create the JOptionPane.
			optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, options, options[0]);

			// Make this dialog display it.
			setContentPane(optionPane);

			// Handle window closing correctly.
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
				}
			});

			// Ensure the text field always gets the first focus.
			addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent ce) {
					textField[0].requestFocusInWindow();
				}
			});

			// Register an event handler that puts the text into the option
			// pane.
			textField[0].addActionListener(this);

			// Register an event handler that reacts to option pane state
			// changes.
			optionPane.addPropertyChangeListener(this);
			setVisible(true);
		}

		/** This method handles events for the text field. */
		public void actionPerformed(ActionEvent e) {
			optionPane.setValue(btnString1);
		}

		/** This method reacts to state changes in the option pane. */
		public void propertyChange(PropertyChangeEvent e) {
			String prop = e.getPropertyName();

			if (isVisible()
					&& (e.getSource() == optionPane)
					&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
							.equals(prop))) {
				Object value = optionPane.getValue();

				if (value == JOptionPane.UNINITIALIZED_VALUE) {
					// ignore reset
					return;
				}

				// Reset the JOptionPane's value.
				// If you don't do this, then if the user
				// presses the same button next time, no
				// property change event will be fired.
				optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

				if (btnString1.equals(value)) {
					String[] strings = null;

					strings = new String[4];
					strings[0] = textField[0].getText();
					strings[1] = textField[1].getText();
					strings[2] = textField[2].getText();
					strings[3] = textField[3].getText();

					// make sure the inputed values are numbers only
					if (tools.MathOps.areNumbers(strings)) {
						Bounds_X.Lower = Float.parseFloat(strings[0]);
						Bounds_X.Upper = Float.parseFloat(strings[1]);
						Bounds_Y.Lower = Float.parseFloat(strings[2]);
						Bounds_Y.Upper = Float.parseFloat(strings[3]);
					} else {
						JOptionPane
								.showMessageDialog(
										null,
										"Error with inputed bounds!  Make sure numbers make sense ",
										"Thresholds", JOptionPane.ERROR_MESSAGE);
						return;
					}
					typedText = null;
					clearAndHide();

				} else { // user closed dialog or clicked cancel
					typedText = null;
					clearAndHide();
				}
			}
		}

		/** This method clears the dialog and hides it. */
		public void clearAndHide() {
			setVisible(false);
		}
	}

	/**
	 * Returns an array containing: [slope, y-Intercept, xMin, xMax, yMin, yMax]
	 */
	private double[] computeLinearRegression(Dot[] dots) {
		double[] data = new double[7];
		int len = dots.length;
		double tot = 0;
		double x = 0;
		double y = 0;
		double xy = 0;
		double x2 = 0;
		double y2 = 0;
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < len; i++) {
			double xp = dots[i].point.x;
			double yp = dots[i].point.y;

			if (xp < 100000 && xp >= 0 && yp < 100000 && yp >= 0) {
				// System.out.println("*******xp: "+xp +"   yp: "+yp);

				if (xp > xMax)
					xMax = xp;
				if (xp < xMin)
					xMin = xp;

				if (yp > yMax)
					yMax = yp;
				if (yp < yMin)
					yMin = yp;

				tot++;
				x += xp;
				y += yp;
				xy += (xp * yp);
				x2 += (xp * xp);
				y2 += (yp * yp);
			}
		}
		// vertical offsets:
		// double m = (tot*xy-x*y)/(tot*x2-x*x);

		// perpendicular offsets:
		double iTot = (1d / (double) tot);
		double B = 0.5d * ((y2 - (iTot * (y * y))) - (x2 - iTot * x * x))
				/ (iTot * x * y - xy);
		double mPlus = -B + Math.sqrt(B * B + 1);
		double mMinus = -B - Math.sqrt(B * B + 1);

		double x0 = x / tot;
		double y0 = y / tot;

		double bPlus = (y0 - mPlus * x0);
		double bMinus = (y0 - mMinus * x0);

		// creating line+
		double xsP = xMin;
		double xeP = xMax;
		double ysP = (mPlus * xMin + bPlus);
		double yeP = (mPlus * xMax + bPlus);
		// creating line-
		double xsM = xMin;
		double xeM = xMax;
		double ysM = (mMinus * xMin + bMinus);
		double yeM = (mMinus * xMax + bMinus);

		double varPlus = 0;
		double varMinus = 0;

		// computing the variance from line:
		for (int i = 0; i < len; i++) {
			double xp = dots[i].point.x;
			double yp = dots[i].point.y;

			if (xp < 100000 && xp >= 0 && yp < 100000 && yp >= 0) {
				double dP = Math.abs((xeP - xsP) * (ysP - yp) - (xsP - xp)
						* (yeP - ysP))
						/ (Math.sqrt((xeP - xsP) * (xeP - xsP) + (yeP - ysP)
								* (yeP - ysP)));
				double dM = Math.abs((xeM - xsM) * (ysM - yp) - (xsM - xp)
						* (yeM - ysM))
						/ (Math.sqrt((xeM - xsM) * (xeM - xsM) + (yeM - ysM)
								* (yeM - ysM)));

				varPlus += dP;
				varMinus += dM;
			}
		}

		double m = 0;
		double b = 0;
		if (varPlus <= varMinus) {
			m = mPlus;
			b = bPlus;
		} else {
			m = mMinus;
			b = bMinus;
		}

		data[0] = m;
		data[1] = b;
		data[2] = tot;
		data[3] = xMin;
		data[4] = xMax;
		data[5] = yMin;
		data[6] = yMax;
		return data;
	}

	/**
	 * Cleans up the RAM that held this dot plot
	 * 
	 * @author BLM
	 */
	public void kill() {
		transComposite = null;
		SmallFont = null;
		StandardFont = null;
		transparencySlider = null;
		Color Gray_1 = null;
		colors = null;
		yAxisLabelBox = null;
		xAxisLabelBox = null;
		ThePlotImage = null;
		PlotType_SideBySide = null;
		PlotType_Overlay = null;
		Button_linearRegression = null;
		Button_slopeDisplay = null;
		AxisBounds_CheckBox = null;
		HighlightInOutButton = null;
		ContourPlotButton = null;
		ThePolygonGate = null;
		ThePolygonGate_Points = null;
		ComboBoxes = null;
		nf = null;
		nf2 = null;
		TheDensitySorter = null;
		killDots();
		ThePlot = null;
		TheDataValues = null;
		ArrayList<ArrayList<CellCoordinates>> TheCell_coords;
		System.gc();
	}



}
