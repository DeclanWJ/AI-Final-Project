package Scripts;

import java.util.ArrayList;
import java.util.List;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.script.Script;

public class State
{
	Position agentPosition;
	int range = 20;
	Area agentBounds;
	Area consideredArea;
	Area initialArea;
	boolean inventoryFull;
	ArrayList<String> legalActions;
	
	public State(Position agentPosition, boolean inventoryFull)
	{
		this.agentPosition = agentPosition;
		this.inventoryFull = inventoryFull;
		createConsideredArea();
		
	}
	
	//Method handles creation of the Area object (used by OSBot) that allows for selection of the area the agent will be restricted to while learning
	public void createConsideredArea()
	{
		Position boundsNW = new Position(3136, 3240, 0);
		Position boundsSE = new Position(3266, 3200, 0);
		agentBounds = new Area(boundsNW.getX(), boundsNW.getY(), boundsSE.getX(), boundsSE.getY());
		
		initialArea = agentPosition.getArea(range);
		List<Position> initallyConsideredPositions = initialArea.getPositions();
		
		Position consideredNW = initallyConsideredPositions.get(initallyConsideredPositions.size()-41); //NorthWest Corner
		if(consideredNW.getX() < boundsNW.getX())
		{
			consideredNW = new Position(3136, consideredNW.getY(), consideredNW.getZ());
		}
		if(consideredNW.getY() > boundsNW.getY())
		{
			consideredNW = new Position(consideredNW.getX(), 3240, consideredNW.getZ());
		}
		
		Position consideredSE = initallyConsideredPositions.get(40);                                     //SouthEast Corner
		if(consideredSE.getX() > boundsSE.getX())
		{
			consideredSE = new Position(3266, consideredSE.getY(), consideredSE.getZ());
		}
		if(consideredSE.getY() < boundsSE.getY())
		{
			consideredSE = new Position(consideredSE.getX(), 3200, consideredSE.getZ());
		}
		
		consideredArea = new Area(consideredNW, consideredSE);
	}
	
	//Method returns consideredArea value
	public Area getConsiderArea()
	{
		return consideredArea;
	}
	
	//Method returns position of the agent in the state
	public Position getAgentPosition()
	{
		return agentPosition;
	}
	
	//Method returns the list of legalActions for the current state
	public ArrayList<String> getLegalActions()
	{
		return legalActions;
	}
	
	//Method sets the list of legal action for the current state
	public void setLegalActions(ArrayList<String> legalActions)
	{
		this.legalActions = legalActions;
	}
}
