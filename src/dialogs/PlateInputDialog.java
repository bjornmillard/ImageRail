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

package dialogs;

import gui.MainGUI;

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
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import models.Model_Main;

public class PlateInputDialog extends JDialog implements ActionListener,PropertyChangeListener
{
	private JOptionPane optionPane;
	int numPlates = 1;
	int numRows = 0;
	int numCols = 0;
	final JTextField numPlatesTextField = new JTextField(4);
	private String btnString1 = "Create";
	private String btnString2 = "Cancel";
	private NumberFormat nf = new DecimalFormat("0.#");
	private JFrame ParentFrame;
	
	public PlateInputDialog(JFrame parentFrame)
	{
		ParentFrame = parentFrame;
		int width = 330;
		int height = 300;
		setTitle("Input Model_Plate Information");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		
		
		numPlatesTextField.setText(""+numPlates);
		//Getting date information
		numPlatesTextField.setBorder(BorderFactory.createTitledBorder("Number of Plates"));
		
		//Setting up the RaidioButtons for pixel saving selections
		JRadioButton r0 = new JRadioButton("384 wells");
		r0.setSelected(true);
		r0.addActionListener(new ActionListener()
							 {
					public void actionPerformed(ActionEvent ae)
					{
						numRows = 16;
						numCols = 24;
					}
				});
		JRadioButton r1 = new JRadioButton("96 wells");
		r1.addActionListener(new ActionListener()
							 {
					public void actionPerformed(ActionEvent ae)
					{
						numRows = 8;
						numCols = 12;
					}
				});
		JRadioButton r2 = new JRadioButton("24 wells");
		r2.addActionListener(new ActionListener()
							 {
					public void actionPerformed(ActionEvent ae)
					{
						numRows = 4;
						numCols = 6;
					}
				});
		JRadioButton r3 = new JRadioButton("12 wells");
		r3.addActionListener(new ActionListener()
							 {
					public void actionPerformed(ActionEvent ae)
					{
						numRows = 3;
						numCols = 4;
					}
				});
		JRadioButton r4 = new JRadioButton("6 wells");
		r4.addActionListener(new ActionListener()
							 {
					public void actionPerformed(ActionEvent ae)
					{
						numRows = 2;
						numCols = 3;
					}
				});
		JRadioButton r5 = new JRadioButton("Movie");
		r5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				numRows = -1;
				numCols = -1;
			}
		});
		//Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(r0);
		group.add(r1);
		group.add(r2);
		group.add(r3);
		group.add(r4);
		group.add(r5);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(7, 1));
		radioPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		radioPanel.add(new JLabel("Wells/Model_Plate:"));
		radioPanel.add(r0);
		radioPanel.add(r1);
		radioPanel.add(r2);
		radioPanel.add(r3);
		radioPanel.add(r4);
		radioPanel.add(r5);

		r1.setSelected(true);
		numRows = 8;
		numCols = 12;
		
		
		
		
		Object[] array = {numPlatesTextField, radioPanel};
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
						numPlatesTextField.requestFocusInWindow();
					}
				});
		
		//Register an event handler that puts the text into the option pane.
		numPlatesTextField.addActionListener(this);
		
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
				String[] strings = new String[1];
				strings[0] = numPlatesTextField.getText();
				
				//make sure the inputed values are numbers only
				if (tools.MathOps.areNumbers(strings))
				{
						
					int numP = Integer.parseInt(strings[0]);
					if (numRows != -1 && numCols != -1) {
						Model_Main TheMainModel = models.Model_Main.getModel();
						if (TheMainModel == null)
							TheMainModel = new Model_Main();

						boolean worked = TheMainModel.initNewPlates(
								numP, numRows, numCols);
						
						if (worked) {
							if (ParentFrame != null)
								ParentFrame.setVisible(false);
							
							MainGUI TheMainGUI = new MainGUI(TheMainModel);
							if(TheMainGUI!=null)
								TheMainGUI.setTitle("Project: "+TheMainModel.getInputProjectPath());	
							TheMainGUI
									.setVisible(true);
						}
					}
				
				}
				else
				{
					JOptionPane.showMessageDialog(null,"Please Enter a Valid Number of Plates ","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				clearAndHide();
				
			}
			else { //user closed dialog or clicked cancel
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

