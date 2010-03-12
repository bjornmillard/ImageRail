/**
 * CSVparser.java
 *
 * @author Created by Omnicore CodeGuide
 */

package tools;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import main.MainGUI;
import main.Plate;
import main.Well;
import midasGUI.Measurement;
import midasGUI.Treatment;
import tempObjects.Cell_RAM;
import tempObjects.Nucleus;

public class CSVparser
{
	private ArrayList allWells;
	private Well currentWell;
	private Well lastWell;
	private Cell_RAM currentCell;
	private boolean newWell;
	
	private int[] columnTypes;
	static final public int WELLNAME = 0;
	static final public int DATE = 1;
	static final public int DESCRIPTION = 2;
	static final public int TR = 3;
	static final public int DA = 4;
	static final public int DV = 5;
	
//	public void parseMidasFile_singleCells(File file, MainGUI TheGUI)
//	{
//		Plate plate = TheGUI.ThePlate;
//		Hashtable hashtable = new Hashtable();
//		newWell = false;
//		allWells = new ArrayList();
//		int counter = 0;
//		int NumHeaders = 0;
//		FileInputStream iStream = null;
//		BetterTokenizer theTokenizer = null;
//		int numDVs = 0;
//		int numDVsParsed = -1;
//		int numChannels = 0;
//		ArrayList thisWellsCells;
//
//		try
//		{
//			iStream = new FileInputStream(file);
//		}
//		catch (IOException e) {System.out.println("Error creating fileInputStream in the parseFile(file) method****"+e);}
//		if (iStream!=null)
//			theTokenizer = new BetterTokenizer(iStream);
//
//		theTokenizer.eolIsSignificant(true);
//
//
//		boolean ParsedHeaders = false;
//		ArrayList headers = new ArrayList();
//
//		String tok = " ";
//		while (true)
//		{
//			int type = -10000;
//			try
//			{
//				type = theTokenizer.nextToken();
//			}
//			catch (Exception e) {System.out.println("Error getting next Token: ");e.printStackTrace();break;}
//
//			if (type==-10000)
//			{
//				System.out.println("error!  Token type = "+type);
//			}
//
//			if (counter == 0 && ParsedHeaders)
//			{
//				//creating new cell
//				currentCell = new Cell();
//				Nucleus nuc =  new Nucleus(currentCell.ID);
//				currentCell.nucleus = nuc;
//				numChannels = numDVs/3;
//				currentCell.initNumChannels(numChannels);
//				currentCell.nucleus.initNumChannels(numChannels);
//				numDVsParsed = -1;
//				thisWellsCells = new ArrayList();
//			}
//
//			//now chekcing to see what type of token was parsed
//			String st = "null";
//			double num = -10000;
//			if (type == BetterTokenizer.TT_WORD)
//			{
//				//first we need to parse all the headers
//				st = theTokenizer.sval;
//
//				if (!ParsedHeaders)
//				{
//					headers.add(st);
//					NumHeaders++;
//				}
//
//				else if (ParsedHeaders)
//				{
//					st = st.trim();
//
//					switch (columnTypes[counter])
//					{
//						case WELLNAME:
//							{
//								currentWell = plate.getWell(st);
//								if (hashtable.get(currentWell)==null)
//								{
//									hashtable.put(currentWell, new ArrayList());
//									newWell = true;
//								}
//
//								break;
//							}
//						case DATE:
//							{
//								if (currentWell.date==null || currentWell.date=="")
//									currentWell.date = st;
//								break;
//							}
//						case DESCRIPTION:
//							{
//								if (currentWell.description==null || currentWell.description=="")
//									currentWell.description = st;
//								break;
//							}
//						case TR:
//							{
//								if (!newWell)
//									break;
//
//								if (currentWell.treatments==null)
//									currentWell.treatments = new ArrayList();
//								String name  = (String)headers.get(counter);
//								name = name.substring(3,name.length());
//								currentWell.treatments.add(new Treatment(name, Float.parseFloat((st.trim()))));
//
//								break;
//							}
//						case DA:
//							{
//								if (!newWell)
//									break;
//								if (currentWell.measurements==null)
//									currentWell.measurements = new ArrayList();
//								String name  = (String)headers.get(counter);
//								name = name.substring(3,name.length());
//								currentWell.measurements.add(new Measurement(name));
//								currentWell.measurementTime = st;
//								break;
//							}
//						case DV:
//							{
//								numDVsParsed++;
////								System.out.println("numDVsParsed: "+numDVsParsed);
//
//								if (numDVsParsed<numChannels)
//									break;
//								int channel = numDVsParsed%numChannels;
//
//
//								int compartment = 1;
//								if ((numDVsParsed)>=2*numChannels)
//									compartment = 2;
//
//								float val = Float.parseFloat(st);
////								System.out.println("Channel: "+channel +"  Comp:"+compartment +"  Val:"+val);
//
//								if (compartment == 1) //cytoplasm
//								{
//									currentCell.setChannelValue(val, channel, Cell.INTEGRATED);
//								}
//								else if (compartment == 2) //nucleus
//								{
//									currentCell.nucleus.setChannelValue(val, channel, Cell.INTEGRATED);
//								}
//
//
//								break;
//							}
//
//						default: System.out.println("Invalid type.");break;
//					}
//
//					counter++;
//
//
//				}
//			}
////			else if (type == BetterTokenizer.TT_NUMBER)
////			{
////				num = theTokenizer.nval;
////				System.out.println(counter+": "+num);
////				counter++;
////			}
//			else if (type == BetterTokenizer.TT_EOL)
//			{
////				System.out.println("EOL");
//				if (!ParsedHeaders)
//				{
//					ParsedHeaders = true;
//					int len = headers.size();
//					columnTypes = new int[len];
//					columnTypes[0] = WELLNAME;
//					columnTypes[1] = DATE;
//					columnTypes[2] = DESCRIPTION;
//					for (int i = 3; i < len; i++)
//					{
//						String head = (String)headers.get(i);
//						if (head.indexOf("TR:")>=0)
//							columnTypes[i] = TR;
//						else if (head.indexOf("DA:")>=0)
//							columnTypes[i] = DA;
//						else if (head.indexOf("DV:")>=0)
//							columnTypes[i] = DV;
//					}
//
//
//					numDVs = 0;
//					for (int i = 0; i < columnTypes.length; i++)
//					{
//						if(columnTypes[i]==DV)
//							numDVs++;
//					}
//				}
//				else
//				{
//					thisWellsCells = (ArrayList)hashtable.get(currentWell);
//					thisWellsCells.add(currentCell);
//				}
//				newWell = false;
//				counter = 0;
//
//			}
//			else if (type == BetterTokenizer.TT_EOF)
//			{
//				TheGUI.StoreCellsCheckBox.setSelected(true);
//				//now loading in all the cells to the wells from the hashtable
//				Enumeration keys = hashtable.keys();
//				while ( keys.hasMoreElements() )
//				{
//					Well well = (Well)keys.nextElement();
//					ArrayList arr = (ArrayList)hashtable.get( well );
//					int len  = arr.size();
//					Cell[] cells = new Cell[len];
//					for (int i = 0; i < len; i++)
//						cells[i] = (Cell)arr.get(i);
//
//					well.setCells(cells);
//				}
//				//geting wavelength names to put into GUI
//				ArrayList arr = new ArrayList();
//				for (int i = 0; i < headers.size(); i++)
//				{
//					String head = (String)headers.get(i);
//					int ind = head.indexOf("_w");
//					if (ind >=0)
//					{
//						String name = head.substring(ind+1, head.length());
//						boolean uniq = true;
//						for (int j = 0; j < arr.size(); j++)
//						{
//							if(((String)arr.get(j)).equalsIgnoreCase(name))
//							{
//								uniq = false;
//								break;
//							}
//						}
//						if (uniq)
//						{
//							arr.add(name);
//						}
//					}
//				}
//
//				int len = arr.size();
//				System.out.println("numWavelengths: "+len);
//
//				String[] channelsNames =  new String[len];
//				for (int i = 0; i < len; i++)
//					channelsNames[i] = (String)arr.get(i);
//
////				for (int i=0; i < channelsNames.length; i++)
////				{
////					TheGUI.WellChannel_radiobuttons[i].setEnabled(true);
////					TheGUI.WellChannel_radiobuttons[i].setVisible(true);
////					TheGUI.WellChannel_radiobuttons[i].setText(channelsNames[i]);
////					if (i==channelsNames.length-1)
////						TheGUI.WellChannel_radiobuttons[i].setSelected(true);
////				}
//
//
//				plate.updatePanel();
//
////				for (int i = 0; i < headers.size(); i++)
////					System.out.println((String)headers.get(i));
//
//				System.out.println("EOF");
//				break;
//			}
//			else if (type == BetterTokenizer.TT_NOTHING)
//			{
//				counter++;
//			}
//
//		}
//	}
	
	
	
//	public void parseMidasFile_template(File file, MainGUI TheGUI)
//	{
//		Plate plate = TheGUI.ThePlates;
//		newWell = false;
//		allWells = new ArrayList();
//		int counter = 0;
//		int NumHeaders = 0;
//		FileInputStream iStream = null;
//		BetterTokenizer theTokenizer = null;
//		int numDVs = 0;
//		int numDVsParsed = -1;
//		int numChannels = 0;
//
//		try
//		{
//			iStream = new FileInputStream(file);
//		}
//		catch (IOException e) {System.out.println("Error creating fileInputStream in the parseFile(file) method****"+e);}
//		if (iStream!=null)
//			theTokenizer = new BetterTokenizer(iStream);
//
//		theTokenizer.eolIsSignificant(true);
//
//
//		boolean ParsedHeaders = false;
//		ArrayList headers = new ArrayList();
//
//		String tok = " ";
//		while (true)
//		{
//			int type = -10000;
//			try
//			{
//				type = theTokenizer.nextToken();
//			}
//			catch (Exception e) {System.out.println("Error getting next Token: ");e.printStackTrace();break;}
//
//			if (type==-10000)
//			{
//				System.out.println("error!  Token type = "+type);
//			}
//
//			if (counter == 0 && ParsedHeaders)
//			{
//				numChannels = numDVs/3;
//				numDVsParsed = -1;
//			}
//
//			//now chekcing to see what type of token was parsed
//			String st = "null";
//			double num = -10000;
//			if (type == BetterTokenizer.TT_WORD)
//			{
//				//first we need to parse all the headers
//				st = theTokenizer.sval;
//
//				if (!ParsedHeaders)
//				{
//					headers.add(st);
//					NumHeaders++;
//				}
//
//				else if (ParsedHeaders)
//				{
//					st = st.trim();
//					System.out.println("st: "+st);
//					switch (columnTypes[counter])
//					{
//						case WELLNAME:
//							{
//								currentWell = plate.getWell(st);
//								break;
//							}
//						case DATE:
//							{
//								if (currentWell.date==null || currentWell.date=="")
//									currentWell.date = st;
//								break;
//							}
//						case DESCRIPTION:
//							{
//								if (currentWell.description==null || currentWell.description=="")
//									currentWell.description = st;
//								break;
//							}
//						case TR:
//							{
//								if (!newWell)
//									break;
//
//								if (currentWell.treatments==null)
//									currentWell.treatments = new ArrayList();
//								String name  = (String)headers.get(counter);
//								name = name.substring(3,name.length());
//								currentWell.treatments.add(new Treatment(name, Float.parseFloat((st.trim()))));
//
//								break;
//							}
//						case DA:
//							{
//								if (!newWell)
//									break;
//								if (currentWell.measurements==null)
//									currentWell.measurements = new ArrayList();
//								String name  = (String)headers.get(counter);
//								name = name.substring(3,name.length());
//								currentWell.measurements.add(new Measurement(name));
//								currentWell.measurementTime = st;
//								break;
//							}
//						case DV:
//							{
//								numDVsParsed++;
////								System.out.println("numDVsParsed: "+numDVsParsed);
////
////								if (numDVsParsed<numChannels)
////									break;
////								int channel = numDVsParsed%numChannels;
////
////
////								int compartment = 1;
////								if ((numDVsParsed)>=2*numChannels)
////									compartment = 2;
////
////								double val = Double.parseDouble(st);
////								System.out.println("Channel: "+channel +"  Comp:"+compartment +"  Val:"+val);
////
////								if (compartment == 1) //cytoplasm
////								{
////									currentCell.setChannelValue_cyto(val, channel);
////								}
////								else if (compartment == 2) //nucleus
////								{
////									currentWell.setChannelValue(val, channel);
////								}
//
//
//								break;
//							}
//
//						default: System.out.println("Invalid type.");break;
//					}
//
//					counter++;
//
//
//				}
//			}
//
//			else if (type == BetterTokenizer.TT_EOL)
//			{
//
//				if (!ParsedHeaders)
//				{
//					ParsedHeaders = true;
//					int len = headers.size();
//					columnTypes = new int[len];
//					columnTypes[0] = WELLNAME;
//					columnTypes[1] = DATE;
//					columnTypes[2] = DESCRIPTION;
//					for (int i = 3; i < len; i++)
//					{
//						String head = (String)headers.get(i);
//						if (head.indexOf("TR:")>=0)
//							columnTypes[i] = TR;
//						else if (head.indexOf("DA:")>=0)
//							columnTypes[i] = DA;
//						else if (head.indexOf("DV:")>=0)
//							columnTypes[i] = DV;
//					}
//
//
//					numDVs = 0;
//					for (int i = 0; i < columnTypes.length; i++)
//					{
//						if(columnTypes[i]==DV)
//							numDVs++;
//					}
//				}
//				else
//				{
//
//				}
//				newWell = false;
//				counter = 0;
//
//			}
//			else if (type == BetterTokenizer.TT_EOF)
//			{
//
//				//geting wavelength names to put into GUI
//				ArrayList arr = new ArrayList();
//				for (int i = 0; i < headers.size(); i++)
//				{
//					String head = (String)headers.get(i);
//					int ind = head.indexOf("_w");
//					if (ind >=0)
//					{
//						String name = head.substring(ind+1, head.length());
//						boolean uniq = true;
//						for (int j = 0; j < arr.size(); j++)
//						{
//							if(((String)arr.get(j)).equalsIgnoreCase(name))
//							{
//								uniq = false;
//								break;
//							}
//						}
//						if (uniq)
//						{
//							arr.add(name);
//						}
//					}
//				}
//
//				int len = arr.size();
//
//				String[] channelsNames =  new String[len];
//				for (int i = 0; i < len; i++)
//					channelsNames[i] = (String)arr.get(i);
//
//				plate.updatePanel();
//
//				for (int i = 0; i < headers.size(); i++)
//					System.out.println((String)headers.get(i));
//
//				System.out.println("EOF");
//				break;
//			}
//			else if (type == BetterTokenizer.TT_NOTHING)
//			{
//				counter++;
//			}
//
//
//
////			System.out.println("counter: "+columnCounter);
//
//		}
//
//	}
	
	
	static public float[][] parseCSVdataFile(File file)
	{
		
		FileInputStream iStream = null;
		BetterTokenizer theTokenizer = null;
		try
		{
			iStream = new FileInputStream(file.getCanonicalPath());
		}
		catch (IOException e) {System.out.println("Error creating fileInputStream in the parseFile(file) method****"+e);}
		if (iStream!=null)
			theTokenizer = new BetterTokenizer(iStream);
		
		theTokenizer.eolIsSignificant(true);
		ArrayList<ArrayList> data = new ArrayList<ArrayList>();
		
		
		//Start Parsing
		if (theTokenizer!=null)
		{
			String tok = " ";
			String startToken = "RLU";
			boolean foundStartToken = false;
			boolean Finished = false;
			int burnCounter = 0;
			int burns = 1;
			ArrayList currArr = null;
			boolean isFloat = false;
			while (!Finished)
			{
				int type = -10000;
				try
				{
					type = theTokenizer.nextToken();
				}
				catch (Exception e) {e.printStackTrace();break;}
				
				if (type==-10000)
				{
					System.out.println("Error parsing file****!");
					Finished = true;
				}
				
				//now chekcing to see what type of token was parsed
				String st = "null";
				double num = -10000;
				if (type == BetterTokenizer.TT_WORD)
				{
					//first we need to parse all the headers
					st = theTokenizer.sval;
					st = st.trim();
//					System.out.println(st);
					
					if (st.equalsIgnoreCase(startToken))
						foundStartToken = true;
					
					//try parsing string as float
					isFloat = true;
					if (burnCounter>burns)
					{
						try
						{
							Float.parseFloat(st);
//							System.out.println("---->  "+Float.parseFloat(st));
						}
						catch (Exception e)
						{
							isFloat=false;
						}
						if(isFloat)
						{
							
							float val = Float.parseFloat(st);
//							System.out.println("adding val:"+val+"=======");
							try
							{
								currArr.add(new Float(val));
							}
							catch (Exception e) {}
						}
					}
				}
				
				if (type == BetterTokenizer.TT_NUMBER)
				{
//
//					num = theTokenizer.nval;
//					System.out.println(num);
//
//					System.out.println("burnt: "+burntArow);
//					if (burntArow)
//					{
//						System.out.println("adding val:"+num);
//						currArr.add(new Float(num));
//					}
					
					
				}
				else if (type == BetterTokenizer.TT_EOL)
				{
//					System.out.println("EOL----|");
					
					if (burnCounter>burns)
					{
						data.add(new ArrayList<Float>());
						currArr =  data.get(data.size()-1);
					}
					else if(foundStartToken && (burnCounter<=burns))
						burnCounter++;
				}
				else if (type == BetterTokenizer.TT_EOF)
				{
					System.out.println("CSV Data File Load Complete***");
					Finished = true;
					
//					System.out.println("Rows: "+data.size());
				}
			}
			
			
			try
			{
				iStream.close();
			}
			catch (IOException e) {System.out.println("Error closing FileInputStream in parseFile() method****"+e);}
			theTokenizer = null;
		}
		
		
		//
		//Convert to array after parsed into ArrayList
		//
		int len = data.size();
		int max = 0;
		int count = 0;
		for (int i = 0; i < len; i++)
		{
			ArrayList arr = data.get(i);
			if (arr.size()>0)
			{
				count++;
				if (arr.size()>max)
					max = arr.size();
			}
		}
		System.out.println("number of nonZero rows = "+count);
		
		float[][] out = null;
		if (count>0)
		{
			out = new float[count][max];
			for (int i = 0; i < count; i++)
				for (int j = 0; j < max; j++)
					out[i][j] = 0;
			
			for (int i = 0; i < count; i++)
			{
				ArrayList arr = data.get(i);
				if (arr.size()>0)
					for (int j = 0; j < arr.size(); j++)
						out[i][j] = ((Float)arr.get(j)).floatValue();
			}
		}
		
		return out;
	}
	
	
	static public void parseDistribution(File file)
	{
		int counter = 0;
		int NumHeaders = 0;
		FileInputStream iStream = null;
		BetterTokenizer theTokenizer = null;
		
		try
		{
			iStream = new FileInputStream(file);
		}
		catch (IOException e) {System.out.println("Error creating fileInputStream in the parseFile(file) method****"+e);}
		if (iStream!=null)
			theTokenizer = new BetterTokenizer(iStream);
		
		theTokenizer.eolIsSignificant(true);
		
		double number = 0;
		Point.Double point = null;
		counter =0;
		ArrayList points = new ArrayList();
		
		String tok = " ";
		while (true)
		{
			int type = -10000;
			try
			{
				type = theTokenizer.nextToken();
			}
			catch (Exception e) {System.out.println("Error getting next Token: ");e.printStackTrace();break;}
			
			if (type==-10000)
			{
				System.out.println("error!  Token type = "+type);
			}
			
			if (type == BetterTokenizer.TT_WORD)
			{
				String val = theTokenizer.sval;
//				System.out.println("val: "+val);
				point.y = Double.parseDouble(val);
				points.add(point);
				System.out.println("y:"+val+"");
				counter++;
			}
			
			if (type == BetterTokenizer.TT_NUMBER)
			{
				number = theTokenizer.nval;
				
				point = new Point.Double();
				point.x = number;
				System.out.print("x:"+number+"  ");
			}
			else if (type == BetterTokenizer.TT_EOF)
			{
				System.out.println("EOF");
				System.out.println("num Points: "+points.size());
				break;
			}
//			System.out.println("counter: "+counter);
		}
		
		
		int len = points.size();
		double tot = 0;
		double x = 0;
		double y = 0;
		double xy = 0;
		double x2 = 0;
		double y2 = 0;
//		double xMin = Double.POSITIVE_INFINITY;
//		double xMax = Double.NEGATIVE_INFINITY;
//		double yMin = Double.POSITIVE_INFINITY;
//		double yMax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < len; i++)
		{
			Point.Double p = (Point.Double)points.get(i);
			//regression stats
			double xp = p.x;
			double yp = p.y;
			
			tot++;
			x+=xp;
			y+=yp;
			xy+=(xp*yp);
			x2+=(xp*xp);
			y2+=(yp*yp);
		}
		
//		double m = (tot*xy-x*y)/(tot*x2-x*x);
//		double b = (x2*y-x*xy)/(x2*tot-x*x);
		
		
		double iTot = (1d/(double)tot);
		double B = 0.5d*((y2-(iTot*(y*y)))-(x2-iTot*x*x))/(iTot*x*y-xy);
		double mPlus = -B + Math.sqrt(B*B+1);
		double mMinus = -B - Math.sqrt(B*B+1);
		
		double x0 = x/tot;
		double y0 = y/tot;
		
		double bPlus = (y0-mPlus*x0);
		double bMinus = (y0-mMinus*x0);
		
		double varPlus = 0;
		double varMinus = 0;
		
		//computing the variance from line:
		for (int i = 0; i < len; i++)
		{
			Point.Double p = (Point.Double)points.get(i);
			//regression stats
			double xp = p.x;
			double yp = p.y;
			
			
			double xMin = (-xp+mPlus*yp-mPlus*bPlus)/(mPlus*mPlus-1);
			varPlus+=Math.abs(xMin-xp);
			
			double xMin2 = (-xp+mMinus*yp-mMinus*bMinus)/(mMinus*mMinus-1);
			varMinus+=Math.abs(xMin2-xp);
		}
		
		System.out.println("p: "+varPlus +"    m: "+varMinus);
		double m = 0;
		double b = 0;
		if (varPlus<=varMinus)
		{
			m = mPlus;
			b = bPlus;
		}
		else
		{
			m = mMinus;
			b = bMinus;
		}
		
		
		System.out.println("y = "+m+"(x) + "+b);
		
		double ang = (180d*Math.atan(m))/Math.PI;
		
		System.out.println("angle: "+ ang);
		
		
		System.exit(0);
	}
}

