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

package filters;


import imagerailio.ImageRail_SDCube;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import models.Model_Field;
import models.Model_Main;
import models.Model_Plate;
import models.Model_PlateRepository;
import models.Model_Well;
import sdcubeio.H5IO_Exception;
import segmentedobject.Cell;
import segmentedobject.CellCoordinates;

public class DotFilterQueue extends JFrame implements Runnable {

	private Thread thread;
	private DotFilterQueue TheFrame;
	private JPanel panel;
	private ArrayList<DotFilter> TheDotFilters;
	private CheckListItem[] checkBoxes;
	private ArrayList<DotFilter> filtersToRun;

	public DotFilterQueue() {

		super("Filter Queue");
		TheFrame = this;
		setResizable(true);
		int height = 400;
		int width = 470;
		setSize(width, height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		// setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
		// - height / 2 - 100);
		initFilters();

		initData();
		panel.repaint();
	}


	private void initData() {
		// Creating the CheckBoxes
		TheFrame.getContentPane().removeAll();
		int numF = TheDotFilters.size();
		checkBoxes = new CheckListItem[numF];
		Object[] arr = new Object[numF];
		for (int i = 0; i < numF; i++) {
			DotFilter f = (DotFilter) TheDotFilters.get(i);
			checkBoxes[i] = new CheckListItem(f);
			checkBoxes[i].setSelected(true);
			arr[i] = checkBoxes[i];

		}
		JPanel p1 = new JPanel();
		p1.setPreferredSize(new Dimension(20, 100));
		JPanel p2 = new JPanel();
		p2.setPreferredSize(new Dimension(20, 100));

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		CheckList ch = new CheckList();
		ch.init(checkBoxes);
		JScrollPane s = new JScrollPane(ch.getList());
		panel.add(s, BorderLayout.CENTER);
		panel
				.add(new JLabel("Feature Filters To Execute:"),
						BorderLayout.NORTH);
		panel.add(p1, BorderLayout.WEST);
		panel.add(p2, BorderLayout.EAST);
		panel.add(new BottomPanel(), BorderLayout.SOUTH);
		getContentPane().add(panel);
		setVisible(true);
	}

	public void addFilter(DotFilter filter) {
		TheDotFilters.add(filter);
		initData();
		panel.repaint();
	}

	public void initFilters() {
		TheDotFilters = new ArrayList<DotFilter>();
	}

	//
	// Setting up the Check list
	//
	//
	public class CheckList extends JPanel {
		private JList list;

		public void init(CheckListItem[] items) {
			// Create a list containing CheckListItem's
			list = new JList(items);
			// to renderer list cells
			list.setCellRenderer((ListCellRenderer) new CheckListRenderer());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// Add a mouse listener to handle changing selection
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					JList list = (JList) event.getSource();
					// Get index of item clicked
					int index = list.locationToIndex(event.getPoint());
					CheckListItem item = (CheckListItem) list.getModel()
							.getElementAt(index);
					// Toggle selected state
					item.setSelected(!item.isSelected());
					// Repaint cell
					list.repaint(list.getCellBounds(index, index));
				}
			});

		}

		public JList getList() {
			return list;
		}
	}

	// Represents items in the list that can be selected
	public class CheckListItem {
		private DotFilter filter;
		private boolean isSelected = false;

		public CheckListItem(DotFilter filter) {
			this.filter = filter;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected_) {
			isSelected = isSelected_;
		}

		public String toString() {
			String st2 = "";
			if (filter.getLessThanGreaterThan() == -1)
				st2 = "   <   ";
			else if (filter.getLessThanGreaterThan() == 1)
				st2 = "   >   ";

			String st = filter.getFeatureToFilter() + st2
					+ +filter.getPivotValue();
			return st;
		}

		public DotFilter getFilter() {
			return filter;
		}
	}

	// Handles rendering cells in the list using a check box
	class CheckListRenderer extends JCheckBox implements ListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean hasFocus) {
			setEnabled(list.isEnabled());
			setSelected(((CheckListItem) value).isSelected());
			setFont(list.getFont());
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}

	}

	private class BottomPanel extends JPanel {
		public BottomPanel() {
			setLayout(new GridLayout(0, 2));

			final JCheckBox check = new JCheckBox("Toggle all on/off");
			check.setSelected(true);
			check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					int len = checkBoxes.length;
					if (check.isSelected())
						for (int i = 0; i < len; i++)
							checkBoxes[i].setSelected(true);
					else
						for (int i = 0; i < len; i++)
							checkBoxes[i].setSelected(false);

					panel.validate();
					panel.repaint();
				}
			});
			add(check, 0);

			JPanel p = new JPanel(new GridLayout(0, 3));
			add(p, 1);



			JButton but = new JButton("Cancel");
			but.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					TheFrame.setVisible(false);
				}
			});
			p.add(but, 0);

			JButton but0 = new JButton("Delete");
			but0.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					TheDotFilters = getUnSelectedFilters();
					initData();
				}
			});
			p.add(but0, 1);

			but = new JButton("Execute");
			but.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					ArrayList<DotFilter> arr = getSelectedFilters();
					execute(arr);
				}
			});
			p.add(but, 2);

		}
	}

	private ArrayList<DotFilter> getSelectedFilters() {
		ArrayList<DotFilter> arr = new ArrayList<DotFilter>();
		// Finding the selected Features
		int len = checkBoxes.length;
		for (int i = 0; i < len; i++) {
			if (checkBoxes[i].isSelected)
				arr.add(checkBoxes[i].getFilter());
		}
		return arr;
	}

	private ArrayList<DotFilter> getUnSelectedFilters() {
		ArrayList<DotFilter> arr = new ArrayList<DotFilter>();
		// Finding the unselected Features
		int len = checkBoxes.length;
		for (int i = 0; i < len; i++) {
			if (!checkBoxes[i].isSelected)
				arr.add(checkBoxes[i].getFilter());
		}
		return arr;
	}

	/** Main execution method */
	public void execute(ArrayList<DotFilter> filters) {

		filtersToRun = filters;
		start();
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void run() {

		System.out.println("Filtering all HDF files");
		Model_Main theModel = models.Model_Main.getModel();
		Model_PlateRepository platePanel = theModel.getPlateRepository();
		int numPlates = platePanel.getNumPlates();
		Model_Plate[] plates = platePanel.getPlates();

		// Converting filters over to array before iterations
		int numFilters = filtersToRun.size();
		if (numFilters == 0) {
			JOptionPane.showMessageDialog(null,
					"	No Filters Selected! \n\n Please add/select some filters"
							+ " and try again\n\n", "Filter Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		DotFilter[] goFilters = new DotFilter[numFilters];
		int[] fIndex = new int[numFilters];
		for (int i = 0; i < numFilters; i++)
 {
			goFilters[i] = filtersToRun.get(i);
			fIndex[i] = goFilters[i].getFeatureToFilter().getGUIindex();
		}

		// for each well:
		ImageRail_SDCube io = models.Model_Main.getModel().getH5IO();

		for (int i = 0; i < numPlates; i++) {
			Model_Plate plate = plates[i];
			int numC = plate.getNumColumns();
			int numR = plate.getNumRows();
			for (int r = 0; r < numR; r++)
				for (int c = 0; c < numC; c++) {
					Model_Well well = plate.getWells()[r][c];
					well.processing = true;
					plate.getGUI().repaint();
					if (well.getHDFcount() > 0)// Just the HDF5 files
					{
						System.out.println("Processing: " + well.name);
						Model_Field[] fields = well.getFields();
						int numF = fields.length;
						try {
							for (int j = 0; j < numF; j++) {

								// Load this well's cells, filter them,
								// then write them back to the HDF files
								ArrayList<Cell> arr = io.readCells(plate
										.getID(), well.getWellIndex(),
										fields[j].getIndexInWell());

								// Selecting all cells above or below the
								// pivot
								// point of this filter
								int numCells = arr.size();
								ArrayList<CellCoordinates> keepers_coords = new ArrayList<CellCoordinates>();
								ArrayList<float[]> keepers_vals = new ArrayList<float[]>();

								for (int n = 0; n < numCells; n++) {
									Cell cell = arr.get(n);
									boolean pass = true;
									// See if this cell passes all the filters
									for (int f = 0; f < numFilters; f++) {
										float pivotValue = goFilters[f]
												.getPivotValue();
										int upDown = goFilters[f]
												.getLessThanGreaterThan();
										if (upDown == 1) { // above pivotValue
											if (cell.getFeatureValues()[fIndex[f]] > pivotValue) {
												pass = false;
												break;
											}
										} else // below pivotValue
										if (cell.getFeatureValues()[fIndex[f]] < pivotValue) {
											pass = false;
											break;
										}
									}
									if (pass) {
										keepers_coords.add(cell
												.getCoordinates());
										keepers_vals.add(cell
												.getFeatureValues());
									}
								}

								// Resaving just the unselected cells
								DotFilter.resaveCells(io, keepers_coords,
										keepers_vals, well.getID(),
										plate.getID(), well.getWellIndex(),
										fields[j].getIndexInWell());

								// Killing temp loaded cells
								int lenC = keepers_coords.size();
								for (int n = 0; n < lenC; n++)
									keepers_coords.get(n).kill();
								keepers_coords = null;
								keepers_vals = null;
							}
						} catch (H5IO_Exception e) {
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
						well.loadCells(io, loadCoords, loadVals);
					}
					well.processing = false;
					plate.getGUI().repaint();
				}
		}
		theModel.getPlateRepository().updateMinMaxValues();
		if (theModel.getGUI() != null)
			theModel.getGUI().updateDotPlot();
	}
}
