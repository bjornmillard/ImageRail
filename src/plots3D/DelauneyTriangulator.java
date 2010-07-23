/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

package plots3D;




import java.util.Arrays;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

public class DelauneyTriangulator
{
	public final static double EPSILON = 0.000001;
	
	/*
	 Return TRUE if a point (xp,yp) is inside the circumcircle made up
	 of the points (x1,y1), (x2,y2), (x3,y3)
	 The circumcircle centre is returned in (xc,yc) and the radius r
	 NOTE: A point on the edge is inside the circumcircle
	 */
	private boolean circumCircle(
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
	public int triangulate (int numPoints, Point3d[] points, Triangle[] triangles )
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
		points[numPoints+0].x = xmid - 2.0 * dmax;
		points[numPoints+0].y = ymid - dmax;
		points[numPoints+0].z = 0.0;
		points[numPoints+1].x = xmid;
		points[numPoints+1].y = ymid + 2.0 * dmax;
		points[numPoints+1].z = 0.0;
		points[numPoints+2].x = xmid + 2.0 * dmax;
		points[numPoints+2].y = ymid - dmax;
		points[numPoints+2].z = 0.0;
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
				x1 = points[triangles[j].p1].x;
				y1 = points[triangles[j].p1].y;
				x2 = points[triangles[j].p2].x;
				y2 = points[triangles[j].p2].y;
				x3 = points[triangles[j].p3].x;
				y3 = points[triangles[j].p3].y;
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
	
	
	
	public IndexedGeometryArray getIndexedTriangleArray(final DataSet data)
	{
		
		//Note that we use the original data for the triangulation and then convert over to a copy of the data that is
		//normalized for the actual plotting of the points and triangles
		Point3d[] points = data.getPoints();
		double[] colors = data.getColors();
	
			//sorting the points according to their xValue
		Arrays.sort(points, new xPointSorter());
		int nv = points.length;
		
		//Need to create 3 dummy points on the end to create a superTriangle
		Point3d[] temp = new Point3d[ nv+3 ];
		for (int i=0; i<points.length; i++)
			temp[i] = points[i];
		temp[temp.length-3] = new Point3d(Math.random(), Math.random(), Math.random());
		temp[temp.length-2] = new Point3d(Math.random(), Math.random(), Math.random());
		temp[temp.length-1] = new Point3d(Math.random(), Math.random(), Math.random());
		points = temp;
		
		Triangle[]	 triangles 	= new Triangle[nv*3];
		for (int i=0; i<triangles.length; i++)
			triangles[i] = new Triangle();
		
		//Triangulating
		int numTriangles = triangulate( nv, points, triangles );
		
		//Normalizing the data so we can plot it within the bounding box
		DataSet normData = data.getNormalizedDataSet();
		points = normData.getPoints();
//		Arrays.sort(points, new xPointSorter());
//		double zMin = normData.getAxisMin_Z();
//		double zMax = normData.getAxisMax_Z();
		double zMin=normData.getDataMin_Z();
		double zMax=normData.getDataMax_Z();
			
			//Now creating the GeometryArray to return
			IndexedGeometryArray TheTriangleArray = new IndexedTriangleArray(nv, GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.NORMALS, numTriangles*3);
		
		float[] colorTemp = new float[3];
		
		
		
		//setting up all the coordinates
		for (int i= 0; i < nv; i++)
		{
			TheTriangleArray.setCoordinate(i, points[i]);
			ColorMaps.getColorValue(colors[i], 0f, 1f, colorTemp, SurfacePlotMainPanel.getColorMap());
			TheTriangleArray.setColor(i, colorTemp);
		}
		
		
		
		//creating the triangles
		int counter = 0;
		for (int i= 0; i < numTriangles; i++)
		{
			Triangle tri = triangles[i];
			TheTriangleArray.setCoordinateIndex(counter, tri.p1);
			TheTriangleArray.setColorIndex(counter,tri.p1);
			counter++;
			TheTriangleArray.setCoordinateIndex(counter, tri.p2);
			TheTriangleArray.setColorIndex(counter,tri.p2);
			counter++;
			TheTriangleArray.setCoordinateIndex(counter, tri.p3);
			TheTriangleArray.setColorIndex(counter,tri.p3);
			counter++;
		}
		
		
		TheTriangleArray.setCapability(GeometryArray.ALLOW_COUNT_READ);
		TheTriangleArray.setCapability(GeometryArray.ALLOW_FORMAT_READ);
		TheTriangleArray.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		TheTriangleArray.setCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ);
		
		
		
		GeometryInfo gi = new GeometryInfo(TheTriangleArray);
		// initialize the geometry info here
		// generate normals
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		// stripify
		Stripifier st = new Stripifier();
		st.stripify(gi);
		TheTriangleArray = gi.getIndexedGeometryArray();
		
		
		return TheTriangleArray;
		
		
	}
	
	
	/** Class holding the indicies of each triangle vertex*/
	private class Triangle
	{
		public int p1;
		public int p2;
		public int p3;
	}
	/** Represents an edge between points*/
	private class Edge
	{
		int p1, p2;
		Edge()
		{
			p1=-1;
			p2=-1;
		}
	}
	
	
}
