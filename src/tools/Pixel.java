package tools;

public class Pixel implements Comparable {
	private int row;
	private int col;
	private int height_raster;
	private int width_raster;
	private int value;
	private int ID;

	public Pixel(int r, int c, int id, int height_raster, int width_raster) {
		row = r;
		col = c;
		value = -5;
		ID = id;
		this.height_raster = height_raster;
		this.width_raster = width_raster;
	}

	public Pixel(int r, int c, int z_, int id, int height_raster,
			int width_raster) {
		row = r;
		col = c;
		value = z_;
		ID = id;
		this.height_raster = height_raster;
		this.width_raster = width_raster;
	}

	/**
	 * Sets the pixel value as an integer
	 * 
	 * @author BLM
	 */
	public void setValue(int val) {
		value = val;
	}

	/**
	 * Sets the ID of the Pixel
	 * 
	 * @author BLM
	 */
	public void setID(int id) {
		ID = id;
	}

	/**
	 * Returns the pixel value as an integer
	 * 
	 * @author BLM
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns the pixel ID number
	 * 
	 * @author BLM
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Returns the pixel row where it is found in the image
	 * 
	 * @author BLM
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Returns the pixel column where it is found in the image
	 * 
	 * @author BLM
	 */
	public int getColumn() {
		return col;
	}

	public double getDistance_L1(Pixel p) {
		double dist = 0;
		dist += Math.abs(p.row - row);
		dist += Math.abs(p.col - col);

		return dist;
	}

	public int compareTo(Object o) {
		if (!(o instanceof Pixel))
			throw new ClassCastException();

		Pixel obj = (Pixel) o;

		if (obj.value < value)
			return 1;

		if (obj.value > value)
			return -1;

		return 0;
	}

	/**
	 * Says if this pixels is above an intensity threshold
	 * 
	 * @author BLM
	 */
	public boolean pixelOn(int[] pix) {
		int sum = 0;
		int len = pix.length;

		for (int i = 1; i < len; i++)
			sum += pix[i];

		if (sum > 0) {

			return true;
		}
		return false;
	}

	public boolean pixelOn(int[] pix, float threshold) {
		int sum = 0;
		int len = pix.length;

		for (int i = 1; i < len; i++)
			sum += pix[i];

		if (sum > threshold)
			return true;

		return false;
	}

	public void resetIDs(Pixel[][] pixels) {
		int rows = pixels.length;
		int cols = pixels[0].length;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				pixels[r][c].ID = -1;
	}

	public void resetIDs(Pixel[] pixels) {
		int len = pixels.length;
		for (int r = 0; r < len; r++)
			pixels[r].ID = -1;
	}

	public boolean pixelOn(int pix, float threshold) {
		int sum = 0;

		if (sum > threshold)
			return true;

		return false;
	}

	public boolean pixelOn_binary(int[] pix) {
		int sum = 0;
		int len = pix.length;

		for (int i = 0; i < len; i++)
			sum += pix[i];

		if (sum > 230)
			return true;
		return false;
	}

	/**
	 * Checks if the given pixel that has the given Euclidean Distance is
	 * Surrounded by pixels at a higher level
	 * 
	 * @author BLM
	 **/
	public float getLargestNeighborEuclidDist(Pixel pix, Pixel[] pixels,
			float[][][] distanceMap) {
		Pixel[] neigh = pix.getNeighbors(pixels);
		int len = neigh.length;
		float max = -1;
		for (int i = 0; i < len; i++) {
			Pixel p = neigh[i];
			float val = distanceMap[p.row][p.col][0];
			if (val > max)
				max = val;
		}

		return max;
	}

