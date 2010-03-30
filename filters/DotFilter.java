package filters;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

import main.Field;
import main.MainGUI;
import main.Plate;
import main.PlateHoldingPanel;
import main.Well;
import plots.DotPlot;
import us.hms.systemsbiology.data.Data2D;
import us.hms.systemsbiology.data.HDFConnectorException;
import us.hms.systemsbiology.data.SegmentationHDFConnector;
import us.hms.systemsbiology.segmentedobject.Cell;
import us.hms.systemsbiology.segmentedobject.CellCoordinates;
import features.Feature;

public class DotFilter {
	private AlphaComposite transComposite = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.85f);
	private Feature feature;
	private int fIndex;
	private Point p;
	private Polygon arrow;
	private Rectangle closeBox;
	private Rectangle executeBox_selectedWells;
	private Rectangle executeBox_allWells;
	private Rectangle executeBox_allHDF;
	private Rectangle box_addToFilterQueue;

	private DotPlot TheDotPlot;
	private Rectangle roi;
	// The feature value that is filter point either above or below it
	private float pivotValue;
	// if 1, then all cells above get filtered, and -1 below
	private int upDown;
	private Color lightBlue = new Color(224, 255, 255);
	private Color ivory = new Color(255, 255, 240);
	public int highlight;
	private MainGUI TheMainGUI;

	public DotFilter(DotPlot dPlot, Feature featureToFilter, float pivotValue_,
			int upDown_,
			Point point_) {
		feature = featureToFilter;
		fIndex = feature.getGUIindex();
		p = point_;
		upDown = upDown_;
		pivotValue = pivotValue_;
		highlight = -1;
		TheDotPlot = dPlot;
		// Init arrow polygon
		int width = 85;
		int height = 60;
		int offset = (int) (width / 2f);
		TheMainGUI = main.MainGUI.getGUI();
		if (upDown == 1) // Up Arrow
		{
			// Making Arrow
			int numP = 7;
			int[] xps = new int[numP];
			int[] yps = new int[numP];
			xps[0] = (int) (p.x + width * 1f / 5f) - offset;
			xps[1] = (int) (p.x + width * 4f / 5f) - offset;
			xps[2] = (int) (p.x + width * 4f / 5f) - offset;
			xps[3] = (int) (p.x + width) - offset;
			xps[4] = (int) (p.x + width * 1f / 2f) - offset;
			xps[5] = (int) (p.x) - offset;
			xps[6] = (int) (p.x + width * 1f / 5f) - offset;
			yps[0] = (int) (p.y);
			yps[1] = (int) (p.y);
			yps[2] = (int) (p.y - height * 3f / 5f);
			yps[3] = (int) (p.y - height * 3f / 5f);
			yps[4] = (int) (p.y - height);
			yps[5] = (int) (p.y - height * 3f / 5f);
			yps[6] = (int) (p.y - height * 3f / 5f);
			arrow = new Polygon(xps, yps, numP);
			// Making CloseBox
			closeBox = new Rectangle(xps[0], yps[0] - height / 3,
					(int) (width * 3f / 5f), height / 3);
			roi = new Rectangle((int) (int) TheDotPlot.getXStart(),
					(int) TheDotPlot
					.getYStart()
							- (int) TheDotPlot.getAxisLength_Y(),
					(int) TheDotPlot.getAxisLength_X(), (int) TheDotPlot
							.getYStart()
							- (int) TheDotPlot.getAxisLength_Y() + yps[0] - 39); // Not
			// sure
			// where
			// 39
			// comes
			// from
			// ...
			// hmm...
			executeBox_selectedWells = new Rectangle(
					(int) (xps[0] - 1f / 5f * width), yps[0],
					width + 50, 20);
			executeBox_allWells = new Rectangle(
					(int) (xps[0] - 1f / 5f * width),
 yps[0] + 25,
					width + 20, 20);
			executeBox_allHDF = new Rectangle((int) (xps[0] - 1f / 5f * width),
					yps[0] + 50, width + 40, 20);
			box_addToFilterQueue = new Rectangle(
					(int) (xps[0] - 1f / 5f * width),
 yps[0] + 75,
					width + 50, 20);
		} else // Down Arrow
		{
			int numP = 7;
			int[] xps = new int[numP];
			int[] yps = new int[numP];

			xps[0] = (int) (p.x + width * 1f / 5f) - offset;
			xps[1] = (int) (p.x + width * 4f / 5f) - offset;
			xps[2] = (int) (p.x + width * 4f / 5f) - offset;
			xps[3] = (int) (p.x + width) - offset;
			xps[4] = (int) (p.x + width * 1f / 2f) - offset;
			xps[5] = (int) (p.x) - offset;
			xps[6] = (int) (p.x + width * 1f / 5f) - offset;

			yps[0] = (int) (p.y);
			yps[1] = (int) (p.y);
			yps[2] = (int) (p.y + height * 3f / 5f);
			yps[3] = (int) (p.y + height * 3f / 5f);
			yps[4] = (int) (p.y + height);
			yps[5] = (int) (p.y + height * 3f / 5f);
			yps[6] = (int) (p.y + height * 3f / 5f);

			arrow = new Polygon(xps, yps, numP);
			// Making CloseBox
			closeBox = new Rectangle(xps[0], yps[0], (int) (width * 3f / 5f),
					height / 3);
			roi = new Rectangle((int) TheDotPlot.getXStart(), p.y,
					(int) TheDotPlot.getAxisLength_X(), (int) TheDotPlot
							.getYStart()
							- p.y);
			executeBox_selectedWells = new Rectangle(
					(int) (xps[0] - 1f / 5f * width), yps[0] - 20,
					width + 50, 20);
			executeBox_allWells = new Rectangle(
					(int) (xps[0] - 1f / 5f * width), yps[0] - 20 - 25,
					width + 20, 20);
			executeBox_allHDF = new Rectangle((int) (xps[0] - 1f / 5f * width),
					yps[0] - 20 - 50, width + 40, 20);
			box_addToFilterQueue = new Rectangle(
					(int) (xps[0] - 1f / 5f * width), yps[0] - 20 - 75,
					width + 50, 20);
		}
	}

	/**
	 * Returns the non-log version of the value that is the filter point for
	 * this feature filter. Whether to filter all dots above or below this pivot
	 * value is defined in the upDown variable returned by the
	 * getLessThanGreaterThan() method. lessThan == -1 greaterThan == 1
	 * 
	 * @author BLM
	 */
	public float getPivotValue() {
		return pivotValue;
	}

	/**
	 * Returns whether to filter lessThan == -1 or greaterThan == 1; the pivot
	 * value
	 * 
	 * @author BLM
	 */
	public int getLessThanGreaterThan() {
		return upDown;
	}

	/**
	 * Returns the feature that we are filtering
	 * 
	 * @author BLM
	 */
	public Feature getFeatureToFilter() {
		return feature;
	}

	public void draw(Graphics2D g2) {
		g2.setColor(Color.black);
		Composite comp = g2.getComposite();
		g2.setComposite(transComposite);
		g2.fill(roi);

		g2.setColor(lightBlue);
		g2.fill(arrow);

		g2.draw(arrow);
		if (highlight == 0)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.drawString("Cancel", closeBox.x + 4, closeBox.y + 15);

		// Selected Wells
		g2.setColor(ivory);
		g2.fill(executeBox_selectedWells);
		if (highlight == 1)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.draw(executeBox_selectedWells);
		g2.drawString("Filter Selected Wells", executeBox_selectedWells.x + 7,
				executeBox_selectedWells.y + 15);

		// All loaded wells
		g2.setColor(ivory);
		g2.fill(executeBox_allWells);
		if (highlight == 2)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.draw(executeBox_allWells);
		g2.drawString("Filter All Wells", executeBox_allWells.x + 7,
				executeBox_allWells.y + 15);

		// All HDF files
		g2.setColor(ivory);
		g2.fill(executeBox_allHDF);
		if (highlight == 3)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.draw(executeBox_allHDF);
		g2.drawString("Filter All HDF files", executeBox_allHDF.x + 7,
				executeBox_allHDF.y + 15);

		// Add to filter queue
		g2.setColor(ivory);
		g2.fill(box_addToFilterQueue);
		if (highlight == 4)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.draw(box_addToFilterQueue);
		g2.drawString("Add To Fitler Queue", box_addToFilterQueue.x + 7,
				box_addToFilterQueue.y + 15);
		g2.setComposite(comp);
	}

	public boolean shouldClose(Point p) {
		if (arrow.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean shouldExecute_selectedWells(Point p) {
		if (executeBox_selectedWells.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean shouldExecute_allWells(Point p) {
		if (executeBox_allWells.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean shouldExecute_allHDF(Point p) {
		if (executeBox_allHDF.contains(p)) {
			return true;
		}
		return false;
	}

	public boolean shouldAddToFilterQueue(Point p) {
		if (box_addToFilterQueue.contains(p)) {
			return true;
		}
		return false;
	}

	public void execute_selectedWells() {
		Well[] wells = TheDotPlot.getWells();
		for (int j = 0; j < wells.length; j++) {
			Well well = wells[j];
			ArrayList<Cell> cells = well.getCells();
			if (cells != null && cells.size() > 0) {
				selectCellsToFilter(cells, well);
				System.out.println("Processing: " + well.name);
				well.purgeSelectedCellsAndRecomputeWellMeans();
			}

			TheMainGUI.getPlateHoldingPanel().updateMinMaxValues();
			TheMainGUI.updateDotPlot();
		}

	}

	public void execute_allWells() {
		PlateHoldingPanel platePanel = TheMainGUI.getPlateHoldingPanel();
		int numPlates = platePanel.getNumPlates();
		Plate[] plates = platePanel.getThePlates();
		// for each well:
		for (int i = 0; i < numPlates; i++) {
			Plate plate = plates[i];
			int numC = plate.getNumColumns();
			int numR = plate.getNumRows();
			for (int r = 0; r < numR; r++)
				for (int c = 0; c < numC; c++) {
					Well well = plate.getTheWells()[r][c];
					ArrayList<Cell> cells = well.getCells();
					if (cells != null && cells.size() > 0) {
						selectCellsToFilter(cells, well);
						System.out.println("Processing: " + well.name);
						well.purgeSelectedCellsAndRecomputeWellMeans();
					}
				}
		}
		TheMainGUI.getPlateHoldingPanel().updateMinMaxValues();
		TheMainGUI.updateDotPlot();
	}

	public void execute_allHDF() {
		System.out.println("Filtering all HDF files");
		PlateHoldingPanel platePanel = TheMainGUI.getPlateHoldingPanel();
		int numPlates = platePanel.getNumPlates();
		Plate[] plates = platePanel.getThePlates();
		// for each well:
		SegmentationHDFConnector sCon = new SegmentationHDFConnector(
				main.MainGUI.getGUI().getProjectDirectory().getAbsolutePath());
		for (int i = 0; i < numPlates; i++) {
			Plate plate = plates[i];
			int numC = plate.getNumColumns();
			int numR = plate.getNumRows();
			for (int r = 0; r < numR; r++)
				for (int c = 0; c < numC; c++) {
					Well well = plate.getTheWells()[r][c];

					if (well.getHDFcount() > 0)// Just the HDF5 files
					{
						System.out.println("Processing: " + well.name);
						Field[] fields = well.getFields();
						int numF = fields.length;
						try {
							for (int j = 0; j < numF; j++) {

								// Load this well's cells, filter them,
								// then write them back to the HDF files
								ArrayList<Cell> arr = sCon.readCells(plate
										.getID() - 1, well.getWellIndex(),
										fields[j].getIndexInWell());

								// Selecting all cells above or below the
								// pivot
								// point of this filter
								int numCells = arr.size();
								ArrayList<CellCoordinates> keepers_coords = new ArrayList<CellCoordinates>();
								ArrayList<float[]> keepers_vals = new ArrayList<float[]>();
								int counter = 0;
								for (int n = 0; n < numCells; n++) {
									Cell cell = arr.get(n);

									if (upDown == 1) { // above pivotValue
										if (cell.getFeatureValues()[fIndex] < pivotValue) {

											keepers_coords.add(cell
													.getCoordinates());
											keepers_vals.add(cell
													.getFeatureValues());
											counter++;
										}
									} else // below pivotValue
									if (cell.getFeatureValues()[fIndex] > pivotValue) {

										keepers_coords.add(cell
												.getCoordinates());
										keepers_vals.add(cell
												.getFeatureValues());
										counter++;

									}
								}

								// Resaving just the unselected cells
								resaveCells(sCon, keepers_coords, keepers_vals,
										plate.getID() - 1, well.getWellIndex(),
										fields[j].getIndexInWell());

								// Killing temp loaded cells
								int lenC = keepers_coords.size();
								for (int n = 0; n < lenC; n++)
									keepers_coords.get(n).kill();
								keepers_coords = null;
								keepers_vals = null;
							}
						} catch (HDFConnectorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					// Reloading if was loaded before
					ArrayList<Cell> cells = well.getCells();
					if (cells != null && cells.size() > 0) {
						boolean loadCoords = false;
						boolean loadVals = false;
						if (cells.get(0).getCoordinates() != null)
							loadCoords = true;
						if (cells.get(0).getFeatureValues() != null)
							loadVals = true;
						well.loadCells(sCon, loadCoords, loadVals);
					}
				}
		}
		TheMainGUI.getPlateHoldingPanel().updateMinMaxValues();
		TheMainGUI.updateDotPlot();
	}

	/**
	 * Overwrites the current cells in RAM for this field in the master HDF5
	 * file
	 * 
	 * @author BLM
	 */
	static public void resaveCells(SegmentationHDFConnector sCon,
			ArrayList<CellCoordinates> coords, ArrayList<float[]> vals,
			int plateIndex, int wellIndex, int fieldIndex) {
		try {

			if (coords != null && coords.size() > 0) {
				sCon.createField(plateIndex, wellIndex, fieldIndex);
				writeCoordinates(sCon, coords, plateIndex, wellIndex,
						fieldIndex);
			}

			// Writing the feature values
			Float[][] feature = new Float[vals.size()][vals.get(0).length];
			int numC = feature.length;
			int numF = feature[0].length;
			for (int i = 0; i < numC; i++)
				for (int j = 0; j < numF; j++)
					feature[i][j] = new Float(vals.get(i)[j]);
			Data2D<Float> cellFeature = new Data2D<Float>(feature);
			sCon.writeFeature(plateIndex, wellIndex, fieldIndex, cellFeature);

			// Writing the feature names to file
			Feature[] features = MainGUI.getGUI().getFeatures();
			StringBuffer[] fNames = new StringBuffer[features.length];
			for (int i = 0; i < features.length; i++)
				fNames[i] = new StringBuffer(features[i].toString());
			sCon.writeFeatureNames(plateIndex, wellIndex, fieldIndex, fNames);

		} catch (HDFConnectorException e) {
			e.printStackTrace();
		}
	}

	static public void writeCoordinates(SegmentationHDFConnector sCon,
			ArrayList<CellCoordinates> cellCoords, int plateIndex,
			int wellIndex, int fieldIndex) throws HDFConnectorException {
		// Assuming all cells in this cell bank are stored with the same
		// structure
		CellCoordinates cell = cellCoords.get(0);
		int numC = cell.getComSize();
		String comName = "";
		if (numC > 0)
			comName = cell.getComNames()[0];
		if (numC == 1) {
			comName = cell.getComNames()[0];
			if (comName.trim().equalsIgnoreCase("BoundingBox")) {
				// System.out.println("___Saving BoundingBoxes");
				sCon.writeCellBoundingBoxes(plateIndex, wellIndex, fieldIndex,
						cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Centroid")) {
				// System.out.println("___Saving Centroids");
				sCon.writeCellCentroids(plateIndex, wellIndex, fieldIndex,
						cellCoords);
			} else if (comName.trim().equalsIgnoreCase("Outline")) {
				// System.out.println("___Saving All Outlines");
				sCon.writeWholeCells(plateIndex, wellIndex, fieldIndex,
						cellCoords);
			}
		} else // Each cell has more than 1 compartment: ex: Nucleus,
		// cytoplasm
		// etc...
		{

			// System.out.println("___Saving All Coordinates");
			sCon.writeWholeCells(plateIndex, wellIndex, fieldIndex, cellCoords);
		}
	}

	/** 
	 * 
	 * */
	public void selectCellsToFilter(ArrayList<Cell> cells, Well well) {
		if (cells == null)
			return;
		int numCells = cells.size();
		if (numCells == 0)
			return;
		int counter = 0;

		// Selecting all cells above or below the pivot point of this filter
		for (int i = 0; i < numCells; i++) {
			Cell cell = cells.get(i);
			if (upDown == 1) // above pivotValue
				if (cell.getFeatureValues()[fIndex] > pivotValue) {
					cell.setSelected(true);
					counter++;
				} else {
					cell.setSelected(false);
					// System.out.println(cellValues[i][fIndex]);
				}
			else // below pivotValue
			if (cell.getFeatureValues()[fIndex] < pivotValue) {
				cell.setSelected(true);
				counter++;
			} else {
				cell.setSelected(false);
				// System.out.println(cellValues[i][fIndex]);
			}
		}
		// Tag to note that we should ask to save these changes at closing
		if (counter > 0)
			well.setCellsModified(true);
	}

	public boolean mouseOver(Point p) {
		if (arrow.contains(p)) {
			highlight = 0;
			return true;
		} else if (executeBox_selectedWells.contains(p)) {
			highlight = 1;
			return true;
		} else if (executeBox_allWells.contains(p)) {
			highlight = 2;
			return true;
		} else if (executeBox_allHDF.contains(p)) {
			highlight = 3;
			return true;
		}
 else if (box_addToFilterQueue.contains(p)) {
			highlight = 4;
			return true;
		}
		highlight = -1;
		return false;
	}

	public void kill() {
		arrow = null;
		closeBox = null;
		executeBox_allWells = null;
		executeBox_selectedWells = null;
		executeBox_allHDF = null;
		box_addToFilterQueue = null;
		roi = null;
		p = null;
	}
}