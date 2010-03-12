package segmentedObj;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Michael Menden
 *
 */
public class CellCoordinates
{
	private CellCompartment[] com;
	
	/**
	 * Constructor.
	 */
	public CellCoordinates(CellCompartment[] com)
	{
		this.com = com;
	}


	/**
	 * Constructor.
	 */
	public CellCoordinates(ArrayList<CellCompartment> com)
	{
		this.com = (CellCompartment[]) com.toArray(new CellCompartment[0]);
	}
	
	/**
	 * Get Compartment.
	 */
	public CellCompartment getCompartment (int i)
	{
		return com[i];
	}
	
	
	/**
	 * Get names of compartments.
	 */
	public String[] getComNames()
	{
		String[] names = new String[com.length];
		for (int i=0; i<com.length; i++)
			names[i] = com[i].getName();
		return names;
	}
	
	/**
	 * Get Coordinates of compartment of the given index
	 */
	public Point[] getComCoordinates (int index)
	{
		return com[index].getCoordinates();
	}
	
	/**
	 * Get Coordinates of compartments with the given name
	 * @author BLM
	 */
	public Point[] getComCoordinates (String compartmentCoordinates)
	{
		int index = -1;
		int num = getComSize();
		String[] names = getComNames();
		for (int i = 0; i < num; i++)
			if (names[i].equalsIgnoreCase(compartmentCoordinates))
			{
				index = i;
				break;
			}
		
		if (index==-1)
			return null;
		
		return com[index].getCoordinates();
	}

	/**
	 * Get all unique coordinates; for example, it is not required that all
	 * sub-compartments contain unique points
	 * 
	 * @author BLM
	 */
	public Point[] getComCoordinates_AllUnique ()
	{
		//First seeing if we have already computed all the unique points for this cell (saves time)
		Point[] temp = getComCoordinates("AllUnique");
		if (temp != null)
			return temp;
		
		// Else if we haven't computed them, then we find all unique points,
		// then save them for later as a new compartment, then returns the
		// compartment
		Hashtable<String, Point> hash = new Hashtable<String, Point>();
		String key = "";
		int num = getComSize();
		for (int i = 0; i < num; i++)
		{
			Point[] pts = getComCoordinates(i);
			int len = pts.length;
			for (int j = 0; j < len; j++)
			{
				key=pts[j].x+","+pts[j];
				if (hash.get(key)==null)
					hash.put(key, pts[j]);
			}
		}
		
		ArrayList<Point> coords = new ArrayList<Point>();
		Enumeration<Point> e = hash.elements();
		while( e. hasMoreElements() )
			coords.add((Point)e.nextElement());
		Point[] coordsArr = new Point[coords.size()];
		coords.toArray(coordsArr);
		
		CellCompartment allUnique = new CellCompartment(coordsArr, "AllUnique");
		
		//Adding the new unique compartment to the compartment list
		CellCompartment[] newCom = new CellCompartment[com.length+1];
		for (int i = 0; i < com.length; i++)
			newCom[i] = com[i];
		newCom[com.length] = allUnique;
		com = newCom;
		
		return allUnique.getCoordinates();
	}
	
	
	/**
	 * Get number of compartments.
	 */
	public int getComSize()
	{
		return com.length;
	}
	
	/** Sets the field that this cell object originated from
	 * @author BLM**/
//	public void setParentField(Field field)
//	{
//		TheField = field;
//	}
//	/** Gets the field that this cell object originated from
//	 * @author BLM**/
//	public Field getField()
//	{
//		return TheField;
//	}
	
	
	/** Takes in all the cell_coord objects, searches them for a compartment that was previously called the given name==compartmentNameOfInterest,
	 * and then creates and returns a list of new Cell_coord objects that only contain that compartment
	 * @author BLM*/
	static public  ArrayList<CellCoordinates> getSingleCompartmentCoords(ArrayList<CellCoordinates> cell_coords, String compartmentNameOfInterest)
	{
		ArrayList<CellCoordinates> out = new ArrayList<CellCoordinates>();
		int numCells = cell_coords.size();
		for (int i = 0; i < numCells; i++)
		{
			CellCoordinates cell = cell_coords.get(i);
			String[] names = cell.getComNames();
			int numCom = cell.getComSize();
			for (int j = 0; j < numCom; j++)
			{
				if (names[j].equalsIgnoreCase(compartmentNameOfInterest))
				{
					ArrayList<CellCompartment> temp = new ArrayList<CellCompartment>();
					//Transfering outline points
					Point[] com = cell.getComCoordinates(j);
					ArrayList<Point> outPts  = new ArrayList<Point>();
					for (int r = 0; r < com.length; r++)
						outPts.add(com[r]);
					Point[] coordsArr = new Point[outPts.size()];
					outPts.toArray(coordsArr);
					
					
					
					CellCompartment outline = new CellCompartment(coordsArr, compartmentNameOfInterest);
					temp.add(outline);
					
					out.add(new CellCoordinates(temp));
				}
			}
		}
		return out;
	}
	
