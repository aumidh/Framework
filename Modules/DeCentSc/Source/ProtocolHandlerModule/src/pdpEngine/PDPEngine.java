package pdpEngine;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.factories.PolicyFactory;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.PolicyLocator;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;
import org.jboss.security.xacml.interfaces.XACMLConstants;
import org.jboss.security.xacml.interfaces.XACMLPolicy;
import org.jboss.security.xacml.locators.JBossPolicyLocator;

public class PDPEngine {
public String policiesConfigFilePath="";
	public PDPEngine()
	{
	}
	public boolean evaluate(String policyFile,RequestContext request)
	{
			try
			{
				PolicyDecisionPoint pdp = getPDP(policyFile);
				ResponseContext response = pdp.evaluate(request);
				int res=response.getDecision();
				if(res==XACMLConstants.DECISION_PERMIT)
				{
					return true;
				}
				else if (res==XACMLConstants.DECISION_DENY)
				{
					return false;
				}
			}catch(Exception ex)
			{
				System.out.println("Exception in PDP evaluate" + ex.getMessage());
			}
			return false;
	}
	private PolicyDecisionPoint getPDP(String policyFile) throws Exception
	{
		PolicyDecisionPoint pdp=new JBossPDP();		
		File f=new File(policyFile);
		InputStream in = new FileInputStream(f);
		XACMLPolicy policy=PolicyFactory.createPolicy(in);//(constructPolicy());
		Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
		policies.add(policy);
		pdp.setPolicies(policies);
		//Add the basic locators also
		PolicyLocator policyLocator = new JBossPolicyLocator();
		policyLocator.setPolicies(policies);
		//Locators need to be given the policies
		Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
		locators.add(policyLocator);
		pdp.setLocators(locators);
		return pdp;
	}
}
