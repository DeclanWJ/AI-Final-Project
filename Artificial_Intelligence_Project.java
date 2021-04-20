package Scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

	Area agentBounds;
	Area consideredArea;
	Area initialArea;
	Position agentPosition;

	long startTime;
	double gatherTime = 0;
	ArrayList<Double> gatherRunTimes = new ArrayList<Double>();
	ArrayList<Entity> knownTrees = new ArrayList<Entity>();
	ArrayList<String> treeNames = new ArrayList<String>();
	State currentState;
	State startState;
	public void onStart() throws InterruptedException
	{
		super.onStart();
		//		treeNames.add("Willow");
		treeNames.add("Tree");
		//		treeNames.add("Strong Yew");

		pathFinder = new LocalPathFinder(getBot());

		agent = myPlayer();
		startingPosition = new Position(3222, 3219, 0); 
		log("Starting position is: " + startingPosition);

		if(!getInventory().isEmpty())
			bank();

		getWalking().webWalk(startingPosition);

		startTime = System.currentTimeMillis();
		startState = new State(agent, knownTrees, getInventory().isFull(), getInventory().isEmpty());
	}

	//Code will run when the script is closed
	public void onExit()throws InterruptedException
	{
		super.onExit();
	}

	//Code will run every loop/tick - Bulk of code will stem from here (method calls will primarily occur here)
	public int onLoop() throws InterruptedException 
	{	
		currentState = new State(agent, knownTrees, getInventory().isFull(), getInventory().isEmpty());
		log("Legal actions in state: " + getLegalActions(currentState).toString());
		if(getLegalActions(currentState).contains("North"))
		{
			log("North is a possible action. ");
			takeAction("North");	
		}
		else if(getLegalActions(currentState).contains("East"))
		{
			log("East is a possible action. ");
			takeAction("East");	
		}
		else if(getLegalActions(currentState).contains("South"))
		{
			log("South is a possible action. ");
			takeAction("South");	
		}
		else if(getLegalActions(currentState).contains("West"))
		{
			log("West is a possible action. ");
			takeAction("West");	
		}
		
		adjustCamera();

		if(agent.getHealthPercent() <= 50) //Health check
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
					log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");

					pathPositions = pathFinder.findPath(agent.getPosition(), closestTree);
					if(pathPositions != null)
					{
						if(!firstPathFound)
							firstPathFound = true;
						log("Length of pathPositions list: " + (pathPositions.size() - 1));
					}

					if(gather)
						chopTree(closestTree);
					//closestTree = null;
				}	
			}

			//If inventory is full
			if(getInventory().isFull())
			{
				bank(); 
			}
		}

		return 1000; //Milliseconds until next loop/tick
	}

	public void onPaint(Graphics2D g)
	{
		g.setPaint(Color.WHITE);
		g.drawString("Debug Information: ", 5, 30);
		g.drawString("Player Grid Location (X,Y,Z): (" + agent.getX() + "," + agent.getY() + "," + agent.getZ() + ")", 5, 45);
		g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
		g.drawString("Gathering enabled: " + gather, 5, 120);
		g.drawString("Pause enabled: " + pause, 5, 135);
		g.drawString("Current state exists: " + (currentState != null), 5, 150);

		if(!gatherRunTimes.isEmpty())
			g.drawString("Gathering Run Time (in seconds):" + gatherRunTimes.toString(), 5, 105);
		else
			g.drawString("Gathering Run Time (in seconds):", 5, 105);

		if(currentState != null)
		{
			List<Position> temp = currentState.getConsiderArea().getPositions();

			g.setPaint(Color.RED);

			for(int i = 0; i < temp.size(); i++)
			{
				g.drawPolygon(temp.get(i).getPolygon(getBot()));
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

		if(pathPositions != null)
		{
			//			g.setPaint(Color.WHITE);

			for(int i = 0; i < pathPositions.size(); i++)
			{
				g.drawPolygon(pathPositions.get(i).getPolygon(getBot()));
			}
		}

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
		adjustCamera();
		if(pathPositions != null)
			getWalking().walkPath(pathPositions);
		adjustCamera();
		tree.interact("Chop down"); //Agent will select the "Chop down" action on the passed tree	
	}

	//Empties inventory in bank, except for specified items
	public void bank() throws InterruptedException
	{
		log("Emptying inventory in Lumbridge castle bank...");

		if (!Banks.LUMBRIDGE_UPPER.contains(myPosition())) //If the agent's character is not in Lumbridge castle bank
		{
			getWalking().webWalk(Banks.LUMBRIDGE_UPPER); //Move agent's character into the Lumbridge castle bank
		}

		else if (!getBank().isOpen()) //If the bank interface is not open
		{
			getBank().open(); //Open the bank interface
			getBank().depositAll(); //Deposit all items in the agent's character's inventory
			getBank().close(); //Close the bank interface
			gatherRunTimes.add(((double)(System.currentTimeMillis() - (double)startTime) / 1000));
			startTime = System.currentTimeMillis();
			if(getInventory().isEmpty())
				getWalking().webWalk(startingPosition); //Move agent's character back to the starting tile
		}

		adjustCamera();
	}

	public void adjustCamera()
	{
		getCamera().moveYaw(agent.getRotationForCamera());
	}

	ArrayList<String> getLegalActions(State s)
	{
		ArrayList<String> legalActions = new ArrayList<String>();
		List<Position> temp = currentState.getConsiderArea().getPositions();

		Position agentPosition = s.getAgentPosition();
		Position north = new Position(agentPosition.getX(), agentPosition.getY()+1, agentPosition.getZ());
		Position east = new Position(agentPosition.getX()+1, agentPosition.getY(), agentPosition.getZ());
		Position south = new Position(agentPosition.getX(), agentPosition.getY()-1, agentPosition.getZ());
		Position west = new Position(agentPosition.getX()-1, agentPosition.getY(), agentPosition.getZ());

		if(getMap().canReach(north) && getMap().realDistance(agentPosition, north) <= 2 && temp.contains(north))
			legalActions.add("North");
		if(getMap().canReach(east) && getMap().realDistance(agentPosition, east) <= 2 && temp.contains(east))
			legalActions.add("East");
		if(getMap().canReach(south) && getMap().realDistance(agentPosition, south) <= 2 && temp.contains(south))
			legalActions.add("South");
		if(getMap().canReach(west) && getMap().realDistance(agentPosition, west) <= 2 && temp.contains(west))
			legalActions.add("West");

		//		if(closestTree != null && pathPositions != null)
		//			log("Distance to closest valid tree is: " + (pathPositions.size()-1));
		//		if(pathPositions == null)
		//			log("Adjacent to a tree");

		if(!s.inventoryFull && pathPositions == null && firstPathFound) //TODO check for being beside a tree
			legalActions.add("Chop Wood");
		if(!s.inventoryEmpty)
			legalActions.add("Bank");
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
			
		}
		if(action.equals("Bank"))
		{
			
		}
	}
}

