package Scripts;

import java.util.ArrayList;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Player;

public class State 
{
	Position agentPosition;
	int range = 20;
	Area consideredArea;
	ArrayList<Entity> knownTrees;
	public State(Player agent, ArrayList<Entity> knownTrees)
	{
		agentPosition = agent.getPosition();
		consideredArea = agentPosition.getArea(range);
		this.knownTrees = knownTrees;
		
	}
	
	public ArrayList<Entity> getConsideredTrees()
	{
		ArrayList<Entity> consideredTrees = new ArrayList<Entity>();
		for(int i = 0; i < knownTrees.size(); i++)
		{
			if(consideredArea.contains(knownTrees.get(i)))
			{
				consideredTrees.add(knownTrees.get(i));
			}
		}
		
		return consideredTrees;
	}
	
	public Area getConsiderArea()
	{
		return consideredArea;
	}
	
	public Position getAgentPosition()
	{
		return agentPosition;
	}
}
