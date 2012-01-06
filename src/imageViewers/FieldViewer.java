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

import gui.MainGUI;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.util.ArrayList;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Field;
import models.Model_FieldCellRepository;
import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;
import plots.DotSelectionListener;
import segmentedobject.Cell;
import segmentedobject.CellCoordinates;

import com.sun.media.jai.widget.DisplayJAI;

public class FieldViewer extends DisplayJAI implements MouseListener,
		MouseMotionListener, WindowListener {
	private AlphaComposite translucComposite = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.2f);
	private AlphaComposite translucComposite_100 = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 1f);
	private AlphaComposite translucComposite_90 = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.9f);
	private AlphaComposite translucComposite_20 = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.2f);
	private DotSelectionListener TheDotSelectionListener;
	private FieldViewer_Frame TheParentContainer;
	private boolean CreateNewBox;
	private int dX;
	private int dY;
	private Color lightGray = new Color(50,50,50);
	private Color gray = new Color(40,40,40);
	private Color lighterGray = new Color(60,60,60);

	private JPanel ThePanel;
	// Allowing Both rectangle and oval selection -BLM Aug_09
	private Rectangle tempRect;
	private Ellipse2D.Float tempOval;
	private Shape SelectedROI;

	private Point startHighlightPoint;
	private Point startBox_XY;

	private int x_shape;
	private int y_shape;
	private int width_shape;
	private int height_shape;
	//

	private boolean continuallyDisplayBox;
	private Color highlightColor;
	private JSlider MaxValueSlider;
	private JSlider MinValueSlider;

	private JSlider ImageSelectionSlider;
	private int ImageSelected_index;
	private Model_Field TheField;
	private File[] ImagesToView;
	private Model_Plate[] ThePlates;
	private Model_Well TheWell;
	private int[] pixel;
	private int pixelValue;
	private int areaValue;
	private int HandleGrab;
	final int NONE = -1;
	final int LEFT = 0;
	final int RIGHT = 1;
	final int TOP = 2;
	final int BOTTOM = 3;
	
	private int ID;
	private RenderedImage TheCurrentImage;
	private RenderedImage TheDisplayedImage;
	private int[] LineProfileValues_X;
	private int[] LineProfileValues_Y;
	private FieldViewer_Frame HolderFrame;
	private Model_FieldCellRepository TheCellBank;

	/** Used for polygon ROI selection */
	private Polygon ThePolygonGate;
	private ArrayList<Point> ThePolygonGate_Points;

	//Normalized pixel Histograms for all fields for display on right. Size [numChannels][numBins]
	private float[][] histograms;
	
	public FieldViewer(FieldViewer_Frame holderFrame_, Model_Well well, Model_Field field) {
		HolderFrame = holderFrame_;
		ImageSelected_index = 0;
		pixelValue = -1;
		areaValue = -1;
		ImagesToView = field.getImageFiles();
		TheField = field;
		TheCellBank = TheField.getCellRepository();
		setBackground(Color.black);
		pixel = new int[3];
		float scalingFactor = 1f;
		HandleGrab = NONE;

		LineProfileValues_X = null;
		LineProfileValues_Y = null;

		ThePanel = this;
		addMouseListener(this);
		addMouseMotionListener(this);
		highlightColor = Color.white;
		SelectedROI = null;
		startHighlightPoint = null;
		continuallyDisplayBox = true;
		ID = (int) (Math.random() * 1000000000);

		ThePanel.setLayout(new BorderLayout());
		MaxValueSlider = new JSlider(0, Model_Main.MAXPIXELVALUE,
				Model_Main.MAXPIXELVALUE);

		MaxValueSlider.setToolTipText("Max = "+MaxValueSlider.getValue());
		MaxValueSlider.setName("Max");
		MaxValueSlider.setOrientation(JSlider.VERTICAL);
		SliderListener_RescaleDisplay listener = new SliderListener_RescaleDisplay();
		MaxValueSlider.addChangeListener(listener);

		MinValueSlider = new JSlider(0, Model_Main.MAXPIXELVALUE, 0);
		MinValueSlider.setEnabled(true);
		MinValueSlider.setToolTipText("Min = "+MinValueSlider.getValue());
		MinValueSlider.setName("Min");
		MinValueSlider.setPaintTicks(false);
		MinValueSlider.setPaintLabels(false);
		MinValueSlider.setOrientation(JSlider.VERTICAL);
		MinValueSlider.addChangeListener(listener);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 2));
		sliderPanel.add(MinValueSlider, 0);
		sliderPanel.add(MaxValueSlider, 1);

		ThePanel.add(sliderPanel, BorderLayout.EAST);

		if (ImagesToView.length > 0) {
			ImageSelectionSlider = new JSlider(0, ImagesToView.length - 1,
					ImageSelected_index);
			ImageSelectionSlider.setToolTipText("Channel Slider");
			ImageSelectionSlider.setMajorTickSpacing(1);
			ImageSelectionSlider.setPaintTicks(true);
			ImageSelectionSlider.setSnapToTicks(true);
			ImageSelectionSlider.setPaintLabels(true);
			ImageSelectionSlider
					.addChangeListener(new SliderListener_ImageSelection());
			ThePanel.add(ImageSelectionSlider, BorderLayout.SOUTH);

			ImageSelected_index = ImageSelectionSlider.getValue();

			// Scaling image if desired
			scalingFactor = FieldViewer_Frame.getScaling();
			ParameterBlock pb = new ParameterBlock();
			TheCurrentImage = JAI.create("fileload",
					ImagesToView[ImageSelected_index].getAbsolutePath());
			pb.addSource(TheCurrentImage);
			pb.add(scalingFactor);
			pb.add(scalingFactor);
			pb.add(0.0F);
			pb.add(0.0F);
			pb.add(new InterpolationNearest());
			// Creates a new, scaled image and uses it on the DisplayJAI
			// component
			TheCurrentImage = JAI.create("scale", pb);

			set(TheCurrentImage);
			TheDisplayedImage = TheCurrentImage;

			int numP = models.Model_Main.getModel().getPlateRepository()
					.getPlates().length;
			ThePlates = new Model_Plate[numP];
			for (int p = 0; p < numP; p++) {
				ThePlates[p] = models.Model_Main.getModel().getPlateRepository()
						.getPlates()[p].copy();
				ThePlates[p].getGUI().allowImageCountDisplay(false);
				ThePlates[p].getGUI().setSize(200, 400);

				int rows = ThePlates[p].getWells().length;
				int cols = ThePlates[p].getWells()[0].length;
				for (int i = 0; i < rows; i++)
					for (int c = 0; c < cols; c++) {
						Model_Well w = models.Model_Main.getModel()
								.getPlateRepository().getPlates()[p]
								.getWells()[i][c];
						Model_Well w2 = ThePlates[p].getWells()[i][c];
						if (!w.isSelected())
							w2.getGUI().color_outline = Color.darkGray;
						else
							w2.getGUI().color_outline = Color.white;
					}
			}
			TheWell = well;

			//Creating the histogram data
			// updateHistograms();
			
			ThePanel.repaint();
		}
	}
	
	/** 
	 * 
	 */
	public void updateHistograms()
	{
		int numBins = 30;
		int numChannels = ImagesToView.length;
		histograms = new float[numChannels][numBins];
		for (int j = 0; j < ImagesToView.length; j++) {
			//rescaling image to be small
			ParameterBlock pb = new ParameterBlock();
			RenderedImage TheCurrentImage = JAI.create("fileload",
					ImagesToView[j].getAbsolutePath());
			pb.addSource(TheCurrentImage);
			pb.add(0.1F);
			pb.add(0.1F);
			pb.add(0.0F);
			pb.add(0.0F);
			pb.add(new InterpolationNearest());
			// Creates a new, scaled image and uses it on the DisplayJAI component
			TheCurrentImage = JAI.create("scale", pb);
			Raster raster = TheCurrentImage.getData();
			int width = raster.getWidth();
			int height = raster.getHeight();
			int[][] im = new int[width][height];
			int[] pix = new int[raster.getNumBands()];
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					raster.getPixel(c, r, pix);
					im[c][r] = pix[0];
				}
			}
			//create histogram for this raster
			float[][] hist = tools.ImageTools.getHistogram_bounds_norm(im, numBins, (int)models.Model_Main.getModel().getMinValues_ImageDisplay()[j], (int)models.Model_Main.getModel().getMaxValues_ImageDisplay()[j]);
			histograms[j] = hist[1];
			im = null;
		}
	}
	

	/**
	 * Sets the listener in case dots have been selected in the dot plot we can
	 * display them properly in the
	 */
	public void setDotSelectionListener(DotSelectionListener d) {
		TheDotSelectionListener = d;
	}

	public Raster getCurrentRaster() {
		return TheCurrentImage.getData();
	}

	public void setParentContainer(FieldViewer_Frame v) {
		TheParentContainer = v;
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;

		// Scaling image if desired
		float scalingFactor = FieldViewer_Frame.getScaling();

		int nr = ThePlates.length;
		for (int p = 0; p < nr; p++) {
			ThePlates[p].getGUI().setYstart(60);
			ThePlates[p].getGUI().setXstart(TheCurrentImage.getWidth() + 15);
		}
		TheParentContainer.setSize(TheCurrentImage.getWidth() + 260,
				TheCurrentImage.getHeight() + 170);

		float factor = 1;
		if (scalingFactor == 0.50f)
			factor = 2f;
		else if (scalingFactor == 0.25f)
			factor = 3f;
		else if (scalingFactor == 0.1f)
			factor = 4f;

		if (TheCellBank != null) {

			ArrayList<Cell> selectedCells = TheCellBank
					.getSelectedCells();

			int radius = (int) (scalingFactor * 5);
			if (radius < 1)
				radius = 0;
			g2.setColor(Color.magenta); // TODO-X
			int leng = 0;
			if (selectedCells != null)
				leng = selectedCells.size();

			for (int i = 0; i < leng; i++) {
				Cell cell = selectedCells.get(i);
				CellCoordinates one = cell.getCoordinates();
				// System.out.println("numCom: "+one.getComSize());
				if (one != null && one.getComSize() == 1) {
					// If there is 1 compartment we assume its either a Centroid
					// (1 point) or a Bounding box (2 points)
					String comName = one.getComNames()[0];
					imagerailio.Point[] pts = one.getComCoordinates(0);
					int ptsLen = pts.length;

					if (comName.trim().equalsIgnoreCase("Centroid")) // Draw a
					// centroid
					// with
					// the
					// single
					// point
					{
						for (int z = 0; z < ptsLen; z++)
							g2.fillOval((int) (scalingFactor * pts[z].x),
									(int) (scalingFactor * pts[z].y), radius,
									radius);
						// System.out.println();
					} else if (comName.trim().equalsIgnoreCase("BoundingBox")) // Draw
						// a
						// bounding
						// box
						// with
						// the
						// 2
						// points
						// as
						// the
						// upper
						// left
						// and
						// bottom
						// right
						// corner
						g2.drawRect((int) (scalingFactor * pts[0].x),
								(int) (scalingFactor * pts[0].y),
								(int) (scalingFactor * (pts[1].x - pts[0].x)),
								(int) (scalingFactor * (pts[1].y - pts[0].y)));
					else if (comName.trim().equalsIgnoreCase("Outline")) {
						ptsLen = pts.length;
						for (int z = 0; z < ptsLen; z++) {
							if (z % factor == 0) {
								imagerailio.Point p = pts[z];
								g2.drawLine((int) (scalingFactor * p.x),
										(int) (scalingFactor * p.y),
										(int) (scalingFactor * p.x),
										(int) (scalingFactor * p.y));
							}
						}
					}
				}

			}
			

			// ----
			// TODO-X
			// if (false) {
			// g2.setColor(Color.magenta);
			// ArrayList<Cell> selCells = TheCellBank.getSelectedCells();
			// ArrayList<Cell> allCells = TheCellBank.getCells();
			// int selLen = selCells.size();
			// int allLen = allCells.size();
			// Raster raster = getCurrentRaster();
			// int numBands = raster.getNumBands();
			// int[] pix = new int[numBands];
			// for (int c = 0; c < selLen; c++) {
			// imagerailio.Point p1 = selCells.get(c).getCoordinates()
			// .getCentroid_subcompartment("Outline");
			// for (int r = 0; r < allLen; r++) {
			// if (c != r) {
			// imagerailio.Point p2 = allCells.get(r)
			// .getCoordinates()
			// .getCentroid_subcompartment("Outline");
			//
			// int xDist = p2.x - p1.x;
			// int yDist = p2.y - p1.y;
			// int yStart = p1.y;
			// int xStart = p1.x;
			// // compute slope btw points
			// float m = ((float) yDist) / ((float) xDist);
			//
			// boolean staysAboveBkgd = true;
			// int bkgd = (int) TheWell.getParameterSet()
			// .getParameter_float("Thresh_Bkgd_Value");
			// // Need to invert line tracking if slope too
			// // vertical
			// // (ex: walk along y axis not x axis)
			// if (m != Float.NaN || m != 0) {
			// // st = "";
			// if (Math.abs(m) < 1) {
			// for (int x = 0; x < Math.abs(xDist); x++) {
			//
			// int xI = x;
			// if (xDist < 0)
			// xI = -x;
			//
			// int xP = xStart + xI;
			// int y = (int) (m * xI + yStart);
			//
			// raster.getPixel(xP, y, pix);
			//
			// if (pix[0] < bkgd) {
			// staysAboveBkgd = false;
			// break;
			// }
			// }
			//
			// } else {
			// for (int y = 0; y < Math.abs(yDist); y++) {
			//
			// float yI = y;
			// if (yDist < 0)
			// yI = -y;
			//
			// float yP = (float) yStart + yI;
			// int x = (int) (1f / m * yI + xStart);
			//
			// raster.getPixel(x, (int) yP, pix);
			//
			// if (pix[0] < bkgd) {
			// staysAboveBkgd = false;
			// break;
			// }
			// }
			// }
			// if (staysAboveBkgd)
			// g2.drawLine((int) (scalingFactor * p2.x),
			// (int) (scalingFactor * p2.y),
			// (int) (scalingFactor * p1.x),
			// (int) (scalingFactor * p1.y));
			// }
			//
			// }
			// }
			// }
			// }
			// -----
			// pw.flush();
			// pw.close();
			//
			//
			//

		}

//		/**
//		 * checking all pixels to see if they are @ the threshold value for
//		 * background, then highlight them in the image
//		 * 
//		 * @author BLM
//		 */
		// if (TheParentContainer.shouldPlotBackground() && TheField != null
		// && TheField.getBackgroundValues() != null
		// && TheField.getBackgroundValues().length > 0
		// && TheField.getParentWell().getParameterSet() != null) {
		//
		// int index = TheField.getParentWell().getParameterSet()
		// .getParameter_int("Thresh_Cyt_ChannelIndex");
		//
		// if (ImageSelected_index == index) {
		// float bkgd = TheField.getParentWell().getParameterSet()
		// .getParameter_float("Thresh_Bkgd_Value");
		//
		// g2.setColor(Color.white);
		//
		// Raster raster = getCurrentRaster();
		// int numBands = raster.getNumBands();
		// int[] pix = new int[numBands];
		// int width = raster.getWidth();
		// int height = raster.getHeight();
		// for (int x = 0; x < width; x++) {
		// for (int y = 0; y < height; y++) {
		//
		// raster.getPixel(x, y, pix);
		// if (pix[0] < bkgd) {
		// g2.drawLine((int) (scalingFactor * x),
		// (int) (scalingFactor * y),
		// (int) (scalingFactor * x),
		// (int) (scalingFactor * y));
		//
		// }
		// }
		// }
		// }
		//			
		//			
		// }

		// Drawing the mini-plates
		int len = ThePlates.length;
		Model_Plate platew = TheWell.getPlate();
		Model_Plate platev = null;
		for (int i = 0; i < len; i++)
			if (ThePlates[i].getID() == platew.getID()) {
				platev = ThePlates[i];
				break;
			}
		
		//Draw the mini plate in upper right corner
		drawMiniPlate(g2, platev);
		//Draws the dynamic pixel value indicator under the plate
		drawPixelValue(g2);
		//Draw the x,y line intensity profile
//		drawLineProfiles(g2, platev.getGUI().getXstart() + 50, platev.getGUI().getYstart() + 310 );
		//Draw all stored and unstored ROIs
		drawROIs(g2);
		//Draws the image pixel intensity histograms for all channels
		drawHistograms(g2, platev.getGUI().getXstart(), platev.getGUI().getYstart() + 200, 170, 100);

		// Finally drawing current highlighting
		paintHighlighting(g2, scalingFactor);

	}
	
	public void drawHistograms(Graphics2D g2, int xStart, int yStart, int width, int height)
	{
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		// If histograms not init, try first then give up else
		if (histograms == null)
			updateHistograms();

		if(histograms==null || histograms.length == 0 || histograms[0].length == 0)
			return;
		
		//Drawing histogram containing box
		int border = 5;
		xStart = xStart+border;
		yStart = yStart+border;
		width = width - 2*border;
		height = height -2*border;
		
		g2.setColor(gray);
		g2.fillRect(xStart-border, yStart-border, width+2*border, height+2*border);
		g2.setColor(lightGray);
		g2.drawRect(xStart-border, yStart-border, width+2*border, height+2*border);
		g2.setColor(lighterGray);
		g2.fillRect(xStart, yStart, width, height);
		g2.setColor(lightGray);
		g2.drawRect(xStart, yStart, width, height);
		
		//Drawing the graph
		Composite orig = g2.getComposite();
		int channelSelected = ImageSelectionSlider.getValue();
		for (int i = 0; i < histograms.length; i++) {
		if( i!=channelSelected)
		{
			Polygon histo = getHistogramPolygon(histograms[i], xStart, yStart, width, height);
			if (histo != null ) {	
					g2.setComposite(translucComposite_20);
				if(i==0)
					g2.setColor(Color.blue);
				else if(i==1)
					g2.setColor(Color.green);
				else if(i==2)
					g2.setColor(Color.orange);
				else if(i==3)
					g2.setColor(Color.red);
				
					g2.fill(histo);
					g2.setColor(Color.black);
					g2.draw(histo);
			}
			}
		}
		//Making sure we print the active histo on top
		for (int i = 0; i < histograms.length; i++) {
			if( i==channelSelected)
			{
				Polygon histo = getHistogramPolygon(histograms[i], xStart, yStart, width, height);
				if (histo != null ) {	
						g2.setComposite(translucComposite_90);
					if(i==0)
						g2.setColor(Color.blue);
					else if(i==1)
						g2.setColor(Color.green);
					else if(i==2)
						g2.setColor(Color.orange);
					else if(i==3)
						g2.setColor(Color.red);
					
						g2.fill(histo);
						g2.setColor(Color.black);
						g2.draw(histo);
				}
				}
			}
		g2.setComposite(orig);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

	}
	
	public Polygon getHistogramPolygon(float[] bins, int xStart, int yStart, int width, int height)
	{
			Polygon p = new Polygon();
			int numBins = bins.length;
			float dx = (float) width / (float) numBins;
		int border = 3;

		p.addPoint((int) (xStart - border), (int) ((yStart + height) - height
				* bins[0]));

			for (int i = 0; i < numBins; i++)
				p.addPoint((int) (xStart + dx * i),
						(int) ((yStart + height) - height * bins[i]));

		p.addPoint((int) (xStart + dx * (numBins - 1) + border),
				(int) ((yStart + height) - height * bins[numBins - 1]));
		p.addPoint((int) (xStart + dx * (numBins - 1) + border), yStart
				+ height);
		p.addPoint(xStart - border, yStart + height);
		p.npoints = numBins + 4;

			return p;
	}
	
	
	public void drawPixelValue(Graphics2D g2)
	{
		g2.setFont(gui.MainGUI.Font_12);
		g2.setColor(Color.white);
		if (pixelValue != -1)
			g2.drawString("Pixel Value = " + pixelValue, ThePlates[0]
.getGUI()
					.getXstart() + 20, ThePlates[0].getGUI().getYstart() + 140);
		if (areaValue != -1)
			g2.drawString("Pixel Area = " + areaValue,
 ThePlates[0].getGUI()
					.getXstart() + 20, ThePlates[0].getGUI().getYstart() + 160);
	}
	
	public void drawROIs(Graphics2D g2)
	{
		// Drawing any stored field ROI's
		ArrayList<Shape> rois = TheField.getROIs();
		if (rois != null && rois.size() > 0) {
			int num = rois.size();
			for (int i = 0; i < num; i++) {
				Shape shape = rois.get(i);
				Color color = null;
				if (TheField.isROIselected(i))
					color = Color.red;
				else
					color = Color.green;

				drawScaledShape(g2, shape, color);
				
				
			}
		}


		/** Drawing polygon ROIs */
		if (ThePolygonGate_Points != null) {
			float scalingFactor = FieldViewer_Frame.getScaling();
			g2.setColor(Color.white);
			int len = ThePolygonGate_Points.size();
			if (len > 0) {
				Point p = ThePolygonGate_Points.get(0);
				for (int i = 1; i < len; i++) {
					Point pLast = p;
					p = ThePolygonGate_Points.get(i);
					g2.drawLine((int) (pLast.x * scalingFactor),
							(int) (pLast.y * scalingFactor),
							(int) (p.x * scalingFactor),
							(int) (p.y * scalingFactor));
				}
			}
		} else if (ThePolygonGate != null && HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_POLYGON) {
			g2.setColor(Color.white);
			// if (ThePolygonGate.xpoints.length == 1)
			// g2.drawLine(ThePolygonGate.xpoints[0],
			// ThePolygonGate.xpoints[0], ThePolygonGate.ypoints[0],
			// ThePolygonGate.ypoints[0]);
			// else {
			drawScaledShape(g2, ThePolygonGate, Color.white);
				g2.setColor(Color.black);
			// }
		}
	}
	

	
	public void drawLineProfiles(Graphics2D g2, int xStart, int yStart)
	{
		if (LineProfileValues_X != null && LineProfileValues_Y != null) {
			g2.setColor(Color.white);
			float scalingFactor = FieldViewer_Frame.getScaling();
			int len1 = LineProfileValues_X.length;
			int len2 = LineProfileValues_Y.length;
			int yMax = -10000000;
			int yMin = 10000000;

			for (int i = 0; i < len1; i++) {
				if (LineProfileValues_X[i] > yMax)
					yMax = LineProfileValues_X[i];
				if (LineProfileValues_X[i] < yMin)
					yMin = LineProfileValues_X[i];
			}
			for (int i = 0; i < len2; i++) {
				if (LineProfileValues_Y[i] > yMax)
					yMax = LineProfileValues_Y[i];
				if (LineProfileValues_Y[i] < yMin)
					yMin = LineProfileValues_Y[i];
			}

			int xLen = 60;
		
			float yHeight = 30f;
			// mini scale requires smaller params
			if (scalingFactor == 0.25f || scalingFactor == 0.1f) {
				return;
//				yStart = platev.getGUI().getYstart() + 200;
//				xLen = 20;
//				yHeight = 20;
			}
			// Drawing the xProfile
			int lastVal = yStart;
			int xLast = xStart;
			int counter = 0;
			for (int i = 1; i < len1; i++) {
				int yH = (int) (yStart - yHeight
						* (LineProfileValues_X[i] - yMin) / (yMax - yMin));
				int x = (int) (xStart + xLen * (float) i / (float) len1);
				g2.drawLine(xLast, lastVal, x, yH);
				lastVal = yH;
				xLast = x;
				counter++;
			}

			g2.setColor(Color.lightGray);
			// base Axis
			g2.drawLine(xStart, yStart, xStart + xLen, yStart);
			g2.drawLine(xStart, yStart, xStart, yStart + xLen);
			// diag axis splitter in upper left corner
			g2.drawLine((int) (xStart - yHeight), (int) (yStart - yHeight),
					xStart, yStart);
			// Max vals
			g2.drawLine((int) (xStart - yHeight), (int) (yStart - yHeight),
					(int) (xStart + xLen), (int) (yStart - yHeight));
			g2.drawLine((int) (xStart - yHeight), (int) (yStart - yHeight),
					(int) (xStart - yHeight), (int) (yStart + xLen));
			// 1/2 marks
			g2.drawLine((int) (xStart - 0.5f * yHeight),
					(int) (yStart - 0.5f * yHeight), (int) (xStart + xLen),
					(int) (yStart - 0.5f * yHeight));
			g2.drawLine((int) (xStart - 0.5f * yHeight),
					(int) (yStart - 0.5f * yHeight),
					(int) (xStart - 0.5f * yHeight), (int) (yStart + xLen));

			g2.setColor(tools.ColorRama.Gray_veryDark);
			// 3/4 vals
			g2.drawLine((int) (xStart - 0.75f * yHeight),
					(int) (yStart - 0.75f * yHeight), (int) (xStart + xLen),
					(int) (yStart - 0.75f * yHeight));
			g2.drawLine((int) (xStart - 0.75f * yHeight),
					(int) (yStart - 0.75f * yHeight),
					(int) (xStart - 0.75f * yHeight), (int) (yStart + xLen));
			// 1/4 marks
			g2.drawLine((int) (xStart - 0.25f * yHeight),
					(int) (yStart - 0.25f * yHeight), (int) (xStart + xLen),
					(int) (yStart - 0.25f * yHeight));
			g2.drawLine((int) (xStart - 0.25f * yHeight),
					(int) (yStart - 0.25f * yHeight),
					(int) (xStart - 0.25f * yHeight), (int) (yStart + xLen));
			// 1/8 marks
			g2.drawLine((int) (xStart - 0.125f * yHeight),
					(int) (yStart - 0.125f * yHeight), (int) (xStart + xLen),
					(int) (yStart - 0.125f * yHeight));
			g2.drawLine((int) (xStart - 0.125f * yHeight),
					(int) (yStart - 0.125f * yHeight),
					(int) (xStart - 0.125f * yHeight), (int) (yStart + xLen));

			g2.setColor(Color.white);
			g2.setFont(gui.MainGUI.Font_8);
			// Drawing the recommended thresholds
			int xOff = 10;
			g2.drawString("" + (int) (yMax), (int) (xStart + xLen + xOff),
					(int) (yStart - yHeight) - 2);
			g2.drawString("" + (int) (yMin + (yMax - yMin) * 0.75f),
					(int) (xStart + xLen + xOff),
					(int) (yStart - 0.75f * yHeight) + 3);
			g2.drawString("" + (int) (yMin + (yMax - yMin) * 0.25f),
					(int) (xStart + xLen + xOff),
					(int) (yStart - 0.25f * yHeight) + 3);
			g2.drawString("" + (int) (yMin + (yMax - yMin) * 0.125f),
					(int) (xStart + xLen + xOff),
					(int) (yStart - 0.125f * yHeight) + 8);
			g2.drawString("" + (int) (yMin), xStart + xLen + xOff, yStart + 15);

			// Drawing the yProfile
			g2.setColor(Color.white);
//			xStart = platev.getGUI().getYstart() + 250;
//			yStart = platev.getGUI().getXstart() + 50;

			if (scalingFactor == 0.25f || scalingFactor == 0.1f) {
				return;
//				xStart = platev.getGUI().getYstart() + 200;
			}
			lastVal = xStart;
			xLast = yStart;
			counter = 0;
			for (int i = 1; i < len2; i++) {
				int x = (int) (yStart - yHeight
						* (LineProfileValues_Y[i] - yMin) / (yMax - yMin));
				int yH = (int) (xStart + xLen * (float) i / (float) len2);
				g2.drawLine(xLast, lastVal, x, yH);
				lastVal = yH;
				xLast = x;
				counter++;
			}

		}
	}

	public void drawMiniPlate(Graphics2D g2, Model_Plate platev)
	{
		int numR = platev.getWells().length;
		int numC = platev.getWells()[0].length;
		// Drawing the plate name above it
		g2.setColor(Color.WHITE);
		g2.setFont(MainGUI.Font_12);
		String st = "Plate #" + platev.getID();
		g2.drawString(st, (platev.getGUI().getXstart() + (platev.getGUI()
				.getWidth() / 2 - st.length() * 5)), platev.getGUI()
				.getYstart() - 5);
		for (int r = 0; r < numR; r++)
			for (int c = 0; c < numC; c++) {
				if (r == TheWell.Row && c == TheWell.Column)
					platev.getWells()[r][c].setSelected(true);
				else
					platev.getWells()[r][c].setSelected(false);
				platev.getWells()[r][c].getGUI().draw(g2, false);
			}
	}
	
	public void drawScaledShape(Graphics2D g2, Shape shape, Color color) {
		float scale = FieldViewer_Frame.getScaling();
		Shape shapeToDraw = null;

		if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float oval = (Ellipse2D.Float) shape;
			shapeToDraw = new Ellipse2D.Float(scale * oval.x, scale * oval.y,
					scale * oval.width, scale * oval.height);
		} else if (shape instanceof Rectangle) {

			Rectangle rect = (Rectangle) shape;
			shapeToDraw = new Rectangle((int) (scale * rect.x),
					(int) (scale * rect.y), (int) (scale * rect.width),
					(int) (scale * rect.height));
		}
 else if (shape instanceof Polygon) {
			Polygon poly = (Polygon) shape;
			int len = poly.npoints;
			int[] xp = new int[len];
			int[] yp = new int[len];

			for (int i = 0; i < len; i++)
 {
				xp[i] = (int) (scale * (float) poly.xpoints[i]);
				yp[i] = (int) (scale * (float) poly.ypoints[i]);
			}
			shapeToDraw = new Polygon(xp, yp, len);
		}



		if (shapeToDraw != null) {
			g2.setColor(color);
			Composite orig = g2.getComposite();
			g2.setComposite(translucComposite);
			g2.fill(shapeToDraw);
			g2.setComposite(orig);
			g2.draw(shapeToDraw);
			
		}
	}

	public void updatePanel() {
		if (TheDotSelectionListener != null)
			TheDotSelectionListener.updateDotPlot();

		validate();
		repaint();
	}

	public void continuallyDisplayHighlightBox(boolean b) {
		continuallyDisplayBox = b;
	}

	public void setHightlightColor(Color color) {
		highlightColor = color;
	}

	public void paintHighlighting(Graphics2D g2, float scale) {
		// for the highlighting region
		if (SelectedROI != null) {

			Shape shapeToDraw = null;

			if (SelectedROI instanceof Ellipse2D.Float) {
				Ellipse2D.Float oval = (Ellipse2D.Float) SelectedROI;
				shapeToDraw = new Ellipse2D.Float(scale * oval.x, scale
						* oval.y, scale * oval.width, scale * oval.height);
				
			} else if (SelectedROI instanceof Rectangle) {

				Rectangle rect = (Rectangle) SelectedROI;
				shapeToDraw = new Rectangle((int) (scale * rect.x),
						(int) (scale * rect.y), (int) (scale * rect.width),
						(int) (scale * rect.height));
			}
 else if (SelectedROI instanceof Polygon) {
				Polygon poly = (Polygon) SelectedROI;
				Polygon copy = new Polygon();
				int len = poly.npoints;
				for (int i = 0; i < len; i++)
					copy.addPoint((int) (scale * poly.xpoints[i]),
							(int) (scale * poly.ypoints[i]));
				shapeToDraw = copy;
			}
			

			g2.setColor(highlightColor);
			Composite orig = g2.getComposite();
			g2.setComposite(translucComposite);

			g2.fill(shapeToDraw);
			g2.draw(shapeToDraw);
			g2.setComposite(orig);
			
			//Draw reshaping handles for oval or rectangle
			if(SelectedROI instanceof Ellipse2D.Float || SelectedROI instanceof Rectangle)
			{
				Rectangle bounds = shapeToDraw.getBounds();
				int width = 5;
				int xs = bounds.x;
				int xe = bounds.x+bounds.width;
				int ys = bounds.y;
				int ye = bounds.y+bounds.height;
								
				g2.setColor(Color.white);
				//draw left handle
				g2.fillRect(xs-width/2, (ye+ys)/2-width/2, width, width);
				//draw right handle
				g2.fillRect(xe-width/2, (ye+ys)/2-width/2, width, width);
				//draw top handle
				g2.fillRect((xe+xs)/2-width/2, ys-width/2, width, width);
				//draw bottom handle
				g2.fillRect((xe+xs)/2-width/2, ye-width/2, width, width);	
			}
		}

	}

	/**
	 * Returns the currently defined ROI if one exists
	 * 
	 * @author BLM
	 */
	public Shape getSelectedROI() {
		if (SelectedROI != null) {
			if (TheParentContainer.getShapeType() == TheParentContainer.SHAPE_POLYGON)
				return SelectedROI;
			else if (width_shape * height_shape > 20)
				return SelectedROI;
		}
		// if no region is selected, return the whole thing
			return new Rectangle(0, 0, ThePanel.getWidth(), ThePanel
					.getHeight());
	}

	/**
	 * Sets the ROI
	 * 
	 * @author BLM
	 */
	public void setSelectedROI(Shape roi) {
		SelectedROI = roi;
	}

	public void mouseClicked(MouseEvent p1) {
		if (p1.getClickCount() >= 2) {
			SelectedROI = null;
			startHighlightPoint = null;
			areaValue = -1;
		}

		// Seeing if clicked on a Fields saved ROI
		if (TheField != null && TheField.getNumberOfROIs() > 0) {
			float scale = FieldViewer_Frame.getScaling();
			Point p = new Point((int) (p1.getPoint().x / scale), (int) (p1
					.getPoint().y / scale));
			ArrayList<Shape> rois = TheField.getROIs();
			int len = rois.size();
			for (int i = 0; i < len; i++) {
				Shape shape = rois.get(i);
				if (shape != null && p != null && shape.contains(p))
					TheField.setROIselected(i, !TheField.isROIselected(i));
			}
		}

		// // Seeing if clicked on a cell to toggle highlighting
		// if (p1.getClickCount() != 1)
		// if (TheCellBank != null) {
		// Cell inCell = getClickedOnCell(p1.getPoint());
		// System.out.println(inCell);
		// if (inCell != null)
		// inCell.setSelected(!inCell.isSelected());
		// }

		// Seeing if clicked on the little plate in upper right corner in order
		// to change well/field
		int nr = ThePlates.length;
		for (int rr = 0; rr < nr; rr++) {

			int rows = ThePlates[rr].getWells().length;
			int cols = ThePlates[rr].getWells()[0].length;
			Point point = p1.getPoint();
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					if (ThePlates[rr].getWells()[r][c] != null)
						if (ThePlates[rr].getWells()[r][c].getGUI().outline
								.contains(point)) {
							Model_Well well = ThePlates[rr].getWells()[r][c];
							TheParentContainer.setCurrentWell(well);
							break;
						}
				}
		}

		updatePanel();
	}

	/**
	 * Sets whether the FieldViewer should create a new high-light box or to use
	 * the existing one
	 * 
	 * @author BLM
	 */
	public void setCreateNewBox(boolean boo) {
		CreateNewBox = boo;
	}



	/**
	 * Retuns the ID for this viewer
	 * 
	 * @author BLM
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Returns the raw image that is currently being displayed in an unscaled
	 * format
	 * 
	 * @author BLM
	 */
	public RenderedImage getTheCurrentImage() {
		return TheCurrentImage;
	}

	// /**
	// *
	// * */
	// public int[][][] getImageRaster_Banded() {
	// int numChannels = 3;
	// int width = TheCurrentImage.getWidth();
	// int height = TheCurrentImage.getHeight();
	// int max =
	// int[][][] raster = new int[width][height][numChannels];
	// int[] pix = new int[1];
	// for (int i = 0; i < width; i++)
	// for (int j = 0; j < height; j++)
	// for (int c = 0; c < numChannels; c++)
	// {
	// TheCurrentImage.getData().getPixel(i, j, pix);
	// System.out.println(pix[0]);
	// raster[i][j][c] = pix[0];
	// }
	//
	// return raster;
	// }

	/**
	 * Returns the image that is currently being displayed in a SCALED format
	 * 
	 * @author BLM
	 */
	public RenderedImage getTheDisplayedImage() {
		return TheDisplayedImage;
	}

	/**
	 * Return the well where this field is located
	 * 
	 * @author BLM
	 */
	public Model_Well getTheWell() {
		return TheWell;
	}

	/**
	 * Returns the source field that is being displayed in this viewer
	 * 
	 * @author BLM
	 */
	public Model_Field getTheField() {
		return TheField;
	}

	/**
	 * Sets the cell objects that were discovered in this field via segmentation
	 * 
	 * @author BLM
	 */
	// public void setTheCells(Cell_RAM[] cells)
	// {
	// TheCells = cells;
	// }

	public void mousePressed(MouseEvent p1) {
		Shape shape = null;
		float scale = FieldViewer_Frame.getScaling();
		if (SelectedROI != null)
		{
			//Draw reshaping handles for oval or rectangle
			if(SelectedROI instanceof Ellipse2D.Float || SelectedROI instanceof Rectangle)
			{
				Rectangle bounds = getScaledSelectionBounds(scale);
				int width = 15;
				int xs = bounds.x;
				int xe = bounds.x+bounds.width;
				int ys = bounds.y;
				int ye = bounds.y+bounds.height;

				HandleGrab = NONE;
				
				//init left handle
				Rectangle rectLeft = new Rectangle(xs-width/2, (ye+ys)/2-width/2, width, width);
				//init right handle
				Rectangle rectRight = new Rectangle(xe-width/2, (ye+ys)/2-width/2, width, width);
				//init top handle
				Rectangle rectTop = new Rectangle((xe+xs)/2-width/2, ys-width/2, width, width);
				//init bottom handle
				Rectangle rectBottom = new Rectangle((xe+xs)/2-width/2, ye-width/2, width, width);	

				if(rectLeft.contains(p1.getPoint()))
					HandleGrab = LEFT;
				else if(rectRight.contains(p1.getPoint()))
					HandleGrab = RIGHT;
				else if(rectTop.contains(p1.getPoint()))
					HandleGrab = TOP;
				else if(rectBottom.contains(p1.getPoint()))
					HandleGrab = BOTTOM;
				
				
				//Clean up
				rectLeft = null;
				rectRight = null;
				rectTop = null;
				rectBottom = null;
			
				if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_OVAL) {
					 bounds = getScaledSelectionBounds(scale);
					Ellipse2D.Float temp = new Ellipse2D.Float();
					temp.x = bounds.x;
					temp.y = bounds.y;
					x_shape = (int)temp.x;
					y_shape = (int)temp.y;
					temp.width = bounds.width;
					temp.height = bounds.height;
					shape = temp;
				} else if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_RECTANGLE) {
					 bounds = getScaledSelectionBounds(scale);
					Rectangle temp = new Rectangle();
					temp.x = bounds.x;
					temp.y = bounds.y;
					x_shape = (int)temp.x;
					y_shape = (int)temp.y;
					temp.width = bounds.width;
					temp.height = bounds.height;
					shape = temp;
				}
			}
		}
		if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_POLYGON
				|| HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_LINE) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				ThePolygonGate_Points = new ArrayList<Point>();
			ThePolygonGate_Points.add(new Point(
					(int) ((float) p1.getPoint().x / (float) scale),
					((int) ((float) p1.getPoint().y / (float) scale))));
			}

	
			if (HandleGrab!=NONE || (shape != null && shape.contains(p1.getPoint()))) {
				dX = p1.getX() - x_shape;
				dY = p1.getY() - y_shape;
				CreateNewBox = false;
			}
			else{
				startBox_XY = p1.getPoint();
				startHighlightPoint = p1.getPoint();
				CreateNewBox = true;
			}
		
		updatePanel();
	}

	public void mouseReleased(MouseEvent p1) {

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		HandleGrab = NONE;
		
		if (TheParentContainer.getShapeType() == TheParentContainer.SHAPE_POLYGON)
 {
			if (ThePolygonGate_Points != null) {
				ThePolygonGate = new Polygon();
				for (int i = 0; i < ThePolygonGate_Points.size(); i++) {
					Point p = ThePolygonGate_Points.get(i);
					ThePolygonGate.addPoint((int) ((float) p.x),
							(int) ((float) p.y));
				}
				ThePolygonGate_Points = null;
				// scaling
				SelectedROI = ThePolygonGate;

			}
		} else if (TheParentContainer.getShapeType() == TheParentContainer.SHAPE_LINE) {
			if (ThePolygonGate_Points != null) {
				ThePolygonGate = new Polygon();
				for (int i = 0; i < ThePolygonGate_Points.size(); i++) {
					Point p = ThePolygonGate_Points.get(i);
					ThePolygonGate.addPoint((int) ((float) p.x),
							(int) ((float) p.y));
				}
				// Adding mirror points to create a line
				for (int i = ThePolygonGate_Points.size() - 1; i >= 0; i--) {
					Point p = ThePolygonGate_Points.get(i);
					ThePolygonGate.addPoint((int) ((float) p.x),
							(int) ((float) p.y));
				}

				ThePolygonGate_Points = null;
				// scaling
				SelectedROI = ThePolygonGate;
			}
		}



		// reseting the highlightbox
		if (!continuallyDisplayBox) {
			SelectedROI = null;
			startHighlightPoint = null;
		}

		updatePanel();

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent p1) {
	}

	public void mouseDragged(MouseEvent p1) {

		float scalingFactor = FieldViewer_Frame.getScaling();

		if ((HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_POLYGON || HolderFrame
				.getShapeType() == FieldViewer_Frame.SHAPE_LINE)
				&& ThePolygonGate_Points != null
				&& ThePolygonGate_Points.size() > 0) {
			int len = ThePolygonGate_Points.size();
			Point p = ThePolygonGate_Points.get(len - 1);
			if (p1.getPoint().distance(p) > 2) {
				ThePolygonGate_Points.add(new Point((int) ((float) p1
						.getPoint().x / scalingFactor), ((int) ((float) p1
						.getPoint().y / scalingFactor))));
			}
		}
 else if (CreateNewBox) {
			if (p1 == null || p1.getPoint() == null
					|| startHighlightPoint == null)
				return;
			int xval = p1.getPoint().x - startHighlightPoint.x;
			int yval = p1.getPoint().y - startHighlightPoint.y;
			//

			if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_RECTANGLE) {

				tempRect = new Rectangle();
				tempRect.x = startHighlightPoint.x;
				tempRect.y = startHighlightPoint.y;
				tempRect.width = 0;
				tempRect.height = getHeight();

				if (xval >= 0)
					tempRect.width = xval;
				else {
					tempRect.x = p1.getPoint().x;
					tempRect.width = -xval;
				}
				if (yval >= 0)
					tempRect.height = yval;
				else {
					tempRect.y = p1.getPoint().y;
					tempRect.height = -yval;
				}

				x_shape = tempRect.x;
				y_shape = tempRect.y;
				width_shape = tempRect.width;
				height_shape = tempRect.height;

				// seeing if box has highlighted any cells
				if (TheCellBank != null) {
					ArrayList<Cell> cells = TheCellBank.getCells();
					if (cells != null) {
						int len = cells.size();
						for (int c = 0; c < len; c++) {
							Cell cell = cells.get(c);
							CellCoordinates coords = cell.getCoordinates();
							if (coords != null)
 {
								if (coords.getComSize() > 0) {

									imagerailio.Point[] pts = coords
											.getComCoordinates(0);
									Rectangle bounds = getScaledSelectionBounds(scalingFactor);
									if (bounds != null && pts != null
											&& pts.length > 0)
										if (bounds
												.contains(
														(int) (scalingFactor * pts[0].x),
														(int) (scalingFactor * pts[0].y)))
 {
											cell.setSelected(true);
										}
										else
 {
											cell.setSelected(false);
										}
								}


							}

						}
					}


				}


				// scaling
				SelectedROI = new Rectangle((int) (tempRect.x / scalingFactor),
						(int) (tempRect.y / scalingFactor),
						(int) (tempRect.width / scalingFactor),
						(int) (tempRect.height / scalingFactor));

				// for creating of the
				pixelValue = -1;
				areaValue = (int) (tempRect.width * tempRect.height / (scalingFactor * scalingFactor)); // adjusting
																										// for
																										// scaling

			} else if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_OVAL) {
				tempOval = new Ellipse2D.Float();
				tempOval.x = startHighlightPoint.x;
				tempOval.y = startHighlightPoint.y;
				tempOval.width = 0;
				tempOval.height = getHeight();

				if (xval >= 0)
					tempOval.width = xval;
				else {
					tempOval.x = p1.getPoint().x;
					tempOval.width = -xval;
				}
				if (yval >= 0)
					tempOval.height = yval;
				else {
					tempOval.y = p1.getPoint().y;
					tempOval.height = -yval;
				}

				x_shape = (int) tempOval.x;
				y_shape = (int) tempOval.y;
				width_shape = (int) tempOval.width;
				height_shape = (int) tempOval.height;

				// seeing if box has highlighted any cells
				if (TheCellBank != null) {
					ArrayList<Cell> cells = TheCellBank.getCells();
					int len = cells.size();

					for (int c = 0; c < len; c++) {

						Cell cell = cells.get(c);
						CellCoordinates coords = cell.getCoordinates();
						if (coords.getComSize() > 0) {
							imagerailio.Point[] pts = coords
									.getComCoordinates(0);
							if (tempOval != null
									&& tempOval.contains(
											(int) (scalingFactor * pts[0].x),
											(int) (scalingFactor * pts[0].y)))
								cell.setSelected(true);
							else
								cell.setSelected(false);
						}

					}
				}

				// scaling
				SelectedROI = new Ellipse2D.Float(tempOval.x / scalingFactor,
						tempOval.y / scalingFactor, tempOval.width
								/ scalingFactor, tempOval.height
								/ scalingFactor);

				// for creating of the
				pixelValue = -1;
				areaValue = (int) (tempOval.width / 2f * tempOval.height / 2f
						* Math.PI / (scalingFactor * scalingFactor)); // adjusting
																		// for
																		// scaling
				repaint();
			}

		} else // dragging extant box
		{

			if (SelectedROI != null && p1 != null) {

				if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_OVAL) {

					Rectangle bounds = getScaledSelectionBounds(scalingFactor);
					if (tempOval == null)
						tempOval = new Ellipse2D.Float();
					
					
					if(HandleGrab!=NONE) //Reshape it, dont translate
					{
						if(HandleGrab==LEFT)
						{
							int dx = bounds.x-p1.getX();
							tempOval.x = p1.getX();
							tempOval.y = bounds.y;
							tempOval.width = bounds.width+dx;
							tempOval.height = bounds.height;
						}
						else if(HandleGrab==RIGHT)
						{
							int dx = (bounds.x+bounds.width)-p1.getX();
							tempOval.x = bounds.x;
							tempOval.y = bounds.y;
							tempOval.width = bounds.width-dx;
							tempOval.height = bounds.height;
						}
						else if(HandleGrab==TOP)
						{
							int dy = bounds.y-p1.getY();
							tempOval.x = bounds.x;
							tempOval.y = p1.getY();
							tempOval.width = bounds.width;
							tempOval.height = bounds.height+dy;
						}
						else if(HandleGrab==BOTTOM)
						{
							int dy = (bounds.y+bounds.height)-p1.getY();
							tempOval.x = bounds.x;
							tempOval.y = bounds.y;
							tempOval.width = bounds.width;
							tempOval.height = bounds.height-dy;
						}
					}
					else // translate the whole shape
					{
						tempOval.x = bounds.x;
						tempOval.y = bounds.y;
						tempOval.width = bounds.width;
						tempOval.height = bounds.height;
	
						tempOval.x = p1.getX() - dX ;//- tempOval.width / 2f;
						tempOval.y = p1.getY() - dY ;//- tempOval.height / 2f;
					}
					
					// seeing if box has highlighted any cells
					if (TheCellBank != null) {
						ArrayList<Cell> cells = TheCellBank.getCells();
						int len = cells.size();
						for (int c = 0; c < len; c++) {

							Cell cell = cells.get(c);
							CellCoordinates coords = cell.getCoordinates();
							if (coords.getComSize() > 0) {
								imagerailio.Point[] pts = coords
										.getComCoordinates(0);
								if (tempOval != null
										&& tempOval
												.contains(
														(int) (scalingFactor * pts[0].x),
														(int) (scalingFactor * pts[0].y)))
									cell.setSelected(true);
								else
									cell.setSelected(false);
							}

						}
					}

					// scaling
					if (SelectedROI != null) {
						Ellipse2D.Float oval = (Ellipse2D.Float) SelectedROI;
						oval.x = tempOval.x / scalingFactor;
						oval.y = tempOval.y / scalingFactor;
						oval.width = tempOval.width / scalingFactor;
						oval.height = tempOval.height / scalingFactor;

					} else {
						SelectedROI = new Ellipse2D.Float(tempOval.x
								/ scalingFactor, tempOval.y / scalingFactor,
								tempOval.width / scalingFactor, tempOval.height
										/ scalingFactor);
					}

					pixelValue = -1;
					areaValue = (int) (tempOval.width * tempOval.height / (scalingFactor * scalingFactor)); // adjusting
																											// for
																											// scaling
					repaint();
				} else if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_RECTANGLE) {
					Rectangle bounds = getScaledSelectionBounds(scalingFactor);
					if (tempRect == null)
						tempRect = new Rectangle();

					if(HandleGrab!=NONE) //Reshape it, dont translate
					{
						if(HandleGrab==LEFT)
						{
							int dx = bounds.x-p1.getX();
							tempRect.x = p1.getX();
							tempRect.y = bounds.y;
							tempRect.width = bounds.width+dx;
							tempRect.height = bounds.height;
						}
						else if(HandleGrab==RIGHT)
						{
							int dx = (bounds.x+bounds.width)-p1.getX();
							tempRect.x = bounds.x;
							tempRect.y = bounds.y;
							tempRect.width = bounds.width-dx;
							tempRect.height = bounds.height;
						}
						else if(HandleGrab==TOP)
						{
							int dy = bounds.y-p1.getY();
							tempRect.x = bounds.x;
							tempRect.y = p1.getY();
							tempRect.width = bounds.width;
							tempRect.height = bounds.height+dy;
						}
						else if(HandleGrab==BOTTOM)
						{
							int dy = (bounds.y+bounds.height)-p1.getY();
							tempRect.x = bounds.x;
							tempRect.y = bounds.y;
							tempRect.width = bounds.width;
							tempRect.height = bounds.height-dy;
						}
					}
					else
					{
						tempRect.x = bounds.x;
						tempRect.y = bounds.y;
						tempRect.width = bounds.width;
						tempRect.height = bounds.height;
	
						tempRect.x = (int) (p1.getX() - dX );//- tempRect.width / 2f);
						tempRect.y = (int) (p1.getY() - dY );//- tempRect.height / 2f);
					}
					// seeing if box has highlighted any cells
					if (TheCellBank != null) {
						ArrayList<Cell> cells = TheCellBank.getCells();
						int len = cells.size();
						for (int c = 0; c < len; c++) {

							Cell cell = cells.get(c);
							CellCoordinates coords = cell.getCoordinates();
							if (coords != null && coords.getComSize() > 0) {
								imagerailio.Point[] pts = coords
										.getComCoordinates(0);
								bounds = getScaledSelectionBounds(scalingFactor);
								if (bounds != null && pts != null
										&& pts.length > 0)
									if (bounds.contains(
											(int) (scalingFactor * pts[0].x),
											(int) (scalingFactor * pts[0].y)))
										cell.setSelected(true);
									else
										cell.setSelected(false);
							}

						}
					}

					// scaling
					if (SelectedROI != null) {
						Rectangle rect = (Rectangle) SelectedROI;
						rect.x = (int) (tempRect.x / scalingFactor);
						rect.y = (int) (tempRect.y / scalingFactor);
						rect.width = (int) (tempRect.width / scalingFactor);
						rect.height = (int) (tempRect.height / scalingFactor);

					} else {
						SelectedROI = new Rectangle(
								(int) (tempRect.x / scalingFactor),
								(int) (tempRect.y / scalingFactor),
								(int) (tempRect.width / scalingFactor),
								(int) (tempRect.height / scalingFactor));
					}

					// for creating of the
					pixelValue = -1;
					areaValue = (int) (tempRect.width * tempRect.height / (scalingFactor * scalingFactor)); // adjusting
																											// for
																											// scaling
					repaint();

				}

			}
		}

		// Only allow this feature with Rectangle
		if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_RECTANGLE)
			updateLineProfiles((Rectangle) SelectedROI, scalingFactor);
		else {
			LineProfileValues_X = null;
			LineProfileValues_Y = null;
		}

		updatePanel();

		// Periodically invoking garbage collector
		if (Math.random() > 0.97)
			System.gc();
	}

	private void updateLineProfiles(Rectangle box, float scaling) {
		int xStart = (int) (box.x * scaling);
		int xEnd = (int) ((box.x + box.width) * scaling);
		int yStart = (int) (box.y * scaling);
		int yEnd = (int) ((box.y + box.height) * scaling);

		Raster currentRaster = getCurrentRaster();
		if (xStart < 0 || yStart < 0 || xEnd >= currentRaster.getWidth()
				|| yEnd >= currentRaster.getHeight() || yStart == yEnd
				|| xStart == xEnd) {
			LineProfileValues_X = null;
			LineProfileValues_Y = null;
			areaValue = -1;
			pixelValue = -1;
			repaint();
			return;
		}

		LineProfileValues_X = new int[xEnd - xStart];
		LineProfileValues_Y = new int[yEnd - yStart];

		for (int i = 0; i < LineProfileValues_X.length; i++)
			LineProfileValues_X[i] = 0;

		int xCounter = 0;
		for (int x = xStart; x < xEnd; x++) {
			int yCounter = 0;
			for (int y = yStart; y < yEnd; y++) {
				currentRaster.getPixel(x, y, pixel);
				pixelValue = pixel[0];
				if (pixelValue > LineProfileValues_X[xCounter])
					LineProfileValues_X[xCounter] = pixelValue;
				if (pixelValue > LineProfileValues_Y[yCounter])
					LineProfileValues_Y[yCounter] = pixelValue;
				yCounter++;
			}
			xCounter++;
		}

		repaint();
	}

	public void setShape(Shape shape, int type) {

		if (type == FieldViewer_Frame.SHAPE_RECTANGLE) {
			SelectedROI = shape;
			tempRect = (Rectangle) shape;
			HolderFrame.setShapeType(FieldViewer_Frame.SHAPE_RECTANGLE);
			CreateNewBox = false;
			x_shape = tempRect.x;
			y_shape = tempRect.y;
			width_shape = tempRect.width;
			height_shape = tempRect.height;
		} else if (type == FieldViewer_Frame.SHAPE_OVAL) {
			SelectedROI = shape;
			tempOval = (Ellipse2D.Float) shape;
			HolderFrame.setShapeType(FieldViewer_Frame.SHAPE_OVAL);
			CreateNewBox = false;
			x_shape = (int) tempOval.x;
			y_shape = (int) tempOval.y;
			width_shape = (int) tempOval.width;
			height_shape = (int) tempOval.height;
		}

	}

	public void mouseMoved(MouseEvent p1) {
		if (SelectedROI != null
				&& SelectedROI.getBounds().width
						* SelectedROI.getBounds().height > 6)
			return;

		Point point = p1.getPoint();
		int x = point.x;
		int y = point.y;

		Raster currentRaster = getCurrentRaster();

		if (x >= currentRaster.getWidth() || y >= currentRaster.getHeight()) {
			areaValue = -1;
			pixelValue = -1;
			repaint();
			return;
		}

		currentRaster.getPixel(x, y, pixel);
		pixelValue = pixel[0];

		repaint();

		if (Math.random() > 0.97)
			System.gc();
	}

	public void windowOpened(WindowEvent p1) {
	}

	public void windowClosing(WindowEvent p1) {

		TheDotSelectionListener.removeListener(this);
		TheParentContainer.kill();
		System.gc();
	}

	public void windowClosed(WindowEvent p1) {
	}

	public void windowIconified(WindowEvent p1) {
	}

	public void windowDeiconified(WindowEvent p1) {
	}

	public void windowActivated(WindowEvent p1) {
	}

	public void windowDeactivated(WindowEvent p1) {
	}

	/** 
	 * Rescales the image with a new max-value and min-value
	 * */
	public class SliderListener_RescaleDisplay implements ChangeListener {
		public void stateChanged(ChangeEvent e) {

			JSlider j = (JSlider) e.getSource();
			int channel = ImageSelectionSlider.getValue();
			if (j.getName().equalsIgnoreCase("Max")
					&& !MaxValueSlider.getValueIsAdjusting())
				models.Model_Main.getModel().getMaxValues_ImageDisplay()[channel] = MaxValueSlider
						.getValue();
			else if (j.getName().equalsIgnoreCase("Min")
					&& !MinValueSlider.getValueIsAdjusting())
			{
				//Dont let minvalue be greater than maxvalue
				if( MinValueSlider.getValue()> MaxValueSlider.getValue())
				{
					MinValueSlider.setValue(MaxValueSlider.getValue()-1);
					MinValueSlider.repaint();
				}
				models.Model_Main.getModel().getMinValues_ImageDisplay()[channel] = MinValueSlider
						.getValue();
			}

			RenderedImage newImage = rescale(TheCurrentImage,
					ImageSelectionSlider.getValue());
			set(newImage);
			TheDisplayedImage = newImage;
			
			//Update value that is displayed when hover over
			if (!MaxValueSlider.getValueIsAdjusting())
			{
				MaxValueSlider.setToolTipText("Max = "+MaxValueSlider.getValue());
				MinValueSlider.setToolTipText("Min = "+MinValueSlider.getValue());
			}
			
			//update the histogram with new scalings	
			if (!MaxValueSlider.getValueIsAdjusting())
				updateHistograms();
			
			ThePanel.repaint();

		}
	}



	/** Changes the channel */
	public class SliderListener_ImageSelection implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			ImageSelectionSlider = (JSlider) e.getSource();

			ImageSelected_index = ImageSelectionSlider.getValue();
			TheCurrentImage = JAI.create("fileload",
					ImagesToView[ImageSelected_index].getAbsolutePath());
			// currentRaster = TheCurrentImage.getData();
			// Scaling image if desired
			float scalingFactor = FieldViewer_Frame.getScaling();
			// if (scalingFactor!=currentScalingFactor)
			{
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(TheCurrentImage);
				pb.add(scalingFactor);
				pb.add(scalingFactor);
				pb.add(0.0F);
				pb.add(0.0F);
				pb.add(new InterpolationNearest());
				// Creates a new, scaled image and uses it on the DisplayJAI
				// component
				TheCurrentImage = JAI.create("scale", pb);

				// currentRaster = TheCurrentImage.getData();
				set(TheCurrentImage);
				TheDisplayedImage = TheCurrentImage;
			}

			// if (!TheParentContainer.AutoScaleCheck.isSelected())
			// {
			// now rescaling it
			int channel = ImageSelectionSlider.getValue();

			MaxValueSlider.setValue((int) models.Model_Main.getModel()
					.getMaxValues_ImageDisplay()[channel]);
			MinValueSlider.setValue((int) models.Model_Main.getModel()
					.getMinValues_ImageDisplay()[channel]);

			// Now we can rescale the pixels gray levels:
			RenderedImage newImage = rescale(TheCurrentImage,
					ImageSelectionSlider.getValue());

			set(newImage);
			TheDisplayedImage = newImage;

			ThePanel.repaint();

		}
	}

	/** 
	 * Rescales the given RenderedImage 
	 * */
	static public RenderedImage rescale(RenderedImage image, int channel) {
		int minValue = (int) Model_Main.getModel().getMinValues_ImageDisplay()[channel];
		int maxValue = (int) Model_Main.getModel().getMaxValues_ImageDisplay()[channel];
	
		// set the levels for the dynamic
		final ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		// rescaling each band 
		final double[] scale = new double[1];
		final double[] offset = new double[1];
		scale[0] = (double) Model_Main.MAXPIXELVALUE / (maxValue - minValue);
		offset[0] = -(((double) Model_Main.MAXPIXELVALUE * minValue) / (maxValue - minValue));
		pb.add(scale);
		pb.add(offset);
		return JAI.create("rescale", pb);
	}

	public void updateAllImages() {
		// Scaling image if desired
		float scalingFactor = FieldViewer_Frame.getScaling();
		ParameterBlock pb = new ParameterBlock();
		TheCurrentImage = JAI.create("fileload",
				ImagesToView[ImageSelected_index].getAbsolutePath());

		pb.addSource(TheCurrentImage);
		pb.add(scalingFactor);
		pb.add(scalingFactor);
		pb.add(0.0F);
		pb.add(0.0F);
		pb.add(new InterpolationNearest());
		// Creates a new, scaled image and uses it on the DisplayJAI component
		TheCurrentImage = JAI.create("scale", pb);
		// currentRaster = TheCurrentImage.getData();

		set(TheCurrentImage);
		TheDisplayedImage = TheCurrentImage;
		updatePanel();

		// now rescaling it
		int channel = ImageSelectionSlider.getValue();
		MaxValueSlider.setValue((int) models.Model_Main.getModel()
				.getMaxValues_ImageDisplay()[channel]);
		MinValueSlider.setValue((int) models.Model_Main.getModel()
				.getMinValues_ImageDisplay()[channel]);

		RenderedImage newImage = rescale(TheCurrentImage, ImageSelectionSlider
				.getValue());

		set(newImage);
		TheDisplayedImage = newImage;
		ThePanel.repaint();

		ImageSelectionSlider.validate();
		ImageSelectionSlider.repaint();

		MaxValueSlider.validate();
		MaxValueSlider.repaint();
	}

	public void copySettings(FieldViewer from, FieldViewer to) {
		to.ImageSelectionSlider.setValue(from.ImageSelectionSlider.getValue());
	}

	public int getApproximateMeanValue(Raster raster, Shape shape, int increment) {
		int xStart = 0;
		int xEnd = 0;
		int yStart = 0;
		int yEnd = 0;

		Rectangle bounds = shape.getBounds();
		xStart = (int) bounds.x;
		yStart = (int) bounds.y;
		xEnd = (int) (bounds.x + bounds.width);
		yEnd = (int) (bounds.y + bounds.height);

		int sum = 0;
		int xCounter = 0;
		int mean = 0;

		if (xStart > 0 && xEnd <= raster.getWidth() && yStart > 0
				&& yEnd <= raster.getHeight()) {
			int totalCounter = 0;
			for (int x = xStart; x < xEnd; x = x + increment) {
				int yCounter = 0;
				for (int y = yStart; y < yEnd; y = y + increment) {
					if (shape.contains(x, y)) {
						Raster currentRaster = getCurrentRaster();
						currentRaster.getPixel(x, y, pixel);
						pixelValue = pixel[0];
						sum += pixelValue;
						totalCounter++;
					}
					yCounter++;
				}
				xCounter++;
			}
			if (totalCounter == 0)
				mean = 0;
			else
				mean = sum / totalCounter;
		}
		return mean;
	}

	public int getMeanValue(Raster raster, Shape shape) {
		int xStart = 0;
		int xEnd = 0;
		int yStart = 0;
		int yEnd = 0;

		Rectangle bounds = shape.getBounds();
		xStart = (int) bounds.x;
		yStart = (int) bounds.y;
		xEnd = (int) (bounds.x + bounds.width);
		yEnd = (int) (bounds.y + bounds.height);

		int sum = 0;
		int xCounter = 0;
		int mean = 0;

		if (xStart > 0 && xEnd <= raster.getWidth() && yStart > 0
				&& yEnd <= raster.getHeight()) {
			int totalCounter = 0;
			for (int x = xStart; x < xEnd; x++) {
				int yCounter = 0;
				for (int y = yStart; y < yEnd; y++) {
					if (shape.contains(x, y)) {
						raster.getPixel(x, y, pixel);
						pixelValue = pixel[0];
						sum += pixelValue;
						totalCounter++;
					}
					yCounter++;
				}
				xCounter++;
			}
			if (totalCounter == 0)
				mean = 0;
			else
				mean = sum / totalCounter;
		}
		return mean;
	}

	/**
	 * Built originally August 09 for wound assay to detect a hole in the
	 * epithelial sheet
	 * 
	 * @author BLM
	 */
	public void optimizeXYshapeCoordinatesToHole(int channelIndex) {
		if (SelectedROI == null)
			return;
		int numI = ImagesToView.length;
		if (channelIndex >= numI)
			return;
		// Get desired raster in which we will optimize on
		Raster raster = getScaledRaster(ImagesToView[channelIndex],
				FieldViewer_Frame.getScaling());
		int increment = (int) (raster.getWidth() * 0.1f);
		Shape tempShape = null;

		if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_OVAL) {
			// first round --> 10% raster size steps
			long startTime = System.currentTimeMillis();

			Rectangle bounds1 = getScaledSelectionBounds(FieldViewer_Frame
					.getScaling());
			tempShape = new Ellipse2D.Float(bounds1.x, bounds1.y,
					bounds1.width, bounds1.height);

			// begin search in upper left corner
			Ellipse2D.Float shape = (Ellipse2D.Float) tempShape;
			shape.x = 0;
			shape.y = 0;

			// now go find the smallest mean value area
			Rectangle bounds = raster.getBounds();
			// /cordinates of min mean values
			int xMin = 0;
			int yMin = 0;
			float minMean = Float.POSITIVE_INFINITY;

			int xStart = 0;
			int xEnd = 0;
			int yStart = 0;
			int yEnd = 0;
			for (int c = 0; c < 2; c++) {
				System.out.println("--> Processing Model_Well = " + TheWell.name);
				// First pass we look at whole image
				if (c == 0) {
					increment = (int) (raster.getWidth() * 0.05f);
					xStart = 0;
					xEnd = (int) (bounds.width - shape.width);
					yStart = 0;
					yEnd = (int) (bounds.height - shape.height);
				}
				if (c != 0) {
					// make region smaller each iteration
					float fract = 0.2f;
					increment = 5;// (int)(raster.getWidth()*(0.1f-0.04f*c));

					xStart = (int) (xMin - fract * shape.width);
					xEnd = (int) ((xMin + shape.width) + fract * shape.width);
					yStart = (int) (yMin - fract * shape.height);
					yEnd = (int) ((yMin + shape.height) + fract * shape.height);

				}

				for (int j = yStart; j < yEnd; j = j + increment) {
					shape.x = xStart;
					shape.y = j;

					for (int i = xStart; i < xEnd; i = i + increment) {
						shape.x = i;

						float mean = getApproximateMeanValue(raster, shape, 3);
						if (mean < minMean && mean > 0) {
							minMean = mean;
							xMin = i;
							yMin = j;
						}
					}
				}
				shape.x = xMin;
				shape.y = yMin;
			}

			float scaling = TheParentContainer.getScaling();
			shape.x = xMin / scaling;
			shape.y = yMin / scaling;
			shape.width = shape.width / scaling;
			shape.height = shape.height / scaling;

			SelectedROI = shape;

			// System.out.println(System.currentTimeMillis()-startTime);
		} else if (HolderFrame.getShapeType() == FieldViewer_Frame.SHAPE_RECTANGLE) {

			Rectangle bounds1 = getScaledSelectionBounds(FieldViewer_Frame
					.getScaling());
			tempShape = new Rectangle(bounds1.x, bounds1.y, bounds1.width,
					bounds1.height);

			// begin search in upper left corner
			Rectangle shape = (Rectangle) tempShape;
			shape.x = 0;
			shape.y = 0;

			// now go find the smallest mean value area
			Rectangle bounds = raster.getBounds();
			// /cordinates of min mean values
			int xMin = 0;
			int yMin = 0;
			float minMean = Float.POSITIVE_INFINITY;

			int xStart = 0;
			int xEnd = 0;
			int yStart = 0;
			int yEnd = 0;

			for (int c = 0; c < 2; c++) {
				System.out.println("--> Processing Model_Well = " + TheWell.name);
				// First pass we look at whole image
				if (c == 0) {
					increment = (int) (raster.getWidth() * 0.05f);
					xStart = 0;
					xEnd = (int) (bounds.width - shape.width);
					yStart = 0;
					yEnd = (int) (bounds.height - shape.height);
				}
				if (c != 0) {
					// make region smaller each iteration
					float fract = 0.2f;
					increment = 5;// (int)(raster.getWidth()*(0.1f-0.04f*c));

					xStart = (int) (xMin - fract * shape.width);
					xEnd = (int) ((xMin + shape.width) + fract * shape.width);
					yStart = (int) (yMin - fract * shape.height);
					yEnd = (int) ((yMin + shape.height) + fract * shape.height);

				}

				for (int j = yStart; j < yEnd; j = j + increment) {
					shape.x = xStart;
					shape.y = j;

					for (int i = xStart; i < xEnd; i = i + increment) {
						shape.x = i;

						float mean = getApproximateMeanValue(raster, shape, 3);
						if (mean < minMean && mean > 0) {
							minMean = mean;
							xMin = i;
							yMin = j;
						}
					}
				}
				shape.x = xMin;
				shape.y = yMin;
			}

			float scaling = TheParentContainer.getScaling();
			shape.x = (int) (xMin / scaling);
			shape.y = (int) (yMin / scaling);
			shape.width = (int) (shape.width / scaling);
			shape.height = (int) (shape.height / scaling);

			SelectedROI = shape;

		}

		repaint();

	}

	public Raster getScaledRaster(File file, float scale) {
		RenderedImage im = JAI.create("fileload", file.getAbsolutePath());
		// Scaling image if desired
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(im);
		pb.add(scale);
		pb.add(scale);
		pb.add(0.0F);
		pb.add(0.0F);
		pb.add(new InterpolationNearest());
		// Creates a new, scaled image and uses it on the DisplayJAI component
		im = JAI.create("scale", pb);
		return im.getData();
	}

	public Rectangle getScaledSelectionBounds(float scale) {
		if (SelectedROI == null)
			return null;

		Rectangle bounds = new Rectangle();

		if (SelectedROI instanceof Rectangle) {
			Rectangle rect = (Rectangle) SelectedROI;

			bounds.x = (int) (rect.x * scale);
			bounds.y = (int) (rect.y * scale);
			bounds.width = (int) (rect.width * scale);
			bounds.height = (int) (rect.height * scale);
		} else if (SelectedROI instanceof Ellipse2D.Float) {
			Ellipse2D.Float oval = (Ellipse2D.Float) SelectedROI;

			bounds.x = (int) (oval.x * scale);
			bounds.y = (int) (oval.y * scale);
			bounds.width = (int) (oval.width * scale);
			bounds.height = (int) (oval.height * scale);
		}

		return bounds;
	}

	public Shape getCopyOfROI() {
		Shape shape = null;
		if (SelectedROI instanceof Rectangle) {
			Rectangle rect = (Rectangle) SelectedROI;
			Rectangle copy = new Rectangle();
			copy.x = (int) (rect.x);
			copy.y = (int) (rect.y);
			copy.width = (int) (rect.width);
			copy.height = (int) (rect.height);
			shape = copy;
		} else if (SelectedROI instanceof Ellipse2D.Float) {
			Ellipse2D.Float oval = (Ellipse2D.Float) SelectedROI;
			Ellipse2D.Float copy = new Ellipse2D.Float();
			copy.x = (int) (oval.x);
			copy.y = (int) (oval.y);
			copy.width = (int) (oval.width);
			copy.height = (int) (oval.height);
			shape = copy;
		} else if (SelectedROI instanceof Polygon) {
			Polygon poly = (Polygon) SelectedROI;
			Polygon copy = new Polygon();
			int len = poly.npoints;
			for (int i = 0; i < len; i++)
				copy.addPoint(poly.xpoints[i], poly.ypoints[i]);
			shape = copy;
		}
		return shape;
	}

	/**
	 * Returns the x,y,channel dimensions for the current raster
	 * 
	 * @author Bjorn Millard
	 * @return int[] dimensions of field
	 * */
	public int[] getFieldDimensions() {
		Raster raster = getCurrentRaster();
		int height = raster.getHeight();
		int width = raster.getWidth();

		int bands = models.Model_Main.getModel().getNumberOfChannels();
		int[] dims = new int[] { height, width, bands };
		return dims;
	}

	// /**
	// * Returns a single-cell that has been clicked on in the image. If none,
	// * then returns null
	// *
	// * @param Point
	// * @return Cell
	// */
	// private Cell getClickedOnCell(Point p) {
	// if (TheCellBank == null)
	// return null;
	// ArrayList<Cell> cells = TheCellBank.getCells();
	// int len = cells.size();
	// for (int i = 0; i < len; i++) {
	// Rectangle bbox = cells.get(i).getCoordinates().getBoundingBox();
	//
	// System.out.println(bbox);
	// System.out.println(p.x + "," + p.y);
	//
	// if (bbox != null && bbox.contains(p))
	// {
	// System.out.println(bbox);
	// return cells.get(i);
	// }
	// }
	// return null;
	// }

	/**
	 * Free up RAM
	 * 
	 * @author BLM
	 */
	public void kill() {
		translucComposite = null;
		TheDotSelectionListener.kill();
		ThePanel = null;
		tempRect = null;
		tempOval = null;
		SelectedROI = null;
		startHighlightPoint = null;
		startBox_XY = null;
		highlightColor = null;
		MaxValueSlider = null;
		MinValueSlider = null;
		ImageSelectionSlider = null;
		ImagesToView = null;
		ThePlates = null;
		pixel = null;
		TheCurrentImage = null;
		TheDisplayedImage = null;
		LineProfileValues_X = null;
		LineProfileValues_Y = null;
		ThePolygonGate = null;
		ThePolygonGate_Points = null;
	}
}
