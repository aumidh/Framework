package core;
import java.util.ArrayList;
/**
 * This class holds the list of the responses of nested services for the decentralized WF case
 * @author Sardar Hussain
 *
 */
public class OtherResources {
	ArrayList<OtherResource> otherResources=new ArrayList<OtherResource>();
	public OtherResources()
	{
	}
	public void addResource(OtherResource or)
	{
		otherResources.add(or);
	}
	public void removeResource(OtherResource or)
	{
		otherResources.remove(or);
	}
}
