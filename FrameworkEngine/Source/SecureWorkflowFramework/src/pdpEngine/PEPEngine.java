package pdpEngine;


import java.net.URI;
import java.util.ArrayList;

 
import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.RequestContext;
import core.ConfigurationParameter;
 
/**
 * This class represent the PEP component. This create the request based on the credentials provided 
 * for the service.
 * @author Sardar Hussain
 *
 */
public class PEPEngine
{
	String ACTION_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	String CURRENT_TIME_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
	String RESOURCE_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	String SUBJECT_IDENTIFIER = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	String SUBJECT_ATT_IDENTIFIER = "urn:oasis:names:tc:xacml:2.0:subject:";
	
	public PEPEngine()
	{
		
	}
	/**
	 * This function create the PDP request.
	 * @param uri The uri of the service.
	 * @param issuer The Issuer.
	 * @param attList The credentials as the list of attributes.
	 * @param methodName The function name which is required to be call for the service.
	 * @return The PDP request
	 * @throws Exception
	 */
	public RequestContext createXACMLRequest(String uri,String issuer, ArrayList<Attribute> attList,String methodName) throws Exception
	   {  
	      RequestContext requestCtx = RequestResponseContextFactory.createRequestCtx(); 
	      
	      //Create a subject type
	      SubjectType subject = new SubjectType(); 
	      subject.getAttribute().add(RequestAttributeFactory.createStringAttributeType(
	            SUBJECT_IDENTIFIER, issuer, ""));
	      for(int i=0;i<attList.size();i++)
	      {
	         Attribute att = attList.get(i);
	         AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
	               SUBJECT_ATT_IDENTIFIER + att.getName(), issuer, att.getValue()); 
	         subject.getAttribute().add(attSubjectID);
	      } 
	      
	      //Create a resource type
	      ResourceType resourceType = new ResourceType();
	      resourceType.getAttribute().add(RequestAttributeFactory.createAnyURIAttributeType(
	            RESOURCE_IDENTIFIER, null, new URI(uri)));
	      
	      //Create an action type
	      ActionType actionType = new ActionType();
	      actionType.getAttribute().add(RequestAttributeFactory.createStringAttributeType(
	            ACTION_IDENTIFIER, "jboss.org", methodName));
	      
	      //Create an Environment Type (Optional)
	      EnvironmentType environmentType = new EnvironmentType(); 
	      environmentType.getAttribute().add(RequestAttributeFactory.createDateTimeAttributeType(
	            CURRENT_TIME_IDENTIFIER, null));
	       
	      //Create a Request Type
	      RequestType requestType = new RequestType();
	      requestType.getSubject().add(subject);
	      requestType.getResource().add(resourceType);
	      requestType.setAction(actionType);
	      requestType.setEnvironment(environmentType);
	      
	      requestCtx.setRequest(requestType); 
	      
	      return requestCtx;
	   }  
	/**
	 * This function will make request and then will evaluate the request by interacting with PDP engine.
	 * @param issuer The issuer of the credentials.
	 * @param credentials The credentials list.
	 * @param serviceName The WF Service.
	 * @param methodName The target function of the service.
	 * @return The true/false value, which determine whether the credentials satisfied the policy or not 
	 */
	public boolean evaluate(String issuer,ArrayList<ConfigurationParameter> credentials,String serviceName,String methodName)
	{
			try
			{
				String requestURI = "http://nesc/sowfDemoService.wsdl";
				if(methodName.startsWith("urn:")==true);
					methodName=methodName.substring(4);
				ArrayList<pdpEngine.Attribute> attList=new ArrayList<pdpEngine.Attribute>();
			    for(int i=0;i<credentials.size();i++)
			    {
			    	pdpEngine.Attribute att=new pdpEngine.Attribute(credentials.get(i).name.toLowerCase(),credentials.get(i).value);
				    attList.add(att);
			    }
			    RequestContext request = createXACMLRequest(requestURI,issuer, attList,methodName);
				PDPEngine pdp=new PDPEngine();
				return pdp.evaluate(request, serviceName);
			}catch(Exception ex)
			{
				System.out.println("Exception in validateCredentials" + ex.getMessage());
			}
			return false;
	}
}