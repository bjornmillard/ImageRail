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

package tools;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import tempObjects.Cell_RAM;

public class DelaunayTriangulator
{
	public final static double EPSILON = 0.000001;
	
	/*
	 Return TRUE if a point (xp,yp) is inside the circumcircle made up
	 of the points (x1,y1), (x2,y2), (x3,y3)
	 The circumcircle centre is returned in (xc,yc) and the radius r
	 NOTE: A point on the edge is inside the circumcircle
	 */
	static public boolean circumCircle(
		double xp, double yp,
		double x1, double y1,
		double x2, double y2,
		double x3, double y3,
		/*double xc, double yc, double r*/
		Point3d circle
	)
	{
		double m1,m2,mx1,mx2,my1,my2;
		double dx,dy,rsqr,drsqr;
		double xc, yc, r;
		
		/* Check for coincident points */
		if ( Math.abs(y1-y2) < EPSILON && Math.abs(y2-y3) < EPSILON )
		{
//			System.out.println("y1 "+y1);
//			System.out.println("y2 "+y2);
//			System.out.println("y3 "+y3);
//
//			System.out.println("CircumCircle: Points are coincident.");
			return false;
		}
		
		if ( Math.abs(y2-y1) < EPSILON )
		{
			m2 = - (x3-x2) / (y3-y2);
			mx2 = (x2 + x3) / 2.0;
			my2 = (y2 + y3) / 2.0;
			xc = (x2 + x1) / 2.0;
			yc = m2 * (xc - mx2) + my2;
			
		}
		else if ( Math.abs(y3-y2) < EPSILON )
		{
			m1 = - (x2-x1) / (y2-y1);
			mx1 = (x1 + x2) / 2.0;
			my1 = (y1 + y2) / 2.0;
			xc = (x3 + x2) / 2.0;
			yc = m1 * (xc - mx1) + my1;
		}
		else
		{
			m1 = - (x2-x1) / (y2-y1);
			m2 = - (x3-x2) / (y3-y2);
			mx1 = (x1 + x2) / 2.0;
			mx2 = (x2 + x3) / 2.0;
			my1 = (y1 + y2) / 2.0;
			my2 = (y2 + y3) / 2.0;
			xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
			yc = m1 * (xc - mx1) + my1;
		}
		
		dx = x2 - xc;
		dy = y2 - yc;
		rsqr = dx*dx + dy*dy;
		r = Math.sqrt(rsqr);
		
		dx = xp - xc;
		dy = yp - yc;
		drsqr = dx*dx + dy*dy;
		
		circle.x = xc;
		circle.y = yc;
		circle.z = r;
		
		return ( drsqr <= rsqr ? true : false );
	}
	
	/*
	 Triangulation subroutine
	 Takes as input NV vertices in array pxyz
	 Returned is a list of ntri triangular faces in the array v
	 These triangles are arranged in a consistent clockwise order.
	 The triangle array 'v' should be malloced to 3 * nv
	 The vertex array pxyz must be big enough to hold 3 more points
	 The vertex array must be sorted in increasing x values say
	 
	 qsort(p,nv,sizeof(XYZ),XYZCompare);
	 
	 int XYZCompare(void *v1,void *v2)
	 {
	 XYZ *p1,*p2;
	 p1 = v1;
	 p2 = v2;
	 if (p1->x < p2->x)
	 return(-1);
	 else if (p1->x > p2->x)
	 return(1);
	 else
	 return(0);
	 }
	 */
	
