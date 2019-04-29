import java.util.ArrayList;

public class Agent {

	//info about their agent#, their position and their current direction
	int agentID;
	int x;
	int y;
	String direction; //u=up, d=down, l=left, r=right, n=no movement
	
	//memory array of 5 objects, 1 for each of the 5 agents (including themselves)
	AgentMemory[] memory = new AgentMemory[5]; // index represents agent #
	//2d array for keep track of which coordinates of the map they have seen
	int[][] gridChecked = new int[101][101]; // 1 for visited/seen, 0 for not
	//count of steps they have taken
	int stepsTaken;
	//vision radius
	final int radius = 10;
	
	//other variables for assisting in direction choice
	
	//remembering the best direction to head in (towards a corner that is most empty)
	String emptyDir; //NW, SW, NE, SE
	//timer for how long after seeing an agent to backoff for
	int avoidAgentCount;
	//direction to head in when moving away from an agent they are too close to
	String avoidAgentDir;
	//variable to ensure messages are only sent out once
	int previousCount;
	//variable to track what the first place agent has for # of targets collected
	int firstPlaceCount;

	//constructor
	public Agent(int id, int x, int y) { // ids from 0..4
		this.agentID = id;
		this.x = x;
		this.y = y;
		this.stepsTaken = 0;
		this.avoidAgentCount = 0;
		this.previousCount = 0;
		this.firstPlaceCount = 0;
		
		//random initial direction
		int tempDir = (int) Math.floor(Math.random() * 4);
		if (tempDir == 0) {
			this.direction = "u";
		} else if (tempDir == 1) {
			this.direction = "r";
		} else if (tempDir == 2) {
			this.direction = "d";
		} else {
			this.direction = "l";
		}

		for (int j = 0; j < 101; j++) {
			for (int k = 0; k < 101; k++) {
				this.gridChecked[j][k] = 0;
			}
		}

		for (int i = 0; i < 5; i++) {
			this.memory[i] = new AgentMemory();
		}

	}

	
	//move the agent and update the grid global store with your new position
	boolean moveAgent(Grid grid) {
		
		//update your step counter
		this.stepsTaken++;
		
		//and move in your direction if possible
		if (this.direction == "u" && this.y <= 99) {
			this.y++;
		} else if (this.direction == "r" && this.x <= 99) {
			this.x++;
		} else if (this.direction == "d" && this.y >= 1) {
			this.y--;
		} else if (this.direction == "l" && this.x >= 1) {
			this.x--;
		} else { //if invalid direction or direction of "n" (no movement), don't move, still update position to count steps for grid memory
			grid.updateAgentPosition(this.agentID, this.x, this.y);
			return false;
		}
		
		//tell the grid about your new position
		grid.updateAgentPosition(this.agentID, this.x, this.y);
		
		return true;
	}

