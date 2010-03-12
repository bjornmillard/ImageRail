/**
 * FeatureFilter.java
 *
 * @author Bjorn Millard
 */

package filters;

import features.Feature;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import main.MainGUI;
import tempObjects.Cell_RAM;

public class FeatureFilter
{
	public String name;
	public JTextField[] InputFields;
	private Feature TheFeature;
	private int lowerBound;
	private int upperBound;
	
	public FeatureFilter(Feature feature_)
	{
		TheFeature = feature_;
		
		name = TheFeature.toString();
		lowerBound = 0;
		upperBound = MainGUI.MAXPIXELVALUE;
		
		InputFields = new JTextField[2];
		InputFields[0] = new JTextField(5);
		InputFields[0].setText(""+lowerBound);
		
		InputFields[1] = new JTextField(5);
		InputFields[1].setText(""+upperBound);
		
		
		//When text field changes, it automatically modifies the bounds
		InputFields[0].getDocument().addDocumentListener(new DocumentListener()
														 {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[0].getText();
						if(tools.MathOps.isNumber(string))
						{
							lowerBound = Integer.parseInt(string);
						}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[0].getText();
						if(tools.MathOps.isNumber(string))
						{
							lowerBound = Integer.parseInt(string);
						}
						
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[0].getText();
						if(tools.MathOps.isNumber(string))
						{
							lowerBound = Integer.parseInt(string);
						}
						
					}
				});
		
		InputFields[1] = new JTextField(5);
		InputFields[1].setText(""+upperBound);
		//When text field changes, it automatically modifies the bounds
		InputFields[1].getDocument().addDocumentListener(new DocumentListener()
														 {
					public void changedUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[1].getText();
						if(tools.MathOps.isNumber(string))
						{
							upperBound = Integer.parseInt(string);
						}
					}
					public void removeUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[1].getText();
						if(tools.MathOps.isNumber(string))
						{
							upperBound = Integer.parseInt(string);
						}
						
					}
					public void insertUpdate(DocumentEvent e)
					{
						//getting the text
						String string = InputFields[1].getText();
						if(tools.MathOps.isNumber(string))
						{
							upperBound = Integer.parseInt(string);
						}
						
					}
				});
	}
	
	public boolean pass(Cell_RAM cell)
	{
		if (TheFeature==null)
			return true;
		
//		double val = TheFeature.getValue(cell);
//		if (val<lowerBound || TheFeature.getValue(cell)>upperBound)
//			return false;
		
		return true;
	}
	
	public String toString()
	{
		return name;
	}
	
	public JPanel getParameterPanel()
	{
		JPanel pan = new JPanel();
		pan.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		pan.setLayout(new GridLayout(5,1));
		pan.add(new JLabel(name));
		pan.add(new JLabel("Lower Bound"));
		pan.add(InputFields[0]);
		
		pan.add(new JLabel("Upper Bound"));
		pan.add(InputFields[1]);
		
		
		return pan;
	}
	
	public int[] getBounds()
	{
		if (InputFields==null)
			return null;
		
		int len = InputFields.length;
		int[] arr = new int[len];
		for (int i = 0; i < len; i++)
		{
			String string = InputFields[i].getText();
			if(tools.MathOps.isNumber(string))
				arr[i] = Integer.parseInt(string);
			else
				return null;
		}
		
		return arr;
	}
	
}

