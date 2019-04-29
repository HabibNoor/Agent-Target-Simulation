import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;

//main class that combines the Agent and Grid objects to run the simulations and generatve CSVs
public class Main {
	
	//main interface which provides a CLI for interacting with our simulations and generating CSV files
	public static void main(String[] args) throws IOException {
		
		Scanner input = new Scanner(System.in);
		String tempString;
		int tempInt;
		boolean keepRunning = true;
		while (keepRunning == true) {
			System.out.println("Enter what you want to do: ");
			System.out.println("competitive: to run the competitive simulation");
			System.out.println("collaborative: to run the collaborative simulation");
			System.out.println("compassionate: to run the compassionate simulation");
			System.out.println("csvs: to generate the CSV files again");
			System.out.println("exit: to exit the interface");
			while(!input.hasNextLine()) {
				input.next();
			}
			tempString = input.nextLine();
			switch(tempString) {
			case "collaborative":
				System.out.println("Enter the delay in milliseconds for the visualizer to slow down the animation.");
				while (!input.hasNextInt()) {
					input.next();
				}
				tempInt = input.nextInt();
				Main.runCollaborative(tempInt);
				break;
			case "competitive":
				System.out.println("Enter the delay in milliseconds for the visualizer to slow down the animation.");
				while (!input.hasNextInt()) {
					input.next();
				}
				tempInt = input.nextInt();
				Main.runCompetitive(tempInt);
				break;
			case "compassionate":
				System.out.println("Enter the delay in milliseconds for the visualizer to slow down the animation.");
				while (!input.hasNextInt()) {
					input.next();
				}
				tempInt = input.nextInt();
				Main.runCompassionate(tempInt);
				break;
			case "csvs":
				System.out.println("Enter the number of iterations to be generated for each scenario in the CSV files.");
				while (!input.hasNextInt()) {
					input.next();
				}
				tempInt = input.nextInt();
				Main.generateCSVP1(tempInt);
				Main.generateCSVP2();
				break;
			case "exit":
				keepRunning = false;
				break;
			}
		}
		return;
		
	}

