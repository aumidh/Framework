package gla.sowf.module.core;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class MessageRequest 
{
	private static final Log log = LogFactory.getLog(ProtocolHandler.class);
	public Date notBefore;
	public Date notAfter;
	public String id; 
	public String issuer;
	public String obligation;
	public Resource resource;
	public MessageRequest(Date _notBefore, Date _notAfter, String _id, String _issuer, String _obligation,Resource _resource)
	{
		notBefore =_notBefore;
		notAfter = _notAfter;
		id =_id;
		issuer = _issuer;
		obligation = _obligation;
		resource = _resource;
	}
	public boolean isExecutable(Resource _resource)
	{
		Date dt=new Date();
		
		if (dt.after(notAfter) || dt.before(notBefore))
			return false;
		if (resource.areEquals(_resource) == false)
			return false;
		return true;
	}
}

