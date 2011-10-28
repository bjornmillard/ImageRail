package midasGUI;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import models.Model_Plate;
import models.Model_Well;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sdcubeio.ExpDesign_Description;
import sdcubeio.ExpDesign_IO;
import sdcubeio.ExpDesign_Model;
import sdcubeio.ExpDesign_Sample;

public class MetadataParser {

	public int id_plate;
	
	
	static public ExpDesign_Model parse_IncuCyteXML(String xmlPath, Model_Plate plate, ExpDesign_Model model )
	{
		ArrayList<ExpDesign_Sample> samples = parseSamples(xmlPath, plate);
		
		//Transfering the results to SDCube format
		int len = samples.size();
		for(int i = 0; i< len; i++)
		{
			model.removeSample(samples.get(i).getId());
			model.addSample(samples.get(i));	
		}
		
		return model;
	}

	
	static public ArrayList<ExpDesign_Sample> parseSamples(
String xmlPath, Model_Plate plate) {

		ArrayList<ExpDesign_Sample> samples = new ArrayList<ExpDesign_Sample>();
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using builder to get DOM representation of the XML file
			dom = db.parse(xmlPath);

		} catch (Exception e) {
			System.out.println("Error parsing XML and creating dom object");
			return null;
		}

		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("well");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				// add it to list
				samples.add(parseSample(el, plate));
			}
		}
		return samples;
	}
	
	static public ExpDesign_Sample parseSample(Element el, Model_Plate plate) {

		String row = el.getAttribute("row");
		String col = el.getAttribute("col");
//		System.out.println(row+","+col);		
//		System.out.println(plate.getWells().length+ ","+plate.getWells()[0].length);
//		System.out.println(plate.getNumRows()+ ","+plate.getNumColumns());

		
		Model_Well well = plate.getWells()[Integer.parseInt(row)][Integer.parseInt(col)];

		ExpDesign_Sample sample = new ExpDesign_Sample();
		sample.setId(well.getID().trim());

		
		NodeList nl = el.getElementsByTagName("items");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				
				Element el2 = (Element) nl.item(i);
				NodeList nl2 = el2.getElementsByTagName("wellItem");
				if (nl2 != null && nl2.getLength() > 0) {
					for (int j = 0; j < nl2.getLength(); j++) {
					
						Element el3 = (Element) nl2.item(j);
						String type = el3.getAttribute("type");						
						System.out.println("type = "+type);

						//Parsing all compounds
						if(type.equalsIgnoreCase("Compound"))
						{
							String conc = el3.getAttribute("concentration");
							String concUnits = el3.getAttribute("concentrationUnits");
							String description = "";						
							String displayName = "";						
							String colorArgb = "";		
							
							NodeList nl4 = el3.getElementsByTagName("referenceItem");
							if (nl4 != null && nl4.getLength() > 0) {
								for (int x = 0; x < nl4.getLength(); x++) {
								
									Element el4 = (Element) nl4.item(x);
									 description = el4.getAttribute("description");						
									 displayName = el4.getAttribute("displayName");						
									 colorArgb = el4.getAttribute("colorArgb");						
								}
							}
							
							System.out.println("description = "+description);
							System.out.println("displayName = "+displayName);
							System.out.println("colorArgb = "+colorArgb);
							System.out.println("conc = "+conc);
							System.out.println("concUnits = "+concUnits.trim());
							
							ExpDesign_Description desc  = new ExpDesign_Description();
							desc.setType("Treatment");
							desc.setCategory("Compound");
							desc.setName(displayName);
							desc.setValue(conc);
							desc.setUnits(concUnits.trim());
//							desc.setUnits("testUnits");

							sample.addDescription(desc);
						}
						//Parsing CellTypes
						else if (type.equalsIgnoreCase("CellType"))
						{
							String passage = el3.getAttribute("passage");
							String seedingDensity = el3.getAttribute("seedingDensity");
							String seedingDensityUnits = el3.getAttribute("seedingDensityUnits");
							String description = "";						
							String displayName = "";						
							String colorArgb = "";						

							NodeList nl4 = el3.getElementsByTagName("referenceItem");
							if (nl4 != null && nl4.getLength() > 0) {
								for (int x = 0; x < nl4.getLength(); x++) {
								
									Element el4 = (Element) nl4.item(x);
									description = el4.getAttribute("description");						
									displayName = el4.getAttribute("displayName");						
									colorArgb = el4.getAttribute("colorArgb");						
								}
							}
							System.out.println("passage = "+passage);
							System.out.println("seedingDensity = "+seedingDensity);
							System.out.println("seedingDensityUnits = "+seedingDensityUnits);
							System.out.println("description = "+description);
							System.out.println("displayName = "+displayName);
							System.out.println("colorArgb = "+colorArgb);	
							
							ExpDesign_Description desc  = new ExpDesign_Description();
							desc.setType("Treatment");
							desc.setCategory("CellType");
							desc.setName(displayName);
							desc.setValue(seedingDensity);
							desc.setUnits(seedingDensityUnits.trim());

							sample.addDescription(desc);
						}
						//Parsing GrowthCondition
						else if (type.equalsIgnoreCase("GrowthCondition"))
						{
							String description = "";					
							String displayName = "";						
							String colorArgb = "";
							
							NodeList nl4 = el3.getElementsByTagName("referenceItem");
							if (nl4 != null && nl4.getLength() > 0) {
								for (int x = 0; x < nl4.getLength(); x++) {
								
									Element el4 = (Element) nl4.item(x);
									 description = el4.getAttribute("description");						
									 displayName = el4.getAttribute("displayName");						
									 colorArgb = el4.getAttribute("colorArgb");						
									
								}
							}
							System.out.println("description = "+description);
							System.out.println("displayName = "+displayName);
							System.out.println("colorArgb = "+colorArgb);

							ExpDesign_Description desc  = new ExpDesign_Description();
							desc.setType("Treatment");
							desc.setCategory("Compound");
							desc.setName(displayName);
							sample.addDescription(desc);
						}
						
											
					}


			}
		


			}
		}
		return sample;
	}
	
}
