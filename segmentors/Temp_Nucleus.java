/**
 * Nucleus_temp.java
 *
 * @author Created by Omnicore CodeGuide
 */

package segmentors;



/** Each segmented nucleus will hold its own pixels and properties
 * @author BLM*/


import java.awt.geom.Point2D;
import java.util.ArrayList;
import segmentors.Temp_Pixel;
import us.hms.systemsbiology.idx2coordinates.Point;

public class Temp_Nucleus
{
	private int ID;
	private int numPixels;
	private Point[] pixelCoordinates;
	private Point[] boundaryPoints;
	private Point2D.Double Centroid;
	
	public Temp_Nucleus(int id)
	{
		ID = id;
	}
	
	public Temp_Nucleus(ArrayList pixels_, int id)
	{
		ID = id;
		numPixels = pixels_.size();
		pixelCoordinates = new Point[numPixels];
		for (int i = 0; i < numPixels; i++)
			pixelCoordinates[i] = new Point(((Temp_Pixel)pixels_.get(i)).getColumn(), ((Temp_Pixel)pixels_.get(i)).getRow());
		Centroid = getCentroid();
	}
	
	/** Returns the ID of the cell
	 * @author BLM*/
	public int getID()
	{
		return ID;
	}
	
	/** Sets the number of pixels that compse this nucleus
	 * @author BLM*/
	public void setNumPixels(int numPix)
	{
		numPixels = numPix;
	}
	
	/** Returns the array of coordinates that denote the outline of the nucleus
	 * @author BLM*/
	public Point[] getBoundaryPoints()
	{
		return boundaryPoints;
	}

	public void clearPixelData()
	{
		boundaryPoints = null;
		pixelCoordinates = null;
	}
	
	public Point[] getAllPixelCoordinates()
	{
		return pixelCoordinates;
	}
	
	/** returns the requested indexed pixel */
	public Point getPixelCoordinate(int i)
	{
		return pixelCoordinates[i];
	}
	
	/** Returns the number of pixels that compose the nucleus (ex: nuclear area in pixel units)
	 * @author BLM*/
	public int getNumPixels()
	{
		return numPixels;
	}
	
	/**Computes the pixel_XY location of the centroid of the cell
	 * @author BLM*/
	public Point2D.Double getCentroid()
	{
		if (Centroid==null && pixelCoordinates!=null)
		{
			Centroid = new Point2D.Double();
			int col = 0;
			int row = 0;
			for (int i =0; i < numPixels; i++)
			{
				row += pixelCoordinates[i].y;
				col += pixelCoordinates[i].x;
			}
			Centroid.y=row/numPixels;
			Centroid.x=col/numPixels;
		}
		return Centroid;
	}

	public void initBoundaryPoints(Temp_Pixel[][] pixels)
	{
		if (pixelCoordinates==null || pixelCoordinates.length==0)
			return;
		//For each pixel in the nucleus, See if any of the neighbors do not belong to this group; if so, then its a boundary pixel
		ArrayList arr = new ArrayList();
		int len = pixelCoordinates.length;
		for (int i = 0; i < len; i++)
		{
			Point po = pixelCoordinates[i];
			Temp_Pixel p = pixels[po.y][po.x];
			Temp_Pixel[] ne = Temp_Pixel.getNeighbors(p, pixels);
			int num = ne.length;
			for (int j = 0; j < num; j++)
			{
				if (p.getID()!=ne[j].getID())
				{
					arr.add(po);
					break;
				}
			}
		}
		len = arr.size();
		boundaryPoints = new Point[len];
		for (int i = 0; i < len; i++)
			boundaryPoints[i] = (Point)arr.get(i);
	}
	
	public void kill()
	{
		if (pixelCoordinates!=null)
		{
			for (int i = 0; i < pixelCoordinates.length; i++)
				pixelCoordinates[i]= null;
			pixelCoordinates = null;
		}
	}
}

