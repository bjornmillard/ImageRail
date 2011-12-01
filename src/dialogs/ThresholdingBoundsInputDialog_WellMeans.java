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
 * ThresholdingBoundsInputDialog_WellMeans.java
 *
 * @author Bjorn Millard
 */

package dialogs;

import java.awt.Dimension;
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

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import models.Model_Field;
import models.Model_ParameterSet;
import models.Model_Well;
import processors.Processor_WellAverage;

public class ThresholdingBoundsInputDialog_WellMeans extends JDialog implements ActionListener,PropertyChangeListener
{
	private String typedText = null;
	private JTextField[] textField;
	private JOptionPane optionPane;
	private String btnString1 = "Run";
	private String btnString2 = "Cancel";
	private Model_Well[] TheWells;
	private JComboBox channelBox;
	
	public ThresholdingBoundsInputDialog_WellMeans(Model_Well[] wells)
	{
		int width = 370;
		int height = 250;
		setTitle("Input");
		setSize(width,height);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		
		setModal(true);
		
		Model_ParameterSet pset = Model_ParameterSet
				.doWellsHaveSameParameterSet(wells);
		
		/** Features comboBox*/
		ArrayList list = new ArrayList();
		int len = models.Model_Main.getModel().getTheChannelNames().length;
		
		for (int i = 0; i < len; i++)
			list.add(models.Model_Main.getModel().getTheChannelNames()[i]);
		
		Object[] obX = new Object[list.size()];
		if (list.size()>0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		channelBox = new JComboBox(obX);
		
		TheWells = wells;
		textField = new JTextField[1];
		textField[0] = new JTextField(10);
		
		
		 if (pset != null) {
			if (pset.exists("Thresh_Cyt_ChannelName")) {
				int num = channelBox.getItemCount();
				for (int i = 0; i < num; i++) {
					String name = (String) channelBox.getItemAt(i);
					if (name.equalsIgnoreCase(pset
							.getParameter_String("Thresh_Cyt_ChannelName")))
						channelBox.setSelectedIndex(i);
				}
			}

			if (pset.exists("Thresh_Cyt_Value"))
				textField[0].setText(""
					+ pset.getParameter_float("Thresh_Cyt_Value"));

		}
		
		
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[3];
		
		mess[0] = "Thresholding Channel";
		mess[1] = "Cell Threshold";
		// mess[2] = "Background Threshold";
		
		Object[] array = { mess[0], channelBox, mess[1], textField[0] };
		
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
						optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
					}
				});
		
		//Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter()
							 {
					public void componentShown(ComponentEvent ce)
					{
						textField[0].requestFocusInWindow();
					}
				});
		
		//Register an event handler that puts the text into the option pane.
		textField[0].addActionListener(this);
		
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
			
			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(
				JOptionPane.UNINITIALIZED_VALUE);
			
			if (btnString1.equals(value))
			{
				String[] strings  = null;
				strings = new String[1];
					strings[0] = textField[0].getText();
					//make sure the inputed values are numbers only
					if (tools.MathOps.areNumbers(strings))
					{
						int CellBoundaryChannel = channelBox.getSelectedIndex();
						float Thresh_CellBoundary = Float.parseFloat(strings[0]);
						
						//Storing the Parameters for each Model_Well
						int len = TheWells.length;
						for (int i = 0; i < len; i++)
						{
							Model_Well well = TheWells[i];
							Model_Field[] fields = well.getFields();
							for (int p = 0; p < fields.length; p++) {

								Model_ParameterSet pset = fields[p]
										.getParameterSet();

								// Threshold Channel
							pset.setParameter(
									"Thresh_Cyt_ChannelName",
									models.Model_Main.getModel()
											.getTheChannelNames()[CellBoundaryChannel]);
								pset.setParameter(
										"Thresh_Cyt_ChannelIndex", ""
												+ CellBoundaryChannel);
								// Cell bound Threshold
								pset.setParameter("Thresh_Cyt_Value",""+Thresh_CellBoundary);
							pset.setParameter("Algorithm", "WellAverage_v1");

							}
						}
						
						
						//Creating and Starting the processor
						Processor_WellAverage tasker = new Processor_WellAverage(TheWells);
						tasker.start();
					}
				
				
				

				typedText = null;
				clearAndHide();
				
			}
			else { //user closed dialog or clicked cancel
				typedText = null;
				clearAndHide();
			}
		}
	}
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide()
	{
		setVisible(false);
	}
	
}

