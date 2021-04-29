package Scripts;

import java.awt.Color;
import java.awt.Graphics2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//OSBot API specific imports
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.util.LocalPathFinder;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Shane Phillips, Declan, Wren", info = "", logo = "", name = "Artificial Intelligence Project", version = 0.1)
public class Artificial_Intelligence_Project extends Script
{
	Player agent;
	Player validPlayer;
	Position startingPosition;
	Entity closestTree;
	WebWalkEvent walkToNode;
	LinkedList<Position> pathPositions;
	boolean realDistance = true;
	LocalPathFinder pathFinder;
	boolean gather = false;
	boolean pause = false;
	boolean logging = false;
	boolean debugInfo = true;
	boolean firstPathFound = false;
	boolean setupBanking;
	String lastAction = "";

	Area agentBounds;
	Area consideredArea;
	Area initialArea;
	Position agentPosition;

	long startTime;
	double gatherTime = 0;
	ArrayList<Double> gatherRunTimes = new ArrayList<Double>();
	ArrayList<String> treeNames = new ArrayList<String>();
	State currentState;
	State startState;
	Map<Position, Double> visitedPositions = new HashMap<Position, Double>();
	Map<Position, Map<String, Double>> mapOfQValues;
	Map<Position, Map<String, Double>> tempMapOfQValues = new HashMap<Position, Map<String, Double>>();

	double explorationRate;

	QLearning_Agent qAgent;
	File trainingData = new File("C:\\Users\\shane\\OSBot\\Data\\trainingData.txt"); //File path with vary depending on OSBot software location
	FileWriter fileWriter;
	PrintWriter dataRecorder;
	Scanner scan;

	//Method called on start of the script in the OSBot client
	public void onStart() throws InterruptedException
	{
		super.onStart();
		treeNames.add("Tree");
		treeNames.add("Dead tree");

		pathFinder = new LocalPathFinder(getBot());

		agent = myPlayer();
		startingPosition = new Position(3160, 3232, 0); 
		if(logging)
			log("Starting position is: " + startingPosition);

		while(!getInventory().isEmpty())
		{
			setupBanking = true;
			bank();
			setupBanking = false;
		}
		if(agent.getPosition() != startingPosition)
			getWalking().webWalk(startingPosition);

		explorationRate = 0.50;

		if(trainingData.exists()) //If the file is found
		{
			if(logging)
				log("File \"trainingData.txt\" found...");
			try {
				scan = new Scanner(trainingData); //Open a scanner with the file
				if(logging)
					log("Scanner is created in onStart() method.");
			} catch (FileNotFoundException e) 
			{
				if(logging)
					log("File not found when creating scanner in onStart() method.");
			}

			if(scan.hasNext()) //If file is found and not empty
			{
				try {
					if(logging)
						log("Scanner found that file was not empty, calling createMapFromRecord() method");
					createMapFromRecord(); //Read the information stored in the file
					if(logging)
						log("createMapFromRecord() method called successfully");
				} catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}

				qAgent = new QLearning_Agent(tempMapOfQValues); //Create a QLearning_Agent that makes use of the data found in the file
				if(logging)
					log("qAgent created with values from found file");
				for(Position position : tempMapOfQValues.keySet()) //Store all positions (of states) found in the file for displaying if debugging is enabled
				{
					visitedPositions.put(position, qAgent.getHighestQValueAtPosition(position));
				}
			}
			else //If file was found but was empty
			{
				qAgent = new QLearning_Agent();
				if(logging)
					log("qAgent created without values from found file");
			}
		}
		else //If the file was not found
		{
			if(logging)
				log("File \"trainingData.txt\" not found. Creating file now...");
			trainingData = new File("C:\\Users\\shane\\OSBot\\Data\\trainingData.txt");
		}

		if(qAgent == null)
		{
			log("Default qAgent created");
			qAgent = new QLearning_Agent();
		}
		
