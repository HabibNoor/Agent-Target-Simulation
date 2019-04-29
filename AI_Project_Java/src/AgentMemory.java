//memory for agents to use, with each agent's collected count and their targets in objects
public class AgentMemory {
	public TargetMemory[] target = new TargetMemory[5]; //index represents target number
	public int collectedCount;
	public AgentMemory() {
		for (int i = 0; i < 5; i++){
			this.target[i] = new TargetMemory();
		}

		this.collectedCount = 0;
	}
}