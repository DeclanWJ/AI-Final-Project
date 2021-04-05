package Scripts;

import java.awt.Graphics;
import java.util.ArrayList;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Shane Phillips, Declan, Wren", info = "", logo = "", name = "Woodcutting Baseline", version = 0.1)
public class Woodcutting_Baseline extends Script
{
	Player agent;
	Position startingPosition;
	ArrayList<String> treeNames = new ArrayList<String>();
	
	public void onStart() throws InterruptedException
	{
		super.onStart();
		treeNames.add("Tree");

		startingPosition = new Position(3222, 3219, 0);
		log("Starting position is: " + startingPosition);
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
			Entity closestTree = getObjects().closest(tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree)); //Using the OSBot API Filter object to find the closest tree of a valid name-type, that can be accessed by the agent's character
			
			if(closestTree == null) //If a tree meeting the filter requirements above is not found 
			{
				log("No valid trees nearby...");
			}
			
			else //If a tree meeting the filter requirements above is found
			{
				log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");
				chopTree(closestTree);
			}	
		}
		
		//If inventory is full
		else
		{
			bank(); 
		}
		
		return 1000; //Milliseconds until next loop/tick
	}

	public void onPaint(Graphics g)
	{
		
	}
	
	//Makes agent move to a passed tree, and chop it down
	public void chopTree(Entity tree)
	{
		log("Chopping down closest tree...");
		
		getWalking().webWalk(tree.getArea(1)); //Moves agent's character to a position adjacent to the passed tree
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
			if(getInventory().isEmpty())
				getWalking().webWalk(startingPosition); //Move agent's character back to the starting tile
		}
	}
}
