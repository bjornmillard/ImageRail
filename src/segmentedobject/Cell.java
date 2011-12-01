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

/**
 * This class contains all the information of the coordinates (pixels) of every compartment,
 * but also all the calculated features; for example the nucleus intensity.
 * 
 * @author Bjorn Millard & Michael Menden
 */
public class Cell {

	private CellCoordinates coordinates;
	private float[] featureValues;
	private boolean selected;
	private int ID;

	/**
	 * Constructs and initializes a cell with an id, the coordinates and features.
	 * @param ID A unique identifier for the cell.
	 * @param coords An array of the compartment coordinates; for example could be the compartment
	 * coordinates a bounding box (2-points), centroid (1-point) or the whole pixels (?-points).
	 * @param featureValues The calculated features for this cell; for example ratio of nucleus/cytoplasm intensity.
	 */
	public Cell(int ID, CellCoordinates coords, float[] featureValues) {
		this.coordinates = coords;
		this.featureValues = featureValues;
		selected = false;
		this.ID = ID;
	}

	/**
	 * Get the Coordinates of all the compartments.
	 * @return Returns the cell coordinates.
	 */
	public CellCoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Set the Coordinates.
	 * @param coords The new cell coordinates.
	 */
	public void setCoordinates(CellCoordinates coords) {
		coordinates = coords;
	}

	/**
	 * Check if the cell is selected. The user could select or unselect a cell.
	 * This feature is useful for the GUI.
	 * @return Returns if the cell is selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Select or unselect a cell.
	 * @param select Value true to select the cell, and value false to unselect the
	 * cell
	 * */
	public void setSelected(boolean select) {
		selected = select;
	}

	/**
	 * Returns the cell ID which also denotes the index that this cell's
	 * coordinates and feature values are stored in the HDF5 file
	 * @return Returns the identifier.
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Copy the cell coordinates
	 * @return Returns the cell coordinates.
	 */
	public CellCoordinates getCoordinates_copy() {
		if (coordinates == null)
			return null;
		return coordinates.copy();
	}

	/**
	 * When done with this temporary cell imagerailio, this clears all the memory where
	 * this imagerailio was stored
	 */
	public void kill() {
		if (coordinates != null)
			coordinates.kill();
		featureValues = null;
	}


	/**
	 * Get the vector which contains the computed feature values for a single cell.
	 * @return Returns the feature values.
	 */
	public float[] getFeatureValues() {
		return featureValues;
	}

	public String toString() {
		String st = "";
		st += "*******CELL - ID: " + ID + "\n";
		st += "feature len: " + featureValues.length + "\n";
		int len = coordinates.getComSize();
		st += "Compartment len: " + len + "\n";
		for (int i = 0; i < len; i++) {
			st += "com: " + i + "" + coordinates.getComNames()[i] + "  "
					+ coordinates.getComCoordinates(i) + "\n";
		}
		st += "*********";
		return st;
	}

	
}
