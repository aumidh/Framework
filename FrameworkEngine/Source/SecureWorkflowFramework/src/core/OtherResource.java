package core;
import java.util.Date;
/**
 * This class holds the response for the nested service in case of decentralized WF
 * @author Sardar Hussain
 *
 */
public class OtherResource 
{
	public String service;
	public Date notBefore;
	public Date notAfter;
	public String id;
	public String obligation;
	public Resource resource;
	public OtherResource(String _service,Date _notBefore, Date _notAfter, String _id, String _obligation,Resource _resource)
	{
		service = _service;
		notBefore =_notBefore;
		notAfter = _notAfter;
		id =_id;
		obligation = _obligation;
		resource = _resource;
	}
}
