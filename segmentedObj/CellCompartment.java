package segmentedObj;

import java.util.ArrayList;

/**
 *
 * @author Michael Menden
 *
 */
public class CellCompartment
{
	
	private Point[] coordinates;
	private String name;
	
	/**
	 * Constructor.
	 */
	public CellCompartment( Point[] coordinates, String name)
	{
		this.coordinates = coordinates;
		this.name = name;
	}
	
	/**
	 * Constructor.
	 */
	public CellCompartment( ArrayList<Point> coordinates, String name)
	{
		this.coordinates = (Point[]) coordinates.toArray( new Point[0]);
		this.name = name;
	}

	/**
	 * Makes a copy of this CellCompartment
	 * 
	 * @author BLM
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
	 * Get the Coordinates.
	 */
	public Point[] getCoordinates()
	{
		return coordinates;
	}
	
	/**
	 * Get the Coordinates of a specific point in the compartment.
	 */
	public Point getCoordinate( int i)
	{
		return coordinates[i];
	}
	
	/**
	 * Get the size of the compartment.
	 */
	public int getSize()
	{
		return coordinates.length;
	}
	
	/**
	 * Get the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/** When done with this temporary cell data, this clears all the memory where this  data was stored
	 * @author BLM*/
	public void kill()
	{
		coordinates = null;
	}
}
