package core;

import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.AuthorizationDecisionStatement;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml1.core.impl.AuthenticationStatementBuilder;
import org.opensaml.saml1.core.impl.AuthorizationDecisionStatementBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Element;
/**
 * This is supporting class to other components which provides common functions used in different places
 * of the project.
 * @author Sardar Hussain
 *
 */
public class CommonFunctions {
	
	public static OMElement getAssertionElement(Assertion asser) throws Exception
	{
		 // marshall Assertion Java class into XML
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(asser);
        Element assertionElement = marshaller.marshall(asser);
        OMElement om=XMLUtils.toOM(assertionElement);
        return om;
	}
	public static String getAttributeValue(Assertion samlAssertion, String attName)
	{
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
	public static Assertion getAssertionObject(Element elem)throws Exception
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
	public static Assertion getAssertionBuilder()
	{
		AssertionBuilder ab = new AssertionBuilder();
        Assertion assertion = ab.buildObject();
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setID("123"); // in reality, must be unique for all assertions
        assertion.setIssueInstant(new DateTime());
        return assertion;
	}
	public static Issuer getIssuerBuilder()
	{
		IssuerBuilder ib = new IssuerBuilder();
        Issuer myIssuer = ib.buildObject();
        myIssuer.setValue("http://sowf.gla.ac.uk");
        return myIssuer;
    }
	public static Subject getSubject()
	{
		SubjectBuilder sb = new SubjectBuilder();
        Subject mySubject = sb.buildObject();
        NameIDBuilder nb = new NameIDBuilder();
        NameID myNameID = nb.buildObject();
        myNameID.setValue("bob");
        myNameID.setFormat(NameIdentifier.X509_SUBJECT);
        mySubject.setNameID(myNameID);
        return mySubject;
	}
	public static Attribute getAttributeOnly(String attName,String attValue)
	{
		AttributeBuilder attbldr = new AttributeBuilder();
		Attribute attr = attbldr.buildObject();
        attr.setName(attName);
        attr.setNameFormat("http://www.example.org/DoubleIt/Security");
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration
              .getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder
              .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                    XSString.TYPE_NAME);
        stringValue.setValue(attValue);
        attr.getAttributeValues().add(stringValue);
        return attr;
	}
	public static AttributeStatement getAttributeStatement()
	{
		AttributeStatementBuilder attstmtb = new AttributeStatementBuilder();
        AttributeStatement attstmt = attstmtb.buildObject();
        return attstmt;
	}
	public static AttributeStatement getAttribute(String attName,String attValue)
	{
		 AttributeStatementBuilder attstmtb = new AttributeStatementBuilder();
        AttributeStatement attstmt = attstmtb.buildObject();
        AttributeBuilder attbldr = new AttributeBuilder();
        Attribute attr = attbldr.buildObject();
        attr.setName(attName);
        attr.setNameFormat("http://www.example.org/DoubleIt/Security");
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration
              .getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder
              .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                    XSString.TYPE_NAME);
        stringValue.setValue(attValue);
        attr.getAttributeValues().add(stringValue);
        attstmt.getAttributes().add(attr);
        return attstmt;
	}
}
