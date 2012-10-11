import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class Pot is used for keeping track of the pot size, both from the main pot and
 * the possible side pots, and the players that are involved in the side pots.
 */
public class Pot
{
	private Map<Robot, Integer> botBetSizes;
	private int totalPot;
	
	
	/**
	 * Creates a Pot object, used for keeping track of the pot for a specific hand.
	 * @param bots : integer array of bot IDs that play in the current hand
	 */
	public Pot(Collection<Robot> bots)
	{
		botBetSizes = new HashMap<Robot, Integer>();
		Iterator<Robot> botIterator = bots.iterator();
		while(botIterator.hasNext())
			botBetSizes.put(botIterator.next(), 0);
		
		totalPot = 0;
	}
	
	
	/**
	 * Stores the bet of a bot.
	 */
	public void addBet(Robot bot, int size)
	{
		botBetSizes.put(bot, botBetSizes.get(bot) + size);
		totalPot += size;
	}
	
	
	/**
	 * Calculates the amount of the pot that the winner receives. If it is about winning a side pot, the amount that
	 * the player betted more than the next better hand can be given in parameter 'maxSize'.
	 * @param bot : the winning bot
	 * @param maxSize : the maximal amount of chips per players bet that goes to this winner, any value <= 0 is
	 * interpreted as an unrestricted amount that may go to this player.
	 */
	public int getWinnersPot(Robot bot, int maxSize)
	{
		int winnersBet = botBetSizes.get(bot);
		if(maxSize > 0 && maxSize < winnersBet)
			winnersBet = maxSize;
		int winnersPot = 0;
		for (Entry<Robot, Integer> entry : botBetSizes.entrySet())
		{
		    Robot key = entry.getKey();
		    Integer value = entry.getValue();
		    
		    // If a player put in more chips than the winning player
			if(value > winnersBet)
			{
				botBetSizes.put(key, value - winnersBet);
				winnersPot += winnersBet;
				totalPot -= winnersBet;
			}
			// if a player put in equal or less chips than the winning player
			else
			{
				botBetSizes.put(key, 0);
				winnersPot += value;
				totalPot -= value;
			}
		}
		
		return winnersPot;
	}
	
	
	/**
	 * Calculates the amount of the pot that the given two winners receive. Returns an array of two integers pot sizes,
	 * the first one is the amount the first given bot wins, the second one is the amount the second bot wins. The main
	 * pot is split in half and if one bot put in more than the other, he gets this side pot back.
	 * @param bot1 : The first winning bot
	 * @param bot2 : The second winning bot
	 */
	public int[] getTwoWinnersPot(Robot bot1, Robot bot2)
	{
		int difference = botBetSizes.get(bot1) - botBetSizes.get(bot2);
		int[] botPots = new int[2];
		if(difference > 0)
			botPots[0] = getWinnersPot(bot1, difference);
		else if(difference < 0)
			botPots[1] = getWinnersPot(bot2, difference);
		int mainPot = getWinnersPot(bot1, difference);
		int mainPotHalf = mainPot / 2;
		botPots[1] += mainPotHalf;
		botPots[0] += mainPot - mainPotHalf;
		return botPots;
	}
	
	
	/**
	 * Returns whether the pot is empty or not
	 */
	public boolean isEmpty()
	{
		return totalPot == 0;
	}
	
	
	/**
	 * Returns the total size of the pot
	 */
	public int getCurrentPotSize()
	{
		return totalPot;
	}
}
