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

/**
 * ThresholdingBoundsInputDialog_SingleCells_Osteo.java
 *
 * @author Bjorn Millard
 */

package dialogs;

   import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import models.Model_Field;
import models.Model_ParameterSet;
import models.Model_Well;
import processors.Processor_SingleCells;
import segmentors.NucleiDescentAndMerge_OC;

public class ThresholdingBoundsInputDialog_SingleCells_Osteo extends JDialog
		implements ActionListener, PropertyChangeListener {
	private String typedText = null;
	private JTextField[] textField;
	private JOptionPane optionPane;
	private String btnString1 = "Run";
	private String btnString2 = "Cancel";
	private Model_Well[] TheWells;
	private JComboBox channelBox_nuc;
	private JComboBox channelBox_cyto;
	private JComboBox channelBox_membrane;
	private JComboBox channelBox_marker;
	private int CoordsToSave;

	public ThresholdingBoundsInputDialog_SingleCells_Osteo(Model_Well[] wells) {
		int width = 470;
		int height = 675;
		setTitle("Input");
		setSize(width, height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
				- height / 2);
		setModal(true);

		/** Features comboBox */
		ArrayList<String> list = new ArrayList<String>();
		String[] channelNames = models.Model_Main.getModel()
				.getTheChannelNames();
		int len = channelNames.length;

		for (int i = 0; i < len; i++)
			list.add(channelNames[i]);

		Object[] obX = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		channelBox_nuc = new JComboBox(obX);

		Object[] obX2 = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX2[i] = list.get(i);
		channelBox_cyto = new JComboBox(obX2);

		Object[] obX3 = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX3[i] = list.get(i);
		channelBox_membrane = new JComboBox(obX3);

		Object[] obX4 = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX4[i] = list.get(i);
		channelBox_marker = new JComboBox(obX4);

		TheWells = wells;
		textField = new JTextField[5];
		textField[0] = new JTextField(6); // Nuc bound
		textField[1] = new JTextField(6); // Marker bound
		textField[2] = new JTextField(6); // Membrane bound
		textField[3] = new JTextField(6); // merge bound
		textField[4] = new JTextField(6); // cyto param

		// Setting up the RaidioButtons for pixel saving selections
		JRadioButton r0 = new JRadioButton("Bounding Box");
		r0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				CoordsToSave = 0;
			}
		});
		JRadioButton r1 = new JRadioButton("Centroid");
		r1.setSelected(true);
		r1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				CoordsToSave = 1;
			}
		});
		JRadioButton r2 = new JRadioButton("Outlines");
		r2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				CoordsToSave = 2;
			}
		});
		JRadioButton r3 = new JRadioButton("Everything");
		r3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				CoordsToSave = 3;
			}
		});
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(r1);
		group.add(r0);
		group.add(r2);
		// group.add(r3);
		CoordsToSave = 1;
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(5, 1));
		radioPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		radioPanel.add(new JLabel("Coordinates for HDF Storage:"));
		radioPanel.add(r1);
		radioPanel.add(r0);
		radioPanel.add(r2);
		// radioPanel.add(r3);


		models.Model_Main.getModel().getGUI().getLoadCellsImmediatelyCheckBox()
				.setSelected(false);
		textField[2].setText("65536");
		textField[3].setText("0.1");

		models.Model_Main.getModel().getGUI().getLoadCellsImmediatelyCheckBox()
				.setSelected(false);

		Model_ParameterSet pset = Model_ParameterSet
				.doWellsHaveSameParameterSet(wells);
		if (pset != null) {
			// nucBoundChannel
			if (pset.exists("Thresh_Nuc_ChannelName")) {
			int num = channelBox_nuc.getItemCount();
			for (int i = 0; i < num; i++) {
				String name = (String) channelBox_nuc.getItemAt(i);
				if (name.equalsIgnoreCase(pset
						.getParameter_String("Thresh_Nuc_ChannelName")))
					channelBox_nuc.setSelectedIndex(i);
			}
			}

			if (pset.exists("ThreshChannel_marker_Name"))
				{
				// markerBoundChannel
				int num = channelBox_marker.getItemCount();
				for (int i = 0; i < num; i++) {
					String name = (String) channelBox_marker.getItemAt(i);
					if (name.equalsIgnoreCase(pset
							.getParameter_String("ThreshChannel_marker_Name")))
						channelBox_marker.setSelectedIndex(i);
				}
				}

			if (pset.exists("ThreshChannel_membrane_Name"))
				{
					// memBoundChannel
					int num = channelBox_membrane.getItemCount();
					for (int i = 0; i < num; i++) {
						String name = (String) channelBox_membrane.getItemAt(i);
						if (name.equalsIgnoreCase(pset
								.getParameter_String("ThreshChannel_membrane_Name")))
							channelBox_membrane.setSelectedIndex(i);
					}
				}
			if (pset.exists("Thresh_Cyt_ChannelName"))
				{
					// cytoBoundChannel
					int num = channelBox_cyto.getItemCount();
					for (int i = 0; i < num; i++) {
						String name = (String) channelBox_cyto.getItemAt(i);
						if (name.equalsIgnoreCase(pset
								.getParameter_String("Thresh_Cyt_ChannelName")))
							channelBox_cyto.setSelectedIndex(i);
					}
				}

			if (pset.exists("Thresh_Nuc_Value")) 
			textField[0].setText(""
					+ pset.getParameter_float("Thresh_Nuc_Value"));
			if (pset.exists("Thresh_Marker")) 
			textField[1].setText("" + pset.getParameter_float("Thresh_Marker"));
			if (pset.exists("Thresh_Membrane")) 
			textField[2].setText(""
					+ pset.getParameter_float("Thresh_Membrane"));
			if (pset.exists("Thresh_Cyt_Value")) 
			textField[4].setText(""
					+ pset.getParameter_float("Thresh_Cyt_Value"));

		}


		// Create an array of the text and components to be displayed.
		String[] mess = new String[9];
		mess[0] = "Nucleus Channel";
		mess[1] = "Nucleus Threshold (>)";
		mess[2] = "Osteoclast Marker Channel";
		mess[3] = "Osteoclast Marker Threshold (>)";
		mess[4] = "Membrane Channel";
		mess[5] = "Membrane Threshold (>)";
		mess[6] = "Merge Parameter (fraction common boundary):";
		mess[7] = "Precursor Cytoplasm Channel";
		mess[8] = "Precursor Cytoplasm Threshold (>)";

		Object[] array = { mess[0], channelBox_nuc, mess[1], textField[0],
				mess[2], channelBox_marker, mess[3], textField[1], mess[4],
				channelBox_membrane, mess[5], textField[2], mess[6],
				textField[3], mess[7], channelBox_cyto, mess[8], textField[4],
				new JLabel("   "), radioPanel };


		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { btnString1, btnString2 };

		// Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION, null, options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		// Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				textField[0].requestFocusInWindow();
			}
		});

		// Register an event handler that puts the text into the option pane.
		textField[0].addActionListener(this);

		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		setVisible(true);
	}

	/** This method handles events for the text field. */
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(btnString1);
	}

	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
						.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				// ignore reset
				return;
			}

			// Reset the JOptionPane's value.
			// If you don't do this, then if the user
			// presses the same button next time, no
			// property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			if (btnString1.equals(value)) {
				String[] strings = null;

				strings = new String[5];
				strings[0] = textField[0].getText(); // Nuc thresh
				strings[1] = textField[1].getText(); // marker Thresh
				strings[2] = textField[2].getText(); // membrane thresh
				strings[3] = textField[3].getText(); // merge thresh
				strings[4] = textField[4].getText(); // cyto thresh

				// make sure the inputed values are numbers only
				if (tools.MathOps.areNumbers(strings)) {
					int NucBoundaryChannel = channelBox_nuc.getSelectedIndex();
					int CytoBoundaryChannel = channelBox_cyto
							.getSelectedIndex();
					int MembraneBoundaryChannel = channelBox_membrane
							.getSelectedIndex();
					int MarkerBoundaryChannel = channelBox_marker
							.getSelectedIndex();

					float Thresh_Nuc_Value = Float.parseFloat(strings[0]);
					float Thresh_Marker = Float.parseFloat(strings[1]);
					float Thresh_Bkgd_Value = 0;
					float Thresh_Membrane = Float.parseFloat(strings[2]);
					float MergeFactor = Float.parseFloat(strings[3]);
					float Thresh_CellBoundary = Float.parseFloat(strings[4]);

					// Storing the Parameters for each Model_Well
					int len = TheWells.length;
					for (int i = 0; i < len; i++) {
						Model_Well well = TheWells[i];
						Model_Field[] fields = well.getFields();
						for (int p = 0; p < fields.length; p++) {

							Model_ParameterSet pset = fields[p]
									.getParameterSet();
						// ProcessType
						// pset.setProcessType(Model_ParameterSet.SINGLECELL);
						// Threshold Channel Nucleus
						pset.setParameter("Thresh_Nuc_ChannelName",""+models.Model_Main.getModel()
								.getTheChannelNames()[NucBoundaryChannel]);
						// Threshold Channel Marker
						pset.setParameter("ThreshChannel_marker_Name",""+models.Model_Main.getModel()
								.getTheChannelNames()[MarkerBoundaryChannel]);
						// Threshold Channel Cytoplasm
						pset.setParameter("Thresh_Cyt_ChannelName",models.Model_Main.getModel()
								.getTheChannelNames()[CytoBoundaryChannel]);
						// Nuc bound threshold
						pset.setParameter("Thresh_Nuc_Value",""+Thresh_Nuc_Value);
						// Marker bound threshold
						pset.setParameter("Thresh_Marker",""+Thresh_Marker);
						// Cell bound Threshold
						pset.setParameter("Thresh_Cyt_Value",""+Thresh_CellBoundary);
						// Bkgd threshold
						pset.setParameter("Thresh_Bkgd_Value",""+ Thresh_Bkgd_Value);
						// Thresh channel Membrane
						pset.setParameter("ThreshChannel_membrane_Name",""+models.Model_Main.getModel()
								.getTheChannelNames()[MembraneBoundaryChannel]);
						// Mem threshold
						pset.setParameter("Thresh_Membrane",""+Thresh_Membrane);

						if (CoordsToSave == 0)
							pset.setParameter("CoordsToSaveToHDF","BoundingBox");
						else if (CoordsToSave == 1)
							pset.setParameter("CoordsToSaveToHDF","Centroid");
						else if (CoordsToSave == 2)
							pset.setParameter("CoordsToSaveToHDF","Outlines");
						else if (CoordsToSave == 3)
							pset.setParameter("CoordsToSaveToHDF","Everything");

							pset.setParameter("MergeFactor", "" + MergeFactor);

							pset.setParameter("Algorithm", "Segmentor_Osteo_v1");


						// Finding the index of this channel name
						for (int j = 0; j < models.Model_Main.getModel()
								.getTheChannelNames().length; j++)
							if (models.Model_Main.getModel().getTheChannelNames()[j]
									.equalsIgnoreCase(pset.getParameter_String("Thresh_Nuc_ChannelName")))
									pset.setParameter(
											"Thresh_Nuc_ChannelIndex", ""
													+ j);
						// Finding the index of this channel name
						for (int j = 0; j < models.Model_Main.getModel()
								.getTheChannelNames().length; j++)
							if (models.Model_Main.getModel().getTheChannelNames()[j]
									.equalsIgnoreCase(pset
											.getParameter_String("ThreshChannel_marker_Name")))
								pset.setParameter("ThreshChannel_marker_Index",""+j);
						// Finding the index of this channel name
						for (int j = 0; j < models.Model_Main.getModel()
								.getTheChannelNames().length; j++)
							if (models.Model_Main.getModel().getTheChannelNames()[j]
									.equalsIgnoreCase(pset.getParameter_String("Thresh_Cyt_ChannelName")))
								pset.setParameter("Thresh_Cyt_ChannelIndex",""+j);
						// Finding the index of this channel name
						for (int j = 0; j < models.Model_Main.getModel()
								.getTheChannelNames().length; j++)
							if (models.Model_Main.getModel().getTheChannelNames()[j]
									.equalsIgnoreCase(pset
											.getParameter_String("ThreshChannel_membrane_Name")))
								pset.setParameter("ThreshChannel_membrane_Index",""+j);
						}
					}
					if (Thresh_Bkgd_Value > 0)
						models.Model_Main.getModel().setBackgroundSubtract(true);

					// Only getting wells with Images that we can process
					int numWells = TheWells.length;
					ArrayList<Model_Well> wellsWIm = new ArrayList<Model_Well>();
					for (int i = 0; i < numWells; i++)
						if (TheWells[i].getFields() != null
								&& TheWells[i].getFields().length > 0)
							wellsWIm.add(TheWells[i]);
					int numW = wellsWIm.size();
					Model_Well[] wellsWithImages = new Model_Well[numW];
					for (int i = 0; i < numW; i++)
						wellsWithImages[i] = wellsWIm.get(i);


					if (models.Model_Main.getModel().isProcessing())
 {
						JOptionPane
								.showMessageDialog(
										null,
										"Please wait till segmentation is complete before starting another process",
										"Be Patient", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// Single thread run
						Processor_SingleCells tasker = new Processor_SingleCells(
							wellsWithImages, new NucleiDescentAndMerge_OC());

					tasker.start();

				} else {
					JOptionPane
							.showMessageDialog(
									null,
									"Error with inputed bounds!  Make sure numbers make sense ",
									"Thresholds", JOptionPane.ERROR_MESSAGE);
					return;
				}
				typedText = null;
				clearAndHide();

			} else { // user closed dialog or clicked cancel
				typedText = null;
				clearAndHide();
			}
		}
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}
}
