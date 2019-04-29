import java.util.ArrayList;

public class Grid {

	// create an array which holds all agents
	GridAgentStore[] agent = new GridAgentStore[5]; // 5 agents: id#, x, y, collectedCount

	// size of the grid
	int minX = 0;
	int minY = 0;
	int maxX = 100;
	int maxY = 100;

	// variable for person who finished first
	int finishOrder;

	// string which is used for visualizer to display the mdoe
	String gameType;

	// constructor initializing all variables
	public Grid(String gameType) {
		this.finishOrder = 1;
		this.gameType = gameType;

		// create each agent and put it inside the agent array
		for (int i = 0; i < 5; i++) {
			this.agent[i] = new GridAgentStore();
		}

		// initilize positions for all targets and agents
		initializeRandomTargetLocations();
		initializeRandomAgentLocations();
	}

	// get x position for the agent passed in
	int getAgentX(int agentID) {
		return this.agent[agentID].x;
	}

	// get y position for the agent passed in
	int getAgentY(int agentID) {
		return this.agent[agentID].y;
	}

	// initailize the x and y variables for every target for every agent
	void initializeRandomTargetLocations() {
		int i1, i2, j1, j2;

		// for loop for each target
		for (int i = 0; i < 25; i++) {

			// select agent that target belongs to
			i1 = (int) Math.floor(i / 5);
			i2 = Math.floorMod(i, 5);

			// generate random values for x and y, from size of the map
			agent[i1].target[i2].x = (int) Math.floor(Math.random() * (this.maxX + 1)); // 0..100
			agent[i1].target[i2].y = (int) Math.floor(Math.random() * (this.maxY + 1));

			// for each target initialized, check if the current target collides with any
			// other target
			for (int j = 0; j < i; j++) {
				j1 = (int) Math.floor(j / 5);
				j2 = Math.floorMod(j, 5);

				// checks if current target collides with any other target, by checking if x and
				// y values are equal
				if (agent[i1].target[i2].x == agent[j1].target[j2].x && agent[i1].target[i2].y == agent[j1].target[j2].y) {
					// if collides, decrement for loop and try target again
					i--;
					break;
				}
			}
		}
	}

	//Initialize all locations for agents
	void initializeRandomAgentLocations() {
		int flag = 0;
		int flag2 = 0;
		
		//loop for every agent
		for (int i = 0; i < 5; i++) {
			flag = 0;
			flag2 = 0;
			
			//random locations for every agent
			agent[i].x = (int) Math.floor(Math.random() * (this.maxX + 1)); // 0 .. 100
			agent[i].y = (int) Math.floor(Math.random() * (this.maxY + 1));

			//check if current agent collides with any other agent
			for (int j = 0; j < i; j++) {
				if (agent[i].x == agent[j].x && agent[i].y == agent[j].y) {
					//if collision, then try agent again by decrementing for loop
					i--;
					flag = 1;
					break;
				}
			}

			//if agent collided with other agent, break loop, not point in continuing
			if (flag == 1) {
				continue;
			}
			
			//check agent, with every target
			for (int k = 0; k < 5; k++) {
				for (int k2 = 0; k2 < 5; k2++) {
					if (agent[i].x == agent[k].target[k2].x && agent[i].y == agent[k].target[k2].y) {
						i--;
						flag2 = 1;
						break;
					}
				}
				//if collision, break out of loop, no point in continuing
				if (flag2 == 1) {
					break;
				}
			}

			//if collision, go to next loop
			if (flag2 == 1) {
				continue;
			}
		}
	}

