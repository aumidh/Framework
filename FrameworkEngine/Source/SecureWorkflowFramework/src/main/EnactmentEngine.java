package main;

import invoker.InvokerMain;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis2.AxisFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.StopWatch;
import utils.UtilPerformanceEvaluation;
import analyzer.Analyzer;
import analyzer.ExecutableStatus;
import analyzer.Workflow;
import authZEngine.AuthZEngine;
import authZEngine.AuthorizationEngine;


import core.ParamSettings;
import core.RequestResponse;
import core.ResourceParameter;
import core.Service;
/**
 * This function is the enacement engine component.
 * @author Sardar Hussain
 *
 */
public class EnactmentEngine {

	public ArrayList<Service> wfServices;
	public ArrayList<AuthorizationEngine> authEngines;
	public ArrayList<String> time;
	private StopWatch stopWatch;
	public EnactmentEngine()
	{
		wfServices=new ArrayList<Service>();
		authEngines=new ArrayList<AuthorizationEngine>();
		time=new ArrayList<String>();
		stopWatch=new StopWatch("EE");
	}
	/**
	 * The main function of the enactment engine.
	 * @param args The commandline arguments.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ParamSettings.readParamFile();
		EnactmentEngine engine=new EnactmentEngine();
		if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
		{
			System.out.println("Centralized Workflow");
			engine.readCentralizedXML();
			if(ParamSettings.mode.toLowerCase().equals("w_security"))
			{
				System.out.println("Mode: With Security" );
				engine.startCentralziedProcess();
			}
			else
			{
				System.out.println("Mode: With Out Security" );
				engine.startWOSecurityCentralizedProcess();
			}
		}
		else
		{
			System.out.println("DeCentralized Workflow");
			engine.readDecentralizedXML();
			if(ParamSettings.mode.toLowerCase().equals("w_security"))
			{
				System.out.println("Mode: With Security" );
				engine.startDecentralizedProcess();
			}
			else
			{
				System.out.println("Mode: With out Security" );
				engine.startDeCentWOSecurityExecProcess();
			}
		}
		
		engine.printTime();
		engine.DisplayServiceCount();
	}
	/**
	 * This function start the token request process for Centralized WF without security
	 * @throws Exception
	 */
	public void testCentralizedTokenGenerationProcess() throws Exception
	{
		AuthZEngine aEMain=new AuthZEngine(wfServices);
		ArrayList<RequestResponse> responses=null;
		ArrayList<Boolean> isAllowedStatuses;
		if(ParamSettings.securityModel.toLowerCase().equals("centralizedpull")) // For Scenario 1
		{
			isAllowedStatuses=aEMain.startCentralizedPullProcess();
		}
		else                                                                   // For decentralized{Push and Pull} model  
		{
			responses=aEMain.startCentralizedProcess();
			isAllowedStatuses=aEMain.getIsAllowedStatuses();
		}
	}
	/**
	 * This function start the process of execution for the Centralized WF.
	 * @throws Exception
	 */
	public void startCentralziedProcess() throws Exception 
	{
			// Authorization and Tokens gathering
			AuthZEngine aEMain=new AuthZEngine(wfServices);
			ArrayList<RequestResponse> responses=null;
			ArrayList<Boolean> isAllowedStatuses;
			if(ParamSettings.securityModel.toLowerCase().equals("centralizedpull")) // For Scenario 1
			{
				isAllowedStatuses=aEMain.startCentralizedPullProcess();
			}
			else                                                                   // For decentralized{Push and Pull} model  
			{
				responses=aEMain.startCentralizedProcess();
				isAllowedStatuses=aEMain.getIsAllowedStatuses();
			}
			if(wfServices.size()==isAllowedStatuses.size())
			{
				Hashtable<String,Boolean> ht=makeAuthDecisionRepository(isAllowedStatuses);
				ExecutableStatus execStatus=Analyzer.getExecutionStatus(ht);
				if(execStatus==ExecutableStatus.Executable || execStatus==ExecutableStatus.Potential_Executable)
				{
					if(ParamSettings.securityModel.toLowerCase().equals("centralizedpull"))
					{
						System.err.println("Centralized Pull");
						// Invoke for the message....
						InvokerMain invoker=new InvokerMain(wfServices);
						invoker.startCentralizedExecProcess(ParamSettings.DN_Engine);
					}
					else
					{
						// Start Execution if the analyzer result is "Executable.
						InvokerMain invoker=new InvokerMain(wfServices, responses);
						invoker.startCentralizedDistanceWFExecProcess(); // For gulucose scenario
					}
				}
				else
				{
					System.out.println("The workflow isn't executable.");
				}
			}
			else
			{
				System.out.println("The number of services and the responses collected from AuthZEngin isn't euqal");
			}
	}
	/**
	 * This function retreive the authroizationdecision of all services  for the Decentralized WF.
	 * @return The authorization decisions
	 */
	public Hashtable<String,Boolean> makeDistributedAuthDecisionRepository()
	{
		Hashtable<String,Boolean> ht=new Hashtable<String, Boolean>();
		for(int i=0;i<wfServices.size();i++)
		{
			Service ser=wfServices.get(i);
			System.out.println("Service Name: " + ser.serviceName);
			if(ht.size() > 0 && ht.containsKey(ser.serviceName)==true)
				continue;
			ht.put(ser.serviceName.toLowerCase(),ser.isExecutable);
			for(int j=0;j<ser.nestedServices.size();j++)
			{
				Service nesSer=ser.nestedServices.get(j);
				System.out.println("Nested Service: " + nesSer.serviceName);
				if(ht.size() > 0 && ht.containsKey(nesSer.serviceName)==true)
					continue;
				ht.put(nesSer.serviceName.toLowerCase(),nesSer.isExecutable);
			}
		}
		return ht;
	}
	/**
	 * This function retreive the autorization decision of all services for the centralized WF.
	 * @param isAllowedStatuses the execution statuess of services.
	 * @return The authorization decisions
	 */
	public Hashtable<String,Boolean> makeAuthDecisionRepository(ArrayList<Boolean> isAllowedStatuses)
	{
		Hashtable<String,Boolean> ht=new Hashtable<String, Boolean>();
		for(int i=0;i<wfServices.size();i++)
		{
			System.out.println(wfServices.get(i).serviceName);
			if(ht.size() > 0 && ht.containsKey(wfServices.get(i).serviceName)==true)
				continue;
			ht.put(wfServices.get(i).serviceName.toLowerCase(),isAllowedStatuses.get(i));
		}
		System.out.println("Size of ht:" + ht.size());
		return ht;
	}
	/**
	 * This function start the Decentralized WF process with out security.
	 * @throws Exception
	 */
	public void startDeCentWOSecurityExecProcess() throws Exception
	{
		System.out.println("Decentralized Execution Started .......");
		InvokerMain invoker=new InvokerMain(wfServices);
		invoker.startDeCentWOSecurityExecProcess();
	}
	/**
	 * This function start the centralized WF process with out security.
	 * @throws Exception
	 */
	public void startWOSecurityCentralizedProcess() throws Exception
	{
		StopWatch sw=new StopWatch();
		UtilPerformanceEvaluation.recordColumns("S.No,Time");
		sw.start();
		InvokerMain invoker=new InvokerMain(wfServices);
		invoker.startCentWOSecurityProcess(); 
		sw.stop();
		UtilPerformanceEvaluation.recordEntry(sw.getLastTaskTimeMillis()+"");
	}
	
