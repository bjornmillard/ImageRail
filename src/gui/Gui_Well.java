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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import models.Model_Well;

public class Gui_Well {

	/** */
	public Rectangle outline;
	/** */
	public Color color;
	/** */
	public Color color_outline;
	/** */
	static final public AlphaComposite translucComposite = AlphaComposite
			.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	/** */
	private Color initColor = Color.black;
	/** */
	public boolean AllowImageCountDisplay;
	/** Model for this GUI representation of the well */
	private Model_Well TheModel;

	/** Main Model_Well Constructor */
	public Gui_Well(Model_Well model) {
		TheModel = model;
		model.setGUI(this);
		color = initColor;
		color_outline = Color.white;
		outline = new Rectangle();
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();
		AllowImageCountDisplay = true;
	}

	/** Returns the model that this well represents */
	public Model_Well getModel() {
		return TheModel;
	}

	/**
	 * Computes the width of this Model_Well in the GUI
	 * 
	 * @author BLM
	 */
	public int getWidth() {
		int val = 5;
		if (TheModel.getPlate().getNumColumns() == 24)
			val = 4;
		if (TheModel.getPlate().getNumColumns() == 12)
			val = 5;
		if (TheModel.getPlate().getNumColumns() == 6)
			val = 7;
		if (TheModel.getPlate().getNumColumns() == 4)
			val = 10;
		if (TheModel.getPlate().getNumColumns() == 3)
			val = 12;

		int bufferX = 0;
		int bufferY = 50;
		float panelW = TheModel.getPlate().getGUI().getWidth() - bufferX;
		float panelH = TheModel.getPlate().getGUI().getHeight() - bufferY;
		float ratio = panelW / panelH;
		float desiredRatio = 12f / 8f;
		if (ratio < desiredRatio)
			return (int) (panelW / TheModel.getPlate().getNumColumns() - val);
		return (int) (panelH / TheModel.getPlate().getNumRows() - val);
	}

	/**
	 * Computes the height of this Model_Well in the GUI
	 * 
	 * @author BLM
	 */
	public int getHeight() {
		int val = 5;
		if (TheModel.getPlate().getNumRows() == 16)
			val = 4;
		if (TheModel.getPlate().getNumRows() == 8)
			val = 5;
		if (TheModel.getPlate().getNumRows() == 4)
			val = 7;
		if (TheModel.getPlate().getNumRows() == 2)
			val = 10;
		if (TheModel.getPlate().getNumRows() == 2)
			val = 12;

		int bufferX = 0;
		int bufferY = 50;
		float panelW = TheModel.getPlate().getGUI().getWidth() - bufferX;
		float panelH = TheModel.getPlate().getGUI().getHeight() - bufferY;
		float ratio = panelW / panelH;
		float desiredRatio = 12f / 8f;
		if (ratio < desiredRatio)
			return (int) (panelW / TheModel.getPlate().getNumColumns() - val);
		return (int) (panelH / TheModel.getPlate().getNumRows() - val);
	}

	/**
	 * Computes the xPosition of this Model_Well in the GUI
	 * 
	 * @author BLM
	 */
	public int getXpos() {
		return (int) (TheModel.getPlate().getGUI().getXstart() + TheModel.Column * (outline.width + 3));
	}

	/**
	 * Computes the yPosition of this Model_Well in the GUI
	 * 
	 * @author BLM
	 */
	public int getYpos() {
		return (int) (TheModel.getPlate().getGUI().getYstart() + TheModel.Row * (outline.height + 3));
	}

