package xml_metaData;

import hdf.HDFConnector.HDFConnectorException;
import hdf.ProjectHDFConnector;
import idx2coordinates.IdxConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Michael Menden
 *
 */
public class MetaDataXMLParser
{
	private MicrotiterPlate plate;
	
	/**
	 * Constructor.
	 * @throws MetaDataXMLParserException
	 * @throws FileNotFoundException
	 */
	public MetaDataXMLParser( String projectPath, int plateIdx) throws MetaDataXMLParserException, FileNotFoundException
	{
		FileReader xml = new FileReader( projectPath + "/plate_" + plateIdx + "/ExpMetaData.xml");
		ProjectXMLHandler handler = new ProjectXMLHandler(projectPath, plateIdx);
		XMLReader xr;
		try
		{
			xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			xr.parse(new InputSource(xml));
			plate = handler.getPlate();
		}
		catch (SAXException e)
		{
			throw new MetaDataXMLParserException("Cannot parse the XML meta data file: " + e.getMessage());
		}
		catch (IOException e)
		{
			throw new MetaDataXMLParserException("Cannot open the XML meta data file: " + e.getMessage());
		}
	}
	
	/**
	 * Get the project structure.
	 */
	public MicrotiterPlate getPlate()
	{
		return plate;
	}
	
	/**
	 * Nested class.
	 */
	public class MetaDataXMLParserException extends Exception
	{
		/**
		 * For serializing the Class
		 */
		private static final long serialVersionUID = 6671397455786252L;
		
		MetaDataXMLParserException(String msg)
		{
			super("MetaDataReaderException: " + msg);
		}
	}
	
	/**
	 * Nested class.
	 */
	class ProjectXMLHandler extends DefaultHandler
	{
		
		private final Logger logger = Logger.getLogger("xml.MetaDataParser.ProjectXMLHandler");
		
		private MicrotiterPlate plate;
		private int plateSize;
		
		private int wellID;
		private Well well;
		
		/**
		 * Constructor.
		 */
		public ProjectXMLHandler ( String projPath, int plateID)
		{
			// get size of the plate (necessary for index to well name converter)
			try
			{
				plateSize = (new ProjectHDFConnector(new File(projPath).getParentFile().getAbsolutePath())).readPlateSize(plateID);
			}
			catch (HDFConnectorException ex)
			{
				logger.log(Level.SEVERE, "XML meta data reader cannot read plate size: ", ex);
			}
		}
		
		/**
		 * Is called at the beginning of an XML tag.
		 */
		public void startElement(String namespaceURI, String localName,
								 String qName, Attributes attr)
		{
			if (qName.equals("plate"))
			{
				plate = new MicrotiterPlate();
			}
			else if (qName.equals("well"))
			{
				wellID = IdxConverter.well2index( attr.getValue("id"), plateSize);
				well = new Well(wellID);
				plate.addWell(well);
			}
			else
			{
				String descName = attr.getValue("name");
				String descValue = attr.getValue("value");
				String descUnits = attr.getValue("units");
				Description desc = null;
				if ( descName == null)
					desc = new Description(qName, descValue);
				else if ( descValue == null)
					desc = new Description(qName, descName, "");
				else if (descUnits == null)
					desc = new Description(qName, descName, descValue, "");
				else
					desc = new Description(qName, descName, descValue, descUnits);
				
				well.addDesc(desc);
			}
		}
		
		/**
		 * Return the result.
		 */
		public MicrotiterPlate getPlate()
		{
			return plate;
		}
	}
}