	/**
	 * Method triangulate
	 *
	 * @param    numPoints           an int
	 * @param    points              a  Point3d[]
	 * @param    triangles           a  Triangle[]
	 *
	 * @return   an int
	 *
	 */
	public int triangulate (int numPoints, Point2D.Double[] points, Triangle[] triangles )
	{
		boolean complete[] 		= null;
		Edge 	edges[] 		= null;
		int 	nedge 			= 0;
		int 	trimax, emax 	= 200;
		int 	status 			= 0;
		
		boolean	inside;
		//int 	i, j, k;
		double 	xp, yp, x1, y1, x2, y2, x3, y3, xc, yc, r;
		double 	xmin, xmax, ymin, ymax, xmid, ymid;
		double 	dx, dy, dmax;
		
		int		ntri			= 0;
		
		/* Allocate memory for the completeness list, flag for each triangle */
		trimax = 4*numPoints;
		complete = new boolean[trimax];
		for (int ic=0; ic<trimax; ic++)
			complete[ic] = false;
		
		/* Allocate memory for the edge list */
		edges = new Edge[emax];
		for (int ie=0; ie<emax; ie++)
			edges[ie] = new Edge();
		
		/*
		 Find the maximum and minimum vertex bounds.
		 This is to allow calculation of the bounding triangle
		 */
		xmin = points[0].x;
		ymin = points[0].y;
		xmax = xmin;
		ymax = ymin;
		for (int i=1;i<numPoints;i++)
		{
			if (points[i].x < xmin) xmin = points[i].x;
			if (points[i].x > xmax) xmax = points[i].x;
			if (points[i].y < ymin) ymin = points[i].y;
			if (points[i].y > ymax) ymax = points[i].y;
		}
		dx = xmax - xmin;
		dy = ymax - ymin;
		dmax = (dx > dy) ? dx : dy;
		xmid = (xmax + xmin) / 2.0;
		ymid = (ymax + ymin) / 2.0;
		
		/*
		 Set up the supertriangle
		 This is a triangle which encompasses all the sample points.
		 The supertriangle coordinates are added to the end of the
		 vertex list. The supertriangle is the first triangle in
		 the triangle list.
		 */
		points[numPoints+0].x = (int)(xmid - 2.0 * dmax);
		points[numPoints+0].y = (int)(ymid - dmax);
		
		points[numPoints+1].x = (int)(xmid);
		points[numPoints+1].y = (int)(ymid + 2.0 * dmax);
		
		points[numPoints+2].x = (int)(xmid + 2.0 * dmax);
		points[numPoints+2].y = (int)(ymid - dmax);
		
		triangles[0].p1 = numPoints;
		triangles[0].p2 = numPoints+1;
		triangles[0].p3 = numPoints+2;
		complete[0] = false;
		ntri = 1;
		
		
		/*
		 Include each point one at a time into the existing mesh
		 */
		
		for (int i=0;i<numPoints;i++)
		{
			
			xp = points[i].x;
			yp = points[i].y;
			nedge = 0;
			
			
			/*
			 Set up the edge buffer.
			 If the point (xp,yp) lies inside the circumcircle then the
			 three edges of that triangle are added to the edge buffer
			 and that triangle is removed.
			 */
			Point3d circle = new Point3d();
			for (int j=0;j<ntri;j++)
			{
				if (complete[j])
					continue;
				x1 = points[(int)triangles[j].p1].x;
				y1 = points[(int)triangles[j].p1].y;
				x2 = points[(int)triangles[j].p2].x;
				y2 = points[(int)triangles[j].p2].y;
				x3 = points[(int)triangles[j].p3].x;
				y3 = points[(int)triangles[j].p3].y;
				inside = circumCircle( xp, yp,  x1, y1,  x2, y2,  x3, y3,  circle );
				
				xc = circle.x; yc = circle.y; r = circle.z;
				if (xc + r < xp) {complete[j] = true;}
				if (inside)
				{
					/* Check that we haven't exceeded the edge list size */
					if (nedge+3 >= emax)
					{
						emax += 100;
						Edge[] edges_n = new Edge[emax];
						for (int ie=0; ie<emax; ie++) edges_n[ie] = new Edge();
						System.arraycopy(edges, 0, edges_n, 0, edges.length);
						edges = edges_n;
					}
					edges[nedge+0].p1 = triangles[j].p1;
					edges[nedge+0].p2 = triangles[j].p2;
					edges[nedge+1].p1 = triangles[j].p2;
					edges[nedge+1].p2 = triangles[j].p3;
					edges[nedge+2].p1 = triangles[j].p3;
					edges[nedge+2].p2 = triangles[j].p1;
					nedge += 3;
					triangles[j].p1 = triangles[ntri-1].p1;
					triangles[j].p2 = triangles[ntri-1].p2;
					triangles[j].p3 = triangles[ntri-1].p3;
					complete[j] = complete[ntri-1];
					ntri--;
					j--;
				}
			}
			
			/*
			 Tag multiple edges
			 Note: if all triangles are specified anticlockwise then all
			 interior edges are opposite pointing in direction.
			 */
			for (int j=0;j<nedge-1;j++)
			{
				//if ( !(edges[j].p1 < 0 && edges[j].p2 < 0) )
				for (int k=j+1;k<nedge;k++)
				{
					if ((edges[j].p1 == edges[k].p2) && (edges[j].p2 == edges[k].p1))
					{
						edges[j].p1 = -1;
						edges[j].p2 = -1;
						edges[k].p1 = -1;
						edges[k].p2 = -1;
					}
					/* Shouldn't need the following, see note above */
					if ((edges[j].p1 == edges[k].p1) && (edges[j].p2 == edges[k].p2))
					{
						edges[j].p1 = -1;
						edges[j].p2 = -1;
						edges[k].p1 = -1;
						edges[k].p2 = -1;
					}
				}
			}
			
			/*
			 Form new triangles for the current point
			 Skipping over any tagged edges.
			 All edges are arranged in clockwise order.
			 */
			for (int j=0;j<nedge;j++)
			{
				if (edges[j].p1 == -1 || edges[j].p2 == -1)
					continue;
				if (ntri >= trimax) return -1;
				triangles[ntri].p1 = edges[j].p1;
				triangles[ntri].p2 = edges[j].p2;
				triangles[ntri].p3 = i;
				complete[ntri] = false;
				ntri++;
			}
		}
		
		
		/*
		 Remove triangles with supertriangle vertices
		 These are triangles which have a vertex number greater than nv
		 */
		for (int i=0;i<ntri;i++)
		{
			if (triangles[i].p1 >= numPoints || triangles[i].p2 >= numPoints || triangles[i].p3 >= numPoints)
			{
				triangles[i] = triangles[ntri-1];
				ntri--;
				i--;
			}
		}
		
		return ntri;
	}
	
