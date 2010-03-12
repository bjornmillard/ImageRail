package segmentedObj;



/**
 * 
 * @author Bjorn Millard
 * 
 */
public class Cell {

	private CellCoordinates coordinates;
	private float[] featureValues;
	private boolean selected;
	private int ID;

	/**
	 * Constructor.
	 */
	public Cell(int ID, CellCoordinates coords, float[] featureValues) {
		this.coordinates = coords;
		this.featureValues = featureValues;
		selected = false;
		this.ID = ID;
	}

	/**
	 * Get the Coordinates.
	 */
	public CellCoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Set the Coordinates.
	 */
	public void setCoordinates(CellCoordinates coords) {
		coordinates = coords;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean boo) {
		selected = boo;
	}

	/**
	 * Returns the cell ID which also denotes the index that this cell's
	 * coordinates and feature values are stored in the HDF5 file
	 * 
	 * @author BLM
	 */
	public int getID() {
		return ID;
	}

	public CellCoordinates getCoordinates_copy() {
		if (coordinates == null)
			return null;
		return coordinates.copy();
	}

	/**
	 * When done with this temporary cell data, this clears all the memory where
	 * this data was stored
	 * 
	 * @author BLM
	 */
	public void kill() {
		if (coordinates != null)
			coordinates.kill();
		featureValues = null;
	}


	/**
	 * @return the featureValues
	 */
	public float[] getFeatureValues() {
		return featureValues;
	}
}
