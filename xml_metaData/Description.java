package xml_metaData;

/**
 *
 * @author Michael Menden
 *
 */
public class Description
{
	
	private String id    = "";
	private String name  = "";
	private String value = "";
	private String units = "";
	
	/**
	 * Constructor.
	 */
	public Description( String id, String value)
	{
		this.id = id;
		this.value = value;
	}
	
	/**
	 * Constructor.
	 */
	public Description( String id, String name, String value)
	{
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	public Description( String id, String name, String value, String units)
	{
		this.id = id;
		this.name = name;
		this.value = value;
		this.units = units;
	}
	
	
	/**
	 * Get the identifier of the description field: treatment, time point,...
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Get the name of the description. In case of an treatment it could be "EGF"
	 */
	public String getName()
	{
		if(name==null)
			return "";
		return name;
	}
	
	/**
	 * Get the value.
	 */
	public String getValue()
	{
		if(value==null)
			return "";
		return value;
	}
	
	/**
	 * Get the units.
	 */
	public String getUnits()
	{
		if (units==null)
			return "";
		return units;
	}
	
	/** Determines if these two descriptions have identical fields
	 * @author BLM*/
	public boolean isSame(Description desc)
	{
		if(!id.equalsIgnoreCase(desc.getId()))
			return false;
		if(!name.equalsIgnoreCase(desc.getName()))
			return false;
		if(!value.equalsIgnoreCase(desc.getValue()))
			return false;
		if(!units.equalsIgnoreCase(desc.getUnits()))
			return false;
		return true;
	}
	
	public String toString()
	{
		if (!value.equalsIgnoreCase(""))
			return name+" ("+value+" "+units+")";
		return name;
	}
}
