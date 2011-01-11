package imagerailio;

/**
 * This class contains the Cartesian coordinates (x;y) of a Pixel
 * in an image.
 * 
 * @author Bjorn Millard & Michael Menden
 */
public class Point {
	
	/**
	 * The x coordinate.
	 */
	public int x;
	/**
	 * The y coordinate.
	 */
	public int y;
	
	/**
	 * Constructs and initializes a point at the specified (x, y) 
	 * location in the coordinate space.
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the X coordinate of the pixel.
	 * @return Returns the x coordinate.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Returns the Y coordinate of the pixel.
	 * @return Returns the y coordinate.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Get the Cartesian coordinates.
	 * @return (x;y) coordinates as an integer array.
	 */
	public int[] getPoint() {
		return new int[] {x, y};
	}
}
