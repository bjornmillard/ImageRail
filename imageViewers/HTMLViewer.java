package imageViewers;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.*;

/** HTMLViewer.java
 A simple HTML Viewer for browsing through Ingeneue help files.
 @author WJS
 */
public class HTMLViewer extends JFrame
{
	
	/** Button for navigating back through list of visited links */
	private JButton                     BackButton;
	/** The EditorPane for storing  */
	private JEditorPane                 EditorPane;
	/** Button for navigating forward through list of visited links */
	private JButton                     ForwardButton;
	/** The main pane holds all */
	private JPanel                      MainPane;
	/** The current link in the visited links list */
	private int                         VisitedLinkIndex = 0;
	/** the list of visited links */
	private Vector                      VisitedLinkList;
	
	
	/** Simple constructor
	 @param URL - The url to initialize the Editor pane with
	 */
	public HTMLViewer(URL url)
	{
		
		
		VisitedLinkList = new Vector();
		
		// Make main pane
		MainPane = new JPanel();
		setContentPane(MainPane);
		MainPane.setLayout(new BorderLayout());
		
		// Make the toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(Color.lightGray);
		toolBar.setFloatable(false);
		
		// Back button moves us back to previous page
		BackButton = new JButton(new ImageIcon("icons/left_arrow.png"));
		BackButton.addActionListener(new ActionListener()
									 {
					public void actionPerformed(ActionEvent ae)
					{
						if (VisitedLinkIndex>0)
						{
							VisitedLinkIndex--;
							try
							{
								EditorPane.setPage((URL)VisitedLinkList.get(VisitedLinkIndex));
							}
							catch (Exception ex)
							{
							}
						}
					}
				});
		toolBar.add(BackButton);
		
		// Forward button moves us forward to a page iff back was used
		ForwardButton = new JButton(new ImageIcon("icons/right_arrow.png"));
		ForwardButton.addActionListener(new ActionListener()
										{
					public void actionPerformed(ActionEvent ae)
					{
						if (VisitedLinkIndex<VisitedLinkList.size()-1)
						{
							VisitedLinkIndex++;
							try
							{
								EditorPane.setPage((URL)VisitedLinkList.get(VisitedLinkIndex));
							}
							catch (Exception ex)
							{
							}
						}
					}
				});
		toolBar.add(ForwardButton);
		
		MainPane.add(toolBar,BorderLayout.NORTH);
		
		// Make the editor pane
		try
		{
			EditorPane = new JEditorPane(url);
			VisitedLinkList.add(url);
			EditorPane.setEditable(false);
			
			// Add a hyperlink listener to handle clicks on links
			EditorPane.addHyperlinkListener(new HyperlinkListener()
											{
						public void hyperlinkUpdate(HyperlinkEvent e)
						{
							if (e.getEventType()==HyperlinkEvent.EventType.ENTERED)
								setCursor(new Cursor(Cursor.HAND_CURSOR));
							if (e.getEventType()==HyperlinkEvent.EventType.EXITED)
								setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
							{
								try
								{
									EditorPane.setPage(e.getURL());
									for (int i=VisitedLinkList.size()-1;i>VisitedLinkIndex;i--)
										VisitedLinkList.remove(i);
									VisitedLinkList.add(e.getURL());
									VisitedLinkIndex++;
								}
								catch (Exception ex)
								{
									System.out.println("Failed to open url - "+e.getURL().getPath());
								}
							}
						}
					});
		}
		catch (IOException ioex)
		{
			System.out.println("File not found - "+url.getPath());
		}
		
		JScrollPane scrollPane = new JScrollPane(EditorPane);
		MainPane.add(scrollPane,BorderLayout.CENTER);
		
		setSize(525 ,800);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = getWidth();
		int height = getHeight();
		setLocation((int)(d.width/2f)-width/2+500,(int)(d.height/2f)-height/2-100);
		
	}
	
	/** Sets the page contents according to the URL
	 @param String - url string.
	 @author WJS
	 */
	public void setURL(String urlString)
	{
		
		try
		{
			URL url = new URL(urlString);
			try
			{
				EditorPane.setPage(url);
			}
			catch (Exception ex)
			{
				System.out.println("HTMLViewer - page not found: "+urlString);
			}
		}
		catch (MalformedURLException mue)
		{
			System.out.println("HTMLViewer - malformed URL: "+urlString);
		}
		
	}
	
	/** Sets the page contents according to the URL
	 @param URL - the url object.
	 @author WJS
	 */
	public void setURL(URL url)
	{
		
		try
		{
			EditorPane.setPage(url);
		}
		catch (Exception ex)
		{
			System.out.println("HTMLViewer - page not found");
		}
		
	}
	
}

