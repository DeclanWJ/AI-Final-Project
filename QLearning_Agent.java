package Scripts;
import java.util.*;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public class QLearning_Agent 
{
	Map<Position, Map<String, Double>> mapOfQValues;
	//Position to reference as state, String of action label, Double of current Qvalue
	
	Map<String, Double> newPositionActionValues; 
	double discount;
	double learningRate;
	
	public QLearning_Agent()
	{
		mapOfQValues = new HashMap<Position, Map<String, Double>>();
		
		newPositionActionValues = new HashMap<String, Double>();
		newPositionActionValues.put("North", 0.0);
		newPositionActionValues.put("East", 0.0);
		newPositionActionValues.put("South", 0.0);
		newPositionActionValues.put("West", 0.0);
		newPositionActionValues.put("Chop Wood", 0.0);
//		newPositionActionValues.put("Bank", 0.0);
		
		discount = 0.9;
		learningRate = 1.0;
		
		fillMapForAreaBounds();
	}
	
	public QLearning_Agent(HashMap<Position, Map<String, Double>> map)
	{
		mapOfQValues = map;
		
		newPositionActionValues = new HashMap<String, Double>();
		newPositionActionValues.put("North", 0.0);
		newPositionActionValues.put("East", 0.0);
		newPositionActionValues.put("South", 0.0);
		newPositionActionValues.put("West", 0.0);
		newPositionActionValues.put("Chop Wood", 0.0);

		discount = 0.9;
		learningRate = 1.0;
		
		fillMapForAreaBounds();
	}
	
	//TODO: Add in a method to handle adding an un-evaluated state to the map (i.e. first time visiting a tile, give it all 0s, or if "Chop Wood" is an option do so")
	boolean checkIfKnown(State s)
	{
		Position statePosition = s.getAgentPosition();
		
		if(mapOfQValues.get(statePosition) == null) //If position does not have state representation yet
		{	
			Map<String, Double> temp = new HashMap<String, Double>(newPositionActionValues);
			
			mapOfQValues.put(statePosition, temp); //
			return false; //If state was not known return false
		}
		
		return true; //If state was known return true
	}
	
	void fillMapForAreaBounds() //Goes through entirety of restricted area for learning, and gives 0s for QValues in states
	{
		Position boundsNW = new Position(3136, 3240, 0);
		Position boundsSE = new Position(3266, 3200, 0);
		Area areaBounds = new Area(boundsNW.getX(), boundsNW.getY(), boundsSE.getX(), boundsSE.getY());
		for(Position pos : areaBounds.getPositions())
		{
			Map<String, Double> temp = new HashMap<String, Double>(newPositionActionValues);
			
			mapOfQValues.put(pos, temp);
		}
	}
	
	double getQValue(State state, String actionLabel)
	{
		return mapOfQValues.get(state.getAgentPosition()).get(actionLabel);
	}
	
//	void incrementQValue(State state, String actionLabel, Double value)
//	{
//		double currentQVal = mapOfQValues.get(state.getAgentPosition()).get(actionLabel);
//		mapOfQValues.get(state.getAgentPosition()).replace(actionLabel, currentQVal + value);	
//	}
//	
//	void setQValue(State state, String actionLabel, Double value)
//	{
//		double currentQVal = mapOfQValues.get(state.getAgentPosition()).get(actionLabel);
//		mapOfQValues.get(state.getAgentPosition()).replace(actionLabel, currentQVal + value);	
//	}

	double computeValueFromQValues(State state)
	{
		String bestAction = computeActionFromQValues(state);
		
		if(getQValue(state, "Chop Wood") != 0.0)
		{
			bestAction = "Chop Wood";
		}
		
		if(bestAction == null)
			return 0.0;
		
		return getQValue(state, bestAction);
	}
	
	double computeStateValueFromQValues(State state)
	{
		String bestAction = computeActionFromQValues(state);
		double max = Double.NEGATIVE_INFINITY;
		
		for(String actionLabel : mapOfQValues.get(state.getAgentPosition()).keySet())
		{
			double qVal = getQValue(state, actionLabel);
			if(qVal > max)
			{
				max = qVal;
				bestAction = actionLabel;
			}
		}
		
		return max;
	}
	
	String computeActionFromQValues(State state) //TODO: Find a way to pass legal actions of the state here to choose randomly if all 0s
	{
		String bestAction = null;
		double max = Double.NEGATIVE_INFINITY;
		boolean qValsAllSame = true;
		double firstQVal = getQValue(state, "North");
		
		for(String actionLabel : state.getLegalActions())
		{
			double qVal = getQValue(state, actionLabel);
			if(qVal > max)
			{
				max = qVal;
				bestAction = actionLabel;
			}
			if(qVal != firstQVal)
			{
				qValsAllSame = false;
			}
		}
		
		if(qValsAllSame)
		{
			Random r = new Random();
			int randomActionIndex = r.nextInt(state.getLegalActions().size()); //Random int 0 to 3
			bestAction = state.getLegalActions().get(randomActionIndex);	
		}
		
		return bestAction;
	}
	
	String getAction(State state, Double epsilon, ArrayList<String> legalActions)
	{
		String action = null;
		Random r = new Random();
		int randomInt = r.nextInt(100) + 1; //Generate a value between 1 and 100	
		
		if(randomInt <= (epsilon * 100)) //Return a random action if we fall inside of the epsilon range
		{
			int randomActionIndex = r.nextInt(legalActions.size());
			action =  legalActions.get(randomActionIndex);
		}
		else
		{
			action = computeActionFromQValues(state);
		}
		
		return action;
	}
	
	void update(State state, String actionLabel, State nextState, double reward)
	{
		double currentQVal = getQValue(state, actionLabel);
		double updateVal = learningRate * (reward + (discount * (computeValueFromQValues(nextState))) - currentQVal);	
		mapOfQValues.get(state.getAgentPosition()).replace(actionLabel, currentQVal + updateVal);
	}
}
