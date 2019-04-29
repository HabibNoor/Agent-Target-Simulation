//message object used for sending target locations, with a recipient, a location and an agent and target id for the data passed
public class Message_TargetLoc {
	public int recipient;
	public int x;
	public int y;
	public int agentID;
	public int targetID;
	
	//object for targets, so the agents know who it belongs to, its target number, and the coordinates
	public Message_TargetLoc(int recipient, int agentID, int targetID, int x, int y)
	{
		this.recipient=recipient;
		this.agentID=agentID;
		this.targetID=targetID;
		this.x=x;
		this.y=y;
	}
}
