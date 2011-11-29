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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import models.Model_Main;
import dialogs.PlateInputDialog;

public class MainStartupDialog extends JFrame
{
	private Image im;
	private Icon sdc_icon;
	private int width = 475;
	private int height = 200;
	private JFrame TheStartUpDialog = this;
	
	
	public MainStartupDialog()
	{
		setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
		JPanel content = (JPanel)getContentPane();
		Color color = new Color(1f, 1f, 1f,  1f);
		content.setBackground(color);
		content.setLayout(new BorderLayout());
		
		im = Toolkit.getDefaultToolkit().getImage(
				"icons/ImageRail_long_newLogo.png");
		Image sdc_icon_image = Toolkit.getDefaultToolkit().getImage("icons/ImageRail_icon.png")
				.getScaledInstance(-1, 16, Image.SCALE_SMOOTH);
		sdc_icon = new ImageIcon(sdc_icon_image);
		
		// Set the window's bounds, centering the window
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		setBounds(x,y,width,height);
		
		
		content.add(new TopPanel(), BorderLayout.NORTH);
		content.add(new MainPanel(), BorderLayout.CENTER);
		//Adding a border
		content.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		setVisible(true);
		
		
		repaint();
		
	}
	
	public class TopPanel extends JPanel
	{
		TopPanel()
		{
			setPreferredSize(new Dimension(width,85));
			repaint();
		}
		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			super.paintComponent(g);
			if(im != null)
				g.drawImage(im, 0, 0, this);
			
			g2.setFont(MainGUI.Font_12);
		}
		
	}
	
	public class MainPanel extends JPanel
	{
		MainPanel()
		{
			setLayout(new GridLayout());
			add(new NewProjPanel(),0);
			add(new LoadProjPanel(),1);
		}
		
	}
	
	public class HyperLink extends JLabel
	{
		private final HyperLink link = this;
		public HyperLink(String text)
		{
			
			setText(text);
			setFont(new Font("SansSerif", Font.PLAIN, 18));
			setHorizontalAlignment(JLabel.CENTER);
			setVerticalAlignment(JLabel.CENTER);
			addMouseListener(new java.awt.event.MouseAdapter()
							 {
						public void mouseEntered(java.awt.event.MouseEvent evt)
						{
							link.setForeground(Color.red);
						}
					});
			addMouseListener(new java.awt.event.MouseAdapter()
							 {
						public void mouseExited(java.awt.event.MouseEvent evt)
						{
							link.setForeground(Color.black);
						}
					});
		}
	}
	
	public class NewProjPanel extends JPanel
	{
		NewProjPanel()
		{
			setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
			HyperLink but = new HyperLink("Create new project...");
			
			
			but.addMouseListener(new java.awt.event.MouseAdapter()
								 {
						public void mouseClicked(java.awt.event.MouseEvent evt)
						{
							if(evt.getClickCount() > 0)
							{
								new PlateInputDialog(TheStartUpDialog);
							}
						}
					});
			
			
			setLayout(new GridLayout(3,1));
			add(new JLabel(""), 0);
			add(but, 1);
			add(new JLabel(""), 2);
			
			
		}
		
	}
	
	public class LoadProjPanel extends JPanel
	{
		LoadProjPanel()
		{
			setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
			HyperLink but = new HyperLink("Open existing project...");
			but.addMouseListener(new java.awt.event.MouseAdapter()
								 {
						public void mouseClicked(java.awt.event.MouseEvent evt)
						{
							if(evt.getClickCount() > 0)
 {
						// Get current model first if exists
						Model_Main TheMainModel = models.Model_Main.getModel();
						if (TheMainModel == null)
							TheMainModel = new Model_Main();
						File dir = TheMainModel.getTheDirectory();
								JFileChooser fc = null;
								if (dir!=null)
									fc = new JFileChooser(dir);
								else
									fc = new JFileChooser();
								
						fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						fc.addChoosableFileFilter(new FileChooserFilter_IR());
						fc.setFileView(new FileChooserView_IR());

								int returnVal = fc.showOpenDialog(null);
								if (returnVal == JFileChooser.APPROVE_OPTION)
								{
									File f = fc.getSelectedFile();
									if (f!=null && f.isDirectory())
									{
								if (f.getName().indexOf(".sdc") > 0)
										{
											TheStartUpDialog.setVisible(false);
									TheMainModel.loadProject(f
.getAbsolutePath(),
											f.getAbsolutePath());
									MainGUI TheMainGUI = new MainGUI(
											TheMainModel);
									if(TheMainGUI!=null)
										TheMainGUI.setTitle("Project: "+TheMainModel.getInputProjectPath());
									TheMainGUI.setVisible(true);
										}
										else
										{
											JOptionPane.showMessageDialog(null,"Invalid Project! \n\n Please try again","Invalid Project",JOptionPane.ERROR_MESSAGE);
										}
									}
									
								}
								
							}
						}
					});
			setLayout(new GridLayout(3,1));
			add(new JLabel(""), 0);
			add(but, 1);
			add(new JLabel(""), 2);
			
		}
		
		
	}
	
	

	class FileChooserFilter_IR extends javax.swing.filechooser.FileFilter {
		public boolean accept(File file) {
			return file.isDirectory();
		}
		
		public String getDescription() {
			return "ImageRail projects";
		}
	}
	

	/*
	 * For IR projects, prevents navigating into them and displays a custom icon
	 */
	class FileChooserView_IR extends javax.swing.filechooser.FileView {
		public Boolean isTraversable(File f) {
			return (f.isDirectory() && !f.getName().endsWith(".sdc")) ? Boolean.TRUE : Boolean.FALSE;
		}
		
		public Icon getIcon(File f) {
			return f.getName().endsWith(".sdc") ? sdc_icon : null;
		}
	}
	
	
}

