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

import imPanels.ImageCapturePanel;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import plots.Bound;

public class CaptureImage_Dialog extends JDialog implements ActionListener,PropertyChangeListener
{
	private JCheckBox[] checkBoxes;
	private JOptionPane optionPane;
	private String btnString1 = "Capture Image";
	private String btnString2 = "Cancel";
	private Bound TheBounds;
	public static final double NOVALUE = Double.POSITIVE_INFINITY;
	private NumberFormat nf = new DecimalFormat("0.##");
	private ImageCapturePanel ThePanel;
	
	public CaptureImage_Dialog(ImageCapturePanel panel)
	{
		ThePanel = panel;
		int width = 360;
		int height = 200;
		setTitle("Capture Image");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		setModal(false);
		
		checkBoxes = new JCheckBox[3];
		checkBoxes[0] = new JCheckBox("JPG");
		checkBoxes[1] = new JCheckBox("PNG");
		checkBoxes[2] = new JCheckBox("SVG");
		
		
		//Create an array of the text and components to be displayed.
		String[] mess = new String[1];
		mess[0] = "Select Image Format: ";
		
		Object[] array = {mess[0], checkBoxes[0], checkBoxes[1], checkBoxes[2]};
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
						checkBoxes[0].requestFocusInWindow();
					}
				});
		
		//Register an event handler that puts the text into the option pane.
//		checkBoxes[0].addActionListener(this);
		
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
				setVisible(false);

				JFileChooser fc = null;
				if (models.Model_Main.getModel().getTheDirectory()!=null)
					fc = new JFileChooser(models.Model_Main.getModel().getTheDirectory());
				else
					fc = new JFileChooser();
				
				File outDir = null;
				
				fc.setDialogTitle("Save as...");
				int returnVal = fc.showSaveDialog(models.Model_Main.getModel()
						.getGUI());
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					outDir = fc.getSelectedFile();
				}
				else
					System.out.println("Open command cancelled by user." );
				
				if (outDir!=null)
				{
					models.Model_Main.getModel().setTheDirectory(new File(outDir.getParent()));
					//
					//		JPEG image format
					//
					if (checkBoxes[0].isSelected())
					{
						File file  = (new File(outDir.getAbsolutePath()+".jpg"));
						ThePanel.captureImage(file, "jpg");
					}
					//
					//		PNG image format
					//
					if (checkBoxes[1].isSelected())
					{
						File file = (new File(outDir.getAbsolutePath()+".png"));
						ThePanel.captureImage(file, "png");
					}
					//
					//		SVG image format
					//
					if (checkBoxes[2].isSelected())
					{
						PrintWriter pw = null;
						
						File file  = (new File(outDir.getAbsolutePath()+".svg"));
						try
						{
							pw = new PrintWriter(file);
						}
						catch (FileNotFoundException e3) {}
						if (pw!=null)
							ThePanel.captureSVG(pw);
					}
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


