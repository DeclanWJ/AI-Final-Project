package Scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Player;
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
	Position[] pathPositions = new Position[0];
	
	long startTime;
	double gatherTime = 0;
	ArrayList<Double> gatherRunTimes = new ArrayList<Double>();
	
	ArrayList<String> treeNames = new ArrayList<String>();
	
	public void onStart() throws InterruptedException
	{
		super.onStart();
//		treeNames.add("Willow");
		treeNames.add("Tree");
//		treeNames.add("Strong Yew");
		
		agent = myPlayer();
		startingPosition = new Position(3222, 3219, 0);
		log("Starting position is: " + startingPosition);
		
		getWalking().webWalk(startingPosition);
		
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
		//If inventory is not full
		if(!getInventory().isFull())
		{
			closestTree = getObjects().closest(tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree)); //Using the OSBot API Filter object to find the closest tree of a valid name-type, that can be accessed by the agent's character
			
			if(closestTree == null) //If a tree meeting the filter requirements above is not found 
			{
				log("No valid trees nearby...");
			}
			
			else //If a tree meeting the filter requirements above is found
			{
				log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");
				chopTree(closestTree);
//				closestTree = null;
			}	
		}
		
		//If inventory is full
		else
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
		if(closestTree != null) 
		{
			g.drawString("Target Tree: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")", 5, 60);
			g.drawString("Length of Path to tree: " + pathPositions.length, 5, 75);
		}	
		else 
		{
			g.drawString("Target Tree not selected", 5, 60);
		}
		g.drawString("Agent Inventory Spaces Remaining: " + getInventory().getEmptySlotCount(), 5, 90);
		if(!gatherRunTimes.isEmpty())
			g.drawString("Gathering Run Time (in seconds):" + gatherRunTimes.toString(), 5, 105);
	}
	
	//Makes agent move to a passed tree, and chop it down
	public void chopTree(Entity tree)
	{
		log("Chopping down closest tree...");
		
//		getWalking().webWalk(tree.getArea(1)); //Moves agent's character to a position adjacent to the passed tree
		walkToNode = new WebWalkEvent(tree.getArea(1));
		execute(walkToNode);
		pathPositions = walkToNode.getPositions();
		log("Length of Path: " + pathPositions.length);
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
			
			if(getInventory().isEmpty())
				getWalking().webWalk(startingPosition); //Move agent's character back to the starting tile
		}
	}
}
