/**
 * Plate_MetaDataDisplay.java
 *
 * @author BLM
 */

package analysisModules;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import main.MainGUI;
import main.Plate;
import us.hms.systemsbiology.metadata.Description;
import us.hms.systemsbiology.metadata.MetaDataConnector;

public class Plate_MetaDataDisplay extends JPanel
{
	
	private JFrame TheFrame;
	private ArrayList<Description> uniqueTreats;
	private ArrayList<Description> allTreat;
//	private ArrayList<Description[]> allMeas;
	private MetaDataConnector TheMetaDataConnector;
	private Hashtable minMaxHash;
	private Plate ThisPlate;
	private String[] TheColLabels;
	private String[] TheRowLabels;
	
	public Plate_MetaDataDisplay(final Plate plate, String title, int width, int height, MetaDataConnector metaDataConnector)
	{
		TheMetaDataConnector = metaDataConnector;
		
		ThisPlate = plate.copy();
		ThisPlate.allowImageCountDisplay(false);
		ThisPlate.setYstart(200);
		ThisPlate.setXstart(200);
		ThisPlate.setSize((int)(1.5f*200f), (int)(1.5f*400f));
		
		
		uniqueTreats = new ArrayList<Description>();
		int pIndex = plate.getPlateIndex();
		allTreat = new ArrayList<Description>();
//		allMeas = new ArrayList<Description[]>();
		for (int r = 0; r < plate.getNumRows(); r++)
			for (int c = 0; c < plate.getNumColumns(); c++)
			{
				Description[] treat = metaDataConnector.readTreatments(plate.getTheWells()[r][c].getWellIndex());
				
				if(treat!=null && treat.length>0)
				{
					for (int i = 0; i < treat.length; i++)
					{
						boolean unique = true;
						int num = uniqueTreats.size();
						for (int j = 0; j < num; j++)
						{
							if(uniqueTreats.get(j).getName().equalsIgnoreCase(treat[i].getName()))
							{
								unique = false;
								break;
							}
						}
						if(unique)
							uniqueTreats.add(treat[i]);
					}
				}
			}
		
		//Init the minMax values for each unique treatment
		minMaxHash = new Hashtable();
		int len = uniqueTreats.size();
		for (int z = 0; z < len; z++)
		{
			String name = (uniqueTreats.get(z)).getName();
			float min = Float.POSITIVE_INFINITY;
			float max = Float.NEGATIVE_INFINITY;
			float min_log = Float.POSITIVE_INFINITY;
			float max_log = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < plate.getNumRows(); i++)
			{
				for (int j = 0; j < plate.getNumColumns(); j++)
				{
					Description treat = getTreatmentFromWell(name, plate.getTheWells()[i][j].getWellIndex(), TheMetaDataConnector);
					if (treat!=null)
					{
						float val = Float.parseFloat(treat.getValue());
						if (val<min)
							min =val;
						if (val>max)
							max = val;
						
						float val_log = (float)(Math.log(1d+val));
						if (val_log < min_log)
							min_log = val_log;
						if (val_log > max_log)
							max_log = val_log;
					}
				}
			}
			minMaxHash.put(name+"_min", new Float(min));
			minMaxHash.put(name+"_max", new Float(max));
			minMaxHash.put(name+"_min_log", new Float(min_log));
			minMaxHash.put(name+"_max_log", new Float(max_log));
		}
		
		
		JPanel controlPanel = new JPanel();
		controlPanel.setSize(60,height);
		controlPanel.setLayout(new GridLayout(20,1));
		
		
		//Adding titleLabel to combobox
		GradientLabel label = new GradientLabel("  Treatments:");
		label.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		controlPanel.add(label);
		
		
		//Adding unique treatments to combobox
		ArrayList list = new ArrayList();
		len = uniqueTreats.size();
		for (int i = 0; i < len; i++)
			list.add((uniqueTreats.get(i)).getName());
		Object[] obX = new Object[list.size()];
		if (list.size()>0)
			for (int i = 0; i < list.size(); i++)
				obX[i] = list.get(i);
		final JComboBox treatCombo = new JComboBox(obX);
		treatCombo.setToolTipText("Treatments");
		treatCombo.addActionListener(new ActionListener()
									 {
					public void actionPerformed(ActionEvent ae)
					{
						String name = (String)treatCombo.getSelectedItem();
						for (int r = 0; r < ThisPlate.getNumRows(); r++)
							for (int c = 0; c < ThisPlate.getNumColumns(); c++)
							{
								Description[] treats  = TheMetaDataConnector.readTreatments(plate.getTheWells()[r][c].getWellIndex());
								Color color = Color.black;
								float val = 0;
								if (treats!=null && treats.length>0)
								{
									for (int i = 0; i < treats.length; i++)
										if(treats[i].getName().equalsIgnoreCase(name))
											val = Float.parseFloat(treats[i].getValue());
									
									
									float min = 0;
									float max = 1;
									boolean log = false;
									if (log)
									{
										min = (Float)minMaxHash.get(name+"_min");
										max = (Float)minMaxHash.get(name+"_max");
									}
									else
									{
										val = (float)Math.log(1d+val);
										min = (Float)minMaxHash.get(name+"_min_log");
										max = (Float)minMaxHash.get(name+"_max_log");
										if((max-min)==0)
											max =1;
									}
									
									
									float norm = ((val-min)/(max-min));
									
									color = new Color(norm, 0, 0);
								}
								else
								{
									color = Color.white;
									
								}
								
								ThisPlate.getTheWells()[r][c].color = color;
								ThisPlate.getTheWells()[r][c].color_outline = Color.darkGray;
								
							}
						
						validate();
						repaint();
					}
				});
		controlPanel.add(treatCombo);
		
		
		
