/**
 * SpecialGraph.java
 *
 * @author Created by Omnicore CodeGuide
 */

package plots3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;

public abstract class SpecialGraph
{
	/** Accepts a given data object and returns an appropriate Shape3D
	 * @author BLM*/
	public Shape3D getVisualization(DataSet data){return null;}
	/** Returns an appropriate appearance for the Shape3D object returned by the getVis() method
	 * @author BLM*/
	public Appearance getAppearance()
	{
		//allows for front and back rendering of the surface
		Appearance app = new Appearance();
//		TransparencyAttributes trans = new TransparencyAttributes();
//		trans.setTransparencyMode(TransparencyAttributes.FASTEST);
//		trans.setSrcBlendFunction(TransparencyAttributes.SCREEN_DOOR);
//		trans.setTransparency(0.5f);
//		app.setTransparencyAttributes(trans);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//		polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
		
		polyAttrib.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(polyAttrib);
		
		Material mat = new Material();
		mat.setAmbientColor(new Color3f(0.0f,0.0f,1.0f));
		mat.setDiffuseColor(new Color3f(0.7f,0.7f,0.7f));
		mat.setSpecularColor(new Color3f(0.7f,0.7f,0.7f));
		app.setMaterial(mat);
		
		return app;
	}
}

