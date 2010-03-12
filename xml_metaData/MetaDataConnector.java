package xml_metaData;

import hdf.ProjectHDFConnector;
import idx2coordinates.IdxConverter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import xml_metaData.MetaDataXMLParser.MetaDataXMLParserException;

/**
 *
 * @author Michael Menden
 *
 */
public class MetaDataConnector
{
	
	private String projPath;
	private int plateIdx;
	private MicrotiterPlate plate;
	
	/**
	 * Constructor.
	 * @throws MetaDataReaderException
	 */
	public MetaDataConnector(String projPath_, int plateIdx_) throws MetaDataXMLParserException
	{
		this.projPath = projPath_+"/Data";
		this.plateIdx = plateIdx_;
		// Load meta data, if XML file already exists.
		try
		{
			MetaDataXMLParser reader = new MetaDataXMLParser( projPath, plateIdx);
			plate = reader.getPlate();
		}
		catch (FileNotFoundException e)
		{
			File dir = new File(projPath);
			if(!dir.exists())
				dir.mkdir();
			
			(new File( this.projPath + "/plate_" + plateIdx)).mkdir();
			plate = new MicrotiterPlate();
		}
	}
	
	/**
	 * Overwrite the meta data XML file.
	 */
	public void writeMetaDataXML()
	{
		try
		{
			FileWriter fstream = new FileWriter(projPath + "/plate_" + plateIdx + "/ExpMetaData.xml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\"?>\n");
			
			// get size of the plate (necessary for index to well name converter)
			ProjectHDFConnector projCon = new ProjectHDFConnector(new File(projPath).getParentFile().getAbsolutePath());
			int plateSize = projCon.readPlateSize( plateIdx);
			
			out.write("<plate>\n");
			ArrayList<Well> wells = plate.getWells();
			
			// well loop
			for (int j=0; j<wells.size(); j++)
			{
				out.write("\t<well id=\"" +  IdxConverter.index2well( wells.get(j).getIdx(), plateSize) + "\">\n");
				ArrayList<Description> descriptions = wells.get(j).getDescriptions();
				
				// description loop
				for (int k=0; k<descriptions.size(); k++)
				{
					Description desc =  descriptions.get(k);
					String line = "\t\t<" + desc.getId();
					if (desc.getName().compareTo("") != 0)
						line += " name=\"" + desc.getName() + "\"";
					if (desc.getValue().compareTo("") != 0)
						line += " value=\"" + desc.getValue() + "\"";
					if (desc.getUnits().compareTo("") != 0)
						line += " units=\"" + desc.getUnits() + "\"";
					line += "/>\n";
					out.write( line);
				}
				out.write("\t</well>\n");
			}
			out.write("</plate>\n");
			out.close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a treatment for a specific well.
	 */
	public void writeTreatment(int wellIdx, String name, String value, String units)
	{
		// Get the correct well object.
		Well well = getWell(  wellIdx);
		// Write Treatment to well.
		well.replaceDesc( new Description( "treatment", name, value, units));
	}
	
	/**
	 * Write the description for a specific well.
	 */
	public void writeDescription( int wellIdx, String value)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		// Write Description to well.
		well.replaceDesc( new Description( "description", value));
	}
	
	/**
	 * Write a measurement for a specific well.
	 */
	public void writeMeasurement( int wellIdx, String name)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		// Write measurement to well.
		well.replaceDesc( new Description( "measurement", name, ""));
	}
	