	/** Computes the centroid of all the points that compose this cell_coords object
	 * @author BLM*/
	public  Point getCentroid()
	{
		int xM = 0;
		int yM = 0;
		int counter = 0;
		
		Point[] com = getComCoordinates_AllUnique();
		counter+=com.length;
		for (int r = 0; r < com.length; r++)
		{
			xM+=com[r].x;
			yM+=com[r].y;
		}
		
		return new Point((int)(xM/counter), (int)(yM/counter));
	}
	
	/** Computes the centroid of all the points within this cell_coordinate object and repackages it in another cell_coord obj with
	 * one compartment and one point (the centroid)
	 * @author BLM*/
	static public  ArrayList<CellCoordinates> getCentroidOfCoordinates(ArrayList<CellCoordinates> cell_coords)
	{
		ArrayList<CellCoordinates> out = new ArrayList<CellCoordinates>();
		int numCells = cell_coords.size();
		for (int i = 0; i < numCells; i++)
		{
			CellCoordinates cell = cell_coords.get(i);
			int xM = 0;
			int yM = 0;
			int counter = 0;
			
			Point[] com = cell.getComCoordinates_AllUnique();
			counter+=com.length;
			for (int r = 0; r < com.length; r++)
			{
				xM+=com[r].x;
				yM+=com[r].y;
			}
			
			
			Point[] outPts  = {new Point((int)(xM/counter), (int)(yM/counter))};
			
			ArrayList<CellCompartment> temp = new ArrayList<CellCompartment>();
			temp.add(new CellCompartment(outPts, "Centroid"));
			out.add(new CellCoordinates(temp));
		}
		return out;
	}
	
	/** Computes the BoundingBox of the points within this cell_coordinate object and repackages it in another cell_coord obj with
	 * one compartment and 2 point (the upper left and bottom right point
	 * @author BLM*/
	static public  ArrayList<CellCoordinates> getBoundingBoxOfCoordinates(ArrayList<CellCoordinates> cell_coords)
	{
		ArrayList<CellCoordinates> out = new ArrayList<CellCoordinates>();
		int numCells = cell_coords.size();
		for (int i = 0; i < numCells; i++)
		{
			CellCoordinates cell = cell_coords.get(i);
			int numCom = cell.getComSize();
			int xMin = 1000000000;
			int yMin = 1000000000;
			int xMax = 0;
			int yMax = 0;
			for (int j = 0; j < numCom; j++)
			{
				Point[] com = cell.getComCoordinates(j);
				for (int r = 0; r < com.length; r++)
				{
					if (com[r].x<xMin)
						xMin = com[r].x;
					if (com[r].x>xMax)
						xMax = com[r].x;
					if (com[r].y<yMin)
						yMin = com[r].y;
					if (com[r].y>yMax)
						yMax = com[r].y;
				}
			}

			ArrayList<CellCompartment> temp = new ArrayList<CellCompartment>();
			Point[] outPts  = {new Point(xMin, yMin), new Point(xMax, yMax)};
			
			temp.add(new CellCompartment(outPts, "BoundingBox"));
			out.add(new CellCoordinates(temp));
		}
		return out;
	}
	
	/** Returns a copy of this object
	 * @author BLM*/
	public CellCoordinates copy() {
		int len = com.length;
		CellCompartment[] com_copy = new CellCompartment[len];
		for (int i = 0; i < len; i++)
			com_copy[i] = com[i].copy();
		return new CellCoordinates(com_copy);
	}
		
	/** When done with this temporary cell data, this clears all the memory where this  data was stored
	 * @author BLM*/
	public void kill()
	{
		int numCom  = getComSize();
		for (int i = 0; i < numCom; i++)
			getCompartment(i).kill();
	}
	

	// public boolean isSelected()
	// {
	// return selected;
	// }
	// public void setSelected(boolean boo)
	// {
	// selected = boo;
	// }
	//	
	/** Returns a bounding box if those cell compartments exist
	 * @author BLM*/
	public Rectangle getBoundingBox()
	{
		segmentedObj.Point[] com = getComCoordinates_AllUnique();
		if(com.length==2)
			return 	new Rectangle(com[0].x, com[0].y, (com[1].x-com[0].x), (com[1].y-com[0].y));
		return null;
	}
	
	/** Returns a image files where this cell came from
	 * @author BLM*/
//	public File[] getFilesOfOrigin()
//	{
//		if (TheField==null)
//			return null;
//		return TheField.getImageFiles();
//	}
}
