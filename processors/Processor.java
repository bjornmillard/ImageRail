/**
 * Processor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package processors;

import main.Well;

public interface Processor
{
	public void runProcess();
	public Well getWellForGivenImage(String fileName);
	public void setClusterRun(boolean boo);
}

