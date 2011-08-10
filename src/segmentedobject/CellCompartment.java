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

package segmentedobject;

import imagerailio.Point;

import java.util.ArrayList;

/**
 * A compartment is a "part" of the cell; for example a compartment could be the nucleus, 
 * cytoplasm or endoplasmic reticulumg (ER). This class contains the name of a specific 
 * compartment as well as its Cartesian coordinates (x;y).
 * 
 * @author Bjorn Millard & Michael Menden
 */
public class CellCompartment
{
	
	private Point[] coordinates;
	private String name;
	private int ID;
	
	/**
	 * Constructs and initializes a compartment with its coordinates and name.
	 * @param coordinates An array of the pixels, which belongs to this compartment. It could be
	 * a bounding box (2-points), centroid (1-point) or the whole pixels (?-points). Bounding
	 * boxes are recommended.
	 * @param name The name of the compartment.
	 */
	public CellCompartment( Point[] coordinates, String name)
	{
		this.coordinates = coordinates;
		this.name = name;
		this.ID = -1;
	}
	
	public CellCompartment(Point[] coordinates, String name, int ID) {
		this.coordinates = coordinates;
		this.name = name;
		this.ID = ID;
	}

	/**
	 * Constructs and initializes a compartment with its coordinates and name.
	 * @param coordinates An array of the pixels, which belongs to this compartment. It could be
	 * a bounding box (2-points), centroid (1-point) or the whole pixels (?-points). Bounding
	 * boxes are recommended.
	 * @param name The name of the compartment.
	 */
	public CellCompartment( ArrayList<Point> coordinates, String name)
	{
		int len = coordinates.size();
		this.coordinates = new Point[len];
		for (int i = 0; i < len; i++)
			this.coordinates[i] = coordinates.get(i);
		this.name = name;
	}

	/**
	 * Makes a copy of this CellCompartment.
	 * @return Returns a equal copy of this class.
	 */
	public CellCompartment copy() {
		int len = coordinates.length;
		Point[] pointsList = new Point[len];
		for (int i = 0; i < len; i++)
			pointsList[i] = new Point(coordinates[i].x, coordinates[i].y);

		CellCompartment clone = new CellCompartment(pointsList, name);
		return clone;
	}

	/**
	 * Get the Cartesian Coordinates (x;y), which are the result of the segmentation for this compartment.
	 * @return Returns an array of Points.
	 */
	public Point[] getCoordinates()
	{
		return coordinates;
	}
	
	/**
	 * Get the Coordinates of a specific point in the compartment.
	 * @param i The index of a specific pixel.
	 * @return Returns a single pixel.
	 */
	public Point getCoordinate( int i)
	{
		return coordinates[i];
	}
	
	/**
	 * Get the size of the compartment.
	 * @return Returns the size of coordinates.
	 */
	public int getSize()
	{
		return coordinates.length;
	}
	
	/**
	 * Get the name of the compartment.
	 * @return Returns the name of the compartment.
	 */
	public String getName()
	{
		return name;
	}
	
	/** 
	 * When done with this temporary cell imagerailio, this clears all the memory where this imagerailio was stored.
	 */
	public void kill()
	{
		coordinates = null;
	}

	/**
	 * Sets the ID of this compartment
	 * 
	 * @param int ID
	 * @author BLM
	 * */
	public void setID(int ID) {
		this.ID = ID;
	}

	/**
	 * Gets the ID of this compartment
	 * 
	 * @return int ID
	 * @author BLM
	 * */
	public int getID() {
		return ID;
	}

}
