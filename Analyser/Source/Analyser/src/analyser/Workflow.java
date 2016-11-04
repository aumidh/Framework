package analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;




//import org.apache.commons.lang.time.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The main class of the Analyzer component. This class provides functions to read the WF xml file
 * and to figure out the execution status of WF based on the given credentials of services contained in the WF file.
 * @author Sardar Hussain
 *
 */
public class Workflow
{
	private List<WFConstruct> constructs;
	private Element wfElement;
	private Hashtable<String,Boolean> decisions=new Hashtable<String,Boolean>();
	public int noOfOperators;
	public int workflowSize;
	private String wfFile;
	private String authDecisionFile;
	// The constructor of the class which initailize the class variables.
	public Workflow()
	{
		wfFile="";
		constructs=new ArrayList<WFConstruct>();
		authDecisionFile="";
		wfElement=null;
	}
	private boolean readSettings()
	{
		
			String paramFile=System.getProperty("user.dir") + "//AnalyserSettings//settings.param";
			BufferedReader br=null;
			try {
					br = new BufferedReader(new FileReader(paramFile));
			} catch (FileNotFoundException e) {
				System.out.println("The program can't find out the settings file and therefore exiting. Please provide a valid settings file.");
				System.exit(1);
			}
			String line="";
			try {
				while ( (line = br.readLine()) != null) {
					if (line.startsWith("#") ) { 
						continue;
					}
					String[] parts = line.split("=");
					if(parts[0].toLowerCase().startsWith("workflowfile")){
						wfFile=parts[1];
					}
					if(parts[0].toLowerCase().startsWith("authdecisions")){
						authDecisionFile=parts[1];
					}
				}
				br.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}
			if(wfFile.isEmpty() == false && authDecisionFile.isEmpty() == false)
				return true;
			else{
				System.out.println("The settings file should include file location for the workflow file and authorization decisions file.");
				return false;
			}
	}
	/**
	 * This function triggers the process of checking the execution workflow.
	 * @throws Exception 
	 */
	public ExecutableStatus startProcess() throws Exception
	{
		if(readSettings()==true)
		{
			getAuthDecisionFromFile();
			getWFNodeFromXML();
			getConstructs();
			return checkWFExecutable();
		}
		else
			throw new Exception("Unable to read settings file.");
	}
	/**
	 * This function read the authorization decisions for each service, which is currently provided from a text file.
	 */
	private void getAuthDecisionFromFile()
	{
		BufferedReader br=null;
		try {
				br = new BufferedReader(new FileReader(authDecisionFile));
		} catch (FileNotFoundException e) {
			System.out.println("The specified authorization decision file is not available. Please provide a valid authorization file and then re-run the program");
			System.out.println("The program will now exit.");
			System.exit(0);
		}
		String line="";
		try {
			while ( (line = br.readLine()) != null) 
			{
				if (line.startsWith("#") ) { 
					continue;
				}
				String[] parts = line.split("=");
				if(parts.length==2)
				{
					if(decisions.size()>0 && decisions.containsKey(parts[0].toLowerCase())==true)
						continue;
					else
					{
						boolean status;
						if(parts[1].toLowerCase().equals("true")==true)
							status=true;
						else
							status=false;
						decisions.put(parts[0].toLowerCase(), status);
					}
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("The program is unable to read the provided file and therefore exiting. Please check the format of the provided file.");
			System.exit(1);
		}
		
	}
	/**
	 * This method read the workflow XML file and retreive the root workflow node.
	 */
	private void getWFNodeFromXML()
	{
		try 
		{
			File file = new File(wfFile);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			NodeList lst = doc.getElementsByTagName("workflow");
			for (int i = 0; i < lst.getLength(); i++)
			{
				Node myNode = lst.item(i);
				if (myNode.getNodeType()== Node.ELEMENT_NODE) 
				{
					wfElement = (Element) myNode;
					break;
				}
			}
			if(wfElement==null){
				System.out.println("The XML file doesn't contain the root workflow node. Please provide a correct XML file, where the root node should be named \"Workflow\"");
				System.exit(1);
			}
		} catch (Exception e) 
		{
			System.out.println("There is a problem while reading the provided XML file and therefore, the program is exiting. The full details of the error is provided below,");
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * This function return the corresponding execution status when called. This status
	 * is obtained from the decisions list (where the list is currently populated from a file as a proof of concept. However, in case the when the authorization
	 * decision can be obtained from external sources then this function should implement the procedure of obtaining the authorization decision for the given service)
	 * @param strName the name of the service, for which the authorization status is required.
	 */
	private boolean isServiceExecutable(String strName)
	{
		if(decisions.size()>0)
		{
			return decisions.get(strName.toLowerCase());
		}
		return false;
	}
	/**
	 * This function check the execution status of a given node. This is a recursive function, which call all the child node 
	 * in order to obtain the execution status of the given node. Thus, if call with the first node of the workflow file i.e. (root node of the exml file)
	 * then it will return the execution status of the workflow.  
	 * @param con The workflow node.
	 */
	private ExecutableStatus getExecutionStatus(WFConstruct con){
		List<WFConstruct> conList=con.getList();
		boolean isPE=false,isNE=false,isE=false;
		for (int i=0;i<conList.size();i++){
			WFConstruct childNode=conList.get(i);
			if(childNode.getConstructType()==WFConstructsType.SERVICE)
			{
				boolean res=isServiceExecutable(childNode.getName());
				if(res==true)
				{
					isE=true;
					childNode.setStatus(ExecutableStatus.Executable);
				}
				else{
					isNE=true;
					childNode.setStatus(ExecutableStatus.Non_Executable);
					if(con.getConstructType()==WFConstructsType.OR_SPLIT || con.getConstructType()==WFConstructsType.XOR_SPLIT){
						if(isE==true || isPE==true) 
							return ExecutableStatus.Potential_Executable;
					}
					else{
						return ExecutableStatus.Non_Executable;
					}
				}
			}else{ // End of Service
				ExecutableStatus _status = getExecutionStatus(childNode);
				if (_status == ExecutableStatus.Non_Executable){
					isNE = true;
					if(con.getConstructType()==WFConstructsType.AND_JOIN || con.getConstructType()==WFConstructsType.AND_SPLIT || con.getConstructType()==WFConstructsType.SEQUENCE)
						return ExecutableStatus.Non_Executable;
					else if(con.getConstructType()==WFConstructsType.OR_JOIN || con.getConstructType()==WFConstructsType.XOR_JOIN)
						continue;
					else{
						if(isE==true || isPE==true)
							return ExecutableStatus.Potential_Executable;
					}
				}else if(_status == ExecutableStatus.Potential_Executable){
					isPE = true;
				}else{
					isE = true;
				}
			}
		} // End of For loop
		if (isE == true && isPE==false && isNE == false)
			return ExecutableStatus.Executable;
		else if(isPE == true || isE==true)
			return ExecutableStatus.Potential_Executable;
		else
			return ExecutableStatus.Non_Executable;
	}
	/**
	 * This function triggers the recursive function for checking the execution status of the workflow.
	 * @return The execution status of the workflow.
	 */
	private ExecutableStatus checkWFExecutable()
	{
		WFConstruct main=null;
		if(constructs.size()>0){
			main=constructs.get(0);
		}else{
			System.out.println("The provided XML file doesn't contain workflow node.");
			System.exit(1);
		}
		return getExecutionStatus(main);
	}
	/**
	 * This function transform the XML based workflow into a self defined format. 
	 * The structure of this format is defined in WFConstruct.java. This function 
	 * reads the elements at the first level and for each element it further triggers 
	 * a recursive function to read the child elements until it reaches to an end.
	 */
	public void getConstructs()
	{
		try
		{
			NodeList lst = wfElement.getChildNodes();
			for (int i = 0; i < lst.getLength(); i++)
			{
				Node myNode = lst.item(i);
				if (myNode.getNodeType()== Node.ELEMENT_NODE) 
				{
					String nodeName=myNode.getNodeName();
					Element elem=(Element) myNode;
					WFConstructsType nodeType=ConstructTypeNameRelationship.getConstructType(nodeName);
					WFConstruct cons=new WFConstruct(nodeType, elem);
					constructs.add(cons);
					if(nodeType.equals(WFConstructsType.SERVICE)==false)
					{
						noOfOperators++; // Count as Operator...
						String id=elem.getAttribute("id");
						cons.setID(id);
						getChildConstructs(cons);
					}
					else
					{
						workflowSize++;
						String name=elem.getAttribute("name");
						String uri=elem.getAttribute("uri");
						cons.setServiceTagAttribute(name, uri);
					}
				}
			}
		}catch(Exception ex)
		{
			System.out.println("An unhandeld exception has occured while retreiving child nodes of workflow node. The full details are provided below,");
			ex.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * This is a recursive function that is triggered from the "getConstruct" function 
	 * to read the child of given construct (Node). This further call itself to read the child elements of a child.
	 * @param parentConstruct The node, for which the child elements has to be obtained.
	 */
	public void getChildConstructs(WFConstruct parentConstruct) 
	{
		try
		{
			NodeList lst = parentConstruct.getElement().getChildNodes(); 
			for (int i = 0; i < lst.getLength(); i++)
			{
				Node myNode = lst.item(i);
				if (myNode.getNodeType()== Node.ELEMENT_NODE) 
				{
					String nodeName=myNode.getNodeName(); 
					Element elem=(Element) myNode;
					WFConstructsType nodeType=ConstructTypeNameRelationship.getConstructType(nodeName);
					WFConstruct cons=new WFConstruct(nodeType, elem); 
					parentConstruct.addConstruct(cons); 
					if(nodeType.equals(WFConstructsType.SERVICE)==false) 
					{
						String id=elem.getAttribute("id"); 
						cons.setID(id);
						getChildConstructs(cons); 
					}
					else
					{
						workflowSize++;
						String name=elem.getAttribute("name");
						String uri=elem.getAttribute("uri");
						cons.setServiceTagAttribute(name, uri);
					}
				}
			}
		}catch(Exception ex)
		{
			System.out.println("An unhandled exception has occured, while reading the child constructs of " + parentConstruct.getName() + ". The full error details are provided below \n" );
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
