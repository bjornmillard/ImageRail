/**
 * Cell.java
 *
 * @author Created by BLM
 */

package tempObjects;

import gui.MainGUI;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import segmentors.Temp_Pixel;

public class Cell_RAM {
	static public final int MEAN = 0;
	static public final int INTEGRATED = 1;
	private int ID;
	private File[] FilesOfOrigin;
	private int FieldNumber;
	private Nucleus nucleus;
	private Cytoplasm cytoplasm;
	private ArrayList<Point> boundaryPoints;
	private boolean selected;
	private Color color;
	private Cell_RAM[] NeighborCells;
	private Rectangle boundingBox;
	private ArrayList neighborCellIDs;
	private Line2D[] neighborLines;
	private Cell_RAM[] allCells;
	private float[] backgroundValues;
	private int SourceImage_Width;
	private int SourceImage_Height;

	public Cell_RAM(Nucleus nucleus_, Cytoplasm cytoplasm_) {
		ID = (int) (Math.random() * 100000000);
		nucleus = nucleus_;
		cytoplasm = cytoplasm_;
		color = new Color((float) Math.random(), (float) Math.random(),
				(float) Math.random());
		neighborCellIDs = new ArrayList();
	}

	/**
	 * Returns the background values of the image this cell was found. It is of
	 * length N, where N = number of channels in the field this cell was
	 * discovered.
	 * 
	 * @author BLM
	 */
	public float[] getBackgroundValues() {
		return backgroundValues;
	}

	/**
	 * Returns all the neighbor cells of this cell if they have been determined
	 * by an algorithm
	 * 
	 * @author BLM
	 */
	public Cell_RAM[] getNeighborCells() {
		return NeighborCells;
	}

	/**
	 * Sets all the neighbor cells of this cell if they have been determined by
	 * an algorithm
	 * 
	 * @author BLM
	 */
	public void setNeighborCells(Cell_RAM[] neighborCells) {
		NeighborCells = neighborCells;
	}

	/**
	 * Returns the bounding box around this cell centered at its Centroid
	 * 
	 * @author BLM
	 */
	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Sets the bounding box of this cell
	 * 
	 * @author BLM
	 */
	public void setBoundingBox(Rectangle box) {
		boundingBox = box;
	}

	/**
	 * Returns the color of this cell
	 * 
	 * @author BLM
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color variable of this cell
	 * 
	 * @author BLM
	 * **/
	public void setColor(Color color_) {
		color = color_;
	}

	/**
	 * Returns whether this cell has been selected
	 * 
	 * @author BLM
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the cell to be selected or not
	 * 
	 * @author BLM
	 */
	public void setSelected(boolean boo) {
		selected = boo;
	}

	/**
	 * Returns the boundary points in an arrayList demarking the cytoplasmic
	 * boundary of the cell
	 * 
	 * @author BLM
	 */
	public ArrayList<Point> getBoundaryPoints() {
		return boundaryPoints;
	}

	/**
	 * Sets the boundary points in an arrayList demarking the cytoplasmic
	 * boundary of the cell
	 * 
	 * @author BLM
	 */
	public void setBoundaryPoints(ArrayList<Point> points) {
		boundaryPoints = points;
	}

	/**
	 * Returns the cytoplasm object of this cell
	 * 
	 * @author BLM
	 */
	public Cytoplasm getCytoplasm() {
		return cytoplasm;
	}

	/**
	 * Sets the cytoplasm object of this cell
	 * 
	 * @author BLM
	 */
	public void setCytoplasm(Cytoplasm cytoplasm_) {
		cytoplasm = cytoplasm_;
	}

	/**
	 * Returns the nucleus object of this cell
	 * 
	 * @author BLM
	 */
	public Nucleus getNucleus() {
		return nucleus;
	}

	/**
	 * Sets the nucleus object of this cell
	 * 
	 * @author BLM
	 */
	public void setNucleus(Nucleus nucleus_) {
		nucleus = nucleus_;
	}

	/**
	 * Gets the image files this cell came from
	 * 
	 * @author BLM
	 */
	public File[] getFilesOfOrigin() {
		return FilesOfOrigin;
	}

