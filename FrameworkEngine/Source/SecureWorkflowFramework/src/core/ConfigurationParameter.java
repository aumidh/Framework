package core;
/**
 * This class holds a <name,value> pair for the project configuration.
 * @author Sardar Hussain
 *
 */
public class ConfigurationParameter {
	public String name;
	public String value;
	public ConfigurationParameter(String _name,String _value)
	{
		name=_name;
		value=_value;
	}
}
