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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Field;
import models.Model_Main;
import models.Model_Plate;
import models.Model_Well;


public class ImageMontage extends JFrame implements WindowListener {
	public Model_Plate ThePlate;
	private JFrame TheFrame;
	private DisplayPanel TheDisplayPanel;
	public BufferedImage[][][] TheImageArray;
	static public int ALL = -1;
	private int cols_display;
	private int rows_display;
	private int maxNumFields;
	private int YBUFFER = 0;
	private Thread thread;
	private JSlider MaxValueSlider;
	private JSlider MinValueSlider;
	public int ChannelSelected;
	public int FieldSelected;
	private int imHeight;
	private int imWidth;
	private float scale;
	private JScrollPane TheScrollPane;
	private int totalWidth = 0;
	private int totalHeight = 0;
	private JPanel sliderPanel;
	private float lastScrollValueX;
	private float lastScrollValueY;
	
	public ImageMontage(Model_Plate plate, int channelIndex, int fieldIndex)
	{
		scale = 0.1f;
		ThePlate = plate;
		Model_Well[][] wells = plate.getWells();
		getContentPane().setLayout(new BorderLayout());
		TheFrame = this;
		Dimension dim = new Dimension(1300, 868);
		setPreferredSize(dim);
		setSize(dim);
		ChannelSelected = channelIndex;
		FieldSelected = fieldIndex;
		imHeight = -1;
		imWidth = -1;
		MaxValueSlider = new JSlider(0, Model_Main.MAXPIXELVALUE,
				Model_Main.MAXPIXELVALUE);
		MinValueSlider = new JSlider(0, Model_Main.MAXPIXELVALUE, 0);
		// Adding the intensity sliders
		MaxValueSlider.setToolTipText("Maximum");
		MaxValueSlider.setName("Max");
		MaxValueSlider.setOrientation(JSlider.VERTICAL);
		SliderListener_RescaleDisplay listener = new SliderListener_RescaleDisplay();
		MaxValueSlider.addChangeListener(listener);

		MinValueSlider.setEnabled(false);
		MinValueSlider.setToolTipText("Minimum");
		MinValueSlider.setName("Min");
		MinValueSlider.setPaintTicks(false);
		MinValueSlider.setPaintLabels(false);
		MinValueSlider.setOrientation(JSlider.VERTICAL);
		MinValueSlider.addChangeListener(listener);
		setLayout(new BorderLayout());
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 2));
		sliderPanel.add(MinValueSlider, 0);
		sliderPanel.add(MaxValueSlider, 1);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sliderPanel, BorderLayout.EAST);

		TheDisplayPanel = new DisplayPanel(wells, channelIndex, fieldIndex);
		TheScrollPane = new JScrollPane(TheDisplayPanel);

		JScrollBar verticalScrollBar = TheScrollPane.getVerticalScrollBar();
		JScrollBar horizScrollBar = TheScrollPane.getHorizontalScrollBar();
		lastScrollValueX = horizScrollBar.getValue();
		lastScrollValueY = verticalScrollBar.getValue();

		getContentPane().add(TheScrollPane, BorderLayout.CENTER);

		TheFrame.setVisible(true);
		addWindowListener(this);

		// Setting up the Menubar
		//
		JMenuBar TheMenuBar = new JMenuBar();
		JMenu FileMenu = new JMenu("File");
		JMenu OptionsMenu = new JMenu("Options");

		TheMenuBar.add(FileMenu);
		TheMenuBar.add(OptionsMenu);
		setJMenuBar(TheMenuBar);

		JMenuItem item = new JMenuItem("Close");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				killAll();
				setVisible(false);
			}
		});
		FileMenu.add(item);

		// Field Selection
		JMenu mItem = new JMenu("Field");
		OptionsMenu.add(mItem);
		ButtonGroup bg1 = new ButtonGroup();
		for (int i = 0; i < maxNumFields; i++) {
			item = new JCheckBoxMenuItem("Field_" + i);
			final int index = i;
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					FieldSelected = index;
					// TheDisplayPanel = new DisplayPanel(ThePlate.getWells(),
					// ChannelSelected, FieldSelected);
					TheDisplayPanel.initDisplay();
					TheDisplayPanel.repaint();
					TheFrame.validate();
					TheFrame.repaint();
				}
			});
			mItem.add(item);
			bg1.add(item);
			if (i == 0)
				item.setSelected(true);
		}
		item = new JCheckBoxMenuItem("All");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				FieldSelected = ALL;
				// TheDisplayPanel = new DisplayPanel(ThePlate.getWells(),
				// ChannelSelected, FieldSelected);
				TheDisplayPanel.initDisplay();
				TheDisplayPanel.repaint();
				TheFrame.validate();
				TheFrame.repaint();
			}
		});
		mItem.add(item);
		bg1.add(item);


		// Channel Selection
		mItem = new JMenu("Channel");
		ButtonGroup bg = new ButtonGroup();
		String[] names = models.Model_Main.getModel().getTheChannelNames();
		OptionsMenu.add(mItem);
		for (int i = 0; i < names.length; i++) {
			item = new JCheckBoxMenuItem("" + names[i]);
			final int index = i;
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					ChannelSelected = index;
					// TheDisplayPanel = new DisplayPanel(ThePlate.getWells(),
					// ChannelSelected, FieldSelected);
					TheDisplayPanel.initDisplay();
					TheDisplayPanel.repaint();
					TheFrame.validate();
					TheFrame.repaint();

				}
			});
			bg.add(item);
			mItem.add(item);
			if (i == 0)
				item.setSelected(true);
		}

		validate();
		repaint();

	}
	

	

	private RenderedImage rescale(RenderedImage input, int min, int max) {
		int minValue = min;
		int maxValue = max;
		// Subtracting basline
		double[] subtract = new double[1];
		subtract[0] = minValue;
		// Now we can rescale the pixels gray levels:
		ParameterBlock pbSub = new ParameterBlock();
		pbSub.addSource(input);
		pbSub.add(subtract);
		RenderedImage surrogateImage = (PlanarImage) JAI.create(
				"subtractconst", pbSub, null);

		return createSurrogate(surrogateImage, maxValue);

	}

	// This method creates a surrogate image -- one which is
	// used to display data which originally was outside the
	// [0-255] range.
	private RenderedImage createSurrogate(RenderedImage image,
			double maxValue) {
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
		multiplyBy[0] = Model_Main.MAXPIXELVALUE / (maxValue - minValue);
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

		return surrogateImage;
	}

	
	private RenderedImage resizeImage(RenderedImage src, float scaleX,
			float scaleY) {

		ParameterBlock pb2 = new ParameterBlock();
		pb2.addSource(src);
		pb2.add((float) scaleX);
		pb2.add((float) scaleY);
		pb2.add(0.0f);
		pb2.add(0.0f);
		return JAI.create("scale", pb2, null);

	}
	
	/** 
	 * 
	 * 
	 * 
	 * */
	private class DisplayPanel extends JPanel implements MouseWheelListener,
			Runnable, MouseListener, MouseMotionListener {

		public DisplayPanel TheDisplayPanel;
		private boolean Dragging;
		private Thread runnerThread;
		private Model_Well[][] wells;
		private boolean initializing;
		private int numWellsWithFields;
		private int numWellsDone;
		private Point startPoint;
		private Point mouseMovePoint;


		public DisplayPanel(Model_Well[][] wells, int channelIndex,
				int fieldIndex) {

			FieldSelected = fieldIndex;
			Dragging = false;
			ChannelSelected = channelIndex;
			MaxValueSlider.setValue((int) models.Model_Main.getModel()
					.getMaxValues_ImageDisplay()[ChannelSelected]);
			MinValueSlider.setValue((int) models.Model_Main.getModel()
					.getMinValues_ImageDisplay()[ChannelSelected]);

			numWellsWithFields = 0;
			for (int r = 0; r < wells.length; r++)
				for (int c = 0; c < wells[0].length; c++)
					if (wells[r][c].getFields() != null
							&& wells[r][c].getFields().length > 0)
						numWellsWithFields++;

			numWellsDone = 0;
			initializing = true;
			this.wells = wells;
			addMouseWheelListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);

			TheDisplayPanel = this;
			TheDisplayPanel.setBackground(Color.black);

			initDisplay();
		}

		public void initDisplay() {
			start();
		}

		public void start() {
			thread = new Thread(this);
			thread.start();
		}

		public void run() {
			totalHeight = 0;
			totalWidth = 0;
			int rows = wells.length;
			int cols = wells[0].length;
			int[][] widths = new int[rows][cols];
			int[][] heights = new int[rows][cols];
			maxNumFields = ThePlate.getMaxNumberOfFields();


			// if (rows * cols == 384)
			// scale = 0.5f;

			if (scale < 0.1f)
				scale = 0.1f;

			if (FieldSelected != ALL)
 {
				TheImageArray = new BufferedImage[rows][cols][1];

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {

					if (wells[r][c].getFields() != null
							&& wells[r][c].getFields().length > 0)
					{
							File f = wells[r][c].getFields()[FieldSelected]
									.getImageFile(ChannelSelected);
							RenderedImage im = JAI.create("fileload", f.getAbsolutePath());
						RenderedImage im2 = rescale(
									resizeImage(im, scale, scale),
									0,
								(int) models.Model_Main.getModel()
											.getMaxValues_ImageDisplay()[ChannelSelected]);
					widths[r][c] = im2.getWidth();
					heights[r][c] = im2.getHeight();
		
							TheImageArray[r][c][0] = tools.ImageTools
							.convertRenderedImage(im2);

							numWellsDone++;
					}

						repaint();
						TheFrame.validate();
						TheFrame.repaint();
					}
				}
				cols_display = 1;
				rows_display = 1;
			}
 else // Displaying all fields
			{
				// Calc number of display rows and cols for each well (make a
				// square grid)
				cols_display = 0;
				rows_display = 0;
				if (maxNumFields == 2) {

					cols_display = 2;
					rows_display = 1;
				} else if (maxNumFields == 4 || maxNumFields == 3) {
					cols_display = 2;
					rows_display = 2;
				} else if (maxNumFields > 4 && maxNumFields < 10) {
					cols_display = 3;
					rows_display = 3;
				}
				if (maxNumFields == 1) {
					cols_display = 1;
					rows_display = 1;
				}

				TheImageArray = new BufferedImage[rows][cols][maxNumFields];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {

							if (wells[r][c].getFields() != null
									&& wells[r][c].getFields().length > 0) {
								Model_Field[] fields = wells[r][c].getFields();
								if (fields != null) {
								int nf = fields.length;

								for (int i = 0; i < nf; i++) {
									File f = fields[i]
											.getImageFile(ChannelSelected);
									RenderedImage im = JAI.create("fileload", f
											.getAbsolutePath());
									RenderedImage im2 = rescale(
											resizeImage(im, scale
													/ rows_display, scale
													/ cols_display),
											0,
											(int) models.Model_Main
													.getModel()
													.getMaxValues_ImageDisplay()[ChannelSelected]);
									widths[r][c] = im2.getWidth();
									heights[r][c] = im2.getHeight();
		
									TheImageArray[r][c][i] = tools.ImageTools
											.convertRenderedImage(im2);
									System.out.println(wells[r][c].name
											+ "  f_" + i);
								}
								System.out.println();
						}
						}
						numWellsDone++;
						repaint();
						TheFrame.validate();
						TheFrame.repaint();
					}
				}

			}
			
			// Computing maxWidth & maxHeights for entire ThePlate
			imWidth = -1;
			imHeight = -1;
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {

					if (widths[r][c] != 0) {
						if (imWidth == -1)
							imWidth = widths[r][c];
						if (imHeight == -1)
							imHeight = heights[r][c];
					}
				}
			}
			totalHeight = rows * imHeight * rows_display;
			totalWidth = cols * imWidth * cols_display;

			Dimension dim2 = new Dimension(totalWidth, totalHeight);

			TheDisplayPanel.setPreferredSize(dim2);
			TheDisplayPanel.revalidate();
			TheScrollPane.revalidate();

			initializing = false;
			TheDisplayPanel.repaint();
		}
		


		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.black);
			g2.fillRect(0, 0, getWidth(), getHeight());

			if (initializing)
 {


				int barWidth = 100;
				int barHeight = 10;
				int progressWidth = (int) (barWidth * getFractionOfWellsDone());
				int xs = getWidth() / 2 - 30;
				int ys = getHeight() / 2 - 10;


				g2.setColor(Color.white);
				g2.drawRect(xs, ys + 5, barWidth, barHeight);

				Paint paint = g2.getPaint();
				// Drawing the empty progress bar
				GradientPaint gradient = new GradientPaint(xs, ys, Color.white,
						xs, ys + (float) barHeight / 2.5f,
 Color.darkGray, true);
				g2.setPaint(gradient);

				g2.fillRect(xs, ys + 5, progressWidth, barHeight);

				g2.setPaint(paint);
				Font f = g2.getFont();
				g2.setFont(gui.MainGUI.Font_16);
				g2.drawString("Loading Images", xs - 10, ys);
				g2.setFont(f);
				g2.setColor(Color.BLACK);
				return;
			}

			int xOffset = 0;
			int yOffset = 0;
			int rows = TheImageArray.length;
			int cols = TheImageArray[0].length;
			Model_Well[][] wells = ThePlate.getWells();
			if (FieldSelected != ALL)
 {

				for (int r = 0; r < rows; r++) {
					xOffset = 0;
					for (int c = 0; c < cols; c++) {



						if (TheImageArray[r][c] != null
								&& TheImageArray[r][c][0] != null)
							g2.drawImage(TheImageArray[r][c][0],
									null, xOffset,
									yOffset + YBUFFER);
						// Draw well name
						g2.setColor(Color.black);
						g2.fillRect(xOffset + 9, yOffset + 3 + YBUFFER, 28, 14);
						g2.setColor(Color.gray);
						g2.drawString(wells[r][c].name, xOffset + 10, yOffset
								+ 15 + YBUFFER);
						xOffset += imWidth;

					}
					yOffset += imHeight;
				}
			}
 else { // drawing all fields

				int well_yOff = 0;
				int well_xOff = 0;
				int field_yOff = 0;
				int field_xOff = 0;


				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {

						well_xOff = c * imWidth * cols_display;
						well_yOff = r * imHeight * rows_display;

						for (int i = 0; i < maxNumFields; i++) {

							field_xOff = 0;
							field_yOff = 0;
							//Positioning the field 

							//
							// 2 x 1
							//
							if (cols_display ==2 && rows_display==1) {
								if (i == 1) {
									field_xOff += (float) imWidth;
								}
							}
							//
							// 2 x 2
							//
							else if (cols_display ==2 && rows_display==2) {
								if (i == 0) {
									field_yOff += (float) imHeight;

								} else if (i == 1)
								{
									field_xOff += (float) imWidth;
									field_yOff += (float) imHeight;

								}
 else if (i == 2) {
									field_xOff += (float) imWidth;

								} else if (i == 3)
								{

								}

							}
							


							if (TheImageArray[r][c] != null
									&& TheImageArray[r][c][i] != null)
								g2.drawImage(TheImageArray[r][c][i], null,
										well_xOff + field_xOff, well_yOff
												+ field_yOff + YBUFFER);

						}

						// Draw well name
						g2.setColor(Color.black);
						g2.fillRect(well_xOff + 9, well_yOff + 3 + YBUFFER, 28,
								14);
						g2.setColor(Color.gray);
						g2.drawString(wells[r][c].name, well_xOff + 10,
								well_yOff + 15 + YBUFFER);

					}

				}
			}
			
			//
			// Drawing the grid lines
			//
			yOffset = 0;
			g2.setColor(Color.darkGray);
			for (int r = 0; r < rows; r++) {
				g2
						.drawLine(0, yOffset + YBUFFER, totalWidth, yOffset
								+ YBUFFER);
				yOffset += imHeight * rows_display;
			}
			xOffset = 0;
			for (int c = 0; c < cols; c++) {
				g2.drawLine(xOffset, 0 + YBUFFER, xOffset, totalHeight
						+ YBUFFER);
				xOffset += imWidth * cols_display;
			}
			g2.drawLine(0, yOffset + YBUFFER, totalWidth, yOffset + YBUFFER);
			g2.drawLine(xOffset, 0 + YBUFFER, xOffset, totalHeight + YBUFFER);

	}

		public void mouseWheelMoved(MouseWheelEvent e) {
		

			 int notches = e.getWheelRotation();
			int sign = 1;
			if (notches > 0) {
				sign = -1;
			}
			 if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				int numClicks = e.getScrollAmount();
				scale = scale + (numClicks * 0.1f) * sign;
				if (scale<=0)
					scale = 0.1f;
			 }
			 
			TheDisplayPanel.initDisplay();
			TheDisplayPanel.repaint();

			JScrollBar verticalScrollBar = TheScrollPane.getVerticalScrollBar();
			JScrollBar horizScrollBar = TheScrollPane.getHorizontalScrollBar();

			// Point pInImage = new
			// Point((int)(mouseMovePoint.x+lastScrollValueX),
			// (int)(mouseMovePoint.y+lastScrollValueY));

			verticalScrollBar
					.setValue((int) (lastScrollValueY + mouseMovePoint.y));
			horizScrollBar
					.setValue((int) (lastScrollValueX + mouseMovePoint.x));

			lastScrollValueX = horizScrollBar.getValue();
			lastScrollValueY = verticalScrollBar.getValue();


			TheFrame.validate();
			TheFrame.repaint();



		}

		/**
		 * Returns the number of wells done being processed
		 * 
		 * @author BLM
		 */
		public float getFractionOfWellsDone() {
			return (float) numWellsDone / (float) numWellsWithFields;
		}

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

			mouseMovePoint = e.getPoint();

		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			Dragging = true;
			startPoint = e.getPoint();
			JScrollBar verticalScrollBar = TheScrollPane.getVerticalScrollBar();
			JScrollBar horizScrollBar = TheScrollPane.getHorizontalScrollBar();
			lastScrollValueX = horizScrollBar.getValue();
			lastScrollValueY = verticalScrollBar.getValue();
		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseDragged(MouseEvent e) {

			JScrollBar verticalScrollBar = TheScrollPane.getVerticalScrollBar();
			JScrollBar horizScrollBar = TheScrollPane.getHorizontalScrollBar();

			verticalScrollBar
					.setValue((int) (lastScrollValueY + startPoint.y - e
							.getPoint().y));
			horizScrollBar.setValue((int) (lastScrollValueX + startPoint.x - e
					.getPoint().x));

			lastScrollValueX = horizScrollBar.getValue();
			lastScrollValueY = verticalScrollBar.getValue();
		}

		public void mouseMoved(MouseEvent e) {

		}

	}

	public void windowOpened(WindowEvent p1) {
	}

	public void windowClosing(WindowEvent p1) {
			killAll();
		System.gc();
	}

	public void windowClosed(WindowEvent p1) {
			killAll();
			System.gc();
	}

	public void windowIconified(WindowEvent p1) {
	}

	public void windowDeiconified(WindowEvent p1) {
	}

	public void windowActivated(WindowEvent p1) {
	}

	public void windowDeactivated(WindowEvent p1) {
	}

		public void killAll() {
			int len = TheImageArray.length;
			int num = TheImageArray[0].length;
			for (int r = 0; r < len; r++)
				for (int j = 0; j < num; j++)
					for (int i = 0; i < TheImageArray[i][j].length; i++) {
						TheImageArray[r][j][i] = null;
					}
		
			TheImageArray = null;
		System.gc();
		}



	/** Rescales the image with a new max-value */
	public class SliderListener_RescaleDisplay implements ChangeListener {
		public void stateChanged(ChangeEvent e) {

			JSlider j = (JSlider) e.getSource();
			int channel = ChannelSelected;
			if (j.getName().equalsIgnoreCase("Max")
					&& !MaxValueSlider.getValueIsAdjusting()) {
				models.Model_Main.getModel().getMaxValues_ImageDisplay()[channel] = MaxValueSlider
						.getValue();
				TheDisplayPanel = new DisplayPanel(ThePlate.getWells(),
						ChannelSelected, FieldSelected);
				TheDisplayPanel.repaint();
				TheFrame.validate();
				TheFrame.repaint();
			} else if (j.getName().equalsIgnoreCase("Min")
					&& !MinValueSlider.getValueIsAdjusting()) {
				models.Model_Main.getModel().getMinValues_ImageDisplay()[channel] = MinValueSlider
						.getValue();
				TheDisplayPanel = new DisplayPanel(ThePlate.getWells(),
						ChannelSelected, FieldSelected);
				TheDisplayPanel.repaint();
				TheFrame.validate();
				TheFrame.repaint();
			}

			}
	}
}
