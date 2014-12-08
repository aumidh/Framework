package gla.sowf.services.core;
import java.util.Date;

public class Response 
{
	public String notBefore;
	public String notAfter;
	public String id;
	public String issuer;
	public String obligation;
	public String serviceEndPoint;
	public Resource resource;
	public Response(String _serviceEndPoint,String notBefore2, String notAfter2, String _id, String _issuer, String _obligation,Resource _resource)
	{
		notBefore =notBefore2;
		notAfter = notAfter2;
		id =_id;
		issuer = _issuer;
		obligation = _obligation;
		serviceEndPoint = _serviceEndPoint;
		resource =_resource;
	}
}
