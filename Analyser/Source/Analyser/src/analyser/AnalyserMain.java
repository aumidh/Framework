package analyser;

public class AnalyserMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Workflow wf=new Workflow();
		ExecutableStatus status=wf.startProcess();
		if(status==ExecutableStatus.Executable)
			System.out.println("Workflow is executable");
		else if(status==ExecutableStatus.Potential_Executable)
		{
			System.out.println("Workflow is potentially executable");
		}
		else
			System.out.println("Workflow is not executable");
	}

}
