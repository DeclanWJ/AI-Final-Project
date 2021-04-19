package Scripts;

import java.util.ArrayList;

import org.osbot.rs07.script.Script;

public class Action 
{
	ArrayList<String> getLegalActions(State s)
	{
		ArrayList<String> legalActions = new ArrayList<String>();
		getMap().canReach();
		return legalActions;
	}
}
