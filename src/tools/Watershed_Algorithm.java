/**
This method was adapted from a Watershed Algorithm from the NIH's ImageJ software (http://rsbweb.nih.gov/ij) 
 */

package tools;


import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import segmentors.DefaultSegmentor.Pixel;


public class Watershed_Algorithm
{
	private int nucleusThreshold;
	final static int HMIN = 0;
	final static int HMAX = 256;
	
	public void run(float[][][] iraster, int[][][] raster, int nucleusThreshold, int index)
	{
		byte[] pixels = Pixel.convertToByteArray(iraster);
		
		/** First step : the pixels are sorted according to increasing grey values **/
		WatershedStructure watershedStructure = new WatershedStructure(pixels, raster[0].length  ,raster.length );
		
		/** Start flooding **/
		WatershedFIFO queue = new WatershedFIFO();
		int curlab = 0;
		
		int heightIndex1 = 0;
		int heightIndex2 = 0;
		
		for(int h=HMIN; h<HMAX; h++)
		{
			for(int pixelIndex = heightIndex1 ; pixelIndex<watershedStructure.size() ; pixelIndex++) /*mask all pixels at level h*/
			{
				//Scrolling through the sorted height values till we get to the current Height
				WatershedPixel p = watershedStructure.get(pixelIndex);
				if(p.getIntHeight() != h)
				{
					// This pixel is at level h+1
					heightIndex1 = pixelIndex;
					break;
				}
				
				//This pixel is at level H... so lets work with it
				p.setLabelToMASK();
				
				Vector neighbours = p.getNeighbours();
				for(int i = 0 ; i < neighbours.size() ; i++)
				{
					WatershedPixel q = (WatershedPixel) neighbours.get(i);
					//Initialise queue with neighbours at level h of current basins or watersheds
					if(q.getLabel()>=0)
					{
						p.setDistance(1);
						queue.fifo_add(p);
						break;
					}
				}
			}
			
			
			int curdist = 1;
			queue.fifo_add_FICTITIOUS();
			
			//
			// Iteratively extending basins
			//
			while(true)
			{
				WatershedPixel p = queue.fifo_remove();
				if(p.isFICTITIOUS())
					if(queue.fifo_empty())
						break;
					else
					{
						queue.fifo_add_FICTITIOUS();
						curdist++;
						p = queue.fifo_remove();
					}
				
				
				/* Labelling p by inspecting neighbours */
				Vector neighbours = p.getNeighbours();
				for(int i=0 ; i<neighbours.size() ; i++)
				{
					WatershedPixel q = (WatershedPixel) neighbours.get(i);
					if( (q.getDistance() <= curdist) && (q.getLabel()>=0) )
					{
						/* q belongs to an existing basin or to a watershed */
						if(q.getLabel() > 0)
						{
							if( p.isLabelMASK() )
								p.setLabel(q.getLabel());
							else
								if(p.getLabel() != q.getLabel())
									p.setLabelToWSHED();
						}
						else
							if(p.isLabelMASK())
								p.setLabelToWSHED();
					}
					else
						if(q.isLabelMASK() && (q.getDistance() == 0) )
						{
							q.setDistance( curdist+1 );
							queue.fifo_add( q );
						}
				}
			}
			
			//
			// Detect and process new minima at level h
			//
			for(int pixelIndex = heightIndex2 ; pixelIndex<watershedStructure.size() ; pixelIndex++)
			{
				WatershedPixel p = watershedStructure.get(pixelIndex);
				
				/** This pixel is at level h+1 **/
				if(p.getIntHeight() != h)
				{
					heightIndex2 = pixelIndex;
					break;
				}
				
				// Reset distance to zero
				p.setDistance(0);
				// the pixel is inside a new minimum
				if(p.isLabelMASK())
				{
					curlab++;
					p.setLabel(curlab);
					queue.fifo_add(p);
					
					while(!queue.fifo_empty())
					{
						WatershedPixel q = queue.fifo_remove();
						Vector neighbours = q.getNeighbours();
						
						/* inspect neighbours of p2*/
						for(int i=0 ; i<neighbours.size() ; i++)
						{
							WatershedPixel r = (WatershedPixel) neighbours.get(i);
							
							if( r.isLabelMASK() )
							{
								r.setLabel(curlab);
								queue.fifo_add(r);
							}
						}
					}
				}
			}
		}
		
		float[][][] newPixels = new float[raster.length][raster[0].length][1];
		for(int pixelIndex = 0 ; pixelIndex<watershedStructure.size(); pixelIndex++)
		{
			WatershedPixel p = watershedStructure.get(pixelIndex);
			if(p.isLabelWSHED() && !p.allNeighboursAreWSHED())
				newPixels[p.getY()][p.getX()][0] = 0f;
			else
				if (raster[p.getY()][p.getX()][index]>nucleusThreshold)
					newPixels[p.getY()][p.getX()][0] = 1f;//raster[p.getY()][p.getX()][index];
		}
		tools.ImageTools.displayRaster(newPixels);
		
	}
	
	
	
