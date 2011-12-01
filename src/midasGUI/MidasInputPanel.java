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

package midasGUI;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import models.Model_Plate;
import models.Model_Well;
import sdcubeio.ExpDesign_Description;
import sdcubeio.ExpDesign_Model;



public class MidasInputPanel extends JPanel
{
	
    private JTextField[] textField;
	private JLabel wellTitleLabel;
    private JOptionPane optionPane;
	
	private Model_Plate thePlate;
	private JComboBox treatmentComboBox;
	private JComboBox measurementComboBox;
	private JButton addTreatmentButton;
	private JButton addMeasurementButton;
	private JButton removeTreatmentButton;
	private JButton editTreatmentButton;
	private JButton removeMeasurementButton;
	private JButton editMeasurementButton;
	private JCheckBox[] checkBoxes;
	private Model_Well[] theWells;
	private JPanel TheInputContainerPanel;
	private ExpDesign_Model TheExpDesign_Model;
	
	public MidasInputPanel(Model_Plate plate, ExpDesign_Model expDesign_model)
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		thePlate = plate;
		TheExpDesign_Model = expDesign_model;
		setLayout(new BorderLayout());
		setVisible(true);
		int width = 330;
		int height = 450;
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		theWells = thePlate.getAllSelectedWells();

		textField = new JTextField[5];
		for (int i=0; i < textField.length; i ++)
			textField[i] = new JTextField(6);
		
		
		
