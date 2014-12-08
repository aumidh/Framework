package authZEngine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.XMLUtils;
import org.jibx.runtime.QName;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import core.CommonFunctions;
import core.ConfigurationParameter;
import core.ParamSettings;
import core.RequestResponse;
import core.ResourceParameter;
import core.Service;
import credentialsMgmt.CredentialsMgmt;
import pdpEngine.PEPEngine;
import utils.UtilPerformanceEvaluation;
/**
 * This class provides functionality for each service of the WF to forumlate the token request.
 * And retrieve the token from the service. 
 * @author Sardar Hussain
 *
 */
public class AuthorizationEngine 
{
	public Service targetService;
	private RequestResponse tokenResponse=null;
	OMNamespace omNs = null;
	ServiceClient sc;
	CredentialsMgmt credMgmt;
	private boolean isAllowedToExecute;
	public AuthorizationEngine(Service _service) throws AxisFault
	{
		isAllowedToExecute=false;
		targetService=_service;
		sc=new ServiceClient();
		credMgmt=new CredentialsMgmt();
	}
	/**
	 * This function will return the retrieved authorization decision of service to the 
	 * calling component.
	 * @return The authorization decision of the associated service.
	 */
	public boolean getAuthorizationDecision()
	{
		ArrayList<ConfigurationParameter> list=credMgmt.getCredForService(targetService.serviceName);
		if(list.size()>0)
		{
			PEPEngine pep=new PEPEngine();
			return pep.evaluate(credMgmt.getIssuerAttribute(targetService.serviceName), list, targetService.serviceName,targetService.actionName);
		}
		else
			System.out.println("In else case");
		return false;
	}
	/**
	 * This function will return the token response received in response of request for the
	 * token for the associated service.
	 * @return The token response
	 */
	public RequestResponse getTokenResponse()
	{
		return tokenResponse;
	}
	/**
	 * This function will return whether the associated service is allowed to execute
	 * by the current user or not.
	 * @return The user is allowed to execute the service or not.
	 */
	public boolean getIsAllowedToExecute()
	{
		return isAllowedToExecute;
	}
	/**
	 * This function is part of the functions which is used to create soap message to request token
	 * from the protocol handler for the associated service.
	 * @param isQuery This flag represent whether this is a query request or not
	 * @return The OperationClient object,
	 * @throws AxisFault Any exception raised during the process.
	 */
	public OperationClient getOperationClient(boolean isQuery) throws AxisFault
	{
		OperationClient op=sc.createClient(ServiceClient.ANON_OUT_IN_OP);
		
		op.addMessageContext(createMessageContext(isQuery));
		return op;
	}
	/** This function is part of the functions which is used to create soap message to request token
	 * from the protocol handler for the associated service.
	 * @param isQuery This flag represent whether this is a query request or not
	 * @return The MessageContext object,
	 * @throws AxisFault Any exception raised during the process.
	 */
	public MessageContext createMessageContext(boolean isQuery) throws AxisFault
	{
		MessageContext outMsgCtx = new MessageContext();
		Options opts = outMsgCtx.getOptions();
		opts.setTo(new EndpointReference(targetService.endpointName));
		opts.setAction(targetService.actionName);
		outMsgCtx.setEnvelope(creatSOAPEnvelop(isQuery));
		return outMsgCtx;
	}
	/** This function is part of the functions which is used to create soap message to request token
	 * from the protocol handler for the associated service.
	 * @param isQuery This flag represent whether this is a query request or not
	 * @return The Soap Envelope object,
	 * @throws AxisFault Any exception raised during the process.
	 */
	public SOAPEnvelope creatSOAPEnvelop(boolean isQuery) 
	{
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();
		//omNs = fac.createOMNamespace("http://ws.apache.org/axis2", "ns1");
		omNs = fac.createOMNamespace(targetService.namespace, "ns1");
		OMElement method = fac.createOMElement(targetService.actionName.substring(4), omNs);
		
		//Loop on parameters
		for (int i=0;i<targetService.parameters.size();i++)
		{
			ResourceParameter param=targetService.parameters.get(i);
			OMElement value = fac.createOMElement(param.name, omNs);
			value.setText(param.value);
			method.addChild(value);
		}
		createFirstMessage(envelope,isQuery);
		envelope.getBody().addChild(method);
		//System.out.println(envelope);
		return envelope;
	}
	/** This function is part of the functions which is used to create soap message to request token
	 * from the protocol handler for the associated service.
	 *@param envelope The envelope for the request.
	 * @param isQuery This flag represent whether this is a query request or not
	 */
	public void createFirstMessage(SOAPEnvelope envelope,boolean isQuery)
	{
		try
		{
			DefaultBootstrap.bootstrap();
			
	        Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	        // Query attribute statement
	        AttributeStatement attstmt=CommonFunctions.getAttributeStatement();
	        if(isQuery==true)
	        {
	        	attstmt= CommonFunctions.getAttribute("Query","Am I allowed");
		        assertion.getAttributeStatements().add(attstmt);
	        }
	     // ---------------------------------------------------
	     // Authorization attribute statement
	        if (ParamSettings.securityModel.toLowerCase().equals("decentralizedpush")==true)
	        {
	        	String serviceName=targetService.endpointName.substring(targetService.endpointName.lastIndexOf("/")+1);
	        	//System.out.println(serviceName);
		        ArrayList<ConfigurationParameter> list=credMgmt.getCredForService(serviceName);
		        if(list.size()>0)
		        	attstmt=CommonFunctions.getAttributeStatement();
		        for(int i=0;i<list.size();i++)
		        {
		        	ConfigurationParameter param=list.get(i);
		        	Attribute att=CommonFunctions.getAttributeOnly(param.name,param.value);//"sowf_example_signature");
		        	attstmt.getAttributes().add(att);
		        }
		        assertion.getAttributeStatements().add(attstmt);
	        }
	        // ---------------------------------------------------
	        // Authentication attribute statement
	        attstmt=CommonFunctions.getAttributeStatement();
	        Attribute att=CommonFunctions.getAttributeOnly("authNCrd_EE",ParamSettings.DN_Engine);
	        attstmt.getAttributes().add(att); 
        	//if (ParamSettings.securityModel.toLowerCase().equals("pull")==true)
	        //{
		         att=CommonFunctions.getAttributeOnly("authNCrd_user",ParamSettings.DN_User);
		        attstmt.getAttributes().add(att); 
	        
//	        	attstmt= CommonFunctions.getAttribute("authNCrd_user",ParamSettings.DN_User);
//	        	assertion.getAttributeStatements().add(attstmt);
	        //}
		       assertion.getAttributeStatements().add(attstmt);
        	OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
	        
		}catch(Exception ex)
		{
			System.out.println("Exception in here " + ex.getMessage());
		}
	}
	/**
	 * This function creates the execution message which is used to call the service function.
	 * @param envelope The outgoing message envelope.
	 * @param id The id which is retreived earlier from the token response.
	 * @param fac The soapfactory object used to create elements for the envelope
	 * @param omNs The namespace for the current service
	 */
	public void createExecuteMessage(SOAPEnvelope envelope,String id,SOAPFactory fac, OMNamespace omNs)
	{
		try
		{
			DefaultBootstrap.bootstrap();
			
	        Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	
	        AttributeStatement attstmt= CommonFunctions.getAttribute("Id",id);
	        assertion.getAttributeStatements().add(attstmt);
	        for(int i=0;i<credMgmt.configParameters.size();i++)
	        {
	        	ConfigurationParameter param=credMgmt.configParameters.get(i);
	        	attstmt= CommonFunctions.getAttribute(param.name,param.value);//"sowf_example_signature");
	 	        assertion.getAttributeStatements().add(attstmt);
	        }
	        
	        // -----------------
	        OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
	        OMElement otherResource= fac.createOMElement("OtherResources", omNs);
	        // Add Dummy Other resources element
	        envelope.getHeader().addChild(otherResource);
		}catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
	/**
	 * This function parse the fault response obtained from the token request message.
	 * @param strResponse The message obtained from the token request
	 * @param msgContext The corresponding message context of the response message.
	 */
	private void parseAxisFaultResponse(String strResponse,MessageContext msgContext)
	{
		try 
		{
			OMElement payload = AXIOMUtil.stringToOM(strResponse);  
			if (payload != null && payload.getLocalName().equals("Assertion"))
			{
				Element assertionElement=XMLUtils.toDOM(payload);
				Assertion samlAssertion = CommonFunctions.getAssertionObject(assertionElement);
				String requestResult=CommonFunctions.getAttributeValue(samlAssertion,"RequestResult");
				if (requestResult.toLowerCase().equals("allowed") == true)
				{
					isAllowedToExecute=true;
					String notBefore=CommonFunctions.getAttributeValue(samlAssertion,"NotBefore");
					String notAfter=CommonFunctions.getAttributeValue(samlAssertion,"notAfter");
					String id=CommonFunctions.getAttributeValue(samlAssertion,"Id");
					String obligation=CommonFunctions.getAttributeValue(samlAssertion,"Obligation");
					String issuer=samlAssertion.getIssuer().getValue();
					tokenResponse=new RequestResponse(notBefore, notAfter, id, issuer, obligation, msgContext);
				}
				else
				{
					isAllowedToExecute=false;
					tokenResponse=null;
					System.err.println("Result " + requestResult);
				}
			}
			else
			{
				System.err.println("Unknown Result");
			}
			
		} catch (SAXException e) 
		{
			System.err.println(e.getMessage());
		} catch (IOException e) 
		{
			System.err.println(e.getMessage());
		} catch (ParserConfigurationException e) 
		{
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	/**
	 * This function is the interface method which is called from other class to initiate the
	 * process of requesting token.
	 * @throws AxisFault
	 * @throws InterruptedException
	 */
	public void requestToken()throws AxisFault, InterruptedException
	{
		OperationClient opClient=sc.createClient(ServiceClient.ANON_OUT_IN_OP);
		try
		{
			// Operation Client....
			MessageContext outMsgCtx = new MessageContext();
			Options opts = outMsgCtx.getOptions();
			opts.setTo(new EndpointReference(targetService.endpointName));
			opts.setAction(targetService.actionName);
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope envelope = fac.getDefaultEnvelope();
			omNs = fac.createOMNamespace(targetService.namespace, "ns1");
			OMElement method = fac.createOMElement(targetService.actionName.substring(4), omNs);
			//Loop on parameters
			for (int i=0;i<targetService.parameters.size();i++)
			{
				ResourceParameter param=targetService.parameters.get(i);
				OMElement value = fac.createOMElement(param.name, omNs);
				value.setText(param.value);
				method.addChild(value);
			}
			
			DefaultBootstrap.bootstrap();
			Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	        // Query attribute statement
	        AttributeStatement attstmt=CommonFunctions.getAttributeStatement();
	        attstmt= CommonFunctions.getAttribute("Query","Am I allowed");
		    assertion.getAttributeStatements().add(attstmt);
	     // ---------------------------------------------------
	     // Authorization attribute statement
	        if (ParamSettings.securityModel.toLowerCase().equals("decentralizedpush")==true)
	        {
	        	String serviceName=targetService.endpointName.substring(targetService.endpointName.lastIndexOf("/")+1);
	        	//System.out.println(serviceName);
		        ArrayList<ConfigurationParameter> list=credMgmt.getCredForService(serviceName);
		        if(list.size()>0)
		        	attstmt=CommonFunctions.getAttributeStatement();
		        for(int i=0;i<list.size();i++)
		        {
		        	ConfigurationParameter param=list.get(i);
		        	Attribute att=CommonFunctions.getAttributeOnly(param.name,param.value);//"sowf_example_signature");
		        	attstmt.getAttributes().add(att);
		        }
		        assertion.getAttributeStatements().add(attstmt);
	        }
	        // ---------------------------------------------------
	        // Authentication attribute statement
	        attstmt=CommonFunctions.getAttributeStatement();
	        Attribute att=CommonFunctions.getAttributeOnly("authNCrd_EE",ParamSettings.DN_Engine);
	        attstmt.getAttributes().add(att); 
	    	att=CommonFunctions.getAttributeOnly("authNCrd_user",ParamSettings.DN_User);
		    attstmt.getAttributes().add(att); 
		    assertion.getAttributeStatements().add(attstmt);
	    	OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
	        envelope.getBody().addChild(method);
			outMsgCtx.setEnvelope(envelope);
			opClient.addMessageContext(outMsgCtx);
			System.out.println("Envelope is:" + envelope);
			opClient.execute(true);
			MessageContext inMsgCtxt=opClient.getMessageContext("In");
			SOAPEnvelope response = inMsgCtxt.getEnvelope();
		}catch(AxisFault fault)
		{
			parseAxisFaultResponse(fault.getMessage(),opClient.getMessageContext("Out"));
		}
		catch(Exception ex)
		{
			System.err.println(" I am here");
		}
	}
	/**
	 * This function extract the value from a given XML element.
	 * @param sTag The XML tag name
	 * @param eElement The XML element 
	 * @return The value obtained from the element for the given tag name.
	 */
	private String getTagValue(String sTag, Element eElement) 
	 {
			NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		    Node nValue = (Node) nlList.item(0);
		 	return nValue.getNodeValue();
	 }
	/**
	 * Get the child element from the XML element for the given tag name.
	 * @param sTag The child tag name
	 * @param eElement The parent XML element
	 * @return The child XML node for the given tag name.
	 */
	 private Node getChildElement(String sTag, Element eElement) 
	 {
			NodeList nlList = eElement.getElementsByTagName(sTag);
		    Node node = (Node) nlList.item(0);
		 	return node;
	 }
	 /**
	  * This function creates the SOAP envelope for the associate service with the current class.
	  * @param endPoint The uri of the service
	  * @param id The service id
	  * @param actionName The corresponding action name which will be triggered.
	  * @param _names The list of the parameter names.
	  * @param _value The list of the parameter values.
	  * @return The soap envelope.
	  */
	public SOAPEnvelope creatOPSOAPEnvelop(String endPoint,String id,String actionName,ArrayList<String> _names,ArrayList<String> _value) 
	{
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();
		SOAPHeader header=envelope.getHeader();
		OMNamespace omNs = fac.createOMNamespace(targetService.namespace, "ns1");
		OMElement method = fac.createOMElement(actionName, omNs);
		
		//Loop on parameters
		for (int i=0;i<_names.size();i++)
		{
			String name=(String)_names.get(i);
			String value=(String)_value.get(i);
			OMElement _param = fac.createOMElement(name, omNs);
			_param.setText(value);
			method.addChild(_param);
		}
		createExecuteMessage(envelope,id,fac,omNs);
		envelope.getBody().addChild(method);
		return envelope;
	}
	/**
	 * This function creates the Operation client object, which dynamically call a given service function
	 * @param otherResources The XML element for the other resources in the Decentralized WF case.
	 * @throws Exception
	 */
	public void createOPClient(OMElement otherResources) throws Exception
	{
		if(otherResources.getChildElements().hasNext())
		{
			ServiceClient sClient=new ServiceClient();
			OperationClient op=sClient.createClient(ServiceClient.ANON_OUT_IN_OP);
			MessageContext outMsgCtx = new MessageContext();
			Options opts = outMsgCtx.getOptions();
			
			Iterator trOR=otherResources.getChildElements();
			OMElement nextService=(OMElement)trOR.next();
		
			Element service=XMLUtils.toDOM(nextService);
			String endpointName=getTagValue(service.getPrefix() + ":Service", service);
			opts.setTo(new EndpointReference(endpointName));
			
			String id=getTagValue(service.getPrefix() + ":Id", service);
			
			Element actualResource=(Element)getChildElement(service.getPrefix() + ":ActualResource", service);
			ArrayList<String> names=new ArrayList<String>();
			ArrayList<String> values=new ArrayList<String>();
			String methodName="";
			if(actualResource.getChildNodes().getLength()>0)
			{
				NodeList list=actualResource.getChildNodes();
				for(int i=0;i<list.getLength();i++)
				{
					Node method=list.item(i);
					if(method.getNodeType()==Node.ELEMENT_NODE)
					{
						methodName=method.getLocalName();
						opts.setAction(methodName);
						NodeList parameters=method.getChildNodes();
						for(int j=0;j<parameters.getLength();j++)
						{
							Node parameter=parameters.item(j);
							if(parameter.getNodeType()==Node.ELEMENT_NODE)
							{
								String name=parameter.getLocalName();
								String value=parameter.getTextContent();
								names.add(name);
								values.add(value);
							}
						}
						
						break;
					}
				}
			}
			// Create SOAP Envelope
			OMElement otherResource=null;
			SOAPEnvelope envelope=creatOPSOAPEnvelop(endpointName,id,methodName,names,values);
			Iterator headerTR=envelope.getHeader().getChildElements();
			while(headerTR.hasNext())
			{
				OMElement oR=(OMElement)headerTR.next();
				
				if(oR.getLocalName().equals("OtherResources")==true)
				{
					otherResource=oR;
					break;
				}
			}
			// Get Other Resources and resources to it....
			while(trOR.hasNext())
			{
				OMElement elem=(OMElement)trOR.next();
				if(otherResource !=null)
				{
					otherResource.addChild(elem.cloneOMElement());
				}
				else
					System.out.println("Other Resource is NULL");
			}
			outMsgCtx.setEnvelope(envelope);
			op.addMessageContext(outMsgCtx);
			//op.execute(true);
		}
	}
}
