package analyzer;

import java.io.File;
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
 * This class is the main class of the Analyzer component. This class read the WF XML file.
 * and then find out the execution status of WF based on given credentials of the contained services.
 * @author Sardar Hussain
 *
 */
public class Workflow
{
	private List<WFConstruct> constructs;
	private Element wfElement;
	Hashtable<String,Boolean> decisions=new Hashtable<String,Boolean>();
	public int noOfOperators;
	public int workflowSize;
	private String wfFile;
	// The constructor of the class which initailize the class variables.
	public Workflow(String _wfFile,Hashtable<String,Boolean> _decisions)
	{
		wfFile=_wfFile;
		constructs=new ArrayList<WFConstruct>();
		decisions=_decisions;
	}
	/**
	 * The starter function which triggers the process of checking execution
	 * of the workflow based on the constructs.
	 */
	public ExecutableStatus startProcess()
	{
		loadXML();
		getConstructs();
		return checkWFExecutable();
	}
	/**
	 * The main function call this method in order to load the XML file and to
	 * create the object of the workflow class, which further deals with all the
	 * necessary processing.
	 */
	public void loadXML()
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
		} catch (SAXException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		} 
	}
	/**
	 * This function will be called for each "Service" constructs during the recursive process 
	 * of reading the workflow constructs to decide whether the Service is authorized or not based on
	 * the decisions we stored earlier for each service.
	 */
	public boolean isServiceExecutable(String strName)
	{
		if(decisions.size()>0)
			return decisions.get(strName.toLowerCase());
		return false;
	}
	/**
	 * The recursive function that read every workflow construct and check their status
	 * of execution based on the type of the construct and finally on the service authorization decision.
	 * The input parameter of this function is a construct, which initially will be the main construct and
	 * then it will be called with every child construct untill it encounter a service construct.  
	 */
	public ExecutableStatus getExecutionStatus(WFConstruct con)
	{
		if(con.getConstructType()==WFConstructsType.AND_SPLIT || con.getConstructType()==WFConstructsType.AND_JOIN || con.getConstructType()==WFConstructsType.SEQUENCE)
		{
			List<WFConstruct> conList=con.getList();
			boolean isPE=false;
			for (int i=0;i<conList.size();i++)
			{
				WFConstruct t=conList.get(i);
				if(t.getConstructType()==WFConstructsType.SERVICE)
				{
					boolean res=isServiceExecutable(t.getName());
					if(res==false)
					{
						t.setStatus(ExecutableStatus.Non_Executable);
						return ExecutableStatus.Non_Executable;
					}
					t.setStatus(ExecutableStatus.Executable);
				}
				else
				{
					ExecutableStatus _status=getExecutionStatus(t);
					if(_status==ExecutableStatus.Non_Executable)
					{
						t.setStatus(ExecutableStatus.Non_Executable);
						return ExecutableStatus.Non_Executable;
					}
					else if(_status==ExecutableStatus.Potential_Executable)
						isPE=true;
					t.setStatus(_status);
				}
			}
			if(isPE==true)
				return ExecutableStatus.Potential_Executable;
			else
				return ExecutableStatus.Executable;
		}
		else if(con.getConstructType()==WFConstructsType.OR_JOIN || con.getConstructType()==WFConstructsType.XOR_JOIN)
		{
			List<WFConstruct> conList=con.getList();
			boolean isPE=false,isNE=false,isE=false;
			for (int i=0;i<conList.size();i++)
			{
				WFConstruct t=conList.get(i);
				if(t.getConstructType()==WFConstructsType.SERVICE)
				{
					boolean res=isServiceExecutable(t.getName());
					if(res==false)
					{
						t.setStatus(ExecutableStatus.Non_Executable);
						return ExecutableStatus.Non_Executable;
					}
					else
						isE=true;
					t.setStatus(ExecutableStatus.Executable);
				}
				else
				{
					ExecutableStatus _status=getExecutionStatus(t);
					t.setStatus(_status);
					if(_status==ExecutableStatus.Non_Executable)
						isNE=true;
					else if(_status==ExecutableStatus.Potential_Executable)
						isPE=true;
					else
						isE=true;
				}
			}
			if(isE==true && isPE==false && isNE==false) //When all child constructs are executable
				return ExecutableStatus.Executable;
			else if(isPE==true) 						//When any child constructs are Potential Executable
				return ExecutableStatus.Potential_Executable;
			else                          				// When there is no construct PE or E and irrespective of SERVICE is E or NE
				return ExecutableStatus.Non_Executable;
		}
		else if(con.getConstructType()==WFConstructsType.OR_SPLIT || con.getConstructType()==WFConstructsType.XOR_SPLIT)
		{
			List<WFConstruct> conList=con.getList();
			boolean isE=false,isPE=false, isNE=false;
			for (int i=0;i<conList.size();i++)
			{
				WFConstruct t=conList.get(i);
				if(t.getConstructType()==WFConstructsType.SERVICE)
				{
					boolean res=isServiceExecutable(t.getName());
					if(res==true)
					{
						t.setStatus(ExecutableStatus.Executable);
						isE=true;
					}
					else
					{
						isNE=true;
						t.setStatus(ExecutableStatus.Non_Executable);
						if(isE==true || isPE==true)
							return ExecutableStatus.Potential_Executable;
					}
					
				}
				else
				{
					ExecutableStatus _status=getExecutionStatus(t);
					t.setStatus(_status);
					if(_status==ExecutableStatus.Non_Executable)
					{
						isNE=true;
						if (isE==true || isPE==true)
							return ExecutableStatus.Potential_Executable;
					}
					else if(_status==ExecutableStatus.Potential_Executable)
						isPE=true;
					else if(_status==ExecutableStatus.Executable)
						isE=true;
				}
			}
			if(isE==false && isPE==false)             			  // When all childs are not executable
				return ExecutableStatus.Non_Executable;
			else if(isE==true && isPE==false && isNE==false)  	  // When all childs are executable    
				return ExecutableStatus.Executable;
			else if (isPE==true || (isE==true && isNE==true))     // when any child is potential executable
				return ExecutableStatus.Potential_Executable;
		}
		return ExecutableStatus.Non_Executable;
	}
	/**
	 * This function triggers the recursive function and then finally display the
	 * output that whether workflow is executable or not.
	 */
	public ExecutableStatus checkWFExecutable()
	{
		WFConstruct main=constructs.get(0);
		return getExecutionStatus(main);
	}
	/**
	 * This function transform the XML based workflow into a self defined format. The structure of this format
	 * is defined in WFConstruct.java. This function reads the elements at the first level and for each element it 
	 * further triggers a recursive function to read the child elements until it reaches to an end.
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
			System.out.println("In Exception.");
		}
	}
	/**
	 * This is a recursive function that is triggered from the "getConstruct" function to read the child of input
	 * construct parameter and further this call itself to read the child elements of a child.
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
			
		}
	}
}
