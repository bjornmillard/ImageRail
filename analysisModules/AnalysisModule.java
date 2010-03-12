/**
 * AnalysisModule.java
 *
 * @author Created by Bjorn Millard
 */

package analysisModules;

import javax.swing.JFrame;
import javax.swing.JPanel;

public interface AnalysisModule
{
	public int getWidth();
	public int getHeight();
	public JPanel getPanel();
	public boolean isResizable();
}

