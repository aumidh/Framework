package gla.sowf.module.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import gla.sowf.module.core.ProtocolHandler.SamlParseResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.util.XMLUtils;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Element;
/**
 * This class represent the TokenService component which deals with the token generation.
 * @author Sardar Hussain
 *
 */
public class TokenService 
{
	public Requests requests=new Requests();
	public TokenService()
	{
	}
	public boolean verifyToken(String _id,Resource _resource)
	{
		if(requests.isExist(_id) == true)
		{
			MessageRequest request = requests.getRequest(_id);
			boolean res=request.isExecutable(_resource);
			return res;
		}
		else
			return false;
	}
	/**
	 * THis function creates the token
	 * @param res The decision in the form of SAMLParseResult. See SAMLParseResult for further details. 
	 * @param envelope The SOAP Envelope
	 * @return The XML Element
	 * @throws Exception
	 */
	public OMElement CreateToken(SamlParseResult res,SOAPEnvelope envelope) throws Exception
	{
		try
		{
			DefaultBootstrap.bootstrap();
			AssertionBuilder ab = new AssertionBuilder();
	        Assertion assertion = ab.buildObject();
	        assertion.setVersion(SAMLVersion.VERSION_20);
	        assertion.setID("123"); // in reality, must be unique for all assertions
	        assertion.setIssueInstant(new DateTime());
	        
	        IssuerBuilder ib = new IssuerBuilder();
	        Issuer myIssuer = ib.buildObject();
	        myIssuer.setValue("Service URI");
	        assertion.setIssuer(myIssuer);
	        String attValue = "";
	        if(res == SamlParseResult.EXECURE_NOTALLOWED || res == SamlParseResult.QUERY_NOTALLOWED)
			{
	        	setNotAllowed(assertion);
			}
			else
			{
	        	setAllowed(assertion,envelope);
	        }		
	        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
	        Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
	        Element assertionElement = marshaller.marshall(assertion);
	        OMElement om=XMLUtils.toOM(assertionElement);
	        return om;
		}catch(Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}
	/**
	 * This function set the result as "Not Allowed"
	 * @param assertion The assertion objection
	 */
	public void setNotAllowed(Assertion assertion)
	{
		AttributeStatement attstmt= getAttribute("RequestResult","Not Allowed");
	    assertion.getAttributeStatements().add(attstmt);
		AttributeStatement att= getAttribute("TokenGenerateTime","Not yet Assigned");
		assertion.getAttributeStatements().add(att);
	}
	/**
	 * This function set the result as "Allowed"
	 * @param assertion The assertion object
	 * @param envelope The SOAP Envelope
	 */
	public void setAllowed(Assertion assertion,SOAPEnvelope envelope)
	{
		MessageRequest mr=MakeMessageRequest(envelope);
		if (mr != null)
		{
			// Allowed.
			AttributeStatement attReqResult= getAttribute("RequestResult","Allowed");
			assertion.getAttributeStatements().add(attReqResult);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			AttributeStatement attNB= getAttribute("NotBefore",format.format(mr.notBefore));
			assertion.getAttributeStatements().add(attNB);
			
			AttributeStatement attNA= getAttribute("notAfter",format.format(mr.notAfter));
			assertion.getAttributeStatements().add(attNA);
			
			AttributeStatement attId= getAttribute("Id",mr.id);
			assertion.getAttributeStatements().add(attId);
			
			AttributeStatement attObl= getAttribute("Obligation",mr.obligation);
			assertion.getAttributeStatements().add(attObl);
			
			requests.addRequest(mr.id, mr);
			//TestResources.add(TestResources.getKey(),TestResources.getKey());
		}
		else
		{
			AttributeStatement attstmt= getAttribute("RequestResult","Unable to create message.");
			assertion.getAttributeStatements().add(attstmt);
		}
		AttributeStatement attstmt= getAttribute("TokenGenerateTime","Not yet Assigned");
		assertion.getAttributeStatements().add(attstmt);
	}
	/**
	 * This function create the soap envelope
	 * @param envelope The SOAP Envelope
	 * @return The Message request.
	 */
	public MessageRequest MakeMessageRequest(SOAPEnvelope envelope)
	{
		Date notBefore = new Date();
		Date notAfter = new Date();
		notAfter.setMinutes(notAfter.getMinutes() + 3);
		String id = UUID.randomUUID().toString();
		String Obligation="TestObligation";
		String issuer = "TestIssuer";
		Resource resource=getResource(envelope);
		if (resource !=null)
		{
			MessageRequest mr=new MessageRequest(notBefore, notAfter, id, issuer, Obligation, resource);
			return mr;
		}
		return null;
	}
	public void cancelToken(SOAPEnvelope envelope)
	{
		
	}
	public void renewToken(SOAPEnvelope envelope)
	{
		
	}
	  private AttributeStatement getAttribute(String attName,String attValue)
		{
			AttributeStatementBuilder attstmtb = new AttributeStatementBuilder();
	        AttributeStatement attstmt = attstmtb.buildObject();
	        AttributeBuilder attbldr = new AttributeBuilder();
	        Attribute attr = attbldr.buildObject();
	        attr.setName(attName);
	        attr.setNameFormat("http://www.example.org/DoubleIt/Security");
	        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
	        XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,XSString.TYPE_NAME);
	        stringValue.setValue(attValue);
	        attr.getAttributeValues().add(stringValue);
	        attstmt.getAttributes().add(attr);
	        return attstmt;
		}
	  /**
	   * This function retreives the function from the SOAP envelop
	   * @param envelope The SOAP Envelope
	   * @return Return the function in the Resource form.
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
}
