package gla.sowf.module.core;

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

public class CredentialsMgmt {
	public String DN_User;
	public String filePath;
	public ArrayList<ConfigurationParameter> configParameters;
	public Hashtable<String,ArrayList<ConfigurationParameter>> htCredentials;
	public CredentialsMgmt(String _filePath,String _dnUser)
	{
		DN_User=_dnUser;
		filePath=_filePath;
		configParameters=new ArrayList<ConfigurationParameter>();
		htCredentials=new Hashtable<String, ArrayList<ConfigurationParameter>>();
		readConfigurationFile();
	}
	public ArrayList<ConfigurationParameter> getCredForService(String _serviceName)
	{
		return htCredentials.get(_serviceName.toLowerCase());
	}
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
					if (parts[1].equals(DN_User))
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
