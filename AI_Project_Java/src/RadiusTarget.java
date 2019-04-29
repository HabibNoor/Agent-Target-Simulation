//target within radius location, used for the radius generation functions
public class RadiusTarget {
	public int x;
	public int y;
	public int agentID;
	public int targetID;
	
	//object for targets, so the agents know who it belongs to, its target number, and the coordinates
	public RadiusTarget(int agentID, int targetID, int x, int y)
	{
		this.agentID=agentID;
		this.targetID=targetID;
		this.x=x;
		this.y=y;
	}

}
