/**
 * AxisBoundsBar.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots;

import java.awt.Color;
import java.awt.Composite;

public class AxisBoundsBar
{
	
//
//	{
//	//
//	//
//	//Drawing the yAxisLabelBox
//	g2.setColor(Color.lightGray);
//	Composite co = g2.getComposite();
//	g2.setComposite(transComposite);
//	yAxisLabelBox.x = XMARGIN+1;
//	yAxisLabelBox.y = yStart-yRange+1;
//	yAxisLabelBox.width = 45;
//	yAxisLabelBox.height = yRange-1;
//	g2.fill(yAxisLabelBox);
//	g2.setColor(Color.gray);
//	g2.draw(yAxisLabelBox);
//
//
//	//Drawing the right border lines
////		for (int i =0; i < 4; i++)
////		{
////			g2.setColor(colors[i]);
////			g2.drawLine(yAxisLabelBox.x+yAxisLabelBox.width+i, yAxisLabelBox.y , yAxisLabelBox.x+yAxisLabelBox.width+i, yAxisLabelBox.y+yAxisLabelBox.height);
////		}
//
//	//Drawing handles on yAxisLabelBox
//	for (int i =0; i <4;i++)
//	{
//		g2.setColor(Color.black);
//		g2.drawLine(yAxisLabelBox.x+8, yAxisLabelBox.y+8+2*i, yAxisLabelBox.x+yAxisLabelBox.width-10,  yAxisLabelBox.y+8+2*i);
//		g2.drawLine(yAxisLabelBox.x+8, yAxisLabelBox.y+yAxisLabelBox.height-8-2*i, yAxisLabelBox.x+yAxisLabelBox.width-10,  yAxisLabelBox.y+yAxisLabelBox.height-8-2*i);
//	}
//	g2.setComposite(co);
//	//Drawing the axis ticks and labels:
//	//
//
//	//Y-axis
//	g2.setColor(Color.DARK_GRAY);
//	int numTicks = 10;
//	int tickLen = 4;
//	//ticks
//	for (int i =0; i < numTicks; i++)
//		g2.drawLine(XMARGIN+1, (int)(yStart-((float)i/(float)numTicks*yRange)) , (XMARGIN+tickLen), (int)(yStart-((float)i/(float)numTicks*yRange)));
//	//labels
//	if (UpperBounds_Y!=0 && MaxValue!=0)
//	{
//		g2.setFont(SmallFont);
//		for (int i =0; i < numTicks; i++)
//			if ((i+1)%2==0 && i!=numTicks) // odd ticks, but not top
//			{
//				if (UpperBounds_Y<10) //float display
//				{
//					float val = (float)i/(float)numTicks*(UpperBounds_Y-LowerBounds_Y)+LowerBounds_Y;
//					g2.drawString(""+val, (XMARGIN+tickLen+3), (int)(yStart-((float)i/(float)numTicks*yRange)+3));
//				}
//				else //integer display
//				{
//					int val = (int)((float)i/(float)numTicks*(UpperBounds_Y-LowerBounds_Y)+LowerBounds_Y);
//					if (val<0)
//						g2.setColor(Color.red);
//					g2.drawString(""+val, (XMARGIN+tickLen+4), (int)(yStart-((float)i/(float)numTicks*yRange)+3));
//					g2.setColor(Color.black);
//				}
//			}
//	}
//	//
//	//
//}

}

