package Scripts;

import java.awt.Graphics;
import java.util.ArrayList;

import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Shane Phillips, Declan, Wren", info = "", logo = "", name = "Artificial Intelligence Project", version = 0.1)
public class Artificial_Intelligence_Project extends Script
{
	ArrayList<String> treeNames = new ArrayList<String>();
	public void onStart() throws InterruptedException
	{
		super.onStart();
		treeNames.add("Willow");
		treeNames.add("Tree");
		treeNames.add("Strong Yew");
		
	}
	
	public void onExit()throws InterruptedException
	{
		super.onExit();
	}
	
	//Code will run every loop/tick - Bulk of code will likely go here
	public int onLoop() throws InterruptedException 
	{
		if(!getInventory().isFull())
		{
			Entity closestTree = getObjects().closest(tree -> treeNames.contains(tree.getName()) && getMap().canReach(tree));
			if(closestTree == null)
			{
				log("No valid trees nearby...");
			}
			else
			{
				log("Nearest valid tree is a: " + closestTree.getName() + ", located at grid-space: (" + closestTree.getGridX() + ", " + closestTree.getGridY() + ")");
			}
		}
		return 1000; //Milliseconds until next loop
	}

	public void onPaint(Graphics g)
	{
		
	}
}
