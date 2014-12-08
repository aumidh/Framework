package authZEngine;

import java.util.ArrayList;

import org.apache.axis2.AxisFault;

import utils.StopWatch;
import utils.UtilPerformanceEvaluation;

import core.RequestResponse;
import core.Service;

/**
 * AuthorizationEngine Component of the framework
 * @author Sardar Hussain
 *
 */
public class AuthZEngine {
	public ArrayList<Service> wfServices;
	//public ArrayList<AuthorizationEngine> authEngines;
	public ArrayList<RequestResponse> responses;
	public ArrayList<Boolean> isAllowedStatuses;
	public AuthZEngine(ArrayList<Service> _wfservices)
	{
		wfServices=_wfservices;
		//authEngines=new ArrayList<AuthorizationEngine>();
		responses=new ArrayList<RequestResponse>();
		isAllowedStatuses=new ArrayList<Boolean>();
	}
	/**
	 * This function returns the execution statuses of all the services.
	 * @return
	 */
	public ArrayList<Boolean> getIsAllowedStatuses()
	{
		return isAllowedStatuses;
	}
	/**
	 * This function retreive execution statuses for all services of the Centralied WF with Pull security model 
	 * @return The list of execution statuses for all services. 
	 * @throws AxisFault
	 */
	public ArrayList<Boolean> startCentralizedPullProcess() throws AxisFault
	{
		for (int i=0;i<wfServices.size();i++)
		{
			AuthorizationEngine client=new AuthorizationEngine(wfServices.get(i));
			boolean res=client.getAuthorizationDecision();
			isAllowedStatuses.add(res);
		}
		return isAllowedStatuses;
	}
	/**
	 * This function retreive token responses for the DeCentralized WF.
	 * @return The request responses
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public ArrayList<RequestResponse> startDecentralizedProcess()throws AxisFault, InterruptedException
	{
		System.out.println("Requesting Decentralized Tokens .......");
		for (int i=0;i<wfServices.size();i++)
		{
			Service service=wfServices.get(i);
			AuthorizationEngine client=new AuthorizationEngine(wfServices.get(i));
			client.requestToken();
			//authEngines.add(client);
			responses.add(client.getTokenResponse());
			service.isExecutable=client.getIsAllowedToExecute();
			// Get Other tokens
			for (int j=0;j<service.nestedServices.size();j++)
			{
				Service nestSrv=service.nestedServices.get(j);
				AuthorizationEngine nestClient=new AuthorizationEngine(nestSrv);
				nestClient.requestToken();
				nestSrv.isExecutable = nestClient.getIsAllowedToExecute();
				client.getTokenResponse().otherResources.add(nestClient.getTokenResponse());
			}
		}
		return responses;
	}
	/**
	 * This function retreive the token responses for the Centralized WF
	 * @return the list of token responses
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public ArrayList<RequestResponse> startCentralizedProcess() throws AxisFault, InterruptedException 
	{
		System.out.println("Requesting Tokens .......");
		for (int i=0;i<wfServices.size();i++)
		{
			AuthorizationEngine client=new AuthorizationEngine(wfServices.get(i));
			client.requestToken();
			//authEngines.add(client);
			responses.add(client.getTokenResponse());
			isAllowedStatuses.add(client.getIsAllowedToExecute());
		}
		return responses;
	}
}
