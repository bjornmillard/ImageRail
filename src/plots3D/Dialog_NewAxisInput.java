/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

package plots3D;

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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.vecmath.Vector3d;


public class Dialog_NewAxisInput extends JDialog
	implements ActionListener,
	PropertyChangeListener
{
    private String typedText = null;
    private JTextField[] textField;
    private JOptionPane optionPane;
	
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
	private SurfacePlotMainPanel TheMasterFrame;
	
	
    /** Creates the reusable dialog. */
    public Dialog_NewAxisInput(SurfacePlotMainPanel frame)
	{
		
		TheMasterFrame = frame;
		int width = 330;
		int height = 220;
		setTitle("Input");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		
		textField = new JTextField[3];
		textField[0] = new JTextField(10);
		textField[1] = new JTextField(10);
		textField[2] = new JTextField(10);
		
		textField[0].setText(frame.TheControlPanel.nf.format(frame.TheControlPanel.angles.x));
		textField[1].setText(frame.TheControlPanel.nf.format(frame.TheControlPanel.angles.y));
		textField[2].setText(frame.TheControlPanel.nf.format(frame.TheControlPanel.angles.z));
		
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[4];
		mess[0] = "Enter New Axis of Rotation";
		mess[1] = "x";
		mess[2] = "y";
		mess[3] = "z";
		Object[] array = {mess[0], mess[1], textField[0], mess[2], textField[1], mess[3], textField[2]};
		
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
						/*
						 * Instead of directly closing the window,
						 * we're going to change the JOptionPane's
						 * value property.
						 */
						optionPane.setValue(new Integer(
												JOptionPane.CLOSED_OPTION));
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
				String[] strings = new String[3];
				strings[0] = textField[0].getText();
				strings[1] = textField[1].getText();
				strings[2] = textField[2].getText();
				
				//make sure the inputed values are numbers only
				if (Tools.areNumbers(strings))
				{
					Vector3d vect = new Vector3d();
					vect.x = Double.parseDouble(strings[0]);
					vect.y = Double.parseDouble(strings[1]);
					vect.z = Double.parseDouble(strings[2]);
					vect.normalize();
					//make sure it has a unit magnitude and NaN value
					if(vect.lengthSquared()>0.5f)
					{
						TheMasterFrame.TheControlPanel.angles.x =vect.x;
						TheMasterFrame.TheControlPanel.angles.y =vect.y;
						TheMasterFrame.TheControlPanel.angles.z =vect.z;
						
						TheMasterFrame.setView(Tools.matrixFromAxisAngle(TheMasterFrame.TheControlPanel.angles), TheMasterFrame.TheCurrentTranslation);
						TheMasterFrame.TheControlPanel.updateAngles();
						
					}
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
//		textField.setText(null);
		setVisible(false);
    }
}

