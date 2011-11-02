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

public class LinearKernels
{
	public LinearKernels()
	{
	}
	
	static public float[][] getLinearSmoothingKernal(int width)
	{
		float[][] ker = new float[width][width];
		for (int r= 0; r < width; r++)
			for (int c =0; c < width; c++)
				ker[r][c] = 1f/(float)(width*width);
		return ker;
	}
	static public float[][] getLinearSmoothingKernal_weighted()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 1; ker[0][1] = 2; ker[0][2] = 1;
		ker[1][0] = 2; ker[1][1] = 4; ker[1][2] = 2;
		ker[2][0] = 1; ker[2][1] = 2; ker[2][2] = 1;
		for (int r= 0; r < 3; r++)
			for (int c =0; c < 3; c++)
				ker[r][c] *= 1f/16f;
		return ker;
	}
	
	/** 2nd order derivative for pixel changes*/
	static public float[][] getLaplacian()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = -1; ker[0][1] = -1; ker[0][2] = -1;
		ker[1][0] = -1; ker[1][1] = 8; ker[1][2] = -1;
		ker[2][0] = -1; ker[2][1] = -1; ker[2][2] = -1;
		
		return ker;
	}
	
	static public float[][] getLaplacian2()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 1; ker[0][1] = 1; ker[0][2] = 1;
		ker[1][0] = 1; ker[1][1] = -8; ker[1][2] = 1;
		ker[2][0] = 1; ker[2][1] = 1; ker[2][2] = 1;
		
		return ker;
	}
	
	/** Horizontal Sobel filter for edge detection*/
	static public float[][] getSobel_h()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = -1; ker[0][1] = -2; ker[0][2] = -1;
		ker[1][0] = 0; ker[1][1] = 0; ker[1][2] = 0;
		ker[2][0] = 1; ker[2][1] = 2; ker[2][2] = 1;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getSobel_v()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = -1; ker[0][1] = 0; ker[0][2] = 1;
		ker[1][0] = -2; ker[1][1] = 0; ker[1][2] = 2;
		ker[2][0] = -1; ker[2][1] = 0; ker[2][2] = 1;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getSobel_d1()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 0; ker[0][1] = 1; ker[0][2] = 2;
		ker[1][0] = -1; ker[1][1] = 0; ker[1][2] = 1;
		ker[2][0] = -2; ker[2][1] = -1; ker[2][2] = 0;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getSobel_d2()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = -2; ker[0][1] = -1; ker[0][2] = 0;
		ker[1][0] = -1; ker[1][1] = 0; ker[1][2] = 1;
		ker[2][0] = 0; ker[2][1] = 1; ker[2][2] = 2;
		
		return ker;
	}
	
	
	/** Horizontal Sobel filter for edge detection*/
	static public float[][] getValley_h()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 1; ker[0][1] = 1; ker[0][2] = 1;
		ker[1][0] = -2; ker[1][1] = -2; ker[1][2] = -2;
		ker[2][0] = 1; ker[2][1] = 1; ker[2][2] = 1;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getValley_v()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 1; ker[0][1] = -2; ker[0][2] = 1;
		ker[1][0] = 1; ker[1][1] = -2; ker[1][2] = 1;
		ker[2][0] = 1; ker[2][1] = -2; ker[2][2] = 1;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getValley_d1()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = -2; ker[0][1] = 1; ker[0][2] = 1;
		ker[1][0] = 1; ker[1][1] = -2; ker[1][2] = 1;
		ker[2][0] = 1; ker[2][1] = 1; ker[2][2] = -2;
		
		return ker;
	}
	/** Vertical Sobel filter for edge detection*/
	static public float[][] getValley_d2()
	{
		float[][] ker = new float[3][3];
		ker[0][0] = 1; ker[0][1] = 1; ker[0][2] = -2;
		ker[1][0] = 1; ker[1][1] = -2; ker[1][2] = 1;
		ker[2][0] = -2; ker[2][1] = 1; ker[2][2] = 1;
		
		return ker;
	}
	
}


