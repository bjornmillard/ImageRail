package xml_metaData;

import java.util.ArrayList;

/**
 *
 * @author Michael Menden
 *
 */
public class Well
{
	
	private int idx;
	private ArrayList<Description> descriptions;
	
	/**
	 * Constructor.
	 */
	public Well( int idx)
	{
		this.idx = idx;
		descriptions = new ArrayList<Description>();
	}
	
	/**
	 * Get the well index.
	 */
	public int getIdx()
	{
		return idx;
	}
	
	/**
	 * Replace the description.
	 */
	public void replaceDesc( Description desc)
	{
		dropDesc( desc.getId(), desc.getName());
		descriptions.add( desc);
	}
	
	/**
	 * Add a description to the well.
	 */
	public void addDesc( Description desc)
	{
		descriptions.add( desc);
	}
	
	/**
	 * Drop a description.
	 */
	public void dropDesc( String descId, String descName)
	{
		for (int i=0; i<descriptions.size(); i++)
		{
			if ( descId.compareTo( descriptions.get(i).getId()) == 0 &&
				descName.compareTo( descriptions.get(i).getName()) == 0)
			{
				descriptions.remove(i);
			}
		}
	}
	
	/**
	 * Drop a description.
	 */
	public void dropDesc(Description desc)
	{

		for (int i=0; i<descriptions.size(); i++)
		{
			if(desc!=null && descriptions.get(i)!=null)
				if (desc.isSame(descriptions.get(i)))
					descriptions.remove(i);

		}
	}
	
	/**
	 * Get all descriptions.
	 */
	public ArrayList<Description> getDescriptions()
	{
		return descriptions;
	}
	
	/**
	 * Get description size.
	 */
	public int getDescriptionSize()
	{
		return descriptions.size();
	}
}
