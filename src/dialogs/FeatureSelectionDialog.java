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
 * FeatureSelectionDialog.java
 *
 * @author Created by Omnicore CodeGuide
 */

package dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import features.Feature;

public class FeatureSelectionDialog extends JDialog
{
	static final JDialog TheDialog = new JDialog(models.Model_Main.getModel()
			.getGUI(),
												 "Select Features to Use",
												 true);
	static public boolean readyToReturn;
	static public Feature[] selectedFeatures;
	
	static public Feature[] showFeatureSelectionDialog()
	{
		final FeatureSelectionPanel panel = new FeatureSelectionPanel();
		readyToReturn =false;
		
		TheDialog.setContentPane(panel);
		TheDialog.setDefaultCloseOperation(
			JDialog.DO_NOTHING_ON_CLOSE);
		TheDialog.addWindowListener(new WindowAdapter()
									{
					public void windowClosing(WindowEvent we)
					{
						
					}
				});
		
		panel.addPropertyChangeListener(
			new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent e)
				{
					String prop = e.getPropertyName();
					
					if (TheDialog.isVisible()
						&& (e.getSource() == panel)
						&& (JOptionPane.VALUE_PROPERTY.equals(prop)))
					{
//						selectedFeatures = panel.getSelectedFeatures();;
						TheDialog.setVisible(false);
					}
				}
			});
		
		
		
		TheDialog.pack();
		TheDialog.setLocationRelativeTo(models.Model_Main.getModel().getGUI());
		TheDialog.setVisible(true);
		
		//Im an idiot and cant figure out how to use dialogs properly.
		//This sleep loop is my hack to wait till the response is ready from my custom dialog -BLM 7/09
		while(!readyToReturn)
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) {}
		}
		return selectedFeatures;
	}
	
	
	static public class FeatureSelectionPanel extends JPanel
	{
		
		static public CheckListItem[] checkBoxes;
		private String btnString1 = "Save";
		private String btnString2 = "Cancel";
		private ArrayList TheFeatures;
		private JPanel TheMainPanel;
		public Feature[] TheSelectedFeatures;
		
		
		public FeatureSelectionPanel()
		{
			int width = 350;
			int height = 400;
			
			setSize(width,height);
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.width/2f)-width/2,(int)(d.height/2f)-height/2);
			
			//Creating the CheckBoxes
			TheFeatures = models.Model_Main.getModel().getTheFeatures();
			int numF = TheFeatures.size();
			checkBoxes = new CheckListItem[numF];
			Object[] arr = new Object[numF];
			for (int i = 0; i < numF; i++)
			{
				Feature f = (Feature)TheFeatures.get(i);
				checkBoxes[i] = new CheckListItem(f);
				arr[i] = checkBoxes[i];
			}
			JPanel p1 = new JPanel();
			p1.setPreferredSize(new Dimension(20,100));
			JPanel p2 = new JPanel();
			p2.setPreferredSize(new Dimension(20,100));
			
			
			TheMainPanel = new JPanel();
			TheMainPanel.setLayout(new BorderLayout());
			CheckList ch = new CheckList();
			ch.init(checkBoxes);
			JScrollPane s = new JScrollPane(ch.getList());
			TheMainPanel.add(s, BorderLayout.CENTER);
			TheMainPanel.add(new JLabel("Features to Save:"), BorderLayout.NORTH);
			TheMainPanel.add(p1, BorderLayout.WEST);
			TheMainPanel.add(p2, BorderLayout.EAST);
			TheMainPanel.add(new BottomPanel(), BorderLayout.SOUTH);
			add(TheMainPanel);
			setVisible(true);
		}
		
		
		
		//
		// 	Setting up the Check list
		//
		//
		public class CheckList extends JPanel
		{
			private JList list;
			public void init(CheckListItem[] items)
			{
				// Create a list containing CheckListItem's
				list = new JList(items);
				// to renderer list cells
				list.setCellRenderer(new CheckListRenderer());
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				// Add a mouse listener to handle changing selection
				list.addMouseListener(new MouseAdapter()
									  {
							public void mouseClicked(MouseEvent event)
							{
								JList list = (JList) event.getSource();
								// Get index of item clicked
								int index = list.locationToIndex(event.getPoint());
								CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
								// Toggle selected state
								item.setSelected(!item.isSelected());
								// Repaint cell
								list.repaint(list.getCellBounds(index, index));
							}
						});
				
//			JScrollPane p = new JScrollPane(list);
//			p.setPreferredSize(new Dimension(150, 100));
//			p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			p.add(list);
//
//			add(p, BorderLayout.CENTER);
				
//			JFrame f = new JFrame();
//			f.getContentPane().add(new JScrollPane(list));
//			f.show();
			}
			public JList getList()
			{
				return list;
			}
		}
		
		
		
		
		// Represents items in the list that can be selected
		public class CheckListItem
		{
			private Feature TheFeature;
			private boolean isSelected = false;
			
			public CheckListItem(Feature feature)
			{
				TheFeature = feature;
			}
			public boolean isSelected()
			{
				return isSelected;
			}
			public void setSelected(boolean isSelected_)
			{
				isSelected = isSelected_;
			}
			public String toString()
			{
				return TheFeature.Name;
			}
			public Feature getFeature()
			{
				return TheFeature;
			}
		}
		
		
		
		// Handles rendering cells in the list using a check box
		class CheckListRenderer extends JCheckBox implements ListCellRenderer
		{
			public Component getListCellRendererComponent(JList list, Object value,
														  int index, boolean isSelected, boolean hasFocus)
			{
				setEnabled(list.isEnabled());
				setSelected(((CheckListItem)value).isSelected());
				setFont(list.getFont());
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				setText(value.toString());
				return this;
			}
			
		}
		
		
		private class BottomPanel extends JPanel
		{
			public BottomPanel()
			{
				setLayout(new GridLayout(0,2));
				
				final JCheckBox check = new JCheckBox("Toggle all on/off");
				check.addActionListener(new ActionListener()
										{
							public void actionPerformed(ActionEvent ae)
							{
								int len = checkBoxes.length;
								if (check.isSelected())
									for (int i = 0; i < len; i++)
										checkBoxes[i].setSelected(true);
								else
									for (int i = 0; i < len; i++)
										checkBoxes[i].setSelected(false);
								
								TheMainPanel.validate();
								TheMainPanel.repaint();
							}
						});
				add(check, 0);
				
				JPanel p = new JPanel(new GridLayout(0,2));
				add(p, 1);
				
				JButton but = new JButton("Cancel");
				but.addActionListener(new ActionListener()
									  {
							public void actionPerformed(ActionEvent ae)
							{
								readyToReturn = true;
								TheDialog.setVisible(false);
								TheDialog.dispose();
							}
						});
				p.add(but, 0);
				
				but = new JButton("Save");
				but.addActionListener(new ActionListener()
									  {
							public void actionPerformed(ActionEvent ae)
							{
								selectedFeatures = getSelectedFeatures();
								readyToReturn = true;
								TheDialog.setVisible(false);
								TheDialog.dispose();
							}
						});
				p.add(but, 1);
			}
			
			
		}
		
		
		static public Feature[] getSelectedFeatures()
		{
			ArrayList arr = new ArrayList();
			//Finding the selected Features
			int len = checkBoxes.length;
			for (int i = 0; i < len; i++)
			{
				if (checkBoxes[i].isSelected)
					arr.add(checkBoxes[i].getFeature());
			}
			
			//Converting to array
			len = arr.size();
			if (len==0)
				return null;
			
			Feature[] f = new Feature[len];
			for (int i = 0; i < len; i++)
				f[i] = (Feature)arr.get(i);
			
			return f;
		}
		
	}
	
	
	
}

