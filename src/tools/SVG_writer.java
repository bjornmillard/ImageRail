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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.PrintWriter;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class SVG_writer extends Graphics2D
{
	
	public void fillPolygon(int[] xvals, int[] yvals, int numPoints)
	{
		String st = "<polygon points='";
		for (int i = 0; i < numPoints; i++)
			st+=(" "+xvals[i]+","+yvals[i]+" ");
		st+="' style='fill:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); fill-opacity:"+alpha+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void drawPolygon(int[] xvals, int[] yvals, int numPoints)
	{
		
		String st = "<polygon points='";
		for (int i = 0; i < numPoints; i++)
			st+=(" "+xvals[i]+","+yvals[i]+" ");
		st+="' style='fill:rgb("+1+","+1+","+1+"); fill-opacity:"+0+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	
	/**
	 * Method setStroke
	 *
	 * @param    p1                  a  Stroke
	 *
	 */
	public void setStroke(Stroke p1)
	{
		// TODO
	}
	
	
	
	
	private Stroke stroke;
	private float alpha;
	private Color color;
	private PrintWriter pw;
	private int fontSize;
	private int strokeWidth;
	private Composite TheComposite;
	
	
	/**
	 * Method draw
	 *
	 * @param    p1                  a  Shape
	 *
	 */
	public void draw(Shape p1)
	{
		// TODO
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an AffineTransform
	 * @param    p3                  an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, AffineTransform p2, ImageObserver p3)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  a  BufferedImage
	 * @param    p2                  a  BufferedImageOp
	 * @param    p3                  an int
	 * @param    p4                  an int
	 *
	 */
	public void drawImage(BufferedImage p1, BufferedImageOp p2, int p3, int p4)
	{
		// TODO
	}
	
	/**
	 * Method drawRenderedImage
	 *
	 * @param    p1                  a  RenderedImage
	 * @param    p2                  an AffineTransform
	 *
	 */
	public void drawRenderedImage(RenderedImage p1, AffineTransform p2)
	{
		// TODO
	}
	
	/**
	 * Method drawRenderableImage
	 *
	 * @param    p1                  a  RenderableImage
	 * @param    p2                  an AffineTransform
	 *
	 */
	public void drawRenderableImage(RenderableImage p1, AffineTransform p2)
	{
		// TODO
	}
	
	
	/**
	 * Method drawString
	 *
	 * @param    p1                  an AttributedCharacterIterator
	 * @param    p2                  an int
	 * @param    p3                  an int
	 *
	 */
	public void drawString(AttributedCharacterIterator p1, int p2, int p3)
	{
		// TODO
	}
	
	/**
	 * Method drawString
	 *
	 * @param    p1                  an AttributedCharacterIterator
	 * @param    p2                  a  float
	 * @param    p3                  a  float
	 *
	 */
	public void drawString(AttributedCharacterIterator p1, float p2, float p3)
	{
		// TODO
	}
	
	/**
	 * Method drawGlyphVector
	 *
	 * @param    p1                  a  GlyphVector
	 * @param    p2                  a  float
	 * @param    p3                  a  float
	 *
	 */
	public void drawGlyphVector(GlyphVector p1, float p2, float p3)
	{
		// TODO
	}
	
	/**
	 * Method fill
	 *
	 * @param    p1                  a  Shape
	 *
	 */
	public void fill(Shape p1)
	{
		// TODO
	}
	
	/**
	 * Method hit
	 *
	 * @param    p1                  a  Rectangle
	 * @param    p2                  a  Shape
	 * @param    p3                  a  boolean
	 *
	 * @return   a boolean
	 *
	 */
	public boolean hit(Rectangle p1, Shape p2, boolean p3)
	{
		// TODO
		return false;
	}
	
//	/**
//	 * Method getDeviceConfiguration
//	 *
//	 * @return   a Graphiciofiguration
//	 *
//	 */
//	public Graphiciofiguration getDeviceConfiguration()
//	{
//		// TODO
//		return null;
//	}
	
	/**
	 * Method setComposite
	 *
	 * @param    p1                  a  Composite
	 *
	 */
	public void setComposite(Composite p1)
	{
		// TODO
	}
	
	/**
	 * Method setPaint
	 *
	 * @param    p1                  a  Paint
	 *
	 */
	public void setPaint(Paint p1)
	{
		// TODO
	}
	
	
	
	/**
	 * Method setRenderingHint
	 *
	 * @param    p1                  a  Key
	 * @param    p2                  an Object
	 *
	 */
	public void setRenderingHint(RenderingHints.Key p1, Object p2)
	{
		// TODO
	}
	
	/**
	 * Method getRenderingHint
	 *
	 * @param    p1                  a  Key
	 *
	 * @return   an Object
	 *
	 */
	public Object getRenderingHint(RenderingHints.Key p1)
	{
		// TODO
		return null;
	}
	
	/**
	 * Method setRenderingHints
	 *
	 * @param    p1                  a  Map
	 *
	 */
	public void setRenderingHints(Map<?, ?> p1)
	{
		// TODO
	}
	
	/**
	 * Method addRenderingHints
	 *
	 * @param    p1                  a  Map
	 *
	 */
	public void addRenderingHints(Map<?, ?> p1)
	{
		// TODO
	}
	
	/**
	 * Method getRenderingHints
	 *
	 * @return   a RenderingHints
	 *
	 */
	public RenderingHints getRenderingHints()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method translate
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 *
	 */
	public void translate(int p1, int p2)
	{
		// TODO
	}
	
	/**
	 * Method translate
	 *
	 * @param    p1                  a  double
	 * @param    p2                  a  double
	 *
	 */
	public void translate(double p1, double p2)
	{
		// TODO
	}
	
	/**
	 * Method rotate
	 *
	 * @param    p1                  a  double
	 *
	 */
	public void rotate(double p1)
	{
		// TODO
	}
	
	/**
	 * Method rotate
	 *
	 * @param    p1                  a  double
	 * @param    p2                  a  double
	 * @param    p3                  a  double
	 *
	 */
	public void rotate(double p1, double p2, double p3)
	{
		// TODO
	}
	
	/**
	 * Method scale
	 *
	 * @param    p1                  a  double
	 * @param    p2                  a  double
	 *
	 */
	public void scale(double p1, double p2)
	{
		// TODO
	}
	
	/**
	 * Method shear
	 *
	 * @param    p1                  a  double
	 * @param    p2                  a  double
	 *
	 */
	public void shear(double p1, double p2)
	{
		// TODO
	}
	
	/**
	 * Method transform
	 *
	 * @param    p1                  an AffineTransform
	 *
	 */
	public void transform(AffineTransform p1)
	{
		// TODO
	}
	
	/**
	 * Method setTransform
	 *
	 * @param    p1                  an AffineTransform
	 *
	 */
	public void setTransform(AffineTransform p1)
	{
		// TODO
	}
	
	/**
	 * Method getTransform
	 *
	 * @return   an AffineTransform
	 *
	 */
	public AffineTransform getTransform()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method getPaint
	 *
	 * @return   a Paint
	 *
	 */
	public Paint getPaint()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method getComposite
	 *
	 * @return   a Composite
	 *
	 */
	public Composite getComposite()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method setBackground
	 *
	 * @param    p1                  a  Color
	 *
	 */
	public void setBackground(Color p1)
	{
		// TODO
	}
	
	/**
	 * Method getBackground
	 *
	 * @return   a Color
	 *
	 */
	public Color getBackground()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method clip
	 *
	 * @param    p1                  a  Shape
	 *
	 */
	public void clip(Shape p1)
	{
		// TODO
	}
	
	/**
	 * Method getFontRenderContext
	 *
	 * @return   a FontRenderContext
	 *
	 */
	public FontRenderContext getFontRenderContext()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method create
	 *
	 * @return   a Graphics
	 *
	 */
	public Graphics create()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method setPaintMode
	 *
	 */
	public void setPaintMode()
	{
		// TODO
	}
	
	/**
	 * Method setXORMode
	 *
	 * @param    p1                  a  Color
	 *
	 */
	public void setXORMode(Color p1)
	{
		// TODO
	}
	
	/**
	 * Method getFont
	 *
	 * @return   a Font
	 *
	 */
	public Font getFont()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method setFont
	 *
	 * @param    p1                  a  Font
	 *
	 */
	public void setFont(Font p1)
	{
		// TODO
	}
	
	/**
	 * Method getFontMetrics
	 *
	 * @param    p1                  a  Font
	 *
	 * @return   a FontMetrics
	 *
	 */
	public FontMetrics getFontMetrics(Font p1)
	{
		// TODO
		return null;
	}
	
	/**
	 * Method getClipBounds
	 *
	 * @return   a Rectangle
	 *
	 */
	public Rectangle getClipBounds()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method clipRect
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 *
	 */
	public void clipRect(int p1, int p2, int p3, int p4)
	{
		// TODO
	}
	
	/**
	 * Method setClip
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 *
	 */
	public void setClip(int p1, int p2, int p3, int p4)
	{
		// TODO
	}
	
	/**
	 * Method getClip
	 *
	 * @return   a Shape
	 *
	 */
	public Shape getClip()
	{
		// TODO
		return null;
	}
	
	/**
	 * Method setClip
	 *
	 * @param    p1                  a  Shape
	 *
	 */
	public void setClip(Shape p1)
	{
		// TODO
	}
	
	/**
	 * Method copyArea
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 *
	 */
	public void copyArea(int p1, int p2, int p3, int p4, int p5, int p6)
	{
		// TODO
	}
	
	
	/**
	 * Method clearRect
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 *
	 */
	public void clearRect(int p1, int p2, int p3, int p4)
	{
		// TODO
	}
	
	/**
	 * Method drawRoundRect
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 *
	 */
	public void drawRoundRect(int p1, int p2, int p3, int p4, int p5, int p6)
	{
		// TODO
	}
	
	/**
	 * Method fillRoundRect
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 *
	 */
	public void fillRoundRect(int p1, int p2, int p3, int p4, int p5, int p6)
	{
		// TODO
	}
	
	
	/**
	 * Method drawArc
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 *
	 */
	public void drawArc(int p1, int p2, int p3, int p4, int p5, int p6)
	{
		// TODO
	}
	
	/**
	 * Method fillArc
	 *
	 * @param    p1                  an int
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 *
	 */
	public void fillArc(int p1, int p2, int p3, int p4, int p5, int p6)
	{
		// TODO
	}
	
	/**
	 * Method drawPolyline
	 *
	 * @param    p1                  an int[]
	 * @param    p2                  an int[]
	 * @param    p3                  an int
	 *
	 */
	public void drawPolyline(int[] p1, int[] p2, int p3)
	{
		// TODO
	}
	
	/**
	 * Method drawPolygon
	 *
	 * @param    p1                  an int[]
	 * @param    p2                  an int[]
	 * @param    p3                  an int
	 *
	 */
//	public void drawPolygon(int[] p1, int[] p2, int p3)
//	{
//		// TODO
//	}
//
//	/**
//	 * Method fillPolygon
//	 *
//	 * @param    p1                  an int[]
//	 * @param    p2                  an int[]
//	 * @param    p3                  an int
//	 *
//	 */
//	public void fillPolygon(int[] p1, int[] p2, int p3)
//	{
//		// TODO
//	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, ImageObserver p4)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, int p4, int p5, ImageObserver p6)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  a  Color
	 * @param    p5                  an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, Color p4, ImageObserver p5)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  a  Color
	 * @param    p7                  an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, int p4, int p5, Color p6, ImageObserver p7)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 * @param    p7                  an int
	 * @param    p8                  an int
	 * @param    p9                  an int
	 * @param    p10                 an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, ImageObserver p10)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method drawImage
	 *
	 * @param    p1                  an Image
	 * @param    p2                  an int
	 * @param    p3                  an int
	 * @param    p4                  an int
	 * @param    p5                  an int
	 * @param    p6                  an int
	 * @param    p7                  an int
	 * @param    p8                  an int
	 * @param    p9                  an int
	 * @param    p10                 a  Color
	 * @param    p11                 an ImageObserver
	 *
	 * @return   a boolean
	 *
	 */
	public boolean drawImage(Image p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, Color p10, ImageObserver p11)
	{
		// TODO
		return false;
	}
	
	/**
	 * Method dispose
	 *
	 */
	public void dispose()
	{
		// TODO
	}
	
	
	public SVG_writer(PrintWriter pw_)
	{
		pw = pw_;
		color = new Color(1,1,1);
		alpha = 1f;
		stroke = null;
		fontSize = 10;
	}
	
	
	public void setFontSize(int size)
	{
		fontSize = size;
	}
	public int getFontSize()
	{
		return fontSize;
	}
	
	public void setColor(Color color_)
	{
		color = color_;
	}
	public Color getColor()
	{
		return color;
	}
	
	public void setAlpha(float alpha_)
	{
		alpha = alpha_;
	}
	public float getAlpha()
	{
		return alpha;
	}
	
