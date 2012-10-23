package nl.starapple.poker;
import java.util.ArrayList;

/**
 * Class that visualizes the replay of matches.
 */
public class MatchViewer
{
	private ArrayList<String> botNames;
	
	private final int[] chipValues = {1, 10, 100, 1000};
	
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
	
	public void collectPots(ArrayList<Integer> pots){}
	
	public void returnPot(String name, int amount){}
	
	
	/**
	 * Returns an array giving the number of chips per chip value of which the combined value equals the given amount
	 * of chips. Can be used to translate pots and bets into the corresponding set of chips. The returned array gives
	 * the amount of chips per chip value as listed in 'chipValues' from small to large.
	 * @param chips : the number of chips to be represented
	 */
	private int[] calculateChipsImage(int chips)
	{
		int index = chipValues.length;
		int[] chipsPerValue = new int[index--];
		while(index >= 0)
		{
			int number = chips / chipValues[index];
			chips -= number * chipValues[index];
			chipsPerValue[index--] = number;
		}
		return chipsPerValue;
	}
}
