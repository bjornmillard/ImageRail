package imPanels;

import gui.MainGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import tools.PanelDropTargetListener;

public class MovieLoaderFrame extends JFrame{

	private LoadPanel TheLoadPanel;
	private int height = 170;
	private int width = 280;
	private int borderWidth = 15;
	
	public MovieLoaderFrame() {
		super("Movie Loader");
		setResizable(false);
		setSize(width, height);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.width / 2f) - width / 2, (int) (d.height / 2f)
				- height / 2);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});

		LoadPanel TheLoadPanel = new LoadPanel();
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(TheLoadPanel, BorderLayout.CENTER);

		TheLoadPanel.repaint();
		validate();
		repaint();
		setVisible(true);
	}

	public void go() {
		
		int numRows = 1;
		int numCols = 1;
	
		boolean worked = gui.MainGUI.getGUI()
				.initNewPlates(1, numRows, numCols);
		if (worked) {

			gui.MainGUI.getGUI().setVisible(true);
		}
	}
	
	
	private class LoadPanel extends JPanel
	{
		/** Array of colors for the border of the plate rendering */
		private Color[] BorderColors;

		public LoadPanel() {
			setSize(width, height);
			setLayout(new BorderLayout());
			// Adding the Drag and Drop feature for file loading
			PanelDropTargetListener drop = new PanelDropTargetListener();
			new DropTarget(this, drop);

			BorderColors = new Color[6];
			BorderColors[0] = Color.lightGray;
			BorderColors[1] = new Color(200, 200, 200);
			BorderColors[2] = new Color(150, 150, 150);
			BorderColors[3] = new Color(100, 100, 100);
			BorderColors[4] = new Color(80, 80, 80);
			BorderColors[5] = new Color(50, 50, 50);

		}

		/**
		 *
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// finding the Ystart
			int Ystart = borderWidth;
			int Xstart = borderWidth;


			// drawing a graded border around it
			for (int i = BorderColors.length - 1; i >= 0; i--) {
				g2.setColor(BorderColors[i]);
				g2.fillRoundRect((Xstart - i), (Ystart - i),
						(width - 2 + 2 * i - 2 * borderWidth), (height - 25 + 2
								* i - 2 * borderWidth), 10, 10);
			}
			g2.setColor(Color.BLACK);
			Font f = g2.getFont();
			g2.setFont(MainGUI.Font_16);
			String st = "Drag Movie Folder Here";
			g2.drawString(st, width / 2f - (st.length() * 4), height / 2f - 7);
			g2.setFont(f);
		}
	}
}
