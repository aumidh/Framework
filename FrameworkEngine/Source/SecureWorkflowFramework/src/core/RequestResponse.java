package core;
import java.util.ArrayList;

import org.apache.axis2.context.MessageContext;
/**
 * This class holds the response obtained from token request for a given service.
 * @author Sardar Hussain
 *
 */
public class RequestResponse 
{
	public String notBefore;
	public String notAfter;
	public String id;
	public String issuer;
	public String obligation;
	public MessageContext msgContext;
	public ArrayList<RequestResponse> otherResources;
	public RequestResponse(String _notBefore, String _notAfter, String _id, String _issuer, String _obligation,MessageContext _msgContext)
	{
		notBefore =_notBefore;
		notAfter = _notAfter;
		id =_id;
		issuer = _issuer;
		obligation = _obligation;
		msgContext = _msgContext;
		otherResources=new ArrayList<RequestResponse>();
	}
}
