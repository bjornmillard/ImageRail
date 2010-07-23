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

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.vecmath.Point3d;


public class Dialog_NewAxisBoundsInput extends JDialog
	implements ActionListener,
	PropertyChangeListener
{
    private String typedText = null;
    private JTextField[] textField;
    private JOptionPane optionPane;
	private final JCheckBox TheAutoBoundBox;
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
	private SurfacePlotMainPanel TheMasterFrame;
	
    /**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
    public String getValidatedText()
	{
		return typedText;
    }
	
    /** Creates the reusable dialog. */
    public Dialog_NewAxisBoundsInput(SurfacePlotMainPanel frame)
	{
		
		TheMasterFrame = frame;
		int width = 330;
		int height = 330;
		setTitle("Input");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		
		textField = new JTextField[6];
		for (int i=0; i < 6; i ++)
			textField[i] = new JTextField(10);
		
		
		textField[0].setText(frame.ThePlotPanel.Data.getAxisMin_X()+"");
		textField[1].setText(frame.ThePlotPanel.Data.getAxisMax_X()+"");
		
		textField[2].setText(frame.ThePlotPanel.Data.getAxisMin_Y()+"");
		textField[3].setText(frame.ThePlotPanel.Data.getAxisMax_Y()+"");
		
		textField[4].setText(frame.ThePlotPanel.Data.getAxisMin_Z()+"");
		textField[5].setText(frame.ThePlotPanel.Data.getAxisMax_Z()+"");
		
		
		TheAutoBoundBox = new JCheckBox("Auto-bound");
		TheAutoBoundBox.addActionListener(new ActionListener()
										  {
					public void actionPerformed(ActionEvent ae)
					{
						if (TheAutoBoundBox.isSelected())
							for (int i = 0 ; i < 6; i++)
								textField[i].setEnabled(false);
						else
							for (int i = 0 ; i < 6; i++)
								textField[i].setEnabled(true);
					}
				});
		
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[4];
		mess[0] = "Enter New Axis Bounds";
		mess[1] = "X-Axis (min/max)";
		mess[2] = "Y-Axis (min/max)";
		mess[3] = "Z-Axis (min/max)";
		Object[] array = {mess[0], mess[1], textField[0],textField[1], mess[2], textField[2],textField[3], mess[3], textField[4],textField[5], TheAutoBoundBox};
		
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
				//if we want to autobound... then ignore the inputted text
				if (TheAutoBoundBox.isSelected())
				{
					Point3d[] points = TheMasterFrame.TheOriginalData.getPoints();
					double[] colors = TheMasterFrame.TheOriginalData.getColors();
					
					String[] labels = TheMasterFrame.ThePlotPanel.TheAxisLabelText;
					double[][] axisRanges = new double[3][2];
					axisRanges[0][0] = TheMasterFrame.TheOriginalData.getAxisMin_X();
					axisRanges[0][1] = TheMasterFrame.TheOriginalData.getAxisMax_X();
					axisRanges[1][0] = TheMasterFrame.TheOriginalData.getAxisMin_Y();
					axisRanges[1][1] = TheMasterFrame.TheOriginalData.getAxisMax_Y();
					axisRanges[2][0] = TheMasterFrame.TheOriginalData.getAxisMin_Z();
					axisRanges[2][1] = TheMasterFrame.TheOriginalData.getAxisMax_Z();
					int[] axisScales = TheMasterFrame.AxisScaleTypes;
					TheMasterFrame.loadDataSet(points, colors, labels, axisRanges, axisScales);
				}
				else
				{
					//getting the text
					String[] strings = new String[6];
					for (int i=0; i < 6; i ++)
						strings[i] = textField[i].getText();
					
					//make sure the inputed values are numbers only
					if (Tools.areNumbers(strings))
					{
						double[] bounds = new double[6];
						for (int i=0; i < 6; i ++)
							bounds[i] = Double.parseDouble(strings[i]);
						//make sure the min vals are lower than the max vals
						if (Tools.isBigger(bounds[1],bounds[0]) && Tools.isBigger(bounds[3],bounds[2]) && Tools.isBigger(bounds[5],bounds[4]))
						{
							//creating a new surfacePlotPanel with the new valid bounds
//							public void loadDataSet(Point3d[] points, String[] axisLabels, double[][] axisRanges ,int[] axisScaleTypes)
							Point3d[] points = TheMasterFrame.TheOriginalData.getPoints();
							double[] colors = TheMasterFrame.TheOriginalData.getColors();
							String[] labels = TheMasterFrame.ThePlotPanel.TheAxisLabelText;
							double[][] axisRanges = new double[3][2];
							axisRanges[0][0] = bounds[0];
							axisRanges[0][1] = bounds[1];
							axisRanges[1][0] = bounds[2];
							axisRanges[1][1] = bounds[3];
							axisRanges[2][0] = bounds[4];
							axisRanges[2][1] = bounds[5];
							int[] axisScales = TheMasterFrame.AxisScaleTypes;
							
							TheMasterFrame.loadDataSet(points , colors, labels, axisRanges, axisScales);
						}
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

