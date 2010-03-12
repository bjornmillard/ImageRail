/**
 * StartupDialog.java
 *
 * @author Created by Omnicore CodeGuide
 */

package main;

import java.awt.*;
import javax.swing.*;

import dialogs.PlateInputDialog;
import java.io.File;

public class StartupDialog extends JFrame
{
	private Image im;
	private int width = 475;
	private int height = 250;
	private JFrame TheStartUpDialog = this;
	
	
	public StartupDialog()
	{
		setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
		JPanel content = (JPanel)getContentPane();
		Color color = new Color(1f, 1f, 1f,  1f);
		content.setBackground(color);
		content.setLayout(new BorderLayout());
		
		im = Toolkit.getDefaultToolkit().getImage("doc/Images/ImageRail_long.png");
		
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
			setFont(new Font("Serif", Font.BOLD, 16));
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
								MainGUI gui = main.MainGUI.getGUI();
								File dir = main.MainGUI.getGUI().getTheDirectory();
								JFileChooser fc = null;
								if (dir!=null)
									fc = new JFileChooser(dir);
								else
									fc = new JFileChooser();
								
								fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								int returnVal = fc.showOpenDialog(null);
								if (returnVal == JFileChooser.APPROVE_OPTION)
								{
									File f = fc.getSelectedFile();
									if (f!=null && f.isDirectory())
									{
										if(gui.containsFile(f, "project.h5") || f.getName().indexOf(".ir")>0)
										{
											TheStartUpDialog.setVisible(false);
											gui.loadProject(f);
											gui.setVisible(true);
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
	
	

	
	
	
	
}