	/**
	 * Draws this Model_Well in the GUI Model_Plate
	 * 
	 * @author BLM
	 */
	public void draw(Graphics2D g2, boolean plotSVG) {
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();

		if (color != null)
			g2.setColor(color);

		// Seeing if we want to plot well according to metadata
		// if (TheModel.getPlate().getGUI().shouldDisplayMetaData() > -1) {
		// int Type = TheModel.getPlate().getGUI().shouldDisplayMetaData();
		// ArrayList<String> arr = new ArrayList<String>();
		// if (Type == 0) // Treatments
		// arr =
		// TheModel.getPlate().getMetaDataConnector().getAllTreatmentNames(
		// TheModel.getPlate().getPlateIndex(),
		// TheModel.getWellIndex());
		// else if (Type == 1) // Measurements
		// arr = TheModel.getPlate().getMetaDataConnector()
		// .getAllMeasurementNames(
		// TheModel.getPlate().getPlateIndex(),
		// TheModel.getWellIndex());
		// else if (Type == 2) // Descriptions
		// {
		// Description des = ((Description) TheModel.getPlate()
		// .getMetaDataConnector().readDescription(
		// TheModel.getWellIndex()));
		// if (des == null || des.getValue() == null)
		// arr = null;
		// else
		// arr.add(des.getValue());
		// } else if (Type == 3) {
		// Description des = ((Description) TheModel.getPlate()
		// .getMetaDataConnector().readTimePoint(
		// TheModel.getWellIndex()));
		// if (des == null || des.getValue() == null)
		// arr = null;
		// else
		// arr.add(des.getValue());
		// }
		//
		// if (arr != null && arr.size() > 0) {
		// Hashtable hash = TheModel.getPlate().getMetaDataHashtable();
		// // Now adding it to the hastable
		// String treatsCat = "";
		// for (int i = 0; i < arr.size() - 1; i++)
		// treatsCat += arr.get(i) + " + ";
		// treatsCat += arr.get(arr.size() - 1);
		//
		// Color color2 = (Color) hash.get(treatsCat);
		// if (color2 != null)
		// g2.setColor(color2);
		// }
		// }


		g2.fillRect(outline.x, outline.y, outline.width, outline.height);

		// 384 well plates get too busy to have a white border

		if (TheModel.processing)
			g2.setColor(Color.green);
		else if (!TheModel.isSelected())
			g2.setColor(color_outline);
		else if (TheModel.isSelected())
			g2.setColor(Color.red);
		g2.drawRect(outline.x, outline.y, outline.width, outline.height);

		if (!!TheModel.isSelected() && !TheModel.processing)
			g2.setColor(color_outline);

		// Drawing the number of images text display
		if (AllowImageCountDisplay
				&& models.Model_Main.getModel().getGUI()
						.getDisplayNumberLoadedImagesCheckBox() != null
				&& models.Model_Main.getModel().getGUI()
						.getDisplayNumberLoadedImagesCheckBox()
						.isSelected() && TheModel.getFields() != null) {
			g2.setFont(new Font("Helvetca", Font.PLAIN, 9));
			if (TheModel.getFields().length > 0)
				g2.drawString((TheModel.getFields().length + "x" + TheModel
						.getFields()[0]
						.getNumberOfChannels()), (outline.x + outline.width
						/ 2f - 9), (outline.y + outline.height / 2f + 5));
		}

		// Checking to see if any HDF files are available for this well
		if (models.Model_Main.getModel().getGUI().shouldDisplayHDFicons()) {
			int numHDF = TheModel.getHDFcount();

			if (numHDF > 0) {
				int xS = outline.x + 5;
				int yS = outline.y + 5;
				int hdfIco_width = 5;
				int hdfIco_height = 8;
				int offset = 3;
				int xCounter = 0;
				int yCounter = 0;
				for (int i = 0; i < numHDF; i++) {
					int xStart = xS + xCounter * (hdfIco_width + offset);
					int yStart = yS + yCounter * (hdfIco_height + offset);

					if ((xStart + hdfIco_width) > (outline.x + outline.width)) {
						xCounter = 0;
						yCounter++;
						xStart = xS + xCounter * (hdfIco_width + offset);
						yStart = yS + yCounter * (hdfIco_height + offset);
					}

					int xEnd = xStart + hdfIco_width;
					int yEnd = yStart + hdfIco_height;
					drawHDFicon(g2, xStart, yStart, xEnd, yEnd);
					xCounter++;
				}
			}
		}


		// drawing the histograms if desired
		if (!TheModel.isLoading()
				&& models.Model_Main.getModel().getPlateRepository_GUI()
						.shouldDisplayHistograms()
				&& TheModel.containsCellData()
				&& TheModel.getCell_values() != null) {
			int x = outline.x + 1;
			int y = outline.y + 1;
			int width = outline.width - 1;
			int height = outline.height - 2;
			Polygon histo = getHistogram(x, y, width, height);
			if (histo != null) {
				g2.setColor(Color.white);
				if (plotSVG)
				{
					g2.fillPolygon(histo.xpoints, histo.ypoints, histo.npoints);
					g2.setColor(Color.black);
					g2.drawPolygon(histo.xpoints, histo.ypoints, histo.npoints);

				}
				else
				{
					g2.fill(histo);
					g2.setColor(Color.black);
					g2.draw(histo);

				}
				



			}
		}

	}

