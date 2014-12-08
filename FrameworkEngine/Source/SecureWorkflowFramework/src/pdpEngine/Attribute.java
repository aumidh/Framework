package pdpEngine;
/**
 * This class holds the attribute value required for the PDP policy.
 * @author Sardar Hussain
 *
 */
public class Attribute 
{
	private String _name;
	private String _value;
	public Attribute(String name,String value)
	{
		_name=name;
		_value=value;
	}
	public String getName()
	{
		return _name;
	}
	public String getValue()
	{
		return _value;
	}
}
