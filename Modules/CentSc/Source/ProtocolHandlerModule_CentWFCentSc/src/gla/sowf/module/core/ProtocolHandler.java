
package gla.sowf.module.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.soap.SOAPEnvelope;

import org.apache.axis2.util.XMLUtils;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;

import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Element;

import java.io.*;

//public class LogHandler extends AbstractHandler implements Handler {
public class ProtocolHandler extends AbstractHandler implements Handler {
	private static final Log log = LogFactory.getLog(ProtocolHandler.class);
    private String name;
	private TokenService tokenService=new TokenService();
		
	// Configuration File
	private ArrayList<String> dns;
	// End of Configuration
	public enum SamlParseResult {
        QUERY_ALLOWED, QUERY_NOTALLOWED, EXECUTE_ALLOWED, EXECURE_NOTALLOWED, UNKNOWN 
    }
    public ProtocolHandler()
    {
    	dns=new ArrayList<String>();
    	readConfiguration();
    }
   public void readConfiguration()
    {
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "//SOWF//Module_Configs//moduleConfig.config"));
			String line="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts[0].toLowerCase().startsWith("dn")){
					dns.add(parts[1]);
				}
			}
			br.close();
		}
		catch (Exception e) {
			System.out.println(e.getMessage()); 
		}
	}
    public String getName() {
        return name;
    }
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
	public boolean validateCredentials(String _dn)
	{
		for(int i=0;i<dns.size();i++)
		{
			if(dns.get(i).equals(_dn)==true)
				return true;
		}
		return false;
	}
	public SamlParseResult ProcessToken(SOAPEnvelope envelope)  throws Exception
	{
		OMElement assertionElement=envelope.getHeader().getFirstElement();
		if (assertionElement != null && assertionElement.getLocalName().equals("Assertion"))
		{
			Element elem=XMLUtils.toDOM(assertionElement);
			
			// Unmarshall SAML Assertion into an OpenSAML Java object.
			Assertion samlAssertion = getAssertionObject(elem);
			String _dn=getAttributeValue(samlAssertion,"authNCrd_EE");
			boolean res=validateCredentials(_dn);
			if(res== true)
			{
				return SamlParseResult.EXECUTE_ALLOWED;
			}
			else
				return SamlParseResult.EXECURE_NOTALLOWED;
		}
		else
		{
			return SamlParseResult.UNKNOWN;
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
	public InvocationResponse processMessage(MessageContext msgContext) throws AxisFault
	{
		if(msgContext.getEnvelope().getHeader() != null && checkSignature(msgContext) == true)
		{
			log.info("Header exist");
			try
			{
				// Decrypt will be called here, if implemented with some functionality.
				SamlParseResult result=ProcessToken(msgContext.getEnvelope());
				if( result == SamlParseResult.EXECUTE_ALLOWED )
				{
					log.info(" Saml: Execute Allowed");
					return InvocationResponse.CONTINUE;
				}
				else if (result == SamlParseResult.EXECURE_NOTALLOWED)
				{
					OMElement samlElem=tokenService.CreateToken(result,msgContext.getEnvelope());
					//assignTokenProcessTime(samlElem);
					throw new AxisFault (samlElem.toString());
				}
				else
				{
					OMElement samlElem=tokenService.CreateToken(result,msgContext.getEnvelope());
					//assignTokenProcessTime(samlElem);
					throw new AxisFault (samlElem.toString());
				}
				
			}catch(Exception ex)
			{
				log.info(ex.getMessage());
				throw new AxisFault (ex.getMessage());
			}
		}
		log.info("Saml Without Header");
    	return InvocationResponse.ABORT;
	}
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
		        return processMessage(msgContext);
    }
	public void revoke(MessageContext msgContext) {
        log.info(" Saml: Revoke" + msgContext.getEnvelope().toString());
    }
    public void setName(String name) {
        this.name = name;
    }

}
