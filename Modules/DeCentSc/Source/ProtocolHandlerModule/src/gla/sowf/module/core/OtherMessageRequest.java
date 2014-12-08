package gla.sowf.module.core;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class OtherMessageRequest
{
	public String service;
	public Date notBefore;
	public Date notAfter;
	public String id;
	public String issuer;
	public String obligation; 
	public Resource resource;
	public OtherMessageRequest(String _service,Date _notBefore, Date _notAfter, String _id, String _issuer, String _obligation,Resource _resource)
	{
		service = _service;
		notBefore =_notBefore;
		notAfter = _notAfter;
		id =_id;
		issuer = _issuer;
		obligation = _obligation;
		resource = _resource;
	}
}