	public void run(int[][][] raster)
	{
		byte[] pixels = Pixel.convertToByteArray(raster);
		
		/** First step : the pixels are sorted according to increasing grey values **/
		WatershedStructure watershedStructure = new WatershedStructure(pixels, raster[0].length  ,raster.length );
		
		/** Start flooding **/
		WatershedFIFO queue = new WatershedFIFO();
		int curlab = 0;
		
		int heightIndex1 = 0;
		int heightIndex2 = 0;
		
		for(int h=HMIN; h<HMAX; h++)
		{
			for(int pixelIndex = heightIndex1 ; pixelIndex<watershedStructure.size() ; pixelIndex++) /*mask all pixels at level h*/
			{
				//Scrolling through the sorted height values till we get to the current Height
				WatershedPixel p = watershedStructure.get(pixelIndex);
				if(p.getIntHeight() != h)
				{
					// This pixel is at level h+1
					heightIndex1 = pixelIndex;
					break;
				}
				
				//This pixel is at level H... so lets work with it
				p.setLabelToMASK();
				
				Vector neighbours = p.getNeighbours();
				for(int i = 0 ; i < neighbours.size() ; i++)
				{
					WatershedPixel q = (WatershedPixel) neighbours.get(i);
					//Initialise queue with neighbours at level h of current basins or watersheds
					if(q.getLabel()>=0)
					{
						p.setDistance(1);
						queue.fifo_add(p);
						break;
					}
				}
			}
			
			
			int curdist = 1;
			queue.fifo_add_FICTITIOUS();
			
			//
			// Iteratively extending basins
			//
			while(true)
			{
				WatershedPixel p = queue.fifo_remove();
				if(p.isFICTITIOUS())
					if(queue.fifo_empty())
						break;
					else
					{
						queue.fifo_add_FICTITIOUS();
						curdist++;
						p = queue.fifo_remove();
					}
				
				
				/* Labelling p by inspecting neighbours */
				Vector neighbours = p.getNeighbours();
				for(int i=0 ; i<neighbours.size() ; i++)
				{
					WatershedPixel q = (WatershedPixel) neighbours.get(i);
					if( (q.getDistance() <= curdist) && (q.getLabel()>=0) )
					{
						/* q belongs to an existing basin or to a watershed */
						if(q.getLabel() > 0)
						{
							if( p.isLabelMASK() )
								p.setLabel(q.getLabel());
							else
								if(p.getLabel() != q.getLabel())
									p.setLabelToWSHED();
						}
						else
							if(p.isLabelMASK())
								p.setLabelToWSHED();
					}
					else
						if(q.isLabelMASK() && (q.getDistance() == 0) )
						{
							q.setDistance( curdist+1 );
							queue.fifo_add( q );
						}
				}
			}
			
			//
			// Detect and process new minima at level h
			//
			for(int pixelIndex = heightIndex2 ; pixelIndex<watershedStructure.size() ; pixelIndex++)
			{
				WatershedPixel p = watershedStructure.get(pixelIndex);
				
				/** This pixel is at level h+1 **/
				if(p.getIntHeight() != h)
				{
					heightIndex2 = pixelIndex;
					break;
				}
				
				// Reset distance to zero
				p.setDistance(0);
				// the pixel is inside a new minimum
				if(p.isLabelMASK())
				{
					curlab++;
					p.setLabel(curlab);
					queue.fifo_add(p);
					
					while(!queue.fifo_empty())
					{
						WatershedPixel q = queue.fifo_remove();
						Vector neighbours = q.getNeighbours();
						
						/* inspect neighbours of p2*/
						for(int i=0 ; i<neighbours.size() ; i++)
						{
							WatershedPixel r = (WatershedPixel) neighbours.get(i);
							
							if( r.isLabelMASK() )
							{
								r.setLabel(curlab);
								queue.fifo_add(r);
							}
						}
					}
				}
			}
		}
		
		float[][][] newPixels = new float[raster.length][raster[0].length][1];
		for(int pixelIndex = 0 ; pixelIndex<watershedStructure.size(); pixelIndex++)
		{
			WatershedPixel p = watershedStructure.get(pixelIndex);
			if(p.isLabelWSHED() && !p.allNeighboursAreWSHED())
				newPixels[p.getY()][p.getX()][0] = 255f;
			else
				newPixels[p.getY()][p.getX()][0] = 255-raster[p.getY()][p.getX()][0];
		}
		tools.ImageTools.displayRaster(newPixels);
		
	}
	
	
	public class WatershedFIFO
	{
		private LinkedList watershedFIFO;
		
