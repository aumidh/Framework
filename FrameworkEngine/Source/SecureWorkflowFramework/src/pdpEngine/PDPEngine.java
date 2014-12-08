package pdpEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
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
/**
 * This class is responsible for reading the PDP policy and 
 * interfacing with the JBOSS library for PDP policy and the evaluation
 * @author Sardar Hussain
 *
 */
public class PDPEngine {
public String policiesConfigFilePath="";
	private Hashtable<String,String> listPoliciesFiles;
	public PDPEngine()
	{
		policiesConfigFilePath = System.getProperty("user.dir") + "\\CentWFCentSc\\policiesConfig.ini";
		listPoliciesFiles=new Hashtable<String,String>();
		readConfigurationFile();
	}
	private String getPolicyFile(String serviceName)
	{
		if(listPoliciesFiles.size()>0 && listPoliciesFiles.containsKey(serviceName.toLowerCase())==true)
			return listPoliciesFiles.get(serviceName.toLowerCase());
		else
			return "";
	}
	private void readConfigurationFile()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(policiesConfigFilePath));
			String line="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts.length==2)
				{
					listPoliciesFiles.put(parts[0].toLowerCase(),parts[1]);
				}
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	/**
	 * This function evaluate the policy for a given service
	 * @param request The pdpRequest
	 * @param serviceName The service for which the policy will be evaluated.
	 * @return true/false value, which determine whether the credentials satisfied the policy or not
	 */
	public boolean evaluate(RequestContext request,String serviceName)
	{
			try
			{
				String file=getPolicyFile(serviceName.toLowerCase());
				PolicyDecisionPoint pdp = getPDP(file);
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
