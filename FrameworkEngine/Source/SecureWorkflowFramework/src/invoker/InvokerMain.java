package invoker;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;



import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.XMLUtils;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.w3c.dom.Element;

import com.ibm.wsdl.extensions.http.HTTPConstants;

import utils.StopWatch;
import utils.UtilPerformanceEvaluation;
import core.CommonFunctions;
import core.ParamSettings;
import core.ProtocolHandler;
import core.RequestResponse;
import core.ResourceParameter;
import core.Service;
/**
 * This class represent the Invoker component.
 * @author Sardar Hussain
 *
 */
public class InvokerMain {
	private ArrayList<Service> wfServices;
	private ArrayList<RequestResponse> responses;
	ProtocolHandler proHandler;
	String responseOutput="";
	public InvokerMain(ArrayList<Service> _wfServices,ArrayList<RequestResponse> _responses)
	{
		wfServices=_wfServices;
		responses=_responses;
		proHandler=new ProtocolHandler();
	}
	public InvokerMain(ArrayList<Service> _wfServices)
	{
		wfServices=_wfServices;
	}
	/**
	 * This function execute the Centralized Distance WF
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public void startCentralizedDistanceWFExecProcess() throws AxisFault, InterruptedException 
	{
		System.out.println("Distance WF Execution Started .......");
		int xA=0,xB=0,yA=0,yB=0;
		// Check if the workflow has 3 services as per the workflow graph.
		if(wfServices.size()!=3)
		{
			System.out.println("The workflow must contains 3 services");
			return;
		}
		
		String strOP=executeAction(wfServices.get(0), responses.get(0)); // Execute ServiceA
		// If ServiceA successfuly completed execution then it will return a point in the form x,y
		// which will be stored in strOP.
		if(strOP.isEmpty()==false) 
		{
			System.out.println("Output from ServiceA:" + strOP);
			String[] parts = strOP.split(",");
			if(parts.length==2)
			{
				xA=Integer.parseInt(parts[0]);
				yA=Integer.parseInt(parts[1]);
			}
			// Service B
			strOP=executeAction(wfServices.get(1), responses.get(1)); // Execute ServiceB
			// If ServiceB successfuly completed execution then it will return a point in the form x,y
			// which will be stored in strOP.
			if(strOP.isEmpty()==false)
			{
				System.out.println("Output from ServiceB:" + strOP);
				String[] parts1 = strOP.split(",");
				if(parts1.length==2)
				{
					xB=Integer.parseInt(parts1[0]);
					yB=Integer.parseInt(parts1[1]);
				}
				if(wfServices.get(2).parameters.size()==4)
				{
					wfServices.get(2).parameters.get(0).value=xA + "";
					wfServices.get(2).parameters.get(1).value=yA + "";
					wfServices.get(2).parameters.get(2).value=xB + "";
					wfServices.get(2).parameters.get(3).value=yB + "";
					strOP=executeAction(wfServices.get(2), responses.get(2));
					System.out.println("The final output(Distance) is:" + strOP);
				}
				else
				{
					System.out.println("The distance calculator service will have 4 parameters");
				}
			}
		}
	}
	/**
	 * This function execute the Any Centralized WF. So this function can be extended for customized 
	 * implementation of any WF.
	 * @throws Exception
	 */
	public void startCentExecProcess() throws Exception
	{
		//System.out.println("WF Execution Started .......");
		for(int i=0;i<wfServices.size();i++)
		{
			String strOP=executeAction(wfServices.get(i), responses.get(i)); // Execute ServiceA
			System.out.println(wfServices.get(i).serviceName + " Response:" + strOP);
		}
	}
	/**
	 * This function execute the Any DeCentralized WF. This function can be extended for customized 
	 * implementation of any Decentralized WF.
	 * @throws Exception
	 */
	public void startDeCentExecProcess() throws Exception
	{
		System.out.println("Decentralized WF Execution Started .......");
		for(int i=0;i<wfServices.size();i++)
		{
			String strOP=executeAction(wfServices.get(i), responses.get(i)); // Execute ServiceA
			System.out.println(wfServices.get(i).serviceName + " Response:" + strOP);
		}
	}
	/**
	 * This function execute The Decentralized WF without security.
	 * @throws Exception
	 */
	public void startDeCentWOSecurityExecProcess() throws Exception
	{
		System.out.println("Centralized WO Security WF Execution Started .......");
		for(int i=0;i<wfServices.size();i++)
		{
			String strOP=executeActionWOSecurity(wfServices.get(i)); // Execute ServiceA
			System.out.println(wfServices.get(i).serviceName + " Response:" + strOP);
		}
	}
	/**
	 * This function execute the centralized WF witout the involment of secuirty process.
	 * @throws Exception
	 */
	public void startCentWOSecurityProcess() throws Exception
	{
		System.out.println("Centralized WO Security WF Execution Started .......");
		for(int i=0;i<wfServices.size();i++)
		{
			String strOP=executeActionWOSecurity(wfServices.get(i)); // Execute ServiceA
			System.out.println(wfServices.get(i).serviceName + " Response:" + strOP);
		}
		//System.out.println("Execution time: " + sw.getLastTaskTimeMillis());
	}
	/**
	 * This function execute the centralized NIGM WF process. 
	 * @param _engineDN The DN of the enactment engine.
	 * @throws Exception
	 */
	public void startCentralizedExecProcess(String _engineDN) throws Exception
	{
		//System.out.println("WF Execution Started .......");
		for(int i=0;i<wfServices.size();i++)
		{
			String strOP=executeSc1Action(wfServices.get(i), _engineDN); 
			System.out.println(wfServices.get(i).serviceName + " Response:" + strOP);
		}
	}
	/**
	 * This function execute the centralized Distance WF.
	 * @param _engineDN
	 * @throws Exception
	 */
	public void startCentralizedExecSc1Process(String _engineDN) throws Exception
	{
		System.out.println("Scenario 1 - Execution Started .......");
		int xA=0,xB=0,yA=0,yB=0;
		// Check if the workflow has 3 services as per the workflow graph.
		if(wfServices.size()!=3)
		{
			System.out.println("The workflow must contains 3 services");
			return;
		}
		
		String strOP=executeSc1Action(wfServices.get(0), _engineDN); // Execute ServiceA
		// If ServiceA successfuly completed execution then it will return a point in the form x,y
		// which will be stored in strOP.
		if(strOP.isEmpty()==false) 
		{
			System.out.println("Output from A:" + strOP);
			String[] parts = strOP.split(",");
			if(parts.length==2)
			{
				xA=Integer.parseInt(parts[0]);
				yA=Integer.parseInt(parts[1]);
			}
			// Service B
			strOP=executeSc1Action(wfServices.get(1), _engineDN); // Execute ServiceB
			// If ServiceB successfuly completed execution then it will return a point in the form x,y
			// which will be stored in strOP.
			if(strOP.isEmpty()==false)
			{
				System.out.println("Output from B:" + strOP);
				String[] parts1 = strOP.split(",");
				if(parts1.length==2)
				{
					xB=Integer.parseInt(parts1[0]);
					yB=Integer.parseInt(parts1[1]);
				}
				if(wfServices.get(2).parameters.size()==4)
				{
					wfServices.get(2).parameters.get(0).value=xA + "";
					wfServices.get(2).parameters.get(1).value=yA + "";
					wfServices.get(2).parameters.get(2).value=xB + "";
					wfServices.get(2).parameters.get(3).value=yB + "";
					strOP=executeSc1Action(wfServices.get(2), _engineDN);
					System.out.println("The final output(Distance) is:" + strOP);
				}
				else
				{
					System.out.println("The distance calculator service will have 4 parameters");
				}
			}
		}
	}
	public String executeSc1Action(Service wfService,String _engineDN) throws Exception
	{
		try
		{
			ServiceClient sc=new ServiceClient();
			OperationClient op=sc.createClient(ServiceClient.ANON_OUT_IN_OP);
			// Message Context
			MessageContext outMsgCtx = new MessageContext();
			Options opts = outMsgCtx.getOptions();
			opts.setTo(new EndpointReference(wfService.endpointName));
			opts.setAction(wfService.actionName);
			
			// Envelope
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope envelope = fac.getDefaultEnvelope();
			OMNamespace omNs = fac.createOMNamespace(wfService.namespace, "ns1");
			OMElement method = fac.createOMElement(wfService.actionName.substring(4), omNs);
			
			//Loop on parameters
			for (int i=0;i<wfService.parameters.size();i++)
			{
				ResourceParameter param=wfService.parameters.get(i);
				OMElement value = fac.createOMElement(param.name, omNs);
				value.setText(param.value);
				method.addChild(value);
			}
			// Message
			DefaultBootstrap.bootstrap();
	        Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	        // Query attribute statement
	        AttributeStatement attstmt=CommonFunctions.getAttributeStatement();
	  
	        // Authentication attribute statement
	        Attribute att=CommonFunctions.getAttributeOnly("authNCrd_EE",_engineDN);
	        attstmt.getAttributes().add(att); 
	    	assertion.getAttributeStatements().add(attstmt);
	    	OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
			//////////////////////////////////////////////
			envelope.getBody().addChild(method);
			//System.out.println(envelope);
			///////////////////////////////////////////////////////
			outMsgCtx.setEnvelope(envelope);
			op.addMessageContext(outMsgCtx);
			
			// Execute the operation
			op.execute(true);
			//Thread.sleep(3000);
			MessageContext inMsgCtxt=op.getMessageContext("In");
			SOAPEnvelope response = inMsgCtxt.getEnvelope();
			responseOutput=getOutput(response);
			return responseOutput;
		}catch(AxisFault fault)
		{
			if(isNotAllowed(fault.getMessage())==true)
				System.err.println(wfService.serviceName + " Execution isn't allowed by the protocol handler");
		}
		catch(Exception ex)
		{
			System.err.println("Exception while execution: " + ex.getMessage());
		}
		return "";
	}
	private boolean isNotAllowed(String strResponse)
	{
		try 
		{
			OMElement payload = AXIOMUtil.stringToOM(strResponse);  
			if (payload != null && payload.getLocalName().equals("Assertion"))
			{
				Element assertionElement=XMLUtils.toDOM(payload);
				Assertion samlAssertion = CommonFunctions.getAssertionObject(assertionElement);
				String requestResult=CommonFunctions.getAttributeValue(samlAssertion,"RequestResult");
				if (requestResult.toLowerCase().equals("not allowed") == true)
				{
					return true;
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return false;
	}
	/**
	 * This function create the execution message for a service with Security.
	 * @param wfService The WF service
	 * @param response The token response for the service.
	 * @return The output of the WF Service
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public String executeAction(Service wfService, RequestResponse response) throws AxisFault, InterruptedException
	{
		if (response !=null && response.msgContext != null)
		{
			ServiceClient sc=new ServiceClient();
			OperationClient op=sc.createClient(ServiceClient.ANON_OUT_IN_OP);
			MessageContext outMsgCtx = new MessageContext();
			Options opts = outMsgCtx.getOptions();
			opts.setTo(response.msgContext.getTo());
			opts.setAction(opts.getAction());
			// Attachement if WFType is centralized
			if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
			{
				opts.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
                        Constants.VALUE_TRUE);
				//opts.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,TempDir);
				opts.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "20000");
				opts.setProperty(Constants.Configuration.ENABLE_SWA,Constants.VALUE_TRUE);
			}
			// Envelope 
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope envelope = fac.getDefaultEnvelope();
			//OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2", "ns1");
			OMNamespace omNs = fac.createOMNamespace(wfService.namespace, "ns1");
			OMElement method = fac.createOMElement(wfService.actionName.substring(4), omNs);
			//Loop on parameters
			for (int i=0;i<wfService.parameters.size();i++)
			{
				ResourceParameter param=wfService.parameters.get(i);
				OMElement value = fac.createOMElement(param.name, omNs);
				value.setText(param.value);
				method.addChild(value);
				// Attached to message
//				if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
//				{
//					FileDataSource dataSource = new FileDataSource(param.value); 
//					DataHandler dataHandler = new DataHandler(dataSource); 
//					outMsgCtx.addAttachment(param.name, dataHandler);
//					//System.out.println("In attachment process for: " + wfService.serviceName);
//				}
				//----------------------
			}
			if(ParamSettings.workflowType.toLowerCase().equals("")==true)
				proHandler.create2ndMessage(envelope,response);
			else
				proHandler.createDistributed2ndMessage(envelope,response,fac,omNs);
			
			//createFirstMessage(envelope);
			envelope.getBody().addChild(method);
			// -----------------------
			outMsgCtx.setEnvelope(envelope);
			op.addMessageContext(outMsgCtx);
			op.execute(true);
			//-----------------------------------------
			MessageContext inMsgCtxt=op.getMessageContext("In");
			SOAPEnvelope execResponse = inMsgCtxt.getEnvelope();
			System.out.println(execResponse);
			return getOutput(execResponse );
		}
		return "Objects are NULL";
	}
	/**
	 * This function creates the execution message without security.
	 * @param wfService The WF service.
	 * @return The out of the WF service function
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public String executeActionWOSecurity(Service wfService) throws AxisFault, InterruptedException
	{
		try
		{
			ServiceClient sc=new ServiceClient();
			OperationClient op=sc.createClient(ServiceClient.ANON_OUT_IN_OP);
			// Message Context
			MessageContext outMsgCtx = new MessageContext();
			Options opts = outMsgCtx.getOptions();
			opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.REUSE_HTTP_CLIENT, "true");
			opts.setTo(new EndpointReference(wfService.endpointName));
			opts.setAction(wfService.actionName);
			// Attachement if WFType is centralized
			if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
			{
				opts.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
                        Constants.VALUE_TRUE);
				//opts.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,TempDir);
				opts.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "20000");
				opts.setProperty(Constants.Configuration.ENABLE_SWA,Constants.VALUE_TRUE);
			}
			
			// Envelope
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope envelope = fac.getDefaultEnvelope();
			//OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2", "ns1");
			OMNamespace omNs = fac.createOMNamespace(wfService.namespace, "ns1");
			OMElement method = fac.createOMElement(wfService.actionName.substring(4), omNs);
			
			//Loop on parameters
			for (int i=0;i<wfService.parameters.size();i++)
			{
				ResourceParameter param=wfService.parameters.get(i);
				OMElement value = fac.createOMElement(param.name, omNs);
				value.setText(param.value);
				method.addChild(value);
				// Attached to message
				if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
				{
					FileDataSource dataSource = new FileDataSource(param.value); 
					DataHandler dataHandler = new DataHandler(dataSource); 
					outMsgCtx.addAttachment(param.name, dataHandler);
					System.out.println("In attachment process for: " + wfService.serviceName);
				}
				//----------------------
			}
			envelope.getBody().addChild(method);
			System.out.println(envelope);
			///////////////////////////////////////////////////////
			outMsgCtx.setEnvelope(envelope);
			op.addMessageContext(outMsgCtx);
			// Execute the operation
			op.execute(true);
			//Thread.sleep(3000);
			MessageContext inMsgCtxt=op.getMessageContext("In");
//			if(ParamSettings.workflowType.toLowerCase().equals("centralized"))
//			{
//				//getAttachements(wfService.serviceName,inMsgCtxt);
//			}
//			else
//				System.out.println("Else case:" + ParamSettings.workflowType.toLowerCase());
			SOAPEnvelope response = inMsgCtxt.getEnvelope();
			responseOutput=getOutput(response);
			//sc.cleanupTransport();
			//sc.cleanup();
			return responseOutput;
		}catch(AxisFault fault)
		{
			System.err.println("Exception while execution: " + fault.getMessage());
			
		}
		catch(Exception ex)
		{
			System.err.println("Exception while execution: " + ex.getMessage());
		}
		return "";
	}
	/**
	 * Download the attachment came with the response of the WF Service
	 * @param serviceName The service name
	 * @param ctx The message context
	 */
	public void getAttachements(String serviceName,MessageContext ctx)
	{
		try
		{
			if(ctx.attachments.getAllContentIDs().length > 0)
			{
				String[] ids=ctx.attachments.getAllContentIDs();
				for(int i=0;i<ids.length;i++)
				{
					 File graphFile = new File(System.getProperty("user.dir") + "//Attachments//" + serviceName + "_" + ids[i] + ".xml");
				     FileOutputStream outputStream = new FileOutputStream(graphFile);
				     DataHandler d=ctx.getAttachment(ids[i]);
				     d.writeTo(outputStream);
					 outputStream.flush();
					 outputStream.close();
					 System.out.println("Attachment is saved to disk");
				}
			}
			else
				System.out.println("No attachement found for service: " + serviceName);
		}catch(Exception ex)
		{
			System.out.println("Exception:" + ex.getMessage());
		}
	}
	/**
	 * This function extract the output value from the response of the WF Service function call.
	 * @param _response The response received from WF Service function call.
	 * @return
	 */
	public String getOutput(SOAPEnvelope _response)
	{
		try
		{
			OMElement method = _response.getBody().getFirstElement().getFirstElement();
			if(method!=null)
			{
				String result= method.getText().toString();
				if(result.isEmpty())
					return " Operation executed successfully";
				return result;
			}
		}catch(Exception ex)
		{
			System.err.println(ex.getMessage());
		}
		return "Objects are NULL";
	}
}
