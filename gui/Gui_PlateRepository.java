package gui;

import imageViewers.FieldViewer_Frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.Model_Plate;
import models.Model_PlateRepository;

public class Gui_PlateRepository extends JPanel {
	private Model_PlateRepository TheModel;
	/** */
	private JToolBar TheToolBar;
	/** */
	private boolean blocked;
	/** */
	private JTabbedPane TheMainPanel;
	/** Buttons for the toolbar */
	private JButton DisplayHistogramsButton;
	/** */
	private JButton LogScaleButton;
	/** */
	private JButton DataViewButton;
	/** */
	private boolean Grid;
	/** */
	private int LastTouched_PlateID;

	public Gui_PlateRepository(Model_PlateRepository model) {
		TheModel = model;
		model.setGUI(this);
		blocked = false;
		int sqNumPlates = (int) Math.ceil(Math.sqrt(TheModel.getNumPlates()));
		Grid = true;

		// Adding the grid tab panel
		LastTouched_PlateID = 1;
		TheMainPanel = new JTabbedPane();
		final JPanel GridPanel = new JPanel();
		GridPanel.setLayout(new GridLayout(sqNumPlates, sqNumPlates));
		int counter = 0;
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int r = 0; r < TheModel.getNumPlates(); r++) {
			GridPanel.add(ThePlates[r].getGUI(), counter);
			counter++;
		}
		TheMainPanel.addTab("All", GridPanel);

		// Adding the single plate tabs
		for (int p = 0; p < TheModel.getNumPlates(); p++) {
			if (ThePlates[p].getTitle() == null)
				ThePlates[p].setTitle("Plate #" + ThePlates[p].getID());
			TheMainPanel.addTab(ThePlates[p].getTitle(), null);
		}

		TheMainPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent p1) {
				int i = TheMainPanel.getSelectedIndex();
				Model_Plate[] ThePlates = TheModel.getPlates();

				if (i == 0) // All plates view
				{
					int counter = 0;
					GridPanel.removeAll();
					for (int p = 0; p < TheModel.getNumPlates(); p++) {
						GridPanel.add(ThePlates[p].getGUI(), counter);
						counter++;
					}
					TheMainPanel.setComponentAt(i, GridPanel);

					// Adding the single plate tabs
					for (int p = 0; p < TheModel.getNumPlates(); p++)
						TheMainPanel.addTab(ThePlates[p].getTitle(), null);
					Grid = true;
				} else {
					int counter = 0;
					for (int p = 0; p < TheModel.getNumPlates(); p++) {
						counter++;
						TheMainPanel.setComponentAt(counter, ThePlates[p]
								.getGUI());
					}
					Grid = false;
				}
			}
		});

		setLayout(new BorderLayout());
		add(TheMainPanel, BorderLayout.CENTER);
		updatePanel();

		TheToolBar = new JToolBar();
		add(TheToolBar, BorderLayout.NORTH);
		addToolbarComponents();

	}

	/** Returns the Plate repository model that this gui represents */
	public Model_PlateRepository getModel() {
		return TheModel;
	}

	/**
	 * States whether we should normalized across all plates or just singles.
	 * This is because sometimes the panel can display all plates in a grid or
	 * only view one plate at a time
	 * 
	 * @author BLM
	 */
	public boolean shouldNormalizeAcrossAllPlates() {
		return Grid;
	}

	/**
	 * Returns the tabbed pane
	 * 
	 * @athor BLM
	 */
	public JTabbedPane getTheMainPanel() {
		return TheMainPanel;
	}

	/**
	 * Returns boolean whether we should display the data table or not
	 * 
	 * @author BLM
	 */
	public boolean showData() {
		return DataViewButton.isSelected();
	}

	public int getSelectedPlateID() {
		return LastTouched_PlateID;
	}

	public void setLastTouched_PlateID(int id) {
		LastTouched_PlateID = id;
	}

	public void setTab(int index) {
		TheMainPanel.setSelectedIndex(index);
		updatePanel();
	}

	public void updatePanel() {
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int p = 0; p < TheModel.getNumPlates(); p++)
			ThePlates[p].getGUI().updatePanel();
	}

	public void setDisplayRowLegends(boolean boo) {
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int p = 0; p < TheModel.getNumPlates(); p++)
			ThePlates[p].getGUI().setDisplayRowLegend(boo);
	}

	public void addToolbarComponents() {
		LogScaleButton = new JButton(tools.Icons.Icon_Log_selected);
		LogScaleButton.setSelected(true);
		LogScaleButton.setToolTipText("Log Scale");
		TheToolBar.add(LogScaleButton);
		LogScaleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				LogScaleButton.setSelected(!LogScaleButton.isSelected());
				if (LogScaleButton.isSelected())
					LogScaleButton.setIcon(tools.Icons.Icon_Log_selected);
				else
					LogScaleButton.setIcon(tools.Icons.Icon_Log);
				validate();
				repaint();
				updatePanel();
			}
		});

		DisplayHistogramsButton = new JButton(
				tools.Icons.Icon_PlateHistogram_selected);
		DisplayHistogramsButton.setSelected(true);
		DisplayHistogramsButton.setToolTipText("Mini-Histograms");
		TheToolBar.add(DisplayHistogramsButton);
		DisplayHistogramsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				DisplayHistogramsButton.setSelected(!DisplayHistogramsButton
						.isSelected());
				if (DisplayHistogramsButton.isSelected())
					DisplayHistogramsButton
							.setIcon(tools.Icons.Icon_PlateHistogram_selected);
				else
					DisplayHistogramsButton
							.setIcon(tools.Icons.Icon_PlateHistogram);

				updatePanel();
			}
		});

		TheToolBar.add(LogScaleButton);
		TheToolBar.add(DisplayHistogramsButton);

		DataViewButton = new JButton(tools.Icons.Icon_Data);
		DataViewButton.setToolTipText("Toggle Data Tables/Model_Plate View");
		TheToolBar.add(DataViewButton);
		DataViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				DataViewButton.setSelected(!DataViewButton.isSelected());
				if (DataViewButton.isSelected())
					DataViewButton.setIcon(tools.Icons.Icon_Data_selected);
				else
					DataViewButton.setIcon(tools.Icons.Icon_Data);
				validate();
				repaint();
				updatePanel();
			}
		});

		// final JButton but = new JButton("Mono");
		// but.setSelected(false);
		// but.setToolTipText("Fit Single Gaussian");
		// TheToolBar.add(but);
		// but.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent ae)
		// {
		// but.setSelected(!but.isSelected());
		// if (but.isSelected())
		// FitGaussian = 1;
		// else
		// FitGaussian = 0;
		// validate();
		// repaint();
		// updatePanel();
		// }
		// });
		//

	}

	public boolean isBlocked() {
		return blocked;
	}

	/**
	 * Returns the main toolbar for this panel
	 * 
	 * @author BLM
	 */
	public JToolBar getTheToolBar() {
		return TheToolBar;
	}

	public void unblock() {
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int p = 0; p < TheModel.getNumPlates(); p++) {
			ThePlates[p].getGUI().unblock();
			ThePlates[p].getGUI().updatePanel();
		}
		blocked = false;
	}

	public void block(FieldViewer_Frame im) {
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int p = 0; p < TheModel.getNumPlates(); p++) {
			ThePlates[p].getGUI().block(im);
			ThePlates[p].getGUI().updatePanel();
		}
		blocked = true;
	}

	/**
	 * Returns whether the mini-histograms should be displayed
	 * 
	 * @athor BLM
	 */
	public boolean shouldDisplayHistograms() {
		return DisplayHistogramsButton.isSelected();
	}

	/**
	 * Returns whether the data is log transformed for the mini-histograms
	 * 
	 * @athor BLM
	 */
	public boolean isLogScaled() {
		return LogScaleButton.isSelected();
	}

	public void unHighlightAllWells() {
		Model_Plate[] ThePlates = TheModel.getPlates();
		for (int p = 0; p < TheModel.getNumPlates(); p++) {
			Model_Plate plate = ThePlates[p];
			for (int r = 0; r < plate.getNumRows(); r++)
				for (int c = 0; c < plate.getNumColumns(); c++)
					plate.getWells()[r][c].setSelected(false);
		}
		updatePanel();
	}

}
