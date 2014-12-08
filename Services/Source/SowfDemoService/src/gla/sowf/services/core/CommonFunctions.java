package gla.sowf.services.core;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.XMLUtils;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
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

public class CommonFunctions {
	
	public static void setAttacements(MessageContext ctx,String file,String id)
	{
		try
		{
			FileDataSource dataSource = new FileDataSource(file); 
			DataHandler dataHandler = new DataHandler(dataSource); 
			ctx.addAttachment(id, dataHandler);
			saveToFile("File attached", "Set Attachement:");
		}catch(Exception ex)
		{
			saveToFile(ex.getMessage(), "Exception in set attachement:");
		}
	}
	public static void getAttachements(MessageContext ctx)
	{
		try
		{
			if(ctx.attachments.getAllContentIDs().length > 0)
			{
				String[] ids=ctx.attachments.getAllContentIDs();
				for(int i=0;i<ids.length;i++)
				{
					 File graphFile = new File(System.getProperty("user.dir") + "//SOWF//" + ids[i] + ".xml");
				     FileOutputStream outputStream = new FileOutputStream(graphFile);
				     DataHandler d=ctx.getAttachment(ids[i]);
				     d.writeTo(outputStream);
					 outputStream.flush();
					 outputStream.close();
					 saveToFile("Attachment is saved to disk", "Attachement:");
				}
			}
			else
				saveToFile("Attachements size is zero", "Attachement:");
		}catch(Exception ex)
		{
			saveToFile(ex.getMessage(), "Exception in attachement:");
		}
	}
	
	public static void saveToFile(String _data,String _description)
	{
		String filename= System.getProperty("user.dir") + "//SOWF//sowfLog.txt";
		try{
			FileWriter fw = new FileWriter(filename,true);
			//BufferedWriter bw=new BufferedWriter(fw);
			fw.write(System.getProperty("line.separator"));
			fw.write(System.getProperty("line.separator"));
			fw.write(_description +  " " + _data);
			fw.write(System.getProperty("line.separator"));
			fw.write("------------------ ------- ------------------ ");
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("SaveToFile:" + ex.getMessage());
		}
	}
	public static void save(String _data)
	{
		String filename= System.getProperty("user.dir") + "//SOWF//results.txt";
		try{
			FileWriter fw = new FileWriter(filename,true);
			fw.write(_data);
			fw.write(System.getProperty("line.separator"));
			fw.close();
		}catch(Exception ex)
		{
			System.out.println("Save action:" + ex.getMessage());
		}
	}
	public static String getWFExecMode()
	{
		try {
			String configFile= System.getProperty("user.dir") + "//SOWF//WF_Config.config";
			BufferedReader br = new BufferedReader(new FileReader(configFile));
			String line="",mode="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts[0].toLowerCase().startsWith("mode")){
					 mode=parts[1];
				}
			}
			br.close();
			return mode;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	} 
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
