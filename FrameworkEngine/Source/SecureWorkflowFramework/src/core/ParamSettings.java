package core;

import java.io.BufferedReader;
import java.io.FileReader;
/**
 * This class is responsible for Parameter file reading and storing values for all configuration.
 * @author Sardar Hussain
 *
 */
public class ParamSettings 
{
	public static String workflowFile;       
	public static String workflowType;
	public static String DN_User;
	public static String DN_Engine;
	public static String workflowExecPathFile;
	public static String securityModel;
	public static String mode;
	public static void readParamFile()
	{
		try {
			String paramFile=System.getProperty("user.dir") + "//SWFConfigFiles//settings.param";
			BufferedReader br = new BufferedReader(new FileReader(paramFile));
			String line="";
			while ( (line = br.readLine()) != null) {
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts[0].toLowerCase().startsWith("dnuser")){
					DN_User=parts[1];
				}
				if(parts[0].toLowerCase().startsWith("dnengine")){
					DN_Engine=parts[1];
				}
				if(parts[0].toLowerCase().startsWith("workflowtype")){
					workflowType=parts[1];
				}
				if(parts[0].startsWith("workflowFile")){
					workflowFile=parts[1];
				}
				if(parts[0].toLowerCase().startsWith("workflowexecpathfile")){
					workflowExecPathFile=parts[1];
				}
				if(parts[0].toLowerCase().startsWith("securitymodel")){
					securityModel=parts[1];
				}
				if(parts[0].toLowerCase().startsWith("mode")){
					mode=parts[1];
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
