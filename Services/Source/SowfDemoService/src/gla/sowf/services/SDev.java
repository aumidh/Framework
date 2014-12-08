package gla.sowf.services;

import gla.sowf.services.core.CommonFunctions;

public class SDev {

	public void calculateStandardDev(String varience)
	{
		CommonFunctions.saveToFile("In StDevCalculator Service", "Current Service:");
		
		double stDev= Math.sqrt(Double.parseDouble(varience));
		// Write the results.
		CommonFunctions.saveToFile(stDev + "", "St.De is: ");
	}
}
