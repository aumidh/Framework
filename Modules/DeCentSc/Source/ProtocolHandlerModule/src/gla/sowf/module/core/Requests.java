package gla.sowf.module.core;
import java.util.Hashtable;

public class Requests 
{
	public Hashtable<String,MessageRequest> requestsHT=new Hashtable<String,MessageRequest>();
	public Requests()
	{
	}
	public void addRequest(String _id,MessageRequest _request)
	{
		requestsHT.put(_id,_request);
	} 
	public void removeRequest(String _id)
	{
		requestsHT.remove(_id);
	}
	public boolean isExist(String _id)
	{
		return requestsHT.containsKey(_id);
	}
	public MessageRequest getRequest(String _id)
	{
		return requestsHT.get(_id);
	}
}
