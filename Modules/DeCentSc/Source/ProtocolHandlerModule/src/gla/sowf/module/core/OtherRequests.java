package gla.sowf.module.core;
import java.util.Hashtable;
import java.util.ArrayList;

public class OtherRequests 
{
	public static ArrayList<OtherMessageRequest> othersRequest=new ArrayList<OtherMessageRequest>();
	public OtherRequests()
	{
	} 
	public static void add(OtherMessageRequest _req)
	{
		othersRequest.add(_req);
	}
	public static void remove(OtherMessageRequest _req)
	{
		othersRequest.remove(_req);
	}
	public static int getSize()
	{
		return othersRequest.size();
	}
	public static OtherMessageRequest get(String _serviceName,String _methodName)
	{
		for(int i=0; i<othersRequest.size();i++)
		{
			OtherMessageRequest or=othersRequest.get(i);
			if( or.service.equals(_serviceName)== true && or.resource.methodName.equals(_methodName) == true)
			{
				return or;
			}
		}
		return null;
	}
}
