package xml_metaData;

import java.util.ArrayList;

/**
 *
 * @author Michael Menden
 *
 */
public class MicrotiterPlate {

	private ArrayList<Well> wells;
	
	/**
	 * Constructor.
	 */
	public MicrotiterPlate() {
		wells = new ArrayList<Well>();
	}
	
	/**
	 * Replace a well.
	 */
	public void replaceWell( Well well) {
		dropWell(well.getIdx());
		wells.add(well);
	}
	
	/**
	 * Add a well.
	 */
	public void addWell( Well well) {
		wells.add(well);
	}
	
	/**
	 * Drop the well if it exist.
	 */
	public void dropWell( int wellIdx) {
		for (int i=0; i<wells.size(); i++) {
			if (wellIdx == wells.get(i).getIdx()) {
				wells.remove(i);
			}
		}
	}
	
	/**
	 * Get all wells.
	 */
	public ArrayList<Well> getWells() {
		return wells;
	}
	
	/**
	 * Get a well.
	 */
	public Well getWell( int wellIdx) {
		Well well = null;
		for (int i=0; i<wells.size(); i++) {
			if (wellIdx == wells.get(i).getIdx()) {
				well = wells.get(i);
			}
		}
		return well;
	}

	/**
	 * Get well size.
	 */
	public int getWellSize() {
		return wells.size();
	}
}
