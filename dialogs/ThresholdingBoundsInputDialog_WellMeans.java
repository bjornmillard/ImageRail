/**
 * ThresholdingBoundsInputDialog_WellMeans.java
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
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import models.Model_Well;
import models.Model_ParameterSet;
import processors.Processor_WellAverage;
import processors.Processor_WellAverage_Compartmented;

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
		
		Model_ParameterSet pset = Model_ParameterSet.doWellsHaveSameParameterSet(wells);
		if (pset!=null)
			if (!pset.getProcessType().equalsIgnoreCase(Model_ParameterSet.WELLMEAN))
				pset = null;
		
		
		MainGUI.getGUI().setStoreCytoAndNuclearWellMeans(new JCheckBoxMenuItem("Nucleus Threshold"));
		MainGUI.getGUI().getStoreCytoAndNuclearWellMeans().setSelected(false);
		MainGUI.getGUI().getStoreCytoAndNuclearWellMeans().addActionListener(new ActionListener()
																			 {
					public void actionPerformed(ActionEvent ae)
					{
						if (MainGUI.getGUI().getStoreCytoAndNuclearWellMeans().isSelected())
						{
							textField[1].setText("0");
							textField[1].setEnabled(true);
						}
						else
						{
							textField[1].setText("N/A");
							textField[1].setEnabled(false);
						}
						
						validate();
						repaint();
					}
				});
		
		
		/** Features comboBox*/
		ArrayList list = new ArrayList();
		int len = MainGUI.getGUI().getTheChannelNames().length;
		
		for (int i = 0; i < len; i++)
			list.add(MainGUI.getGUI().getTheChannelNames()[i]);
		
		Object[] obX = new Object[list.size()];
		if (list.size()>0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		channelBox = new JComboBox(obX);
		
		
		
		TheWells =wells;
		textField = new JTextField[3];
		textField[0] = new JTextField(10);
		textField[1] = new JTextField(10);
		textField[1].setEnabled(false);
		textField[1].setText("N/A");
		textField[2] = new JTextField(10);
		textField[2].setText("0");
		
		
		//
		if(pset!=null)
		{
			int num = channelBox.getItemCount();
			for (int i = 0; i < num; i++)
			{
				String name = (String)channelBox.getItemAt(i);
				if (name.equalsIgnoreCase(pset.getThresholdChannel_cyto_Name()))
					channelBox.setSelectedIndex(i);
			}
			
			textField[0].setText("" + pset.getThreshold_Cell());
			if (pset.getThreshold_Nucleus() != Model_ParameterSet.NOVALUE)
			{
				textField[1].setEnabled(true);
				textField[1].setText("" + pset.getThreshold_Nucleus());
			}
			
			textField[2].setText("" + pset.getThreshold_Background());
		}
		//
		
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[3];
		
		mess[0] = "Thresholding Channel";
		mess[1] = "Cytoplasm/Background Threshold";
		mess[2] = "Background Threshold";
		
//		Object[] array = {mess[0], channelBox, mess[1], textField[0], MainGUI.getGUI().getStoreCytoAndNuclearWellMeans(), textField[1], mess[2], textField[2]};
		Object[] array = {mess[0], channelBox, mess[1], textField[0],  mess[2], textField[2]};
		
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
				if (MainGUI.getGUI().getStoreCytoAndNuclearWellMeans().isSelected())
				{
					strings = new String[3];
					strings[0] = textField[0].getText();
					strings[1] = textField[1].getText();
					strings[2] = textField[2].getText();
					//make sure the inputed values are numbers only
					if (tools.MathOps.areNumbers(strings))
					{
						
						int CellBoundaryChannel = channelBox.getSelectedIndex();
						float Threshold_CellBoundary = Float.parseFloat(strings[0]);
						float Threshold_Nucleus = Float.parseFloat(strings[1]);
						float Threshold_Background = Float.parseFloat(strings[2]);
						
						MainGUI.getGUI().getLoadCellsImmediatelyCheckBox().setSelected(false);
						Processor_WellAverage_Compartmented tasker = new Processor_WellAverage_Compartmented();
						
						tasker.setWellsToProcess(TheWells);
						
						tasker.start();
						
						//Storing the Parameters for each Model_Well
						int len = TheWells.length;
						for (int i = 0; i < len; i++)
						{
							Model_Well well = TheWells[i];
							Model_ParameterSet pset = well.TheParameterSet;
							pset.setModified(true);
							//Threshold Channel
							pset.setThresholdChannel_cyto_Name(MainGUI.getGUI()
									.getTheChannelNames()[CellBoundaryChannel]);
							//Nuc bound threshold
							pset.setThreshold_Nucleus(Threshold_Nucleus);
							//Cell bound Threshold
							pset.setThreshold_Cell(Threshold_CellBoundary);
							//Bkgd threshold
							pset.setThreshold_Background(Threshold_Background);
							
							
							if (MainGUI.getGUI().getWellMeanOrIntegratedIntensityCheckBox().isSelected())
								well.TheParameterSet
										.setMeanOrIntegrated(well.TheParameterSet.MEAN);
							else
								well.TheParameterSet
										.setMeanOrIntegrated(well.TheParameterSet.INTEGRATED);
						}
					}
				}
				else
				{
					strings = new String[2];
					strings[0] = textField[0].getText();
					strings[1] = textField[2].getText();
					//make sure the inputed values are numbers only
					if (tools.MathOps.areNumbers(strings))
					{
						int CellBoundaryChannel = channelBox.getSelectedIndex();
						float Threshold_CellBoundary = Float.parseFloat(strings[0]);
						float Threshold_Background = Float.parseFloat(strings[1]);
						float Threshold_Nucleus = -1;
						MainGUI.getGUI().getLoadCellsImmediatelyCheckBox().setSelected(false);
						
						//Storing the Parameters for each Model_Well
						int len = TheWells.length;
						for (int i = 0; i < len; i++)
						{
							Model_Well well = TheWells[i];
							Model_ParameterSet pset = well.TheParameterSet;
							pset.setModified(true);
							//ProcessType
							pset.setProcessType(Model_ParameterSet.WELLMEAN);
							//Threshold Channel
							pset.setThresholdChannel_cyto_Name(MainGUI.getGUI()
									.getTheChannelNames()[CellBoundaryChannel]);
							//Nuc bound threshold
							pset.setThreshold_Nucleus(Threshold_Nucleus);
							//Cell bound Threshold
							pset.setThreshold_Cell(Threshold_CellBoundary);
							//Bkgd threshold
							pset.setThreshold_Background(Threshold_Background);
							
							if (MainGUI.getGUI().getWellMeanOrIntegratedIntensityCheckBox().isSelected())
								well.TheParameterSet
										.setMeanOrIntegrated(well.TheParameterSet.MEAN);
							else
								well.TheParameterSet
										.setMeanOrIntegrated(well.TheParameterSet.INTEGRATED);
							
						}
						
						
						//Creating and Starting the processor
						Processor_WellAverage tasker = new Processor_WellAverage(TheWells);
						tasker.start();
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
		setVisible(false);
	}
	
}

