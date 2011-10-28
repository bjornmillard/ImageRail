package filters;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.util.ArrayList;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;

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
	static public boolean containsArtifact(Model_Field field)
	{
		ArrayList<Cell> cells = field.getCells();
		File[] images = field.getImageFiles();
		
		if(cells==null || cells.size()==0 || images==null || images.length==0)
			return false;
		
		for (int j = 0; j < images.length; j++) {
			
			//rescaling image to be small
			ParameterBlock pb = new ParameterBlock();
			RenderedImage TheCurrentImage = JAI.create("fileload",
				images[j].getAbsolutePath());
	
			pb.addSource(TheCurrentImage);
			pb.add(0.2F);
			pb.add(0.2F);
			pb.add(0.0F);
			pb.add(0.0F);
			pb.add(new InterpolationNearest());
			// Creates a new, scaled image and uses it on the DisplayJAI component
			TheCurrentImage = JAI.create("scale", pb);
			Raster raster = TheCurrentImage.getData();
			int width = raster.getWidth();
			int height = raster.getHeight();
			int[][] im = new int[width][height];
			int[] pix = new int[raster.getNumBands()];
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					raster.getPixel(c, r, pix);
					im[c][r] = pix[0];
				}
			}
//			tools.ImageTools.displayRaster(im);
			
			//create histogram for this raster
			int[][] hist = tools.ImageTools.getHistogram(im, 50);
			System.out.println("*******************");
			for (int i = 0; i < hist[0].length; i++) 
				System.out.println(hist[0][i]);
			System.out.println("-------------------");
			for (int i = 0; i < hist[0].length; i++) 
				System.out.println(hist[1][i]);
			
			im = null;
		}
		
		
		
		
		
		int numC = cells.size();
		if(Math.random()>0)
		{
			for (int i = 0; i < numC; i++) {
				if(i%2==0)
					cells.get(i).setSelected(true);
			}
		
			return true;
		}
		
		return false;
	}
	
}
