/**
 * FieldViewer.java
 *
 * @author Bjorn L Millard
 */

package imageViewers;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.Cells_oneField;
import main.Field;
import main.MainGUI;
import main.Plate;
import main.Well;
import plots.DotSelectionListener;
import us.hms.systemsbiology.segmentedobject.Cell;
import us.hms.systemsbiology.segmentedobject.CellCoordinates;

import com.sun.media.jai.widget.DisplayJAI;

public class FieldViewer extends DisplayJAI implements MouseListener,
		MouseMotionListener, WindowListener {
	private AlphaComposite translucComposite = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.07f);
	private DotSelectionListener TheDotSelectionListener;
	private FieldViewer_Frame TheParentContainer;
	private boolean CreateNewBox;
	private int dX;
	private int dY;

	private Point DummyPoint;
	private JPanel ThePanel;
	private Rectangle DummyBox;


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
	private Field TheField;
	private File[] ImagesToView;
	private Plate[] ThePlates;
	private Well TheWell;
	private int[] pixel;
	private int pixelValue;
	private int areaValue;

	private int ID;
	private RenderedImage TheCurrentImage;
	private RenderedImage TheDisplayedImage;
	private int[] LineProfileValues_X;
	private int[] LineProfileValues_Y;
	private FieldViewer_Frame HolderFrame;
	private Cells_oneField TheCellBank;

	public FieldViewer(FieldViewer_Frame holderFrame_, Well well, Field field) {
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
		DummyPoint = new Point();
		DummyBox = new Rectangle();

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
		MaxValueSlider = new JSlider(0, MainGUI.MAXPIXELVALUE,
				MainGUI.MAXPIXELVALUE);

		MaxValueSlider.setToolTipText("Maximum");
		MaxValueSlider.setName("Max");
		MaxValueSlider.setOrientation(JSlider.VERTICAL);
		SliderListener_RescaleDisplay listener = new SliderListener_RescaleDisplay();
		MaxValueSlider.addChangeListener(listener);

		MinValueSlider = new JSlider(0, MainGUI.MAXPIXELVALUE, 0);
		MinValueSlider.setEnabled(false);
		MinValueSlider.setToolTipText("Minimum");
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

			// currentRaster = TheCurrentImage.getData();

			int numP = MainGUI.getGUI().getThePlateHoldingPanel()
					.getThePlates().length;
			ThePlates = new Plate[numP];
			for (int p = 0; p < numP; p++) {
				ThePlates[p] = MainGUI.getGUI().getThePlateHoldingPanel()
						.getThePlates()[p].copy();
				ThePlates[p].allowImageCountDisplay(false);
				ThePlates[p].setSize(200, 400);

				int rows = ThePlates[p].getTheWells().length;
				int cols = ThePlates[p].getTheWells()[0].length;
				for (int i = 0; i < rows; i++)
					for (int c = 0; c < cols; c++) {
						Well w = MainGUI.getGUI().getThePlateHoldingPanel()
								.getThePlates()[p].getTheWells()[i][c];
						Well w2 = ThePlates[p].getTheWells()[i][c];
						if (!w.isSelected())
							w2.color_outline = Color.darkGray;
						else
							w2.color_outline = Color.white;
					}
			}

			TheWell = null;
			for (int p = 0; p < numP; p++)
				if (ThePlates[p].getID() == well.getPlate().getID()) {
					TheWell = ThePlates[p].getWell(well.name);
					break;
				}

			ThePanel.repaint();
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
			ThePlates[p].setYstart(60);
			ThePlates[p].setXstart(TheCurrentImage.getWidth() + 15);
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
			g2.setColor(Color.magenta);
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
					// System.out.println("comName: "+comName);
					us.hms.systemsbiology.segmentedobject.Point[] pts = one.getComCoordinates(0);
					int ptsLen = pts.length;

					if (comName.trim().equalsIgnoreCase("Centroid")) // Draw a
																		// centroid
																		// with
																		// the
																		// single
																		// point
						for (int z = 0; z < ptsLen; z++)
							g2.fillOval((int) (scalingFactor * pts[z].x),
									(int) (scalingFactor * pts[z].y), radius,
									radius);
					else if (comName.trim().equalsIgnoreCase("BoundingBox")) // Draw
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
						// System.out.println("pts: "+pts.length);
						for (int z = 0; z < ptsLen; z++) {
							if (z % factor == 0) {
								us.hms.systemsbiology.segmentedobject.Point p = pts[z];
								g2.drawLine((int) (scalingFactor * p.x),
										(int) (scalingFactor * p.y),
										(int) (scalingFactor * p.x),
										(int) (scalingFactor * p.y));
							}
						}
					}
				}
				// else //Draw all points - used for outlines and the such
				// {
				// us.hms.systemsbiology.segmentedobject.Point[] pts = one.getComCoordinates_AllUnique();
				// int ptsLen = pts.length;
				// for (int z = 0; z < ptsLen; z++)
				// {
				// if (z%factor==0)
				// {
				// us.hms.systemsbiology.segmentedobject.Point p = pts[z];
				// g2.drawLine((int)(scalingFactor*p.x),
				// (int)(scalingFactor*p.y),
				// (int)(scalingFactor*p.x),(int)(scalingFactor*p.y));
				// }
				// }
				// }
			}
		}

		//
		//

		// drawing annotations
		// if (TheCells!=null)
		// {
		// int len = TheCells.length;
		// g2.setFont(main.MainGUI.Font_Standard);
		// float factor = 1;
		// if (scalingFactor==0.50f)
		// factor = 2f;
		// else if (scalingFactor==0.25f)
		// factor = 3f;
		// else if (scalingFactor==0.1f)
		// factor = 4f;
		//
		// for (int i = 0; i < len ;i++)
		// {
		// if (TheCells[i].isSelected())
		// {
		// g2.setColor(TheCells[i].getColor());
		// g2.setColor(Color.red);
		// //drawing detailed cell boundary if information exists
		// if (TheCells[i].getBoundaryPoints()!=null)
		// {
		// int num = TheCells[i].getBoundaryPoints().size();
		// for (int j =0; j < num ;j++)
		// {
		// if (j%factor==0)
		// {
		// Point p = (Point)TheCells[i].getBoundaryPoints().get(j);
		// g2.drawLine((int)(scalingFactor*p.x), (int)(scalingFactor*p.y),
		// (int)(scalingFactor*p.x),(int)(scalingFactor*p.y));
		// }
		// }
		// }
		//
		//
		// //If no detailed cell boundary, then draw a simple bounding box
		// around the cell
		// if (TheCells[i].getBoundaryPoints()==null &&
		// TheCells[i].getBoundingBox()!=null)
		// {
		// Rectangle box = TheCells[i].getBoundingBox();
		// int x = box.x;
		// int y = box.y;
		// int width = box.width;
		// int height = box.height;
		// g2.drawRect((int)(scalingFactor*x), (int)(scalingFactor*y),
		// (int)(scalingFactor*width),(int)(scalingFactor*height));
		// }
		//
		// //drawing nucleus boundary
		// if (TheCells[i].getNucleus().getBoundaryPoints()!=null)
		// {
		// g2.setColor(Color.magenta);
		// int num = TheCells[i].getNucleus().getBoundaryPoints().length;
		// for (int j =0; j < num ;j++)
		// {
		// if (j%factor==0)
		// {
		// us.hms.systemsbiology.segmentedobject.Point p =
		// TheCells[i].getNucleus().getBoundaryPoints()[j];
		// g2.drawLine((int)(scalingFactor*p.x), (int)(scalingFactor*p.y),
		// (int)(scalingFactor*p.x),(int)(scalingFactor*p.y));
		// }
		// }
		// }
		//
		// // Drawing neighbor lines
		// Line2D[] li = TheCells[i].getNeighborLines();
		// if (li!=null)
		// {
		// g2.setColor(Color.white);
		// int num = li.length;
		// for (int j = 0; j < num ;j++)
		// {
		// if
		// (Math.abs(li[j].getX1()-li[j].getX2())+Math.abs(li[j].getY1()-li[j].getY2())
		// < 30)
		// g2.drawLine((int)(scalingFactor*li[j].getX1()),
		// (int)(scalingFactor*li[j].getY1()),
		// (int)(scalingFactor*li[j].getX2()),(int)(scalingFactor*li[j].getY2()));
		// }
		// }
		//
		//
		//
		//
		//
		//
		//
		// }
		//
		// }
		//
		// }

		// Drawing the mini-plates
		int len = ThePlates.length;
		Plate platew = TheWell.getPlate();
		Plate platev = null;
		for (int i = 0; i < len; i++)
			if (ThePlates[i].getID() == platew.getID()) {
				platev = ThePlates[i];
				break;
			}

		int numR = platev.getTheWells().length;
		int numC = platev.getTheWells()[0].length;
		// Drawing the plate name above it
		g2.setColor(Color.WHITE);
		g2.setFont(MainGUI.Font_12);
		String st = "Plate #" + platev.getID();
		g2.drawString(st, (platev.getXstart() + (platev.getWidth() / 2 - st
				.length() * 5)), platev.getYstart() - 5);
		for (int r = 0; r < numR; r++)
			for (int c = 0; c < numC; c++) {
				if (r == TheWell.Row && c == TheWell.Column)
					platev.getTheWells()[r][c].setSelected(true);
				else
					platev.getTheWells()[r][c].setSelected(false);
				platev.getTheWells()[r][c].draw(g2);
			}

		g2.setFont(main.MainGUI.Font_12);
		g2.setColor(Color.white);
		if (pixelValue != -1)
			g2.drawString("Pixel Value = " + pixelValue, ThePlates[0]
					.getXstart() + 20, ThePlates[0].getYstart() + 140);
		if (areaValue != -1)
			g2.drawString("Pixel Area = " + areaValue,
					ThePlates[0].getXstart() + 20,
					ThePlates[0].getYstart() + 160);

		if (LineProfileValues_X != null && LineProfileValues_Y != null) {
			g2.setColor(Color.white);
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
			int xStart = platev.getXstart() + 50;
			int yStart = platev.getYstart() + 250;
			float yHeight = 30f;
			// mini scale requires smaller params
			if (scalingFactor == 0.25f || scalingFactor == 0.1f) {
				yStart = platev.getYstart() + 200;
				xLen = 20;
				yHeight = 20;
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
			g2.setFont(main.MainGUI.Font_8);
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
			xStart = platev.getYstart() + 250;
			yStart = platev.getXstart() + 50;

			if (scalingFactor == 0.25f) {
				xStart = platev.getYstart() + 200;
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

		// Finally drawing current highlighting
		paintHighlighting(g2, scalingFactor);

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

		g2.setColor(color);
		Composite orig = g2.getComposite();
		g2.setComposite(translucComposite);

		if (shapeToDraw != null) {
			g2.fill(shapeToDraw);
			g2.draw(shapeToDraw);
			g2.setComposite(orig);
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

			g2.setColor(highlightColor);
			Composite orig = g2.getComposite();
			g2.setComposite(translucComposite);

			g2.fill(shapeToDraw);
			g2.draw(shapeToDraw);
			g2.setComposite(orig);
		}

	}

	/**
	 * Returns the currently defined ROI if one exists
	 * 
	 * @author BLM
	 */
	public Shape getSelectedROI() {
		if (SelectedROI != null && width_shape * height_shape > 20) {
			return SelectedROI;
		}
		// if no region is selected, return the whole thing
		else
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
				if (shape.contains(p))
					TheField.setROIselected(i, !TheField.isROIselected(i));
			}
		}

		// if (TheCells!=null)
		// {
		// int inCell = getCellIndex(p1);
		// Cell_RAM cell = null;
		// if (inCell!=-1)
		// {
		// SelectedROI = null;
		// startHighlightPoint = null;
		// /** Things get messy when the user double/triple clicks, so lets just
		// consider single clicks*/
		// if (p1.getClickCount()!=1)return;
		// cell = TheCells[inCell];
		// cell.setSelected(!cell.isSelected());
		// inCell = -1;
		//
		// }
		// //checking to see if only one cell is selected
		// int len = TheCells.length;
		// boolean foundOne = false;
		// for (int i = 0; i < len; i++)
		// {
		// if (TheCells[i].isSelected())
		// {
		// if (foundOne)//found more than one selected cell
		// {
		// foundOne = false;
		// break;
		// }
		// else
		// foundOne = true;
		// }
		// }
		// if (foundOne)
		// {
		// foundOne=false;
		// if (cell!=null)
		// {
		// areaValue = cell.getPixelArea_wholeCell();
		// }
		//
		// repaint();
		// }
		// }

		// Seeing if clicked on the little plate in upper right corner in order
		// to change well/field
		int nr = ThePlates.length;
		for (int rr = 0; rr < nr; rr++) {

			int rows = ThePlates[rr].getTheWells().length;
			int cols = ThePlates[rr].getTheWells()[0].length;
			Point point = p1.getPoint();
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					if (ThePlates[rr].getTheWells()[r][c] != null)
						if (ThePlates[rr].getTheWells()[r][c].outline
								.contains(point)) {
							Well well = ThePlates[rr].getTheWells()[r][c];
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

	private int getCellIndex(MouseEvent p) {
		// float scale = FieldViewer_Frame.getScaling();
		// Point po = p.getPoint();
		//
		// int len = TheCells.length;
		// for (int i=0; i < len; i++)
		// {
		// Rectangle r = TheCells[i].getBoundingBox();
		// DummyBox.x = (int)(scale*(float)r.x);
		// DummyBox.y = (int)(scale*(float)r.y);
		// DummyBox.width = (int)(scale*(float)r.width);
		// DummyBox.height = (int)(scale*(float)r.height);
		// if (DummyBox.contains(po))
		// return i;
		// }
		return -1;
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
	 * Returns the raw image that is currently being displayed in a non-scaled
	 * formate
	 * 
	 * @author BLM
	 */
	public RenderedImage getTheCurrentImage() {
		return TheCurrentImage;
	}

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
	public Well getTheWell() {
		return TheWell;
	}

	/**
	 * Returns the source field that is being displayed in this viewer
	 * 
	 * @author BLM
	 */
	public Field getTheField() {
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
			if (HolderFrame.getShapeType() == FieldViewer_Frame.OVAL) {
				Rectangle bounds = getScaledSelectionBounds(scale);
				Ellipse2D.Float temp = new Ellipse2D.Float();
				temp.x = bounds.x;
				temp.y = bounds.y;
				temp.width = bounds.width;
				temp.height = bounds.height;
				shape = temp;
			} else if (HolderFrame.getShapeType() == FieldViewer_Frame.RECTANGLE) {
				Rectangle bounds = getScaledSelectionBounds(scale);
				Rectangle temp = new Rectangle();
				temp.x = bounds.x;
				temp.y = bounds.y;
				temp.width = bounds.width;
				temp.height = bounds.height;
				shape = temp;
			}

		if (shape != null && shape.contains(p1.getPoint())) {
			dX = p1.getX() - x_shape;
			dY = p1.getY() - y_shape;
			CreateNewBox = false;
		} else {
			startBox_XY = p1.getPoint();
			startHighlightPoint = p1.getPoint();
			CreateNewBox = true;
		}
		updatePanel();
	}

	public void mouseReleased(MouseEvent p1) {
		if (SelectedROI != null
				&& SelectedROI.getBounds().width
						* SelectedROI.getBounds().height > 6) {
			Raster rast = getCurrentRaster();
			int mean = getMeanValue(rast, SelectedROI);
			pixelValue = mean;
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
		if (CreateNewBox) {
			if (p1 == null || p1.getPoint() == null
					|| startHighlightPoint == null)
				return;
			int xval = p1.getPoint().x - startHighlightPoint.x;
			int yval = p1.getPoint().y - startHighlightPoint.y;
			//

			if (HolderFrame.getShapeType() == FieldViewer_Frame.RECTANGLE) {

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
								if (coords.getComSize() > 0) {
									us.hms.systemsbiology.segmentedobject.Point[] pts = coords
											.getComCoordinates(0);
									Rectangle bounds = getScaledSelectionBounds(scalingFactor);
									if (bounds != null && pts != null
											&& pts.length > 0)
										if (bounds
												.contains(
														(int) (scalingFactor * pts[0].x),
														(int) (scalingFactor * pts[0].y)))
											cell.setSelected(true);
										else
											cell.setSelected(false);
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

			} else if (HolderFrame.getShapeType() == FieldViewer_Frame.OVAL) {
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
							us.hms.systemsbiology.segmentedobject.Point[] pts = coords
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

				if (HolderFrame.getShapeType() == FieldViewer_Frame.OVAL) {

					Rectangle bounds = getScaledSelectionBounds(scalingFactor);
					if (tempOval == null)
						tempOval = new Ellipse2D.Float();
					tempOval.x = bounds.x;
					tempOval.y = bounds.y;
					tempOval.width = bounds.width;
					tempOval.height = bounds.height;

					tempOval.x = p1.getX() - tempOval.width / 2f;
					tempOval.y = p1.getY() - tempOval.height / 2f;

					// seeing if box has highlighted any cells
					if (TheCellBank != null) {
						ArrayList<Cell> cells = TheCellBank.getCells();
						int len = cells.size();
						for (int c = 0; c < len; c++) {

							Cell cell = cells.get(c);
							CellCoordinates coords = cell.getCoordinates();
							if (coords.getComSize() > 0) {
								us.hms.systemsbiology.segmentedobject.Point[] pts = coords
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
				} else if (HolderFrame.getShapeType() == FieldViewer_Frame.RECTANGLE) {
					Rectangle bounds = getScaledSelectionBounds(scalingFactor);
					if (tempRect == null)
						tempRect = new Rectangle();

					tempRect.x = bounds.x;
					tempRect.y = bounds.y;
					tempRect.width = bounds.width;
					tempRect.height = bounds.height;

					tempRect.x = (int) (p1.getX() - tempRect.width / 2f);
					tempRect.y = (int) (p1.getY() - tempRect.height / 2f);

					// seeing if box has highlighted any cells
					if (TheCellBank != null) {
						ArrayList<Cell> cells = TheCellBank.getCells();
						int len = cells.size();
						for (int c = 0; c < len; c++) {

							Cell cell = cells.get(c);
							CellCoordinates coords = cell.getCoordinates();
							if (coords.getComSize() > 0) {
								us.hms.systemsbiology.segmentedobject.Point[] pts = coords
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
		if (HolderFrame.getShapeType() == FieldViewer_Frame.RECTANGLE)
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

		if (type == FieldViewer_Frame.RECTANGLE) {
			SelectedROI = shape;
			tempRect = (Rectangle) shape;
			HolderFrame.setShapeType(FieldViewer_Frame.RECTANGLE);
			CreateNewBox = false;
			x_shape = tempRect.x;
			y_shape = tempRect.y;
			width_shape = tempRect.width;
			height_shape = tempRect.height;
		} else if (type == FieldViewer_Frame.OVAL) {
			SelectedROI = shape;
			tempOval = (Ellipse2D.Float) shape;
			HolderFrame.setShapeType(FieldViewer_Frame.OVAL);
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

	/** Rescales the image with a new max-value */
	public class SliderListener_RescaleDisplay implements ChangeListener {
		public void stateChanged(ChangeEvent e) {

			JSlider j = (JSlider) e.getSource();
			int channel = ImageSelectionSlider.getValue();
			if (j.getName().equalsIgnoreCase("Max")
					&& !MaxValueSlider.getValueIsAdjusting())
				MainGUI.getGUI().getMaxValues_ImageDisplay()[channel] = MaxValueSlider
						.getValue();
			else if (j.getName().equalsIgnoreCase("Min")
					&& !MinValueSlider.getValueIsAdjusting())
				MainGUI.getGUI().getMinValues_ImageDisplay()[channel] = MinValueSlider
						.getValue();

			RenderedImage newImage = rescale(TheCurrentImage,
					ImageSelectionSlider.getValue());
			set(newImage);
			TheDisplayedImage = newImage;

			ThePanel.repaint();

		}
	}

	// This method creates a surrogate image -- one which is
	// used to display data which originally was outside the
	// [0-255] range.
	static public RenderedImage createSurrogate(RenderedImage image, double maxValue) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		RenderedOp extrema = JAI.create("extrema", pb);
		// Must get the extrema of all bands.
		double[] allMins = (double[]) extrema.getProperty("minimum");
		// double[] allMaxs = (double[])extrema.getProperty("maximum");
		double minValue = allMins[0];
		// double maxValue = allMaxs[0];
		for (int v = 1; v < allMins.length; v++) {
			if (allMins[v] < minValue)
				minValue = allMins[v];
			// if (allMaxs[v] > maxValue) maxValue = allMaxs[v];
		}
		// Rescale the image with the parameters
		double[] multiplyBy = new double[1];
		multiplyBy[0] = MainGUI.MAXPIXELVALUE / (maxValue - minValue);
		double[] addThis = new double[1];
		addThis[0] = -minValue * multiplyBy[0];
		// Now we can rescale the pixels gray levels:
		ParameterBlock pbSub = new ParameterBlock();
		pbSub.addSource(image);
		pbSub.add(addThis);
		RenderedImage surrogateImage = (PlanarImage) JAI.create(
				"subtractconst", pbSub, null);
		ParameterBlock pbMult = new ParameterBlock();
		pbMult.addSource(surrogateImage);
		pbMult.add(multiplyBy);
		surrogateImage = (PlanarImage) JAI
				.create("multiplyconst", pbMult, null);
		// // Let's convert the data type for displaying.
		// pb = new ParameterBlock();
		// pb.addSource(surrogateImage);
		// pb.add(DataBuffer.TYPE_BYTE);
		// surrogateImage = JAI.create("format", pb);
		return surrogateImage;
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

			MaxValueSlider.setValue((int) MainGUI.getGUI()
					.getMaxValues_ImageDisplay()[channel]);
			MinValueSlider.setValue((int) MainGUI.getGUI()
					.getMinValues_ImageDisplay()[channel]);

			// Now we can rescale the pixels gray levels:
			RenderedImage newImage = rescale(TheCurrentImage,
					ImageSelectionSlider.getValue());

			set(newImage);
			TheDisplayedImage = newImage;

			ThePanel.repaint();

		}
	}

	static public RenderedImage rescale(RenderedImage input, int channel) {
		int minValue = (int) MainGUI.getGUI().getMinValues_ImageDisplay()[channel];
		int maxValue = (int) MainGUI.getGUI().getMaxValues_ImageDisplay()[channel];
		// Subtracting basline
		double[] subtract = new double[1];
		subtract[0] = minValue;
		// Now we can rescale the pixels gray levels:
		ParameterBlock pbSub = new ParameterBlock();
		pbSub.addSource(input);
		pbSub.add(subtract);
		RenderedImage surrogateImage = (PlanarImage) JAI.create("subtractconst", pbSub, null);

		return createSurrogate(surrogateImage, maxValue);

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
		MaxValueSlider.setValue((int) MainGUI.getGUI()
				.getMaxValues_ImageDisplay()[channel]);
		MinValueSlider.setValue((int) MainGUI.getGUI()
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

		if (HolderFrame.getShapeType() == FieldViewer_Frame.OVAL) {
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
				System.out.println("--> Processing Well = " + TheWell.name);
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
		} else if (HolderFrame.getShapeType() == FieldViewer_Frame.RECTANGLE) {

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
				System.out.println("--> Processing Well = " + TheWell.name);
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
		}
		return shape;
	}

	/**
	 * Free up RAM
	 * 
	 * @author BLM
	 */
	public void kill() {
		translucComposite = null;
		TheDotSelectionListener.kill();
		DummyPoint = null;
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
	}
}
