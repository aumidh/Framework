package gla.sowf.module.core;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class Resource 
{
	public String methodName;
	public ArrayList<ResourceParameter> parameters;
	private static final Log log = LogFactory.getLog(ProtocolHandler.class);
	public Resource(String _name)
	{
		methodName = _name;
		parameters=new ArrayList<ResourceParameter>();
	}
	public void AddParameter(ResourceParameter _parameter)
	{ 
		parameters.add(_parameter);
	}
	public boolean areEquals(Resource _newResource)
	{
		if (methodName.equals(_newResource.methodName) == false)
			return false;
		if (parameters.size() != _newResource.parameters.size())
			return false;
		// The following code needed if parameter values need to be checked as well.
//		for (int i=0;i<parameters.size();i++)
//		{
//			ResourceParameter source=parameters.get(i);
//			ResourceParameter destination=_newResource.parameters.get(i);
//			if (source.name.equals(destination.name) == false || source.value.equals(destination.value) == false )
//				return false;
//		}
		return true;
	}
}