		public WatershedFIFO()
		{
			watershedFIFO = new LinkedList();
		}
		
		public void fifo_add(WatershedPixel p)
		{
			watershedFIFO.addFirst(p);
		}
		
		public WatershedPixel fifo_remove()
		{
			return (WatershedPixel) watershedFIFO.removeLast();
		}
		
		public boolean fifo_empty()
		{
			return watershedFIFO.isEmpty();
		}
		
		public void fifo_add_FICTITIOUS()
		{
			watershedFIFO.addFirst(new WatershedPixel());
		}
		
		
	}
	
	
	public class WatershedPixel implements Comparable
	{
		/** Value used to initialise the image */
		final static int INIT = -1;
		/** Value used to indicate the new pixels that
		 *  are going to be processed (intial value
		 *  at each level)
		 **/
		final static int MASK = -2;
		/** Value indicating that the pixel belongs
		 *  to a watershed.
		 **/
		final static int WSHED = 0;
		/** Fictitious pixel **/
		final static int FICTITIOUS = -3;
		
		/** x coordinate of the pixel **/
		private int x;
		/** y coordinate of the pixel **/
		private int y;
		/** grayscale value of the pixel **/
		private byte height;
		/** Label used in the Watershed immersion algorithm **/
		private int label;
		/** Distance used for working on pixels */
		private int dist;
		
		/** Neighbours **/
		private Vector neighbours;
		
		public WatershedPixel(int x, int y, byte height)
		{
			this.x = x;
			this.y = y;
			this.height = height;
			label = INIT;
			dist = 0;
			neighbours = new Vector(8);
		}
		
		public WatershedPixel()
		{
			label = FICTITIOUS;
		}
		
		public void addNeighbour(WatershedPixel neighbour)
		{
			/*IJ.write("In Pixel, adding :");
			 IJ.write(""+neighbour);
			 IJ.write("Add done");
			 */
			neighbours.add(neighbour);
		}
		
		public Vector getNeighbours()
		{
			return neighbours;
		}
		
		
		
		public String toString()
		{
			return new String("("+x+","+y+"), height : "+getIntHeight()+", label : "+label+", distance : "+dist);
		}
		
		
		
