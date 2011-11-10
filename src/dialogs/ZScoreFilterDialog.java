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
 * ZScoreFilterDialog.java
 *
 * @author Bjorn Millard
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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;


public class ZScoreFilterDialog extends JDialog
		implements ActionListener, PropertyChangeListener {
	private String typedText = null;
	private JTextField[] textField;
	private JOptionPane optionPane;
	private String btnString1 = "Run";
	private String btnString2 = "Cancel";
	private JComboBox channelBox;
	public float zScore_threshold;
	public String zScore_channel;
	public String Operater = ">";
	public final int GREATERTHAN = 0;
	public final int LESSTHAN = 1;
	public final int BOTH = 2;


	public ZScoreFilterDialog() {
		int width = 270;
		int height = 300;
		setTitle("Z-Score Filter");
		setSize(width, height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
				- height / 2);
		setModal(true);
		zScore_channel = "All";
		zScore_threshold = 1;

		/** Features comboBox */
		ArrayList<String> list = new ArrayList<String>();
		String[] channelNames = models.Model_Main.getModel().getTheChannelNames();
		int len = channelNames.length;
		for (int i = 0; i < len; i++)
			list.add(channelNames[i]);
		list.add("All");
		
		Object[] obX = new Object[list.size()];
		if (list.size() > 0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		channelBox = new JComboBox(obX);

		textField = new JTextField[1];
		textField[0] = new JTextField(6); // Z-score cutoff
		textField[0].setText("2");

		// Setting up the RaidioButtons for pixel saving selections
		JRadioButton r0 = new JRadioButton(">");
		r0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Operater = ">";
			}
		});
		JRadioButton r1 = new JRadioButton("<");
		r1.setSelected(true);
		r1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Operater = "<";
			}
		});
		JRadioButton r2 = new JRadioButton("< and >");
		r2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Operater = "<>";
			}
		});
		

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		r0.setSelected(true);
		group.add(r0);
		group.add(r1);
		group.add(r2);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(3, 0));
		radioPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		radioPanel.add(r0);
		radioPanel.add(r1);
		radioPanel.add(r2);


		// Create an array of the text and components to be displayed.
		String[] mess = new String[3];
		mess[0] = "Channels to Search:";
		mess[1] = "Z-Score Threshold:";
		mess[2] = "Search Criteria:";


		Object[] array = {mess[0], channelBox, mess[1], textField[0],
 mess[2],
				radioPanel };


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

				strings = new String[1];
				strings[0] = textField[0].getText().trim(); // zScore thresh

				// make sure the inputed values are numbers only
				if (tools.MathOps.areNumbers(strings)) {

					zScore_channel = (String) channelBox.getSelectedItem();
					zScore_threshold = Float.parseFloat(strings[0].trim());

					clearAndHide();

				} else {
					JOptionPane
							.showMessageDialog(
									null,
									"Error with inputed bounds!  Make sure numbers make sense ",
									"Thresholds", JOptionPane.ERROR_MESSAGE);
				}

			} else {
				// user closed dialog or clicked cancel
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
