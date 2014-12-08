
package gla.sowf.module.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.util.XMLUtils;

import java.util.Date;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Element;

import pdpEngine.PEPEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
/**
 * This is the required class for the module(Protocol Handler) component. This contains all the logic of intercepting
 * message.
 * @author Sardar Hussain
 *
 */
//public class LogHandler extends AbstractHandler implements Handler {
public class ProtocolHandler extends AbstractHandler implements Handler {
	private static final Log log = LogFactory.getLog(ProtocolHandler.class);
    private String name;
	private TokenService tokenService=new TokenService();
	
	private String policyFilePath="";
	private String credConfigFilePath="";
	/**
	 * This defines what kind of SAMLE result possible we can have for an incoming message.
	 * @author Sardar Hussain
	 *
	 */
    public enum SamlParseResult {
        QUERY_ALLOWED, QUERY_NOTALLOWED, EXECUTE_ALLOWED, EXECURE_NOTALLOWED, UNKNOWN 
    }
    public ProtocolHandler()
    {
    	//readConfiguration();
    }
    /**
     * This function read the module configuration file.
     * @param _fileName The configuration file name
     */
   public void readConfiguration(String _fileName)
    {
	   try {
    		String filePath=System.getProperty("user.dir") + "//SOWF//Module_Configs//" +  _fileName + ".config";
    		BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts[0].toLowerCase().startsWith("policyfilepath")){
					policyFilePath=parts[1];
					//writeData(policyFilePath);
				}
				if(parts[0].toLowerCase().startsWith("credconfigfilepath")){
					credConfigFilePath=parts[1];
				}
			}
			br.close();
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
    public String getName() {
        return name;
    }
	  /**
	   * This function retreive the Resource from the SOAP envelope
	   * @param envelope The SOAP envelope
	   * @return The resource
	   */
	public Resource getResource(SOAPEnvelope envelope)
	{
		SOAPBody body=envelope.getBody();
		if (body != null)
		{
			OMElement method = body.getFirstElement();
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
	/**
	 * This function retreive the Resource object from the XML element.
	 * @param element The XML element.
	 * @return The Resource object
	 */
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
	/**
	 * This function retreive the Assertion object from the XML element.
	 * @param elem The XML Element
	 * @return The Assertion object
	 * @throws Exception
	 */
	public Assertion getAssertionObject(Element elem)throws Exception
	{
		try
		{
			DefaultBootstrap.bootstrap();
	        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
	        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(elem);
	        Assertion samlAssertion = (Assertion) unmarshaller.unmarshall(elem);
	        return samlAssertion;
		}catch(Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}
	/**
	 * This function retreive the attribute value for a given SAML attribute
	 * @param samlAssertion The SAML Assertion
	 * @param attName The attribute name
	 * @return The value of the attribute
	 */
	public String getAttributeValue(Assertion samlAssertion, String attName)
	{
		// Check if math degree, error otherwise
        List<AttributeStatement> asList = samlAssertion.getAttributeStatements();
        if (asList != null && asList.size() > 0) 
        {
           for (Iterator<AttributeStatement> it = asList.iterator(); it.hasNext();) 
           {
              AttributeStatement as = it.next();
              List<Attribute> attList = as.getAttributes();
              if (attList != null && attList.size() > 0) 
              {
                 for (Iterator<Attribute> it2 = attList.iterator(); it2.hasNext();) 
                 {
                    Attribute att = it2.next();
                    if (att.getName().equals(attName)) 
                    {
                       List<XMLObject> xoList = att.getAttributeValues();
                       if (xoList != null && xoList.size() == 1) 
                       {
                          XMLObject xmlObj = xoList.get(0);
                          return xmlObj.getDOM().getFirstChild().getTextContent();
                       }
                    }
                 }
              }
           }
        }
        return "";
	}
	/**
	 * This function obtain the credentials list from the SAML Assertion for a given service
	 * @param serviceName The service name
	 * @param samlAssertion The Assertion object
	 * @return The list of credentials
	 */
	public  ArrayList<ConfigurationParameter> getAuthorizationCredentials(String serviceName,Assertion samlAssertion)
	{
		List<AttributeStatement> asList = samlAssertion.getAttributeStatements();
		ArrayList<ConfigurationParameter> list=new ArrayList<ConfigurationParameter>();
		if(asList != null && asList.size()==3) // It is triggered for PushModel
		{
			// Get Authorization attributes only.
			AttributeStatement authAttribute=asList.get(1); 
			List<Attribute> attList = authAttribute.getAttributes();
            if (attList != null && attList.size() > 0) 
            {
               for (Iterator<Attribute> it2 = attList.iterator(); it2.hasNext();) 
               {
            	   
                  Attribute att = it2.next();
                  String name=att.getName();
                  XMLObject xmlObj=att.getAttributeValues().get(0);
                  String value=xmlObj.getDOM().getFirstChild().getTextContent();
                  list.add(new ConfigurationParameter(name, value));
               }
            }
		}
		else if(asList != null &&  asList.size()==2)  // It is triggered for pull model 
		{
			AttributeStatement authAttribute=asList.get(1); 
			List<Attribute> attList = authAttribute.getAttributes();
			// Get User Authentication only.
			String userDN=getAttributeValue(samlAssertion,"authNCrd_user");
			CredentialsMgmt cred=new CredentialsMgmt(credConfigFilePath, userDN);
			list=cred.getCredForService(serviceName);
		}
		return list;
    }
	/**
	 * This function retreive the issuer attribute value from the list
	 * @param authList The list of credentials.
	 * @return The issuer value
	 */
	public String getIssuerAttribute(ArrayList<ConfigurationParameter> authList)
	{
		for(int i=0;i<authList.size();i++)
    	{
    		if(authList.get(i).name.toLowerCase().equals("issuer")==true)
    			return authList.get(i).value;
    	}
		return ""; 
	}
	public void writeLog(String _data)
	{
		String filename="";
		filename=System.getProperty("user.dir") + "//SOWF//Sowflog.txt";
		try{
			FileWriter fw = new FileWriter(filename,true);
			fw.write(System.getProperty("line.separator"));
			fw.write(_data);
			fw.close();
		}catch(Exception ex)
		{
			System.err.println("recordTime:" + ex.getMessage());
		}
	}
	/**
	 * This function Parse the intercepted message.
	 * @param serviceName The service name
	 * @param envelope The corresponding envelope
	 * @return The SAML result
	 * @throws Exception
	 */
	public SamlParseResult ProcessToken(String serviceName,SOAPEnvelope envelope)  throws Exception
	{
		OMElement assertionElement=envelope.getHeader().getFirstElement();
		if (assertionElement != null && assertionElement.getLocalName().equals("Assertion"))
		{
			Element elem=XMLUtils.toDOM(assertionElement);
			
			// Unmarshall SAML Assertion into an OpenSAML Java object.
			Assertion samlAssertion = getAssertionObject(elem);
			String query=getAttributeValue(samlAssertion,"Query");
			if (query.toLowerCase().equals("am i allowed") == true)
			{
				ArrayList<ConfigurationParameter> authList=getAuthorizationCredentials(serviceName,samlAssertion);
				PEPEngine pep=new PEPEngine();
				String issuer=getIssuerAttribute(authList);
				String methodName=getResource(envelope).methodName;
				boolean res=pep.evaluate( policyFilePath,issuer,authList,serviceName,methodName);
				if(res== true)
				{
					return SamlParseResult.QUERY_ALLOWED;
				}
				else
				{
					return SamlParseResult.QUERY_NOTALLOWED;
				}
			}
			else
			{
				String Id=getAttributeValue(samlAssertion,"Id");
				Resource resource= getResource(envelope);
				boolean res=tokenService.verifyToken(Id,resource);
				if(res== true)
				{
					return SamlParseResult.EXECUTE_ALLOWED;
				}
				else
					return SamlParseResult.EXECURE_NOTALLOWED;
			}
		}
		else
		{
			return SamlParseResult.UNKNOWN;
		}
	}
	/**
	 * In the decentralized WF case, this function retreives the Nested services.
	 * @param header The header of the soap.
	 * @return The XML element containing the Nested Services.
	 */
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
	private void GetOtherResources(SOAPEnvelope envelope)
	{
		OMElement orElement=getOtherResroucesElement(envelope.getHeader());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (orElement != null)
		{
			Iterator<OMElement> resources=orElement.getChildrenWithLocalName("Resource");
			while(resources.hasNext())
			{
				OMElement omResource=resources.next();
				Iterator<OMElement> omResourceChilds=omResource.getChildElements();
				String _service="";
				Date _notbefore=null;
				Date _notafter=null;
				String _id="";
				String _issuer="";
				String _obligation="";
				Resource _resource=null;
				while(omResourceChilds.hasNext())
				{
					OMElement leafNode=omResourceChilds.next();
					if (leafNode.getLocalName().toLowerCase().equals("service") == true)
						_service=leafNode.getText();
					try
					{					
						if (leafNode.getLocalName().toLowerCase().equals("notbefore") == true)
						{
							_notbefore=format.parse(leafNode.getText());
						}
						if (leafNode.getLocalName().toLowerCase().equals("notafter") == true)
						{
							_notafter=format.parse(leafNode.getText());
						}
					} catch (ParseException e) 
					{
						e.printStackTrace();
					}
					if (leafNode.getLocalName().toLowerCase().equals("id") == true)
						_id=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("obligations") == true)
						_obligation=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("Issuer") == true)
						_issuer=leafNode.getText();
					if (leafNode.getLocalName().toLowerCase().equals("actualresource") == true)
					{
							_resource=getResourceFromElement(leafNode);
					}
				}
				// Create OtherMessagerequest and add it to the list.
				OtherMessageRequest omr=new OtherMessageRequest(_service,_notbefore,_notafter,_id,_issuer,_obligation,_resource);
				OtherRequests.add(omr);
			}
		}
	}
	public boolean checkSignature(MessageContext msgContext)
	{
		// Signature will be checked here that whether request is digitally signed from an authenticated sender.
		return true;
	}
	public MessageContext decrypt(MessageContext msgContext)
	{
		// The encrypted data will be decrypt here with valid private key of the receiver.
		return msgContext;
	}
	/**
	 * This function retreive the Service name from the incoming message context
	 * @param msgContext The incoming message context.
	 * @return The Service Name
	 */
	public String getServiceName(MessageContext msgContext)
	{
		String str=msgContext.getTo().toString();
		str= str.substring(str.lastIndexOf('/')+1);
		return str;
	}
	/**
	 * The function that process the incoming message
	 * @param msgContext The incoming message context
	 * @return The response that indicate whether the service will be allowed to execute or not
	 * @throws AxisFault
	 */
	public InvocationResponse processMessage(MessageContext msgContext) throws AxisFault
	{
		if(msgContext.getEnvelope().getHeader() != null && checkSignature(msgContext) == true)
		{
			try
			{
				
				// Decrypt will be called here, if implemented with some functionality.
				String serviceName=getServiceName(msgContext);
				readConfiguration(serviceName.toLowerCase());
				SamlParseResult result=ProcessToken(serviceName,msgContext.getEnvelope());
				if( result == SamlParseResult.EXECUTE_ALLOWED )
				{
					return InvocationResponse.CONTINUE;
				}
				else if (result == SamlParseResult.EXECURE_NOTALLOWED)
				{
					throw new AxisFault ("Invalid Ticket.");
				}
				else
				{
					OMElement samlElem=tokenService.CreateToken(result,msgContext.getEnvelope());
					throw new AxisFault (samlElem.toString());
				}
				
			}catch(Exception ex)
			{
				//log.info(ex.getMessage());
				throw new AxisFault (ex.getMessage());
			}
		}
		//log.info("Saml Without Header");
    	return InvocationResponse.ABORT;
	}
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
		        return processMessage(msgContext);
    }
 	public void revoke(MessageContext msgContext) {
        //log.info(" Saml: Revoke" + msgContext.getEnvelope().toString());
    }

    public void setName(String name) {
        this.name = name;
    }

}
