/**
 * FilterManager.java
 *
 * @author Bjorn Millard
 */

package filters;

import java.awt.*;
import javax.swing.*;

import features.Feature;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import main.MainGUI;
import main.Plate;
import main.PlateHoldingPanel;
import main.Well;
import tempObjects.Cell_RAM;

public class FilterManager extends JFrame
{
	public MainGUI TheMainGUI;
	public JPanel TheMainPanel;
	public JPanel TheButtonPanel;
	public JList ThePossibleFilters;
	public JList TheFilters;
	private JScrollPane selectedFiltersScroller;
	private JScrollPane possibleFiltersScroller;
	
	public FilterManager()
	{
		super("Filter Manager");
		setResizable(true);
		int width = 700;
		int height = 200;
		setSize(width,height);
		setVisible(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(d.width/2f)-width/2,(int)(d.height/5f)-height/2);
		
		TheMainGUI = main.MainGUI.getGUI();
		
		
		TheMainPanel = new JPanel();
		getContentPane().add(TheMainPanel);
		setLayout(new GridLayout(1,4));
		addWindowListener(new WindowAdapter()
						  {
					public void windowClosing(WindowEvent we)
					{
						setVisible(false);
					}
				});
		
		//creating the button panel
		TheButtonPanel = new JPanel();
		TheButtonPanel.setLayout(new GridLayout(3,0));
		JButton but =  new JButton(">");
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						DefaultListModel mod = (DefaultListModel)ThePossibleFilters.getModel();
						if (mod.getSize()>0)
						{
							if (ThePossibleFilters.getSelectedIndex()<0)
								return;
							
							FeatureFilter f = (FeatureFilter)ThePossibleFilters.getSelectedValue();
							mod.remove(ThePossibleFilters.getSelectedIndex());
							ThePossibleFilters.repaint();
							
							mod = (DefaultListModel)TheFilters.getModel();
							mod.addElement(f);
							TheFilters.repaint();
						}
						
					}
				});
		TheButtonPanel.add(but,0);
		
		but =  new JButton("<");
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						DefaultListModel mod = (DefaultListModel)TheFilters.getModel();
						if (mod.getSize()>0)
						{
							if (TheFilters.getSelectedIndex()<0)
								return;
							FeatureFilter f = (FeatureFilter)TheFilters.getSelectedValue();
							mod.remove(TheFilters.getSelectedIndex());
							TheFilters.repaint();
							
							mod = (DefaultListModel)ThePossibleFilters.getModel();
							mod.addElement(f);
							ThePossibleFilters.repaint();
						}
						
						
						
					}
				});
		TheButtonPanel.add(but,1);
		
		but =  new JButton("Execute");
		but.addActionListener(new ActionListener()
							  {
					public void actionPerformed(ActionEvent ae)
					{
						executeFiters();
						
					}
				});
		TheButtonPanel.add(but,2);
		
		
		//adding the possible filters
		ThePossibleFilters = new JList(getAllPossibleFilters());
		ThePossibleFilters.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		ThePossibleFilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ThePossibleFilters.setLayoutOrientation(JList.VERTICAL);
		ThePossibleFilters.setSelectedIndex(0);
		ThePossibleFilters.setVisibleRowCount(-1);
		ThePossibleFilters.addListSelectionListener(new ListSelectionHandler());
		possibleFiltersScroller = new JScrollPane(ThePossibleFilters);
		possibleFiltersScroller.setPreferredSize(new Dimension(200, 150));
		TheMainPanel.add(possibleFiltersScroller, 0);
		
		
		
		TheFilters = new JList(new DefaultListModel());
		TheFilters.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		TheFilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TheFilters.setLayoutOrientation(JList.VERTICAL);
		TheFilters.setSelectedIndex(0);
		TheFilters.setVisibleRowCount(-1);
		TheFilters.addListSelectionListener(new ListSelectionHandler());
		selectedFiltersScroller = new JScrollPane(TheFilters);
		selectedFiltersScroller.setPreferredSize(new Dimension(200, 150));
		
		
		FeatureFilter f = (FeatureFilter)ThePossibleFilters.getSelectedValue();
		if(f!=null)
		{
			TheMainPanel.add(f.getParameterPanel(),1);
			TheMainPanel.add(TheButtonPanel, 2);
			TheMainPanel.add(selectedFiltersScroller, 3);
		}
		else
		{
			getContentPane().removeAll();
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(new EmptyPanel());
		}
	}
	
	public void updateRightPanelWithNewFilter(FeatureFilter filter)
	{
		TheMainPanel.removeAll();
		TheMainPanel.add(possibleFiltersScroller,0);
		if (filter!=null)
			TheMainPanel.add(filter.getParameterPanel(), 1);
		else
			TheMainPanel.add(new JPanel(),1);
		TheMainPanel.add(TheButtonPanel, 2);
		TheMainPanel.add(selectedFiltersScroller, 3);
		TheMainPanel.validate();
		TheMainPanel.repaint();
		
	}
	
	
	class ListSelectionHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			JList lsm = (JList)e.getSource();
			if (!lsm.isSelectionEmpty())
			{
				// Find out which indexes are selected.
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();
				for (int i = minIndex; i <= maxIndex; i++)
				{
					if (lsm.isSelectedIndex(i))
					{
						updateRightPanelWithNewFilter((FeatureFilter)lsm.getSelectedValue());
						
					}
				}
			}
			
		}
	}
	
	public FeatureFilter[] getSelectedFilters()
	{
		DefaultListModel f = (DefaultListModel)	TheFilters.getModel();
		int len = f.getSize();
		FeatureFilter[] fs = new FeatureFilter[len];
		for (int i = 0; i < len; i++)
			fs[i] = (FeatureFilter)f.get(i);
		return fs;
	}
	
	
	/** This is where you add the possible (or newly written) filters to potentially be used
	 * @author BLM*/
	public ListModel getAllPossibleFilters()
	{
		if (TheMainGUI==null)
			return null;
		DefaultListModel listModel = new DefaultListModel();

		ArrayList<Feature> arr = TheMainGUI.getTheFeatures();
		if (arr==null)
			return null;
		
		int len = arr.size();
		for (int i = 0; i < len; i++)
			listModel.addElement(new FeatureFilter(arr.get(i)));
		
//		listModel.addElement(new FeatureFilter(new Size_Nucleus()));
		return listModel;
	}
	
	
	/** Determines if the given cell passes all the defined and selected filters
	 * @author BLM*/
	public boolean doesCellPass(Cell_RAM cell)
	{
		boolean pass = true;
		FeatureFilter[] filters = getSelectedFilters();
		int numF = filters.length;
		for (int i = 0; i < numF; i++)
		{
			FeatureFilter f = filters[i];
			if(!f.pass(cell))
			{
				pass = false;
				break;
			}
		}
		return pass;
	}
	
	
	public void executeFiters()
	{
//		//TODO - fix filter manager with new Cell_coords structure
//		FeatureFilter[] filters = getSelectedFilters();
//		int numF = filters.length;
//		System.out.println("numFilters: "+numF);
//
//		PlateHoldingPanel platePanel = TheMainGUI.getPlateHoldingPanel();
//		Plate[] plates = platePanel.getThePlates();
//
//		int numPlates = platePanel.getNumPlates();
//		for (int p = 0; p < numPlates; p++)
//		{
//			//For each plate, execute this filter
//			Plate plate = plates[p];
//			for (int r = 0; r < plate.getNumRows(); r++)
//				for (int c = 0; c < plate.getNumColumns(); c++)
//				{
//					Well well = plate.getTheWells()[r][c];
//					float[][] cellValues = well.getCell_values();
//					int numCells = cellValues.length;
//					
//					
//					for(int n = 0; n<numCells; n++)
//					{
//						Cell_RAM cell = well.TheCells[n];
//						boolean pass = true;
//						//Filtering
//						for (int f = 0; f < numF; f++)
//							if(!filters[f].pass(cell))
//							{
//								cell.setSelected(true);
//								pass = false;
//								break;
//							}
//						if (pass)
//							cell.setSelected(false);
//					}
//
//					if (numCells>0 && well!=null)
//					{
//						//Delete selected cells
//						well.deleteSelectedCells();
//					}
//				}
//		}
	}
	
	
	private class EmptyPanel extends JPanel
	{
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(TheMainGUI.Font_18);
			g2.drawString("No Features Loaded to Filter", getWidth()/2f-130,30);
		}
	}
}

