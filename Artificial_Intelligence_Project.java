package Scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	double explorationRate; //Want an initially high value, and taper down as more information is gotten

	QLearning_Agent qAgent;
	public void onStart() throws InterruptedException
	{
		super.onStart();
		//treeNames.add("Willow");
		treeNames.add("Tree");
		treeNames.add("Dead tree");
		//treeNames.add("Strong Yew");

		pathFinder = new LocalPathFinder(getBot());

		agent = myPlayer();
		startingPosition = new Position(3160, 3232, 0); 
		log("Starting position is: " + startingPosition);

		while(!getInventory().isEmpty())
		{
			setupBanking = true;
			bank();
			setupBanking = false;
		}
		if(agent.getPosition() != startingPosition)
			getWalking().webWalk(startingPosition);

		qAgent = new QLearning_Agent();
		explorationRate = 0.35;

		startTime = System.currentTimeMillis();
		//		startState = new State(agent.getPosition(), getInventory().isFull(), getLegalActions(agent.getPosition()));
	}

	//Code will run when the script is closed
	public void onExit()throws InterruptedException
	{
		super.onExit();
	}

	//Code will run every loop/tick - Bulk of code will stem from here (method calls will primarily occur here)
	public int onLoop() throws InterruptedException 
	{	
		if(!agent.isAnimating()) //If the player is not doing anything, or has finished their last action
		{
			//If inventory is full
			if(getInventory().isFull())
			{
				bank();
				startTime = System.currentTimeMillis(); //Use this to start the next run after returning to the start position
			}
			
			currentState = new State(agent.getPosition(), getInventory().isFull());
			currentState.setLegalActions(getLegalActions(agent.getPosition()));
			//			log("Current state was known: " + qAgent.checkIfKnown(currentState));

			log("Current state Qval information: " + qAgent.mapOfQValues.get(agent.getPosition()).toString());
			//		log("Incrementing value of North action...");	
			//		qAgent.incrementQValue(currentState, "North", 1.0);

			log("Legal actions in state: " + getLegalActions(agent.getPosition()).toString());
			
			String bestAction = qAgent.getAction(currentState, explorationRate, getLegalActions(agent.getPosition()));
			log("Best action found was: " + bestAction);
			qAgent.update(currentState, bestAction, getNextState(currentState, bestAction), getReward(currentState, bestAction));
			takeAction(bestAction);
			lastAction = bestAction;
			
//			adjustCamera();
			while(agent.getHealthPercent() <= 50) //Health check
			{
				pause = true;
				log("Agent has been paused, returning to start position.");
				getWalking().webWalk(startingPosition);
			}

			if(pause == true && agent.getHealthPercent() == 100)
			{
				pause = false;
				log("Agent has been un-paused.");
			}
			
			if(!pause) //Pause condtion
			{
				//If inventory is not full
				if(!getInventory().isFull() && !agent.isAnimating())
				{
					//			 && !areaContainsAnimatingPlayer(tree.getArea(2)) additional parameter for checking for other players near chosen tree
					closestTree = getObjects().closest(realDistance, tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree) && currentState.getConsiderArea().getPositions().contains(tree.getPosition())); //Using the OSBot API Filter object to find the closest tree of a valid name-type, that can be accessed by the agent's character

					if(closestTree == null) //If a tree meeting the filter requirements above is not found 
					{
						log("No valid trees nearby...");
					}

					else //If a tree meeting the filter requirements above is found
					{
//						log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");

						pathPositions = pathFinder.findPath(agent.getPosition(), closestTree);
						if(pathPositions != null)
						{
							if(!firstPathFound)
								firstPathFound = true;
//							log("Length of pathPositions list: " + (pathPositions.size() - 1));
						}
//
						if(gather)
							chopTree(closestTree);
					}	
				}
			}

		}
		return 500; //Milliseconds until next loop/tick
	}

	public void onPaint(Graphics2D g)
	{
		g.setPaint(Color.WHITE);
		g.drawString("Debug Information: ", 5, 30);
		g.drawString("Player Grid Location (X, Y, Z): (" + agent.getX() + ", " + agent.getY() + ", " + agent.getZ() + ")", 5, 45);
		g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
		g.drawString("Gathering enabled: " + gather, 5, 120);
		g.drawString("Pause enabled: " + pause, 5, 135);
		g.drawString("Current state exists: " + (currentState != null), 5, 150);
		g.drawString("Current run time since last start: " + ((double)(System.currentTimeMillis() - (double)startTime) / 1000), 5, 165);
		
		if(!gatherRunTimes.isEmpty())
			g.drawString("Gathering Run Time (in seconds):" + gatherRunTimes.toString(), 5, 105);
		else
			g.drawString("Gathering Run Time (in seconds):", 5, 105);

		//		if(startingPosition != null)
		//		{
		//			g.setPaint(Color.BLUE);
		//			g.draw(startingPosition.getPolygon(getBot()));
		//			g.setPaint(Color.WHITE);
		//		}
		//		if(currentState != null)
		//		{
		//			List<Position> temp = currentState.getConsiderArea().getPositions();
		//
		//			g.setPaint(Color.WHITE);
		//
		//			for(int i = 0; i < temp.size(); i++)
		//			{
		//				g.drawPolygon(temp.get(i).getPolygon(getBot()));
		//			}
		//		}

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
				if(visitedPositions.get(pos) >= 4.9)
					g.setPaint(Color.GREEN);
				else if(visitedPositions.get(pos) > 0.0)
					g.setPaint(Color.YELLOW);
				else
					g.setPaint(Color.RED);

				g.drawPolygon(pos.getPolygon(getBot()));
			}

		}

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

	public boolean areaContainsAnimatingPlayer(Area a) 
	{
		//TODO: Make the check for the list of players around that aren't the agent, not just the closest
		validPlayer = getPlayers().closest(player -> !player.equals(null) && !player.equals(agent));

		if(a.contains(validPlayer) == true) //If there is a player in the area
		{
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
		log("Chopping down closest tree...");

		//		getWalking().webWalk(tree.getArea(1)); //Moves agent's character to a position adjacent to the passed tree

		//		walkToNode = new WebWalkEvent(tree.getArea(1));
		//		execute(walkToNode);
//		adjustCamera();
		tree.interact("Chop down"); //Agent will select the "Chop down" action on the passed tree	
	}

	//Empties inventory in bank, except for specified items
	public void bank() throws InterruptedException
	{
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
//			startTime = System.currentTimeMillis(); //Use if want to start run time after banking, but before going back to start position
			if(getInventory().isEmpty())
				getWalking().webWalk(startingPosition); //Move agent's character back to the starting tile
		}

//		adjustCamera();
	}

	public void adjustCamera()
	{
		getCamera().moveYaw(agent.getRotationForCamera());
	}

	ArrayList<String> getLegalActions(Position agentPosition) //TODO: Change this to be able to be sent to State constructor
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
		log("Take action has been called.");
		Position agentPosition = currentState.getAgentPosition();

		if(action.equals("North"))
		{
			log("Action to be taken: North");

			Position north = new Position(agentPosition.getX(), agentPosition.getY()+1, agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(north);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("East"))
		{
			log("Action to be taken: East");

			Position east = new Position(agentPosition.getX()+1, agentPosition.getY(), agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(east);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("South"))
		{
			log("Action to be taken: South");

			Position south = new Position(agentPosition.getX(), agentPosition.getY()-1, agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(south);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("West"))
		{
			log("Action to be taken: West");

			Position west = new Position(agentPosition.getX()-1, agentPosition.getY(), agentPosition.getZ());
			WalkingEvent desiredMovement = new WalkingEvent(west);
			desiredMovement.setMinDistanceThreshold(0);

			execute(desiredMovement);
		}
		if(action.equals("Chop Wood"))
		{
			log("Action to be taken: Chop Wood");

			if(closestTree != null)
				chopTree(closestTree);
		}
		if(action.equals("Bank")) //TODO: Should be the only option when your inventory is full, ignoring moves taken to actually get back to bank for now
		{

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
			if(getInventory().getEmptySlotCount() - 1 == 0)
				inventoryFull = true;

		State nextState = new State(nextPosition, inventoryFull);
		nextState.setLegalActions(getLegalActions(nextPosition));

		return nextState;
	}

	double getReward(State currentState, String actionLabel)
	{
		double reward = -0.05;

		if(actionLabel.equals("Chop Wood"))
			reward += 5.0;

		return reward;
	}

}

