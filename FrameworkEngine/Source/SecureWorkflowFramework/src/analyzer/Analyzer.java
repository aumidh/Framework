package analyzer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import utils.StopWatch;
import utils.UtilPerformanceEvaluation;
import core.ParamSettings;
/**
 * This class provides interface to the Analyzer main algorithm. 
 * The enactment engine will triggers this class which further initiate the main class Workflow. 
 * @author Sardar Hussain
 *
 */
public class Analyzer {
	
	public Analyzer()
	{
		
	}
	/**
	 * This function retrieves the execution status of the WF using the analyzer algorithm. 
	 * @param _ht The execution status repository holding <Key,Value> pair for <Service, Execution Status> 
	 * @return The execution status of the WF.
	 */
	public static ExecutableStatus getExecutionStatus(Hashtable<String,Boolean> _ht)
	{
		Workflow wf=new Workflow(ParamSettings.workflowExecPathFile, _ht);
		return wf.startProcess();
	}
}
