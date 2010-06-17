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

/** Given an Point3f[], this algorithm finds a mesh made out of the given point vertices
 * in the form of a TriangleArray
 * @author BLM*/
import java.util.ArrayList;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

public class TriangleArrayFromPoints
{
	private PointMarker[] points;
	private int numP;
	
	public TriangleArray getTriangleArrayFromPoints(Point3f[] p)
	{
		//each three points represents the verts of a single triangle
		ArrayList listOfTriVerts = new ArrayList();
		
		numP = p.length;
		
		points = new PointMarker[numP];
		for (int i = 0; i < numP; i++)
			points[i] = new PointMarker(p[i], i);
		
		//creating the first triangle manually
		PointMarker[] neighbors = null;
		neighbors = points[0].getClosestTwoPoints();
		//creating a triangle from these 3 points
		listOfTriVerts.add(points[0]);
		listOfTriVerts.add(neighbors[0]);
		listOfTriVerts.add(neighbors[1]);
		//now marking them all as being visited
		points[0].free = false;
		neighbors[0].free = false;
		neighbors[1].free = false;
		
		
		//pick a free point, find 2 closest neighbors,
		for (int i = 1; i < numP; i++)
		{
			//If this point has not been connected to another clump yet, then set it as a center and
			// search for its neighbors and connect them
			neighbors = null;
			if (points[i].free)
			{
				neighbors = points[i].getClosestTwoNonFreePoints();
				//creating a triangle from these 3 points
				listOfTriVerts.add(points[i]);
				listOfTriVerts.add(neighbors[0]);
				listOfTriVerts.add(neighbors[1]);
				//now marking them all as being visited
				points[i].free = false;
				neighbors[0].free = false;
				neighbors[1].free = false;

			}
		}
		
		
		//Finally creating a triangleArray from all the triangleVertices discovered
		TriangleArray ta = new TriangleArray(listOfTriVerts.size(), TriangleArray.COORDINATES | TriangleArray.COLOR_3);
		System.out.println(numP);
		int counter = 0;
		for (int i = 0; i < numP; i=i+3)
			for (int j = 0; j < 3; j++)
			{
				ta.setCoordinate(counter, ((PointMarker)listOfTriVerts.get(counter)).point);
				ta.setColor(counter, Tools.getRandomColor3f());
				counter++;
			}
		
		return ta;
	}
	
	/** Creating a dummy class to tag if this point has been visited before*/
	public class PointMarker
	{
		public boolean free;
		public Point3f point;
		public int thisIndex;
		
		PointMarker(Point3f p, int index)
		{
			thisIndex = index;
			point = p;
			free = true;
		}
		
		/** must return at 2 closest neighbors to create a triangle */
		public PointMarker[] getClosestTwoPoints()
		{
			PointMarker[] neighbors = new PointMarker[2];
			double min0 = Double.MAX_VALUE;
			double min1 = Double.MAX_VALUE;
			
			for (int i =0; i < numP; i++)
			{
				if (i!=thisIndex)
				{
					double dist = getXYDist(points[i]);
					if (dist<min0)
					{
						//move current best to second best, then update best
						neighbors[1] = neighbors[0];
						min1 = min0;
						neighbors[0] = points[i];
						min0 = dist;
					}
					else if (dist<min1)
					{
						//ubdate second best
						neighbors[1] = points[i];
						min1 = dist;
					}
				}
			}
			return neighbors;
		}
		
		/** must return at 2 closest neighbors to create a triangle */
		public PointMarker[] getClosestTwoNonFreePoints()
		{
			PointMarker[] neighbors = new PointMarker[2];
			double min0 = Double.MAX_VALUE;
			double min1 = Double.MAX_VALUE;
			
			for (int i =0; i < numP; i++)
			{
				if (!points[i].free && i!=thisIndex)
				{
					double dist = getXYDist(points[i]);
					if (dist<min0)
					{
						//move current best to second best, then update best
						neighbors[1] = neighbors[0];
						min1 = min0;
						neighbors[0] = points[i];
						min0 = dist;
					}
					else if (dist<min1)
					{
						//ubdate second best
						neighbors[1] = points[i];
						min1 = dist;
					}
				}
			}
			return neighbors;
		}
		
		/** returns the euclidean dist of the given point and this one
		 * @author BLM*/
		public double getDist(PointMarker p)
		{
			double diffX = point.x-p.point.x;
			double diffY = point.y-p.point.y;
			double diffZ = point.z-p.point.z;
			diffX = diffX*diffX;
			diffY = diffY*diffY;
			diffZ = diffZ*diffZ;
			double dist = Math.sqrt(diffX+diffY+diffZ);
//			System.out.println(dist);
			return dist;
		}
		public double getXYDist(PointMarker p)
		{
			double diffX = point.x-p.point.x;
			double diffY = point.y-p.point.y;
			diffX = diffX*diffX;
			diffY = diffY*diffY;
			double dist = Math.sqrt(diffX+diffY);
//			System.out.println(dist);
			return dist;
		}
	}
}

