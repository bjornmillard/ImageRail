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

package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class MainSplash extends JWindow {
	private Image im;
	private int duration;
	private int width = 388;
	private int height = 136;
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
				"icons/ImageRail_splash_newLogo.png");

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
		} else // put it in a closable frame
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

			g2.setFont(MainGUI.Font_9);
			g2.setColor(Color.black);
			g2.drawString(TheMessage, 10, getHeight() - 10);
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
