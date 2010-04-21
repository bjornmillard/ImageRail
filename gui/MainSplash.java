package gui;


import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class MainSplash extends JWindow {
	private Image im;
	private int duration;
	private int width = 580;
	private int height = 160;
	private String TheMessage;

	public MainSplash(int d) {
		duration = d;
	}

	public MainSplash() {
		duration = -1;
	}

	// A simple little method to show a title screen in the center
	// of the screen for the amount of time given in the constructor
	public void showSplash() {
		TheMessage = "Starting...";
		JPanel content = (JPanel) getContentPane();
		Color color = new Color(1f, 1f, 1f, 1f);
		content.setBackground(color);
		content.setLayout(new BorderLayout());

		im = Toolkit.getDefaultToolkit().getImage(
				"doc/Images/ImageRailSplashScreen.png");

		// Set the window's bounds, centering the window
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - width) / 2;
		int y = (screen.height - height) / 2;
		setBounds(x, y, width, height);

		if (duration != -1) {
			// Adding the center panel
			content.add(new DisplayPanel());
			// Adding a border
			content.setBorder(BorderFactory
					.createLineBorder(Color.DARK_GRAY, 2));
			setVisible(true);
		} else // put it in a closableframe
		{
			JFrame f = new JFrame();
			f.setSize(width, height);
			f.getContentPane().add(new DisplayPanel());
			f.setBounds(x, y, width - 3, height + 18);
			f.setVisible(true);
		}

	}

	public class DisplayPanel extends JPanel {
		DisplayPanel() {
			repaint();
		}

		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			super.paintComponent(g);
			if (im != null)
				g.drawImage(im, 0, 0, this);

			g2.setFont(MainGUI.Font_12);
			g2.drawString(TheMessage, 5, getHeight() - 10);
		}

	}

	public void showSplashAndExit() {
		showSplash();
	}

	public void setMessage(String message) {
		TheMessage = message;
		validate();
		repaint();
		try {
			Thread.sleep(80);
		} catch (Exception e) {
		}
	}
}