	/**
	 * Draws the mini-HDF file icon
	 * 
	 * @author BLM
	 */
	private void drawHDFicon(Graphics2D g2, int xStart, int yStart, int xEnd,
			int yEnd) {
		int width = xEnd - xStart - 1;
		int height = yEnd - yStart - 1;


		g2.setColor(Color.white);
		g2.fillRect(xStart, yStart, width, height);
		g2.setColor(Color.gray);
		g2.drawRect(xStart, yStart, width, height);
	}


	public Polygon getHistogram(int x, int y, int width, int height) {
		int xStart = x + 1;
		int yStart = y + 1;
		int xLen = width - 1;
		int yLen = height - 2;
		int numBins = 50;
		float[][] cells = TheModel.getCell_values();
		if (cells == null)
			return null;
		int numCells = cells.length;
		float dX = (float) xLen / (float) numBins;
		int[] bins = new int[numBins];
		int feature_index = models.Model_Main.getModel().getTheSelectedFeature_Index();

		double minVal = Double.POSITIVE_INFINITY;
		double maxVal = Double.NEGATIVE_INFINITY;

		// Getting the min/max pre-stored values
		if (!models.Model_Main.getModel().getPlateRepository_GUI().isLogScaled()) {
			if (TheModel.getPlate().getMinMaxFeatureValues() != null
					&& TheModel.getPlate().getMinMaxFeatureValues().length > 0
					&& TheModel.getPlate().getMinMaxFeatureValues()[0].length > 0) {
				minVal = TheModel.getPlate().getMinMaxFeatureValues()[0][models.Model_Main
						.getModel()
						.getTheSelectedFeature_Index()];
				maxVal = TheModel.getPlate().getMinMaxFeatureValues()[1][models.Model_Main
						.getModel()
						.getTheSelectedFeature_Index()];
			}
		} else {
			if (TheModel.getPlate().getMinMaxFeatureValues_log() != null
					&& TheModel.getPlate().getMinMaxFeatureValues_log().length > 0
					&& TheModel.getPlate().getMinMaxFeatureValues_log()[0].length > 0) {
				minVal = TheModel.getPlate().getMinMaxFeatureValues_log()[0][models.Model_Main
						.getModel().getTheSelectedFeature_Index()];
				maxVal = TheModel.getPlate().getMinMaxFeatureValues_log()[1][models.Model_Main
						.getModel().getTheSelectedFeature_Index()];
			}
		}

		// Binning values
		for (int i = 0; i < numCells; i++) {
			double val = cells[i][feature_index];
			if (models.Model_Main.getModel().getPlateRepository_GUI().isLogScaled()) {
				if (val <= 1)
					val = 1;
				val = tools.MathOps.log(val);
			} else if (val <= 0)
				val = 0;

			if (val > Double.NEGATIVE_INFINITY
					&& val < Double.POSITIVE_INFINITY) {
				double normVal = (val - minVal) / (maxVal - minVal);
				int ind = (int) (numBins * normVal);
				if (ind > 0 && ind < numBins - 1)
					bins[ind]++;
			}
		}
		// Finding max bin value so we can normalize the histogram
		float maxBinVal = 0;
		for (int i = 0; i < numBins; i++)
			if (bins[i] > maxBinVal)
				maxBinVal = bins[i];

		// Creating the polygon
		if (maxBinVal > 0) {
			Polygon p = new Polygon();
			for (int i = 0; i < numBins; i++)
				p.addPoint((int) (xStart + dX * i),
						(int) ((yStart + yLen) - yLen * bins[i] / maxBinVal));

			p.addPoint((int) (xStart + dX * (numBins - 1)), yStart + yLen);
			p.addPoint(xStart, yStart + yLen);
			p.npoints = numBins + 2;

			return p;
		}

		return null;
	}

	public void updateDimensions() {
		outline.width = getWidth();
		outline.height = getHeight();
		outline.x = getXpos();
		outline.y = getYpos();
	}
}
