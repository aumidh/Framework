package core;
import java.util.ArrayList;
/**
 * This class holds information about the calling function of the service.
 * @author Sardar Hussain
 *
 */
public class Resource 
{
	public String methodName;
	public ArrayList<ResourceParameter> parameters;
	public Resource(String _name)
	{
		methodName = _name;
	}
	/**
	 * This function add one parameter to the service function.
	 * @param _parameter The parameter i.e. <name,value> pair in the form of ResrouceParameter
	 */
	public void AddParameter(ResourceParameter _parameter)
	{
		parameters.add(_parameter);
	}
	/**
	 * This function compare two resources i.e. functions of the service
	 *  to check whether they are same or not.
	 * @param _newResource The function to be checked with the current function
	 * @return The result of the comparison.
	 */
	public boolean areEquals(Resource _newResource)
	{
		if (this.methodName.equals(_newResource.methodName) == false)
			return false;
		if (this.parameters.size() != _newResource.parameters.size())
			return false;
		for (int i=0;i<parameters.size();i++)
		{
			ResourceParameter source=parameters.get(i);
			ResourceParameter destination=_newResource.parameters.get(i);
			if (source.name.equals(destination.name) == false || source.value.equals(destination.value) == false )
				return false;
		}
		return true;
	}
}
