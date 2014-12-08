package gla.sowf.module.core;

import java.util.Iterator;

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

public class TokenService 
{
	public TokenService()
	{
	}
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
	public void setNotAllowed(Assertion assertion)
	{
		AttributeStatement attstmt= getAttribute("RequestResult","Not Allowed");
	    assertion.getAttributeStatements().add(attstmt);
		AttributeStatement att= getAttribute("TokenGenerateTime","Not yet Assigned");
		assertion.getAttributeStatements().add(att);
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
}
