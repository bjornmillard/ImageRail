package xml_metaData;

import xml_metaData.MetaDataXMLParser.MetaDataXMLParserException;
import hdf.ProjectHDFConnector;
import hdf.HDFConnector.HDFConnectorException;

/**
 *
 * @author Michael Menden
 *
 */
public class Demo {

	public static void main(String[] args) {
		
		// -------------- Create Project file --------------------------------------------------------
		try {
			
			// Project name.
			String projPath = "./experiment1";
			ProjectHDFConnector con = new ProjectHDFConnector( projPath);
			con.createProject();
			
			// write plate sizes.
			// parameters: plateIdx; plateSize
			con.writePlateSize( 0, 96);
			con.writePlateSize( 1, 96);
			con.writePlateSize( 2, 384);
			
			// write field height.
			// Parameters: plateIdx; wellIdx; fieldIdx; height
			con.writeFieldHeight( 0, 10, 0, 1000);
			
		} catch (HDFConnectorException e) {
			// Handle this exception!!!
			e.printStackTrace();
		}

		// -------------- Write MIDAS meta data -------------------------------------------------------
		String projPath = "./experiment1";
		try {
			// -------------------------------
			//           1. plate
			// -------------------------------
			int plateIdx = 0;
			MetaDataConnector con1 = new MetaDataConnector( projPath, plateIdx);
	
			// ----- 1. plate ----
			// 1. well
			
			int wellIdx = 10;
			con1.writeTreatment( wellIdx, "EGF", "10 mmol", "M");
			con1.writeTreatment( wellIdx, "EGFR", "12 mmol", "M");
			con1.writeTreatment( wellIdx, "FOO", "14 mmol", "M");

			// 2. well
			wellIdx = 15;
			con1.writeTreatment( wellIdx, "EGF", "2 mmol", "M");
			con1.writeTreatment( wellIdx, "EGFR", "5 mmol", "M");
			con1.writeTreatment( wellIdx, "FOO", "9 mmol", "M");
			con1.writeMeasurement( wellIdx, "intensity");

			// 3. well
			wellIdx = 20;
			con1.writeTreatment( wellIdx, "EGF", "2 mmol", "M");
			con1.writeTreatment( wellIdx, "EGFR", "5 mmol", "M");
			con1.writeTreatment( wellIdx, "FOO", "9 mmol", "M");
			con1.writeDate( wellIdx, "10/10/2009");
			con1.writeDescription( wellIdx, "blub...");
			con1.writeTimePoint( wellIdx, "10 min");
			
			// Write the meta data from RAM to XML file.
			con1.writeMetaDataXML();
			
			// -------------------------------
			//           2. plate
			// -------------------------------
			plateIdx = 1;
			MetaDataConnector con2 = new MetaDataConnector( projPath, plateIdx);
			// 1. well
			
			wellIdx = 20;
//			con2.writeTreatment( wellIdx, "EGF", "2 mmol");
//			con2.writeTreatment( wellIdx, "EGFR", "5 mmol");
//			con2.writeTreatment( wellIdx, "FOO", "9 mmol");
			con2.writeMeasurement( wellIdx, "intensity");
			con2.writeDate( wellIdx, "10/10/2009");
			con2.writeDescription( wellIdx, "blub...");
			con2.writeTimePoint( wellIdx, "10 min");
			
			// Drop examples:
//			con2.dropTreatment( wellIdx, "EGF");
//			con2.dropTreatment( wellIdx, "EGFR");
//			con2.dropTreatment( wellIdx, "FOO");
//			con2.dropMeasurement( wellIdx, "intensity1");
//			con2.dropDate( wellIdx);
//			con2.dropDescription( wellIdx);
//			con2.dropTimePoint( wellIdx);
		
			
			// Write the meta data from RAM to XML file.
			con2.writeMetaDataXML();
		
		} catch (MetaDataXMLParserException e) {
			// Handle this exception!!!
			e.printStackTrace();
		}
		
		// -------------- Read MIDAS meta data -------------------------------------------------------
		try {
			
			int plateIdx = 1;
			MetaDataConnector con = new MetaDataConnector( projPath, plateIdx);
			
			int wellIdx = 20;
			
			// time point
			Description timePoint = con.readTimePoint( wellIdx);
			plotDesc( plateIdx, wellIdx, timePoint);
			
			// description
			Description desc = con.readDescription( wellIdx);
			plotDesc( plateIdx, wellIdx, desc);
			
			// date
			Description date = con.readDate( wellIdx);
			plotDesc( plateIdx, wellIdx, date);
			
			// treatments
			Description[] treatments = con.readTreatments( wellIdx);
			plotDesc( plateIdx, wellIdx, treatments);
			
			// measurements
			Description[] measurements = con.readMeasurements( wellIdx);
			plotDesc( plateIdx, wellIdx, measurements);
			
		} catch (MetaDataXMLParserException e) {
			// Handle this exception!!!
			e.printStackTrace();
		}
	}
	
	public static void plotDesc( int plateIdx, int wellIdx, Description[] desc) {
		for (int i=0; i<desc.length; i++) {
			System.out.println("plateIdx: " + plateIdx + "\twellIdx: " + wellIdx
					            + "\tid: " + desc[i].getId() + "\tname: " + desc[i].getName()
					            + "\tvalue: " + desc[i].getValue());
		}
		System.out.println("----------------------------------------------------------------------------\n");
	}

	public static void plotDesc( int plateIdx, int wellIdx, Description desc) {
		if (desc != null)
			System.out.println("plateIdx: " + plateIdx + "\twellIdx: " + wellIdx + "\tid: "
								+ desc.getId() + "\tname: " + desc.getName()
								+ "\tvalue: " + desc.getValue());
		System.out.println("----------------------------------------------------------------------------\n");
	}
}
