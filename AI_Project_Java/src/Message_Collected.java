//message type for telling agents how many targets they have collected
public class Message_Collected {

	public int sourceAgentID;
	public int collectedCount;
	
	//object for targets, so the agents know who it belongs to, its target number, and the coordinates
	public Message_Collected(int sourceAgentID, int collectedCount)
	{
		this.sourceAgentID=sourceAgentID;
		this.collectedCount=collectedCount;
	}
}
