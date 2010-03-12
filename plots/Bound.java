/**
 * Bound.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots;

import javax.swing.JPanel;

public class Bound
{
	public double Upper;
	public double Lower;
	public JPanel TheParentPanel;
	
	public Bound(JPanel panel)
	{
		TheParentPanel = panel;
	}
}

