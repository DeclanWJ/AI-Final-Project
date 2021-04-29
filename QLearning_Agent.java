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
	
	//Constructor if no prior information is found
	public QLearning_Agent()
	{
		mapOfQValues = new HashMap<Position, Map<String, Double>>();
		
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
	
	//Constructor for if previous learning information is found
	public QLearning_Agent(Map<Position, Map<String, Double>> map)
	{
		mapOfQValues = new HashMap<Position, Map<String, Double>>();
		
		newPositionActionValues = new HashMap<String, Double>();
		newPositionActionValues.put("North", 0.0);
		newPositionActionValues.put("East", 0.0);
		newPositionActionValues.put("South", 0.0);
		newPositionActionValues.put("West", 0.0);
		newPositionActionValues.put("Chop Wood", 0.0);

		discount = 0.9;
		learningRate = 1.0;
		
		fillMapForAreaBounds();
		
		for(Position position : map.keySet())
		{
			if(map.get(position) != null)
			{
				mapOfQValues.replace(position, map.get(position)); 	
			}
		}
	}
	
	//Method checks if a state is already present in the hashmap, if it is not it is added with "blank" values (all 0.0s) for every action
	boolean checkIfKnown(State state)
	{
		Position statePosition = state.getAgentPosition();
		
		if(mapOfQValues.get(statePosition) == null) //If position does not have state representation yet
		{	
			Map<String, Double> temp = new HashMap<String, Double>(newPositionActionValues);
			
			mapOfQValues.put(statePosition, temp); //
			return false; //If state was not known return false
		}
		
		return true; //If state was known return true
	}
	
	//Method is used to ensure every position (and the respective state) within the area the agent is restricted to has at least "blank" values (all 0.0s) for every action
	void fillMapForAreaBounds()
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
	
	//Method returns the current hash map of Q-value information for all states
	Map<Position, Map<String, Double>> getStateData()
	{
		return mapOfQValues;
	}
	
	//Method returns the highest Q-value for the state at the provided position
	double getHighestQValueAtPosition(Position position)
	{
		double max = Double.NEGATIVE_INFINITY;
		
		for(String actionLabel : mapOfQValues.get(position).keySet())
		{
			if(mapOfQValues.get(position).get(actionLabel) > max)
				max = mapOfQValues.get(position).get(actionLabel);
		}
		
		return max;
	}
	
	//Method returns the Q-value of a specified action in a given state
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

	//Method returns the highest Q-value for the state, but if the "Chop Wood" action has a non-zero value its value is used instead
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
	
//	double computeStateValueFromQValues(State state)
//	{
//		String bestAction = computeActionFromQValues(state);
//		double max = Double.NEGATIVE_INFINITY;
//		
//		for(String actionLabel : mapOfQValues.get(state.getAgentPosition()).keySet())
//		{
//			double qVal = getQValue(state, actionLabel);
//			if(qVal > max)
//			{
//				max = qVal;
//				bestAction = actionLabel;
//			}
//		}
//		
//		return max;
//	}
	
	//Method returns the action with the highest Q-value for the state, or a random legal action if all Q-values are the same at this state (should only occur on the first entrance into a state)
	String computeActionFromQValues(State state)
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
	
	//Method returns an action with consideration for random action selection if the epsilon (exploration rate) value is relevant
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
	
	//Method updates Q-values for actions in given states based on values of the state resultant of the specified action given a reward value
	void update(State state, String actionLabel, State nextState, double reward)
	{
		double currentQVal = getQValue(state, actionLabel);
		double updateVal = learningRate * (reward + (discount * (computeValueFromQValues(nextState))) - currentQVal);	
		mapOfQValues.get(state.getAgentPosition()).replace(actionLabel, currentQVal + updateVal);
	}
}
