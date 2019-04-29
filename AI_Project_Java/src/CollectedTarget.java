//target that is collected, represented by the agent and target id combination, used to send to grid
public class CollectedTarget {
	public int targetID;
	public int agentID;
	
	public CollectedTarget(int agentID, int targetID) {
		this.targetID = targetID;
		this.agentID = agentID;
	}
}