	/**
	 * Sets the image files this cell came from
	 * 
	 * @author BLM
	 */
	public void setFilesOfOrigin(File[] files) {
		FilesOfOrigin = files;
	}

	public void setSourceImageWidth(int width) {
		SourceImage_Width = width;
	}

	public int getSourceImageWidth() {
		return SourceImage_Width;
	}

	public void setSourceImageHeight(int height) {
		SourceImage_Height = height;
	}

	public int getSourceImageHeight() {
		return SourceImage_Height;
	}

	/**
	 * Sets the Model_Field number that this cell came from
	 * 
	 * @author BLM
	 */
	public void setFieldNumber(int field_) {
		FieldNumber = field_;
	}

	/**
	 * Returns the field number that this cell came from
	 * 
	 * @author BLM
	 */
	public int getFieldNumber() {
		return FieldNumber;
	}

	/**
	 * Returns Cell ID
	 * 
	 * @author BLM
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Sets Cell ID
	 * 
	 * @author BLM
	 */
	public void setID(int id) {
		ID = id;
	}

	/**
	 * Clears the pixel coordinates representing this cell
	 * 
	 * @author BLM
	 */
	public void clearPixelData() {
		boundaryPoints = null;
		cytoplasm.clearPixelData();
		nucleus.clearPixelData();
	}

	/**
	 * Sets a list of all the cells that were in the same image as this cell
	 * 
	 * @author BLM
	 */
	public void setAllCells(Cell_RAM[] cells) {
		allCells = cells;
	}

	/**
	 * Returns a list of all the cells that were in the same image as this cell
	 * 
	 * @author BLM
	 */
	public Cell_RAM[] getAllCells() {
		return allCells;
	}

	/**
	 * Sets up the mean value float[] arrays (length==numChannels) to store
	 * values for this cell
	 * 
	 * @author BLM
	 */
	public void initNumChannels(int numChannels) {
		cytoplasm.initNumChannels(numChannels);
	}

	/**
	 * Sets the mean background values found in the image that this cell was
	 * found in. This is used for bkgd subtraction
	 * 
	 * @author BLM
	 */
	public void setBackgroundValues(float[] bkgdValues) {
		int num = bkgdValues.length;
		backgroundValues = new float[num];
		for (int i = 0; i < num; i++)
			backgroundValues[i] = bkgdValues[i];
	}

	/**
	 * Sets the integrated cytoplasm value for this cell of the given index
	 * 
	 * @author BLM
	 */
	// public void setChannelValue(float val, int index, int TYPE)
//	{
	// if (TYPE == Cell.INTEGRATED)
	// channelValues_cyto_integrated[index] = val;
//	}

	/**
	 * Getting the cell intensity values. Note that we are storing the
	 * integrated intensities not mean values... we compute means now
	 * 
	 * @author BLM
	 */
	public double getChannelValue_wholeCell_mean(int index) {
		if (cytoplasm == null
				|| cytoplasm.getChannelValues_integrated() == null
				|| index >= cytoplasm.getChannelValues_integrated().length)
			return 0;

		return ((cytoplasm.getChannelValue(index, INTEGRATED) + nucleus
				.getChannelValue(index, INTEGRATED)) / (cytoplasm
				.getNumPixels() + nucleus.getNumPixels()));
	}

	public double getChannelValue_wholeCell_integrated(int index) {
		if (cytoplasm == null
				|| cytoplasm.getChannelValues_integrated() == null
				|| index >= cytoplasm.getChannelValues_integrated().length)
			return 0;

		return ((cytoplasm.getChannelValue(index, INTEGRATED) + nucleus
				.getChannelValue(index, INTEGRATED)));
	}

	/**
	 * Returns the value of this cell of feature index given and either MEAN
	 * VALUE or INTEGRATED intensity
	 * 
	 * @author BLM
	 */
	// public double getChannelValue(int index, int TYPE)
//	{
	// if (TYPE == Cell.MEAN)
	// {
	// if (index>=cytoplasm.channelValues_integrated.length)
	// return 0;
	//
	// return cytoplasm.channelValues_integrated[index]/(float)numPixels;
	// }
	// else if (TYPE == Cell.INTEGRATED)
//		{
	// if (index>=cytoplasm.channelValues_integrated.length)
	// return 0;
	// return cytoplasm.channelValues_integrated[index];
//		}
	// return 0;
//	}

