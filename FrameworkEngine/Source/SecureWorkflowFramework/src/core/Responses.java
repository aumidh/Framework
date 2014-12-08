package core;
import java.util.ArrayList;
/**
 * The list of the responses obtained for all the services of the WF.
 * @author Sardar Hussain
 *
 */
public class Responses 
{
	ArrayList<RequestResponse> responseList=new ArrayList<RequestResponse>();
	public Responses()
	{
	}
	public void addResponse(RequestResponse rr)
	{
		responseList.add(rr);
	}
	public void removeResponse(RequestResponse rr)
	{
		responseList.remove(rr);
	}
}