		TheRowLabels = getRowLabels();
		TheColLabels = getColLabels();
		
		
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		
		TheFrame = new JFrame();
		Dimension dim =	new Dimension(width+150, height);
		TheFrame.setSize(dim);
		TheFrame.setPreferredSize(dim);
		TheFrame.setResizable(false);
		TheFrame.setVisible(true);
		
		
		TheFrame.getContentPane().add(controlPanel, BorderLayout.EAST);
		TheFrame.getContentPane().add(this, BorderLayout.CENTER);
		
		TheFrame.repaint();
		TheFrame.validate();
		
		
		
	}
	
	private String[] getColLabels()
	{
		String name = "EGF";
		String[] oneRow = new String[ThisPlate.getNumColumns()];
		for (int r = 0; r < ThisPlate.getNumRows(); r++)
		{
			//Looking for the first non-null row
			boolean nonNull = false;
			for (int c = 0; c < ThisPlate.getNumColumns(); c++)
			{
				Description treat = getTreatmentFromWell(name,  ThisPlate.getTheWells()[r][c].getWellIndex(), TheMetaDataConnector);
				if(treat!=null)
				{
					oneRow[c] = ""+(treat.getValue());
					nonNull = true;
					System.out.println("treat: "+treat.getName()+" = "+treat.getValue());
				}
				else
				{
					oneRow[c] = "";
				}
				
			}
			if(nonNull)
				break;
		}
		
		for (int r = 1; r < ThisPlate.getNumRows(); r++)
		{
			for (int c = 0; c < ThisPlate.getNumColumns(); c++)
			{
				Description treat = getTreatmentFromWell(name,  ThisPlate.getTheWells()[r][c].getWellIndex(), TheMetaDataConnector);
				if(treat!=null&&!(treat.getValue().equalsIgnoreCase(oneRow[c])))
				{
					return null;
				}
			}
		}
		
		return oneRow;
	}
	
	
	
	private String[] getRowLabels()
	{
		String name = "gefitinib";
		String[] oneCol = new String[ThisPlate.getNumRows()];
		
		
		for (int c = 0; c < ThisPlate.getNumColumns(); c++)
		{
			//Looking for the first non-null row
			boolean nonNull = false;
			for (int r = 0; r < ThisPlate.getNumRows(); r++)
			{
				Description treat = getTreatmentFromWell(name,  ThisPlate.getTheWells()[r][c].getWellIndex(), TheMetaDataConnector);
				if(treat!=null)
				{
					oneCol[r] = ""+(treat.getValue());
					nonNull = true;
					System.out.println("treat: "+treat.getName()+" = "+treat.getValue());
				}
				else
				{
					oneCol[r] = "";
				}
				
			}
			if(nonNull)
				break;
		}
		
		for (int c = 1; c < ThisPlate.getNumColumns(); c++)
		{
			for (int r = 0; r < ThisPlate.getNumRows(); r++)
			{
				Description treat = getTreatmentFromWell(name, ThisPlate.getTheWells()[r][c].getWellIndex(), TheMetaDataConnector);
				if(treat!=null&&!(treat.getValue().equalsIgnoreCase(oneCol[r])))
				{
					return null;
				}
			}
		}
		
		return oneCol;
	}
	
	
	/** Looks in the given plate/well for the given treatment with the given name
	 * @author BLM*/
	private Description getTreatmentFromWell(String name, int wIndex, MetaDataConnector meta)
	{
		Description[] d = meta.readTreatments(wIndex);
		for (int i = 0; i < d.length; i++)
		{
			if (d[i].getName().equalsIgnoreCase(name))
				return d[i];
		}
		return null;
	}
	
	
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		int numR = ThisPlate.getTheWells().length;
		int numC = ThisPlate.getTheWells()[0].length;
		//Drawing the plate name above it
		g2.setColor(Color.black);
		g2.setFont(MainGUI.Font_12);
		
		for (int r =0; r < numR; r++)
			for (int c = 0; c < numC; c++)
			{
				ThisPlate.getTheWells()[r][c].draw(g2);
			}
		
		if(TheRowLabels!=null)
		{
			
			
			g2.setColor(Color.black);
			for (int r = 0; r < numR; r++)
			{
				int x = (int)((ThisPlate.getTheWells()[r][0].getXpos()-TheRowLabels[r].length()*7f-25));
				int y = (int)(ThisPlate.getTheWells()[r][0].getYpos()+(float)ThisPlate.getTheWells()[r][0].getHeight());
				
				g2.drawString(TheRowLabels[r], x, y);
			}
			
			int order = isOrderedSequence(TheRowLabels);
			if(order!=0)
			{
				
				int x0 = ThisPlate.getTheWells()[0][0].getXpos()-5;
				int x1 = x0-15;
				int y0 = ThisPlate.getTheWells()[0][0].getYpos();
				int y1 = ThisPlate.getTheWells()[ThisPlate.getNumRows()-1][0].getYpos()+ThisPlate.getTheWells()[ThisPlate.getNumRows()-1][0].getHeight();
				
				
				if(order==1)
				{
					int[] xp = {x0, x0, x1};
					int[] yp = {y0, y1, y1};
					GradientPaint whiteToGray = new GradientPaint(x0, y0, Color.white, x0, y1, Color.gray);
					g2.setPaint(whiteToGray);
					g2.fillPolygon(xp, yp, 3);
					g2.setColor(Color.black);
					g2.drawPolygon(xp, yp,3);
				}
				else
				{
					int[] xp = {x0, x0, x1};
					int[] yp = {y1, y0, y0};
					GradientPaint whiteToGray = new GradientPaint(x0, y1, Color.white, x0, y0, Color.gray);
					g2.setPaint(whiteToGray);
					g2.fillPolygon(xp, yp, 3);
					g2.setColor(Color.black);
					g2.drawPolygon(xp, yp,3);
				}
				
			}
		}
		if (TheColLabels!=null)
		{
			g2.setColor(Color.black);
			for (int c = 0; c < numC; c++)
			{
				
				int x = (int)((ThisPlate.getTheWells()[0][c].getXpos())+((float)ThisPlate.getTheWells()[0][c].getWidth()/1.5));
				int y = ThisPlate.getTheWells()[0][c].getYpos()-25;
				
				Font oldFont = g2.getFont();
				Font f = oldFont.deriveFont(AffineTransform.getRotateInstance(-Math.PI / 2.0));
				g2.setFont(f);
				g2.drawString(TheColLabels[c], x, y);
				g2.setFont(oldFont);
			}
			
			int order = isOrderedSequence(TheColLabels);
			if(order!=0)
			{
				int x0 = ThisPlate.getTheWells()[0][0].getXpos();
				int x1 = ThisPlate.getTheWells()[0][ThisPlate.getNumColumns()-1].getXpos()+ThisPlate.getTheWells()[0][ThisPlate.getNumColumns()-1].getWidth();
				int y0 = ThisPlate.getTheWells()[0][0].getYpos()-5;
				int y1 = y0-15;
				
				
				if(order==-1)
				{
					int[] xp = {x1, x0, x0};
					int[] yp = {y0, y0, y1};
					GradientPaint whiteToGray = new GradientPaint(x1, y0, Color.white, x0, y0, Color.gray);
					g2.setPaint(whiteToGray);
					g2.fillPolygon(xp, yp, 3);
					g2.setColor(Color.black);
					g2.drawPolygon(xp, yp,3);
				}
				else
				{
					int[] xp = {x0, x1, x1};
					int[] yp = {y0, y0, y1};
					GradientPaint whiteToGray = new GradientPaint(x0, y0, Color.white, x1, y0, Color.gray);
					g2.setPaint(whiteToGray);
					g2.fillPolygon(xp, yp, 3);
					g2.setColor(Color.black);
					g2.drawPolygon(xp, yp,3);
				}
				
				
				
			}
		}
		
		
		g2.setFont(main.MainGUI.Font_12);
		g2.setColor(Color.white);
		
		
	}
	
	private int isOrderedSequence(String[] vals)
	{
		int len = vals.length;
		int counter = 0;
		float last = 0;
		int sIndex =0;
		int endIndex = len-1;
		float val = 0;
		//looking for first non-Blank value
		for (int i = 0; i < len; i++)
			if(!vals[i].equalsIgnoreCase(""))
			{
				last = Float.parseFloat(vals[i]);
				sIndex = i;
				break;
			}
		
		counter = 0;
		for (int i = sIndex; i < len; i++)
		{
			if(!vals[i].equalsIgnoreCase(""))
			{
				val = Float.parseFloat(vals[i]);
				
				if (val>last)
				{
					counter++;
					last = val;
				}
				else if (val < last)
				{
					counter--;
					last = val;
				}
			}
			else
			{
				endIndex = i;
				break;
			}
		}
		
		
		if (counter==(endIndex-sIndex-1))
			return 1;
		else if (counter==-(endIndex-sIndex-1))
			return -1;
		return 0;
	}
	
	
	
	
	
	private class GradientLabel extends JLabel
	{
		// ------------------------------ FIELDS ------------------------------
		
		private Color start;
		private Color end;
		
		// --------------------------- CONSTRUCTORS ---------------------------
		
		public GradientLabel( String text )
		{
			super( text );
			
			start = Color.LIGHT_GRAY;
			end = getBackground();
		}
		
		public GradientLabel( String text, Color start, Color end )
		{
			super( text );
			this.start = start;
			this.end = end;
		}
		
		// -------------------------- OTHER METHODS --------------------------
		
		public void paint( Graphics g )
		{
			int width = getWidth();
			int height = getHeight();
			
			// Create the gradient paint
			GradientPaint paint = new GradientPaint( 0, 0, start, width, height, end, true );
			
			// we need to cast to Graphics2D for this operation
			Graphics2D g2d = ( Graphics2D )g;
			
			// save the old paint
			Paint oldPaint = g2d.getPaint();
			
			// set the paint to use for this operation
			g2d.setPaint( paint );
			
			// fill the background using the paint
			g2d.fillRect( 0, 0, width, height );
			
			// restore the original paint
			g2d.setPaint( oldPaint );
			
			super.paint( g );
		}
	}
	
}