//	public void setStroke(Stroke stroke_)
//	{
//		stroke = stroke_;
//	}
	public void setStrokeWidth(int width)
	{
		strokeWidth=width;
	}
	
	public Stroke getStroke()
	{
		return stroke;
	}
	
	public void printHeader()
	{
		String st = "<?xml version='1.0' standalone='no'?> \n <!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' \n'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>";
		st+="<svg width='100%' height='100%' version='1.1' xmlns='http://www.w3.org/2000/svg'>";
		pw.print(st);
		pw.println();
		pw.println();
	}
	
	public void printEnd()
	{
		pw.println();
		pw.println("</svg>");
	}
	
	public void printTitle(String title)
	{
		pw.println("<title>"+title+"</title>");
		pw.println();
	}
	
	public void drawLine(int x1, int y1, int x2, int y2)
	{
		String st = "<line x1='"+x1+"' y1='"+y1+"' x2='"+x2+"' y2='"+y2+"' ";
		st+="style='stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");stroke-width:"+strokeWidth+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	
	public void fillRect(int x, int y, int width, int height)
	{
		String st = "<rect x='"+x+"' y='"+y+"' width='"+width+"' height='"+height+"' ";
		st+="style='fill:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); fill-opacity:"+alpha+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	
	public void drawRect(int x, int y, int width, int height)
	{
		String st = "<rect x='"+x+"' y='"+y+"' width='"+width+"' height='"+height+"' ";
		st+="style='fill:rgb("+1f+","+1f+","+1f+"); fill-opacity:"+0+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void fillOval(int x, int y, int radiusX, int radiusY)
	{
		String st = "<ellipse cx='"+x+"' cy='"+y+"' rx='"+radiusX+"' ry='"+radiusY+"' ";
		st+="style='fill:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); fill-opacity:"+alpha+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void drawOval(int x, int y, int radiusX, int radiusY)
	{
		String st = "<ellipse cx='"+x+"' cy='"+y+"' rx='"+radiusX+"' ry='"+radiusY+"' ";
		st+="style='fill:rgb("+1+","+1+","+1+"); fill-opacity:"+0+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void fillPolygon(Polygon polygon)
	{
		int[] xvals = polygon.xpoints;
		int[] yvals = polygon.ypoints;
		int numPoints = polygon.npoints;
		
		String st = "<polygon points='";
		for (int i = 0; i < numPoints; i++)
			st+=(" "+xvals[i]+","+yvals[i]+" ");
		st+="' style='fill:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); fill-opacity:"+alpha+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void drawPolygon(Polygon polygon)
	{
		int[] xvals = polygon.xpoints;
		int[] yvals = polygon.ypoints;
		int numPoints = polygon.npoints;
		
		
		String st = "<polygon points='";
		for (int i = 0; i < numPoints; i++)
			st+=(" "+xvals[i]+","+yvals[i]+" ");
		st+="' style='fill:rgb("+1+","+1+","+1+"); fill-opacity:"+0+";";
		st+=" stroke:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+"); stroke-width:"+stroke+"; stroke-opacity:"+alpha+";'/>";
		pw.print(st);
		pw.println();
	}
	
	public void drawString(String body, int x, int y)
	{
		String st = "<text x='"+x+"' y = '"+y+"'";
		st+= " fill='rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")' font-size = '"+fontSize+"'> ";
		st+=body+" </text>";
		pw.print(st);
		pw.println();
	}
	
	public void drawString(String body, float x, float y)
	{
		String st = "<text x='"+x+"' y = '"+y+"'";
		st+= " fill='rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")' font-size = '"+fontSize+"'> ";
		st+=body+" </text>";
		pw.print(st);
		pw.println();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

