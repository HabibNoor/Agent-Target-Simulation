//object for grid to use to remember agent information, which has their location, their finish place, their step count, and their target info
public class GridAgentStore {
	public int x;
	public int y;
	public TargetMemory[] target = new TargetMemory[5];
	public int collectedCount;
	public int finishPlace;
	public int steps;
	public GridAgentStore() {
		this.steps = 0;
		this.finishPlace = -1;
		this.collectedCount = 0;
		this.x = -1;
		this.y = -1;
		for (int i = 0; i < 5; i++){
			this.target[i] = new TargetMemory();
		}
	}
}
