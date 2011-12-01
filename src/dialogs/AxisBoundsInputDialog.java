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
 * ThresholdingBoundsInputDialog_SingleCells.java
 *
 * @author Bjorn Millard
 */

package dialogs;

import gui.MainGUI;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import plots.Bound;

public class AxisBoundsInputDialog extends JDialog implements ActionListener,PropertyChangeListener
{
	private JTextField[] textField;
	private JOptionPane optionPane;
	private String btnString1 = "Set Bounds";
	private String btnString2 = "Cancel";
	private Bound TheBounds;
	public static final double NOVALUE = Double.POSITIVE_INFINITY;
	private NumberFormat nf = new DecimalFormat("0.#");
	private MainGUI TheMainGUI;
	
	public AxisBoundsInputDialog(Bound bounds, MainGUI theMainGUI)
	{
		TheMainGUI = theMainGUI;
		int width = 300;
		int height = 200;
		setTitle("Set Bounds");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		setModal(true);
		
		textField = new JTextField[2];
		textField[0] = new JTextField(6);  //Nuc bound
		textField[1] = new JTextField(6);  //Cell bound
		
		if (bounds.Lower != NOVALUE)
			textField[0].setText(nf.format(bounds.Lower)+""	);
		if (bounds.Upper != NOVALUE)
			textField[1].setText(nf.format(bounds.Upper)+""	);
		
		TheBounds = bounds;
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[2];
		mess[0] = "Lower Bound: ";
		mess[1] = "Upper Bound: ";
		
		Object[] array = {mess[0], textField[0], mess[1], textField[1]};
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
				String[] strings = new String[2];
				strings[0] = textField[0].getText();
				strings[1] = textField[1].getText();
				
				//make sure the inputed values are numbers only
				if (tools.MathOps.areNumbers(strings))
				{
					TheBounds.Lower = Double.parseDouble(textField[0].getText());
					TheBounds.Upper = Double.parseDouble(textField[1].getText());
					TheBounds.TheParentPanel.repaint();
					
					if (TheMainGUI.getDotPlot()!=null)
						if (TheMainGUI.getLeftDisplayPanelType()==TheMainGUI.DOTPLOT)
						{
							TheMainGUI.getDotPlot().UpdatePlotImage = true;
							TheMainGUI.getDotPlot().repaint();
						}
						else
							TheMainGUI.getLinePlot().repaint();
				}
				else
				{
					JOptionPane.showMessageDialog(null,"Error with inputed bounds!  Make sure numbers make sense ","Bounds",JOptionPane.ERROR_MESSAGE);
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