	//main function of the entire algorithm/project, takes in a list of agents that are currently visible to the current agent
	//	and uses this information as well as their target locations, and their heuristic function to determine which corner from their location
	//	is least explored, as well as random movement
	boolean updateDirection(ArrayList<RadiusCoordinates> currentSeenAgents) {
		
		//every 200 steps or when running into a wall, generate a new heuristic direction to travel in that will be used 
		//		if have no other information (towards most unexplored corner)
		//is not done every step so that the agent does not move a bit in one direction, turn around, and go back and forth
		int every200Steps = this.stepsTaken%200;
		if (every200Steps == 0 || this.runningIntoWall() != "n") {
			
			//calculate the "sum" of the number of positions unexplored in the 4 corners around your agents
			int sumNW = 0;
			int sumSW = 0;
			int sumSE = 0;
			int sumNE = 0;
			for (int yNW = this.y + 1; yNW < 101; yNW++) {
				for (int xNW = 0; xNW < this.x; xNW++) {
					if (this.gridChecked[xNW][yNW] == 0) {
						sumNW ++;
					}
				}
			}
			for (int ySW = 0; ySW < this.y; ySW++) {
				for (int xSW = 0; xSW < this.x; xSW++) {
					if (this.gridChecked[xSW][ySW] == 0) {
						sumSW ++;
					}
				}
			}
			for (int yNE = this.y + 1; yNE < 101; yNE++) {
				for (int xNE = this.x + 1; xNE < 101; xNE++) {
					if (this.gridChecked[xNE][yNE] == 0) {
						sumNE ++;
					}
				}
			}
			for (int ySE = 0; ySE < this.y; ySE++) {
				for (int xSE = this.x + 1; xSE < 101; xSE++) {
					if (this.gridChecked[xSE][ySE] == 0) {
						sumSE ++;
					}
				}
			}
			
			//set the emptyDir to the direction that is the least explored
			if (sumNW == Math.max(Math.max(sumNW, sumSW), Math.max(sumSE, sumNE))) {
				this.emptyDir = "NW";
			} else if (sumSW == Math.max(Math.max(sumNW, sumSW), Math.max(sumSE, sumNE))) {
				this.emptyDir = "SW";
			} else if (sumNE == Math.max(Math.max(sumNW, sumSW), Math.max(sumSE, sumNE))) {
				this.emptyDir = "NE";
			} else {
				this.emptyDir = "SE";
			}
		}
		
		
		
		//if the list of agents seen in the current position/iteration is not zero (aka can currently see an agent)
		//	attempt to move away from them, by figuring out the average agent location of those you can see, and picking the direction
		// 	that gets the agent furthest from them the fastest, and travels in it
		//	this also starts a counter of time in which they must most away from them and then perform random movement, in order to
		//	prevent getting stuck indefinitely if agents see targets behind other agents in their way
		if (currentSeenAgents.size() != 0) {
			//get away from agents if any within your radius
			
			//count and sum of X and Y values for agents seen
			int n = currentSeenAgents.size();
			int sumX = 0;
			int sumY = 0;
			
			for (int j = 0; j < n; j++) {
				sumX += currentSeenAgents.get(j).x;
				sumY += currentSeenAgents.get(j).y;
			}
			
			//calculate the average X and Y position of seen agents in order to move away from that
			int avgX = (int) Math.round(((double)sumX)/n);
			int avgY = (int) Math.round(((double)sumY)/n);
						
			//check which direction to move to get away from the average agent position
			if (Math.abs(this.x - avgX) == Math.abs(this.y - avgY) && Math.abs(this.y - avgY) == 0) {
				//no moves
				this.avoidAgentDir = "n";
			} else if (Math.abs(this.x - avgX) >= Math.abs(this.y - avgY)) {
				//if further from the average agent in horizontal, head away from them horizontally ( to get away faster)
				if ((this.x - avgX) > 0) {
					this.avoidAgentDir = "r";
				} else {
					this.avoidAgentDir = "l";
				}
			} else {
				//else further from the average agent in vertical, head away from them vertically
				if ((this.y - avgY) > 0) {
					this.avoidAgentDir = "u";
				} else {
					this.avoidAgentDir = "d";
				}
			}
			
			//set a timer in order to continue moving away from them for and update the direction of the agent
			this.avoidAgentCount = 8;
			this.direction = this.avoidAgentDir;			
			
			
		} else if (this.avoidAgentCount > 4){
			//if seen an agent in prev 4 steps, continue running away from them for 4 steps
			//	before returning to other choices of movement
			this.avoidAgentCount--;
		} else if (this.avoidAgentCount > 0) {
			//if seen agent not long ago, randomize, to avoid getting stuck running away from eachother, then back towards targets
			int randHelper = (int) Math.floor(Math.random() * 4);
			if (randHelper%4 == 0) {
				this.direction = "d";
			} else if (randHelper%4 == 1) {
				this.direction = "r";
			} else if (randHelper%4 == 2) {
				this.direction = "l";
			} else {
				this.direction = "u";
			}
			this.avoidAgentCount--;
		} else {
			//check if any targets to head towards, and if any, calculate the best direction to travel
			//	if any seen, head to the closest target first always
			
			//initialize variable stepsToTarget to high value, as well as variable for counting temporary best steps, and bestDir to travel
			int stepsToTarget = 9999999;
			int tempSteps;
			String bestDir = "x";
			//loop through the targets in your memory, calculating the distance to each, and remember the target that is 
			//	the closest as the one to head towards
			for (int u = 0; u < 5; u++) {
				//only consider targets that you have not collected, and targets who have valid positions (-1 is default for unseen)
				if (memory[agentID].target[u].collected != true && memory[agentID].target[u].x != -1 && memory[agentID].target[u].y != -1) {
					//calculate steps to the target
					tempSteps = Math.abs(this.x - memory[agentID].target[u].x) + Math.abs(this.y - memory[agentID].target[u].y);
					//if target is closer than previous loop
					if (tempSteps < stepsToTarget) {
						//set the new stepcount, and set bestDir to the direction towards that target
						//	moves towards them to shorten the x/y distance that is largest, then moves diagonally (zigzag)
						stepsToTarget = tempSteps;
						if (Math.abs(this.x - memory[agentID].target[u].x) >= Math.abs(this.y - memory[agentID].target[u].y)) {
							//if X diff is larger (between agent and their target), go left/right to get closer
							if ((this.x - memory[agentID].target[u].x) > 0) {
								//left
								bestDir = "l";
							} else {
								bestDir = "r";
							}
						} else {
							//else up/down, based on sign of difference to get closer to target
							if ((this.y - memory[agentID].target[u].y) > 0) {
								//down
								bestDir = "d";
							} else {
								bestDir = "u";
							}
						}
					}
				}
			}
			
			
			
			//check if a target direction was picked (which will be done if any targets known and not collected)
			if (stepsToTarget < 9999999 && !bestDir.equals("x")) {
				//head towards closest target of yours that is not collected
				this.direction = bestDir;
			} else {
				//else if no target locations known
				
				//use a random number to help decide what choice to make
				int randHelper = (int) Math.floor(Math.random() * 16);
				
				//now will either choose a random direction or go towards corner that has most unseen, given by gridChecked[][]
				
				//75% of the time (when no agent or target to avoid/collect), the agent will pick a random direction
				if (randHelper < 12) {
					if (stepsTaken%25 == 0 || this.runningIntoWall() != "n"){
						if (randHelper%4 == 0) {
							this.direction = "d";
						} else if (randHelper%4 == 1) {
							this.direction = "r";
						} else if (randHelper%4 == 2) {
							this.direction = "l";
						} else {
							this.direction = "u";
						}
					}
					
				} else { //the other 25% of the time they will move towards the corner given by emptyDir, calculated at the start of the function
					if (this.emptyDir == "NW") {
						//since the corner directions are diagonal, alternate the two direction to move there
						if (stepsTaken%2 == 0) {
							this.direction = "u";
						} else {
							this.direction = "l";
						}
					} else if (this.emptyDir == "NE") {
						if (stepsTaken%2 == 0) {
							this.direction = "u";
						} else {
							this.direction = "r";
						}
					} else if (this.emptyDir == "SW") {
						if (stepsTaken%2 == 0) {
							this.direction = "d";
						} else {
							this.direction = "l";
						}
					} else {
						if (stepsTaken%2 == 0) {
							this.direction = "d";
						} else {
							this.direction = "r";
						}
					}					
				}	
			}
		}
		
		// if any movement was made, return true, else false
		if (this.direction != "n") {
			return true;
		} else {
			return false;
		}		
	}

