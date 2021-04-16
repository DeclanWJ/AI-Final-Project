package Scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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
	Position startingPosition;
	Entity closestTree;
	WebWalkEvent walkToNode;
	LinkedList<Position> pathPositions;
	boolean realDistance = true;
	LocalPathFinder pathFinder;
	boolean gather = false;
	
	long startTime;
	double gatherTime = 0;
	ArrayList<Double> gatherRunTimes = new ArrayList<Double>();

	ArrayList<String> treeNames = new ArrayList<String>();

	public void onStart() throws InterruptedException
	{
		super.onStart();
		treeNames.add("Willow");
		treeNames.add("Tree");
		//		treeNames.add("Strong Yew");

		pathFinder = new LocalPathFinder(getBot());

		agent = myPlayer();
		startingPosition = new Position(3222, 3219, 0);
		log("Starting position is: " + startingPosition);

		//		getWalking().webWalk(startingPosition);

		startTime = System.currentTimeMillis();
	}

	//Code will run when the script is closed
	public void onExit()throws InterruptedException
	{
		super.onExit();
	}

	//Code will run every loop/tick - Bulk of code will stem from here (method calls will primarily occur here)
	public int onLoop() throws InterruptedException 
	{
//				if(areaContainsAnimatingPlayer(agent.getArea(10)))
//				{
//					log("There is at least 1 player animated within a 10 tile radius of the agent");
//				}
//				else
//				{
//					log("No players found animated within 10 tile radius of the agent");
//				}

		//If inventory is not full
		if(!getInventory().isFull() && !agent.isAnimating())
		{
			closestTree = getObjects().closest(realDistance, tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree)); //Using the OSBot API Filter object to find the closest tree of a valid name-type, that can be accessed by the agent's character
			
			log("closest tree has animated player beside it: " + areaContainsAnimatingPlayer(closestTree.getArea(1)));
			
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
					log("Length of pathPositions list: " + (pathPositions.size() - 1));
				}

				if(gather)
					chopTree(closestTree);
				//				closestTree = null;
			}	
		}

		//If inventory is full
		if(getInventory().isFull())
		{
			bank(); 
		}

		return 1000; //Milliseconds until next loop/tick
	}

	public void onPaint(Graphics2D g)
	{
		g.setPaint(Color.WHITE);
		g.drawString("Debug Information: ", 5, 30);
		g.drawString("Player Grid Location (X,Y): (" + agent.getX() + "," + agent.getY() + ")", 5, 45);
		g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
		if(!gatherRunTimes.isEmpty())
			g.drawString("Gathering Run Time (in seconds):" + gatherRunTimes.toString(), 5, 105);
		if(closestTree != null) 
		{
			g.drawString("Target Tree: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")", 5, 60);
			g.drawString("Length of Path to tree: " + (pathPositions.size() - 1), 5, 75);
		}	
		else 
		{
			g.drawString("Target Tree not selected", 5, 60);
		}
		//		g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
		if(pathPositions != null)
		{
			for(int i = 0; i < pathPositions.size(); i++)
			{
				g.drawPolygon(pathPositions.get(i).getPolygon(getBot()));
			}
		}
	}

	public boolean areaContainsAnimatingPlayer(Area a) 
	{
		Player validPlayer = getPlayers().closest(player -> player != null && !player.equals(agent));

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
		if(pathPositions != null)
			getWalking().walkPath(pathPositions);
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
	}
}
