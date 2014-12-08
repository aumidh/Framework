package gla.sowf.services;

import gla.sowf.services.core.CommonFunctions;
import gla.sowf.services.core.ProtocolHandler;

public class MathSolver {
	public void calculateVar(String array) throws Exception
	{
		CommonFunctions.saveToFile("In MathSolver Service", "Current Service:");
		String mode=CommonFunctions.getWFExecMode();
		ProtocolHandler protHandler=new ProtocolHandler();
		String[] paramNames=new String[1];
		String[] paramValues=new String[1];
		paramNames[0]="array";
		paramValues[0]=array;
		
		if(mode.toLowerCase().equals("w_security")==true)
		{
			protHandler.callWSecurityNextService("http://services.sowf.gla","VarienceCalculator",true,paramNames,paramValues);
		}
		else
		{
			protHandler.callWOSecurityNextService("http://services.sowf.gla", "VarienceCalculator", "http://127.0.0.1:8080/axis2/services/VarienceCalculator", "urn:calculateVarience", paramNames, paramValues);
		}
	}
}