	//check if every agent is done by checking if they have all targets
	boolean allAgentsDone() {
		int sum = 0;
		boolean done;
		//check if every agent has every target
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (agent[i].target[j].collected == true) {
					sum++;
				}
			}
		}
		//if sum is 25, all 5 agents collected all 5 targets
		if (sum == 25) {
			done = true;
		} else {
			done = false;
		}
		return done;
	}

	//check if any agent has all 5 targets
	boolean anyAgentDone() {
		int sum = 0;
		for (int i = 0; i < 5; i++) {
			sum = 0;
			//check if any agent has all 5 targets, if so then return true
			for (int j = 0; j < 5; j++) {
				if (agent[i].target[j].collected == true) {
					sum++;
				}
				if (sum == 5) {
					return true;
				}
			}
		}
		return false;
	}

	// this returns an array list of targets within the agent's radius, so pass
	// in all the coordinates within their radius from the agent into the function
	// this checks for any TARGETS within the set of coordinates and returns the
	// RadiusTarget arraylist of all targets found.
	ArrayList<RadiusTarget> checkForTargets(ArrayList<RadiusCoordinates> radiusCoords) {

		//get number of coordinates in radius
		int coordsInRadius = radiusCoords.size();
		
		//create arraylist of all objects to return
		ArrayList<RadiusTarget> toReturn = new ArrayList<RadiusTarget>();

		//loop through  every item inside the radius
		for (int i = 0; i < coordsInRadius; i++) {
			//loop for every agent and its targets
			for (int j = 0; j < 5; j++) {
				for (int j2 = 0; j2 < 5; j2++) {
					//if the coordinates inside the radius is equal to a targets coordinates location and is not collected yet, add it to arraylist of targets
					if (radiusCoords.get(i).x == agent[j].target[j2].x && radiusCoords.get(i).y == agent[j].target[j2].y
							&& agent[j].target[j2].collected == false) {
						RadiusTarget tempObject = new RadiusTarget(j, j2, agent[j].target[j2].x, agent[j].target[j2].y);
						toReturn.add(tempObject);
					}
				}
			}
		}

		return toReturn;
	}

	// this returns an arraylist of all the coordinates of the agents found within
	// the radius provided by the parameter
	// of radiusCoordinates
	ArrayList<RadiusCoordinates> checkForAgents(ArrayList<RadiusCoordinates> radiusCoords) {
		
		//get number of coordinates in radius
		int coordsInRadius = radiusCoords.size();
		
		//create arraylist to return
		ArrayList<RadiusCoordinates> toReturn = new ArrayList<RadiusCoordinates>();
		//for every coordinate and every agent
		for (int i = 0; i < coordsInRadius; i++) {
			for (int j = 0; j < 5; j++) {
				//check if the coordinate is equal to an agent, if so add it to arraylist of targets inside radius
				if (radiusCoords.get(i).x == agent[j].x && radiusCoords.get(i).y == agent[j].y) {
					RadiusCoordinates tempObject = new RadiusCoordinates(agent[j].x, agent[j].y);
					toReturn.add(tempObject);
				}
			}
		}
		return toReturn;
	}

	//this will update the agents position by getting the agentsID, its new X and Y value
	boolean updateAgentPosition(int agentID, int x, int y) {
		//temporary variables for the agents current X and Y values
		int prevX = agent[agentID].x;
		int prevY = agent[agentID].y;

		//update the agents X and Y values to parameters passed
		agent[agentID].x = x;
		agent[agentID].y = y;

		//if agent does not have all targets, update the number of steps it took
		if (this.agent[agentID].collectedCount != 5) {
			this.agent[agentID].steps++;
		}

		//if the previous X  and Y of the agent is equal to the value to be updated too, return false
		if (prevX == x && prevY == y) {
			return false;
		}
		return true;
	}

	//update every agent's collected count, and checks if it wins
	//takes in arraylist of collected targets
	void updateCollected(ArrayList<CollectedTarget> collected) {
		//for every value inside the arraylist
		for (int j = 0; j < collected.size(); j++) {
			// if previously not collected, increment collect count
			if (agent[collected.get(j).agentID].target[collected.get(j).targetID].collected == false) {
				this.agent[collected.get(j).agentID].collectedCount++;
			}

			// set it to collected
			agent[collected.get(j).agentID].target[collected.get(j).targetID].collected = true;

			// if collected count is 5 and dont have a place assigned to them, set the
			// finish order for this agent
			if (this.agent[collected.get(j).agentID].collectedCount == 5) {
				if (this.agent[collected.get(j).agentID].finishPlace == -1) {
					this.agent[collected.get(j).agentID].finishPlace = this.finishOrder;
					this.finishOrder++;
				}
			}
		}

	}

	//get the agents happiness 
	public double getHappiness(int agentID) {
		return (double) agent[agentID].collectedCount / (agent[agentID].steps + 1);
	}
}
