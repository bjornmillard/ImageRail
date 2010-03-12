package idx2coordinates;

import segmentedObj.Point;

public class IdxConverter
{
	
	/**
	 * Convert a well name to the index.
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
	 * Convert a index to the well name.
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
	 */
	public static int point2index( Point pt, int height)
	{
		return  pt.getX() * height + pt.getY();
	}
	
	/**
	 * Convert index to Cartesian coordinates.
	 */
	public static Point index2point(int index, int height)
	{
		int x = index / height;
		int y = index % height;
		return new Point( x, y);
	}
}
