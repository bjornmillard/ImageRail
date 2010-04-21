/**
 * Processor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package processors;

import models.Model_Well;

public interface Processor
{
	public void runProcess();
	public Model_Well getWellForGivenImage(String fileName);
	public void setClusterRun(boolean boo);
}