	/**
	 * This function start the Decentralized WF process with security
	 * @throws Exception
	 */
	public void startDecentralizedProcess() throws Exception
	{
		// Authorization and Tokens gathering
		AuthZEngine aEMain=new AuthZEngine(wfServices);
		ArrayList<RequestResponse> responses=null;
		responses=aEMain.startDecentralizedProcess();
		// Verify from Analyzer
		Hashtable<String,Boolean> ht=makeDistributedAuthDecisionRepository();
		ExecutableStatus execStatus=Analyzer.getExecutionStatus(ht);
		if(execStatus==ExecutableStatus.Executable || execStatus==ExecutableStatus.Potential_Executable)
		{
			// Start Execution if the analyzer result is "Executable.
			InvokerMain invoker=new InvokerMain(wfServices, responses);
			invoker.startDeCentExecProcess(); // For gulucose scenario
		}
		else
		{
			System.out.println("The workflow isn't executable.");
		}
	}
//	public void startDecentralizedProcess() throws AxisFault, InterruptedException
//	{
//		System.out.println("Requesting Tokens .......");
//		for (int i=0;i<wfServices.size();i++)
//		{
//			Service service=wfServices.get(i);
//			AuthorizationEngine client=new AuthorizationEngine(wfServices.get(i),params.currUser);
//			client.requestToken();
//			// Get Other tokens
//			for (int j=0;j<service.nestedServices.size();j++)
//			{
//				Service nestSrv=service.nestedServices.get(j);
//				AuthorizationEngine nestClient=new AuthorizationEngine(nestSrv,params.currUser);
//				nestClient.requestToken();
//				client.tokenResponse.otherResources.add(nestClient.tokenResponse);
//				//System.out.println("Not Before :    " + nestClient.tokenResponse.notBefore);
//			}
//			authEngines.add(client);
//			System.out.println("Total Number of Nested Service " + service.nestedServices.size());
//		}
//		System.out.println("Requesting Executions .......");
//		for (int j=0;j<authEngines.size();j++)
//		{
//			AuthorizationEngine client=authEngines.get(j);
//			client.executeDistributedAction();
//		}
//	}
	public void printTime()
	{
		System.out.println("---------Timing-------");
		for (int i=0;i<time.size();i++)
		{
			System.out.println(time.get(i));
		}
	}
	/**
	 * This function read the Decentralized WF XML file
	 */
	public void readDecentralizedXML()
	{
		try {
			 
			File fXmlFile = new File(ParamSettings.workflowFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
	 
			NodeList nList = doc.getElementsByTagName("Service");
			System.out.println("-----------------------");
	 
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
	 		   Node nNode = nList.item(temp);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			   {
	 		      Element eElement = (Element) nNode;
	 		      String endpoint="",action="",namespace="";
	 		      endpoint = getTagValue("EndPoint", eElement);
	 		      action =  getTagValue("ActionName", eElement);
	 		      namespace=getTagValue("Namespace", eElement);
	 		      Element parameters =(Element)getChildElement("Parameters",eElement);
	 		      NodeList childNodes = parameters.getElementsByTagName("Parameter");
	 		      Service sr=new Service(endpoint,action,namespace);
	 		     wfServices.add(sr);
	 		      for (int i=0;i<childNodes.getLength();i++)
	 		      {
	 		    	  Node parameter=childNodes.item(i);
	 		    	  if (parameter.getNodeType() == Node.ELEMENT_NODE) 
	 				  {
	 		 		      Element pElement = (Element) parameter;
	 		 		      String name= getTagValue("Name", pElement);
	 		 		      String value=getTagValue("Value", pElement);
	 		 		      ResourceParameter param=new ResourceParameter(name,value);
	 		 		      sr.addParameter(param);
	 		 		  }
	 		      }
	 		      // Get Other Services
	 		      Element OtherServices =(Element)getChildElement("OtherServices",eElement);
 		      	  NodeList nestedServices=OtherServices.getElementsByTagName("ChildService");
 		      	  for (int i=0;i<nestedServices.getLength();i++)
	 		      {
	 		    	  Node otherService=nestedServices.item(i);
	 		    	  if (otherService.getNodeType() == Node.ELEMENT_NODE) 
	 				  {
	 		 		      Element oeElement = (Element) otherService;
	 		 		      String oeEndpoint = getTagValue("EndPoint", oeElement);
	 		 		      String oeAction =  getTagValue("ActionName", oeElement);
	 		 		      String oenamespace=getTagValue("Namespace", oeElement);
	 		 		      Service oeService=new Service(oeEndpoint,oeAction,oenamespace);
	 		 		      sr.addService(oeService);
	 		 		      Element oeParameters =(Element)getChildElement("Parameters",oeElement);
	 		 		      NodeList oEParameterchildNodes = oeParameters.getElementsByTagName("Parameter");
	 		 		     for (int k=0;k<oEParameterchildNodes.getLength();k++)
	 		 		      {
	 		 		    	  Node oeParameter=oEParameterchildNodes.item(k);
	 		 		    	  if (oeParameter.getNodeType() == Node.ELEMENT_NODE) 
	 		 				  {
	 		 		 		      Element oePElement = (Element) oeParameter;
	 		 		 		      String name= getTagValue("Name", oePElement);
	 		 		 		      String value=getTagValue("Value", oePElement);
	 		 		 		      ResourceParameter param=new ResourceParameter(name,value);
	 		 		 		      oeService.addParameter(param);
	 		 		 		  }
	 		 		      }
	 		 		  }
	 		      }
 		      }
			}
		  } catch (Exception e) {
			e.printStackTrace();
		  }
	}
	/**
	 * This function read the Centralied XML file.
	 */
	public void readCentralizedXML()
	{
		try {
			 
			File fXmlFile = new File(ParamSettings.workflowFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
	 
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("Service");
			//System.out.println("-----------------------");
	 
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
	 		   Node nNode = nList.item(temp);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			   {
	 		      Element eElement = (Element) nNode;
	 		      String endpoint="",action="",namespace="";
	 		      endpoint = getTagValue("EndPoint", eElement);
	 		      action =  getTagValue("ActionName", eElement);
	 		      namespace=getTagValue("Namespace", eElement);
	 		      //System.out.println(endpoint);
	 		      //System.out.println(action);
	 		      Element parameters =(Element)getChildElement("Parameters",eElement);
	 		      NodeList childNodes = parameters.getElementsByTagName("Parameter");
	 		      Service sr=new Service(endpoint,action,namespace);
	 		     wfServices.add(sr);
	 		      for (int i=0;i<childNodes.getLength();i++)
	 		      {
	 		    	  Node parameter=childNodes.item(i);
	 		    	  if (parameter.getNodeType() == Node.ELEMENT_NODE) 
	 				  {
	 		 		      Element pElement = (Element) parameter;
	 		 		      String name= getTagValue("Name", pElement);
	 		 		      String value=getTagValue("Value", pElement);
	 		 		      ResourceParameter param=new ResourceParameter(name,value);
	 		 		      sr.addParameter(param);
	 		 		  }
	 		      }
	 		   }
			}
		  } catch (Exception e) {
			e.printStackTrace();
		  }
	}
	/**
	 * Print the total number of services in the WF.
	 */
	public void DisplayServiceCount()
	{
		System.out.println(wfServices.size());
	}
	 private String getTagValue(String sTag, Element eElement) 
	 {
		 	NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		    Node nValue = (Node) nlList.item(0);
		    if(nValue==null)
		    {
		    	return "";
		    }
		 	return nValue.getNodeValue();
	 }
	 private Node getChildElement(String sTag, Element eElement) 
	 {
			NodeList nlList = eElement.getElementsByTagName(sTag);
		    Node node = (Node) nlList.item(0);
		 	return node;
	 }
}