	//function to generate a list of coordinates in the agent's sight, for checking with the grid to see if any agents or targets are seen
	//	returns an  array list RadiusCoordinate objects
	public ArrayList<RadiusCoordinates> generateRadiusCoordinates() {
		//array list  to hold return values
		ArrayList<RadiusCoordinates> toReturn = new ArrayList<RadiusCoordinates>();

		//loop through the square around your agent (square of size radius*2 by radius*2, so 20x20 square)
		for (int currY = this.y - this.radius; currY <= this.radius + this.y; currY++) {
			//if the coordinate is off the map, skip it
			if (currY > 100 || currY < 0) {
				continue;
			}
			for (int currX = this.x - this.radius; currX <= this.radius + this.x; currX++) {
				//if the coordinate is off the map, skip it
				if (currX > 100 || currX < 0) {
					continue;
				}
				//also, if the coordinate is your location, skip it but also set it to seen in your memory grid
				if (currX == this.x && currY == this.y) {
					this.gridChecked[currX][currY] = 1;
					continue;
				}
				//check if the current coordinate is actually in your semi-circular radius, and if so, set it to seen and add this coord to the arraylist
				if ((int) Math.pow(currX - this.x, 2) + (int) Math.pow(currY - this.y, 2) <= (int) Math.pow(this.radius, 2)) {
					this.gridChecked[currX][currY] = 1;
					RadiusCoordinates tempObject = new RadiusCoordinates(currX, currY);
					toReturn.add(tempObject);
				}

			}
		}
		//return the list of coordinates
		return toReturn;
	}
	
	//update the memory of the agent, given the list of currently seen targets, and return the list 
	//	of collected targets, to be sent to the grid to update its memory
	public ArrayList<CollectedTarget> updateMemory (ArrayList<RadiusTarget> currentSeenTargets) {
		
		//list to return
		ArrayList<CollectedTarget> collected = new ArrayList<CollectedTarget>();
		
		//loop through targets seen, updating the current agent's memory
		for(int currTarget = 0; currTarget < currentSeenTargets.size(); currTarget++) {
			
			//if this is the first time this agent is seeing the target enter the if statement, otherwise dont want to waste time if seen it before
			if (this.memory[currentSeenTargets.get(currTarget).agentID].target[currentSeenTargets.get(currTarget).targetID].seen == false) 
			{
				//set the target to seen in memory
				this.memory[currentSeenTargets.get(currTarget).agentID].target[currentSeenTargets.get(currTarget).targetID].seen = true;
				
				//update the x and y for the target seen in memory for the target it belongs to
				this.memory[currentSeenTargets.get(currTarget).agentID].target[currentSeenTargets.get(currTarget).targetID].x = currentSeenTargets.get(currTarget).x;
				this.memory[currentSeenTargets.get(currTarget).agentID].target[currentSeenTargets.get(currTarget).targetID].y = currentSeenTargets.get(currTarget).y;
				
				//if the target is yours, also set the collected value to true in memory, and put it in return list, and update your collecetd count
				if(this.agentID==currentSeenTargets.get(currTarget).agentID) {
					this.memory[agentID].target[currentSeenTargets.get(currTarget).targetID].collected = true;
					this.memory[agentID].collectedCount++;
					CollectedTarget tempObject = new CollectedTarget(agentID, currentSeenTargets.get(currTarget).targetID);
					collected.add(tempObject);
				}
			}
		}
		
		return collected;
	}

