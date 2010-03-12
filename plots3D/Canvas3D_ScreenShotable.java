/**
 * Canvas3D_ScreenShotable.java
 *
 * @author BLM
 */

package plots3D;

import javax.media.j3d.*;

import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.vecmath.Point3f;

public class Canvas3D_ScreenShotable extends Canvas3D
{
	private File outputFile;
	public boolean printJPEG;
	public Transform3D TheTransform;
	public int PlotType;
	static public final int SURFACEPLOT = 0;
	static public final int HISTOGRAMPLOT = 1;
	
	
	public Canvas3D_ScreenShotable(GraphicsConfiguration gc, int plottype)
	{
		super(gc);
		PlotType = plottype;
		TheTransform = new Transform3D();
	}
	
	public void setFileOutput(File file)
	{
		outputFile = file;
	}
	
	public void postSwap()
	{
		//updating the viewAngle display
		if (PlotType == SURFACEPLOT)
		{
			SurfacePlotMainPanel.TheControlPanel.repaint();
		}
		else if (PlotType == HISTOGRAMPLOT)
		{
			if (ImageRail3D_MainContainer.TheControlPanel!=null)
				ImageRail3D_MainContainer.TheControlPanel.repaint();
		}
		
		
		//checks to see if it should take a screenshot
		if(printJPEG)
		{
			GraphicsContext3D  ctx = getGraphicsContext3D();
			Raster ras = new Raster(
				new Point3f(-1.0f,-1.0f,-1.0f),
				Raster.RASTER_COLOR,
				0,0,
				getWidth(),getHeight(),
				new ImageComponent2D(ImageComponent.FORMAT_RGB,new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB)),null);
			ctx.readRaster(ras);
			BufferedImage img = ras.getImage().getImage();
			
			String imageType = outputFile.getName();
			int dot = imageType.lastIndexOf(".");
			if (dot>-1)
			{
				imageType = imageType.substring(dot+1).toLowerCase();
				if (imageType.equals("png"))
				{
					imageType = "png";
				}
				else if (imageType.equals("jpg") || imageType.equals("jpeg"))
				{
					imageType = "jpeg";
				}
				else
				{
					System.out.println("Image file type un-available\n use .jpg/.jpeg), .png or .tif/.tiff");
					return;
				}
			}
			else
			{
				outputFile = new File(outputFile.getAbsolutePath()+".png");
				imageType = "png";
			}
			
			
			
			try
			{
				//making sure we dont overwrite an existing file... rename its "Name_1", "Name_2" ect..
				String name = outputFile.getName();
				int len = name.length();
				String prefix = name;
				String suffix = name;
				dot = name.lastIndexOf(".");
				if (dot>-1)
				{
					
					prefix = name.substring(0,dot);
					suffix = name.substring(dot, len);
					outputFile = new File(outputFile.getParent()+File.separator+prefix+name.substring(dot,len));
				}
				
				
				int counter = 1;
				while (outputFile.exists())
				{
					outputFile =new File(outputFile.getParent()+File.separator+prefix+"_"+counter+suffix);
					counter++;
				}
				
				
				
				//finally writing the image
				FileOutputStream o = new FileOutputStream(outputFile);
				ImageIO.write(img,imageType,o);
				o.flush();
				o.close();
			}
			catch (Exception ex)
			{
				System.out.println("NetworkTranslator.saveScreenShot error "+ex);
			}
			
//			//Printing the JPEG
//			try
//			{
//				outputFile = new File(outputFile.getAbsolutePath()+".jpg");
//				FileOutputStream out = new FileOutputStream(outputFile);
//
//
//				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//				JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
//				param.setQuality(0.9f,false); // 90% quality JPEG
//				encoder.setJPEGEncodeParam(param);
//				encoder.encode(img);
//				printJPEG = false;
//				out.close();
//			}
//			catch ( IOException e )
//			{
//				System.out.println("Error encoding JPEG!");
//			}
			printJPEG = false;
		}
	}
}