		public final byte getHeight()
		{
			return height;
		}
		
		public final int getIntHeight()
		{
			return (int) height&0xff;
		}
		
		public final int getX()
		{
			return x;
		}
		
		public final int getY()
		{
			return y;
		}
		
		
		/** Method to be able to use the Collections.sort static method. **/
		public int compareTo(Object o)
		{
			if(!(o instanceof WatershedPixel))
				throw new ClassCastException();
			
			WatershedPixel obj =  (WatershedPixel) o;
			
			if( obj.getIntHeight() < getIntHeight() )
				return 1;
			
			if( obj.getIntHeight() > getIntHeight() )
				return -1;
			
			return 0;
		}
		
		public void setLabel(int label)
		{
			this.label = label;
		}
		
		public void setLabelToINIT()
		{
			label = INIT;
		}
		
		public void setLabelToMASK()
		{
			label = MASK;
		}
		
		public void setLabelToWSHED()
		{
			label = WSHED;
		}
		
		
		public boolean isLabelINIT()
		{
			return label == INIT;
		}
		public boolean isLabelMASK()
		{
			return label == MASK;
		}
		public boolean isLabelWSHED()
		{
			return label == WSHED;
		}
		
		public int getLabel()
		{
			return label;
		}
		
		public void setDistance(int distance)
		{
			dist = distance;
		}
		
		public int getDistance()
		{
			return dist;
		}
		
		public boolean isFICTITIOUS()
		{
			return label == FICTITIOUS;
		}
		
		public boolean allNeighboursAreWSHED()
		{
			for(int i=0 ; i<neighbours.size() ; i++)
			{
				WatershedPixel r = (WatershedPixel) neighbours.get(i);
				
				if( !r.isLabelWSHED() )
					return false;
			}
			return true;
		}
		
	}
	
	
	
	public class WatershedStructure
	{
		private Vector watershedStructure;
		
		public WatershedStructure(byte[] im, int width_, int height_)
		{
//		System.out.println("v: "+v);
//		System.out.println("e: "+e);
			byte[] pixels = im;
			Rectangle r = new Rectangle(width_,height_);
			int width = width_;
			int offset, topOffset, bottomOffset, i;
			
			watershedStructure = new Vector(r.width*r.height);
			
			/** The structure is filled with the pixels of the image. **/
			for (int y=r.y; y<(r.y+r.height); y++)
			{
				offset = y*width;
				
				for (int x=r.x; x<(r.x+r.width); x++)
				{
					i = offset + x;
					
					int indiceY = y-r.y;
					int indiceX = x-r.x;
					
					watershedStructure.add(new WatershedPixel(indiceX, indiceY, pixels[i]));
				}
			}
			
			/** The WatershedPixels are then filled with the reference to their neighbours. **/
			for (int y=0; y<r.height; y++)
			{
				
				offset = y*width;
				topOffset = offset+width;
				bottomOffset = offset-width;
				
				
				for (int x=0; x<r.width; x++)
				{
					WatershedPixel currentPixel = (WatershedPixel)watershedStructure.get(x+offset);
					
					if(x+1<r.width)
					{
						currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+offset));
						
						if(y-1>=0)
							currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+bottomOffset));
						
						if(y+1<r.height)
							currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+topOffset));
					}
					
					if(x-1>=0)
					{
						currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+offset));
						
						if(y-1>=0)
							currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+bottomOffset));
						
						if(y+1<r.height)
							currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+topOffset));
					}
					
					if(y-1>=0)
						currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+bottomOffset));
					
					if(y+1<r.height)
						currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+topOffset));
				}
			}
			
			Collections.sort(watershedStructure);
			//IJ.showProgress(0.8);
		}
		
		
		public int size()
		{
			return watershedStructure.size();
		}
		
		public WatershedPixel get(int i)
		{
			return (WatershedPixel) watershedStructure.get(i);
		}
	}
	
}