	//generate a public message containing the number of targets you currently have
	public Message_Collected generatePublicMessage() {
		//ensure that the message is new information by checking if it has changed recently
		if (this.memory[agentID].collectedCount != this.previousCount) {
			//if it is new information, create the message and return it, updating the previous count in your own memory
			Message_Collected toSend = new Message_Collected(this.agentID, this.memory[agentID].collectedCount);
			this.previousCount = this.memory[agentID].collectedCount;
			return toSend;
		} else { //else return null, no message generated
			return null;
		}
	}
	
	//receive the public message, setting collected count for the sender to the updated value
	public void receivePublicMessage(Message_Collected message) {
		this.memory[message.sourceAgentID].collectedCount = message.collectedCount;
		//also, if the new count is the highest in the game, update the firstplace count
		if (message.collectedCount > this.firstPlaceCount) {
			this.firstPlaceCount = message.collectedCount;
		}
	}
	
	//generate a list of collaborative private messages, sending out all information about targets you have seen to specific agents who they belong to
	//	takes in the currently seen list of targets by an agent to generate the message
	public ArrayList<Message_TargetLoc> generateCollaborativePrivateMessages(ArrayList<RadiusTarget> currentSeenTargets) {
		ArrayList<Message_TargetLoc> toSend = new ArrayList<Message_TargetLoc>();
		//loop through the list of targets seen
		for(int currTarget = 0; currTarget < currentSeenTargets.size(); currTarget++) {
			//if the target belongs to someone else, create a message with them as recipient and add it to the list to send
			if (currentSeenTargets.get(currTarget).agentID != this.agentID) {
				Message_TargetLoc tempObject = new Message_TargetLoc(currentSeenTargets.get(currTarget).agentID,currentSeenTargets.get(currTarget).agentID,currentSeenTargets.get(currTarget).targetID, currentSeenTargets.get(currTarget).x, currentSeenTargets.get(currTarget).y);
				toSend.add(tempObject);
			}
		}
		return toSend;
	}
	
	//receive the private messages of either type, compassionate or collaborative, updating your memory based on received info
	public void receivePrivateMessage(Message_TargetLoc message) {
		this.memory[message.agentID].target[message.targetID].x = message.x;
		this.memory[message.agentID].target[message.targetID].y = message.y;
	}
	
	//generate a list of private messages to be sent only if the sender and would-be recipient are far behind the target count leader
	public ArrayList<Message_TargetLoc> generateCompassionatePrivateMessages() {
		//check before creating any messages if you are far behind the leader (3+ behind)
		//	our compassionate agents only collaborate when they are far behind, and need to work together to catch up
		if (this.memory[agentID].collectedCount + 2 < this.firstPlaceCount) {
			ArrayList<Message_TargetLoc> toSend = new ArrayList<Message_TargetLoc>();
			//loop through each of the 5 agents in memory
			for (int agnt = 0; agnt < 5; agnt++) {
				//if they are also far behind, send them information about their targets
				if (agnt != agentID && memory[agnt].collectedCount + 2 <= this.firstPlaceCount) {
					//loop through your memory of their targets, sending any info you can
					for (int targ = 0; targ < 5; targ++) {
						//ensure you have seen their target and have not previously sent them this info, in order to send only valid info
						// and to not spam them with messages
						if (this.memory[agnt].target[targ].sent == false && this.memory[agnt].target[targ].seen == true) {
							this.memory[agnt].target[targ].sent = true;
							Message_TargetLoc tempObject = new Message_TargetLoc(agnt, agnt, targ, this.memory[agnt].target[targ].x, this.memory[agnt].target[targ].y);
							toSend.add(tempObject);
						}
						
					}
					
				}
			}
			//return the list of messages if generated
			return toSend;
		} else {
			return null;
		}
	}
	
	//function to help with direction choice, which returns "n" if not running into a wall, else the direction they are heading while hitting the wall
	public String runningIntoWall(){
		if (this.direction == "l" && this.x == 0 || this.direction == "r" && this.x == 100 || this.direction == "u" && this.y == 100 || this.direction == "d" && this.y == 0){
			return this.direction;
		} else {
			return "n";
		}
	}
	
}