		//Getting date information
		textField[1].getDocument().addDocumentListener(new DocumentListener()
													   {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = textField[1].getText();
				// date
				if (checkBoxes[0].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

						if (!string.equalsIgnoreCase("")) {
							ExpDesign_Description desc = new ExpDesign_Description(
									"Date", null, string, null, null, null,
									"Date");

								TheExpDesign_Model.replaceDescription(theWells[i]
									.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Date");

					}

					TheExpDesign_Model.write();
				}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = textField[1].getText();
				// date
				if (checkBoxes[0].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

						if (!string.equalsIgnoreCase("")) {
							ExpDesign_Description desc = new ExpDesign_Description(
									"Date", null, string, null, null, null,
									"Date");

							TheExpDesign_Model.replaceDescription(theWells[i]
									.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Date");

					}

					TheExpDesign_Model.write();
				}
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
				String string = textField[1].getText();
				// date
				if (checkBoxes[0].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

						if (!string.equalsIgnoreCase("")) {
							ExpDesign_Description desc = new ExpDesign_Description(
									"Date", null, string, null, null, null,
									"Date");

							TheExpDesign_Model.replaceDescription(theWells[i]
									.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Date");

					}

					TheExpDesign_Model.write();
				}
					}
				});
		
		
		//Getting description information
		textField[2].getDocument().addDocumentListener(new DocumentListener()
													   {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = textField[2].getText();
				if (checkBoxes[1].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

							if (!string.equalsIgnoreCase("")) {
						theWells[i].description = string;

						
						ExpDesign_Description desc = new ExpDesign_Description();
						desc.setType("Description");
						desc.setValue(string);
						TheExpDesign_Model.replaceDescription(theWells[i]
								.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Description");
						}
						TheExpDesign_Model.write();
				}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = textField[2].getText();
				if (checkBoxes[1].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

						if (!string.equalsIgnoreCase("")) {
						theWells[i].description = string;


						ExpDesign_Description desc = new ExpDesign_Description();
						desc.setType("Description");
						desc.setValue(string);
						TheExpDesign_Model.replaceDescription(theWells[i]
								.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Description");
					}
						TheExpDesign_Model.write();
						}
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = textField[2].getText();
				if (checkBoxes[1].isSelected()) {
						for (int i =0; i < theWells.length; i++)
						{

						if (!string.equalsIgnoreCase("")) {
						theWells[i].description = string;


						ExpDesign_Description desc = new ExpDesign_Description();
						desc.setType("Description");
						desc.setValue(string);
						TheExpDesign_Model.replaceDescription(theWells[i]
								.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Description");
					}
						TheExpDesign_Model.write();
						}
					}
				});
		
		
		//Getting measurement time information
		textField[3].getDocument().addDocumentListener(new DocumentListener()
													   {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
				String string = textField[3].getText().trim();


				String time = string;
				String time_units = "";

				int ind = string.indexOf(" ");
				if (ind > 0) {
					time = string.substring(0, ind);
					time_units = string.substring(ind + 1, string.length());
				}
						
				// date
				if (checkBoxes[4].isSelected()) {
					for (int i = 0; i < theWells.length; i++) {

						if (!string.equalsIgnoreCase("")) {

						theWells[i].measurementTime = string;

					ExpDesign_Description desc = new ExpDesign_Description(
								"Measurement_Time", null, null, null, time,
									time_units, "Time");

					TheExpDesign_Model.replaceDescription(theWells[i]
								.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Measurement_Time");

				}
					TheExpDesign_Model.write();
					}
						
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
				String string = textField[3].getText().trim();

						
				
						String time = string;
				String time_units = "";

						int ind = string.indexOf(" ");
				if (ind > 0) {
					time = string.substring(0, ind);
					time_units = string.substring(ind + 1, string.length());
				}

				// date
				if (checkBoxes[4].isSelected()) {
					for (int i = 0; i < theWells.length; i++) {

								if (!string.equalsIgnoreCase("")) {

							theWells[i].measurementTime = string;

							ExpDesign_Description desc = new ExpDesign_Description(
									"Measurement_Time", null, null, null, time,
									time_units, "Time");

							TheExpDesign_Model.replaceDescription(theWells[i]
									.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Measurement_Time");

					}
					TheExpDesign_Model.write();
				}
			}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
				String string = textField[3].getText().trim();
						String time = string;
				String time_units = "";

						int ind = string.indexOf(" ");
				if (ind > 0) {
					time = string.substring(0, ind);
					time_units = string.substring(ind + 1, string.length());
				}

				// date
				if (checkBoxes[4].isSelected()) {
					for (int i = 0; i < theWells.length; i++) {

								if (!string.equalsIgnoreCase("")) {

							theWells[i].measurementTime = string;

							ExpDesign_Description desc = new ExpDesign_Description(
									"Measurement_Time", null, null, null, time,
									time_units, "Time");

							TheExpDesign_Model.replaceDescription(theWells[i]
									.getID(), desc);
						} else
							// remove it completely if there is no value
							TheExpDesign_Model.removeDescriptionOfType(
									theWells[i].getID(), "Measurement_Time");

					}
					TheExpDesign_Model.write();
				}
			}
				});
		
		
		
		treatmentComboBox = new JComboBox();
		treatmentComboBox.setVisible(true);
		measurementComboBox = new JComboBox();
		measurementComboBox.setVisible(true);
		
		
		JPanel butPan = new JPanel();
		addTreatmentButton = new JButton("Add");
		butPan.add(addTreatmentButton);
		addTreatmentButton.addActionListener(new ActionListener()
											 {
					public void actionPerformed(ActionEvent ae)
					{
						NewTreatmentInputDialog n = new NewTreatmentInputDialog();
				// thePlate.getGUI().updateMetaDataLegend();
				updateInputPanel();
					}
					
					
				});
		removeTreatmentButton = new JButton("Remove");
		butPan.add(removeTreatmentButton);
		removeTreatmentButton.addActionListener(new ActionListener()
												{
					public void actionPerformed(ActionEvent ae)
					{
						if (treatmentComboBox.getItemCount()>0)
						{
					ExpDesign_Description treat = (ExpDesign_Description) treatmentComboBox
							.getSelectedItem();
							int len = theWells.length;
							for (int i = 0; i < len; i++)
						TheExpDesign_Model.removeDescription(theWells[i]
								.getID(), treat);
							

							treatmentComboBox.removeItemAt(treatmentComboBox.getSelectedIndex());
							treatmentComboBox.repaint();
							
					TheExpDesign_Model.write();
					// thePlate.getGUI().updateMetaDataLegend();
						}
					}
				});
		editTreatmentButton = new JButton("Edit");
		butPan.add(editTreatmentButton);
		editTreatmentButton.addActionListener(new ActionListener()
											  {
					public void actionPerformed(ActionEvent ae)
					{
						if (treatmentComboBox.getItemCount()>0)
						{
					ExpDesign_Description treat = (ExpDesign_Description) treatmentComboBox
							.getSelectedItem();
							//open the edit dialog
							NewTreatmentInputDialog n = new NewTreatmentInputDialog(treat);
							
							treatmentComboBox.repaint();
					thePlate.getGUI().updateMetaDataLegend();
						}
					}
				});
		
		
		
		
		
		JPanel butPan2 = new JPanel();
		addMeasurementButton = new JButton("Add");
		butPan2.add(addMeasurementButton);
		addMeasurementButton.addActionListener(new ActionListener()
											   {
					public void actionPerformed(ActionEvent ae)
					{
						NewMeasurmentInputDialog n = new NewMeasurmentInputDialog();
						
						measurementComboBox.repaint();
				thePlate.getGUI().updateMetaDataLegend();
					}
				});
		removeMeasurementButton = new JButton("Remove");
		butPan2.add(removeMeasurementButton);
		removeMeasurementButton.addActionListener(new ActionListener()
												  {
					public void actionPerformed(ActionEvent ae)
					{
						if (measurementComboBox.getItemCount()>0)
						{
					ExpDesign_Description meas = (ExpDesign_Description) measurementComboBox
							.getSelectedItem();
							int len = theWells.length;
							for (int i = 0; i < len; i++)
						TheExpDesign_Model.removeDescription(
								theWells[i].getID(), meas);
							
							measurementComboBox.removeItemAt(measurementComboBox.getSelectedIndex());
							measurementComboBox.repaint();
					TheExpDesign_Model.write();

					// thePlate.getGUI().updateMetaDataLegend();
						}
					}
				});
		editMeasurementButton = new JButton("Edit");
		butPan2.add(editMeasurementButton);
		editMeasurementButton.addActionListener(new ActionListener()
												{
					public void actionPerformed(ActionEvent ae)
					{
						if (measurementComboBox.getItemCount()>0)
						{
					ExpDesign_Description meas = (ExpDesign_Description) measurementComboBox
							.getSelectedItem();
							//open the edit dialog
							NewMeasurmentInputDialog n = new NewMeasurmentInputDialog(meas);
							

					// TheExpDesign_Model.write();
							measurementComboBox.repaint();
					thePlate.getGUI().updateMetaDataLegend();
						}
					}
				});
		
		
		checkBoxes = new JCheckBox[5];
		checkBoxes[0] = new JCheckBox("Date:");
		checkBoxes[0].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						textField[1].setEnabled(checkBoxes[0].isSelected());
						textField[1].setEditable(checkBoxes[0].isSelected());
					}
				});
		checkBoxes[1] = new JCheckBox("Description:");
		checkBoxes[1].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						textField[2].setEnabled(checkBoxes[1].isSelected());
						textField[2].setEditable(checkBoxes[1].isSelected());
					}
				});
		checkBoxes[2] = new JCheckBox("Treatments:");
		checkBoxes[2].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						treatmentComboBox.setEnabled(checkBoxes[2].isSelected());
						addTreatmentButton.setEnabled(checkBoxes[2].isSelected());
						removeTreatmentButton.setEnabled(checkBoxes[2].isSelected());
						editTreatmentButton.setEnabled(checkBoxes[2].isSelected());
					}
				});
		checkBoxes[3] = new JCheckBox("Measurements:");
		checkBoxes[3].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						measurementComboBox.setEnabled(checkBoxes[3].isSelected());
						addMeasurementButton.setEnabled(checkBoxes[3].isSelected());
						removeMeasurementButton.setEnabled(checkBoxes[3].isSelected());
						editMeasurementButton.setEnabled(checkBoxes[3].isSelected());
					}
				});
		
		checkBoxes[4] = new JCheckBox("Measurement Time Point:");
		checkBoxes[4].addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						textField[3].setEnabled(checkBoxes[4].isSelected());
						textField[3].setEditable(checkBoxes[4].isSelected());
					}
				});
		
		for (int i =0;i  < checkBoxes.length; i++)
		{
			checkBoxes[i].setSelected(true);
			checkBoxes[i].doClick();
		}
		BufferPanel buffer1 = new BufferPanel(BorderLayout.EAST);
		add(buffer1, BorderLayout.EAST);
		BufferPanel buffer = new BufferPanel(BorderLayout.WEST);
		add(buffer, BorderLayout.WEST);
		BufferPanel buffer2 = new BufferPanel(BorderLayout.SOUTH);
		add(buffer2, BorderLayout.SOUTH);
		
		JPanel pan = new JPanel();
		pan.setLayout(new BorderLayout());
		pan.add(wellTitleLabel = new JLabel("No wells Selected"), BorderLayout.CENTER);
		wellTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		wellTitleLabel.setFont(new Font("Helvetca", Font.BOLD, 18));
		pan.add(new BufferPanel(BorderLayout.EAST) ,BorderLayout.EAST);
		pan.add(new BufferPanel(BorderLayout.WEST) ,BorderLayout.WEST);
		add(pan, BorderLayout.NORTH);
		
		TheInputContainerPanel = new JPanel();
		TheInputContainerPanel.setLayout(new GridLayout(13,1));
		TheInputContainerPanel.add(checkBoxes[0],0);
		TheInputContainerPanel.add(textField[1],1);
		TheInputContainerPanel.add(checkBoxes[1],2);
		TheInputContainerPanel.add(textField[2],3);
		TheInputContainerPanel.add(checkBoxes[2],4);
		TheInputContainerPanel.add(treatmentComboBox,5);
		TheInputContainerPanel.add(butPan,6);
		TheInputContainerPanel.add(checkBoxes[3],7);
		TheInputContainerPanel.add(measurementComboBox,8);
		TheInputContainerPanel.add(butPan2,9);
		TheInputContainerPanel.add(checkBoxes[4],10);
		TheInputContainerPanel.add(textField[3],11);
		add(TheInputContainerPanel, BorderLayout.CENTER);
		JPanel buffP = new JPanel();
		buffP.setPreferredSize(new Dimension(125,100));
		JPanel buffP2 = new JPanel();
		buffP2.setPreferredSize(new Dimension(125,100));
		add(buffP, BorderLayout.EAST);
		add(buffP2, BorderLayout.WEST);
		updateInputPanel();
	}

	/**
	 *
	 * */
	public void updateInputPanel() {

		enableComponents(false);
		theWells = thePlate.getAllSelectedWells();
		
		int numWells = theWells.length;
		if (numWells==0)
		{
			wellTitleLabel.setText("No Wells Selected");
			TheInputContainerPanel.setVisible(false);
		}
		else
		{
			TheInputContainerPanel.setVisible(true);
			String[][] ranges = getWellRanges(theWells);
			String str1 = ranges[0][0]+ranges[1][0];
			String str2 = ranges[0][1]+ranges[1][1];
			if (str1.equalsIgnoreCase(str2))
				wellTitleLabel.setText("Well: " + str1);
			else
				wellTitleLabel.setText("Wells: "+str1+ "-"+str2);
			
			//inserting appropriate info into each component
			
			//date:
			boolean same = true;

			ExpDesign_Description val0 = TheExpDesign_Model.getDate(theWells[0]
					.getID());
			for (int i=1; i < numWells; i++)
			{
				ExpDesign_Description date_i = TheExpDesign_Model
						.getDate(theWells[i].getID());
				if (date_i == null)
				{
					same = false;
					break;
				}
				if (val0 != null
						&& !val0.getValue().equalsIgnoreCase(date_i.getValue()))
				{
					same = false;
					break;
				}
			}
			if (same && val0!=null)
				textField[1].setText(val0.getValue());
			else
				textField[1].setText("");
			
			//description:
			same = true;
			val0 = TheExpDesign_Model.getDescription(theWells[0]
.getID(),
					"Description");
			for (int i=1; i < numWells; i++)
			{
				ExpDesign_Description val_i = TheExpDesign_Model
						.getDescription(theWells[i].getID(), "Description");
				if(val_i==null)
				{
					same = false;
					break;
				}
				if (val0 != null
						&& val0.getValue() != null
						&& !val0.getValue().equalsIgnoreCase(
val_i.getValue()))
				{
					same = false;
					break;
				}
			}
			if (same&&val0!=null)
				textField[2].setText(val0.getValue());
			else
				textField[2].setText("");
			
			//Treatments
			ExpDesign_Description[] arr0 = TheExpDesign_Model
					.getTreatments(theWells[0].getID());
			if (arr0 != null) {
			int len = arr0.length;
			treatmentComboBox.removeAllItems();
			for (int i=0; i < len; i++)
			{
				boolean containsIt = true;
				for (int j=1; j < numWells; j++)
				{
					boolean foundIt = false;
						ExpDesign_Description[] arrj = TheExpDesign_Model
								.getTreatments(theWells[j].getID()
										);
					for (int z = 0; z < arrj.length; z++)
					{
						if(arrj[z].isSame(arr0[i]))
						{
							foundIt = true;
							break;
						}
					}
					if(!foundIt)
					{
						containsIt = false;
						break;
					}
				}
				//Finally if it was in all wells, then we add it
				if(containsIt)
					treatmentComboBox.addItem(arr0[i]);
			}
			}

			
			//Measurements
			arr0 = TheExpDesign_Model.getMeasurements(theWells[0].getID());
			if (arr0 != null) {
				int len = arr0.length;
			measurementComboBox.removeAllItems();
			for (int i=0; i < len; i++)
			{
				boolean containsIt = true;
				for (int j=1; j < numWells; j++)
				{
					boolean foundIt = false;
						ExpDesign_Description[] arrj = TheExpDesign_Model
								.getMeasurements(theWells[j].getID());
					for (int z = 0; z < arrj.length; z++)
					{
						if(arrj[z].isSame(arr0[i]))
						{
							foundIt = true;
							break;
						}
					}
					if(!foundIt)
					{
						containsIt = false;
						break;
					}
				}
				//Finally if it was in all wells, then we add it
				if(containsIt)
					measurementComboBox.addItem(arr0[i]);
			}
			}
			
			//Measurement Time Point:
			same = true;
			val0 = TheExpDesign_Model.getTimePoint(theWells[0]
.getID());


			for (int i=1; i < numWells; i++)
			{
				ExpDesign_Description val_i = TheExpDesign_Model
						.getTimePoint(theWells[i].getID());

				if(val_i==null)
				{
					same = false;
					break;
				}
				if (val0 != null && !val0.isSame(val_i))
				{
					same = false;
					break;
				}
			}
			if (same&&val0!=null)
				textField[3].setText(val0.getTimeValue() + " "
						+ val0.getTimeUnits());
			else
				textField[3].setText("");
			
		}
		
		for (int i =0; i < checkBoxes.length; i++)
			checkBoxes[i].setSelected(false);
		enableComponents();

	}
	

	// /**
	// *
	// * */
	// private boolean containSameMeasurements(Description[] arr1, Description[]
	// arr2)
	// {
	// int len1 = arr1.length;
	// int len2 = arr2.length;
	// for (int i =0; i < len1; i++)
	// {
	// Description meas = arr1[i];
	// boolean foundIt = false;
	// for (int j =0 ; j < len2; j++)
	// {
	// if((arr2[j]).isSame(meas))
	// {
	// foundIt = true;
	// break;
	// }
	// }
	// if (!foundIt)
	// return false;
	// }
	//		
	// return true;
	// }
	//	
	// private boolean containSameTreatments(Description[] arr1, Description[]
	// arr2)
	// {
	// int len1 = arr1.length;
	// int len2 = arr2.length;
	// for (int i =0; i < len1; i++)
	// {
	// Description treat = (Description)arr1[i];
	// boolean foundIt = false;
	// for (int j =0 ; j < len2; j++)
	// {
	// if(arr2[j].isSame(treat))
	// {
	// foundIt = true;
	// break;
	// }
	// }
	// if (!foundIt)
	// return false;
	// }
	//		
	// return true;
	// }
	
	private void enableComponents(boolean boo)
	{
		textField[1].setEnabled(boo);
		textField[1].setEditable(boo);
		
		textField[2].setEnabled(boo);
		textField[2].setEditable(boo);
		
		treatmentComboBox.setEnabled(boo);
		addTreatmentButton.setEnabled(boo);
		removeTreatmentButton.setEnabled(boo);
		editTreatmentButton.setEnabled(boo);
		
		measurementComboBox.setEnabled(boo);
		addMeasurementButton.setEnabled(boo);
		removeMeasurementButton.setEnabled(boo);
		editMeasurementButton.setEnabled(boo);
		
		textField[3].setEnabled(boo);
		textField[3].setEditable(boo);
		
		for (int i = 0; i < checkBoxes.length; i++)
			checkBoxes[i].setSelected(false);
	}
	
	private void enableComponents()
	{
		textField[1].setEnabled(checkBoxes[0].isSelected());
		textField[1].setEditable(checkBoxes[0].isSelected());
		
		textField[2].setEnabled(checkBoxes[1].isSelected());
		textField[2].setEditable(checkBoxes[1].isSelected());
		
		treatmentComboBox.setEnabled(checkBoxes[2].isSelected());
		addTreatmentButton.setEnabled(checkBoxes[2].isSelected());
		removeTreatmentButton.setEnabled(checkBoxes[2].isSelected());
		editTreatmentButton.setEnabled(checkBoxes[2].isSelected());
		
		measurementComboBox.setEnabled(checkBoxes[3].isSelected());
		addMeasurementButton.setEnabled(checkBoxes[3].isSelected());
		removeMeasurementButton.setEnabled(checkBoxes[3].isSelected());
		editMeasurementButton.setEnabled(checkBoxes[3].isSelected());
		
		textField[3].setEnabled(checkBoxes[4].isSelected());
		textField[3].setEditable(checkBoxes[4].isSelected());
	}
	
	private String[][] getWellRanges(Model_Well[] wells)
	{
		String[][] ranges = new String[2][2];
		int len = wells.length;
		
		String rowMin = "Z";
		String rowMax = "A";
		int colMin = 100;
		int colMax = -1;
		
		for (int i =0; i < len ;i ++)
		{
			if (wells[i].Column<=colMin)
				colMin = wells[i].Column;
			if (wells[i].Column>=colMax)
				colMax = wells[i].Column;
			
			
			if (Model_Plate.getRowName(wells[i].Row).compareTo(rowMin)<0)
				rowMin = Model_Plate.getRowName(wells[i].Row);
			if (Model_Plate.getRowName(wells[i].Row).compareTo(rowMin)>=0)
				rowMax = Model_Plate.getRowName(wells[i].Row);
		}
		
		ranges[0][0] = rowMin;
		ranges[0][1] = rowMax;
		
		ranges[1][0] = ""+Model_Plate.getColumnName(colMin);
		ranges[1][1] = ""+Model_Plate.getColumnName(colMax);
		
		return ranges;
	}
	
	/** Looks at all the wells in this plate and returns a list of all unique treatments
	 * @author BLM*/
	private ArrayList<ExpDesign_Description> getAllUniqueTreatments(
			Model_Plate plate)
	{
		ArrayList<ExpDesign_Description> arr = new ArrayList<ExpDesign_Description>();
		
		for (int i = 0; i < plate.getNumRows(); i++)
		{
			for (int j = 0; j < plate.getNumColumns(); j++)
			{
				ExpDesign_Description[] des = TheExpDesign_Model
						.getTreatments(plate
						.getWells()[i][j].getID());
				if (des!=null)
				{
					int len = des.length;
					for (int z = 0; z < len; z++)
					{
						boolean unique = true;
						for (int p = 0; p < arr.size(); p++)
						{
							if (des[z].isSame(arr.get(p)))
							{
								unique=false;
								break;
							}
						}
						if (unique)
							arr.add(des[z]);
					}
				}
			}
		}
		
		return arr;
	}
	
	
	public class NewTreatmentInputDialog extends JDialog implements ActionListener, PropertyChangeListener
	{
		private JTextField[] textField;
		private JOptionPane optionPane;
		private String btnString1 = "Enter";
		private String btnString2 = "Cancel";
		private JComboBox CurrentTreatments;
		private ExpDesign_Description TreatmentToEdit;
		
		
		/** Creates the reusable dialog. */
		public NewTreatmentInputDialog()
		{
			TreatmentToEdit = null;
			int width = 330;
			int height = 400;
			setTitle("Add Treatment:");
			setSize(width,height);
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
			setModal(true);
			
			//adding currently used treatments
			ArrayList<ExpDesign_Description> allUniqueTreatments = getAllUniqueTreatments(thePlate);
			int numT =	allUniqueTreatments.size();
			Object[] treats = new Object[numT];
			for (int i =0; i < numT; i++)
				treats[i] = allUniqueTreatments.get(i);
			CurrentTreatments = new JComboBox(treats);
			CurrentTreatments.setEditable(false);
			
			
			textField = new JTextField[6];
			for (int i=0; i < textField.length; i ++)
				textField[i] = new JTextField(10);
			
			CurrentTreatments.setEnabled(false);
			CurrentTreatments.setEnabled(false);
			textField[0].setEditable(true);
			textField[1].setEditable(true);
			textField[2].setEditable(true);
			textField[3].setEditable(true);
			textField[4].setEditable(true);
			textField[5].setEditable(true);

			textField[0].setEnabled(true);
			textField[1].setEnabled(true);
			textField[2].setEnabled(true);
			textField[3].setEnabled(true);
			textField[4].setEnabled(true);
			textField[5].setEnabled(true);

			
			Object[] array = { "Name:", textField[0], "Value", textField[1],
					"Units", textField[2], "Time", textField[3], "Time Units",
					textField[4], "Category", textField[5] };
			//Create an array specifying the number of dialog buttons
			//and their text.
			Object[] options = {btnString1, btnString2};
			
			//Create the JOptionPane.
			optionPane = new JOptionPane(array,
										 JOptionPane.QUESTION_MESSAGE,
										 JOptionPane.YES_NO_OPTION,
										 null,
										 options,
										 options[0]);
			
			//Make this dialog display it.
			setContentPane(optionPane);
			
			//Handle window closing correctly.
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
							  {
						public void windowClosing(WindowEvent we)
						{
							
							optionPane.setValue(new Integer(
													JOptionPane.CLOSED_OPTION));
						}
					});
			
			
			
			//Register an event handler that reacts to option pane state changes.
			optionPane.addPropertyChangeListener(this);
			setVisible(true);
		}
		
		/** This constructor lets you edit an existing treatment */
		public NewTreatmentInputDialog(ExpDesign_Description treatmentToEdit)
		{
			TreatmentToEdit = treatmentToEdit;
			int width = 330;
			int height = 400;
			setTitle("Edit Treatment:");
			setSize(width,height);
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
			setModal(true);
			
			//adding currently used treatments
			ArrayList<ExpDesign_Description> allUniqueTreatments = getAllUniqueTreatments(thePlate);
			int numT =	allUniqueTreatments.size();
			Object[] treats = new Object[numT];
			for (int i =0; i < numT; i++)
				treats[i] = allUniqueTreatments.get(i);
			CurrentTreatments = new JComboBox(treats);
			CurrentTreatments.setEditable(false);
			
			
			textField = new JTextField[6];
			for (int i=0; i < textField.length; i ++)
				textField[i] = new JTextField(10);
			
			CurrentTreatments.setEnabled(false);
			CurrentTreatments.setEditable(false);
			textField[0].setEditable(true);
			textField[1].setEditable(true);
			textField[2].setEditable(true);
			textField[3].setEditable(true);
			textField[4].setEditable(true);
			textField[5].setEditable(true);


			textField[0].setEnabled(true);
			textField[1].setEnabled(true);
			textField[2].setEnabled(true);
			textField[3].setEnabled(true);
			textField[4].setEnabled(true);
			textField[5].setEnabled(true);
			
			textField[0].setText(treatmentToEdit.getName());
			textField[1].setText(treatmentToEdit.getValue());
			textField[2].setText(treatmentToEdit.getUnits());
			textField[3].setText(treatmentToEdit.getTimeValue());
			textField[4].setText(treatmentToEdit.getTimeUnits());
			textField[5].setText(treatmentToEdit.getCategory());
			
			Object[] array = {"Name:", textField[0], "Value",textField[1], "Units", textField[2], "Time", textField[3], "Time Units",
					textField[4], "Category", textField[5] };
			
			//Create an array specifying the number of dialog buttons
			//and their text.
			Object[] options = {btnString1, btnString2};
			
			//Create the JOptionPane.
			optionPane = new JOptionPane(array,
										 JOptionPane.QUESTION_MESSAGE,
										 JOptionPane.YES_NO_OPTION,
										 null,
										 options,
										 options[0]);
			
			//Make this dialog display it.
			setContentPane(optionPane);
			
			//Handle window closing correctly.
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
							  {
						public void windowClosing(WindowEvent we)
						{
							optionPane.setValue(new Integer(
													JOptionPane.CLOSED_OPTION));
						}
					});
			
			
			
			//Register an event handler that reacts to option pane state changes.
			optionPane.addPropertyChangeListener(this);
			setVisible(true);
		}
		
		/** This method handles events for the text field. */
		public void actionPerformed(ActionEvent e)
		{
			optionPane.setValue(btnString1);
		}
		
		
		
		/** This method reacts to state changes in the option pane. */
		public void propertyChange(PropertyChangeEvent e)
		{
			String prop = e.getPropertyName();
			
			if (isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
			{
				Object value = optionPane.getValue();
				
				if (value == JOptionPane.UNINITIALIZED_VALUE)
				{
					//ignore reset
					return;
				}
				
				
				optionPane.setValue(
					JOptionPane.UNINITIALIZED_VALUE);
				
				if (btnString1.equals(value))
				{
					ExpDesign_Description theTreatment = null;
					
					String name = textField[0].getText().trim();
					String val = textField[1].getText().trim();
					String units = textField[2].getText().trim();
					String timeValue = textField[3].getText().trim();
					String timeUnits = textField[4].getText().trim();
					String category = textField[5].getText().trim();
					
					theTreatment = new ExpDesign_Description("Treatment", name,
							"" + val, units, timeValue, timeUnits, category);
					
					treatmentComboBox.addItem(theTreatment);
					treatmentComboBox.repaint();
					
					
					// setting it automatically now
					for (int i =0; i < theWells.length; i++)
							TheExpDesign_Model.addDescription(theWells[i]
									.getID(), theTreatment.getCopy());


					//If we were editing an old treatment, delete old one now
					//then delete the old one
					if(TreatmentToEdit!=null)
					{
						treatmentComboBox.removeItemAt(treatmentComboBox.getSelectedIndex());
						int len = theWells.length;
						for (int i = 0; i < len; i++)
							TheExpDesign_Model.removeDescription(
									theWells[i].getID(), TreatmentToEdit);
					}
					
					TheExpDesign_Model.write();
					clearAndHide();
					
				}
				else { //user closed dialog or clicked cancel
					clearAndHide();
				}
			}
			

			// thePlate.getGUI().updateMetaDataLegend();
		}
		
		/** This method clears the dialog and hides it. */
		public void clearAndHide()
		{
			setVisible(false);
		}
	}
	
	
	public class NewMeasurmentInputDialog extends JDialog implements ActionListener, PropertyChangeListener
	{
		private JTextField[] textField;
		private JOptionPane optionPane;
		private String btnString1 = "Enter";
		private String btnString2 = "Cancel";
		private JComboBox CurrentMeasurments;
		private ExpDesign_Description MeasurementToEdit;
		
		
		/** Creates the reusable dialog. */
		public NewMeasurmentInputDialog(ExpDesign_Description measurementToEdit)
		{
			MeasurementToEdit = measurementToEdit;
			int width = 330;
			int height = 170;
			setTitle("Add Measurement:");
			setSize(width,height);
			setModal(true);
			
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
			
			//adding currently used treatments
			int numT =	thePlate.getAllMeasurements().size();
			Object[] meas = new Object[numT];
			for (int i =0; i < numT; i++)
				meas[i] = thePlate.getAllMeasurements().get(i);
			CurrentMeasurments = new JComboBox(meas);
			CurrentMeasurments.setEditable(false);
			
			
			textField = new JTextField[1];
			for (int i=0; i < textField.length; i ++)
				textField[i] = new JTextField(10);
			
			CurrentMeasurments.setEnabled(false);
			CurrentMeasurments.setEnabled(false);
			textField[0].setEditable(true);
			textField[0].setEnabled(true);
			textField[0].setText(measurementToEdit.getName());
			
			
			Object[] array = {"Name:", textField[0]};
			
			//Create an array specifying the number of dialog buttons
			//and their text.
			Object[] options = {btnString1, btnString2};
			
			//Create the JOptionPane.
			optionPane = new JOptionPane(array,
										 JOptionPane.QUESTION_MESSAGE,
										 JOptionPane.YES_NO_OPTION,
										 null,
										 options,
										 options[0]);
			
			//Make this dialog display it.
			setContentPane(optionPane);
			
			//Handle window closing correctly.
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
							  {
						public void windowClosing(WindowEvent we)
						{
							
							optionPane.setValue(new Integer(
													JOptionPane.CLOSED_OPTION));
						}
					});
			
			
			
			//Register an event handler that reacts to option pane state changes.
			optionPane.addPropertyChangeListener(this);
			setVisible(true);
		}
		
		/** Creates the reusable dialog. */
		public NewMeasurmentInputDialog()
		{
			MeasurementToEdit = null;
			int width = 330;
			int height = 170;
			setTitle("Add Measurement:");
			setSize(width,height);
			setModal(true);
			
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
			
			//adding currently used treatments
			int numT =	thePlate.getAllMeasurements().size();
			Object[] meas = new Object[numT];
			for (int i =0; i < numT; i++)
				meas[i] = thePlate.getAllMeasurements().get(i);
			CurrentMeasurments = new JComboBox(meas);
			CurrentMeasurments.setEditable(false);
			
			
			textField = new JTextField[1];
			for (int i=0; i < textField.length; i ++)
				textField[i] = new JTextField(10);
			
			CurrentMeasurments.setEnabled(false);
			CurrentMeasurments.setEnabled(false);
			textField[0].setEditable(true);
			textField[0].setEnabled(true);
			
			
			Object[] array = {"Name:", textField[0]};
			
			//Create an array specifying the number of dialog buttons
			//and their text.
			Object[] options = {btnString1, btnString2};
			
			//Create the JOptionPane.
			optionPane = new JOptionPane(array,
										 JOptionPane.QUESTION_MESSAGE,
										 JOptionPane.YES_NO_OPTION,
										 null,
										 options,
										 options[0]);
			
			//Make this dialog display it.
			setContentPane(optionPane);
			
			//Handle window closing correctly.
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
							  {
						public void windowClosing(WindowEvent we)
						{
							
							optionPane.setValue(new Integer(
													JOptionPane.CLOSED_OPTION));
						}
					});
			
			
			
			//Register an event handler that reacts to option pane state changes.
			optionPane.addPropertyChangeListener(this);
			setVisible(true);
		}
		
		/** This method handles events for the text field. */
		public void actionPerformed(ActionEvent e)
		{
			optionPane.setValue(btnString1);
		}
		
		
		
		/** This method reacts to state changes in the option pane. */
		public void propertyChange(PropertyChangeEvent e)
		{
			String prop = e.getPropertyName();
			
			if (isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
			{
				Object value = optionPane.getValue();
				
				if (value == JOptionPane.UNINITIALIZED_VALUE)
				{
					//ignore reset
					return;
				}
				
				
				optionPane.setValue(
					JOptionPane.UNINITIALIZED_VALUE);
				
				if (btnString1.equals(value))
				{
					ExpDesign_Description theMeasurement = null;
					String name = textField[0].getText();
					theMeasurement = new ExpDesign_Description("Measurement",
							name, null,
 null, null, null, null);

					
					measurementComboBox.addItem(theMeasurement);
					measurementComboBox.repaint();
					
					//measurements
					for (int i =0; i < theWells.length; i++)
						TheExpDesign_Model.addDescription(theWells[i].getID(),
								theMeasurement.getCopy());

					
					//If we were editing an old treatment, delete old one now
					//then delete the old one
					if(MeasurementToEdit!=null)
					{
						measurementComboBox.removeItemAt(measurementComboBox.getSelectedIndex());
						int len = theWells.length;
						for (int i = 0; i < len; i++)
							TheExpDesign_Model.removeDescription(
									theWells[i].getID(), MeasurementToEdit);
					}
					TheExpDesign_Model.write();
					clearAndHide();
					
				}
				else
				{
					clearAndHide();
				}
				thePlate.getGUI().updateMetaDataLegend();
			}
		}
		
		/** This method clears the dialog and hides it. */
		public void clearAndHide()
		{
			setVisible(false);
		}
	}
	
	private class BufferPanel extends JPanel
	{
		private String type;
		public BufferPanel(String location)
		{
			type = location;
			setPreferredSize(new Dimension(50,50));
		}
		
	}
	
}




