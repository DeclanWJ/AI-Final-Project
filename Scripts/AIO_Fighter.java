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

import javax.swing.SwingUtilities;



@ScriptManifest(name = "AIO Fighty Thing", version = 1.0, info = "", author = "Shane", logo = "")
public class AIO_Fighter extends Script 
{
	//Variable Instantiation
	//------------------------------------------------------[+]
	Player myPlayer;
	Area startingArea;
	int healAtPercentage;
	int foodWithdrawAmount;
	ArrayList<String> targets = new ArrayList<String>();
	ArrayList<String> loot = new ArrayList<String>();
	ArrayList<String> foods = new ArrayList<String>();
	int activeTargetX;
	int activeTargetY;
	NPC activeTarget = null;
	GroundItem lootToGet;
	Model displayModel = null;
	int[] displayModelXs;
	int[] displayModelYs;
	boolean drawModel = false;



//	private GUI gui = new GUI();
//	private Tree tree;
	//------------------------------------------------------[+]





	//Methods
	//---------------------------------------------------------------------------------[+]
	//Code executes when script is started
	public void onStart() 
	{
		log("Started wawa");
//		try 
//		{
//			SwingUtilities.invokeAndWait(() -> 
//			{
//				gui = new GUI();
//				gui.open();
//			});
//		} 
//		catch (InterruptedException | InvocationTargetException e) 
//		{
//			e.printStackTrace();
//			stop();
//			return;
//		}
//
//
//
//		if (!gui.isStarted()) {
//			stop();
//			return;
//		}
		
//		tree = gui.getSelectedTree();

		myPlayer = myPlayer();
		startingArea = myPlayer.getArea(5);
		targets.add("Cow");
		targets.add("Goblin");
		targets.add("Barbarian");
		targets.add("Minotaur");
		loot.add("Bones");
		loot.add("Cowhide");
		loot.add("Coins");
		loot.add("Iron arrow");
		loot.add("Tin ore");
		loot.add("Rune essence");
		loot.add("Copper ore");
		foods.add("Trout");
		healAtPercentage = 50;
		foodWithdrawAmount = 5;

//		drawModel = false;
	}

	//Code is run continuously when script is being run
	public int onLoop() throws InterruptedException 
	{	
//		checkHealth();
		checkInventory();
		checkLoot();
		attackTargets();

		if(activeTarget != null)
		{
			activeTargetX = activeTarget.getX();
			activeTargetY = activeTarget.getY();	
		}

		//		log("Player Grid Location (X,Y): (" + myPlayer.getX() + "," + myPlayer.getY() + ")");
		//		log("Camera - Pitch (Vertical): " + getCamera().getPitchAngle() + ", Yaw (Horizontal): " + getCamera().getYawAngle());

		return 500;
	}

	//Code executes when script is closed
	public void onExit() 
	{
//		if (gui != null) {
//			gui.close();
//		}

	}

	//Check player's health, and heal if necessary
	public void checkHealth() throws InterruptedException
	{	
		if (myPlayer().getHealthPercent() < healAtPercentage) 
		{
			Item foodToEat = getInventory().getItem(item -> foods.contains(item.getName()));

			if(foodToEat != null)
			{
				foodToEat.interact("Eat");
				checkInventory();
			}
			sleep(random(2500, 3000));
		}
	}

	//Check if player's inventory is full or out of food
	public void checkInventory() throws InterruptedException
	{
		if(getInventory().isFull())
			bank();

//		if(getInventory().getItem(item -> foods.contains(item.getName())) == null)
//			bank();
	}

	//Check for desired loot at location of last active target, if it has been killed
	public void checkLoot() throws InterruptedException
	{

		if(activeTarget != null && activeTarget.getHealthPercent() <= 0)
		{
			sleep(random(1750, 2000));

			for(String itemName : loot)
			{
				lootToGet = getGroundItems().closest(loot -> (loot.getX() == activeTargetX && loot.getY() == activeTargetY && loot.getZ() == activeTarget.getZ() && loot.getName().equals(itemName) || loot.getName().contains("Uncut")));
				if(getGroundItems().get(activeTargetX, activeTargetY).contains(lootToGet))
				{
					lootToGet.interact("Take");
					attackTargets(); //Why does this have to be in the loop!?
				}
			}

			checkInventory();
		}
	}

	//Attempt to attack closest available NPC of selected 'targets'
	public void attackTargets() throws InterruptedException
	{
		checkInventory();

		if (activeTarget != null && activeTarget.getHealthPercent() == 0)
		{
			checkLoot();
		}

		if (!myPlayer().isAnimating() && myPlayer().getInteracting() == null && !myPlayer.isMoving()) 
		{

			for(String name : targets) //Checking to see if any local NPCs match the list of selected targets
			{
				activeTarget = (getNpcs().closest(npc -> targets.contains(npc.getName()) && npc.isAttackable() && !npc.isUnderAttack() && npc.getInteracting() == null && getMap().canReach(npc)));
			}

			if (activeTarget != null) //if a valid target has been selected
			{
				activeTargetX = activeTarget.getX();
				activeTargetY = activeTarget.getY();
				activeTarget.interact("Attack");

				new ConditionalSleep(5000) 
				{
					public boolean condition() throws InterruptedException 
					{
						return myPlayer().getInteracting() != null;
					}
				}.sleep();
			}

		}
	}

	//Stores the X and Y coordinates of the model
	public void setModel(Model model)
	{
		displayModel = model;

		displayModelXs = model.getVerticesX();
		displayModelYs = model.getVerticesY();
	}

	//Randomly move camera to simulate player camera input
	public void wiggleCamera()
	{
		getCamera().movePitch(10);
		getCamera().moveYaw(10);
	}

	//Empties inventory in bank, withdraws food, and returns to starting area
	public void bank() throws InterruptedException
	{
		activeTarget = null;

		//		LUMBRIDGE_UPPER
		//		EDGEVILLE
		if (!Banks.EDGEVILLE.contains(myPosition())) 
		{
			getWalking().webWalk(Banks.EDGEVILLE);
		}

		else if (!getBank().isOpen()) 
		{
			getBank().open();
			getBank().depositAllExcept(getInventory().getItem(item -> foods.contains(item.getName())) == null);

			for(String foodType : foods)
			{
				getBank().withdraw(foodType, foodWithdrawAmount);
			}

			getBank().close();
			getWalking().webWalk(startingArea);
		}
	}

	//Code handles updating graphical displays
	public void onPaint(final Graphics2D g) 
	{
		g.setPaint(Color.WHITE);
		g.drawString("Player Grid Location (X,Y): (" + myPlayer.getX() + "," + myPlayer.getY() + ")", 10, 10);

		if(drawModel)
		{
			if(activeTarget != null)
				setModel(activeTarget.getModel());

			if(displayModelXs != null && displayModelYs != null)
			{
				Polygon displayableModel = new Polygon(displayModelXs, displayModelYs, displayModelXs.length);
				displayableModel.translate(100, 300);
				g.drawPolygon(displayableModel);
			}
		}



		g.drawPolygon(activeTarget.getPosition().getPolygon(getBot()));
	}
	//---------------------------------------------------------------------------------[+]



}