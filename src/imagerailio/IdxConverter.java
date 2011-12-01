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
 * This class is useful to convert indexes to Cartesian coordinates (x;y) as
 * well as to convert Cartesian coordinates back to indexes; for example the pixels
 * of an image could be described with Cartesian coordinates as well as an index.
 * This class also contains methods for conversion between indexes and common well name 
 * of a microtiter plate; for example the left top well on a microtiter plate is typically 
 * named as "A1" and would be translated to "0".
 * 
 * @author Bjorn Millard & Michael Menden
 *
 */
public class IdxConverter
{
	
	/**
	 * Convert a common well name to the index.
	 * @param well The common well name; for example "A1", "A2", "B1", etc.
	 * @param plateSize Microtiter plate size; for example 6, 24, 96, 384, 1536, etc.
	 * @return Returns the corresponding index to the common well name.
	 */
	public static int well2index(String well, int plateSize)
	{
		int idx = 0;
		// the y size of the microtiter plate (ratio 2/3)
		int ySize = (int) Math.sqrt((2*plateSize) / 3);
		
		if (well.charAt(1) >= 'A' && well.charAt(1) <= 'Z')
		{
			idx = (well.charAt(0) - 'A' + 1) * 26;
			idx += well.charAt(1) - 'A' + 1;
			idx += (new Integer(well.substring(2,well.length()))-1) * ySize;
		}
		else
		{
			idx = well.charAt(0) - 'A' + 1;
			idx += (new Integer(well.substring(1,well.length()))-1) * ySize;
		}
		
		return --idx;
	}
	
	/**
	 * Convert an index to the common well name.
	 * @param index Index on a microtiter plate. 
	 * @param plateSize Microtiter plate size; for example 6, 24, 96, 384, 1536, etc.
	 * @return Returns the corresponding common well name to the index.
	 */
	public static String index2well(int index, int plateSize)
	{
		String well = "";
		// the y size of the microtiter plate (ratio 2/3)
		int ySize = (int) Math.sqrt((2*plateSize) / 3);
		

		// Add the chars which label the left side of the microtiter plate.
		// Two chars are enough to address 676 rows.
		int row = index % ySize;
		// WARNING: 676 rows => biggest microtiter plate, which could
		//                      be addressed is 393216 wells big.
		if (row > 25)
		{
			int ch = (int) (row / 26);
			well += (char) (ch + 'A' - 1);
		}
		well += (char) ((row % 26) + 'A');
		
		// Add the number which label the top side of the microtiter plate
		int number = (index / ySize) + 1;
		well += number;
		return well;
	}
	
	/**
	 * Convert Cartesian coordinates to index.
	 * @param pt Point, which contains x,y-coordinates.
	 * @param height Height of the image.
	 * @return Returns the corresponding index to the point.
	 */
	public static int point2index( Point pt, int height)
	{
		return  pt.getX() * height + pt.getY();
	}
	
	/**
	 * Convert index to Cartesian coordinates.
	 * @param index The index of a pixel in an image.
	 * @param height Height of the image.
	 * @return Returns the corresponding point to the index.
	 */
	public static Point index2point(int index, int height)
	{
		int x = index / height;
		int y = index % height;
		return new Point( x, y);
	}
}
