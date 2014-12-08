package gla.sowf.services;

import gla.sowf.services.core.CommonFunctions;
import gla.sowf.services.core.ProtocolHandler;

public class VarienceCalculator {

	public void calculateVarience(String array) throws Exception
	{
		CommonFunctions.saveToFile("In VarienceCalculator Service", "Current Service:");
		
		String[] arrayValues=array.split(",");
		if(arrayValues.length>0)
		{
			double total = 0.0;
			for(int i=0;i<arrayValues.length;i++)       // calculate total
			{
				total += Double.parseDouble(arrayValues[i]);
		    }
			double mean=total/arrayValues.length;       // calculate mean
			double varience=0.0;
			for(int i=0;i<arrayValues.length;i++)
			{
				double val=Double.parseDouble(arrayValues[i]);
				varience += (mean-val)*(mean-val);
			}
			varience=varience/arrayValues.length;     // calculate varience
			CommonFunctions.saveToFile(varience + "", "Varience is:");
		} // Record values are null
		else
		{
			CommonFunctions.saveToFile("Array don't contains any values", "Varience Calculator Message:");
		}
	}
	private void callStDevService(double varience) throws Exception
	{
		String mode=CommonFunctions.getWFExecMode();
		ProtocolHandler protHandler=new ProtocolHandler();
		String[] paramNames=new String[1];
		String[] paramValues=new String[1];
		paramNames[0]="varience";
		paramValues[0]=varience + "";
		
		if(mode.toLowerCase().equals("w_security")==true)
		{
			protHandler.callWSecurityNextService("http://services.sowf.gla","StDevCalculator",false,paramNames,paramValues);
		}
		else
		{
			
			protHandler.callWOSecurityNextService("http://services.sowf.gla", "StDevCalculator", "http://127.0.0.1:8080/axis2/services/StDevCalculator", "urn:calculateStDev", paramNames, paramValues);
		}
	}
}
