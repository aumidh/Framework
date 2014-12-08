package analyser;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * This class provides the hierarchical structure of the constructs that resembel the XML file structure.
 * The object of this class store one construct and their child constructs which would be a list of objects of
 * this class too and so on... 
 * @author Sardar Hussain
 *
 */
public class WFConstruct 
{
	private WFConstructsType type;
	private Element xmlElement;
	private List<WFConstruct> constructList;
	private String serviceName;
	private String serviceURI;
	private String id;
	private ExecutableStatus status;
	public WFConstruct(WFConstructsType _type,Element _xmlElement)
	{
		id="";
		type=_type;
		xmlElement=_xmlElement;
		constructList=new ArrayList<WFConstruct>();
		serviceName="";
		serviceURI="";
		status=ExecutableStatus.Unknown_Yet;
	}
	/**
	 * Set the status of the current WF construct
	 * @param _status the status of the construct
	 */
	public void setStatus(ExecutableStatus _status)
	{
		status=_status;
	}
	/**
	 * This function will return the execution status of the current construct
	 * @return the execution status.
	 */
	public ExecutableStatus getStatus()
	{
		return status;
	}
	/**
	 * This function will return the associated XML element of the construct
	 * @return The associated XML element.
	 */
	public Element getElement()
	{
		return xmlElement;
	}
	/**
	 * This function will add a construct to the list.
	 * @param _construct the construct which will be added.
	 */
	public void addConstruct(WFConstruct _construct)
	{
		constructList.add(_construct);
	}
	/**
	 * This function set the service tag attribute
	 * @param _name The name of the service.
	 * @param _uri The uri address of the service.
	 */
	public void setServiceTagAttribute(String _name,String _uri)
	{
		serviceName=_name;
		serviceURI=_uri;
	}
	/**
	 * The function set the id.
	 * @param _id The Id of the tag.
	 */
	public void setID(String _id)
	{
		id=_id;
	}
	/**
	 * Return the type of the construct.
	 * @return The type of the construct.
	 */
	public WFConstructsType getConstructType()
	{
		return type;
	}
	/**
	 * Return the name of the service.
	 * @return the service name
	 */
	public String getName()
	{
		return serviceName;
	}
	/**
	 * Return the uri of the service.
	 * @return The service uri
	 */
	public String getURI()
	{
		return serviceURI;
	}
	public String getID()
	{
		return id;
	}
	/**
	 * Get the list of all constructs.
	 * @return list of constructs
	 */
	public List<WFConstruct> getList()
	{
		return constructList;
	}
}