	/**
	 * Write the date for a specific well.
	 */
	public void writeDate( int wellIdx, String value)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		// Write date to well.
		well.replaceDesc( new Description( "date", value));
	}
	
	/**
	 * Write the time point for a specific well.
	 */
	public void writeTimePoint( int wellIdx, String value)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		// Write time point to well.
		well.replaceDesc( new Description( "time_point", value));
	}
	
	/**
	 * Get the correct well reference.
	 */
	private Well getWell( int wellIdx)
	{
		// Get Well.
		Well well = plate.getWell( wellIdx);
		if (well == null)
		{
			well = new Well( wellIdx);
			plate.addWell(well);
		}
		return well;
	}
	
	/**
	 * Drop a treatment for a specific well.
	 */
	public void dropTreatment( int wellIdx, String name)
	{
		// Drop Treatment.
		dropDesc( wellIdx, "treatment", name);
	}
	
	/**
	 * Drop the description for a specific well.
	 */
	public void dropDescription(  int wellIdx)
	{
		// Drop Description.
		dropDesc( wellIdx, "description", "");
	}
	
	/**
	 * Drop a measurement for a specific well.
	 */
	public void dropMeasurement(  int wellIdx, String name)
	{
		// Drop measurement.
		dropDesc( wellIdx, "measurement", name);
	}
	
	/**
	 * Drop the date for a specific well.
	 */
	public void dropDate( int wellIdx)
	{
		// Drop date.
		dropDesc( wellIdx, "date", "");
	}
	
	/**
	 * Drop the time point for a specific well.
	 */
	public void dropTimePoint( int wellIdx)
	{
		// Drop time point.
		dropDesc( wellIdx, "time_point", "");
	}
	
	/**
	 * Drop description.
	 */
	private void dropDesc( int wellIdx, String id, String name)
	{
		// Get Well.
		Well well = plate.getWell( wellIdx);
		if (well == null)
		{
			well = new Well( wellIdx);
			plate.addWell(well);
		}
		// Drop time point.
		well.dropDesc( id, name);
		// Check drop well
		if (well.getDescriptionSize() == 0)
		{
			plate.dropWell(wellIdx);
		}
	}
	
	/**
	 * Read treatments for a specific well.
	 */
	public Description[] readTreatments(  int wellIdx)
	{
		return readDescs( wellIdx, "treatment");
	}
	
	/**
	 * Read measurement for a specific well.
	 */
	public Description[] readMeasurements( int wellIdx)
	{
		return readDescs( wellIdx, "measurement");
	}
	
	/**
	 * Read descriptions for a specific well.
	 */
	private Description[] readDescs( int wellIdx, String id)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		ArrayList<Description> descriptions = well.getDescriptions();
		ArrayList<Description> descs = new ArrayList<Description>();
		for (int i=0; i<descriptions.size(); i++)
		{
			if (descriptions.get(i).getId().compareTo(id) == 0)
				descs.add(descriptions.get(i));
		}
		return (Description[]) descs.toArray(new Description[0]);
	}
	
	/**
	 * Read the description for a specific well.
	 */
	public Description readDescription( int wellIdx)
	{
		return readDesc( wellIdx, "description");
	}
	
	
	/**
	 * Read the date for a specific well.
	 */
	public Description readDate( int wellIdx)
	{
		return readDesc( wellIdx, "date");
	}
	
	/**
	 * Read the time point for a specific well.
	 */
	public Description readTimePoint(  int wellIdx)
	{
		return readDesc( wellIdx, "time_point");
	}
	
	/**
	 * Read a description for a specific well.
	 */
	private Description readDesc( int wellIdx, String id)
	{
		// Get the correct well object.
		Well well = getWell( wellIdx);
		ArrayList<Description> descriptions = well.getDescriptions();
		Description desc = null;
		for (int i=0; i<descriptions.size(); i++)
		{
			if (descriptions.get(i).getId().compareTo(id) == 0)
			{
				desc = descriptions.get(i);
				break;
			}
		}
		return desc;
	}
	
	/** Returns all the treatments within the given well as an ordered name (String) list
	 * @author BLM*/
	public ArrayList<String> getAllTreatmentNames(int pIndex, int wIndex)
	{
		ArrayList<String> arr = new ArrayList<String>();
		Description[] des = readTreatments(wIndex);
		if(des!=null&&des.length>0)
		{
			for (int i = 0; i < des.length; i++)
				arr.add(des[i].getName());
			Collections.sort(arr);
		}
		return arr;
	}
	/** Returns all the measurements within the given well as an ordered name (String) list
	 * @author BLM*/
	public ArrayList<String> getAllMeasurementNames(int pIndex, int wIndex)
	{
		ArrayList<String> arr = new ArrayList<String>();
		Description[] des = readMeasurements(wIndex);
		if(des!=null&&des.length>0)
		{
			for (int i = 0; i < des.length; i++)
				arr.add(des[i].getName());
			Collections.sort(arr);
		}
		return arr;
	}
	
	/**
	 * Drop description.
	 */
	public void dropTreatmentOrMeasurement( int plateIdx, int wellIdx, Description desc)
	{
		// Get Plate.
		if (plate == null)
		{
			plate = new MicrotiterPlate( );
		}
		// Get Well.
		Well well = plate.getWell( wellIdx);
		if (well == null)
		{
			well = new Well( wellIdx);
			plate.addWell(well);
		}
		// Drop time point.
		well.dropDesc( desc);
		// Check drop well
		if (well.getDescriptionSize() == 0)
		{
			plate.dropWell(wellIdx);
			// Check drop plate
			if (plate.getWellSize() == 0)
			{
//				proj.dropPlate(plateIdx);
			}
		}
	}
	
	
}
