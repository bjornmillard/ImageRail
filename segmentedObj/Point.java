package segmentedObj;

/**
 *
 * @author Michael Menden
 *
 */
public class Point {
	
	public int x;
	public int y;
	
	/**
	 * Constructor.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Get x value.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Get y value.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Get the Cartesian coordinates.
	 */
	public int[] getPoint() {
		return new int[] {x, y};
	}
}
