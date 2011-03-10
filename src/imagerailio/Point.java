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
