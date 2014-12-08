package core;
import java.util.ArrayList;
/**
 * This class represent one service of the WF.
 * @author Sardar Hussain
 *
 */
public class Service 
{
	public String endpointName;
	public String actionName;
	public String namespace;
	public String serviceName;
	public ArrayList<ResourceParameter> parameters;
	public boolean isExecutable;
	public ArrayList<Service> nestedServices;
	public Service(String _endpoint, String _name,String _namespace)
	{
		endpointName=_endpoint;
		namespace=_namespace;
		retrieveServiceName();
		actionName=_name;
		isExecutable=false;
		parameters=new ArrayList<ResourceParameter>();
		nestedServices=new ArrayList<Service>();
	}
	/**
	 * This function retreive service name from the uri of the service
	 */
	private void retrieveServiceName()
	{
		serviceName=endpointName.substring(endpointName.lastIndexOf('/')+1);
	}
	/**
	 * This function add one parameter to the function of the service.
	 * @param _parameter The parameter i.e. <name,value> pair in the form of ResrouceParameter
	 */
	public void addParameter(ResourceParameter _parameter)
	{
		parameters.add(_parameter);
	}
	/**
	 * This function will add one service to the list of nested service in the case of decentralized WF
	 * @param _service The new nested services to be added.
	 */
	public void addService(Service _service)
	{
		nestedServices.add(_service);
	}
}
