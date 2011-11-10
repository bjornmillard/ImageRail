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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Main;
import models.Model_Well;

import com.sun.media.jai.widget.DisplayJAI;

public class ThresholdWizard extends JFrame {

	private JFrame TheFrame;
	private Raster raster;
	private Model_Well parentWell;
	private int max;
	private int min;
	private AlphaComposite translucComposite = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.6f);
	private int imWidth;
	private int imHeight;
	private JSlider NucleusSlider;
	private JSlider CytoSlider;
	private JSlider BkgdSlider;
	private int NucThreshold;
	private int CytoThreshold;
	private int BkgdThreshold;

	public ThresholdWizard(BufferedImage displayImage, Raster realRaster) {
		super("Threshold Wizard");
		raster = realRaster;
		TheFrame = this;
		setResizable(false);
		imWidth = displayImage.getWidth();
		imHeight = displayImage.getHeight();
		int height = imHeight + 80;
		int width = imWidth;
		setSize(width, height);

		// finding min/max of raster for default Slider settings
		min = Model_Main.MAXPIXELVALUE;
		max = 0;
		int numBands = raster.getNumBands();
		int[] pix = new int[numBands];
		for (int x = 0; x < imWidth; x++) {
			for (int y = 0; y < imHeight; y++) {
				raster.getPixel(x, y, pix);
				if (pix[0] < min)
					min = pix[0];
				if (pix[0] > max)
					max = pix[0];
			}
		}

		NucThreshold = max;
		CytoThreshold = (int) ((max - min) / 2d) + min;
		BkgdThreshold = min;

		NucleusSlider = new JSlider(min, max,
 NucThreshold);
		NucleusSlider.setToolTipText("Nucleus Threshold");
		NucleusSlider.setName("Nucleus Threshold");
		NucleusSlider.setOrientation(JSlider.HORIZONTAL);
		SliderListener_Thresholding listener = new SliderListener_Thresholding();
		NucleusSlider.addChangeListener(listener);

		CytoSlider = new JSlider(min, max, CytoThreshold);
		CytoSlider.setToolTipText("Cytoplasm Threshold");
		CytoSlider.setName("Cytoplasm Threshold");
		CytoSlider.setOrientation(JSlider.HORIZONTAL);
		listener = new SliderListener_Thresholding();
		CytoSlider.addChangeListener(listener);

		BkgdSlider = new JSlider(min, max, BkgdThreshold);
		BkgdSlider.setToolTipText("Bkgd Threshold");
		BkgdSlider.setName("Bkgd Threshold");
		BkgdSlider.setOrientation(JSlider.HORIZONTAL);
		listener = new SliderListener_Thresholding();
		BkgdSlider.addChangeListener(listener);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(3, 0));
		sliderPanel.add(NucleusSlider, 0);
		sliderPanel.add(CytoSlider, 1);
		sliderPanel.add(BkgdSlider, 2);
		
		add(new DisplayPanel(displayImage), BorderLayout.CENTER);
		add(sliderPanel, BorderLayout.SOUTH);
		setVisible(true);
	}

	private class DisplayPanel extends DisplayJAI implements MouseListener {
		public Rectangle clickTargetRect;

		public DisplayPanel(BufferedImage displayBuffer) {
			addMouseListener(this);
			clickTargetRect = new Rectangle(raster.getWidth() - 100, 10, 80, 70);
			set(displayBuffer);
		}

		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;

			Composite comp = g2.getComposite();
			g2.setComposite(translucComposite);

					int numBands = raster.getNumBands();
					int[] pix = new int[numBands];
					int width = raster.getWidth();
					int height = raster.getHeight();
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {

							raster.getPixel(x, y, pix);

					if (pix[0] < BkgdThreshold) {
						g2.setColor(Color.white);
						g2.drawLine(x, y, x, y);
							}
 else if (pix[0] > CytoThreshold && pix[0] < NucThreshold) {

						g2.setColor(Color.green);
						g2.drawLine(x, y, x, y);

					} else if (pix[0] > NucThreshold) {

						g2.setColor(Color.magenta);
						g2.drawLine(x, y, x, y);
					}

						}
					}

			g2.setComposite(comp);

			// Drawing the Threshold text values
			g2.setColor(Color.black);
			g2.fill(clickTargetRect);
			g2.setColor(Color.white);
			g2.fillRect(width - 97, 13, 74, 64);
			g2.setColor(Color.black);
			g2.drawString("N: " + NucThreshold, width - 92, 30);
			g2.drawString("C: " + CytoThreshold, width - 92, 50);
			g2.drawString("B: " + BkgdThreshold, width - 92, 70);

		}

		/**
		 * A little quick set for all wells
		 * 
		 * @author BLM
		 */
		// public void mouseClicked(MouseEvent e) {
		// if (e.isShiftDown()) {
		// if (clickTargetRect.contains(e.getPoint())) {
		// Model_Plate[] plates = models.Model_Main.getModel()
		// .getPlateHoldingPanel().getModel().getPlates();
		// int numPlates = plates.length;
		// for (int p = 0; p < numPlates; p++) {
		// Model_Well[] wells = plates[p].getAllWells();
		// for (int i = 0; i < wells.length; i++) {
		// Model_ParameterSet pset = wells[i].getParameterSet();
		//
		// // ProcessType
		// pset.setProcessType(Model_ParameterSet.SINGLECELL);
		// // Threshold Channel Nucleus
		// pset.setParameter("Thresh_Nuc_ChannelName",""+models.Model_Main.getModel()
		// .getTheChannelNames()[0]);
		// // Threshold Channel Cytoplasm
		// pset.setParameter("Thresh_Cyt_ChannelName",models.Model_Main.getModel()
		// .getTheChannelNames()[0]);
		// // Nuc bound threshold
		// pset.setParameter("Thresh_Nuc_Value",""+NucThreshold);
		// // Cell bound Threshold
		// pset.setParameter("Thresh_Cyt_Value",""+CytoThreshold);
		// // Bkgd threshold
		// pset.setParameter("Thresh_Bkgd_Value",""+BkgdThreshold);
		//
		// pset.setParameter("CoordsToSaveToHDF","Centroid");
		//
		// pset
		// .setMeanOrIntegrated(wells[i].TheParameterSet.MEAN);
		//
		// // Finding the index of this channel name
		// for (int j = 0; j < models.Model_Main.getModel()
		// .getTheChannelNames().length; j++)
		// if (models.Model_Main.getModel().getTheChannelNames()[j]
		// .equalsIgnoreCase(pset
		// .getParameter_String("Thresh_Nuc_ChannelName")))
		// pset.setParameter("Thresh_Nuc_ChannelIndex",""+j);
		// // Finding the index of this channel name
		// for (int j = 0; j < models.Model_Main.getModel()
		// .getTheChannelNames().length; j++)
		// if (models.Model_Main.getModel().getTheChannelNames()[j]
		// .equalsIgnoreCase(pset
		// .getParameter_String("Thresh_Cyt_ChannelName")))
		// pset.setParameter("Thresh_Cyt_ChannelIndex",""+j);
		// }
		// }
		// }
		// }
		// TheFrame.setVisible(false);
		// }

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}

	/** Updating thresholding slider displays */
	public class SliderListener_Thresholding implements ChangeListener {
		public void stateChanged(ChangeEvent e) {

			JSlider j = (JSlider) e.getSource();
			if (j.getName().equalsIgnoreCase("Nucleus Threshold")
					&& !NucleusSlider.getValueIsAdjusting())
				NucThreshold = NucleusSlider.getValue();
			else if (j.getName().equalsIgnoreCase("Cytoplasm Threshold")
					&& !CytoSlider.getValueIsAdjusting())
				CytoThreshold = CytoSlider
						.getValue();
			else if (j.getName().equalsIgnoreCase("Bkgd Threshold")
					&& !BkgdSlider.getValueIsAdjusting())
				BkgdThreshold = BkgdSlider
						.getValue();

			repaint();

		}
	}


	}


