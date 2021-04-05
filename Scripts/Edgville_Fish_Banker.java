package Scripts;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.Model;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;



@ScriptManifest(name = "Fishing Bank (Edgvile)", version = 1.0, info = "", author = "Shane", logo = "")
public class Edgville_Fish_Banker extends Script 
{
	//Variable Instantiation
	//------------------------------------------------------[+]
	Player myPlayer;
	Position startingTile;
	Area startingArea;
	String[] keepWhenBanking;
	ArrayList<String> itemNames = new ArrayList<String>();
	//------------------------------------------------------[+]





	//Methods
	//---------------------------------------------------------------------------------[+]
	//Code executes when script is started
	public void onStart() 
	{
		myPlayer = myPlayer();
		startingTile = myPlayer.getPosition();
		startingArea = myPlayer.getArea(5);
		
		itemNames.add("Feather");
		itemNames.add("Fly fishing rod");
		fillKeptItemsList(itemNames);
	}

	//Code is run continuously when script is being run
	public int onLoop() throws InterruptedException 
	{	
		checkInventory();
		
		return 500;
	}

	//Code executes when script is closed
	public void onExit() 
	{

	}

	//Check if player's inventory is full or out of food
	public void checkInventory() throws InterruptedException
	{
		if(getInventory().isFull())
			bank();
	}

	//Empties inventory in bank, except for specified items
	public void bank() throws InterruptedException
	{
		//		LUMBRIDGE_UPPER
		//		EDGEVILLE
		if (!Banks.EDGEVILLE.contains(myPosition())) 
		{
			getWalking().webWalk(Banks.EDGEVILLE);
		}

		else if (!getBank().isOpen()) 
		{
			getBank().open();
			getBank().depositAllExcept(keepWhenBanking);
			getBank().close();
			getWalking().webWalk(startingArea);
		}
	}

	//Fills list of items to keep in inventory when banking
	public void fillKeptItemsList(ArrayList<String> itemNames)
	{
		keepWhenBanking = new String[itemNames.size()];
		
		for(int i = 0; i < itemNames.size(); i++)
		{
			keepWhenBanking[i] = itemNames.get(i);
		}
	}
	
	//Code handles updating graphical displays
	public void onPaint(final Graphics2D g) 
	{
		g.setPaint(Color.WHITE);
		g.drawString("Some text", 10, 10);
	}
	//---------------------------------------------------------------------------------[+]



}