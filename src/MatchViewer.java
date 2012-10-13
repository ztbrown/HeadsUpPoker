import java.util.ArrayList;

/**
 * Class that visualizes the replay of matches.
 */
public class MatchViewer
{
	private ArrayList<String> botNames;
	
	public MatchViewer()
	{
		
	}
	
	public void setupPlayers(ArrayList<String> names)
	{
		botNames = names;
	}
	
	public void updateRound(int number){}
	
	public void setStacks(ArrayList<String> names, ArrayList<Integer> stacks){}
	
	public void dealHands(ArrayList<String> names, ArrayList<String> hands){}
	
	public void playerFold(String botName){}
	
	public void playerCheck(String botName){}
	
	public void playerBet(String botName, int amount){}
	
	public void updateTable(String table){}
	
	public void collectPot(int totalPot){}
	
	public void returnPot(String name, int amount){}
}