	/** Takes in the points, computes the triangulation, then returns a set of lines that can then be drawn to
	 * show the trianguation
	 * @author BLM*/
	public Triangle[] getTriangulation(Point2D.Double[] points, int frameWidth, int frameHeight)
	{
		//sorting the points according to their xValue
		Arrays.sort(points, new tools.xPointSorter());
		int numPoints = points.length;
		
		//Need to create 3 dummy points on the end to create a superTriangle
		Point2D.Double[] temp = new Point2D.Double[numPoints+3];
		for (int i=0; i<points.length; i++)
			temp[i] = points[i];
		
//		int widthSpaceing = (frameWidth+100)/10;
//		int heightSpaceing = (frameHeight+100)/10;
//
//		for (int i =0; i < 10; i++)
//			temp[temp.length-40+i] = new Point2D.Double(-50, -50+i*heightSpaceing);
//		for (int i =0; i < 10; i++)
//			temp[temp.length-30+i] = new Point2D.Double(-50+i*widthSpaceing, -50);
//		for (int i =0; i < 10; i++)
//			temp[temp.length-20+i] = new Point2D.Double(frameWidth+50, -50+i*heightSpaceing);
//		for (int i =0; i < 10; i++)
//			temp[temp.length-10+i] = new Point2D.Double(-50+i*widthSpaceing, frameHeight);
		
		temp[temp.length-3] = new Point2D.Double(-50, -50);
		temp[temp.length-2] = new Point2D.Double(frameHeight+50, frameWidth/2);
		temp[temp.length-1] = new Point2D.Double(-50, frameWidth+50);
		points = temp;
		
		Triangle[]	 triangles 	= new Triangle[numPoints*3];
		for (int i=0; i<triangles.length; i++)
			triangles[i] = new Triangle();
		
		//Triangulating
		int numTriangles = triangulate(numPoints, points, triangles );
		numTriangles = triangles.length;
		//The triangulation above just puts the indices of the points in the triangles... now putting the actual points for later
		for (int i =0; i < numTriangles; i++)
		{
			Triangle tri = triangles[i];
			tri.points = new Point2D.Double[3];
			tri.points[0] = points[(int)tri.p1];
			tri.points[1] = points[(int)tri.p2];
			tri.points[2] = points[(int)tri.p3];
		}
		return triangles;
		
	}
	
	/** Takes in the points, computes the triangulation, then returns a set of lines that can then be drawn to
	 * show the trianguation
	 * @author BLM*/
	static public Line2D.Double[] getTriangulationLines(Triangle[] triangles)
	{
		int numTriangles = triangles.length;
		
		//creating the lines
		Line2D.Double[] lines = new Line2D.Double[numTriangles*3];
		int counter = 0;
		for (int i= 0; i < numTriangles; i++)
		{
			Triangle tri = triangles[i];
			Point2D.Double p1 = tri.points[0];
			Point2D.Double p2 = tri.points[1];
			Point2D.Double p3 = tri.points[2];
			
			lines[counter] = new Line2D.Double(p1, p2);
			counter++;
			lines[counter] = new Line2D.Double(p2, p3);
			counter++;
			lines[counter] = new Line2D.Double(p3, p1);
			counter++;
			
		}
		
		return lines;
		
	}
	
