package credentialsMgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;

import core.ConfigurationParameter;
import core.ParamSettings;
/**
 * This class represent the credentials management component.
 * @author Sardar Hussain
 *
 */
public class CredentialsMgmt {
	public String filePath="SWFConfigFiles\\credentials.ini"; // Path to the configuration file
	public ArrayList<ConfigurationParameter> configParameters;
	public Hashtable<String,ArrayList<ConfigurationParameter>> htCredentials;
	public CredentialsMgmt()
	{
		configParameters=new ArrayList<ConfigurationParameter>();
		htCredentials=new Hashtable<String, ArrayList<ConfigurationParameter>>();
		readConfigurationFile();
	}
	/**
	 * This function will return the list of credentials for the given service name.
	 * @param _serviceName The service for which the credentials are required.
	 * @return The list of the credentials.
	 */
	public ArrayList<ConfigurationParameter> getCredForService(String _serviceName)
	{
		return htCredentials.get(_serviceName.toLowerCase());
	}
	/**
	 * This function will return the issuer configuration for the given service.
	 * @param _serviceName The service for which the issuer configuration is required.
	 * @return The issuer value.
	 */
	public String getIssuerAttribute(String _serviceName)
	{
		ArrayList<ConfigurationParameter> aList=htCredentials.get(_serviceName.toLowerCase());
    	for(int i=0;i<aList.size();i++)
    	{
    		if(aList.get(i).name.toLowerCase().equals("issuer")==true)
    			return aList.get(i).value;
    	}
		return ""; 
	}
	/**
	 * This function will read the configuration file of the credentials for all services and
	 * will store it in the hash table where the services will be unique.
	 */
	private void readConfigurationFile()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts.length==2 && parts[0].toLowerCase().equals("dn")==true)
				{
					if (parts[1].equals(ParamSettings.DN_User))
					{
						while ( (line = br.readLine()).equals("]")==false){
							if (line.startsWith("#") )  
								continue;
							else
							{
								parts = line.split("=");
								if(parts.length==2 && parts[0].toLowerCase().equals("service")==true)
								{
									String serviceIden=parts[1].toLowerCase();
									ArrayList<ConfigurationParameter> serviceConfigurations=new ArrayList<ConfigurationParameter>();
									while ( (line = br.readLine()).equals("}")==false){
										if (line.startsWith("#") )  
											continue;
										else
										{
											parts = line.split("=");
											if(parts.length==2)
											{
												serviceConfigurations.add(new ConfigurationParameter(parts[0], parts[1]));
											}
										}
									}
									htCredentials.put(serviceIden, serviceConfigurations);
								}
							}
						}
					}
				}
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}	
}
