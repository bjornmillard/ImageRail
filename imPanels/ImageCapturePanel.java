/**
 * ImageCapturePanel.java
 *
 * @author Created by Omnicore CodeGuide
 */

package imPanels;

import java.io.File;
import java.io.PrintWriter;

public interface ImageCapturePanel
{
	public void captureImage(File file, String imageType);
	public void captureSVG(PrintWriter pw);
}