	public Line2D.Double[] assignNeighbors(Cell_RAM[] cells, int frameWidth, int frameHeight)
	{
		if (cells==null || cells.length==0)
			return null;
		//
		//	Finding Neighbor Cells
		//
		long start= System.currentTimeMillis();
		Triangle[] triangles;
		Point2D.Double[] points;
		
		int numPoints = cells.length;
		points = new Point2D.Double[numPoints];
		for (int i =0; i < numPoints; i++)
			points[i] = cells[i].getNucleus().getCentroid();
		
		//adding points along the edge of the image frame to help with boundary effects
		int numEx = 40; //per side
		Point2D.Double[] points_new = new Point2D.Double[numPoints+numEx*4];
		for (int i =0; i < numPoints; i++)
			points_new[i]  = points[i];
		
		//top points & bottom points
		int incW = (10+frameWidth)/numEx;
		int counter = 0;
		for (int i =0; i < numEx; i++)
		{
			points_new[numPoints+counter] = new Point2D.Double(i*incW, -5);
			counter++;
			points_new[numPoints+counter] = new Point2D.Double(i*incW, frameHeight+5);
			counter++;
		}
		//lateral points
		int incH = (10+frameHeight)/numEx;
		for (int i =0; i < numEx; i++)
		{
			points_new[numPoints+counter] = new Point2D.Double(-5, i*incH);
			counter++;
			points_new[numPoints+counter] = new Point2D.Double(frameHeight+5, i*incW);
			counter++;
		}
		
		DelaunayTriangulator triangulator = new  DelaunayTriangulator();
		triangles = triangulator.getTriangulation(points_new, frameWidth, frameHeight);
		int numTris = triangles.length;
		System.out.println("Triangulating: "+(System.currentTimeMillis()-start));
		
		
		
		
		int numCells = cells.length;
		start= System.currentTimeMillis();
		for (int i =0; i < numCells; i++)
		{
			//	For each cell, get its nuclear centroid and get all triangles its involved with
			ArrayList allTris = new ArrayList();
//			System.out.println(cells[i]);
//			System.out.println(cells[i].nucleus);
			Point2D.Double p = cells[i].getNucleus().getCentroid();
			for (int j = 0; j < numTris; j++)
			{
				Triangle tri = triangles[j];
				for (int q = 0; q<3; q++)
					if (p.distance(tri.points[q])<0.00001d)	//dist from this point to triangle point is zero then its assumed to be same point
					{
						allTris.add(tri);
						break; // cant have same point 2x in same triangle
					}
			}
			
			// For each triangle, find the cells that are associated with it and assign them as neighbor cells of the center point
			int numT = allTris.size();
			cells[i].setNeighborCells(new Cell_RAM[numT]);
			ArrayList tempN = new ArrayList();
			for (int n = 0; n < numT; n++)
			{
				Triangle tri = ((Triangle)allTris.get(n));
				for (int q = 0; q<3; q++)
					if (p.distance(tri.points[q])>0.00001d)	//for each point that is not the center point of the triangle
						for (int m = 0; m < numCells; m++) //find the cell associated with this nuclear centroid point
							if (m!=i) //dont check same cell as potential neighbor
							{
								Point2D.Double p2 = cells[m].getNucleus().getCentroid();
								if (p2.distance(tri.points[q])<0.00001d)
								{
									boolean AlreadyFound = false;
									int numNe = tempN.size();
									for (int w = 0; w < numNe; w++) //checking neighbors found so far so we dont add same cell 2x in neighbor array
										if (((Cell_RAM)tempN.get(w)).getID()==cells[m].getID()) //already found this guy
										{
											AlreadyFound = true;
											break;
										}
									
									if (!AlreadyFound) //if new neighbor cell, then add it to the neighbor array
										tempN.add(cells[m]);
								}
							}
			}
			//now adding all the found neighbors
			int numNe = tempN.size();
			cells[i].setNeighborCells(new Cell_RAM[numNe]);
			for (int w = 0; w < numNe; w++)
				cells[i].getNeighborCells()[w]  = (Cell_RAM)tempN.get(w);
		}
		System.out.println("Finding Neighbor Cells: "+(System.currentTimeMillis()-start));
		
		return getTriangulationLines(triangles);
	}
	
	/** Represents an edge between points*/
	public class Edge
	{
		double p1, p2;
		Edge()
		{
			p1=-1;
			p2=-1;
		}
	}
	
	
	/**
	 * Triangle.java
	 *   Class holding the indicies of each triangle vertex
	 * @author Bjorn Millard
	 */

	public class Triangle
	{
		public double p1;
		public double p2;
		public double p3;
		public Point2D.Double[] points;
		private Point Centroid;
		
		
		/** Returns or computes and returns the centroid of this triangle
		 * @author BLM*/
		public Point getCentroid()
		{
			if (points==null)
				return null;
			if (Centroid!=null)
				return Centroid;
			double x = 0;
			double y = 0;
			x = points[0].x+points[1].x+points[2].x;
			y = points[0].y+points[1].y+points[2].y;
			Centroid = new Point((int)(x/3d), (int)(y/3d));
			return Centroid;
		}
	}
	
	
}
