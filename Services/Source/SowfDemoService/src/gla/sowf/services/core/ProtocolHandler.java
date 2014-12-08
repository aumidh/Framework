package gla.sowf.services.core;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;


public class ProtocolHandler {
	Response response =null;
	ArrayList<Response> otherResponses = new ArrayList<Response>();
	boolean isOtherResource = false;
	public String currService="";
	String namespace="";
	ServiceClient sc;
	OMElement omCurrElement=null;
	OMElement omOtherElement=null;
	public ProtocolHandler(){
		
	}
	public void cleanup() throws AxisFault
	{
		if(sc!=null)
			sc.cleanupTransport();
	}
	public String callWOSecurityNextService(String _namespace,String _currService,String endPoint,String actionName,String[] paramNames,String[] paramValues) throws AxisFault, InterruptedException
	{
		currService = _currService;
		namespace=_namespace;
		
		sc=new ServiceClient();
		OperationClient op=sc.createClient(ServiceClient.ANON_OUT_ONLY_OP);
		// Message Context
		MessageContext outMsgCtx = new MessageContext();
		Options opts = outMsgCtx.getOptions();
		opts.setTo(new EndpointReference(endPoint));
		opts.setAction(actionName);
		
		// Envelope
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();
		OMNamespace omNs = fac.createOMNamespace(namespace, "ns1");
		OMElement method = fac.createOMElement(actionName.substring(4), omNs);
		
		//Loop on parameters
		for (int i=0;i<paramNames.length;i++)
		{
			String name=paramNames[i];
			String value=paramValues[i];
			OMElement val = fac.createOMElement(name, omNs);
			val.setText(value);
			method.addChild(val);
		}
		envelope.getBody().addChild(method);
		///////////////////////////////////////////////////////
		outMsgCtx.setEnvelope(envelope);
		op.addMessageContext(outMsgCtx);
		
		// Execute the operation
		op.execute(false);
		return "Success triggered action of Service: " + currService;
	}
	public String callWSecurityNextService(String _namespace,String _currService,boolean _includeOtherServices,String[] paramNames,String[] paramValues) throws Exception
	{
		currService = _currService;
		namespace=_namespace;
		String strReturnMessage="";
		try
		{
			MessageContext ctx=MessageContext.getCurrentMessageContext();
			if (ctx.getEnvelope()!=null)
			{
				// Get credentials and resource,
				getOtherResources(ctx.getEnvelope());
				if(isOtherResource == true)
				{
					// Create New message
					// OperationClient
					ServiceClient sc=new ServiceClient();
					OperationClient opClient=sc.createClient(ServiceClient.ANON_OUT_ONLY_OP);
					// Message Context
					MessageContext outMsgCtx = new MessageContext();
					Options opts = outMsgCtx.getOptions();
					opts.setTo(new EndpointReference(response.serviceEndPoint));
					opts.setAction(response.resource.methodName); 
					opts.setCallTransportCleanup(true);
					// SOAP Envelope
					SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
					SOAPEnvelope envelope = fac.getDefaultEnvelope();
					SOAPHeader header=envelope.getHeader();
					OMNamespace omNs = fac.createOMNamespace(namespace, "ns1");
					OMElement method = fac.createOMElement(response.resource.methodName, omNs);
					//for (int j=0;j<response.resource.parameters.size();j++)
					for (int j=0;j<paramNames.length;j++)
					{
						//ResourceParameter param=response.resource.parameters.get(j);
						OMElement value = fac.createOMElement(paramNames[j], omNs);
						value.setText(paramValues[j]);
						method.addChild(value);
					}
					// Message
					
					DefaultBootstrap.bootstrap();
					Assertion assertion = CommonFunctions.getAssertionBuilder();
			        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
			        assertion.setIssuer(myIssuer);
			        Subject mySubject=CommonFunctions.getSubject();
			        assertion.setSubject(mySubject);
			        AttributeStatement attstmt= CommonFunctions.getAttribute("Id",response.id);
			        assertion.getAttributeStatements().add(attstmt);
			        OMElement om=CommonFunctions.getAssertionElement(assertion);
				    envelope.getHeader().addChild(om);
				    if(_includeOtherServices==true && omOtherElement!=null)
			        {
			        	OMElement otherResources=omOtherElement.cloneOMElement();
			        	envelope.getHeader().addChild(otherResources);
			        }
			        else if(omOtherElement==null)
			        	CommonFunctions.saveToFile("Yes","Is OM Other Element is null:");
			        else if(_includeOtherServices==false)
			        	CommonFunctions.saveToFile("Yes","include other services is false");
				   
					//----------------------------------------------------------
					
					envelope.getBody().addChild(method);
					outMsgCtx.setEnvelope(envelope);
					// -----------------------------------------------
					opClient.addMessageContext(outMsgCtx);
					//------------------------------------------------
					opClient.execute(false);
				}
				else
				{
					CommonFunctions.saveToFile("Other Resource isn't found", "WSecurityNext Function:" );
					strReturnMessage= "Other resource isn't found.";
				}
			}
			strReturnMessage= "Envelop is null";
		}catch(Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
		
		return strReturnMessage;
	}
	public OMElement getOtherResroucesElement(SOAPHeader header)
	{
		Iterator<OMElement> it=header.getChildElements();
		while (it.hasNext()) {
			OMElement om = it.next();
			if (om.getLocalName().toLowerCase().equals("otherresources"))
				return om;
		}
		return null;
	}
	private void getOtherResources(SOAPEnvelope envelope)
	{
		//int cnt =0;
		isOtherResource = false;
		String notBefore="";
		String notAfter="";
		String id="";
		String issuer="";
		String obligation="";
		String serviceEndPoint="";
		Resource resource=null;
		
		OMElement orElement=getOtherResroucesElement(envelope.getHeader());
		
		if (orElement != null)
		{
			isOtherResource = true;
			otherResponses.clear();
			Iterator<OMElement> resources=orElement.getChildrenWithName(new QName("Resource"));
			if(resources.hasNext())
			{
				omCurrElement=resources.next();
				Iterator<OMElement> omResourceChilds=omCurrElement.getChildElements();
				while(omResourceChilds.hasNext())
				{
					OMElement leafNode=omResourceChilds.next();
					if (leafNode.getLocalName().toLowerCase().equals("service") == true)
						serviceEndPoint=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("notbefore") == true)
					{
						notBefore=leafNode.getText();
					}
					if (leafNode.getLocalName().toLowerCase().equals("notafter") == true)
					{
						notAfter=leafNode.getText();
					}
					if (leafNode.getLocalName().toLowerCase().equals("id") == true)
						id=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("obligations") == true)
						obligation=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("Issuer") == true)
						issuer=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("actualresource") == true)
					{
							resource=getResourceFromElement(leafNode);
					}
				}
				response = new Response(serviceEndPoint, notBefore, notAfter, id, issuer, obligation, resource);
				omCurrElement.detach();
				omOtherElement=orElement;
			}
		}
	}
	public Resource getResourceFromElement(OMElement element)
	{
		OMElement method=element.getFirstElement();
		if (method != null)
		{
			Resource resource=new Resource(method.getLocalName());
			for(Iterator<OMElement> i = method.getChildElements(); i.hasNext(); ) 
			{
				  OMElement item = i.next();
				  ResourceParameter rp=new ResourceParameter(item.getLocalName(), item.getText());
				  resource.AddParameter(rp);
			}
			return resource;
		}
		return null;
	} 
}
