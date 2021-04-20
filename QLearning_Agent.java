package Scripts;
import java.util.*;

import org.osbot.rs07.api.map.Position;

public class QLearning_Agent 
{
	Map<Position, Map<String, Double>> mapOfQValues = new HashMap<Position, Map<String, Double>>();
	
	//TODO: Add in a method to handle adding an un-evaluated state to the map (i.e. first time visiting a tile, give it all 0s, or if "Chop Wood" is an option do so")
	void checkIfKnown(State s)
	{
		
	}
	
	double getQValue(State s, Action a)
	{
		return 0;
		
	}
	
	double computeValueFromQValues(State s)
	{
		return 0;
		
	}
	
	Action computeActionFromQValues(State s)
	{
		return null;
		
	}
	
	Action getAction(State s)
	{
		return null;
		
	}
	
	void update(State s, Action a, State nextState, double reward)
	{
		
	}
}