		startTime = System.currentTimeMillis();
	}

	//Code will run when the script is closed
	public void onExit()throws InterruptedException
	{
		try {
			fileWriter = new FileWriter(trainingData);
			dataRecorder = new PrintWriter(fileWriter);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		dataRecorder.println(explorationRate); //First line of file will store current exploration rate
		
		for(Double time : gatherRunTimes) //Second line stores the list of all recorded gathering run times
		{
			dataRecorder.print(time + " ");
		}
		dataRecorder.println();
		mapOfQValues = qAgent.getStateData();
		
		//Each additional line will store information from the hash map for every visited (learned about) state
		//Use each visitedPosition for a key in the map for action data, and for the positions XYZs
		for(Position position : visitedPositions.keySet())
		{
			dataRecorder.print(position.getX() + " ");
			dataRecorder.print(position.getY() + " ");
			dataRecorder.print(position.getZ() + " ");

			for(Double qVal : mapOfQValues.get(position).values())
			{
				dataRecorder.print(qVal + " ");
			}

			dataRecorder.println();
		}

		dataRecorder.close();
		if(logging)
			log("State data for visited positions recorded in file: \"trainingData.txt\"");
		super.onExit();
	}	

	//Code will run every loop/tick - Bulk of code will stem from here (method calls will primarily occur here)
	public int onLoop() throws InterruptedException 
	{	
		if(!agent.isAnimating()) //If the player is not doing anything, or has finished their last action
		{
			if(getInventory().isFull()) //If inventory is full
			{
				bank();
				startTime = System.currentTimeMillis(); //Use this to track the start of the next run after returning to the start position
			}

			currentState = new State(agent.getPosition(), getInventory().isFull());
			currentState.setLegalActions(getLegalActions(agent.getPosition()));

			if(logging)
			{
				log("Current state Qval information: " + qAgent.mapOfQValues.get(agent.getPosition()).toString());
				log("Legal actions in state: " + getLegalActions(agent.getPosition()).toString());
			}

			String bestAction = qAgent.getAction(currentState, explorationRate, getLegalActions(agent.getPosition()));
			if(logging)
				log("Best action found was: " + bestAction);
			qAgent.update(currentState, bestAction, getNextState(currentState, bestAction), getReward(currentState, bestAction));
			takeAction(bestAction);
			lastAction = bestAction;

			if(agent.getHealthPercent() <= 50) //Health check to avoid death from enemies in the world while learning
			{
				pause = true;
				if(logging)
					log("Agent has been paused, returning to start position.");
				if(agent.getPosition() != startingPosition)
					getWalking().webWalk(startingPosition);
			}

			if(pause == true && agent.getHealthPercent() == 100)
			{
				pause = false;
				if(logging)
					log("Agent has been un-paused.");
			}

			if(!pause) //If not paused due to low health
			{
				if(!getInventory().isFull() && !agent.isAnimating()) //If inventory is not full
				{
					closestTree = getObjects().closest(realDistance, tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree) && currentState.getConsiderArea().getPositions().contains(tree.getPosition())); //Using the OSBot API Filter object to find the closest tree of a valid name-type, that can be accessed by the agent's character

					if(closestTree == null) //If a tree meeting the filter requirements above is not found 
					{
						if(logging)
							log("No valid trees nearby...");
					}

					else //If a tree meeting the filter requirements above is found
					{
						if(logging)
							log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");
						pathPositions = pathFinder.findPath(agent.getPosition(), closestTree);
						if(pathPositions != null)
						{
							if(!firstPathFound)
								firstPathFound = true;
							if(logging)
								log("Length of pathPositions list: " + (pathPositions.size() - 1));
						}
						
						if(gather) //As long as gathering is enabled
							chopTree(closestTree);
					}	
				}
			}

		}
		return 500; //Milliseconds until next loop/tick
	}

	//Method handles drawing of anything outside of content displayed by the base game
	public void onPaint(Graphics2D g)
	{
		if(debugInfo)
		{
			//Drawing bulk of debug information for actively updating variables
			g.setPaint(Color.WHITE);
			g.drawString("Debug Information: ", 5, 30);
			g.drawString("Player Grid Location (X, Y, Z): (" + agent.getX() + ", " + agent.getY() + ", " + agent.getZ() + ")", 5, 45);
			g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
			g.drawString("Gathering enabled: " + gather, 5, 120);
			g.drawString("Pause enabled: " + pause, 5, 135);
			g.drawString("Current state exists: " + (currentState != null), 5, 150);
			g.drawString("Current run time since last start: " + ((double)(System.currentTimeMillis() - (double)startTime) / 1000), 5, 165);
			g.drawString("Current exploration rate percentage: " + explorationRate, 5, 180);

			//Drawing debug information for gathering run times both from the start of the script and loaded from data
			if(!gatherRunTimes.isEmpty())
				g.drawString("Gathering Run Time (in seconds):" + gatherRunTimes.toString(), 5, 105);
			else
				g.drawString("Gathering Run Time (in seconds):", 5, 105);

			//Drawing the tiles that have either been visited this run, or in the stored data loaded on start with the appropriate color for state values
			if(currentState != null) //TODO: Only print each positions we've seen once
			{
				double stateValue = qAgent.computeValueFromQValues(currentState);
				if(visitedPositions.containsKey(currentState.getAgentPosition())) //If position is known
				{
					visitedPositions.replace(currentState.getAgentPosition(), stateValue);
				}
				else //If position is not known
				{
					visitedPositions.put(currentState.getAgentPosition(), stateValue);
				}

				for(Position pos : visitedPositions.keySet())
				{
					if(visitedPositions.get(pos) >= 20.0)
						g.setPaint(Color.CYAN);
					else if(visitedPositions.get(pos) > 15.0)
						g.setPaint(Color.BLUE);
					else if(visitedPositions.get(pos) > 10.0)
						g.setPaint(Color.GREEN);
					else if(visitedPositions.get(pos) > 5.0)
						g.setPaint(Color.YELLOW);
					else if(visitedPositions.get(pos) > 0.0)
						g.setPaint(Color.ORANGE);
					else
						g.setPaint(Color.RED);
					
					g.drawPolygon(pos.getPolygon(getBot()));
				}

			}

			//Drawing debug information for the closest tree
			g.setPaint(Color.WHITE);
			if(closestTree != null) 
			{

				g.drawString("Target Tree: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")", 5, 60);
				g.drawString("Length of Path to tree: " + (pathPositions.size() - 1), 5, 75);
			}	
			else 
			{
				g.drawString("Target Tree not selected", 5, 60);
			}

			// Drawing for the path to the closest tree 
			//		if(pathPositions != null)
			//		{
			//			//			g.setPaint(Color.WHITE);
			//
			//			for(int i = 0; i < pathPositions.size(); i++)
			//			{
			//				g.drawPolygon(pathPositions.get(i).getPolygon(getBot()));
			//			}
			//		}
		}
	}

	//Method originally written for detecting nodes that other players were gathering in order to avoid them, we were unable to get this working
	//and thus it does not get used in this code
	public boolean areaContainsAnimatingPlayer(Area a) 
	{
		validPlayer = getPlayers().closest(player -> !player.equals(null) && !player.equals(agent));

		if(a.contains(validPlayer) == true) //If there is a player in the area
		{
			if(logging)
				log("Player found within checked area");
			if(validPlayer.isAnimating()) //If the player is animated
				return true;
			else                          //If the player is not animated
				return false;
		}
		else                                //If no player was found in the area
			return false;

	}

	//Makes agent move to a passed tree, and chop it down
	public void chopTree(Entity tree)
	{
		if(logging)
			log("Chopping down closest tree...");
		tree.interact("Chop down"); //Agent will select the "Chop down" action on the passed tree	
	}

	//Empties inventory in bank, except for specified items
	public void bank() throws InterruptedException
	{
		if(logging)
			log("Emptying inventory in Lumbridge castle bank...");

		while(!Banks.LUMBRIDGE_UPPER.contains(myPosition())) //If the agent's character is not in Lumbridge castle bank
		{
			getWalking().webWalk(Banks.LUMBRIDGE_UPPER); //Move agent's character into the Lumbridge castle bank
		}

		if(!getBank().isOpen()) //If the bank interface is not open
		{
			getBank().open(); //Open the bank interface
			getBank().depositAll(); //Deposit all items in the agent's character's inventory
			getBank().close(); //Close the bank interface
			if(!setupBanking)
				gatherRunTimes.add(((double)(System.currentTimeMillis() - (double)startTime) / 1000));
			if(getInventory().isEmpty())
				getWalking().webWalk(startingPosition); //Move agent's character back to the starting tile
		}
		
		if(explorationRate >= 0.075)
			explorationRate -= 0.025;
	}

	public void adjustCamera()
	{
		getCamera().moveYaw(agent.getRotationForCamera());
	}

	ArrayList<String> getLegalActions(Position agentPosition)
	{
		ArrayList<String> legalActions = new ArrayList<String>();

		List<Position> temp = currentState.getConsiderArea().getPositions();

		Position currentAgentPosition = agentPosition;
		Position north = new Position(currentAgentPosition.getX(), currentAgentPosition.getY()+1, currentAgentPosition.getZ());
		Position east = new Position(currentAgentPosition.getX()+1, currentAgentPosition.getY(), currentAgentPosition.getZ());
		Position south = new Position(currentAgentPosition.getX(), currentAgentPosition.getY()-1, currentAgentPosition.getZ());
		Position west = new Position(currentAgentPosition.getX()-1, currentAgentPosition.getY(), currentAgentPosition.getZ());

		if(getMap().canReach(north) && getMap().realDistance(currentAgentPosition, north) <= 2 && temp.contains(north))
			legalActions.add("North");
		if(getMap().canReach(east) && getMap().realDistance(currentAgentPosition, east) <= 2 && temp.contains(east))
			legalActions.add("East");
		if(getMap().canReach(south) && getMap().realDistance(currentAgentPosition, south) <= 2 && temp.contains(south))
			legalActions.add("South");
		if(getMap().canReach(west) && getMap().realDistance(currentAgentPosition, west) <= 2 && temp.contains(west))
			legalActions.add("West");

		if(!getInventory().isFull() && pathPositions == null && firstPathFound && lastAction != "Chop Wood")
			legalActions.add("Chop Wood");

		return legalActions;
	}

	void takeAction(String action)
	{
		if(logging)
			log("Take action has been called.");
		Position agentPosition = currentState.getAgentPosition();

		if(action.equals("North"))
		{
			if(logging)
				log("Action to be taken: North");

			Position north = new Position(agentPosition.getX(), agentPosition.getY()+1, agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(north);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("East"))
		{
			if(logging)
				log("Action to be taken: East");

			Position east = new Position(agentPosition.getX()+1, agentPosition.getY(), agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(east);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("South"))
		{
			if(logging)
				log("Action to be taken: South");

			Position south = new Position(agentPosition.getX(), agentPosition.getY()-1, agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(south);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("West"))
		{
			if(logging)
				log("Action to be taken: West");

			Position west = new Position(agentPosition.getX()-1, agentPosition.getY(), agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(west);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("Chop Wood"))
		{
			if(logging)
				log("Action to be taken: Chop Wood");

			if(closestTree != null)
				chopTree(closestTree);
		}
	}

	State getNextState(State currentState, String actionLabel)
	{
		boolean inventoryFull = currentState.inventoryFull;
		Position nextPosition = currentState.getAgentPosition();

		if(actionLabel.equals("North"))
			nextPosition = new Position(nextPosition.getX(), nextPosition.getY()+1, nextPosition.getZ());
		if(actionLabel.equals("East"))
			nextPosition = new Position(nextPosition.getX()+1, nextPosition.getY(), nextPosition.getZ());
		if(actionLabel.equals("South"))
			nextPosition = new Position(nextPosition.getX(), nextPosition.getY()-1, nextPosition.getZ());
		if(actionLabel.equals("West"))
			nextPosition = new Position(nextPosition.getX()-1, nextPosition.getY(), nextPosition.getZ());
		else //If action is to chop a tree
			if(getInventory().getEmptySlotCount() - 1 == 0) //Deprecated method, but still functions
				inventoryFull = true;

		State nextState = new State(nextPosition, inventoryFull);
		nextState.setLegalActions(getLegalActions(nextPosition));

		return nextState;
	}

	double getReward(State currentState, String actionLabel)
	{
		double reward = -1.0;

		if(actionLabel.equals("Chop Wood"))
			reward += 6.0;

		return reward;
	}

	void createMapFromRecord() throws FileNotFoundException
	{
		if(scan == null) //if scan not already set (should be by the time this method gets called)
		{
			scan = new Scanner(trainingData);
		}

		explorationRate = scan.nextDouble(); //Reading exploration rate from file
		scan.nextLine();
		
		String listOfTimes = scan.nextLine();
		Scanner timeScan = new Scanner(listOfTimes);
		
		while(timeScan.hasNext())
			gatherRunTimes.add(timeScan.nextDouble());
		
		while(scan.hasNextLine())
		{
			int x = 0, y = 0, z = 0;

			if(scan.hasNext())
			{
				x = scan.nextInt();
				y = scan.nextInt();
				z = scan.nextInt();
			}

			Position tempKey = new Position(x, y, z);
			Map<String, Double> tempQValues = new HashMap<String, Double>();
			if(scan.hasNext())
			{
				tempQValues.put("North", scan.nextDouble());
				tempQValues.put("East", scan.nextDouble());
				tempQValues.put("South", scan.nextDouble());
				tempQValues.put("West", scan.nextDouble());
				tempQValues.put("Chop Wood", scan.nextDouble());
			}

			tempMapOfQValues.put(tempKey, tempQValues);
			scan.nextLine();
		}
	}

}