	//runs the compassionate simulation, in which agents will send other agents private messages about their target  locations if both sender
	//  and receiver are far behind the target leader, as well as public messages to others how many they have collected
	public static void runCompassionate(int sleepTimeMill) {
		
		//create a grid object for storing global data
		Grid grid = new Grid("Compassionate");

		//create the visualizer and move it to front of screen
		Visualizer vis = new Visualizer(grid);
		vis.setExtendedState(JFrame.ICONIFIED);
		vis.setExtendedState(JFrame.NORMAL);

		//create an array of agent objects, with locations from the grid's location stores to make sure they dont start on top of eachother
		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));

		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		
		//arraylists for pass coordinates in the radius, passing list of targets seen, 
		//	list of agents seen, targets collected, messages to be sent, and another singular public message
		//	these lists are used for transferring data from the grid to the agents and vice versa, as well as agent to agent communication
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();
		ArrayList<Message_TargetLoc> messagesToSend = new ArrayList<Message_TargetLoc>();
		Message_Collected publicMessageToSend;

		// continue until any agent done (compassionate)
		while (!grid.anyAgentDone()) {
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				// get the current list of coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory of where targets are located for any agents
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// generate potential public message telling agents if they have collected any targets
				publicMessageToSend = agents[currentAgent].generatePublicMessage();

				// distribute public message if generated to all agents (except themself)
				if (publicMessageToSend != null) {
					for (int t = 0; t < 5; t++) {
						if (t != currentAgent) {
							agents[t].receivePublicMessage(publicMessageToSend);
						}
					}
				}

				// generate compassionate private messages from the current agent to other agents
				//		our compassionate messages are only sent if both the sender and the would-be receiver 
				//		are far behind the target count leader
				messagesToSend = agents[currentAgent].generateCompassionatePrivateMessages();

				// loop through all private messages created, sending to their recipient
				if (messagesToSend != null && messagesToSend.size() != 0) {
					for (int p = 0; p < messagesToSend.size(); p++) {
						agents[messagesToSend.get(p).recipient].receivePrivateMessage(messagesToSend.get(p));
					}
				}

				// update the agent's direction (based on if another agent is too close, if there is a target they know of, 
				//		or else sometimes random direction or heuristic direction based on which corner (from where you are)
				//		you have least explored
				agents[currentAgent].updateDirection(currentSeenAgents);
				
				// move the agent in the direction generated if possible
				agents[currentAgent].moveAgent(grid);
				

				// update global grid stores of collected targets, to know when to  end the game
				grid.updateCollected(collectedThisRound);
			}

			//sleep to make visualization watchable
			try {
				Thread.sleep(sleepTimeMill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//repaint the visualization
			vis.repaint();

		}

	}

	
	//runs the collaborative scenario, in which agents always send eachother private messages about targets they see
	public static void runCollaborative(int sleepTimeMill) {
		Grid grid = new Grid("Collaborative");

		//set up visualizer
		Visualizer vis = new Visualizer(grid);
		vis.setExtendedState(JFrame.ICONIFIED);
		vis.setExtendedState(JFrame.NORMAL);

		//set up agents
		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));

		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		
		//arraylists for pass coordinates in the radius, passing list of targets seen, 
		//	list of agents seen, targets collected, messages to be sent
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();
		ArrayList<Message_TargetLoc> messagesToSend = new ArrayList<Message_TargetLoc>();

		// continue until all agents done (collaboration)
		while (!grid.allAgentsDone()) {
			
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				// get the current coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// generate collaborative private messages from the current agent
				messagesToSend = agents[currentAgent].generateCollaborativePrivateMessages(currentSeenTargets);

				// distribute collaborative private messages to recipients
				for (int p = 0; p < messagesToSend.size(); p++) {
					agents[messagesToSend.get(p).recipient].receivePrivateMessage(messagesToSend.get(p));
				}

				// update the agent's direction and move them in that direction
				agents[currentAgent].updateDirection(currentSeenAgents);
				agents[currentAgent].moveAgent(grid);
				
				// update grid stores of collected targets
				grid.updateCollected(collectedThisRound);
			}

			//delay to make it watchable
			try {
				Thread.sleep(sleepTimeMill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//repaint visualiztion
			vis.repaint();

		}

	}

	//
	public static void runCompetitive(int sleepTimeMill) {
		Grid grid = new Grid("Competitive");

		//set up visualizer
		Visualizer vis = new Visualizer(grid);
		vis.setExtendedState(JFrame.ICONIFIED);
		vis.setExtendedState(JFrame.NORMAL);

		//set up agents
		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));

		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		
		//array lists for radius coordinates, target list seen, agent list seen, and targets collected this round
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();


		// continue until any agent done (competition)
		while (!grid.anyAgentDone()) {
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				// get the current coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// update the agent's direction and move the agent
				agents[currentAgent].updateDirection(currentSeenAgents);
				agents[currentAgent].moveAgent(grid);

				// update grid stores of collected targets
				grid.updateCollected(collectedThisRound);
			}

			//delay for watchability
			try {
				Thread.sleep(sleepTimeMill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//delay
			vis.repaint();
		}
	}
	
	// a very similar function to runCollaborative, except it returns the CSV String lines for CSV1 for the collaborative scenario
	//	parameter for the # of iterations to run
	public static StringBuilder[] runCollaborativeCSV(int iterationNum) {
		Grid grid = new Grid("Collaborative");
		StringBuilder strings[] = new StringBuilder[5];
		StringBuilder string0 = new StringBuilder();
		StringBuilder string1 = new StringBuilder();
		StringBuilder string2 = new StringBuilder();
		StringBuilder string3 = new StringBuilder();
		StringBuilder string4 = new StringBuilder();

		strings[0] = string0;
		strings[1] = string1;
		strings[2] = string2;
		strings[3] = string3;
		strings[4] = string4;

		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));

		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();
		ArrayList<Message_TargetLoc> messagesToSend = new ArrayList<Message_TargetLoc>();

		// continue until all agents done (collaboration)
		while (!grid.allAgentsDone()) {
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				// get the current coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// generate collaborative private messages from the current agent
				messagesToSend = agents[currentAgent].generateCollaborativePrivateMessages(currentSeenTargets);

				// distribute collaborative private messages
				for (int p = 0; p < messagesToSend.size(); p++) {
					if (messagesToSend.get(p).recipient == -1) {
						for (int e = 0; e < 5; e++) {
							agents[e].receivePrivateMessage(messagesToSend.get(p));
						}
					} else {
						agents[messagesToSend.get(p).recipient].receivePrivateMessage(messagesToSend.get(p));
					}
				}

				// update the agent's direction
				agents[currentAgent].updateDirection(currentSeenAgents);
				agents[currentAgent].moveAgent(grid);
				
				// update grid stores of collected targets
				grid.updateCollected(collectedThisRound);
			}

		}
		
		
		//after running 1 iteration, calculate the max and min happiness for the agents, as well as the avg and st dev
		double maxHappiness = -1;
		double minHappiness = 1000;
		double sum = 0;
		double current;
		double average;
		double summation = 0;
		double std;
		double comp;

		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			if (current > maxHappiness) {
				maxHappiness = current;
			}
			if (current < minHappiness) {
				minHappiness = current;
			}
			sum += current;
		}
		
		average = sum / 5;
		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			summation += Math.pow(current - average, 2);
		}
		
		std = Math.sqrt(summation / (5 - 1));

		
		//append these values to the correct string, 1 for each agent
		for (int i = 0; i < 5; i++) {
			strings[i].append('2');
			strings[i].append(',');
			strings[i].append(iterationNum);
			strings[i].append(',');
			strings[i].append(i);
			strings[i].append(',');
			strings[i].append(agents[i].memory[i].collectedCount);
			strings[i].append(',');
			strings[i].append(grid.agent[i].steps);
			strings[i].append(',');
			strings[i].append(grid.getHappiness(i));
			strings[i].append(',');
			strings[i].append(maxHappiness);
			strings[i].append(',');
			strings[i].append(minHappiness);
			strings[i].append(',');
			strings[i].append(average);
			strings[i].append(',');
			strings[i].append(std);
			strings[i].append(',');
			
			if (agents[i].memory[i].collectedCount == 0) {
				comp = 0;
			} else {
				comp = (grid.getHappiness(i) - minHappiness) / (maxHappiness - minHappiness);
			}
			
			strings[i].append(comp);
			strings[i].append('\n');
			System.out.println(strings[i].toString());
		}

		//returns an array of  5 strings, 1 for the info about each agent for the individual iteration
		return strings;

	}

	// a very similar function to runCompetitive, except it returns the CSV String lines for CSV1 for the collaborative scenario
	//	parameter for the # of iterations to run
	public static StringBuilder[] runCompetitiveCSV(int iterationNum) {
		Grid grid = new Grid("Competitive");
		StringBuilder strings[] = new StringBuilder[5];
		StringBuilder string0 = new StringBuilder();
		StringBuilder string1 = new StringBuilder();
		StringBuilder string2 = new StringBuilder();
		StringBuilder string3 = new StringBuilder();
		StringBuilder string4 = new StringBuilder();

		strings[0] = string0;
		strings[1] = string1;
		strings[2] = string2;
		strings[3] = string3;
		strings[4] = string4;

		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));
		
		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();


		// continue until any agents done (comp)
		while (!grid.anyAgentDone()) {
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				/**
				 * System.out.println("Agent Num:"+agents[currentAgent].agentID+"
				 * X:"+agents[currentAgent].x+" Y:"+agents[currentAgent].y);
				 **/

				// get the current coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// update the agent's direction
				agents[currentAgent].updateDirection(currentSeenAgents);
				agents[currentAgent].moveAgent(grid);

				
				// update grid stores of collected targets
				grid.updateCollected(collectedThisRound);
			}

		}

		double maxHappiness = -1;
		double minHappiness = 1000;
		double sum = 0;
		double current;
		double average;
		double summation = 0;
		double std;
		double comp;

		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			if (current > maxHappiness) {
				maxHappiness = current;
			}
			if (current < minHappiness) {
				minHappiness = current;
			}
			sum += current;
		}
		
		average = sum / 5;
		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			summation += Math.pow(current - average, 2);
		}
		
		std = Math.sqrt(summation / (5 - 1));
		
		
		for (int i = 0; i < 5; i++) {
			strings[i].append('1');
			strings[i].append(',');
			strings[i].append(iterationNum);
			strings[i].append(',');
			strings[i].append(i);
			strings[i].append(',');
			strings[i].append(agents[i].memory[i].collectedCount);
			strings[i].append(',');
			strings[i].append(grid.agent[i].steps);
			strings[i].append(',');
			strings[i].append(grid.getHappiness(i));
			strings[i].append(',');
			strings[i].append(maxHappiness);
			strings[i].append(',');
			strings[i].append(minHappiness);
			strings[i].append(',');
			strings[i].append(average);
			strings[i].append(',');
			strings[i].append(std);
			strings[i].append(',');
			
			if (agents[i].memory[i].collectedCount == 0) {
				comp = 0;
			} else {
				comp = (grid.getHappiness(i) - minHappiness) / (maxHappiness - minHappiness);
			}
			
			strings[i].append(comp);
			strings[i].append('\n');
			System.out.println(strings[i].toString());
		}

		//return array of 5 strings for the CSV_1
		return strings;

	}

	// a very similar function to runCompassionate, except it returns the CSV String lines for CSV1 for the collaborative scenario
	//	parameter for the # of iterations to run
	public static StringBuilder[] runCompassionateCSV(int iterationNum) {
		Grid grid = new Grid("Compassionate");

		StringBuilder strings[] = new StringBuilder[5];
		StringBuilder string0 = new StringBuilder();
		StringBuilder string1 = new StringBuilder();
		StringBuilder string2 = new StringBuilder();
		StringBuilder string3 = new StringBuilder();
		StringBuilder string4 = new StringBuilder();

		strings[0] = string0;
		strings[1] = string1;
		strings[2] = string2;
		strings[3] = string3;
		strings[4] = string4;

		Agent agents[] = new Agent[5];

		Agent a0 = new Agent(0, grid.getAgentX(0), grid.getAgentY(0));
		Agent a1 = new Agent(1, grid.getAgentX(1), grid.getAgentY(1));
		Agent a2 = new Agent(2, grid.getAgentX(2), grid.getAgentY(2));
		Agent a3 = new Agent(3, grid.getAgentX(3), grid.getAgentY(3));
		Agent a4 = new Agent(4, grid.getAgentX(4), grid.getAgentY(4));
		
		agents[0] = a0;
		agents[1] = a1;
		agents[2] = a2;
		agents[3] = a3;
		agents[4] = a4;
		
		ArrayList<RadiusCoordinates> agentRadiusCoordinates = new ArrayList<RadiusCoordinates>();
		ArrayList<RadiusTarget> currentSeenTargets = new ArrayList<RadiusTarget>();
		ArrayList<RadiusCoordinates> currentSeenAgents = new ArrayList<RadiusCoordinates>();
		ArrayList<CollectedTarget> collectedThisRound = new ArrayList<CollectedTarget>();
		ArrayList<Message_TargetLoc> messagesToSend = new ArrayList<Message_TargetLoc>();
		Message_Collected publicMessageToSend;

		// continue until any agents done (compassionate)
		while (!grid.anyAgentDone()) {
			// loop through each agent in sequence
			for (int currentAgent = 0; currentAgent < 5; currentAgent++) {

				// get the current coordinates within the agent's radius
				agentRadiusCoordinates = agents[currentAgent].generateRadiusCoordinates();

				// using these radius coordinates, check for agents and targets nearby, storing
				// to a temporary array list
				currentSeenTargets = grid.checkForTargets(agentRadiusCoordinates);
				currentSeenAgents = grid.checkForAgents(agentRadiusCoordinates);

				// loop through targets seen, updating the current agent's memory
				collectedThisRound = agents[currentAgent].updateMemory(currentSeenTargets);

				// generate potential public message
				publicMessageToSend = agents[currentAgent].generatePublicMessage();

				// distribute public message if generated
				if (publicMessageToSend != null) {
					for (int t = 0; t < 5; t++) {
						if (t != currentAgent) {
							agents[t].receivePublicMessage(publicMessageToSend);
						}
					}
				}

				// generate compassionate private messages from the current agent
				messagesToSend = agents[currentAgent].generateCompassionatePrivateMessages();

				// distribute compassionate private messages if any created
				if (messagesToSend != null && messagesToSend.size() != 0) {
					for (int p = 0; p < messagesToSend.size(); p++) {
						agents[messagesToSend.get(p).recipient].receivePrivateMessage(messagesToSend.get(p));
					}
				}

				// update the agent's direction
				agents[currentAgent].updateDirection(currentSeenAgents);
				agents[currentAgent].moveAgent(grid);
				
				// update grid stores of collected targets
				grid.updateCollected(collectedThisRound);
			}

		}
		
		
		double maxHappiness = -1;
		double minHappiness = 1000;
		double sum = 0;
		double current;
		double average;
		double summation = 0;
		double std;
		double comp;

		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			if (current > maxHappiness) {
				maxHappiness = current;
			}
			if (current < minHappiness) {
				minHappiness = current;
			}
			sum += current;
		}
		
		average = sum / 5;
		for (int agnt = 0; agnt < 5; agnt++) {
			current = grid.getHappiness(agnt);
			summation += Math.pow(current - average, 2);
		}
		
		std = Math.sqrt(summation / (5 - 1));
		
		
		
		for (int i = 0; i < 5; i++) {
			strings[i].append('3');
			strings[i].append(',');
			strings[i].append(iterationNum);
			strings[i].append(',');
			strings[i].append(i);
			strings[i].append(',');
			strings[i].append(agents[i].memory[i].collectedCount);
			strings[i].append(',');
			strings[i].append(grid.agent[i].steps);
			strings[i].append(',');
			strings[i].append(grid.getHappiness(i));
			strings[i].append(',');
			strings[i].append(maxHappiness);
			strings[i].append(',');
			strings[i].append(minHappiness);
			strings[i].append(',');
			strings[i].append(average);
			strings[i].append(',');
			strings[i].append(std);
			strings[i].append(',');
			
			if (agents[i].memory[i].collectedCount == 0) {
				comp = 0;
			} else {
				comp = (grid.getHappiness(i) - minHappiness) / (maxHappiness - minHappiness);
			}
			
			strings[i].append(comp);
			strings[i].append('\n');
			System.out.println(strings[i].toString());
		}

		//return array of 5 strings, 1 for each agent
		return strings;

	}

	//runs each of the 3 scenario CSV generators for the given # of iterations, and then combines the strings into 1 CSV file
	public static void generateCSVP1(int numOfIterations) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File("G8_1.csv"));
		StringBuilder strings[];

		//loop through the scenarios, running each the number of iterations given and writing to the CSV1
		for (int i = 1; i <= numOfIterations; i++) {
			strings = Main.runCompetitiveCSV(i);
			for (int j = 0; j < 5; j++) {
				pw.write(strings[j].toString());
			}
		}
		for (int i = 1; i <= numOfIterations; i++) {
			strings = Main.runCollaborativeCSV(i);;
			for (int j = 0; j < 5; j++) {
				pw.write(strings[j].toString());
			}
		}
		for (int i = 1; i <= numOfIterations; i++) {
			strings = Main.runCompassionateCSV(i);
			for (int j = 0; j < 5; j++) {
				pw.write(strings[j].toString());
			}
		}
		pw.close();

	}

	//reads in the CSV_1 file, and aggregates the data into 3 lines representing the avg happiness and competitiveness for each scenario
	public static void generateCSVP2() throws IOException {
		BufferedReader input = new BufferedReader(new FileReader("G8_1.csv"));
		PrintWriter output = new PrintWriter(new File("G8_2.csv"));
		String line;
		String[] cutLine;
		int scenario;
		StringBuilder strings[] = new StringBuilder[3];
		StringBuilder string0 = new StringBuilder();
		StringBuilder string1 = new StringBuilder();
		StringBuilder string2 = new StringBuilder();
		strings[0] = string0;
		strings[1] = string1;
		strings[2] = string2;

		double sumsI[] = { 0, 0, 0 };
		double sumsK[] = { 0, 0, 0 };
		double averagesI[] = new double[3];
		double averagesK[] = new double[3];
		double counters[] = { 0, 0, 0 };

		//sums of competitiveness and happiness
		while ((line = input.readLine()) != null) {
			cutLine = line.split(",");
			scenario = Integer.parseInt(cutLine[0]);
			if (scenario == 1) {
				sumsI[0] += Double.parseDouble(cutLine[8]);
				sumsK[0] += Double.parseDouble(cutLine[10]);
				counters[0]++;
			} else if (scenario == 2) {
				sumsI[1] += Double.parseDouble(cutLine[8]);
				sumsK[1] += Double.parseDouble(cutLine[10]);
				counters[1]++;
			} else {
				sumsI[2] += Double.parseDouble(cutLine[8]);
				sumsK[2] += Double.parseDouble(cutLine[10]);
				counters[2]++;
			}
		}
		
		//generates averages
		averagesI[0] = sumsI[0] / counters[0];
		averagesK[0] = sumsK[0] / counters[0];

		averagesI[1] = sumsI[1] / counters[1];
		averagesK[1] = sumsK[1] / counters[1];

		averagesI[2] = sumsI[2] / counters[2];
		averagesK[2] = sumsK[2] / counters[2];

		//creates the strings to be written
		for (int i = 0; i < 3; i++) {
			strings[i].append(i+1);
			strings[i].append(',');
			strings[i].append(averagesI[i]);
			strings[i].append(',');
			strings[i].append(averagesK[i]);
			strings[i].append('\n');
		}
		
		//writes to the new CSV file
		for (int i = 0; i < 3; i++) {
			output.write(strings[i].toString());
		}
		output.close();
		input.close();
	}

}
