//agent's memory of targets (5 of these classes per AgentMemory), info about collected or not, seen or not and sent out or not
public class TargetMemory {
	public int x;
	public int y;
	public boolean collected;
	public boolean seen;
	public boolean sent;
	
	public TargetMemory() {
		this.x = -1;
		this.y = -1;
		this.collected = false;
		this.seen = false;
		this.sent = false;
	}
	
}