/**
 * WellGraph.java
 *
 * @author BLM
 */

package analysisModules;
import main.*;

import features.Feature;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import tools.GaussianDistribution;

public class WellGraph extends Well
{
	public int xStart;
	public int yStart;
	public int xLen;
	public int yLen;
	private ArrayList<Polygon> PolygonsToDraw;
	private ArrayList<Color> PolygonsColors;
	
	public WellGraph(Plate plate, int row, int col)
	{
		super(plate, row, col);
		
		PolygonsToDraw = new ArrayList<Polygon>();
		PolygonsColors = new ArrayList<Color>();
		xStart = outline.x+1;
		yStart = outline.y+1;
		xLen = outline.width-1;
		yLen = outline.height-2;
		
	}
	
	/** Adds polyons that we want drawn inside of this well
	 * @author BLM*/
	public void addPolygonToDraw(Polygon p, Color color)
	{
		PolygonsToDraw.add(p);
		PolygonsColors.add(color);
	}
	
	
	
	/** Draws this Well in the PlateGraph
	 * @author BLM*/
	public void draw(Graphics2D g2)
	{
		g2.setColor(color);
		g2.fillRect(outline.x, outline.y, outline.width, outline.height);
		g2.drawRect(outline.x, outline.y, outline.width, outline.height);
		
		if (isSelected())
			g2.setColor(Color.RED);
		else
			g2.setColor(Color.white);
		g2.drawRect(outline.x, outline.y, outline.width, outline.height);
		
		int nP = PolygonsToDraw.size();
		for (int i = 0; i < nP; i++)
		{
			g2.setColor(PolygonsColors.get(i));
			g2.fillPolygon(PolygonsToDraw.get(i));
		}
		
	}
	
}

