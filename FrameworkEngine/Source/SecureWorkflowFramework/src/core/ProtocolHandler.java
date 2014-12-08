package core;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
/**
 * This class acts as a Protocol Handler component as proposed. 
 * @author Sardar Hussain
 *
 */
public class ProtocolHandler {
	public ProtocolHandler(){
	}
	/**
	 * This function creates the execution message i.e. the Ticket. This function will be called for
	 * the creation of the execution message for the Centralized WF.
	 * @param envelope The reference of the outgoing envelope which will be updated within the function
	 * @param res The response received earlier from the token request. 
	 */
	public void create2ndMessage(SOAPEnvelope envelope, RequestResponse res)
	{
		try
		{
			DefaultBootstrap.bootstrap();
			
	        Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	
	        AttributeStatement attstmt= CommonFunctions.getAttribute("Id",res.id);
	        assertion.getAttributeStatements().add(attstmt);
	        
	        OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
	        
	    }catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
    }
	/**
	 * This function creates the execution message i.e. the Ticket. This function will be called for
	 * the creation of the execution message for DeCentralized WF.
	 * @param envelope The reference of the outgoing envelope which will be updated within the function
	 * @param res The response received earlier from the token request.
	 * @param fac The factory reference which is used for the creation of the SOAP part
	 * @param omNs The namespace reference of the service.
	 */
	public void createDistributed2ndMessage(SOAPEnvelope envelope, RequestResponse res,SOAPFactory fac, OMNamespace omNs)
	{
		try
		{
			DefaultBootstrap.bootstrap();
			  
	        Assertion assertion = CommonFunctions.getAssertionBuilder();
	        Issuer myIssuer=CommonFunctions.getIssuerBuilder();
	        assertion.setIssuer(myIssuer);
	
	        Subject mySubject=CommonFunctions.getSubject();
	        assertion.setSubject(mySubject);
	
	        AttributeStatement attstmt= CommonFunctions.getAttribute("Id",res.id);
	        assertion.getAttributeStatements().add(attstmt);
	        OMElement otherResources= fac.createOMElement(new QName("OtherResources"));
	        
	        for(int i=0;i<res.otherResources.size();i++)
			{
	        	
				OMElement resource= fac.createOMElement(new QName("Resource"));
				RequestResponse ot=res.otherResources.get(i);
				OMElement notBefore= fac.createOMElement(new QName("notBefore")); //("notBefore", omNs);
				notBefore.setText(ot.notBefore);
				OMElement notAfter= fac.createOMElement(new QName("notAfter"));//("notAfter", omNs);
				notAfter.setText(ot.notAfter);
				
				  
				  
				OMElement id= fac.createOMElement("Id", omNs);
				id.setText(ot.id);
				
				OMElement obligation= fac.createOMElement(new QName("Obligation"));//"Obligation", omNs);
				obligation.setText(ot.obligation);
				OMElement issuer= fac.createOMElement(new QName("issuer"));//"issuer", omNs);
				issuer.setText(ot.issuer);
				
				OMElement serviceName= fac.createOMElement(new QName("Service"));//"Service", omNs);
				serviceName.setText(ot.msgContext.getTo().getAddress());
				
				OMElement actualResource= fac.createOMElement(new QName("ActualResource"));//"ActualResource", omNs);
				OMElement method = ot.msgContext.getEnvelope().getBody().getFirstElement();
				actualResource.addChild(method.cloneOMElement());
				// Add to Resource ..
				resource.addChild(serviceName);
				resource.addChild(notBefore);
				resource.addChild(notAfter);
				resource.addChild(id);
				resource.addChild(obligation);
				resource.addChild(issuer);
				resource.addChild(actualResource);
				otherResources.addChild(resource);
			}
			// -----------------
	      
	        OMElement om=CommonFunctions.getAssertionElement(assertion);
	        envelope.getHeader().addChild(om);
	        
	        // Add Other resources element
	        envelope.getHeader().addChild(otherResources);
	    }catch(Exception ex)
		{
			System.err.println(ex.getMessage());
		}
    }
}
