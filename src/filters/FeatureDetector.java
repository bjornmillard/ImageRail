package filters;

import java.io.File;
import java.util.ArrayList;

import models.Model_Field;
import segmentedobject.Cell;

/** 
 * Static class that contains methods used to determine if a group of cells contains a specified feature
 * */
public class FeatureDetector {

	/** 
	 * Attempts to determine if somewhere within the given collection of cells there is also a bubble or other fluorescent artifact
	 * @author BLM
	 * @param Model_Field field
	 * @return boolean existArtifact?
	 * */
	static public boolean containsCellsWithZScoreCriteria(Model_Field field,
			String channel, float threshold, String operator)
	{
		ArrayList<Cell> cells = field.getCells();
		File[] images = field.getImageFiles();
		
		if(cells==null || cells.size()==0 || images==null || images.length==0)
			return false;
		
//		int[] thresholds = new int[images.length];
//		for (int j = 0; j < images.length; j++) {
//
//			// rescaling image to be small
//			ParameterBlock pb = new ParameterBlock();
//			RenderedImage TheCurrentImage = JAI.create("fileload",
//					images[j].getAbsolutePath());
//
//			pb.addSource(TheCurrentImage);
//			pb.add(0.1F);
//			pb.add(0.1F);
//			pb.add(0.0F);
//			pb.add(0.0F);
//			pb.add(new InterpolationNearest());
//			// Creates a new, scaled image and uses it on the DisplayJAI
//			// component
//			TheCurrentImage = JAI.create("scale", pb);
//			Raster raster = TheCurrentImage.getData();
//			int width = raster.getWidth();
//			int height = raster.getHeight();
//			int[][] im = new int[width][height];
//			int[] pix = new int[raster.getNumBands()];
//			for (int r = 0; r < height; r++) {
//				for (int c = 0; c < width; c++) {
//					raster.getPixel(c, r, pix);
//					im[c][r] = pix[0];
//				}
//			}
//
//			// int[][] im2 = tools.SpatialFilter.linearFilter(im,
//			// tools.LinearKernels.getLinearSmoothingKernal(1));
//			// tools.ImageTools.displayRaster(im2);
//
//			// // create histogram for this raster
//			// int[][] hist = tools.ImageTools.getHistogram(im, 50);
//			//
//			// int val = tools.AutoThresholder.Otsu(hist[1]);
//			// System.out.println("Otsu: " + hist[0][val]);
//			// thresholds[j] = hist[0][val];
//		
//			im = null;
//		}
		boolean foundOne = false;
		int numChannels = images.length;
		String[] channelNames = models.Model_Main.getModel().getTheChannelNames();
		for (int j = 0; j < numChannels; j++) {

			if (channelNames[j].equalsIgnoreCase(channel.trim())
					|| channel.equalsIgnoreCase("All")) {

				String featureName = "Whole_" + channelNames[j] + " (Mean)";
				// Getting mean and stdev so we can compute zScores for each
				// cell
				float mean = field.getParentWell().getValue_Mean(featureName);
				float stdev = field.getParentWell().getValue_Stdev(featureName);

				int numC = cells.size();
				int fIndex = models.Model_Main.getModel().getFeature_Index(featureName);
				for (int i = 0; i < numC; i++) {
					float thisVal = cells.get(i).getFeatureValues()[fIndex];
					float zScore = (thisVal - mean) / stdev;
					// System.out.println(zScore);
					// System.out.println(operator);
					// System.out.println(channel);
					if (operator.trim().equalsIgnoreCase(">"))
 {
						if (zScore > threshold) {
							foundOne = true;
							cells.get(i).setSelected(true);
						}
					} else if (operator.trim().equalsIgnoreCase("<")) {
							if (zScore < threshold) {
								foundOne = true;
								cells.get(i).setSelected(true);
						}
					} else if (operator.trim().equalsIgnoreCase("<>")) {
								if (zScore < threshold || zScore > threshold) {
									foundOne = true;
									cells.get(i).setSelected(true);
								}
					}
				}


			}
		}
		if (foundOne)
			return true;
		return false;
	}
	
}