	/**
	 * Returns an array of pixels of the given pixel, top to bottom, L->R
	 * starting at r-1, c-1
	 * 
	 * @author BLM
	 */
	public Pixel[] getNeighbors(Pixel[] pixels) {
		Pixel centerPixel = this;
		Pixel[] pixs = null;
		// top row
		if (centerPixel.row == 0) {
			if (centerPixel.col == 0) {
				// top left corner
				pixs = new Pixel[3];
				pixs[0] = pixels[centerPixel.row + (centerPixel.col + 1)
						* height_raster];
				pixs[1] = pixels[centerPixel.row + 1 + (centerPixel.col + 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row + 1 + (centerPixel.col)
						* height_raster];
			} else if (centerPixel.col == width_raster - 1) {
				// top right corner
				pixs = new Pixel[3];
				pixs[0] = pixels[centerPixel.row + (centerPixel.col - 1)
						* height_raster];
				pixs[1] = pixels[centerPixel.row + 1 + (centerPixel.col - 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row + 1 + (centerPixel.col)
						* height_raster];
			}

			else {
				// general top row
				pixs = new Pixel[5];
				pixs[0] = pixels[centerPixel.row + (centerPixel.col - 1)
						* height_raster];

				pixs[1] = pixels[centerPixel.row + 1 + (centerPixel.col - 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row + 1 + (centerPixel.col)
						* height_raster];
				pixs[3] = pixels[centerPixel.row + 1 + (centerPixel.col + 1)
						* height_raster];

				pixs[4] = pixels[centerPixel.row + (centerPixel.col + 1)
						* height_raster];
			}
		}
		// bottom row pixels
		else if (centerPixel.row == height_raster - 1) {
			if (centerPixel.col == 0) {
				// bottom left corner
				pixs = new Pixel[3];
				pixs[0] = pixels[centerPixel.row - 1 + (centerPixel.col)
						* height_raster];
				pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col + 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row + (centerPixel.col + 1)
						* height_raster];
			} else if (centerPixel.col == width_raster - 1) {
				// bottom right corner
				pixs = new Pixel[3];
				pixs[0] = pixels[centerPixel.row + (centerPixel.col - 1)
						* height_raster];
				pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col - 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row - 1 + (centerPixel.col)
						* height_raster];
			} else {
				// general bottom row
				pixs = new Pixel[5];
				pixs[0] = pixels[centerPixel.row + (centerPixel.col - 1)
						* height_raster];

				pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col - 1)
						* height_raster];
				pixs[2] = pixels[centerPixel.row - 1 + (centerPixel.col)
						* height_raster];
				pixs[3] = pixels[centerPixel.row - 1 + (centerPixel.col + 1)
						* height_raster];

				pixs[4] = pixels[centerPixel.row + (centerPixel.col + 1)
						* height_raster];
			}
		}

		// general left column
		else if (centerPixel.col == 0) {
			pixs = new Pixel[5];
			pixs[0] = pixels[centerPixel.row - 1 + (centerPixel.col)
					* height_raster];

			pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col + 1)
					* height_raster];
			pixs[2] = pixels[centerPixel.row + (centerPixel.col + 1)
					* height_raster];
			pixs[3] = pixels[centerPixel.row + 1 + (centerPixel.col + 1)
					* height_raster];

			pixs[4] = pixels[centerPixel.row + 1 + (centerPixel.col)
					* height_raster];
		}
		// far right column pixels
		else if (centerPixel.col == width_raster - 1) {
			pixs = new Pixel[5];
			pixs[0] = pixels[centerPixel.row - 1 + (centerPixel.col)
					* height_raster];

			pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col - 1)
					* height_raster];
			pixs[2] = pixels[centerPixel.row + (centerPixel.col - 1)
					* height_raster];
			pixs[3] = pixels[centerPixel.row + 1 + (centerPixel.col - 1)
					* height_raster];

			pixs[4] = pixels[centerPixel.row + 1 + (centerPixel.col)
					* height_raster];
		}

		else {
			// general body pixels
			pixs = new Pixel[8];
			pixs[0] = pixels[centerPixel.row - 1 + (centerPixel.col - 1)
					* height_raster];
			pixs[1] = pixels[centerPixel.row - 1 + (centerPixel.col)
					* height_raster];
			pixs[2] = pixels[centerPixel.row - 1 + (centerPixel.col + 1)
					* height_raster];

			pixs[3] = pixels[centerPixel.row + (centerPixel.col - 1)
					* height_raster];
			pixs[4] = pixels[centerPixel.row + (centerPixel.col + 1)
					* height_raster];

			pixs[5] = pixels[centerPixel.row + 1 + (centerPixel.col - 1)
					* height_raster];
			pixs[6] = pixels[centerPixel.row + 1 + (centerPixel.col)
					* height_raster];
			pixs[7] = pixels[centerPixel.row + 1 + (centerPixel.col + 1)
					* height_raster];
		}
		return pixs;
	}

	public Pixel[] getFourNeighbors(Pixel[] pixels) {
		Pixel centerPixel = this;
		Pixel[] pixs = null;

		if (centerPixel.row >= height_raster - 1
				|| centerPixel.col >= width_raster - 1) {
			pixs = new Pixel[0];
			return pixs;
		} else if (centerPixel.row <= 1 || centerPixel.col <= 1) {
			pixs = new Pixel[0];
			return pixs;
		}

		// general body pixels
		pixs = new Pixel[4];

		pixs[0] = pixels[centerPixel.row - 1 + (centerPixel.col)
				* height_raster];
		pixs[1] = pixels[centerPixel.row + (centerPixel.col - 1)
				* height_raster];
		pixs[2] = pixels[centerPixel.row + (centerPixel.col + 1)
				* height_raster];
		pixs[3] = pixels[centerPixel.row + 1 + (centerPixel.col)
				* height_raster];

		return pixs;
	}

}