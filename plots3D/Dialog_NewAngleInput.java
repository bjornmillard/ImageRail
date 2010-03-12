package plots3D;

import java.awt.event.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class Dialog_NewAngleInput extends JDialog
	implements ActionListener,
	PropertyChangeListener
{
    private String typedText = null;
    private JTextField textField;
    private JOptionPane optionPane;
	
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
	private SurfacePlotMainPanel TheMasterPanel;
	
    /**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
    public String getValidatedText()
	{
		return typedText;
    }
	
    /** Creates the reusable dialog. */
    public Dialog_NewAngleInput(SurfacePlotMainPanel frame)
	{
		
		TheMasterPanel = frame;
		int width = 330;
		int height = 150;
		setTitle("Input");
		setSize(width,height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
		
		textField = new JTextField(10);
		textField.setText(frame.TheControlPanel.nf.format(frame.TheControlPanel.angles.angle));
		
		//Create an array of the text and components to be displayed.
		String msgString1  = "Enter New Angle (Radians):";
		Object[] array = {msgString1, textField};
		
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
						textField.requestFocusInWindow();
					}
				});
		
		//Register an event handler that puts the text into the option pane.
		textField.addActionListener(this);
		
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
				String[] strings = new String[1];
				strings[0] = textField.getText();
				//make sure the inputed values are numbers only
				if (Tools.areNumbers(strings))
				{
					double val = Double.parseDouble(strings[0]);
					TheMasterPanel.TheControlPanel.angles.angle = val;
					
					TheMasterPanel.setView(Tools.matrixFromAxisAngle(TheMasterPanel.TheControlPanel.angles), TheMasterPanel.TheCurrentTranslation);
					TheMasterPanel.TheControlPanel.updateAngles();
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
		textField.setText(null);
		setVisible(false);
	}
}

