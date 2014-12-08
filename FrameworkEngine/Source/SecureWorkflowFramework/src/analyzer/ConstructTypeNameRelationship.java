package analyzer;
/**
 * This class provides the relation among what are the different tag names we used for the operators
 * in the XML file and what are their corresponding construct types from the enumerator we defines in
 * the "WFConstructsType". This is used enhance the readability and maintainability of the code.
 * @author Sardar Hussain
 *
 */
public class ConstructTypeNameRelationship 
{
	private static WFConstructsType[] constructType={WFConstructsType.SERVICE,WFConstructsType.AND_SPLIT,WFConstructsType.AND_JOIN,WFConstructsType.OR_SPLIT,WFConstructsType.OR_JOIN,WFConstructsType.XOR_SPLIT,WFConstructsType.XOR_JOIN,WFConstructsType.SEQUENCE};
	private static String[] tagName={"SERVICE","AND_SPLIT","AND_JOIN","OR_SPLIT","OR_JOIN","XOR_SPLIT","XOR_JOIN","SEQUENCE"};
	private static int size=8;
	/**
	 * This function will return the associated construct type for a given XML tag.
	 * @param _tagName the construct type tag Name retreived from XML file.
	 * @return The Construct type.
	 */
	public static WFConstructsType getConstructType(String _tagName)
	{
		for(int i=0;i<size;i++)
		{
			if(tagName[i].equals(_tagName.toUpperCase())==true)
				return constructType[i];
		}
		return null;
	}
}
