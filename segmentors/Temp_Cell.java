/**
 * Cell.java
 *
 * @author Created by BLM
 */

package segmentors;

import features.Feature;
import gui.MainGUI;

import java.util.ArrayList;
import java.util.Iterator;
import segmentors.Temp_Pixel;
import tempObjects.Cell_RAM;
import us.hms.systemsbiology.idx2coordinates.Point;

public class Temp_Cell
{

	private int ID;
	private int FieldNumber;
	private Temp_Nucleus nucleus;
	private Temp_Cytoplasm cytoplasm;
	private ArrayList<Point> boundaryPoints;
	private Cell_RAM[] allCells;

	private int SourceImage_Width;
	private int SourceImage_Height;
	
	
	public Temp_Cell(Temp_Nucleus nucleus_, Temp_Cytoplasm cytoplasm_)
	{
		ID = (int)(Math.random()*100000000);
		nucleus = nucleus_;
		cytoplasm = cytoplasm_;
	}

	
	
	
	/** Returns the boundary points in an arrayList demarking the cytoplasmic boundary of the cell
	 * @author BLM*/
	public ArrayList<Point> getBoundaryPoints()
	{
		Point[] pts = nucleus.getBoundaryPoints();
		ArrayList<Point> arr = new ArrayList<Point>();
		for (int i = 0; i < pts.length; i++)
			arr.add(pts[i]);
		for (int i = 0; i < boundaryPoints.size(); i++)
			arr.add(boundaryPoints.get(i));
		return arr;
	}
	/** Sets the boundary points in an arrayList demarking the cytoplasmic boundary of the cell
	 * @author BLM*/
	public void setBoundaryPoints(ArrayList<Point> points)
	{
		boundaryPoints = points;
	}
	
	
	/** Returns the cytoplasm object of this cell
	 * @author BLM*/
	public Temp_Cytoplasm getCytoplasm()
	{
		return cytoplasm;
	}
	
	/** Sets the cytoplasm object of this cell
	 * @author BLM*/
	public void setCytoplasm(Temp_Cytoplasm cytoplasm_)
	{
		cytoplasm = cytoplasm_;
	}
	
	/** Returns the nucleus object of this cell
	 * @author BLM*/
	public Temp_Nucleus getNucleus()
	{
		return nucleus;
	}
	
	/** Sets the nucleus object of this cell
	 * @author BLM*/
	public void setNucleus(Temp_Nucleus nucleus_)
	{
		nucleus = nucleus_;
	}
	

	
	public void setSourceImageWidth(int width)
	{
		SourceImage_Width = width;
	}
	public int getSourceImageWidth()
	{
		return SourceImage_Width;
	}
	public void setSourceImageHeight(int height)
	{
		SourceImage_Height = height;
	}
	public int getSourceImageHeight()
	{
		return SourceImage_Height;
	}
	
	/** Sets the Model_Field number that this cell came from
	 * @author BLM*/
	public void setFieldNumber(int field_)
	{
		FieldNumber = field_;
	}
	
	/** Returns the field number that this cell came from
	 * @author BLM*/
	public int getFieldNumber()
	{
		return	FieldNumber;
	}
	
	/** Returns Cell ID
	 * @author BLM*/
	public int getID()
	{
		return ID;
	}
	
	/** Sets Cell ID
	 * @author BLM*/
	public void setID(int id)
	{
		ID = id;
	}
	
	/** Clears the pixel coordinates representing this cell
	 * @author BLM*/
	public void clearPixelData()
	{
		boundaryPoints = null;
		cytoplasm.clearPixelData();
		nucleus.clearPixelData();
	}
	
	/** Sets a list of all the cells that were in the same image as this cell
	 * @author BLM*/
	public void setAllCells(Cell_RAM[] cells)
	{
		allCells = cells;
	}
	
	/** Returns a list of all the cells that were in the same image as this cell
	 * @author BLM*/
	public Cell_RAM[] getAllCells()
	{
		return allCells;
	}
	
	/** Sets up the mean value float[] arrays (length==numChannels) to store values for this cell
	 * @author BLM*/
	public void initNumChannels(int numChannels)
	{
		cytoplasm.initNumChannels(numChannels);
	}
	

	
	
	/** Returns the whole cell pixel area (numPixels) of this cell
	 * @author BLM*/
	public int getPixelArea_wholeCell()
	{
		return (getPixelArea_cytoplasmic()+getPixelArea_nuclear());
	}
	
	/** Returns the cytoplasmic pixel area (numPixels) of this cell
	 * @author BLM*/
	public int getPixelArea_cytoplasmic()
	{
		return cytoplasm.getNumPixels();
	}
	
	/** Returns the nuclear pixel area (numPixels) of this cell
	 * @author BLM*/
	public int getPixelArea_nuclear()
	{
		return 	nucleus.getNumPixels();
	}
	

	
	
	/** Given the arrayList of points, this method sets the boundary points and bounding box of the cell
	 * @author BLM*/
	public void initBoundary(ArrayList points)
	{
		boundaryPoints = points;
		if (boundaryPoints==null)
			return;
		
		float xMin = Float.POSITIVE_INFINITY;
		float xMax = Float.NEGATIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		int len = boundaryPoints.size();
		for (int i=0; i < len; i++)
		{
			Point p = (Point)points.get(i);
			if (p.x< xMin)
				xMin = p.x;
			if (p.x > xMax)
				xMax = p.x;
			if (p.y < yMin)
				yMin = p.y;
			if (p.y > yMax)
				yMax = p.y;
		}
		
	}
	
	/** Given a arrayList of all coordinate points of this cell, and the original pixel matrix from the image, this will search all the coordinates and
	 * determine which pixels are the boundary pixels and then initialize this cell with those boundaries
	 * @author BLM*/
	public void findAndInitBoundary(ArrayList allPoints, Temp_Pixel[][] pixels)
	{
		//init the cell boundary pixels now
		ArrayList arr = allPoints;
		int numPix = arr.size();
		cytoplasm.setNumPixels(numPix);
		
		ArrayList boundaryPixels = new ArrayList();
		for (int pi = 0; pi < numPix; pi++)
		{
			Point po = (Point)arr.get(pi);
			Temp_Pixel pix = pixels[po.y][po.x];
			
			Temp_Pixel[] neighbors = Temp_Pixel.getNeighbors(pix, pixels);
			int len = neighbors.length;
			for (int j =0; j < len; j++)
			{
				Temp_Pixel neigh = neighbors[j];
				if (neigh.getID()!=pix.getID())
				{
					boundaryPixels.add(po);
					break;
				}
			}
		}
		initBoundary(boundaryPixels);
	}
	
	/** In order to save memory, this method tries to clear every trace of this cell
	 * @author BLM*/
	public void kill()
	{
		cytoplasm.kill();
		nucleus.kill();
		if (boundaryPoints!=null)
		{
			Iterator iter = boundaryPoints.iterator();
			while (iter.hasNext())
			{
				Point element = (Point)iter.next();
				element = null;
			}
			boundaryPoints = null;
		}
	}
}