	public Point getCentroid() {
		Point p = null;
		if (boundingBox == null)
			return p;

		int x = boundingBox.x;
		int y = boundingBox.y;
		int width = boundingBox.width;
		int height = boundingBox.height;

		p = new Point((x + width / 2), (y + height / 2));

		return p;
	}

	public void setNeighborsFromCellIDs(Cell_RAM[] allCells) {
		int numC = allCells.length;
		int numN = neighborCellIDs.size();
		NeighborCells = new Cell_RAM[numN];
		int counter = 0;
		for (int i = 0; i < numN; i++) {
			boolean foundIt = false;
			int id = ((Integer) neighborCellIDs.get(i)).intValue();
			for (int n = 0; n < numC; n++) {
				if (id == allCells[n].nucleus.getID()) {
					NeighborCells[counter] = allCells[n];
					counter++;
					foundIt = true;
					break;
				}
			}

		}
	}

	/**
	 * Returns the whole cell pixel area (numPixels) of this cell
	 * 
	 * @author BLM
	 */
	public int getPixelArea_wholeCell() {
		return (getPixelArea_cytoplasmic() + getPixelArea_nuclear());
	}

	/**
	 * Returns the cytoplasmic pixel area (numPixels) of this cell
	 * 
	 * @author BLM
	 */
	public int getPixelArea_cytoplasmic() {
		return cytoplasm.getNumPixels();
	}

	/**
	 * Returns the nuclear pixel area (numPixels) of this cell
	 * 
	 * @author BLM
	 */
	public int getPixelArea_nuclear() {
		return nucleus.getNumPixels();
	}

	/**
	 * Returns the number of neighbors this cell has
	 * 
	 * @author BLM
	 */
	public int getNumberOfNeighbors() {
		if (NeighborCells == null)
			return 0;
		return NeighborCells.length;
	}

	public void addNeighborID(int id) {
		int len = neighborCellIDs.size();

		// checking neighbors
		for (int i = 0; i < len; i++)
			if (((Integer) neighborCellIDs.get(i)).intValue() == id)
				return;

		neighborCellIDs.add(new Integer(id));
	}

	/**
	 * For visualization purposes, this will return an array of lines to draw
	 * from this cell's centroid to it's neighbors centroids
	 * 
	 * @author BLM
	 */
	public Line2D[] getNeighborLines() {
		if (neighborLines == null) {
			Line2D.Double[] lines = null;

			if (NeighborCells != null) {
				int num = NeighborCells.length;
				lines = new Line2D.Double[num];
				Point2D.Double p0 = nucleus.getCentroid();

				for (int i = 0; i < num; i++) {
					Point2D.Double p = NeighborCells[i].nucleus.getCentroid();
					lines[i] = new Line2D.Double(p0.x, p0.y, p.x, p.y);
				}
			}
			neighborLines = lines;
		}

		return neighborLines;
	}

	/**
	 * If neighbors have been stored, this method computes a float scaler value
	 * of the mean neigbhor distance from this cell
	 * 
	 * @author BLM
	 */
	public float getMeanNeighborDistance() {
		float val = 0;
		int numN = NeighborCells.length;
		Point2D.Double p0 = nucleus.getCentroid();
		for (int i = 0; i < numN; i++)
			val += p0.distance(NeighborCells[i].nucleus.getCentroid());
		return val / (float) numN;
	}

	/**
	 * If neighbors have been stored, this method computes a float scaler value
	 * of the mean neigbhor response of this cell of the Feature of index given
	 * 
	 * @author BLM
	 */
	public float getMeanNeighborResponse(int indexOfValue) {
		float val = 0;
		int numN = NeighborCells.length;
		Point2D.Double p0 = nucleus.getCentroid();
		int counter = 0;
		for (int i = 0; i < numN; i++) {
			double d = p0.distance(NeighborCells[i].nucleus.getCentroid());
			// System.out.println(d);
			// if (d<100)
//			{
			counter++;
			val += NeighborCells[i]
					.getChannelValue_wholeCell_mean(indexOfValue);
//			}
		}
		return val / (float) counter;
	}

	/**
	 * Returns the mean and stdev values of the given cells for all features
	 * loaded into the MainGUI
	 * 
	 * @author BLM
	 * @return float[][] where float[].length = 2 (mean/stdev) and
	 *         float[][].length = numFeatures
	 */
	static public float[][] getCellMeansAndStdev_allFeatures(Cell_RAM[] cells) {
		// TODO
		// if (cells==null || cells.length<=0)
		// return null;
		//
		int numF = MainGUI.getGUI().getTheFeatures().size();

		int numCells = cells.length;
		float[][] data = new float[2][numF];
		// float[] mean = new float[numF];
		// float[] stdev = new float[numF];
		//
		// int[] counter = new int[numF];
		// for (int j = 0; j < numCells; j++)
		// for (int i = 0; i < numF; i++)
//			{
		// Feature f = (Feature)MainGUI.getGUI().getTheFeatures().get(i);
		// double val = f.getValue(cells[j]);
		// if (val>-1000000000 && val<1000000000)
//				{
		// mean[i] += val;
		// counter[i]++;
//				}
//			}
		// for (int i = 0; i < numF; i++)
		// mean[i] = (mean[i]/(float)counter[i]);
		//
		// //computing the stdev
		// for (int j = 0; j < numCells; j++)
		// for (int i = 0; i < numF; i++)
//			{
		// Feature f = (Feature)MainGUI.getGUI().getTheFeatures().get(i);
		// double val = f.getValue(cells[j]);
		// if (val>-1000000000 && val<1000000000)
		// stdev[i] += Math.pow(val - mean[i],2);
//			}
		//
		// for (int i = 0; i < numF; i++)
		// stdev[i] = (float)Math.sqrt((1f/(counter[i]-1f)*stdev[i]));
		//
		//
		// data[0] = mean;
		// data[1] = stdev;
		return data;
	}

	/**
	 * Given the arrayList of points, this method sets the boundary points and
	 * bounding box of the cell
	 * 
	 * @author BLM
	 */
	public void initBoundary(ArrayList points) {
		boundaryPoints = points;
		if (boundaryPoints == null)
			return;

		float xMin = Float.POSITIVE_INFINITY;
		float xMax = Float.NEGATIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		int len = boundaryPoints.size();
		for (int i = 0; i < len; i++) {
			Point p = (Point) points.get(i);
			if (p.x < xMin)
				xMin = p.x;
			if (p.x > xMax)
				xMax = p.x;
			if (p.y < yMin)
				yMin = p.y;
			if (p.y > yMax)
				yMax = p.y;
		}

		boundingBox = new Rectangle((int) xMin, (int) yMin,
				(int) (xMax - xMin), (int) (yMax - yMin));
	}

	/**
	 * Given a arrayList of all coordinate points of this cell, and the original
	 * pixel matrix from the image, this will search all the coordinates and
	 * determine which pixels are the boundary pixels and then initialize this
	 * cell with those boundaries
	 * 
	 * @author BLM
	 */
	public void findAndInitBoundary(ArrayList allPoints, Temp_Pixel[][] pixels) {
		// init the cell boundary pixels now
		ArrayList arr = allPoints;
		int numPix = arr.size();
		cytoplasm.setNumPixels(numPix);

		ArrayList boundaryPixels = new ArrayList();
		for (int pi = 0; pi < numPix; pi++) {
			Point po = (Point) arr.get(pi);
			Temp_Pixel pix = pixels[po.y][po.x];

			Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix, pixels);
			int len = neighbors.length;
			for (int j = 0; j < len; j++) {
				Temp_Pixel neigh = neighbors[j];
				if (neigh.getID() != pix.getID()) {
					boundaryPixels.add(po);
					break;
				}
			}
		}
		initBoundary(boundaryPixels);
	}

	/**
	 * In order to save memory, this method tries to clear every trace of this
	 * cell
	 * 
	 * @author BLM
	 */
	public void kill() {
		cytoplasm.kill();
		nucleus.kill();
		color = null;
		if (boundaryPoints != null) {
			Iterator iter = boundaryPoints.iterator();
			while (iter.hasNext()) {
				Point element = (Point) iter.next();
				element = null;
			}
			boundaryPoints = null;
		}
		boundingBox = null;
	}
}

